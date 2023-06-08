/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.aliyun.auth.model;


public class VodErrorResponse {
    private String RequestId;
    private String HostId;
    private String Message;
    private String Code;

    public String getRequestId() {
        return RequestId;
    }

    public void setRequestId(String requestId) {
        RequestId = requestId;
    }

    public String getHostId() {
        return HostId;
    }

    public void setHostId(String hostId) {
        HostId = hostId;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }
}
