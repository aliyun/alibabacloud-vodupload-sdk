/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.vod.common.logger;

/**
 * Logger is a wrapper of {@link android.util.Log}
 * But more pretty, simple and powerful
 */
public final class Logger {

    public static final String DEFAULT_TAG = "QuLogger";

    private static boolean debug = false;
    private static LoggerPrinter loggerPrinter;

    public static void setDebug(boolean isDebug) {
        debug = isDebug;
    }

    public static LoggerPrinter getDefaultLogger() {
        if ( loggerPrinter == null) {
            loggerPrinter = LoggerFactory.getFactory(DEFAULT_TAG, debug);
        }
        return loggerPrinter;
    }

}
