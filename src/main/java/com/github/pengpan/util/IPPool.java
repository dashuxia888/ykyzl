package com.github.pengpan.util;

import com.alibaba.fastjson.JSON;
import com.github.kevinsawicki.http.HttpRequest;
import com.github.pengpan.common.Constant;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class IPPool {

    private final static LinkedBlockingDeque<ProxyInfo> storage;

    private static boolean isRun = false;

    static {
        storage = new LinkedBlockingDeque<>(Constant.IP_POOL_CAPACITY);
    }

    public static List<ProxyInfo> produce() {
        String url = "http://webapi.http.zhimacangku.com/getip?num=400&type=2&pro=110000&city=110105&yys=0&port=1&time=1&ts=1&ys=0&cs=0&lb=1&sb=0&pb=4&mr=1&regions=";

        String result = HttpRequest.get(url).body();
        ProxyResult proxyResult = JSON.parseObject(result, ProxyResult.class);

        Assert.isTrue("true".equals(proxyResult.getSuccess()), "Get proxy fail");

        List<ProxyResult.ProxyData> data = proxyResult.getData();
        if (data == null) {
            return new ArrayList<>();
        }
        return data.stream()
                .map(x -> ProxyInfo.instance(x.getIp(), x.getPort(), x.getExpire_time()))
                .collect(Collectors.toList());
    }

    public static void batchProduce() {
        if (isRun) {
            return;
        }
        isRun = true;
        while (true) {
            if (!Constant.ENABLED_PROXY) {
                break;
            }
            try {
                for (ProxyInfo p : produce()) {
                    storage.put(p);
                }
                TimeUnit.SECONDS.sleep(Constant.GET_PROXY_INTERVAL);
            } catch (Exception e) {
                isRun = false;
                log.error("获取IP代理发生异常：{}", e.getMessage());
                break;
            }
        }
    }

    public static ProxyInfo consume() {
        ProxyInfo p;
        while (true) {
            if ((p = storage.poll()) == null || p.getExpire().after(new Date())) {
                break;
            }
        }
        return p;
    }

    @Data
    public static class ProxyInfo {
        private String host;
        private int port;
        private Date expire;

        private ProxyInfo(String host, int port, Date expire) {
            this.host = host;
            this.port = port;
            this.expire = expire;
        }

        public static ProxyInfo instance(String host, int port, Date expire) {
            return new ProxyInfo(host, port, expire);
        }
    }

    @Data
    public static class ProxyResult {
        private int code;
        private String success;
        private String msg;
        private List<ProxyData> data;

        @Data
        public static class ProxyData {
            private String ip;
            private int port;
            private Date expire_time;
        }

    }

    public static void main(String[] args) {
        ProxyInfo produce = IPPool.produce().get(0);

        String body = HttpRequest.get("https://www.baidu.com")
                .useProxy(produce.getHost(), produce.getPort())
                .body();

        log.info(body);
    }
}
