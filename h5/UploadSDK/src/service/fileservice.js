//Copyright (C) 2020 Alibaba Group Holding Limited
var MD5 = require('crypto-js/md5');
var latin1 = require('crypto-js/enc-latin1');
var hex = require('crypto-js/enc-hex');
export default class FileService
{
    static getMd5(file, callback,error)
    {
        var fileReader = new FileReader();
        fileReader.onload = function (e) {
            try
            {
                if(e && e.target)
                {
                    var hash = MD5(latin1.parse(e.target.result));
                    var md5 = hash.toString();
                    callback(md5);
                }
            }catch(e)
            {
                console.log(e);
            }
        };
        fileReader.onerror = function (e) {
            console.log(e);
            errorCallback(e);
        };

        var start = 0
        var end = 1024;
        var blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice;
        var blobPacket = blobSlice.call(file, start, end);
        fileReader.readAsBinaryString(blobPacket);
    }
}