package com.github.pengpan.service;

import com.github.pengpan.dto.UserDto;
import com.github.pengpan.entity.User;
import com.github.pengpan.mapper.UserMapper;
import com.github.pengpan.util.Result;
import com.github.pengpan.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Objects;

@Slf4j
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    public Result<UserDto> login(HttpSession session, String mobile, String password) {
        if (StringUtil.isEmpty(mobile)) {
            return Result.error("手机号不能为空");
        }
        if (StringUtil.isEmpty(password)) {
            return Result.error("密码不能为空");
        }

        User user = userMapper.selectByMobile(mobile);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!Objects.equals(password, user.getPassword())) {
            return Result.error("密码错误");
        }

        session.setAttribute("user", user);

        UserDto dto = UserDto.builder().access_token(session.getId()).build();
        return Result.success(dto);
    }

    public Result<Object> logout(HttpSession session) {
        session.invalidate();
        return Result.success();
    }

}
