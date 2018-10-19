package com.kmnfsw.work.version.utils;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.kmnfsw.work.R;
import com.kmnfsw.work.util.ProperTies;
import com.kmnfsw.work.util.spring.RequestFactory;
import com.kmnfsw.work.version.entity.VersionEntity;
import com.kmnfsw.work.version.utils.DownLoadUtils.MyCallBack;
import com.kmnfsw.work.welcomeLogin.LoginActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/** 更新提醒工具类 */
public class VersionUpdateUtils {
	private static final int DOWNAPK_ERROR = 404;
	private static final int MESSAGE_NET_EEOR = 101;
	private static final int UNAUTHORIZED_RESPONSE = 401;
	private static final int MESSAGE_SHOEW_DIALOG = 104;
	protected static final int MESSAGE_ENTERHOME = 105;
	
	private String SERVICE_IP;
	
	private static final String TAG = "version.utils.VersionUpdateUtils";
	
	/** 用于更新UI */
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UNAUTHORIZED_RESPONSE:
				Toast.makeText(context, "401非法响应", 0).show();
				enterHome();
				break;
			case  DOWNAPK_ERROR:
				Toast.makeText(context, "网络下载软件包失败", 0).show();
				enterHome();
				break;
			case MESSAGE_NET_EEOR:
				Toast.makeText(context, "网络异常", 0).show();
				enterHome();
				break;
			case MESSAGE_SHOEW_DIALOG:
				showUpdateDialog(versionEntity);
				break;
			case MESSAGE_ENTERHOME://跳转LoginActivity
				Intent intent = new Intent(context,LoginActivity.class);
				context.startActivity(intent);
				context.finish();
				break;
			}
		};
	};
	/** 本地版本号 */
	private String mVersion;
	private Activity context;
	/**进度条对话框*/
	private ProgressDialog mProgressDialog;
	private VersionEntity versionEntity;

	/**
	 * 更新提醒工具类构造器
	 * @param Version
	 * @param activity
	 */
	public VersionUpdateUtils(String Version,Activity activity) {
		mVersion = Version;
		context = activity;
		
		Properties proper = ProperTies.getProperties(context);
		SERVICE_IP = proper.getProperty("serverIp");
	}

	/**
	 * 获取服务器版本号
	 */
	public void getCloudVersion(){
		try {
			// The connection URL 
			String url = SERVICE_IP+"/softUpdate?softwareId=V_EmployeeWork";
			// Set the Accept header
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAccept(Collections.singletonList(new MediaType("application","json")));
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

			// Create a new RestTemplate instance
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			RequestFactory.configTimeoutByReflect(restTemplate,  -1, 2*1000);//请求超时设置
			ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, LinkedHashMap.class);
			//Log.i("打桩 "+TAG, ""+responseEntity);
			if (responseEntity.getStatusCode().value()==200) {//判断状态码是否正常
				LinkedHashMap<String, Object> entity = responseEntity.getBody();
				versionEntity = new VersionEntity();
				versionEntity.versionname = (String)entity.get("softwareVersion");
				versionEntity.apkurl = (String)entity.get("softwareApkurl");
				versionEntity.description = (String)entity.get("softwareDescription");
				if (!mVersion.equals(versionEntity.versionname)) {
					// 版本号不一致
					handler.sendEmptyMessage(MESSAGE_SHOEW_DIALOG);
				}else{
					handler.sendEmptyMessage(MESSAGE_ENTERHOME);
				}
			}
		} catch (HttpClientErrorException e) {
			handler.sendEmptyMessage(UNAUTHORIZED_RESPONSE); 
			Log.e(TAG+".Exception", ""+e);
		}catch (ResourceAccessException e){
			handler.sendEmptyMessage(MESSAGE_NET_EEOR); 
			Log.e(TAG+".Exception", ""+e);
		}catch (Exception e) {
			handler.sendEmptyMessage(MESSAGE_NET_EEOR); 
			Log.e(TAG+".Exception", ""+e);
		}
	}

	/**
	 * 弹出更新提示对话框
	 * 
	 * @param versionEntity
	 */
	private void showUpdateDialog(final VersionEntity versionEntity) {
		// 创建dialog
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("检查到新版本：" + versionEntity.versionname);// 设置标题
		builder.setMessage(versionEntity.description);// 根据服务器返回描述,设置升级描述信息
		builder.setCancelable(false);// 设置不能点击手机返回按钮隐藏对话框
		builder.setIcon(R.drawable.ic_launcher);// 设置对话框图标
		// 设置立即升级按钮点击事件  
		builder.setPositiveButton("立即升级",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						initProgressDialog();
						downloadNewApk(versionEntity.apkurl);
					}
				});
		// 设置暂不升级按钮点击事件
		builder.setNegativeButton("暂不升级",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						enterHome();
					}
				});
		// 对话框必须调用show方法 否则不显示
		builder.show();
	}
	
	/**
	 * 初始化进度条对话框
	 */
	private void initProgressDialog() {
		mProgressDialog = new ProgressDialog(context);
		mProgressDialog.setMessage("准备下载...");
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.show();
	}

	/***
	 * 下载新版本
	 */
	protected void downloadNewApk(String apkurl) {
		DownLoadUtils downLoadUtils = new DownLoadUtils();
		Log.i(TAG+".apkurl", apkurl);
		downLoadUtils.downapk(apkurl, context,new MyCallBack() {
			
			@Override
			public void onSuccess(File fileResult) {
				mProgressDialog.dismiss();
				MyUtils.installApk(context,fileResult);
			}
			
			@Override
			public void onLoadding(long total, long current, boolean isUploading) {
				mProgressDialog.setMax((int)total);
				mProgressDialog.setMessage("正在下载...");
				mProgressDialog.setProgress((int) current);
			}
			
			@Override
			public void onFailure(Throwable ex, boolean isOnCallback) {
				mProgressDialog.dismiss();
				handler.sendEmptyMessage(DOWNAPK_ERROR);
			}

			
		});
	}
	/**延迟几秒钟后直接回到主页面上*/
	public void enterHome() {
		handler.sendEmptyMessageDelayed(MESSAGE_ENTERHOME, 1000);
	}
}