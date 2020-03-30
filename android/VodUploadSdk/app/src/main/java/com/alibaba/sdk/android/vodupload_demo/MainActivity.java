/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vodupload_demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button vodCertificateMultiUpload;
    public static final String[] PERMISSION_MANIFEST = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final int PERMISSION_REQUEST_CODE = 1000;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_main);
        vodCertificateMultiUpload = (Button)findViewById(R.id.vod_certificate_multi_upload);
        vodCertificateMultiUpload.setOnClickListener(this);
        requestPermissions(this,PERMISSION_MANIFEST,PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.vod_certificate_multi_upload){
            Intent intent =new Intent();
            intent.setClass(v.getContext(),GetVodAuthActivity.class);
            startActivity(intent);
        }
    }
    /**
     * 申请权限
     * @param activity Activity
     * @param permissions 权限数组
     * @param requestCode 请求码
     */
    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        // 先检查是否已经授权
        if (!checkPermissionsGroup(activity, permissions)) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }
    /**
     * 检查多个权限
     *
     * 检查权限
     * @param permissions 权限数组
     * @param context Context
     * @return true 已经拥有所有check的权限 false存在一个或多个未获得的权限
     */
    public static boolean checkPermissionsGroup(Context context, String[] permissions) {

        for (String permission : permissions) {
            if (!checkPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }
    /**
     * 检查单个权限
     * @param context Context
     * @param permission 权限
     * @return boolean
     */
    private static boolean checkPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                Toast.makeText(this, R.string.permission_success, Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this,  R.string.permission_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
