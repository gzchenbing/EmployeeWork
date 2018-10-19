package com.kmnfsw.work.sign.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmnfsw.work.sign.db.dao.CheckedPointDao;
import com.kmnfsw.work.sign.db.dao.TaskPointDao;
import com.kmnfsw.work.sign.entity.TaskPointEntity;
import com.kmnfsw.work.util.ProperTies;
import com.kmnfsw.work.util.ReceiveJson;
import com.kmnfsw.work.util.spring.RequestFactory;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class SignTaskPointService extends Service{
	private final static String Tag = ".sign.service.SignTaskPointService";

	private final static int SERVICE_ID = 3;
	
	private  String SERVICE_IP;
	
    private static final int corePoolSize = 4;
    private static final int maximumPoolSize = Runtime.getRuntime().availableProcessors();;
    private final BlockingQueue<Runnable> mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
    private static final int keepAliveTime = 1;
    private static final  TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.DAYS;//天
    
    /**线程池管理器*/
    private ThreadPoolExecutor mDecodeThreadPool;
	
	private String checktaskno; 
	private String oldCheckTaskNo;
	private String peopleno;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	private LocalBroadcastManager setPointLocalBroadcastManager;
	private LocalBroadcastManager getTaskLocalBroadcastManager;
	
	private TaskReceiver taskReceiver;
	
	private TaskPointDao taskPointDao;
	private CheckedPointDao checkedPointDao;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	 /** 当服务被创建时调用*/
    @Override
    public void onCreate() {
    	super.onCreate();
    	
    	Properties proper = ProperTies.getProperties(getApplicationContext());
		SERVICE_IP = proper.getProperty("serverIp");

    	/**
    	 * 线程池知识见https://blog.csdn.net/l540675759/article/details/62230562
    	 */
    	mDecodeThreadPool = new ThreadPoolExecutor(
        		corePoolSize,       // Initial pool size
        		maximumPoolSize,       // Max pool size
                keepAliveTime, 		//线程保活时间
                KEEP_ALIVE_TIME_UNIT, //线程保活时间的类型
                mDecodeWorkQueue); //任务队列
    	
    	taskPointDao = new TaskPointDao(getApplication());
		checkedPointDao = new CheckedPointDao(getApplication());
    	
    	setPointLocalBroadcastManager = LocalBroadcastManager.getInstance( this );
    	
    	getTaskLocalBroadcastManager = LocalBroadcastManager.getInstance( this );
    	taskReceiver = new TaskReceiver();
    	IntentFilter filter = new IntentFilter("com.kmnfsw.work.sign.SignFragment.selectTask");
    	getTaskLocalBroadcastManager.registerReceiver( taskReceiver , filter );//注册广播服务
    }

    /** 调用startService()启动服务时回调 */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	startForeground(SERVICE_ID, new Notification());
    	
    	if (intent !=null) {
    		checktaskno = intent.getStringExtra("checktaskno");
    		oldCheckTaskNo = intent.getStringExtra("oldCheckTaskNo");
    		peopleno = intent.getStringExtra("peopleno");
    		
    		//开启线程池并执行所有线程
    		if (mDecodeThreadPool !=null) {
    			mDecodeThreadPool.execute(new TaskPoin());
    			mDecodeThreadPool.execute(new CheckedPoint());
    			
    		}
		}
    	return super.onStartCommand(intent, flags, startId);
    }
    
    /**操作任务点线程*******************/
    class TaskPoin implements Runnable{
		@Override
		public void run() {
			taskPointDao.deleteTaskPoint();
			operateTaskPoint();
		}
    }
    private void operateTaskPoint(){
    	try {
    		//Log.i(Tag, "checktaskno"+checktaskno+" oldCheckTaskNo"+oldCheckTaskNo);
			// The connection URL
			String url = SERVICE_IP+"/ReceiveTask?checktaskno={1}&oldCheckTaskNo={2}&peopleno={3}";
			// Set the Accept header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAccept(Collections.singletonList(new MediaType("application","json")));
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
			
			// Create a new RestTemplate instance
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			RequestFactory.configTimeoutByReflect(restTemplate,  -1, 2*1000);//请求超时设置
			ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(url,
					HttpMethod.GET, requestEntity, LinkedHashMap.class,checktaskno,oldCheckTaskNo,peopleno);
			//Log.i(Tag, "TaskPoint："+responseEntity);
			if (responseEntity.getStatusCode().value()==200) {//判断状态码是否正常
				LinkedHashMap receiveJson = responseEntity.getBody();
				//Log.i(Tag, "任务点"+receiveJson);
				
				if ((int)receiveJson.get("state") == 200) {//效验成功
					
					List<TaskPointEntity> listTaskPointEntity = new ArrayList<>();
					
					//将LinkedHashMap对象反系列化为想要的对象
					List<LinkedHashMap<String, Object>> list = objectMapper.convertValue(receiveJson.get("data"), List.class);
					for (LinkedHashMap<String, Object> linkTaskPoint : list) {
						TaskPointEntity taskPointEntity = new TaskPointEntity();
						
						taskPointEntity.id = (String)linkTaskPoint.get("id");
						List locationGD = objectMapper.convertValue(linkTaskPoint.get("locationGD"),List.class);
						taskPointEntity.localhostLong = (double)locationGD.get(0);
						taskPointEntity.localhostLat = (double)locationGD.get(1);
						taskPointEntity.lineId = (String)linkTaskPoint.get("lineId");
						taskPointEntity.lineName = (String)linkTaskPoint.get("lineName");
						
						sendTaskPoint(taskPointEntity);//与UI通信
						
						
						listTaskPointEntity.add(taskPointEntity);
						
						boolean bl = taskPointDao.addTaskPoint(taskPointEntity);//将数据保存SQLite
						if (!bl) {
							sendException("android性能不够，请联系管理员");
						}
					}
					sendTaskPointList(listTaskPointEntity);
				}else if((int)receiveJson.get("state") == 400){//接受后端异常信息
					sendException((String)receiveJson.get("msg"));
				
				}else {//效验失败
					sendException("获取任务点失败，请联系管理员");
				}
				
			}else if(responseEntity.getStatusCode().value()==500){
				sendException("服务器后端报错500");
			}
		} catch (HttpClientErrorException e) {
			sendException("401非法响应");
			Log.e(Tag+".Exception", ""+e);
		}catch (ResourceAccessException e){
			sendException("网络异常，获取巡检任务点失败！");
			Log.e(Tag+".Exception", ""+e);
		}catch(Exception e){
			sendException("未知程序异常！");
			Log.e(Tag+".Exception", ""+e);
		}
    }
    /**向SignFragment发送任务点*/
    private void sendTaskPoint(TaskPointEntity taskPointEntity){
    	Intent intent = new Intent("com.kmnfsw.work.SignTaskPointService.sendTaskPoint");
    	intent.putExtra("taskPointEntity", taskPointEntity);
    	setPointLocalBroadcastManager.sendBroadcast(intent);
    }
    /**向SignFragment发送任务点集合*/
    private void sendTaskPointList(List<TaskPointEntity> listTaskPointEntity){
    	Intent intent = new Intent("com.kmnfsw.work.SignTaskPointService.sendListTaskPoint");
    	intent.putParcelableArrayListExtra("listTaskPointEntity", (ArrayList<? extends Parcelable>) listTaskPointEntity);
    	setPointLocalBroadcastManager.sendBroadcast(intent);
    }
    
    
    
    /**操作签到过的点线程**********************/
    class CheckedPoint implements Runnable{
		@Override
		public void run() {
			checkedPointDao.deleteCheckedPoint();
			operateCheckedPoint();
		}
    }
    private void operateCheckedPoint(){
    	try {
			// The connection URL 
			String url = SERVICE_IP+"/getNspectionPoint?cheskNo={1}&peopleno={2}";
			// Set the Accept header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAccept(Collections.singletonList(new MediaType("application","json")));
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
			
			// Create a new RestTemplate instance
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			RequestFactory.configTimeoutByReflect(restTemplate,  -1, 2*1000);//请求超时设置
			ResponseEntity<ReceiveJson> responseEntity = restTemplate.exchange(url,
					HttpMethod.GET, requestEntity, ReceiveJson.class,checktaskno,peopleno);
			
			if (responseEntity.getStatusCode().value()==200) {//判断状态码是否正常
				ReceiveJson receiveJson = responseEntity.getBody();
				//Log.i(Tag+".签到过的点", ""+receiveJson);
				
				if (receiveJson.getState() == 200) {//效验成功
					List<TaskPointEntity> listCheckPointEntity = new ArrayList<>();
					
					//将LinkedHashMap对象反系列化为想要的对象
					List<LinkedHashMap<String, Object>> list = objectMapper.convertValue(receiveJson.getData(), List.class);
					for (LinkedHashMap<String, Object> linkTaskPoint : list) {
						TaskPointEntity checkedPointEntity = new TaskPointEntity();
						
						checkedPointEntity.id = (String)linkTaskPoint.get("id");
						checkedPointEntity.localhostLong = (double)linkTaskPoint.get("localhostLong");
						checkedPointEntity.localhostLat = (double)linkTaskPoint.get("localhostLat");
						checkedPointEntity.lineId = (String)linkTaskPoint.get("lineId");
						checkedPointEntity.lineName = (String)linkTaskPoint.get("lineName");
						
						listCheckPointEntity.add(checkedPointEntity);
						
						sendCheckedPoint(checkedPointEntity);
						boolean b1 = checkedPointDao.addCheckedPoint(checkedPointEntity);
						if (!b1) {
							sendException("android性能不够，请联系管理员");
						}
					}
					sendCheckedList(listCheckPointEntity);
					
				}else if(receiveJson.getState() == 400){//效验失败
					sendException("获取签到过的点失败，请联系管理员");
				}
				
			}else if(responseEntity.getStatusCode().value()==500){
				sendException("服务器后端报错500");
			}
		} catch (HttpClientErrorException e) {
			sendException("401非法响应");
			Log.e(Tag+".Exception", ""+e);
		}catch (ResourceAccessException e){
			sendException("网络异常，获取签到过的点失败！");
			Log.e(Tag+".Exception", ""+e);
		}catch(Exception e){
			sendException("未知程序异常！");
			Log.e(Tag+".Exception", ""+e);
		}
    }
    /**向SignFragment发送签到点*/
    private void sendCheckedPoint(TaskPointEntity checkedPointEntity){
    	Intent intent = new Intent("com.kmnfsw.work.SignTaskPointService.sendCheckedPoint");
    	intent.putExtra("checkedPointEntity", checkedPointEntity);
    	setPointLocalBroadcastManager.sendBroadcast(intent);
    }
    
    /**向signFragment发送签到过点集合*/
    private void sendCheckedList(List<TaskPointEntity> listcheckedPointEntity){
    	Intent intent = new Intent("com.kmnfsw.work.SignTaskPointService.sendListCheckPoint");
    	intent.putParcelableArrayListExtra("listcheckedPointEntity", (ArrayList<? extends Parcelable>) listcheckedPointEntity);
    	setPointLocalBroadcastManager.sendBroadcast(intent);
    }
    
    
    /**向SignFragment发送异常信息*/
    private void sendException(String massgeEx){
    	Intent intent = new Intent("com.kmnfsw.work.SignTaskPointService.sendException");
    	intent.putExtra("massgeEx", massgeEx);
    	setPointLocalBroadcastManager.sendBroadcast(intent);//与sendBroadcast(intent);类同 
    }
    
    /**任务点接受广播*/
    class TaskReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
			case "com.kmnfsw.work.sign.SignFragment.selectTask":
				peopleno = intent.getStringExtra("peopleno");
				checktaskno = intent.getStringExtra("checktaskno");
				oldCheckTaskNo = intent.getStringExtra("oldCheckTaskNo");
				
				//执行任务
				if (mDecodeThreadPool !=null) {
		    		mDecodeThreadPool.execute(new TaskPoin());
		    		mDecodeThreadPool.execute(new CheckedPoint());
				}
				break;

			default:
				break;
			}
			
		}
    	
    }
    
    
    /** 服务不再有用且将要被销毁时调用 */
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	mDecodeThreadPool.shutdownNow();//关闭线程池
    	getTaskLocalBroadcastManager.unregisterReceiver(taskReceiver);//关闭广播
    	
    	Log.e(Tag, "销毁");
    }

}
