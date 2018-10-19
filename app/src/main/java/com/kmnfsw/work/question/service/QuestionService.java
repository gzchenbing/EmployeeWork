package com.kmnfsw.work.question.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.kmnfsw.work.question.entity.QuestionEntity;
import com.kmnfsw.work.question.entity.QuestionTypeEntity;
import com.kmnfsw.work.question.rabbitmq.QuestionDataPushMQ;
import com.kmnfsw.work.question.util.androidTree.Node;
import com.kmnfsw.work.util.ProperTies;
import com.kmnfsw.work.util.ReceiveJson;
import com.kmnfsw.work.util.spring.RequestFactory;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * 问题服务
 * 
 * @author YanFaBu
 *
 */
public class QuestionService extends Service {
	private final static String Tag = ".question.service.QuestionService";

	private String SERVICE_IP;

	private ExTypeCallBack exTypeCallBack;
	private LnOrganizeCallBack lnOrganizeCallBack;
	public LocationCallBack locationCallBack;
	private QuestionReportCallBack questionReportCallBack;

	// 声明AMapLocationClient类对象
	private AMapLocationClient mLocationClient;
	// 声明AMapLocationClientOption对象
	private AMapLocationClientOption mLocationOption;
	private AMapLocation amapLocation;

	@Override
	public void onCreate() {
		super.onCreate();

		Properties proper = ProperTies.getProperties(getApplicationContext());
		SERVICE_IP = proper.getProperty("serverIp");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new MBinder();
	}

	public class MBinder extends Binder {
		//// 异常类型
		public void setExTypeCallBack(ExTypeCallBack back) {
			exTypeCallBack = back;
		}
		public void getExType() {
			threadGetExType();
		}

		//// 线路机构
		public void setLnOrganizeCallBack(LnOrganizeCallBack back) {
			lnOrganizeCallBack = back;
		}
		public void getLnOrganize() {
			threadGetLnOrganize();
		}

		/// 进行定位操作
		public void setLocationCallBack(LocationCallBack back) {
			locationCallBack = back;
		}
		public void getLocation() {
			new Thread() {
				@Override
				public void run() {
					executeLocation();
				};
			}.start();
		}

		//// 问题上报
		public void setQuestionReportCallBack(QuestionReportCallBack back) {
			questionReportCallBack = back;
		}
		public void reportQuestion(QuestionEntity questionEntity, int tag) {
			threadReportQuestion( questionEntity,  tag);
		}

	}

	/** 执行定位 */
	private void executeLocation() {
		// 初始化定位
		mLocationClient = new AMapLocationClient(getApplicationContext());
		// 设置定位回调监听
		mLocationClient.setLocationListener(mLocationListener);

		// 初始化AMapLocationClientOption对象
		mLocationOption = new AMapLocationClientOption();
		// 设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
		mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		// 设置定位间隔,单位毫秒,默认为2000ms
		mLocationOption.setInterval(2000);
		// 获取一次定位结果：
		// 该方法默认为false。0
		mLocationOption.setOnceLocation(true);
		mLocationOption.setOnceLocationLatest(true);
		// 关闭缓存机制
		mLocationOption.setLocationCacheEnable(false);
		// 给定位客户端对象设置定位参数
		mLocationClient.setLocationOption(mLocationOption);

		// 先停止定位
		mLocationClient.stopLocation();
		// 启动定位
		mLocationClient.startLocation();
	}

	private AMapLocationListener mLocationListener = new AMapLocationListener() {

		@Override
		public void onLocationChanged(AMapLocation amapLocation1) {
			if (amapLocation1 == null) {
				Log.e(Tag, "amapLocation is null!");
				locationCallBack.getLocationFail("定位失败！请检查网络、GPS是否打开");
				return;
			}
			if (amapLocation1.getErrorCode() != 0) {
				Log.e(Tag, "location Error, ErrCode:" + amapLocation1.getErrorCode() + ", errInfo:"
						+ amapLocation1.getErrorInfo());
				locationCallBack.getLocationFail("定位失败！请检查网络、GPS是否打开");
				return;
			}
			
			String exlocation = amapLocation1.getAddress();
			String exceptionlong = String.valueOf(amapLocation1.getLongitude());
			String exceptionlat = String.valueOf(amapLocation1.getLatitude());
			//Log.i(Tag, "exlocation；"+exlocation+"exceptionlong："+exceptionlong+"exceptionlat："+exceptionlat);
			
			locationCallBack.getLocationData(exlocation, exceptionlong, exceptionlat);

		}
	};

	private void threadGetExType() {
		new Thread() {
			@Override
			public void run() {
				netGetExType();

			}
		}.start();

	}

	/** 网络获取异常类型 */
	private void netGetExType() {
		try {
			// The connection URL
			String url = SERVICE_IP + "/showExType";
			//String url = "http://192.168.0.36:8061/showExType";
			// Set the Accept header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAccept(Collections.singletonList(new MediaType("application", "json")));
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

			// Create a new RestTemplate instance
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			RequestFactory.configTimeoutByReflect(restTemplate,  -1, 2*1000);//请求超时设置
			ResponseEntity<ReceiveJson> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
					ReceiveJson.class);
			//Log.i(Tag, ""+responseEntity);

			if (responseEntity.getStatusCode().value() == 200) {// 判断状态码是否正常
				ReceiveJson receiveJson = responseEntity.getBody();
				if (receiveJson.state == 200) {
					List<QuestionTypeEntity> listQuestionTypeEntity = new ArrayList<>();
					List<LinkedHashMap<String, Object>> listData = (List) receiveJson.data;
					for (LinkedHashMap<String, Object> linkedHashMap : listData) {
						QuestionTypeEntity questionTypeEntity = new QuestionTypeEntity();
						questionTypeEntity.extypeid = (String) linkedHashMap.get("extypeid");
						questionTypeEntity.extypename = (String) linkedHashMap.get("extypename");
						listQuestionTypeEntity.add(questionTypeEntity);
					}
					exTypeCallBack.getExTypeData(listQuestionTypeEntity);
				} else {
					exTypeCallBack.getExTypeDataFail(receiveJson.msg);
				}

			} else if (responseEntity.getStatusCode().value() == 500) {
				exTypeCallBack.getExTypeDataFail("服务器后端报错500");
			}
		} catch (HttpClientErrorException e) {
			exTypeCallBack.getExTypeDataFail("401非法响应");
			Log.e(Tag + ".Exception", "" + e);
		} catch (ResourceAccessException e) {
			exTypeCallBack.getExTypeDataFail("网络异常，获取问题类型失败！");
			Log.e(Tag + ".Exception", "" + e);
		} catch (Exception e) {
			exTypeCallBack.getExTypeDataFail("未知程序异常！");
			Log.e(Tag + ".Exception", "" + e);
		}
	}

	private void threadGetLnOrganize() {
		new Thread() {
			@Override
			public void run() {
				netGetLnOrganize();
			};
		}.start();
	}

	/** 获取服务器端的路线 */
	private void netGetLnOrganize() {
		try {
			// The connection URL
			String url = SERVICE_IP + "/getOrganizeRank";
			//String url =  "http://192.168.0.36:8061/getOrganizeRank";
			// Set the Accept header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAccept(Collections.singletonList(new MediaType("application", "json")));
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

			// Create a new RestTemplate instance
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			RequestFactory.configTimeoutByReflect(restTemplate,  -1, 2*1000);//请求超时设置
			ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
					LinkedHashMap.class);

			if (responseEntity.getStatusCode().value() == 200) {// 判断状态码是否正常
				LinkedHashMap<String, Object> resultData = responseEntity.getBody();
				if ((int) resultData.get("state") == 200) {
					List<LinkedHashMap<String, Object>> listResult = (List) resultData.get("list");
					if (listResult == null || listResult.size() == 0) {
						return;
					}

					lnOrganizeCallBack.getLnOrganizeData(JsonNnalyzeUtil.JsonLnOrganize(listResult));
				} else {
					lnOrganizeCallBack.getLnOrganizeDataFail("服务器后端报错");
				}

			} else if (responseEntity.getStatusCode().value() == 500) {
				lnOrganizeCallBack.getLnOrganizeDataFail("服务器后端报错500");
			}
		} catch (HttpClientErrorException e) {
			lnOrganizeCallBack.getLnOrganizeDataFail("401非法响应");
			Log.e(Tag + ".Exception", "" + e);
		} catch (ResourceAccessException e) {
			lnOrganizeCallBack.getLnOrganizeDataFail("网络异常，获取问题类型失败！");
			Log.e(Tag + ".Exception", "" + e);
		} catch (Exception e) {
			lnOrganizeCallBack.getLnOrganizeDataFail("未知程序异常！");
			Log.e(Tag + ".Exception", "" + e);
		}
	}
	
	private void threadReportQuestion(final QuestionEntity questionEntity, final int tag){
		new Thread(){
			@Override
			public void run() {
				netReportQuestion( questionEntity,  tag);
			};
		}.start();
	}
	/**网络问题上报*/
	private void netReportQuestion(QuestionEntity questionEntity, int tag){
		try {
			Log.i(Tag, "异常点id"+questionEntity.exceptionpointid);
			// The connection URL 
			String url =SERVICE_IP+"/reportException?tag={1}";
			//String url= "http://192.168.0.36:8061/reportException?tag={1}";
			// Set the Accept header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setContentType(new MediaType("application","json"));
			HttpEntity<QuestionEntity> requestEntity = new HttpEntity<QuestionEntity>(questionEntity, requestHeaders);
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			RequestFactory.configTimeoutByReflect(restTemplate,  -1, 2*1000);//请求超时设置
			ResponseEntity<ReceiveJson> responseEntity = restTemplate.exchange(url,
					HttpMethod.POST, requestEntity, ReceiveJson.class,tag);
			
			if (responseEntity.getStatusCode().value()==200) {//判断状态码是否正常
				ReceiveJson receiveJson = responseEntity.getBody();
				//Log.i(Tag+".ReceiveJson", ""+receiveJson);
				
				if (receiveJson.getState() == 200) {//效验成功
					String data = receiveJson.getMsg();
					questionReportCallBack.reportQuestionData(data);
					
					QuestionDataPushMQ.basicPublish(questionEntity, getApplicationContext());
					
				}else if(receiveJson.getState() == 400){//效验失败
					String msg = receiveJson.getMsg();
					questionReportCallBack.reportQuestionData(msg);
				}
				
			}else if(responseEntity.getStatusCode().value()==500){
				questionReportCallBack.reportQuestionData("服务器后端报错500");
			}
		} catch (HttpClientErrorException e) {
			questionReportCallBack.reportQuestionData("401非法响应");
			Log.e(Tag+".Exception", ""+e);
		}catch (ResourceAccessException e){
			questionReportCallBack.reportQuestionData("网络异常，签到失败！");
			Log.e(Tag+".Exception", ""+e);
//		}catch (TimeoutException e) {
//			questionReportCallBack.reportQuestionData("消息中间件连接超时！");
//			Log.e(Tag+".Exception", ""+e);
		}catch(Exception e){
			questionReportCallBack.reportQuestionData("未知程序异常！");
			Log.e(Tag+".Exception", ""+e);
		}
	}
	

	/** 获取服务器后端异常类型数据回调 */
	public interface ExTypeCallBack {
		public void getExTypeData(List<QuestionTypeEntity> listQuestionTypeEntity);

		public void getExTypeDataFail(String msg);
	}

	/** 获取服务器后端路线回调 */
	public interface LnOrganizeCallBack {
		public void getLnOrganizeData(LinkedList<Node> LineList);

		public void getLnOrganizeDataFail(String msg);
	}

	/** 问题上报 */
	public interface QuestionReportCallBack {
		public void reportQuestionData(String msg);
	}

	/** 定位 */
	public interface LocationCallBack {
		public void getLocationData(String exlocation, String exceptionlong, String exceptionlat);

		public void getLocationFail(String msg);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (null != mLocationClient) {
			mLocationClient.onDestroy();
		}
		Log.e(Tag, "服务销毁");
	}

}
