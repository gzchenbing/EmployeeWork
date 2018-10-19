package com.kmnfsw.work.question.adapter;

import java.util.List;

import com.kmnfsw.work.R;
import com.kmnfsw.work.question.entity.QuestionTypeEntity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
/***
 * 异常类型适配器
 * @author YanFaBu
 *
 */
public class TypeListAdapter extends BaseAdapter{
	private Context context;
	private List<QuestionTypeEntity> listQuestionType;
	private LayoutInflater inflater;
	
	public TypeListAdapter(Context context,List<QuestionTypeEntity> data){
		inflater = LayoutInflater.from(context);
		this.context = context;
		this.listQuestionType = data;
	}

	
	@Override
	public int getCount() {//总条目数
		return listQuestionType.size();
	}

	@Override
	public Object getItem(int position) {//某一条的对象
		return listQuestionType.get(position);
	}

	@Override
	public long getItemId(int position) {//莫一条对应的列号
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {//具体加载的视图
		ViewHolder holder;
		if (convertView == null){
            convertView = inflater.inflate(R.layout.question_type_listitem,null);
            holder = new ViewHolder();
            holder.type_list_item = (TextView)convertView.findViewById(R.id.type_list_item);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
		holder.type_list_item.setText(listQuestionType.get(position).extypename);
		return convertView;
	}
	
	class ViewHolder{
		public TextView type_list_item;
	}

}
