package com.aliyun.vod.log.struct;

import com.aliyun.vod.log.core.AliyunLogger;
import com.aliyun.vod.log.core.AliyunLoggerManager;

/**
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

public class AliyunLogInfo {
    private String mLoggerTag;
    public AliyunLogInfo(String tag) {
        this.mLoggerTag = tag;
    }
    public String getRequestID() {
        AliyunLogger logger = AliyunLoggerManager.getLogger(mLoggerTag);
        if(logger != null) {
            return logger.getRequestID();
        }
        return null;
    }
}
