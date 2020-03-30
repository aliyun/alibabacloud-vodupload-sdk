//
//  VODUploadSVideoModel.m
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import "VODUploadSVideoModel.h"

@implementation VodSVideoInfo

- (instancetype)init
{
    self = [super init];
    if (self) {
        _isProcess = YES;
        _priority = @(6);
    }
    return self;
}

@end

@implementation VodSVideoUploadResult

@end
