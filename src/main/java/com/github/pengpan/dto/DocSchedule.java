package com.github.pengpan.dto;

import lombok.Data;

@Data
public class DocSchedule {

    private String docLevel;
    private String admitRange;
    private String fee;
    private String scheduleItemCode;
    private boolean isEmpty;
    private long taskId;

}
