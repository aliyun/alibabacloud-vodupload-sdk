/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.aliyun.vod.qupaiokhttp;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

class ProgressRequestBody extends RequestBody {
    //开始时间，用户计算加载速度
    private long previousTime;

    protected RequestBody delegate;
    protected ProgressCallback callback;

    protected CountingSink countingSink;

    public ProgressRequestBody(RequestBody delegate, ProgressCallback callback) {
        this.delegate = delegate;
        this.callback = callback;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return delegate.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        previousTime = System.currentTimeMillis();
        countingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);

        delegate.writeTo(bufferedSink);

        bufferedSink.flush();

    }

    protected final class CountingSink extends ForwardingSink {

        private long bytesWritten = 0;
        //总字节长度，避免多次调用contentLength()方法
        long contentLength = 0L;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);

            if (contentLength == 0) {
                //获得contentLength的值，后续不再调用
                contentLength = contentLength();
            }
            bytesWritten += byteCount;
            //回调
            if (callback != null) {
                //计算速度
                long totalTime = (System.currentTimeMillis() - previousTime) / 1000;
                if (totalTime == 0) {
                    totalTime += 1;
                }
                long networkSpeed = bytesWritten / totalTime;
                int progress = (int) (bytesWritten * 100 / contentLength);
                boolean done = bytesWritten == contentLength;
                callback.updateProgress(progress, networkSpeed, done);
            }
        }

    }
}
