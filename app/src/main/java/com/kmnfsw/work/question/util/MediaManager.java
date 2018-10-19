package com.kmnfsw.work.question.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.xutils.common.util.LogUtil;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Handler;

/**录音播放管理*/
public class MediaManager {
	private final static String Tag = ".question.util.MediaManager";

	private static MediaPlayer mMediaPlayer; 
	private static boolean isPause;

	/**
	 * 播放音乐
	 * @param filePath
	 * @param onCompletionListener
	 */
	public static void playSound(Context context,String filePath,OnCompletionListener onCompletionListener) {
		if (mMediaPlayer == null) {
			mMediaPlayer = getMediaPlayer(context);
			
			//设置一个error监听器
			mMediaPlayer.setOnErrorListener(new OnErrorListener() {

				public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
					mMediaPlayer.reset();
					return false;
				}
			});
		} else {
			mMediaPlayer.reset();
		}

		try {
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setOnCompletionListener(onCompletionListener);
			mMediaPlayer.setDataSource(filePath);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (Exception e) {

		}
	}

	/**
	 * 暂停播放
	 */
	public static void pause() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) { //正在播放的时候
			mMediaPlayer.pause();
			isPause = true;
		}
	}

	/**
	 * 启动播放器
	 */
	public static void resume() {
		if (mMediaPlayer != null && isPause) {  
			mMediaPlayer.start();
			isPause = false;
		}
	}

	/**
	 * 释放资源
	 */
	public static void release() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
	
	/**
	 * 此工具方法解决MediaPlayer系统bug出现（Should have subtitle controller already set）的情况
	 * @param context
	 * @return
	 */
	private static MediaPlayer getMediaPlayer(Context context) {
        MediaPlayer mediaplayer = new MediaPlayer();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
            return mediaplayer;
        }
        try {
            Class<?> cMediaTimeProvider = Class.forName("android.media.MediaTimeProvider");
            Class<?> cSubtitleController = Class.forName("android.media.SubtitleController");
            Class<?> iSubtitleControllerAnchor = Class.forName("android.media.SubtitleController$Anchor");
            Class<?> iSubtitleControllerListener = Class.forName("android.media.SubtitleController$Listener");
            Constructor constructor = cSubtitleController.getConstructor(
                    new Class[]{Context.class, cMediaTimeProvider, iSubtitleControllerListener});
            Object subtitleInstance = constructor.newInstance(context, null, null);
            Field f = cSubtitleController.getDeclaredField("mHandler");
            f.setAccessible(true);
            try {
                f.set(subtitleInstance, new Handler());
            } catch (IllegalAccessException e) {
                return mediaplayer;
            } finally {
                f.setAccessible(false);
            }
            Method setsubtitleanchor = mediaplayer.getClass().getMethod("setSubtitleAnchor",
                    cSubtitleController, iSubtitleControllerAnchor);
            setsubtitleanchor.invoke(mediaplayer, subtitleInstance, null);
        } catch (Exception e) {
            LogUtil.d(Tag,e);
        }
        return mediaplayer;
    }
}
