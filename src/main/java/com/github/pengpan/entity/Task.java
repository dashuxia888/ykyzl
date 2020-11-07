package com.github.pengpan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 科室ID
     */
    private String deptId;

    /**
     * 科室名
     */
    private String deptName;

    /**
     * 医生ID
     */
    private String docId;

    /**
     * 医生名
     */
    private String docName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 排班号
     */
    private String scheduleItemCode;

    /**
     * 状态（0：预约中，1：成功，2：失败）
     */
    private Integer status;

    /**
     * 挂号日期
     */
    private String targetDate;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 医生级别
     */
    private String docLevel;

    /**
     * 挂号费
     */
    private String regFee;

    /**
     * 就诊时段
     */
    private String admitRange;

}
