/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload;

import com.alibaba.sdk.android.vod.upload.session.VodSessionCreateInfo;

public interface VODSVideoUploadClient {

    /**
     * 初始化短视频上传
     */
    void init();

    /**
     *
     * @param vodSessionCreateInfo 短视频点播上传配置，需要构造一个 VodSessionCreateInfo，涉及封面和视频上传的相关参数可以参考 https://help.aliyun.com/document_detail/55619.html
     *                             以及 https://help.aliyun.com/document_detail/55407.html
     * @param callback 回调
     * {@link VodSessionCreateInfo}
     * {@link VODSVideoUploadCallback}
     */
    void uploadWithVideoAndImg(VodSessionCreateInfo vodSessionCreateInfo , VODSVideoUploadCallback callback);


    /**
     * STSToken 过期刷新
     * @param accessKeyId
     * @param accessKeySecret
     * @param securityToken 安全token
     * @param expriedTime 过期时间
     */
    void refreshSTSToken(String accessKeyId,String accessKeySecret,String securityToken,String expriedTime);

    /**
     * 取消上传
     */
    void cancel();

    /**
     * 恢复上传
     */
    void resume();

    /**
     * 暂停上传
     */
    void pause();

    /**
     * 释放资源
     */
    void release();

    /**
     * 为打通log数据传递appversion
     * @param appVersion
     */
    void setAppVersion(String appVersion);

    /**
     * 配置上传地址region，默认为 cn-shanghai，不传则使用默认
     * @param region
     */
    void setRegion(String region);

    /**
     * 是否开启断点续传，默认为开启
     * @param enabled
     */
    void setRecordUploadProgressEnabled(boolean enabled);
}
