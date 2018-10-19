package com.kmnfsw.work.repair.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.kmnfsw.work.question.entity.QuestionTypeEntity;
import com.kmnfsw.work.question.service.QuestionService.MBinder;
import com.kmnfsw.work.repair.entity.AppointEntity;
import com.kmnfsw.work.util.ProperTies;
import com.kmnfsw.work.util.ReceiveJson;
import com.kmnfsw.work.util.spring.RequestFactory;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
/**
 * 初始化维修任务服务
 * @author YanFaBu
 *
 */
public class AppointService extends Service{

	private static final String Tag = ".repair.service.AppointService";
	
	private String SERVICE_IP;
	private SharedPreferences shares;
	
	private OneselfAppointCallBack oneselfAppointCallBack;
	private OtherAppointCallBack otherAppointCallBack;
	
	@Override
	public void onCreate() {
		super.onCreate();
		shares = getSharedPreferences("config", Context.MODE_PRIVATE);
		Properties proper = ProperTies.getProperties(getApplicationContext());
		SERVICE_IP = proper.getProperty("serverIp");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new MBinder();
	}
	
	public class MBinder extends Binder {
		public void setOneselfAppointCallBack(OneselfAppointCallBack back){
			oneselfAppointCallBack = back;
		}
		public void getOneselfAppoint(String peopleno){
			threadOneselfAppoint(peopleno);
		}
		
		public void setOtherAppointCallBack(OtherAppointCallBack back){
			otherAppointCallBack = back;
		}
		public void getOtherAppoint(String peopleno){
			threadOtherAppoint(peopleno);
		}
	}
	
	private void threadOtherAppoint(final String peopleno){
		new Thread(){
			@Override
			public void run() {
				netOtherAppoint(peopleno);
			};
		}.start();
	}
	
	/**获取他人指派任务*/
	private void netOtherAppoint(String peopleno){
		try {
			// The connection URL
			String url = SERVICE_IP+"/initTask?peopleno={1}";
			//String url = "http://192.168.0.36:8061/initTask?peopleno={1}";
			// Set the Accept header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAccept(Collections.singletonList(new MediaType("application", "json")));
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

			// Create a new RestTemplate instance
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			RequestFactory.configTimeoutByReflect(restTemplate,  -1, 2*1000);//请求超时设置
			ResponseEntity<List> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
					List.class, peopleno);

			//Log.i(Tag, "获取他人指派任务"+responseEntity);
			if (responseEntity.getStatusCode().value() == 200) {// 判断状态码是否正常
				List<LinkedHashMap<String, Object>> listData = responseEntity.getBody();
				List<AppointEntity> listAppointEntity = new ArrayList<>();
				for(LinkedHashMap<String, Object>  data : listData){
					AppointEntity appointEntity = new AppointEntity();
					appointEntity.checkrepairno = (String)data.get("checkrepairno");
					appointEntity.state = (int)data.get("state");
					appointEntity.releasedate = (String)data.get("releasedate");
					appointEntity.exLocation = (String)data.get("exLocation");
					appointEntity.extypename = (String)data.get("extypename");
					
					listAppointEntity.add(appointEntity);
				}
				otherAppointCallBack.getOtherAppointData(listAppointEntity);

			} else if (responseEntity.getStatusCode().value() == 500) {
				otherAppointCallBack.getOtherAppointFail("服务器后端报错500");
			}
		} catch (HttpClientErrorException e) {
			otherAppointCallBack.getOtherAppointFail("401非法响应");
			Log.e(Tag + ".Exception", "" + e);
		} catch (ResourceAccessException e) {
			otherAppointCallBack.getOtherAppointFail("网络异常！");
			Log.e(Tag + ".Exception", "" + e);
		} catch (Exception e) {
			otherAppointCallBack.getOtherAppointFail("未知程序异常！");
			Log.e(Tag + ".Exception", "" + e);
		}
	}
	
	
	private void threadOneselfAppoint(final String peopleno){
		new Thread(){
			@Override
			public void run() {
				netOneselfAppoint(peopleno);
			};
		}.start();
	}
	/**获取自行任务*/
	private void netOneselfAppoint(String peopleno){
		try {
			// The connection URL
			String url = SERVICE_IP+"/selfRepairTask?peopleno={1}";
			//String url = "http://192.168.0.36:8061/selfRepairTask?peopleno={1}";
			// Set the Accept header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAccept(Collections.singletonList(new MediaType("application", "json")));
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

			// Create a new RestTemplate instance
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			RequestFactory.configTimeoutByReflect(restTemplate,  -1, 2*1000);//请求超时设置
			ResponseEntity<List> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
					List.class, peopleno);
			//Log.i(Tag, "获取自行任务"+responseEntity);
			if (responseEntity.getStatusCode().value() == 200) {// 判断状态码是否正常
				List<LinkedHashMap<String, Object>> listData = responseEntity.getBody();
				List<AppointEntity> listAppointEntity = new ArrayList<>();
				for(LinkedHashMap<String, Object>  data : listData){
					AppointEntity appointEntity = new AppointEntity();
					appointEntity.checkrepairno = (String)data.get("checkrepairno");
					appointEntity.state = (int)data.get("state");
					appointEntity.releasedate = (String)data.get("releasedate");
					appointEntity.exLocation = (String)data.get("exLocation");
					appointEntity.extypename = (String)data.get("extypename");
					
					listAppointEntity.add(appointEntity);
				}
				oneselfAppointCallBack.getOneselfAppointData(listAppointEntity);

			} else if (responseEntity.getStatusCode().value() == 500) {
				oneselfAppointCallBack.getOneselfAppointFail("服务器后端报错500");
			}
		} catch (HttpClientErrorException e) {
			oneselfAppointCallBack.getOneselfAppointFail("401非法响应");
			Log.e(Tag + ".Exception", "" + e);
		} catch (ResourceAccessException e) {
			oneselfAppointCallBack.getOneselfAppointFail("网络异常！");
			Log.e(Tag + ".Exception", "" + e);
		} catch (Exception e) {
			oneselfAppointCallBack.getOneselfAppointFail("未知程序异常！");
			Log.e(Tag + ".Exception", "" + e);
		}
	}
	
	public abstract interface OneselfAppointCallBack{
		public void getOneselfAppointData(List<AppointEntity> listAppointEntity);
		public void getOneselfAppointFail(String msg);
	}
	
	public abstract interface OtherAppointCallBack{
		public void getOtherAppointData(List<AppointEntity> listAppointEntity);
		public void getOtherAppointFail(String msg);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		
		Log.e(Tag, "销毁！");
	}

}
