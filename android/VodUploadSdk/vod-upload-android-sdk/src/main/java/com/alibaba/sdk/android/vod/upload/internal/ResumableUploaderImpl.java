/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload.internal;

import android.content.Context;
import android.os.Environment;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.OSSResult;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;
import com.alibaba.sdk.android.vod.upload.common.RequestIDSession;
import com.alibaba.sdk.android.vod.upload.common.UploadStateType;
import com.alibaba.sdk.android.vod.upload.common.utils.StringUtil;
import com.alibaba.sdk.android.vod.upload.exception.VODErrorCode;
import com.alibaba.sdk.android.vod.upload.model.OSSConfig;
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;
import com.aliyun.auth.core.VodThreadService;
import com.aliyun.vod.common.httpfinal.QupaiHttpFinal;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

/**
 * Created by Mulberry on 2018/1/2.
 */
public class ResumableUploaderImpl implements OSSUploader {

    private final static int DEFAULT_PART_SIZE = 1024 * 1024;

    private String recordDirectory;
    private OSSConfig config;
    private OSSUploadListener listener;
    private ClientConfiguration clientConfig;
    private OSS oss;
    private WeakReference<Context> context;
    private OSSRequest ossRequest;

    private OSSProgressCallback<ResumableUploadRequest> progressCallback;
    private OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> resumableCallback;

    private VodThreadService vodThreadService;
    private OSSAsyncTask rusumebleTask;
    private UploadFileInfo uploadFileInfo;
    private RequestIDSession requestIDSession;

    private String domainRegion = null;
    private boolean recoredUploadProgressEnabled = true;

    public ResumableUploaderImpl(Context context) {
        this.context = new WeakReference(context);
        recordDirectory = getRecordDirectory();
        OSSLog.logDebug("OSS_RECORD : " + recordDirectory);
    }

    private String getRecordDirectory() {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.get().getApplicationContext().getExternalCacheDir().getPath();
        } else {
            cachePath = context.get().getCacheDir().getPath();
        }
        return cachePath + File.separator + "oss_record";
    }

    public void setDomainRegion(String domainRegion) {
        this.domainRegion = domainRegion;
    }

    @Override
    public void init(OSSConfig ossConfig, OSSUploadListener listener) {
        this.config = ossConfig;
        this.listener = listener;
        QupaiHttpFinal.getInstance().initOkHttpFinal();
        progressCallback = new OSSProgressCallbackImpl();
        resumableCallback = new ResumableCompletedCallbackImpl();
        requestIDSession = RequestIDSession.getInstance();
        vodThreadService = new VodThreadService(String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void setOSSClientConfiguration(ClientConfiguration configuration) {
        clientConfig = new ClientConfiguration();
        if (configuration == null) {
            clientConfig.setMaxErrorRetry(Integer.MAX_VALUE);
            clientConfig.setSocketTimeout(ClientConfiguration.getDefaultConf().getSocketTimeout());
            clientConfig.setConnectionTimeout(ClientConfiguration.getDefaultConf().getSocketTimeout());
        } else {
            clientConfig.setMaxErrorRetry(configuration.getMaxErrorRetry());
            clientConfig.setSocketTimeout(configuration.getSocketTimeout());
            clientConfig.setConnectionTimeout(configuration.getConnectionTimeout());
        }
    }

    @Override
    public void start(UploadFileInfo uploadFileInfo) throws FileNotFoundException {
        File recordDir = new File(recordDirectory);
        // 要保证目录存在，如果不存在则主动创建
        if (!recordDir.exists()) {
            boolean bool = recordDir.mkdirs();
            if (!bool) {
                listener.onUploadFailed(VODErrorCode.PERMISSION_DENIED, "Create RecordDir Failed! Please Check Permission WRITE_EXTERNAL_STORAGE!");
                return;
            }
        }

        if (null == this.uploadFileInfo) {

        } else if (!uploadFileInfo.equals(this.uploadFileInfo)) {
            uploadFileInfo.setStatus(UploadStateType.INIT);
        }

        if (null != null &&
                UploadStateType.INIT != uploadFileInfo.getStatus() &&
                UploadStateType.CANCELED != uploadFileInfo.getStatus()) {
            OSSLog.logDebug("[OSSUploader] - status: " + uploadFileInfo.getStatus() + " cann't be start!");
            return;
        }

        this.uploadFileInfo = uploadFileInfo;

        vodThreadService.execute(new Runnable() {
            @Override
            public void run() {
                asycResumableUpload(ResumableUploaderImpl.this.uploadFileInfo);
            }
        });

    }

    private void asycResumableUpload(UploadFileInfo uploadFileInfo) {
        OSSLog.logDebug("VODSTS", "OSS:\n" + "\nAccessKeyId:" + config.getAccessKeyId() + "\nAccessKeySecret:" + config.getAccessKeySecret() + "\nSecrityToken:" + config.getSecrityToken());

        oss = new OSSClient(context.get(), uploadFileInfo.getEndpoint(), config.getProvider(), clientConfig);

        OSSLog.logDebug("ResumeableUplaod", "BucketName:" + uploadFileInfo.getBucket() + "\nobject:" + uploadFileInfo.getObject() +
                "\nobject:" + uploadFileInfo.getFilePath());

        // 创建断点上传请求，参数中给出断点记录文件的保存位置，需是一个文件夹的绝对路径
        ossRequest = new
                ResumableUploadRequest(uploadFileInfo.getBucket(), uploadFileInfo.getObject(),
                uploadFileInfo.getFilePath(), recordDirectory);
        ((ResumableUploadRequest) ossRequest).setDeleteUploadOnCancelling(!recoredUploadProgressEnabled);
        ((ResumableUploadRequest) ossRequest).setProgressCallback(progressCallback);
        long partSize = config.getPartSize() == 0 ? DEFAULT_PART_SIZE : config.getPartSize();
        File file = new File(uploadFileInfo.getFilePath());
        long fileLengh = file.length();
        if (fileLengh / partSize > 5000) {
            partSize = fileLengh / 4999;
        }
        ((ResumableUploadRequest) ossRequest).setPartSize(partSize);

        rusumebleTask = oss.asyncResumableUpload((ResumableUploadRequest) ossRequest, resumableCallback);
        this.uploadFileInfo.setStatus(UploadStateType.UPLOADING);
    }

    @Override
    public void resume() {
        OSSLog.logDebug(ResumableUploaderImpl.class.getClass().getName(), "Resumeable Uploader Resume");

        this.uploadFileInfo.setStatus(UploadStateType.UPLOADING);
        vodThreadService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    start(uploadFileInfo);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void pause() {
        if (uploadFileInfo == null) {
            return;
        }

        UploadStateType status = uploadFileInfo.getStatus();
        if (!UploadStateType.UPLOADING.equals(status)) {
            OSSLog.logDebug("[OSSUploader] - status: " + status + " cann't be pause!");
            return;
        }

        OSSLog.logDebug("[OSSUploader] - pause...");
        uploadFileInfo.setStatus(UploadStateType.PAUSING);
        OSSLog.logDebug(ResumableUploaderImpl.class.getClass().getName(), "Resumeable Uploader Pause");

        vodThreadService.execute(new Runnable() {
            @Override
            public void run() {
                rusumebleTask.cancel();
            }
        });
    }

    @Override
    public void cancel() {
        if (oss == null || ossRequest == null) {
            return;
        }
        OSSLog.logDebug(ResumableUploaderImpl.class.getClass().getName(), "Resumeable Uploader Cancel");

        vodThreadService.execute(new Runnable() {
            @Override
            public void run() {
                rusumebleTask.cancel();
                uploadFileInfo.setStatus(UploadStateType.CANCELED);
            }
        });

    }

    @Override
    public void setRecordUploadProgressEnabled(boolean enabled) {
        recoredUploadProgressEnabled = enabled;
    }

    class OSSProgressCallbackImpl implements OSSProgressCallback {

        @Override
        public void onProgress(Object request, long currentSize, long totalSize) {
            OSSLog.logDebug("[OSSUploader] - onProgress..." + (currentSize * 100 / totalSize));
            listener.onUploadProgress(request, currentSize, totalSize);
        }

    }

    class ResumableCompletedCallbackImpl implements OSSCompletedCallback {
        @Override
        public void onSuccess(OSSRequest request, OSSResult result) {
            rusumebleTask.isCompleted();
            uploadFileInfo.setStatus(UploadStateType.SUCCESS);
            listener.onUploadSucceed();
        }

        @Override
        public void onFailure(OSSRequest request, ClientException clientException, ServiceException serviceException) {
            OSSLog.logDebug("[OSSUploader] - onFailure Enter");
            if (clientException != null) {
                OSSLog.logDebug("[OSSUploader] - onFailure ClientException");
                if (clientException.isCanceledException()) {
                    OSSLog.logDebug("[OSSUploader] - onFailure ClientException isCanceledException");
                    if (uploadFileInfo.getStatus() != UploadStateType.CANCELED) {
                        uploadFileInfo.setStatus(UploadStateType.PAUSED);
                    }
                    return;
                }
                OSSLog.logDebug("[OSSUploader] - onFailure..." + clientException.getMessage());
                uploadFileInfo.setStatus(UploadStateType.FAIlURE);
                listener.onUploadFailed(UploaderErrorCode.CLIENT_EXCEPTION, clientException.toString());
            } else if (serviceException != null) {
                OSSLog.logDebug("[OSSUploader] - onFailure ServiceException " + serviceException.getStatusCode());
                if (config != null) {
                    OSSLog.logDebug("[OSSUploader] - onFailure ServiceException token" + config.getSecrityToken());
                    OSSLog.logDebug("[OSSUploader] - onFailure ServiceException id" + config.getAccessKeyId());
                    OSSLog.logDebug("[OSSUploader] - onFailure ServiceException secret" + config.getAccessKeySecret());
                }
                if (serviceException.getStatusCode() == 403 && !StringUtil.isEmpty(config.getSecrityToken())) {
                    OSSLog.logDebug("[OSSUploader] - onFailure ServiceException onUploadTokenExpired");
                    listener.onUploadTokenExpired();
                } else {
                    OSSLog.logDebug("[OSSUploader] - onFailure ServiceException onUploadFailed");
                    OSSLog.logDebug("[OSSUploader] - onFailure..." + serviceException.getErrorCode() + serviceException.getMessage());
                    listener.onUploadFailed(serviceException.getErrorCode(), serviceException.getMessage());
                }
                OSSLog.logDebug("[OSSUploader] - onFailure ServiceException Done");
            }

        }
    }
}
