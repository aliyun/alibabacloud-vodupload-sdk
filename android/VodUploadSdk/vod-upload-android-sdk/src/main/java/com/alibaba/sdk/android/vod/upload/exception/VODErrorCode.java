/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload.exception;

/**
 * CVS定义的错误代码。
 */
public class VODErrorCode {
    /**
     * 参数格式错误。
     */
    public static final String INVALID_ARGUMENT = "InvalidArgument";

    /**
     * 缺少必须参数。
     */
    public static final String MISSING_ARGUMENT = "MissingArgument";
    /**
     * 文件已经存在。
     */
    public static final String FILE_ALREADY_EXIST = "FileAlreadyExist";
    /**
     * 文件不存在。
     */
    public static final String FILE_NOT_EXIST = "FileNotExist";

    /**
     * 文件已经取消。
     */
    public static final String FILE_ALREADY_CANCEL = "FileAlreadyCancel";
    /**
     * 上传没有过期。
     */
    public static final String UPLOAD_NOT_EXPIRE = "UploadNotExpire";
    /**
     * 上传还未开始。
     */
    public static final String UPLOAD_NOT_START = "UploadNotStart";

    /**
     * 上传步骤错误
     */
    public static final String UPLOAD_STEP_NOT_IDLE = "Step Not Idle";

    /**
     * 上传状态错误
     */
    public static final String UPLOAD_STATUS_RRROR = "Upload Status  Error";

    /**
     * 上传Token过期
     */
    public static final String UPLOAD_EXPIRED = "UploadTokenExpired";

    /**
     * SecurityTokenExpired
     */
    public static final String SECURITY_TOKEN_EXPIRED = "SecurityTokenExpired";

    /**
     * Permission Denied
     */
    public static final String PERMISSION_DENIED = "PermissionDenied";

}
