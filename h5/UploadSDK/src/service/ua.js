 //Copyright (C) 2020 Alibaba Group Holding Limited
 // make available to unit tests
var getOSName = function(os)
  {
    var sUserAgent = navigator.userAgent;
    var operator = "other";
    if(!!os.ios)
    {
      return 'iOS';
    }
    if(!!os.android)
    {
      return 'android';
    }
    if(sUserAgent.indexOf('Baiduspider')>-1)
    {
      return 'Baiduspider';
    }
    if(sUserAgent.indexOf('PlayStation')>-1)
    {
      return 'PS4';
    }
    var isWin = (navigator.platform == "Win32") || (navigator.platform == "Windows") || sUserAgent.indexOf('Windows') > -1;
      var isMac = (navigator.platform == "Mac68K") || (navigator.platform == "MacPPC") || (navigator.platform == "Macintosh") || (navigator.platform == "MacIntel");
      if (isMac) operator = "macOS";
      var isUnix = (navigator.platform == "X11") && !isWin && !isMac;
      if (isUnix) operator =  "Unix";
      var isLinux = (String(navigator.platform).indexOf("Linux") > -1);
      if (isLinux) operator =  "Linux";
      if (isWin) {
          return "windows";
      }
      return operator;
  }


var getWinVersion= function()
  {
      var sUserAgent = navigator.userAgent;
      var operator = "";
      var isWin2K = sUserAgent.indexOf("Windows NT 5.0") > -1 || sUserAgent.indexOf("Windows 2000") > -1;
      if (isWin2K) operator =  "2000";
      var isWinXP = sUserAgent.indexOf("Windows NT 5.1") > -1 || sUserAgent.indexOf("Windows XP") > -1;
      if (isWinXP) operator =  "XP";
      var isWin2003 = sUserAgent.indexOf("Windows NT 5.2") > -1 || sUserAgent.indexOf("Windows 2003") > -1;
      if (isWin2003) operator =  "2003";
      var isWinVista= sUserAgent.indexOf("Windows NT 6.0") > -1 || sUserAgent.indexOf("Windows Vista") > -1;
      if (isWinVista) operator =  "Vista";
      var isWin7 = sUserAgent.indexOf("Windows NT 6.1") > -1 || sUserAgent.indexOf("Windows 7") > -1;
      if (isWin7) operator =  "7";
      var isWin8 = sUserAgent.indexOf("Windows NT 6.2") > -1 || sUserAgent.indexOf("Windows 8") > -1;
      if (isWin8) operator =  "8";
      var isWin81 = sUserAgent.indexOf("Windows NT 6.3") > -1 || sUserAgent.indexOf("Windows 8.1") > -1;
      if (isWin81) operator =  "8.1";
      var isWin10 = sUserAgent.indexOf("Windows NT 10") > -1 || sUserAgent.indexOf("Windows 10") > -1;
      if (isWin10) operator =  "10";

      return operator;
  }


var getBrowserType = function(browser)
  {
     var UserAgent = navigator.userAgent.toLowerCase();
     if(!!browser.chrome)
     {
       return "Chrome";
     }
     else if(!!browser.firefox)
     {
       return "Firefox";
     }
     else if(!!browser.safari)
     {
       return "Safari";
     }
     else if(!!browser.webview)
     {
       return "webview";
     }
     else if(!!browser.ie)
     {
        if(/edge/.test(UserAgent))
          return "Edge";
        return "IE";
     }
     else if(/baiduspider/.test(UserAgent))
     {
       return 'Baiduspider';
     }
      else if(/ucweb/.test(UserAgent) || /UCBrowser/.test(UserAgent))
     {
       return 'UC';
     }
     else if(/opera/.test(UserAgent))
     {
       return "Opera";
     }
     else if(/ucweb/.test(UserAgent))
     {
       return 'UC';
     }
     else if(/360se/.test(UserAgent))
     {
       return "360浏览器";
     }
     else if(/bidubrowser/.test(UserAgent))
     {
       return "百度浏览器";
     }
     else if(/metasr/.test(UserAgent))
     {
       return "搜狗浏览器";
     }
     else if(/lbbrowser/.test(UserAgent))
     {
       return "猎豹浏览器";
     }
     else if(/micromessenger/.test(UserAgent))
     {
       return "微信内置浏览器";
     }
     else if(/qqbrowser/.test(UserAgent))
     {
       return "QQ浏览器";
     }
     else if(/playstation/.test(UserAgent))
    {
      return 'PS4浏览器';
    }
  }

var sysInfo = (function(){
        var os = {}, browser ={},
           ua = navigator.userAgent, 
           platform = navigator.platform,
          webkit = ua.match(/Web[kK]it[\/]{0,1}([\d.]+)/),
          android = ua.match(/(Android);?[\s\/]+([\d.]+)?/),
          osx = !!ua.match(/\(Macintosh\; Intel /),
          ipad = ua.match(/(iPad).*OS\s([\d_]+)/),
          ipod = ua.match(/(iPod)(.*OS\s([\d_]+))?/),
          iphone = !ipad && ua.match(/(iPhone\sOS)\s([\d_]+)/),
          webos = ua.match(/(webOS|hpwOS)[\s\/]([\d.]+)/),
          win = /Win\d{2}|Windows/.test(platform),
          wp = ua.match(/Windows Phone ([\d.]+)/),
          touchpad = webos && ua.match(/TouchPad/),
          kindle = ua.match(/Kindle\/([\d.]+)/),
          silk = ua.match(/Silk\/([\d._]+)/),
          blackberry = ua.match(/(BlackBerry).*Version\/([\d.]+)/),
          bb10 = ua.match(/(BB10).*Version\/([\d.]+)/),
          rimtabletos = ua.match(/(RIM\sTablet\sOS)\s([\d.]+)/),
          playbook = ua.match(/PlayBook/),
          chrome = ua.match(/Chrome\/([\d.]+)/) || ua.match(/CriOS\/([\d.]+)/),
          firefox = ua.match(/Firefox\/([\d.]+)/),
          firefoxos = ua.match(/\((?:Mobile|Tablet); rv:([\d.]+)\).*Firefox\/[\d.]+/),
          ie = ua.match(/MSIE\s([\d.]+)/) || ua.match(/Trident\/[\d](?=[^\?]+).*rv:([0-9.].)/),
          webview = !chrome && ua.match(/(iPhone|iPod|iPad).*AppleWebKit(?!.*Safari)/),
          safari = webview || ua.match(/Version\/([\d.]+)([^S](Safari)|[^M]*(Mobile)[^S]*(Safari))/)

        // Todo: clean this up with a better OS/browser seperation:
        // - discern (more) between multiple browsers on android
        // - decide if kindle fire in silk mode is android or not
        // - Firefox on Android doesn't specify the Android version
        // - possibly devide in os, device and browser hashes

        if (browser.webkit = !!webkit) browser.version = webkit[1]

        if (android) os.android = true, os.version = android[2]
        if (iphone && !ipod) os.ios = os.iphone = true, os.version = iphone[2].replace(/_/g, '.')
        if (ipad) os.ios = os.ipad = true, os.version = ipad[2].replace(/_/g, '.')
        if (ipod) os.ios = os.ipod = true, os.version = ipod[3] ? ipod[3].replace(/_/g, '.') : null
        if (wp) os.wp = true, os.version = wp[1]
        if (webos) os.webos = true, os.version = webos[2]
        if (touchpad) os.touchpad = true
        if (blackberry) os.blackberry = true, os.version = blackberry[2]
        if (bb10) os.bb10 = true, os.version = bb10[2]
        if (rimtabletos) os.rimtabletos = true, os.version = rimtabletos[2]
        if (playbook) browser.playbook = true
        if (kindle) os.kindle = true, os.version = kindle[1]
        if (silk) browser.silk = true, browser.version = silk[1]
        if (!silk && os.android && ua.match(/Kindle Fire/)) browser.silk = true
        if (chrome) browser.chrome = true, browser.version = chrome[1]
        if (firefox) browser.firefox = true, browser.version = firefox[1]
        if (firefoxos) os.firefoxos = true, os.version = firefoxos[1]
        if (ie) browser.ie = true, browser.version = ie[1];
        if (safari && (osx || os.ios || win || android)) {
          browser.safari = true
          if (!os.ios) browser.version = safari[1]
        }
        if (webview) browser.webview = true;
        if(osx)
        {
          var version = ua.match(/[\d]*_[\d]*_[\d]*/);
          if(version && version.length > 0 && version[0])
          {
            os.version = version[0].replace(/_/g,'.');
          }
        }

        os.tablet = !!(ipad || playbook || (android && !ua.match(/Mobile/)) ||
          (firefox && ua.match(/Tablet/)) || (ie && !ua.match(/Phone/) && ua.match(/Touch/)))
        os.phone  = !!(!os.tablet && !os.ipod && (android || iphone || webos || blackberry || bb10 ||
          (chrome && ua.match(/Android/)) || (chrome && ua.match(/CriOS\/([\d.]+)/)) ||
          (firefox && ua.match(/Mobile/)) || (ie && ua.match(/Touch/))))

        os.pc = !os.tablet && !os.phone

        if(osx)
        {
          os.name ='macOS';
        }
        else if(win)
        {
          os.name  = "windows";
          os.version = getWinVersion();
        }
        else
        {
          os.name = getOSName(os);
        }
        browser.name = getBrowserType(browser);

        return {os:os,browser:browser};
      })();
export default class UA 
{
	static get os()
	{
		return sysInfo.os;
	}

	static get browser()
	{
		var browser = sysInfo.browser;
		if(!browser.name)
		{
			browser.name = getBrowserType();
		}
		return browser;
	}

  static getHost(url){
      var host = "";
      if(typeof url == 'undefined' || url==null || url==""){
         return ""; 
      }
      var index = url.indexOf("//"),
      str = url;
      if(index > -1)
      {
        str = url.substring(index + 2);
      }
      var host = str;
      var arr = str.split("/");
      if(arr && arr.length >0)
      {
        host = arr[0];
      }
      arr = host.split(':');
      if(arr && arr.length >0)
      {
        host = arr[0];
      }
      return host;
    }


}