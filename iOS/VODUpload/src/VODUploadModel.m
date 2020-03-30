//
//  VODUploadModel.m
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import "VODUploadModel.h"


@implementation VODUploadListener

//@synthesize success;
//@synthesize finish;
//@synthesize failure;
//@synthesize progress;
//@synthesize expire;
//@synthesize retry;
//@synthesize retryResume;
//@synthesize started;

@end

@implementation OSSConfig

@synthesize accessKeyId;
@synthesize accessKeySecret;
@synthesize secretToken;
@synthesize expireTime;

@end

@implementation VodInfo

@synthesize title;
@synthesize desc;
@synthesize cateId;
@synthesize coverUrl;
@synthesize userData;
@synthesize tags;
@synthesize isProcess;
@synthesize isShowWaterMark;
@synthesize storageLocation;
@synthesize templateGroupId;
//@synthesize priority;


-(id)init {
    if (self = [super init])  {
        self.isProcess = YES;
        self.isShowWaterMark = NO;
        _priority = @(6);
    }
    return self;
}
-(void)setPriority:(NSNumber *)priority {
    if (priority.integerValue < 1 || priority.integerValue > 10) {
        NSAssert(false, @"the value of priority must be a integer value which between 1 and 10");
        return;
    }
    _priority = @(priority.integerValue);
}

- (NSString*)toJson {
    // {"Vod":{"Title":"this is title.","Description":"this is desc.","CateId":"19","Tags":"tag1,tag2","IsProcess":"true","UserData":"user defined info here"}}';
    NSError *error;
    NSDictionary *dictUserData = [NSDictionary dictionaryWithObjectsAndKeys:
                                  (isShowWaterMark?@"true":@"false"), @"IsShowWaterMark", _priority,@"Priority", nil];
    NSDictionary *dict1;
    if (nil == userData || [userData length] <= 0) {
        dict1 = [NSDictionary dictionaryWithObjectsAndKeys:
                 title, @"Title", desc,@"Description", cateId,@"CateId",
                 tags, @"Tags", dictUserData,@"UserData", coverUrl,@"CoverUrl",
                 (isProcess ? @"true" : @"false"),@"IsProcess",
                 nil];
    } else {
        dict1 = [NSDictionary dictionaryWithObjectsAndKeys:
                 title, @"Title", desc,@"Description", cateId,@"CateId",
                 tags, @"Tags", userData,@"UserData", coverUrl,@"CoverUrl",
                 (isProcess ? @"true" : @"false"),@"IsProcess",
                 nil];
    }
    NSDictionary *dict2 = @{@"Vod":dict1};
    NSData *dataInfo = [NSJSONSerialization dataWithJSONObject:dict2
                                                       options:0
                                                         error:&error];
    //    NSString * strInfo = [[NSString alloc] initWithData:dataInfo encoding:NSUTF8StringEncoding];
    NSString *base64Info = [dataInfo base64EncodedStringWithOptions:0];
    return base64Info;
}

@end


@implementation UploadFileInfo

@synthesize filePath;
@synthesize endpoint;
@synthesize bucket;
@synthesize object;
@synthesize state;
@synthesize vodInfo;

@end


@implementation VODUploadModel

@end

@implementation VodUploadResult

@end
