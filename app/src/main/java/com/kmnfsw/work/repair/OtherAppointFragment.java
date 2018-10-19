package com.kmnfsw.work.repair;


import java.util.List;

import com.kmnfsw.work.R;
import com.kmnfsw.work.repair.adapter.AppointAdapter;
import com.kmnfsw.work.repair.entity.AppointEntity;
import com.kmnfsw.work.repair.service.AppointService;
import com.kmnfsw.work.repair.service.AppointService.MBinder;
import com.kmnfsw.work.repair.service.AppointService.OtherAppointCallBack;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
/**
 * 他人指派任务
 * @author YanFaBu
 *
 */
public class OtherAppointFragment extends Fragment {
	private final static String Tag =".repair.OtherAppointFragment";

	private Context context;
	private View view;
	private SharedPreferences shares;
	private TextView appoint_number;
	private ListView appoint_list;

	private MBinder mBinder;

	private String peopleno;
	private List<AppointEntity> listAppointEntity;
	private AppointAdapter adapter;
	/**是否有维修任务消息 ：默认无维修任务*/
	private boolean isAppointNews = false;
	
	///广播
	private LocalBroadcastManager getLocalBroadcastManager;
	private OtherAppointReceiver otherAppointReceiver;
	
	private LocalBroadcastManager setLocalBroadcastManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.appoint_fragment, null);
		context = getActivity();
		shares = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		
		//注册广播进行接收消息
		getLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
		otherAppointReceiver = new OtherAppointReceiver();
		IntentFilter infi = new IntentFilter();
		infi.addAction("com.kmnfsw.work.repair.AppointDetailsActivity.AppointReceive");
		infi.addAction("com.kmnfsw.work.repair.AppointApplyActivity.AppointDelete");
		infi.addAction("com.kmnfsw.work.backstage.RabbitmqListenService.sendRepairTask");
		getLocalBroadcastManager.registerReceiver(otherAppointReceiver, infi);
		
		///设置广播用于推送
		setLocalBroadcastManager = LocalBroadcastManager.getInstance(context);//设置广播实例
		

		peopleno = shares.getString("peopleno", "");

		appoint_number = (TextView) view.findViewById(R.id.appoint_number);
		appoint_list = (ListView) view.findViewById(R.id.appoint_list);

		// 绑定service
		Intent intent = new Intent(context, AppointService.class);
		context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
		return view;
	}

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = (MBinder) service;

			if (!"".equals(peopleno)) {
				mBinder.setOtherAppointCallBack(otherAppointCallBack);
				mBinder.getOtherAppoint(peopleno);
			}

		}
	};

	/** 获取自行任务回调 */
	private OtherAppointCallBack otherAppointCallBack = new OtherAppointCallBack() {

		@Override
		public void getOtherAppointFail(String msgEx) {
			Message msg = new Message();
			msg.what = 0x05;
			Bundle data = new Bundle();
			data.putString("msgEx", msgEx);
			msg.setData(data);
			handler.sendMessage(msg);

		}

		@Override
		public void getOtherAppointData(List<AppointEntity> list) {
			listAppointEntity = list;
			handler.sendEmptyMessage(0x01);

		}
	};

	private Handler handler = new Handler() {
		@Override
		public void dispatchMessage(Message msg) {
			Bundle data = msg.getData();
			switch (msg.what) {
			case 0x01:
				initAppointList();
				break;
			case 0x02://重新刷新适配器
				adapter.notifyDataSetChanged();
				break;
			case 0x05:
				String msgEx = data.getString("msgEx");
				Toast.makeText(context, msgEx, Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		}
	};
	/**初始化维修任务列表*/
	private void initAppointList(){
		appoint_number.setText("  任务个数"+listAppointEntity.size()+"个");
		if (null ==adapter) {
			adapter= new AppointAdapter(context,listAppointEntity);
			appoint_list.setAdapter(adapter);
			appoint_list.setOnItemClickListener(onItemClickListener);
			appoint_list.setOnScrollListener(onScrollListener);
		}else{
			adapter.notifyDataSetChanged();
		}
	}
	
	/**listView点击监听*/
	private OnItemClickListener onItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id){
			Intent intent = new Intent(context, AppointDetailsActivity.class);
			intent.putExtra("checkrepairno", listAppointEntity.get(position).checkrepairno);
			startActivity(intent);
		}
	};
	/**listView滚动监听*/
	private OnScrollListener onScrollListener = new OnScrollListener() {
		//根据scrollState来决定其回调
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://手指触屏拉动准备滚动，只触发一次
				int currentFirst = appoint_list.getFirstVisiblePosition();//当前适配器第一个Item的位置
//				Log.i(Tag, "currentFirst "+currentFirst);
//				Log.i(Tag, "isAppointNews "+isAppointNews);
				if (currentFirst == 0 && isAppointNews) {
					if (!"".equals(peopleno)) {
						isAppointNews = false;
						sendRefurbishAction();//推送刷新后的动作
						
						mBinder.setOtherAppointCallBack(otherAppointCallBack);
						mBinder.getOtherAppoint(peopleno);
						
					}
				}
				break;

			default:
				break;
			}
			
		}
		//滚动时会一直被回调
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			
		}
	};
	/**推送刷新后的广播推送动作：主要推到RepairFragment*/
	private void sendRefurbishAction(){
		Intent intent = new Intent("com.kmnfsw.work.repair.OtherAppointFragment.RefurbishAction");//注册动作
		setLocalBroadcastManager.sendBroadcast(intent);
	}
	
	/**注册广播接受者*/
	class OtherAppointReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (null == listAppointEntity) {
				return;
			}
			
			switch (intent.getAction()) {
			case "com.kmnfsw.work.repair.AppointDetailsActivity.AppointReceive"://领取维修任务
				String checkrepairno = intent.getStringExtra("checkrepairno");
				for (AppointEntity appointEntity : listAppointEntity) {
					if (appointEntity.checkrepairno.equals(checkrepairno)) {//找到任务
						//Log.i(Tag, "接受任务前"+listAppointEntity);
						listAppointEntity.remove(appointEntity);
						appointEntity.state = 2;//将状态改为已接收
						listAppointEntity.add(appointEntity);
						//Log.i(Tag, "接受任务后"+listAppointEntity);
						handler.sendEmptyMessage(0x02);
						break;
					}
				}
				break;

			case "com.kmnfsw.work.repair.AppointApplyActivity.AppointDelete"://删除发送过的任务
				String de_checkrepairno = intent.getStringExtra("checkrepairno");
				for (AppointEntity appointEntity : listAppointEntity) {
					if (appointEntity.checkrepairno.equals(de_checkrepairno)) {
						listAppointEntity.remove(appointEntity);
						
						handler.sendEmptyMessage(0x02);
						break;
					}
					
				}
				break;
			case "com.kmnfsw.work.backstage.RabbitmqListenService.sendRepairTask"://收到维修任务
				//String repairTaskNo = intent.getStringExtra("repairTask"); 
				isAppointNews = true;
				
				
				break;
			default:
				break;
			}
			
		}
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		context.unbindService(conn);
		
		getLocalBroadcastManager.unregisterReceiver(otherAppointReceiver);//注销广播
		
		Log.i(Tag, "销毁！");
	}
}
