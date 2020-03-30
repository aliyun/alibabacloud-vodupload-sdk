//
//  AVAssetInfo.h
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import <Foundation/Foundation.h>
#include <AVFoundation/AVFoundation.h>

@interface AVCVAssetInfo : NSObject

+ (CGSize)AVCVNaturalSize:(AVAsset*)asset;

+ (CGFloat)AVCVFrameRate:(AVAsset*)asset;

+ (CGFloat)AVCVBitrate:(AVAsset*)asset;

+ (CGFloat)AVCVDuration:(AVAsset*)asset;

+ (CGFloat)AVCVVideoDuration:(AVAsset*)asset;

+ (CGFloat)AVCVAudioDuration:(AVAsset*)asset;
@end
