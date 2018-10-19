package com.kmnfsw.work.welcomeLogin;

import com.kmnfsw.work.MainActivity;
import com.kmnfsw.work.backstage.LocalhostReportService;
import com.kmnfsw.work.backstage.RabbitmqListenService;
import com.kmnfsw.work.sign.db.dao.CheckedPointDao;
import com.kmnfsw.work.sign.db.dao.TaskPointDao;
import com.kmnfsw.work.version.SplashActivity;
import com.kmnfsw.work.welcomeLogin.service.LoginService;
import com.kmnfsw.work.welcomeLogin.service.LoginService.BackstageIsLoginCallBack;
import com.kmnfsw.work.welcomeLogin.service.LoginService.MBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * 欢迎程序，进行选择跳转的页面
 * @author YanFaBu
 *
 */
public class WelcomeActivity extends Activity {
	private final static String Tag = ".welcomeLogin.WelcomeActivity";
	boolean islogin;
	Handler handler;
	private static final String TABLE = "config";
	private static final String ISLOGIN = "islogin";
	
	private SharedPreferences shares;
	private String peopleno;
	private LoginService.MBinder mBinder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		shares = getSharedPreferences(TABLE, Context.MODE_PRIVATE);
		islogin =  shares.getBoolean(ISLOGIN, false);
		peopleno = shares.getString("peopleno", "");
		
		//绑定service
		Intent intent = new Intent(this,LoginService.class);
		bindService(intent,connection,Context.BIND_AUTO_CREATE);
		
		
	}
	
	
	
	private ServiceConnection connection = new ServiceConnection() {
		
		/**解除绑定时调用*/
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		/**服务绑定时调用*/
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = (MBinder)service;//绑定service并拿到它
			
			if ("".equals(peopleno)) {
				selectActivityHandler.sendEmptyMessage(0x02);
				return;
			}
			////效验后台是否登录
			mBinder.setBackstageIsLoginCallBack(backstageIsLoginCallBack);
			mBinder.isBackstageLoginCallBack(peopleno);
		}
	};
	
	BackstageIsLoginCallBack backstageIsLoginCallBack = new BackstageIsLoginCallBack() {
		
		@Override
		public void isBackstageLogin(boolean isBackstageLogin) {
			Message msg = new Message();
			msg.what = 0x03;
			Bundle data = new Bundle();
			data.putBoolean("isBackstageLogin", isBackstageLogin);
			msg.setData(data);
			selectActivityHandler.sendMessage(msg);
			
		}
		
		@Override
		public void getIsBackstageLoginFail(String msgEx) {
			Message msg = new Message();
			msg.what = 0x05;
			Bundle data = new Bundle();
			data.putString("msgEx", msgEx);
			msg.setData(data);
			selectActivityHandler.sendMessage(msg);
		}
	};
	
	private Handler selectActivityHandler = new Handler(){

		@Override
		public void dispatchMessage(Message msg) {
			Bundle data = msg.getData();
			switch(msg.what){
			case 0x01://已经登录
				Intent intent1 = new Intent();
				intent1.setClass(WelcomeActivity.this, MainActivity.class);
				startActivity(intent1);
				
				//启动rabbitmq监听服务
				startService(new Intent(WelcomeActivity.this, RabbitmqListenService.class));//启动rabbitmq监听服务
				WelcomeActivity.this.finish();
				break;
			case 0x02://未登录
				Intent intent2 = new Intent();
				intent2.setClass(WelcomeActivity.this, SplashActivity.class);
				startActivity(intent2);
				WelcomeActivity.this.finish();
				break;
			case 0x03://后端获取是否已登录数据成功
				boolean isBackstageLogin = data.getBoolean("isBackstageLogin");
				//做页面跳转判断
				pageSkip(isBackstageLogin);
				break;
			case 0x05://异常处理
				String msgEx = data.getString("msgEx");
				Toast.makeText(getApplication(), msgEx, Toast.LENGTH_SHORT).show();
				finish();
				break;
			}
		}
	};
	
	
	
	/**页面跳转*/
	private void pageSkip(boolean isBackstageLogin){
		if(islogin){//本地是否有登录过的记录
			if (isBackstageLogin) {//后台是否登陆过
				selectActivityHandler.sendEmptyMessage(0x01);
			}else{
				Log.i(Tag, "进入清除记录操作");
				//去除任务
				shares.edit().remove("checkTaskNo").commit();
				shares.edit().remove("oldCheckTaskNo").commit();
				shares.edit().remove("checkPlanNo").commit();
				shares.edit().remove("peopleType").commit();
				
				//去除定位经纬度
				shares.edit().remove("whilst_longtitud").commit();
				shares.edit().remove("whilst_latitud").commit();
				
				shares.edit().remove("Long").commit();
				shares.edit().remove("Lat").commit();
				
				shares.edit().remove("islogin").commit();//去除登录标记
				
				//删除数据库SQLite中任务点和签到点的数据
				TaskPointDao taskPointDao = new TaskPointDao(this);
				CheckedPointDao checkedPointDao = new CheckedPointDao(this);
				boolean a = taskPointDao.deleteTaskPoint();
				boolean b = checkedPointDao.deleteCheckedPoint();
				selectActivityHandler.sendEmptyMessage(0x02);
			}
		}else{
			selectActivityHandler.sendEmptyMessage(0x02);
		}
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e(Tag, "进行销毁");
		unbindService(connection);//解除service绑定
	}

}
