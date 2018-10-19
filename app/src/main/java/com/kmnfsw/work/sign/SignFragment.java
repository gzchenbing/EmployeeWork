package com.kmnfsw.work.sign;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.kmnfsw.work.R;
import com.kmnfsw.work.backstage.LocalhostReportService;
import com.kmnfsw.work.sign.adapter.TaskListAdapter;
import com.kmnfsw.work.sign.db.dao.CheckedPointDao;
import com.kmnfsw.work.sign.db.dao.TaskPointDao;
import com.kmnfsw.work.sign.entity.CheckTaskEntity;
import com.kmnfsw.work.sign.entity.SignLocationEntity ;
import com.kmnfsw.work.sign.entity.TaskPointEntity;
import com.kmnfsw.work.sign.service.SignInitPointService;
import com.kmnfsw.work.sign.service.SignTaskPointService;
import com.kmnfsw.work.sign.service.SignTaskService;
import com.kmnfsw.work.sign.service.SignTaskService.CheckTaskCallback;
import com.kmnfsw.work.sign.service.SignTaskService.LocationNearbyPointCallback;
import com.kmnfsw.work.sign.service.SignTaskService.MBinder;
import com.kmnfsw.work.sign.service.SignTaskService.SignDataCallback;
import com.kmnfsw.work.sign.service.SignTaskService.SignTaskPerCallback;
import com.kmnfsw.work.sign.view.SimpleProgressbar;
import com.kmnfsw.work.util.ServiceState;
import com.kmnfsw.work.welcomeLogin.LoginActivity;

import android.app.Fragment;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SignFragment extends Fragment implements AMap.OnMyLocationChangeListener,AMap.OnMarkerClickListener
	,AMap.OnInfoWindowClickListener{

	private static final String Tag = ".sign.SignFragment";
	
	private View view;
	private MapView mMapView;
	private AMap aMap;
	private MyLocationStyle myLocationStyle;;
	private Circle circle;

	private Context context;

	private static final int FILL_COLOR1 = Color.argb(0, 0, 0, 0);//(254, 246, 246, 246)
	private static final int STROKE_COLOR1 = Color.argb(0, 0, 0, 0);
	private static final int STROKE_COLOR = Color.argb(60, 3, 145, 255);
	private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
	/**精度圆半径*/
	private static final int RADIUS = 20;

	private double whilst_longtitud;
	private double whilst_latitud;
	
	private RelativeLayout sign_task;
	private View mTaskMenuView;
	private RelativeLayout sign_progressbar_Layout;
	
	private TextView sign_progressbar_value;
	private SimpleProgressbar sign_progressbar;
	
	private PopupWindow moreTask;
	private List<CheckTaskEntity> checkTaskList;
	private String peopleno;
	private String checkTaskNo;
	private int peopleType;
	private String oldCheckTaskNo;
	
	private SharedPreferences shares;
	private MBinder mBinder;
	
	private TaskPointReceiver taskPointReceiver;
	private  LocalBroadcastManager getPointLocalBroadcastManager;
	private  LocalBroadcastManager selectTaskLocalBroadcastManager;
	
	/**所有任务点marker*/
	private HashMap<String, Marker> taskPointMarker = new HashMap<>();
	/**签到过点marker*/
	private HashMap<String, Marker> checkedPointMarker = new HashMap<>();
	private List<TaskPointEntity> listTaskPointEntity;
	
	/**任务点是否加载完成*/
	private boolean taskIsLoading = true; 
	/**签到是否完成*/
	private boolean signIsFinnish = true;
	/**获取点时是否异常*/
	private boolean getPointIsEx = false;
	
	private Marker windowMarker;
	
	
	/**定位园精度内,存在的点*/
	private HashMap<String,TaskPointEntity> nearbyPoints;
	
	private TaskPointEntity selectTaskPointMarker;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		context = getActivity();
		view = inflater.inflate(R.layout.sign_fragment, null);
		
		shares = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		
		selectTaskLocalBroadcastManager = LocalBroadcastManager.getInstance( context );
		
		peopleno = shares.getString("peopleno", "");
		if ("".equals(peopleno)) {
			Intent it = new Intent(context,LoginActivity.class);
			startActivity(it);
			getActivity().finish();
		}
		
		//注册获取所有坐标点广播
		getPointLocalBroadcastManager = LocalBroadcastManager.getInstance( context );
		taskPointReceiver = new TaskPointReceiver(); 
		IntentFilter infi = new IntentFilter();
		infi.addAction("com.kmnfsw.work.SignTaskPointService.sendListTaskPoint");
		infi.addAction("com.kmnfsw.work.SignTaskPointService.sendTaskPoint");
		infi.addAction("com.kmnfsw.work.SignTaskPointService.sendException");
		infi.addAction("com.kmnfsw.work.SignTaskPointService.sendCheckedPoint");
		infi.addAction("com.kmnfsw.work.SignTaskPointService.sendListCheckPoint");
		getPointLocalBroadcastManager.registerReceiver(taskPointReceiver,infi);
		
		//绑定service
		Intent intentTask = new Intent(context, SignTaskService.class);
		context.bindService(intentTask, connectionTask, Context.BIND_AUTO_CREATE);

		// 获取地图控件引用
		mMapView = (MapView) view.findViewById(R.id.sign_map);
		// 在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
		mMapView.onCreate(savedInstanceState);
		aMap = mMapView.getMap();// 初始化地图控制器对象
		
		//设置进度条
		sign_progressbar = (SimpleProgressbar)view.findViewById(R.id.sign_progressbar);
		sign_progressbar_value = (TextView)view.findViewById(R.id.sign_progressbar_value);
		sign_progressbar_Layout = (RelativeLayout)view.findViewById(R.id.sign_progressbar_Layout);
		sign_progressbar_Layout.setVisibility(View.INVISIBLE);//先隐藏进度条
		
		sign_task = (RelativeLayout) view.findViewById(R.id.sign_task);
		RelativeLayout sign_task_content = (RelativeLayout) view.findViewById(R.id.sign_task_content);
		
		peopleType = shares.getInt("peopleType", 0);
		checkTaskNo = shares.getString("checkTaskNo", "");
		if (!"".equals(checkTaskNo)) {//已经选择了任务
			Intent intent = new Intent(context,SignInitPointService.class);
			context.startService(intent);
		}
		
		aMap.setOnMapClickListener(mapClickListener);
		aMap.setOnMapLongClickListener(mapLongClickListener);
		
		myLocationStyle = new MyLocationStyle();;
		aMap.setOnMyLocationChangeListener(this);//注册位置改变监听
		
		
		aMap.setOnMarkerClickListener(this);//覆着物点击事件
		aMap.setInfoWindowAdapter(windowAdapter);//覆着物点击弹出框
		aMap.setOnInfoWindowClickListener(this);//覆着物点击弹出框的点击事件
		
		
		sign_task.setOnClickListener(mOnClickListener);
		sign_task_content.setOnClickListener(mOnClickListener);
		
		
		double latdata = 24.9341100000;
		double lngdata = 102.8199100000;
		aMap.moveCamera(CameraUpdateFactory
		 .newLatLngZoom(new LatLng(latdata, lngdata), 19));//进行缩放
		initViewBluePoint();
		
		return view;

	}
	
	
	private ServiceConnection connectionTask = new ServiceConnection() {

		/**服务绑定时调用*/
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = (MBinder) service;
			mBinder.setCheckTaskCallback(checkTaskCallback);
			
			if (!"".equals(peopleno)) {
				mBinder.getCheckTask(peopleno);
			}
			if (!"".equals(checkTaskNo) && !"".equals(peopleno)){
				//获取任务进度
				mBinder.setSignTaskPerCallback(signTaskPerCallback);
				mBinder.getSignTaskPerCallback(checkTaskNo, peopleno);
			}
		}

		/**服务解除时调用*/
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
	};
	
	CheckTaskCallback checkTaskCallback = new CheckTaskCallback() {
		
		@Override
		public void getTaskFailed(String massge) {
			Message msg = new Message();
			msg.what = 0x05;
			msg.obj = massge;
			mHandle.sendMessage(msg);
		}
		
		@Override
		public void getTask(List<CheckTaskEntity> entityList) {
			checkTaskList = entityList;
		}
	};
	
	
	
	Handler mHandle = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			Bundle data = msg.getData();
			switch (msg.what) {
			case 0x01://签到成功
				if (selectTaskPointMarker==null) {
					return;
				}
				threadSafeOperateMarker(null,selectTaskPointMarker);
				String massge = (String)msg.obj;
				Toast.makeText(context, massge, Toast.LENGTH_SHORT).show();
				break;
			case 0x02://显示任务进度
				int totalPointSize = data.getInt("totalPointSize");
				int signSize = data.getInt("signSize");
				String per = data.getString("per");
				showSignTaskPer(totalPointSize, signSize, per);
				break;
			case 0x05://出现异常数据时
				sign_progressbar_Layout.setVisibility(View.INVISIBLE);//隐藏进度条
				String massgeEx = (String)msg.obj;
				Toast.makeText(context, massgeEx, Toast.LENGTH_SHORT).show();
				
				break;
			default:
				break;
			}
		}
	};
	
	private OnClickListener mOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.sign_task://点击任务图标
				if (checkTaskList !=null && checkTaskList.size()>0) {
					showTaskList(sign_task);
				}
				break;

			case R.id.sign_task_content://点击任务详情图标
				String ctn = shares.getString("checkTaskNo", "");
				if (!"".equals(ctn)) {
					Intent it = new Intent(context,SignTaskActivity.class);
					it.putExtra("checkTaskNo", ctn);
					it.putExtra("peopleno", peopleno);
					context.startActivity(it);
				}
				break;
			default:
				break;
			}
			
		}
	};
	
	/**地图点击事件*/
	private AMap.OnMapClickListener mapClickListener = new AMap.OnMapClickListener() {
		
		@Override
		public void onMapClick(LatLng paramLatLng) {
			
		}
	};
	/**地图长按事件*/
	private AMap.OnMapLongClickListener mapLongClickListener = new AMap.OnMapLongClickListener() {
		
		@Override
		public void onMapLongClick(LatLng paramLatLng) {
			if (windowMarker !=null) {
				//Log.i(Tag, "进行隐藏");
				windowMarker.hideInfoWindow();//去除覆着物弹出框
				windowMarker = null;
			}
		}
	};
	
	/**初始化任务list弹出框*/
	private void showTaskList(View v){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mTaskMenuView = inflater.inflate(R.layout.task_list, null,false);
		moreTask =  new PopupWindow(mTaskMenuView, 600, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
		// 设置SelectPicPopupWindow弹出窗体可点击
		moreTask.setFocusable(true);
		// 设置SelectPicPopupWindow弹出窗体动画效果
		moreTask.setAnimationStyle(R.style.morestyle);
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0000000000);
		// 设置SelectPicPopupWindow弹出窗体的背景
		moreTask.setBackgroundDrawable(dw);
		// 设置允许在外点击消失
		moreTask.setOutsideTouchable(true);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		moreTask.setBackgroundDrawable(new BitmapDrawable());
		moreTask.showAsDropDown(v);
		
		taskListAdapter();
	}
	/**弹出框任务集合适配器*/
	private void taskListAdapter(){
		ListView task_list_view = (ListView)mTaskMenuView.findViewById(R.id.task_list);
		TaskListAdapter adapter = new TaskListAdapter(checkTaskList,context);
		task_list_view.setAdapter(adapter);
		
		task_list_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if ("".equals(peopleno)) {
					return;
				}
				if (!taskIsLoading) {//判断点是否加载完成
					return;
				}
				oldCheckTaskNo = shares.getString("oldCheckTaskNo", "");
				
				CheckTaskEntity selectCheckTask = checkTaskList.get(position);
				checkTaskNo = selectCheckTask.checkTaskNo;
				peopleType = selectCheckTask.peopleType;
				shares.edit().putString("checkPlanNo", selectCheckTask.checkPlanNo).commit();
				
				Log.i(Tag, "任务编号："+checkTaskNo);
				
				//获取任务进度
				mBinder.setSignTaskPerCallback(signTaskPerCallback);
				mBinder.getSignTaskPerCallback(checkTaskNo, peopleno);
				
				//启动定位服务
				startLocation();
				
				//调用服务获取巡检任务点和已签到的点
				startSignTaskPoint();
				
				moreTask.dismiss();//关闭弹出框
				
				taskIsLoading = false;
			}
		});
	}
	SignTaskPerCallback signTaskPerCallback = new SignTaskPerCallback() {
		
		@Override
		public void getSignTaskPer(int totalPointSize, int signSize, String per) {
			Message msg = new Message();
			msg.what = 0x02;
			Bundle data = new Bundle();
			data.putInt("totalPointSize", totalPointSize);
			data.putInt("signSize", signSize);
			data.putString("per", per);
			msg.setData(data);
			
			mHandle.sendMessage(msg);
		}
	};
	/**显示任务进度*/
	private void showSignTaskPer(int totalPointSize, int signSize, String per){
		//Log.i(Tag, "任务进度百分数："+per);
		
		sign_progressbar.setMax(totalPointSize);
		sign_progressbar.setProgress(signSize);
		sign_progressbar_value.setText(per);
	}
	
	/**启动SignTaskPointService服务*/
	private void startSignTaskPoint(){
		//进行数据清除操作
		aMap.clear();
		checkedPointMarker.clear();
		taskPointMarker.clear();
		
		//任务点服务是否开启
		boolean isRunningSignTaskPoint = ServiceState.isServiceRunning(context, "com.kmnfsw.work.sign.service.SignTaskPointService");
		//签到过的点服务是否开启
		boolean isRunningSignInitPoint = ServiceState.isServiceRunning(context, "com.kmnfsw.work.sign.service.SignInitPointService");
		
		if (!isRunningSignTaskPoint) {//未启动
			if (isRunningSignInitPoint) {
				context.stopService(new Intent(context, SignInitPointService.class));
			}
			Intent intent = new Intent(context,SignTaskPointService.class);
			intent.putExtra("checktaskno", checkTaskNo);
			intent.putExtra("oldCheckTaskNo", oldCheckTaskNo);
			intent.putExtra("peopleno",peopleno);
			context.startService(intent);
		}else{//已启动
			//进行广播推送
			Intent intent = new Intent("com.kmnfsw.work.sign.SignFragment.selectTask");//这里推到SignTaskPointService
			intent.putExtra("checktaskno", checkTaskNo);
			intent.putExtra("oldCheckTaskNo", oldCheckTaskNo);
			intent.putExtra("peopleno",peopleno);
			if (selectTaskLocalBroadcastManager==null) {
				selectTaskLocalBroadcastManager = LocalBroadcastManager.getInstance( getActivity() );
			}
			selectTaskLocalBroadcastManager.sendBroadcast(intent);
		}
		
	}
	/**关闭SignTaskPointService服务*/
	private void closeSignTaskPoint(){
		//任务点服务是否开启
		boolean isRunningSignTaskPoint = ServiceState.isServiceRunning(context, "com.kmnfsw.work.sign.service.SignTaskPointService");
		if (isRunningSignTaskPoint) {
			context.stopService(new Intent(context, SignTaskPointService.class));//停止SignTaskPointService服务
		}
	}
	/**关闭SignInitPointService服务*/
	private void closeSignInitPoint(){
		//签到过的点服务是否开启
		boolean isRunningSignInitPoint = ServiceState.isServiceRunning(context, "com.kmnfsw.work.sign.service.SignInitPointService");
		if (isRunningSignInitPoint) {
			context.stopService(new Intent(context, SignInitPointService.class));//停止SignInitPointService服务
		}
				
	}
	
	/**启动LocalhostReportService定位即时通信推送*/
	private void startLocation(){
		boolean isRunning = ServiceState.isServiceRunning(context, "com.kmnfsw.work.backstage.LocalhostReportService");
		if (!isRunning) {
			Intent intent = new Intent(context,LocalhostReportService.class);
			context.startService(intent);
		}
		
	}
	

	/** 初始化蓝点定位 */
	private void initViewBluePoint() {
		/*
		 * 初始化定位蓝点样式类 连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
		 */
		myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
		myLocationStyle.interval(10000); // 设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
		
		//去除默认精度园的设置 。
		myLocationStyle.radiusFillColor(FILL_COLOR1);
		myLocationStyle.strokeColor(STROKE_COLOR1);
		myLocationStyle.strokeWidth(0);
		
		aMap.setMyLocationStyle(myLocationStyle);// 设置定位蓝点的Style
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示，非必需设置。
		aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

		
		
	}

	/**位置改变回调*/
	@Override
	public void onMyLocationChange(Location paramLocation) {
		whilst_longtitud = paramLocation.getLongitude();
		whilst_latitud = paramLocation.getLatitude();
		LatLng Localhost = new LatLng(whilst_latitud, whilst_longtitud);
		//Log.i(Tag, "经度："+whilst_longtitud+" 纬度："+whilst_latitud);
		
		/*****设置自定义精度园******/
		if(circle != null){
			circle.setVisible(false);
			circle = null;
		}
		CircleOptions coption = new CircleOptions();
		coption.fillColor(FILL_COLOR).center(new LatLng(whilst_latitud, whilst_longtitud)).radius(RADIUS)//将自我定位的图标加到RADIUS范围内
		.strokeWidth(20).strokeColor(STROKE_COLOR);
		circle = aMap.addCircle(coption);
		
		Double spf_whilst_latitud = (double) shares.getFloat("whilst_latitud", 0);
		Double spf_whilst_longtitud = (double) shares.getFloat("whilst_longtitud", 0);
		LatLng old_point = new LatLng(spf_whilst_latitud, spf_whilst_longtitud);
		LatLng now_point = new LatLng(whilst_latitud, whilst_longtitud);
		float distance = AMapUtils.calculateLineDistance(old_point, now_point);// 单位米
		
		shares.edit().putFloat("whilst_longtitud", (float) whilst_longtitud).commit();// 将经纬度加入历史shares
		shares.edit().putFloat("whilst_latitud", (float) whilst_latitud).commit();
		//Log.i(Tag, "distance 移动的距离:	" + distance);
//		if (distance < 10) {
//			return;
//		}
		
		/*****根据定位的经纬度查询定位点半径内周边的点*****/
		
		if (listTaskPointEntity !=null && listTaskPointEntity.size()>0) {
			//Log.i(Tag, "任务点数"+listTaskPointEntity.size());
			mBinder.setLocationNearbyPointCallback(locationNearbyPointCallback);
			mBinder.getLocationNearbyPoint(listTaskPointEntity, Localhost, RADIUS);
		}

	}
	
	/**获取定位点半径内的数据*/
	LocationNearbyPointCallback locationNearbyPointCallback = new LocationNearbyPointCallback() {
		
		@Override
		public void getLocationNearbyPoint(HashMap<String,TaskPointEntity> nearbyPoint) {
			nearbyPoints = nearbyPoint;
			//Log.i(Tag, "定位地位点周边的数目"+nearbyPoints.size());
		}
	};
	
	
	/**覆着物点击事件
	 * 返回true表示不可点击(无弹出框）
	 * 返回false表示可点击（有弹出框）
	 * */
	@Override
	public boolean onMarkerClick(Marker paramMarker) {
		if (nearbyPoints !=null && nearbyPoints.size() >0) {
			TaskPointEntity taskPointEntity = (TaskPointEntity)paramMarker.getObject();
			if (taskPointEntity == null) {
				return true;
			}
			if (nearbyPoints.containsKey(taskPointEntity.id)) {
				windowMarker = paramMarker;
				return false;
			}else{
				return true;
			}
		}else{
			return true;
		}
		
	}
	/**弹出框适配器*/
	InfoWindowAdapter windowAdapter = new InfoWindowAdapter() {
		
		@Override
		public View getInfoWindow(Marker arg0) {
			
			View infowindow = getActivity().getLayoutInflater().inflate(R.layout.infowindow, null);
			TextView content = (TextView) infowindow.findViewById(R.id.content_text);
			content.setText(arg0.getSnippet());
			
			return infowindow;
		}
		
		@Override
		public View getInfoContents(Marker arg0) {
			return null;
		}
	};
	/**覆着物弹出框点击事件*/
	@Override
	public void onInfoWindowClick(Marker paramMarker) {
		if ("".equals(checkTaskNo)) {
			return;
		}
		if ("".equals(peopleno)) {
			return;
		}
		if (!signIsFinnish) {//签到未完成
			return;
		}
		
		String snippet = paramMarker.getSnippet();
		if (getString(R.string.sure_sign).equals(snippet)) {//确定签到
			signIsFinnish = false;
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			Date date = new Date();
			calendar.setTime(date);
			String strDeviceSignDatetime = sdf.format(date);
			Log.i(Tag,"strDeviceSignDatetime："+ strDeviceSignDatetime);
			selectTaskPointMarker = (TaskPointEntity)paramMarker.getObject();
			
			SignLocationEntity  checkPoint = new SignLocationEntity ();
			checkPoint.peopleno = peopleno;
			checkPoint.pointno = selectTaskPointMarker.id;
			checkPoint.pointlnggd = selectTaskPointMarker.localhostLong;
			checkPoint.pointlatdg = selectTaskPointMarker.localhostLat;
			checkPoint.checktaskno = checkTaskNo;
			checkPoint.signdate = strDeviceSignDatetime;
			checkPoint.lnorganizeid = selectTaskPointMarker.lineId;
			
			//将签到数据推送到后台
			mBinder.setSignDataCallback(setSignDataback);
			mBinder.getSignCallback(checkPoint,selectTaskPointMarker);

		}
		paramMarker.hideInfoWindow();
	}
	
	SignDataCallback setSignDataback = new SignDataCallback() {
		@Override
		public void getSignDataCallbackFailed(String massageEx) {
			signIsFinnish = true;
			Message msg = new Message();
			msg.what = 0x05;
			msg.obj = massageEx;
			mHandle.sendMessage(msg);
			
		}
		
		@Override
		public void getSignDataCallback(String massage) {
			signIsFinnish = true;
			Message msg = new Message();
			msg.what = 0x01;
			msg.obj = massage;
			mHandle.sendMessage(msg);
		}

		@Override
		public void getSignTaskPer(int totalPointSize, int signSize, String per) {
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putInt("totalPointSize", totalPointSize);
			data.putInt("signSize", signSize);
			data.putString("per", per);
			msg.setData(data);
			msg.what = 0x02;
			mHandle.sendMessage(msg);
		}
	};
	
	
	
	/**设置TaskPoint marker覆着物*/
	public void setTaskPointMarker(TaskPointEntity taskPointEntity){
		MarkerOptions markerOption = new MarkerOptions();
		
		markerOption.icon(new BitmapDescriptorFactory().fromResource(R.drawable.task_point));
		markerOption.snippet(getResources().getString(R.string.sure_sign));
		markerOption.position(new LatLng(taskPointEntity.localhostLat,taskPointEntity.localhostLong));
		if (aMap != null) {
			Marker mark = aMap.addMarker(markerOption);
			mark.setObject(taskPointEntity);
			//Log.i(Tag, "画任务点覆着物");
			taskPointMarker.put(taskPointEntity.id, mark);
		}
		
	}
	/**设置CheckedPoint marker覆着物*/
	public void setCheckedPointMarker(TaskPointEntity checkedPointEntity){
		MarkerOptions markerOption = new MarkerOptions();
		
		markerOption.icon(new BitmapDescriptorFactory().fromResource(R.drawable.checked_point));
		markerOption.snippet(getResources().getString(R.string.already_sign));
		markerOption.position(new LatLng(checkedPointEntity.localhostLat,checkedPointEntity.localhostLong));
		
		if (aMap != null) {
			Marker mark = aMap.addMarker(markerOption);
			mark.setObject(checkedPointEntity);
			//Log.i(Tag, "画签到过的点覆着物");
			
			checkedPointMarker.put(checkedPointEntity.id, mark);
		}
		
	}
	/**根据异常删除覆着物点*/
	public void clearMarkerByEx(){
		if (getPointIsEx) {
			checkedPointMarker.clear();
			taskPointMarker.clear();
			aMap.clear();
			//Log.i(Tag, "清除marker点11111111");
			new Thread(){
				@Override
				public void run() {
					TaskPointDao taskPointDao = new TaskPointDao(context);
					taskPointDao.deleteTaskPoint();
					CheckedPointDao checkedPointDao = new CheckedPointDao(context);
					checkedPointDao.deleteCheckedPoint();
				}}.start();
			getPointIsEx = false;
		}else{
			if (listTaskPointEntity==null || listTaskPointEntity.size()==0) {
				return;
			}
			//Log.i(Tag, "11111111111111111111111");
			sign_progressbar_Layout.setVisibility(View.VISIBLE);//显示进度条
		}
	}
	
	/**添加覆着点标记marker的统一入口*/
	public synchronized void threadSafeOperateMarker(TaskPointEntity taskPointEntity,TaskPointEntity checkedPointEntity){
		
		
		//所有任务点
		if (taskPointEntity != null) {
			if (!checkedPointMarker.containsKey(taskPointEntity.id)) {//所有任务点不在签到过的点里时
				setTaskPointMarker(taskPointEntity);
				//Log.i(Tag, "画任务点");
			}
		}
		//签到过的点
		if (checkedPointEntity !=null) {
			if (taskPointMarker.containsKey(checkedPointEntity.id)) {//在任务集合中时
				Marker mark = taskPointMarker.get(checkedPointEntity.id);
				mark.destroy();//销毁已覆着的未签到点
				taskPointMarker.remove(checkedPointEntity.id);
				//Log.i(Tag, "去除任务点再画签到过的点");
				setCheckedPointMarker(checkedPointEntity);
			}else{
				//Log.i(Tag, "画签到过的点");
				setCheckedPointMarker(checkedPointEntity);
			}
		}
	}

	/**注册广播接受者*/
	class TaskPointReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
			case "com.kmnfsw.work.SignTaskPointService.sendTaskPoint"://所有任务点
				TaskPointEntity taskPointEntity = intent.getParcelableExtra("taskPointEntity");
				//Log.i(Tag, "任务点:"+taskPointEntity);
				threadSafeOperateMarker(taskPointEntity,null);
				break;
			case "com.kmnfsw.work.SignTaskPointService.sendListTaskPoint"://所有任务点集合
				listTaskPointEntity =  intent.getParcelableArrayListExtra("listTaskPointEntity");
				if (listTaskPointEntity !=null && listTaskPointEntity.size()>0) {
					TaskPointEntity point = listTaskPointEntity.get(0);
					aMap.moveCamera(CameraUpdateFactory
							 .newLatLngZoom(new LatLng(point.localhostLat, point.localhostLong), 19));//进行缩放
					initViewBluePoint();
				}
				//获取任务点后进行任务存储
				shares.edit().putString("checkTaskNo", checkTaskNo).commit();
				shares.edit().putInt("peopleType", peopleType).commit();
				shares.edit().putString("oldCheckTaskNo", checkTaskNo).commit();
				
				taskIsLoading = true;
				clearMarkerByEx();
				break;
			case "com.kmnfsw.work.SignTaskPointService.sendCheckedPoint"://签到过的任务点
				TaskPointEntity checkedPointEntity = intent.getParcelableExtra("checkedPointEntity");
				//Log.i(Tag, "签到过的点:"+checkedPointEntity);
				threadSafeOperateMarker(null,checkedPointEntity);
				break;
			case "com.kmnfsw.work.SignTaskPointService.sendListCheckPoint"://签到过的所有点
				List<TaskPointEntity> listcheckedPointEntity =  intent.getParcelableArrayListExtra("listcheckedPointEntity");
				clearMarkerByEx();
				break;
			case "com.kmnfsw.work.SignTaskPointService.sendException"://异常反馈
				String massge = intent.getStringExtra("massgeEx");
				Message msg = new Message();
				msg.what = 0x05;
				msg.obj = massge;
				mHandle.sendMessage(msg);
				
				//根据任务获取点数据失败，执行删除checkTaskNo
				shares.edit().remove("oldCheckTaskNo").commit();
				
				taskIsLoading = true;
				getPointIsEx = true;
				break;

			default:
				break;
			}
			
		}
		
	}
	
	
	
	
	
	
	

	@Override
	public void onStart(){
		super.onStart();
		
		if (!"".equals(shares.getString("checkTaskNo", "")) && shares.getInt("peopleType", 0) !=0) {
			startLocation();
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
		mMapView.onDestroy();
		//解除service绑定
		context.unbindService(connectionTask);
		//销毁任务点广播注册
		getPointLocalBroadcastManager.unregisterReceiver(taskPointReceiver);
		
		closeSignInitPoint();
		closeSignTaskPoint();
		

	}
	@Override
	public void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
		mMapView.onResume();
		initViewBluePoint();
	}
	@Override
	public void onStop(){
		super.onStop();
		closeSignInitPoint();
	}
	@Override
	public void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
		mMapView.onPause();
		aMap.setMyLocationEnabled(false);//停止定位
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// 在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState
		// (outState)，保存地图当前的状态
		mMapView.onSaveInstanceState(outState);

	}
	
	

}
