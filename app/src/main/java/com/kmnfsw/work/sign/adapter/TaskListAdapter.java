package com.kmnfsw.work.sign.adapter;

import java.util.List;

import com.kmnfsw.work.R;
import com.kmnfsw.work.sign.entity.CheckTaskEntity;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TaskListAdapter extends BaseAdapter{
	
	private List<CheckTaskEntity> checkTaskList;
	private Context context;
	
	
	public TaskListAdapter(List<CheckTaskEntity> checkTaskList,Context context){
		this.checkTaskList = checkTaskList;
		this.context = context;
	}

	// 适配器中数据集的数据个数；
	@Override
	public int getCount() {
		return checkTaskList.size();
	}

	//获取数据集中与索引对应的数据项；
	@Override
	public Object getItem(int position) {
		return checkTaskList.get(position);
	}

	//获取指定行对应的ID；
	@Override
	public long getItemId(int position) {
		return position;
	}

	//获取每一行Item的显示内容。
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//初始化View视图
		ViewHolder viewHolder = new ViewHolder();
		
		if (convertView != null & convertView instanceof LinearLayout) {
			viewHolder = (ViewHolder)convertView.getTag();
		}else{
			convertView = View.inflate(context, R.layout.task_list_item ,null);
			viewHolder.taskListView = (TextView)convertView.findViewById(R.id.task_list_item);	
		}
		
		//设置text文本内容
		if (checkTaskList !=null) {
			viewHolder.taskListView.setText(checkTaskList.get(position).checkPlanNo+"（"+
					exchangeType(checkTaskList.get(position).peopleType)+"）");
//			viewHolder.taskListView.setText(checkTaskList.get(position).checkPlanNo+"："+checkTaskList.get(position).lineRoadList+"（"+
//					exchangeType(checkTaskList.get(position).peopleType)+"）");
		}
		
		return convertView;
	}
	private String  exchangeType(int peopleType){
		if (peopleType==1) {
			return "巡视";
		}else if(peopleType==2){
			return "维护";
		}else{
			return "未知";
		}
	}
	static class ViewHolder {
		TextView taskListView;
	}

}
