package com.github.pengpan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pengpan.common.cache.key.CacheKey;
import com.github.pengpan.dto.AccountAuth;
import com.github.pengpan.entity.Account;
import com.github.pengpan.mapper.AccountMapper;
import com.github.pengpan.util.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Resource
    private CoreService coreService;
    @Resource
    private RedisManager redisManager;
    @Resource
    private AccountMapper accountMapper;

    public String getAuth(@NonNull String mobile) {
        if (StringUtil.isEmpty(mobile)) {
            return StringUtil.EMPTY;
        }

        String key = CacheKey.accountAuth(mobile);
        AccountAuth accountAuth = redisManager.get(key, AccountAuth.class);

        return Optional.ofNullable(accountAuth).map(AccountAuth::getAuth).orElse(StringUtil.EMPTY);
    }

    public boolean initAuth(String mobile, String password) {
        if (StringUtil.isNotEmpty(getAuth(mobile))) {
            return true;
        }

        Result<AccountAuth> loginData = coreService.login(mobile, password);

        AccountAuth accountAuth = loginData.getData();
        if (accountAuth == null) {
            return false;
        }

        String token = accountAuth.getToken();
        if (StringUtil.isEmpty(token)) {
            return false;
        }

        String base64Token = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        String auth = "Basic " + base64Token.replace("=", "6");

        String key = CacheKey.accountAuth(mobile);

        accountAuth.setAuth(auth);
        redisManager.set(key, accountAuth, 3, TimeUnit.HOURS);

        // 初始化用户信息
        coreService.viewPatientList(mobile);
        return true;
    }

    public boolean initAuthLoop(String mobile, String password) {
        AtomicInteger loginCount = new AtomicInteger(3);

        boolean result;
        while (true) {
            result = initAuth(mobile, password);
            if (result) {
                break;
            }
            if (loginCount.incrementAndGet() <= 0) {
                break;
            }
        }
        return result;
    }

    public Result<Boolean> add(Account account) {
        if (account == null) {
            return Result.error("非法请求");
        }
        if (StringUtil.isEmpty(account.getMobile())) {
            return Result.error("手机号不能为空");
        }
        if (StringUtil.isEmpty(account.getPassword())) {
            return Result.error("密码不能为空");
        }

        accountMapper.insert(account);
        return Result.success(true);
    }

    public Result<Boolean> delete(long id) {
        Assert.isLegalId(id);
        accountMapper.deleteById(id);
        return Result.success(true);
    }

    public Result<Boolean> update(Account account) {
        if (account == null) {
            return Result.error("非法请求");
        }
        if (StringUtil.isEmpty(account.getMobile())) {
            return Result.error("手机号不能为空");
        }
        if (StringUtil.isEmpty(account.getPassword())) {
            return Result.error("密码不能为空");
        }

        Account record = Account.builder()
                .mobile(account.getMobile())
                .password(account.getPassword())
                .id(account.getId())
                .build();
        accountMapper.updateById(record);
        return Result.success(true);
    }

    public Result<List<Account>> getList(long userId, String mobile, int page, int limit) {
        IPage<Account> p = accountMapper.selectPage(new Page<>(page, limit), userId, mobile);

        List<Account> data = p.getRecords().stream()
                .peek(x -> x.setOnline(StringUtil.isNotEmpty(getAuth(x.getMobile()))))
                .collect(Collectors.toList());

        return Result.success(data, p.getTotal());
    }

    public Result<Account> get(long id) {
        Account account = findById(id);
        if (account == null) {
            return Result.error("账户不存在");
        }
        return Result.success(account);
    }

    public Account findById(long id) {
        Assert.isLegalId(id);
        return accountMapper.selectById(id);
    }

    public List<Account> getAccountByUserId(long userId) {
        return accountMapper.selectByUserId(userId);
    }

    public Result<Boolean> oneKeyLogin() {
        List<Account> allAccount = accountMapper.selectAll();
        for (Account account : allAccount) {
            initAuth(account.getMobile(), account.getPassword());
        }
        return Result.success();
    }

    public List<Account> selectByIds(List<Long> accountIds) {
        if (CollUtil.isEmpty(accountIds)) {
            return new ArrayList<>();
        }
        return accountMapper.selectByIds(accountIds);
    }
}
