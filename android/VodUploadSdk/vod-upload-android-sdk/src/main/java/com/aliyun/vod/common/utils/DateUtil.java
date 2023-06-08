package com.aliyun.vod.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * @ClassName: DateUtil
 * @Author: fengming.fm
 * @CreateDate: 2018/11/16 下午1:35
 * @Description: 类作用描述
 * @Version: 1.0
 */
public class DateUtil {

    /*生成当前UTC时间戳Time*/
    public static String generateTimestamp() {
        return generateTimestamp(System.currentTimeMillis());
    }

    public static String generateTimestamp(long time) {
        Date date = new Date(time);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return df.format(date);
    }
}
