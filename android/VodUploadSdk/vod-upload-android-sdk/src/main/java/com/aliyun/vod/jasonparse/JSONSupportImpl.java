/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.aliyun.vod.jasonparse;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;

/**
 *
 */
public class JSONSupportImpl extends JSONSupport {

    private final Gson mGson = new Gson();

    @Override
    public <T> T readListValue(String content, Type klass) throws Exception {
        return mGson.fromJson(content, klass);
    }

    @Override
    public <T> T readValue(InputStream istream, Class<? extends T> klass) throws Exception {
        JsonReader reader = new JsonReader(new InputStreamReader(istream, "UTF-8"));
        reader.setLenient(true);
        T t = mGson.fromJson(reader, klass);
        reader.close();
        return t;
    }

    @Override
    public <T> T readValue(File fin, Class<? extends T> klass) throws Exception {
        return readValue(new FileInputStream(fin), klass);
    }

    @Override
    public <T> T readValue(String content, Class<? extends T> klass) throws Exception {
        return mGson.fromJson(content, klass);
    }

    @Override
    public <T> void writeValue(OutputStream ostream, T instance) throws Exception {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(ostream, "UTF-8"));
        mGson.toJson(instance, instance.getClass(), writer);
        writer.flush();
        writer.close();
    }

    @Override
    public <T> void writeValue(File fout, T instance) throws Exception {
        writeValue(new FileOutputStream(fout), instance);
    }

    @Override
    public <T> String writeValue(T instance) throws Exception {
        return mGson.toJson(instance);
    }

    private byte[] getByteFromInputStream(InputStream inputStream) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        try {
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static String fileToStr(InputStream inputStream) throws IOException {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            arrayOutputStream.write(buffer, 0, len);
        }
        return arrayOutputStream.toString();
    }
}
