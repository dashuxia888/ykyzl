package com.github.pengpan.dto;

import lombok.Data;

@Data
public class TaskDto {

    private long id;
    private long userId;
    private long accountId;
    private String deptId;
    private String docId;
    private int timeSlot;
    private String scheduleItemCode;

}
