//
//  MainViewController.m
//  VODUploadDemo
//
//  Copyright (C) 2020 Alibaba Group Holding Limited.
//

#import "MainViewController.h"
#import <VODUpload/VODUploadClient.h>
#import "TestRequest.h"

@interface MainViewController ()<UITableViewDataSource, UITableViewDelegate>

@property(nonatomic, strong)NSMutableArray *percentList;
@property(nonatomic, strong)UITableView *tableFiles;
@property(nonatomic, strong)VODUploadClient *uploader;
@property(nonatomic, assign)NSInteger pos;

@end

@implementation MainViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self setupSubviews];
    
    self.percentList = [[NSMutableArray alloc] init];
    self.uploader = [VODUploadClient new];
    
    __weak UITableView *weakTable = self.tableFiles;
    __weak VODUploadClient *weakClient = self.uploader;
    __weak NSMutableArray *weakList = self.percentList;
    __weak MainViewController *weakSelf = self;
    
    // callback functions and listener
    OnUploadFinishedListener testFinishCallbackFunc = ^(UploadFileInfo* fileInfo,  VodUploadResult* result){
        NSLog(@"on upload finished videoid:%@, imageurl:%@", result.videoId, result.imageUrl);
        dispatch_async(dispatch_get_main_queue(), ^{
            NSIndexSet *indexSet=[[NSIndexSet alloc]initWithIndex:0];
            [weakTable reloadSections:indexSet withRowAnimation:UITableViewRowAnimationAutomatic];
        });
    };
    
    OnUploadFailedListener testFailedCallbackFunc = ^(UploadFileInfo* fileInfo, NSString *code, NSString* message){
        NSLog(@"failed code = %@, error message = %@", code, message);
        dispatch_async(dispatch_get_main_queue(), ^{
            NSIndexSet *indexSet=[[NSIndexSet alloc]initWithIndex:0];
            [weakTable reloadSections:indexSet withRowAnimation:UITableViewRowAnimationAutomatic];
        });
    };
    
    OnUploadProgressListener testProgressCallbackFunc = ^(UploadFileInfo* fileInfo, long uploadedSize, long totalSize) {
        NSLog(@"progress uploadedSize : %li, totalSize : %li", uploadedSize, totalSize);
        UploadFileInfo* info;
        int i = 0;
        for(; i<[[weakClient listFiles] count]; i++) {
            info = [[weakClient listFiles] objectAtIndex:i];
            if (info == fileInfo) {
                break;
            }
        }
        if (nil == info) {
            return;
        }
        
        [weakList setObject:[NSString stringWithFormat:@"%ld", uploadedSize*100/totalSize] atIndexedSubscript:i];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            NSIndexSet *indexSet=[[NSIndexSet alloc]initWithIndex:0];
            [weakTable reloadSections:indexSet withRowAnimation:UITableViewRowAnimationAutomatic];
        });
    };
    
    OnUploadTokenExpiredListener testTokenExpiredCallbackFunc = ^{
        NSLog(@"token expired.");
        dispatch_async(dispatch_get_main_queue(), ^{
            [TestRequest getVideoUploadAuthWithWithToken:nil title:@"testtitle" filePath:@"test/testFile.mp4" coverURL:nil desc:@"testDesc" tags:@"101" handler:^(NSString * _Nullable uploadAddress, NSString * _Nullable uploadAuth, NSString * _Nullable videoId, NSError * _Nullable error) {
                if (!error) {
                    [weakClient resumeWithAuth:uploadAuth];
                }
            }];
        });
    };
    
    OnUploadRertyListener testRetryCallbackFunc = ^{
        NSLog(@"manager: retry begin.");
    };
    
    OnUploadRertyResumeListener testRetryResumeCallbackFunc = ^{
        NSLog(@"manager: retry begin.");
    };
    
    OnUploadStartedListener testUploadStartedCallbackFunc = ^(UploadFileInfo* fileInfo) {
        NSLog(@"upload started .");
        // Warning:每次上传都应该是独立的uploadAuth和uploadAddress
        // Warning:demo为了演示方便，使用了固定的uploadAuth和uploadAddress
        [weakClient setUploadAuthAndAddress:fileInfo uploadAuth:weakSelf.uploadAuth uploadAddress:weakSelf.uploadAddress];
    };
    
    VODUploadListener *listener = [[VODUploadListener alloc] init];
    listener.finish = testFinishCallbackFunc;
    listener.failure = testFailedCallbackFunc;
    listener.progress = testProgressCallbackFunc;
    listener.expire = testTokenExpiredCallbackFunc;
    listener.retry = testRetryCallbackFunc;
    listener.retryResume = testRetryResumeCallbackFunc;
    listener.started = testUploadStartedCallbackFunc;
    // 点播上传。每次上传都是独立的鉴权，所以初始化时，不需要设置鉴权
    //    [self.uploader init];
    [self.uploader setListener:listener];
}

#pragma mark - action

- (void)addFile:(id)sender {
    NSLog(@"addFile clicked.");
    NSString *filePath = [[NSBundle mainBundle] pathForResource:@"1" ofType:@"mp4"];
    
    VodInfo *vodInfo = [[VodInfo alloc] init];
    vodInfo.title = [NSString stringWithFormat:@"IOS标题%ld", self.pos];
    vodInfo.desc = [NSString stringWithFormat:@"IOS描述%ld", self.pos];
    vodInfo.cateId = @(19);
    vodInfo.tags = [NSString stringWithFormat:@"IOS标签1%ld, IOS标签2%ld", self.pos, self.pos];
    [self.uploader addFile:filePath vodInfo:vodInfo];
    
    [self.percentList addObject:[NSString stringWithFormat:@"%d", 0]];
    NSIndexSet *indexSet=[[NSIndexSet alloc]initWithIndex:0];
    [self.tableFiles reloadSections:indexSet withRowAnimation:UITableViewRowAnimationAutomatic];
}

- (void)deleteFile:(id)sender {
    NSLog(@"deleteFile clicked.");
    NSMutableArray<UploadFileInfo *> *list = [self.uploader listFiles];
    if ([list count] <= 0) {
        return;
    }
    NSInteger index = [self.uploader listFiles].count-1;
    NSString *fileName = [list objectAtIndex:index].filePath;
    [self.uploader deleteFile:(int)index];
    NSLog(@"Delete file: %@", fileName);
    if (self.percentList.count > 0) {
        [self.percentList removeObjectAtIndex:self.percentList.count - 1];
    }
    
    NSIndexSet *indexSet=[[NSIndexSet alloc]initWithIndex:0];
    [self.tableFiles reloadSections:indexSet withRowAnimation:UITableViewRowAnimationAutomatic];
}

- (void)cancelFile:(id)sender {
    NSLog(@"cancelFile clicked.");
    NSMutableArray<UploadFileInfo *> *list = [self.uploader listFiles];
    if ([list count] <= 0) {
        return;
    }
    
    NSInteger index = [self.uploader listFiles].count-1;
    NSString *fileName = [list objectAtIndex:index].filePath;
    [self.uploader cancelFile:(int)index];
    NSLog(@"cancelFile file: %@", fileName);
    
    NSIndexSet *indexSet=[[NSIndexSet alloc]initWithIndex:0];
    [self.tableFiles reloadSections:indexSet withRowAnimation:UITableViewRowAnimationAutomatic];
}

- (void)resumeFile:(id)sender {
    NSLog(@"resumeFile clicked.");
    NSMutableArray<UploadFileInfo *> *list = [self.uploader listFiles];
    if ([list count] <= 0) {
        return;
    }
    
    NSInteger index = [self.uploader listFiles].count-1;
    NSString *fileName = [list objectAtIndex:index].filePath;
    [self.uploader resumeFile:(int)index];
    NSLog(@"resumeFile file: %@", fileName);
    NSIndexSet *indexSet=[[NSIndexSet alloc]initWithIndex:0];
    [self.tableFiles reloadSections:indexSet withRowAnimation:UITableViewRowAnimationAutomatic];
}

- (void)getList:(id)sender {
    NSLog(@"getList clicked.");
    NSMutableArray *list = [self.uploader listFiles];
    NSLog(@"%@",list);
}

- (void)clearList:(id)sender {
    NSLog(@"clearList clicked.");
    [self.uploader clearFiles];
    [self.percentList removeAllObjects];
    
    NSIndexSet *indexSet=[[NSIndexSet alloc]initWithIndex:0];
    [self.tableFiles reloadSections:indexSet withRowAnimation:UITableViewRowAnimationAutomatic];
}

- (void)startUpload:(id)sender {
    NSLog(@"startUpload clicked.");
    
    [self.uploader start];
    NSIndexSet *indexSet=[[NSIndexSet alloc]initWithIndex:0];
    [self.tableFiles reloadSections:indexSet withRowAnimation:UITableViewRowAnimationAutomatic];
}

- (void)stopUpload:(id)sender {
    NSLog(@"stopUpload clicked");
    [self.uploader stop];
    
    NSIndexSet *indexSet=[[NSIndexSet alloc]initWithIndex:0];
    [self.tableFiles reloadSections:indexSet withRowAnimation:UITableViewRowAnimationAutomatic];
}

- (void)pauseUpload:(id)sender {
    NSLog(@"pauseUpload clicked");
    [self.uploader pause];
    
    NSIndexSet *indexSet=[[NSIndexSet alloc]initWithIndex:0];
    [self.tableFiles reloadSections:indexSet withRowAnimation:UITableViewRowAnimationAutomatic];
}

- (void)resumeUpload:(id)sender {
    NSLog(@"resumeUpload clicked");
    [self.uploader resume];
    
    NSIndexSet *indexSet=[[NSIndexSet alloc]initWithIndex:0];
    [self.tableFiles reloadSections:indexSet withRowAnimation:UITableViewRowAnimationAutomatic];
}

#pragma mark - setup view

- (void)setupSubviews {
    
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(40, 90, 180, 44)];
    label.textColor = [UIColor blackColor];
    label.text = @"文件管理";
    [self.view addSubview:label];
    
    UIButton *addButton = [UIButton buttonWithType:UIButtonTypeSystem];
    addButton.frame = CGRectMake(40, 140, 44, 44);
    [addButton setTitle:@"添加" forState:UIControlStateNormal];
    [addButton addTarget:self action:@selector(addFile:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:addButton];
    
    UIButton *deleButton = [UIButton buttonWithType:UIButtonTypeSystem];
    deleButton.frame = CGRectMake(100, 140, 44, 44);
    [deleButton setTitle:@"删除" forState:UIControlStateNormal];
    [deleButton addTarget:self action:@selector(deleteFile:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:deleButton];
    
    UIButton *cancelButton = [UIButton buttonWithType:UIButtonTypeSystem];
    cancelButton.frame = CGRectMake(160, 140, 44, 44);
    [cancelButton setTitle:@"取消" forState:UIControlStateNormal];
    [cancelButton addTarget:self action:@selector(cancelFile:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:cancelButton];
    
    UIButton *resumeButton = [UIButton buttonWithType:UIButtonTypeSystem];
    resumeButton.frame = CGRectMake(220, 140, 44, 44);
    [resumeButton setTitle:@"恢复" forState:UIControlStateNormal];
    [resumeButton addTarget:self action:@selector(resumeFile:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:resumeButton];
    
    UILabel *listLabel = [[UILabel alloc] initWithFrame:CGRectMake(40, 190, 180, 44)];
    listLabel.textColor = [UIColor blackColor];
    listLabel.text = @"列表管理";
    [self.view addSubview:listLabel];
    
    UIButton *getButton = [UIButton buttonWithType:UIButtonTypeSystem];
    getButton.frame = CGRectMake(40, 240, 44, 44);
    [getButton setTitle:@"获取" forState:UIControlStateNormal];
    [getButton addTarget:self action:@selector(getList:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:getButton];
    
    UIButton *clearButton = [UIButton buttonWithType:UIButtonTypeSystem];
    clearButton.frame = CGRectMake(100, 240, 44, 44);
    [clearButton setTitle:@"清除" forState:UIControlStateNormal];
    [clearButton addTarget:self action:@selector(clearList:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:clearButton];
    
    UILabel *managerLabel = [[UILabel alloc] initWithFrame:CGRectMake(40, 290, 180, 44)];
    managerLabel.textColor = [UIColor blackColor];
    managerLabel.text = @"上传管理";
    [self.view addSubview:managerLabel];
    
    UIButton *startButton = [UIButton buttonWithType:UIButtonTypeSystem];
    startButton.frame = CGRectMake(40, 340, 44, 44);
    [startButton setTitle:@"开始" forState:UIControlStateNormal];
    [startButton addTarget:self action:@selector(startUpload:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:startButton];
    
    UIButton *stopButton = [UIButton buttonWithType:UIButtonTypeSystem];
    stopButton.frame = CGRectMake(100, 340, 44, 44);
    [stopButton setTitle:@"停止" forState:UIControlStateNormal];
    [stopButton addTarget:self action:@selector(stopUpload:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:stopButton];
    
    UIButton *pauseButton = [UIButton buttonWithType:UIButtonTypeSystem];
    pauseButton.frame = CGRectMake(160, 340, 44, 44);
    [pauseButton setTitle:@"暂停" forState:UIControlStateNormal];
    [pauseButton addTarget:self action:@selector(pauseUpload:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:pauseButton];
    
    UIButton *resetButton = [UIButton buttonWithType:UIButtonTypeSystem];
    resetButton.frame = CGRectMake(240, 340, 44, 44);
    [resetButton setTitle:@"恢复" forState:UIControlStateNormal];
    [resetButton addTarget:self action:@selector(resumeUpload:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:resetButton];
    
    self.tableFiles = [[UITableView alloc] initWithFrame:CGRectMake(0, 400, CGRectGetWidth(self.view.frame), 260)];
    self.tableFiles.delegate = self;
    self.tableFiles.dataSource = self;
    [self.view addSubview:self.tableFiles];
    self.view.backgroundColor = [UIColor whiteColor];
}

#pragma mark - tableview delegate

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSMutableArray<UploadFileInfo *> * list = [self.uploader listFiles];
    return [list count];
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSMutableArray<UploadFileInfo *> * list = [self.uploader listFiles];
    UploadFileInfo *info = [list objectAtIndex:(long)indexPath.row];
    NSString * cellID = [NSString stringWithFormat:@"cell_sec_%ld", (long)indexPath.section ];
    UITableViewCell * cell = [tableView dequeueReusableCellWithIdentifier:cellID];
    if (cell == nil) {
        cell = [[UITableViewCell alloc]initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:cellID];
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    }
    cell.detailTextLabel.font = [UIFont systemFontOfSize:10];
    cell.textLabel.text = [NSString stringWithFormat:@"%@ %@",[info.filePath substringFromIndex:info.filePath.length - 14], [self convertToString:info.state]];
    cell.detailTextLabel.text = [NSString stringWithFormat:@"%@%%", [self.percentList objectAtIndex:(long)indexPath.row]];
    
    return cell;
}

- (NSString*)convertToString:(VODUploadFileStatus)state {
    NSString *result = nil;
    switch(state) {
        case 0:
            result = @"Ready";
            break;
        case 1:
            result = @"Uploading";
            break;
        case 2:
            result = @"Canceled";
            break;
        case 3:
            result = @"Paused";
            break;
        case 4:
            result = @"Success";
            break;
        case 5:
            result = @"Failure";
            break;
        default:
            result = @"unknown";
    }
    return result;
}

@end
