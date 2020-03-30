//
//  AddressViewController.m
//  VODUploadDemo
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import "AddressViewController.h"
#import "MainViewController.h"
#include "TestRequest.h"

@interface AddressViewController ()

@end

@implementation AddressViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navigationItem.title = @"凭证获取";
    
    [self configBaseUI];
}

- (void)configBaseUI{
    UIButton *fetch = [UIButton buttonWithType:UIButtonTypeSystem];
    fetch.frame = CGRectMake(0, 100, 320, 50);
    [fetch setTitle:@"获取点播凭证" forState:UIControlStateNormal];
    [fetch addTarget:self action:@selector(fetchClicked) forControlEvents:UIControlEventTouchUpInside];
    self.view.backgroundColor = [UIColor whiteColor];
    [self.view addSubview:fetch];
}

- (void)fetchClicked {
    [TestRequest getVideoUploadAuthWithWithToken:nil title:@"testtitle" filePath:@"test/testFile.mp4" coverURL:nil desc:@"testDesc" tags:@"101" handler:^(NSString * _Nullable uploadAddress, NSString * _Nullable uploadAuth, NSString * _Nullable videoId, NSError * _Nullable error) {
        if (error) {
            NSLog(@"request error");
            return ;
        }
        dispatch_async(dispatch_get_main_queue(), ^{
            MainViewController *vc = [MainViewController new];
            vc.uploadAuth = uploadAuth;
            vc.uploadAddress = uploadAddress;
            [self.navigationController pushViewController:vc animated:YES];
        });
    }];
}

@end
