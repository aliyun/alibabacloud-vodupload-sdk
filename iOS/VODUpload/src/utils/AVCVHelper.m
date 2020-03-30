//
//  AVCVHelper.m
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import "AVCVHelper.h"
#import <CommonCrypto/CommonHMAC.h>
#import <CommonCrypto/CommonCryptor.h>

@implementation AVCVHelper
+ (NSString*)timeStamp {
    UInt64 recordTime = [[NSDate date] timeIntervalSince1970];
    return [NSString stringWithFormat:@"%lld",recordTime];
}

+ (NSString *)timeStampFormatted {
    NSDateFormatter *format = [[NSDateFormatter alloc] init];
    [format setTimeZone:[NSTimeZone timeZoneWithName:@"GMT"]];
    [format setDateFormat:@"yyyy-MM-dd'T'HH:mm:sss'Z'"];
    return [format stringFromDate:[NSDate date]];
}

+ (NSString *)randomUUID {
    CFUUIDRef uuid_ref = CFUUIDCreate(NULL);
    CFStringRef uuid_string_ref= CFUUIDCreateString(NULL, uuid_ref);
    CFRelease(uuid_ref);
    NSString *uuid = [NSString stringWithString:(__bridge NSString*)uuid_string_ref];
    CFRelease(uuid_string_ref);
    return uuid;
}

+ (NSString *)hmacSha1:(NSString *)key data:(NSString *)data {
    const char *cKey  = [key cStringUsingEncoding:NSASCIIStringEncoding];
    const char *cData = [data cStringUsingEncoding:NSASCIIStringEncoding];
    unsigned char cHMAC[CC_SHA1_DIGEST_LENGTH];
    CCHmac(kCCHmacAlgSHA1, cKey, strlen(cKey), cData, strlen(cData), cHMAC);
    NSData *HMAC = [[NSData alloc] initWithBytes:cHMAC
                                          length:sizeof(cHMAC)];
    NSString *hash = [HMAC base64EncodedStringWithOptions:0];//将加密结果进行一次BASE64编码。
    return hash;
}

+ (NSString *)md5WithString:(NSString *)md5String {
    const char *str = [md5String UTF8String];
    unsigned char r[CC_MD5_DIGEST_LENGTH];
    CC_MD5(str, (CC_LONG)strlen(str), r);
    NSMutableString *hash = [NSMutableString string];
    for (int i = 0; i < CC_MD5_DIGEST_LENGTH; i++) {
        [hash appendFormat:@"%02x", r[i]];
    }
    return hash;
}

+(NSString *)urlEncodeStringFromDict:(NSDictionary *)dict {
    NSMutableArray *parts = [NSMutableArray array];
    for (id key in dict.allKeys) {
        id value = [dict objectForKey:key];
        NSString *part = [NSString stringWithFormat:@"%@=%@",
                          [self percentEncode:[NSString stringWithFormat:@"%@", key]],
                          [self percentEncode:[NSString stringWithFormat:@"%@", value]]];
        [parts addObject: part];
    }
    NSArray<NSString *> *sortedArray = [parts sortedArrayUsingSelector:@selector(localizedCaseInsensitiveCompare:)];
    NSString *string = [sortedArray componentsJoinedByString:@"&"];
    return string;
}

+ (NSString*)urlEncode:(NSString*)str {
    if (str == nil) {
        return @"";
    }
    //    return [str stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
    
    NSString* outputStr = (__bridge NSString *)CFURLCreateStringByAddingPercentEscapes(
                                                                                       NULL, /* allocator */
                                                                                       (__bridge CFStringRef)str,
                                                                                       NULL, /* charactersToLeaveUnescaped */
                                                                                       (CFStringRef)@"!*'();:@&=+$,/?%#[]",
                                                                                       kCFStringEncodingUTF8);
    
    NSString* resStr = [[NSString alloc] initWithString:outputStr];
    CFRelease((__bridge CFStringRef)outputStr);
    return resStr;
}

+ (NSString *)percentEncode:(id)object {
    NSString *string = [NSString stringWithFormat:@"%@", object];
    
//    return [string stringByAddingPercentEncodingWithAllowedCharacters:NSCharacterSet.URLHostAllowedCharacterSet];
    
//    NSString* outputStr = (__bridge NSString *)CFURLCreateStringByAddingPercentEscapes(
//                                                                                       NULL, /* allocator */
//                                                                                       (__bridge CFStringRef)string,
//                                                                                       NULL, /* charactersToLeaveUnescaped */
//                                                                                       (CFStringRef)@"!*'();:@&=+$,/?%#[]",
//                                                                                       kCFStringEncodingUTF8);
//
//    NSString* resStr = [[NSString alloc] initWithString:outputStr];
//    CFRelease((__bridge CFStringRef)outputStr);
//    return  resStr;
//
    
    
    NSString * const kAFCharactersGeneralDelimitersToEncode = @":#[]@?/"; // does not include "?" or "/" due to RFC 3986 - Section 3.4
    NSString * const kAFCharactersSubDelimitersToEncode = @"!$&'()*+,;=";
    NSMutableCharacterSet * allowedCharacterSet = [[NSCharacterSet URLQueryAllowedCharacterSet] mutableCopy];
    [allowedCharacterSet removeCharactersInString:[kAFCharactersGeneralDelimitersToEncode stringByAppendingString:kAFCharactersSubDelimitersToEncode]];
    
    
    NSString *percentstring = [string stringByAddingPercentEncodingWithAllowedCharacters:allowedCharacterSet];
    NSString * plusReplaced = [percentstring stringByReplacingOccurrencesOfString:@"+" withString:@"%20"];
    NSString * starReplaced = [plusReplaced stringByReplacingOccurrencesOfString:@"*" withString:@"%2A"];
    NSString * waveReplaced = [starReplaced stringByReplacingOccurrencesOfString:@"%7E" withString:@"~"];
    return waveReplaced;
}
@end
