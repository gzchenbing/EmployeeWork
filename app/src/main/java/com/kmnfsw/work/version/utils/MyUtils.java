package com.kmnfsw.work.version.utils;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.Log;

public class MyUtils {
	
	private static final String TAG = "version.utils.MyUtils";

	/**
	 * 获取版本号
	 * 
	 * @param context
	 * @return 返回版本号
	 */
	public static String getVersion(Context context) {
		// PackageManager 可以获取清单文件中的所有信息
		PackageManager manager = context.getPackageManager();
		try {
			// 获取到一个应用程序的信息
			// getPackageName() 获取到当前程序的包名
			PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, ""+e);
			return "";
		}
	}

	/**
	 * 安装新版本
	 * 
	 * @param activity
	 */
	public static void installApk(Activity activity, File fileResult) {
		// apk下载完成后，调用系统的安装方法
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setDataAndType(Uri.fromFile(fileResult), "application/vnd.android.package-archive");
		// 如果用户取消安装的话,会返回结果,回调方法onActivityResult
		activity.startActivityForResult(intent, 0);
	}
}