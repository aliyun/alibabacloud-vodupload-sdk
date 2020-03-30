<template>
  <div class="container">
    <div class="setting">
      <div class="input-control">
        <label for="timeout">请求过期时间（构造参数 timeout, 默认 60000）:</label>
        <input type="text" id="timeout" v-model="timeout" placeholder="输入过期时间, 单位毫秒">
      </div>

      <div class="input-control">
        <label for="partSize">分片大小（构造参数 partSize, 默认 1048576）:</label>
        <input type="text" class="form-control" id="partSize" v-model="partSize" placeholder="输入分片大小, 单位bit, 最小100k">
      </div>

      <div class="input-control">
        <label for="parallel">上传分片数（构造参数 parallel, 默认 5）:</label>
        <input type="text" class="form-control" id="parallel" v-model="parallel" placeholder="输入并行上传分片个数, 默认为5">
      </div>

      <div class="input-control">
        <label for="retryCount">网络失败重试次数（构造参数 retryCount, 默认 3）:</label>
        <input type="text" class="form-control" id="retryCount" v-model="retryCount" placeholder="输入网络失败重试次数, 默认为3">
      </div>

      <div class="input-control">
        <label for="retryDuration">网络失败重试间隔（构造参数 retryDuration, 默认 2）:</label>
        <input type="text" class="form-control" id="retryDuration" v-model="retryDuration" placeholder="输入网络失败重试间隔, 默认2秒">
      </div>

      <div class="input-control">
        <label for="region">配置项 region, 默认 cn-shanghai:</label>
        <select v-model="region">
          <option>cn-shanghai</option>
          <option>eu-central-1</option>
          <option>ap-southeast-1</option>
        </select>
      </div>

      <div class="input-control">
        <label for="userId">阿里云账号ID</label>
        <input type="text" class="form-control" v-model="userId" disabled placeholder="输入阿里云账号ID">
        集成产品后需要使用用户自己的账号ID,<a href="https://help.aliyun.com/knowledge_detail/37196.html "target="_blank">如何获取帐号ID</a>
      </div>

    </div>

    <div class="upload">
      <div>
        <input type="file" id="fileUpload" @change="fileChange($event)">
        <label class="status">上传状态: <span>{{statusText}}</span></label>
      </div>
      <div class="upload-type">
        上传方式二, 使用 STSToken 上传:
        <button @click="stsUpload" :disabled="uploadDisabled">开始上传</button>
        <button @click="pauseUpload" :disabled="pauseDisabled">暂停</button>
        <button :disabled="resumeDisabled" @click="resumeUpload">恢复上传</button>
        <span class="progress">上传进度: <i id="sts-progress">{{stsProgress}}</i> %</span>
      </div>
    </div>
    <div class="info">点播STS参数如何获取，请查阅<a href="https://help.aliyun.com/document_detail/28788.html?spm=a2c4g.11186623.2.6.1mSfTK" target="_blakn">获取STS</a></div>
  </div>
</template>
<script>
  import axios from 'axios'

  export default {
    data () {
      return {
        timeout: '',
        partSize: '',
        parallel: '',
        retryCount: '',
        retryDuration: '',
        region: 'cn-shanghai',
        userId: '1303984639806000',        
        file: null,
        stsProgress: 0,
        uploadDisabled: true,
        resumeDisabled: true,
        pauseDisabled: true,
        statusText: '',
        pauseDisabled: true,
        uploader: null
      }
    },
    methods: {
      fileChange (e) {
        this.file = e.target.files[0]
        if (!this.file) {
          alert("请先选择需要上传的文件!")
          return
        }
        var Title = this.file.name
        var userData = '{"Vod":{}}'
        if (this.uploader) {
          this.uploader.stopUpload()
          this.authProgress = 0
          this.statusText = ""
        }
        this.uploader = this.createUploader()
        // 首先调用 uploader.addFile(event.target.files[i], null, null, null, userData)
        console.log(userData)
        this.uploader.addFile(this.file, null, null, null, userData)
        this.uploadDisabled = false
        this.pauseDisabled = true
        this.resumeDisabled = false
      },
      // 开始上传
      stsUpload () {
        // 然后调用 startUpload 方法, 开始上传
        if (this.uploader !== null) {
          this.uploader.startUpload()
          this.uploadDisabled = true
          this.pauseDisabled = false
        }
      },
      // 暂停上传
      pauseUpload () {
        if (this.uploader !== null) {
          this.uploader.stopUpload()
          this.resumeDisabled = false
          this.pauseDisabled = true
        }
      },
      // 恢复上传
      resumeUpload () {
        if (this.uploader !== null) {
          this.uploader.startUpload()
          this.resumeDisabled = true
          this.pauseDisabled = false
        }
      },
      createUploader () {
        let self = this
        let uploader = new AliyunUpload.Vod({
          timeout: self.timeout || 60000,
          partSize: self.partSize || 1048576,
          parallel: self.parallel || 5,
          retryCount: self.retryCount || 3,
          retryDuration: self.retryDuration || 2,
          region: self.region,
          userId: self.userId,
          // 添加文件成功
          addFileSuccess: function (uploadInfo) {
            self.uploadDisabled = false
            self.resumeDisabled = false
            self.statusText = '添加文件成功, 等待上传...'
            console.log("addFileSuccess: " + uploadInfo.file.name)
          },
          // 开始上传
          onUploadstarted: function (uploadInfo) {
            // 如果是 STSToken 上传方式, 需要调用 uploader.setUploadAuthAndAddress 方法
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
          // 文件上传成功
          onUploadSucceed: function (uploadInfo) {
            console.log("onUploadSucceed: " + uploadInfo.file.name + ", endpoint:" + uploadInfo.endpoint + ", bucket:" + uploadInfo.bucket + ", object:" + uploadInfo.object)
            self.statusText = '文件上传成功!'
          },
          // 文件上传失败
          onUploadFailed: function (uploadInfo, code, message) {
            console.log("onUploadFailed: file:" + uploadInfo.file.name + ",code:" + code + ", message:" + message)
            self.statusText = '文件上传失败!'
          },
          // 取消文件上传
          onUploadCanceled: function (uploadInfo, code, message) {
            console.log("Canceled file: " + uploadInfo.file.name + ", code: " + code + ", message:" + message)
            self.statusText = '文件已暂停上传'
          },
          // 文件上传进度，单位：字节, 可以在这个函数中拿到上传进度并显示在页面上
          onUploadProgress: function (uploadInfo, totalSize, progress) {
            console.log("onUploadProgress:file:" + uploadInfo.file.name + ", fileSize:" + totalSize + ", percent:" + Math.ceil(progress * 100) + "%")
            let progressPercent = Math.ceil(progress * 100)
            self.stsProgress = progressPercent
            self.statusText = '文件上传中...'
          },
          // 上传凭证超时
          onUploadTokenExpired: function (uploadInfo) {
            // 如果是上传方式二即根据 STSToken 实现时，从新获取STS临时账号用于恢复上传
            // 上传文件过大时可能在上传过程中 sts token 就会失效, 所以需要在 token 过期的回调中调用 resumeUploadWithSTSToken 方法
            // 这里是测试接口, 所以我直接获取了 STSToken
            let stsUrl = 'http://demo-vod.cn-shanghai.aliyuncs.com/voddemo/CreateSecurityToken?BusinessType=vodai&TerminalType=pc&DeviceModel=iPhone9,2&UUID=67999yyuuuy&AppVersion=1.0.0'
            axios.get(stsUrl).then(({data}) => {
              let info = data.SecurityTokenInfo
              let accessKeyId = info.AccessKeyId
              let accessKeySecret = info.AccessKeySecret
              let secretToken = info.SecurityToken
              let expiration = info.Expiration
              uploader.resumeUploadWithSTSToken(accessKeyId, accessKeySecret, secretToken, expiration)
            })
            self.statusText = '文件超时...'
          },
          // 全部文件上传结束
          onUploadEnd: function (uploadInfo) {
            console.log("onUploadEnd: uploaded all the files")
            self.statusText = '文件上传完毕'
          }
        })
        return uploader
      }
    }
  }
</script>