//Copyright (C) 2020 Alibaba Group Holding Limited
export default class Util
{
	static  detectIEVersion() {
        var v = 4,
            div = document.createElement('div'),
            all = div.getElementsByTagName('i');
        while (
            div.innerHTML = '<!--[if gt IE ' + v + ']><i></i><![endif]-->',
                all[0]
            ) {
            v++;
        }
        return v > 4 ? v : false;
    };

    static extend(dst, src) {
        for (var i in src) {
            if (Object.prototype.hasOwnProperty.call(src, i) && src[i]) {
                dst[i] = src[i];
            }
        }
    };

    static isArray(arr){
	  return Object.prototype.toString.call(arg) === '[object Array]';
	}

	static getFileType(url)
	{
		url = url.toLowerCase();
		if(/.mp4|.flv|.m3u8|.avi|.rm|.rmvb|.mpeg|.mpg|.mov|.wmv|.3gp|.asf|.dat|.dv|.f4v|.gif|.m2t|.m4v|.mj2|.mjpeg|.mpe|.mts|.ogg|.qt|.swf|.ts|.vob|.wmv|.webm/.test(url))
		{
			return 'video';
		}
		else if(/.mp3|.wav|.ape|.cda|.au|.midi|.mac|.aac|.ac3|.acm|.amr|.caf|.flac|.m4a|.ra|.wma/.test(url))
		{
			return 'audio';
		}
		else if(/.bmp|.jpg|.jpeg|.png/.test(url))
		{
			return 'img';
		}
		else
		{
			return 'other';
		}
	}

	static isImage(url)
	{
		url = url.toLowerCase();
		if(/.jpg|.jpeg|.png/.test(url))
			return true;

		return false;
	}

	static ISODateString(d) {
	    function pad(n) {
	      return n < 10 ? '0' + n : n
	    }
	    return d.getUTCFullYear() + '-' + pad(d.getUTCMonth() + 1) + '-' + pad(d.getUTCDate()) + 'T' + pad(d.getUTCHours()) + ':' + pad(d.getUTCMinutes()) + ':' + pad(d.getUTCSeconds()) + 'Z'
	}

	static isIntNum(val){
	    var regPos = /^\d+$/; // 非负整数
	    if(regPos.test(val)){
	        return true;
	    }else{
	        return false;
	    }
    }
}


