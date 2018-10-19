package com.kmnfsw.work.util.notify;

import com.kmnfsw.work.R;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * 振铃实现类
 * 
 * @author YanFaBu
 *
 */
public class MediaPlayerUtil {

	private static MediaPlayer mMediaPlayer;

	/**
	 *  开始播放
	 * @param activity
	 */
	public static void playRing(final Activity activity) {
		try {
			//Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);// 用于获取手机默认铃声的Uri
			Uri soundUri = Uri.parse("android.resource://" + activity.getPackageName() + "/" + R.raw.my_notification);
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDataSource(activity, soundUri);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);// 告诉mediaPlayer播放的是铃声流
			mMediaPlayer.setLooping(true);//设置是否循环播放
			mMediaPlayer.prepare();//让MediaPlayer真正去装载音频文件
			mMediaPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *  停止播放
	 */
	public static void stopRing() {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
		}
	}
}
