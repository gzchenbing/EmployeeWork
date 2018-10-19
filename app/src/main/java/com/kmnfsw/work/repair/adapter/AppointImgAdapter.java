package com.kmnfsw.work.repair.adapter;

import java.util.List;

import com.kmnfsw.work.R;
import com.kmnfsw.work.repair.adapter.AppointAdapter.ViewHolder;
import com.kmnfsw.work.repair.entity.AppointPhoto;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
/**
 * 维修任务图片适配器
 * @author YanFaBu
 *
 */
public class AppointImgAdapter extends BaseAdapter{

	private static final String Tag = ".repair.adapter.AppointImgAdapter";
	
	private Context context;
	private List<AppointPhoto> listAppointPhoto;
	private LayoutInflater inflater;
	
	public AppointImgAdapter(Context context,List<AppointPhoto> listAppointPhoto){
		super();
		inflater = LayoutInflater.from(context);
		this.listAppointPhoto = listAppointPhoto;
		this.context = context;
	}
	
	@Override
	public int getCount() {
		//Log.i(Tag+"总数", listAppointPhoto.size()+1+"");
		return listAppointPhoto.size()+1;
	}

	@Override
	public Object getItem(int position) {
		if (position == listAppointPhoto.size()) {
			return null;
		}
		
		return listAppointPhoto.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.appoint_apply_grid, null);
			holder = new ViewHolder();
			
			holder.appoint_grid_image = (ImageView)convertView.findViewById(R.id.appoint_grid_image);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		
		if (null !=listAppointPhoto) {
			if (position == listAppointPhoto.size()) {
				//Log.i(Tag, "加载最后一张图片"+position);
				holder.appoint_grid_image.setImageResource(R.drawable.appoint_grid_add_img);
			}else{
				//Log.i(Tag, "加载图片"+position);
				holder.appoint_grid_image.setImageBitmap(listAppointPhoto.get(position).new_bitmap);//加载图片
			}
		}
		
		return convertView;
	}
	
	//具体的子项视图
	class ViewHolder{
		public ImageView appoint_grid_image;
	}

}
