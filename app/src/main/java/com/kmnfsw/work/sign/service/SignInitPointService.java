package com.kmnfsw.work.sign.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.kmnfsw.work.sign.db.dao.CheckedPointDao;
import com.kmnfsw.work.sign.db.dao.TaskPointDao;
import com.kmnfsw.work.sign.entity.TaskPointEntity;
import com.kmnfsw.work.sign.service.SignTaskPointService.CheckedPoint;
import com.kmnfsw.work.sign.service.SignTaskPointService.TaskPoin;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class SignInitPointService extends Service {
	private final static String Tag = ".sign.service.SignInitPointService";
	
	private final static int SERVICE_ID = 4;
	
	private static final int corePoolSize = 5;
    private static final int maximumPoolSize = Runtime.getRuntime().availableProcessors();;
    private final BlockingQueue<Runnable> mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
    private static final int keepAliveTime = 1;
    private static final  TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;//秒
    
    /**线程池管理器*/
    private ThreadPoolExecutor mDecodeThreadPool;
	private LocalBroadcastManager mLocalBroadcastManager;
	
	
    @Override
    public void onCreate() {
    	super.onCreate();

    	/**
    	 * 线程池知识见https://blog.csdn.net/l540675759/article/details/62230562
    	 */
    	mDecodeThreadPool = new ThreadPoolExecutor(
        		corePoolSize,       // Initial pool size
        		maximumPoolSize,       // Max pool size
                keepAliveTime, 		//线程保活时间
                KEEP_ALIVE_TIME_UNIT, //线程保活时间的类型
                mDecodeWorkQueue); //任务队列
    	mLocalBroadcastManager = LocalBroadcastManager.getInstance( this );
    }

    /** 调用startService()启动服务时回调 */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	startForeground(SERVICE_ID, new Notification());
    	
    	//开启线程池并执行所有线程
    	if (mDecodeThreadPool !=null) {
    		mDecodeThreadPool.execute(new TaskPoin());
    		mDecodeThreadPool.execute(new CheckedPoint());
    		
		}
    	return super.onStartCommand(intent, flags, startId);
    }
    
	
	 /**操作任务点线程*******************/
    class TaskPoin implements Runnable{
		@Override
		public void run() {
			operateTaskPoint();
		}
    }
	private void operateTaskPoint(){
		TaskPointDao taskPointDao = new TaskPointDao(getApplication());
		List<TaskPointEntity> listTaskPointEntity = taskPointDao.getAllTaskPoint();
		sendTaskPointList(listTaskPointEntity);
		for (TaskPointEntity taskPointEntity : listTaskPointEntity) {
			sendTaskPoint(taskPointEntity);
		}
	}
	/**向SignFragment发送任务点*/
    private void sendTaskPoint(TaskPointEntity taskPointEntity){
    	Intent intent = new Intent("com.kmnfsw.work.SignTaskPointService.sendTaskPoint");
    	intent.putExtra("taskPointEntity", taskPointEntity);
    	mLocalBroadcastManager.sendBroadcast(intent);
    }
    /**向SignFragment发送任务点集合*/
    private void sendTaskPointList(List<TaskPointEntity> listTaskPointEntity){
    	Intent intent = new Intent("com.kmnfsw.work.SignTaskPointService.sendListTaskPoint");
    	intent.putParcelableArrayListExtra("listTaskPointEntity", (ArrayList<? extends Parcelable>) listTaskPointEntity);
    	mLocalBroadcastManager.sendBroadcast(intent);
    }
    
    
    
    
    /**操作签到点线程**********************/
    class CheckedPoint implements Runnable{
		@Override
		public void run() {
			operateCheckedPoint();
		}
    }
	private void operateCheckedPoint(){
		 CheckedPointDao checkedPointDao = new CheckedPointDao(getApplication());
		 List<TaskPointEntity> listTaskPointEntity = checkedPointDao.getAllCheckedPoint();
		 for (TaskPointEntity checkedPointEntity : listTaskPointEntity) {
			 sendCheckedPoint(checkedPointEntity);
		}
	}
	 /**向SignFragment发送签到点*/
    private void sendCheckedPoint(TaskPointEntity checkedPointEntity){
    	Intent intent = new Intent("com.kmnfsw.work.SignTaskPointService.sendCheckedPoint");
    	intent.putExtra("checkedPointEntity", checkedPointEntity);
    	mLocalBroadcastManager.sendBroadcast(intent);
    }
	

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	/** 服务不再有用且将要被销毁时调用 */
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	mDecodeThreadPool.shutdownNow();//关闭线程池
    	Log.e(Tag, "销毁");
    }

}
