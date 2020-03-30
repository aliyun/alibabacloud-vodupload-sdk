//
//  TestRequest.h
//  VODUploadDemo
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface TestRequest : NSObject

/**
 获取视频上传的凭证

 @param tokenString 用户token
 @param title 视频标题
 @param filePath 视频路径
 @param coverURL 封面图
 @param desc 描述
 @param tags tag-标签
 @param handler 回调
 */
+ (void)getVideoUploadAuthWithWithToken:(NSString *_Nullable)tokenString
                                  title:(NSString *)title
                               filePath:(NSString *)filePath
                               coverURL:(NSString * _Nullable)coverURL
                                   desc:(NSString *_Nullable)desc
                                   tags:(NSString * _Nullable)tags
                                handler:(void (^)(NSString *_Nullable uploadAddress, NSString *_Nullable uploadAuth, NSString *_Nullable videoId, NSError *_Nullable error))handler;

@end

NS_ASSUME_NONNULL_END
