/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload.internal;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
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
import com.alibaba.sdk.android.vod.upload.VODUploadClientImpl;
import com.alibaba.sdk.android.vod.upload.common.RequestIDSession;
import com.alibaba.sdk.android.vod.upload.common.UploadStateType;
import com.alibaba.sdk.android.vod.upload.common.utils.MD5;
import com.alibaba.sdk.android.vod.upload.common.utils.StringUtil;
import com.alibaba.sdk.android.vod.upload.exception.VODErrorCode;
import com.alibaba.sdk.android.vod.upload.model.OSSConfig;
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;
import com.aliyun.auth.core.VodThreadService;
import com.aliyun.vod.common.httpfinal.QupaiHttpFinal;
import com.aliyun.vod.common.utils.FileUtils;
import com.aliyun.vod.common.utils.StringUtils;
import com.aliyun.vod.log.core.AliyunLogCommon;
import com.aliyun.vod.log.core.AliyunLogParam;
import com.aliyun.vod.log.core.AliyunLogger;
import com.aliyun.vod.log.core.AliyunLoggerManager;
import com.aliyun.vod.log.core.LogService;
import com.aliyun.vod.log.report.AliyunUploadProgressReporter;
import com.aliyun.vod.log.struct.AliyunLogEvent;
import com.aliyun.vod.log.struct.AliyunLogKey;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;


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

    private AliyunUploadProgressReporter uploadProgressReporter;

    private String domainRegion = null;
    private boolean recoredUploadProgressEnabled = true;

    public ResumableUploaderImpl(Context context){
        this.context = new WeakReference(context);
        recordDirectory = getRecordDirectory();
        OSSLog.logDebug("OSS_RECORD : " + recordDirectory);
        if (AliyunLoggerManager.isLoggerOpen()) {
            uploadProgressReporter = new AliyunUploadProgressReporter(context);
        }
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
        if (configuration == null){
            clientConfig.setMaxErrorRetry(3);
            clientConfig.setSocketTimeout(ClientConfiguration.getDefaultConf().getSocketTimeout());
            clientConfig.setConnectionTimeout(ClientConfiguration.getDefaultConf().getSocketTimeout());
        }else {
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
            if (!bool){
                listener.onUploadFailed(VODErrorCode.PERMISSION_DENIED,"Create RecordDir Failed! Please Check Permission WRITE_EXTERNAL_STORAGE!");
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

    private void asycResumableUpload(UploadFileInfo uploadFileInfo){
        OSSLog.logDebug("VODSTS","OSS:\n" + "\nAccessKeyId:" + config.getAccessKeyId() + "\nAccessKeySecret:" + config.getAccessKeySecret() + "\nSecrityToken:" +config.getSecrityToken());

        oss = new OSSClient(context.get(), uploadFileInfo.getEndpoint(), config.getProvider(), clientConfig);

        OSSLog.logDebug("ResumeableUplaod","BucketName:" + uploadFileInfo.getBucket() + "\nobject:"+ uploadFileInfo.getObject() +
            "\nobject:"+ uploadFileInfo.getFilePath());

        // 创建断点上传请求，参数中给出断点记录文件的保存位置，需是一个文件夹的绝对路径
        if (StringUtils.isUriPath(uploadFileInfo.getFilePath())) {
            ossRequest = new
                ResumableUploadRequest(uploadFileInfo.getBucket(), uploadFileInfo.getObject(),
                Uri.parse(uploadFileInfo.getFilePath()), recordDirectory);
        } else {
            ossRequest = new
                ResumableUploadRequest(uploadFileInfo.getBucket(), uploadFileInfo.getObject(),
                uploadFileInfo.getFilePath(), recordDirectory);
        }
        ((ResumableUploadRequest)ossRequest).setDeleteUploadOnCancelling(!recoredUploadProgressEnabled);
        ((ResumableUploadRequest)ossRequest).setProgressCallback(progressCallback);
        long partSize = config.getPartSize() == 0 ? DEFAULT_PART_SIZE: config.getPartSize();
        File file = new File(uploadFileInfo.getFilePath());
        long fileLengh = FileUtils.getFileLength(context.get(), uploadFileInfo.getFilePath());
        if (fileLengh / partSize > 5000){
            partSize =  fileLengh / 4999;
        }
        ((ResumableUploadRequest)ossRequest).setPartSize(partSize);

        if (uploadProgressReporter != null) {
            // begin for progress report
            uploadProgressReporter.setDomainRegion(domainRegion);
            uploadProgressReporter.setFileName(file.getName());
            uploadProgressReporter.setFileSize(file.length());
            uploadProgressReporter.setFileCreateTime(AliyunLogParam.generateTimestamp(file.lastModified()));
            String fileHash = MD5.calculateMD5(this.context.get(), uploadFileInfo.getFilePath());
            uploadProgressReporter.setFileHash(fileHash);
            long totolSize = fileLengh;
            int totolPart = (int) (totolSize / partSize);
            uploadProgressReporter.setPartSize(partSize);
            uploadProgressReporter.setTotalPart(totolPart);
            uploadProgressReporter.setVideoId(config.getVideoId());
            uploadProgressReporter.setUploadAddress(config.getUploadAddress());
            // end for progress report
        }

        rusumebleTask = oss.asyncResumableUpload((ResumableUploadRequest)ossRequest,resumableCallback);
        this.uploadFileInfo.setStatus(UploadStateType.UPLOADING);

        startUploadLogger(uploadFileInfo);
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
        OSSLog.logDebug(ResumableUploaderImpl.class.getClass().getName(),"Resumeable Uploader Cancel");

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

    class OSSProgressCallbackImpl implements OSSProgressCallback{

        @Override
        public void onProgress(Object request, long currentSize, long totalSize) {
            OSSLog.logDebug("[OSSUploader] - onProgress..."+ (currentSize * 100 / totalSize));
            listener.onUploadProgress(request, currentSize,totalSize);
            if (uploadProgressReporter != null) {
                String timestamp = String.valueOf(System.currentTimeMillis());
                uploadProgressReporter.setAuthTimestamp(timestamp);
                uploadProgressReporter.setAuthInfo();
                uploadProgressReporter.setUploadRatio(currentSize * 1f / totalSize);
                if (request instanceof ResumableUploadRequest) {
                    ResumableUploadRequest resumableUploadRequest = (ResumableUploadRequest) request;
                    uploadProgressReporter.setUploadId(resumableUploadRequest.getUploadId());
                    uploadProgressReporter.setDonePartsCount((int) (currentSize / (config.getPartSize() == 0 ? DEFAULT_PART_SIZE: config.getPartSize())));
                }
                // do report
                if (uploadFileInfo.getFileType() != UploadFileInfo.UPLOAD_FILE_TYPE_IMAGE) {
                    // upload image not report
                    uploadProgressReporter.pushUploadProgress(config.getAccessKeySecret());
                }
            }

        }

    }

    class ResumableCompletedCallbackImpl implements OSSCompletedCallback{
        @Override
        public void onSuccess(OSSRequest request, OSSResult result) {
            rusumebleTask.isCompleted();
            uploadFileInfo.setStatus(UploadStateType.SUCCESS);
            listener.onUploadSucceed();
            uploadSuccessedLogger();
        }

        @Override
        public void onFailure(OSSRequest request, ClientException clientException, ServiceException serviceException) {
            OSSLog.logDebug("[OSSUploader] - onFailure Enter");
            if (clientException != null) {
                OSSLog.logDebug("[OSSUploader] - onFailure ClientException");
                if (clientException.isCanceledException()){
                    OSSLog.logDebug("[OSSUploader] - onFailure ClientException isCanceledException");
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
                OSSLog.logDebug("[OSSUploader] - onFailure ServiceException " + serviceException.getStatusCode());
                if (config != null) {
                    OSSLog.logDebug("[OSSUploader] - onFailure ServiceException token" + config.getSecrityToken());
                    OSSLog.logDebug("[OSSUploader] - onFailure ServiceException id" + config.getAccessKeyId());
                    OSSLog.logDebug("[OSSUploader] - onFailure ServiceException secret" + config.getAccessKeySecret());
                }
                if (serviceException.getStatusCode() == 403 && !StringUtil.isEmpty(config.getSecrityToken())){
                    OSSLog.logDebug("[OSSUploader] - onFailure ServiceException onUploadTokenExpired");
                    listener.onUploadTokenExpired();
                }else{
                    OSSLog.logDebug("[OSSUploader] - onFailure ServiceException onUploadFailed");
                    OSSLog.logDebug("[OSSUploader] - onFailure..."+ serviceException.getErrorCode() + serviceException.getMessage());
                    listener.onUploadFailed(serviceException.getErrorCode(), serviceException.getMessage());
                }
                OSSLog.logDebug("[OSSUploader] - onFailure ServiceException Done");
                uploadPartFailedLogger(serviceException.getErrorCode(), serviceException.toString());
                uploadFailedLogger(serviceException.getErrorCode(), serviceException.toString());
            }

        }
    }

    private long getPartSize(UploadFileInfo fileInfo){
        long partSize = config.getPartSize() == 0 ? DEFAULT_PART_SIZE: config.getPartSize();
        long fileLengh = new File(fileInfo.getFilePath()).length();
        if (fileLengh / partSize > 5000){
            partSize =  fileLengh / 4999;
        }
        return partSize;
    }

    private long getPartNum(String  filePath){
        long partSize = config.getPartSize() == 0 ? DEFAULT_PART_SIZE: config.getPartSize();
        long fileLengh = new File(filePath).length();
        long partNum = fileLengh / partSize;
        if (fileLengh / partSize > 5000){
            return 4999;
        }
        return partNum;
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
                        Bitmap bitmap = null;
                        if (uploadFileInfo.getFileType() == UploadFileInfo.UPLOAD_FILE_TYPE_VIDEO) {
                            bitmap = FileUtils.getVideoSize(fileInfo.getFilePath());
                        }
                        Map<String, String> args = new HashMap<>();
                        args.put(AliyunLogKey.KEY_FILE_TYPE, FileUtils.getMimeType(fileInfo.getFilePath()));
                        args.put(AliyunLogKey.KEY_FILE_SIZE, String.valueOf(new File(fileInfo.getFilePath()).length()));//byte???
                        args.put(AliyunLogKey.KEY_FILE_WIDTH, bitmap == null ?"":String.valueOf(bitmap.getWidth()));
                        args.put(AliyunLogKey.KEY_FILE_HEIGHT,bitmap == null ?"":String.valueOf(bitmap.getHeight()) );
                        args.put(AliyunLogKey.KEY_FILE_MD5, FileUtils.getMd5OfFile(fileInfo.getFilePath()));
                        args.put(AliyunLogKey.KEY_PART_SIZE, String.valueOf(getPartSize(fileInfo)));
                        args.put(AliyunLogKey.KEY_BUCKET, fileInfo.getBucket());
                        args.put(AliyunLogKey.KEY_OBJECT_KEY, fileInfo.getObject());
                        logger.pushLog(args, AliyunLogCommon.Product.VIDEO_UPLOAD,AliyunLogCommon.LogLevel.DEBUG, AliyunLogCommon.MODULE,AliyunLogCommon.SubModule.UPLOAD,
                                AliyunLogEvent.EVENT_UPLOAD_STARTED,AliyunLogCommon.LogStores.UPLOAD,requestIDSession.getRequestID());
                    }
                });
            }
        }
    }

    private void startUploadPartLogger(final String uploadId,final String filePath,final boolean retryShouldNotify) {
        final AliyunLogger logger = AliyunLoggerManager.getLogger(VODUploadClientImpl.class.getName());
        if (logger != null) {
            LogService logService = logger.getLogService();
            if (logService != null) {
                logService.execute(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, String> args = new HashMap<>();
                        args.put(AliyunLogKey.KEY_UPLOADID, uploadId);
                        args.put(AliyunLogKey.KEY_PART_NUMBER, String.valueOf(getPartNum(filePath)));
                        args.put(AliyunLogKey.KEY_PART_RETRY,retryShouldNotify?"0":"1");
                        logger.pushLog(args, AliyunLogCommon.Product.VIDEO_UPLOAD,AliyunLogCommon.LogLevel.DEBUG, AliyunLogCommon.MODULE,AliyunLogCommon.SubModule.UPLOAD,
                                AliyunLogEvent.EVENT_UPLOAD_PART_START,AliyunLogCommon.LogStores.UPLOAD,requestIDSession.getRequestID());
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
                                AliyunLogEvent.EVENT_UPLOAD_PART_COMPLETED,AliyunLogCommon.LogStores.UPLOAD,requestIDSession.getRequestID());
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

    private void uploadCancelLogger(){
        final AliyunLogger logger = AliyunLoggerManager.getLogger(VODUploadClientImpl.class.getName());
        if (logger != null) {
            LogService logService = logger.getLogService();
            if (logService != null) {
                logService.execute(new Runnable() {
                    @Override
                    public void run() {
                        logger.pushLog(null, AliyunLogCommon.Product.VIDEO_UPLOAD,AliyunLogCommon.LogLevel.DEBUG, AliyunLogCommon.MODULE,AliyunLogCommon.SubModule.UPLOAD,
                                AliyunLogEvent.EVENT_UPLOAD_CANCEL,AliyunLogCommon.LogStores.UPLOAD,requestIDSession.getRequestID());
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
}
