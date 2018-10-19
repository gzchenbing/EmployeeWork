//package com.kmnfsw.work.backstage;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//
//import com.amap.api.location.AMapLocation;
//import com.amap.api.location.AMapLocationClient;
//import com.amap.api.location.AMapLocationClientOption;
//import com.amap.api.location.AMapLocationListener;
//import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
//import com.amap.api.maps.AMapUtils;
//import com.amap.api.maps.model.LatLng;
//import com.kmnfsw.work.backstage.entity.SendLocalhostEntity;
//import com.kmnfsw.work.backstage.mqtt.SendLocalhostMqtt;
//
//import android.app.job.JobInfo;
//import android.app.job.JobParameters;
//import android.app.job.JobScheduler;
//import android.app.job.JobService;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//
///** 位置时时定位上报 */
//public class LocalhostReportService1 extends JobService {
//
//	private final static String Tag = ".backstage.service.LocalhostReportService";
//
//	private JobParameters jobParameters;
//	// 声明AMapLocationClient类对象
//	private AMapLocationClient mLocationClient;
//	// 声明AMapLocationClientOption对象
//	private AMapLocationClientOption mLocationOption;
//
//	@Override
//	public boolean onStartJob(JobParameters params) {
//		this.jobParameters = params;
//		handler.sendMessage(Message.obtain(handler, 1, params));// 进行绑定执行另一个线程
//		return true;
//	}
//
//	private Handler handler = new Handler(new Handler.Callback() {
//
//		@Override
//		public boolean handleMessage(Message msg) {
//			/* 实施定位操作 *****************/
//			// 初始化定位
//			mLocationClient = new AMapLocationClient(getApplicationContext());
//			// 设置定位回调监听
//			mLocationClient.setLocationListener(mLocationListener);
//			// 初始化AMapLocationClientOption对象
//			mLocationOption = new AMapLocationClientOption();
//			// 设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
//			mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
//			// 设置定位间隔,单位毫秒,默认为2000ms
//			mLocationOption.setInterval(2000);
//			// 获取一次定位结果：
//			// 该方法默认为false。
//			mLocationOption.setOnceLocation(true);
//			mLocationOption.setOnceLocationLatest(true);
//			// 关闭缓存机制
//			mLocationOption.setLocationCacheEnable(false);
//			// 给定位客户端对象设置定位参数
//			mLocationClient.setLocationOption(mLocationOption);
//			// 启动定位
//			mLocationClient.startLocation();
//
//			return true;
//		}
//	});
//	// 声明定位回调监听器
//	private AMapLocationListener mLocationListener = new AMapLocationListener() {
//
//		@Override
//		public void onLocationChanged(AMapLocation amapLocation) {
//			if (amapLocation == null) {
//				Log.e(Tag, "amapLocation is null!");
//				return;
//			}
//			if (amapLocation.getErrorCode() != 0) {
//				Log.e(Tag, "location Error, ErrCode:" + amapLocation.getErrorCode() + ", errInfo:"
//						+ amapLocation.getErrorInfo());
//				return;
//			}
//			double Lat = amapLocation.getLatitude();// 获取纬度
//			double Long = amapLocation.getLongitude();// 获取经度
//
//			SharedPreferences shares = getSharedPreferences("config", Context.MODE_PRIVATE);
//			Double spf_Lat = (double) shares.getFloat("Lat", 0);
//			Double spf_Long = (double) shares.getFloat("Long", 0);
//			Log.i(Tag, "spf_Lat:" + spf_Lat + ";spf_Long:" + spf_Long);
//			LatLng old_point = new LatLng(spf_Lat, spf_Long);
//			LatLng now_point = new LatLng(Lat, Long);
//			float distance = AMapUtils.calculateLineDistance(old_point, now_point);// 单位米
//			Log.i(Tag, "distance 移动的距离:	" + distance);
////			if (distance < 10) {// 小于10米就终止
////
////				// 递归再次创建JobService用于定位上报
////				jobFinished(jobParameters, false);
////				reSetSchedul();
////
////				return;
////			}
//
////			shares.edit().remove("Lat").commit();
////			shares.edit().remove("Long").commit();
//
//			shares.edit().putFloat("Long", (float) Long).commit();// 将经纬度加入历史shares
//			shares.edit().putFloat("Lat", (float) Lat).commit();
//
//			// 获取定位时间
//			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			Date date = new Date(amapLocation.getTime());
//			String timestr = df.format(date);
//
//			String peopleno = shares.getString("peopleno", "");// 员工编号
//			int peopleType = shares.getInt("peopleType", 0);// 工种类型
//			String Mac = shares.getString("Mac", "");
//			final SendLocalhostEntity entity = new SendLocalhostEntity(Lat, Long, peopleno,"", peopleType, timestr, Mac);
//
//			if (!"".equals(peopleno) && peopleType != 0) {// 员工登录后并且不是维修工才执行
//				new Thread() {
//					public void run() {
//						// 将位置信息上传mqtt服务
//						SendLocalhostMqtt slm = new SendLocalhostMqtt();
//						slm.sendMagge(entity,getApplicationContext());
//						slm.disconnectMqtt();
//
//						// 递归再次创建JobService用于定位上报
//						jobFinished(jobParameters, false);
//						reSetSchedul();
//					};
//				}.start();
//			}
//
//		}
//	};
//	
//	
//
//	/**
//	 * 重新设置JobService，并启动
//	 */
//	private void reSetSchedul() {
//		JobScheduler mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
//		JobInfo.Builder builder = new JobInfo.Builder(1,
//				new ComponentName(getPackageName(), LocalhostReportService1.class.getName()));
//		List<JobInfo> jobs = mJobScheduler.getAllPendingJobs();
//		Log.i(Tag, "job数"+jobs.size());
//		builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // 设置需要的网络条件，默认NETWORK_TYPE_NONE
//		//builder.setPeriodic(1000);//设置间隔时间
//		builder.setMinimumLatency(1000);// 设置任务运行最少延迟时间
//		builder.setOverrideDeadline(2000);// 设置deadline，若到期还没有达到规定的条件则会开始执行
//		builder.setRequiresCharging(true);// 设置是否充电的条件,默认false
//		builder.setRequiresDeviceIdle(true);// 设置手机是否空闲的条件,默认false
//		builder.setPersisted(false);//设备重启之后你的任务是否还要继续执行
//		if (mJobScheduler.schedule(builder.build()) <= 0) {
//			// If something goes wrong
//			Log.e(Tag, "JobScheduler is error!");
//		} else {
//			Log.i(Tag, "JobScheduler go to run!");
//		}
//	}
//
//	@Override
//	public boolean onStopJob(JobParameters params) {
//		handler.removeCallbacksAndMessages(null);
//		return false;
//	}
//	/** 服务不再有用且将要被销毁时调用 */
//    @Override
//    public void onDestroy() {
//    	super.onDestroy();
//    	Log.e(Tag, "服务销毁！");
//    	
//    }
//
//}
