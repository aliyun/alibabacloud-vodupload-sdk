//
//  VODUploadClient.m
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import <AliyunOSSiOS/OSSService.h>
#import "VODUploadClient.h"
#import "AVCVAssetInfo.h"
#import <CommonCrypto/CommonDigest.h>
#import <MobileCoreServices/MobileCoreServices.h>
#import "VODOpenApi.h"
#import "VODObjectCache.h"
#import "VODInnerFileInfo.h"

NSString *const VODUploadSDKVersion = @"1.5.3";

typedef NS_ENUM(NSInteger, VODRetryType) {
    VODRetryTypeNotRetry,
    VODRetryTypeRetry,
    VODRetryTypeRetryWithSTS,
    VODRetryTypeNext,
    VODRetryTypeNotNotify
};

@interface VODUploadClient()
@property (nonatomic, assign) int retryNotify;
@end

@implementation VODUploadClient
{
    VODStatus state;
    OSSClient* client;
    OSSClientConfiguration *conf;
    OSSConfig *ossConfig;
    NSString* uploadId;
    NSString* _curUploadAddress;
    NSMutableArray *uploadList;
    UploadFileInfo *cur;
    VODInnerFileInfo *_curInnerFileInfo;
    OSSResumableUploadRequest* resumableUploadReq;
    VODUploadListener *callback;
    // open api 返回参数
    NSString *_curImageURL;
    NSString *_curVideoId;
    OSSConfig *_openApiConfig;
}

@synthesize requestId;

-(id<OSSCredentialProvider>) getCredentialProvider {
    OSSConfig *config = ossConfig;
    if (_openApiConfig) {
        config = _openApiConfig;
    }
    if (config == nil) {
        return nil;
    }
    
    if (config.accessKeyId.length <= 0 || config.accessKeySecret.length <= 0) {
        return nil;
    }
    
    if (config.secretToken == nil || config.secretToken.length <= 0) {
        return [[OSSPlainTextAKSKPairCredentialProvider alloc] initWithPlainTextAccessKey:config.accessKeyId
                             secretKey:config.accessKeySecret];
    } else {
        __weak OSSConfig *weakConfig = config;
        id<OSSCredentialProvider> credential1 = [[OSSFederationCredentialProvider alloc] initWithFederationTokenGetter:^OSSFederationToken * {
            OSSFederationToken * token = [OSSFederationToken new];
            token.tAccessKey = weakConfig.accessKeyId;
            token.tSecretKey = weakConfig.accessKeySecret;
            token.tToken = weakConfig.secretToken;
            token.expirationTimeInGMTFormat = weakConfig.expireTime;
            return token;
        }];
        
        return credential1;
    }
}

- (id)init {
    if (self = [super init]) {
        client = nil;
        _maxRetryCount = INT_MAX;
        _timeoutIntervalForRequest = 30;
        conf = [OSSClientConfiguration new];
        conf.maxRetryCount = 2;
        conf.timeoutIntervalForRequest = 30;
        
        ossConfig = [[OSSConfig alloc] init];
        uploadId = nil;
        resumableUploadReq = nil;
        callback = nil;
        state = VODStatusReady;
        uploadList = [[NSMutableArray alloc] init];
        _retryNotify = 0;
        _recordDirectoryPath = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject];
        _uploadPartSize = 1024 * 1024;
        _transcode = YES;
        _region = @"cn-shanghai";
        _recordUploadProgress = YES;
    }
    return self;
}

- (BOOL)init:(VODUploadListener *) listener {
    callback = listener;
    
    return TRUE;
}

- (BOOL)setListener:(VODUploadListener *) listener {
    callback = listener;
    
    return TRUE;
}

- (BOOL)        init:(NSString *)accessKeyId
     accessKeySecret:(NSString *)accessKeySecret
            listener:(VODUploadListener *)listener {
    if (accessKeyId == nil || accessKeyId.length == 0) {
        return FALSE;
    }
    
    if (accessKeySecret == nil || accessKeySecret.length == 0) {
        return FALSE;
    }
    
    ossConfig.accessKeyId = accessKeyId;
    ossConfig.accessKeySecret = accessKeySecret;
    ossConfig.secretToken = nil;
    ossConfig.expireTime = nil;
    callback = listener;
    
    return TRUE;
}

- (BOOL)    setKeyId:(NSString *)accessKeyId
     accessKeySecret:(NSString *)accessKeySecret
            listener:(VODUploadListener *) listener {
    if (accessKeyId == nil || accessKeyId.length == 0) {
        return FALSE;
    }
    
    if (accessKeySecret == nil || accessKeySecret.length == 0) {
        return FALSE;
    }
    
    ossConfig.accessKeyId = accessKeyId;
    ossConfig.accessKeySecret = accessKeySecret;
    ossConfig.secretToken = nil;
    ossConfig.expireTime = nil;
    callback = listener;
    
    return TRUE;
}

- (BOOL)        init:(NSString *)accessKeyId
     accessKeySecret:(NSString *)accessKeySecret
         secretToken:(NSString *)secretToken
          expireTime:(NSString *)expireTime
            listener:(VODUploadListener *)listener {
    if (accessKeyId == nil || accessKeyId.length == 0) {
        return FALSE;
    }
    
    if (accessKeySecret == nil || accessKeySecret.length == 0) {
        return FALSE;
    }
    
    if (secretToken == nil || secretToken.length == 0) {
        return FALSE;
    }
    
    if (expireTime == nil || expireTime.length == 0) {
        return FALSE;
    }
    
    ossConfig.accessKeyId = accessKeyId;
    ossConfig.accessKeySecret = accessKeySecret;
    ossConfig.secretToken = secretToken;
    ossConfig.expireTime = expireTime;
    
    callback = listener;
    
    return TRUE;
}

- (BOOL)    setKeyId:(NSString *)accessKeyId
     accessKeySecret:(NSString *)accessKeySecret
         secretToken:(NSString *)secretToken
          expireTime:(NSString *)expireTime
            listener:(VODUploadListener *) listener {
    if (accessKeyId == nil || accessKeyId.length == 0) {
        return FALSE;
    }
    
    if (accessKeySecret == nil || accessKeySecret.length == 0) {
        return FALSE;
    }
    
    if (secretToken == nil || secretToken.length == 0) {
        return FALSE;
    }
    
    if (expireTime == nil || expireTime.length == 0) {
        return FALSE;
    }
    
    ossConfig.accessKeyId = accessKeyId;
    ossConfig.accessKeySecret = accessKeySecret;
    ossConfig.secretToken = secretToken;
    ossConfig.expireTime = expireTime;
    
    callback = listener;
    
    return TRUE;
}

#pragma mark - getter setter

-(void)setMaxRetryCount:(uint32_t)maxRetryCount {
    _maxRetryCount = maxRetryCount;
//    conf.maxRetryCount = maxRetryCount;
}

-(void)setTimeoutIntervalForRequest:(NSTimeInterval)timeoutIntervalForRequest {
    _timeoutIntervalForRequest = timeoutIntervalForRequest;
    conf.timeoutIntervalForRequest = timeoutIntervalForRequest;
}

-(void)setRecordUploadProgress:(BOOL)recordUploadProgress {
    _recordUploadProgress = recordUploadProgress;
    [DefaultCache setEnabled:recordUploadProgress];
}

-(void)setLogStore:(NSString *)logStore {

}

- (void)setVideoId:(NSString *)videoId {
    _curVideoId = videoId;
}

/**
 添加视频上传
 */
- (BOOL)addFile:(NSString *)filePath
        vodInfo:(VodInfo *)vodInfo {
    OSSLogDebug(@"addFile called.%@", filePath);
    if (filePath == nil || filePath.length == 0) {
        return FALSE;
    }
//    for(int i=0; i<[uploadList count]; i++) {
//        if ([[[uploadList objectAtIndex:i] filePath] isEqualToString:filePath]) {
//            return FALSE;
//        }
//    }
    
    UploadFileInfo* info = [[UploadFileInfo alloc] init];
    info.filePath = filePath;
    info.state = VODUploadFileStatusReady;
    info.vodInfo = vodInfo;
    [uploadList addObject:info];
    
    return TRUE;
}

/**
 添加视频上传
 */
- (BOOL)addFile:(NSString *)filePath
       endpoint:(NSString *)endpoint
         bucket:(NSString *)bucket
         object:(NSString *)object {
    OSSLogDebug(@"addFile called.%@", filePath);
    if (filePath == nil || filePath.length == 0 ||
        endpoint == nil || endpoint.length == 0 ||
        bucket == nil || bucket.length == 0 ||
        object == nil || object.length == 0) {
        return FALSE;
    }
    
//    for(int i=0; i<[uploadList count]; i++) {
//        if ([[[uploadList objectAtIndex:i] filePath] isEqualToString:filePath]) {
//            return FALSE;
//        }
//    }
    
    UploadFileInfo* info = [[UploadFileInfo alloc] init];
    info.filePath = filePath;
    info.endpoint = endpoint;
    info.bucket = bucket;
    info.object = object;
    info.state = VODUploadFileStatusReady;
    
    [uploadList addObject:info];
    return TRUE;
}

- (BOOL)addFile:(NSString *)filePath
       endpoint:(NSString *)endpoint
         bucket:(NSString *)bucket
         object:(NSString *)object
        vodInfo:(VodInfo *)vodInfo{
    OSSLogDebug(@"addFile called.%@", filePath);
    if (filePath == nil || filePath.length == 0 ||
        endpoint == nil || endpoint.length == 0 ||
        bucket == nil || bucket.length == 0 ||
        object == nil || object.length == 0) {
        return FALSE;
    }
    
//    for(int i=0; i<[uploadList count]; i++) {
//        if ([[[uploadList objectAtIndex:i] filePath] isEqualToString:filePath]) {
//            return FALSE;
//        }
//    }
    
    UploadFileInfo* info = [[UploadFileInfo alloc] init];
    info.filePath = filePath;
    info.endpoint = endpoint;
    info.bucket = bucket;
    info.object = object;
    info.vodInfo = vodInfo;
    info.state = VODUploadFileStatusReady;
    
    [uploadList addObject:info];
    return TRUE;
}

/**
 删除视频上传
 */
- (BOOL)deleteFile:(int)index {
    OSSLogDebug(@"deleteFile called.%d", index);
    if (index < 0 || index >= [uploadList count]) {
        return FALSE;
    }

    UploadFileInfo *obj = [uploadList objectAtIndex:index];
    if (obj.state == VODUploadFileStatusUploading) {
        if (resumableUploadReq) {
            [resumableUploadReq cancel];
            if ([resumableUploadReq isCancelled]) {
                OSSLogDebug(@"Cancel upload request.");
            }
        }
        [self next];
    }
    [uploadList removeObjectAtIndex:index];
    [DefaultCache clearObjectWithPath:obj.filePath];
    return TRUE;
}

/**
 清除上传列表
 */
- (BOOL)clearFiles {
    OSSLogDebug(@"clearFiles called.");
    for (UploadFileInfo *info in uploadList) {
        [DefaultCache clearObjectWithPath:info.filePath];
    }
    [uploadList removeAllObjects];
    if (cur.state == VODUploadFileStatusUploading) {
        if (resumableUploadReq) {
            [resumableUploadReq cancel];
            if ([resumableUploadReq isCancelled]) {
                OSSLogDebug(@"Cancel upload request.");
            }
        }
    }
    
    return TRUE;
}

- (NSMutableArray<UploadFileInfo *> *)listFiles {
    return uploadList;
}

/**
 取消单个视频上传
 */
- (BOOL)cancelFile:(int)index {
    OSSLogDebug(@"cancelFile called.%d", index);
    if (index < 0 || index >= [uploadList count]) {
        return FALSE;
    }

    UploadFileInfo *obj = [uploadList objectAtIndex:index];
    if (obj.state != VODUploadFileStatusReady && obj.state != VODUploadFileStatusUploading &&
        obj.state != VODUploadFileStatusPaused) {
        return FALSE;
    }
    
    obj.state = VODUploadFileStatusCanceled;
    if (obj.state == VODUploadFileStatusUploading) {
        if (resumableUploadReq) {
            [resumableUploadReq cancel];
            if ([resumableUploadReq isCancelled]) {
                OSSLogDebug(@"Cancel upload request.");
            }
        }
        [self next];
    }
    
    return TRUE;
}

/**
 恢复已取消的上传文件
 */
- (BOOL)resumeFile:(int)index {
    OSSLogDebug(@"resumeFile called.%d", index);
    if (index < 0 || index >= [uploadList count]) {
        return FALSE;
    }

    UploadFileInfo *obj = [uploadList objectAtIndex:index];
    if (obj.state != VODUploadFileStatusCanceled) {
        return FALSE;
    }
    obj.state = VODUploadFileStatusReady;
    if (state == VODStatusSuccess || state == VODStatusFailure) {
        state = VODStatusStarted;
        [self next];
    }
    
    return TRUE;
}

/**
 开始上传
 */
- (BOOL)start {
    OSSLogDebug(@"start called.");
    if (state == VODStatusStarted) {
        return FALSE;
    }
    
    state = VODStatusStarted;
    
    [self next];
    
    return TRUE;
}

/**
 停止上传
 */
- (BOOL)stop {
    OSSLogDebug(@"stop called.");
    if (state != VODStatusStarted && state != VODStatusPaused) {
        return FALSE;
    }
    
    state = VODStatusStoped;
    if (resumableUploadReq) {
        [resumableUploadReq cancel];
        if ([resumableUploadReq isCancelled]) {
            OSSLogDebug(@"Cancel upload request.");
        }
    } else {
        OSSLogDebug(@"No runnning upload request.");
    }
    
    resumableUploadReq = nil;
    
    return TRUE;
}

/**
 暂停上传
 */
- (BOOL)pause {
    OSSLogDebug(@"pause called.");
    if (state != VODStatusStarted) {
        return FALSE;
    }
    
    state = VODStatusPaused;
    if (resumableUploadReq) {
        [resumableUploadReq cancel];
        if ([resumableUploadReq isCancelled]) {
            OSSLogDebug(@"Cancel upload request.");
        }
    } else {
        OSSLogDebug(@"No runnning upload request.");
    }
    
    return TRUE;
}

/**
 恢复上传
 */
- (BOOL)resume {
    OSSLogDebug(@"resume called.");
    if (state != VODStatusPaused) {
        return FALSE;
    }
    
    state = VODStatusStarted;
    
    if (uploadId.length > 0) {
        [self resumableUpload];
    } else {
        [self next];
    }
    
    return TRUE;
}

/**
 使用Token恢复上传
 */
- (BOOL)resumeWithAuth:(NSString *)uploadAuth {
    if (uploadAuth == nil || uploadAuth.length == 0) {
        return FALSE;
    }
    NSError *error = nil;
    NSData *nsdataUploadAuthFromBase64String = [[NSData alloc]
                                      initWithBase64EncodedString:uploadAuth
                                      options:0];
    NSDictionary *authKey = [NSJSONSerialization
                             JSONObjectWithData:nsdataUploadAuthFromBase64String
                             options:NSJSONReadingMutableLeaves
                             //options:NSJSONReadingMutableContainers
                             error:&error];
    if (nil != error) {
        OSSLogDebug(@"upload auth format is error.");
        return FALSE;
    }
    NSString *expireTime = [authKey objectForKey:@"Expiration"];
    // 如果能解析出ExpireUTCTime，使用ExpireUTCTime
    NSString *expireUTCTime = [authKey objectForKey:@"ExpireUTCTime"];
    if (expireUTCTime.length) {
        expireTime = expireUTCTime;
    }
    return [self resumeWithToken:[authKey objectForKey:@"AccessKeyId"]
                 accessKeySecret:[authKey objectForKey:@"AccessKeySecret"]
                     secretToken:[authKey objectForKey:@"SecurityToken"]
                      expireTime:expireTime];
}

- (BOOL)resumeWithToken:(NSString *)accessKeyId
        accessKeySecret:(NSString *)accessKeySecret
            secretToken:(NSString *)secretToken
             expireTime:(NSString *)expireTime {
    OSSLogDebug(@"resume upload with new token.");
    if (state != VODStatusPaused) {
        return FALSE;
    }
    
    if (accessKeyId == nil || accessKeyId.length == 0) {
        return FALSE;
    }
    
    if (accessKeySecret == nil || accessKeySecret.length == 0) {
        return FALSE;
    }
    
    if (secretToken == nil || secretToken.length == 0) {
        return FALSE;
    }
    
    if (expireTime == nil || expireTime.length == 0) {
        return FALSE;
    }
    
    ossConfig.accessKeyId = accessKeyId;
    ossConfig.accessKeySecret = accessKeySecret;
    ossConfig.secretToken = secretToken;
    ossConfig.expireTime = expireTime;
    
    state = VODStatusStarted;
    
    if (uploadId.length > 0) {
        if (_openApiConfig) {   // 上传到vod，需要先调用openapi
            [self requestUploadAuthAddress];
        }else {
            [self resumableUpload];
        }
    } else {
        [self next];
    }
    
    return TRUE;
}

/**
  继续上传
 */
- (void)next {
    //clear
    uploadId = nil;
    cur = nil;
    _curImageURL = nil;
    _curVideoId = nil;
    _openApiConfig = nil;
    _curInnerFileInfo = nil;
    for(int i=0; i<[uploadList count]; i++) {
        if ([[uploadList objectAtIndex:i] state] == VODUploadFileStatusReady) {
            cur =[uploadList objectAtIndex:i];
            break;
        }
    }
    
    if (nil == cur) {
        state = VODStatusSuccess;
        return;
    }
     if (nil != callback.started) {
         callback.started(cur);
     }
    
    _curInnerFileInfo = [[VODInnerFileInfo alloc] initWithPath:cur.filePath];
    
    if (cur.bucket && cur.endpoint) {   // 直接上传到oss
        [self startOSSClientUpload];
    } else {                            // 先请求点播凭证
        [self requestUploadAuthAddress];
    }
}

-(VODRetryType)shouldRetry:(NSError *)error {
    switch (error.code) {
        case OSSClientErrorCodeNetworkError:
            return VODRetryTypeRetry;
            
        case OSSClientErrorCodeTaskCancelled:
            if (state != VODStatusPaused && state != VODStatusStoped)
                return VODRetryTypeNext;
            else
                return VODRetryTypeNotNotify;
            break;
            
        case -1 * (403):
            if ([[error.userInfo objectForKey:@"Code"] isEqualToString:@"InvalidAccessKeyId"] ||
                [[error.userInfo objectForKey:@"Code"] isEqualToString:@"SecurityTokenExpired"]) {
                return VODRetryTypeRetryWithSTS;
            }
            
            break;
            
        default:
            if (error.code < -500) {
                return VODRetryTypeRetry;
            }
            
            break;
    }
    
    return VODRetryTypeNotRetry;
}

-(void)initMultiUpload {
    OSSLogDebug(@"send initMultiUpload request.....");
    OSSInitMultipartUploadRequest * init = [OSSInitMultipartUploadRequest new];
    init.bucketName = cur.bucket;
    init.objectKey = cur.object;
    OSSTask * task = [client multipartUploadInit:init];
    [self sendStartEvent];
    [task continueWithBlock:^id(OSSTask *task) {
        if (!task.error) {
            if (_retryNotify) {
                if (nil == callback.retryResume) {
                    callback.retryResume();
                }
                _retryNotify = 0;
            }
            OSSInitMultipartUploadResult * result = task.result;
            uploadId = result.uploadId;
            OSSLogDebug(@"init uploadid success. uploadId:%@", uploadId);
            cur.state = VODUploadFileStatusUploading;
            [self resumableUpload];
        } else {
            OSSLogDebug(@"init uploadid failed, error: %@", task.error);
            
            switch ([self shouldRetry:task.error]) {
                case VODRetryTypeNotRetry:
                    callback.failure(cur,
                                     [task.error.userInfo objectForKey:@"Code"] ? : @(task.error.code).stringValue,
                                     [task.error.userInfo objectForKey:@"Message"] ? : task.error.description);
                    cur.state = VODUploadFileStatusFailure;
                    break;
                    
                case VODRetryTypeNotNotify:
                    break;
                    
                case VODRetryTypeRetryWithSTS:
                    OSSLogDebug(@"Token expire.");
                    state = VODStatusPaused;
                    callback.expire();
                    break;
                    
                case VODRetryTypeNext:
                    cur.state = VODUploadFileStatusCanceled;
                    [self next];
                    break;
                    
                default:
                    if (state != VODStatusPaused) {
                        OSSLogDebug(@"initMultiUpload Retry after 2 second....");
                        if (_retryNotify < _maxRetryCount) {
                            if (nil != callback.retry && _retryNotify == 0) {
                                callback.retry();
                            }
                            _retryNotify ++;
                        }else {
                            callback.failure(cur,
                                             [task.error.userInfo objectForKey:@"Code"] ? : @(task.error.code).stringValue,
                                             [task.error.userInfo objectForKey:@"Message"] ? : task.error.description);
                            cur.state = VODUploadFileStatusFailure;
                            _retryNotify = 0;
                            break;
                        }
                        sleep(2);
                        if (state != VODStatusPaused) {
                            [self initMultiUpload];
                        }
                    }
                    
                    break;
            }
        }
        
        return nil;
    }];
}

-(void)resumableUpload {
    OSSLogDebug(@"send resumableUpload request.....");
    
    UploadFileInfo* fileInfo = cur;
    [client setCredentialProvider:[self getCredentialProvider]];
    NSDictionary *fileAttributes = [[NSFileManager defaultManager] attributesOfItemAtPath:fileInfo.filePath error:nil];
    NSNumber *fileSizeNumber = [fileAttributes objectForKey:NSFileSize];
    long long fileSize = [fileSizeNumber longLongValue];
    int minPartSize = (int)(fileSize/4999);
    resumableUploadReq = [OSSResumableUploadRequest new];
    resumableUploadReq.recordDirectoryPath = _recordDirectoryPath;
    resumableUploadReq.bucketName = cur.bucket;
    resumableUploadReq.objectKey = cur.object;
    resumableUploadReq.uploadId = uploadId;
    resumableUploadReq.partSize = _uploadPartSize > minPartSize ? _uploadPartSize : minPartSize;
    resumableUploadReq.deleteUploadIdOnCancelling = !_recordUploadProgress;
    NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
    
    if (nil != cur.vodInfo) {
        [dictionary setObject:cur.vodInfo.toJson forKey:@"x-oss-notification"];
        resumableUploadReq.completeMetaHeader = dictionary;
    }
    
    __weak OnUploadProgressListener weakProcessCallback = callback.progress;
    __weak OnUploadRertyResumeListener weakRetryResume = callback.retryResume;
    __weak NSString *weakUploadId = uploadId;
    __weak NSString *weakVideoId = _curVideoId;
    __block int index = 0;
    __weak VODUploadClient *weakSelf = self;
    __weak VODInnerFileInfo *weakInnerFileInfo = _curInnerFileInfo;
    __weak NSString *weakRegion = _region;
    __weak NSString *weakUploadAddress = _curUploadAddress;
    
    resumableUploadReq.uploadProgress = ^(int64_t bytesSent, int64_t totalByteSent, int64_t totalBytesExpectedToSend) {
        OSSLogDebug(@"%lld, %lld, %lld", bytesSent, totalByteSent, totalBytesExpectedToSend);
        
        if (nil != weakRetryResume && weakSelf.retryNotify) {
            weakRetryResume();
            weakSelf.retryNotify = 0;
        }
        weakProcessCallback(fileInfo, (long)totalByteSent, (long)totalBytesExpectedToSend);
    };
    
    resumableUploadReq.uploadingFileURL = [NSURL fileURLWithPath:cur.filePath];
    OSSTask * resumeTask = [client resumableUpload:resumableUploadReq];
    [resumeTask continueWithBlock:^id(OSSTask *task) {
        if (task.error) {
            OSSLogDebug(@"resumableUpload error: %@", task.error);
            switch ([self shouldRetry:task.error]) {
                case VODRetryTypeNotRetry:
                    callback.failure(cur,
                                     [task.error.userInfo objectForKey:@"Code"] ? : @(task.error.code).stringValue,
                                     [task.error.userInfo objectForKey:@"Message"] ? : task.error.description);
                    cur.state = VODUploadFileStatusFailure;
                    break;
                    
                case VODRetryTypeNotNotify:
                    break;
                    
                case VODRetryTypeRetryWithSTS:
                    OSSLogDebug(@"Token expire.");
                    state = VODStatusPaused;
                    callback.expire();
                    break;
                    
                case VODRetryTypeNext:
                    [self next];
                    break;
                    
                default:
                    if (state != VODStatusPaused) {
                        OSSLogDebug(@"resumableUpload Retry after 2 second....%ld", (long)cur.state);
                        if (_retryNotify < _maxRetryCount) {
                            if (nil != callback.retry && _retryNotify == 0) {
                                callback.retry();
                            }
                            _retryNotify ++;
                        }else {
                            callback.failure(cur,
                                             [task.error.userInfo objectForKey:@"Code"] ? : @(task.error.code).stringValue,
                                             [task.error.userInfo objectForKey:@"Message"] ? : task.error.description);
                            cur.state = VODUploadFileStatusFailure;
                            _retryNotify = 0;
                            break;
                        }
                        sleep(2);
                        if (state != VODStatusPaused) {
                            [self resumableUpload];
                        }
                    }
                    
                    break;
            }
        } else {
            if (_retryNotify) {
                if (nil != callback.retryResume) {
                    callback.retryResume();
                }
                _retryNotify = 0;
            }
            OSSLogDebug(@"Upload file success");
            cur.state = VODUploadFileStatusSuccess;
            [DefaultCache clearObjectWithPath:cur.filePath];
            if (callback.success) {
                callback.success(cur);
            }
            if (callback.finish) {
                VodUploadResult *result = [VodUploadResult new];
                result.videoId = _curVideoId;
                result.imageUrl = _curImageURL;
                result.bucket = cur.bucket;
                result.endpoint = cur.endpoint;
                callback.finish(cur, result);
            }
            resumableUploadReq = nil;
            [self next];
        }
        
        return nil;
    }];
}

/**
 设置上传凭证
 */
- (BOOL)setUploadAuthAndAddress:(UploadFileInfo *)uploadFileInfo
           uploadAuth:(NSString *)uploadAuth
        uploadAddress:(NSString *)uploadAddress {
    if (uploadFileInfo == nil) {
        return NO;
    }
    if (uploadAuth == nil || uploadAuth.length == 0) {
        return NO;
    }
    BOOL rv = [self parseUploadAuth:uploadAuth config:ossConfig];
    if (!rv) {
        return NO;
    };
    rv = [self parseUploadAddress:uploadAddress uploadInfo:uploadFileInfo];
    return rv;
}

- (BOOL)parseUploadAddress:(NSString *)uploadAddress uploadInfo:(UploadFileInfo *)uploadFileInfo {
    NSError *error = nil;
    NSData *nsdataUploadAddressFromBase64String = [[NSData alloc]
                                      initWithBase64EncodedString:uploadAddress
                                      options:0];
    if (!nsdataUploadAddressFromBase64String) {
        OSSLogDebug(@"upload address format is error.");
        return NO;
    }
    NSDictionary *addressKey = [NSJSONSerialization
                         JSONObjectWithData:nsdataUploadAddressFromBase64String
                         options:NSJSONReadingMutableLeaves
                         //options:NSJSONReadingMutableContainers
                         error:&error];
    if (nil != error) {
        OSSLogDebug(@"upload address format is error.");
        return NO;
    }
    uploadFileInfo.endpoint = [addressKey objectForKey:@"Endpoint"];
    uploadFileInfo.bucket = [addressKey objectForKey:@"Bucket"];
    NSString *object =  [addressKey objectForKey:@"FileName"];
    
    VODObject* vodObj = [DefaultCache getObjectWithPath:cur.filePath];
    if(vodObj.object.length == 0) {
        vodObj.object = object;
    }else if (vodObj.videoId.length == 0) {
        vodObj.object = object;
    }
    [DefaultCache saveObject:vodObj path:cur.filePath];
    
    uploadFileInfo.object = vodObj.object ? : object;
    _curUploadAddress = uploadAddress;
    return YES;
}

- (BOOL)parseUploadAuth:(NSString *)uploadAuth config:(OSSConfig *)config {
    NSError *error = nil;
    NSData *nsdataUploadAuthFromBase64String = [[NSData alloc]
                                      initWithBase64EncodedString:uploadAuth
                                      options:0];
    if (!nsdataUploadAuthFromBase64String) {
        OSSLogDebug(@"upload auth format is error");
        return NO;
    }
    NSDictionary *authKey = [NSJSONSerialization
                             JSONObjectWithData:nsdataUploadAuthFromBase64String
                             options:NSJSONReadingMutableLeaves
                             //options:NSJSONReadingMutableContainers
                             error:&error];
    
    if (nil != error) {
        OSSLogDebug(@"upload auth format is error.");
        return NO;
    }
    config.accessKeyId = [authKey objectForKey:@"AccessKeyId"];
    config.accessKeySecret = [authKey objectForKey:@"AccessKeySecret"];
    config.secretToken = [authKey objectForKey:@"SecurityToken"];
    config.expireTime = [authKey objectForKey:@"Expiration"];
    // 如果能解析出ExpireUTCTime，使用ExpireUTCTime
    NSString *expireUTCTime = [authKey objectForKey:@"ExpireUTCTime"];
    if (expireUTCTime.length) {
        config.expireTime = expireUTCTime;
    }
    
    // 点播凭证上传，需要解析Auth中的Region
    NSString* region = [authKey objectForKey:@"Region"];
    if (region.length) {
        _region = region;
    }
    return YES;
}

#pragma mark - open api

- (void)startOSSClientUpload {
    client = [[OSSClient alloc] initWithEndpoint:cur.endpoint credentialProvider:[self getCredentialProvider] clientConfiguration:conf];
        if (client) {
            OSSLogError(@"oss client init success!");
        } else {
            OSSLogDebug(@"oss client init failed.");
        }
        [self initMultiUpload];
}

- (void)requestUploadAuthAddress {
    if ([_curInnerFileInfo.fileType isEqualToString:@"other"]) {
        NSString *code = @"400";
        NSString *message = @"upload file path has no extension or extension type is not video/image";
        callback.failure(cur, code, message);
        cur.state = VODUploadFileStatusFailure;
    }else if ([_curInnerFileInfo.fileType isEqualToString:@"img"]) {
        [self createUploadImage];
    }else {
        [self createUploadVideo];
    }
}

// 调用创建视频上传OpenApi
- (void)createUploadVideo {
    NSMutableDictionary *params = [NSMutableDictionary dictionary];
    if (cur.vodInfo.coverUrl) {
        [params setValue:cur.vodInfo.coverUrl forKey:@"CoverURL"];
    }
    [params setValue:_curInnerFileInfo.fileSize forKey:@"FileSize"];
    if (cur.vodInfo.desc) {
        [params setValue:cur.vodInfo.desc forKey:@"Description"];
    }
    if (cur.vodInfo.cateId) {
        [params setValue:cur.vodInfo.cateId forKey:@"CateId"];
    }
    if (cur.vodInfo.tags) {
        [params setValue:cur.vodInfo.tags forKey:@"Tags"];
    }
    if (cur.vodInfo.storageLocation) {
        [params setValue:cur.vodInfo.storageLocation forKey:@"StorageLocation"];
    }
    if (cur.vodInfo.templateGroupId) {
        [params setValue:cur.vodInfo.templateGroupId forKey:@"TemplateGroupId"];
    }
    
    if (!_transcode && !cur.vodInfo.templateGroupId.length) {
        [params setValue:@"NoTranscode" forKey:@"TranscodeMode"];
    }
    if (_appId.length) {
        [params setValue:_appId forKey:@"AppId"];
    }
    if (_workflowId.length) {
        [params setValue:_workflowId forKey:@"WorkflowId"];
    }
    NSString *userData = [self userDataWithVideoPath:cur.filePath userData:cur.vodInfo.userData];
    if (userData) {
        [params setValue:userData forKey:@"UserData"];
    }
    
    [self createOrRefreshUploadVideoWithKeyId:ossConfig.accessKeyId keySecret:ossConfig.accessKeySecret token:ossConfig.secretToken title:cur.vodInfo.title fileName:cur.filePath optionalParams:params completionHandler:^(NSURLResponse *response, id  _Nullable responseObject, NSError * _Nullable error) {
        if (error) {
            // sts 过期处理
            NSDictionary *info = [error userInfo];
            NSString *code = [info objectForKey:@"Code"] ? : @(error.code).stringValue;
            NSString *message = [info objectForKey:@"Message"] ? : error.description;
            if ([[info objectForKey:@"Code"] isEqualToString:VODOpenApiTokenExpired]) {
                state = VODStatusPaused;
                callback.expire();
            }else if([[info objectForKey:@"Code"] isEqualToString:VODOpenApiVideoNotFound]){
                // 处理刷新videoid上传，服务端没有找到对应videoid的场景，需要把缓存删掉，创建新的videoid
                [DefaultCache clearObjectWithPath:cur.filePath];
                [self createUploadVideo];
            }else {
                callback.failure(cur, code, message);
                cur.state = VODUploadFileStatusFailure;
            }
        }else {
//            NSString *_curRequestId = [responseObject objectForKey:@"RequestId"];
            BOOL shouldResume = NO;
            if (_openApiConfig) { // 续传到vod
                shouldResume = YES;
            }
            NSString *_curAddress = [responseObject objectForKey:@"UploadAddress"];
            NSString *_curAuth = [responseObject objectForKey:@"UploadAuth"];
            _curVideoId = [responseObject objectForKey:@"VideoId"] ? [responseObject objectForKey:@"VideoId"] : _curVideoId;
            
            
            VODObject* vodObj = [DefaultCache getObjectWithPath:cur.filePath];
            if(vodObj.videoId.length == 0) {
                vodObj.videoId = _curVideoId;
                [DefaultCache saveObject:vodObj path:cur.filePath];
            }
            
            _openApiConfig = [OSSConfig new];
            [self parseUploadAddress:_curAddress uploadInfo:cur];
            [self parseUploadAuth:_curAuth config:_openApiConfig];
            client = [[OSSClient alloc] initWithEndpoint:cur.endpoint credentialProvider:[self getCredentialProvider] clientConfiguration:conf];
            if (client) {
                OSSLogError(@"oss client init success!");
            } else {
                OSSLogDebug(@"oss client init failed.");
            }
            if (shouldResume) {
                [self resumableUpload];
            }else {
                [self initMultiUpload];
            }
        }
    }];
}

- (void)createUploadImage {
    NSMutableDictionary *params = [NSMutableDictionary dictionary];
    [params setValue:cur.filePath.pathExtension.lowercaseString ? : @"" forKey:@"ImageExt"];
    if(cur.vodInfo.title) {
        [params setValue:cur.vodInfo.title forKey:@"Title"];
    }
    if (cur.vodInfo.desc) {
        [params setValue:cur.vodInfo.desc forKey:@"Description"];
    }
    if (cur.vodInfo.cateId) {
        [params setValue:cur.vodInfo.cateId forKey:@"CateId"];
    }
    if (cur.vodInfo.tags) {
        [params setValue:cur.vodInfo.tags forKey:@"Tags"];
    }
    if (cur.vodInfo.storageLocation) {
        [params setValue:cur.vodInfo.storageLocation forKey:@"StorageLocation"];
    }
    if (_appId.length) {
        [params setValue:_appId forKey:@"AppId"];
    }
    if (cur.vodInfo.userData) {
        [params setValue:cur.vodInfo.userData forKey:@"UserData"];
    }
    
    [VODOpenApi createUploadImageWithKeyId:ossConfig.accessKeyId keySecret:ossConfig.accessKeySecret token:ossConfig.secretToken imageType:@"cover" region:_region optionalParams:params completionHandler:^(NSURLResponse *response, id  _Nullable responseObject, NSError * _Nullable error) {
        if (error) {
            // sts 过期处理
            NSDictionary *info = [error userInfo];
            NSString *code = [info objectForKey:@"Code"] ? : @(error.code).stringValue;
            NSString *message = [info objectForKey:@"Message"] ? : error.description;
            if ([[info objectForKey:@"Code"] isEqualToString:VODOpenApiTokenExpired]) {
                state = VODStatusPaused;
                callback.expire();
            }else {
                callback.failure(cur, code, message);
                cur.state = VODUploadFileStatusFailure;
            }
        }else {
            BOOL shouldResume = NO;
            if (_openApiConfig) { // 续传到vod
                shouldResume = YES;
            }
//            NSString *_curRequestId = [responseObject objectForKey:@"RequestId"];
            NSString *_curAddress = [responseObject objectForKey:@"UploadAddress"];
            NSString *_curAuth = [responseObject objectForKey:@"UploadAuth"];
            _curImageURL = [responseObject objectForKey:@"ImageURL"];
            _openApiConfig = [OSSConfig new];
            [self parseUploadAddress:_curAddress uploadInfo:cur];
            [self parseUploadAuth:_curAuth config:_openApiConfig];
            client = [[OSSClient alloc] initWithEndpoint:cur.endpoint credentialProvider:[self getCredentialProvider] clientConfiguration:conf];
            if (client) {
                OSSLogError(@"oss client init success!");
            } else {
                OSSLogDebug(@"oss client init failed.");
            }
            if (shouldResume) {
                [self resumableUpload];
            }else {
                [self initMultiUpload];
            }
        }
    }];
}

#pragma mark - event

- (void)sendStartEvent {

    NSURL *fileURL = [NSURL fileURLWithPath:cur.filePath];
    AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:fileURL options:nil];
    CGSize size = [AVCVAssetInfo AVCVNaturalSize:asset];
    
    NSString *fileType = _curInnerFileInfo.fileType;
}

#pragma mark - util


- (NSString *)userDataWithVideoPath:(NSString *)videoPath userData:(NSString *)userData {
    NSMutableDictionary *mutableDict =[NSMutableDictionary dictionary];
    if (userData) {
        NSError* error = NULL;
        NSDictionary *userDict = [NSJSONSerialization JSONObjectWithData:[userData dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingMutableLeaves error:&error];
        if (!error) {
            [mutableDict addEntriesFromDictionary:userDict];
        }
    }
    
    AVURLAsset *asset = [AVURLAsset assetWithURL:[NSURL fileURLWithPath:videoPath]];
    CGFloat duration = [AVCVAssetInfo AVCVDuration:asset];
    CGFloat bitrate = [AVCVAssetInfo AVCVBitrate:asset] / 1024;
    CGFloat fps = [AVCVAssetInfo AVCVFrameRate:asset];
    CGSize size = [AVCVAssetInfo AVCVNaturalSize:asset];
    
    NSDictionary *dict = @{
                           @"Duration":[NSString stringWithFormat:@"%.2f", duration],
                           @"Bitrate":[NSString stringWithFormat:@"%.2f", bitrate],
                           @"Fps":[NSString stringWithFormat:@"%.2f", fps],
                           @"Width":@(size.width).stringValue,
                           @"Height":@(size.height).stringValue
                           };
    [mutableDict addEntriesFromDictionary:dict];
    NSError *error = nil;
    NSData *dataInfo = [NSJSONSerialization dataWithJSONObject:mutableDict
                                                       options:0
                                                         error:&error];
    if (error) {
        return nil;
    }
    NSString * strInfo = [[NSString alloc] initWithData:dataInfo encoding:NSUTF8StringEncoding];
    return strInfo;
}

- (void)setRequestId:(NSString *)requestId {
    
}

-(NSString *)requestId {
    return @"";
}

-(NSURLSessionDataTask* )createOrRefreshUploadVideoWithKeyId:(NSString * _Nonnull)keyId
                        keySecret:(NSString * _Nonnull)keySecret
                            token:(NSString * _Nonnull)token
                            title:(NSString * _Nonnull)title
                         fileName:(NSString * _Nonnull)fileName
                   optionalParams:(NSDictionary * _Nullable)optionalParams
                         completionHandler:(VODCompletionHandler _Nonnull)completionHandler {
    VODObject* vodObj = [DefaultCache getObjectWithPath:fileName];
    if (vodObj && vodObj.videoId.length) {
        _curVideoId = vodObj.videoId;
       return [VODOpenApi refreshUploadVideoWithKeyId:keyId keySecret:keySecret token:token videoId:vodObj.videoId  region:_region completionHandler:completionHandler];
    }else {
       return [VODOpenApi createUploadVideoWithKeyId:keyId keySecret:keySecret token:token title:title fileName:fileName region:_region optionalParams:optionalParams completionHandler:completionHandler];
    }
}

@end
