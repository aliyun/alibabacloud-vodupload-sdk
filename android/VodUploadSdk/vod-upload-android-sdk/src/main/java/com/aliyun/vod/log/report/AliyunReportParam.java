/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.aliyun.vod.log.report;

import android.text.TextUtils;

import com.aliyun.vod.common.utils.DateUtil;
import com.aliyun.vod.log.core.AliyunLogSignature;
import com.aliyun.vod.log.util.UUIDGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: AliyunReportParam
 * @CreateDate: 2018/11/16 下午1:33
 * @Description: for upload progress report param generate
 * @Version: 1.0
 */
public class AliyunReportParam {

    // for upload progress report
    // 支持region可配置, ex: https://vod.region.aliyuncs.com/
    private static final String DOMAIN_PREFIX = "http://vod.";
    private static final String DOMAIN_REGION = "cn-hangzhou";
    private static final String DOMAIN_SUFFIX = ".aliyuncs.com/";

    public static final String generateDomainWithRegion(String region) {
        return DOMAIN_PREFIX + (TextUtils.isEmpty(region) ? DOMAIN_REGION : region) + DOMAIN_SUFFIX;
    }

    // for upload progress report
    static final String UP_ACTION = "Action";
    static final String UP_SOURCE = "Source";
    static final String UP_CLIENTID = "ClientId";
    static final String UP_BUSINESSTYPE = "BusinessType";
    static final String UP_TERMINALTYPE = "TerminalType";
    static final String UP_DEVICEMODEL = "DeviceModel";
    static final String UP_APPVERSION = "AppVersion";
    static final String UP_AUTHTIMESTAMP = "AuthTimestamp";
    /**
     * AuthInfo=md5(UserId+|ClientId+|+secretKey+|+AuthTimestamp)
     * ClientId为空时：
     * AuthInfo=md5(UserId+|+secretKey+|+AuthTimestamp)
     */
    static final String UP_AUTHINFO = "AuthInfo";
    static final String UP_FILENAME = "FileName";
    static final String UP_FILESIZE = "FileSize";
    static final String UP_FILECREATETIME = "FileCreateTime";
    static final String UP_FILEHASH = "FileHash";
    static final String UP_UPLOADRATIO = "UploadRatio";
    static final String UP_UPLOADID = "UploadId";
    static final String UP_DONEPARTSCOUNT = "DonePartsCount";
    static final String UP_TOTALPART = "TotalPart";
    static final String UP_PARTSIZE = "PartSize";
    static final String UP_UPLOADPOINT = "UploadPoint";
    static final String UP_USERID = "UserId";
    static final String UP_VIDEOID = "VideoId";
    static final String UP_UPLOADADRESS = "UploadAddress";

    static final String generateUploadProgressParams(Map<String, String> publicParams, String accessSecretKey) {
        Map<String, String> privateParams = new HashMap<>();
        privateParams.put("Format", "JSON");
        privateParams.put("Version", "2017-03-14");
        privateParams.put("SignatureMethod", "HMAC-SHA1");
        privateParams.put("SignatureNonce", UUIDGenerator.generateRequestID());
        privateParams.put("SignatureVersion", "1.0");
        privateParams.put("Timestamp", DateUtil.generateTimestamp());

        List<String> allEncodeParams = AliyunLogSignature.getAllParams(publicParams, privateParams);
        String cqsString = AliyunLogSignature.getCQS(allEncodeParams);
        String stringToSign = "POST" + "&" + AliyunLogSignature.percentEncode("/") + "&" + AliyunLogSignature.percentEncode(cqsString);
        String signature = AliyunLogSignature.hmacSHA1Signature(accessSecretKey, stringToSign);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("?");
        stringBuilder.append(cqsString);
        stringBuilder.append("&");
        stringBuilder.append(AliyunLogSignature.percentEncode("Signature"));
        stringBuilder.append("=");
        stringBuilder.append(AliyunLogSignature.percentEncode(signature));
        return stringBuilder.toString();

    }
}
