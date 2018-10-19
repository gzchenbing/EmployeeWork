package com.kmnfsw.work.version.utils;

import java.io.File;

import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * 下载APK工具类
 * @author YanFaBu
 *
 */
public class DownLoadUtils {
	private static final String TAG = "version.utils.DownLoadUtils";
	
	private Activity context;
	
    public void downapk(String url,Activity context, final MyCallBack myCallBack) {  
    	this.context = context;
    	RequestParams params = new RequestParams(url);
    	
    	//删除保存的图片
    	File apkFile = new File(getDiskCacheDir("apk"));
		folderDelete(apkFile);
		
		//添加安装路径
    	params.setSaveFilePath(getDiskCacheDir("apk")+File.separator+"employeework.apk");
    	//params.setSaveFilePath(Environment.getExternalStorageDirectory()+"/employeework/");
    	//是否自动为文件命名
    	params.setAutoRename(false);
    	params.setConnectTimeout(2000);//设置连接超时两秒
    	x.http().get(params, new Callback.ProgressCallback<File>() {
    	    @Override
    	    public void onSuccess(File result) {
    	    	Log.i(TAG, "apk文件本地路径："+result.toString());
    	        //apk下载完成后，调用系统的安装方法
    	    	myCallBack.onSuccess(result);
    	    }
    	    @Override
    	    public void onError(Throwable ex, boolean isOnCallback) {
    	    	Log.e(TAG+".onError", ""+ex);
    	    	myCallBack.onFailure(ex, isOnCallback);
    	    }
    	    @Override
    	    public void onCancelled(CancelledException cex) {
    	    	Log.e(TAG+".onCancelled", ""+cex);
    	    }
    	    @Override
    	    public void onFinished() {
    	    }
    	    //网络请求之前回调
    	    @Override
    	    public void onWaiting() {
    	    }
    	    //网络请求开始的时候回调
    	    @Override
    	    public void onStarted() {
    	    }
    	    //下载的时候不断回调的方法
    	    @Override
    	    public void onLoading(long total, long current, boolean isDownloading) {
    	        //当前进度和文件总大小
    	        //Log.i(TAG,"current速度："+ current +"，total总大小："+total); 
    	        myCallBack.onLoadding(total, current, isDownloading);
    	    }
    	});
    }  
    /**应用磁盘路径*/
	private String getDiskCacheDir(String   folder) {
		String cachepath;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable()) {
			cachepath = context.getExternalCacheDir().getPath() + File.separator +folder;
		} else {
			cachepath = context.getCacheDir().getPath() + File.separator +folder;
		}
		File fileDir = new File(cachepath);
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		return cachepath;
	}
	/**删除文件夹*/
	private  void  folderDelete(File file) {
        //生成File[]数组   listFiles()方法获取当前目录里的文件夹  文件
        File[] files = file.listFiles();
        //判断是否为空   //有没有发现讨论基本一样
        if(files!=null){
            //遍历
            for (File file2 : files) {
                //是文件就删除
                if(file2.isFile()){
                    file2.delete();
                }else if(file2.isDirectory()){
                    //是文件夹就递归
                	folderDelete(file2);
                    //空文件夹直接删除
                    file2.delete();
                }
            }
        }
        
    }
	/**
	 *接口，用于监听下载状态的接口
	 * */
	interface MyCallBack{
		/**下载成功时调用*/
		void onSuccess(File result);
		/**下载失败时调用*/
		void onFailure(Throwable ex, boolean isOnCallback);
		/**下载中调用*/
		void onLoadding(long total, long current, boolean isDownloading);
	}
}
