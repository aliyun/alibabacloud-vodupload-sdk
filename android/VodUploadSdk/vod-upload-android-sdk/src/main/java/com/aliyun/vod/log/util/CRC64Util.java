package com.aliyun.vod.log.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

public class CRC64Util {
    public static String calculateCrc64(String outputPath) {
        byte[] buffer = new byte[1024];
        CRC64 crc64 = new CRC64();
        int length = 0;
        try {
            FileInputStream fi = new FileInputStream(outputPath);
            BufferedInputStream bi = new BufferedInputStream(fi);
            while((length = bi.read(buffer)) > 0) {
                crc64.update(buffer, 0, length);
            }
            fi.close();
            bi.close();
            return String.valueOf(crc64.getValue());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
