/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload.auth;

import android.text.TextUtils;

import com.alibaba.sdk.android.vod.upload.model.VodInfo;
import com.aliyun.auth.common.AliyunVodHttpCommon;
import com.aliyun.auth.common.AliyunVodSignature;
import com.aliyun.auth.core.AliyunVodKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mulberry on 2017/11/2.
 */
public class AliyunVodParam {

    /**
     * 生成视频点播OpenAPI:CREATE_UPLOAD_VIDEO 的私有参数
     * <p>
     * 不同API需要修改此方法中的参数
     *
     * @return
     */
    public static Map<String, String> generatePrivateParamtersToUploadVideo(VodInfo vodInfo, boolean transcodeMode, String templateGroupId, String storageLocation, String workFlowId, String appId) {
        Map<String, String> privateParams = new HashMap<>();
        privateParams.put(AliyunVodKey.KEY_VOD_ACTION, AliyunVodHttpCommon.Action.CREATE_UPLOAD_VIDEO);
        privateParams.put(AliyunVodKey.KEY_VOD_TITLE, vodInfo.getTitle());
        privateParams.put(AliyunVodKey.KEY_VOD_FILENAME, vodInfo.getFileName());
        privateParams.put(AliyunVodKey.KEY_VOD_FILESIZE, vodInfo.getFileSize());
        privateParams.put(AliyunVodKey.KEY_VOD_DESCRIPTION, vodInfo.getDesc());
        privateParams.put(AliyunVodKey.KEY_VOD_COVERURL, vodInfo.getCoverUrl());
        privateParams.put(AliyunVodKey.KEY_VOD_CATEID, String.valueOf(vodInfo.getCateId()));
        privateParams.put(AliyunVodKey.KEY_VOD_TAGS, generateTags(vodInfo.getTags()));//generateTags(vodInfo.getTags())
        privateParams.put(AliyunVodKey.KEY_VOD_STORAGELOCATION, storageLocation);
        privateParams.put(AliyunVodKey.KEY_VOD_USERDATA, vodInfo.getUserData());
        //TranscodeMode 为NoTranscode时，点播服务无法获取到视频文件的部分参数，需要用户在调用该上传接口时传入UserData中。
        // 当传入TemplateGroupId时，该参数不需要传，两者同时传入时， TemplateGroupId优先级高
        if (TextUtils.isEmpty(templateGroupId)) {
            privateParams.put(AliyunVodKey.KEY_VOD_TRANSCODEMODE, transcodeMode ? AliyunVodHttpCommon.COMON_FAST_TRANSCODEMODE : AliyunVodHttpCommon.COMON_NO_TRANSCODEMODE);
        } else {
            privateParams.put(AliyunVodKey.KEY_VOD_TEMPLATEGROUPID, templateGroupId);
        }
        privateParams.put(AliyunVodKey.KEY_VOD_WORKFLOWLD, workFlowId);
        privateParams.put(AliyunVodKey.KEY_VOD_APPID, appId);

        return privateParams;
    }

    /**
     * 生成视频点播OpenAPI:CreateUploadImage 的私有参数
     * 不同API需要修改此方法中的参数
     *
     * @return
     */
    public static Map<String, String> generatePrivateParamtersToUploadImage(VodInfo vodInfo, String storageLocation, String appId) {
        Map<String, String> privateParams = new HashMap<>();
        privateParams.put(AliyunVodKey.KEY_VOD_ACTION, AliyunVodHttpCommon.Action.CREATE_UPLOAD_IMAGE);
        privateParams.put(AliyunVodKey.KEY_VOD_IMAGETYPE, AliyunVodHttpCommon.ImageType.IMAGETYPE_COVER);
        privateParams.put(AliyunVodKey.KEY_VOD_IMAGEEXT, AliyunVodHttpCommon.ImageExt.IMAGEEXT_PNG);
        privateParams.put(AliyunVodKey.KEY_VOD_TITLE, vodInfo.getTitle());
        privateParams.put(AliyunVodKey.KEY_VOD_TAGS, generateTags(vodInfo.getTags()));
        privateParams.put(AliyunVodKey.KEY_VOD_CATEID, String.valueOf(vodInfo.getCateId()));
        privateParams.put(AliyunVodKey.KEY_VOD_DESCRIPTION, vodInfo.getDesc());
        privateParams.put(AliyunVodKey.KEY_VOD_STORAGELOCATION, storageLocation);
        privateParams.put(AliyunVodKey.KEY_VOD_USERDATA, vodInfo.getUserData());
        privateParams.put(AliyunVodKey.KEY_VOD_APPID, appId);

        return privateParams;
    }

    /**
     * 生成视频点播OpenAPI:RefreshUploadVideo 的私有参数
     * 不同API需要修改此方法中的参数
     *
     * @return
     */
    public static Map<String, String> generatePrivateParamtersToReUploadVideo(String videoId) {
        Map<String, String> privateParams = new HashMap<>();
        privateParams.put(AliyunVodKey.KEY_VOD_ACTION, AliyunVodHttpCommon.Action.REFRESH_UPLOAD_VIDEO);
        privateParams.put(AliyunVodKey.KEY_VOD_VIDEOID, videoId);
        return privateParams;
    }

    /**
     * 生成视频点播OpenAPI公共参数
     * 不需要修改
     *
     * @return
     */
    public static Map<String, String> generatePublicParamters(String accessKeyId, String securityToken, String requestID) {
        Map<String, String> publicParams = new HashMap<>();
        publicParams.put(AliyunVodKey.KEY_VOD_COMMON_FORMAT, AliyunVodHttpCommon.Format.FORMAT_JSON);
        publicParams.put(AliyunVodKey.KEY_VOD_COMMON_VERSION, AliyunVodHttpCommon.COMMON_API_VERSION);
        publicParams.put(AliyunVodKey.KEY_VOD_COMMON_ACCESSKEYID, accessKeyId);
        publicParams.put(AliyunVodKey.KEY_VOD_COMMON_SIGNATURE_METHOD, AliyunVodHttpCommon.COMMON_SIGNATURE_METHOD);
        publicParams.put(AliyunVodKey.KEY_VOD_COMMON_SIGNATURE_VERSION, AliyunVodHttpCommon.COMMON_SIGNATUREVERSION);
        publicParams.put(AliyunVodKey.KEY_VOD_COMMON_SIGNATURE_NONCE, AliyunVodHttpCommon.generateRandom());
        publicParams.put(AliyunVodKey.KEY_VOD_COMMON_REQUEST_ID, requestID);
        if (securityToken != null && securityToken.length() > 0) {
            publicParams.put(AliyunVodKey.KEY_VOD_COMMON_SECURITY_TOKEN, securityToken);
        }
        return publicParams;
    }

    /**
     * 生成OpenAPI地址
     *
     * @param privateParams
     * @return
     * @throws Exception
     */
    public static String generateOpenAPIURL(Map<String, String> publicParams, Map<String, String> privateParams, String accessKeySecret) {
        return generateURL(AliyunVodHttpCommon.VOD_DOMAIN, AliyunVodHttpCommon.HTTP_METHOD, publicParams, privateParams, accessKeySecret);
    }

    /**
     * 生成OpenAPI地址
     *
     * @param region
     * @param publicParams
     * @param privateParams
     * @param accessKeySecret
     * @return
     */
    public static String generateOpenAPIURL(String region, Map<String, String> publicParams, Map<String, String> privateParams, String accessKeySecret) {
        return generateURL(AliyunVodHttpCommon.generateVodDomain(region), AliyunVodHttpCommon.HTTP_METHOD, publicParams, privateParams, accessKeySecret);
    }

    /**
     * @param domain        请求地址
     * @param httpMethod    HTTP请求方式GET，POST等
     * @param publicParams  公共参数
     * @param privateParams 接口的私有参数
     * @return 最后的url
     */
    private static String generateURL(String domain, String httpMethod, Map<String, String> publicParams, Map<String, String> privateParams, String accessKeySecret) {
        List<String> allEncodeParams = AliyunVodSignature.getAllParams(publicParams, privateParams);
        String cqsString = AliyunVodSignature.getCQS(allEncodeParams);
        System.out.print("CanonicalizedQueryString = " + cqsString);
        String stringToSign = httpMethod + "&" + AliyunVodSignature.percentEncode("/") + "&" + AliyunVodSignature.percentEncode(cqsString);
        System.out.print("StringtoSign = " + stringToSign);
        String signature = AliyunVodSignature.hmacSHA1Signature(accessKeySecret, stringToSign);
        System.out.print("Signature = " + signature);
        return domain + "?" + cqsString + "&" + AliyunVodSignature.percentEncode(AliyunVodKey.KEY_VOD_COMMON_SIGNATURE) + "=" + AliyunVodSignature.percentEncode(signature);
    }


    private static String generateTags(List<String> tags) {
        String tag = "";
        if (tags == null || tags.size() <= 0)
            return tag;
        for (int i = 0; i < tags.size(); i++) {
            tag = tag + "," + tags.get(i).toString();
        }
        return trimFirstAndLastChar(tag, ',');
    }

    /**
     * 去除字符串首尾出现的某个字符.
     *
     * @param source  源字符串.
     * @param element 需要去除的字符.
     * @return String.
     */
    public static String trimFirstAndLastChar(String source, char element) {
        boolean beginIndexFlag = true;
        boolean endIndexFlag = true;
        do {
            int beginIndex = source.indexOf(element) == 0 ? 1 : 0;
            int endIndex = source.lastIndexOf(element) + 1 == source.length() ? source.lastIndexOf(element) : source.length();
            source = source.substring(beginIndex, endIndex);
            beginIndexFlag = (source.indexOf(element) == 0);
            endIndexFlag = (source.lastIndexOf(element) + 1 == source.length());
        } while (beginIndexFlag || endIndexFlag);
        return source;
    }
}
