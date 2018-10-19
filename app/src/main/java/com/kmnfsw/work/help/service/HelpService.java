package com.kmnfsw.work.help.service;


import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
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

import com.kmnfsw.work.backstage.LocalhostReportService;
import com.kmnfsw.work.backstage.RabbitmqListenService;
import com.kmnfsw.work.sign.db.dao.CheckedPointDao;
import com.kmnfsw.work.sign.db.dao.TaskPointDao;
import com.kmnfsw.work.util.ProperTies;
import com.kmnfsw.work.util.ReceiveJson;
import com.kmnfsw.work.util.ServiceState;
import com.kmnfsw.work.util.spring.RequestFactory;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class HelpService extends Service{
	private static final String Tag = ".help.service.HelpService";

	private String SERVICE_IP;
	
	private Binder mBinder;
	private SharedPreferences shares;
	
	private EscCallback escCallback;
	
	private static final int corePoolSize = 2;
    private static final int maximumPoolSize = Runtime.getRuntime().availableProcessors();;
    private final BlockingQueue<Runnable> mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
    private static final int keepAliveTime = 1;
    private static final  TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.DAYS;//天
    private ThreadPoolExecutor mDecodeThreadPool;
	
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
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		shares = getSharedPreferences("config", Context.MODE_PRIVATE);
		mBinder = new MBinder();
		return mBinder;
	}
	
	public class MBinder extends Binder {
		public void setCheckEscCallback(EscCallback back){
			escCallback = back;
		}
		/**巡检工退出操作*/
		public void operateCheckEsc(){
			mDecodeThreadPool.execute(runnableCheckEsc);
		}
		
		
		public void setDefaultEscCallback(EscCallback back){
			escCallback = back;
		}
		/**未知员工退出操作*/
		public void operateDefaultEsc(){
			mDecodeThreadPool.execute(runnableDefaultEsc);
		}
	}
	private Runnable runnableCheckEsc = new Runnable() {
		@Override
		public void run() {
			netCheckEsc();
		}
	};
	
	private Runnable runnableDefaultEsc = new Runnable() {
		
		@Override
		public void run() {
			netDefaultEsc();
			
		}
	};
	
	
	
	/**网络操作巡检工退出*/
	private void netCheckEsc(){
		try {
			String checkTaskNo = shares.getString("checkTaskNo", "");
			String peopleno = shares.getString("peopleno", "");
			
			// The connection URL 
			String url = SERVICE_IP+"/SignOut?taskNo={1}&peopleno={2}";
			// Set the Accept header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAccept(Collections.singletonList(new MediaType("application","json")));
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
			
			
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			RequestFactory.configTimeoutByReflect(restTemplate,  -1, 2*1000);//请求超时设置
			ResponseEntity<ReceiveJson> responseEntity = restTemplate.exchange(url,
					HttpMethod.GET, requestEntity, ReceiveJson.class,checkTaskNo,peopleno);
			
			if (responseEntity.getStatusCode().value()==200) {//判断状态码是否正常
				ReceiveJson receiveJson = responseEntity.getBody();
				boolean isSuccess = (boolean)receiveJson.getData();
				boolean isClear = clearSignData();
				if (isClear && isSuccess) {
					escCallback.escDataSuccess(true);
				}else{
					escCallback.escDataSuccess(false);
				}
				
			}else if(responseEntity.getStatusCode().value()==500){
				escCallback.escDataError("服务器报错500");
			}
		} catch (HttpClientErrorException e) {
			escCallback.escDataError("401非法响应");
			Log.e(Tag+".Exception", ""+e);
		}catch (ResourceAccessException e){
			escCallback.escDataError("网络异常，获取巡检任务失败！");
			Log.e(Tag+".Exception", ""+e);
		}catch(Exception e){
			escCallback.escDataError("未知程序异常！");
			Log.e(Tag+".Exception", ""+e);
		}
	}
	/**清除遗留下的静态数据*/
	private boolean clearSignData(){
		//去除任务
		shares.edit().remove("checkTaskNo").commit();
		shares.edit().remove("oldCheckTaskNo").commit();
		shares.edit().remove("checkPlanNo").commit();
		shares.edit().remove("peopleType").commit();
		
		//去除定位经纬度
		shares.edit().remove("whilst_longtitud").commit();
		shares.edit().remove("whilst_latitud").commit();
		
		//停止推送定位服务
		closeService(LocalhostReportService.class, "com.kmnfsw.work.backstage.LocalhostReportService");
		closeService(RabbitmqListenService.class, "com.kmnfsw.work.backstage.RabbitmqListenService");
		shares.edit().remove("Long").commit();
		shares.edit().remove("Lat").commit();
		
		shares.edit().remove("islogin").commit();//去除登录标记
		
		//删除数据库SQLite中任务点和签到点的数据
		TaskPointDao taskPointDao = new TaskPointDao(this);
		CheckedPointDao checkedPointDao = new CheckedPointDao(this);
		boolean a = taskPointDao.deleteTaskPoint();
		boolean b = checkedPointDao.deleteCheckedPoint();
		if (a && b) {
			return true;
		}else{
			return false;
		}
	}
	
	/**网络操作未知员工退出*/
	private void netDefaultEsc(){
		try {
			String peopleno = shares.getString("peopleno", "");
			
			// The connection URL 
			String url = SERVICE_IP+"/repairOut?peopleno={1}";
			// Set the Accept header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAccept(Collections.singletonList(new MediaType("application","json")));
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
			
			
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			RequestFactory.configTimeoutByReflect(restTemplate,  -1, 2*1000);//请求超时设置
			ResponseEntity<ReceiveJson> responseEntity = restTemplate.exchange(url,
					HttpMethod.GET, requestEntity, ReceiveJson.class,peopleno);
			
			if (responseEntity.getStatusCode().value()==200) {//判断状态码是否正常
				shares.edit().remove("islogin").commit();//去除登录标记
				
				ReceiveJson receiveJson = responseEntity.getBody();
				boolean isSuccess = (boolean)receiveJson.getData();
				if (isSuccess) {
					//关闭消息队列服务
					closeService(RabbitmqListenService.class, "com.kmnfsw.work.backstage.RabbitmqListenService");
					escCallback.escDataSuccess(true);
				}else{
					escCallback.escDataSuccess(false);
				}
				
			}else if(responseEntity.getStatusCode().value()==500){
				escCallback.escDataError("服务器报错500");
			}
		} catch (HttpClientErrorException e) {
			escCallback.escDataError("401非法响应");
			Log.e(Tag+".Exception", ""+e);
		}catch (ResourceAccessException e){
			escCallback.escDataError("网络异常，获取巡检任务失败！");
			Log.e(Tag+".Exception", ""+e);
		}catch(Exception e){
			escCallback.escDataError("未知程序异常！");
			Log.e(Tag+".Exception", ""+e);
		}
	}
	
	/**关闭某服务*/
	private void closeService(Class<?> cls,String clsStr){
		//签到过的点服务是否开启
		boolean isRunningSignInitPoint = ServiceState.isServiceRunning(this, clsStr);
		if (isRunningSignInitPoint) {
			stopService(new Intent(this, cls));//停止服务
		}
				
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		mDecodeThreadPool.shutdownNow();//关闭线程池
		Log.e(Tag, "销毁");
    }
	
	public interface EscCallback{
		public void escDataSuccess(boolean isSuccess);
		
		public void escDataError(String msgEx);
	}

}
