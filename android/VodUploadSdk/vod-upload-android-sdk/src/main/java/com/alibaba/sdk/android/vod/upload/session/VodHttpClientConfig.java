/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload.session;

/**
 * HttpClient配置项
 */
public class VodHttpClientConfig {

    private int maxRetryCount = Integer.MAX_VALUE;
    private int connectionTimeout = 15 * 1000;
    private int socketTimeout = 15 * 1000;

    public static VodHttpClientConfig.Builder builder() {
        return new VodHttpClientConfig.Builder();
    }

    protected VodHttpClientConfig(VodHttpClientConfig.Builder builder) {
        this.maxRetryCount = builder._MaxRetryCount;
        this.connectionTimeout = builder._ConnectionTimeout;
        this.socketTimeout = builder._SocketTimeout;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * 构造器
     */
    public static class Builder {
        int _MaxRetryCount = Integer.MAX_VALUE;
        int _ConnectionTimeout = 15 * 1000;
        int _SocketTimeout = 15 * 1000;

        public Builder() {
        }

        /**
         * 设置最大重试次数，默认是 Integer.MAX_VALUE
         * @param maxRetryCount
         * @return
         */
        public VodHttpClientConfig.Builder setMaxRetryCount(int maxRetryCount) {
            if(maxRetryCount > 0) {
                this._MaxRetryCount = maxRetryCount;
                return this;
            } else {
                this._MaxRetryCount = 2;
                return this;
            }
        }

        /**
         * 设置链接超时时间，默认：15ms
         * @param _ConnectionTimeout 单位：毫秒
         * @return
         */
        public VodHttpClientConfig.Builder setConnectionTimeout(int _ConnectionTimeout) {
            this._ConnectionTimeout = _ConnectionTimeout;
            return this;
        }

        /**
         * 设置Socket超时时间，默认：15ms
         * @param _SocketTimeout
         * @return
         */
        public VodHttpClientConfig.Builder setSocketTimeout(int _SocketTimeout) {
            this._SocketTimeout = _SocketTimeout;
            return this;
        }

        public VodHttpClientConfig build() {
            return new VodHttpClientConfig(this);
        }
    }
}
