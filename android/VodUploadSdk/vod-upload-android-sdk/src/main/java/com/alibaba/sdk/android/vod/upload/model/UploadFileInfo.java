/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload.model;

import com.alibaba.sdk.android.vod.upload.common.UploadStateType;
import com.alibaba.sdk.android.vod.upload.common.utils.StringUtil;

/**
 * Created by Leigang on 16/3/2.
 */
public class UploadFileInfo {
    public static final int UPLOAD_FILE_TYPE_IMAGE = 0;
    public static final int UPLOAD_FILE_TYPE_VIDEO = 1;
    private String filePath;
    private String endpoint;
    private String bucket;
    private String object;
    private VodInfo vodInfo;
    private UploadStateType status;
    // for report use
    private int fileType = UPLOAD_FILE_TYPE_VIDEO;

    public void setFileType(int type) {
        fileType = type;
    }

    public int getFileType() {
        return fileType;
    }

    public UploadStateType getStatus() {
        return status;
    }

    public void setStatus(UploadStateType status) {
        this.status = status;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public boolean equals(UploadFileInfo uploadFileInfo) {
        if (null == uploadFileInfo) {
            return false;
        }

        if (StringUtil.isEmpty(uploadFileInfo.filePath) || !uploadFileInfo.filePath.equals(filePath)) {
            return false;
        }

        if (StringUtil.isEmpty(uploadFileInfo.endpoint) || !uploadFileInfo.endpoint.equals(endpoint)) {
            return false;
        }

        if (StringUtil.isEmpty(uploadFileInfo.bucket) || !uploadFileInfo.bucket.equals(bucket)) {
            return false;
        }

        if (StringUtil.isEmpty(uploadFileInfo.object) || !uploadFileInfo.object.equals(object)) {
            return false;
        }

        if (StringUtil.isEmpty(uploadFileInfo.object) || !uploadFileInfo.object.equals(object)) {
            return false;
        }

        return true;
    }

    public VodInfo getVodInfo() {
        return vodInfo;
    }

    public void setVodInfo(VodInfo vodInfo) {
        this.vodInfo = vodInfo;
    }
}
