package com.kmnfsw.work.help.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kmnfsw.work.R;

/**gridview适配器*/
public class HelpGridviewAdapter extends BaseAdapter{
	
	int[] imageId = {R.drawable.help_gridview_img1,R.drawable.help_gridview_img2,R.drawable.help_gridview_img3};
	String[] names ={"使用帮助","作业指导","安全生产"};

	private Context context;
	public HelpGridviewAdapter(Context context){
		this.context = context;
	}
	/**设置条目总数*/
	@Override
	public int getCount() {
		return 3;
	}

	/**设置数据集中与指定索引对应的数据项*/
	@Override
	public Object getItem(int position) {
		return null;
	}

	/**设置列表中与指定索引对应的行id*/
	@Override
	public long getItemId(int position) {
		return 0;
	}

	/**设置每个条目的界面*/
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = View.inflate(context,
				R.layout.help_grid_view, null);
		ImageView help_grid_image = (ImageView)view.findViewById(R.id.help_grid_image);
		help_grid_image.setImageResource(imageId[position]);
		TextView help_grid_text = (TextView)view.findViewById(R.id.help_grid_text);
		help_grid_text.setText(names[position]);
		return view;
	}

}
