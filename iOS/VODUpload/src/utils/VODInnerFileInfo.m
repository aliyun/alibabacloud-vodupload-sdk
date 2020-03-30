//
//  VODInnerApi.m
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import "VODInnerFileInfo.h"
#import <UIKit/UIKit.h>
#import <sys/utsname.h>
#include <sys/mman.h>
#include <sys/stat.h>
#import <CommonCrypto/CommonCrypto.h>
#import "AVCVHelper.h"
#import "AVCVSystemInfo.h"
#import <MobileCoreServices/MobileCoreServices.h>

extern NSString *const VODUploadSDKVersion;
static NSString *const VOD_INNER_DOMAIN = @"https://vod.%@.aliyuncs.com/";
static NSString *const VOD_INNER_SECRET_KEY = @"OX4GcA*6UNW39EBu";
static NSString *const VOD_INNER_HTTP_METHOD = @"POST";
static NSString *const VOD_INNER_HMAC_SHA1_ALGORITHM = @"HMAC-SHA1";
static NSString *const VOD_INNER_API_VERSION = @"2017-03-14";
static NSString *const VOD_INNER_SIGN_KEY = @"TODO";
static NSString *const VOD_ACCESS_KEY_ID = @"TODO";
static NSString *const VOD_UPLOAD_POINT = @"";

@implementation VODInnerFileInfo

- (instancetype)initWithPath:(NSString *)path {
    self = [super init];
    if (self) {
        _filePath = path;
        [self setup];
    }
    return self;
}


- (void)setup {
    NSDictionary *fileAttributes = [[NSFileManager defaultManager] attributesOfItemAtPath:_filePath error:nil];
    NSNumber *fileSizeNumber = [fileAttributes objectForKey:NSFileSize];
    long long fileSize = [fileSizeNumber longLongValue];
    _fileName = [_filePath lastPathComponent] ? : @"";
    _fileSize = [NSNumber numberWithLongLong:fileSize].stringValue;
    NSDate *date = [fileAttributes objectForKey:NSFileCreationDate];
    NSDateFormatter *format = [[NSDateFormatter alloc] init];
    [format setDateFormat:@"yyyy-MM-dd'T'HH:mm:sss'Z'"];
    _fileCreateTime = [format stringFromDate:date] ? : @"";
    _fileHash = [VODInnerFileInfo md5WithPath:_filePath maxHashSize:1024*1024] ? : @"";
    
    NSString *ext = _filePath.pathExtension;
    _fileType = [self fileType:ext];
}


+ (NSString *)md5WithPath:(NSString *)filePath
              maxHashSize:(size_t)maxHashSize {
    
    
    // Declare needed variables
    CFStringRef result = NULL;
    CFReadStreamRef readStream = NULL;
    
    // Get the file URL
    CFURLRef fileURL =
    CFURLCreateWithFileSystemPath(kCFAllocatorDefault,
                                  (__bridge CFStringRef)filePath,
                                  kCFURLPOSIXPathStyle,
                                  (Boolean)false);
    if (!fileURL) goto done;
    
    // Create and open the read stream
    readStream = CFReadStreamCreateWithFile(kCFAllocatorDefault,
                                            (CFURLRef)fileURL);
    if (!readStream) goto done;
    bool didSucceed = (bool)CFReadStreamOpen(readStream);
    if (!didSucceed) goto done;
    
    // Initialize the hash object
    CC_MD5_CTX hashObject;
    CC_MD5_Init(&hashObject);
    
    // Make sure chunkSizeForReadingData is valid
    size_t chunkSizeForReadingData = 256;
    
    // Feed the data to the hash object
    bool hasMoreData = true;
    size_t size = 0;
    while (hasMoreData) {
        if ( size >= maxHashSize) {
            hasMoreData = false;
            break;
        }
        uint8_t buffer[chunkSizeForReadingData];
        CFIndex readBytesCount = CFReadStreamRead(readStream,
                                                  (UInt8 *)buffer,
                                                  (CFIndex)sizeof(buffer));
        if (readBytesCount == -1) break;
        if (readBytesCount == 0) {
            hasMoreData = false;
            continue;
        }
        CC_MD5_Update(&hashObject,
                      (const void *)buffer,
                      (CC_LONG)readBytesCount);
        size += chunkSizeForReadingData;
    }
    
    // Check if the read operation succeeded
    didSucceed = !hasMoreData;
    
    // Compute the hash digest
    unsigned char digest[CC_MD5_DIGEST_LENGTH];
    CC_MD5_Final(digest, &hashObject);
    
    // Abort if the read operation failed
    if (!didSucceed) goto done;
    
    // Compute the string result
    char hash[2 * sizeof(digest) + 1];
    for (size_t i = 0; i < sizeof(digest); ++i) {
        snprintf(hash + (2 * i), 3, "%02x", (int)(digest[i]));
    }
    result = CFStringCreateWithCString(kCFAllocatorDefault,
                                       (const char *)hash,
                                       kCFStringEncodingUTF8);
    
done:
    
    if (readStream) {
        CFReadStreamClose(readStream);
        CFRelease(readStream);
    }
    if (fileURL) {
        CFRelease(fileURL);
    }
    return (__bridge NSString *)result;
}

- (NSString *)fileType:(NSString *)extension {
    if (!extension) {
        return @"other";
    }
    CFStringRef UTI = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension,(__bridge CFStringRef) extension, NULL);
    if (UTI == NULL) {
        return @"other";
    }
    NSString* MIMEType = (__bridge_transfer NSString*)UTTypeCopyPreferredTagWithClass
    (UTI, kUTTagClassMIMEType);
    if ([MIMEType containsString:@"video"]) {
        return @"video";
    }else if ([MIMEType containsString:@"audio"]) {
        return @"audio";
    }else if ([MIMEType containsString:@"image"]) {
        return @"img";
    }
    return @"other";
}

- (NSDictionary *)toDictionary {
    return @{
             @"FileName":_fileName,
             @"FileSize":_fileSize,
             @"FileCreateTime":_fileCreateTime,
             @"FileHash":_fileHash
             };
}

- (NSString *)toUrlString {
    return [NSString stringWithFormat:@"{\"FileHash\":\"%@\",\"FileName\":\"%@\",\"FileCreateTime\":\"%@\",\"FileSize\":\"%@\"}", _fileHash,_fileName,_fileCreateTime,_fileSize];
}

@end

