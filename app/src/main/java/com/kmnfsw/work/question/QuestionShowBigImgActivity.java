package com.kmnfsw.work.question;

import com.kmnfsw.work.R;
import com.kmnfsw.work.question.util.bigImg.IntensifyImageView;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 大图展示activity
 * 
 * @author YanFaBu
 *
 */
public class QuestionShowBigImgActivity extends Activity {

	private final static String Tag = ".question.ShowBigImgActivity";
	private static final int TEXT_COLOR1 = Color.argb(11, 011, 110, 0);

	private String photo_path;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 解决软盘弹出影响布局
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

		setContentView(R.layout.question_show_big_img);
		initView();

		Intent intent = getIntent();
		photo_path = intent.getStringExtra("photo_path");
		IntensifyImageView imageView = (IntensifyImageView) findViewById(R.id.intensify_image);
		// 通过文件路径设置
		imageView.setImage(photo_path);
	}

	/** 初始化所有视图 */
	private void initView() {
		/////// 设置标题
		((TextView) findViewById(R.id.big_img_title)).setText("图片");
		ImageView imgv_leftbtn = (ImageView) findViewById(R.id.big_leftbtn);
		imgv_leftbtn.setImageResource(R.drawable.back);

		TextView imgv_rightbtn = (TextView) findViewById(R.id.big_rightbtn);
		imgv_rightbtn.setText("重新选择图片");
		imgv_rightbtn.setTextColor(getResources().getColor(R.color.white));
		imgv_rightbtn.setTextSize(16f);

		RelativeLayout Layout_imgv_leftbtn = (RelativeLayout) findViewById(R.id.big_imgv_leftbtn);
		Layout_imgv_leftbtn.setBackgroundResource(R.drawable.sign_button_operate);
		Layout_imgv_leftbtn.setOnClickListener(onClickListener);

		RelativeLayout Layout_imgv_rightbtn = (RelativeLayout) findViewById(R.id.big_imgv_rightbtn);
		Layout_imgv_rightbtn.setBackgroundResource(R.drawable.sign_button_operate);
		Layout_imgv_rightbtn.setOnClickListener(onClickListener);

		// 设置导航栏颜色
		findViewById(R.id.big_img_titlebar).setBackgroundColor(getResources().getColor(R.color.bottom_tabs_press));
	}

	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.big_imgv_leftbtn:
				finish();
				break;

			case R.id.big_imgv_rightbtn:
				setResult(RESULT_OK);// 标注返回给父项的响应代码
				finish();
				break;
			default:
				break;
			}

		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.e(Tag, "服务销毁！");
	}

}
