/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload.internal;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.vod.upload.common.utils.MD5;
import com.alibaba.sdk.android.vod.upload.common.utils.SharedPreferencesUtil;
import com.alibaba.sdk.android.vod.upload.model.OSSUploadInfo;
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Mulberry on 2018/1/11.
 */
public class ResumeableSession {

    public final static String SHAREDPREFS_OSSUPLOAD = "OSS_UPLOAD_CONFIG";
    private static final String OSSUPLOAD_INFO = "OSS_UPLOAD_INFO";
    private WeakReference<Context> context;
    private boolean enabled = true;

    public ResumeableSession(Context context) {
        this.context = new WeakReference<Context>(context);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 文件是否需要断点续传
     *
     * @param filePath
     * @return
     */
    public synchronized String getResumeableFileVideoID(String filePath) {
        if (!enabled) {
            return null;
        }
        OSSUploadInfo ossUploadInfo =
                SharedPreferencesUtil.getUploadInfo(context.get(), SHAREDPREFS_OSSUPLOAD, filePath);
        OSSLog.logDebug("getResumeableFileInfo1" + ossUploadInfo);
        if (ossUploadInfo != null && MD5.checkMD5(ossUploadInfo.getMd5(), new File(filePath))) {
            return ossUploadInfo.getVideoID();
        }
        return null;
    }


    /**
     * 保存断点续传
     *
     * @param curFileInfo
     * @param videoId
     */
    public synchronized void saveResumeableFileInfo(UploadFileInfo curFileInfo, String videoId) {
        OSSUploadInfo ossUploadInfo = new OSSUploadInfo();
        ossUploadInfo.setBucket(curFileInfo.getBucket());
        ossUploadInfo.setEndpoint(curFileInfo.getEndpoint());
        ossUploadInfo.setObject(curFileInfo.getObject());
        ossUploadInfo.setMd5(MD5.calculateMD5(new File(curFileInfo.getFilePath())));
        ossUploadInfo.setVideoID(videoId);
        try {
            OSSLog.logDebug("saveUploadInfo" + ossUploadInfo, toString());
            SharedPreferencesUtil.saveUploadInfp(context.get(), SHAREDPREFS_OSSUPLOAD, curFileInfo.getFilePath(), ossUploadInfo);
        } catch (Exception e) {
            e.printStackTrace();
            OSSLog.logDebug("saveUploadInfo error");
        }
    }

    /**
     * 获取缓存待续传的文件，如缓存中存在需要续传的文件，将缓存中数据去除上传到指定的bucket,endpoint,objectkey
     *
     * @param curFileInfo
     */
    public synchronized UploadFileInfo getResumeableFileInfo(UploadFileInfo curFileInfo, String videoId) {
        if (!enabled) {
            return curFileInfo;
        }
        OSSUploadInfo ossUploadInfo =
                SharedPreferencesUtil.getUploadInfo(context.get(), SHAREDPREFS_OSSUPLOAD, curFileInfo.getFilePath());

        //step 3 如果存在并且匹配则取出bucket endpoint objectkey并保存
        if (!TextUtils.isEmpty(videoId)) {
            //图片永远不断点续传
            curFileInfo.setBucket(ossUploadInfo.getBucket());
            curFileInfo.setObject(ossUploadInfo.getObject());
            curFileInfo.setEndpoint(ossUploadInfo.getEndpoint());

        } else {
            OSSLog.logDebug("videoId cannot be null");
        }
        return curFileInfo;
    }

    public synchronized boolean deleteResumeableFileInfo(String filePath) {
        if (!enabled) {
            return true;
        }
        //step 4 上传完成需要根据MD5匹配删除值
        OSSUploadInfo ossUploadInfo = SharedPreferencesUtil.getUploadInfo(context.get(), SHAREDPREFS_OSSUPLOAD, filePath);
        if (ossUploadInfo != null && MD5.checkMD5(ossUploadInfo.getMd5(), new File(filePath))) {
            return SharedPreferencesUtil.clearUploadInfo(context.get(), SHAREDPREFS_OSSUPLOAD, filePath);
        }
        return false;
    }

    // 仅供断点续传开关关闭时调用
    public synchronized boolean deleteResumeableFileInfo(String filePath, boolean force) {
        if (!force) {
            if (!enabled) {
                return true;
            }
        }
        //step 4 上传完成需要根据MD5匹配删除值
        OSSUploadInfo ossUploadInfo = SharedPreferencesUtil.getUploadInfo(context.get(), SHAREDPREFS_OSSUPLOAD, filePath);
        if (ossUploadInfo != null && MD5.checkMD5(ossUploadInfo.getMd5(), new File(filePath))) {
            return SharedPreferencesUtil.clearUploadInfo(context.get(), SHAREDPREFS_OSSUPLOAD, filePath);
        }
        return false;
    }
}
