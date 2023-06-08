package com.aliyun.vod.log.core;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

public class LogService {
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    public LogService(String serviceName) {
        mHandlerThread = new HandlerThread(serviceName);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public void execute(Runnable runnable) {
        mHandler.post(runnable);
    }

    public void quit() {
        if(mHandlerThread != null) {
            if (Build.VERSION.SDK_INT >= 18) {
                mHandlerThread.quitSafely();
            } else {
                mHandlerThread.quit();
            }
        }
    }
}
