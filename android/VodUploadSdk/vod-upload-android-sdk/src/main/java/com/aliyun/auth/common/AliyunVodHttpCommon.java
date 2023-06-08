/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.aliyun.auth.common;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.UUID;



public class AliyunVodHttpCommon {

    // 支持region可配置, ex: https://vod.region.aliyuncs.com/
    private static final String VOD_DOMAIN_PREFIX = "https://vod.";
    private static final String VOD_DOMAIN_REGION = "cn-shanghai";
    private static final String VOD_DOMAIN_SUFFIX = ".aliyuncs.com/";

    public static final String VOD_DOMAIN = "https://vod.cn-shanghai.aliyuncs.com/";
    public static final String  HTTP_METHOD = "GET";

    public static final String generateVodDomain(String region) {
        return VOD_DOMAIN_PREFIX + (TextUtils.isEmpty(region) ? VOD_DOMAIN_REGION : region) + VOD_DOMAIN_SUFFIX;
    }

    public static class Action{
        public static final String CREATE_UPLOAD_IMAGE = "CreateUploadImage";
        public static final String CREATE_UPLOAD_VIDEO = "CreateUploadVideo";
        public static final String REFRESH_UPLOAD_VIDEO = "RefreshUploadVideo";
    }

    public  static class ImageType{
        public static final String IMAGETYPE_COVER = "cover";
        public static final String IMAGETYPE_DEFAULT = "default";
    }

    public static class ImageExt{
        public static final String IMAGEEXT_PNG = "png";
        public static final String IMAGEEXT_JPG = "jpg";
        public static final String IMAGEEXT_JPEG = "jpeg";
    }

    public static class Format{
        public static final String FORMAT_JSON = "json";
        public static final String FORMAT_XML = "xml";
    }

    public static final String COMMON_API_VERSION = "2017-03-21";
    public static final String COMMON_TIMESTAMP = generateTimestamp();

    public static final String COMMON_SIGNATURE = "HMAC-SHA1";
    public static final String COMMON_SIGNATURE_METHOD = "HMAC-SHA1";
    public static final String COMMON_SIGNATUREVERSION = "1.0";
    public static final String COMMON_SIGNATURE_NONCE = generateRandom();
    public static final String COMON_NO_TRANSCODEMODE = "NoTranscode";
    public static final String COMON_FAST_TRANSCODEMODE = "FastTranscode";


    /*生成当前UTC时间戳Time*/
    public static String generateTimestamp() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return df.format(date);
    }

    public static String generateRandom() {
        String signatureNonce = UUID.randomUUID().toString();
        return signatureNonce;
    }

}
