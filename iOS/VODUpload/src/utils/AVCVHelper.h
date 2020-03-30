//
//  AVCVHelper.h
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AVCVHelper : NSObject
+ (NSString*)timeStamp;
+ (NSString *)timeStampFormatted;
+ (NSString *)randomUUID;
+ (NSString *)hmacSha1:(NSString *)key data:(NSString *)data;
+ (NSString *)md5WithString:(NSString *)md5String;
+ (NSString *)urlEncodeStringFromDict:(NSDictionary *)dict;
+ (NSString *)percentEncode:(id)object;
+ (NSString *)urlEncode:(NSString *)str;
@end

NS_ASSUME_NONNULL_END
