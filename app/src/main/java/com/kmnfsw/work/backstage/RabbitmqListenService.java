package com.kmnfsw.work.backstage;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmnfsw.work.backstage.entity.ReceiveWorkerEntity;
import com.kmnfsw.work.backstage.rabbitmq.RabbitmqListen;
import com.kmnfsw.work.backstage.rabbitmq.RabbitmqListen.WorkerReceiveMsg;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class RabbitmqListenService extends Service {

	private final static String Tag = ".backstage.RabbitmqListenService";
	private SharedPreferences shares;
	private String peopleno;
	
	private static final int corePoolSize = 5;
    private static final int maximumPoolSize = Runtime.getRuntime().availableProcessors();;
    private final BlockingQueue<Runnable> mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
    private static final int keepAliveTime = 1;
    private static final  TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;//秒
    
    /**线程池管理器*/
    private ThreadPoolExecutor mDecodeThreadPool;
    /**局部广播管理器*/
    private LocalBroadcastManager mLocalBroadcastManager;
    
    private RabbitmqListen rabbitmqListen;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
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
    	
    	mDecodeThreadPool.execute(new RabbitmqConnection());
    	
	}
	
	class RabbitmqConnection implements Runnable{

		@Override
		public void run() {
			try {
				rabbitmqListen = RabbitmqListen.connectionRabbitmq(getApplication());
				
				//开启线程服务
				if (null != mDecodeThreadPool) {
					mDecodeThreadPool.execute(new RPCThred());
					mDecodeThreadPool.execute(new WorkerReceiveThred());
				}
			} catch (IOException | TimeoutException e) {
				
				e.printStackTrace();
			}
			
		}
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		shares = getSharedPreferences("config", Context.MODE_PRIVATE);
		peopleno = shares.getString("peopleno", "");
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	class RPCThred implements Runnable{

		@Override
		public void run() {
			try {
				if (null != rabbitmqListen) {
					rabbitmqListen.startRPCServer(peopleno);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	class WorkerReceiveThred implements Runnable{

		@Override
		public void run() {
			try {
				if (null != rabbitmqListen) {
					rabbitmqListen.startWorkerReceive(peopleno, workerReceiveMsg);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	private WorkerReceiveMsg workerReceiveMsg = new WorkerReceiveMsg() {
		
		@Override
		public void getReceiveMsg(String msg) {
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				ReceiveWorkerEntity receiveWorkerEntity = objectMapper.readValue(msg,ReceiveWorkerEntity.class);
				
				if (receiveWorkerEntity.sendType == 1) {//web端推维修任务
					Log.i(Tag, "收到web端推维修任务编号 "+receiveWorkerEntity.sendContent);
					sendRepairTask(receiveWorkerEntity.sendContent.toString(), "com.kmnfsw.work.backstage.RabbitmqListenService.sendRepairTask");
					
				}
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
			
		}
	};
	
	/**推送维修任务*/
	private void sendRepairTask(String workerReceiveMsg ,String action){
		Intent intent = new Intent(action);
		intent.putExtra("repairTask", workerReceiveMsg);
		mLocalBroadcastManager.sendBroadcast(intent);
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		rabbitmqListen.stopRPCServer();
		rabbitmqListen.stopWorkerReceive();
		rabbitmqListen.stopRabbitmqListen();
		
		mDecodeThreadPool.shutdownNow();//关闭线程池
		
		
		
		Log.e(Tag, "服务销毁");
	}

}
