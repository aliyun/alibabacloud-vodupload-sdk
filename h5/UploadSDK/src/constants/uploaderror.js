//Copyright (C) 2020 Alibaba Group Holding Limited
export default class VODUploadError 
{

   static get CODE()
   {
        return {
            SUCCESS: "Successful",
            EmptyValue: "InvalidParameter.EmptyValue",
            STSInvalid: "InvalidParameter.TokenInvalid",
            ReadFileError: "ReadFileError",
            FILEDUPLICATION: "FileDuplication",
            UploadALEADRYSTARTED: "UploadAlearyStarted"

        }
    }

    static get MESSAGE(){
        return {
            SUCCESS: "Successful",
            EmptyValue: "参数 {0} 不能为空。",
            STSInvalid: "STS参数非法， accessKeyId、accessKeySecret、secretToken、expireTime都不能为空。",
            ReadFileError: "读取文件{0}{1}失败.",
            FILEDUPLICATION: "文件重复添加 {0}",
            UploadALEADRYSTARTED: "重复开始."
            }
    }
    
    static format(code) {
        if (arguments.length < 2) {
            return null;
        }

        var str = arguments[1];
        for (var i = 1; i < arguments.length; i++) {
            var re = new RegExp('\\{' + (i - 1) + '\\}', 'gm');
            str = str.replace(re, arguments[i + 1]);
        }

        return {"code": code, "message": str};
    }
}