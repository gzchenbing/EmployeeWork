package com.kmnfsw.work.util.notify;

import android.app.Activity;
import android.app.Service;
import android.os.Vibrator;
import android.widget.Toast;

/**
 * 振动通知类
 * @author YanFaBu
 *
 */
public class VibratorUtil {
	
	/*
	 * 主要api
	 * boolean hasVibrator ()//检查硬件是否有振动器
		void vibrate (long milliseconds)//控制手机制动milliseconds毫秒
		void vibrate (long[] pattern,  int repeat)//让手机以指定pattern模式震动。
		void cancel ()//关闭震动

	 */
	

	/**
	 * 震动milliseconds毫秒
	 * @param activity
	 * @param milliseconds
	 */
	public static void vibrate(final Activity activity, long milliseconds) {
        vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        if (vib.hasVibrator()) {//检查硬件是否有振动器
        	vib.hasVibrator();
	        vib.vibrate(milliseconds);
		}else{
			Toast.makeText(activity, "设备不支持振动", Toast.LENGTH_SHORT).show();
		}
        
    }
	/**
	 * 以pattern[]方式震动
	 * 
	 * 对于pattern数组的解释是：第一个值表示在打开振动器之前要等待的毫秒数。
	 * 下一个值表示在关闭振动器之前保持振动器的毫秒数。随后的值在以毫秒为单位的
	 * 持续时间之间交替，以关闭振动器或打开振动器。
	 * repeat表示重复模式，
	 * 就是指定pattern数组的下标从哪一位开始重复，使用-1是禁用重复。
	 * 
	 * 举例举个例子：
	 * vibrate(new int[]{100,200,300,400},2)是指：先等待100ms，
	 * 震动200ms，再等待300ms，震动400ms，接着就从pattern[2]的位置开始重复，
	 * 就是继续的等待300ms，震动400ms，一直重复下去。当然传入0就是从开头一直重复下去，传入-1就是不重复震动。
	 * @param activity
	 * @param pattern
	 * @param repeat
	 */
	public static void vibrate(final Activity activity, long[] pattern,int repeat){
        vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        if (vib.hasVibrator()) {//检查硬件是否有振动器
        	vib.vibrate(pattern,repeat);
		}else{
			Toast.makeText(activity, "设备不支持振动", Toast.LENGTH_SHORT).show();
		}
    }
	private static Vibrator vib;
	/**
	 * 取消震动
	 * @param activity
	 */
	public static void virateCancle(){
		if (null != vib) {
			vib.cancel();
			vib = null;
		}
    }

}
