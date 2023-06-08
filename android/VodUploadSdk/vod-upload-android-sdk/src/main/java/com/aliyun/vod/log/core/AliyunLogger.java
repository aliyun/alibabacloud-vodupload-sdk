package com.aliyun.vod.log.core;

import android.content.Context;
import android.util.Log;

import com.aliyun.vod.common.global.AliyunTag;
import com.aliyun.vod.common.global.Version;
import com.aliyun.vod.common.utils.DeviceUtils;
import com.aliyun.vod.common.utils.ManifestUtils;
import com.aliyun.vod.common.utils.ProcessUtil;
import com.aliyun.vod.log.util.UUIDGenerator;
import com.aliyun.vod.qupaiokhttp.BaseHttpRequestCallback;
import com.aliyun.vod.qupaiokhttp.HttpRequest;

import java.lang.ref.WeakReference;
import java.util.Map;

import okhttp3.Headers;

/**
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

public class AliyunLogger {
    private static final String TAG = "AliyunLogger";
    private static final String KEY_SHARED_PREFERENCE = "aliyun_svideo_global_info";
    private String mRequestID = null;
    private boolean canModify = true;
    private boolean productSVideo = false;
    private String appVersion;
    private LogService mLogService;
    private LogService mHttpService;
    private WeakReference<Context> mContextRef;

    private String domainRegion = null;

    protected AliyunLogger(LogService logService) {
        mLogService = logService;
        mHttpService = new LogService(String.valueOf(System.currentTimeMillis()));
    }

    public void init(Context context) {
        this.mContextRef = new WeakReference<Context>(context.getApplicationContext());
        initGlobalInfo();
//        updateRequestID();
    }

    private void initGlobalInfo() {
        Context context = mContextRef.get();
        if (context != null) {
            if (AliyunLogCommon.APPLICATION_ID == null) {
                AliyunLogCommon.APPLICATION_ID = context.getPackageName();
                AliyunLogCommon.APPLICATION_NAME = ManifestUtils.getAppName(context);
            }
        } else {
            Log.w(TAG, "context release??");
        }
        if (AliyunLogCommon.UUID == null) {
            AliyunLogCommon.UUID = UUIDGenerator.generateUUID();
        }
    }

    public void updateRequestID() {
        if (canModify) {
            mRequestID = UUIDGenerator.generateRequestID();
        }
    }

    public void setRequestID(String requestID, boolean canModify) {
        mRequestID = requestID;
        this.canModify = canModify;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    // videocloud不需要改domain
    private void setDomainRegion(String region) {
        this.domainRegion = region;
    }

    public void setProductSVideo(boolean productSVideo) {
        this.productSVideo = productSVideo;
    }

    public void pushLog(final Map<String, String> args,
                        final String product,
                        final String logLevel,
                        final String module,
                        final String subModule,
                        final int eventId,
                        final String logstore,
                        final String requestID
    ) {

        final Context context = mContextRef.get();
        if (ProcessUtil.isMainThread()) {
            mHttpService.execute(new Runnable() {
                @Override
                public void run() {
                    HttpRequest.get(AliyunLogCommon.generateDomainWithRegion(domainRegion) + (productSVideo ? AliyunLogCommon.LogStores.SVIDEO : logstore) + AliyunLogCommon.LOG_PUSH_TRACK_APIVERSION + AliyunLogParam.generatePushParams(args, productSVideo ? AliyunLogCommon.Product.VIDEO_SVIDEO : product, logLevel, module, subModule, eventId,
                            requestID == null ? mRequestID : requestID, DeviceUtils.getNetWorkType(context), canModify ? Version.VERSION : appVersion)
                            , new BaseHttpRequestCallback() {
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
            });
        } else {
            HttpRequest.get(AliyunLogCommon.generateDomainWithRegion(domainRegion) + (productSVideo ? AliyunLogCommon.LogStores.SVIDEO : logstore) + AliyunLogCommon.LOG_PUSH_TRACK_APIVERSION + AliyunLogParam.generatePushParams(args, productSVideo ? AliyunLogCommon.Product.VIDEO_SVIDEO : product, logLevel, module, subModule, eventId,
                    requestID == null ? mRequestID : requestID, DeviceUtils.getNetWorkType(context), canModify ? Version.VERSION : appVersion)
                    , new BaseHttpRequestCallback() {
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
    }

    public LogService getLogService() {
        return mLogService;
    }

    public void destroy() {
        if (mLogService != null) {
            mLogService.quit();
            mLogService = null;
        }
        if (mHttpService != null) {
            mHttpService.quit();
            mHttpService = null;
        }
    }

    public String getRequestID() {
        return mRequestID;
    }


}
