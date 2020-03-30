//
//  AVAssetInfo.m
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import "AVCVAssetInfo.h"

@implementation AVCVAssetInfo
+(CGSize)AVCVNaturalSize:(AVAsset *)asset {
    AVAssetTrack *assetTrackVideo;
    NSArray *videoTracks = [asset tracksWithMediaType:AVMediaTypeVideo];
    if (videoTracks.count) {
        assetTrackVideo = videoTracks[0];
    }
    float sw = assetTrackVideo.naturalSize.width, sh = assetTrackVideo.naturalSize.height;
    BOOL isAssetPortrait = NO;
    CGAffineTransform trackTrans = assetTrackVideo.preferredTransform;
    if ((trackTrans.b == 1.0 && trackTrans.c == -1.0) || (trackTrans.b == -1.0 && trackTrans.c == 1.0)) {
        isAssetPortrait = YES;
    }
    if (isAssetPortrait) {
        float t = sw;
        sw = sh;
        sh = t;
    }
    return CGSizeMake(sw, sh);
}

+(CGFloat)AVCVFrameRate:(AVAsset *)asset {
    AVAssetTrack *assetTrackVideo;
    NSArray *videoTracks = [asset tracksWithMediaType:AVMediaTypeVideo];
    if (videoTracks.count) {
        assetTrackVideo = videoTracks[0];
    }
    return assetTrackVideo.nominalFrameRate;
}

+(CGFloat)AVCVBitrate:(AVAsset *)asset {
    AVAssetTrack *assetTrackVideo;
    NSArray *videoTracks = [asset tracksWithMediaType:AVMediaTypeVideo];
    if (videoTracks.count) {
        assetTrackVideo = videoTracks[0];
        return [assetTrackVideo estimatedDataRate];
    }
    return 0;
}

+(CGFloat)AVCVDuration:(AVAsset *)asset {
    return CMTimeGetSeconds(asset.duration);
}

+(CGFloat)AVCVVideoDuration:(AVAsset *)asset {
    NSArray *videoTracks = [asset tracksWithMediaType:AVMediaTypeVideo];
    if (videoTracks.count) {
        AVAssetTrack *track = videoTracks[0];
        return CMTimeGetSeconds(CMTimeRangeGetEnd(track.timeRange));
    }
    return 0;
}

+(CGFloat)AVCVAudioDuration:(AVAsset *)asset {
    NSArray *audioTracks = [asset tracksWithMediaType:AVMediaTypeAudio];
    if (audioTracks.count) {
        AVAssetTrack *track = audioTracks[0];
        return CMTimeGetSeconds(CMTimeRangeGetEnd(track.timeRange));
    }
    return 0;
}
@end
