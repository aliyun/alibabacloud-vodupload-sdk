/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.vod.upload.auth.AliyunVodAuth;
import com.alibaba.sdk.android.vod.upload.common.RequestIDSession;
import com.alibaba.sdk.android.vod.upload.common.UploadStateType;
import com.alibaba.sdk.android.vod.upload.common.VodUploadStateType;
import com.alibaba.sdk.android.vod.upload.common.utils.MD5;
import com.alibaba.sdk.android.vod.upload.common.utils.SharedPreferencesUtil;
import com.alibaba.sdk.android.vod.upload.common.utils.StringUtil;
import com.alibaba.sdk.android.vod.upload.common.utils.VideoInfoUtil;
import com.alibaba.sdk.android.vod.upload.exception.VODClientException;
import com.alibaba.sdk.android.vod.upload.exception.VODErrorCode;
import com.alibaba.sdk.android.vod.upload.internal.OSSPutUploaderImpl;
import com.alibaba.sdk.android.vod.upload.internal.OSSUploadListener;
import com.alibaba.sdk.android.vod.upload.internal.OSSUploader;
import com.alibaba.sdk.android.vod.upload.internal.ResumableUploaderImpl;
import com.alibaba.sdk.android.vod.upload.internal.ResumeableSession;
import com.alibaba.sdk.android.vod.upload.model.OSSConfig;
import com.alibaba.sdk.android.vod.upload.model.OSSUploadInfo;
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;
import com.alibaba.sdk.android.vod.upload.model.UserData;
import com.alibaba.sdk.android.vod.upload.model.VodInfo;
import com.alibaba.sdk.android.vod.upload.model.VodUploadResult;
import com.alibaba.sdk.android.vod.upload.session.VodHttpClientConfig;
import com.aliyun.auth.common.AliyunVodUploadType;
import com.aliyun.auth.core.AliyunVodErrorCode;
import com.aliyun.auth.model.CreateImageForm;
import com.aliyun.auth.model.CreateVideoForm;
import com.aliyun.vod.common.httpfinal.QupaiHttpFinal;
import com.aliyun.vod.common.utils.FileUtils;
import com.aliyun.vod.jasonparse.JSONSupport;
import com.aliyun.vod.jasonparse.JSONSupportImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Leigang
 * Created by Leigang on 16/7/2.
 */
public class VODUploadClientImpl implements OSSUploadListener, VODUploadClient {
    private OSSUploader upload;
    private WeakReference<Context> context;
    private UploadFileInfo curFileInfo;
    private VodUploadResult resultInfo;
    private OSSConfig ossConfig;
    private VodUploadStateType status;
    private VODUploadCallback callback;
    private ResumableVODUploadCallback resumableVODcallback;
    private List<UploadFileInfo> fileList;
    private AliyunVodAuth aliyunVodAuth;
    private boolean isTranscode = true;
    private String storageLocation;
    private String templateGroupId;
    private ResumeableSession resumeableSession;
    private RequestIDSession requestIDSession;
    private ClientConfiguration configuration;
    private JSONSupport jsonSupport;

    // 是否开启断点续传
    private boolean recordUploadProgressEnabled = true;
    // domain
    private String domainRegion = null;
    private String appId;
    private String workflowId;
    /**
     * 是否使用点播凭证方式上传,只有在设置setUploadAuthAndAddress即认为是点播凭证的方式上传
     */
    private boolean isVODAuthMode = false;

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    public VODUploadClientImpl(Context applicationContext) {
        QupaiHttpFinal.getInstance().initOkHttpFinal();
        this.context = new WeakReference<Context>(applicationContext);

        ossConfig = new OSSConfig();
        resultInfo = new VodUploadResult();
        resumeableSession = new ResumeableSession(applicationContext.getApplicationContext());
        requestIDSession = RequestIDSession.getInstance();
        aliyunVodAuth = new AliyunVodAuth(new AliyunAuthCallback());
        fileList = Collections.synchronizedList(new ArrayList<UploadFileInfo>());
    }

    @Override
    public void init(VODUploadCallback callback) {
        if (null == callback) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"callback\" cannot be null");
        }
        jsonSupport = new JSONSupportImpl();

        this.callback = callback;

        status = VodUploadStateType.INIT;
        isVODAuthMode = true;
    }

    /**
     * mian ak upload oss
     *
     * @param accessKeyId
     * @param accessKeySecret
     * @param callback
     */
    @Override
    public void init(String accessKeyId, String accessKeySecret, VODUploadCallback callback) {
        if (StringUtil.isEmpty(accessKeyId)) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"accessKeyId\" cannot be null");
        }

        if (StringUtil.isEmpty(accessKeySecret)) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"accessKeySecret\" cannot be null");
        }

        if (null == callback) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"callback\" cannot be null");
        }

        jsonSupport = new JSONSupportImpl();

        ossConfig.setAccessKeyId(accessKeyId);
        ossConfig.setAccessKeySecret(accessKeySecret);

        this.callback = callback;

        status = VodUploadStateType.INIT;
    }

    /**
     * OSS 通过STS方式上传
     * VOD 通过STS方式上传
     *
     * @param accessKeyId                        //临时accessKeyId
     * @param accessKeySecret//临时accessKeySecret
     * @param secrityToken//临时securityToken
     * @param expireTime//STStoken过期时间
     * @param callback//上传的监听
     */
    @Override
    public void init(String accessKeyId, String accessKeySecret, String secrityToken,
                     String expireTime, VODUploadCallback callback) {
        if (StringUtil.isEmpty(accessKeyId)) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"accessKeyId\" cannot be null");
        }

        if (StringUtil.isEmpty(accessKeySecret)) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"accessKeySecret\" cannot be null");
        }

        if ((StringUtil.isEmpty(secrityToken) && !StringUtil.isEmpty(expireTime)) ||
                (!StringUtil.isEmpty(secrityToken) && StringUtil.isEmpty(expireTime))) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"secrityToken\" and \"expireTime\" cannot be null");
        }

        if (null == callback) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"callback\" cannot be null");
        }

        OSSLog.logDebug("VODUpload", "init:STS:\n" + "\nAccessKeyId:" + accessKeyId + "\nAccessKeySecret:" + accessKeySecret + "\nSecrityToken:" + secrityToken + "\nexpireTime:" + expireTime);

        jsonSupport = new JSONSupportImpl();

        ossConfig.setAccessKeyIdToVOD(accessKeyId);
        ossConfig.setAccessKeySecretToVOD(accessKeySecret);
        ossConfig.setSecrityTokenToVOD(secrityToken);
        ossConfig.setExpireTimeToVOD(expireTime);


        if (callback instanceof ResumableVODUploadCallback) {
            this.resumableVODcallback = (ResumableVODUploadCallback) callback;
        } else if (callback instanceof VODUploadCallback) {
            this.callback = callback;
        }

        status = VodUploadStateType.INIT;
    }

    @Override
    public void setRegion(String region) {
        if (aliyunVodAuth == null) {
            aliyunVodAuth = new AliyunVodAuth(new AliyunAuthCallback());
        }
        aliyunVodAuth.setDomainRegion(region);
        domainRegion = region;
    }

    @Override
    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    @Override
    public void setRecordUploadProgressEnabled(boolean enabled) {
        recordUploadProgressEnabled = enabled;
        if (resumeableSession != null) {
            resumeableSession.setEnabled(enabled);
        }
    }

    @Override
    public void setVodHttpClientConfig(VodHttpClientConfig vodHttpClientConfig) {
        configuration = new ClientConfiguration();
        configuration.setMaxErrorRetry(vodHttpClientConfig.getMaxRetryCount());
        configuration.setConnectionTimeout(vodHttpClientConfig.getConnectionTimeout());
        configuration.setSocketTimeout(vodHttpClientConfig.getSocketTimeout());
    }

    @Override
    public void addFile(String localFilePath, VodInfo vodInfo) {
        UploadFileInfo info = new UploadFileInfo();
        info.setFilePath(localFilePath);
        info.setVodInfo(vodInfo);
        info.setStatus(UploadStateType.INIT);
        fileList.add(info);
    }

    @Override
    public void addFile(String localFilePath, String endpoint, String bucket,
                        String object) {
        if ((StringUtil.isEmpty(localFilePath))) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"localFilePath\" cannot be null");
        }

        if ((StringUtil.isEmpty(endpoint))) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"endpoint\" cannot be null");
        }

        if ((StringUtil.isEmpty(endpoint))) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"bucket\" cannot be null");
        }

        if ((StringUtil.isEmpty(object))) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"object\" cannot be null");
        }

        UploadFileInfo info = new UploadFileInfo();
        info.setFilePath(localFilePath);
        info.setEndpoint(endpoint);
        info.setBucket(bucket);
        info.setObject(object);
        info.setStatus(UploadStateType.INIT);
        fileList.add(info);
    }

    @Override
    public void addFile(String localFilePath, String endpoint, String bucket,
                        String object, VodInfo vodInfo) {
        if ((StringUtil.isEmpty(localFilePath))) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"localFilePath\" cannot be null");
        }

        if ((StringUtil.isEmpty(endpoint))) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"endpoint\" cannot be null");
        }

        if ((StringUtil.isEmpty(endpoint))) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"bucket\" cannot be null");
        }

        if ((StringUtil.isEmpty(object))) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"object\" cannot be null");
        }

        UploadFileInfo info = new UploadFileInfo();
        info.setFilePath(localFilePath);
        info.setEndpoint(endpoint);
        info.setBucket(bucket);
        info.setObject(object);
        info.setVodInfo(vodInfo);
        info.setStatus(UploadStateType.INIT);
        fileList.add(info);
    }

    @Override
    public void deleteFile(int index) {
        if (index < 0 || index >= fileList.size()) {
            throw new VODClientException(VODErrorCode.INVALID_ARGUMENT,
                    "index out of range");
        }

        UploadFileInfo uploadFileInfo = fileList.get(index);
        if (uploadFileInfo != null) {
            if (uploadFileInfo.getStatus() == UploadStateType.UPLOADING) {
                if (upload != null) {
                    upload.pause();
                }
            }
            if (resumeableSession != null) {
                resumeableSession.deleteResumeableFileInfo(uploadFileInfo.getFilePath());
            }
        }
        fileList.remove(index);
        status = VodUploadStateType.INIT;
    }

    @Override
    public void clearFiles() {
        if (fileList != null && fileList.size() > 0) {
            for (UploadFileInfo info : fileList) {
                if (info != null && resumeableSession != null) {
                    resumeableSession.deleteResumeableFileInfo(info.getFilePath());
                }
            }
        }
        fileList.clear();
        if (null != upload) {
            upload.cancel();
        }
        status = VodUploadStateType.INIT;
    }

    @Override
    public List<UploadFileInfo> listFiles() {
        return fileList;
    }

    @Override
    public void setPartSize(long partSize) {
        if (ossConfig != null) {
            ossConfig.setPartSize(partSize);
        }
    }

    /**
     * 是否开启转码
     */
    @Override
    public void setTranscodeMode(boolean bool) {
        isTranscode = bool;
    }

    /**
     * 指定视频存储区域
     *
     * @param storageLocation
     */
    @Override
    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    /**
     * 指定转码模板
     *
     * @param templateGroupId
     */
    @Override
    public void setTemplateGroupId(String templateGroupId) {
        this.templateGroupId = templateGroupId;
    }

    @Override
    public synchronized void start() {
        OSSLog.logDebug("[VODUploadClientImpl] - start called status: " + status);
        if (VodUploadStateType.STARTED != this.status && VodUploadStateType.PAUSED != this.status) {
            this.status = VodUploadStateType.STARTED;
            if (this.next()) {
            }
        } else {
            OSSLog.logDebug("[VODUploadClientImpl] - status: " + this.status + " cann't be start!");
        }
    }

    @Override
    public void pause() {
        OSSLog.logDebug("[VODUploadClientImpl] - pause called status: " + status);
        if (VodUploadStateType.STARTED != status) {
            OSSLog.logDebug("[VODUploadClientImpl] - status: " + status + " cann't be pause!");
            return;
        }

        if (curFileInfo == null) {
            return;
        }

        if (curFileInfo.getStatus() == UploadStateType.UPLOADING) {
            if (upload != null) {
                upload.pause();
            }
        }

        status = VodUploadStateType.PAUSED;
        OSSLog.logDebug("[VODUploadClientImpl] - pause called. status: " + status + "");
    }

    @Override
    public void resume() {
        OSSLog.logDebug("[VODUploadClientImpl] - resume called status: " + this.status);
        if (VodUploadStateType.PAUSED != this.status) {
            OSSLog.logDebug("[VODUploadClientImpl] - status: " + this.status + " cann't be resume!");
        } else {
            this.status = VodUploadStateType.STARTED;
            OSSLog.logDebug("[VODUploadClientImpl] - resume called. status: " + this.status + "");
            if (this.curFileInfo.getStatus() != UploadStateType.PAUSED && this.curFileInfo.getStatus() != UploadStateType.PAUSING) {
                if (this.curFileInfo.getStatus() == UploadStateType.CANCELED || this.curFileInfo.getStatus() == UploadStateType.SUCCESS || this.curFileInfo.getStatus() == UploadStateType.FAIlURE) {
                    this.next();
                }
            } else {
                if (upload != null) {
                    this.upload.resume();
                }
            }
        }
    }

    @Override
    public VodUploadStateType getStatus() {
        return status;
    }

    @Override
    public void stop() {
        OSSLog.logDebug("[VODUploadClientImpl] - stop called status: " + status);
        if (VodUploadStateType.STARTED != status && VodUploadStateType.PAUSED != status) {
            OSSLog.logDebug("[VODUploadClientImpl] - status: " + status + " cann't be stop!");
            return;
        }

        status = VodUploadStateType.STOPED;

        if (upload != null && curFileInfo != null) {
            if (curFileInfo.getStatus() == UploadStateType.UPLOADING) {
                upload.cancel();
            }
        }
    }

    @Override
    public void resumeWithAuth(String uploadAuth) {
        OSSLog.logDebug("[VODUploadClientImpl] - resumeWithAuth called status: " + status);
        if ((StringUtil.isEmpty(uploadAuth))) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"uploadAuth\" cannot be null");
        }
        try {
            byte[] jsonBytes = Base64.decode(uploadAuth, Base64.DEFAULT);
            String jsonString = new String(jsonBytes);
            JSONObject key = new JSONObject(jsonString);

            String expiration = key.optString("Expiration");
            String expirationUTC = key.optString("ExpireUTCTime");
            String expirationTime = !TextUtils.isEmpty(expirationUTC) ? expirationUTC : expiration;

            OSSLog.logDebug("[VODUploadClientImpl] resumeWithAuth : " + jsonString);

            resumeWithToken(key.optString("AccessKeyId"),
                    key.optString("AccessKeySecret"),
                    key.optString("SecurityToken"),
                    expirationTime);

        } catch (JSONException e) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"uploadAuth\" format is error");
        }
    }

    /**
     * vod sts upload resumeWithToken
     *
     * @param accessKeyId
     * @param accessKeySecret
     * @param secrityToken
     * @param expireTime
     */
    @Override
    public void resumeWithToken(String accessKeyId, String accessKeySecret, String secrityToken,
                                String expireTime) {
        OSSLog.logDebug("[VODUploadClientImpl] - resumeWithToken called status: " + status);
        if (VodUploadStateType.PAUSED != status && VodUploadStateType.FAIlURE != status && VodUploadStateType.GETVODAUTH != status) {
            OSSLog.logDebug("[VODUploadClientImpl] - status: " + status + " cann't be resume with token!");
            return;
        }

        ossConfig.setAccessKeyIdToVOD(accessKeyId);
        ossConfig.setAccessKeySecretToVOD(accessKeySecret);
        ossConfig.setSecrityTokenToVOD(secrityToken);
        ossConfig.setExpireTimeToVOD(expireTime);

        if (status == VodUploadStateType.GETVODAUTH) {
            needCreateVODUploadAuth();
            return;
        }

        status = VodUploadStateType.STARTED;
        if (upload != null) {
            upload.resume();
        }

    }

    @Override
    public void cancelFile(int index) {
        OSSLog.logDebug("[VODUploadClientImpl] - cancelFile called status: " + status);
        if (index < 0 || index >= fileList.size()) {
            throw new VODClientException(VODErrorCode.INVALID_ARGUMENT,
                    "index out of range");
        }

        UploadFileInfo uploadFileInfo = fileList.get(index);
        if (uploadFileInfo != null) {
            if (uploadFileInfo.getStatus() == UploadStateType.CANCELED) {
                OSSLog.logDebug("The file \"" + uploadFileInfo.getFilePath() + "\" is already canceled!");
                return;
            } else {
                if (uploadFileInfo.getStatus() == UploadStateType.UPLOADING) {
                    if (upload != null) {
                        upload.cancel();
                    }
                } else {
                    uploadFileInfo.setStatus(UploadStateType.CANCELED);
                }
                if (!recordUploadProgressEnabled && resumeableSession != null) {
                    resumeableSession.deleteResumeableFileInfo(uploadFileInfo.getFilePath(), true);
                }
            }
        }
    }

    @Override
    public void resumeFile(int index) {
        OSSLog.logDebug("[VODUploadClientImpl] - resumeFile called status: " + status);
        if (index >= 0 && index < this.fileList.size()) {
            UploadFileInfo uploadFileInfo = (UploadFileInfo) this.fileList.get(index);
            if (uploadFileInfo.getStatus() == UploadStateType.FAIlURE || uploadFileInfo.getStatus() == UploadStateType.CANCELED) {
                uploadFileInfo.setStatus(UploadStateType.INIT);
            }
            if (status == VodUploadStateType.STARTED && curFileInfo != null && curFileInfo.getStatus() != UploadStateType.UPLOADING) {
                next();
            }

        } else {
            throw new VODClientException("InvalidArgument", "index out of range");
        }
    }

    @Override
    public void onUploadSucceed() {
        if (callback != null) {
            callback.onUploadSucceed(curFileInfo);
        }

        if (resumeableSession != null && curFileInfo != null) {
            resumeableSession.deleteResumeableFileInfo(curFileInfo.getFilePath());
        }

        if (resumableVODcallback != null) {
            resumableVODcallback.onUploadFinished(curFileInfo, resultInfo);
        }

        next();
    }

    @Override
    public void onUploadFailed(String code, String message) {
        OSSLog.logDebug("[VODUploadClientImpl] - onUploadFailed: " + code + message);
        if (code.equals(UploadStateType.CANCELED.toString())) {
            OSSLog.logDebug("[VODUploadClientImpl] - onUploadFailed Canceled");
            if (status == VodUploadStateType.STARTED) {
                next();
            } else if (status == VodUploadStateType.STOPED) {
                curFileInfo.setStatus(UploadStateType.INIT);
            }
        } else {
            OSSLog.logDebug("[VODUploadClientImpl] - onUploadFailed Callback");

            OSSLog.logDebug("[VODUploadClientImpl] - onUploadFailed Callback " + callback);
            OSSLog.logDebug("[VODUploadClientImpl] - onUploadFailed Callback vod " + resumableVODcallback);
            if (callback != null) {
                callback.onUploadFailed(curFileInfo, code, message);
                status = VodUploadStateType.FAIlURE;
            }

            if (resumableVODcallback != null) {
                resumableVODcallback.onUploadFailed(curFileInfo, code, message);
                status = VodUploadStateType.FAIlURE;
            }
        }

    }

    @Override
    public void onUploadProgress(Object request, long uploadedSize, long totalSize) {
        if (callback != null) {
            callback.onUploadProgress(curFileInfo, uploadedSize, totalSize);
        }
        if (resumableVODcallback != null) {
            resumableVODcallback.onUploadProgress(curFileInfo, uploadedSize, totalSize);
        }
    }

    @Override
    public void onUploadTokenExpired() {
        OSSLog.logDebug("[VODUploadClientImpl] - onUploadTokenExpired");
        status = VodUploadStateType.PAUSED;

        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onUploadTokenExpired();
                }

                if (resumableVODcallback != null) {
                    resumableVODcallback.onUploadTokenExpired();
                }
            }
        });
    }

    @Override
    public void onUploadRetry(String code, String message) {
        if (callback != null) {
            callback.onUploadRetry(code, message);
        }

        if (resumableVODcallback != null) {
            resumableVODcallback.onUploadRetry(code, message);
        }
    }

    @Override
    public void onUploadRetryResume() {
        if (callback != null) {
            callback.onUploadRetryResume();
        }

        if (resumableVODcallback != null) {
            resumableVODcallback.onUploadRetryResume();
        }
    }

    /**
     * 1.上传成功之后上传下一个 ---> 上传失败依旧上传下一个
     * 2.开始上传文件必须是INIT状态才能上传，开始上传置为STARTED状态, 点播需要获取上传凭证 获取上传凭证状态为GETVODAUTH
     * 3.上传状态如果是stop状态. 再start需要从上传到第几个文件开始继续上传
     * 4.上传状态如果是pause状态，不能再开始LOG报错给用户 需要resume
     * 5.上传状态如果是UPLOADING状态，不能再start，resume 只能stop/pause
     * 6.只要有一个文件上传失败，上传状态就是FAIlURE // 只有在整个列表文件都失败才会FAIlURE??
     * 7.只有所有文件上传完成了才是FINISHED状态
     *
     * @return
     */
    private boolean next() {
        if (this.status != VodUploadStateType.PAUSED && this.status != VodUploadStateType.STOPED) {
            for (int i = 0; i < fileList.size(); i++) {
                if (fileList.get(i).getStatus() == UploadStateType.INIT) {
                    curFileInfo = fileList.get(i);

                    if (needCreateVODUploadAuth()) {
                        return false;
                    }
                    if (callback != null) {
                        callback.onUploadStarted(curFileInfo);
                    }

                    startUpload(curFileInfo);

                    return true;
                }
            }

            status = VodUploadStateType.FINISHED;
            return false;
        } else {
            return false;
        }
    }

    private boolean needCreateVODUploadAuth() {
        OSSLog.logDebug("[VODUploadClientImpl] - needCreateVODUploadAuth");
        boolean isSTSMode = isSTSMode(curFileInfo);
        //只有在使用STS方式，且非点播上传凭证方式上传才允许通过OpenApi获取点播凭证
        if (isSTSMode && !isVODAuthMode) {
            String mimeType = null;
            try {
                OSSLog.logDebug("[VODUploadClientImpl] filePath : " + curFileInfo.getFilePath());
                String encodePath = FileUtils.percentEncode(curFileInfo.getFilePath());
                mimeType = FileUtils.getMimeType(encodePath);
            } catch (Exception e) {
                e.printStackTrace();
                if (resumableVODcallback != null) {
                    resumableVODcallback.onUploadFailed(curFileInfo, VODErrorCode.FILE_NOT_EXIST,
                            "The file \"" + curFileInfo.getFilePath() + "\" is not exist!");
                }
                return true;
            }
            OSSLog.logDebug("[VODUploadClientImpl] file mimeType : " + mimeType);
            if (TextUtils.isEmpty(mimeType)) {
                if (resumableVODcallback != null) {
                    resumableVODcallback.onUploadFailed(curFileInfo, VODErrorCode.FILE_NOT_EXIST,
                            "The file mimeType\"" + curFileInfo.getFilePath() + "\" is not recognized!");
                }
                return true;
            }
            status = VodUploadStateType.GETVODAUTH;
            if (mimeType.substring(0, mimeType.lastIndexOf("/")).equals("video") || mimeType.substring(0, mimeType.lastIndexOf("/")).equals("audio")) {
                curFileInfo.getVodInfo().setFileName(new File(curFileInfo.getFilePath()).getName());
                String videoid = resumeableSession.getResumeableFileVideoID(curFileInfo.getFilePath());
                try {
                    UserData userData = VideoInfoUtil.getVideoBitrate(curFileInfo.getFilePath());

                    String customJson = curFileInfo.getVodInfo().getUserData();
                    String videoJson = jsonSupport.writeValue(userData);

                    OSSLog.logDebug("[VODUploadClientImpl] - userdata-custom : " + customJson);
                    OSSLog.logDebug("[VODUploadClientImpl] - userdata-video : " + videoJson);

                    if (!TextUtils.isEmpty(videoJson)) {
                        curFileInfo.getVodInfo().setUserData(videoJson);
                    }
                    if (!TextUtils.isEmpty(customJson)) {
                        curFileInfo.getVodInfo().setUserData(customJson);
                    }
                    if (!TextUtils.isEmpty(videoJson) && !TextUtils.isEmpty(customJson)) {
                        JSONObject a = new JSONObject(videoJson);
                        JSONObject b = new JSONObject(customJson);
                        JSONObject c = new JSONObject();
                        try {
                            Iterator<String> it = a.keys();
                            while (it.hasNext()) {
                                String key = it.next();
                                c.put(key, a.get(key));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            Iterator<String> it = b.keys();
                            while (it.hasNext()) {
                                String key = it.next();
                                c.put(key, b.get(key));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        OSSLog.logDebug("[VODUploadClientImpl] - userdata : " + c.toString());
                        curFileInfo.getVodInfo().setUserData(c.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    curFileInfo.getVodInfo().setUserData(null);
                }
                if (!TextUtils.isEmpty(videoid)) {
                    aliyunVodAuth.refreshUploadVideo(ossConfig.getAccessKeyIdToVOD(), ossConfig.getAccessKeySecretToVOD(), ossConfig.getSecrityTokenToVOD(), videoid, resultInfo.getImageUrl(), requestIDSession.getRequestID());
                } else {
                    aliyunVodAuth.createUploadVideo(ossConfig.getAccessKeyIdToVOD(), ossConfig.getAccessKeySecretToVOD(), ossConfig.getSecrityTokenToVOD(), curFileInfo.getVodInfo(), isTranscode, templateGroupId, storageLocation,
                            workflowId, appId, requestIDSession.getRequestID());
                }
            } else if (mimeType.substring(0, mimeType.lastIndexOf("/")).equals("image")) {
                aliyunVodAuth.createUploadImage(ossConfig.getAccessKeyIdToVOD(), ossConfig.getAccessKeySecretToVOD(), ossConfig.getSecrityTokenToVOD(), curFileInfo.getVodInfo(), workflowId, appId, requestIDSession.getRequestID());
            }

            return true;
        }
        return false;
    }

    /**
     * 是否使用STS方式上传到点播，目前认为如果上传前的文件信息没有OSS相关的信息即为STS上传
     *
     * @param uploadFileInfo
     * @return
     */
    private boolean isSTSMode(UploadFileInfo uploadFileInfo) {
        if (uploadFileInfo.getBucket() == null || uploadFileInfo.getEndpoint() == null
                || uploadFileInfo.getObject() == null) {
            return true;
        }
        return false;
    }

    @Override
    public void setUploadAuthAndAddress(UploadFileInfo uploadFileInfo,
                                        String uploadAuth,
                                        String uploadAddress) {
        UploadFileInfo curInfo = null;
        if (null == uploadFileInfo) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"uploadFileInfo\" cannot be null");
        }

        if ((StringUtil.isEmpty(uploadAuth))) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"uploadAuth\" cannot be null");
        }

        if ((StringUtil.isEmpty(uploadAddress))) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"uploadAddress\" cannot be null");
        }

        for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i).getFilePath().equals(uploadFileInfo.getFilePath())
                    && fileList.get(i).getStatus() == UploadStateType.INIT) {
                OSSLog.logDebug("setUploadAuthAndAddress" + uploadFileInfo.getFilePath());
                fileList.get(i).setStatus(UploadStateType.INIT);
                curInfo = fileList.get(i);
                break;
            }
        }

        if (null == curInfo) {
            throw new VODClientException(VODErrorCode.INVALID_ARGUMENT,
                    "The specified parameter \"uploadFileInfo\" is invalid");
        }

        try {

            byte[] authJsonBytes = Base64.decode(uploadAuth, Base64.DEFAULT);
            String authJsonString = new String(authJsonBytes);
            JSONObject authKey = new JSONObject(authJsonString);

            ossConfig.setAccessKeyId(authKey.optString("AccessKeyId"));
            ossConfig.setAccessKeySecret(authKey.optString("AccessKeySecret"));
            ossConfig.setSecrityToken(authKey.optString("SecurityToken"));
            ossConfig.setExpireTime(authKey.optString("Expiration"));

            // !!important, 点播凭证方式要使用服务端返回的region，外部设置无效
            String region = authKey.optString("Region");
            OSSLog.logDebug("VODSTS", "region : " + region);
            if (!TextUtils.isEmpty(region)) {
                if (aliyunVodAuth == null) {
                    aliyunVodAuth = new AliyunVodAuth(new AliyunAuthCallback());
                }
                aliyunVodAuth.setDomainRegion(region);
                domainRegion = region;
            }

            String expirationUTCTime = authKey.optString("ExpireUTCTime");
            OSSLog.logDebug("VODSTS", "expirationUTCTime : " + expirationUTCTime);
            // update expirateUTCTime
            if (!TextUtils.isEmpty(expirationUTCTime)) {
                ossConfig.setExpireTime(expirationUTCTime);
            }

            OSSLog.logDebug("VODSTS", "AccessKeyId:" + ossConfig.getAccessKeyId() + "\nAccessKeySecret:" + ossConfig.getAccessKeySecret() + "\nSecrityToken:" + ossConfig.getSecrityToken() + "\nRegion:" + region);
        } catch (JSONException e) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"uploadAuth\" format is error");
        }

        try {

            byte[] addressJsonBytes = Base64.decode(uploadAddress, Base64.DEFAULT);
            String addressJsonString = new String(addressJsonBytes);
            JSONObject addressKey = new JSONObject(addressJsonString);

            curInfo.setEndpoint(addressKey.optString("Endpoint"));
            curInfo.setBucket(addressKey.optString("Bucket"));
            curInfo.setObject(addressKey.optString("FileName"));

            curFileInfo = curInfo;

            OSSUploadInfo ossUploadInfo =
                    SharedPreferencesUtil.getUploadInfo(context.get(), ResumeableSession.SHAREDPREFS_OSSUPLOAD, curFileInfo.getFilePath());

            if (ossUploadInfo != null && MD5.checkMD5(ossUploadInfo.getMd5(), new File(curFileInfo.getFilePath()))) {
                curFileInfo = resumeableSession.getResumeableFileInfo(curFileInfo, resultInfo.getVideoid());
            } else {
                resumeableSession.saveResumeableFileInfo(curFileInfo, resultInfo.getVideoid());
            }

            ossConfig.setUploadAddress(uploadAddress);
        } catch (JSONException e) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"uploadAddress\" format is error");
        }
    }

    class AliyunAuthCallback implements AliyunVodAuth.VodAuthCallBack {
        @Override
        public void onCreateUploadImaged(CreateImageForm createImageForm) {
            status = VodUploadStateType.STARTED;
            setUploadAuthAndAddress(curFileInfo, createImageForm.getUploadAuth(), createImageForm.getUploadAddress());
            resultInfo.setImageUrl(createImageForm.getImageURL());
            startUpload(curFileInfo);
        }

        @Override
        public void onCreateUploadVideoed(CreateVideoForm createVideoForm, String coverUrl) {
            status = VodUploadStateType.STARTED;
            Log.d("VodUpload", createVideoForm.getVideoId());
            resultInfo.setVideoid(createVideoForm.getVideoId());
            ossConfig.setVideoId(createVideoForm.getVideoId());
            ossConfig.setUploadAddress(createVideoForm.getUploadAddress());
            setUploadAuthAndAddress(curFileInfo, createVideoForm.getUploadAuth(), createVideoForm.getUploadAddress());
            startUpload(curFileInfo);

        }

        @Override
        public void onSTSExpired(AliyunVodUploadType uploadType) {
            resumableVODcallback.onUploadTokenExpired();
        }

        @Override
        public void onError(String code, String message) {
            // 处理videoid服务端不存在的情况，需要删除本地的埋点
            if (AliyunVodErrorCode.VODERRORCODE_INVALIDVIDEO.equals(code)) {
                if (resumeableSession != null) {
                    String filePath = curFileInfo.getFilePath();
                    resumeableSession.deleteResumeableFileInfo(filePath);
                    needCreateVODUploadAuth();
                    return;
                }
            }
            resumableVODcallback.onUploadFailed(curFileInfo, code, message);


        }
    }

    /**
     * 开始上传
     */
    private void startUpload(UploadFileInfo uploadFileInfo) {
        if (new File(uploadFileInfo.getFilePath()).length() < 100 * 1024) {
            upload = null;
            upload = new OSSPutUploaderImpl(context.get());
            upload.init(ossConfig, this);

            upload.setOSSClientConfiguration(configuration);

            try {
                upload.start(uploadFileInfo);
            } catch (FileNotFoundException e) {
                e.printStackTrace();

                if (resumableVODcallback != null) {
                    resumableVODcallback.onUploadFailed(curFileInfo, VODErrorCode.FILE_NOT_EXIST,
                            "The file \"" + curFileInfo.getFilePath() + "\" is not exist!");
                }

                if (callback != null) {
                    callback.onUploadFailed(curFileInfo, VODErrorCode.FILE_NOT_EXIST,
                            "The file \"" + curFileInfo.getFilePath() + "\" is not exist!");
                }
            }
        } else {
            upload = null;
            upload = new ResumableUploaderImpl(context.get());
            ((ResumableUploaderImpl) upload).setDomainRegion(domainRegion);
            upload.init(ossConfig, this);

            upload.setOSSClientConfiguration(configuration);

            try {
                upload.start(uploadFileInfo);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                resumableVODcallback.onUploadFailed(curFileInfo, VODErrorCode.FILE_NOT_EXIST,
                        "The file \"" + curFileInfo.getFilePath() + "\" is not exist!");
            }
        }
    }
}
