/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.aliyun.auth.common;

/**
 * 点播视频上传状态
 */
public enum AliyunVodUploadSTATUS {
    VODSVideoStepIdle,//初始状态
    VODSVideoStepCreateImage,//创建图片凭证
    VODSVideoStepCreateImageFinish,//图片凭证创建完成
    VODSVideoStepUploadImage,//上传图片
    VODSVideoStepUploadImageFinish,//图片上传完成
    VODSVideoStepCreateVideo,//创建视频凭证
    VODSVideoStepCreateVideoFinish,//创建视频凭证完成
    VODSVideoStepUploadVideo,//上传视频
    VODSVideoStepUploadCancel,//取消上传
}
