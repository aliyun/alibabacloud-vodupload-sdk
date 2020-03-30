//
//  VODUploadModel.h
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, VODUploadFileStatus) {
    VODUploadFileStatusReady,
    VODUploadFileStatusUploading,
    VODUploadFileStatusCanceled,
    VODUploadFileStatusPaused,
    VODUploadFileStatusSuccess,
    VODUploadFileStatusFailure
};

typedef NS_ENUM(NSInteger, VODStatus) {
    VODStatusReady,
    VODStatusStarted,
    VODStatusPaused,
    VODStatusCancel,
    VODStatusStoped,
    VODStatusSuccess,
    VODStatusFailure,
    VODStatusExpire
};

@interface OSSConfig : NSObject

@property (nonatomic, strong) NSString* accessKeyId;
@property (nonatomic, strong) NSString* accessKeySecret;
@property (nonatomic, strong) NSString* secretToken;
@property (nonatomic, strong) NSString* expireTime;

@end


@interface VodInfo : NSObject

/**
 标题
 */
@property (nonatomic, copy) NSString* title;

/**
 标签
 */
@property (nonatomic, copy) NSString* tags;

/**
 描述
 */
@property (nonatomic, copy) NSString* desc;

/**
 分类id
 */
@property (nonatomic, strong) NSNumber* cateId;

/**
 封面url
 */
@property (nonatomic, copy) NSString* coverUrl;

/**
 设置自定义数据
 */
@property (nonatomic, copy) NSString* userData;

/**
 isProcess
 */
@property (nonatomic, assign) BOOL isProcess;

/**
 是否显示水印
 */
@property (nonatomic, assign) BOOL isShowWaterMark;

/**
 优先级
 */
@property (nonatomic, strong) NSNumber* priority;

/**
 设置存储区域
 */
@property (nonatomic, copy) NSString* storageLocation;

/**
 设置转码模板id
 */
@property (nonatomic, copy) NSString* templateGroupId;

/**
 获取json字符串
 */
- (NSString*)toJson;


@end


@interface UploadFileInfo : NSObject

@property (nonatomic, copy) NSString* filePath;
@property (nonatomic, copy) NSString* endpoint;
@property (nonatomic, copy) NSString* bucket;
@property (nonatomic, copy) NSString* object;
@property (nonatomic, strong) VodInfo* vodInfo;
@property VODUploadFileStatus state;

@end


@interface VodUploadResult: NSObject
@property (nonatomic, copy) NSString* videoId;
@property (nonatomic, copy) NSString* imageUrl;
@property (nonatomic, copy) NSString* bucket;
@property (nonatomic, copy) NSString* endpoint;
@end

typedef void (^OnUploadSucceedListener) (UploadFileInfo* fileInfo);

/**
 上传完成回调

 @param fileInfo 上传文件信息
 @param result 上传结果信息
 */
typedef void (^OnUploadFinishedListener) (UploadFileInfo* fileInfo, VodUploadResult* result);

/**
 上传失败回调

 @param fileInfo 上传文件信息
 @param code 错误码
 @param message 错误描述
 */
typedef void (^OnUploadFailedListener) (UploadFileInfo* fileInfo, NSString *code, NSString * message);

/**
 上传进度回调

 @param fileInfo 上传文件信息
 @param uploadedSize 已上传大小
 @param totalSize 总大小
 */
typedef void (^OnUploadProgressListener) (UploadFileInfo* fileInfo, long uploadedSize, long totalSize);

/**
 token过期回调
 上传地址和凭证方式上传需要调用resumeWithAuth:方法继续上传
 STS方式上传需要调用resumeWithToken:accessKeySecret:secretToken:expireTime:方法继续上传
 */
typedef void (^OnUploadTokenExpiredListener) ();

/**
 上传开始重试回调
 */
typedef void (^OnUploadRertyListener) ();

/**
 上传结束重试，继续上传回调
 */
typedef void (^OnUploadRertyResumeListener) ();

/**
 开始上传回调
 上传地址和凭证方式上传需要调用setUploadAuthAndAddress:uploadAuth:uploadAddress:方法设置上传地址和凭证
 @param fileInfo 上传文件信息
 */
typedef void (^OnUploadStartedListener) (UploadFileInfo* fileInfo);

@interface VODUploadListener : NSObject
@property (nonatomic, copy) OnUploadSucceedListener success
__attribute__((deprecated("", "use OnUploadFinishedListener to replace")));

@property (nonatomic, copy) OnUploadFinishedListener finish;
@property (nonatomic, copy) OnUploadFailedListener failure;
@property (nonatomic, copy) OnUploadProgressListener progress;
@property (nonatomic, copy) OnUploadTokenExpiredListener expire;
@property (nonatomic, copy) OnUploadRertyListener retry;
@property (nonatomic, copy) OnUploadRertyResumeListener retryResume;
@property (nonatomic, copy) OnUploadStartedListener started;

@end


@interface VODUploadModel : NSObject

@end
