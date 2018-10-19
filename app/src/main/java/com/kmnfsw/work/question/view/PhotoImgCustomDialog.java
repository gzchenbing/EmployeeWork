package com.kmnfsw.work.question.view;


import com.kmnfsw.work.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
/**
 * 照相机选择弹出框
 * @author YanFaBu
 *
 */
public class PhotoImgCustomDialog extends Dialog{

	private Context context;
	private RelativeLayout camecattext;
	private RelativeLayout photostext;
	private View.OnClickListener cameralistener;
	private View.OnClickListener photoslistener;
	public PhotoImgCustomDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
		initview();
	}

	public PhotoImgCustomDialog(Context context) {
		super(context);
		this.context = context;
		initview();
		
	}
	
	public void initview(){
		
		setContentView(R.layout.photo_img_dialog);
		camecattext = (RelativeLayout)findViewById(R.id.camera);
		photostext = (RelativeLayout)findViewById(R.id.photos);
		
	}
	
	public void setCameralistener(View.OnClickListener listener){
		cameralistener = listener;
		camecattext.setOnClickListener(cameralistener);
	}
	
	public void setPhotoslistener(View.OnClickListener listener){
		photoslistener = listener;
		photostext.setOnClickListener(photoslistener);
	}
	
}

