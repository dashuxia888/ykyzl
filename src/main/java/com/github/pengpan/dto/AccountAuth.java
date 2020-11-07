package com.github.pengpan.dto;

import lombok.Data;

@Data
public class AccountAuth {

    /**
     * 账号
     */
    private String mobile;

    /**
     * 令牌
     */
    private String token;

    /**
     * 授权
     */
    private String auth;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 会话ID
     */
    private String sessionId;
}
