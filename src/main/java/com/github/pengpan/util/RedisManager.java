package com.github.pengpan.util;

import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public final class RedisManager {

    @Resource
    private StringRedisTemplate template;

    public String get(String key) {
        return template.opsForValue().get(key);
    }

    public <T> T get(String key, Class<T> type) {
        String value = get(key);
        return JSON.parseObject(value, type);
    }

    public <T> List<T> getList(String key, Class<T> type) {
        String value = template.opsForValue().get(key);
        return JSON.parseArray(value, type);
    }

    public <T> void set(String key, T value, long expire) {
        set(key, value, expire, TimeUnit.MICROSECONDS);
    }

    public <T> void set(String key, T value, long expire, TimeUnit unit) {
        String val = value instanceof String ? String.valueOf(value) : JSON.toJSONString(value);
        template.opsForValue().set(key, val, expire, unit);
    }

    public void set(String key, String hKey, String value, long expire, TimeUnit unit) {
        Assert.notEmpty(key);
        Assert.notEmpty(hKey);
        template.opsForHash().put(key, hKey, value);
        template.expire(key, expire, unit);
    }

    public String get(String key, String hKey) {
        Assert.notEmpty(key);
        Assert.notEmpty(hKey);
        return (String) template.opsForHash().get(key, hKey);
    }

    public void del(String key) {
        Assert.notEmpty(key);
        template.delete(key);
    }

}