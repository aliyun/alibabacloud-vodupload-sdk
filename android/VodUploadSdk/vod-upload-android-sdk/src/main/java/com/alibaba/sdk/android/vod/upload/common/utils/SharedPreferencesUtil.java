/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.alibaba.sdk.android.vod.upload.model.OSSUploadInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;



public class SharedPreferencesUtil {

    /**
     * @param uploadInfo
     */
    public static void saveUploadInfp(Context context, String preferenceName, String key, OSSUploadInfo uploadInfo) throws Exception {
        if(uploadInfo instanceof Serializable) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceName, context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(uploadInfo);//把对象写到流里
                String temp = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
                editor.putString(key, temp);
                editor.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            throw new Exception("User must implements Serializable");
        }
    }

    public static OSSUploadInfo getUploadInfo(Context context, String preferenceName,String key) {
        SharedPreferences sharedPreferences=context.getSharedPreferences(preferenceName,Context.MODE_PRIVATE);
        String temp = sharedPreferences.getString(key, "");
        ByteArrayInputStream bais =  new ByteArrayInputStream(Base64.decode(temp.getBytes(), Base64.DEFAULT));
        OSSUploadInfo ossUploadInfo = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            ossUploadInfo = (OSSUploadInfo) ois.readObject();
        } catch (IOException e) {
        }catch(ClassNotFoundException e1) {

        }
        return ossUploadInfo;
    }

    public static boolean clearUploadInfo(Context context,String preferenceName,String  key){
        SharedPreferences sharedPreferences=context.getSharedPreferences(preferenceName,context.MODE_PRIVATE);
        return sharedPreferences.edit().remove(key).commit();
    }

}
