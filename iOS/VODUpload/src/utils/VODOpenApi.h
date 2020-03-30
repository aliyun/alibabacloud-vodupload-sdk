//
//  VODOpenApi.h
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import <Foundation/Foundation.h>

typedef void (^VODCompletionHandler)(NSURLResponse *response, id _Nullable responseObject,  NSError * _Nullable error);

extern NSString * _Nonnull const VODOpenApiTokenExpired;
extern NSString * _Nonnull const VODOpenApiVideoNotFound;
@interface VODOpenApi : NSObject

+ (NSURLSessionDataTask *)createUploadVideoWithKeyId:(NSString * _Nonnull)keyId
                                           keySecret:(NSString * _Nonnull)keySecret
                                               token:(NSString * _Nonnull)token
                                               title:(NSString * _Nonnull)title
                                            fileName:(NSString * _Nonnull)fileName
                                              region:(NSString * _Nonnull)region
                                      optionalParams:(NSDictionary * _Nullable)optionalParams
                                   completionHandler:(VODCompletionHandler _Nonnull)completionHandler;

+ (NSURLSessionDataTask *)refreshUploadVideoWithKeyId:(NSString * _Nonnull)keyId
                                            keySecret:(NSString * _Nonnull)keySecret
                                                token:(NSString * _Nonnull)token
                                              videoId:(NSString * _Nonnull)videoId
                                               region:(NSString * _Nonnull)region
                                    completionHandler:(VODCompletionHandler _Nonnull)completionHandler;

+ (NSURLSessionDataTask *)createUploadImageWithKeyId:(NSString * _Nonnull)keyId
                                           keySecret:(NSString * _Nonnull)keySecret
                                               token:(NSString * _Nonnull)token
                                           imageType:(NSString * _Nonnull)imageType
                                              region:(NSString * _Nonnull)region
                                      optionalParams:(NSDictionary * _Nullable)optionalParams
                                   completionHandler:(VODCompletionHandler _Nonnull)completionHandler;
@end
