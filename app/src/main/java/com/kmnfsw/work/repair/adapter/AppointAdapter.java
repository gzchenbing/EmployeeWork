package com.kmnfsw.work.repair.adapter;

import java.util.List;

import com.kmnfsw.work.R;
import com.kmnfsw.work.repair.entity.AppointEntity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AppointAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<AppointEntity> listAppointEntity;
	public AppointAdapter(Context context,List<AppointEntity> listAppointEntity){
		this.context = context;
		inflater = LayoutInflater.from(context);
		this.listAppointEntity = listAppointEntity;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listAppointEntity.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return listAppointEntity.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (null == convertView) {
			convertView = inflater.inflate(R.layout.appoint_list_item,null);
            holder = new ViewHolder();
            holder.appoint_repairno = (TextView)convertView.findViewById(R.id.appoint_repairno);
            holder.appoint_exTypeName = (TextView)convertView.findViewById(R.id.appoint_exTypeName);
            holder.appoint_state = (TextView)convertView.findViewById(R.id.appoint_state);
            holder.appoint_time = (TextView)convertView.findViewById(R.id.appoint_time);
            
            convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		holder.appoint_repairno.setText("维修编号："+listAppointEntity.get(position).checkrepairno);
		holder.appoint_exTypeName.setText("问题类型："+listAppointEntity.get(position).extypename);
		holder.appoint_time.setText("发布时间："+listAppointEntity.get(position).releasedate);
		if (listAppointEntity.get(position).state==1) {
			holder.appoint_state.setText("待接收");
			holder.appoint_state.setTextColor(context.getResources().getColor(R.color.dark_red));
		}else if(listAppointEntity.get(position).state==2){
			holder.appoint_state.setText("已接收");
			holder.appoint_state.setTextColor(context.getResources().getColor(R.color.black));
		}
		
		return convertView;
	}
	
	class ViewHolder{
		public TextView appoint_repairno;
		public TextView appoint_exTypeName;
		public TextView appoint_state;
		public TextView appoint_time;
	}

}
