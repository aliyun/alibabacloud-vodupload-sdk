/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload.model;

/**
 * 片段信息
 */
public class FilePartInfo {
    /**
     * 片段开始地址
     */
    private long mSeek;
    /**
     * 片段大小
     */
    private long mSize;
    /**
     * 片段号
     */
    private int mPartNumber;

    public long getSeek() {
        return mSeek;
    }

    public void setSeek(long seek) {
        this.mSeek = seek;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        this.mSize = size;
    }

    public int getPartNumber() {
        return mPartNumber;
    }

    public void setPartNumber(int partNumber) {
        this.mPartNumber = partNumber;
    }
}
