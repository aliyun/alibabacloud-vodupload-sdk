/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.aliyun.auth.model;

/**
 * 创建图片凭证的Response
 * Created by Mulberry on 2017/11/3.
 */
public class CreateImageForm {

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

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }

    public String getImageId() {
        return ImageId;
    }

    public void setImageId(String imageId) {
        ImageId = imageId;
    }

    private String RequestId;
    private String UploadAddress;
    private String UploadAuth;
    private String ImageURL;
    private String ImageId;

}
