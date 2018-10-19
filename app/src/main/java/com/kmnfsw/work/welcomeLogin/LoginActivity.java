package com.kmnfsw.work.welcomeLogin;

import com.kmnfsw.work.MainActivity;
import com.kmnfsw.work.R;
import com.kmnfsw.work.backstage.RabbitmqListenService;
import com.kmnfsw.work.sign.service.SignTaskService;
import com.kmnfsw.work.welcomeLogin.entity.PeopleEntity;
import com.kmnfsw.work.welcomeLogin.service.LoginService;
import com.kmnfsw.work.welcomeLogin.service.LoginService.LoginCallBack;
import com.kmnfsw.work.welcomeLogin.service.LoginService.MBinder;
import com.kmnfsw.work.welcomeLogin.utils.MacUtill;
import com.kmnfsw.work.welcomeLogin.view.MonIndicator;
import com.kmnfsw.work.welcomeLogin.view.SmoothCheckBox;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private final static String Tag = ".welcomeLogin.LoginActivity";
	
	/**定义共享参数选择*/
	private SharedPreferences shares;
	/**定义全局上下文*/
	private Context context;
	
	private LoginService.MBinder mBinder;
	
	/**附加窗体*/
	private WindowManager windowManager;
	/**输入框账号*/
	private EditText editName;
	/**输入框密码*/
	private EditText editPWD;
	/**单选框*/
	private SmoothCheckBox checkbox;
	private Button sure;
	private Button cancel;
	/**动画登录布局*/
	private View loadingLayout;
	private MonIndicator monIndicator;
	
	
	/**员工工号、登录账号*/
	private String peopleno;
	/**员工登录密码*/
	private String password;
	/**员工姓名*/
	private String name;
	/**mac地址*/
	private String Mac;
	/**是否是记住了密码和账号*/
	private boolean isRemenber;
	/**定义是否登陆过*/
	private boolean isLoading = false;
	
	/**记录点击反回键间隔的时间*/
	private long mExitTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login_activity);
		
		context = getApplicationContext();
		shares = this.getSharedPreferences("config", Context.MODE_PRIVATE);
		isRemenber = shares.getBoolean("isremenber", false);
		
		//初始化视图view
		initView();
		
		//绑定service
		Intent intent = new Intent(this,LoginService.class);
		bindService(intent,connection,Context.BIND_AUTO_CREATE);


		
	}
	
	private void initView(){
		windowManager = (WindowManager) getApplication()
				.getSystemService(Context.WINDOW_SERVICE);
		
		editName = (EditText)findViewById(R.id.name_edit);
		editPWD = (EditText)findViewById(R.id.pwd_edit);
		checkbox = (SmoothCheckBox)findViewById(R.id.remenber_pwd);
		checkbox.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
				isRemenber = isChecked;
				//Log.i(Tag, "isremenber:"+isChecked);
				shares.edit().putBoolean("isremenber", isChecked).commit();
			}
		});
		if(isRemenber){//如果勾选了单选框，进行账号密码保存
			//Log.i(Tag, "isremenber_:"+isRemenber);
			checkbox.setChecked(isRemenber);
			String peopleno = shares.getString("peopleno", "");
			String password = shares.getString("password", "");
			editName.setText(peopleno);
			editPWD.setText(password);
		}
		sure = (Button)findViewById(R.id.login_sure);
		cancel = (Button)findViewById(R.id.login_cancel);
		sure.setOnClickListener(new myOnClickListener());
		cancel.setOnClickListener(new myOnClickListener());
		
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
			mBinder.setLoginCallBack(loginCall);
		}
	};
	
	/**
	 * 与后台通信的登录回回调
	 */
	 private LoginCallBack loginCall = new LoginCallBack() {
		
		@Override
		public void loginSuccess(PeopleEntity peopleEntity) {
			
			Message meg = new Message();
			meg.what=0x01;
			Bundle data = new Bundle();
			data.putString("peopleno", peopleEntity.getPeopleno());
			data.putString("password", peopleEntity.getPassword());
			data.putString("name", peopleEntity.getName());
			meg.setData(data);
			loginHandler.sendMessage(meg);
		}
		
		@Override
		public void loginFailed(String massge) {
			Message meg = new Message();
			meg.what = 0x02;
			Bundle data = new Bundle();
			data.putString("massge", massge);
			meg.setData(data);
			loginHandler.sendMessage(meg);
			
		}
	};
	
	/**
	 * 点击事件注册
	 * @author YanFaBu
	 *
	 */
	class myOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			switch (v.getId()){
			case R.id.login_sure:
				Mac = MacUtill.getMac(context);
				shares.edit().putString("Mac", Mac).commit();
				String userid = editName.getText().toString().trim();
				String pwdstr = editPWD.getText().toString().trim();
				Log.i(Tag, "userid:"+userid+"; pwdstr:"+pwdstr);
				if(userid==null||pwdstr==null){
					Toast.makeText(context, "用户名或密码不能为空！", Toast.LENGTH_SHORT).show();
					return;
				}
				if(userid.length()<=0||pwdstr.length()<=0){
					Toast.makeText(context, "用户名或密码不能为空！", Toast.LENGTH_SHORT).show();
					return;
				}
				
				startLoading();
				
				//如果是记住密码
				if(isRemenber){
					shares.edit().putString("peopleno", userid).commit();
					shares.edit().putString("password", pwdstr).commit();
				}
				//进行后台登录验证
				mBinder.loginCheck(userid, pwdstr, Mac);
				
				break;
			case R.id.login_cancel:
				finish();
				break;
			}
		}
	}
	private Handler loginHandler = new Handler(){

		@Override
		public void dispatchMessage(Message msg) {
			Bundle data = msg.getData();
			switch(msg.what){
			case 0x01:
				//成功
				//Log.i(Tag, "login success!");
				loginHandler.removeMessages(0x3);
				if(isLoading){
					windowManager.removeView(loadingLayout);
					isLoading = false;
				}
				peopleno = data.getString("peopleno");
				password = data.getString("password");
				name = data.getString("name");
				
				shares.edit().putBoolean("islogin", true).commit();
				shares.edit().putString("peopleno", peopleno).commit();
				shares.edit().putString("password", password).commit();
				shares.edit().putString("name", name).commit();
				
				Intent intent = new Intent(LoginActivity.this,MainActivity.class);
				startActivity(intent);
				
				//启动rabbitmq监听服务
				startService(new Intent(getApplication(), RabbitmqListenService.class));
				LoginActivity.this.finish();//使用方法finish进行activity销毁
				break;
			case 0x02:
				//失败
				loginHandler.removeMessages(0x3);
				if(isLoading){
					windowManager.removeView(loadingLayout);
					isLoading = false;
				}
				String massge = data.getString("massge");
				
				shares.edit().putBoolean("islogin", false).commit();
				
				Toast.makeText(LoginActivity.this, massge, Toast.LENGTH_SHORT).show();
				break;
			case 0x3:
				if(isLoading){
					windowManager.removeView(loadingLayout);
					isLoading = false;
				}
				Toast.makeText(LoginActivity.this, "登录超时!", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	
	
	
	
	
	
	/**
	 * 开始gif动画
	 */
	private void startLoading(){
		if(loadingLayout==null){
			LayoutInflater  inflayter = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			loadingLayout =  inflayter.inflate(R.layout.loading_view, null);
			monIndicator =(MonIndicator) loadingLayout.findViewById(R.id.monIndicator);
			monIndicator.setColors(new int[]{0xFFff1493, 0xFFff1493, 0xFFff1493, 0xFFff1493, 0xFFff1493});
		}
		if(!isLoading){
			windowManager.addView(loadingLayout, getDialogParmas());
			isLoading = true;
		}
		Message msg = new Message();
		msg.what = 0x3;
		loginHandler.sendMessageDelayed(msg,1000*10);
		//Log.i(Tag, "go run set!");
	}
	private WindowManager.LayoutParams getDialogParmas() {
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.format = PixelFormat.TRANSLUCENT;
		params.x = 0;
		params.y = 0;
		params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
				| WindowManager.LayoutParams.FLAG_FULLSCREEN
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		params.width = WindowManager.LayoutParams.MATCH_PARENT;
		params.height = WindowManager.LayoutParams.MATCH_PARENT;
		return params;
	}
	

	/** 与用户开始可交互的时候调用 */
	@Override
	protected void onResume() {
		super.onResume();
//		Mac = MacUtill.getMac(context);
//		//Log.i(Tag, "Mac："+Mac);
//		shares.edit().putString("Mac", Mac).commit();
	}

	/** 当活动将被销毁时调用 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(Tag, "进行销毁");
		mBinder=null;
		unbindService(connection);//解除service绑定
	}
	/** 当活动不再可见时调用 */
   @Override
   public void onStop() {
      super.onStop();
      //Log.d(Tag, "The onStop() event");
      
   }

	/***
	 * 按两次返回键退出程序
	 */
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			if ((System.currentTimeMillis() - mExitTime) > 2000) {
//				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
//				mExitTime = System.currentTimeMillis();
//			} else {
//				System.exit(0);
//			}
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}

}
