package com.kmnfsw.work.question.view;

import java.io.File;

import com.kmnfsw.work.R;
import com.kmnfsw.work.question.util.AudioDialogManager;
import com.kmnfsw.work.question.util.AudioManager;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**自定义录音按钮*/
public class AudioRecorderButton extends Button {

	/**默认的状态(未录音）*/
	private static final int STATE_NORMAL = 1;
	/**正在录音*/
	private static final int STATE_RECORDING = 2;
	/**希望取消*/
	private static final int STATE_WANT_TO_CANCEL = 3;

	/**当前的状态*/
	private int mCurrentState = STATE_NORMAL;
	/**已经开始录音*/
	private boolean isRecording = false;

	private static final int DISTANCE_Y_CANCEL = 50;

	private AudioDialogManager mDialogManager;
	private AudioManager mAudioManager;

	/**录音的时长*/
	private float mTime;
	/**是否触发longClick*/
	private boolean mReady;

	private static final int MSG_AUDIO_PREPARED = 0x110;
	private static final int MSG_VOICE_CHANGED = 0x111;
	private static final int MSG_DIALOG_DIMISS = 0x112;

	/**
	 * 获取音量大小的线程
	 */
	private Runnable mGetVoiceLevelRunnable = new Runnable() {

		public void run() {
			while (isRecording) {
				try {
					Thread.sleep(100);
					mTime += 0.1f;
					mHandler.sendEmptyMessage(MSG_VOICE_CHANGED);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_AUDIO_PREPARED://开始录音
				// 显示對話框在开始录音以后
				mDialogManager.showRecordingDialog();
				isRecording = true;
				// 开启一个线程
				new Thread(mGetVoiceLevelRunnable).start();
				break;

			case MSG_VOICE_CHANGED://录音音调级别改变
				mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
				break;

			case MSG_DIALOG_DIMISS://录音销毁
				mDialogManager.dimissDialog();
				break;

			}

			super.handleMessage(msg);
		}
	};

	/**
	 * 以下2个方法是构造方法
	 */
	public AudioRecorderButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDialogManager = new AudioDialogManager(context);

		String dir = getDiskCacheDir(context);
		//String dir = Environment.getExternalStorageDirectory()+"/my_weixin";

		mAudioManager = AudioManager.getInstance(dir);
		mAudioManager.setOnAudioStateListener(new AudioManager.AudioStateListener() {

			public void wellPrepared() {
				mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
			}
		});

		// 由于这个类是button所以在构造方法中添加监听事件
		setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View v) {
				mReady = true;

				mAudioManager.prepareAudio();

				return false;
			}
		});
	}
	/**创建缓冲区图片文件目录*/
	private String getDiskCacheDir(Context context){
    	String cachepath;
    	if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
    			|| !Environment.isExternalStorageRemovable()){
    		cachepath = context.getExternalCacheDir().getPath() + File.separator +"voice";
    	}else{
    		cachepath = context.getCacheDir().getPath()+ File.separator +"voice";
    	}
    	File fileDir = new File(cachepath);
    	if(!fileDir.exists()){
    		fileDir.mkdirs();
    	}
    	return cachepath;
    }
	

	public AudioRecorderButton(Context context) {
		this(context, null);
	}

	/**
	 * 录音完成后的回调
	 */
	public interface AudioFinishRecorderListener {
		void onFinish(float seconds, String filePath);
		void onCancel();
	}

	private AudioFinishRecorderListener audioFinishRecorderListener;

	public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener) {
		audioFinishRecorderListener = listener;
	}

	/**
	 * 屏幕的触摸事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();
		int x = (int) event.getX();// 获得x轴坐标
		int y = (int) event.getY();// 获得y轴坐标

		switch (action) {
		case MotionEvent.ACTION_DOWN://按下
			changeState(STATE_RECORDING);
			break;
		case MotionEvent.ACTION_MOVE://按下移动

			if (isRecording) {
				// 如果想要取消，根据x,y的坐标看是否需要取消
				if (wantToCancle(x, y)) {
					changeState(STATE_WANT_TO_CANCEL);
				} else {
					changeState(STATE_RECORDING);
				}
			}

			break;
		case MotionEvent.ACTION_UP://松开
			if (!mReady) {//未触动长按机制
				reset();
				return super.onTouchEvent(event);
			}
			if (!isRecording || mTime < 0.6f) {//长按机制未达到0.6f
				mDialogManager.tooShort();
				mAudioManager.cancel();
				mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1000);// 延迟显示对话框
				
				audioFinishRecorderListener.onFinish(mTime,mAudioManager.getCurrentFilePath());
				audioFinishRecorderListener.onCancel();
			} else if (mCurrentState == STATE_RECORDING) { // 正在录音
				mDialogManager.dimissDialog();
				mAudioManager.release();

				if (audioFinishRecorderListener != null) {
					audioFinishRecorderListener.onFinish(mTime,mAudioManager.getCurrentFilePath());
				}

			} else if (mCurrentState == STATE_WANT_TO_CANCEL) { // 想要取消
				mDialogManager.dimissDialog();
				mAudioManager.cancel();
				audioFinishRecorderListener.onFinish(mTime,mAudioManager.getCurrentFilePath());
				audioFinishRecorderListener.onCancel();
			}
			reset();
			break;

		}
		return super.onTouchEvent(event);
	}

	/**
	 * 恢复状态及标志位
	 */
	private void reset() {
		isRecording = false;
		mTime = 0;
		mReady = false;
		changeState(STATE_NORMAL);
	}

	private boolean wantToCancle(int x, int y) {
		if (x < 0 || x > getWidth()) { // 超过按钮的宽度
			return true;
		}
		// 超过按钮的高度
		if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
			return true;
		}

		return false;
	}

	/**
	 * 录音动作改变
	 */
	private void changeState(int state) {
		if (mCurrentState != state) {
			mCurrentState = state;
			switch (state) {
			case STATE_NORMAL:
				setBackgroundResource(R.drawable.btn_record_normal);
//				setText(R.string.str_recorder_normal);
				break;

			case STATE_RECORDING:
				setBackgroundResource(R.drawable.btn_record_recording);
//				setText(R.string.str_recorder_recording);
				if (isRecording) {
					mDialogManager.recording();
				}
				break;

			case STATE_WANT_TO_CANCEL:
				setBackgroundResource(R.drawable.btn_record_recording);
//				setText(R.string.str_recorder_want_cancel);

				mDialogManager.wantToCancel();
				break;
			}
		}
	}
}
