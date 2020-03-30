/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vodupload_demo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.vod.upload.VODUploadCallback;
import com.alibaba.sdk.android.vod.upload.VODUploadClient;
import com.alibaba.sdk.android.vod.upload.VODUploadClientImpl;
import com.alibaba.sdk.android.vod.upload.common.UploadStateType;
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo;
import com.alibaba.sdk.android.vod.upload.model.VodInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 多文件上传示例：可支持OSS上传和点播上传.
 * 列表上传SDK支持多种上传模式.不同模式对应不同的产品，不同的产品对应不同的参数.\n
 * // 点播上传。每次上传都是独立的鉴权，所以初始化时，不需要设置鉴权，主要需要两个参数UploadAuthAndAddress,这两个参数由开发者的Appserver提供.参考：https://help.aliyun.com/document_detail/55407.html\n
 * // OSS直接上传:STS方式，安全但是较为复杂，建议生产环境下使用。参考STS介绍：\nhttps://help.aliyun.com/document_detail/28756.html
 * // OSS直接上传:AK方式，简单但是不够安全，建议测试环境下使用。
 * Created by Mulberry on 2017/11/24.
 */
public class MultiUploadActivity extends AppCompatActivity {

    private static final String TAG = MultiUploadActivity.class.getSimpleName();

    private String filePathPrefix = "/sdcard/";

    private String uploadAuth = "";
    private String uploadAddress = "";

    private String endpoint = "";
    private String bucket = "";
    // 工作流的输入路径。
    private String vodPath = "";

    private int index = 0;
    private Random random = new Random();

    private Handler handler;

    private VODUploadClient uploader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multi_upload);

        getIntentExtra();

        List<ItemInfo> list = new ArrayList<>();
        final VODUploadAdapter vodUploadAdapter = new VODUploadAdapter(MultiUploadActivity.this,
                R.layout.listitem, list);

        final ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(vodUploadAdapter);

        // 打开日志。
        OSSLog.enableLog();

        // UI只允许在主线程更新。
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                vodUploadAdapter.notifyDataSetChanged();
            }
        };


        uploader = new VODUploadClientImpl(getApplicationContext());
        uploader.setRegion(VodUploadApplication.VOD_REGION);
        uploader.setRecordUploadProgressEnabled(VodUploadApplication.VOD_RECORD_UPLOAD_PROGRESS_ENABLED);
        VODUploadCallback callback = new VODUploadCallback() {

            @Override
            public void onUploadSucceed(UploadFileInfo info) {
                OSSLog.logDebug("onsucceed ------------------" + info.getFilePath());
                for (int i = 0; i < vodUploadAdapter.getCount(); i++) {
                    if (vodUploadAdapter.getItem(i).getFile().equals(info.getFilePath())) {
                        if (vodUploadAdapter.getItem(i).getStatus() == String.valueOf(UploadStateType.SUCCESS)) {
                            // 传了同一个文件，需要区分更新
                            continue;
                        }
                        vodUploadAdapter.getItem(i).setProgress(100);
                        vodUploadAdapter.getItem(i).setStatus(info.getStatus().toString());
                        handler.sendEmptyMessage(0);
                        break;
                    }
                }
            }

            @Override
            public void onUploadFailed(UploadFileInfo info, String code, String message) {
                OSSLog.logError("onfailed ------------------ " + info.getFilePath() + " " + code + " " + message);
                for (int i = 0; i < vodUploadAdapter.getCount(); i++) {
                    if (vodUploadAdapter.getItem(i).getFile().equals(info.getFilePath())) {
                        vodUploadAdapter.getItem(i).setStatus(info.getStatus().toString());
                        handler.sendEmptyMessage(0);
                        break;
                    }
                }
            }

            @Override
            public void onUploadProgress(UploadFileInfo info, long uploadedSize, long totalSize) {
                OSSLog.logDebug("onProgress ------------------ " + info.getFilePath() + " " + uploadedSize + " " + totalSize);
                for (int i = 0; i < vodUploadAdapter.getCount(); i++) {
                    if (vodUploadAdapter.getItem(i).getFile().equals(info.getFilePath())) {
                        if (vodUploadAdapter.getItem(i).getStatus() == String.valueOf(UploadStateType.SUCCESS)) {
                            // 传了同一个文件，需要区分更新
                            continue;
                        }
                        vodUploadAdapter.getItem(i).setProgress(uploadedSize * 100 / totalSize);
                        vodUploadAdapter.getItem(i).setStatus(info.getStatus().toString());
                        handler.sendEmptyMessage(0);
                        break;
                    }
                }
            }

            @Override
            public void onUploadTokenExpired() {
                Log.d(TAG, "demo onUploadTokenExpired");
                OSSLog.logError("onExpired ------------- ");
                // 实现时，重新获取上传凭证:UploadAuth
                try {
                    run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onUploadRetry(String code, String message) {
                OSSLog.logError("onUploadRetry ------------- ");
            }

            @Override
            public void onUploadRetryResume() {
                OSSLog.logError("onUploadRetryResume ------------- ");
            }

            @Override
            public void onUploadStarted(UploadFileInfo uploadFileInfo) {
                OSSLog.logError("onUploadStarted ------------- ");
                uploader.setUploadAuthAndAddress(uploadFileInfo, uploadAuth, uploadAddress);
                OSSLog.logError("file path:" + uploadFileInfo.getFilePath() +
                        ", endpoint: " + uploadFileInfo.getEndpoint() +
                        ", bucket:" + uploadFileInfo.getBucket() +
                        ", object:" + uploadFileInfo.getObject() +
                        ", status:" + uploadFileInfo.getStatus());
            }
        };
        // 点播上传。每次上传都是独立的鉴权，所以初始化时，不需要设置鉴权
        uploader.init(callback);
        uploader.setPartSize(1024 * 1024);
        uploader.setTemplateGroupId("xxx");
        uploader.setStorageLocation("xxx");

        Button btnAdd = (Button) findViewById(R.id.btn_addFile);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fineName = filePathPrefix + "media" + index + ".mp4";
                String ossName = vodPath + index + ".mp4";

                // 如果文件不存在，生成一个临时文件。正式环境换成用户视频文件。
                generateTempFile(fineName, random.nextInt(300000) + 100000);
                uploader.addFile(fineName, getVodInfo());

                OSSLog.logDebug("添加了一个文件：" + fineName);
                // 获取刚添加的文件。
                UploadFileInfo uploadFileInfo = uploader.listFiles().get(uploader.listFiles().size() - 1);

                // 添加到列表。
                ItemInfo info = new ItemInfo();
                info.setFile(uploadFileInfo.getFilePath());
                info.setProgress(0);
                info.setStatus(uploadFileInfo.getStatus().toString());
                vodUploadAdapter.add(info);

                index++;
            }
        });

        Button btnDelete = (Button) findViewById(R.id.btn_deleteFile);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vodUploadAdapter.getCount() == 0) {
                    Context context = getApplicationContext();
                    Toast.makeText(context, "列表为空啦!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int index = uploader.listFiles().size() - 1;
                UploadFileInfo info = uploader.listFiles().get(index);
                uploader.deleteFile(index);
                OSSLog.logDebug("删除了一个文件：" + info.getFilePath());

                vodUploadAdapter.remove(vodUploadAdapter.getItem(index));

                for (UploadFileInfo uploadFileInfo : uploader.listFiles()) {
                    OSSLog.logDebug("file path:" + uploadFileInfo.getFilePath() +
                        ", status:" + uploadFileInfo.getStatus());
                }
            }
        });

        Button btnCancel = (Button) findViewById(R.id.btn_cancelFile);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                Toast.makeText(context, "取消文件上传", Toast.LENGTH_SHORT).show();

                int index = uploader.listFiles().size() - 1;
                if (index < 0) {
                    Toast.makeText(context, "请先添加文件再执行取消操作.", Toast.LENGTH_SHORT).show();
                    return;
                }

                UploadFileInfo info = uploader.listFiles().get(index);
                uploader.cancelFile(index);

                for (int i = 0; i < vodUploadAdapter.getCount(); i++) {
                    if (vodUploadAdapter.getItem(i).getFile().equals(info.getFilePath())) {
                        vodUploadAdapter.getItem(i).setStatus(info.getStatus().toString());
                        handler.sendEmptyMessage(0);
                        break;
                    }
                }
            }
        });

        Button btnResumeFile = (Button) findViewById(R.id.btn_resumeFile);
        btnResumeFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                Toast.makeText(context, "恢复文件上传", Toast.LENGTH_SHORT).show();

                int index = uploader.listFiles().size() - 1;
                if (index < 0) {
                    Toast.makeText(context, "请先添加文件再执行取消操作.", Toast.LENGTH_SHORT).show();
                    return;
                }

                UploadFileInfo info = uploader.listFiles().get(index);
                uploader.resumeFile(index);

                for (int i = 0; i < vodUploadAdapter.getCount(); i++) {
                    if (vodUploadAdapter.getItem(i).getFile().equals(info.getFilePath())) {
                        vodUploadAdapter.getItem(i).setStatus(info.getStatus().toString());
                        handler.sendEmptyMessage(0);
                        break;
                    }
                }

                return;
            }
        });

        Button btnList = (Button) findViewById(R.id.btn_getList);
        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                Toast.makeText(context, "获取文件列表，" + uploader.listFiles().size(),
                        Toast.LENGTH_SHORT).show();

                for (UploadFileInfo uploadFileInfo : uploader.listFiles()) {
                    OSSLog.logDebug("file path:" + uploadFileInfo.getFilePath() +
                        ", status:" + uploadFileInfo.getStatus());
                }
                return;
            }
        });

        Button btnClear = (Button) findViewById(R.id.btn_clearList);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();

                uploader.clearFiles();

                Toast.makeText(context, "清理文件列表完成。", Toast.LENGTH_SHORT).show();
                vodUploadAdapter.clear();
                OSSLog.logDebug("列表大小为：" + uploader.listFiles().size());

                return;
            }
        });

        Button btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                Toast.makeText(context, "开始上传", Toast.LENGTH_SHORT).show();

                uploader.start();
            }
        });

        Button btnStop = (Button) findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                Toast.makeText(context, "停止上传", Toast.LENGTH_SHORT).show();

                uploader.stop();
            }
        });

        Button btnPause = (Button) findViewById(R.id.btn_pause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                Toast.makeText(context, "暂停上传", Toast.LENGTH_SHORT).show();

                uploader.pause();
                return;
            }
        });

        Button btnResume = (Button) findViewById(R.id.btn_resume);
        btnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                Toast.makeText(context, "恢复上传", Toast.LENGTH_SHORT).show();

                uploader.resume();
                return;
            }
        });
    }

    private VodInfo getVodInfo() {
        VodInfo vodInfo = new VodInfo();
        vodInfo.setTitle("标题😄😄😄😄😄😄😄😄" + index);
        vodInfo.setDesc("描述." + index);
        vodInfo.setCateId(index);
        vodInfo.setIsProcess(true);
        vodInfo.setCoverUrl("http://www.taobao.com/" + index + ".jpg");
        List<String> tags = new ArrayList<>();
        tags.add("标签" + index);
        vodInfo.setTags(tags);
        vodInfo.setIsShowWaterMark(false);
        vodInfo.setPriority(7);
        return vodInfo;
    }

    private void generateTempFile(String filePath, long fileSize) {
        String content = "1";
        try {
            File dir = new File(filePath);
            if (!dir.exists()) {
                dir.createNewFile();
                dir.mkdir();
            } else {
                return;
            }

            File f = new File(filePath);
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);

            long size = 0;
            while (fileSize > size) {
                fos.write(content.getBytes());
                size += content.length();
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void getIntentExtra() {
        uploadAuth = getIntent().getStringExtra("UploadAuth");
        uploadAddress = getIntent().getStringExtra("UploadAddress");
    }

    public void run() throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .build();
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
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }
                try {

                    JSONObject jsonObject = new JSONObject(responseBody.string());
                    uploadAddress = jsonObject.optString("UploadAddress");
                    uploadAuth = jsonObject.optString("UploadAuth");
                    Log.d(TAG, "uploadAddress" + uploadAddress);
                    Log.d(TAG, "uploadAuth" + uploadAuth);
                    Log.d(TAG, "VideoId" + jsonObject.optString("VideoId"));
                    handler.sendEmptyMessage(1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                uploader.resumeWithAuth(uploadAuth);
            }
        });
    }
}
