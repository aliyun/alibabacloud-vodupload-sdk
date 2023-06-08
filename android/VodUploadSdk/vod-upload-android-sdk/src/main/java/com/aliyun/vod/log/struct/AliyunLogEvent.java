package com.aliyun.vod.log.struct;

/**
 * Copyright (C) 2020 Alibaba Group Holding Limited
 */

public class AliyunLogEvent {
    public static final int EVENT_INIT_RECORDER = 2001;//初始化录制
    public static final int EVENT_START_RECORDING = 2002;//开始录制
    public static final int EVENT_STOP_RECORDING = 2003;//结束录制
    public static final int EVENT_DELETE_CLIP = 2004;//回删
    public static final int EVENT_FINISH_RECORDING = 2005;  //完成录制
    public static final int EVENT_RECORDING_FAILED = 2006;//录制失败
    public static final int EVENT_ADD_PASTER = 2007;    //添加动图
    public static final int EVENT_ADD_MUSIC = 2008;//加音乐
    public static final int EVENT_ADD_FILTER = 2009;//添加滤镜
    public static final int EVENT_CHANGE_SPEED = 2010;//切换录制速率
    public static final int EVENT_CHANGE_BEAUTY = 2011;//切换美颜
    public static final int EVENT_CHANGE_CAMREA = 2012;//切换摄像头



    public static final int EVENT_INIT_EDITOR = 3001;//初始化编辑器
    public static final int EVENT_CREATE_PLAYER = 3002;//创建播放器
    public static final int EVENT_CREATE_PASTER_MANAGER = 3003;//创建贴图管理器
    public static final int EVENT_CREATE_CANVAS_CONTROLLER = 3004;//创建涂鸦控制器
    public static final int EVENT_APPLY_FILTER = 3005;//应用滤镜
    public static final int EVENT_APPLY_MV = 3006;//应用MV
    public static final int EVENT_APPLY_WATERMARK = 3007;//应用水印
    public static final int EVENT_APPLY_MUSIC = 3008;//应用音乐
    public static final int EVENT_SET_WEIGHT = 3009;//设置音乐和视频原音权重
    public static final int EVENT_SET_TAIL_BMP = 3010;//设置片尾水印
    public static final int EVENT_START_COMPOSE = 3011;//开始合成
    public static final int EVENT_CANCEL_COMPOSE = 3012;//取消合成
    public static final int EVENT_ON_EDITOR_RESUME = 3013;//onResume
    public static final int EVENT_ON_EDITOR_PAUSE = 3014;//onPause
    public static final int EVENT_ON_EDITOR_DESTORY = 3015;//onDestroy
    public static final int EVENT_COMPOSE_ERROR = 3016;//合成出错
    public static final int EVENT_COMPOSE_COMPLETE = 3017;//合成完成


    public static final int EVENT_START_PLAY = 4001;//开始播放
    public static final int EVENT_PAUSE_PLAY = 4002;//暂停播放
    public static final int EVENT_RESUME_PLAY = 4003;//恢复播放
    public static final int EVENT_STOP_PLAY = 4004;//停止播放
    public static final int EVENT_CURRENT_PLAY_POSITION = 4005;//获取当前播放位置
    public static final int EVENT_OBTAIN_DURATION = 4006;//获取视频时长
    public static final int EVENT_SET_VOLUME = 4007;//设置音量
    public static final int EVENT_SET_SCALE_MODE = 4008;//设置缩放模式

    /*---------------upload event------------------------**/
    public static final int EVENT_UPLOAD_ADD_FILES = 20001;
    public static final int EVENT_UPLOAD_STARTED = 20002;
    public static final int EVENT_UPLOAD_SUCCESSED = 20003;
    public static final int EVENT_UPLOAD_FILE_FAILED = 20004;
    public static final int EVENT_UPLOAD_PART_START = 20005;
    public static final int EVENT_UPLOAD_PART_FAILED = 20006;
    public static final int EVENT_UPLOAD_PART_COMPLETED = 20007;
    public static final int EVENT_UPLOAD_CANCEL = 20008;

}
