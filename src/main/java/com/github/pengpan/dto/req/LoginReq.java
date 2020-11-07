package com.github.pengpan.dto.req;

import lombok.Data;

@Data
public class LoginReq {
    private String appKey;
    private String deviceId;
    private String phoneNum;
    private String password;
    private int type;
    private String verify_code;
}
