package com.kmnfsw.work.version;

import org.xutils.BuildConfig;
import org.xutils.x;

import com.kmnfsw.work.R;
import com.kmnfsw.work.version.utils.MyUtils;
import com.kmnfsw.work.version.utils.VersionUpdateUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

/**
 * 欢迎页面
 * 
 * @author admin
 */
public class SplashActivity extends Activity {

	/** 应用版本号 */
	private TextView mVersionTV;
	/** 本地版本号 */
	private String mVersion;
	
	private VersionUpdateUtils updateUtils;
	private static final String Tag= ".version.SplashActivity";
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/*
		 * xutils框架：http://blog.csdn.net/QPC908694753/article/details/70463709
		 * https://github.com/wyouflf/xUtils3
		 */
		x.Ext.init(this.getApplication());//初始化xutils框架
	    x.Ext.setDebug(BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.
		
		// 设置没有标题栏 在加载布局之前调用
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		mVersion = MyUtils.getVersion(getApplicationContext());
		initView();
		updateUtils = new VersionUpdateUtils(mVersion,
				SplashActivity.this);//创建版本工具实例
		new Thread() {
			public void run() {
				// 获取服务器版本号
				updateUtils.getCloudVersion();
			};
		}.start();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0 && resultCode == Activity.RESULT_CANCELED) {//当取消安装时
			updateUtils.enterHome();
		}
		
	}

	/** 初始化控件 */
	private void initView() {
		mVersionTV = (TextView) findViewById(R.id.tv_splash_version);
		mVersionTV.setText("版本号 " + mVersion);
	}
}