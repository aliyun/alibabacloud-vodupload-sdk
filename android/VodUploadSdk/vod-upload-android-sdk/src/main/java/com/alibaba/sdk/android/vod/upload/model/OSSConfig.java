/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload.model;

import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;

/**
 * Created by Leigang on 16/6/25.
 */
public class OSSConfig {
    // for sts, request auth
    private String accessKeyIdToVOD;
    private String accessKeySecretToVOD;
    private String secrityTokenToVOD;
    private String expireTimeToVOD;
    // for oss
    private String accessKeyId;
    private String accessKeySecret;
    private String secrityToken;
    private String expireTime;
    private long partSize;
    private String videoId;
    private String uploadAddress;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getSecrityToken() {
        return secrityToken;
    }

    public void setSecrityToken(String secrityToken) {
        this.secrityToken = secrityToken;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }

    public String getAccessKeyIdToVOD() {
        return accessKeyIdToVOD;
    }

    public void setAccessKeyIdToVOD(String accessKeyIdToVOD) {
        this.accessKeyIdToVOD = accessKeyIdToVOD;
    }

    public String getAccessKeySecretToVOD() {
        return accessKeySecretToVOD;
    }

    public void setAccessKeySecretToVOD(String accessKeySecretToVOD) {
        this.accessKeySecretToVOD = accessKeySecretToVOD;
    }

    public String getSecrityTokenToVOD() {
        return secrityTokenToVOD;
    }

    public void setSecrityTokenToVOD(String secrityTokenToVOD) {
        this.secrityTokenToVOD = secrityTokenToVOD;
    }

    public String getExpireTimeToVOD() {
        return expireTimeToVOD;
    }

    public void setExpireTimeToVOD(String expireTimeToVOD) {
        this.expireTimeToVOD = expireTimeToVOD;
    }

    public OSSCredentialProvider getProvider() {
        if (null == this.secrityToken || null == this.expireTime) {
            return new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);
        } else {
            return new OSSFederationCredentialProvider() {
                @Override
                public OSSFederationToken getFederationToken() {
                    return new OSSFederationToken(accessKeyId,
                            accessKeySecret, secrityToken, expireTime);
                }
            };
        }
    }

    public long getPartSize() {
        return partSize;
    }

    public void setPartSize(long partSize) {
        this.partSize = partSize;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoId() {
        return this.videoId;
    }

    public void setUploadAddress(String videoId) {
        this.uploadAddress = videoId;
    }

    public String getUploadAddress() {
        return this.uploadAddress;
    }
}
