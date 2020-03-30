## 简介
阿里云视频点播上传SDK是阿里视频云端到云到端服务的重要一环，为您提供上传媒体文件到点播存储的开发工具包。集成点播上传SDK，就可以快捷上传包括视频、音频、图片、字幕等在内的各种媒体文件。同时提供服务端、Web端、移动端等多种版本SDK，全面适配各个主流平台和运行环境。
本文档主要介绍VOD Uoload SDK for iOS的安装和使用。

## 开发环境
OS X 10.10 (or later)
iOS 8.0 (or later)
xcode11 (or later)

## 工程结构
### 目录说明
VODUpload为SDK工程
VODUploadDemo为demo工程
ReadMe.md为ReadMe文件
VODUpload.podspec为pod文件

### 编译SDK
**clone工程**
```bash
git clone https://github.com/aliyun/alibabacloud-vodupload-sdk.git
```
**进入目录**
```bash
cd alibabacloud-vodupload-sdk/iOS/VODUpload
```
**使用shell命令运行脚本进行编译**
```bash
sh buildFramework.sh
```
alibabacloud-vodupload-sdk/iOS/VODUpload/VODUpload.framework为产物SDK

### 调试
从alibabacloud-vodupload-sdk/iOS/VODUploadDemo/VODUploadDemo.xcodeproj打开项目
选择VODUploadDemo运行，即可进行源码调试

## 项目集成
### pod方式集成
在Podfile文件中添加VODUpload库依赖
```
pod 'VODUpload'
```
执行 pod install

### 手动方式集成
1.在Xcode中，把VODUpload.framework和AliyunOSSiOS.framework拖入项目Target下，在弹出框勾选Copy items if needed
2.添加以下系统依赖库：AVFoundation.framework,CoreMedia.framework,SystemConfiguration.framework,MobileCoreServices.framework,libresolv.9.tbd
3.SDK集成后，打开项目工程并修改以下配置：配置Build Setting — Linking — Other Linker Flags，添加-ObjC


## 完整文档
SDK提供的上传等功能，详见官方完整文档：[点击查看](https://help.aliyun.com/document_detail/62954.html)


## License
Apache License 2.0


## 联系我们
阿里云VOD控制台：[点击查看](https://vod.console.aliyun.com)
阿里云VOD帮助文档：[点击查看](https://help.aliyun.com/product/29932.html)
