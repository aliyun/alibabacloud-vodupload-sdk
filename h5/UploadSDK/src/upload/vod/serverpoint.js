//Copyright (C) 2020 Alibaba Group Holding Limited
import io from '../../service/io';
import UA from '../../service/ua';
import Log from '../../service/log';
import config from '../../config';
import util from '../../service/util';
import signature from './signature';
var MD5 = require('crypto-js/md5');
var hex = require('crypto-js/enc-hex');
var utf8 = require('crypto-js/enc-utf8');
const secretKey = 'f#Ylm&^1TppeRhLg'

export default class ServerPoint
{
	static getAuthInfo(userId, clientId, timestamp)
	{
		let str = `${userId}|${secretKey}|${timestamp}`;
		if(clientId)
		{
			str = `${userId}|${clientId}|${secretKey}|${timestamp}`;
		}

		let mdsStr = MD5(utf8.parse(str));
		return mdsStr.toString(hex);
	}

	static upload(params,success,failed)
	{ 
		var timestamp = util.ISODateString(new Date());
		var authTimestamp = Math.floor(new Date().valueOf()/1000);
        var clientId = Log.getClientId();
        clientId = Log.setClientId(clientId);
		var authInfo = ServerPoint.getAuthInfo(params.userId,clientId,authTimestamp);
		var SignatureNonceNum = signature.randomUUID();
        var SignatureMethodT = 'HMAC-SHA1';
		var newAry = {
		    'Source': 'WebSDK',
		    'BusinessType': 'UploadVideo',
		    'Action': 'ReportUploadProgress',
		    'TerminalType': 'H5',
		    'DeviceModel': UA.browser.name + (UA.browser.version || ""),
		    'AppVersion': config.version,
		    'AuthTimestamp': authTimestamp,
		    'Timestamp':timestamp,
		    'AuthInfo':authInfo,
		    'FileName':params.file.name,
		    'FileSize':params.file.size,
		    'FileCreateTime':params.file.lastModified,
		    'FileHash':params.fileHash,
		    'UploadId':params.checkpoint.checkpoint.uploadId,
		    'PartSize':params.checkpoint.checkpoint.partSize,
		    'DonePartsCount':params.checkpoint.checkpoint.doneParts.length,
		    'UploadPoint':JSON.stringify(params.checkpoint),
		    'UploadRatio':params.checkpoint.loaded,
		    'UserId':params.userId,
		    'VideoId':params.videoId,
		    'Version': '2017-03-21',
		    //'Version':'2017-03-14',
		    'Format': 'JSON',
		    'SignatureMethod': SignatureMethodT,
		    'SignatureVersion': '1.0',
		    'SignatureNonce': SignatureNonceNum
		}
		if(clientId)
		{
			newAry['ClientId'] = clientId;
		}
		
		var pbugramsdic = signature.makeUTF8sort(newAry, '=', '&') + '&Signature=' + signature.aliyunEncodeURI(signature.makeChangeSiga(newAry, params.accessKeySecret));

		var httpUrlend = 'https://vod.'+params.region + '.aliyuncs.com/?' + pbugramsdic;

		io.get(httpUrlend, function(data) {
		        if (success) {
		          success();
		        }
		    },
		    function(errorText) {
		      if (errorText) {
		      	failed(errorText);
		      	console.log(errorText);
		      }
		});
	}

	static get(params, success,failed){
		var timestamp = util.ISODateString(new Date());
		var authTimestamp = Math.floor(new Date().valueOf()/1000);
		var clientId = Log.getClientId();
		var authInfo = ServerPoint.getAuthInfo(params.userId,clientId,authTimestamp);
		var SignatureNonceNum = signature.randomUUID();
        var SignatureMethodT = 'HMAC-SHA1';
		var newAry = {
		    'Source': 'WebSDK',
		    'BusinessType': 'UploadVideo',
		    'Action': 'GetUploadProgress',
		    'TerminalType': 'H5',
		    'DeviceModel': UA.browser.name + (UA.browser.version || ""),
		    'AppVersion': config.version,
		    'AuthTimestamp': authTimestamp,
		    'Timestamp':timestamp,
		    'AuthInfo':authInfo,
		    'UserId':params.userId,
		    'UploadInfoList':JSON.stringify(params.uploadInfoList),
		    'Version': '2017-03-21',
		    //'Version':'2017-03-14',
		    'Format': 'JSON',
		    'SignatureMethod': SignatureMethodT,
		    'SignatureVersion': '1.0',
		    'SignatureNonce': SignatureNonceNum
		}

		if(clientId)
		{
			newAry['ClientId'] = clientId;
		}
		
		var pbugramsdic = signature.makeUTF8sort(newAry, '=', '&') + '&Signature=' + signature.aliyunEncodeURI(signature.makeChangeSiga(newAry, params.accessKeySecret));

		var httpUrlend = 'https://vod.'+params.region + '.aliyuncs.com/?' + pbugramsdic;

		io.get(httpUrlend, function(data) {
				var progress = {},
				cid = clientId;
				data = data ? JSON.parse(data) : {};
				if(data.UploadProgress && data.UploadProgress.UploadProgressList && data.UploadProgress.UploadProgressList.length  > 0)
				{
					progress = data.UploadProgress.UploadProgressList[0]
					cid = progress.ClientId;
				}
				Log.setClientId(cid);
		        if (success) {
		          success(progress);
		        }
		    },
		    function(errorText) {
		      if (errorText) {
		      	failed(errorText);
		      	console.log(errorText);
		      }
		});
	}
}