package com.kmnfsw.work.question.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.util.Log;
/**
 * 录音IO操作
 * @author YanFaBu
 *
 */
public class AudioManager {
	private final static String Tag = ".question.util.AudioManager";
	
	/**录音实例*/
	private MediaRecorder mMediaRecorder;
	private String mDir;
	/**录音当前文件路径*/
	private String mCurrentFilePath;

	private static AudioManager mInstance;
	
	private boolean isPrepare;
	
//	/**频率*/
//	private int frequency = 11025;
//	/**声道配置*/
//	private int channelConfiguration = AudioFormat.CHANNEL_IN_DEFAULT; 
//	/**音频编码*/
//	private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	
	
	
	private AudioManager(String dir) {
		mDir = dir;
	}

	public static AudioManager getInstance(String dir) {
		if (mInstance == null) {
			synchronized (AudioManager.class) {
				if (mInstance == null) {
					mInstance = new AudioManager(dir);
				}
			}
		}
		return mInstance;
	}

	/**
	 * 使用接口 用于回调
	 */
	public interface AudioStateListener {
		void wellPrepared();
	}

	public AudioStateListener mAudioStateListener;

	/**
	 * 回调方法
	 */
	public void setOnAudioStateListener(AudioStateListener listener) {
		mAudioStateListener = listener;
	}

	/**
	 * 准备录音
	 */
	public void prepareAudio() {
		try {
			isPrepare = false;
			File dir = new File(mDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String fileName = generateFileName();
			
			File file = new File(dir, "question_report_raw.mp4");

			mCurrentFilePath =file.getAbsolutePath();
			
			mMediaRecorder = new MediaRecorder();
			// 设置输出文件
			mMediaRecorder.setOutputFile(mCurrentFilePath);
			// 设置MediaRecorder的音频源为麦克风
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			// 设置音频格式
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			// 设置音频编码
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

			// 准备录音
			mMediaRecorder.prepare();
			// 开始
			mMediaRecorder.start();
			// 准备结束
			isPrepare = true;
			if (mAudioStateListener != null) {
				mAudioStateListener.wellPrepared();
			}

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 随机生成文件的名称
	 */
	private String generateFileName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");//时间格式到毫秒
		Calendar calendar = Calendar.getInstance();
		Date date = new Date();
		calendar.setTime(date);
		String strDeviceSignDatetime = sdf.format(date);
		//Log.i(Tag, strDeviceSignDatetime);
		//return UUID.randomUUID().toString() + ".amr";
		return strDeviceSignDatetime+".mp4";
	}

	public int getVoiceLevel(int maxlevel) {
		if (isPrepare) {
			try {
				// mMediaRecorder.getMaxAmplitude() 1~32767
				return maxlevel * mMediaRecorder.getMaxAmplitude() / 32768 + 1;
			} catch (Exception e) {
			}
		}
		return 1;
	}

	/**
	 * 释放资源
	 */
	public void release() {
		//mMediaRecorder.stop();
		mMediaRecorder.reset();
		mMediaRecorder = null;
	}

	/**
	 * 取消录音
	 */
	public void cancel() {
		release();
		if (mCurrentFilePath != null) {
			File file = new File(mCurrentFilePath);
			file.delete();
			mCurrentFilePath = null;
		}

	}

	
	public String getCurrentFilePath() {

		return mCurrentFilePath;
	}
}
