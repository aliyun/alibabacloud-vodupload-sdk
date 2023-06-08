/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload.common.utils;


public class StringUtil {
    static public boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }

        if (str.trim().length() == 0) {
            return true;
        }

        return false;
    }
}
