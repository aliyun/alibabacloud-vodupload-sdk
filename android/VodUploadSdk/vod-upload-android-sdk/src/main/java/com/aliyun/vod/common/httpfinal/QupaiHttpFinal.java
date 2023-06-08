/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.vod.common.httpfinal;

import android.text.TextUtils;
import android.util.Log;

import com.aliyun.vod.common.global.AliyunTag;
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
import javax.net.ssl.HttpsURLConnection;
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
                    Log.d(AliyunTag.TAG, "X509TrustManager checkClientTrusted: " + (chain == null ? "null" : chain.length));
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    checkServerTrusted(chain, authType, null);
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType, String host) throws CertificateException {
                    if (chain == null) {
                        Log.e(AliyunTag.TAG, "X509TrustManager checkServerTrusted: X509Certificate is null");
                        throw new IllegalArgumentException("X509TrustManager checkServerTrusted: X509Certificate is null");
                    }
                    try {
                        if (chain.length > 0) {
                            chain[0].checkValidity();
                        }
                        Log.d(AliyunTag.TAG, "X509TrustManager checkServerTrusted: checkValidity " + chain.length);
                    } catch (Exception e) {
                        Log.e(AliyunTag.TAG, "X509TrustManager checkServerTrusted: checkValidity exception " + e.getMessage());
                    }
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    Log.d(AliyunTag.TAG, "X509TrustManager getAcceptedIssuers:");
                    return new X509Certificate[0];
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            builder.setSSLSocketFactory(sc.getSocketFactory(), trustManager);
            builder.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    if (!TextUtils.isEmpty(hostname)) {
                        HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
                        try {
                            boolean result = hostnameVerifier.verify(hostname, session);
                            Log.d(AliyunTag.TAG, "HostnameVerifier verify true, default verify " + result);
                        } catch (Exception exception) {
                            Log.d(AliyunTag.TAG, "HostnameVerifier verify true, default exception " + exception.getMessage());
                        }
                        return true;
                    }
                    Log.d(AliyunTag.TAG, "HostnameVerifier verify false");
                    return false;
                }
            });
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

}
