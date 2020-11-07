package com.github.pengpan.util;

import lombok.Data;

@Data
public class Result<T> {

    private boolean success;
    private int code;
    private String msg;
    private T data;
    private long count;

    public Result(boolean success, int code, String msg, T data) {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result(boolean success, int code, String msg, T data, int count) {
        this(success, code, msg, data);
        this.count = count;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, 0, "成功", data);
    }

    public static <T> Result<T> success(T data, long count) {
        Result<T> result = success(data);
        result.setCount(count);
        return result;
    }

    public static <T> Result<T> error() {
        return error("服务器繁忙");
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(false, -1, msg, null);
    }

    public static <T> Result<T> invalid() {
        return new Result<>(false, 1001, "登录失效", null);
    }

    public boolean isSuccess() {
        return getCode() == 0;
    }

    public boolean isError() {
        return !isSuccess();
    }
}
