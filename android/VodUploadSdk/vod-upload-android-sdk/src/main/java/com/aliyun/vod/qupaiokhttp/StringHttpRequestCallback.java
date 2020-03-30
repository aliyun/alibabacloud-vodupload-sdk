/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.aliyun.vod.qupaiokhttp;

public class StringHttpRequestCallback extends BaseHttpRequestCallback<String> {

    public StringHttpRequestCallback() {
        super();
        type = String.class;
    }
}
