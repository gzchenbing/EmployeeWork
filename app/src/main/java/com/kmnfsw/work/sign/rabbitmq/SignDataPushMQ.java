package com.kmnfsw.work.sign.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.BasicProperties;

import android.content.Context;
import android.util.Log;

import com.kmnfsw.work.util.ProperTies;
import com.rabbitmq.client.Channel;
/**
 * 进行rabbitmq推送	开发文档http://www.blogjava.net/qbna350816/archive/2016/06/04/430771.html
	代理：http://192.168.0.18:15672
 * @author YanFaBu
 *
 */
public class SignDataPushMQ {

	private final static String Tag = ".sign.rabbitmq.SignDataPushMQ";
	
	public static void basicPublish (String signData,int peopleType,Context context) throws IOException, TimeoutException{
		Properties proper = ProperTies.getProperties(context);
		String rabbitmqIp = proper.getProperty("rabbitmqIp");
		//创建连接工厂
		ConnectionFactory factory = new ConnectionFactory(); 
		factory.setUsername("guest"); 
		factory.setPassword("kmnfsw_yanfabu"); 
		factory.setHost(rabbitmqIp);
		factory.setPort(5672);
		
		//进行一个连接
		Connection conn = factory.newConnection();
		
		//从连接中开通一个通道
		Channel channel = conn.createChannel();
		
		
		
		//设置队例形式为topic类型
		BasicProperties props = new  BasicProperties.Builder().type("topic").contentType("text/plain").build();
		String exchangeTopic = "";
		
		if (peopleType == 1) {
			exchangeTopic = "sign_1";
			Log.i(Tag, "主题sign_1");
		}else if(peopleType == 2){
			exchangeTopic = "sign_2";
			Log.i(Tag, "主题sign_2");
		}
		
		/**
		 * 发布一个交换器
		 * 参数1：交换器
		 * 参数2：交互器类型
		 * 参数3: 该交换器是否持久
		 */
//		channel.exchangeDeclare(exchangeTopic, "topic", true);
		
		/**进行消息推送
		 * 参数1：交换机exchange
		 * 参数2：路由通道标识routingKey
		 * 参数3：rabbitmq属性props
		 * 参数4：要发送的数据
		 * */
		channel.basicPublish(exchangeTopic, "",props, signData.getBytes("UTF-8"));
		
		//关闭连接
		channel.close();
		conn.close();
	}
}
