/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.aliyun.vod.log.report;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.aliyun.vod.common.global.AliyunTag;
import com.aliyun.vod.common.global.Version;
import com.aliyun.vod.common.utils.DeviceUtils;
import com.aliyun.vod.common.utils.MD5Util;
import com.aliyun.vod.common.utils.ManifestUtils;
import com.aliyun.vod.common.utils.ProcessUtil;
import com.aliyun.vod.log.core.AliyunLogCommon;
import com.aliyun.vod.log.struct.AliyunLogKey;
import com.aliyun.vod.log.util.UUIDGenerator;
import com.aliyun.vod.qupaiokhttp.BaseHttpRequestCallback;
import com.aliyun.vod.qupaiokhttp.HttpRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import okhttp3.Headers;

/**
 * @ClassName: AliyunUploadProgressReporter
 * @CreateDate: 2018/11/16 上午11:19
 * @Description: for upload progress report
 * @Version: 1.0
 */
public class AliyunUploadProgressReporter {

    private static final String TAG = AliyunUploadProgressReporter.class.getSimpleName();

    private String mAction = "ReportUploadProgress";
    private String mSource = "AndroidSDK";
    // uuid
    private String mClientId = AliyunLogCommon.UUID;
    private String mBusinessType = "UploadVideo";
    private String mTerminalType = "APhone";
    // brand
    private String mDeviceModel = android.os.Build.MODEL;
    // sdk version, TODO 动态获取
    private String mAppVersion = Version.VERSION;
    private String mAuthTimestamp = "";
    /**
     * AuthInfo=md5(UserId+|ClientId+|+secretKey+|+AuthTimestamp)
     * ClientId为空时：
     * AuthInfo=md5(UserId+|+secretKey+|+AuthTimestamp)
     */
    private String mAuthInfo = "";
    private String mFileName = "";
    private Long mFileSize = 0L;
    private String mFileCreateTime = "";
    private String mFileHash = "";
    private Float mUploadRatio = 0f;
    private String mUploadId = "todo";
    private Integer mDonePartsCount = 0;
    private Integer mTotalPart = 0;
    private Long mPartSize = 0L;
    @Deprecated
    private String mUploadPoint = "todo";
    @Deprecated
    private Long mUserId = -1L;
    private String mVideoId = "";
    private String mUploadAddress = "todo";

    // 辅助生成authinfo, 固定值 TODO
    // online use, !!!与新source要对应
    private String INNER_SECRET_KEY = "FqQ^jDLpi0PVZ74A";
    // test use, !!!测试key情况，source需要改为CLIENT
    //private String INNER_SECRET_KEY = "LZliQdg37Nm@yzJ1";

    private String mDomainRegion = null;

    private static final String KEY_SHARED_PREFERENCE = "aliyun_svideo_global_info";

    public AliyunUploadProgressReporter(Context context) {
        initGlobalInfo(context);
        boolean isTable = DeviceUtils.isTabletDevice(context);
        mTerminalType = isTable ? "APad" : "APhone";
    }

    public void setDomainRegion(String domainRegion) {
        mDomainRegion = domainRegion;
    }

    private void initGlobalInfo(Context context) {
        if (context != null) {
            if (AliyunLogCommon.APPLICATION_ID == null) {
                AliyunLogCommon.APPLICATION_ID = context.getPackageName();
                AliyunLogCommon.APPLICATION_NAME = ManifestUtils.getAppName(context);
            }
            if (AliyunLogCommon.UUID == null) {
                SharedPreferences sp = context.getSharedPreferences(KEY_SHARED_PREFERENCE, Context.MODE_PRIVATE);
                if (sp.contains(AliyunLogKey.KEY_UUID)) {
                    AliyunLogCommon.UUID = sp.getString(AliyunLogKey.KEY_UUID, null);
                }
                if (AliyunLogCommon.UUID == null) {
                    AliyunLogCommon.UUID = UUIDGenerator.generateUUID();
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(AliyunLogKey.KEY_UUID, AliyunLogCommon.UUID);
                    editor.commit();
                }
                mClientId = AliyunLogCommon.UUID;
            }
        }
    }

    public void setAuthTimestamp(String authTimestamp) {
        mAuthTimestamp = authTimestamp;
    }

    public void setAuthInfo() {
        StringBuilder sb = new StringBuilder();
        //mUserId is deprecated
        //sb.append(mUserId).append("|");
        sb.append(mClientId).append("|");
        sb.append(INNER_SECRET_KEY).append("|");
        sb.append(mAuthTimestamp);
        mAuthInfo = MD5Util.encryptToHexStr(sb.toString());
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public void setFileSize(Long fileSize) {
        mFileSize = fileSize;
    }

    public void setFileCreateTime(String fileCreateTime) {
        mFileCreateTime = fileCreateTime;
    }

    public void setFileHash(String fileHash) {
        mFileHash = fileHash;
    }

    public void setUploadRatio(Float uploadRatio) {
        mUploadRatio = uploadRatio;
    }

    public void setUploadId(String uploadId) {
        mUploadId = uploadId;
    }

    public void setDonePartsCount(Integer donePartsCount) {
        mDonePartsCount = donePartsCount;
    }

    public void setTotalPart(Integer totalPart) {
        mTotalPart = totalPart;
    }

    public void setPartSize(Long partSize) {
        mPartSize = partSize;
    }

    @Deprecated
    void setUploadPoint(String uploadPoint) {
        mUploadPoint = uploadPoint;
    }

    @Deprecated
    void setUserId(Long userId) {
        mUserId = userId;
    }

    public void setVideoId(String videoId) {
        mVideoId = videoId;
    }

    public void setUploadAddress(String uploadAddress) {
        mUploadAddress = uploadAddress;
    }

    public void pushUploadProgress(final String accessSecretKey) {
        Log.d(TAG, "pushUploadProgress");
        setAuthInfo();
        if (ProcessUtil.isMainThread()) {
            Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    doRun(accessSecretKey);
                }
            });
        } else {
            doRun(accessSecretKey);
        }
    }

    private void doRun(String accessSecretKey) {
        String domain = AliyunReportParam.generateDomainWithRegion(mDomainRegion);
        String params = AliyunReportParam.generateUploadProgressParams(generatePublicParams(), accessSecretKey);
        Log.d(TAG, "domain : " + domain);
        Log.d(TAG, "params : " + params);
        HttpRequest.get(domain + params,
                new BaseHttpRequestCallback() {
                    @Override
                    protected void onSuccess(Headers headers, Object o) {
                        super.onSuccess(headers, o);
                        Log.d(AliyunTag.TAG, "Push log success");
                    }

                    @Override
                    public void onFailure(int errorCode, String msg) {
                        super.onFailure(errorCode, msg);
                        Log.d(AliyunTag.TAG, "Push log failure, error Code " + errorCode + ", msg:" + msg);
                    }
                });
    }

    private Map<String, String> generatePublicParams() {
        Map<String, String> publicParams = new HashMap<>();
        publicParams.put(AliyunReportParam.UP_ACTION, mAction);
        publicParams.put(AliyunReportParam.UP_SOURCE, mSource);
        publicParams.put(AliyunReportParam.UP_CLIENTID, mClientId);
        publicParams.put(AliyunReportParam.UP_BUSINESSTYPE, mBusinessType);
        publicParams.put(AliyunReportParam.UP_TERMINALTYPE, mTerminalType);
        publicParams.put(AliyunReportParam.UP_DEVICEMODEL, mDeviceModel);
        publicParams.put(AliyunReportParam.UP_APPVERSION, mAppVersion);
        publicParams.put(AliyunReportParam.UP_AUTHTIMESTAMP, mAuthTimestamp);
        publicParams.put(AliyunReportParam.UP_AUTHINFO, mAuthInfo);
        publicParams.put(AliyunReportParam.UP_FILENAME, mFileName);
        publicParams.put(AliyunReportParam.UP_FILESIZE, String.valueOf(mFileSize));
        publicParams.put(AliyunReportParam.UP_FILECREATETIME, mFileCreateTime);
        publicParams.put(AliyunReportParam.UP_FILEHASH, mFileHash);
        publicParams.put(AliyunReportParam.UP_UPLOADRATIO, String.valueOf(mUploadRatio));
        publicParams.put(AliyunReportParam.UP_UPLOADID, mUploadId);
        publicParams.put(AliyunReportParam.UP_DONEPARTSCOUNT, String.valueOf(mDonePartsCount));
        publicParams.put(AliyunReportParam.UP_TOTALPART, String.valueOf(mTotalPart));
        publicParams.put(AliyunReportParam.UP_PARTSIZE, String.valueOf(mPartSize));
        publicParams.put(AliyunReportParam.UP_UPLOADPOINT, mUploadPoint);
        //mUserId is deprecated
        //publicParams.put(AliyunReportParam.UP_USERID, String.valueOf(mUserId));
        if (!TextUtils.isEmpty(mVideoId)) {
            publicParams.put(AliyunReportParam.UP_VIDEOID, mVideoId);
        }
        if (!TextUtils.isEmpty(mUploadAddress)){
            publicParams.put(AliyunReportParam.UP_UPLOADADRESS, mUploadAddress);
        }

        return publicParams;
    }
}
