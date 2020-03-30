//Copyright (C) 2020 Alibaba Group Holding Limited
export default class Cookie
{
	static get(cname) {
        var name = cname + '';
        var ca = document.cookie.split(';');
        for(var i = 0; i < ca.length; i++) {
            var c = ca[i].trim();
            if(c.indexOf(name) == 0) {
                return unescape(c.substring(name.length + 1,c.length));
            }
        }
        return '';
    };

    static set(cname, cvalue, exdays) {
        var d = new Date();
        d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
        var expires = 'expires=' + d.toGMTString();
        document.cookie = cname + '=' + escape(cvalue) + '; ' + expires;
    };
}