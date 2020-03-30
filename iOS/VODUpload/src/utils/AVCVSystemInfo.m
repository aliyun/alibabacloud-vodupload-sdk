//
//  AVCVSystemInfo.m
//  AliyunVideoCore
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import "AVCVSystemInfo.h"
#import <UIKit/UIKit.h>
#import <CoreTelephony/CTTelephonyNetworkInfo.h>
#import <sys/utsname.h>
#include <sys/mman.h>
#include <sys/stat.h>

@implementation AVCVSystemInfo

+ (NSString *)deviceUUID {
    return [[[UIDevice currentDevice] identifierForVendor] UUIDString];
}

+ (NSString*)deviceModel {
    struct utsname systemInfo;
    uname(&systemInfo);
    NSString *deviceString = [NSString stringWithCString:systemInfo.machine encoding:NSUTF8StringEncoding];
    return deviceString;
}

+ (NSString *)systemVersion {
    return [[UIDevice currentDevice] systemVersion];
}

+ (NSString*)terminalType {
    NSString* phoneModel = [[UIDevice currentDevice] model];
    NSRange range = [phoneModel rangeOfString:@"iPhone"];
    if (range.length > 0) {
        return @"phone";
    }
    range = [phoneModel rangeOfString:@"iPad"];
    if (range.length > 0) {
        return @"pad";
    }
    return @"phone";
}

+ (NSString *)bundleId {
    return [NSBundle mainBundle].bundleIdentifier;
}

+ (NSString *)appDisplayName {
    NSString *name = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleDisplayName"];
    if (!name) {
        name = [[[NSBundle mainBundle] infoDictionary] objectForKey:(NSString *)kCFBundleNameKey];
    }
    if (!name) {
        return @"";
    }
    return name;
}

@end
