DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
 `mobile` varchar(11) NOT NULL COMMENT '手机号',
 `password` varchar(50) NOT NULL COMMENT '密码',
 `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 PRIMARY KEY (`id`),
 UNIQUE KEY `uk_mobile` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

INSERT INTO t_user(mobile, password) VALUES('admin', '12345');

DROP TABLE IF EXISTS `t_account`;
CREATE TABLE `t_account` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
 `user_id` bigint(20) NOT NULL COMMENT '用户ID',
 `mobile` varchar(11) NOT NULL COMMENT '账号',
 `password` varchar(50) NOT NULL COMMENT '密码',
 `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 PRIMARY KEY (`id`),
 KEY `idx_user_id` (`user_id`),
 UNIQUE KEY `uk_mobile` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='账户表';

DROP TABLE IF EXISTS `t_task`;
CREATE TABLE `t_task` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
 `account_id` bigint(20) NOT NULL COMMENT '账户ID',
 `user_id` bigint(20) NOT NULL COMMENT '用户ID',
 `user_name` varchar(20) DEFAULT NULL COMMENT '用户账号',
 `dept_id` varchar(20) NOT NULL COMMENT '科室ID',
 `dept_name` varchar(20) DEFAULT NULL COMMENT '科室名',
 `doc_id` varchar(20) NOT NULL COMMENT '医生ID',
 `doc_name` varchar(20) DEFAULT NULL COMMENT '医生名',
 `remark` varchar(200) DEFAULT NULL COMMENT '备注',
 `schedule_item_code` varchar(50) DEFAULT NULL COMMENT '排班号',
 `status` tinyint(2) DEFAULT 0 COMMENT '状态（0：预约中，1：成功，2：失败）',
 `target_date` date NOT NULL COMMENT '挂号日期',
 `doc_level` varchar(20) DEFAULT NULL COMMENT '医生级别',
 `reg_fee` varchar(20) DEFAULT NULL COMMENT '挂号费',
 `admit_range` varchar(20) DEFAULT NULL COMMENT '就诊时段',
 `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='任务表';
