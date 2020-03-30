//
//  AVCVSystemInfo.h
//  AliyunVideoCore
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import <Foundation/Foundation.h>

@interface AVCVSystemInfo : NSObject

+ (NSString *)deviceUUID;

+ (NSString*)deviceModel;

+ (NSString *)systemVersion;

+ (NSString *)terminalType;

+ (NSString *)bundleId;

+ (NSString *)appDisplayName;

@end
