package com.kmnfsw.work.repair;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.xutils.BuildConfig;
import org.xutils.x;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.INaviInfoCallback;
import com.amap.api.navi.model.AMapNaviLocation;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.kmnfsw.work.R;
import com.kmnfsw.work.question.util.MediaManager;
import com.kmnfsw.work.repair.entity.AppointDetailsEntity;
import com.kmnfsw.work.repair.service.AppointDetailsService;
import com.kmnfsw.work.repair.service.AppointDetailsService.AppointDetailsCallBack;
import com.kmnfsw.work.repair.service.AppointDetailsService.MBinder;
import com.kmnfsw.work.repair.view.NavigationDialog;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

public class AppointDetailsActivity extends Activity {

	private static final String Tag = ".repair.AppointDetailsActivity";
	private SharedPreferences shares;

	private TextView big_img_title;
	private RelativeLayout big_imgv_leftbtn;
	private RelativeLayout big_imgv_rightbtn;
	private TextView appoint_details_repairNo;
	private TextView appoint_details_state;
	private TextView appoint_details_exTypeName;
	private TextView appoint_details_exLocation;
	private TextView appoint_details_releaseDate;
	private TextView appoint_details_des;
	private Button appoint_receive;
	private Button appoint_navigation;
	private ImageView appoint_details_pic;
	private FrameLayout appoint_voice_btn;
	private View appoint_details_voice;

	private String checkrepairno;
	private String peopleno;
	private MBinder mBinder;

	/** 起始坐标点 */
	private Poi startPoi;

	/** 图片位置 */
	private String picPath;
	/** 语音位置 */
	private String voicePath;

	/** 任务详情实体 */
	private AppointDetailsEntity appointDetails;

	private LocalBroadcastManager setLocalBroadcastManager;

	/** 正在接受任务 */
	private boolean isAppoint_receiveing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		x.Ext.init(this.getApplication());// 初始化xutils框架
		x.Ext.setDebug(BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.

		// 去除标题头部
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 解决软盘弹出影响布局
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.appoint_details_activity);

		shares = getSharedPreferences("config", Context.MODE_PRIVATE);
		setLocalBroadcastManager = LocalBroadcastManager.getInstance(this);// 设置广播实例

		Intent intent = getIntent();
		checkrepairno = intent.getStringExtra("checkrepairno");
		peopleno = shares.getString("peopleno", "");

		initView();
		Intent service = new Intent(this, AppointDetailsService.class);
		bindService(service, conn, Context.BIND_AUTO_CREATE);
	}

	/** 初始化视图 */
	private void initView() {
		big_img_title = (TextView) findViewById(R.id.big_img_title);
		big_imgv_leftbtn = (RelativeLayout) findViewById(R.id.big_imgv_leftbtn);
		big_imgv_rightbtn = (RelativeLayout) findViewById(R.id.big_imgv_rightbtn);
		appoint_details_repairNo = (TextView) findViewById(R.id.appoint_details_repairNo);
		appoint_details_state = (TextView) findViewById(R.id.appoint_details_state);
		appoint_details_exTypeName = (TextView) findViewById(R.id.appoint_details_exTypeName);
		appoint_details_exLocation = (TextView) findViewById(R.id.appoint_details_exLocation);
		appoint_details_releaseDate = (TextView) findViewById(R.id.appoint_details_releaseDate);
		appoint_details_des = (TextView) findViewById(R.id.appoint_details_des);
		appoint_receive = (Button) findViewById(R.id.appoint_receive);
		appoint_navigation = (Button) findViewById(R.id.appoint_navigation);
		appoint_details_voice = findViewById(R.id.appoint_details_voice);
		appoint_voice_btn = (FrameLayout) findViewById(R.id.appoint_voice_btn);
		appoint_details_pic = (ImageView) findViewById(R.id.appoint_details_pic);

		big_imgv_leftbtn.setOnClickListener(OnClickListener);

		big_imgv_rightbtn.setVisibility(View.INVISIBLE);
		appoint_receive.setVisibility(View.INVISIBLE);
		appoint_navigation.setVisibility(View.INVISIBLE);

		appoint_voice_btn.setOnClickListener(OnClickListener);
		appoint_details_pic.setOnClickListener(OnClickListener);

	}

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = (MBinder) service;

			if (null != checkrepairno) {// 获取后端服务器维修任务详情
				mBinder.setAppointDetailsCallBack(appointDetailsCallBack);
				mBinder.getAppointDetails(checkrepairno);
			}

			mBinder.setLocationCallBack(LocationCallBack);
			mBinder.getLocation();
		}
	};

	private AppointDetailsService.LocationCallBack LocationCallBack = new AppointDetailsService.LocationCallBack() {

		@Override
		public void getLocationFail(String msgEx) {
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putString("msgEx", msgEx);
			msg.setData(data);
			msg.what = 0x05;
			handler.sendMessage(msg);
		}

		@Override
		public void getLocationData(Poi start) {
			startPoi = start;
		}
	};

	private AppointDetailsCallBack appointDetailsCallBack = new AppointDetailsCallBack() {

		@Override
		public void AppointDetailsFail(String msgEx) {
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putString("msgEx", msgEx);
			msg.setData(data);
			msg.what = 0x05;
			handler.sendMessage(msg);
		}

		@Override
		public void AppointDetailsData(AppointDetailsEntity data) {
			appointDetails = data;
			handler.sendEmptyMessage(0x01);
		}
	};

	private Handler handler = new Handler() {
		@Override
		public void dispatchMessage(android.os.Message msg) {
			Bundle data = msg.getData();
			switch (msg.what) {
			case 0x01:// 初始化任务详情
				initViewData();
				break;
			case 0x02:// 接受任务成功
				String appointSuccess = data.getString("appointSuccess");
				Toast.makeText(getApplication(), appointSuccess, Toast.LENGTH_SHORT).show();
				isAppoint_receiveing = false;

				// 向OtherAppointFragment应用推送数据，改变任务状态
				sendBroadcastManager();

				//修改接受状态
				appoint_details_state.setText("已接受");
				// 后隐藏接受按钮、打开上传按钮
				appoint_receive.setVisibility(View.INVISIBLE);
				big_imgv_rightbtn.setVisibility(View.VISIBLE);
				big_imgv_rightbtn.setOnClickListener(OnClickListener);

				break;
			case 0x03:// view加载图片
				appoint_details_pic.setImageURI(Uri.fromFile(new File(picPath)));
				break;
			case 0x04://

				break;
			case 0x05:
				String msgEx = data.getString("msgEx");
				Toast.makeText(getApplication(), msgEx, Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		};
	};

	/** 推送广播 */
	private void sendBroadcastManager() {
		Intent intent = new Intent("com.kmnfsw.work.repair.AppointDetailsActivity.AppointReceive");// 注册动作
		intent.putExtra("checkrepairno", checkrepairno);
		setLocalBroadcastManager.sendBroadcast(intent);
	}

	/** 初始化所有视图数据 */
	private void initViewData() {
		appoint_details_repairNo.setText(appointDetails.checkrepairno);// 任务编号

		if (appointDetails.state < 2) {// 接受状态
			appoint_details_state.setText("未接受");
			appoint_receive.setVisibility(View.VISIBLE);
			appoint_receive.setOnClickListener(OnClickListener);

			big_imgv_rightbtn.setVisibility(View.INVISIBLE);
		} else if (appointDetails.state == 2) {
			appoint_details_state.setText("已接受");
			appoint_receive.setVisibility(View.INVISIBLE);

			big_imgv_rightbtn.setVisibility(View.VISIBLE);
			big_imgv_rightbtn.setOnClickListener(OnClickListener);
		}

		appoint_details_exTypeName.setText(appointDetails.extypename);// 问题类型

		if ("1".equals(appointDetails.distributeType)) {// 他人派发
			big_img_title.setText("他人指派任务详情");
			appoint_details_exLocation.setText(appointDetails.exlocation);
			appoint_navigation.setVisibility(View.VISIBLE);
			appoint_navigation.setOnClickListener(OnClickListener);
		} else if ("2".equals(appointDetails.distributeType)) {// 自行派发
			big_img_title.setText("自行指派任务详情");
			appoint_details_exLocation.setText(appointDetails.exlocation);
			appoint_navigation.setVisibility(View.INVISIBLE);
		}

		appoint_details_releaseDate.setText(appointDetails.releasedate);// 日期

		appoint_details_des.setText(appointDetails.exceptionDescription);// 问题描述

		if (null != appointDetails.voice && !"".equals(appointDetails.voice)) {// 语音
			mBinder.setLoadVoiceCallBack(loadVoiceCallBack);
			mBinder.getVoice(appointDetails.voice);
		}

		if (null != appointDetails.pic && !"".equals(appointDetails.pic)) {// 图片
			mBinder.setLoadPicCallBack(loadPicCallBack);
			mBinder.getPic(appointDetails.pic);
		}

	}

	private OnClickListener OnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.big_imgv_leftbtn:// 退出
				finish();
				break;
			case R.id.big_imgv_rightbtn:// 申请验收
				startApplyActivity();
				finish();
				break;
			case R.id.appoint_receive:// 接受维修任务
				if (isAppoint_receiveing) {
					return;
				}
				isAppoint_receiveing = true;

				if (null == peopleno || "".equals(peopleno)) {
					return;
				}
				if (null == checkrepairno || "".equals(checkrepairno)) {
					return;
				}

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar calendar = Calendar.getInstance();
				Date date = new Date();
				calendar.setTime(date);
				String subscribedate = sdf.format(date);
				mBinder.setAppointReceiveBack(appointReceiveBack);
				mBinder.receiveAppoint(checkrepairno, peopleno, subscribedate);
				break;
			case R.id.appoint_navigation:// 导航
				showNavigationDialog();
				break;
			case R.id.appoint_voice_btn:// 语音
				if (null != voicePath && !"".equals(voicePath)) {// 加载语音成功
					appoint_details_voice.setBackgroundResource(R.drawable.adj);
					appoint_details_voice.setBackgroundResource(R.drawable.play_anim);
					AnimationDrawable animation = (AnimationDrawable) appoint_details_voice.getBackground();
					animation.start();

					MediaManager.playSound(getApplicationContext(), voicePath, new MediaPlayer.OnCompletionListener() {

						public void onCompletion(MediaPlayer mp) {
							appoint_details_voice.setBackgroundResource(R.drawable.adj);
						}
					});
				}
				break;
			case R.id.appoint_details_pic:// 图片
				if (null != picPath && !"".equals(picPath)) {// 加载图片成功
					startBigImgActivity();
				}
				break;

			default:
				break;
			}
		}
	};

	private void startBigImgActivity() {
		Intent intent = new Intent(this, ShowBigImgActivity.class);
		intent.putExtra("picPath", picPath);
		startActivity(intent);
	}

	private void startApplyActivity() {
		if (null == checkrepairno || "".equals(checkrepairno)) {
			return;
		}
		if (null == peopleno || "".equals(peopleno)) {
			return;
		}
		Intent intent = new Intent(this, AppointApplyActivity.class);
		intent.putExtra("checkrepairno", checkrepairno);
		intent.putExtra("peopleno", peopleno);
		startActivity(intent);
	}

	private AppointDetailsService.AppointReceiveBack appointReceiveBack = new AppointDetailsService.AppointReceiveBack() {

		@Override
		public void appointReceiveSuccess(String appointSuccess) {
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putString("appointSuccess", appointSuccess);
			msg.setData(data);
			msg.what = 0x02;
			handler.sendMessage(msg);
		}

		@Override
		public void appointReceiveFail(String msgEx) {
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putString("msgEx", msgEx);
			msg.setData(data);
			msg.what = 0x05;
			handler.sendMessage(msg);
		}
	};

	private AppointDetailsService.LoadPicCallBack loadPicCallBack = new AppointDetailsService.LoadPicCallBack() {

		@Override
		public void getPicData(String path) {
			picPath = path;
			handler.sendEmptyMessage(0x03);
		}

		@Override
		public void getPicFail(String msgEx) {
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putString("msgEx", msgEx);
			msg.setData(data);
			msg.what = 0x05;
			handler.sendMessage(msg);
		}

	};

	private AppointDetailsService.LoadVoiceCallBack loadVoiceCallBack = new AppointDetailsService.LoadVoiceCallBack() {

		@Override
		public void getVoiceFail(String msgEx) {
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putString("msgEx", msgEx);
			msg.setData(data);
			msg.what = 0x05;
			handler.sendMessage(msg);
		}

		@Override
		public void getVoiceData(String path) {
			voicePath = path;
		}
	};

	/** 展现导航类型弹出框 */
	private void showNavigationDialog() {
		final NavigationDialog dialog = new NavigationDialog(this, R.style.CustomDialog);
		dialog.setDriverNavigationListener(new OnClickListener() {// 机动车导航

			@Override
			public void onClick(View v) {
				if (null == appointDetails) {
					return;
				}
				if (null == startPoi) {
					return;
				}

				double endLat = Double.parseDouble(appointDetails.exceptionlat);
				double endLng = Double.parseDouble(appointDetails.exceptionlong);
				Poi endPoi = new Poi(appointDetails.exlocation, new LatLng(endLat, endLng), "");
				AmapNaviParams naviParam = new AmapNaviParams(startPoi, null, endPoi, AmapNaviType.DRIVER);
				naviParam.setUseInnerVoice(true);// 设置是否使用语音播报
				AmapNaviPage.getInstance().showRouteActivity(getApplication(), naviParam, paramINaviInfoCallback);
				dialog.dismiss();

			}
		});
		dialog.setRideNavigationListener(new OnClickListener() {// 电动车导航

			@Override
			public void onClick(View v) {
				if (null == appointDetails) {
					return;
				}
				if (null == startPoi) {
					return;
				}

				double endLat = Double.parseDouble(appointDetails.exceptionlat);
				double endLng = Double.parseDouble(appointDetails.exceptionlong);
				Poi endPoi = new Poi(appointDetails.exlocation, new LatLng(endLat, endLng), "");
				AmapNaviParams naviParam = new AmapNaviParams(startPoi, null, endPoi, AmapNaviType.RIDE);
				naviParam.setUseInnerVoice(true);// 设置是否使用语音播报
				AmapNaviPage.getInstance().showRouteActivity(getApplication(), naviParam, paramINaviInfoCallback);
				dialog.dismiss();

			}
		});
		dialog.setMinimapNavigationListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isAvilible(getApplicationContext(),"com.autonavi.minimap")) {
					Intent intent1 = new Intent();
					intent1.setAction(Intent.ACTION_VIEW);
					intent1.addCategory(Intent.CATEGORY_DEFAULT);
					/**
					 * 将功能Scheme以URI的方式传入data sourceApplication——》第三方调用应用名称，公司名
					 * 必填 poiname——》终点位置的名称 lat——》终点的纬度 必填 lon——》终点的经度 必填
					 * dev——》是否偏移(0:lat 和 lon 是已经加密后的,不需要国测加密; 1:需要国测加密) 必填
					 * style——》导航方式(0 速度快; 1 费用少; 2 路程短; 3 不走高速；4 躲避拥堵；5
					 * 不走高速且避免收费； 6 不走高速且躲避拥堵；7 躲避收费和拥堵；8 不走高速躲避收费和拥堵) 必填
					 */
					Uri uri = Uri.parse("androidamap://navi?sourceApplication=kmnfsw&poiname="
							+ appointDetails.exlocation + "&lat=" + appointDetails.exceptionlat + "&lon="
							+ appointDetails.exceptionlong + "&dev=1&style=2");
					intent1.setData(uri);
					// 启动该页面即可
					startActivity(intent1);
				} else {
					Toast.makeText(getApplicationContext(), "请安装高德地图", Toast.LENGTH_SHORT).show();
				}

			}
		});
		dialog.show();
		dialog.setCanceledOnTouchOutside(true);// 设置点击弹出dialog以外区域进行dialog隐藏
	}

	/**
	 * 判断是否安装目标应用
	 * 
	 * @param packageName
	 * @return
	 */
	private boolean isAvilible(Context context, String packageName) {
		final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
		List<String> pName = new ArrayList<String>();// 用于存储所有已安装程序的包名
		// 从pinfo中将包名字逐一取出，压入pName list中
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				pName.add(pn);
			}
		}
		return pName.contains(packageName);// 判断pName中是否有目标程序的包名，有TRUE，没有FALSE
	}

	private INaviInfoCallback paramINaviInfoCallback = new INaviInfoCallback() {

		@Override
		public void onStopSpeaking() {// 停止语音回调，收到此回调后用户可以停止播放语音
			// TODO Auto-generated method stub

		}

		@Override
		public void onStartNavi(int paramInt) {// 启动导航后的回调函数
			// TODO Auto-generated method stub

		}

		@Override
		public void onReCalculateRoute(int paramInt) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLocationChange(AMapNaviLocation paramAMapNaviLocation) {// 当GPS位置有更新时的回调函数。
			// TODO Auto-generated method stub

		}

		@Override
		public void onInitNaviFailure() {// 导航初始化失败时的回调函数
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetNavigationText(String paramString) {// 导航播报信息回调函数。

		}

		@Override
		public void onExitPage(int paramInt) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCalculateRouteSuccess(int[] paramArrayOfInt) {// 算路成功回调
			// TODO Auto-generated method stub

		}

		@Override
		public void onCalculateRouteFailure(int paramInt) {// 步行或者驾车路径规划失败后的回调函数
			// TODO Auto-generated method stub

		}

		@Override
		public void onArriveDestination(boolean paramBoolean) {// 到达目的地后回调函数。
			// TODO Auto-generated method stub

		}
	};

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// 如果录音播放管理对象存在，就进行关闭
		MediaManager.pause();
		MediaManager.release();

		unbindService(conn);
		Log.i(Tag, "销毁！");
	}
}
