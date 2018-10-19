package com.kmnfsw.work.repair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.kmnfsw.work.R;
import com.kmnfsw.work.question.util.MediaManager;
import com.kmnfsw.work.question.view.AudioRecorderButton;
import com.kmnfsw.work.question.view.PhotoImgCustomDialog;
import com.kmnfsw.work.question.view.AudioRecorderButton.AudioFinishRecorderListener;
import com.kmnfsw.work.repair.adapter.AppointImgAdapter;
import com.kmnfsw.work.repair.entity.AppointPhoto;
import com.kmnfsw.work.repair.entity.AppointReportEntity;
import com.kmnfsw.work.repair.entity.Picture;
import com.kmnfsw.work.repair.service.AppointDetailsService;
import com.kmnfsw.work.repair.service.AppointDetailsService.MBinder;
import com.kmnfsw.work.util.DeleteDialog;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;

public class AppointApplyActivity extends Activity{

	private final static String Tag = ".repair.AppointApplyActivity";
	private SharedPreferences shares;
	private MBinder mBinder;
	
	private String checkrepairno;
	private String peopleno;

	private RelativeLayout big_imgv_leftbtn;
	private Button appoint_send;
	private EditText appoint_apply_userMaterial;
	private EditText appoint_apply_depict;
	
	private AudioRecorderButton record_btn;
	private FrameLayout voice_play;
	private View voice_loud;
	/** 录音文件位置 */
	private String voice_filepath;
	private String voiceName = "";
	private String voiceData = "";
	
	private GridView img_gridview;
	
	private List<Picture> listPicture = new ArrayList<>();
	private List<AppointPhoto> listAppointPhoto = new ArrayList<>();
	private String photo_path;
	
	
	private AppointImgAdapter adapter;
	private static final int IMAGE = 1;
	private static final int CAMERA = 2;
	
	private LocalBroadcastManager setLocalBroadcastManager;
	
	/**是否正在申请*/
	private boolean isAppling = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 去除标题头部
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 解决软盘弹出影响布局
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.appoint_apply_pass);
		
		shares = getSharedPreferences("config", Context.MODE_PRIVATE);
		setLocalBroadcastManager = LocalBroadcastManager.getInstance(this);//设置广播实例
		
		Intent intent = getIntent();
		checkrepairno = intent.getStringExtra("checkrepairno");
		peopleno = intent.getStringExtra("peopleno");
		
		initView();
		
		Intent service = new Intent(this, AppointDetailsService.class);
		bindService(service, conn, Context.BIND_AUTO_CREATE);
	}
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = (MBinder) service;
		}
	};
	
	/**初始化视图*/
	private void initView(){
		big_imgv_leftbtn = (RelativeLayout)findViewById(R.id.big_imgv_leftbtn);
		appoint_send = (Button)findViewById(R.id.appoint_send);
		appoint_apply_userMaterial = (EditText)findViewById(R.id.appoint_apply_userMaterial);
		appoint_apply_depict = (EditText)findViewById(R.id.appoint_apply_depict);
		record_btn = (AudioRecorderButton) findViewById(R.id.record_btn);
		voice_play = (FrameLayout) findViewById(R.id.voice_btn);
		voice_loud = (View) findViewById(R.id.id_recoder_anim);
		img_gridview = (GridView)findViewById(R.id.img_gridview);
		if (null == adapter) {
			adapter = new AppointImgAdapter(this, listAppointPhoto);
			img_gridview.setAdapter(adapter);
		}else{
			adapter.notifyDataSetChanged();
		}
		img_gridview.setOnItemClickListener(onItemClickListener);//GridView点击事件
		img_gridview.setOnItemLongClickListener(onItemLongClickListener);//GridView长按事件
		
		big_imgv_leftbtn.setOnClickListener(mOnClickListener);
		appoint_send.setOnClickListener(mOnClickListener);
		record_btn.setAudioFinishRecorderListener(mAudioFinishListener);
	}
	
	//GridView点击事件
	private OnItemClickListener onItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
			
			if (listAppointPhoto.size() == position) {//如果是最后一张加号图片
				if (position > 4) {//控制所添加的图片数量少于5张
					Toast.makeText(getApplicationContext(), "最多只能添加5张图片！", Toast.LENGTH_SHORT).show();
					return;
				}
				//选择图片
				showPhotoImgDialog();
				
			}else{
				//查看大图
				String picPath = listAppointPhoto.get(position).photo_path;
				Intent intent = new Intent(getApplicationContext(), ShowBigImgActivity.class);
				intent.putExtra("picPath", picPath);
				startActivity(intent);
			}
		}
	};
	
	//GridView长按事件
	private OnItemLongClickListener onItemLongClickListener = new OnItemLongClickListener(){

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			
			if (listAppointPhoto.size() == position) {//如果是最后一张加号图片
				return false;
			}else{
				//删除图片
				longClickDeleteImg(position);
				
				return true;
			}
		}
		
	};
	
	/**删除图片*/
	private void longClickDeleteImg(final int item){
		
		final DeleteDialog dialog = new DeleteDialog(this,  R.style.CustomDialog);
		dialog.setDeleteListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listAppointPhoto.remove(item);
				listPicture.remove(item);
				handler.sendEmptyMessage(0x02);
				
				dialog.dismiss();
				
				//Log.i(Tag, "删除后的listPicture数"+listPicture.size());
			}
		});
		
		dialog.show();
		dialog.setCanceledOnTouchOutside(true);// 设置点击弹出dialog以外区域进行dialog隐藏
	}
	
	
	
	/** 展现拍照图片控件dialog */
	private void showPhotoImgDialog() {

		final PhotoImgCustomDialog dialog = new PhotoImgCustomDialog(this, R.style.CustomDialog);
		dialog.setCameralistener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				Calendar calendar = Calendar.getInstance();
				Date date = new Date();
				calendar.setTime(date);
				String datetimestr = sdf.format(date);
				String temp_photo_name = datetimestr + ".jpg";
				photo_path = getDiskCacheDir() + File.separator + temp_photo_name;
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(photo_path)));
				intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

				startActivityForResult(intent, CAMERA);// 与响应setResult()对应
				dialog.dismiss();
			}
		});

		dialog.setPhotoslistener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, IMAGE);
				dialog.dismiss();
			}
		});
		dialog.show();
		dialog.setCanceledOnTouchOutside(true);// 设置点击弹出dialog以外区域进行dialog隐藏
	}
	
	/**获取本地应用路径*/
	private String getDiskCacheDir() {
		String cachepath;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable()) {
			cachepath = getExternalCacheDir().getPath() + File.separator + "photo";
		} else {
			cachepath = getCacheDir().getPath() + File.separator + "photo";
		}
		File fileDir = new File(cachepath);
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		return cachepath;
	}
	
	
	/** 用于处理子项activity向父项的返回 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//Log.i(Tag, "case the onActivityResult requestCode:" + requestCode + ";resultCode:" + resultCode);
		if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {// 从相册中选择
			Uri selectedImage = data.getData();
			String[] filePathColumns = { MediaStore.Images.Media.DATA };
			Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
			c.moveToFirst();
			int columnIndex = c.getColumnIndex(filePathColumns[0]);
			photo_path = c.getString(columnIndex);
			
			if (photo_path == null || "".equals(photo_path)) {
				c.close();
				return;
			}

			fillPhotoOperate(photo_path, 4, 1024 * 1024);// 最大上传125k

			c.close();
		}
		if (requestCode == CAMERA && resultCode == Activity.RESULT_OK) {// 拍照后得到的照片

			fillPhotoOperate(photo_path, 8, 1024 * 1024);// 最大上传125k

		}
	}
	
	/** 进行图片文件操作 */
	private void fillPhotoOperate(final String path, final int scale, final int maxSize) {

		new Thread() {
			@Override
			public void run() {
				Options opts = new Options();
				opts.inSampleSize = scale;
				Bitmap bitmap = BitmapFactory.decodeFile(path, opts);

				// 使用此流读取
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 100是压缩率，表示压缩百分之0。70压缩率，表示压缩30%
				try {
					byte[] imgBytes = baos.toByteArray();
					String pictureData = Base64.encodeToString(imgBytes, Base64.DEFAULT);
					
					//Log.i(Tag, "photo_str size:" + pictureData.length());
					// Log.i(Tag, "pictureData：" + pictureData);
					if (pictureData.length() > maxSize) {
						bitmap.recycle();
						fillPhotoOperate(path, scale * 2, maxSize);
					} else {
						if (bitmap != null) {
							int height = bitmap.getHeight();
							int width = bitmap.getWidth();
							int templength;
							int startx;
							int starty;
							if (height > width) {
								templength = width;
								startx = 0;
								starty = (height - width) / 2;
							} else {
								templength = height;
								starty = 0;
								startx = (width - height) / 2;
							}
							Bitmap new_bitmap = Bitmap.createBitmap(bitmap, startx, starty, templength, templength);
							bitmap.recycle();

							///将最终数据进行封装
							Picture picture = new Picture();
							String pictureName = path.substring(path.lastIndexOf("/") + 1);
							picture.pictureName = pictureName;
							picture.pictureData = pictureData;
							listPicture.add(picture);
							
							listAppointPhoto.add(new AppointPhoto(photo_path, new_bitmap));
							handler.sendEmptyMessage(0x02);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						baos.flush();
						baos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			};
		}.start();

	}
	
	/** 录音文件位置回调 */
	AudioFinishRecorderListener mAudioFinishListener = new AudioFinishRecorderListener() {

		@Override
		public void onFinish(float seconds, String filePath) {

			voice_play.setVisibility(View.VISIBLE);
			voice_play.setOnClickListener(mOnClickListener);

			voice_filepath = filePath;
			//Log.i(Tag, "voice_filepath：" + voice_filepath);

			if (filePath != null) {
				voiceName = voice_filepath.substring(voice_filepath.lastIndexOf("/") + 1);
				try {
					voiceData = encodeBase64File(voice_filepath);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				voiceName = "";voiceData = "";
			}
		}

		@Override
		public void onCancel() {
			voice_play.setVisibility(View.INVISIBLE);// 隐藏录音展现控件
		}
	};
	
	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.voice_btn:
				if (voice_filepath == null) {
					return;
				}
				voice_loud.setBackgroundResource(R.drawable.adj);
				voice_loud.setBackgroundResource(R.drawable.play_anim);
				AnimationDrawable animation = (AnimationDrawable) voice_loud.getBackground();
				animation.start();

				MediaManager.playSound(getApplicationContext(), voice_filepath, new MediaPlayer.OnCompletionListener() {

					public void onCompletion(MediaPlayer mp) {
						voice_loud.setBackgroundResource(R.drawable.adj);
					}
				});
				break;

			case R.id.big_imgv_leftbtn:
				finish();
				break;
			case R.id.appoint_send://发送数据
				if(isAppling){
					return;
				}
				isAppling = true;
				btnAppointEntitySend();
				break;
			default:
				break;
			}
			
		}
		
	};
	
	private Handler handler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			Bundle data = msg.getData();
			switch (msg.what) {
			case 0x01://发送成功view视图初始化数据
				String msgSc = data.getString("msgSc");
				Toast.makeText(getApplicationContext(), msgSc, Toast.LENGTH_SHORT).show();
				isAppling = false;
				///向OtherAppointFragment和OneselfAppointFragment应用推送数据，删除发送过的任务
				sendBroadcastManager();
				
				finish();//关闭此页
				break;
			case 0x02:
				if (adapter !=null) {
					adapter.notifyDataSetChanged();
				}
				break;
			case 0x05://处理异常
				String msgEx = data.getString("msgEx");
				Toast.makeText(getApplicationContext(), msgEx, Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
			
		};
	};
	
	/**推送广播*/
	private void sendBroadcastManager(){
		Intent intent = new Intent("com.kmnfsw.work.repair.AppointApplyActivity.AppointDelete");//注册动作
		intent.putExtra("checkrepairno", checkrepairno);
		setLocalBroadcastManager.sendBroadcast(intent);
	}
	
	/**发送数据*/
	private void btnAppointEntitySend(){
		if (null == checkrepairno || "".equals(checkrepairno)) {
			return;
		}
		if (null == peopleno || "".equals(peopleno)) {
			return;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		Date date = new Date();
		calendar.setTime(date);
		String datetimestr = sdf.format(date);
		
		AppointReportEntity appointReportEntity = new AppointReportEntity();
		appointReportEntity.checkrepairno = checkrepairno;
		appointReportEntity.peopleno = peopleno;
		appointReportEntity.reportDate = datetimestr;
		appointReportEntity.userMaterial = appoint_apply_userMaterial.getText().toString().trim();
		appointReportEntity.depict = appoint_apply_depict.getText().toString().trim();
		appointReportEntity.voiceName = voiceName;
		appointReportEntity.voiceData = voiceData;
		appointReportEntity.listPicture = listPicture;
		
		//Log.i(Tag, "发送前的listPicture数"+listPicture.size());
		
		mBinder.setAppointApplyBack(AppointApplyBack);
		mBinder.AppointApply(appointReportEntity);
	}
	
	
	private AppointDetailsService.AppointApplyBack AppointApplyBack = new AppointDetailsService.AppointApplyBack() {
		
		@Override
		public void AppointApplySuccess(String msgSc) {
			Message msg = new Message();
			msg.what = 0x01;
			Bundle data = new Bundle();
			data.putString("msgSc", msgSc);
			msg.setData(data);
			handler.sendMessage(msg);
			
		}
		
		@Override
		public void AppointApplyFail(String msgEx) {
			Message msg = new Message();
			msg.what = 0x05;
			Bundle data = new Bundle();
			data.putString("msgEx", msgEx);
			msg.setData(data);
			handler.sendMessage(msg);
			
		}
	};
	
	
	
	/** 将文件转换为字符串字节 */
	public String encodeBase64File(String path) throws Exception {
		File file = new File(path);
		FileInputStream inputFile = new FileInputStream(file);
		byte[] buffer = new byte[(int) file.length()];
		inputFile.read(buffer);
		inputFile.close();
		return Base64.encodeToString(buffer, Base64.DEFAULT);
	}
	
	/**删除文件夹*/
	private  void  folderDelete(File file) {
        //生成File[]数组   listFiles()方法获取当前目录里的文件夹  文件
        File[] files = file.listFiles();
        //判断是否为空   //有没有发现讨论基本一样
        if(files!=null){
            //遍历
            for (File file2 : files) {
                //是文件就删除
                if(file2.isFile()){
                    file2.delete();
                }else if(file2.isDirectory()){
                    //是文件夹就递归
                	folderDelete(file2);
                    //空文件夹直接删除
                    file2.delete();
                }
            }
        }
        
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		// 如果录音播放管理对象存在，就进行关闭
		MediaManager.pause();
		MediaManager.release();

		unbindService(conn);
		
		File photoFile = new File(getDiskCacheDir());//删除保存的图片
		folderDelete(photoFile);
		
		Log.i(Tag, "销毁！");
	}
	
}
