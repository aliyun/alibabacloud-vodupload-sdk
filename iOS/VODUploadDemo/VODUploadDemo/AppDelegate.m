//
//  AppDelegate.m
//  VODUploadDemo
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import "AppDelegate.h"
#import "UploadViewController.h"

@interface AppDelegate ()

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    
    self.window = [[UIWindow alloc]initWithFrame:[UIScreen mainScreen].bounds];
    self.window.backgroundColor = [UIColor whiteColor];
    UploadViewController *vc = [[UploadViewController alloc] init];
    UINavigationController *nvc = [[UINavigationController alloc]initWithRootViewController:vc];
    self.window.rootViewController = nvc;
    [self.window makeKeyAndVisible];
    
    return YES;
}

@end
