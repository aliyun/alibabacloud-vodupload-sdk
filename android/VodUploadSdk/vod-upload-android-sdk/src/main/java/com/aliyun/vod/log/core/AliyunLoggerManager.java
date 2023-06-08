package com.aliyun.vod.log.core;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

public class AliyunLoggerManager {
    private static boolean sLoggerOpen = true;
    private static Map<String, AliyunLogger> mLoggers = new HashMap<>();

    public static AliyunLogger createLogger(Context context, String tag) {
        if (sLoggerOpen) {
            AliyunLogger logger = mLoggers.get(tag);
            if (logger != null) {
                return logger;
            }
            logger = new AliyunLogger(new LogService(tag));
            logger.init(context);
            mLoggers.put(tag, logger);
            return logger;
        } else {
            return null;
        }
    }

    public static void destroyLogger(String tag) {
        AliyunLogger logger = mLoggers.remove(tag);
        if (logger != null) {
            logger.destroy();
        }
    }

    public static AliyunLogger getLogger(String tag) {
        if (!sLoggerOpen) {
            return null;
        }
        return mLoggers.get(tag);
    }

    public static void toggleLogger(boolean open) {
        sLoggerOpen = open;
    }

    public static boolean isLoggerOpen() {
        return sLoggerOpen;
    }
}
