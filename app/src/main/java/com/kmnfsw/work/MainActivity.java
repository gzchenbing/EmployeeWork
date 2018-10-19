package com.kmnfsw.work;

import java.util.ArrayList;

import com.kmnfsw.work.R;
import com.kmnfsw.work.help.HelpFragment;
import com.kmnfsw.work.monitor.MonitorFragment;
import com.kmnfsw.work.question.QuestionFragment;
import com.kmnfsw.work.repair.RepairFragment;
import com.kmnfsw.work.sign.SignFragment;
import com.kmnfsw.work.util.notify.MediaPlayerUtil;
import com.kmnfsw.work.util.notify.VibratorUtil;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

	private static final String Tag = ".MainActivity";

	private RelativeLayout main_sign;
	private RelativeLayout main_exception;
	private RelativeLayout main_repair;
	private RelativeLayout main_monitor;
	private RelativeLayout main_help;
	private LinearLayout tabs;

	private ImageView sign_img;
	private ImageView exception_img;
	private ImageView repair_img;
	private ImageView monitor_img;
	private ImageView help_img;

	private TextView sign_text;
	private TextView exception_text;
	private TextView repair_text;
	private TextView monitor_text;
	private TextView help_text;

	private static final int SIGN = 0;
	private static final int EXCEPTION = 1;
	private static final int REPAIR = 2;
	private static final int MONITOR = 3;
	private static final int HELP = 4;

	/**接受到维修任务的标记*/
	private RelativeLayout repair_receive_tag;
	///广播
	private LocalBroadcastManager getLocalBroadcastManager;
	private MainActivityReceiver mainActivityReceiver;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 去除标题头部
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 解决软盘弹出影响布局
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.activity_main);

		initView();
		//注册广播进行接收消息
		getLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
		mainActivityReceiver = new MainActivityReceiver();
		IntentFilter infi = new IntentFilter();
		infi.addAction("com.kmnfsw.work.backstage.RabbitmqListenService.sendRepairTask");
		infi.addAction("com.kmnfsw.work.repair.OtherAppointFragment.RefurbishAction");
		getLocalBroadcastManager.registerReceiver(mainActivityReceiver, infi);

	}

	/** 初始化视图 */
	private void initView() {
		tabs = (LinearLayout) findViewById(R.id.tabs);

		main_sign = (RelativeLayout) findViewById(R.id.main_sign);
		main_exception = (RelativeLayout) findViewById(R.id.main_exception);
		main_repair = (RelativeLayout) findViewById(R.id.main_repair);
		main_monitor = (RelativeLayout) findViewById(R.id.main_monitor);
		main_help = (RelativeLayout) findViewById(R.id.main_help);

		sign_img = (ImageView) findViewById(R.id.sign_img);
		exception_img = (ImageView) findViewById(R.id.exception_img);
		repair_img = (ImageView) findViewById(R.id.repair_img);
		monitor_img = (ImageView) findViewById(R.id.monitor_img);
		help_img = (ImageView) findViewById(R.id.help_img);

		sign_text = (TextView) findViewById(R.id.sign_text);
		exception_text = (TextView) findViewById(R.id.exception_text);
		repair_text = (TextView) findViewById(R.id.repair_text);
		monitor_text = (TextView) findViewById(R.id.monitor_text);
		help_text = (TextView) findViewById(R.id.help_text);
		
		repair_receive_tag = (RelativeLayout)findViewById(R.id.repair_receive_tag);
		repair_receive_tag.setVisibility(View.INVISIBLE);

		/******** 对屏幕最下方LinearLayout（选择按钮）做手机自适应操作 *********/
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int onetabw = metric.widthPixels / 5;

		android.widget.LinearLayout.LayoutParams lay_param1 = (android.widget.LinearLayout.LayoutParams) main_sign
				.getLayoutParams();
		android.widget.LinearLayout.LayoutParams lay_param2 = (android.widget.LinearLayout.LayoutParams) main_exception
				.getLayoutParams();
		android.widget.LinearLayout.LayoutParams lay_param3 = (android.widget.LinearLayout.LayoutParams) main_repair
				.getLayoutParams();
		android.widget.LinearLayout.LayoutParams lay_param4 = (android.widget.LinearLayout.LayoutParams) main_monitor
				.getLayoutParams();
		android.widget.LinearLayout.LayoutParams lay_param5 = (android.widget.LinearLayout.LayoutParams) main_help
				.getLayoutParams();

		lay_param1.width = onetabw;
		lay_param2.width = onetabw;
		lay_param3.width = onetabw;
		lay_param4.width = onetabw;
		lay_param5.width = onetabw;

		main_sign.setLayoutParams(lay_param1);
		main_exception.setLayoutParams(lay_param2);
		main_repair.setLayoutParams(lay_param3);
		main_monitor.setLayoutParams(lay_param4);
		main_help.setLayoutParams(lay_param5);

		tabs.invalidate();// 将子标签设置到父标签tabs
		selectTab(SIGN);

		// 注册点击监听器
		main_sign.setOnClickListener(this);
		main_exception.setOnClickListener(this);
		main_repair.setOnClickListener(this);
		main_monitor.setOnClickListener(this);
		main_help.setOnClickListener(this);
	}

	/** 点击按钮事件 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.main_sign:
			selectTab(SIGN);
			break;
		case R.id.main_exception:
			selectTab(EXCEPTION);
			break;
		case R.id.main_repair:
			selectTab(REPAIR);
			break;
		case R.id.main_monitor:
			selectTab(MONITOR);
			break;
		case R.id.main_help:
			selectTab(HELP);
			break;

		default:
			break;
		}

	}

	/** 点击下方的标签按钮进行fragment切换 */
	private void selectTab(int index) {
		FragmentManager fm = getFragmentManager();
		// 开启Fragment事务
		FragmentTransaction transaction = fm.beginTransaction();

		switch (index) {
		case SIGN://巡检
			defaultTabColor();
			defeultTabimage();
			sign_text.setTextColor(getResources().getColor(R.color.bottom_tabs_press));
			sign_img.setImageResource(R.drawable.sign_in_select);
			SignFragment frag_sign = new SignFragment();
			transaction.replace(R.id.content_fragment, frag_sign);
			transaction.commit();
			break;
		case EXCEPTION://问题
			defaultTabColor();
			defeultTabimage();
			exception_text.setTextColor(getResources().getColor(R.color.bottom_tabs_press));
			exception_img.setImageResource(R.drawable.question_in_select);
			QuestionFragment frag_question = new QuestionFragment();
			transaction.replace(R.id.content_fragment, frag_question);
			transaction.commit();
			break;
		case REPAIR://维修
			defaultTabColor();
			defeultTabimage();
			repair_text.setTextColor(getResources().getColor(R.color.bottom_tabs_press));
			repair_img.setImageResource(R.drawable.repair_in_select);
			repair_receive_tag.setVisibility(View.INVISIBLE);//隐藏标记
			closeNotification();
			handler.removeMessages(0x01);//移除定时播播报通知
			RepairFragment frag_repair = new RepairFragment();
			transaction.replace(R.id.content_fragment, frag_repair);
			transaction.commit();
			break;
		case MONITOR://监测
			defaultTabColor();
			defeultTabimage();
			monitor_text.setTextColor(getResources().getColor(R.color.bottom_tabs_press));
			monitor_img.setImageResource(R.drawable.monitor_in_select);
			MonitorFragment frag_monitor = new MonitorFragment();
			transaction.replace(R.id.content_fragment, frag_monitor);
			transaction.commit();
			break;
		case HELP://帮助
			defaultTabColor();
			defeultTabimage();
			help_text.setTextColor(getResources().getColor(R.color.bottom_tabs_press));
			help_img.setImageResource(R.drawable.help_in_select);
			HelpFragment frag_help = new HelpFragment(getParent());
			transaction.replace(R.id.content_fragment, frag_help);
			transaction.commit();
			break;
		default:
			break;
		}

	}

	/** 初始化下方标签颜色 */
	private void defaultTabColor() {
		sign_text.setTextColor(getResources().getColor(R.color.bottom_tabs_normal));
		exception_text.setTextColor(getResources().getColor(R.color.bottom_tabs_normal));
		repair_text.setTextColor(getResources().getColor(R.color.bottom_tabs_normal));
		monitor_text.setTextColor(getResources().getColor(R.color.bottom_tabs_normal));
		help_text.setTextColor(getResources().getColor(R.color.bottom_tabs_normal));

	}

	/** 初始化下方标签图片 */
	private void defeultTabimage() {
		sign_img.setImageResource(R.drawable.sign_in_normal);
		exception_img.setImageResource(R.drawable.question_in_normal);
		repair_img.setImageResource(R.drawable.repair_in_normal);
		monitor_img.setImageResource(R.drawable.monitor_in_normal);
		help_img.setImageResource(R.drawable.help_in_normal);
	}
	
	
	class MainActivityReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
				case "com.kmnfsw.work.backstage.RabbitmqListenService.sendRepairTask"://收到维修任务
					repair_receive_tag.setVisibility(View.VISIBLE);
					closeNotification();
					openNotification();
				break;
				case "com.kmnfsw.work.repair.OtherAppointFragment.RefurbishAction"://收到维修任务刷新动作
					repair_receive_tag.setVisibility(View.INVISIBLE);
					closeNotification();
					handler.removeMessages(0x01);//移除定时播播报通知
				break;
			}
			
		}
		
	}
	
	private Handler handler = new Handler(){
		@Override
		public void dispatchMessage(android.os.Message msg) {
			Bundle data = msg.getData();
			switch (msg.what) {
			case 0x01:
				closeNotification();
				break;

			default:
				break;
			}
		};
	};
	
	/**开启通知振铃*/
	private void openNotification(){
		MediaPlayerUtil.playRing(this);
		VibratorUtil.vibrate(this, new long[]{100,200,300,400}, 2);
		handler.sendEmptyMessageDelayed(0x01, 60*1000);//启动定时播报一分钟
	}
	/**关闭通知振铃*/
	private void closeNotification(){
		MediaPlayerUtil.stopRing();
		VibratorUtil.virateCancle();
	}

	
	//============屏幕滑动监听事件OnTouch，方便Fragment注册使用===============
	private ArrayList<MyOnTouchListener> onTouchListeners = new ArrayList<MyOnTouchListener>(10);
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		for (MyOnTouchListener listener : onTouchListeners) {
			listener.onTouch(ev);
		}
		return super.dispatchTouchEvent(ev);
	}

	/**注册OnTouch事件*/
	public void registerMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
		onTouchListeners.add(myOnTouchListener);
	}

	/**卸载OnTouch事件*/
	public void unregisterMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
		onTouchListeners.remove(myOnTouchListener);
	}

	public interface MyOnTouchListener {
		public boolean onTouch(MotionEvent ev);
	}
	
	

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onRestart() {
		super.onRestart();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//注销广播
		getLocalBroadcastManager.unregisterReceiver(mainActivityReceiver);
		Log.e(Tag, "销毁");
	}

	/**
	 * 按两次返回键退出程序： 用这种方式退出不会结束原有进程
	 */
	// private long mExitTime;
	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// if ((System.currentTimeMillis() - mExitTime) > 2000) {
	// Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
	// mExitTime = System.currentTimeMillis();
	// } else {
	// System.exit(0);
	// }
	// return true;
	// }
	// return super.onKeyDown(keyCode, event);
	// }

	/**
	 * 拦截/屏蔽系统返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Toast.makeText(this, "需切换应用，请按Home键", Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}

}
