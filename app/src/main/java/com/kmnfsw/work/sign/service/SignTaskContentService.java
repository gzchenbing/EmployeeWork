package com.kmnfsw.work.sign.service;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Date;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.kmnfsw.work.sign.entity.TaskContentEntity;
import com.kmnfsw.work.sign.util.OperateTaskDate;
import com.kmnfsw.work.util.ProperTies;
import com.kmnfsw.work.util.spring.RequestFactory;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class SignTaskContentService extends IntentService{
	
	private static final String Tag = ".sign.service.SignTaskContentService";
	
	private String SERVICE_IP;

	private String peopleno;
	private String checkTaskNo;
	private LocalBroadcastManager mLocalBroadcastManager;
	private SharedPreferences shares;
	
	public SignTaskContentService() {
		 super("SignTaskContentService");
		 mLocalBroadcastManager = LocalBroadcastManager.getInstance( this );
	}
	
	@Override
	public void onCreate() {
		 super.onCreate();
		 Properties proper = ProperTies.getProperties(getApplicationContext());
		 SERVICE_IP = proper.getProperty("serverIp");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		shares = getSharedPreferences("config", Context.MODE_PRIVATE);
		
		peopleno = intent.getStringExtra("peopleno");
		checkTaskNo = intent.getStringExtra("checkTaskNo");
		
		new Thread(runnableNetgetTask).start();
	}
	
	private Runnable runnableNetgetTask = new Runnable() {
		
		@Override
		public void run() {
			try {
				// The connection URL 
				String url = SERVICE_IP+"/getTaskDetail?checktaskno={1}&peopleno={2}";
				//Log.i(Tag, "任务编号"+checkTaskNo);
				// Set the Accept header
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.setAccept(Collections.singletonList(new MediaType("application","json")));
				HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
				
				
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
				RequestFactory.configTimeoutByReflect(restTemplate,  -1, 2*1000);//请求超时设置
				ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(url,
						HttpMethod.GET, requestEntity, LinkedHashMap.class,checkTaskNo,peopleno);
				Log.i(Tag, ""+responseEntity);
				if (responseEntity.getStatusCode().value()==200) {//判断状态码是否正常
					LinkedHashMap<String,Object> entity = responseEntity.getBody();
					TaskContentEntity taskContentEntity  = new TaskContentEntity();
					
					String startTaskDate = (String)entity.get("starttaskdatetime");
					
			        String  endtaskdatetime = (String)entity.get("endtaskdatetime");
					
					taskContentEntity.peopleno = shares.getString("name", "")+"（"+peopleno+"）";

					taskContentEntity.startTaskDate = startTaskDate;
					taskContentEntity.endTaskDate = endtaskdatetime;
					taskContentEntity.linesName = (String)entity.get("linename");
					taskContentEntity.taskType = convertChecktype((Integer)entity.get("checktype"));
					taskContentEntity.taskCycl = OperateTaskDate.getCheckCycle(
							OperateTaskDate.getDateByStr(startTaskDate), OperateTaskDate.getDateByStr(endtaskdatetime));
					
					sendTaskContent(taskContentEntity);
				}else if(responseEntity.getStatusCode().value()==500){
					responseTaskException("服务器报错500");
				}
			} catch (HttpClientErrorException e) {
				Log.e(Tag+".Exception", ""+e);
				responseTaskException("401非法响应");
			}catch (ResourceAccessException e){
				Log.e(Tag+".Exception", ""+e);
				responseTaskException("网络异常，获取巡检任务失败！");
			}catch(Exception e){
				responseTaskException("未知程序异常！");
				Log.e(Tag+".Exception", ""+e);
			}
			
		}
	};
	
	private String convertChecktype(int checktype){
		if (checktype == 1) {
			return "巡视";
		}else if (checktype == 2) {
			return "维护";
		}
		else{
			return "";
		}
	}
	
	/**推送到SignTaskActivity*/
	private void sendTaskContent(TaskContentEntity taskContentEntity){
		Intent intent = new Intent("com.kmnfsw.work.sign.servic.SignTaskContentService.taskContent");
    	intent.putExtra("taskContentEntity", taskContentEntity);
    	mLocalBroadcastManager.sendBroadcast(intent);
	}
	
	private void responseTaskException(String exStr){
		Intent intent = new Intent("com.kmnfsw.work.sign.servic.SignTaskContentService.TaskException");
    	intent.putExtra("exceptionStr", exStr);
    	mLocalBroadcastManager.sendBroadcast(intent);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(Tag, "销毁");
	}

}
