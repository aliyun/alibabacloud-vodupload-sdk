/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload.common;

import com.aliyun.vod.common.utils.UUIDGenerator;

/**
 * Created by Mulberry on 2018/1/13.
 */

public class RequestIDSession {

    private String requestID;
    private boolean canModify = true;
    private static RequestIDSession requestIDSession;

    public static RequestIDSession getInstance() {
        if (requestIDSession == null) {
            synchronized (RequestIDSession.class) {
                if (requestIDSession == null) {
                    requestIDSession = new RequestIDSession();
                }
            }
        }
        return requestIDSession;
    }

    public String getRequestID() {
        if (requestID == null) {
            requestID = UUIDGenerator.generateUUID();
        }
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public void setRequestID(String requestID, boolean canModify) {
        this.requestID = requestID;
        this.canModify = canModify;
    }

    public void updateRequestID() {
        if (this.canModify) {
            this.requestID = UUIDGenerator.generateUUID();
        }
    }
}
