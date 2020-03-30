//
//  VODObjectCache.m
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import "VODObjectCache.h"

static NSString* const VODObjectCacheKey = @"ALIYUN_VOD_OBJECT_CACHE2";
static NSInteger VODObjectCacheMaxSize = 50;


@implementation VODObject
-(NSDictionary *)toDict {
    return @{
             @"key":_key?_key:@"",
             @"object":_object?_object:@"",
             @"videoId":_videoId?_videoId:@""
             };
}

-(instancetype)initWithDict:(NSDictionary *)dict {
    self = [super init];
    if (self) {
        _key = [dict valueForKey:@"key"]?[dict valueForKey:@"key"]:@"";
        _videoId = [dict valueForKey:@"videoId"]?[dict valueForKey:@"videoId"]:@"";
        _object = [dict valueForKey:@"object"]?[dict valueForKey:@"object"]:@"";
    }
    return self;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        _key = @"";
        _videoId = @"";
        _object = @"";
    }
    return self;
}

@end


@interface VODObjectCache()
@property (nonatomic, strong) NSMutableArray *array;
@property (nonatomic, strong) NSLock *lock;
@end

@implementation VODObjectCache

+(instancetype)sharedCache {
    static dispatch_once_t onceToken;
    static VODObjectCache *cache;
    dispatch_once(&onceToken, ^{
        cache = [VODObjectCache new];
    });
    return cache;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        [self load];
        _lock = [NSLock new];
        _enabled = YES;
    }
    return self;
}



//-(NSString *)cachedObjectWithPath:(NSString *)path object:(NSString *)object {
//    if (!_enabled) {
//        return object;
//    }
//    NSString *cached = [self getObjectWithPath:path];
//    if (cached) {
//        return cached;
//    }
//    [self saveObject:object path:path];
//    return object;
//}

-(void)saveObject:(VODObject *)object path:(NSString *)path {
    if (!_enabled) {
        return;
    }
    if (!object || !path.length) {
        return;
    }
    NSString *key = [self uniqueIdWithFilePath:path];
    object.key = key;
    [_lock lock];
    if (![_array containsObject:object]) {
        [_array addObject:object];
    }
    
    while (_array.count > VODObjectCacheMaxSize) {
        [_array removeObjectAtIndex:0];
    }
    [self save];
    [_lock unlock];
}

-(VODObject *)getObjectWithPath:(NSString *)path {
    if (!_enabled) {
        return nil;
    }
    if (!path.length) {
        return nil;
    }
    NSString *key = [self uniqueIdWithFilePath:path];
    [_lock lock];
    for (VODObject *obj in _array) {
        if ([key isEqualToString:obj.key]) {
            [_lock unlock];
            return obj;
        }
    }
    [_lock unlock];
    VODObject *obj = [VODObject new];
    obj.key = key;
    return obj;
}

-(void)clearObjectWithPath:(NSString *)path {
    if (!path.length) {
        return;
    }
    NSString *key = [self uniqueIdWithFilePath:path];
    
    [_lock lock];
    for (VODObject *obj in _array) {
        if ([key isEqualToString:obj.key]) {
            [_array removeObject:obj];
            break;
        }
    }
    [self save];
    [_lock unlock];
}

- (NSString *)uniqueIdWithFilePath:(NSString *)path {
    NSDictionary *fileAttributes = [[NSFileManager defaultManager] attributesOfItemAtPath:path error:nil];
    NSNumber *fileSizeNumber = [fileAttributes objectForKey:NSFileSize];
    long long fileSize = [fileSizeNumber longLongValue];
    NSString *data = [fileAttributes objectForKey:NSFileCreationDate] ? : @"";
    NSString *string = [NSString stringWithFormat:@"%lld%@%@", fileSize, path.lastPathComponent, data];
    NSData *plainData = [string dataUsingEncoding:NSUTF8StringEncoding];
    NSString *base64String = [plainData base64EncodedStringWithOptions:kNilOptions];
    return base64String;
}

//- (void)load {
//    NSArray *arr = [[NSUserDefaults standardUserDefaults] valueForKey:VODObjectCacheKey];
//    _array = [NSMutableArray arrayWithArray:arr];
//}

- (void)save {
    NSMutableArray *arr = [NSMutableArray array];
    for (VODObject *obj in _array) {
        [arr addObject:[obj toDict]];
    }
    [[NSUserDefaults standardUserDefaults] setValue:arr forKey:VODObjectCacheKey];
}

- (void)load {
    NSArray *arr = [[NSUserDefaults standardUserDefaults] valueForKey:VODObjectCacheKey];
    _array = [NSMutableArray array];
    for (NSDictionary *dict in arr) {
        [_array addObject:[[VODObject alloc] initWithDict:dict]];
    }
}

@end
