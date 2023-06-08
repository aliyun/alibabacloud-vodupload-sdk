/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload;

import com.alibaba.sdk.android.vod.upload.exception.VODErrorCode;
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;
import com.alibaba.sdk.android.vod.upload.model.VodUploadResult;

/**
 * 点播上传回调
 */
public abstract class VODUploadCallback {

    /**
     * 已废弃，请使用 {@link VODUploadCallback#onUploadSucceed(com.alibaba.sdk.android.vod.upload.model.UploadFileInfo, com.alibaba.sdk.android.vod.upload.model.VodUploadResult)} 代替
     * 上传成功回调
     * @param info UploadFileInfo 见{@link UploadFileInfo}
     */
    @Deprecated
    public void onUploadSucceed(UploadFileInfo info) {};

    /**
     * 上传成功回调
     * @param info UploadFileInfo 见{@link UploadFileInfo}
     * @param result 回调结果 见{@link VodUploadResult}
     */
    public void onUploadSucceed(UploadFileInfo info, VodUploadResult result){};

    /**
     * 上传失败
     * @param info UploadFileInfo 见{@link UploadFileInfo}
     * @param code 错误码 见{@link VODErrorCode}
     * @param message 错误信息
     */
    public void onUploadFailed(UploadFileInfo info, String code, String message){};

    /**
     * 回调上传进度
     *
     * @param info UploadFileInfo 见{@link UploadFileInfo}
     * @param uploadedSize 已上传字节数
     * @param totalSize    总共需要上传字节数
     */
    public void onUploadProgress(UploadFileInfo info, long uploadedSize, long totalSize){}

    /**
     * token过期后，会回调这个接口
     * 可在这个回调中获取新的token，然后调用resumeWithToken继续上传
     */
    public void onUploadTokenExpired(){};

    /**
     * 上传过程中，状态由正常切换为异常时，会调用这个消息。
     *
     */
    public void onUploadRetry(String code, String message){};

    /**
     * 上传过程中，从异常中恢复，会调用这个消息。
     *
     */
    public void onUploadRetryResume(){};

    /**
     * 开始上传一个文件时，会调用这个消息。
     *
     */
    public void onUploadStarted(UploadFileInfo uploadFileInfo){};
}
