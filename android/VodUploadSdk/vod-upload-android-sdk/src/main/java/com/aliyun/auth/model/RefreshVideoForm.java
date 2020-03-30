/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.aliyun.auth.model;

/**
 * 刷新文件上传凭证的Response
 * Created by Mulberry on 2017/11/3.
 */
public class RefreshVideoForm {

    private String RequestId;
    private String UploadAuth;

    public String getRequestId() {
        return RequestId;
    }

    public void setRequestId(String requestId) {
        RequestId = requestId;
    }

    public String getUploadAuth() {
        return UploadAuth;
    }

    public void setUploadAuth(String uploadAuth) {
        UploadAuth = uploadAuth;
    }
}
