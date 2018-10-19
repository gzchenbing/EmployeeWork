package com.kmnfsw.work.welcomeLogin.service;

import java.util.Collections;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmnfsw.work.util.ProperTies;
import com.kmnfsw.work.util.ReceiveJson;
import com.kmnfsw.work.util.spring.RequestFactory;
import com.kmnfsw.work.welcomeLogin.entity.PeopleEntity;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class LoginService extends Service {
	
	private final static String Tag = ".welcomeLogin.service.LoginService";
	private final static int SERVICE_ID = 1;
	private String SERVICE_IP;

	/** 绑定的客户端接口 */
	private IBinder mBinder;
	private LoginCallBack callback;
	private ObjectMapper objectMapper = new ObjectMapper();
	
	private String peopleno; //peopleno, password, mac
	private String password;
	private String mac;
	
	private BackstageIsLoginCallBack backstageIsLoginCallBack;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mBinder = new MBinder();
		
		Properties proper = ProperTies.getProperties(getApplicationContext());
		SERVICE_IP = proper.getProperty("serverIp");
        //Log.i(Tag, "SERVICE_IP=" + SERVICE_IP);
	}
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	startForeground(SERVICE_ID, new Notification());
    	
    	return super.onStartCommand(intent, flags, startId);
    }

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	 /** 通过unbindService()解除所有客户端绑定时调用 */
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


	/**此绑定用于和activity交付*/
	public class MBinder extends Binder {
		///登录
		public void setLoginCallBack(LoginCallBack imple) {
			callback = imple;
		}
		public void loginCheck(String peopleno1, String password1,String mac1) {
			peopleno = peopleno1;
			password = password1;
			mac = mac1;
			
			threadLoginOperate();
		}
		
		
		////验证是否在后台已经登录过
		public void setBackstageIsLoginCallBack(BackstageIsLoginCallBack back){
			backstageIsLoginCallBack = back;
		}
		public void isBackstageLoginCallBack(final String peopleno){
			new Thread(){
				@Override
				public void run(){
					netIsBackstageLogin(peopleno);
				}
			}.start();
		}

	}
	
	private void threadLoginOperate(){
		new Thread(){
			@Override
			public void run() {
				netCheckInfo();
			};
		}.start();
	}
	
	
	/**网络与后端通信进行登录效验*/
	public void netCheckInfo(){
		try {
			// The connection URL 
			String url = SERVICE_IP+"/LoginSign?peopleno={1}&password={2}&devicemac={3}";
			// Set the Accept header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAccept(Collections.singletonList(new MediaType("application","json")));
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

			// Create a new RestTemplate instance
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			RequestFactory.configTimeoutByReflect(restTemplate, -1, 2*1000);//请求超时设置
			ResponseEntity<ReceiveJson> responseEntity = restTemplate.exchange(url,
					HttpMethod.GET, requestEntity, ReceiveJson.class,peopleno,password,mac);
			
			if (responseEntity.getStatusCode().value()==200) {//判断状态码是否正常
				ReceiveJson receiveJson = responseEntity.getBody();
				//Log.i(Tag+".ReceiveJson", ""+receiveJson);
				
				if (receiveJson.getState() == 200) {//效验成功
					
					//将LinkedHashMap对象反系列化为想要的对象
					PeopleEntity popleEntity = objectMapper.convertValue(receiveJson.getData(), PeopleEntity.class);
					
					callback.loginSuccess(popleEntity);
				}else if(receiveJson.getState() == 400){//效验失败
					callback.loginFailed(receiveJson.getMsg());
				}
				
			}
		} catch (HttpClientErrorException e) {
			callback.loginFailed("401非法响应");
			Log.e(Tag+".Exception", ""+e);
		}catch (ResourceAccessException e){
			callback.loginFailed("网络异常");
			Log.e(Tag+".Exception", ""+e);
		}catch (Exception e){
			callback.loginFailed("未知程序异常");
			Log.e(Tag, ""+e);
		}
	}
	/**网络请求判断后端该员工是否登录过*/
	private void netIsBackstageLogin(String peopleno){
		try {
			// The connection URL 
			String url = SERVICE_IP+"/isBackstageLogin?peopleno={1}";
			//String url ="http://192.168.0.36:8061/isBackstageLogin?peopleno={1}";
			// Set the Accept header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAccept(Collections.singletonList(new MediaType("application","json")));
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

			// Create a new RestTemplate instance
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			RequestFactory.configTimeoutByReflect(restTemplate,  -1, 2*1000);//请求超时设置
			ResponseEntity<ReceiveJson> responseEntity = restTemplate.exchange(url,
					HttpMethod.GET, requestEntity, ReceiveJson.class,peopleno);
			
			if (responseEntity.getStatusCode().value()==200) {//判断状态码是否正常
				ReceiveJson receiveJson = responseEntity.getBody();
				Log.i(Tag+".ReceiveJson", ""+receiveJson);
				
				if (receiveJson.getState() == 200) {//效验成功
					backstageIsLoginCallBack.isBackstageLogin((boolean)receiveJson.getData());
					
				}else if(receiveJson.getState() == 400){//效验失败
					backstageIsLoginCallBack.getIsBackstageLoginFail(receiveJson.getMsg());
				}
			}
		} catch (HttpClientErrorException e) {
			backstageIsLoginCallBack.getIsBackstageLoginFail("401非法响应");
			Log.e(Tag, ""+e);
		}catch (ResourceAccessException e){
			backstageIsLoginCallBack.getIsBackstageLoginFail("网络异常");
			Log.e(Tag, ""+e);
		}catch (Exception e){
			backstageIsLoginCallBack.getIsBackstageLoginFail("未知程序异常");
			Log.e(Tag, ""+e);
		}
	}
	
	public interface LoginCallBack {
		/**
		 * 登录成功
		 * @param uid
		 */
		public void loginSuccess(PeopleEntity peopleEntity);
		/**
		 * 登录失败
		 */
		public void loginFailed(String mag);
	}
	
	public abstract interface BackstageIsLoginCallBack{
		public void isBackstageLogin(boolean is);
		public void getIsBackstageLoginFail(String msg);
	}

	@Override
	public void onDestroy() {
		Log.e(Tag, "已销毁");
    }

}
