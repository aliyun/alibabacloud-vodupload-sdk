/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.aliyun.vod.qupaiokhttp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

class Utils {

    public static String getFullUrl(String url, List<Part> params, boolean urlEncoder) {
        StringBuffer urlFull = new StringBuffer();
        urlFull.append(url);
        if (urlFull.indexOf("?", 0) < 0 && params.size() > 0) {
            urlFull.append("?");
        }
        int flag = 0;
        for (Part part : params) {
            String key = part.getKey();
            String value = part.getValue();
            if (urlEncoder) {
                //只对key和value编码
                try {
                    key = URLEncoder.encode(key, "UTF-8");
                    value = URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            urlFull.append(key).append("=").append(value);
            if (++flag != params.size()) {
                urlFull.append("&");
            }
        }

        return urlFull.toString();
    }
}
