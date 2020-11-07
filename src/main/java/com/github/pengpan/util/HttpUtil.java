package com.github.pengpan.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.kevinsawicki.http.HttpRequest;
import com.github.pengpan.common.Constant;
import com.github.pengpan.dto.ResponseResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpUtil {

    private final static ThreadLocal<String> session = new ThreadLocal<>();

    public static void setSession(String sessionId) {
        session.set(sessionId);
    }

    public static String getSession() {
        return session.get();
    }

    public static void removeSession() {
        session.remove();
    }

    public static <T, R> T doPost(String url, R req, TypeReference<T> respType) {
        return doPost(url, req, respType, null, false);
    }

    public static <T, R> T doPost(String url, R req, TypeReference<T> respType, String auth) {
        return doPost(url, req, respType, auth, false);
    }

    public static <T, R> T doPost(String url, R req, TypeReference<T> respType, boolean throwExp) {
        return doPost(url, req, respType, null, throwExp);
    }

    public static <T, R> T doPost(String url, R req, TypeReference<T> respType, String auth, boolean throwExp) {
        ResponseResult<T> resultClient = process(url, req, respType, auth);
        if (resultClient == null) {
            Assert.isTrue(!throwExp, "返回结果为空");
            return null;
        }

        int code = resultClient.getCode();
        if (code != 0) {
            Assert.isTrue(!throwExp, resultClient.getMessage());
            return null;
        }

        return resultClient.getData();
    }

    private static <T, R> ResponseResult<T> process(String url, R req, TypeReference<T> respType, String auth) {
        String body = process(url, req, auth);
        if (body == null) {
            return null;
        }

        try {
            return JSON.parseObject(body, new TypeReference<ResponseResult<T>>(respType.getType()) {
            });
        } catch (Exception e) {
            throw new RuntimeException("无法解析返回内容");
        }
    }

    private static <R> String process(String url, R req, String auth) {
        Assert.notEmpty(url, "url can't be empty");
        Assert.isTrue(url.startsWith("http"), "please check url");

        StringBuilder builder = new StringBuilder().append(System.lineSeparator());
        builder.append("-----------------------------------").append(System.lineSeparator());
        builder.append("|  url: ").append(url).append(System.lineSeparator());

        long start = System.currentTimeMillis();
        String result = null;

        try {
            HttpRequest post;

            IPPool.ProxyInfo proxyInfo = IPPool.consume();
            if (proxyInfo != null) {
                post = HttpRequest.post(url).useProxy(proxyInfo.getHost(), proxyInfo.getPort());
                builder.append("|proxy: ").append(JSON.toJSONString(proxyInfo)).append(System.lineSeparator());
            } else {
                post = HttpRequest.post(url);
            }

            post.header("Accept-Language", "zh-Hans-CN;q=1");
            post.header("Accept-Encoding", "br, gzip, deflate");
            post.header("Connection", "Keep-Alive");
            post.header("Keep-Alive", 300);
            post.userAgent("yi ke yuan zhong liu yi yuan/1.2.0 (iPhone; iOS 12.4.1; Scale/2.00)");
            post.accept("*/*");
            post.contentType(HttpRequest.CONTENT_TYPE_JSON, HttpRequest.CHARSET_UTF8);
            post.connectTimeout(Constant.HTTP_CONNECT_TIMEOUT);
            post.readTimeout(Constant.HTTP_READ_TIMEOUT);

            if (StringUtil.isNotEmpty(auth)) {
                post.header("Authorization", auth);
            }

            String sessionId = getSession();
            if (StringUtil.isNotEmpty(sessionId)) {
                post.header("Cookie", "session=" + sessionId);
                builder.append("|sessn: ").append(sessionId).append(System.lineSeparator());
            }

            if (req != null) {
                String body = JSON.toJSONString(req);
                builder.append("|  req: ").append(body).append(System.lineSeparator());
                post.send(body);
            }

            result = post.body();
        } catch (Exception e) {
            log.error("网络请求发生异常：{}", e.getMessage());
        } finally {
            long end = System.currentTimeMillis();
            String take = String.format("%s %s", end - start, "ms");
            builder.append("| resp: ").append(result).append(System.lineSeparator());
            builder.append("| cost: ").append(take).append(System.lineSeparator());
            builder.append("-----------------------------------");
            log.info(builder.toString());
        }

        return result;
    }
}
