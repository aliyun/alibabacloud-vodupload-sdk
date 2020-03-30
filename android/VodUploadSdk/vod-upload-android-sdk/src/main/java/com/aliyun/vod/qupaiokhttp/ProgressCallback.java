/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.aliyun.vod.qupaiokhttp;

interface ProgressCallback {
    void updateProgress(int progress, long networkSpeed, boolean done);
}
