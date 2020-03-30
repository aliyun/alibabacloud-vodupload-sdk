//
//  VODUploadSimpleClient.m
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import "VODUploadSVideoClient.h"
#import "VODUploadClient.h"
#import "VODOpenApi.h"
#import "AVCVAssetInfo.h"
#import "VODObjectCache.h"
#import <AliyunOSSiOS/OSSService.h>

typedef NS_ENUM(NSInteger, VODSVideoStatus) {
    VODSVideoStatusIdle,
    VODSVideoStatusResumed,
    VODSVideoStatusPaused,
};

typedef NS_ENUM(NSInteger, VODSVideoStep) {
    VODSVideoStepIdle,
    VODSVideoStepCreateImage,
    VODSVideoStepCreateImageFinish,
    VODSVideoStepUploadImage,
    VODSVideoStepUploadImageFinish,
    VODSVideoStepCreateVideo,
    VODSVideoStepCreateVideoFinish,
    VODSVideoStepUploadVideo,
};
// 定义内部使用的VODUploadClient方法
@interface VODUploadClient()
- (void)setLogStore:(NSString *)logStore;
- (void)setVideoId:(NSString *)videoId;
@end

@interface VODUploadSVideoClient()

@property (nonatomic, strong) VODUploadClient *client;
@property (nonatomic, assign) VODSVideoStatus status;
@property (nonatomic, assign) VODSVideoStep step;

// 用户传入参数
@property (nonatomic, copy) NSString *accessKeyId;
@property (nonatomic, copy) NSString *accessKeySecret;
@property (nonatomic, copy) NSString *accessToken;
@property (nonatomic, copy) NSString *expiredTime;

@property (nonatomic, copy) NSString *videoPath;
@property (nonatomic, copy) NSString *imagePath;

@property (nonatomic, strong) VodSVideoInfo *svideoInfo;

// open api 返回参数
@property (nonatomic, copy) NSString *curAddress;
@property (nonatomic, copy) NSString *curAuth;
@property (nonatomic, copy) NSString *curRequestId;
@property (nonatomic, copy) NSString *curImageURL;
@property (nonatomic, copy) NSString *curVideoId;


@property (nonatomic, strong) NSURLSessionDataTask *curTask;

// 解析参数
@property (nonatomic, assign) long long videoFileSize;
@property (nonatomic, assign) long long imageFileSize;

@property (nonatomic, strong) NSLock *lock;

@end

@implementation VODUploadSVideoClient

@synthesize status = _status;
@synthesize step = _step;

-(instancetype)init {
    self = [super init];
    if (self) {
        [self setup];
    }
    return self;
}

- (void)setup {
    __weak VODUploadSVideoClient *weakSelf = self;
    
    OnUploadFinishedListener testFinishCallbackFunc = ^(UploadFileInfo* fileInfo, VodUploadResult *result){
        if (weakSelf.step == VODSVideoStepUploadImage) {    // 上传图片成功
            OSSLogDebug(@"svideo client OnUploadFinishedListener image");
            weakSelf.step = VODSVideoStepUploadImageFinish;
            if (weakSelf.status != VODSVideoStatusPaused) {
                [weakSelf createUploadVideo];
            }
        }else if (weakSelf.step  == VODSVideoStepUploadVideo) {     // 上传视频成功
            OSSLogDebug(@"svideo client OnUploadFinishedListener video");
            weakSelf.step = VODSVideoStepIdle;
            weakSelf.status = VODSVideoStatusIdle;
            if ([weakSelf.delegate respondsToSelector:@selector(uploadSuccessWithVid:imageUrl:)]) {
                [weakSelf.delegate uploadSuccessWithVid:weakSelf.curVideoId imageUrl:weakSelf.curImageURL];
            }
            if ([weakSelf.delegate respondsToSelector:@selector(uploadSuccessWithResult:)]) {
                VodSVideoUploadResult *svideoResult = [VodSVideoUploadResult new];
                svideoResult.videoId = weakSelf.curVideoId;
                svideoResult.imageUrl = weakSelf.curImageURL;
                svideoResult.bucket = result.bucket;
                svideoResult.endpoint = result.endpoint;
                [weakSelf.delegate uploadSuccessWithResult:svideoResult];
            }
        }
        
    };
    OnUploadFailedListener testFailedCallbackFunc = ^(UploadFileInfo* fileInfo, NSString *code, NSString* message){
        weakSelf.step = VODSVideoStepIdle;
        weakSelf.status = VODSVideoStatusIdle;
        [weakSelf.delegate uploadFailedWithCode:code message:message];
        OSSLogDebug(@"svideo client OnUploadFailedListener code:%@ desc:%@", code, message);
    };
    OnUploadProgressListener testProgressCallbackFunc = ^(UploadFileInfo* fileInfo, long uploadedSize, long totalSize) {
        if (weakSelf.step == VODSVideoStepUploadImage) {
            [weakSelf.delegate uploadProgressWithUploadedSize:uploadedSize
                                            totalSize:(weakSelf.videoFileSize + weakSelf.imageFileSize)];
        }else if (weakSelf.step  == VODSVideoStepUploadVideo) {
            [weakSelf.delegate uploadProgressWithUploadedSize:(uploadedSize+weakSelf.imageFileSize)
                                            totalSize:(weakSelf.videoFileSize + weakSelf.imageFileSize)];
        }
    };
    OnUploadTokenExpiredListener testTokenExpiredCallbackFunc = ^{
        [weakSelf.delegate uploadTokenExpired];
        OSSLogDebug(@"svideo client OnUploadTokenExpiredListener");
    };
    OnUploadRertyListener testRetryCallbackFunc = ^{
        [weakSelf.delegate uploadRetry];
        OSSLogDebug(@"svideo client OnUploadRertyListener");
    };
    OnUploadRertyResumeListener testRetryResumeCallbackFunc = ^{
        [weakSelf.delegate uploadRetryResume];
        OSSLogDebug(@"svideo client OnUploadRertyResumeListener");
    };
    OnUploadStartedListener testUploadStartedCallbackFunc = ^(UploadFileInfo* fileInfo) {
        [weakSelf.client setVideoId:weakSelf.curVideoId];
        [weakSelf.client setUploadAuthAndAddress:fileInfo uploadAuth:weakSelf.curAuth uploadAddress:weakSelf.curAddress];
        OSSLogDebug(@"svideo client OnUploadStartedListener");
    };
    VODUploadListener *listener = [[VODUploadListener alloc] init];
    listener.finish = testFinishCallbackFunc;
    listener.failure = testFailedCallbackFunc;
    listener.progress = testProgressCallbackFunc;
    listener.expire = testTokenExpiredCallbackFunc;
    listener.retry = testRetryCallbackFunc;
    listener.retryResume = testRetryResumeCallbackFunc;
    listener.started = testUploadStartedCallbackFunc;
    _client = [[VODUploadClient alloc] init];
    [_client init:listener];
    [_client setLogStore:@"svideo"];
    _lock = [NSLock new];
    _maxRetryCount = INT_MAX;
    _timeoutIntervalForRequest = 30;
    _transcode = YES;
    _region = @"cn-shanghai";
    _recordUploadProgress = YES;
}

#pragma mark - getter setter

-(void)setStep:(VODSVideoStep)step {
    [_lock lock];
    _step = step;
    [_lock unlock];
}

-(VODSVideoStep)step {
    [_lock lock];
    VODSVideoStep step = _step;
    [_lock unlock];
    return step;
}

-(void)setStatus:(VODSVideoStatus)status {
    [_lock lock];
    _status = status;
    [_lock unlock];
}

-(VODSVideoStatus)status {
    [_lock lock];
    VODSVideoStatus status = _status;
    [_lock unlock];
    return status;
}

-(void)setMaxRetryCount:(uint32_t)maxRetryCount {
    _maxRetryCount = maxRetryCount;
    _client.maxRetryCount = maxRetryCount;
}

-(void)setTimeoutIntervalForRequest:(NSTimeInterval)timeoutIntervalForRequest {
    _timeoutIntervalForRequest = timeoutIntervalForRequest;
    _client.timeoutIntervalForRequest = timeoutIntervalForRequest;
}

-(void)setUploadPartSize:(NSInteger)uploadPartSize {
    _uploadPartSize = uploadPartSize;
    _client.uploadPartSize = uploadPartSize;
}

-(void)setRecordDirectoryPath:(NSString *)recordDirectoryPath {
    _recordDirectoryPath = recordDirectoryPath;
    _client.recordDirectoryPath = recordDirectoryPath;
}

-(void)setRecordUploadProgress:(BOOL)recordUploadProgress {
    _recordUploadProgress = recordUploadProgress;
    _client.recordUploadProgress = recordUploadProgress;
}

-(void)setTranscode:(BOOL)transcode {
    _transcode = transcode;
    _client.transcode = transcode;
}

-(void)setRegion:(NSString *)region {
    _region = region;
    _client.region = region;
}

#pragma mark - public methods

- (BOOL)uploadWithVideoPath:(NSString *)videoPath
                  imagePath:(NSString *)imagePath
                 svideoInfo:(VodSVideoInfo *)svideoInfo
                accessKeyId:(NSString *)accessKeyId
            accessKeySecret:(NSString *)accessKeySecret
                accessToken:(NSString *)accessToken {
    if (self.status != VODSVideoStatusIdle || self.step != VODSVideoStepIdle) {
        NSLog(@"error: client is uploading, call this method when upload task is finished.");
        return NO;
    }
    if (!svideoInfo.title || svideoInfo.title.length == 0) {
        NSLog(@"error: title in VodSVideoInfo can not be NULL");
        return NO;
    }
    _videoFileSize = [self fileSizeWithPath:videoPath];
    _imageFileSize = [self fileSizeWithPath:imagePath];
    _videoPath = videoPath;
    _imagePath = imagePath;
    _svideoInfo = svideoInfo;
    _accessKeyId = accessKeyId;
    _accessKeySecret = accessKeySecret;
    _accessToken = accessToken;
    self.status = VODSVideoStatusResumed;
    [self createUploadImage];
    return YES;
}

- (void)pause {
    if (self.step == VODSVideoStepUploadImage || self.step == VODSVideoStepUploadVideo) {
        [_client pause];
    }
    self.status = VODSVideoStatusPaused;
}

- (void)resume {
    if (self.status != VODSVideoStatusPaused) {
        return;
    }
    self.status = VODSVideoStatusResumed;
    if (self.step == VODSVideoStepIdle) {
        [self createUploadImage];
    }else if (self.step == VODSVideoStepUploadImageFinish) {
        [self createUploadVideo];
    }else if (self.step == VODSVideoStepUploadImage || self.step == VODSVideoStepUploadVideo) {
        [_client resumeWithAuth:_curAuth];
    }
}

- (void)refreshWithAccessKeyId:(NSString *)accessKeyId
              accessKeySecret:(NSString *)accessKeySecret
                  accessToken:(NSString *)accessToken
                   expireTime:(NSString *)expireTime {
    self.status = VODSVideoStatusResumed;
    _accessKeyId = accessKeyId;
    _accessKeySecret = accessKeySecret;
    _accessToken = accessToken;
    _expiredTime = expireTime;
    if (self.step == VODSVideoStepIdle) {
        [self createUploadImage];
    }else if(self.step == VODSVideoStepUploadImageFinish) {
        [self createUploadVideo];
    }else {
        [self refreshUploadVideo];
    }
}

- (void)cancel {
//    [_client stop];
    [_client clearFiles];
    [_curTask cancel];
    self.status = VODSVideoStatusIdle;
    self.step = VODSVideoStepIdle;
}

#pragma mark - steps
// 调用创建图片上传OpenApi
- (void)createUploadImage {
    if (!_imagePath) {
        NSLog(@"imagePath must not be nil!");
        return;
    }
    OSSLogDebug(@"svideo client createUploadImage");
    NSMutableDictionary *params = [NSMutableDictionary dictionary];
    [params setValue:_imagePath.pathExtension.lowercaseString ? : @"" forKey:@"ImageExt"];
    if (_svideoInfo.storageLocation) {
        [params setValue:_svideoInfo.storageLocation forKey:@"StorageLocation"];
    }
    if (_appId.length) {
        [params setValue:_appId forKey:@"AppId"];
    }
    if (_svideoInfo.userData) {
        [params setValue:_svideoInfo.userData forKey:@"UserData"];
    }
    
    self.step = VODSVideoStepCreateImage;
    _curTask = [VODOpenApi createUploadImageWithKeyId:_accessKeyId keySecret:_accessKeySecret token:_accessToken imageType:@"cover" region:_region optionalParams:params completionHandler:^(NSURLResponse *response, id  _Nullable responseObject, NSError * _Nullable error) {
        self.step = VODSVideoStepCreateImageFinish;
        if (error) {
            // sts 过期处理
            self.step = VODSVideoStepIdle;
            self.status = VODSVideoStatusIdle;
            NSDictionary *info = [error userInfo];
            NSString *code = [info objectForKey:@"Code"] ? : @(error.code).stringValue;
            NSString *message = [info objectForKey:@"Message"] ? : error.description;
            OSSLogDebug(@"svideo client createUploadImage failed code:%@ desc:%@", code, message);
            if ([[info objectForKey:@"Code"] isEqualToString:VODOpenApiTokenExpired]) {
                [_delegate uploadTokenExpired];
            }else {
                [_delegate uploadFailedWithCode:code message:message];
            }
        }else {
            OSSLogDebug(@"svideo client createUploadImage success");
            _curRequestId = [responseObject objectForKey:@"RequestId"];
            _curAddress = [responseObject objectForKey:@"UploadAddress"];
            _curAuth = [responseObject objectForKey:@"UploadAuth"];
            _curImageURL = [responseObject objectForKey:@"ImageURL"];
            if (self.status != VODSVideoStatusPaused) {
                [self uploadImage];
            }
        }
    }];
}

// 上传图片
- (void)uploadImage {
    OSSLogDebug(@"svideo client uploadImage");
    self.step = VODSVideoStepUploadImage;
    VodInfo *info = [VodInfo new];
    info.title = _imagePath.lastPathComponent;
    info.userData = _svideoInfo.userData;
    [_client clearFiles];
    [_client addFile:_imagePath vodInfo:info];
    [_client start];
}

// 调用创建视频上传OpenApi
- (void)createUploadVideo {
    self.step = VODSVideoStepCreateVideo;
    if (!_svideoInfo.title) {
        NSLog(@"title must not be nil!");
        return;
    }
    OSSLogDebug(@"svideo client createUploadVideo");
    NSMutableDictionary *params = [NSMutableDictionary dictionary];
    [params setValue:_curImageURL forKey:@"CoverURL"];
    [params setValue:@(_videoFileSize).stringValue forKey:@"FileSize"];
    if (_svideoInfo.desc) {
        [params setValue:_svideoInfo.desc forKey:@"Description"];
    }
    if (_svideoInfo.cateId) {
        [params setValue:_svideoInfo.cateId forKey:@"CateId"];
    }
    if (_svideoInfo.tags) {
        [params setValue:_svideoInfo.tags forKey:@"Tags"];
    }
    if (_svideoInfo.storageLocation) {
        [params setValue:_svideoInfo.storageLocation forKey:@"StorageLocation"];
    }
    if (_svideoInfo.templateGroupId) {
        [params setValue:_svideoInfo.templateGroupId forKey:@"TemplateGroupId"];
    }

    if (!_transcode && !_svideoInfo.templateGroupId.length) {
        [params setValue:@"NoTranscode" forKey:@"TranscodeMode"];   
    }
    if (_appId.length) {
        [params setValue:_appId forKey:@"AppId"];
    }
    if (_workflowId.length) {
        [params setValue:_workflowId forKey:@"WorkflowId"];
    }
    
    NSString *userData = [self userDataWithCreateUploadVideoUserData:_svideoInfo.userData];
    if (userData) {
        [params setValue:userData forKey:@"UserData"];
    }

//    NSString *fileName = _videoPath.lastPathComponent;
   _curTask = [self createOrRefreshUploadVideoWithKeyId:_accessKeyId keySecret:_accessKeySecret token:_accessToken title:_svideoInfo.title fileName:_videoPath optionalParams:params completionHandler:^(NSURLResponse *response, id  _Nullable responseObject, NSError * _Nullable error) {
        self.step = VODSVideoStepCreateVideoFinish;
        if (error) {
            // sts 过期处理
            self.step = VODSVideoStepIdle;
            self.status = VODSVideoStatusIdle;
            NSDictionary *info = [error userInfo];
            NSString *code = [info objectForKey:@"Code"] ? : @(error.code).stringValue;
            NSString *message = [info objectForKey:@"Message"] ? : error.description;
            OSSLogDebug(@"svideo client createOrRefreshUploadVideo failed code:%@ desc:%@", code, message);
            if ([[info objectForKey:@"Code"] isEqualToString:VODOpenApiTokenExpired]) {
                self.step = VODSVideoStepUploadImageFinish;
                [_delegate uploadTokenExpired];
            }else if([[info objectForKey:@"Code"] isEqualToString:VODOpenApiVideoNotFound]){
                // 处理刷新videoid上传，服务端没有找到对应videoid的场景，需要把缓存删掉，创建新的videoid
                [DefaultCache clearObjectWithPath:_videoPath];
                [self createUploadVideo];
            }else {
                [_delegate uploadFailedWithCode:code message:message];
            }
        }else {
            _curRequestId = [responseObject objectForKey:@"RequestId"];
            _curAddress = [responseObject objectForKey:@"UploadAddress"];
            _curAuth = [responseObject objectForKey:@"UploadAuth"];
            _curVideoId =  [responseObject objectForKey:@"VideoId"] ? [responseObject objectForKey:@"VideoId"]: _curVideoId;
            OSSLogDebug(@"svideo client createOrRefreshUploadVideo success videoid:%@", _curVideoId);
            VODObject* vodObj = [DefaultCache getObjectWithPath:_videoPath];
            if(vodObj.videoId.length == 0) {
                vodObj.videoId = _curVideoId;
                [DefaultCache saveObject:vodObj path:_videoPath];
            }
            
            if (self.status != VODSVideoStatusPaused) {
                [self uploadVideo];
            }
        }
    }];
}

// 上传视频
- (void)uploadVideo {
    OSSLogDebug(@"svideo client uploadVideo");
    self.step = VODSVideoStepUploadVideo;
    VodInfo *info = [VodInfo new];
    info.title = _svideoInfo.title;
    info.coverUrl = _curImageURL;
    info.tags = _svideoInfo.tags;
    info.desc = _svideoInfo.desc;
    info.cateId = _svideoInfo.cateId;
    info.isProcess = _svideoInfo.isProcess;
    info.isShowWaterMark = _svideoInfo.isShowWaterMark;
    info.priority = _svideoInfo.priority;
    info.userData = [self userDataWithCreateUploadVideoUserData:_svideoInfo.userData];
    [_client clearFiles];
    [_client addFile:_videoPath vodInfo:info];
    [_client start];
}

// 调用刷新视频上传OpenApi
- (void)refreshUploadVideo {
    self.step = VODSVideoStepUploadVideo;
    _curTask = [VODOpenApi refreshUploadVideoWithKeyId:_accessKeyId keySecret:_accessKeySecret token:_accessToken videoId:_curVideoId region:_region completionHandler:^(NSURLResponse *response, id  _Nullable responseObject, NSError * _Nullable error) {
        self.step = VODSVideoStepUploadVideo;
        if (error) {
            // sts 过期处理
            self.step = VODSVideoStepIdle;
            self.status = VODSVideoStatusIdle;
            NSDictionary *info = [error userInfo];
            NSString *code = [info objectForKey:@"Code"] ? : @(error.code).stringValue;
            NSString *message = [info objectForKey:@"Message"] ? : error.description;
            OSSLogDebug(@"svideo client refreshUploadVideo failed code:%@ desc:%@", code, message);
            if ([[info objectForKey:@"Code"] isEqualToString:VODOpenApiTokenExpired]) {
                self.step = VODSVideoStepUploadVideo;
                [_delegate uploadTokenExpired];
            }else {
                [_delegate uploadFailedWithCode:code message:message];
            }
        }else {
            _curRequestId = [responseObject objectForKey:@"RequestId"];
            _curAddress = [responseObject objectForKey:@"UploadAddress"];
            _curAuth = [responseObject objectForKey:@"UploadAuth"];
            _curVideoId =  [responseObject objectForKey:@"VideoId"] ? [responseObject objectForKey:@"VideoId"] : _curVideoId;
            OSSLogDebug(@"svideo client refreshUploadVideo success videoid:%@", _curVideoId);
            if (self.status != VODSVideoStatusPaused) {
                [_client resumeWithAuth:_curAuth];
            }
        }
    }];
}

#pragma mark - util

- (long long)fileSizeWithPath:(NSString *)filePath {
    NSDictionary *fileAttributes = [[NSFileManager defaultManager] attributesOfItemAtPath:filePath error:nil];
    NSNumber *fileSizeNumber = [fileAttributes objectForKey:NSFileSize];
    long long fileSize = [fileSizeNumber longLongValue];
    return fileSize;
}

- (NSString *)userDataWithCreateUploadVideoUserData:(NSString *)userData {
    NSMutableDictionary *mutableDict =[NSMutableDictionary dictionary];
    if (userData) {
        NSError* error = NULL;
        NSDictionary *userDict = [NSJSONSerialization JSONObjectWithData:[userData dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingMutableLeaves error:&error];
        if (!error) {
            [mutableDict addEntriesFromDictionary:userDict];
        }
    }
    
    AVURLAsset *asset = [AVURLAsset assetWithURL:[NSURL fileURLWithPath:_videoPath]];
    CGFloat duration = [AVCVAssetInfo AVCVDuration:asset];
    CGFloat bitrate = [AVCVAssetInfo AVCVBitrate:asset] / 1024;
    CGFloat fps = [AVCVAssetInfo AVCVFrameRate:asset];
    CGSize size = [AVCVAssetInfo AVCVNaturalSize:asset];
    
    NSDictionary *dict = @{
                           @"Duration":[NSString stringWithFormat:@"%.2f", duration],
                           @"Bitrate":[NSString stringWithFormat:@"%.2f", bitrate],
                           @"Fps":[NSString stringWithFormat:@"%.2f", fps],
                           @"Width":@(size.width).stringValue,
                           @"Height":@(size.height).stringValue,
                           @"Source":@"short_video"
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
    if ([_client respondsToSelector:@selector(setRequestId:)]) {
        [_client performSelector:@selector(setRequestId:) withObject:requestId];
    }
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
        OSSLogDebug(@"svideo client refresh video id");
        return [VODOpenApi refreshUploadVideoWithKeyId:keyId keySecret:keySecret token:token videoId:vodObj.videoId region:_region completionHandler:completionHandler];
    }else {
        OSSLogDebug(@"svideo client create video id");
        return [VODOpenApi createUploadVideoWithKeyId:keyId keySecret:keySecret token:token title:title fileName:fileName region:_region optionalParams:optionalParams completionHandler:completionHandler];
    }
}

@end
