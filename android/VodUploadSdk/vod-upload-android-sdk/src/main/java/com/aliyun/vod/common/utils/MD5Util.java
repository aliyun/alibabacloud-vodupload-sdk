package com.aliyun.vod.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 *
 */
public class MD5Util {
    private static final int STREAM_BUFFER_LENGTH = 1024;
    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static byte[] encrypt(String text) {
        return encrypt(text.getBytes());
    }

    public static byte[] encrypt(byte[] bytes) {
        try {
            MessageDigest digest = getDigest("MD5");
            digest.update(bytes);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encrypt(InputStream is) throws NoSuchAlgorithmException, IOException {
        return updateDigest(getDigest("MD5"), is).digest();
    }

    public static String encryptToHexStr(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        try {
            byte[] in = text.getBytes();
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(in);
            byte[] out = messageDigest.digest();
            return bytesToHexStr(out);
        } catch (Exception e) {
            return null;
        }
    }

    private static MessageDigest getDigest(final String algorithm) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(algorithm);
    }

    private static MessageDigest updateDigest(final MessageDigest digest, final InputStream data) throws IOException {
        final byte[] buffer = new byte[STREAM_BUFFER_LENGTH];

        int size;
        while ((size = data.read(buffer, 0, STREAM_BUFFER_LENGTH)) > -1) {
            digest.update(buffer, 0, size);
        }

        return digest;
    }

    private static String bytesToHexStr(byte[] bytes) {
        char outChars[] = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            outChars[i * 2] = DIGITS_UPPER[bytes[i] >>> 4 & 0xf];
            outChars[i * 2 + 1] = DIGITS_UPPER[bytes[i] & 0xf];
        }
        return new String(outChars);
    }
}
