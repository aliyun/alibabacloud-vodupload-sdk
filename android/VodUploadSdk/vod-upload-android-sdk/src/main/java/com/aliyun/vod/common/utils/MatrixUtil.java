/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.vod.common.utils;

import android.graphics.Matrix;

public class MatrixUtil {
    public static float[] getTransform(Matrix m) {
        float[] transform = new float[9];
        m.getValues(transform);
        return transform;
    }
}
