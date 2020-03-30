/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.aliyun.vod.qupaiokhttp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpTaskHandler {

    /**
     * 正在请求的任务集合
     */
    private static Map<String, List<OkHttpTask>> httpTaskMap;
    /**
     * 单例请求处理器
     */
    private static HttpTaskHandler httpTaskHandler = null;

    private HttpTaskHandler() {
        httpTaskMap = new ConcurrentHashMap<>();
    }

    /**
     * 获得处理器实例
     */
    public static HttpTaskHandler getInstance() {
        if (httpTaskHandler == null) {
            httpTaskHandler = new HttpTaskHandler();
        }
        return httpTaskHandler;
    }

    /**
     * 移除KEY
     *
     * @param key
     */
    public synchronized void removeTask(String key) {
        if (httpTaskMap.containsKey(key)) {
            //移除对应的Key
            httpTaskMap.remove(key);
        }
    }

    /**
     * 将请求放到Map里面
     *
     * @param key
     * @param task
     */
    synchronized void addTask(String key, OkHttpTask task) {
        if (httpTaskMap.containsKey(key)) {
            List<OkHttpTask> tasks = httpTaskMap.get(key);
            tasks.add(task);
            httpTaskMap.put(key, tasks);
        } else {
            List<OkHttpTask> tasks = new ArrayList<>();
            tasks.add(task);
            httpTaskMap.put(key, tasks);
        }
    }

    /**
     * 判断是否存在
     *
     * @param key
     * @return
     */
    boolean contains(String key) {
        return httpTaskMap.containsKey(key);
    }
}
