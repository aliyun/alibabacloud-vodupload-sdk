/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vodupload_demo;

import android.app.Application;

/**
 * @ClassName: VodUploadApplication
 * @Author: fengming.fm
 * @CreateDate: 2018/11/14 下午4:03
 * @Description: 类作用描述
 * @Version: 1.0
 */
public class VodUploadApplication extends Application {

    public static String VOD_REGION = "cn-shanghai";
    public static boolean VOD_RECORD_UPLOAD_PROGRESS_ENABLED = true;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
