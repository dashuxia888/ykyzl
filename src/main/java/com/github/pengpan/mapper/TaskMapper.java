package com.github.pengpan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pengpan.entity.Task;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskMapper extends BaseMapper<Task> {

    IPage<Task> selectPage(Page<Task> page,
                           @Param("userId") long userId,
                           @Param("userName") String userName);

    List<Task> selectByTargetDate(String targetDate);

    List<Task> findAllTask();

}
