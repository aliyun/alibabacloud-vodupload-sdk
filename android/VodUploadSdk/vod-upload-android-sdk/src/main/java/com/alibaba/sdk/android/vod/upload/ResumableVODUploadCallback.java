/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload;

import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;
import com.alibaba.sdk.android.vod.upload.model.VodUploadResult;

/**
 * Created by Mulberry on 2018/1/4.
 */

public abstract class ResumableVODUploadCallback extends VODUploadCallback {

    /**
     * 点播上传回调
     *
     * @param result 点播上传返回的视频或者图片信息
     */
    public void onUploadFinished(UploadFileInfo uploadFileInfo, VodUploadResult result) {
    }

}
