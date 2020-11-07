package com.github.pengpan.controller;

import com.github.pengpan.entity.Account;
import com.github.pengpan.service.AccountService;
import com.github.pengpan.util.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/account")
public class AccountController extends BaseController {

    @Resource
    private AccountService accountService;

    @RequestMapping("/add")
    public Result<Boolean> add(@RequestBody Account account) {
        return checkLogin(userId -> {
            account.setUserId(userId);
            return accountService.add(account);
        });
    }

    @RequestMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable long id) {
        return checkLogin(() -> accountService.delete(id));
    }

    @RequestMapping("/update")
    public Result<Boolean> update(@RequestBody Account account) {
        return checkLogin(userId -> {
            account.setUserId(userId);
            return accountService.update(account);
        });
    }

    @RequestMapping("/getList")
    public Result<List<Account>> getList(String mobile, int page, int limit) {
        return checkLogin(userId -> accountService.getList(userId, mobile, page, limit));
    }

    @RequestMapping("/get/{id}")
    public Result<Account> get(@PathVariable long id) {
        return checkLogin(() -> accountService.get(id));
    }

    @RequestMapping("/getAccountForMe")
    public Result<List<Account>> getAccountForMe() {
        return checkLogin(userId -> Result.success(accountService.getAccountByUserId(userId)));
    }

    @RequestMapping("/oneKeyLogin")
    public Result<Boolean> oneKeyLogin() {
        return checkLogin(() -> accountService.oneKeyLogin());
    }
}
