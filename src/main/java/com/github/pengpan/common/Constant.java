package com.github.pengpan.common;

public class Constant {

    public final static int HTTP_CONNECT_TIMEOUT = 5000;

    public final static int HTTP_READ_TIMEOUT = 10000;

    /**
     * 包名
     */
    public final static String APP_KEY = "com.aksofy.ykyzl";

    /**
     * IP池容量
     */
    public final static int IP_POOL_CAPACITY = 1000;

    /**
     * 是否开启代理
     */
    public final static boolean ENABLED_PROXY = false;

    /**
     * 获取代理的间隔（单位：秒）
     */
    public final static int GET_PROXY_INTERVAL = 2;

    /**
     * 每个任务刷号次数
     */
    public final static int BRUSH_LOOP_COUNT = 20;

    /**
     * 任务线程数
     */
    public final static int TASK_THREAD_COUNT = 8;

}
