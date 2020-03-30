//
//  VODUploadClient.h
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import <Foundation/Foundation.h>
#import "VODUploadModel.h"

@interface VODUploadClient : NSObject


/**
 transcode default value is YES
 */
@property (nonatomic, assign) BOOL transcode;

/**
 Max retry count, default value is INT_MAX
 Client will retry automatically in every 2 seconds when network is unavailable
 */
@property (nonatomic, assign) uint32_t maxRetryCount;

/**
 Sets single object download's max time
 */
@property (nonatomic, assign) NSTimeInterval timeoutIntervalForRequest;

/**
 directory path about create record uploadId file
 */
@property (nonatomic, copy) NSString * recordDirectoryPath;

/**
 record upload progress, default value if YES
 */
@property (nonatomic, assign) BOOL recordUploadProgress;

/**
 size of upload part, default value is 1024 * 1024
*/
@property (nonatomic, assign) NSInteger uploadPartSize;

/**
 requestId
 */
@property (nonatomic, assign) NSString *requestId;

/**
 vod region, defalut value is "cn-shanghai"
 */
@property (nonatomic, copy) NSString *region;

/**
 App id
 */
@property (nonatomic, copy) NSString* appId;

/**
 workflow id
 */
@property (nonatomic, copy) NSString* workflowId;

/**
 上传地址和凭证方式初始化
 @deprecated 使用`setListener:`方法
 */
- (BOOL)        init:(VODUploadListener *) listener __attribute__((deprecated("This method is conflict with swift initialization method, use `setListener:` instead.")));

/**
 上传地址和凭证方式初始化
 
 */
- (BOOL)        setListener:(VODUploadListener *) listener;

/**
 STS授权方式初始化
 @deprecated 使用`setKeyId: accessKeySecret: secretToken: expireTime: listener:`方法
 */
- (BOOL)        init:(NSString *)accessKeyId
     accessKeySecret:(NSString *)accessKeySecret
         secretToken:(NSString *)secretToken
          expireTime:(NSString *)expireTime
            listener:(VODUploadListener *) listener  __attribute__((deprecated("This method is conflict with swift initialization method, use `setKeyId: accessKeySecret: secretToken: expireTime: listener:` instead.")));

/**
 STS授权方式初始化
 
 */
- (BOOL)    setKeyId:(NSString *)accessKeyId
     accessKeySecret:(NSString *)accessKeySecret
         secretToken:(NSString *)secretToken
          expireTime:(NSString *)expireTime
            listener:(VODUploadListener *) listener;

/**
 AK方式初始化
 端上使用AK方式不安全，不建议使用
 */
- (BOOL)        init:(NSString *)accessKeyId
     accessKeySecret:(NSString *)accessKeySecret
            listener:(VODUploadListener *) listener __attribute__((deprecated("", "Not recommended.")));

/**
 AK方式初始化
 端上使用AK方式不安全，不建议使用
 */
- (BOOL)    setKeyId:(NSString *)accessKeyId
     accessKeySecret:(NSString *)accessKeySecret
            listener:(VODUploadListener *) listener __attribute__((deprecated("", "Not recommended.")));

/**
 添加视频上传
 */
- (BOOL)addFile:(NSString *)filePath
        vodInfo:(VodInfo *)vodInfo;

/**
 添加视频上传
 */
- (BOOL)addFile:(NSString *)filePath
       endpoint:(NSString *)endpoint
         bucket:(NSString *)bucket
         object:(NSString *)object;

/**
 添加视频上传
 */
- (BOOL)addFile:(NSString *)filePath
       endpoint:(NSString *)endpoint
         bucket:(NSString *)bucket
         object:(NSString *)object
        vodInfo:(VodInfo *)vodInfo;

/**
 删除文件
 */
- (BOOL)deleteFile:(int) index;

/**
 清除上传列表
 */
- (BOOL)clearFiles;


/**
 获取上传文件列表
 */
- (NSMutableArray<UploadFileInfo *> *)listFiles;

/**
 取消单个文件上传，文件保留在上传列表中
 */
- (BOOL)cancelFile:(int)index;

/**
 恢复已取消的上传文件
 */
- (BOOL)resumeFile:(int)index;

/**
 开始上传
 */
- (BOOL)start;

/**
 停止上传
 */
- (BOOL)stop;

/**
 暂停上传
 */
- (BOOL)pause;

/**
 恢复上传
 */
- (BOOL)resume;

/**
 使用上传凭证恢复上传
 */
- (BOOL)resumeWithAuth:(NSString *)uploadAuth;

/**
 使用STS恢复上传
 */
- (BOOL)resumeWithToken:(NSString *)accessKeyId
        accessKeySecret:(NSString *)accessKeySecret
            secretToken:(NSString *)secretToken
             expireTime:(NSString *)expireTime;

/**
 设置上传地址和凭证
 */
- (BOOL)setUploadAuthAndAddress:(UploadFileInfo *)uploadFileInfo
           uploadAuth:(NSString *)uploadAuth
        uploadAddress:(NSString *)uploadAddress;

@end

