/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload;

public interface VODSVideoUploadCallback {

    /**
     * 上传成功回调
     */
    void onUploadSucceed(String videoId,String imageUrl);

    /**
     * 上传失败
     */
    void onUploadFailed(String code, String message);

    /**
     * 上传进度回调
     */
    void onUploadProgress(long uploadedSize, long totalSize);

    /**
     * STSToken过期
     */
    void onSTSTokenExpried();

    /**
     * 上传过程中，状态由正常切换为异常时，会调用这个消息。
     *
     */
    void onUploadRetry(String code, String message);

    /**
     * 上传过程中，从异常中恢复，会调用这个消息。
     *
     */
    void onUploadRetryResume();

}
