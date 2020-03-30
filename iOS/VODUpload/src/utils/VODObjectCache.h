//
//  VODObjectCache.h
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import <Foundation/Foundation.h>

#define DefaultCache [VODObjectCache sharedCache]

@interface VODObject : NSObject
@property (nonatomic, copy) NSString* key;
@property (nonatomic, copy) NSString* object;
@property (nonatomic, copy) NSString* videoId;
-(instancetype)initWithDict:(NSDictionary *)dict;
-(NSDictionary *)toDict;
@end

@interface VODObjectCache : NSObject

+(instancetype)sharedCache;

@property (nonatomic, assign) BOOL enabled;

//-(NSString *)cachedObjectWithPath:(NSString *)path object:(NSString *)object;

-(VODObject *)getObjectWithPath:(NSString *)path;
-(void)saveObject:(VODObject *)object path:(NSString *)path;

-(void)clearObjectWithPath:(NSString *)path;
@end
