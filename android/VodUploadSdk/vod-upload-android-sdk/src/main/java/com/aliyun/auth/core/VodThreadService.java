/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.aliyun.auth.core;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;


public class VodThreadService {
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    public VodThreadService(String serviceName) {
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
