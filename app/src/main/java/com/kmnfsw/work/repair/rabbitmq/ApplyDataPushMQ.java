package com.kmnfsw.work.repair.rabbitmq;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmnfsw.work.question.entity.QuestionEntity;
import com.kmnfsw.work.util.ProperTies;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.BasicProperties;

import android.content.Context;
import android.util.Log;

/**
 * 维修申请验收mq
 * @author YanFaBu
 *
 */
public class ApplyDataPushMQ {

	private final static String Tag = "repair.rabbitmq.ApplyDataPushMQ";
	/**交换机主题*/
	private final static String TOPIC = "repair_Apply_check";
	/**rabbitmq推送数据*/
	public static void basicPublish (String checkrepairno,Context context) throws IOException, TimeoutException{
		
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
		String exchangeTopic = TOPIC;
		
		
		
		/**
		 * 发布一个交换器
		 * 参数1：交换器
		 * 参数2：交互器类型
		 * 参数3: 该交换器是否持久
		 */
		//channel.exchangeDeclare(exchangeTopic, "topic", true); 
		
		/**进行消息推送
		 * 参数1：交换机exchange
		 * 参数2：路由通道标识routingKey
		 * 参数3：rabbitmq属性props
		 * 参数4：要发送的数据
		 * */
		channel.basicPublish(exchangeTopic, "",props, checkrepairno.getBytes("UTF-8"));
		
		
		//关闭连接
		channel.close();
		conn.close();
	}
	
}
