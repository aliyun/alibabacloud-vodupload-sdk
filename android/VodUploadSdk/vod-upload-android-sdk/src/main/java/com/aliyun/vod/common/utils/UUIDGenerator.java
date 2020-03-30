/*
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

package com.aliyun.vod.common.utils;

import java.util.UUID;

/**
 * Created by apple on 2017/8/23.
 */

public class UUIDGenerator {
    public static final String generateUUID() {
        return UUID.randomUUID().toString().toUpperCase();
    }
}
