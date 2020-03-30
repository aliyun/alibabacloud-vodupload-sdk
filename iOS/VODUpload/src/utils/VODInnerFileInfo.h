//
//  VODInnerApi.h
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN


@interface VODInnerFileInfo : NSObject


@property (nonatomic, copy) NSString* fileName;
@property (nonatomic, copy) NSString* fileSize;
@property (nonatomic, copy) NSString* fileCreateTime;
@property (nonatomic, copy) NSString* fileHash;

@property (nonatomic, copy) NSString* filePath;
@property (nonatomic, copy) NSString* fileType;

- (instancetype)initWithPath:(NSString *)path;

-(NSDictionary *)toDictionary;

- (NSString *)toUrlString;
@end


NS_ASSUME_NONNULL_END
