/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload.internal;


public enum OSSUploadRetryType {
    ShouldNotRetry,
    ShouldRetry,
    ShouldGetSTS
}
