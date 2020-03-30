/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.aliyun.auth.model;

/**
 * 创建视频凭证的Response
 * Created by Mulberry on 2017/11/3.
 */
public class CreateVideoForm {
    private String RequestId;
    private String UploadAddress;
    private String UploadAuth;
    private String VideoId;

    public String getRequestId() {
        return RequestId;
    }

    public void setRequestId(String requestId) {
        RequestId = requestId;
    }

    public String getUploadAddress() {
        return UploadAddress;
    }

    public void setUploadAddress(String uploadAddress) {
        UploadAddress = uploadAddress;
    }

    public String getUploadAuth() {
        return UploadAuth;
    }

    public void setUploadAuth(String uploadAuth) {
        UploadAuth = uploadAuth;
    }

    public String getVideoId() {
        return VideoId;
    }

    public void setVideoId(String videoId) {
        VideoId = videoId;
    }
}
