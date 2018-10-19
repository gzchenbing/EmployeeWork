package com.kmnfsw.work.sign;

import com.kmnfsw.work.R;
import com.kmnfsw.work.sign.entity.TaskContentEntity;
import com.kmnfsw.work.sign.service.SignTaskContentService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SignTaskActivity extends Activity{
	
	private static final String Tag = ".sign.SignTaskActivity";
	
	private SharedPreferences shares;
	private LocalBroadcastManager getTaskContentBroadcas;
	private TaskContentBroadcast taskContentBroadcast;
	
	private String peopleno;
	private String checkTaskNo;
	
	private ImageView imgv_leftbtn;
	private TextView sign_task_people;
	private TextView sign_task_line;
	private TextView sign_task_starttime;
	private TextView sign_task_endtime;
	private TextView sign_task_type;
	private TextView sign_task_cycle;
	private TextView sign_task_no;
	private RelativeLayout Layout_imgv_leftbtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sign_task_activity);
		shares = getSharedPreferences("config", Context.MODE_PRIVATE);
		
		Intent intent = getIntent();
		peopleno = intent.getStringExtra("peopleno");
		checkTaskNo = intent.getStringExtra("checkTaskNo");
		initView();
		
		//注册广播接受
		getTaskContentBroadcas = LocalBroadcastManager.getInstance( getApplication() );
		taskContentBroadcast = new TaskContentBroadcast();
		IntentFilter infi = new IntentFilter("com.kmnfsw.work.sign.servic.SignTaskContentService.taskContent");
		getTaskContentBroadcas.registerReceiver(taskContentBroadcast, infi);
		
		
		//启动SignTaskContentService
		Intent setServer = new Intent(getApplication(),SignTaskContentService.class);
		setServer.putExtra("peopleno", peopleno);
		setServer.putExtra("checkTaskNo", checkTaskNo);
		startService(setServer);
		
		Layout_imgv_leftbtn.setOnClickListener(onClickListener);
		
	}
	
	/**初始化视图*/
	private void initView(){
		///////设置标题
		((TextView)findViewById(R.id.tv_title)).setText("巡检任务详情");
		imgv_leftbtn = (ImageView)findViewById(R.id.imgv_leftbtn);
		imgv_leftbtn.setImageResource(R.drawable.back);
		
		Layout_imgv_leftbtn = (RelativeLayout)findViewById(R.id.Layout_imgv_leftbtn);
		Layout_imgv_leftbtn.setBackgroundResource(R.drawable.sign_button_operate);
		
		//设置导航栏颜色
		findViewById(R.id.rl_titlebar).setBackgroundColor(
				getResources().getColor(R.color.bottom_tabs_press));
		
		sign_task_people = (TextView)findViewById(R.id.sign_task_people);
		sign_task_line = (TextView)findViewById(R.id.sign_task_line);
		sign_task_starttime = (TextView)findViewById(R.id.sign_task_starttime);
		sign_task_endtime = (TextView)findViewById(R.id.sign_task_endtime);
		sign_task_type = (TextView)findViewById(R.id.sign_task_type);
		sign_task_cycle = (TextView)findViewById(R.id.sign_task_cycle);
		sign_task_no = (TextView)findViewById(R.id.sign_task_no);
	}
	
	class TaskContentBroadcast extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
			case "com.kmnfsw.work.sign.servic.SignTaskContentService.taskContent":
				TaskContentEntity taskContentEntity = (TaskContentEntity)intent.getSerializableExtra("taskContentEntity");
				//Log.i(Tag, ""+taskContentEntity);
				initViewData(taskContentEntity);
				break;
			case "com.kmnfsw.work.sign.servic.SignTaskContentService.TaskException":
				String ExMassage = intent.getStringExtra("exceptionStr");
				Toast.makeText(getApplication(), ExMassage, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
			
		}
		
	}
	/**初始化视图数据*/
	private void initViewData(TaskContentEntity taskContentEntity){
		sign_task_people.setText(taskContentEntity.peopleno);
		sign_task_line.setText(taskContentEntity.linesName);
		sign_task_starttime.setText(taskContentEntity.startTaskDate);
		sign_task_endtime.setText(taskContentEntity.endTaskDate);
		sign_task_type.setText(taskContentEntity.taskType);
		sign_task_cycle.setText(taskContentEntity.taskCycl);
		sign_task_no.setText(shares.getString("checkPlanNo", ""));
	}
	
	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.Layout_imgv_leftbtn:
				finish();
				break;

			default:
				break;
			}
			
		}
		
		
	};
	
	@Override
	protected void onStop() {
		super.onStop();
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		getTaskContentBroadcas.unregisterReceiver(taskContentBroadcast);//注销广播
		Log.e(Tag, "销毁");
	}
	
	
}
