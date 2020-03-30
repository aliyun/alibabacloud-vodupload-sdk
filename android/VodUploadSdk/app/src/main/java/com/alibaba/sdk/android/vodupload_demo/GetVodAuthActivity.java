/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vodupload_demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * Created by Mulberry on 2018/1/5.
 */
public class GetVodAuthActivity extends AppCompatActivity implements View.OnClickListener{
    private String TAG = "VodAuth";

    Button btnGetAuth;
    TextView tvauth;
    private  OkHttpClient client;
    private int uploadType;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = new OkHttpClient.Builder()
                .build();
        setContentView(R.layout.get_auth_layout);
        btnGetAuth = (Button) findViewById(R.id.btn_get_vodauth);
        tvauth = (TextView) findViewById(R.id.tv_auth);
        btnGetAuth.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String uploadAuth;
    String uploadAddress;

    public void run() throws Exception {
            Request request = new Request.Builder()
                    .url("https://demo-vod.cn-shanghai.aliyuncs.com/voddemo/CreateUploadVideo?Title=testvod1&FileName=xxx.mp4&BusinessType=vodai&TerminalType=pc&DeviceModel=iPhone9,2&UUID=59ECA-4193-4695-94DD-7E1247288&AppVersion=1.0.0")
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                        ResponseBody responseBody = response.body();
                        if (!response.isSuccessful()){
                            throw new IOException("Unexpected code " + response);
                        }

                        Headers responseHeaders = response.headers();
                        for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                        }
                        try {

                            JSONObject jsonObject = new JSONObject(responseBody.string());
                            uploadAddress= jsonObject.optString("UploadAddress");
                            uploadAuth= jsonObject.optString("UploadAuth");
                            Log.d(TAG,"uploadAddress" + uploadAddress);
                            Log.d(TAG,"uploadAuth" + uploadAuth);
                            Log.d(TAG,"VideoId" + jsonObject.optString("VideoId"));
                            handler.sendEmptyMessage(1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
            });
    }

    Handler handler =new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            tvauth.setText("\nUploadAuth:" + uploadAuth + "\nUploadAddress:"+ uploadAddress);

            startActivity(uploadType);
            return false;
        }
    });
    private void startActivity(int type){
        Intent vodintent =new Intent();
        vodintent.setClass(GetVodAuthActivity.this,MultiUploadActivity.class);
        vodintent.putExtra("UploadAuth",uploadAuth);
        vodintent.putExtra("UploadAddress",uploadAddress);
        startActivity(vodintent);

    }

}
