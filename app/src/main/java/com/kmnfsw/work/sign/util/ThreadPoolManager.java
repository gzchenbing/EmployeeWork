package com.kmnfsw.work.sign.util;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.Handler;

public class ThreadPoolManager {

	static ThreadPoolManager sInstance;
	static  {
        // Creates a single static instance of PhotoManager
        sInstance = new ThreadPoolManager();
    }
    
    private static int corePoolSize = 5;
    private static int maximumPoolSize = Runtime.getRuntime().availableProcessors();;
            
    private final BlockingQueue<Runnable> mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
    private static  int keepAliveTime = 1;
    private static  TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    
    private ThreadPoolExecutor mDecodeThreadPool = new ThreadPoolExecutor(
    		corePoolSize,       // Initial pool size
    		maximumPoolSize,       // Max pool size
            keepAliveTime,
            KEEP_ALIVE_TIME_UNIT,
            mDecodeWorkQueue);
	
	public  ThreadPoolManager(int corePoolSize,long keepAliveTime) {
		ThreadPoolManager.corePoolSize = corePoolSize;
		ThreadPoolManager.keepAliveTime = corePoolSize;
	}
	
	public  ThreadPoolManager() {
		super();
	}
	
//	static public PhotoTask startDownload(PhotoView imageView,boolean cacheFlag){
//		         
//        sInstance.mDownloadThreadPool.execute(downloadTask.getHTTPDownloadRunnable());
//        sInstance.mDecodeThreadPool.execute(command);   
//        sInstance.mDecodeThreadPool.shutdownNow();
//    }
	
    // Defines a Handler object that's attached to the UI thread
//	private Handler mHandler = new Handler(Looper.getMainLooper()) {
//        /*
//         * handleMessage() defines the operations to perform when
//         * the Handler receives a new Message to process.
//         */
//        @Override
//        public void handleMessage(Message inputMessage) {
//        	
//        }
//    };
	/**
	 * 添加启动任务
	 * @param task
	 */
	public static void executeThread(Runnable task){
		sInstance.mDecodeThreadPool.execute(task);
	}
	
	/**
	 * 向线程池添加一个线程任务,可以返回任务执行结果
	 * @param task
	 * @return
	 */
	public static Future<?> submitThread(Runnable task){
		return sInstance.mDecodeThreadPool.submit(task);
	}
	
	/**
	 * 关闭线程池,不接收新的任务,关闭后，正在等待执行的任务不受任何影响，会正常执行,无返回值!
	 */
	public static void shutdownThread(){
		sInstance.mDecodeThreadPool.shutdown();
	}
	
	/**
	 * 关闭线程池，也不接收新的Task，并停止正等待执行的Task（也就是说，
   	 *	执行到一半的任务将正常执行下去），最终还会给你返回一个正在等待执行但线程池关闭却没有被执行的Task集合
	 */
	public static List<Runnable> shutdownNowThread(){
		 return sInstance.mDecodeThreadPool.shutdownNow();
	}
    
	
	
	/**
	 *停止所有正在运行的线程 
	 */
    public static void cancelAll() {
        /*
         * Creates an array of Runnables that's the same size as the
         * thread pool work queue
         */
        Runnable[] runnableArray = new Runnable[sInstance.mDecodeWorkQueue.size()];
        // Populates the array with the Runnables in the queue
        sInstance.mDecodeWorkQueue.toArray(runnableArray);
        // Stores the array length in order to iterate over the array
        int len = runnableArray.length;
        /*
         * Iterates over the array of Runnables and interrupts each one's Thread.
         */
        synchronized (sInstance) {
            // Iterates over the array of tasks
            for (int runnableIndex = 0; runnableIndex < len; runnableIndex++) {
                // Gets the current thread
                Thread thread = new Thread( runnableArray[runnableIndex]);
                // if the Thread exists, post an interrupt to it
                if (null != thread) {
                    thread.interrupt();
                }
            }
        }
    }
}
