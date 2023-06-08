/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload.session;

import com.alibaba.sdk.android.vod.upload.model.SvideoInfo;

/**
 * 点播上传参数
 */
public class VodSessionCreateInfo {
    private final VodHttpClientConfig vodHttpClientConfig;

    private final String videoPath;
    private final String imagePath;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String securityToken;
    private final String expriedTime;
    private final String requestID;
    private final SvideoInfo svideoInfo;
    private final boolean isTranscode;
    private final long partSize;
    private final String templateGroupId;
    private final String storageLocation;
    private final String appId;
    private final String workFlowId;

    protected VodSessionCreateInfo(VodSessionCreateInfo.Builder builder) {
        this.videoPath = builder._VideoPath;
        this.imagePath = builder._ImagePath;
        this.accessKeyId = builder._AccessKeyId;
        this.accessKeySecret = builder._AccessKeySecret;
        this.securityToken = builder._SecurityToken;
        this.expriedTime = builder._ExpriedTime;
        this.requestID = builder._RequestID;
        this.svideoInfo = builder._SvideoInfo;
        this.isTranscode = builder._IsTranscode;
        this.partSize = builder._PartSize;
        this.vodHttpClientConfig = builder._VodHttpClientConfig;
        this.templateGroupId = builder._TemplateGroupId;
        this.storageLocation = builder._storageLocation;
        this.appId = builder._appId;
        this.workFlowId = builder._workFlowId;
    }


    public VodHttpClientConfig getVodHttpClientConfig() {
        return vodHttpClientConfig;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public String getExpriedTime() {
        return expriedTime;
    }

    public String getRequestID() {
        return requestID;
    }

    public SvideoInfo getSvideoInfo() {
        return svideoInfo;
    }

    public boolean isTranscode() {
        return isTranscode;
    }

    public long getPartSize() {
        return partSize;
    }

    public String getTemplateGroupId() {
        return templateGroupId;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public String getAppId() {
        return appId;
    }

    public String getWorkFlowId() {
        return workFlowId;
    }

    /**
     * 构造器
     */
    public static final class Builder {

        private String _VideoPath;
        private String _ImagePath;
        private String _AccessKeyId;
        private String _AccessKeySecret;
        private String _SecurityToken;
        private String _ExpriedTime;
        private String _RequestID;
        private SvideoInfo _SvideoInfo;
        private boolean _IsTranscode;
        private long _PartSize;
        private String _TemplateGroupId;
        private String _storageLocation;
        private String _appId;
        private String _workFlowId;

        VodHttpClientConfig _VodHttpClientConfig = (new VodHttpClientConfig.Builder()).build();

        public Builder() {
        }

        /**
         * 设置视频路径
         * @param videoPath
         * @return VodSessionCreateInfo.Builder
         */
        public VodSessionCreateInfo.Builder setVideoPath(String videoPath) {
            this._VideoPath = videoPath;
            return this;
        }

        /**
         * 设置图片路径
         * @param imagePath
         * @return VodSessionCreateInfo.Builder
         */
        public VodSessionCreateInfo.Builder setImagePath(String imagePath) {
            this._ImagePath = imagePath;
            return this;
        }

        /**
         * 设置临时accessKeyId
         * @param accessKeyId
         * @return VodSessionCreateInfo.Builder
         */
        public VodSessionCreateInfo.Builder setAccessKeyId(String accessKeyId) {
            this._AccessKeyId = accessKeyId;
            return this;
        }

        /**
         * 设置临时accessKeySecret
         * @param accessKeySecret
         * @return VodSessionCreateInfo.Builder
         */
        public VodSessionCreateInfo.Builder setAccessKeySecret(String accessKeySecret) {
            this._AccessKeySecret = accessKeySecret;
            return this;
        }

        /**
         * 设置 securityToken
         * @param securityToken
         * @return VodSessionCreateInfo.Builder
         */
        public VodSessionCreateInfo.Builder setSecurityToken(String securityToken) {
            this._SecurityToken = securityToken;
            return this;
        }

        /**
         * 设置requestID，开发者可以传将获取STS返回的requestID设置也可以不设
         * @param requestID
         * @return VodSessionCreateInfo.Builder
         */
        public VodSessionCreateInfo.Builder setRequestID(String requestID) {
            this._RequestID = requestID;
            return this;
        }

        /**
         * 设置STStoken过期时间
         * @param expriedTime
         * @return VodSessionCreateInfo.Builder
         */
        public VodSessionCreateInfo.Builder setExpriedTime(String expriedTime) {
            this._ExpriedTime = expriedTime;
            return this;
        }

        /**
         * 设置是否转码，如开启转码请AppSever务必监听服务端转码成功的通知
         * @param isTranscode
         * @return VodSessionCreateInfo.Builder
         */
        public VodSessionCreateInfo.Builder setIsTranscode(Boolean isTranscode) {
            this._IsTranscode = isTranscode;
            return this;
        }

        /**
         * 设置短视频信息
         * @param svideoInfo ,见 {@link SvideoInfo}
         * @return VodSessionCreateInfo.Builder
         */
        public VodSessionCreateInfo.Builder setSvideoInfo(SvideoInfo svideoInfo) {
            this._SvideoInfo = svideoInfo;
            return this;
        }

        public VodSessionCreateInfo.Builder setPartSize(long _PartSize) {
            this._PartSize = _PartSize;
            return this;
        }

        /**
         * 设置网络参数
         * @param httpClientConfig 见{@link VodHttpClientConfig}
         * @return VodSessionCreateInfo.Builder
         */
        public VodSessionCreateInfo.Builder setVodHttpClientConfig(VodHttpClientConfig httpClientConfig) {
            this._VodHttpClientConfig = httpClientConfig;
            return this;
        }

        public VodSessionCreateInfo.Builder setTemplateGroupId(String _TemplateGroupId) {
            this._TemplateGroupId = _TemplateGroupId;
            return this;
        }

        public VodSessionCreateInfo.Builder setStorageLocation(String _storageLocation) {
            this._storageLocation = _storageLocation;
            return this;
        }

        public VodSessionCreateInfo.Builder setAppId(String _appId){
            this._appId = _appId;
            return this;
        }

        public VodSessionCreateInfo.Builder setWorkFlowId(String _workFlowId){
            this._workFlowId = _workFlowId;
            return this;
        }

        public VodSessionCreateInfo build() {
            return new VodSessionCreateInfo(this);
        }
    }

}
