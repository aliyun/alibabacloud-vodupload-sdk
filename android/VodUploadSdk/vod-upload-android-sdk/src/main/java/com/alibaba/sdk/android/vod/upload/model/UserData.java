/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.alibaba.sdk.android.vod.upload.model;

/**
 * Created by Mulberry on 2017/11/8.
 */
public class UserData {
    private String Duration;
    private String Bitrate;
    private String Fps;
    private String Width;
    private String Height;
    private String Source;

    public String getDuration() {
        return Duration;
    }

    public void setDuration(String duration) {
        Duration = duration;
    }

    public String getBitrate() {
        return Bitrate;
    }

    public void setBitrate(String bitrate) {
        Bitrate = bitrate;
    }

    public String getFps() {
        return Fps;
    }

    public void setFps(String fps) {
        Fps = fps;
    }

    public String getWidth() {
        return Width;
    }

    public void setWidth(String width) {
        Width = width;
    }

    public String getHeight() {
        return Height;
    }

    public void setHeight(String height) {
        Height = height;
    }

    public String getSource() {
        return Source;
    }

    public void setSource(String source) {
        Source = source;
    }
}
