/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload.model;

import com.alibaba.sdk.android.vod.upload.common.UploadStateType;
import com.alibaba.sdk.android.vod.upload.common.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 上传文件信息，由SDK封装透出，callback给调用者
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

    public VodUploadResult result;

    /**
     * 是否分片上传
     */
    private boolean isMultipart = false;
    /**
     * 文件长度，默认-1表示还未合成完成
     */
    private long fileLength = -1;
    /**
     * 片段信息列表
     */
    private List<FilePartInfo> partInfoList = new ArrayList<>();

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

    /**
     * support file path (/sdcard/xxx/xxx) or uri path (content://xxxx/xxxx)
     * @param filePath file path or uri path
     */
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

    /**
     * 是否分片上传
     * @return 是否分片
     */
    public boolean isMultipart() {
        return isMultipart;
    }

    /**
     * 设置是否分片上传
     *
     * @param multipart 是否分片
     */
    public void setMultipart(boolean multipart) {
        isMultipart = multipart;
    }

    /**
     * 获取文件长度
     *
     * @return
     */
    public long getFileLength() {
        return fileLength;
    }

    /**
     * 设置文件长度
     *
     * @param fileLength 文件长度
     */
    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    /**
     * 获取分片信息
     *
     * @return 分片信息
     */
    public List<FilePartInfo> getPartInfoList() {
        return partInfoList;
    }

    /**
     * 设置分片信息
     *
     * @param partInfoList 分片信息
     */
    public void setPartInfoList(List<FilePartInfo> partInfoList) {
        this.partInfoList = partInfoList;
    }
}
