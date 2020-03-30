/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vodupload_demo;

/**
 * Created by Leigang on 16/11/7.
 */
public class ItemInfo {
    private String file;
    private long progress;

    public String getOss() {
        return oss;
    }

    public void setOss(String oss) {
        this.oss = oss;
    }

    private String oss;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status;

    public String getFile() {
        return file;
    }

    public void setFile(String title) {
        this.file = title;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

}
