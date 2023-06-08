/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload.internal;

import android.content.Context;
import android.net.Uri;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.alibaba.sdk.android.vod.upload.VODUploadClientImpl;
import com.alibaba.sdk.android.vod.upload.common.RequestIDSession;
import com.alibaba.sdk.android.vod.upload.common.UploadStateType;
import com.alibaba.sdk.android.vod.upload.common.utils.StringUtil;
import com.alibaba.sdk.android.vod.upload.model.OSSConfig;
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;
import com.aliyun.auth.core.VodThreadService;
import com.aliyun.vod.common.utils.StringUtils;
import com.aliyun.vod.log.core.AliyunLogCommon;
import com.aliyun.vod.log.core.AliyunLogger;
import com.aliyun.vod.log.core.AliyunLoggerManager;
import com.aliyun.vod.log.core.LogService;
import com.aliyun.vod.log.struct.AliyunLogEvent;
import com.aliyun.vod.log.struct.AliyunLogKey;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;



public class OSSPutUploaderImpl implements OSSUploader {

    private VodThreadService vodThreadService;

    private OSS oss;
    private OSSConfig config;
    private UploadFileInfo uploadFileInfo;
    private WeakReference<Context> context;
    private ClientConfiguration clientConfig;
    private OSSAsyncTask task;

    private OSSUploadListener listener;
    private RequestIDSession requestIDSession;

    public OSSPutUploaderImpl(Context context){
        this.context = new WeakReference(context);
    }

    @Override
    public void init(OSSConfig ossConfig, OSSUploadListener listener) {
        this.config = ossConfig;
        this.listener = listener;

        requestIDSession = RequestIDSession.getInstance();
        vodThreadService = new VodThreadService(String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void setOSSClientConfiguration(ClientConfiguration configuration) {
        clientConfig = new ClientConfiguration();
        if (configuration == null){
            clientConfig.setMaxErrorRetry(Integer.MAX_VALUE);
            clientConfig.setSocketTimeout(ClientConfiguration.getDefaultConf().getSocketTimeout());
            clientConfig.setConnectionTimeout(ClientConfiguration.getDefaultConf().getSocketTimeout());
        }else {
            clientConfig.setMaxErrorRetry(configuration.getMaxErrorRetry());
            clientConfig.setSocketTimeout(configuration.getSocketTimeout());
            clientConfig.setConnectionTimeout(configuration.getConnectionTimeout());
        }
    }

    @Override
    public void start(final UploadFileInfo uploadFileInfo) throws FileNotFoundException {

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
                oss = new OSSClient(context.get(), uploadFileInfo.getEndpoint(), config.getProvider(), clientConfig);
                asyncPutObjectFromLocalFile(OSSPutUploaderImpl.this.uploadFileInfo.getBucket(),
                        OSSPutUploaderImpl.this.uploadFileInfo.getObject(),
                        OSSPutUploaderImpl.this.uploadFileInfo.getFilePath());
            }
        });


    }

    // upload from local files. Use asynchronous API
    public void asyncPutObjectFromLocalFile(String bucket,String objectKey,String uploadFilePath) {
        // Creates the upload request
        PutObjectRequest put;
        if (StringUtils.isUriPath(uploadFilePath)) {
            put = new PutObjectRequest(bucket, objectKey, Uri.parse(uploadFilePath));
        } else {
            put = new PutObjectRequest(bucket, objectKey, uploadFilePath);
        }

        // Sets the progress callback and upload file asynchronously
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                listener.onUploadProgress(request, currentSize,totalSize);
            }
        });

        task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                uploadFileInfo.setStatus(UploadStateType.SUCCESS);
                listener.onUploadSucceed();
                uploadSuccessedLogger();
                OSSLog.logDebug("PutObject", "UploadSuccess");
                OSSLog.logDebug("ETag", result.getETag());
                OSSLog.logDebug("RequestId", result.getRequestId());
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                if (clientException != null) {
                    if (clientException.getMessage().equals("multipart cancel\n" +
                        "[ErrorMessage]: multipart cancel\n" +
                        "[ErrorMessage]: com.alibaba.sdk.android.oss.ClientException: multipart cancel\n" +
                        "[ErrorMessage]: multipart cancel")){
                        if (uploadFileInfo.getStatus() != UploadStateType.CANCELED){
                            uploadFileInfo.setStatus(UploadStateType.PAUSED);
                        }
                        return;
                    }
                    OSSLog.logDebug("[OSSUploader] - onFailure..."+ clientException.getMessage());
                    uploadFileInfo.setStatus(UploadStateType.FAIlURE);
                    listener.onUploadFailed(UploaderErrorCode.CLIENT_EXCEPTION, clientException.toString());
                    uploadFailedLogger(UploaderErrorCode.CLIENT_EXCEPTION, clientException.toString());
                    uploadPartFailedLogger(UploaderErrorCode.CLIENT_EXCEPTION,clientException.toString());
                } else if (serviceException != null) {
                    if (serviceException.getStatusCode() == 403 && !StringUtil.isEmpty(config.getSecrityToken())){
                        listener.onUploadTokenExpired();
                    }else{
                        OSSLog.logDebug("[OSSUploader] - onFailure..."+ serviceException.getErrorCode() + serviceException.getMessage());
                        listener.onUploadFailed(serviceException.getErrorCode(), serviceException.getMessage());
                    }
                    uploadPartFailedLogger(serviceException.getErrorCode(), serviceException.toString());
                    uploadFailedLogger(serviceException.getErrorCode(), serviceException.toString());
                }

            }
        });
        this.uploadFileInfo.setStatus(UploadStateType.UPLOADING);
    }


    @Override
    public void cancel() {
        if (oss == null){
            return;
        }
        OSSLog.logDebug(ResumableUploaderImpl.class.getClass().getName(),"Resumeable Uploader Cancel");

        vodThreadService.execute(new Runnable() {
            @Override
            public void run() {
                task.cancel();
                uploadFileInfo.setStatus(UploadStateType.CANCELED);
            }
        });
    }

    @Override
    public void pause() {
        if (uploadFileInfo == null){
            return;
        }

        UploadStateType status = uploadFileInfo.getStatus();
        if (!UploadStateType.UPLOADING.equals(status)) {
            OSSLog.logDebug("[OSSUploader] - status: " + status + " cann't be pause!");
            return;
        }

        OSSLog.logDebug("[OSSUploader] - pause...");
        uploadFileInfo.setStatus(UploadStateType.PAUSING);
        OSSLog.logDebug(ResumableUploaderImpl.class.getClass().getName(),"Resumeable Uploader Pause");
        if (task == null){
            return;
        }

        vodThreadService.execute(new Runnable() {
            @Override
            public void run() {
                task.cancel();
            }
        });
    }

    @Override
    public void resume() {
        OSSLog.logDebug(ResumableUploaderImpl.class.getClass().getName(),"Resumeable Uploader Resume");

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
    public void setRecordUploadProgressEnabled(boolean enabled) {
    }

    private void uploadFailedLogger(final String code,final String message){
        final AliyunLogger logger = AliyunLoggerManager.getLogger(VODUploadClientImpl.class.getName());
        if (logger != null) {
            LogService logService = logger.getLogService();
            if (logService != null) {
                logService.execute(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, String> args = new HashMap<>();
                        args.put(AliyunLogKey.KEY_UPLOAD_PART_FAILED_CODE, code);
                        args.put(AliyunLogKey.KEY_UPLOAD_PART_FAILED_MESSAGE, message);
                        logger.pushLog(args, AliyunLogCommon.Product.VIDEO_UPLOAD,AliyunLogCommon.LogLevel.DEBUG, AliyunLogCommon.MODULE,AliyunLogCommon.SubModule.UPLOAD,
                            AliyunLogEvent.EVENT_UPLOAD_FILE_FAILED,AliyunLogCommon.LogStores.UPLOAD,requestIDSession.getRequestID());
                    }
                });
            }
        }
    }

    private void uploadPartFailedLogger(final String code,final String message){
        final AliyunLogger logger = AliyunLoggerManager.getLogger(VODUploadClientImpl.class.getName());
        if (logger != null) {
            LogService logService = logger.getLogService();
            if (logService != null) {
                logService.execute(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, String> args = new HashMap<>();
                        args.put(AliyunLogKey.KEY_UPLOAD_PART_FAILED_CODE, code);
                        args.put(AliyunLogKey.KEY_UPLOAD_PART_FAILED_MESSAGE, message);
                        logger.pushLog(args, AliyunLogCommon.Product.VIDEO_UPLOAD,AliyunLogCommon.LogLevel.DEBUG, AliyunLogCommon.MODULE,AliyunLogCommon.SubModule.UPLOAD,
                            AliyunLogEvent.EVENT_UPLOAD_PART_FAILED,AliyunLogCommon.LogStores.UPLOAD,requestIDSession.getRequestID());
                    }
                });
            }
        }
    }

    private void uploadSuccessedLogger(){
        final AliyunLogger logger = AliyunLoggerManager.getLogger(VODUploadClientImpl.class.getName());
        if (logger != null) {
            LogService logService = logger.getLogService();
            if (logService != null) {
                logService.execute(new Runnable() {
                    @Override
                    public void run() {
                        logger.pushLog(null, AliyunLogCommon.Product.VIDEO_UPLOAD,AliyunLogCommon.LogLevel.DEBUG, AliyunLogCommon.MODULE,AliyunLogCommon.SubModule.UPLOAD,
                            AliyunLogEvent.EVENT_UPLOAD_SUCCESSED,AliyunLogCommon.LogStores.UPLOAD,requestIDSession.getRequestID());
                    }
                });
            }
        }
    }
}
