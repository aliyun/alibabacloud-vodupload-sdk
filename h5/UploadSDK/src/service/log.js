//Copyright (C) 2020 Alibaba Group Holding Limited
import guid from './guid';
import cookie from './cookie';
import config from '../config';
import UA from './ua';

export default class Log
{
	constructor(props) {
		var osName = UA.os.name,
        osVersion = UA.os.version || "",
        exName = UA.browser.name,
        exVersion = UA.browser.version || "";
        var address = window.location.href,
        app_n = "";
        if(address)
        {
            app_n = UA.getHost(address);
        }
        var tt = "pc";
        if (UA.os.ipad) {
            tt = "pad";
        } else if (UA.os.iphone || UA.os.android) {
            tt = "phone";
        }
        this._ri = guid.create();
        this.initParam ={
            APIVersion:'0.6.0',
            lv:'1',
            av:config.version,
            pd:'upload',
            sm:'upload',
            md:'uploader',
            uuid:Log.getUuid(),
            os: osName,
            ov: osVersion,
            et: exName,
            ev: exVersion,
            uat: navigator.userAgent,
            app_n:app_n,
            tt:tt,
            dm:'h5',
            ut:"",
        }	
	}

     /**
     * 唯一表示播放器的id缓存在cookie中
     */
    static getUuid() {
        // p_h5_u表示prism_h5_uuid
        var uuid = cookie.get('p_h5_upload_u');

        if (!uuid) {
            uuid = guid.create();
            cookie.set('p_h5_upload_u', uuid, 730);
        }

        return uuid;
    }

    static getClientId()
    {
         var uuid = cookie.get('p_h5_upload_clientId');
         return uuid;
    }

     /**
     * 唯一表示播放器的id缓存在cookie中
     */
    static setClientId(id) {
        if (!id) {
            id = guid.create();
        }
        cookie.set('p_h5_upload_clientId', id, 730);
        return id;
    }


    /**
	 * jsonp请求
	 */
    log(e,params) {
    	if(params && params.ri)
    	{
    		this._ri = params.ri;
    		delete params.ri;
    	}
    	else
    	{
    		this._ri = guid.create();
    	}
        if(params && params.ut)
        {
            this.initParam.ut = params.ut;
            delete  params.ut;
        }
        this.initParam.t =  new Date().getTime();
        this.initParam.ll = (e =='20006' ? 'error' : 'info');
        this.initParam.ri = this._ri;
        this.initParam.e = e;
        var vargs = [];
        if(params)
        {
            for(var key in params)
            {
                vargs.push(key+'='+params[key]);
            }
        }
        var argsStr = vargs.join('&');
        this.initParam.args= encodeURIComponent(argsStr==""?"0":argsStr);
        var paramsArray = [];
        for(var key in this.initParam)
        {
            paramsArray.push(key+'='+this.initParam[key]);
        }
        var paramsString = paramsArray.join('&');
        if(AliyunUpload && AliyunUpload.__logTestCallback__)
        {
            AliyunUpload.__logTestCallback__(paramsString);
        }
        else
        {
            var img = new Image(0,0);
            img.src = 'https://videocloud.cn-hangzhou.log.aliyuncs.com/logstores/upload/track?' +paramsString;
        }
    }
}