package com.kmnfsw.work.backstage.rabbitmq;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.kmnfsw.work.util.ProperTies;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import android.content.Context;
import android.util.Log;

/**
 * rabbitmq监听接受器
 * 
 * 1、进行RPC模式监听
 * 2、进行员工接受通知
 * @author YanFaBu
 *
 */
public class RabbitmqListen {

	private final static String Tag = ".backstage.rabbitmq.RabbitmqListen";

	private static Connection connection;

	private static RabbitmqListen mInstance;
	private Context context;

	public RabbitmqListen(Context ct){
		context = ct;
	}
	
	public static RabbitmqListen getInstance(Context ct) {
		if (null == mInstance) {
			synchronized (RabbitmqListen.class) {
				if (mInstance == null) {
					mInstance = new RabbitmqListen(ct);
				}
			}
		}
		return mInstance;
	}

	/**
	 * 进行Rabbitmq连接
	 * 
	 * @throws TimeoutException
	 * @throws IOException
	 */
	public static RabbitmqListen connectionRabbitmq(Context context) throws IOException, TimeoutException {
		Properties proper = ProperTies.getProperties(context);
		String rabbitmqIp = proper.getProperty("rabbitmqIp");
		// 创建连接工厂
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername("guest");
		factory.setPassword("kmnfsw_yanfabu");
		factory.setHost(rabbitmqIp);
		factory.setPort(5672);
		factory.setAutomaticRecoveryEnabled(true);// 是否开启自动连接恢复
		factory.setNetworkRecoveryInterval(50000);// 默认5秒钟进行一次重连接
		factory.setRequestedHeartbeat(5);// 设置心跳间隔，5秒一次

		// 进行一个连接
		connection = factory.newConnection();

		return getInstance(context);
	}
	/**
	 * 关闭Rabbitmq监听连接
	 * @throws IOException
	 */
	public void stopRabbitmqListen() {
		try {
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		connection = null;
		mInstance = null;
		Log.i(Tag, "关闭Rabbitmq监听");
	}

	private Channel channelRPC;
	/** 是否要循环监听RPC */
	private boolean isRuningRPC = true;
	/**
	 * 启动RPC服务
	 * @param peoplenoQueue  员工编号
	 * @throws IOException
	 */
	public void startRPCServer(String peoplenoQueue) throws IOException {
		Log.i(Tag, "启动startRPCServer！");
		String rpcQueueName = "RPC_"+peoplenoQueue;
		
		channelRPC = connection.createChannel();

		channelRPC.queueDeclare(rpcQueueName, false, false, false, null);

		channelRPC.basicQos(1);

		Consumer consumer = new DefaultConsumer(channelRPC) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
						.correlationId(properties.getCorrelationId()).build();

				String response = new String(body, "UTF-8");
				Log.i(Tag, "RPC模式收到消息：" + response);

				channelRPC.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));//发送应答消息
				channelRPC.basicAck(envelope.getDeliveryTag(), false);//应答分发消息
				// RabbitMq consumer worker thread notifies the RPC
				// server owner thread
				synchronized (this) {
					this.notify();//唤醒释放资源
				}

			}
		};

		channelRPC.basicConsume(rpcQueueName, false, consumer);//接受消息
		// Wait and be prepared to consume the message from RPC client.
		while (isRuningRPC) {
			synchronized (consumer) {
				try {
					consumer.wait();//线程阻塞，并且加锁
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 停止RPC服务
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public void stopRPCServer()  {
		isRuningRPC = false;
		if (channelRPC != null)
			try {
				channelRPC.close();
			} catch (IOException | TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private Channel channel;
	private boolean isRuningWorkerReceive=true;
	/**
	 * 启动员工接受rabbitmq
	 * @param peopleno
	 * @throws IOException 
	 */
	public void startWorkerReceive(String peopleno,final WorkerReceiveMsg callback) throws IOException{
		Log.i(Tag, "启动startWorkerReceive！");
		channel = connection.createChannel();

		channel.queueDeclare(peopleno, false, false, false, null);

		channel.basicQos(1);

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				String massge = new String(body, "UTF-8");
				Log.i(Tag, "员工模式收到消息：" + massge);
				callback.getReceiveMsg(massge);

				// RabbitMq consumer worker thread notifies the RPC
				// server owner thread
				synchronized (this) {
					this.notify();//线程阻塞，并且加锁
				}

			}
		};

		channel.basicConsume(peopleno, true, consumer);//接受消息
		// Wait and be prepared to consume the message from RPC client.
		while (isRuningWorkerReceive) {
			synchronized (consumer) {
				try {
					consumer.wait();//线程阻塞，并且加锁

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * 监听员工rabbitmq得到的数据
	 * @author YanFaBu
	 *
	 */
	public abstract interface WorkerReceiveMsg{
		public void getReceiveMsg(String msg);
	}
	
	/**
	 * 停止员工接受rabbitmq
	 * @throws TimeoutException 
	 * @throws IOException 
	 */
	public void stopWorkerReceive() {
		isRuningWorkerReceive = false;
		if (null !=channel) {
			try {
				channel.close();
			} catch (IOException | TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
