/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.aliyun.vod.common.httpfinal;

import com.aliyun.vod.qupaiokhttp.OkHttpFinal;
import com.aliyun.vod.qupaiokhttp.OkHttpFinalConfiguration;
import com.aliyun.vod.qupaiokhttp.Part;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Headers;
import okhttp3.Interceptor;

public class QupaiHttpFinal implements HttpInterface {
    private static QupaiHttpFinal instance;

    public static QupaiHttpFinal getInstance() {
        if (instance == null) {
            synchronized (QupaiHttpFinal.class) {
                if (instance == null) {
                    instance = new QupaiHttpFinal();
                }
            }
        }
        return instance;
    }

    @Override
    public void initOkHttpFinal() {
        List<Part> commomParams = new ArrayList<>();
        Headers commonHeaders = new Headers.Builder().build();

        List<Interceptor> interceptorList = new ArrayList<>();
        OkHttpFinalConfiguration.Builder builder = null;
        builder = new OkHttpFinalConfiguration.Builder()
                .setCommenParams(commomParams)
                .setCommenHeaders(commonHeaders)
                .setTimeout(35000)
                .setInterceptors(interceptorList)
//                    .setCookieJar(CookieJar.NO_COOKIES)
//                    .setCertificates(getResources().getAssets().open("rootca.der"))
//                    .setHostnameVerifier(new SkirtHttpsHostnameVerifier())
                .setDebug(true);
        addHttps(builder);
        OkHttpFinal.getInstance().init(builder.build());
    }

    private static void addHttps(OkHttpFinalConfiguration.Builder builder) {
        try {
            final X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                public void checkServerTrusted(X509Certificate[] chain, String authType, String host) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            builder.setSSLSocketFactory(sc.getSocketFactory(), trustManager);
            builder.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

}
