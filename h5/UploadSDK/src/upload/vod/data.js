//Copyright (C) 2020 Alibaba Group Holding Limited
import io from '../../service/io';
import signature from './signature';

export default class Data
{
	static refreshUploadAuth(params, success, error)
	{
		var randNum = signature.randomUUID();
	    var SignatureNonceNum = signature.randomUUID();
	    var SignatureMethodT = 'HMAC-SHA1';
	  
		var newAry = {
		    'AccessKeyId': params.accessKeyId,
		    'SecurityToken': params.securityToken,
		    'Action': 'RefreshUploadVideo',
		    'VideoId': params.videoId,
		    'Version': '2017-03-21',
		    'Format': 'JSON',
		    'SignatureMethod': SignatureMethodT,
		    'SignatureVersion': '1.0',
		    'SignatureNonce': SignatureNonceNum,
		    'RequestId':params.requestId
		  }
		
		var pbugramsdic = signature.makeUTF8sort(newAry, '=', '&') + '&Signature=' + signature.aliyunEncodeURI(signature.makeChangeSiga(newAry, params.accessKeySecret));
 
		var httpUrlend = 'https://vod.'+params.region+'.aliyuncs.com/?' + pbugramsdic;

		io.get(httpUrlend, function(data) {
			    var data = JSON.parse(data);
		        if (success) {
		          success(data);
		        }
		    },
		    function(errorText) {
		      if (error) {
		        var arg = JSON.parse(errorText);
		        error(arg);
		      }
		});
	}

	static getUploadAuth(params, success, error)
	{
		var randNum = signature.randomUUID();
	    var SignatureNonceNum = signature.randomUUID();
	    var SignatureMethodT = 'HMAC-SHA1';
	  
		var newAry = {
		    'AccessKeyId': params.accessKeyId,
		    'SecurityToken': params.securityToken,
		    'Action': 'CreateUploadVideo',
		    'Title': params.title,
		    'FileName': params.fileName,
		    'Version': '2017-03-21',
		    'Format': 'JSON',
		    'SignatureMethod': SignatureMethodT,
		    'SignatureVersion': '1.0',
		    'SignatureNonce': SignatureNonceNum,
		    'RequestId':params.requestId
		 }

		if(params.fileSize)
		{
			newAry.FileSize = params.fileSize;
		}
		if(params.description)
		{
			newAry.Description = params.description;
		}

		if(params.cateId)
		{
			newAry.CateId = params.cateId;
		}

		if(params.tags)
		{
			newAry.Tags = params.tags;
		}

		if(params.templateGroupId)
		{
			newAry.TemplateGroupId = params.templateGroupId;
		}

		if(params.storageLocation)
		{
			newAry.StorageLocation = params.storageLocation;
		}

		if(params.coverUrl)
		{
			newAry.CoverURL = params.coverUrl;
		}

		if(params.transCodeMode)
		{
			newAry.TransCodeMode = params.transCodeMode;
		}

		if(params.userData)
		{
			newAry.UserData = JSON.stringify(params.userData);
		}
		if(params.WorkflowId)
		{
			newAry.WorkflowId = params.WorkflowId;
		}
		if(params.AppId){
			newAry.AppId = params.AppId;
		}
		
		var pbugramsdic = signature.makeUTF8sort(newAry, '=', '&') + '&Signature=' + signature.aliyunEncodeURI(signature.makeChangeSiga(newAry, params.accessKeySecret));

		var httpUrlend = 'https://vod.'+params.region+'.aliyuncs.com/?' + pbugramsdic;

		io.get(httpUrlend, function(data) {
			    try
			    {
				    data = JSON.parse(data);
				}catch(e)
				{
					if (error) {
			           error({Code:"GetUploadAuthFailed", Message:"获取uploadauth失败"});
			           return;
			        }
				}
		        if (success) {
		          success(data);
		        }
		    },
		    function(errorText) {
		      if (error) {
			      	var arg = {Code:"GetUploadAuthFailed", Message:"获取uploadauth失败"};
			      	try
			      	{
			           arg = JSON.parse(errorText);
			        }catch(e)
			        {}
			        error(arg);
		      }
		});
	}

	static getImageUploadAuth(params, success, error)
	{
		var randNum = signature.randomUUID();
	    var SignatureNonceNum = signature.randomUUID();
	    var SignatureMethodT = 'HMAC-SHA1';
	  
		var newAry = {
		    'AccessKeyId': params.accessKeyId,
		    'SecurityToken': params.securityToken,
		    'Action': 'CreateUploadImage',
		    'ImageType': (params.imageType ? params.imageType : 'default'),
		    'Version': '2017-03-21',
		    'Format': 'JSON',
		    'SignatureMethod': SignatureMethodT,
		    'SignatureVersion': '1.0',
		    'SignatureNonce': SignatureNonceNum,
		    'RequestId':params.requestId
		  }

		if(params.title)
		{
			newAry.Title = params.title;
		}
		if(params.imageExt)
		{
			newAry.ImageExt = params.imageExt;
		}

		if(params.tags)
		{
			newAry.Tags = params.tags;
		}

		if(params.storageLocation)
		{
			newAry.StorageLocation = params.storageLocation;
		}

		
		var pbugramsdic = signature.makeUTF8sort(newAry, '=', '&') + '&Signature=' + signature.aliyunEncodeURI(signature.makeChangeSiga(newAry, params.accessKeySecret));

		var httpUrlend = 'https://vod.'+params.region+'.aliyuncs.com/?' + pbugramsdic;

		io.get(httpUrlend, function(data) {
			    data = JSON.parse(data);
		        if (success) {
		          success(data);
		        }
		    },
		    function(errorText) {
		      if (error) {
		        var arg = JSON.parse(errorText);
		        error(arg);
		      }
		});
	} 
}
