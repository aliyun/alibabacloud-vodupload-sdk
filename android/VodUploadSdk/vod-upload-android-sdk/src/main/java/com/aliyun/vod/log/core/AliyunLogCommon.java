package com.aliyun.vod.log.core;

import android.os.Build;
import android.text.TextUtils;

/**
 * Copyright (C) 2020 Alibaba Group Holding Limited on 2017/8/23.
 */

public class AliyunLogCommon {

    //public static final String LOG_PUSH_URL = "https://videocloud.cn-hangzhou.log.aliyuncs.com/logstores/";
    public static final String LOG_PUSH_TRACK_APIVERSION = "/track?APIVersion=0.6.0";

    private static final String DOMAIN_PREFIX = "https://videocloud.";
    private static final String DOMAIN_REGION = "cn-hangzhou";
    private static final String DOMAIN_SUFFIX = ".log.aliyuncs.com/logstores/";

    public static final String generateDomainWithRegion(String region) {
        return DOMAIN_PREFIX + (TextUtils.isEmpty(region) ? DOMAIN_REGION : region) + DOMAIN_SUFFIX;
    }

    public static class LogLevel {
        public static final String DEBUG = "debug";
        public static final String INFO = "info";
        public static final String WARN = "warn";
        public static final String ERROR = "error";
    }

    public static class LogStores{
        public static final String SVIDEO = "svideo";
        public static final String UPLOAD = "upload";
        public static final String UPLOAD_TEST = "uploadtest";
    }

    public static class Module {
        public static final String SAAS_PLAYER = "saas_player";
        public static final String PAAS_PLAYER = "paas_player";
        public static final String MIXER = "mixer";
        public static final String PUBLISHER = "publisher";
        public static final String BASE = "svideo_basic";
        public static final String STANDARD = "svideo_standard";
        public static final String PRO = "svideo_pro";
        public static final String UPLOADER = "uploader";
    }

    public static class SubModule {
        public static final String play ="play";
        public static final String download = "download";
        public static final String RECORD = "record";
        public static final String CUT = "cut";
        public static final String EDIT = "edit";
        public static final String UPLOAD = "upload";
    }

    public static class  Product{
        public static final String VIDEO_PLAYER  = "player";
        public static final String VIDEO_PUSHER  = "pusher";
        public static final String VIDEO_MIXER   = "mixer";
        public static final String VIDEO_SVIDEO  = "svideo";
        public static final String VIDEO_UPLOAD  = "upload";
    }
    public static final String LOG_LEVEL  = "1";

    public static final String PRODUCT = "svideo";
    public static final String MODULE = "upload";
    public static final String TERMINAL_TYPE = "phone";
    public static final String DEVICE_MODEL = Build.MODEL;
    public static final String OPERATION_SYSTEM = "android";
    public static final String OS_VERSION = Build.VERSION.RELEASE;
    public static String APPLICATION_ID = null;
    public static String APPLICATION_NAME = null;
    public static String UUID = null;
    public static String NetWorkType = "WiFi";

}
