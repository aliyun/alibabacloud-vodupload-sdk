//Copyright (C) 2020 Alibaba Group Holding Limited
import  {UPLOADSTATE,UPLOADSTEP} from '../../constants/ossupload';
import UploadError from '../../constants/uploaderror';
import util from '../../service/util';

export default class OssUpload {

	constructor(config) {
		if (!config) {
            // console.log('需要 config');
            return;
        }
        this._config = {
            chunkSize: 1048576    // 1MB
        };

        util.extend(this._config, config);

        if (!this._config.aliyunCredential && !this._config.stsToken) {
            // console.log('需要 stsToken');
            return;
        }

        if (!this._config.endpoint) {
            // console.log('需要 endpoint');
            return;
        }

        var ALY = window.ALY;
        if (this._config.stsToken) {
            this.oss = new ALY.OSS({
                accessKeyId: this._config.stsToken.Credentials.AccessKeyId,
                secretAccessKey: this._config.stsToken.Credentials.AccessKeySecret,
                securityToken: this._config.stsToken.Credentials.SecurityToken,
                endpoint: this._config.endpoint,
                apiVersion: '2013-10-15'
            });
        }
        else {
            this.oss = new ALY.OSS({
                accessKeyId: this._config.aliyunCredential.accessKeyId,
                secretAccessKey: this._config.aliyunCredential.secretAccessKey,
                endpoint: this._config.endpoint,
                apiVersion: '2013-10-15'
            });
        }

        this._uploadInfo = {};
        this._uploadInfo.state = undefined;
        this._uploadInfo.step = undefined;
		
	}
    
    init(options) {
        var onerror = options.onerror;
        var errors = UploadError;

        if (!options) {
            if (typeof onerror == 'function') {
                onerror(errors.format(UploadError.CODE.EmptyValue, UploadError.MESSAGE.EmptyValue, "options"));
            }

            return;
        }

        if (!options.file) {
            if (typeof options.onerror == 'function') {
                onerror(errors.format(UploadError.CODE.EmptyValue, UploadError.MESSAGE.EmptyValue, "file"));
            }
            return;
        }

        if (!options.object) {
            if (typeof options.onerror == 'function') {
                onerror(errors.format(UploadError.CODE.EmptyValue, UploadError.MESSAGE.EmptyValue, "object"));
            }
            return;
        }

        // 去掉 object 开头的 /
        options.object = options.object.replace(new RegExp("^\/"), '');

        this._callback = {};
        this._callback.onerror = options.onerror;
        this._callback.oncomplete = options.oncomplete;
        this._callback.onprogress = options.onprogress;
        this._uploadInfo.file = options.file;
        this._uploadInfo.blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice;
        this._uploadInfo.chunksNum = Math.ceil(options.file.size / this._config.chunkSize);
        this._uploadInfo.currentChunk = 0;
        this._uploadInfo.uploadId = undefined;
        this._uploadInfo.type = undefined;
        this._uploadInfo.multipartMap = {
            Parts: []
        };

        this._uploadInfo.connum = 0;
        this._uploadInfo.object = options.object;
        this._uploadInfo.headers = options.headers;

        this._uploadInfo.state = UPLOADSTATE.INIT;
        this._uploadInfo.step = UPLOADSTEP.INIT;
    }

    oncomplete() {
        if (typeof this._callback.oncomplete == 'function') {
            this._callback.oncomplete(this._uploadInfo.uploadId);
        }
    }

    onprogress() {
        var self = this;
        var multipartMap = self._uploadInfo.multipartMap;

        if (typeof this._callback.onprogress == 'function') {
            var loaded = 0;
            for(var i=0; i<multipartMap.Parts.length; i++) {
                loaded += multipartMap.Parts[i].loaded;
            }

            self._callback.onprogress({
                loaded: loaded,
                total: this._uploadInfo.file.size
            });
        }
    }

    cancelUpload(){
      log('20008',{});
        this._uploadInfo.state = UPLOADSTATE.INTERRUPT;
    }

    createMultipartUpload() {
        var self = this;
        var params = {
            Bucket: self._config.bucket,
            Key: self._uploadInfo.object,
            ContentType: self._uploadInfo.file.type || ''
        };

        self._uploadInfo.state = UPLOADSTATE.START;
        self.oss.createMultipartUpload(params,
            function (err, res) {
                self.onCreateMultipartUpload(err, res);
            });
    };

    onCreateMultipartUpload(err, res) {
        var self = this;
        
        if (this._uploadInfo.state == UPLOADSTATE.INTERRUPT) {
            return;
        }
        
        if (err) {
            log('20006',{code:err.code,message:err.message});
            if (err.code == "NetworkingError") {
                setTimeout(function () {
                    self.createMultipartUpload();
                }, 1000 * 2);

            } else {
                self._callback.onerror(err);
            }
            return;
        }

        self._uploadInfo.uploadId = res.UploadId;

        self._uploadInfo.step = UPLOADSTEP.PART;
        self.loadChunk();
    };

    uploadPart(partNum) {
        var self = this;
        var multipartMap = self._uploadInfo.multipartMap;
        var partParams = {
            Body: multipartMap.Parts[partNum].data,
            Bucket: self._config.bucket,
            Key: self._uploadInfo.object,
            PartNumber: String(partNum + 1),
            UploadId: self._uploadInfo.uploadId
        };
        log('20005',{ft:'video',fs:self._uploadInfo.file.size,ps: self._config.chunkSize,bu:self._config.bucket,ok:self._uploadInfo.object});

        var req = self.oss.uploadPart(partParams, function (err, data) {
            self.onUploadPart(partNum, err, data);
        });

        req.on('httpUploadProgress', function (p) {
            multipartMap.Parts[partNum].loaded = p.loaded;
            self.onprogress();
        });
    }

    onUploadPart(partNum, err, data) {
        var self = this;
        var _uploadInfo = this._uploadInfo;
        var multipartMap = self._uploadInfo.multipartMap;

        if (this._uploadInfo.state == UPLOADSTATE.INTERRUPT) {
            return;
        }

        if (err) {
            log('20006',{code:err.code,message:err.message});
            if (err.code == "NetworkingError") {
                multipartMap.Parts[partNum].loaded = 0;

                setTimeout(function () {
                    self.uploadPart(partNum);
                }, 1000 * 2);
            } else {
                if (self._uploadInfo.state == UPLOADSTATE.INTERRUPT) {
                    return;
                }

                _uploadInfo.state = UPLOADSTATE.INTERRUPT;
                self._callback.onerror(err, data);
            }

            return;
        }

        multipartMap.Parts[partNum].ETag = data.ETag;
        multipartMap.Parts[partNum].loaded = multipartMap.Parts[partNum].data.byteLength;
        delete multipartMap.Parts[partNum].data;
        log('20007');
        if (_uploadInfo.currentChunk < _uploadInfo.chunksNum) {
            self.loadChunk();
        }
        else {
            // finished.
            if (self._uploadInfo.connum == 0 &&
                multipartMap.Parts.length == _uploadInfo.chunksNum) {
                self._uploadInfo.step = UPLOADSTEP.COMPLETE;
                self.completeMultipartUpload();
            }
        }
    };

    completeMultipartUpload() {
        var self = this;
        var multipartMap = self._uploadInfo.multipartMap;
        for (var i in multipartMap.Parts) {
            if (multipartMap.Parts[i].loaded) {
                delete multipartMap.Parts[i].loaded;
            }
        }

        var doneParams = {
            Bucket: self._config.bucket,
            Key: self._uploadInfo.object,
            CompleteMultipartUpload: multipartMap,
            UploadId: self._uploadInfo.uploadId
        };
        util.extend(doneParams, self._uploadInfo.headers);

        this.oss.completeMultipartUpload(doneParams, function (err, res) {
            self.onMultiUploadComplete(err, res);
        });
    };

    onMultiUploadComplete(err, res) {
        var self = this;

        if (this._uploadInfo.state == UPLOADSTATE.INTERRUPT) {
            return;
        }

        if (err) {
            if (typeof self._callback.onerror == 'function') {
                if (err) {
                    if (err.code == "NetworkingError") {
                        setTimeout(function () {
                            self.completeMultipartUpload();
                        }, 1000 * 2);

                    } else {
                        self._callback.onerror(err);
                    }
                } else {
                    console.log("onMultiUploadComplete: error msg is null.");
                }
                return;
            }
            return;
        }

        if (typeof self._callback.oncomplete == 'function') {
            self._uploadInfo.state = UPLOADSTATE.COMPLETE;
            self._callback.oncomplete(res);
        }
    };

    loadChunk() {
        var self = this;
        var _uploadInfo = self._uploadInfo;
        var config = self._config;
        var currentChunk = _uploadInfo.currentChunk;

        var fileReader = new FileReader();
        fileReader.onload = function (e) {
            self.frOnload(currentChunk, e);
        };
        fileReader.onerror = function (e) {
            self.frOnerror(currentChunk, e);
        };

        var start = currentChunk * config.chunkSize;
        var end = ((start + config.chunkSize) >= _uploadInfo.file.size) ? _uploadInfo.file.size : start + config.chunkSize;
        var blobPacket = _uploadInfo.blobSlice.call(_uploadInfo.file, start, end);
        fileReader.readAsArrayBuffer(blobPacket);

        _uploadInfo.currentChunk++;
    }

    // 文件加载完成。
    frOnload(current, e) {
        var self = this;
        var _uploadInfo = self._uploadInfo;

        _uploadInfo.multipartMap.Parts[current] = {
            data: e.target.result,
            PartNumber: current + 1,
            loaded: 0
        };

        if (this._uploadInfo.state == UPLOADSTATE.INTERRUPT) {
            return;
        }

        self.uploadPart(current);
    };

    frOnerror(current, e) {
        var self = this;
        var onerror = self._callback.onerror;
        var errors = self._config.errors;
        var _uploadInfo = self._uploadInfo;
        if (typeof onerror == 'function') {
            onerror(
                errors.format(
                    errors.CODE.ReadFileError, errors.MESSAGE.ReadFileError, _uploadInfo.file.name, current
                )
            );
        }
    };

    resumeUploadWithToken(accessKeyId, accessKeySecret, securityToken) {
        var self = this;
        var multipartMap = self._uploadInfo.multipartMap;

        if (self._uploadInfo.state != UPLOADSTATE.INTERRUPT) {
            return;
        }

        self._config.stsToken.Credentials.AccessKeyId = accessKeyId;
        self._config.stsToken.Credentials.AccessKeySecret = accessKeySecret;
        self._config.stsToken.Credentials.SecurityToken = securityToken;

        var ALY = window.ALY;
        if (self._config.stsToken) {
            self.oss = new ALY.OSS({
                accessKeyId: self._config.stsToken.Credentials.AccessKeyId,
                secretAccessKey: self._config.stsToken.Credentials.AccessKeySecret,
                securityToken: self._config.stsToken.Credentials.SecurityToken,
                endpoint: self._config.endpoint,
                apiVersion: '2013-10-15'
            });
        }
        else {
            self.oss = new ALY.OSS({
                accessKeyId: self._config.aliyunCredential.accessKeyId,
                secretAccessKey: self._config.aliyunCredential.secretAccessKey,
                endpoint: self._config.endpoint,
                apiVersion: '2013-10-15'
            });
        }

        self._uploadInfo.state = UPLOADSTATE.UPLOADING;
        if (self._uploadInfo.step == UPLOADSTEP.INIT) {
            self.createMultipartUpload();
        } else if (self._uploadInfo.step == UPLOADSTEP.PART) {
            for (var i in multipartMap.Parts) {
                if (multipartMap.Parts[i].data) {
                    self.uploadPart(parseInt(i));
                    break;
                }
            }
        } else if (self._uploadInfo.step == UPLOADSTEP.COMPLETE) {
            self.completeMultipartUpload();
        }
    }

    resumeUpload() {
        var self = this;
        var multipartMap = self._uploadInfo.multipartMap;

        if (self._uploadInfo.state != UPLOADSTATE.INTERRUPT) {
            return;
        }

        self._uploadInfo.state = UPLOADSTATE.UPLOADING;
        if (self._uploadInfo.step == UPLOADSTEP.INIT) {
            self.createMultipartUpload();
        } else if (self._uploadInfo.step == UPLOADSTEP.PART) {
            for (var i in multipartMap.Parts) {
                if (multipartMap.Parts[i].data) {
                    self.uploadPart(parseInt(i));
                    break;
                }
            }
        } else if (self._uploadInfo.step == UPLOADSTEP.COMPLETE) {
            self.completeMultipartUpload();
        }
    }



    upload(){
        this._uploadInfo.state = UPLOADSTATE.START;
        this.createMultipartUpload();
    }
}