package com.kmnfsw.work.question;

import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.kmnfsw.work.R;
import com.kmnfsw.work.question.adapter.TypeListAdapter;
import com.kmnfsw.work.question.entity.QuestionEntity;
import com.kmnfsw.work.question.entity.QuestionTypeEntity;
import com.kmnfsw.work.question.service.QuestionService;
import com.kmnfsw.work.question.service.QuestionService.ExTypeCallBack;
import com.kmnfsw.work.question.service.QuestionService.LnOrganizeCallBack;
import com.kmnfsw.work.question.service.QuestionService.LocationCallBack;
import com.kmnfsw.work.question.service.QuestionService.MBinder;
import com.kmnfsw.work.question.service.QuestionService.QuestionReportCallBack;
import com.kmnfsw.work.question.util.MediaManager;
import com.kmnfsw.work.question.util.androidTree.Node;
import com.kmnfsw.work.question.util.androidTree.NodeTreeAdapter;
import com.kmnfsw.work.question.util.androidTree.NodeTreeAdapter.TreeItemClickCallback;
import com.kmnfsw.work.question.view.AudioRecorderButton;
import com.kmnfsw.work.question.view.AudioRecorderButton.AudioFinishRecorderListener;
import com.kmnfsw.work.question.view.PhotoImgCustomDialog;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class QuestionFragment extends Fragment {
	private static final String Tag = ".question.QuestionFragment";

	private View view;
	private Context context;
	private MBinder mBinder;
	private SharedPreferences shares;

	private View treeContainer;
	private PopupWindow treePopupWindow;
	/** 路线节点集合 */
	private LinkedList<Node> mLinkedList;
	private Node lnOrganize;

	private RelativeLayout question_roadline_select;
	private TextView question_roadline_name1;
	private RelativeLayout question_type_select;
	private TextView question_type_name;
	private EditText exception_des;

	private View question_type_listview;
	private PopupWindow typePopupWindow;
	private ListView question_type_container;
	private List<QuestionTypeEntity> listQuestionType;
	private QuestionTypeEntity questionTypeEntity;

	private AudioRecorderButton record_btn;
	private FrameLayout voice_play;
	private View voice_loud;
	/** 录音文件位置 */
	private String voice_filepath;
	private String voiceName = "";
	private String voiceData = "";

	private static final int IMAGE = 1;
	private static final int CAMERA = 2;
	private static final int BIIMAGE = 3;
	private ImageView question_img;
	private String photo_path;
	private String pictureName = "";
	private String pictureData = "";

	private String exlocation;
	private String exceptionlong;
	private String exceptionlat;

	private Button question_report;
	private Button question_report_repair;
	/** 上报标签 */
	private final static int reportTag = 2;
	/** 上报并维修标签 */
	private final static int reportAndRepairTag = 3;
	
	
	private boolean isQuestion_reporting = false;
	private boolean isQuestion_report_repairing = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.question_fragment, null);
		context = getActivity();
		shares = context.getSharedPreferences("config", Context.MODE_PRIVATE);

		//// 绑定service
		Intent intent = new Intent(context, QuestionService.class);
		context.bindService(intent, connection, Context.BIND_AUTO_CREATE);

		question_roadline_select = (RelativeLayout) view.findViewById(R.id.question_roadline_select);
		question_roadline_select.setOnClickListener(mOnClickListener);
		question_roadline_name1 = (TextView)view.findViewById(R.id.question_roadline_name1);
		question_type_select = (RelativeLayout) view.findViewById(R.id.question_type_select);
		question_type_select.setOnClickListener(mOnClickListener);
		question_type_name = (TextView)view.findViewById(R.id.question_type_name);
		record_btn = (AudioRecorderButton) view.findViewById(R.id.record_btn);
		record_btn.setAudioFinishRecorderListener(mAudioFinishListener);
		voice_play = (FrameLayout) view.findViewById(R.id.voice_btn);
		voice_loud = (View) view.findViewById(R.id.id_recoder_anim);
		question_img = (ImageView) view.findViewById(R.id.question_img);
		question_img.setOnClickListener(mOnClickListener);// 设置初始化时图片的点击事件
		question_report = (Button) view.findViewById(R.id.question_report);
		question_report.setOnClickListener(mOnClickListener);
		question_report_repair = (Button) view.findViewById(R.id.question_report_repair);
		question_report_repair.setOnClickListener(mOnClickListener);
		exception_des = (EditText) view.findViewById(R.id.exception_des);

		return view;

	}

	/** 建立与service的连接桥梁 */
	private ServiceConnection connection = new ServiceConnection() {
		/** 解除绑定时调用 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		/** 服务绑定时调用 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = (MBinder) service;// 将MBinder绑定服务迁引出来

			//// 进行后端service请求获取异常类型
			mBinder.setExTypeCallBack(exTypeCallBack);
			mBinder.getExType();

			//// 获取路线
			mBinder.setLnOrganizeCallBack(lnOrganizeCallBack);
			mBinder.getLnOrganize();

			/// 获取定位数据
			mBinder.setLocationCallBack(locationCallBack);
			mBinder.getLocation();
		}
	};

	private LocationCallBack locationCallBack = new LocationCallBack() {

		@Override
		public void getLocationFail(String msgEx) {
			Message msg = new Message();
			msg.what = 0x05;
			Bundle data = new Bundle();
			data.putString("msgEx", msgEx);
			msg.setData(data);
			handler.sendMessage(msg);
		}

		@Override
		public void getLocationData(String location, String Long, String Lat) {
			exlocation = location;
			exceptionlong = Long;
			exceptionlat = Lat;

		}
	};

	private ExTypeCallBack exTypeCallBack = new ExTypeCallBack() {

		@Override
		public void getExTypeDataFail(String msgEx) {
			Message msg = new Message();
			msg.what = 0x05;
			Bundle data = new Bundle();
			data.putString("msgEx", msgEx);
			msg.setData(data);
			handler.sendMessage(msg);
		}

		@Override
		public void getExTypeData(List<QuestionTypeEntity> listQuestionTypeEntity) {
			listQuestionType = listQuestionTypeEntity;
		}
	};

	private LnOrganizeCallBack lnOrganizeCallBack = new LnOrganizeCallBack() {

		@Override
		public void getLnOrganizeDataFail(String msgEx) {
			Message msg = new Message();
			msg.what = 0x05;
			Bundle data = new Bundle();
			data.putString("msgEx", msgEx);
			msg.setData(data);
			handler.sendMessage(msg);

		}

		@Override
		public void getLnOrganizeData(LinkedList<Node> LineList) {
			mLinkedList = LineList;

		}
	};

	private Handler handler = new Handler() {
		@Override
		public void dispatchMessage(Message msg) {
			Bundle data = msg.getData();
			switch (msg.what) {
			case 0x01:// 当重新选择图片时
				Bitmap new_bitmap = data.getParcelable("new_bitmap");
				question_img.setImageBitmap(new_bitmap);
				question_img.invalidate();
				question_img.setOnClickListener(bigImgListener);
				break;
			case 0x02://上报成功
				String reportBack = data.getString("reportBack");
				Toast.makeText(context, reportBack, Toast.LENGTH_SHORT).show();
				isQuestion_report_repairing = false;
				isQuestion_reporting = false;
				clearData();
				break;
			case 0x05:// 接受服务器返回的异常信息
				String msgEx = data.getString("msgEx");
				Toast.makeText(context, msgEx, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};


	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.question_roadline_select:// 点击路段
				if (mLinkedList != null && mLinkedList.size() > 0) {
					initRoadlineTreeView(question_roadline_select);
				}
				break;

			case R.id.question_type_select:// 点击问题类型
				if (listQuestionType != null && listQuestionType.size() > 0) {
					initQuestionTypeView(question_type_select);
				}
				break;
			case R.id.voice_btn:// 点击录音
				if (voice_filepath == null) {
					return;
				}
				voice_loud.setBackgroundResource(R.drawable.adj);
				voice_loud.setBackgroundResource(R.drawable.play_anim);
				AnimationDrawable animation = (AnimationDrawable) voice_loud.getBackground();
				animation.start();

				MediaManager.playSound(context, voice_filepath, new MediaPlayer.OnCompletionListener() {

					public void onCompletion(MediaPlayer mp) {
						voice_loud.setBackgroundResource(R.drawable.adj);
					}
				});
				break;
			case R.id.question_img:// 点击图片
				showPhotoImgDialog();

				break;
			case R.id.question_report:// 上报
				if(isQuestion_reporting){
					return;
				}
				isQuestion_reporting = true;
				reportQuestion(reportTag);

				break;
			case R.id.question_report_repair:// 上报并维修
				if(isQuestion_report_repairing){
					return;
				}
				isQuestion_report_repairing = true;
				reportQuestion(reportAndRepairTag);
				break;
			default:
				break;
			}

		}

	};

	private void reportQuestion(int tag) {
		////开始进行非空效验
		if (questionTypeEntity == null) {
			Message msg = new Message();
			msg.what = 0x05;
			Bundle data = new Bundle();
			data.putString("msgEx", "请选择类型！");
			msg.setData(data);
			handler.sendMessage(msg);
			return;
		}
		if (lnOrganize == null) {
			Message msg = new Message();
			msg.what = 0x05;
			Bundle data = new Bundle();
			data.putString("msgEx", "请选择路段！");
			msg.setData(data);
			handler.sendMessage(msg);
			return;
		}

		if (exceptionlong==null) {
			Message msg = new Message();
			msg.what = 0x05;
			Bundle data = new Bundle();
			data.putString("msgEx", "无法上报，定位失败！");
			msg.setData(data);
			handler.sendMessage(msg);
			return;
		}
	
		int repairirState=0;
		if (tag==reportTag) {
			repairirState=1;//未维修
		}
		if (tag==reportAndRepairTag) {
			repairirState=2;//正在维修
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		Date date = new Date();
		calendar.setTime(date);
		String reportdate = sdf.format(date);
		
		QuestionEntity questionEntity = new QuestionEntity();
		questionEntity.exceptionpointid = UUID.randomUUID().toString();
		questionEntity.repairirState = repairirState;
		questionEntity.peopleno = shares.getString("peopleno", "");
		questionEntity.extypeid = questionTypeEntity.extypeid;
		questionEntity.exTypeName = questionTypeEntity.extypename;
		questionEntity.lnOrganizeId = (String) lnOrganize.get_id();
		questionEntity.lnOrganizeType = (String) lnOrganize.get_rank();
		questionEntity.exlocation = exlocation;
		questionEntity.exceptionlong = exceptionlong;
		questionEntity.exceptionlat = exceptionlat;
		questionEntity.reportdate = reportdate;
		questionEntity.exceptiondescription = exception_des.getText().toString().trim();
		questionEntity.pictureName = pictureName;
		questionEntity.pictureData = pictureData;
		questionEntity.voiceName = voiceName;
		questionEntity.voiceData = voiceData;
		mBinder.setQuestionReportCallBack(questionReportCallBack);
		mBinder.reportQuestion(questionEntity, tag);
	}

	private QuestionReportCallBack questionReportCallBack = new QuestionReportCallBack() {

		@Override
		public void reportQuestionData(String massge) {
			Message msg = new Message();
			msg.what = 0x02;
			Bundle data = new Bundle();
			data.putString("reportBack", massge);
			msg.setData(data);
			handler.sendMessage(msg);
		}
	};

	/** 展现拍照图片控件dialog */
	private void showPhotoImgDialog() {

		final PhotoImgCustomDialog dialog = new PhotoImgCustomDialog(context, R.style.CustomDialog);
		dialog.setCameralistener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				String temp_photo_name = "question_report_img" + ".jpg";
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

	private String getDiskCacheDir() {
		String cachepath;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable()) {
			cachepath = context.getExternalCacheDir().getPath() + File.separator + "photo";
		} else {
			cachepath = context.getCacheDir().getPath() + File.separator + "photo";
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
			Cursor c = context.getContentResolver().query(selectedImage, filePathColumns, null, null, null);
			c.moveToFirst();
			int columnIndex = c.getColumnIndex(filePathColumns[0]);
			photo_path = c.getString(columnIndex);
			if (photo_path != null && !"".equals(photo_path)) {
				// Log.i(Tag, "imagePath:"+photo_path);
				// int lastindex = photo_path.lastIndexOf(".");
				// SimpleDateFormat sdf = new
				// SimpleDateFormat("yyyyMMddHHmmss");
				// Calendar calendar = Calendar.getInstance();
				// Date date = new Date();
				// calendar.setTime(date);
				// String datetimestr = sdf.format(date);
				// photo_name = datetimestr +photo_path.substring(lastindex);
				pictureName = "question_report_img.jpg";
			} else {
				c.close();
				return;
			}

			fillPhotoOperate(photo_path, 4, 1024 * 1024);// 最大上传125k

			c.close();
		}
		if (requestCode == CAMERA && resultCode == Activity.RESULT_OK) {// 拍照后得到的照片

			// int lastindex = photo_path.lastIndexOf(".");
			// SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			// Calendar calendar = Calendar.getInstance();
			// Date date = new Date();
			// calendar.setTime(date);
			// String datetimestr = sdf.format(date);
			// photo_name = datetimestr +photo_path.substring(lastindex);
			pictureName = "question_report_img.jpg";
			fillPhotoOperate(photo_path, 8, 1024 * 1024);// 最大上传125k

		}
		if (requestCode == BIIMAGE && resultCode == Activity.RESULT_OK) {// 由大图activity返回，用来选择图片
			photo_path = "";
			pictureData = "";
			pictureName = "";
			question_img.setImageResource(R.drawable.question_img_selector);
			question_img.setOnClickListener(mOnClickListener);

			showPhotoImgDialog();

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
					pictureData = Base64.encodeToString(imgBytes, Base64.DEFAULT);
					//Log.i(Tag, "photo_str size:" + pictureData.length());
					 Log.i(Tag, "pictureData：");
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

							Message msg = new Message();
							msg.what = 0x01;
							Bundle data = new Bundle();
							data.putParcelable("new_bitmap", new_bitmap);
							msg.setData(data);
							handler.sendMessage(msg);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						baos.flush();
						baos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			};
		}.start();

	}

	/** 大图点击事件 */
	public View.OnClickListener bigImgListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (photo_path == null || "".equals(photo_path)) {
				return;
			}
			Intent intent = new Intent();
			intent.setClass(context, QuestionShowBigImgActivity.class);
			intent.putExtra("photo_path", photo_path);
			startActivityForResult(intent, BIIMAGE);//携带请求码与新打开的activity进行返回值操作
		}
	};

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

	/** 初始化问题类型视图 */
	private void initQuestionTypeView(View anchor) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		question_type_listview = inflater.inflate(R.layout.question_type_listview, null, false);

		typePopupWindow = new PopupWindow(question_type_listview, 860, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
		// 设置SelectPicPopupWindow弹出窗体可点击
		typePopupWindow.setFocusable(true);
		// 设置SelectPicPopupWindow弹出窗体动画效果
		typePopupWindow.setAnimationStyle(R.style.morestyle);
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0000000000);
		// 设置SelectPicPopupWindow弹出窗体的背景
		typePopupWindow.setBackgroundDrawable(dw);
		// 设置允许在外点击消失
		typePopupWindow.setOutsideTouchable(true);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		typePopupWindow.setBackgroundDrawable(new BitmapDrawable());
		typePopupWindow.showAsDropDown(anchor);

		question_type_container = (ListView) question_type_listview.findViewById(R.id.question_type_container);
		question_type_container.setOnItemClickListener(onItemClickListener);
		TypeListAdapter typeListAdapter = new TypeListAdapter(context, listQuestionType);
		question_type_container.setAdapter(typeListAdapter);
		typeListAdapter.notifyDataSetChanged();
	}

	/** 问题类型 */
	private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			questionTypeEntity = listQuestionType.get(position);
			//Log.i(Tag, "" + questionTypeEntity);
			typePopupWindow.dismiss();
			question_type_name.setText(questionTypeEntity.extypename);
		}
	};

	/** 初始化路线（树view） */
	private void initRoadlineTreeView(View anchor) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		treeContainer = inflater.inflate(R.layout.android_tree_container, null, false);

		treePopupWindow = new PopupWindow(treeContainer, 860, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
		// 设置SelectPicPopupWindow弹出窗体可点击
		treePopupWindow.setFocusable(true);
		// 设置SelectPicPopupWindow弹出窗体动画效果
		treePopupWindow.setAnimationStyle(R.style.morestyle);
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0000000000);
		// 设置SelectPicPopupWindow弹出窗体的背景
		treePopupWindow.setBackgroundDrawable(dw);
		// 设置允许在外点击消失
		treePopupWindow.setOutsideTouchable(true);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		treePopupWindow.setBackgroundDrawable(new BitmapDrawable());
		treePopupWindow.showAsDropDown(anchor);

		ListView tree_container = (ListView) treeContainer.findViewById(R.id.tree_container);
	
		NodeTreeAdapter mAdapter = new NodeTreeAdapter(context, tree_container, mLinkedList, treeItemClickCallback);
		tree_container.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
	}

	/** 选择android树控件子项回调（路线选择） */
	private TreeItemClickCallback treeItemClickCallback = new TreeItemClickCallback() {

		@Override
		public void getTreeItemClickData(int position) {
			lnOrganize = mLinkedList.get(position);
			//Log.i(Tag, "点击回调：" + position);
			treePopupWindow.dismiss();
			question_roadline_name1.setText(lnOrganize.get_label());
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

	@Override
	public void onDestroy() {
		super.onDestroy();

		// 如果录音播放管理对象存在，就进行关闭
		MediaManager.pause();
		MediaManager.release();

		context.unbindService(connection);//解除service绑定

		Log.e(Tag, "服务销毁");
	}
	//清空表单数据
	private void clearData(){

		question_type_name.setText("");
		question_roadline_name1.setText("");
		exception_des.setText("");
		question_img.setImageDrawable(null);
	}


}
