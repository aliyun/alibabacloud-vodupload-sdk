/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.vod.common.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

public abstract class WeakReferenceHandler<T> extends Handler {
    private WeakReference<T> mReference;

    public WeakReferenceHandler(Looper looper, T reference) {
        super(looper);
        mReference = new WeakReference<T>(reference);
    }

    @Override
    public void handleMessage(Message msg) {
        T reference = mReference.get();
        if (reference == null)
            return;
        handleMessage(reference, msg);
    }

    protected abstract void handleMessage(T reference, Message msg);
}