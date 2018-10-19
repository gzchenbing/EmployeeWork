package com.kmnfsw.work.repair;

import com.kmnfsw.work.R;
import com.kmnfsw.work.question.util.bigImg.IntensifyImageView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class ShowBigImgActivity extends Activity{
	
	private final static String Tag = ".repair.ShowBigImgActivity";

	private String picPath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.show_big_img);
		Intent intent = getIntent();
		picPath = intent.getStringExtra("picPath");
		IntensifyImageView imageView = (IntensifyImageView) findViewById(R.id.big_img);
		// 通过文件路径设置
		imageView.setImage(picPath);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		Log.e(Tag, "销毁！");
	}
}
