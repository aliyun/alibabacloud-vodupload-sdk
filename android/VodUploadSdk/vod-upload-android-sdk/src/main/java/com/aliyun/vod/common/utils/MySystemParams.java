/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.vod.common.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class MySystemParams {
	
	  public static final int SCREEN_ORIENTATION_HORIZONTAL = 2;
	  public static final int SCREEN_ORIENTATION_VERTICAL = 1;
	  private static MySystemParams params;
	  private final String TAG = "SystemParams";
	  public int densityDpi;
	  public float fontScale;
	  public float scale;
	  public int screenHeight;
	  public int screenOrientation;
	  public int screenWidth;

	  private MySystemParams(){
		  
	  }
	  
	  public void init(Context context){
	    DisplayMetrics localDisplayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();
	    this.screenWidth = localDisplayMetrics.widthPixels;
	    this.screenHeight = localDisplayMetrics.heightPixels;
	    this.densityDpi = localDisplayMetrics.densityDpi;
	    this.scale = localDisplayMetrics.density;
	    this.fontScale = localDisplayMetrics.scaledDensity;
	    if (this.screenHeight > this.screenWidth){
	    	this.screenOrientation=1;
	    }else{
	    	this.screenOrientation=2;
	    }
	  }

	  public static MySystemParams getInstance(){
	    if (params == null)
	      params = new MySystemParams();
	    return params;
	  }	  

}
