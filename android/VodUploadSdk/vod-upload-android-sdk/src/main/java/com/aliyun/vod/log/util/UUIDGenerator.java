package com.aliyun.vod.log.util;

import java.io.File;
import java.util.UUID;

import android.os.Environment;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import android.text.TextUtils;

/**
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

public class UUIDGenerator {

    /**
     * 存储路径父目录名称
     */
    public final static String DATA_DIRECTORY = "AlivcData";

    /**
     * uuid存储文件
     */
    private final static String UUID_FILE = "alivc_data.txt";

    /**
     * 生成唯一ID属性名
     */
    private final static String UUID_PROP = "UUID";

    /**
     * 尝试10次写文件操作
     */
    private final static int MAX_WRITE_COUNT = 10;

    /**
     * Conan组件文件目录
     */
    public final static File ALIVC_DATA_FILE = new File(Environment.getExternalStorageDirectory(), DATA_DIRECTORY);

    private static String sDeviceUUID;
    private static int sWriteUUIDCount;

    public static final String generateRequestID() {
        return UUID.randomUUID().toString().toUpperCase();
    }
    /**
     * 获取设备唯一ID，32位长
     * @return UUID
     */
    public static synchronized String generateUUID() {
        if (!TextUtils.isEmpty(sDeviceUUID)) {
            return sDeviceUUID;
        }

        File targetIDFile = new File(ALIVC_DATA_FILE, UUID_FILE);
        try {
            boolean hasParent = ALIVC_DATA_FILE.exists() || ALIVC_DATA_FILE.mkdir();
            if (hasParent) {
                try {
                    Properties props = new Properties();
                    FileReader fileReader = new FileReader(targetIDFile);
                    props.load(fileReader);
                    fileReader.close();
                    sDeviceUUID = props.getProperty(UUID_PROP);
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignore) {
        }
        if (TextUtils.isEmpty(sDeviceUUID)) {
            sWriteUUIDCount = 0;
            sDeviceUUID = UUID.randomUUID().toString().replace("-", "");
            writeUUIDToFile(targetIDFile, sDeviceUUID);
        }
        return sDeviceUUID;
    }

    private static void writeUUIDToFile(final File uuidFile, final String uuidValue) {
        if (uuidFile == null || TextUtils.isEmpty(uuidValue)) {
            return;
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                boolean writeFileDone = false;
                try {
                    boolean fileCorrect = uuidFile.exists() || uuidFile.createNewFile();
                    Properties props = new Properties();
                    props.setProperty(UUID_PROP, uuidValue);
                    if (fileCorrect) {
                        FileWriter fileWriter = new FileWriter(uuidFile);
                        props.store(fileWriter, null);
                        fileWriter.close();
                        writeFileDone = true;
                    }
                } catch (Throwable ignore) {
                }
                sWriteUUIDCount++;
                if (!writeFileDone && sWriteUUIDCount < MAX_WRITE_COUNT) {
                    writeUUIDToFile(uuidFile, uuidValue);
                }
            }
        }, 3000);
    }
}
