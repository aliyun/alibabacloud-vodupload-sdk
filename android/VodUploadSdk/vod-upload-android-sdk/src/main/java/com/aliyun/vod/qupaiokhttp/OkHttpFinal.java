/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.aliyun.vod.qupaiokhttp;

import android.text.TextUtils;

import com.aliyun.vod.qupaiokhttp.https.HttpsCerManager;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;

import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.OkHttpClient;

public class OkHttpFinal {

    private OkHttpClient okHttpClient;

    private static OkHttpFinal okHttpFinal;
    private OkHttpFinalConfiguration configuration;

    private OkHttpFinal() {
    }

    public synchronized void init(OkHttpFinalConfiguration configuration) {
        this.configuration = configuration;

        long timeout = configuration.getTimeout();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS);
        if (configuration.getHostnameVerifier() != null) {
            builder.hostnameVerifier(configuration.getHostnameVerifier());
        }

        List<InputStream> certificateList = configuration.getCertificateList();
        if (certificateList != null && certificateList.size() > 0) {
            HttpsCerManager httpsCerManager = new HttpsCerManager(builder);
            httpsCerManager.setCertificates(certificateList);
        }

        CookieJar cookieJar = configuration.getCookieJar();
        if (cookieJar != null) {
            builder.cookieJar(cookieJar);
        }

        if (configuration.getCache() != null) {
            builder.cache(configuration.getCache());
        }

        if (configuration.getAuthenticator() != null) {
            builder.authenticator(configuration.getAuthenticator());
        }
        if (configuration.getCertificatePinner() != null) {
            builder.certificatePinner(configuration.getCertificatePinner());
        }
        builder.followRedirects(configuration.isFollowRedirects());
        builder.followSslRedirects(configuration.isFollowSslRedirects());
        if (configuration.getSslSocketFactory() != null && configuration.getX509TrustManager() != null) {
            builder.sslSocketFactory(configuration.getSslSocketFactory(), configuration.getX509TrustManager());
        }
        if (configuration.getDispatcher() != null) {
            builder.dispatcher(configuration.getDispatcher());
        }
        builder.retryOnConnectionFailure(configuration.isRetryOnConnectionFailure());
        if (configuration.getNetworkInterceptorList() != null) {
            builder.networkInterceptors().addAll(configuration.getNetworkInterceptorList());
        }
        if (configuration.getInterceptorList() != null) {
            builder.interceptors().addAll(configuration.getInterceptorList());
        }

        if (configuration.getProxy() != null) {
            builder.proxy(configuration.getProxy());
        }
        ILogger.DEBUG = configuration.isDebug();
        ILogger.d("OkHttpFinal init...");
        Constants.DEBUG = configuration.isDebug();

        okHttpClient = builder.build();
    }

    public static OkHttpFinal getInstance() {
        if (okHttpFinal == null) {
            okHttpFinal = new OkHttpFinal();
        }
        return okHttpFinal;
    }

    /**
     * 修改公共请求参数信息
     *
     * @param key
     * @param value
     */
    public void updateCommonParams(String key, String value) {
        boolean add = false;
        List<Part> commonParams = configuration.getCommonParams();
        if (commonParams != null) {
            for (Part param : commonParams) {
                if (param != null && TextUtils.equals(param.getKey(), key)) {
                    param.setValue(value);
                    add = true;
                    break;
                }
            }
        }
        if (!add) {
            commonParams.add(new Part(key, value));
        }
    }

    /**
     * 修改公共header信息
     *
     * @param key
     * @param value
     */
    public void updateCommonHeader(String key, String value) {
        Headers headers = configuration.getCommonHeaders();
        if (headers == null) {
            headers = new Headers.Builder().build();
        }
        configuration.commonHeaders = headers.newBuilder().set(key, value).build();
    }

    @Deprecated
    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public OkHttpClient.Builder getOkHttpClientBuilder() {
        return okHttpClient.newBuilder();
    }

    public List<Part> getCommonParams() {
        return configuration.getCommonParams();
    }

    public List<InputStream> getCertificateList() {
        return configuration.getCertificateList();
    }

    public HostnameVerifier getHostnameVerifier() {
        return configuration.getHostnameVerifier();
    }

    public long getTimeout() {
        return configuration.getTimeout();
    }

    public Headers getCommonHeaders() {
        return configuration.getCommonHeaders();
    }
}
