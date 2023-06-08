/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload;

import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;
import com.alibaba.sdk.android.vod.upload.model.VodUploadResult;

/**
 * 上传回调
 * 已失效，请直接使用 {@link VODUploadCallback}
 */
@Deprecated
public abstract class ResumableVODUploadCallback extends VODUploadCallback {

    /**
     * 点播上传回调
     * 已废弃，请使用 {@link VODUploadCallback#onUploadSucceed(com.alibaba.sdk.android.vod.upload.model.UploadFileInfo, com.alibaba.sdk.android.vod.upload.model.VodUploadResult)}
     * @param result 点播上传返回的视频或者图片信息
     */
    @Deprecated
    public void onUploadFinished(UploadFileInfo uploadFileInfo, VodUploadResult result){}

}
