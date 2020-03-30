/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload.model;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Leigang on 16/10/19.
 */
public class VodInfo {
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String desc;
    /**
     * 分类ID
     */
    private Integer cateId;
    /**
     * 标签
     */
    private List<String> tags;
    /**
     * 自定义用户数据
     */
    private String userData;
    /**
     * 封面地址
     */
    private String coverUrl;
    /**
     * 是否开启工作流
     */
    private Boolean isProcess = true;
    /**
     * 是否显示水印，仅仅是是要MTS时使用，单纯点播服务是包含在转码模板中的
     */
    private Boolean isShowWaterMark;
    /**
     * 转码优先级
     */
    private Integer priority;

    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件大小
     */
    private String fileSize;

    public Boolean getIsProcess() {
        return isProcess;
    }

    public void setIsProcess(Boolean isProcess) {
        this.isProcess = isProcess;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    // {"Vod":{"Title":"zoe2","Description":"zoe2","CateId":"19","Tags":"tag1,tag2","IsProcess":"true","UserData":"user defined info here"}}
    public String toVodJsonStringWithBase64() {
        JSONObject object = new JSONObject();
        try {
            JSONObject obj = new JSONObject();
            obj.put("Title", getTitle());
            obj.put("Description", getDesc());
            obj.put("CateId", String.valueOf(getCateId()));
            obj.put("CoverUrl", getCoverUrl());
            obj.put("IsProcess", getIsProcess().toString());
            String tags = "";
            if (getTags() != null && getTags().size() > 0) {
                tags = getTags().toString();
                tags = tags.substring(1, tags.length() - 1);
            }
            obj.put("Tags", tags);
            if (null != isShowWaterMark || null != priority) {
                JSONObject userData = new JSONObject();
                if (null != isShowWaterMark && isShowWaterMark) {
                    userData.put("IsShowWaterMark", "true");
                } else {
                    userData.put("IsShowWaterMark", "false");
                }
                userData.put("Priority", String.valueOf(getPriority()));
                obj.put("UserData", userData);
            } else {
                obj.put("UserData", getUserData());
            }
            object.put("Vod", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Base64.encodeToString(object.toString().getBytes(), Base64.NO_WRAP);
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Integer getCateId() {
        return cateId;
    }

    public void setCateId(Integer cateId) {
        this.cateId = cateId;
    }

    public Boolean getIsShowWaterMark() {
        return isShowWaterMark;
    }

    public void setIsShowWaterMark(Boolean isShowWaterMark) {
        this.isShowWaterMark = isShowWaterMark;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
