/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.vod.common.logger;

/**
 * Desction:日志工厂类
 */
public class LoggerFactory {

    public static LoggerPrinter getFactory(String tag, boolean debug) {
        final LoggerPrinter printer = new LoggerPrinter();
        printer.init(tag);
        LogLevel level = LogLevel.NONE;
        if (debug) {
            level = LogLevel.FULL;
        }
        printer.getSettings().methodCount(3).logLevel(level);

        return printer;
    }

}
