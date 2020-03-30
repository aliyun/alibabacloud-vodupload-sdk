/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload.internal;

/**
 * Created by Leigang on 16/6/30.
 */
public enum OSSUploadRetryType {
    ShouldNotRetry,
    ShouldRetry,
    ShouldGetSTS
}
