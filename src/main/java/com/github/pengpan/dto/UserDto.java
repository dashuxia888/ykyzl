package com.github.pengpan.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private String access_token;
}
