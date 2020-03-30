//
//  UploadViewController.m
//  VODUploadDemo
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import "UploadViewController.h"
#import "AddressViewController.h"

@interface UploadViewController ()

@end

@implementation UploadViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navigationItem.title = @"视频上传";
    
    [self configBaseUI];
}

- (void)configBaseUI{
    UIButton *vodButton = [UIButton buttonWithType:UIButtonTypeSystem];;
    vodButton.frame = CGRectMake(0, 100, 320, 50);
    [vodButton setTitle:@"VOD列表上传(点播凭证方式)" forState:UIControlStateNormal];
    [vodButton addTarget:self action:@selector(vodAuthAddressUpload:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview: vodButton];
}

-(void)vodAuthAddressUpload:(id)sender {
    AddressViewController *vc = [AddressViewController new];
    [self.navigationController pushViewController:vc animated:YES];
}

@end
