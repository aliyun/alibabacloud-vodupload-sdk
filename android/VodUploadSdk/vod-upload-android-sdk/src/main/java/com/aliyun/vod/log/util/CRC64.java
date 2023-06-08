/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */
package com.aliyun.vod.log.util;



/**
 * CRC64 checksum calculator based on the polynom specified in ISO 3309. The
 * implementation is based on the following publications:
 *
 * <ul>
 * <li>http://en.wikipedia.org/wiki/Cyclic_redundancy_check</li>
 * <li>http://www.geocities.com/SiliconValley/Pines/8659/crc.htm</li>
 * </ul>
 */
public class CRC64 {

    private static final long poly = 0xC96C5795D7870F42L;
    private static final long crcTable[] = new long[256];

    private long crc = -1;

    static {
        for (int b = 0; b < crcTable.length; ++b) {
            long r = b;
            for (int i = 0; i < 8; ++i) {
                if ((r & 1) == 1)
                    r = (r >>> 1) ^ poly;
                else
                    r >>>= 1;
            }

            crcTable[b] = r;
        }
    }

    public CRC64() {
    }

    public void update(byte b) {
        crc = crcTable[(b ^ (int)crc) & 0xFF] ^ (crc >>> 8);
    }

    public void update(byte[] buf) {
        update(buf, 0, buf.length);
    }

    public void update(byte[] buf, int off, int len) {
        int end = off + len;

        while (off < end)
            crc = crcTable[(buf[off++] ^ (int)crc) & 0xFF] ^ (crc >>> 8);
    }

    public long getValue() {
        return ~crc;
    }
}
