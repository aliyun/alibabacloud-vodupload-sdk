package com.aliyun.vod.log.core;

import com.aliyun.vod.log.struct.AliyunLogKey;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.UUID;

/**
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

public class AliyunLogParam {

    public static String generatePushParams(Map<String, String> args,
                                            String product,
                                            String logLevel,
                                            String module,
                                            String subModule,
                                            int eventId,
                                            String requestID,
                                            String networkType,
                                            String appVersion

    ) {
        StringBuilder params = new StringBuilder("&");
        params.append(AliyunLogKey.KEY_TIME).append("=").append(String.valueOf(System.currentTimeMillis())).append("&");
        params.append(AliyunLogKey.KEY_LOG_LEVEL).append("=").append(logLevel).append("&");
        params.append(AliyunLogKey.KEY_LOG_VERSION).append("=").append(AliyunLogCommon.LOG_LEVEL).append("&");
        params.append(AliyunLogKey.KEY_PRODUCT).append("=").append(product).append("&");
        params.append(AliyunLogKey.KEY_MODULE).append("=").append(module).append("&");
        params.append(AliyunLogKey.KEY_SUB_MODULE).append("=").append(subModule).append("&");
        params.append(AliyunLogKey.KEY_HOSTNAME).append("=").append(getHostIp()).append("&");
        params.append(AliyunLogKey.KEY_BUSINESS_ID).append("=").append("").append("&");
        params.append(AliyunLogKey.KEY_REQUEST_ID).append("=").append(requestID).append("&");
        params.append(AliyunLogKey.KEY_EVENT).append("=").append(String.valueOf(eventId)).append("&");
        params.append(AliyunLogKey.KEY_ARGS).append("=").append(transcodeArgs(args)).append("&");
        params.append(AliyunLogKey.KEY_TERMINAL_TYPE).append("=").append(AliyunLogCommon.TERMINAL_TYPE).append("&");
        params.append(AliyunLogKey.KEY_DEVICE_MODEL).append("=").append(AliyunLogCommon.DEVICE_MODEL).append("&");
        params.append(AliyunLogKey.KEY_OPERATION_SYSTEM).append("=").append(AliyunLogCommon.OPERATION_SYSTEM).append("&");
        params.append(AliyunLogKey.KEY_OSVERSION).append("=").append(AliyunLogCommon.OS_VERSION).append("&");
        params.append(AliyunLogKey.KEY_APP_VERSION).append("=").append(appVersion).append("&");
        params.append(AliyunLogKey.KEY_UUID).append("=").append(AliyunLogCommon.UUID).append("&");
        params.append(AliyunLogKey.KEY_DEFINITION).append("=").append("").append("&");
        params.append(AliyunLogKey.KEY_CONNECTION).append("=").append(networkType).append("&");
        params.append(AliyunLogKey.KEY_USER_AGENT).append("=").append("").append("&");
        params.append(AliyunLogKey.KEY_UI).append("=").append("false").append("&");
        params.append(AliyunLogKey.KEY_APPLICATION_ID).append("=").append(AliyunLogCommon.APPLICATION_ID).append("&");
        params.append(AliyunLogKey.KEY_CDN_IP).append("=").append("").append("&");
        params.append(AliyunLogKey.KEY_REFER).append("=").append("").append("&");
        params.append(AliyunLogKey.KEY_APP_NAME).append("=").append(AliyunLogCommon.APPLICATION_NAME);
        return params.toString();
    }

    public static String transcodeArgs(Map<String, String> args) {
        if (args != null) {
            StringBuilder argsStr = new StringBuilder();
            for (Map.Entry<String, String> entry : args.entrySet()) {
                argsStr.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            argsStr.deleteCharAt(argsStr.lastIndexOf("&"));
            try {
                return URLEncoder.encode(argsStr.toString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
        } else {
            return "";
        }
    }

    /***
     * 获取网关IP地址
     *
     * @return
     */
    public static String getHostIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> ipAddr = intf.getInetAddresses(); ipAddr
                        .hasMoreElements(); ) {
                    InetAddress inetAddress = ipAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
        } catch (Exception e) {
        }
        return null;
    }

    /*生成当前UTC时间戳Time*/
    public static String generateTimestamp() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return df.format(date);
    }

    public static String generateTimestamp(long time) {
        Date date = new Date(time);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return df.format(date);
    }

    public static String generateRandom() {
        String signatureNonce = UUID.randomUUID().toString();
        return signatureNonce;
    }

}
