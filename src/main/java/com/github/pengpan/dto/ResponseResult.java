package com.github.pengpan.dto;

import lombok.Data;

@Data
public class ResponseResult<T> {

    private int code;
    private T data;
    private String message;

}
