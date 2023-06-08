/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.vod.upload.auth.AliyunVodAuth;
import com.alibaba.sdk.android.vod.upload.common.RequestIDSession;
import com.alibaba.sdk.android.vod.upload.common.UploadStateType;
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
import com.alibaba.sdk.android.vod.upload.model.SVideoConfig;
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;
import com.alibaba.sdk.android.vod.upload.model.UserData;
import com.alibaba.sdk.android.vod.upload.model.VodInfo;
import com.alibaba.sdk.android.vod.upload.session.VodSessionCreateInfo;
import com.aliyun.auth.common.AliyunVodUploadType;
import com.aliyun.auth.core.AliyunVodErrorCode;
import com.aliyun.auth.model.CreateImageForm;
import com.aliyun.auth.model.CreateVideoForm;
import com.aliyun.vod.common.httpfinal.QupaiHttpFinal;
import com.aliyun.vod.jasonparse.JSONSupport;
import com.aliyun.vod.jasonparse.JSONSupportImpl;
import com.aliyun.vod.log.core.AliyunLogger;
import com.aliyun.vod.log.core.AliyunLoggerManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.alibaba.sdk.android.vod.upload.VODSVideoUploadClientImpl.AliyunVodUploadStep.VODSVideoStepUploadImage;
import static com.alibaba.sdk.android.vod.upload.VODSVideoUploadClientImpl.AliyunVodUploadStep.VODSVideoStepUploadVideo;

public class VODSVideoUploadClientImpl implements VODSVideoUploadClient {
    private static final String TAG = "VOD_UPLOAD";
    private static final int VOD_GENERATE_VIDEO = 1;
    private static final int VOD_GENERATE_IMAGE = 1;
    private String uploadAuth;
    private String uploadAddress;
    private long imageSize;
    private long videoSize;

    private WeakReference<Context> context;
    private String domainRegion;
    private AliyunVodAuth aliyunVodAuth;
    private List<UploadFileInfo> fileList;
    private OSSUploader uploader;

    private OSSConfig ossConfig;
    private AliyunVodUploadStep step;
    private AliyunVodUploadStatus status;
    private SVideoConfig sVideoConfig;
    private JSONSupport jsonSupport;
    private ResumeableSession resumeableSession;
    private RequestIDSession requestIDSession;
    private VODSVideoUploadCallback videoUploadCallback;

    private ClientConfiguration configuration;

    // 是否上报埋点
    private boolean reportEnabled = true;

    public enum AliyunVodUploadStep {
        //初始状态
        VODSVideoStepIdle,
        //创建图片凭证
        VODSVideoStepCreateImage,
        //图片凭证创建完成
        VODSVideoStepCreateImageFinish,
        //上传图片
        VODSVideoStepUploadImage,
        //图片上传完成
        VODSVideoStepUploadImageFinish,
        //创建视频凭证
        VODSVideoStepCreateVideo,
        //创建视频凭证完成
        VODSVideoStepCreateVideoFinish,
        //上传视频
        VODSVideoStepUploadVideo,
        //上传视频
        VODSVideoStepFinish
    }

    public enum AliyunVodUploadStatus {
        //初始状态
        VODSVideoStatusIdle,
        //恢复状态
        VODSVideoStatusResume,
        //暂停状态
        VODSVideoStatusPause,
        //取消上传
        VODSVideoStatusCancel,
        //释放资源
        VODSVideoStatusRelease
    }


    public VODSVideoUploadClientImpl(Context context) {
        reportEnabled = true;
        this.context = new WeakReference<Context>(context);
        QupaiHttpFinal.getInstance().initOkHttpFinal();
        fileList = Collections.synchronizedList(new ArrayList<UploadFileInfo>());
        ossConfig = new OSSConfig();
        resumeableSession = new ResumeableSession(context.getApplicationContext());
        requestIDSession = new RequestIDSession();
        sVideoConfig = new SVideoConfig();
        AliyunLoggerManager.createLogger(context.getApplicationContext(), VODUploadClientImpl.class.getName());
    }

    public void setReportEnabled(boolean enabled) {
        reportEnabled = enabled;
        AliyunLoggerManager.toggleLogger(reportEnabled);
    }

    @Override
    public void init() {
        jsonSupport = new JSONSupportImpl();
        step = AliyunVodUploadStep.VODSVideoStepIdle;
        status = AliyunVodUploadStatus.VODSVideoStatusIdle;
    }

    @Override
    public void setRegion(String region) {
        domainRegion = region;
    }

    @Override
    public void setRecordUploadProgressEnabled(boolean enabled) {
        if (resumeableSession != null) {
            resumeableSession.setEnabled(enabled);
        }
    }

    @Override
    public void uploadWithVideoAndImg(VodSessionCreateInfo vodSessionCreateInfo, VODSVideoUploadCallback callback) {

        if (StringUtil.isEmpty(vodSessionCreateInfo.getAccessKeyId())) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"accessKeyId\" cannot be null");
        }

        if (StringUtil.isEmpty(vodSessionCreateInfo.getAccessKeySecret())) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"accessKeySecret\" cannot be null");
        }

        if (StringUtil.isEmpty(vodSessionCreateInfo.getSecurityToken())) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"securityToken\" cannot be null");
        }

        if (StringUtil.isEmpty(vodSessionCreateInfo.getExpriedTime())) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"expriedTime\" cannot be null");
        }

        if (!new File(vodSessionCreateInfo.getVideoPath()).exists()) {
            throw new VODClientException(VODErrorCode.FILE_NOT_EXIST,
                    "The specified parameter \"videoPath\" file not exists");
        }

        if (!new File(vodSessionCreateInfo.getImagePath()).exists()) {
            throw new VODClientException(VODErrorCode.FILE_NOT_EXIST,
                    "The specified parameter \"imagePath\" file not exists");
        }
        if (null == callback) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"callback\" cannot be null");
        }

        this.videoUploadCallback = callback;
        if (aliyunVodAuth == null) {
            aliyunVodAuth = new AliyunVodAuth(new AliyunAuthCallback());
        }
        aliyunVodAuth.setDomainRegion(domainRegion);

        if (AliyunVodUploadStatus.VODSVideoStatusPause == status ||
                AliyunVodUploadStatus.VODSVideoStatusRelease == status) {
            OSSLog.logDebug("[VODSVideoUploadClientImpl] - status: " + status + " cann't be start upload!");
            return;
        }

        final AliyunLogger logger = AliyunLoggerManager.getLogger(VODUploadClientImpl.class.getName());
        if (logger != null) {
            logger.setRequestID(vodSessionCreateInfo.getRequestID(), false);
            logger.setProductSVideo(true);
        }

        sVideoConfig.setAccessKeyId(vodSessionCreateInfo.getAccessKeyId());
        sVideoConfig.setAccessKeySecret(vodSessionCreateInfo.getAccessKeySecret());
        sVideoConfig.setSecrityToken(vodSessionCreateInfo.getSecurityToken());
        sVideoConfig.setExpriedTime(vodSessionCreateInfo.getExpriedTime());
        sVideoConfig.setVideoPath(vodSessionCreateInfo.getVideoPath());
        sVideoConfig.setImagePath(vodSessionCreateInfo.getImagePath());
        sVideoConfig.setTranscode(vodSessionCreateInfo.isTranscode());
        sVideoConfig.setPartSize(vodSessionCreateInfo.getPartSize());
        sVideoConfig.setRequestId(vodSessionCreateInfo.getRequestID());
        sVideoConfig.setTemplateGroupId(vodSessionCreateInfo.getTemplateGroupId());
        sVideoConfig.setStorageLocation(vodSessionCreateInfo.getStorageLocation());
        sVideoConfig.setWorkFlowId(vodSessionCreateInfo.getWorkFlowId());
        sVideoConfig.setAppId(vodSessionCreateInfo.getAppId());
        // for userdata
        sVideoConfig.setUserData(vodSessionCreateInfo.getSvideoInfo().getUserData());

        imageSize = new File(vodSessionCreateInfo.getImagePath()).length();
        videoSize = new File(vodSessionCreateInfo.getVideoPath()).length();

        //Set OSS Config
        ossConfig.setAccessKeyId(sVideoConfig.getAccessKeyId());
        ossConfig.setAccessKeySecret(sVideoConfig.getAccessKeySecret());
        ossConfig.setSecrityToken(sVideoConfig.getSecrityToken());
        ossConfig.setExpireTime(sVideoConfig.getExpriedTime());
        ossConfig.setPartSize(sVideoConfig.getPartSize());

        //init addFiles
        VodInfo vodInfo = new VodInfo();
        vodInfo.setTitle(vodSessionCreateInfo.getSvideoInfo().getTitle());
        vodInfo.setDesc(vodSessionCreateInfo.getSvideoInfo().getDesc());
        vodInfo.setCateId(vodSessionCreateInfo.getSvideoInfo().getCateId());
        vodInfo.setTags(vodSessionCreateInfo.getSvideoInfo().getTags());
        vodInfo.setIsProcess(vodSessionCreateInfo.getSvideoInfo().isProcess());
        vodInfo.setIsShowWaterMark(vodSessionCreateInfo.getSvideoInfo().isShowWaterMark());
        vodInfo.setPriority(vodSessionCreateInfo.getSvideoInfo().getPriority());
        vodInfo.setUserData(vodSessionCreateInfo.getSvideoInfo().getUserData());
        sVideoConfig.setVodInfo(vodInfo);
        addFile(sVideoConfig.getVodInfo());

        //http config
        configuration = new ClientConfiguration();
        configuration.setMaxErrorRetry(vodSessionCreateInfo.getVodHttpClientConfig().getMaxRetryCount());
        configuration.setConnectionTimeout(vodSessionCreateInfo.getVodHttpClientConfig().getConnectionTimeout());
        configuration.setSocketTimeout(vodSessionCreateInfo.getVodHttpClientConfig().getSocketTimeout());

        createUploadImage();
    }

    private void addFile(VodInfo vodInfo) {
        UploadFileInfo info = new UploadFileInfo();
        info.setFilePath(sVideoConfig.getImagePath());
        info.setFileType(UploadFileInfo.UPLOAD_FILE_TYPE_IMAGE);
        info.setVodInfo(vodInfo);
        info.setStatus(UploadStateType.INIT);
        fileList.add(info);

        UploadFileInfo videoInfo = new UploadFileInfo();
        videoInfo.setFilePath(sVideoConfig.getVideoPath());
        info.setFileType(UploadFileInfo.UPLOAD_FILE_TYPE_VIDEO);
        videoInfo.setVodInfo(vodInfo);
        videoInfo.setStatus(UploadStateType.INIT);
        fileList.add(videoInfo);
    }

    @Override
    public void refreshSTSToken(String accessKeyId, String accessKeySecret, String securityToken, String expriedTime) {
        if (StringUtil.isEmpty(accessKeyId)) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"accessKeyId\" cannot be null");
        }

        if (StringUtil.isEmpty(accessKeySecret)) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"accessKeySecret\" cannot be null");
        }

        if (StringUtil.isEmpty(securityToken)) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"accessToken\" cannot be null");
        }

        if (StringUtil.isEmpty(expriedTime)) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"expriedTime\" cannot be null");
        }

        sVideoConfig.setAccessKeyId(accessKeyId);
        sVideoConfig.setAccessKeySecret(accessKeySecret);
        sVideoConfig.setSecrityToken(securityToken);
        sVideoConfig.setExpriedTime(expriedTime);

        ossConfig.setAccessKeyId(sVideoConfig.getAccessKeyId());
        ossConfig.setAccessKeySecret(sVideoConfig.getAccessKeySecret());
        ossConfig.setSecrityToken(sVideoConfig.getSecrityToken());
        ossConfig.setExpireTime(sVideoConfig.getExpriedTime());

        refreshSTStoken();
    }

    private void createUploadImage() {
        if (sVideoConfig.getAccessKeyId() != null && sVideoConfig.getAccessKeySecret() != null &&
                sVideoConfig.getSecrityToken() != null && aliyunVodAuth != null) {
            step = AliyunVodUploadStep.VODSVideoStepCreateImage;
            aliyunVodAuth.createUploadImage(sVideoConfig.getAccessKeyId(), sVideoConfig.getAccessKeySecret(), sVideoConfig.getSecrityToken(),
                    sVideoConfig.getVodInfo(),sVideoConfig.getStorageLocation(),sVideoConfig.getAppId(), sVideoConfig.getRequestId() == null ? requestIDSession.getRequestID() : sVideoConfig.getRequestId(),
                true);
            OSSLog.logDebug(TAG, "VODSVideoStepCreateImage");
            OSSLog.logDebug(TAG, "[VODSVideoUploader] - status: " + " VODSVideoStepCreateImage");
        }
    }

    private void refreshSTStoken() {
        OSSLog.logDebug(TAG, "[VODSVideoUploader]: " + " RefreshSTStoken");
        if (status == AliyunVodUploadStatus.VODSVideoStatusPause || status == AliyunVodUploadStatus.VODSVideoStatusCancel) {
            OSSLog.logDebug(TAG, "[VODSVideoUploader] - status: " + status + " cann't be refreshSTStoken!");
            return;
        }
        if (step == AliyunVodUploadStep.VODSVideoStepUploadVideo || step == VODSVideoStepUploadImage) {
            if (uploader != null) {
                uploader.resume();
            }
        } else {
            if (step == AliyunVodUploadStep.VODSVideoStepCreateImage) {
                aliyunVodAuth.createUploadImage(sVideoConfig.getAccessKeyId(), sVideoConfig.getAccessKeySecret(), sVideoConfig.getSecrityToken(),
                        sVideoConfig.getVodInfo(),sVideoConfig.getStorageLocation(),sVideoConfig.getAppId(),sVideoConfig.getRequestId() == null ? requestIDSession.getRequestID() : sVideoConfig.getRequestId(),
                    true);
            } else if (step == AliyunVodUploadStep.VODSVideoStepCreateVideoFinish) {
                aliyunVodAuth.refreshUploadVideo(sVideoConfig.getAccessKeyId(), sVideoConfig.getAccessKeySecret(), sVideoConfig.getSecrityToken(), sVideoConfig.getVideoId(), sVideoConfig.getVodInfo().getCoverUrl(), sVideoConfig.getRequestId() == null ? requestIDSession.getRequestID() : sVideoConfig.getRequestId());
            } else if (step == AliyunVodUploadStep.VODSVideoStepCreateVideo) {
                String videoid = resumeableSession.getResumeableFileVideoID(sVideoConfig.getVideoPath());
                if (!TextUtils.isEmpty(videoid)) {
                    aliyunVodAuth.refreshUploadVideo(sVideoConfig.getAccessKeyId(), sVideoConfig.getAccessKeySecret(), sVideoConfig.getSecrityToken(), videoid, sVideoConfig.getVodInfo().getCoverUrl(), requestIDSession.getRequestID());
                } else {
                    aliyunVodAuth.createUploadVideo(sVideoConfig.getAccessKeyId(), sVideoConfig.getAccessKeySecret(), sVideoConfig.getSecrityToken(), sVideoConfig.getVodInfo(), sVideoConfig.isTranscode(), sVideoConfig.getTemplateGroupId(), sVideoConfig.getStorageLocation(),
                            sVideoConfig.getWorkFlowId(),sVideoConfig.getAppId(),sVideoConfig.getRequestId() == null ? requestIDSession.getRequestID() : sVideoConfig.getRequestId());
                }
            }
        }
    }

    private VodInfo generateVodInfo(int type, SVideoConfig sVideoConfig, String coverUrl) {
        VodInfo vodInfo = new VodInfo();
        vodInfo.setTitle(sVideoConfig.getVodInfo().getTitle());
        vodInfo.setDesc(sVideoConfig.getVodInfo().getDesc());
        if (type == VOD_GENERATE_VIDEO) {
            vodInfo.setFileName(new File(sVideoConfig.getVideoPath()).getName());
            try {
                UserData userData = VideoInfoUtil.getVideoBitrate(context.get(), sVideoConfig.getVideoPath());

                String customJson = sVideoConfig.getUserData();
                String videoJson = jsonSupport.writeValue(userData);

                OSSLog.logDebug("[VODSVideoUploadClientImpl] - userdata-custom : " + customJson);
                OSSLog.logDebug("[VODSVideoUploadClientImpl] - userdata-video : " + videoJson);

                if (!TextUtils.isEmpty(videoJson)) {
                    vodInfo.setUserData(videoJson);
                }
                if (!TextUtils.isEmpty(customJson)) {
                    vodInfo.setUserData(customJson);
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
                    OSSLog.logDebug("[VODSVideoUploadClientImpl] - userdata : " + c.toString());
                    vodInfo.setUserData(c.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
                vodInfo.setUserData(null);
            }
            vodInfo.setFileSize(String.valueOf(new File(sVideoConfig.getVideoPath()).length()));
            vodInfo.setIsProcess(sVideoConfig.getVodInfo().getIsProcess());
            vodInfo.setPriority(sVideoConfig.getVodInfo().getPriority());
            vodInfo.setIsShowWaterMark(sVideoConfig.getVodInfo().getIsShowWaterMark());
        } else {
            vodInfo.setFileName(new File(sVideoConfig.getImagePath()).getName());
        }

        vodInfo.setCateId(sVideoConfig.getVodInfo().getCateId());
        if (coverUrl != null) {
            vodInfo.setCoverUrl(coverUrl);
        }
        vodInfo.setTags(sVideoConfig.getVodInfo().getTags());

        return vodInfo;
    }

    @Override
    public void resume() {
        OSSLog.logDebug(TAG, "[VODSVideoUploader]: " + " resume");
        if (AliyunVodUploadStatus.VODSVideoStatusPause != status && AliyunVodUploadStatus.VODSVideoStatusIdle != status) {
            OSSLog.logDebug("[VODSVideoUploadClientImpl] - status: " + status + " cann't be resume!");
            return;
        }

        if (status == AliyunVodUploadStatus.VODSVideoStatusPause) {
            if (step == AliyunVodUploadStep.VODSVideoStepIdle || step == AliyunVodUploadStep.VODSVideoStepCreateImage
                    || step == AliyunVodUploadStep.VODSVideoStepCreateImageFinish
                    || step == AliyunVodUploadStep.VODSVideoStepCreateVideo) {
                createUploadImage();
            } else if (step == AliyunVodUploadStep.VODSVideoStepFinish) {
                // do nothing
            } else {
                if (uploader != null) {
                    uploader.resume();
                }
            }
            status = AliyunVodUploadStatus.VODSVideoStatusResume;
        }


    }

    @Override
    public void pause() {
        OSSLog.logDebug(TAG, "[VODSVideoUploader]: " + " pause");
        if (status == AliyunVodUploadStatus.VODSVideoStatusIdle ||
                status == AliyunVodUploadStatus.VODSVideoStatusResume) {

            if (uploader != null) {
                uploader.pause();
            }
            status = AliyunVodUploadStatus.VODSVideoStatusPause;
        } else {
            OSSLog.logDebug("[VODSVideoUploadClientImpl] - status: " + status + " cann't be pause!");
        }
    }

    @Override
    public void cancel() {
        OSSLog.logDebug(TAG, "[VODSVideoUploader]: " + "cancel");
        status = AliyunVodUploadStatus.VODSVideoStatusIdle;
        step = AliyunVodUploadStep.VODSVideoStepIdle;

        if (uploader != null) {
            uploader.cancel();
            fileList.clear();
            videoUploadCallback = null;

        }

        if (aliyunVodAuth != null) {
            aliyunVodAuth.cancel();
            aliyunVodAuth = null;
        }
    }

    @Override
    public void release() {
        if (sVideoConfig != null) {
            sVideoConfig = null;
        }

        if (aliyunVodAuth != null) {
            aliyunVodAuth = null;
        }

        if (uploader != null) {
            uploader = null;
        }

        if (videoUploadCallback != null) {
            videoUploadCallback = null;
        }

        status = AliyunVodUploadStatus.VODSVideoStatusRelease;
        step = AliyunVodUploadStep.VODSVideoStepIdle;
    }

    @Override
    public void setAppVersion(String appVersion) {
        final AliyunLogger logger = AliyunLoggerManager.getLogger(VODUploadClientImpl.class.getName());
        if (logger != null) {
            logger.setAppVersion(appVersion);
        }
    }

    class AliyunAuthCallback implements AliyunVodAuth.VodAuthCallBack {
        @Override
        public void onCreateUploadVideoed(CreateVideoForm createVideoForm, String coverUrl) {
            OSSLog.logDebug(TAG, "VODSVideoStepCreateVideoFinish");

            step = AliyunVodUploadStep.VODSVideoStepCreateVideoFinish;
            OSSLog.logDebug(TAG, "[VODSVideoUploader]: step" + step);
            sVideoConfig.setVodInfo(generateVodInfo(VOD_GENERATE_VIDEO, sVideoConfig, coverUrl));
            sVideoConfig.setVideoId(createVideoForm.getVideoId());
            uploadAuth = createVideoForm.getUploadAuth();
            uploadAddress = createVideoForm.getUploadAddress();

            try {
                byte[] authJsonBytes = Base64.decode(uploadAuth, Base64.DEFAULT);
                String authJsonString = new String(authJsonBytes);
                JSONObject authKey = new JSONObject(authJsonString);
                String region = authKey.optString("Region");
                String expirationUTCTime = authKey.optString("ExpireUTCTime");
                OSSLog.logDebug(TAG, "region : " + region + ", expUTC : " + expirationUTCTime);
                if (!TextUtils.isEmpty(region)) {
                    if (aliyunVodAuth == null) {
                        if (aliyunVodAuth == null) {
                            aliyunVodAuth = new AliyunVodAuth(new AliyunAuthCallback());
                        }
                    }
                    aliyunVodAuth.setDomainRegion(domainRegion);
                    domainRegion = region;
                }
                // TODO update expirateUTCTime
                if (!TextUtils.isEmpty(expirationUTCTime)) {
                    sVideoConfig.setExpriedTime(expirationUTCTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            startUpload(uploadAuth, uploadAddress, sVideoConfig);
        }

        @Override
        public void onCreateUploadImaged(CreateImageForm createImageForm) {
            OSSLog.logDebug(TAG, "[VODSVideoUploader]: step" + "VODSVideoStepCreateImageFinish");

            step = AliyunVodUploadStep.VODSVideoStepCreateImageFinish;
            sVideoConfig.setVodInfo(generateVodInfo(VOD_GENERATE_IMAGE, sVideoConfig, createImageForm.getImageURL()));

            uploadAuth = createImageForm.getUploadAuth();
            uploadAddress = createImageForm.getUploadAddress();

            try {
                byte[] authJsonBytes = Base64.decode(uploadAuth, Base64.DEFAULT);
                String authJsonString = new String(authJsonBytes);
                JSONObject authKey = new JSONObject(authJsonString);
                String region = authKey.optString("Region");
                String expirationUTCTime = authKey.optString("ExpireUTCTime");
                OSSLog.logDebug(TAG, "region : " + region + ", expUTC : " + expirationUTCTime);
                // TODO update expirateUTCTime
                if (!TextUtils.isEmpty(expirationUTCTime)) {
                    sVideoConfig.setExpriedTime(expirationUTCTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            startUpload(uploadAuth, uploadAddress, sVideoConfig);
        }

        @Override
        public void onSTSExpired(AliyunVodUploadType uploadType) {
            OSSLog.logDebug(TAG, "[VODSVideoUploader]: status" + "onSTSExpired");
            if (videoUploadCallback != null) {
                videoUploadCallback.onSTSTokenExpried();
            }
        }

        @Override
        public void onError(String code, String message) {
            OSSLog.logDebug(TAG, "[VODSVideoUploader]: onCreateAuthError" + "code" + code + "message" + message);
            // 处理videoid服务端不存在的情况，需要删除本地的埋点
            if (AliyunVodErrorCode.VODERRORCODE_INVALIDVIDEO.equals(code)) {
                if (resumeableSession != null) {
                    String filePath = null;
                    if (step == AliyunVodUploadStep.VODSVideoStepCreateImage) {
                        filePath = sVideoConfig.getImagePath();
                    } else if (step == AliyunVodUploadStep.VODSVideoStepCreateVideo) {
                        filePath = sVideoConfig.getVideoPath();
                    }
                    resumeableSession.deleteResumeableFileInfo(filePath);
                    if (step == AliyunVodUploadStep.VODSVideoStepCreateVideo) {
                        refreshSTStoken();
                        return;
                    }
                }
            }
            if (videoUploadCallback != null) {
                videoUploadCallback.onUploadFailed(code, message);
            }
        }

    }

    class OSSUploadCallback implements OSSUploadListener {

        @Override
        public void onUploadSucceed() {
            if (step == AliyunVodUploadStep.VODSVideoStepUploadVideo) {
                OSSLog.logDebug(TAG, "[VODSVideoUploader]: step" + "VODSVideoStepUploadVideoFinish");
                if (resumeableSession != null && sVideoConfig != null) {
                    resumeableSession.deleteResumeableFileInfo(sVideoConfig.getVideoPath());
                }
                if (videoUploadCallback != null && sVideoConfig != null && sVideoConfig.getVodInfo() != null) {
                    videoUploadCallback.onUploadSucceed(sVideoConfig.getVideoId(), sVideoConfig.getVodInfo().getCoverUrl());
                }
                step = AliyunVodUploadStep.VODSVideoStepFinish;
            } else if (step == VODSVideoStepUploadImage) {
                step = AliyunVodUploadStep.VODSVideoStepUploadImageFinish;
                if (resumeableSession != null && sVideoConfig != null) {
                    resumeableSession.deleteResumeableFileInfo(sVideoConfig.getImagePath());
                }
                OSSLog.logDebug(TAG, "[VODSVideoUploader]: step" + "VODSVideoStepUploadImageFinish");

                String videoid = resumeableSession.getResumeableFileVideoID(sVideoConfig.getVideoPath());
                if (!TextUtils.isEmpty(videoid)) {
                    aliyunVodAuth.refreshUploadVideo(sVideoConfig.getAccessKeyId(), sVideoConfig.getAccessKeySecret(), sVideoConfig.getSecrityToken(), videoid, sVideoConfig.getVodInfo().getCoverUrl(), requestIDSession.getRequestID());
                } else {
                    aliyunVodAuth.createUploadVideo(sVideoConfig.getAccessKeyId(), sVideoConfig.getAccessKeySecret(), sVideoConfig.getSecrityToken(), sVideoConfig.getVodInfo(), sVideoConfig.isTranscode(), sVideoConfig.getTemplateGroupId(), sVideoConfig.getStorageLocation(),
                            sVideoConfig.getWorkFlowId(),sVideoConfig.getAppId(),sVideoConfig.getRequestId() == null ? requestIDSession.getRequestID() : sVideoConfig.getRequestId());
                }
                step = AliyunVodUploadStep.VODSVideoStepCreateVideo;
            }
        }

        @Override
        public void onUploadProgress(Object request, long uploadedSize, long totalSize) {
            OSSLog.logDebug(TAG, "[OSSUploader]:" + "uploadedSize" + uploadedSize + "totalSize" + totalSize);
            if (videoUploadCallback != null) {
                if (step == VODSVideoStepUploadImage) {
                    videoUploadCallback.onUploadProgress(uploadedSize, totalSize + videoSize);
                } else if (step == AliyunVodUploadStep.VODSVideoStepUploadVideo) {
                    videoUploadCallback.onUploadProgress(uploadedSize + imageSize, totalSize + imageSize);
                }
            }
        }

        @Override
        public void onUploadFailed(String code, String message) {
            OSSLog.logDebug(TAG, "[OSSUploader]:" + "code:" + code + "message" + message);
            if (videoUploadCallback != null) {
                videoUploadCallback.onUploadFailed(code, message);
                cancel();
            }
        }


        @Override
        public void onUploadTokenExpired() {
            OSSLog.logDebug(TAG, "[OSSUploader]:" + "onUploadTokenExpired");
            if (videoUploadCallback != null) {
                videoUploadCallback.onSTSTokenExpried();
            }
        }

        @Override
        public void onUploadRetry(String code, String message) {
            OSSLog.logDebug(TAG, "[OSSUploader]:" + "onUploadRetry");
            if (videoUploadCallback != null) {
                videoUploadCallback.onUploadRetry(code, message);
            }
        }

        @Override
        public void onUploadRetryResume() {
            if (videoUploadCallback != null) {
                videoUploadCallback.onUploadRetryResume();
            }
        }

    }

    private void startUpload(String uploadAuth, String uploadAddress, SVideoConfig sVideoConfig) {
        try {
            if (step == AliyunVodUploadStep.VODSVideoStepCreateImageFinish) {
                OSSLog.logDebug(TAG, "[VODSVIDEOUploader]:" + "step:" + step);
                step = VODSVideoStepUploadImage;
            } else if (step == AliyunVodUploadStep.VODSVideoStepCreateVideoFinish) {
                OSSLog.logDebug(TAG, "[VODSVIDEOUploader]:" + "step:" + step);
                step = AliyunVodUploadStep.VODSVideoStepUploadVideo;
            }

            UploadFileInfo curInfo = new UploadFileInfo();

            try {
                byte[] authJsonBytes = Base64.decode(uploadAuth, Base64.DEFAULT);
                String authJsonString = new String(authJsonBytes);
                JSONObject authKey = new JSONObject(authJsonString);

                ossConfig.setAccessKeyId(authKey.optString("AccessKeyId"));
                ossConfig.setAccessKeySecret(authKey.optString("AccessKeySecret"));
                ossConfig.setSecrityToken(authKey.optString("SecurityToken"));
                ossConfig.setExpireTime(authKey.optString("Expiration"));
                String expirationUTCTime = authKey.optString("ExpireUTCTime");
                // TODO update expirateUTCTime
                if (!TextUtils.isEmpty(expirationUTCTime)) {
                    ossConfig.setExpireTime(expirationUTCTime);
                }
                if (step == VODSVideoStepUploadVideo) {
                    ossConfig.setVideoId(sVideoConfig.getVideoId());
                }
                ossConfig.setUploadAddress(uploadAddress);
            } catch (JSONException e) {
                throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                        "The specified parameter \"uploadAuth\" format is error");
            }

            byte[] addressJsonBytes = Base64.decode(uploadAddress, Base64.DEFAULT);
            String addressJsonString = new String(addressJsonBytes);
            JSONObject addressKey = new JSONObject(addressJsonString);

            curInfo.setEndpoint(addressKey.optString("Endpoint"));
            curInfo.setBucket(addressKey.optString("Bucket"));
            curInfo.setObject(addressKey.optString("FileName"));
            if (step == VODSVideoStepUploadImage) {
                curInfo.setFilePath(sVideoConfig.getImagePath());
                curInfo.setFileType(UploadFileInfo.UPLOAD_FILE_TYPE_IMAGE);
            } else if (step == VODSVideoStepUploadVideo) {
                curInfo.setFilePath(sVideoConfig.getVideoPath());
                curInfo.setFileType(UploadFileInfo.UPLOAD_FILE_TYPE_VIDEO);
            }

            curInfo.setVodInfo(sVideoConfig.getVodInfo());
            curInfo.setStatus(UploadStateType.INIT);

            OSSUploadInfo ossUploadInfo =
                    SharedPreferencesUtil.getUploadInfo(context.get(), ResumeableSession.SHAREDPREFS_OSSUPLOAD, curInfo.getFilePath());

            if (ossUploadInfo != null && MD5.checkMD5(context.get(), ossUploadInfo.getMd5(), curInfo.getFilePath())) {
                curInfo = resumeableSession.getResumeableFileInfo(curInfo, sVideoConfig.getVideoId());
            }else {
                resumeableSession.saveResumeableFileInfo(curInfo,sVideoConfig.getVideoId());
            }

            startUpload(curInfo);
        } catch (JSONException e) {
            throw new VODClientException(VODErrorCode.MISSING_ARGUMENT,
                    "The specified parameter \"uploadAddress\" format is error");
        }
    }


    /**
     * 开始上传
     */
    private void startUpload(UploadFileInfo uploadFileInfo) {

        if (new File(uploadFileInfo.getFilePath()).length() < 100 * 1024) {
            uploader = null;
            uploader = new OSSPutUploaderImpl(this.context.get());
            uploader.init(ossConfig, new OSSUploadCallback());
            uploader.setOSSClientConfiguration(configuration);
            try {
                uploader.start(uploadFileInfo);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                videoUploadCallback.onUploadFailed(VODErrorCode.FILE_NOT_EXIST,
                        "The file \"" + uploadFileInfo.getFilePath() + "\" is not exist!");
            }
        } else {
            uploader = null;
            uploader = new ResumableUploaderImpl(context.get());
            ((ResumableUploaderImpl) uploader).setDomainRegion(domainRegion);
            uploader.init(ossConfig, new OSSUploadCallback());

            uploader.setOSSClientConfiguration(configuration);

            try {
                uploader.start(uploadFileInfo);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                videoUploadCallback.onUploadFailed(VODErrorCode.FILE_NOT_EXIST,
                        "The file \"" + uploadFileInfo.getFilePath() + "\" is not exist!");
            }
        }
    }

}