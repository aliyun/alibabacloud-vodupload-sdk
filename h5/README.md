## 简介
阿里云视频点播上传SDK是阿里视频云端到云到端服务的重要一环，为您提供上传媒体文件到点播存储的开发工具包。集成点播上传SDK，就可以快捷上传包括视频、音频、图片、字幕等在内的各种媒体文件。同时提供服务端、Web端、移动端等多种版本SDK，全面适配各个主流平台和运行环境。
本文档主要介绍VOD Uoload SDK for h5的安装和使用。

## 开发环境
PC端和移动端Web浏览器
- IE> = 10＆Edge
- Chrome / Firefox / Safari的主要版本

## 工程结构
### 目录说明
- aliyun-upload-sdk-demo    h5上传展示demo
- src
  + constants   常量定义文件
  + service     工具包文件
  + upload      上传逻辑
  + config.js   版本配置
  + index.js    主入口文件
- 其余编译配置文件不多做介绍
     
### 编译
**clone工程**
```bash
 git clone https://github.com/aliyun/alibabacloud-vodupload-sdk.git 
```
**进入工程**
```bash
cd alibabacloud-vodupload-sdk/h5/UploadSDK 
```
**安装依赖**
```bash
cnpm install 
```
**执行该命令，编译生成`/build`文件夹，里面是未压缩的上传的js文件**
```bash
npm run dev   
```
**执行该命令，编译生成`/disk`文件夹，里面是压缩的上传的js文件**
```bash
npm run prod    
```

### 调试

编译成未压缩的js（方便查看），在页面上引入，在浏览器上运行页面，打开开发者工具，就可以进行调试。引入流程详见下面[项目集成]

## 项目集成
1. 在页面上引入下面三个JS脚本,见[https://help.aliyun.com/document_detail/51992.html?spm=a2c4g.11186623.2.41.21346bd11eABdv]
```bash
<!--  IE需要es6-promise -->
<script src="../lib/es6-promise.min.js"></script>
<script src="../lib/aliyun-oss-sdk5.2.0.min.js"></script>
<script src="../aliyun-vod-upload-sdk1.4.0.min.js"></script>
```
2. 初始化
```bash
 var uploader = new AliyunUpload.Vod({
    //阿里账号ID，必须有值 ，值的来源https://help.aliyun.com/knowledge_detail/37196.html
    userId:"122"
    //上传到点播的地域， 默认值为'cn-shanghai',//eu-central-1,ap-southeast-1
    region:"",
    //分片大小默认1M，不能小于100K
    partSize: 1048576,
    //并行上传分片个数，默认5
    parallel: 5,
    //网络原因失败时，重新上传次数，默认为3
    retryCount: 3,
    //网络原因失败时，重新上传间隔时间，默认为2秒
    retryDuration: 2,
    // 开始上传
    'onUploadstarted': function (uploadInfo) {
    }
    // 文件上传成功
    'onUploadSucceed': function (uploadInfo) {
    },
    // 文件上传失败
    'onUploadFailed': function (uploadInfo, code, message) {
    },
    // 文件上传进度，单位：字节
    'onUploadProgress': function (uploadInfo, totalSize, loadedPercent) {
    },
    // 上传凭证超时
    'onUploadTokenExpired': function (uploadInfo) {
    },
    //全部文件上传结束
    'onUploadEnd':function(uploadInfo){
    }
 });
```
3. 上传地址和凭证方式（推荐使用）
```bash
 onUploadstarted: function (uploadInfo) {
    // 如果是 UploadAuth 上传方式, 需要调用 uploader.setUploadAuthAndAddress 方法
    // 如果是 UploadAuth 上传方式, 需要根据 uploadInfo.videoId是否有值，调用点播的不同接口获取uploadauth和uploadAddress
    // 如果 uploadInfo.videoId 有值，调用刷新视频上传凭证接口，否则调用创建视频上传凭证接口
    // 注意: 这里是测试 demo 所以直接调用了获取 UploadAuth 的测试接口, 用户在使用时需要判断 uploadInfo.videoId 存在与否从而调用 openApi
    // 如果 uploadInfo.videoId 存在, 调用 刷新视频上传凭证接口(https://help.aliyun.com/document_detail/55408.html)
    // 如果 uploadInfo.videoId 不存在,调用 获取视频上传地址和凭证接口(https://help.aliyun.com/document_detail/55407.html)
    if (!uploadInfo.videoId) {
        let createUrl = 'https://demo-vod.cn-shanghai.aliyuncs.com/voddemo/CreateUploadVideo?Title=testvod1&FileName=aa.mp4&BusinessType=vodai&TerminalType=pc&DeviceModel=iPhone9,2&UUID=59ECA-4193-4695-94DD-7E1247288&AppVersion=1.0.0&VideoId=5bfcc7864fc14b96972842172207c9e6'
        axios.get(createUrl).then(({data}) => {
        let uploadAuth = data.UploadAuth
        let uploadAddress = data.UploadAddress
        let videoId = data.VideoId
        uploader.setUploadAuthAndAddress(uploadInfo, uploadAuth, uploadAddress,videoId)                
        })
        self.statusText = '文件开始上传...'
        console.log("onUploadStarted:" + uploadInfo.file.name + ", endpoint:" + uploadInfo.endpoint + ", bucket:" + uploadInfo.bucket + ", object:" + uploadInfo.object)
    } else {
        // 如果videoId有值，根据videoId刷新上传凭证
        // https://help.aliyun.com/document_detail/55408.html?spm=a2c4g.11186623.6.630.BoYYcY
        let refreshUrl = 'https://demo-vod.cn-shanghai.aliyuncs.com/voddemo/RefreshUploadVideo?BusinessType=vodai&TerminalType=pc&DeviceModel=iPhone9,2&UUID=59ECA-4193-4695-94DD-7E1247288&AppVersion=1.0.0&Title=haha1&FileName=xxx.mp4&VideoId=' + uploadInfo.videoId
        axios.get(refreshUrl).then(({data}) => {
        let uploadAuth = data.UploadAuth
        let uploadAddress = data.UploadAddress
        let videoId = data.VideoId
        uploader.setUploadAuthAndAddress(uploadInfo, uploadAuth, uploadAddress,videoId)
        })
    }
 ,
```
4. sts方式
```bash
 onUploadstarted: function (uploadInfo) {
    // 如果是 STSToken 上传方式, 需要调用 uploader.setSTSToken 方法
    // 用户需要自己获取 accessKeyId, accessKeySecret,secretToken
    // 下面的 URL 只是测试接口, 用于获取 测试的 accessKeyId, accessKeySecret,secretToken
    let stsUrl = 'http://demo-vod.cn-shanghai.aliyuncs.com/voddemo/CreateSecurityToken?BusinessType=vodai&TerminalType=pc&DeviceModel=iPhone9,2&UUID=67999yyuuuy&AppVersion=1.0.0'
    axios.get(stsUrl).then(({data}) => {
        let info = data.SecurityTokenInfo
        let accessKeyId = info.AccessKeyId
        let accessKeySecret = info.AccessKeySecret
        let secretToken = info.SecurityToken
        uploader.setSTSToken(uploadInfo, accessKeyId, accessKeySecret, secretToken)
    })
    self.statusText = '文件开始上传...'
    console.log("onUploadStarted:" + uploadInfo.file.name + ", endpoint:" + uploadInfo.endpoint + ", bucket:" + uploadInfo.bucket + ", object:" + uploadInfo.object)
 },
```


## 完整文档
SDK提供的上传等功能，详见官方完整文档：[点击查看](https://help.aliyun.com/document_detail/52204.html)


## License
Apache License 2.0


## 联系我们
阿里云VOD控制台：[点击查看](https://vod.console.aliyun.com)
阿里云VOD帮助文档：[点击查看](https://help.aliyun.com/product/29932.html)