package com.aliyun.vod.common.utils;

import android.util.Log;

/**
 * Copyright (C) 2020 Alibaba Group Holding Limited on 2017/7/4.
 */

public class AliYunMathUtils {
    /**
     * 判定一个整数是否为2的N次方
     * @param i
     * @return
     */
    public static boolean fun(int i){
        return (i > 0) && ((i & (i - 1)) == 0);
    }

    /**
     * 判定一个整数是否为2的N次方，如果不是，转化为比它大的最相近的2的N次方
     * @param i
     * @return
     */
    public static int convertFun(int i) {
        if(fun(i)) {
            return i;
        }
        String binaryStr = Integer.toBinaryString(i);
        Log.d("Math", "the result is : " + binaryStr);
        StringBuilder funStr = new StringBuilder("1");
        funStr.append(String.format("%0"+binaryStr.length()+"d", 0));
        Log.d("Math", "the fun is : " + funStr.toString());
        return Integer.parseInt(funStr.toString(), 2);
    }
}
