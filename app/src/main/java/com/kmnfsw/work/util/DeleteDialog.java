package com.kmnfsw.work.util;

import com.kmnfsw.work.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * 删除弹出框
 * @author YanFaBu
 *
 */
public class DeleteDialog extends Dialog{

	private static final String Tag = ".util.DeleteDialog";
	private Context context;
	private RelativeLayout delete;
	
	public DeleteDialog(Context context) {
		super(context);
		this.context = context;
		initView();
	}
	public DeleteDialog(Context context, int theme) {
		super(context,theme);
		this.context = context;
		
		initView();
	}
	
	private void initView(){
		setContentView(R.layout.delete_dialog);
		delete = (RelativeLayout)findViewById(R.id.delete);
	}
	
	public void setDeleteListener(View.OnClickListener listener){
		delete.setOnClickListener(listener);
	}

}
