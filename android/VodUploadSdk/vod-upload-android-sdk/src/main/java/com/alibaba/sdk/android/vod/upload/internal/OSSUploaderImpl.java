/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload.internal;

import android.content.Context;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
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
import com.alibaba.sdk.android.vod.upload.common.UploadStateType;
import com.alibaba.sdk.android.vod.upload.common.utils.StringUtil;
import com.alibaba.sdk.android.vod.upload.model.OSSConfig;
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;
import com.aliyun.vod.common.httpfinal.QupaiHttpFinal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

/**
 * Created by Leigang on 16/6/25.
 */
public class OSSUploaderImpl implements OSSUploader {
    private final static int SMALL_BLOCK_SIZE = 256 * 1024;
    private final static int LARGE_BLOCK_SIZE = 512 * 1024;
    private final static int RETRY_INTERVAL = 3 * 1000;
    private OSSConfig config;
    private OSSUploadListener listener;
    private ClientConfiguration clientConfig;
    private OSS oss;
    private boolean retryShouldNotify;
    private File file;
    private InputStream inputStream;
    private Context context;
    private String uploadId;
    private Long uploadedSize;
    private Integer currentUploadLength;
    private Integer lastUploadedBlockIndex;
    private Integer blockSize;
    private UploadFileInfo uploadFileInfo;
    private OSSRequest ossRequest;

    //    private UploadPartRequest uploadPartRequest;
//    private InitiateMultipartUploadRequest initiateMultipartUploadRequest;
//    private CompleteMultipartUploadRequest completeMultipartUploadRequest;
    private List<PartETag> uploadedParts = new ArrayList<PartETag>();
    private OSSCompletedCallback<InitiateMultipartUploadRequest, InitiateMultipartUploadResult> initCallback;
    private OSSCompletedCallback<UploadPartRequest, UploadPartResult> partCallback;
    private OSSCompletedCallback<CompleteMultipartUploadRequest, CompleteMultipartUploadResult> completedCallback;

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

    @Override
    public void start(UploadFileInfo uploadFileInfo) throws FileNotFoundException {
        if (null == this.uploadFileInfo) {
            ;
        } else if (!uploadFileInfo.equals(this.uploadFileInfo)) {
            uploadFileInfo.setStatus(UploadStateType.INIT);
        }

        if (null != null &&
                UploadStateType.INIT != uploadFileInfo.getStatus() &&
                UploadStateType.CANCELED != uploadFileInfo.getStatus()) {
            OSSLog.logDebug("[OSSUploader] - status: " + uploadFileInfo.getStatus() + " cann't be start!");
            return;
        }

        OSSLog.logDebug("[OSSUploader] - start..." + uploadFileInfo.getFilePath());

        this.uploadFileInfo = uploadFileInfo;
        oss = new OSSClient(context, uploadFileInfo.getEndpoint(), config.getProvider(),
                clientConfig);
        file = new File(uploadFileInfo.getFilePath());
        if (file.length() < 128 * 1024 * 1024) {
            blockSize = SMALL_BLOCK_SIZE;
        } else {
            blockSize = LARGE_BLOCK_SIZE;
        }
        inputStream = new FileInputStream(file);

        uploadedSize = -1L;
        lastUploadedBlockIndex = 0;
        uploadedParts.clear();
        ossRequest = null;
        retryShouldNotify = true;

        initMultiPartUpload();
        uploadFileInfo.setStatus(UploadStateType.UPLOADING);
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
        UploadStateType status = uploadFileInfo.getStatus();
        if (!UploadStateType.PAUSING.equals(status) && !UploadStateType.PAUSED.equals(status)) {
            OSSLog.logDebug("[OSSUploader] - status: " + status + " cann't be resume!");
            return;
        }

        OSSLog.logDebug("[OSSUploader] - resume...");
        if (UploadStateType.PAUSING.equals(status)) {
            uploadFileInfo.setStatus(UploadStateType.UPLOADING);
        } else if (UploadStateType.PAUSED.equals(status)) {
            uploadFileInfo.setStatus(UploadStateType.UPLOADING);
            if (uploadedSize == -1L) {
                initMultiPartUpload();
            } else if (uploadedSize < file.length()) {
                uploadPart();
            } else {
                completeMultiPartUpload();
            }
        }
    }

    @Override
    public void setRecordUploadProgressEnabled(boolean enabled) {
    }

    private void initMultiPartUpload() {
        ossRequest = new InitiateMultipartUploadRequest(
                uploadFileInfo.getBucket(), uploadFileInfo.getObject());
        oss.asyncInitMultipartUpload((InitiateMultipartUploadRequest) ossRequest, initCallback);
    }

    private void abortUpload() {
        if (uploadId != null) {
            try {
                AbortMultipartUploadRequest abort = new AbortMultipartUploadRequest(
                        uploadFileInfo.getBucket(), uploadFileInfo.getObject(), uploadId);
                oss.abortMultipartUpload(abort);
                inputStream.close();
            } catch (ClientException e) {
                OSSLog.logWarn("[OSSUploader] - abort ClientException!code:" + e.getCause() +
                        ", message:" + e.getMessage());
            } catch (ServiceException e) {
                OSSLog.logWarn("[OSSUploader] - abort ServiceException!code:" + e.getCause() +
                        ", message:" + e.getMessage());
            } catch (IOException e) {
                OSSLog.logWarn("[OSSUploader] - abort IOException!code:" + e.getCause() +
                        ", message:" + e.getMessage());
            }
        }
    }

    private void completeMultiPartUpload() {
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
        ossRequest = request;

        oss.asyncCompleteMultipartUpload(request, completedCallback);
    }

    private void uploadPart() {
        ossRequest = new UploadPartRequest(uploadFileInfo.getBucket(),
                uploadFileInfo.getObject(), uploadId, lastUploadedBlockIndex + 1);

        currentUploadLength = (int) Math.min(blockSize, file.length() - uploadedSize);
        OSSLog.logDebug("[OSSUploader] - filesize:" + file.length() + ", blocksize: " + currentUploadLength);

        try {
            ((UploadPartRequest) ossRequest).setPartContent(IOUtils.readStreamAsBytesArray(inputStream,
                    currentUploadLength));
        } catch (IOException e) {
            OSSLog.logError("[OSSUploader] - read content from file failed!name:" + file.getName() +
                    ", offset:" + uploadedSize + ", length:" + currentUploadLength);
            return;
        }

        ((UploadPartRequest) ossRequest).setProgressCallback(new OSSProgressCallback<UploadPartRequest>() {
            @Override
            public void onProgress(UploadPartRequest uploadPartRequest, long l, long l1) {
                listener.onUploadProgress(uploadPartRequest, uploadedSize + l, file.length());
            }
        });

        oss.asyncUploadPart(((UploadPartRequest) ossRequest), partCallback);
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
            } else if (localException instanceof SocketTimeoutException) {
                return OSSUploadRetryType.ShouldNotRetry;
            } else if (localException instanceof SSLHandshakeException) {
                return OSSUploadRetryType.ShouldNotRetry;
            }
            OSSLog.logDebug("shouldRetry - " + e.toString());
            e.getCause().printStackTrace();
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
                uploadedParts.add(new PartETag(lastUploadedBlockIndex + 1, result.getETag()));
                uploadedSize += currentUploadLength;
                lastUploadedBlockIndex++;
                if (UploadStateType.CANCELED.equals(status)) {
                    abortUpload();
                    listener.onUploadFailed(UploadStateType.CANCELED.toString(), "This task is cancelled!");
                    OSSLog.logDebug("[OSSUploader] - This task is cancelled!");
                    return;
                } else if (UploadStateType.UPLOADING.equals(status)) {
                    if (uploadedSize < file.length()) {
                        uploadPart();
                    } else {
                        completeMultiPartUpload();
                    }
                } else if (UploadStateType.PAUSING.equals(status)) {
                    OSSLog.logDebug("[OSSUploader] - This task is pausing!");
                    uploadFileInfo.setStatus(UploadStateType.PAUSED);
                }
            } else if (res instanceof CompleteMultipartUploadResult) {
                OSSLog.logDebug("[OSSUploader] - CompleteMultipartUploadResult onSuccess ------------------");
                try {
                    inputStream.close();
                } catch (IOException e) {
                    OSSLog.logError("CompleteMultipartUploadResult inputStream close failed.");
                }
                uploadFileInfo.setStatus(UploadStateType.SUCCESS);
                listener.onUploadSucceed();
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

                    if (request instanceof InitiateMultipartUploadRequest) {
                        oss.asyncInitMultipartUpload((InitiateMultipartUploadRequest) ossRequest, initCallback);
                    } else if (request instanceof CompleteMultipartUploadRequest) {
                        oss.asyncCompleteMultipartUpload((CompleteMultipartUploadRequest) ossRequest, completedCallback);
                    } else if (request instanceof UploadPartRequest) {
                        oss.asyncUploadPart((UploadPartRequest) ossRequest, partCallback);
                    }

                    if (retryShouldNotify) {
                        if (clientException != null) {
                            listener.onUploadRetry(UploaderErrorCode.CLIENT_EXCEPTION, clientException.toString());
                        } else if (serviceException != null) {
                            listener.onUploadRetry(serviceException.getErrorCode(), serviceException.getMessage());
                        }

                        retryShouldNotify = false;
                    }

                    break;

                case ShouldGetSTS:
                    uploadFileInfo.setStatus(UploadStateType.PAUSED);
                    listener.onUploadTokenExpired();
                    break;

                case ShouldNotRetry:
                    uploadFileInfo.setStatus(UploadStateType.FAIlURE);
                    if (clientException != null) {
                        listener.onUploadFailed(UploaderErrorCode.CLIENT_EXCEPTION, clientException.toString());
                        if (request instanceof UploadPartRequest) {
                        } else {
                        }

                    } else if (serviceException != null) {
                        listener.onUploadFailed(serviceException.getErrorCode(), serviceException.getMessage());
                        if (request instanceof UploadPartRequest) {
                        } else {
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }


}

