/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.vod.common.utils;

import android.net.Uri;

/**
 */
public class UriUtil {

    public static final String ASSETS = "assets";

    public static final String FILE = "file";

    public static final String QUPAI_ASSETS = "qupai-assets";

    public static final String PROVIDER = "content";

    public static final String QUERY_ID = "id";
    public static final String QUERY_TYPE = "type";
    public static final String QUERY_CATEGORY = "category";
    public static final String MULI_SPLIT = ",";

    public static String formatAssetURI(int type, int id) {
        return String.format("%s://?type=%d&id=%d", QUPAI_ASSETS, type, id);
    }

    public static String formatProvideURI(int[] category, String authority) {
        StringBuilder sb = new StringBuilder();
        for(int c : category){
            sb.append(c).append(MULI_SPLIT);
        }
        sb.deleteCharAt(sb.length() - 1);
        return String.format("%s://%s?category=%s", PROVIDER, authority, sb.toString());
    }

    public static  int[] getQueryIA(Uri uri){
        String value = uri.getQueryParameter(QUERY_CATEGORY);
        if(value == null){
            return null;
        }
        String[] ss = value.split(MULI_SPLIT);
        int[] r = new int[ss.length];
        for(int i = 0; i < ss.length; i++){
            r[i] = Integer.parseInt(ss[i]);
        }
        return r;
    }

    public static <E extends Enum<E>> E getQueryE(Uri uri, String key, E def_val) {
        String value = uri.getQueryParameter(key);

        if (value == null) {
            return def_val;
        }

        try {
            return Enum.valueOf(def_val.getDeclaringClass(), key);
        } catch (Throwable tr) {
            return def_val;
        }
    }

    public static long getQueryL(Uri uri, String name, long def_val) {
        String value = uri.getQueryParameter(name);

        if (value == null) {
            return def_val;
        }

        try {
            return Long.parseLong(value);
        } catch (Throwable tr) {
            return def_val;
        }
    }

    public static int getQueryI(Uri uri, String name, int def_val) {
        String value = uri.getQueryParameter(name);

        if (value == null) {
            return def_val;
        }

        try {
            return Integer.parseInt(value);
        } catch (Throwable tr) {
            return def_val;
        }
    }

    public static boolean getQueryB(Uri uri, String key, boolean def_val) {
        String value = uri.getQueryParameter(key);

        if (value == null) {
            return def_val;
        }

        try {
            return Boolean.parseBoolean(value);
        } catch (Throwable tr) {
            return def_val;
        }
    }
}
