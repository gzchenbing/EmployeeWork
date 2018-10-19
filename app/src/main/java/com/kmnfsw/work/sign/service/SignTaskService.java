package com.kmnfsw.work.sign.service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmnfsw.work.sign.db.dao.CheckedPointDao;
import com.kmnfsw.work.sign.db.dao.TaskPointDao;
import com.kmnfsw.work.sign.entity.CheckTaskEntity;
import com.kmnfsw.work.sign.entity.SignLocationEntity ;
import com.kmnfsw.work.sign.entity.TaskPointEntity;
import com.kmnfsw.work.sign.rabbitmq.SignDataPushMQ;
import com.kmnfsw.work.util.ProperTies;
import com.kmnfsw.work.util.ReceiveJson;
import com.kmnfsw.work.util.spring.RequestFactory;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class SignTaskService extends Service {
	
	private static final String Tag = ".sign.service.SignTaskService";
	private final static int SERVICE_ID = 2;
	private String SERVICE_IP;
	
	private IBinder mBinder;

	private ObjectMapper objectMapper = new ObjectMapper();
	
	private CheckTaskCallback checkTaskCallback;
	private LocationNearbyPointCallback locationNearbyPointCallback;
	private SignDataCallback signDataCallback;
	private SignTaskPerCallback signTaskPerCallback;
	
	private List<TaskPointEntity> listPoint;
	/**定位坐标点*/
	private LatLng Localhost;
	private int radius;
	
	private SignLocationEntity  checkedPointEntity;
	private TaskPointEntity taskPointEntity;
	
	private SharedPreferences shares;
	
	@Override
	public IBinder onBind(Intent intent) {
		mBinder = new MBinder();
		return mBinder;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		shares = getSharedPreferences("config", Context.MODE_PRIVATE);
		
		Properties proper = ProperTies.getProperties(getApplicationContext());
		SERVICE_IP = proper.getProperty("serverIp");
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	startForeground(SERVICE_ID, new Notification());
    	
    	
    	return super.onStartCommand(intent, flags, startId);
    }


	/** 此绑定用于和activity交付 */
	public class MBinder extends Binder {
		//////获取所有签到任务
		public void setCheckTaskCallback(CheckTaskCallback back){
			checkTaskCallback= back;
		}
		public void getCheckTask(String peopleno){
			threadGetCheckTask(peopleno);
		}
		
		
		////////获取定位点半径周边的点
		public void setLocationNearbyPointCallback(LocationNearbyPointCallback back){
			locationNearbyPointCallback = back;
		}
		public void getLocationNearbyPoint(List<TaskPointEntity> p,LatLng l
				,int r){
			listPoint = p;
			Localhost = l;
			radius = r;
			threadGetLocationNearbyPoint();
		}
		
		
		///////签到——》向后台传数据
		public void setSignDataCallback(SignDataCallback back){
			signDataCallback = back;
		}
		public void getSignCallback(SignLocationEntity  checkPoint,TaskPointEntity taskPoint){
			checkedPointEntity = checkPoint;
			taskPointEntity = taskPoint;
			threadGetSignCallback();
		}
		
		
		///////获取某任务的进度
		public void setSignTaskPerCallback(SignTaskPerCallback back){
			signTaskPerCallback = back;
		}
		public void getSignTaskPerCallback(String taskNo,String peopleno){
			threadSignTaskPer(taskNo,peopleno);
		}
		
	}
	
	private void threadSignTaskPer(final String taskNo,final String peopleno){
		new Thread(){
			@Override
			public void run() {
				netSignTaskPer(taskNo,peopleno);
			};
		}.start();
	}
	/**获取任务进度*/
	private void netSignTaskPer(String taskNo,String peopleno){
		try {
			// The connection URL 
			String url = SERVICE_IP+"/showTaskProgress?checktaskno={1}&peopleno={2}";
			// Set the Accept header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAccept(Collections.singletonList(new MediaType("application","json")));
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
			
			
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			RequestFactory.configTimeoutByReflect(restTemplate, 10*1000, 2*1000);//请求超时设置
			ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(url,
					HttpMethod.GET, requestEntity, LinkedHashMap.class,taskNo,peopleno);
			
			if (responseEntity.getStatusCode().value()==200) {//判断状态码是否正常
				LinkedHashMap<String,Object> entity = responseEntity.getBody();
				//Log.i(Tag, "任务进度："+entity);
				int totalPointSize = (int)entity.get("totalPointSize");
				int signSize = (int)entity.get("signSize");
				
				//计算百分比
				double f = ((double)signSize/totalPointSize)*100;
				//Log.i(Tag, "小数为"+f);
				DecimalFormat df = new DecimalFormat("#.0");
				String per = df.format(f);
				
				signTaskPerCallback.getSignTaskPer(totalPointSize, signSize, per);
			}else if(responseEntity.getStatusCode().value()==500){
				checkTaskCallback.getTaskFailed("服务器报错500");
			}
		} catch (HttpClientErrorException e) {
			checkTaskCallback.getTaskFailed("401非法响应");
			Log.e(Tag+".Exception", ""+e);
		}catch (ResourceAccessException e){
			checkTaskCallback.getTaskFailed("网络异常，获取巡检任务失败！");
			Log.e(Tag+".Exception", ""+e);
		}catch(Exception e){
			checkTaskCallback.getTaskFailed("未知程序异常！");
			Log.e(Tag+".Exception", ""+e);
		}
	}
	
	/**创建线程*/
	private void threadGetCheckTask(final String peopleno){
		new Thread() {
			@Override
			public void run() {
				netGetCheckTask(peopleno);
			}
		}.start();
	}
	/**获取所有巡检任务执行网络通信*/
	private void netGetCheckTask(String peopleno){
		try {
			// The connection URL 
			String url = SERVICE_IP+"/initAllTask?peopleno={1}";
			// Set the Accept header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAccept(Collections.singletonList(new MediaType("application","json")));
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
			
			// Create a new RestTemplate instance
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			RequestFactory.configTimeoutByReflect(restTemplate,  -1, 2*1000);//请求超时设置
			ResponseEntity<List> responseEntity = restTemplate.exchange(url,
					HttpMethod.GET, requestEntity, List.class,peopleno);
			
			if (responseEntity.getStatusCode().value()==200) {//判断状态码是否正常
				List<LinkedHashMap<String, Object>> list = responseEntity.getBody();
				//Log.i(Tag+".初始化巡检任务", ""+list);
				List<CheckTaskEntity> entityList = new ArrayList<CheckTaskEntity>();
				for (LinkedHashMap<String, Object> linkedHashMap : list) {
					CheckTaskEntity checkTaskEntity = new CheckTaskEntity();
					
					checkTaskEntity.checkTaskNo = (String) linkedHashMap.get("checktaskno");
					checkTaskEntity.lineRoadList = (String) linkedHashMap.get("organizename");
					checkTaskEntity.peopleType =  Integer.valueOf((String) linkedHashMap.get("checktype"));
					checkTaskEntity.checkPlanNo = (String) linkedHashMap.get("checkPlanNo");
					
					entityList.add(checkTaskEntity);
				}
				checkTaskCallback.getTask(entityList);
				
			}else if(responseEntity.getStatusCode().value()==500){
				checkTaskCallback.getTaskFailed("服务器后端报错500");
			}
		} catch (HttpClientErrorException e) {
			checkTaskCallback.getTaskFailed("401非法响应");
			Log.e(Tag+".Exception", ""+e);
		}catch (ResourceAccessException e){
			checkTaskCallback.getTaskFailed("网络异常，获取巡检任务失败！");
			Log.e(Tag+".Exception", ""+e);
		}catch(Exception e){
			checkTaskCallback.getTaskFailed("未知程序异常！");
			Log.e(Tag+".Exception", ""+e);
		}
	}
	
	
	void threadGetLocationNearbyPoint(){
		new Thread(){
			@Override
			public void run() {
				HashMap<String,TaskPointEntity> nearbyPoints = new HashMap<String,TaskPointEntity>();
				for (TaskPointEntity taskPointEntity : listPoint) {
					float distance = AMapUtils.calculateLineDistance(Localhost,new LatLng(
							taskPointEntity.localhostLat, taskPointEntity.localhostLong));
					//Log.i(Tag, "定位点与周边点的距离"+distance);
					if (Math.abs(distance)<=radius ) {
						nearbyPoints.put(taskPointEntity.id, taskPointEntity);
					}
				}
				locationNearbyPointCallback.getLocationNearbyPoint(nearbyPoints);
			};
		}.start();
	}
	
	
	void threadGetSignCallback(){
		new Thread(){
			@Override
			public void run() {
				netdGetSignCallback();
			};
		}.start();
	}
	/**签到 @RequestBody*/
	void netdGetSignCallback(){
		try {
			// The connection URL 
			String url =SERVICE_IP+"/PeopleSign";
			// Set the Accept header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setContentType(new MediaType("application","json"));
			//Log.i(Tag, "签到数据："+checkedPointEntity);
			HttpEntity<SignLocationEntity> requestEntity = new HttpEntity<SignLocationEntity>(checkedPointEntity, requestHeaders);
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			RequestFactory.configTimeoutByReflect(restTemplate,  -1, 2*1000);//请求超时设置
			ResponseEntity<ReceiveJson> responseEntity = restTemplate.exchange(url,
					HttpMethod.POST, requestEntity, ReceiveJson.class);
			
			if (responseEntity.getStatusCode().value()==200) {//判断状态码是否正常
				ReceiveJson receiveJson = responseEntity.getBody();
				//Log.i(Tag+".ReceiveJson", ""+receiveJson);
				
				if (receiveJson.getState() == 200) {//效验成功
					signDataCallback.getSignDataCallback("签到成功");
					
					//操作存入sqLite
					CheckedPointDao checkedPointDao = new CheckedPointDao(getApplication());
					checkedPointDao.addCheckedPoint(taskPointEntity);
					
					checkedPointEntity.name = shares.getString("name", "");
					String signData = objectMapper.writeValueAsString(checkedPointEntity);//系列化为字符串
					int peopleType = shares.getInt("peopleType", 0);// 工种类型
					if (peopleType == 0) {
						return;
					}
					//进行rabbitmq推送
					SignDataPushMQ.basicPublish(signData, peopleType, getApplicationContext());
					
					//得出任务进度
					TaskPointDao taskPointDao = new TaskPointDao(getApplication());
					int signSize = checkedPointDao.getCheckedPointAllNumber();
					int totalPointSize = taskPointDao.getTaskPointAllNumber();
					//Log.i(Tag, "总条数"+totalPointSize);
					//Log.i(Tag, "签到的条数"+signSize);
					//计算百分比
					double f = ((double)signSize/totalPointSize)*100;
					//Log.i(Tag, "小数"+f);
					DecimalFormat df = new DecimalFormat("#.0");
					String per = df.format(f);
					signDataCallback.getSignTaskPer(totalPointSize, signSize, per);
					
				}else if(receiveJson.getState() == 400){//效验失败
					signDataCallback.getSignDataCallbackFailed("签到失败，请联系管理员");
				}
				
			}else if(responseEntity.getStatusCode().value()==500){
				signDataCallback.getSignDataCallbackFailed("服务器后端报错500");
			}
		} catch (HttpClientErrorException e) {
			signDataCallback.getSignDataCallbackFailed("401非法响应");
			Log.e(Tag+".Exception", ""+e);
		}catch (ResourceAccessException e){
			signDataCallback.getSignDataCallbackFailed("网络异常，签到失败！");
			Log.e(Tag+".Exception", ""+e);
		} catch (TimeoutException e) {
			signDataCallback.getSignDataCallbackFailed("消息中间件连接超时！");
			Log.e(Tag+".Exception", ""+e);
		}catch(Exception e){
			checkTaskCallback.getTaskFailed("未知程序异常！");
			Log.e(Tag+".Exception", ""+e);
		}
	}
	
	
	public interface CheckTaskCallback{
		public void getTask(List<CheckTaskEntity> entityList);
		public void getTaskFailed(String massge);
	}
	public interface SignTaskPerCallback{
		public void getSignTaskPer(int totalPointSize,int signSize,String per);
	}
	
	public interface LocationNearbyPointCallback{
		public void getLocationNearbyPoint(HashMap<String,TaskPointEntity> nearbyPoints);
	}
	public interface SignDataCallback{
		public void getSignDataCallback(String massage);
		public void getSignTaskPer(int totalPointSize,int signSize,String per);
		public void getSignDataCallbackFailed(String massageEx);
	}
	

	/** 服务不再有用且将要被销毁时调用 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(Tag, "服务销毁");
	}

}
