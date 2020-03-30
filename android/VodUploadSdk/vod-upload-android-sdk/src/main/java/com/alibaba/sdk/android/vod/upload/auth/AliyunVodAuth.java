/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload.auth;

import android.util.Log;

import com.alibaba.sdk.android.vod.upload.model.VodInfo;
import com.aliyun.auth.common.AliyunVodUploadType;
import com.aliyun.auth.core.AliyunVodErrorCode;
import com.aliyun.auth.core.VodThreadService;
import com.aliyun.auth.model.CreateImageForm;
import com.aliyun.auth.model.CreateVideoForm;
import com.aliyun.auth.model.VodErrorResponse;
import com.aliyun.vod.jasonparse.JSONSupport;
import com.aliyun.vod.jasonparse.JSONSupportImpl;
import com.aliyun.vod.qupaiokhttp.BaseHttpRequestCallback;
import com.aliyun.vod.qupaiokhttp.HttpRequest;
import com.aliyun.vod.qupaiokhttp.StringHttpRequestCallback;
import com.google.gson.JsonSyntaxException;

import okhttp3.Headers;
import okhttp3.Response;


/**
 * Created by Mulberry on 2017/11/2.
 */
public class AliyunVodAuth {
    private static final String TAG = "AliyunVodAuth";
    private JSONSupport jsonSupportImpl;
    private VodAuthCallBack vodAuthCallBack;
    private VodThreadService mHttpService;
    private String createImageUrl = null;
    private String createVideoUrl = null;
    private String domainRegion = null;

    public AliyunVodAuth(VodAuthCallBack callBack) {
        this.vodAuthCallBack = callBack;
        jsonSupportImpl = new JSONSupportImpl();
        mHttpService = new VodThreadService(String.valueOf(System.currentTimeMillis()));
    }

    public void setDomainRegion(String region) {
        domainRegion = region;
    }

    public void cancel() {
        vodAuthCallBack = null;
        if (createImageUrl != null) {
            HttpRequest.cancel(createImageUrl);
        }
        if (createVideoUrl != null) {
            HttpRequest.cancel(createVideoUrl);
        }
    }

    public void createUploadImage(final String accessKeyId, final String accessKeySecret, final String securityToken, final VodInfo vodInfo, final String storageLocation, final String appId, final String requestID) {
        mHttpService.execute(new Runnable() {
            @Override
            public void run() {
                createImageUrl = AliyunVodParam.generateOpenAPIURL(domainRegion, AliyunVodParam.generatePublicParamters(accessKeyId, securityToken, requestID),
                        AliyunVodParam.generatePrivateParamtersToUploadImage(vodInfo, storageLocation, appId), accessKeySecret);
                HttpRequest.get(createImageUrl,
                        new StringHttpRequestCallback() {

                            @Override
                            protected void onSuccess(Headers headers, String s) {
                                super.onSuccess(headers, s);
                                Log.d(TAG, "headers" + headers + "\nmsg" + s);

                                CreateImageForm createImageForm = null;
                                try {
                                    createImageForm = jsonSupportImpl.readValue(s, CreateImageForm.class);
                                    if (vodAuthCallBack != null) {
                                        vodAuthCallBack.onCreateUploadImaged(createImageForm);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    if (e instanceof JsonSyntaxException) {
                                        if (vodAuthCallBack != null) {
                                            vodAuthCallBack.onError(AliyunVodErrorCode.VODERRORCODE_HTTP_ABNORMAL, "The network is abnormal, please check your network connection.");
                                        }
                                    }
                                }

                            }

                            @Override
                            public void onResponse(Response httpResponse, String response, Headers headers) {
                                super.onResponse(httpResponse, response, headers);
                                Log.d(TAG, "httpResponse" + httpResponse + "\nmsg" + response + "\nheaders" + headers);
                                if (httpResponse != null && httpResponse.code() != 200) {
                                    VodErrorResponse vodErrorResponse = null;
                                    try {
                                        vodErrorResponse = jsonSupportImpl.readValue(response, VodErrorResponse.class);
                                        if (vodErrorResponse.getCode().equals(AliyunVodErrorCode.VODERRORCODE_INVALIDSECURITYTOKEN_EXPIRED)) {
                                            if (vodAuthCallBack != null) {
                                                vodAuthCallBack.onSTSExpired(AliyunVodUploadType.IMAGE);
                                            }

                                        } else {
                                            if (vodAuthCallBack != null) {
                                                vodAuthCallBack.onError(vodErrorResponse.getCode(), vodErrorResponse.getMessage());
                                            }
                                        }
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(int errorCode, String msg) {
                                super.onFailure(errorCode, msg);
                                Log.d(TAG, "code" + errorCode + "msg" + msg + "time:" + System.currentTimeMillis());
                                if (errorCode == ERROR_RESPONSE_UNKNOWN && vodAuthCallBack != null) {
                                    vodAuthCallBack.onError(AliyunVodErrorCode.VODERRORCODE_HTTP_ABNORMAL, "http error response unknown.");
                                }
                            }
                        });
            }
        });
    }

    public void createUploadVideo(final String accessKeyId, final String accessKeySecret, final String securityToken,
                                  final VodInfo vodInfo, final boolean transcodeMode, final String templateGroupId, final String storageLocation, final String workFlowId, final String appId, final String requestID) {
        mHttpService.execute(new Runnable() {
            @Override
            public void run() {
                createVideoUrl = AliyunVodParam.generateOpenAPIURL(domainRegion, AliyunVodParam.generatePublicParamters(accessKeyId, securityToken, requestID),
                        AliyunVodParam.generatePrivateParamtersToUploadVideo(vodInfo, transcodeMode, templateGroupId, storageLocation, workFlowId, appId), accessKeySecret);

                HttpRequest.get(createVideoUrl, new StringHttpRequestCallback() {

                    @Override
                    protected void onSuccess(Headers headers, String s) {
                        super.onSuccess(headers, s);
                        Log.d(TAG, "onSuccess --- createUploadVideo");
                        CreateVideoForm createVideoForm = null;
                        try {
                            createVideoForm = jsonSupportImpl.readValue(s, CreateVideoForm.class);
                            Log.d(TAG, "onSuccess --- createUploadVideo" + "getUploadAuth:" + createVideoForm.getUploadAuth() + "getUploadAddress" + createVideoForm.getUploadAddress() + "\nrequestID:" + createVideoForm.getRequestId());
                            if (vodAuthCallBack != null) {
                                vodAuthCallBack.onCreateUploadVideoed(createVideoForm, vodInfo.getCoverUrl());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (e instanceof JsonSyntaxException) {
                                if (vodAuthCallBack != null) {
                                    vodAuthCallBack.onError(AliyunVodErrorCode.VODERRORCODE_HTTP_ABNORMAL, "The network is abnormal. Please check your network connection. Your network may need to log in.");
                                }
                            }
                        }
                    }


                    @Override
                    public void onResponse(Response httpResponse, String response, Headers headers) {
                        super.onResponse(httpResponse, response, headers);
                        if (httpResponse != null && httpResponse.code() != 200) {
                            Log.d(TAG, "onResponse --- createUploadVideo" + httpResponse + response);
                            VodErrorResponse vodErrorResponse = null;
                            try {
                                vodErrorResponse = jsonSupportImpl.readValue(response, VodErrorResponse.class);
                                if (vodAuthCallBack != null) {
                                    if (vodErrorResponse.getCode().equals(AliyunVodErrorCode.VODERRORCODE_INVALIDSECURITYTOKEN_EXPIRED)) {
                                        vodAuthCallBack.onSTSExpired(AliyunVodUploadType.VIDEO);
                                    } else {
                                        vodAuthCallBack.onError(vodErrorResponse.getCode(), vodErrorResponse.getMessage());
                                    }
                                }
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String msg) {
                        super.onFailure(errorCode, msg);
                        Log.d(TAG, "code" + errorCode + "msg" + msg);
                        if (errorCode == ERROR_RESPONSE_UNKNOWN) {
                            vodAuthCallBack.onError(AliyunVodErrorCode.VODERRORCODE_HTTP_ABNORMAL, "http error response unknown.");
                        }
                    }
                });
            }
        });
    }

    public void refreshUploadVideo(final String accessKeyId, final String accessKeySecret, final String securityToken, final String videoId, final String coverurl, final String requestID) {
        mHttpService.execute(new Runnable() {
            @Override
            public void run() {
                createVideoUrl = AliyunVodParam.generateOpenAPIURL(domainRegion, AliyunVodParam.generatePublicParamters(accessKeyId, securityToken, requestID),
                        AliyunVodParam.generatePrivateParamtersToReUploadVideo(videoId), accessKeySecret);
                HttpRequest.get(createVideoUrl, new BaseHttpRequestCallback() {

                    @Override
                    protected void onSuccess(Headers headers, Object o) {
                        super.onSuccess(headers, o);
                        CreateVideoForm createVideoForm = null;
                        try {
                            if (vodAuthCallBack != null) {
                                createVideoForm = jsonSupportImpl.readValue((String) o, CreateVideoForm.class);
                                createVideoForm.setVideoId(videoId);
                                vodAuthCallBack.onCreateUploadVideoed(createVideoForm, coverurl);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (e instanceof JsonSyntaxException) {
                                if (vodAuthCallBack != null) {
                                    vodAuthCallBack.onError(AliyunVodErrorCode.VODERRORCODE_HTTP_ABNORMAL, "The network is abnormal. Please check your network connection. Your network may need to log in.");
                                }
                            }
                        }
                    }

                    @Override
                    public void onResponse(Response httpResponse, String response, Headers headers) {
                        super.onResponse(httpResponse, response, headers);
                        if (httpResponse != null && httpResponse.code() != 200) {
                            VodErrorResponse vodErrorResponse = null;
                            try {
                                if (vodAuthCallBack != null) {
                                    vodErrorResponse = jsonSupportImpl.readValue(response, VodErrorResponse.class);
                                    String code = "UNKNOWN";
                                    String message = "UNKNOWN";
                                    if (vodErrorResponse != null) {
                                        code = vodErrorResponse.getCode();
                                        message = vodErrorResponse.getMessage();
                                    }
                                    if (AliyunVodErrorCode.VODERRORCODE_INVALIDSECURITYTOKEN_EXPIRED.equals(code)) {
                                        vodAuthCallBack.onSTSExpired(AliyunVodUploadType.VIDEO);
                                    } else {
                                        vodAuthCallBack.onError(code, message);
                                    }
                                }
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String msg) {
                        super.onFailure(errorCode, msg);
                        Log.d(TAG, "code" + errorCode + "msg" + msg);
                    }
                });
            }
        });

    }

    public interface VodAuthCallBack {
        /**
         * 创建图片凭证成功
         *
         * @param createImageForm
         */
        void onCreateUploadImaged(CreateImageForm createImageForm);

        /**
         * 创建上传凭证成功
         *
         * @param createVideoForm
         */
        void onCreateUploadVideoed(CreateVideoForm createVideoForm, String coverUrl);

        /**
         * STS过期
         */
        void onSTSExpired(AliyunVodUploadType uploadType);

        /**
         * 创建失败
         *
         * @param code
         * @param message
         */
        void onError(String code, String message);
    }

}
