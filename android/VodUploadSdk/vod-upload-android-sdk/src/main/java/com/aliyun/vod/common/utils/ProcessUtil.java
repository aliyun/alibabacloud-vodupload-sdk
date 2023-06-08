package com.aliyun.vod.common.utils;

import android.os.Looper;

/**
 * @ClassName: ProcessUtil
 * @Author: fengming.fm
 * @CreateDate: 2018/11/16 下午2:38
 * @Description: 类作用描述
 * @Version: 1.0
 */
public class ProcessUtil {

    public static final boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
