/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.vod.qupaiokhttp;

interface ProgressCallback {
    void updateProgress(int progress, long networkSpeed, boolean done);
}
