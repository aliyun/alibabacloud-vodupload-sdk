/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.aliyun.auth.core;

/**
 * Created by Mulberry on 2017/11/3.
 */

public class AliyunVodErrorCode {

    /**
     * ---公共错误码-----
     **/
    public static String VODERRORCODE_OPERATIONDENIED = "OperationDenied";//账号未开通视频点播服务
    public static String VODERRORCODE_OPERATIONDENIED_SUSPENDED = "OperationDenied.Suspended";//账号已欠费，请充值
    public static String VODERRORCODE_INTERNALERROR = "InternalError";//后台发生未知错误，请稍后重试或联系客服解决
    public static String VODERRORCODE_SERVICEUNAVAILABLE = "ServiceUnAvailable";//服务不可用
    public static String VODERRORCODE_MISSINGPARAMETER = "MissingParameter";//缺少参数
    public static String VODERRORCODE_INVALIDPARAMETER = "InvalidParameter";//参数无效
    public static String VODERRORCODE_FORBIDDEN = "Forbidden";//用户无权限执行该操作
    public static String VODERRORCODE_INVALIDSECURITYTOKEN_EXPIRED = "InvalidSecurityToken.Expired";
    public static String VODERRORCODE_INVALIDVIDEO = "InvalidVideo.NotFound";//找不到videoid

    /**
     * ---获取上传凭证错误码-----
     **/
    public static String VODERRORCODE_INVALIDPARAMETER_FILENAME = "InvalidParameter.FileName";//参数FileName无效
    public static String VODERRORCODE_FORBIDDEN_INITFAILED = "Forbidden.InitFailed";//服务开通时账号初始化失败
    public static String VODERRORCODE_ADDVIDEOFAILED = "AddVideoFailed";//创建视频信息失败，请稍后重试

    /**
     * ---刷新上传凭证错误码---
     **/
    public static String VODERRORCODE_INVALIDVIDEO_NOTFOUND = "InvalidVideo.NotFound";//视频不存在
    public static String VODERRORCODE_INVALIDVIDEO_DAMAGED = "InvalidVideo.Damaged";//视频创建有误或已被损坏

    public static String VODERRORCODE_HTTP_ABNORMAL = "Http.Abnormal";//网络异常

    /**---获取图片凭证的错误码--**/
//    private static String VODERRORCODE_OPERATIONDENIED = "OperationDenied";
//    private static String VODERRORCODE_OPERATIONDENIED = "OperationDenied";
//    private static String VODERRORCODE_OPERATIONDENIED = "OperationDenied";

}
