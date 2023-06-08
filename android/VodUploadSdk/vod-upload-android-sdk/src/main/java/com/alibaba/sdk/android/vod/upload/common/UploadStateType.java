/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload.common;

/**
 * 文件上传状态
 */
public enum UploadStateType {
    INIT,
    UPLOADING,
    SUCCESS,
    FAIlURE,
    CANCELED,
    PAUSING,
    PAUSED,
    DELETED
}
