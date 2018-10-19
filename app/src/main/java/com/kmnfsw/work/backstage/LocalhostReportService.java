package com.kmnfsw.work.backstage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.kmnfsw.work.backstage.entity.SendLocalhostEntity;
import com.kmnfsw.work.backstage.mqtt.SendLocalhostMqtt;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/**
 * Apollo服务器http://192.168.0.18:61680
 * 
 * 巡检任务启动时启动此进程
 * 
 * @author YanFaBu
 *
 */
public class LocalhostReportService extends Service {
	private final static String Tag = ".backstage.LocalhostReportService";

	private final static int SERVICE_ID = 4;

	private static final int corePoolSize = 2;
	private static final int maximumPoolSize = Runtime.getRuntime().availableProcessors();;
	private final BlockingQueue<Runnable> mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
	private static final int keepAliveTime = 1;
	private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.DAYS;// 天

	/** 线程池管理器 */
	private ThreadPoolExecutor mDecodeThreadPool;

	// 声明AMapLocationClient类对象
	private AMapLocationClient mLocationClient;
	// 声明AMapLocationClientOption对象
	private AMapLocationClientOption mLocationOption;
	private AMapLocation amapLocation;
	/** 是否定位 */
	private boolean isLocation;

	private SharedPreferences shares;

	private WakeLock wakeLock;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/** 当服务被创建时调用. */
	@Override
	public void onCreate() {
		super.onCreate();
		// Log.i(Tag, "定位服务创建");

		/**
		 * 线程池知识见https://blog.csdn.net/l540675759/article/details/62230562
		 */
		mDecodeThreadPool = new ThreadPoolExecutor(corePoolSize, // Initial pool
																	// size
				maximumPoolSize, // Max pool size
				keepAliveTime, // 线程保活时间
				KEEP_ALIVE_TIME_UNIT, // 线程保活时间的类型
				mDecodeWorkQueue); // 任务队列
		isLocation = true;

		shares = getSharedPreferences("config", Context.MODE_PRIVATE);
	}

	/** 调用startService()启动服务时回调 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startForeground(SERVICE_ID, new Notification());
		// Log.i(Tag, "定位服务启动");
		acquireWakeLock();

		
		startOrSetLocation();
		mDecodeThreadPool.execute(new threadLocationClient());

		return super.onStartCommand(intent, flags, startId);
	}

	/** 配置参数并启动定位 */
	private void startOrSetLocation() {
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

		// 启动定位
		mLocationClient.startLocation();
	}

	class threadLocationClient implements Runnable {
		@Override
		public void run() {
			while (isLocation) {
				try {
					Thread.currentThread().sleep(2000);
				} catch (InterruptedException e) {
					Log.e(Tag + ".sleep", "" + e);
				}

				// 进行CPU唤醒机制
				if (null != wakeLock) {
					// Log.i(TAG, "call acquireWakeLock");
					wakeLock.acquire();
				}

				
				// 先停止定位
				mLocationClient.stopLocation();
				// 启动定位
				mLocationClient.startLocation();
			}

		}
	}

	private AMapLocationListener mLocationListener = new AMapLocationListener() {

		@Override
		public void onLocationChanged(AMapLocation amapLocation1) {
			if (amapLocation1 == null) {
				Log.e(Tag, "amapLocation is null!");
				return;
			}
			if (amapLocation1.getErrorCode() != 0) {
				Log.e(Tag, "location Error, ErrCode:" + amapLocation1.getErrorCode() + ", errInfo:"
						+ amapLocation1.getErrorInfo());
				return;
			}

			amapLocation = amapLocation1;

			mDecodeThreadPool.execute(new threadExecuteMQTT());
		}
	};

	/** 进行mqtt操作 */
	class threadExecuteMQTT implements Runnable {

		@Override
		public void run() {
			///// 对定位精度进行要求
			double Lat = amapLocation.getLatitude();// 获取纬度
			double Long = amapLocation.getLongitude();// 获取经度
			//Log.i(Tag, "Lat:" + Lat + " Long:" + Long);

			Double spf_Lat = (double) shares.getFloat("Lat", 0);
			Double spf_Long = (double) shares.getFloat("Long", 0);
			//Log.i(Tag, " spf_Lat:" + spf_Lat + ";spf_Long:" + spf_Long);
			LatLng old_point = new LatLng(spf_Lat, spf_Long);
			LatLng now_point = new LatLng(Lat, Long);
			int distance = (int) AMapUtils.calculateLineDistance(old_point, now_point);// 单位米
			//Log.i(Tag, "distance 移动的距离: " + distance);
			
			shares.edit().putFloat("Lat", (float) Lat).commit();
			shares.edit().putFloat("Long", (float) Long).commit();// 将经纬度加入历史shares

			if (distance > 100) {// 小于10米就终止
				return;
			}

			// 获取定位时间
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date(amapLocation.getTime());
			String timestr = df.format(date);
			// Log.i(Tag, "timestr:" + timestr);

			String peopleno = shares.getString("peopleno", "");// 员工编号
			String name = shares.getString("name", "");
			int peopleType = shares.getInt("peopleType", 0);// 工种类型
			String Mac = shares.getString("Mac", "");
			final SendLocalhostEntity entity = new SendLocalhostEntity(Lat, Long, peopleno, name, peopleType, timestr,
					Mac);

			if (!"".equals(peopleno) && peopleType != 0) {// 员工登录后并且不是维修工才执行
				// 将位置信息上传mqtt服务
				SendLocalhostMqtt slm = new SendLocalhostMqtt();
				slm.sendMagge(entity, getApplicationContext());
				slm.disconnectMqtt();
			}

		}

	}

	/** 唤醒CPU设备电源锁 */
	private void acquireWakeLock() {
		if (null == wakeLock) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
					getClass().getCanonicalName());

		}
	}

	/** 释放设备电源锁 */
	private void releaseWakeLock() {
		if (null != wakeLock && wakeLock.isHeld()) {
			// Log.i(TAG, "call releaseWakeLock");
			wakeLock.release();
			wakeLock = null;
		}
	}

	/** 服务不再有用且将要被销毁时调用 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		isLocation = false;
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			Log.e(Tag + ".sleep", "" + e);
		}

		if (null != mDecodeThreadPool) {
			mDecodeThreadPool.shutdownNow();// 关闭线程池
			mDecodeThreadPool = null;
		}
		if (null != mLocationClient) {
			mLocationClient.onDestroy();
		}
		releaseWakeLock();
		Log.e(Tag, "服务销毁");
	}

}
