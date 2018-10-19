package com.kmnfsw.work.repair;

import java.util.ArrayList;
import java.util.List;

import com.kmnfsw.work.MainActivity;
import com.kmnfsw.work.R;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * Fragment嵌套Fragment进行页面滑动切换 https://www.cnblogs.com/zhrxidian/p/3801545.html
 * @author YanFaBu
 *
 */
public class RepairFragment extends Fragment implements OnGestureListener{

	private final static String Tag = ".repair.RepairFragment"; 
	
	private View view;
	private Context context;
	private TextView other_appoint;
	private TextView oneself_appoint;
	private View slide_other_appoint;
	private View slide_oneself_appoint;
	private RelativeLayout repair_receive_tag;
	private TextView repair_receive_tag_text;
	/**记录所接受到的维修任务*/
	private int receiveTagCount=0;
	
	/**他人维修*/
	private final static int OTHER = 0;
	/**自行维修*/
	private final static int ONESELF = 1;
	/**用于记录当前是哪个Fragment*/
	public int mark=0;
	
	private OtherAppointFragment otherFragment;
	private OneselfAppointFragment oneselfFragment;
	
	/**定义手势两点之间的最小距离*/
	private final int DISTANT=150;
	/**定义手势监测实例*/
	private  GestureDetector mGestureDetector;
	private MainActivity.MyOnTouchListener myOnTouchListener;
	
	///广播
	private LocalBroadcastManager getLocalBroadcastManager;
	private RepairFragmentReceiver repairFragmentReceiver;
	
	private LocalBroadcastManager setLocalBroadcastManager;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.repair_fragment, null);
		context = getActivity();
		
		//注册广播进行接收消息
		getLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
		repairFragmentReceiver = new RepairFragmentReceiver();
		IntentFilter infi = new IntentFilter();
		infi.addAction("com.kmnfsw.work.backstage.RabbitmqListenService.sendRepairTask");
		infi.addAction("com.kmnfsw.work.repair.OtherAppointFragment.RefurbishAction");
		getLocalBroadcastManager.registerReceiver(repairFragmentReceiver, infi);
		
		///设置广播用于推送
		setLocalBroadcastManager = LocalBroadcastManager.getInstance(context);//设置广播实例
		
		initView();
		
		//==============创建手势检测器=========
		mGestureDetector=new GestureDetector(context,this);
		myOnTouchListener=new MainActivity.MyOnTouchListener() {  
			
			@Override  
			public boolean onTouch(MotionEvent ev) {  
				boolean result = mGestureDetector.onTouchEvent(ev);  
				return result;  
			}  
		};  
		((MainActivity)getActivity()).registerMyOnTouchListener(myOnTouchListener);  
		
		return view;

	}
	
	

	private void initView() {
		other_appoint = (TextView)view.findViewById(R.id.other_appoint);
		other_appoint.setOnClickListener(mOnClickListener);
		oneself_appoint = (TextView)view.findViewById(R.id.oneself_appoint);
		oneself_appoint.setOnClickListener(mOnClickListener);
		slide_other_appoint = view.findViewById(R.id.slide_other_appoint);
		slide_oneself_appoint = view.findViewById(R.id.slide_oneself_appoint);
		repair_receive_tag = (RelativeLayout)view.findViewById(R.id.repair_receive_tag);
		repair_receive_tag_text = (TextView)view.findViewById(R.id.repair_receive_tag_text);
		repair_receive_tag.bringToFront(); //将其从底层覆盖中提起出来
		repair_receive_tag.setVisibility(View.INVISIBLE);
		
		selectFragment(OTHER);
	}
	
	
	private OnClickListener mOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.other_appoint:
				selectFragment(OTHER);
				break;
			case R.id.oneself_appoint:
				selectFragment(ONESELF);
				break;

			default:
				break;
			}
			
		}
	};
	
	/**选择、切换不同的Fragment*/
	private void selectFragment(int index){
		FragmentManager fm = getFragmentManager();  
        // 开启Fragment事务  
		FragmentTransaction transaction = fm.beginTransaction();
        switch (index) {
		case OTHER://他人指派
			setDefaultTabStyle();
			other_appoint.setTextColor(getResources().getColor(R.color.bottom_tabs_press));
			slide_other_appoint.setBackgroundResource(R.drawable.slide_view);
			
			//关闭消息提醒
			receiveTagCount=0;
			repair_receive_tag.setVisibility(View.INVISIBLE);
			sendRefurbishAction();
			
			otherFragment = new OtherAppointFragment();
			transaction.replace(R.id.viewpager_content, otherFragment);
			transaction.commit();
			mark = OTHER;
			break;
		case ONESELF://自行指派
			setDefaultTabStyle();
			oneself_appoint.setTextColor(getResources().getColor(R.color.bottom_tabs_press));
			slide_oneself_appoint.setBackgroundResource(R.drawable.slide_view);
			
			oneselfFragment = new OneselfAppointFragment();
			transaction.replace(R.id.viewpager_content, oneselfFragment);
			transaction.commit();
			mark = ONESELF;
			break;
		default:
			break;
		}
	}
	
	/**推送刷新后的广播推送动作关闭振铃：主要推到MainActivity*/
	private void sendRefurbishAction(){
		Intent intent = new Intent("com.kmnfsw.work.repair.OtherAppointFragment.RefurbishAction");//注册动作
		setLocalBroadcastManager.sendBroadcast(intent);
	}
	
	/**移除选定的Fragment*/
	private void removeFragment(int index){

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		switch (index) {
		case OTHER:
			transaction.remove(otherFragment);
			transaction.commit();
			break;
		case ONESELF:
			transaction.remove(oneselfFragment);
			transaction.commit();
			break;

		default:
			break;
		}
	}
	
	/**设置默认样式*/
	private void setDefaultTabStyle(){
		other_appoint.setTextColor(getResources().getColor(R.color.black));
		oneself_appoint.setTextColor(getResources().getColor(R.color.black));
		
		slide_other_appoint.setBackgroundResource(0);
		slide_oneself_appoint.setBackgroundResource(0);
	}
	
   
	/**向下滑动手势*/
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	/**Touch了还没有滑动时触发*/
	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	/**点击一下非常快的（不滑动）*/
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	/**Touch了滑动时触发*/
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	/**Touch了不移动一直Touch down时触发*/
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	/**Touch了滑动一点距离后，up时触发*/
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// e1 代表手指第一次触摸屏幕的事件
		// e2 代表手指离开屏幕一瞬间的事件
		// velocityX 水平方向的速度 单位 pix/s
		// velocityY 竖直方向的速度
		
		if(mark==0){//当前是第一个时
			if(e2.getX()>e1.getX()+DISTANT){//手势向右滑
				selectFragment(ONESELF);
				getActivity().overridePendingTransition(R.anim.pre_in,
						R.anim.pre_out);//设置页面切换动画
				return true;
			}else{
				return false;
			}
			
		}else if(mark==1){//当前是第二个时
			if(e1.getX()>e2.getX()+DISTANT){//手势向左滑
				selectFragment(OTHER);
				getActivity().overridePendingTransition(R.anim.next_in,
						R.anim.next_out);//设置页面切换动画
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
		
	}
	
	class RepairFragmentReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
			case "com.kmnfsw.work.backstage.RabbitmqListenService.sendRepairTask"://收到维修任务
				receiveTagCount++;//自增1
				repair_receive_tag.setVisibility(View.VISIBLE);//打开消息提醒
				repair_receive_tag_text.setText(""+receiveTagCount);
				break;
			case "com.kmnfsw.work.repair.OtherAppointFragment.RefurbishAction"://收到维修任务刷新动作
				receiveTagCount=0;
				repair_receive_tag.setVisibility(View.INVISIBLE);//关闭消息提醒
				break;

			default:
				break;
			}
			
		}
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		removeFragment(mark);
		((MainActivity)getActivity()).unregisterMyOnTouchListener(myOnTouchListener); 
		
		
		getLocalBroadcastManager.unregisterReceiver(repairFragmentReceiver);//注销广播
		
		Log.e(Tag, "销毁！");
	}
	
}
