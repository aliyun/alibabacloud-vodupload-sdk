/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.vod.upload.common.utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.alibaba.sdk.android.vod.upload.model.UserData;
import com.aliyun.vod.common.utils.StringUtils;

import java.io.File;


public class VideoInfoUtil {

    private static final String VOD_SOURCE_SHORT_VIDEO = "short_video";

    public static UserData getVideoBitrate(Context context, String path) {
        UserData userData = new UserData();
        MediaMetadataRetriever metadataRetriever = null;
        try {
            metadataRetriever = new MediaMetadataRetriever();
            if (StringUtils.isUriPath(path)) {
                metadataRetriever.setDataSource(context, Uri.parse(path));
            } else {
                metadataRetriever.setDataSource(new File(path).getAbsolutePath());
            }
            userData.setBitrate(String.valueOf(Integer.parseInt(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)) / 1024));
            userData.setDuration(String.valueOf(Integer.parseInt(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000));
            userData.setFps(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE));
            userData.setWidth(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            userData.setHeight(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            userData.setSource(VOD_SOURCE_SHORT_VIDEO);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error err) {
            err.printStackTrace();
        } finally {
            if (metadataRetriever != null) {
                metadataRetriever.release();
            }
        }
        return userData;
    }
}
