package com.github.pengpan.controller;

import com.github.pengpan.entity.User;
import com.github.pengpan.util.Result;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.function.LongFunction;
import java.util.function.Supplier;

public abstract class BaseController {

    @Resource
    private HttpServletRequest request;

    public User getUser() {
        return (User) request.getSession().getAttribute("user");
    }

    public Long getUserId() {
        User user = getUser();
        return user == null ? null : user.getId();
    }

    public <T> Result<T> checkLogin(Supplier<Result<T>> supplier) {
        return getUser() == null ? Result.invalid() : supplier.get();
    }

    public <T> Result<T> checkLogin(LongFunction<Result<T>> function) {
        Long userId = getUserId();
        return userId == null ? Result.invalid() : function.apply(userId);
    }
}
