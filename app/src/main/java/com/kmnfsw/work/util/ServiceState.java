package com.kmnfsw.work.util;

import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

public class ServiceState {
	private final static String Tag = ".util.ServiceState";
    /**
     * 判断服务是否启动,context上下文对象 ，className服务的name 
     */  
    public static boolean isServiceRunning(Context mContext, String className) {  
  
        boolean isRunning = false;  
        ActivityManager activityManager = (ActivityManager) mContext  
                .getSystemService(Context.ACTIVITY_SERVICE);  
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager  
                .getRunningServices(100);  
          
        if (!(serviceList.size() > 0)) {  
            return false;  
        }  
  
        for (int i = 0; i < serviceList.size(); i++) {  
        	//Log.i(Tag, serviceList.get(i).service.getClassName());
            if (serviceList.get(i).service.getClassName().equals(className) == true) {  
                isRunning = true;  
                break;  
            }  
        }  
        return isRunning;  
    }  
}
