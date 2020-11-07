package com.github.pengpan.service;

import com.github.pengpan.common.Constant;
import com.github.pengpan.common.cache.key.CacheKey;
import com.github.pengpan.entity.Account;
import com.github.pengpan.entity.Task;
import com.github.pengpan.util.IPPool;
import com.github.pengpan.util.RedisManager;
import com.github.pengpan.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RegistrationService {

    @Resource
    private AccountService accountService;
    @Resource
    private TaskService taskService;
    @Resource
    private RedisManager redisManager;

    public void initIPPool() {
        log.info("==========初始化IP池==========");

        new Thread(IPPool::batchProduce, "IPPoolThread").start();
    }

    public void initLogin(List<Account> accounts) {
        log.info("==========初始化登录==========");

        ExecutorService threadPool = Executors.newFixedThreadPool(accounts.size());
        List<Future<Boolean>> futures = accounts.stream()
                .map(account ->
                        threadPool.submit(() ->
                                accountService.initAuth(account.getMobile(), account.getPassword())))
                .collect(Collectors.toList());

        threadPool.shutdown();

        long successCount = futures.stream().filter(f -> {
            try {
                return f.get();
            } catch (Exception e) {
                return false;
            }
        }).count();

        log.info("Login success number: {}/{}", successCount, accounts.size());
    }

    public void startTask(List<Task> tasks, List<Account> accounts) {
        log.info("==========开抢啦==========");

        Map<Long, String> accountMap = accounts.stream().collect(Collectors.toMap(Account::getId, Account::getMobile));

        int loopSize = Constant.TASK_THREAD_COUNT;

        ExecutorService threadPool = Executors.newFixedThreadPool(tasks.size() * loopSize);
        for (int i = 0; i < loopSize; i++) {
            for (Task task : tasks) {
                threadPool.execute(() -> taskService.startTask(task, accountMap.get(task.getAccountId())));
            }
        }
        threadPool.shutdown();
    }

    public Result<String> manualStartTask(long taskId) {
        Task task = taskService.findById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }

        Integer status = task.getStatus();
        if (status == 1) {
            return Result.error("该任务已成功挂号");
        }

        Long accountId = task.getAccountId();
        Account account = accountService.findById(accountId);
        if (account == null) {
            return Result.error("账户不存在");
        }

        String key = CacheKey.manualTask(taskId);
        String s = redisManager.get(key);
        if (s != null) {
            return Result.error("存在挂号任务");
        }

        taskService.manualStartTask(task, account);

        return Result.success("创建成功，任务将在一小时后自动结束");
    }

    public Result<String> manualStopTask(long taskId) {
        String key = CacheKey.manualTask(taskId);
        redisManager.del(key);
        return Result.success();
    }

}
