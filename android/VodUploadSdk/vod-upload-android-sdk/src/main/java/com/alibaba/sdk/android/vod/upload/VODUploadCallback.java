/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload;

import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;

/**
 * Created by Leigang on 16/3/24.
 */
public abstract class VODUploadCallback {
    /**
     * 上传成功回调
     */
    public void onUploadSucceed(UploadFileInfo info) {
    }

    /**
     * 上传失败
     */
    public void onUploadFailed(UploadFileInfo info, String code, String message) {
    }

    /**
     * 回调上传进度
     *
     * @param uploadedSize 已上传字节数
     * @param totalSize    总共需要上传字节数
     */
    public void onUploadProgress(UploadFileInfo info, long uploadedSize, long totalSize) {
    }

    /**
     * token过期后，会回调这个接口
     * 可在这个回调中获取新的token，然后调用resumeWithToken继续上传
     */
    public void onUploadTokenExpired() {
    }

    /**
     * 上传过程中，状态由正常切换为异常时，会调用这个消息。
     */
    public void onUploadRetry(String code, String message) {
    }

    /**
     * 上传过程中，从异常中恢复，会调用这个消息。
     */
    public void onUploadRetryResume() {
    }

    /**
     * 开始上传一个文件时，会调用这个消息。
     */
    public void onUploadStarted(UploadFileInfo uploadFileInfo) {
    }

}
