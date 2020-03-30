## 简介
阿里云视频点播上传SDK是阿里视频云端到云到端服务的重要一环，为您提供上传媒体文件到点播存储的开发工具包。集成点播上传SDK，就可以快捷上传包括视频、音频、图片、字幕等在内的各种媒体文件。同时提供服务端、Web端、移动端等多种版本SDK，全面适配各个主流平台和运行环境。
本文档主要介绍VOD Uoload SDK for android的安装和使用。

## 开发环境
- Android系统版本：2.3 及以上。


### 源码编译jar包
可以clone下工程源码之后，运行gradle命令打包：

**clone工程**
```bash
git clone https://github.com/aliyun/alibabacloud-vodupload-sdk.git
```
**进入目录**
```bash
cd alibabacloud-vodupload-sdk/android/VodUploadSdk/
```
**执行打包脚本，要求jdk 1.7**
```bash
sh releaseJar.sh
```
**进入打包生成目录，jar包生成在该目录下**
```bash
cd output && ls
```


### 直接引入上面编译好的jar包
首先进入到alibabacloud-vodupload-sdk/android/VodUploadSdk/output目录下得到jar包，目前包括 aliyun-vod-upload-android-sdk-1.6.0.jar
将以上3个jar包导入工程的libs目录


### 权限设置
以下是VOD Uoload SDK for android所需要的Android权限，请确保您的AndroidManifest.xml文件中已经配置了这些权限，否则，SDK将无法正常工作。

```
<uses-permission android:name="android.permission.INTERNET"></uses-permission>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
<uses-permission android:name="android.permission.READ_PHONE_STATE"></uses-permission>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE"></uses-permission>
```

## 完整文档
SDK提供的上传等功能，详见官方完整文档：[点击查看](https://help.aliyun.com/document_detail/62955.html)


## License
Apache License 2.0


## 联系我们
阿里云VOD控制台：[点击查看](https://vod.console.aliyun.com)
阿里云VOD帮助文档：[点击查看](https://help.aliyun.com/product/29932.html)
