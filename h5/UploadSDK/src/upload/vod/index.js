//Copyright (C) 2020 Alibaba Group Holding Limited
import  {UPLOADSTATE,VODSTATE} from '../../constants/vodupload';
import  {UPLOADDEFAULT} from '../../constants/ossupload';
import OssUpload from '../oss';
import base64  from '../../service/base64';
import Store from '../../service/store';
import Log from '../../service/log';
import util from '../../service/util';
import guid from '../../service/guid';
import data from './data';
import fileService from '../../service/fileService';
var MD5 = require('crypto-js/md5');

export default class VODUpload
{
    constructor(options) {
        this.options = options;
        this.options.partSize = this.options.partSize || UPLOADDEFAULT.PARTSIZE;
        this.options.parallel = this.options.parallel || UPLOADDEFAULT.PARALLEL;
        this.options.region = this.options.region || 'cn-shanghai';
        this.options.cname = this.options.cname || false;
        this.options.localCheckpoint = this.options.localCheckpoint || false;
        this.options.enableUploadProgress = this.options.enableUploadProgress || true;
        this._ossCreditor = new Object();
        this._state = VODSTATE.INIT;
        this._uploadList = [];
        this._curIndex = -1;
        this._ossUpload = null;
        this._log  = new Log();
        this._retryCount = 0;
        this._retryTotal = this.options.retryCount || 3;
        this._retryDuration = this.options.retryDuration || 2;
        this._state = VODSTATE.INIT;
        this._uploadWay = 'vod';
        this._onbeforeunload = false;
        this._invalidUserId = false;
        this._initEvent();
        // if(!util.isIntNum(this.options.userId)){
        //     var msg = 'userId属性(阿里云账号ID)不能为空且只能为数字，如果获取账号ID请求参考：https://help.aliyun.com/knowledge_detail/37196.html';
        //     this._invalidUserId = true;
        //     console.log(msg);
        // }
    }

    // 配置OSS参数
    init(accessKeyId, accessKeySecret, securityToken, expireTime) {
        this._retryCount = 0;
        if ((securityToken && !expireTime) ||
            (!securityToken && expireTime)) {
            return false;
        }
        if ((accessKeyId && !accessKeySecret) ||
            (!accessKeyId && accessKeySecret)) {
            return false;
        } 

        this._ossCreditor.accessKeyId = accessKeyId;
        this._ossCreditor.accessKeySecret = accessKeySecret;
        this._ossCreditor.securityToken = securityToken;
        this._ossCreditor.expireTime = expireTime;
        return true;

    }

    addFile(file, endpoint, bucket, object, userData, callback) {
        if (!file) {
            return false;
        }
        if(file.size==0)
        {
            try
            {
                this.options.onUploadFailed({file:file}, 'EmptyFile', "文件大小为0，不能上传");
            }catch(e)
            {
                console.log(e);
            }
        }
        var options = this.options;
        // 判断重复添加
        for(var i=0; i<this._uploadList.length; i++) {
            if (this._uploadList[i].file == file) {
                return false;
            }
        }

        var uploadObject = new Object();
        uploadObject.file = file;
        uploadObject._endpoint = endpoint;
        uploadObject._bucket = bucket;
        uploadObject._object = object;
        uploadObject.state = UPLOADSTATE.INIT;
        uploadObject.isImage = util.isImage(file.name);
        if(!uploadObject.isImage && this.options.enableUploadProgress)
        {
            var that = this;
            fileService.getMd5(file,function(data){
                uploadObject.fileHash =  data;
                var cp = that._getCheckoutpoint(uploadObject);
                if(!that.options.localCheckpoint && !cp)
                {
                    that._getCheckoutpointFromCloud(uploadObject,function(data){
                        if(data.UploadPoint)
                        {
                            var checkpoint = JSON.parse(data.UploadPoint);
                            if(checkpoint.loaded!=1)
                            {
                                uploadObject.checkpoint = checkpoint.checkpoint;
                                uploadObject.loaded = checkpoint.loaded;
                                uploadObject.videoId = data.VideoId;
                                that._saveCheckoutpoint(uploadObject,checkpoint.checkpoint);
                            }
                        }
                    },function(error){
                        try
                        {
                            error = JSON.parse(error);
                            if (error && error.Code == 'InvalidParameter' && error.Message.indexOf('UserId') > 0) {
                                that._invalidUserId = true;
                                var msg = error.Message +'，正确账号ID(userId)请参考：https://help.aliyun.com/knowledge_detail/37196.html';
                                console.log(msg);
                            }
                        }catch(e)
                        {
                            console.log(e);
                        }
                    });
                }
                
            });
        }
        if(userData)
        {
            uploadObject.videoInfo = userData ?  JSON.parse(userData).Vod: {};
            uploadObject.userData = base64.encode(userData);
        }
        uploadObject.ri = guid.create();

        // 添加文件.
        this._uploadList.push(uploadObject);

        this._reportLog('20001', uploadObject, {ql:this._uploadList.length});
        try{
            if(this.options.addFileSuccess)
            {
                this.options.addFileSuccess(uploadObject);
            }
        }catch(e)
        {
            console.log(e);
        }
        return true;
    }
    deleteFile(index) {
        if (this.cancelFile(index)) {
            this._uploadList.splice(index, 1);
            return true;
        }

        return false;
    }
    cleanList() {
        this.stopUpload();
        this._uploadList.length = 0;
        this._curIndex  = -1;
    }
    cancelFile(index) {
        var options = this.options;

        if (index < 0 || index >= this._uploadList.length) {
            return false;
        }
        var item = this._uploadList[index];
        if (index == this._curIndex && item.state == UPLOADSTATE.UPLOADING) {
            item.state = UPLOADSTATE.CANCELED;
            var cp = this._getCheckoutpoint(item);
            if(cp && cp.checkpoint)
            {
                cp = cp.checkpoint;
            }
            if(cp)
            {
                this._ossUpload.abort(item);
            }
            this._removeCheckoutpoint(item);
            this.nextUpload();
        } else if (item.state != UPLOADSTATE.SUCCESS) {
            item.state = UPLOADSTATE.CANCELED;
        }

        this._reportLog('20008',item);

        return true;
    }

    resumeFile(index) {
        var options = this.options;

        if (index < 0 || index >= this._uploadList.length) {
            return false;
        }
        var item = this._uploadList[index];
        if (item.state != UPLOADSTATE.CANCELED) {
            return false;
        }

        item.state = UPLOADSTATE.INIT;

        return true;
    }
    listFiles() {
        // list all.
        return this._uploadList;
    }

    getCheckpoint(file){
        return this._getCheckoutpoint({file:file})
    }

    // 开始上传
    startUpload(index) {
        this._retryCount = 0;
        var options = this.options;

        if (this._state == VODSTATE.START || this._state == VODSTATE.EXPIRE) {
            console.log('already started or expired');
            return;
        }
        this._initState();
        this._curIndex = this._findUploadIndex();

        if (-1 == this._curIndex) {
            this._state = VODSTATE.END
            return;
        }

        var curObject = this._uploadList[this._curIndex];
        this._ossUpload = null;//重新new一下oss上传对象
        this._upload(curObject);

        this._state = VODSTATE.START;
    }
    nextUpload() {
        var options = this.options;

        if (this._state != VODSTATE.START) {
            return;
        }

        this._curIndex = this._findUploadIndex();

        // 上传结束。
        if (-1 == this._curIndex) {
            this._state = VODSTATE.END;
            try
            {
                if(options.onUploadEnd)
                {
                    options.onUploadEnd(curObject);
                }
                
            }catch(e)
            {
                console.log(e);
            }
            return;
        }

        var curObject = this._uploadList[this._curIndex];
        this._ossUpload = null;//重新new一下oss上传对象
        this._upload(curObject);
    }
    clear(state) {
        var options = this.options;
        var num = 0;
        for(var i=0; i<this._uploadList.length; i++) {
            if (options.uploadList[i].state == UPLOADSTATE.SUCCESS) {
                num++;
            }

            if (this._uploadList[i].state == state) {
                options.uploadList.splice(i, 1);
                i--;
            }
        }

        if (options.onClear) {
            options.onClear(options.uploadList.length, num);
        }
    }

    // // 停止上传
    stopUpload() {
        if (this._state != VODSTATE.START && this._state != VODSTATE.FAILURE && this._curIndex!=-1) {
            return;
        }
        if(this._curIndex != -1)
        {
            var item = this._uploadList[this._curIndex];
            this._state = VODSTATE.STOP;
            item.state = UPLOADSTATE.STOPED;
            this._changeState(item, UPLOADSTATE.STOPED)
            this._ossUpload.cancel();
        }
    }

    // // 停止上传
    // resumeUpload() {
    //     if (this._state == VODSTATE.STOP &&!this._curIndex) {
    //         var item = this._uploadList[this._curIndex];
    //         if(item.state == UPLOADSTATE.STOPED) {
    //             item.state = UPLOADSTATE.UPLOADING;
    //             this._ossUpload.resumeUpload();
    //             this._state = VODSTATE.START;
    //             return;
    //         }
    //     }
    // }
    // 恢复上传
    resumeUploadWithAuth(uploadAuth) {
        var self = this;

        if (!uploadAuth) { 
            return false;
        }
        var key = JSON.parse(base64.decode(uploadAuth));
        if (!key.AccessKeyId || !key.AccessKeySecret || !key.SecurityToken || !key.Expiration) {
            return false;
        }

        return self.resumeUploadWithToken(key.AccessKeyId, key.AccessKeySecret,
                                       key.SecurityToken, key.Expiration);
    }

    // 恢复上传
    resumeUploadWithToken(accessKeyId, accessKeySecret, securityToken, expireTime) {
        var options = this.options;

        if (!accessKeyId || !accessKeySecret || !securityToken || !expireTime) {
            return false;
        }

        if (this._state != VODSTATE.EXPIRE) {
            return false;
        }

        if (-1 == this._curIndex) {
            return false;
        }
        var curObject = "";
        if(this._uploadList.length> this._curIndex)
        {
            curObject = this._uploadList[this._curIndex];
        }
        if(curObject)
        {
            this.init(accessKeyId, accessKeySecret, securityToken, expireTime);
            this._state = VODSTATE.START
            this._ossUpload = null;
            this._uploadCore(curObject,curObject.retry);
            curObject.retry = false;
        }
        return true;
    }

    resumeUploadWithSTSToken(accessKeyId, accessKeySecret, securityToken)
    {
        if (-1 == this._curIndex) {
            return false;
        }
        if (this._state != VODSTATE.EXPIRE) {
            return false;
        }

        if(this._uploadList.length > this._curIndex)
        {
            var curObject = this._uploadList[this._curIndex];
            if(curObject.object)
            {
                this._refreshSTSTokenUpload(curObject,accessKeyId, accessKeySecret, securityToken);
            }
            else
            {
                this.setSTSToken(curObject,accessKeyId, accessKeySecret, securityToken);
            }
        }
    }

    setSTSTokenDirectlyUpload(uploadInfo, accessKeyId, accessKeySecret, securityToken,expiration)
    {
        if (!accessKeyId || !accessKeySecret || !securityToken || !expiration) {
            console.log('accessKeyId、ccessKeySecret、securityToken and expiration should not be empty.')
            return false;
        }
        this._ut = "oss";
        var curObject = uploadInfo;
        this.init(accessKeyId, accessKeySecret, securityToken, expiration);
        curObject.endpoint = curObject._endpoint
        curObject.bucket = curObject._bucket
        curObject.object = curObject._object
        this._ossUpload = null;
        this._uploadCore(curObject,uploadInfo.retry);
        uploadInfo.retry = false;
    }

     // 设置上传凭证
    setSTSToken(uploadInfo, accessKeyId, accessKeySecret, securityToken) {
        if (!accessKeyId || !accessKeySecret || !securityToken) {
            console.log('accessKeyId、ccessKeySecret、securityToken should not be empty.')
            return false;
        }
        this._ut = "vod";
        this._uploadWay = 'sts';
        let videoInfo = uploadInfo.videoInfo;
        let params = {
            'accessKeyId':accessKeyId,
            'securityToken': securityToken,
            'accessKeySecret': accessKeySecret,
            'fileName': uploadInfo.file.name,
            'title':videoInfo.Title,
            'requestId':uploadInfo.ri,
            'region' : this.options.region
          };

        if(videoInfo.ImageType)
        {
            params.imageType = videoInfo.ImageType;
        }

        if(videoInfo.ImageExt)
        {
            params.imageExt = videoInfo.ImageExt;
        }
   
        if(videoInfo.FileSize)
        {
            params.fileSize = videoInfo.FileSize;
        }
        if(videoInfo.Description)
        {
            params.description = videoInfo.Description;
        }

        if(videoInfo.CateId)
        {
            params.cateId = videoInfo.CateId;
        }

        if(videoInfo.Tags)
        {
            params.tags = videoInfo.Tags;
        }

        if(videoInfo.TemplateGroupId)
        {
            params.templateGroupId = videoInfo.TemplateGroupId;
        }
        if(videoInfo.StorageLocation)
        {
            params.storageLocation = videoInfo.StorageLocation;
        }

        if(videoInfo.CoverURL)
        {
            params.coverUrl = videoInfo.CoverURL;
        }

        if(videoInfo.TransCodeMode)
        {
            params.transCodeMode = videoInfo.TransCodeMode;
        }

        if(videoInfo.UserData)
        {
            params.userData = videoInfo.UserData;
        }
        let that = this,
        func = "getUploadAuth";
        if(uploadInfo.videoId)
        {
            params.videoId = uploadInfo.videoId;
            func = "refreshUploadAuth";
        }
        else if(uploadInfo.isImage)
        {
            func = "getImageUploadAuth";
        }
        data[func](params, (result)=>{
            uploadInfo.videoId = result.VideoId ? result.VideoId:uploadInfo.videoId
            that.setUploadAuthAndAddress(uploadInfo, result.UploadAuth,result.UploadAddress)
            that._state = VODSTATE.START
        }, (error)=>{
            that._error(uploadInfo,{
                name:error.Code,
                code:error.Code,
                message: error.Message,
                requestId: error.RequestId
            });
        });
    }

    // 设置上传凭证
    setUploadAuthAndAddress(uploadInfo, uploadAuth, uploadAddress,videoId) {
        if (!uploadInfo || !uploadAuth || !uploadAddress) {
            return false;
        }
        var authKey = JSON.parse(base64.decode(uploadAuth));
        if (!authKey.AccessKeyId || !authKey.AccessKeySecret ||
            !authKey.SecurityToken || !authKey.Expiration) {
            console.error('uploadauth is invalid');
            return false;
        }

        var addressKey = {};
        var curObject = uploadInfo;
        if(uploadAddress)
        {
            addressKey = JSON.parse(base64.decode(uploadAddress));
            if (!addressKey.Endpoint || !addressKey.Bucket || !addressKey.FileName) {
                console.error('uploadAddress is invalid');
                return false;
            }
        }
        else
        {
            addressKey.Endpoint = curObject.endpoint;
            addressKey.Bucket = curObject.bucket;
            addressKey.FileName = curObject.object;
        }
        this._ut = "vod";
        this._uploadWay = 'vod';
        this.options.region =  authKey.Region || this.options.region;

        this.init(authKey.AccessKeyId, authKey.AccessKeySecret, authKey.SecurityToken, authKey.Expiration);
        curObject.endpoint = curObject._endpoint?curObject._endpoint : addressKey.Endpoint;
        curObject.bucket = curObject._bucket?curObject._bucket:addressKey.Bucket;
        curObject.object = curObject._object?curObject._object:addressKey.FileName;
        curObject.region = this.options.region;
        if(videoId)
        {
            curObject.videoId = videoId;
        }
        this._ossUpload = null;
        this._uploadCore(curObject,uploadInfo.retry);
        uploadInfo.retry = false;
    }

    _refreshSTSTokenUpload(uploadInfo,accessKeyId, accessKeySecret, securityToken)
    {
        if (!accessKeyId || !accessKeySecret || !securityToken) {
            console.log('accessKeyId、ccessKeySecret、securityToken should not be empty.')
            return false;
        }
        let params = {
            'accessKeyId':accessKeyId,
            'securityToken': securityToken,
            'accessKeySecret': accessKeySecret,
            'videoId': uploadInfo.object,
            'requestId':uploadInfo.ri,
            'region' : this.options.region
          };
        let that = this,
        func = "refreshUploadAuth";
        if(uploadInfo.isImage)
        {
            func = "getImageUploadAuth";
        }
        data[func](params, (result)=>{
            that.setUploadAuthAndAddress(uploadInfo, result.UploadAuth,UploadAddress)
            that._state = VODSTATE.START
        }, (error)=>{
            that._error(uploadInfo,{
                name:error.Code,
                code:error.Code,
                message: error.Message,
                requestId: error.RequestId
            });
        });
    }

    _upload(curObject, retry=false)
    {
        var options = this.options;
        curObject.retry = retry;
        if (options.onUploadstarted && !retry) {
            try
            {
                let cp = this._getCheckoutpoint(curObject);
                if(cp && cp.state != UPLOADSTATE.UPLOADING)
                {
                    curObject.checkpoint = cp;

                    curObject.videoId = cp.videoId;
                }

                options.onUploadstarted(curObject);
                
            }catch(e)
            {
                console.log(e);
            }
        }
    }

    _uploadCore(curObject, retry=false)
    {
        if(!this._ossCreditor.accessKeyId || !this._ossCreditor.accessKeySecret || !this._ossCreditor.securityToken)
        {
            throw new Error('AccessKeyId、AccessKeySecret、securityToken should not be null');
        }
    
        curObject.state = UPLOADSTATE.UPLOADING;
        if(!this._ossUpload)
        {
            curObject.endpoint = curObject.endpoint || 'http://oss-cn-hangzhou.aliyuncs.com';

            var that = this;
            this._ossUpload = new OssUpload({
                    bucket: curObject.bucket,
                    endpoint: curObject.endpoint,
                    AccessKeyId:  this._ossCreditor.accessKeyId,
                    AccessKeySecret:  this._ossCreditor.accessKeySecret,
                    SecurityToken:  this._ossCreditor.securityToken,
                    timeout: this.options.timeout,
                    cname: this.options.cname
                },{
                    onerror:(obj, info)=>{
                        that._error.call(that, obj, info);
                    },
                    oncomplete:(obj, info)=>{
                        that._complete.call(that, obj, info);
                    },
                    onprogress:(obj, info,res)=>{
                        that._progress.call(that, obj, info,res);
                    }
                });
        }
        let type = util.getFileType(curObject.file.name),
        cp = this._getCheckoutpoint(curObject),
        uploadId = 0,
        vid = "",
        state = "";
        if(cp && cp.checkpoint)
        {
            state = cp.state
            vid = cp.videoId;
            cp = cp.checkpoint;
        }
        if(cp && vid == curObject.videoId && state != UPLOADSTATE.UPLOADING)
        {
            cp.file = curObject.file;
            curObject.checkpoint = cp;
            uploadId = cp.uploadId;
        }
        let partSize = this._adjustPartSize(curObject);
        this._reportLog('20002',curObject,{ft:type,
            fs:curObject.file.size,
            bu:curObject.bucket,
            ok:curObject.object,
            vid:curObject.videoId||"",
            fn:curObject.file.name,
            fw:null,
            fh:null,
            ps:partSize
        });
        
       
        var config = {
          headers: {
            'x-oss-notification': curObject.userData ? curObject.userData : ""
          },
          partSize:partSize,
          parallel : this.options.parallel
        };
        this._ossUpload.upload(curObject, config);
    }

    _findUploadIndex()
    {
        var index = -1;
        for (var i=0;i<this._uploadList.length; i++) {
            if (this._uploadList[i].state == UPLOADSTATE.INIT) {
                index = i;
                break;
            }
        }

        return index;
    }

    _error(uploadInfo,evt)
    {
        if(evt.name == 'cancel')
        {
            try
            {
                this.options.onUploadCanceled(uploadInfo,evt);
            }catch(e)
            {
                console.log(e);
            }
        }
        else if (evt.message.indexOf('InvalidAccessKeyIdError') > 0 ||
            evt.name == "SignatureDoesNotMatchError" || 
            evt.code == "SecurityTokenExpired" || 
            evt.code == "InvalidSecurityToken.Expired"||
            (evt.code == "InvalidAccessKeyId" && this._ossCreditor.securityToken )) {
            if (this.options.onUploadTokenExpired) {
                this._state = VODSTATE.EXPIRE;
                uploadInfo.state = UPLOADSTATE.FAIlURE;

                try
                {
                    this.options.onUploadTokenExpired(uploadInfo,evt);
                }catch(e)
                {
                    console.log(e);
                }
            }
            return ;
        }
        else if((evt.name == "RequestTimeoutError"  
            ||evt.name == 'ConnectionTimeout' 
            ||evt.name == 'ConnectionTimeoutError' ) 
            && this._retryTotal > this._retryCount)
        {
            let that = this;
            setTimeout(()=>{
                that._uploadCore(uploadInfo, true);
            },
            that._retryDuration * 1000
            );
            this._retryCount++;
            return;
        }
        else
        {
            if(evt.name == "NoSuchUploadError")
            {
                this._removeCheckoutpoint(uploadInfo);
            }
            this._handleError(uploadInfo, evt);
        }
        
    }

    _handleError(uploadInfo, evt, changeState = true)
    {
        let state = UPLOADSTATE.FAIlURE;
        if (uploadInfo.state != UPLOADSTATE.CANCELED) {
            uploadInfo.state = UPLOADSTATE.FAIlURE;
            this._state = VODSTATE.FAILURE;
            if (this.options.onUploadFailed) {
                if (evt && evt.code && evt.message) {
                    try
                    {
                        this.options.onUploadFailed(uploadInfo, evt.code, evt.message);
                    }catch(e)
                    {
                        console.log(e);
                    }
                }
            }
        }
        if(changeState)
        {
            this._changeState(uploadInfo, state);
        }
        this._reportLog('20006',uploadInfo,{
            code:evt.name,
            message:evt.message,
            requestId:evt.requestId,
            fs:uploadInfo.file.size,
            bu:uploadInfo.bucket,
            ok:uploadInfo.object,
            fn:uploadInfo.file.name
        });
        this._reportLog('20004',uploadInfo,{
            requestId:evt.requestId,
            fs:uploadInfo.file.size,
            bu:uploadInfo.bucket,
            ok:uploadInfo.object,
            fn:uploadInfo.file.name

        });
        uploadInfo.ri = guid.create();
        let curIndex = this._findUploadIndex();
        if(curIndex != -1)//继续上传下一个文件
        {
            var that = this;
            this._state = VODSTATE.START;
            setTimeout(function () {
                that.nextUpload();
            }, 100);
        }
    }

    _complete(uploadInfo,result)
    {
        uploadInfo.state = UPLOADSTATE.SUCCESS;
        if (this.options.onUploadSucceed) {
            try
            {
               this.options.onUploadSucceed(uploadInfo);
            }catch(e)
            {
                console.log(e);
            }
        }
        let requestId = 0;
        if(result && result.res && result.res.headers)
        {
            requestId = result.res.headers['x-oss-request-id'];
        }
        this._removeCheckoutpoint(uploadInfo);
        var that = this;
        setTimeout(function () {
            that.nextUpload();
        }, 100);
        this._retryCount = 0;
        this._reportLog('20003',uploadInfo,{
            requestId:requestId
        });
    }

    _progress(uploadInfo,info, res)
    {
        if (this.options.onUploadProgress) {
            try
            {
                uploadInfo.loaded = info.loaded;
               this.options.onUploadProgress(uploadInfo, info.total, info.loaded);
            }catch(e)
            {
                console.log(e);
            }
        }
        var checkpoint = info.checkpoint,
        uploadId = 0;
        if(checkpoint)
        {
            uploadInfo.checkpoint = checkpoint;
            this._saveCheckoutpoint(uploadInfo,checkpoint,UPLOADSTATE.UPLOADING);
            uploadId = checkpoint.uploadId;
        }
        this._retryCount = 0;
        var pn = this._getPortNumber(checkpoint);
        let requestId = 0;
        if(res && res.headers)
        {
            requestId = res.headers['x-oss-request-id'];
        }
        if(info.loaded!=0)
        {
            this._reportLog('20007',uploadInfo,{
                pn:pn,
                requestId:requestId
            });
        }
        if(info.loaded!=1)
        {
            this._reportLog('20005',uploadInfo,{
                UploadId:uploadId,
                pn:pn + 1,
                pr: uploadInfo.retry?1:0,
                fs:uploadInfo.file.size,
                bu:uploadInfo.bucket,
                ok:uploadInfo.object,
                fn:uploadInfo.file.name
            });
        }
    }

    _getPortNumber(cp)
    {
        if(cp)
        {
            var doneParts = cp.doneParts;
            if(doneParts && doneParts.length > 0)
            {
                return doneParts[doneParts.length-1].number;
            }
        }
        return 0;
    }

    _removeCheckoutpoint(uploadInfo)
    {
        var key = this._getCheckoutpointKey(uploadInfo);
        Store.remove(key);
    }

    _getCheckoutpoint(uploadInfo)
    {
        var key = this._getCheckoutpointKey(uploadInfo);
        var value = Store.get(key);
        if(value)
        {
            try
            {
                return JSON.parse(value);
            }
            catch(e)
            {}
        }
        return "";
    }

    _saveCheckoutpoint(uploadInfo,checkpoint,state)
    {
        if(checkpoint)
        {
            var key = this._getCheckoutpointKey(uploadInfo),
            file = uploadInfo.file,
            value = {
                fileName:file.name,
                lastModified:file.lastModified,
                size:file.size,
                object:uploadInfo.object,
                videoId:uploadInfo.videoId,
                bucket:uploadInfo.bucket,
                endpoint:uploadInfo.endpoint,
                checkpoint:checkpoint,
                loaded:uploadInfo.loaded,
                state:state
            };
            Store.set(key, JSON.stringify(value));
        }
    }

    _changeState(uploadInfo, state)
    {
        let value = this._getCheckoutpoint(uploadInfo);
        if(value)
        {
            if(this._onbeforeunload = true)
            {
                state = UPLOADSTATE.STOPED;
            }
            this._saveCheckoutpoint(uploadInfo,value.checkpoint,state);
        }
    }

    _getCheckoutpointKey(info)
    {
        var key = `upload_${info.file.lastModified}_${info.file.name}_${info.file.size}`;
        return key;
    }

    _reportLog(e, info, params)
    {
        if(!params)
        {
            params = {};
        }
        params.ri = info.ri;
        if(this._ut)
        {
            params.ut = this._ut;
        }
        this._log.log(e,params);
    }

    _initEvent()
    {
        let that = this;
        if(window)
        {
            window.onbeforeunload= function(e){
                that._onbeforeunload = true;
                if (-1 == that._curIndex) {
                    return;
                }
                if(that._uploadList.length > that._curIndex)
                {
                    let curObject = that._uploadList[that._curIndex];
                    that._changeState(curObject, UPLOADSTATE.STOPED)
                }
            };
        }
    }

    _initState()
    {
        for (var i = 0;i<this._uploadList.length; i++) {
            var item = this._uploadList[i];
            if (item.state == UPLOADSTATE.FAIlURE || item.state == UPLOADSTATE.STOPED) {
                item.state = UPLOADSTATE.INIT;
            }
        }
        this._state = VODSTATE.INIT;
    }

    _adjustPartSize(curObject)
    {
        let currentParts = curObject.file.size/this.options.partSize;
        if(currentParts > 10000)
        {
            return curObject.file.size/9999
        }
        return this.options.partSize;
    }
}  