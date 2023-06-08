package com.aliyun.vod.log.struct;

/**
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

public class AliyunLogKey {

    /** ------------- 公共参数 -----------------**/
    public static final String KEY_TIME = "t";
    public static final String KEY_LOG_LEVEL = "ll";
    public static final String KEY_LOG_VERSION = "lv";
    public static final String KEY_PRODUCT  = "pd";
    public static final String KEY_MODULE = "md";
    public static final String KEY_SUB_MODULE = "sm";
    public static final String KEY_HOSTNAME = "hn";
    public static final String KEY_BUSINESS_ID = "bi";
    public static final String KEY_REQUEST_ID = "ri";
    public static final String KEY_EVENT = "e";
    public static final String KEY_ARGS = "args";
    public static final String KEY_TERMINAL_TYPE = "tt";
    public static final String KEY_DEVICE_MODEL = "dm";
    public static final String KEY_OPERATION_SYSTEM = "os";
    public static final String KEY_OSVERSION = "ov";
    public static final String KEY_APP_VERSION = "av";
    public static final String KEY_UUID = "uuid";
    public static final String KEY_DEFINITION = "dn";
    public static final String KEY_CONNECTION = "co";
    public static final String KEY_USER_AGENT = "uat";
    public static final String KEY_CARRIER = "ca";
    public static final String KEY_UI = "ui";
    public static final String KEY_APPLICATION_ID = "app_id";
    public static final String KEY_CDN_IP = "cdn_ip";
    public static final String KEY_REFER = "r";
    public static final String KEY_APP_NAME= "app_n";


    /** ---------- args key ------------------**/
    public static final String KEY_VIDEO_PATH_LIST = "vpl";//视频列表
    public static final String KEY_RESOURCE_ID = "ri";  //资源ID
    public static final String KEY_RESOURCE_PATH = "rp"; //资源路径
    public static final String KEY_RESULT = "res";      //返回结果
    public static final String KEY_IMAGE_PATH = "ipt";//水印图片地址
    public static final String KEY_WIDTH = "wd"; //宽度（视频、图片、水印）
    public static final String KEY_HEIGHT = "ht";//高度（视频、图片、水印）
    public static final String KEY_POSITION_X = "psx";//x轴坐标
    public static final String KEY_POSITION_Y = "psy";//y轴坐标
    public static final String KEY_AUDIO_ENCODE_FORMAT = "aef";//音频编码格式
    public static final String KEY_WEIGHT = "we";//权重
    public static final String KEY_OUTPUT_PATH = "op";//输出路径
    public static final String KEY_ERROR_CODE = "ec";//错误码
    public static final String KEY_DURATION = "dr";//时长
    public static final String KEY_SIZE = "sz";//大小
    public static final String KEY_VIDEO_CODEC = "cc";//视频编码格式
    public static final String KEY_FORMAT = "fm";//容器格式
    public static final String KEY_FPS = "fps";//帧率
    public static final String KEY_BITRATE = "br";//码率
    public static final String KEY_CRC64 = "crc64";//文件crc64值
    public static final String KEY_CURRENT_PLAY_POSITION = "cpp";//当前播放位置
    public static final String KEY_VOLUME = "vlm";  //音量
    public static final String KEY_SCALE_MODE = "sm";//缩放模式
    public static final String KEY_FILL_COLOR = "fc";//填充颜色值

    /*---------------upload args------------------------**/
    public static final String KEY_QUEUE_LENGHT= "ql";//列表长度
    public static final String KEY_FILE_TYPE= "ft";//文件类型:video,audio,img,other
    public static final String KEY_FILE_SIZE= "fs";//文件大小，单位：kbyte
    public static final String KEY_FILE_WIDTH= "fw";//文件宽度，音频传null
    public static final String KEY_FILE_HEIGHT= "fh";//文件高度，音频传null
    public static final String KEY_FILE_MD5= "fm";//文件md5
    public static final String KEY_PART_SIZE= "ps";//分片大小
    public static final String KEY_VIDEOID= "vi";//videoID
    public static final String KEY_BUCKET= "bu";//oss 的bucket
    public static final String KEY_OBJECT_KEY= "ok";// objectkey
    public static final String KEY_UPLOADID= "ui";//上传id
    public static final String KEY_PART_NUMBER= "pn";//分片文件顺序，第几个分片
    public static final String KEY_PART_RETRY= "pr";//是否重试，0/1, 0为普通分片上传，1是重试
    public static final String KEY_UPLOAD_FAILED_CODE= "ufc";//上传单个文件失败
    public static final String KEY_UPLOAD_PART_FAILED_CODE= "upfc";//上传单个分片失败

    public static final String KEY_UPLOAD_FAILED_MESSAGE = "ufm";//上传文件失败的message
    public static final String KEY_UPLOAD_PART_FAILED_MESSAGE = "uPfm";//上传单个分片失败的message

    public static final String KEY_PATH = "path";
    public static final String KEY_START_TIME = "st";//开始时间
    public static final String KEY_TYPE = "t";//类别

}
