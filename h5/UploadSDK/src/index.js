//Copyright (C) 2020 Alibaba Group Holding Limited
import Vod from './upload/vod';
const aliUpload = {
	  		Vod:Vod
	  	};
// AMD
if (typeof define === 'function' && define['amd']) {
	  define([], function(){ return aliUpload ;});
// commonjs, 支持browserify
} else if (typeof exports === 'object' && typeof module === 'object') {
	  module['exports'] = aliUpload;
}
window.AliyunUpload = aliUpload;

// export default Vod;
