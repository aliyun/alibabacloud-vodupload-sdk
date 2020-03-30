/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.aliyun.vod.qupaiokhttp;

import java.lang.reflect.Type;

import okhttp3.Headers;
import okhttp3.Response;

public class BaseHttpRequestCallback<T> {

    //    public static final int ERROR_RESPONSE_NULL = 1001;
    public static final int ERROR_RESPONSE_DATA_PARSE_EXCEPTION = 1002;
    public static final int ERROR_RESPONSE_UNKNOWN = 1003;
//    public static final int ERROR_RESPONSE_TIMEOUT = 1004;

    protected Type type;
    protected Headers headers;

    public BaseHttpRequestCallback() {
        type = ClassTypeReflect.getModelClazz(getClass());
    }

    public void onStart() {
    }

    public void onResponse(Response httpResponse, String response, Headers headers) {

    }

    public void onResponse(String response, Headers headers) {
    }

    public void onFinish() {
    }

    protected void onSuccess(Headers headers, T t) {
    }

    protected void onSuccess(T t) {

    }

    /**
     * 上传文件进度
     *
     * @param progress
     * @param networkSpeed 网速
     * @param done
     */
    public void onProgress(int progress, long networkSpeed, boolean done) {
    }

    public void onFailure(int errorCode, String msg) {
    }

    public Headers getHeaders() {
        return headers;
    }

    protected void setResponseHeaders(Headers headers) {
        this.headers = headers;
    }


}
