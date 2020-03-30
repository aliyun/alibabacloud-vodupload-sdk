/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload.session;

/**
 * Created by Mulberry on 2017/11/15.
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

    public static class Builder {
        int _MaxRetryCount = Integer.MAX_VALUE;
        int _ConnectionTimeout = 15 * 1000;
        int _SocketTimeout = 15 * 1000;

        public Builder() {
        }

        public VodHttpClientConfig.Builder setMaxRetryCount(int maxRetryCount) {
            if (maxRetryCount > 0) {
                this._MaxRetryCount = maxRetryCount;
                return this;
            } else {
                this._MaxRetryCount = 2;
                return this;
            }
        }

        public VodHttpClientConfig.Builder setConnectionTimeout(int _ConnectionTimeout) {
            this._ConnectionTimeout = _ConnectionTimeout;
            return this;
        }

        public VodHttpClientConfig.Builder setSocketTimeout(int _SocketTimeout) {
            this._SocketTimeout = _SocketTimeout;
            return this;
        }

        public VodHttpClientConfig build() {
            return new VodHttpClientConfig(this);
        }
    }
}
