/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.vod.common.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

public
class BitmapUtil {

    private static final String TAG = "BitmapUtil";

    public static
    boolean writeBitmap(String path, Bitmap bitmap, int w, int h, Bitmap.CompressFormat format, int quality) {
        int orig_w = bitmap.getWidth(), orig_h = bitmap.getHeight();
        Matrix m = new Matrix();
        m.setScale((float) w / orig_w, (float) h / orig_h);
        Bitmap scaled_bitmap = Bitmap.createBitmap(bitmap, 0, 0, orig_w, orig_h, m, true);
        boolean succ = writeBitmap(path, scaled_bitmap, format, quality);
        scaled_bitmap.recycle();
        return succ;
    }

    public static
    boolean writeBitmap(String path, Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        FileOutputStream fout;
        try {
            fout = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "unable to open output file", e);
            return false;
        }

        boolean succ = bitmap.compress(format, quality, fout);

        try {
            fout.close();
        } catch (IOException e) {
            return false;
        }

        return succ;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }


}
