//Copyright (C) 2020 Alibaba Group Holding Limited
// var CryptoJS = require("crypto-js");
// var jsrsasign = require('jsrsasign');
var hmacSHA1 = require('crypto-js/hmac-sha1');
var base64 = require('crypto-js/enc-base64');
var utf8 = require('crypto-js/enc-utf8');

export default class Signature  
{
  static randomUUID() {
    var s = [];
    var hexDigits = "0123456789abcdef";
    for (var i = 0; i < 36; i++) {
      s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
    }
    s[14] = "4"; // bits 12-15 of the time_hi_and_version field to 0010
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1); // bits 6-7 of the clock_seq_hi_and_reserved to 01
    s[8] = s[13] = s[18] = s[23] = "-";

    var uuid = s.join("");
    return uuid;
  }

  static aliyunEncodeURI(input) {
    var output = encodeURIComponent(input);

    output = output
      .replace(/\+/g, "%20")
      .replace(/\*/g, "%2A")
      .replace(/%7E/g, "~")
      .replace(/!/g, "%21")
      .replace(/\(/g, "%28")
      .replace(/\)/g, "%29")
      .replace(/'/g, "%27");

    return output;
  }

  static makeUTF8sort(ary, str1, str2) {
    if (!ary) {
      throw new Error('PrismPlayer Error: vid should not be null!');
    };
    var keys = [];
    for(var key in ary)
    {
      keys.push(key);
    }
    var pbugramsdic = keys.sort();
    var outputPub = "",length = pbugramsdic.length;
    for (var key=0;key<length;key++) {

      var a3 = Signature.aliyunEncodeURI(pbugramsdic[key]);
      var b3 = Signature.aliyunEncodeURI(ary[pbugramsdic[key]]);

      if (outputPub == "") {

        outputPub = a3 + str1 + b3;
      } else {
        outputPub += str2 + a3 + str1 + b3;
      }
    }
    return outputPub;
  }

  //signature
  static makeChangeSiga(obj, secStr) {
    if (!obj) {
      throw new Error('PrismPlayer Error: vid should not be null!');
    };
    return base64.stringify(hmacSHA1('GET&' + Signature.aliyunEncodeURI('/') + '&' + Signature.aliyunEncodeURI(Signature.makeUTF8sort(obj, '=', '&')), secStr + '&'));
  }
}





