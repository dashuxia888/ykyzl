package com.github.pengpan.controller;

import com.github.pengpan.dto.UserDto;
import com.github.pengpan.service.UserService;
import com.github.pengpan.util.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Resource
    private UserService userService;

    @RequestMapping("/login")
    public Result<UserDto> login(HttpSession session,
                                 @RequestParam("username") String mobile,
                                 @RequestParam String password) {
        return userService.login(session, mobile, password);
    }

    @RequestMapping("/logout")
    public Result<Object> logout(HttpSession session) {
        return userService.logout(session);
    }
}
