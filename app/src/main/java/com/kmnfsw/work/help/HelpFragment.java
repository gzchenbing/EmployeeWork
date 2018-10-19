package com.kmnfsw.work.help;

import com.kmnfsw.work.R;
import com.kmnfsw.work.backstage.LocalhostReportService;
import com.kmnfsw.work.help.adapter.HelpGridviewAdapter;
import com.kmnfsw.work.help.service.HelpService;
import com.kmnfsw.work.help.service.HelpService.EscCallback;
import com.kmnfsw.work.help.service.HelpService.MBinder;
import com.kmnfsw.work.sign.db.dao.CheckedPointDao;
import com.kmnfsw.work.sign.db.dao.TaskPointDao;
import com.kmnfsw.work.util.ServiceState;
import com.kmnfsw.work.welcomeLogin.LoginActivity;
import com.kmnfsw.work.welcomeLogin.view.MonIndicator;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HelpFragment extends Fragment{
	private static final String Tag = ".help.HelpFragment";

	private Activity activity;
	
	private Context context;
	private View view;
	private SharedPreferences shares;
	private LocalBroadcastManager localBroadcastManager;
	private MBinder mBinder;
	
	private String peopleno;
	
	private WindowManager windowManager;
	private View loadingLayout;
	
	
	private TextView help_tx_name;
	private TextView help_tx_peopleno;
	private LinearLayout help_esc;
	private GridView help_gridview;
	
	public HelpFragment(Activity activity){
		this.activity = activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		context = getActivity();
		windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		view = inflater.inflate(R.layout.help_fragment, null);
		shares = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		
		localBroadcastManager = LocalBroadcastManager.getInstance( context );
		
		//绑定service
		Intent intent = new Intent(context,HelpService.class);
		context.bindService(intent,connection,Context.BIND_AUTO_CREATE);
		
		
		peopleno = shares.getString("peopleno", "");
		if ("".equals(peopleno)) {
			Intent it = new Intent(context,LoginActivity.class);
			startActivity(it);
			getActivity().finish();
		}
		initView();
		initData();
		
		return view;
	}
	
	/**初始化视图*/
	private void initView(){
		
		help_tx_name = (TextView)view.findViewById(R.id.help_tx_name);
		help_tx_peopleno = (TextView)view.findViewById(R.id.help_tx_peopleno);
		help_esc = (LinearLayout)view.findViewById(R.id.help_esc);
		help_esc.setBackgroundResource(R.drawable.util_button_operate);
		help_esc.setOnClickListener(onClickListener);
		
		help_gridview = (GridView)view.findViewById(R.id.help_gridview);
		HelpGridviewAdapter helpGridviewAdapter = new HelpGridviewAdapter(context);
		help_gridview.setAdapter(helpGridviewAdapter);
		help_gridview.setOnItemClickListener(onItem_help_gridview);
		
		
	}
	
	/**初始化数据*/
	private void initData(){
		help_tx_peopleno.setText(peopleno);
		
		String name = shares.getString("name", "");
		help_tx_name.setText(name);
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
		}
	};
	
	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.help_esc://退出操作
				escing();//开启退出动画
				
				escOperate();
				break;
			default:
				break;
			}
			
		}
	}; 
	
	private EscCallback escCallback = new EscCallback() {
		
		@Override
		public void escDataSuccess(boolean isSuccess) {
			if (isSuccess) {
				Message msg = new Message();
				msg.what = 0x01;
				handler.sendMessage(msg);
			}else{
				Message msg = new Message();
				msg.what = 0x02;
				handler.sendMessageDelayed(msg, 1000*5);
			}
			
		}

		@Override
		public void escDataError(String msgEx) {
			Message msg = new Message();
			msg.what = 0x03;
			Bundle data = new Bundle();
			data.putString("msgEx", msgEx);
			handler.sendMessage(msg);
		}
	};
	
	
	Handler handler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			Bundle data = msg.getData();
			switch (msg.what) {
			case 0x01:
				//进入登录页
				windowManager.removeView(loadingLayout);//关闭动画
				startActivity(LoginActivity.class);
				getActivity().finish();
				break;
			case 0x02:
				//进入登录页
				windowManager.removeView(loadingLayout);//关闭动画
				Toast.makeText(context, "退出失败请重试", Toast.LENGTH_SHORT).show();
				break;
			case 0x03:
				//提示异常信息
				windowManager.removeView(loadingLayout);//关闭动画
				Toast.makeText(context, data.getString("msgEx"), Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};
	
	
	
	/**gridview视图点击事件*/
	private OnItemClickListener onItem_help_gridview = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id){
			switch (position) {
			case 0:
				
				break;

			default:
				break;
			}
		};
	};
	
	/**退出操作*/
	private void escOperate(){
		
		String checkTaskNo = shares.getString("checkTaskNo", "");
		if (!"".equals(checkTaskNo)) {//巡检工退出时
			//清除SignFragment中遗留的数据
			mBinder.setCheckEscCallback(escCallback);;
			mBinder.operateCheckEsc();
		}else{//未知员工退出
			mBinder.setDefaultEscCallback(escCallback);
			mBinder.operateDefaultEsc();
		}
		
		//维修工退出时
		
		//维修巡检工退出
		
	}
	
	
	private void startActivity(Class<?> cls) {
		Intent intent = new Intent(context, cls);
		startActivity(intent);
	}
	/**
	 * 开始gif动画
	 */
	private void escing(){
		if(loadingLayout==null){
			LayoutInflater  inflayter = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			loadingLayout = inflayter.inflate(R.layout.loading_view, null);
		}
		
		MonIndicator monIndicator =(MonIndicator) loadingLayout.findViewById(R.id.monIndicator);
		monIndicator.setColors(new int[]{0xFFff1493, 0xFFff1493, 0xFFff1493, 0xFFff1493, 0xFFff1493});
		
		TextView monIndicator_tx = (TextView)loadingLayout.findViewById(R.id.monIndicator_tx);
		monIndicator_tx.setText("正在退出");
		windowManager.addView(loadingLayout, getDialogParmas());//添加覆着物
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
	@Override
	public void onDestroy(){
		super.onDestroy();
		context.unbindService(connection);//解除service绑定
		//Log.e(Tag, "销毁");
	}
	
	
}
