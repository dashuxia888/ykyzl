package com.github.pengpan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pengpan.entity.User;

public interface UserMapper extends BaseMapper<User> {

    User selectByMobile(String mobile);
}
