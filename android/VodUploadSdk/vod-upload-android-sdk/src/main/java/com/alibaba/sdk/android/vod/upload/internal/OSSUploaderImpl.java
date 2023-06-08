/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload.internal;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.OSSResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.alibaba.sdk.android.oss.model.UploadPartRequest;
import com.alibaba.sdk.android.oss.model.UploadPartResult;
import com.alibaba.sdk.android.vod.upload.VODUploadClientImpl;
import com.alibaba.sdk.android.vod.upload.common.UploadStateType;
import com.alibaba.sdk.android.vod.upload.common.utils.StringUtil;
import com.alibaba.sdk.android.vod.upload.exception.VODErrorCode;
import com.alibaba.sdk.android.vod.upload.model.FilePartInfo;
import com.alibaba.sdk.android.vod.upload.model.OSSConfig;
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;
import com.aliyun.vod.common.httpfinal.QupaiHttpFinal;
import com.aliyun.vod.log.core.AliyunLogCommon;
import com.aliyun.vod.log.core.AliyunLogger;
import com.aliyun.vod.log.core.AliyunLoggerManager;
import com.aliyun.vod.log.core.LogService;
import com.aliyun.vod.log.struct.AliyunLogEvent;
import com.aliyun.vod.log.struct.AliyunLogKey;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;


public class OSSUploaderImpl implements OSSUploader {
    private final static int RETRY_INTERVAL = 3 * 1000;
    private OSSConfig config;
    private OSSUploadListener listener;
    private ClientConfiguration clientConfig;
    private OSS oss;
    private boolean retryShouldNotify;
    private Context context;
    private String uploadId;
    private Long uploadedSize;
    private long currentPartSize;
    /**
     * 当前分片号，-1表示结束
     */
    private int curPartNumber;
    private UploadFileInfo uploadFileInfo;

    private List<PartETag> uploadedParts = new ArrayList<PartETag>();
    private OSSCompletedCallback<InitiateMultipartUploadRequest, InitiateMultipartUploadResult> initCallback;
    private OSSCompletedCallback<UploadPartRequest, UploadPartResult> partCallback;
    private OSSCompletedCallback<CompleteMultipartUploadRequest, CompleteMultipartUploadResult> completedCallback;

    private final HandlerThread mHandleThread = new HandlerThread("UploadThread");
    private Handler mHandler = null;

    public OSSUploaderImpl(Context context) {
        this.context = context;
    }

    @Override
    public void init(OSSConfig config, OSSUploadListener listener) {
        OSSLog.logDebug("[OSSUploader] - init...");
        this.config = config;
        this.listener = listener;

        QupaiHttpFinal.getInstance().initOkHttpFinal();
        initCallback = new OSSCompletedCallbackImpl();
        partCallback = new OSSCompletedCallbackImpl();
        completedCallback = new OSSCompletedCallbackImpl();

    }

    private Handler getHandler() {
        if (mHandler == null) {
            mHandleThread.start();
            mHandler = new Handler(mHandleThread.getLooper());
        }
        return mHandler;
    }

    @Override
    public void start(final UploadFileInfo uploadFileInfo) {
        OSSLog.logDebug("[OSSUploader] - start");

        getHandler().post(new Runnable() {
            @Override
            public void run() {
                OSSLog.logDebug("[OSSUploader] - start Runnable");
                if (!uploadFileInfo.equals(OSSUploaderImpl.this.uploadFileInfo)) {
                    uploadFileInfo.setStatus(UploadStateType.INIT);
                }

                if (null != null &&
                        UploadStateType.INIT != uploadFileInfo.getStatus() &&
                        UploadStateType.CANCELED != uploadFileInfo.getStatus()) {
                    OSSLog.logDebug("[OSSUploader] - status: " + uploadFileInfo.getStatus() + " cann't be start!");
                    return;
                }

                OSSUploaderImpl.this.uploadFileInfo = uploadFileInfo;
                oss = new OSSClient(context, uploadFileInfo.getEndpoint(), config.getProvider(),
                        clientConfig);
                uploadedSize = -1L;
                //默认从分片2开始上传
                curPartNumber = 2;
                uploadedParts.clear();
                retryShouldNotify = true;
                uploadFileInfo.setStatus(UploadStateType.UPLOADING);
                initMultiPartUpload();
            }
        });
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
    public void cancel() {
        if (null == uploadFileInfo) {
            return;
        }

        UploadStateType status = uploadFileInfo.getStatus();
        if (!UploadStateType.INIT.equals(status) && !UploadStateType.UPLOADING.equals(status) &&
            !UploadStateType.PAUSED.equals(status) && !UploadStateType.PAUSING.equals(status)) {
            OSSLog.logDebug("[OSSUploader] - status: " + status + " cann't be cancel!");
            return;
        }
        OSSLog.logDebug("[OSSUploader] - cancel...");
        uploadFileInfo.setStatus(UploadStateType.CANCELED);
        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                OSSLog.logDebug("[OSSUploader] - This task is cancelled!");
                uploadPartFailedLogger(UploadStateType.CANCELED.toString(),"This task is user cancelled!");
                abortUpload();
                release();
            }
        });
    }

    @Override
    public void pause() {
        UploadStateType status = uploadFileInfo.getStatus();
        if (!UploadStateType.UPLOADING.equals(status)) {
            OSSLog.logDebug("[OSSUploader] - status: " + status + " cann't be pause!");
            return;
        }

        OSSLog.logDebug("[OSSUploader] - pause...");
        uploadFileInfo.setStatus(UploadStateType.PAUSING);
    }

    @Override
    public void resume() {
        final UploadStateType status = uploadFileInfo.getStatus();
        if (!UploadStateType.PAUSING.equals(status) && !UploadStateType.PAUSED.equals(status)) {
            OSSLog.logDebug("[OSSUploader] - status: " + status + " cann't be resume!");
            return;
        }

        OSSLog.logDebug("[OSSUploader] - resume...");
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                OSSLog.logDebug("[OSSUploader] - resume Runnable");
                if (UploadStateType.PAUSING.equals(status)) {
                    uploadFileInfo.setStatus(UploadStateType.UPLOADING);
                } else if (UploadStateType.PAUSED.equals(status)){
                    uploadFileInfo.setStatus(UploadStateType.UPLOADING);
                    if (uploadedSize == -1L) {
                        initMultiPartUpload();
                    } else if (curPartNumber != -1) {
                        uploadPart();
                    } else {
                        completeMultiPartUpload();
                    }
                }
            }
        });
    }

    @Override
    public void setRecordUploadProgressEnabled(boolean enabled) {
    }

    private void initMultiPartUpload() {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                OSSLog.logDebug("[OSSUploader] - initMultiPartUpload");
                startUploadLogger(uploadFileInfo);
                InitiateMultipartUploadRequest ossRequest = new InitiateMultipartUploadRequest(
                        uploadFileInfo.getBucket(), uploadFileInfo.getObject());
                try {
                    InitiateMultipartUploadResult result = oss.initMultipartUpload(ossRequest);
                    initCallback.onSuccess(ossRequest, result);
                } catch (ClientException e) {
                    initCallback.onFailure(ossRequest, e, null);
                } catch (ServiceException e) {
                    initCallback.onFailure(ossRequest, null, e);
                }
            }
        });
    }

    private void abortUpload() {
        if (uploadId != null) {
            try {
                AbortMultipartUploadRequest abort = new AbortMultipartUploadRequest(
                        uploadFileInfo.getBucket(), uploadFileInfo.getObject(), uploadId);
                oss.abortMultipartUpload(abort);
            } catch (ClientException e) {
                OSSLog.logWarn("[OSSUploader] - abort ClientException!code:" + e.getCause() +
                        ", message:" + e.getMessage());
            } catch (ServiceException e) {
                OSSLog.logWarn("[OSSUploader] - abort ServiceException!code:" + e.getCause() +
                        ", message:" + e.getMessage());
            }
        }
    }

    private void completeMultiPartUpload() {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                OSSLog.logDebug("[OSSUploader] - completeMultiPartUpload");
                CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(
                        uploadFileInfo.getBucket(), uploadFileInfo.getObject(), uploadId,
                        uploadedParts);

                ObjectMetadata metadata = request.getMetadata();
                if (metadata == null) {
                    metadata = new ObjectMetadata();
                }

                if (null != uploadFileInfo.getVodInfo()) {
                    metadata.addUserMetadata("x-oss-notification",
                            uploadFileInfo.getVodInfo().toVodJsonStringWithBase64());
                }
                request.setMetadata(metadata);
                try {
                    CompleteMultipartUploadResult result = oss.completeMultipartUpload(request);
                    completedCallback.onSuccess(request, result);
                } catch (ClientException e) {
                    completedCallback.onFailure(request, e, null);
                    e.printStackTrace();
                } catch (ServiceException e) {
                    completedCallback.onFailure(request, null, e);
                    e.printStackTrace();
                }
            }
        });
    }

    private void uploadPart() {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                OSSLog.logDebug("[OSSUploader] - uploadPart PartNumber:"+curPartNumber);
                final UploadPartRequest ossRequest = new UploadPartRequest(uploadFileInfo.getBucket(),
                        uploadFileInfo.getObject(), uploadId, curPartNumber);
                if (uploadFileInfo.getPartInfoList().size() < curPartNumber) {
                    uploadFileInfo.setStatus(UploadStateType.PAUSED);
                    return;
                }
                FilePartInfo partInfo = uploadFileInfo.getPartInfoList().get(curPartNumber - 1);
                byte[] bytes = new byte[(int) partInfo.getSize()];
                FileLock fileLock = null;
                RandomAccessFile randomAccessFile = null;
                try {
                    randomAccessFile = new RandomAccessFile(uploadFileInfo.getFilePath(), "r");
                    fileLock = randomAccessFile.getChannel().lock(partInfo.getSeek(), partInfo.getSize(), true);
                    randomAccessFile.seek(partInfo.getSeek());
                    randomAccessFile.read(bytes);
                    ossRequest.setPartContent(bytes);
                } catch (FileNotFoundException e) {
                    OSSLog.logError("[OSSUploader] - uploadPart RandomAccessFile FileNotFoundException");
                } catch (IOException e) {
                    OSSLog.logError("[OSSUploader] - uploadPart RandomAccessFile IOException");
                } finally {
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException ignored) {
                        }
                    }
                    if (fileLock != null) {
                        try {
                            fileLock.release();
                        } catch (IOException ignored) {
                        }
                    }
                }
                currentPartSize = partInfo.getSize();

                ossRequest.setProgressCallback(new OSSProgressCallback<UploadPartRequest>() {
                    @Override
                    public void onProgress(UploadPartRequest uploadPartRequest, long l, long l1) {
                        listener.onUploadProgress(uploadPartRequest, uploadedSize + l, uploadFileInfo.getFileLength());
                    }
                });
                try {
                    UploadPartResult result = oss.uploadPart(ossRequest);
                    startUploadPartLogger();
                    partCallback.onSuccess(ossRequest, result);
                } catch (ClientException e) {
                    partCallback.onFailure(ossRequest, e, null);
                } catch (ServiceException e) {
                    partCallback.onFailure(ossRequest, null, e);
                }
            }
        });
    }

    public OSSUploadRetryType shouldRetry(Exception e) {
        if (e instanceof ClientException) {
            Exception localException = (Exception) e.getCause();
            if (localException instanceof InterruptedIOException
                    && !(localException instanceof SocketTimeoutException)) {
                OSSLog.logError("[shouldNotetry] - is interrupted!");
                return OSSUploadRetryType.ShouldNotRetry;
            } else if (localException instanceof IllegalArgumentException) {
                return OSSUploadRetryType.ShouldNotRetry;
            } else  if (localException instanceof  SocketTimeoutException){
                return OSSUploadRetryType.ShouldNotRetry;
            } else if (localException instanceof SSLHandshakeException){
                return OSSUploadRetryType.ShouldNotRetry;
            }
            OSSLog.logDebug("shouldRetry - " + e.toString());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            return OSSUploadRetryType.ShouldRetry;
        } else if (e instanceof ServiceException) {
            ServiceException serviceException = (ServiceException) e;
            if (serviceException.getErrorCode() != null && serviceException.getErrorCode().equalsIgnoreCase("RequestTimeTooSkewed")) {
                return OSSUploadRetryType.ShouldRetry;
            } else if (serviceException.getStatusCode() >= 500) {
                return OSSUploadRetryType.ShouldRetry;
            } else if (serviceException.getStatusCode() == 403 && !StringUtil.isEmpty(config.getSecrityToken())) {
                return OSSUploadRetryType.ShouldGetSTS;
            } else {
                return OSSUploadRetryType.ShouldNotRetry;
            }
        } else {
            return OSSUploadRetryType.ShouldNotRetry;
        }
    }

    class OSSCompletedCallbackImpl implements OSSCompletedCallback {
        @Override
        public void onSuccess(OSSRequest req, OSSResult res) {
            UploadStateType status = uploadFileInfo.getStatus();

            if (UploadStateType.CANCELED.equals(status)) {
                OSSLog.logError("onSuccess: upload has been canceled, ignore notify.");
                uploadCancelLogger();
                return;
            }

            if (!retryShouldNotify) {
                listener.onUploadRetryResume();
                retryShouldNotify = true;
            }

            if (res instanceof InitiateMultipartUploadResult) {
                InitiateMultipartUploadResult result = (InitiateMultipartUploadResult) res;
                uploadId = result.getUploadId();
                OSSLog.logDebug("[OSSUploader] - InitiateMultipartUploadResult uploadId:" + uploadId);
                uploadedSize = 0L;
                uploadPart();
            } else if (res instanceof UploadPartResult) {
                UploadPartRequest request = (UploadPartRequest) req;
                OSSLog.logDebug("[OSSUploader] - UploadPartResult onSuccess ------------------" + request.getPartNumber());
                UploadPartResult result = (UploadPartResult) res;
                //PartETag需要按片段号排序，所以第一个片段要移到头部
                if (curPartNumber == 1) {
                    uploadedParts.add(0, new PartETag(curPartNumber, result.getETag()));
                } else {
                    uploadedParts.add(new PartETag(curPartNumber, result.getETag()));
                }

                uploadedSize += currentPartSize;
                FilePartInfo partInfo = uploadFileInfo.getPartInfoList().get(curPartNumber - 1);

                //更新片段号，如果是1表示已上传完成
                if (curPartNumber == 1) {
                    curPartNumber = -1;
                } else if (uploadFileInfo.getFileLength() != -1 && uploadFileInfo.getFileLength() == (partInfo.getSeek() + partInfo.getSize())) {
                    //上传完最后一个片段再上传第一个片段
                    curPartNumber = 1;
                } else {
                    curPartNumber++;
                }
                uploadPartCompletedLogger();
                if (UploadStateType.CANCELED.equals(status)) {
                    return;
                } else if (UploadStateType.UPLOADING.equals(status)) {
                    //-1表示所有片段都已上传完成
                    if (curPartNumber == -1) {
                        completeMultiPartUpload();
                    } else {
                        uploadPart();
                    }
                } else if (UploadStateType.PAUSING.equals(status)) {
                    OSSLog.logDebug("[OSSUploader] - This task is pausing!");
                    uploadFileInfo.setStatus(UploadStateType.PAUSED);
                }
            } else if (res instanceof CompleteMultipartUploadResult) {
                OSSLog.logDebug("[OSSUploader] - CompleteMultipartUploadResult onSuccess ------------------");
                uploadFileInfo.setStatus(UploadStateType.SUCCESS);
                listener.onUploadSucceed();
                uploadSuccessedLogger();
                release();
            }
        }

        @Override
        public void onFailure(OSSRequest request, ClientException clientException,
                              ServiceException serviceException) {
            UploadStateType status = uploadFileInfo.getStatus();
            Exception exception = null;
            if (clientException != null) {
                exception = clientException;
            } else if (serviceException != null) {
                exception = serviceException;
            }

            if (exception == null) {
                OSSLog.logError("onFailure error: exception is null.");
                return;
            }

            if (UploadStateType.CANCELED.equals(status)) {
                OSSLog.logError("onFailure error: upload has been canceled, ignore notify.");
                uploadCancelLogger();
                return;
            }

            switch (shouldRetry(exception)) {
                case ShouldRetry:
                    if (UploadStateType.PAUSING.equals(status)) {
                        OSSLog.logDebug("[OSSUploader] - This task is pausing!");
                        uploadFileInfo.setStatus(UploadStateType.PAUSED);
                        return;
                    }


                    try {
                        Thread.sleep(RETRY_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    onRetry(request);

                    if (retryShouldNotify) {
                        if (clientException != null) {
                            listener.onUploadRetry(UploaderErrorCode.CLIENT_EXCEPTION, clientException.toString());
                            if(request instanceof UploadPartRequest){
                                uploadPartFailedLogger(UploaderErrorCode.CLIENT_EXCEPTION,clientException.getMessage().toString());
                            }else {
                                uploadFailedLogger(UploaderErrorCode.CLIENT_EXCEPTION,clientException.getMessage().toString());
                            }
                        } else if (serviceException != null) {
                            listener.onUploadRetry(serviceException.getErrorCode(), serviceException.getMessage());
                            if(request instanceof UploadPartRequest){
                                uploadPartFailedLogger(serviceException.getErrorCode(), serviceException.getMessage());
                            }else {
                                uploadFailedLogger(serviceException.getErrorCode(), serviceException.getMessage());
                            }
                        }

                        retryShouldNotify = false;
                    }

                    break;

                case ShouldGetSTS:
                    uploadFileInfo.setStatus(UploadStateType.PAUSED);
                    listener.onUploadTokenExpired();
                    uploadFailedLogger(VODErrorCode.UPLOAD_EXPIRED, "Upload Token Expired");
                    break;

                case ShouldNotRetry:
                    uploadFileInfo.setStatus(UploadStateType.FAIlURE);
                    if (clientException != null) {
                        listener.onUploadFailed(UploaderErrorCode.CLIENT_EXCEPTION, clientException.toString());
                        if(request instanceof UploadPartRequest){
                            uploadPartFailedLogger(UploaderErrorCode.CLIENT_EXCEPTION,clientException.getMessage().toString());
                        }else {
                            uploadFailedLogger(UploaderErrorCode.CLIENT_EXCEPTION,clientException.getMessage().toString());
                        }

                    } else if (serviceException != null) {
                        listener.onUploadFailed(serviceException.getErrorCode(), serviceException.getMessage());
                        if(request instanceof UploadPartRequest){
                            uploadPartFailedLogger(serviceException.getErrorCode(), serviceException.getMessage());
                        }else {
                            uploadFailedLogger(serviceException.getErrorCode(), serviceException.getMessage());
                        }
                    }

                    break;
            }
        }
    }

    private void onRetry(final OSSRequest request){
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (request instanceof InitiateMultipartUploadRequest) {
                        InitiateMultipartUploadResult result = oss.initMultipartUpload((InitiateMultipartUploadRequest) request);
                        initCallback.onSuccess((InitiateMultipartUploadRequest) request, result);
                    } else if (request instanceof CompleteMultipartUploadRequest) {
                        CompleteMultipartUploadResult result = oss.completeMultipartUpload((CompleteMultipartUploadRequest) request);
                        completedCallback.onSuccess((CompleteMultipartUploadRequest) request, result);
                    } else if (request instanceof UploadPartRequest) {
                        UploadPartResult result = oss.uploadPart((UploadPartRequest) request);
                        partCallback.onSuccess((UploadPartRequest) request, result);
                    }
                } catch (ClientException e) {
                    if (request instanceof InitiateMultipartUploadRequest) {
                        initCallback.onFailure((InitiateMultipartUploadRequest) request, e, null);
                    } else if (request instanceof CompleteMultipartUploadRequest) {
                        completedCallback.onFailure((CompleteMultipartUploadRequest) request, e, null);
                    } else if (request instanceof UploadPartRequest) {
                        partCallback.onFailure((UploadPartRequest) request, e, null);
                    }
                } catch (ServiceException e) {
                    if (request instanceof InitiateMultipartUploadRequest) {
                        initCallback.onFailure((InitiateMultipartUploadRequest) request, null, e);
                    } else if (request instanceof CompleteMultipartUploadRequest) {
                        completedCallback.onFailure((CompleteMultipartUploadRequest) request, null, e);
                    } else if (request instanceof UploadPartRequest) {
                        partCallback.onFailure((UploadPartRequest) request, null, e);
                    }
                }
            }
        });
    }

    private void release() {
        if (mHandler != null) {
            mHandleThread.quit();
            mHandler = null;
        }
    }

    private void startUploadLogger(final UploadFileInfo fileInfo) {
        final AliyunLogger logger = AliyunLoggerManager.getLogger(VODUploadClientImpl.class.getName());
        if (logger != null) {
            logger.updateRequestID();
            LogService logService = logger.getLogService();
            if (logService != null) {
                logService.execute(new Runnable() {
                    @Override
                    public void run() {
//                        Bitmap bitmap = FileUtils.getVideoSize(fileInfo.getFilePath());
                        Map<String, String> args = new HashMap<>();
//                        args.put(AliyunLogKey.KEY_FILE_TYPE, FileUtils.getMimeType(fileInfo.getFilePath()));
//                        args.put(AliyunLogKey.KEY_FILE_SIZE, String.valueOf(mFileLength));//byte???
//                        args.put(AliyunLogKey.KEY_FILE_WIDTH, bitmap == null ?"":String.valueOf(bitmap.getWidth()));
//                        args.put(AliyunLogKey.KEY_FILE_HEIGHT,bitmap == null ?"":String.valueOf(bitmap.getHeight()) );
//                        args.put(AliyunLogKey.KEY_FILE_MD5, FileUtils.getMd5OfFile(fileInfo.getFilePath()));
//                        args.put(AliyunLogKey.KEY_PART_SIZE, String.valueOf(blockSize));
                        args.put(AliyunLogKey.KEY_BUCKET, fileInfo.getBucket());
                        args.put(AliyunLogKey.KEY_OBJECT_KEY, fileInfo.getObject());
                        logger.pushLog(args, AliyunLogCommon.Product.VIDEO_UPLOAD,AliyunLogCommon.LogLevel.DEBUG, AliyunLogCommon.MODULE,AliyunLogCommon.SubModule.UPLOAD,
                                AliyunLogEvent.EVENT_UPLOAD_STARTED,AliyunLogCommon.LogStores.UPLOAD,null);
                    }
                });
            }
        }
    }

    private void startUploadPartLogger() {
        final AliyunLogger logger = AliyunLoggerManager.getLogger(VODUploadClientImpl.class.getName());
        if (logger != null) {
            LogService logService = logger.getLogService();
            if (logService != null) {
                logService.execute(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, String> args = new HashMap<>();
                        args.put(AliyunLogKey.KEY_UPLOADID, uploadId);
                        args.put(AliyunLogKey.KEY_PART_NUMBER, String.valueOf(curPartNumber));
                        args.put(AliyunLogKey.KEY_PART_RETRY, retryShouldNotify ? "0" : "1");
                        logger.pushLog(args, AliyunLogCommon.Product.VIDEO_UPLOAD,AliyunLogCommon.LogLevel.DEBUG, AliyunLogCommon.MODULE,AliyunLogCommon.SubModule.UPLOAD,
                                AliyunLogEvent.EVENT_UPLOAD_PART_START,AliyunLogCommon.LogStores.UPLOAD,null);
                    }
                });
            }
        }
    }

    private void uploadPartCompletedLogger(){
        final AliyunLogger logger = AliyunLoggerManager.getLogger(VODUploadClientImpl.class.getName());
        if (logger != null) {
            LogService logService = logger.getLogService();
            if (logService != null) {
                logService.execute(new Runnable() {
                    @Override
                    public void run() {
                        logger.pushLog(null,AliyunLogCommon.Product.VIDEO_UPLOAD,AliyunLogCommon.LogLevel.DEBUG, AliyunLogCommon.MODULE,AliyunLogCommon.SubModule.UPLOAD,
                                AliyunLogEvent.EVENT_UPLOAD_PART_COMPLETED,AliyunLogCommon.LogStores.UPLOAD,null);
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
                                AliyunLogEvent.EVENT_UPLOAD_SUCCESSED,AliyunLogCommon.LogStores.UPLOAD,null);
                    }
                });
            }
        }
    }

    private void uploadCancelLogger(){
        final AliyunLogger logger = AliyunLoggerManager.getLogger(VODUploadClientImpl.class.getName());
        if (logger != null) {
            LogService logService = logger.getLogService();
            if (logService != null) {
                logService.execute(new Runnable() {
                    @Override
                    public void run() {
                        logger.pushLog(null, AliyunLogCommon.Product.VIDEO_UPLOAD,AliyunLogCommon.LogLevel.DEBUG, AliyunLogCommon.MODULE,AliyunLogCommon.SubModule.UPLOAD,
                                AliyunLogEvent.EVENT_UPLOAD_CANCEL,AliyunLogCommon.LogStores.UPLOAD,null);
                    }
                });
            }
        }
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
                                AliyunLogEvent.EVENT_UPLOAD_FILE_FAILED,AliyunLogCommon.LogStores.UPLOAD,null);
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
                                AliyunLogEvent.EVENT_UPLOAD_PART_FAILED,AliyunLogCommon.LogStores.UPLOAD,null);
                    }
                });
            }
        }
    }

}

