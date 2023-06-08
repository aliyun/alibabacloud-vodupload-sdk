/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload.internal;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.vod.upload.model.OSSConfig;
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;

import java.io.FileNotFoundException;


public interface OSSUploader {
    void init(OSSConfig ossConfig, OSSUploadListener listener);

    void setOSSClientConfiguration(ClientConfiguration configuration);

    void start(UploadFileInfo uploadFileInfo) throws FileNotFoundException;

    void cancel();

    void pause();

    void resume();

    void setRecordUploadProgressEnabled(boolean enabled);
}
