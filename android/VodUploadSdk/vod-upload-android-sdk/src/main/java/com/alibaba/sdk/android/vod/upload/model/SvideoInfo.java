/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload.model;

import java.util.List;

/**
 * 短视频上传点播VideoInfo
 */
public class SvideoInfo {
    private String title;
    private String desc;
    private List<String> tags;
    private Integer cateId;
    private boolean isProcess = true;
    private boolean isShowWaterMark = false;
    private int priority = 6;
    /**
     * 自定义用户数据
     */
    private String userData;

    public String getTitle() {
        return title;
    }

    /**
     * 设置标题
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 设置描述信息
     * @param desc
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<String> getTags() {
        return tags;
    }

    /**
     * 设置标签
     * @param tags
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getCateId() {
        return cateId;
    }

    /**
     * 设置分类ID
     * @param cateId
     */
    public void setCateId(Integer cateId) {
        this.cateId = cateId;
    }

    public boolean isProcess() {
        return isProcess;
    }

    public void setProcess(boolean process) {
        isProcess = process;
    }

    public boolean isShowWaterMark() {
        return isShowWaterMark;
    }

    public void setShowWaterMark(boolean showWaterMark) {
        isShowWaterMark = showWaterMark;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }
}
