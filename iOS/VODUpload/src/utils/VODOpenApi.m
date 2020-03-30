//
//  VODOpenApi.m
//  VODUpload
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import "VODOpenApi.h"
#import <CommonCrypto/CommonHMAC.h>
#import <CommonCrypto/CommonCryptor.h>



static NSString *const VOD_DOMAIN = @"https://vod.%@.aliyuncs.com/";
static NSString *const VOD_HTTP_METHOD = @"GET";
static NSString *const VOD_HMAC_SHA1_ALGORITHM = @"HMAC-SHA1";
static NSString *const VOD_API_VERSION = @"2017-03-21";
static NSString *const VOD_API_FORMAT = @"JSON";

NSString *const VODOpenApiTokenExpired = @"InvalidSecurityToken.Expired";
NSString *const VODOpenApiVideoNotFound = @"InvalidVideo.NotFound";

@implementation VODOpenApi

#pragma mark - api

+ (NSURLSessionDataTask *)createUploadVideoWithKeyId:(NSString *)keyId
                                           keySecret:(NSString *)keySecret
                                               token:(NSString *)token
                                               title:(NSString *)title
                                            fileName:(NSString *)fileName
                                              region:(NSString * _Nonnull)region
                                      optionalParams:(NSDictionary *)optionalParams
                                   completionHandler:(VODCompletionHandler)completionHandler {
    NSDictionary *params = @{
                             @"Action":@"CreateUploadVideo",
                             @"Title":title,
                             @"FileName":fileName
                             };
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    [dict addEntriesFromDictionary:params];
    if (optionalParams) {
        [dict addEntriesFromDictionary:optionalParams];
    }
   return [self requestWithKeyId:keyId keySecret:keySecret token:token region:region params:dict completionHandler:completionHandler];
}


+ (NSURLSessionDataTask *)refreshUploadVideoWithKeyId:(NSString *)keyId
                                            keySecret:(NSString *)keySecret
                                                token:(NSString *)token
                                              videoId:(NSString *)videoId
                                               region:(NSString * _Nonnull)region
                                    completionHandler:(VODCompletionHandler)completionHandler {
    NSDictionary *params = @{
                             @"Action":@"RefreshUploadVideo",
                             @"VideoId":videoId
                             };
   return [self requestWithKeyId:keyId keySecret:keySecret token:token region:region params:params completionHandler:completionHandler];
}

+ (NSURLSessionDataTask *)createUploadImageWithKeyId:(NSString *)keyId
                                           keySecret:(NSString *)keySecret
                                               token:(NSString *)token
                                           imageType:(NSString *)imageType
                                              region:(NSString * _Nonnull)region
                                      optionalParams:(NSDictionary *)optionalParams
                                   completionHandler:(VODCompletionHandler)completionHandler {
    NSDictionary *params = @{
                             @"Action":@"CreateUploadImage",
                             @"ImageType":imageType,
                             };
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    [dict addEntriesFromDictionary:params];
    if (optionalParams) {
        [dict addEntriesFromDictionary:optionalParams];
    }
    return [self requestWithKeyId:keyId keySecret:keySecret token:token region:region params:dict completionHandler:completionHandler];
}

#pragma mark - request

+ (NSURLSessionDataTask *)requestWithKeyId:(NSString *)keyId
                                 keySecret:(NSString *)keySecret
                                     token:(NSString *)token
                                    region:(NSString *)region
                                    params:(NSDictionary *)params
                         completionHandler:(VODCompletionHandler)completionHandler
{
    NSDictionary *publicParams = @{
                                     @"Format": VOD_API_FORMAT,
                                     @"Version":VOD_API_VERSION,
                                     @"AccessKeyId":keyId,
                                     @"SignatureMethod":VOD_HMAC_SHA1_ALGORITHM,
//                                     @"Timestamp":[self timeStamp],
                                     @"SignatureVersion":@"1.0",
                                     @"SignatureNonce":[self randomUUID],
                                     @"SecurityToken":token
                                 };
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    [dict addEntriesFromDictionary:publicParams];
    [dict addEntriesFromDictionary:params];
    NSString *cqsString = [self getParamsString:dict];
    NSString *stringToSign = [NSString stringWithFormat:@"%@&%@&%@", VOD_HTTP_METHOD,[self percentEncode:@"/"],[self percentEncode:cqsString]];
    NSString *signature = [self hmacSha1:[NSString stringWithFormat:@"%@&", keySecret] data:stringToSign];
    NSString *vodDomain = [NSString stringWithFormat:VOD_DOMAIN, region];
    NSString *urlString = [NSString stringWithFormat:@"%@?%@&%@=%@", vodDomain,cqsString,[self percentEncode:@"Signature"],[self percentEncode:signature]];
    NSURLSessionConfiguration *sessionConfiguration = [NSURLSessionConfiguration defaultSessionConfiguration];
    NSURLSession *session = [NSURLSession sessionWithConfiguration:sessionConfiguration];
    NSURL *url = [NSURL URLWithString:urlString];
    NSMutableURLRequest *urlRequest = [NSMutableURLRequest requestWithURL:url];
    NSURLSessionDataTask *task = [session dataTaskWithRequest:urlRequest completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        if (error) {
            if (completionHandler) {
                completionHandler(response, nil, error);
            }
            return ;
        }
        
        if (data == nil) {
            NSError *emptyError = [[NSError alloc] initWithDomain:@"Empty Data" code:-10000 userInfo:nil];
            if (completionHandler) {
                completionHandler(response, nil, emptyError);
            }
            return ;
        }
        
        id jsonObj = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingAllowFragments error:&error];
        if (error) {
            if (completionHandler) {
                completionHandler(response, jsonObj, error);
            }
            return;
        }
        
        if ([response isKindOfClass:[NSHTTPURLResponse class]]) {
            NSInteger statusCode = [(NSHTTPURLResponse *)response statusCode];
            if (statusCode-200 >= 100) {
                error = [NSError errorWithDomain:@"vod" code:statusCode userInfo:jsonObj];
                if (completionHandler) {
                    completionHandler(response, jsonObj, error);
                    return ;
                }
            }
        }
        
        if (completionHandler) {
            completionHandler(response, jsonObj, error);
        }
    }];
    [task resume];
    return task;
}

+ (NSString *)getParamsString:(NSDictionary *)params {
    NSMutableArray *parts = [NSMutableArray array];
    for (id key in params.allKeys) {
        id value = [params objectForKey:key];
        NSString *part = [NSString stringWithFormat:@"%@=%@", [self percentEncode:key], [self percentEncode:value]];
        [parts addObject: part];
    }
    
    NSArray<NSString *> *sortedArray = [parts sortedArrayUsingSelector:@selector(localizedCaseInsensitiveCompare:)];
    NSString *string = [sortedArray componentsJoinedByString:@"&"];
    return string;
}

#pragma mark - util

+ (NSString *)percentEncode:(id)object {
    NSString *string = [NSString stringWithFormat:@"%@", object];
    
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

+ (NSString *)timeStamp {
    NSISO8601DateFormatter *dateFormatter = [NSISO8601DateFormatter new];
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithName:@"GMT"]];
    return [dateFormatter stringFromDate:[NSDate date]];
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

@end
