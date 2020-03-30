## 阿里云上传文件 demo

包含文件的上传, 上传文件的暂停, 取消上传, 删除进行上传的文件等功能。

## 运行

实际上只是起了一个服务, 用文件系统打开 src 目录下的 index.html 也是一样的

```
$ npm start
```

浏览器中打开 `http://127.0.0.1:3000/src/index.html`

## 构建

主要用到了 jquery 1.8.3 和 bootstrap 3。以及视频上传 SDK。 [上传SDK文档](https://help.aliyun.com/document_detail/52204.html?spm=a2c4g.11186623.6.720.uhKBHC)

### 使用依赖项

- 引用 jquery
- 引用 bootstrap
- 引用 SDK

### 注意事项

上传文件时, 本 demo 使用的是第二种方式上传, 需要获取并设置 STS token, 并在上传对象的 onUploadstarted 回调中调用 uploader 的 `setSTSToken` 方法。

**获取 STS Token 的方法中, 需要用户自己的 appserver 提供接口获取sts token, 并将 stsUrl 替换成对应的接口地址, 参考[创建和管理角色](https://help.aliyun.com/document_detail/28788.html?spm=a2c4g.11186623.2.6.YnTX07#)和[访问控制](https://help.aliyun.com/document_detail/65857.html?spm=a2c4g.11186623.6.721.Tk2ekU)**

```javascript
function getSTSToken (success, failed) {
  // stsUrl 需要用户自己实现, 实现之后将 stsUrl 替换成对应的接口地址 
  var stsUrl = 'http://demo-vod.cn-shanghai.aliyuncs.com/voddemo/CreateSecurityToken?BusinessType=vodai&TerminalType=pc&DeviceModel=iPhone9,2&UUID=67999yyuuuy&AppVersion=1.0.0'
  $.get(stsUrl, success, 'json').error(failed)
}
```

设置 STS token:

```javascript
...
'onUploadstarted': function (uploadInfo) {
  getSTSToken(function (data) {
    console.log(data)
    var tokenInfo = data.SecurityTokenInfo
    uploader.setSTSToken(uploadInfo, tokenInfo.AccessKeyId, tokenInfo.AccessKeySecret, tokenInfo.SecurityToken)
    updateStatus(uploadInfo.file.guid, 1)
  }, function (err) {
    console.log(err)
  })
  console.log("onUploadStarted:" + uploadInfo.file.name + ", endpoint:" + uploadInfo.endpoint + ", bucket:" + uploadInfo.bucket + ", object:" + uploadInfo.object)
}
...
```

当上传文件过程中, token 失效时需要重新获取 token, 在 onUploadTokenExpired 回调中调用 uploader 的 `resumeUploadWithSTSToken` 方法。

```javascript
...
'onUploadTokenExpired': function (uploadInfo) {
  console.log("onUploadTokenExpired")
  getSTSToken(function (data) {
    var tokenInfo = data.SecurityTokenInfo
    uploader.resumeUploadWithSTSToken(tokenInfo.accessKeyId, tokenInfo.accessKeySecret, tokenInfo.secretToken, tokenInfo.expireTime)
  }, function (err) {
    console.log(err)
  })
}
...
```