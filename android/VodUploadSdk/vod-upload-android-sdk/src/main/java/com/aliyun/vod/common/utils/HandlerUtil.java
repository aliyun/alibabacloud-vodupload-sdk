/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.vod.common.utils;

import static android.os.Build.VERSION_CODES.*;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public
class HandlerUtil {

    private static @TargetApi(JELLY_BEAN_MR2)
    void quitSafely18(Looper looper) {
        looper.quitSafely();
    }

    public static
    void quitSafely(Handler handler) {
        final Looper looper = handler.getLooper();

        if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR2) {
            quitSafely18(looper);
            return;
        }

        handler.post(new Runnable() {

            @Override
            public void run() {
                looper.quit();
            }
        });
    }

    private static @TargetApi(JELLY_BEAN_MR2)
    void quitSafely18(HandlerThread thread) {
        thread.quitSafely();
    }

    public static
    void quitSafely(HandlerThread thread) {
        if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR2) {
            quitSafely18(thread);
            return;
        }

        quitSafely(new Handler(thread.getLooper()));
    }
}
