/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload.common;

/**
 * Created by Leigang on 16/6/27.
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
