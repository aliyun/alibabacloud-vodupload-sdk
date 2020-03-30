//Copyright (C) 2020 Alibaba Group Holding Limited
import  {UPLOADSTATE,UPLOADSTEP,UPLOADDEFAULT} from '../../constants/ossupload';
import UploadError from '../../constants/uploaderror';
import util from '../../service/util';
// import OSS  from 'ali-oss'

export default class OssUpload {

	constructor(config,callback) {
		if (!config) {
            // console.log('需要 config');
            return;
        }
        this._config = config;

        this.create(this._config);

        this._uploadInfo = null;
        this._callback = {};
        var moon = function(){};
        this._callback.onerror = callback.onerror||moon;
        this._callback.oncomplete  = callback.oncomplete||moon;
        this._callback.onprogress = callback.onprogress || moon;
	}

    create(option)
    {
        option.endpoint = option.endpoint || this._config.endpoint;
        option.bucket = option.bucket || this._config.bucket;
        if(!option.AccessKeyId || !option.AccessKeySecret || !option.endpoint || !option.SecurityToken)
        {
            throw new Error('AccessKeyId、AccessKeySecret、endpoint should not be null');
        }
        var optionValues = {
            accessKeyId: option.AccessKeyId,
            accessKeySecret: option.AccessKeySecret,
            stsToken: option.SecurityToken,
            endpoint: option.endpoint || this._config.endpoint,
            bucket: option.bucket || this._config.bucket,
            secure:true,
            cname: option.cname
        };
        if(option.timeout)
        {
            optionValues.timeout = option.timeout;
        }
        this.oss = new OSS.Wrapper(optionValues);
    }

    abort(uploadInfo)
    {
        if(uploadInfo.checkpoint)
        {
            var uploadId = uploadInfo.checkpoint.uploadId;
            this.oss.abortMultipartUpload(uploadInfo.object, uploadId);
        }
    }

    getVersion()
    {

    }

    cancel()
    {
        if(this.oss.cancel)
        {
            this.oss.cancel();
        }
    }

    upload(uploadInfo,options)
    {
        this._uploadInfo = uploadInfo;
        let that = this;
        let progress = function (percentage, checkpoint,res) {
          return function (done) {
            that._progress(percentage, checkpoint,res);
            done();
          };
        }
        let option = {
          parallel: options.parallel || this._config.parallel || UPLOADDEFAULT.PARALLEL,
          partSize: options.partSize || this._config.partSize || UPLOADDEFAULT.PARTSIZE,
          progress: progress
        }
        if(options.headers)
        {
            option.headers = options.headers;
        }
        if(uploadInfo.checkpoint)
        {
            option.checkpoint = uploadInfo.checkpoint;
        }
        if(!uploadInfo.bucket)
        {
            this.oss.options.bucket = uploadInfo.bucket;
        }

        if(!uploadInfo.endpoint)
        {
            this.oss.options.endpoint = uploadInfo.endpoint;
        }

        this.oss.multipartUpload(uploadInfo.object, uploadInfo.file,option).then(function(result, res) {
            that._complete(result);
        }).catch(function(err) {
            if(that.oss.cancel)
            {
                if(that.oss && that.oss.isCancel())
                {
                    console.log('oss is cancel as error');
                }
                else
                {
                    that.oss.cancel()
                }
            }
            that._error(err);
        });
    }

    header(uploadInfo, success, error)
    {
        this.oss.get(uploadInfo.object).then(function(result) {
            success(result);
        }).catch(function(err) {
            error(err);
        });
    }

    _progress(percentage, checkpoint,res)
    {
        this._callback.onprogress(this._uploadInfo,{
        loaded: percentage,
        total: this._uploadInfo.file.size, 
        checkpoint:checkpoint}, res);
    }

    _error(errorInfo)
    {
        this._callback.onerror(this._uploadInfo,errorInfo);
    }

    _complete(result)
    {
        this._callback.oncomplete(this._uploadInfo, result);
    }
}