package com.github.pengpan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pengpan.entity.Account;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AccountMapper extends BaseMapper<Account> {

    List<Account> selectByUserId(long userId);

    List<Account> selectAll();

    IPage<Account> selectPage(Page<Account> page,
                              @Param("userId") long userId,
                              @Param("mobile") String mobile);

    List<Account> selectByIds(@Param("accountIds") List<Long> accountIds);
}
