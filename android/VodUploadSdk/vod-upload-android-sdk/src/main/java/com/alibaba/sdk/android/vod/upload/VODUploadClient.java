/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload;

import com.alibaba.sdk.android.vod.upload.common.VodUploadStateType;
import com.alibaba.sdk.android.vod.upload.model.FilePartInfo;
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;
import com.alibaba.sdk.android.vod.upload.model.VodInfo;
import com.alibaba.sdk.android.vod.upload.session.VodHttpClientConfig;

import java.util.List;

/**
 * Vod点播上传客户端接口
 */
public interface VODUploadClient {
    /**
     * 初始化
     * @param callback 设置回调
     */
    void init(VODUploadCallback callback);

    /**
     * 初始化:AK方式上传
     *  @param callback 设置回调
     */
    void init(String accessKeyId, String accessKeySecret, VODUploadCallback callback);

    /**
     * 初始化:STS方式上传
     * @param accessKeyId //临时accessKeyId
     * @param accessKeySecret//临时accessKeySecret
     * @param secrityToken//临时securityToken
     * @param expireTime//STStoken过期时间
     * @param callback//上传的监听
     */
    void init(String accessKeyId, String accessKeySecret, String secrityToken,
                 String expireTime, VODUploadCallback callback);

    /**
     * 设置分片大小
     */
    void setPartSize(long partSize);

    /**
     * 是否开启转码
     * @param bool
     */
    void setTranscodeMode(boolean bool);

    /**
     * 指定视频存储区域
     * @param storageLocation
     */
    void setStorageLocation(String storageLocation);

    /**
     * 指定转码模板
     * @param templateGroupId
     */
    void setTemplateGroupId(String templateGroupId);

    /**
     *
     * @param vodHttpClientConfig
     */
    void setVodHttpClientConfig(VodHttpClientConfig vodHttpClientConfig);

    /**
     * 点播服务使用
     * 添加上传文件。
     *@param filePath 文件地址(/sdcard/xxx/xxx)或者Uri文件路径(content://xxxx/xxxx)
     * @param vodInfo 点播VodInfo
     */
    void addFile(String filePath, VodInfo vodInfo);

    /**
     * 分片上传使用
     * @param localFilePath 文件地址
     * @param vodInfo 点播VodInfo
     * @param partInfoList 片段信息
     */
    void addFile(String localFilePath, VodInfo vodInfo, List<FilePartInfo> partInfoList);

    /**
     * 添加文件
     * @param filePath 文件地址(/sdcard/xxx/xxx)或者Uri文件路径(content://xxxx/xxxx)
     * @param endpoint 访问域名
     * @param bucket 存储空间
     * @param object 文件名
     */
    void addFile(String filePath, String endpoint, String bucket,
                    String object);

    /**
     * 添加上传文件
     * @param filePath 文件地址(/sdcard/xxx/xxx)或者Uri文件路径(content://xxxx/xxxx)
     * @param endpoint 访问域名
     * @param bucket 存储空间
     * @param object 文件名
     * @param vodInfo 点播VodInfo
     */
    void addFile(String filePath, String endpoint, String bucket,
                 String object, VodInfo vodInfo);

    /**
     * 删除上传文件。
     */
    void deleteFile(int index);

    /**
     * 清除上传列表。
     */
    void clearFiles();

    /**
     * 获取上传列表。
     */
    List<UploadFileInfo> listFiles();

    /**
     * 取消单个文件上传，文件保留在列表中。
     */
    void cancelFile(int index);

    /**
     * 恢复已取消的上传文件。
     */
    void resumeFile(int index);


    /**
     * 开始上传。
     */
    void start();

    /**
     * 停止上传。
     */
    void stop();

    /**
     * 暂停上传。
     */
    void pause();

    /**
     * 恢复上传。
     */
    void resume();

    /**
     * 超时恢复上传。
     */
    void resumeWithAuth(String uploadAuth);

    /**
     * 超时恢复上传。
     */
    void resumeWithToken(String accessKeyId, String accessKeySecret, String secrityToken,
                         String expireTime);

    /**
     * 获取上传状态。
     */
    VodUploadStateType getStatus();

    /**
     * 超时恢复上传。
     */
    void setUploadAuthAndAddress(UploadFileInfo uploadFileInfo,
                                 String uploadAuth, String uploadAddress);

    /**
     * 配置上传地址region，默认为 cn-shanghai，不传则使用默认
     * @param region
     */
    void setRegion(String region);

    /**
     * 应用ID。取值如：app-1000000。
     * 使用说明参考文档https://help.aliyun.com/document_detail/113600.html
     */
    void setAppId(String appId);

    /**
     * 工作流ID。注意：如果同时传递了WorkflowId和TemplateGroupId，以WorkflowId为准。
     * 使用说明参考文档https://help.aliyun.com/document_detail/115347.html
     */
    void setWorkflowId(String workflowId);

    /**
     * 是否开启断点续传，默认为开启
     * @param enabled
     */
    void setRecordUploadProgressEnabled(boolean enabled);
}
