/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload.internal;

/**
 * Created by Leigang on 16/3/24.
 */
public interface OSSUploadCallback {
    /**
     * 上传成功回调
     */
    void onUploadSucceed();

    /**
     * 上传失败
     * <p>
     * 异常码说明：
     * <p>
     * -1001: 文件不存在
     * -1002: IO异常
     * -1003: 其他本地异常如网络异常；
     * -1004: OSS服务异常；
     * -1005: 主动取消任务；
     * -1006: Token格式不合法，解析失败；
     */
    void onUploadFailed(String code, String message);

    /**
     * 回调上传进度
     *
     * @param uploadedSize 已上传字节数
     * @param totalSize    总共需要上传字节数
     */
    void onUploadProgress(long uploadedSize, long totalSize);

    /**
     * token过期后，会回调这个接口
     */
    void onUploadTokenExpired();
}
