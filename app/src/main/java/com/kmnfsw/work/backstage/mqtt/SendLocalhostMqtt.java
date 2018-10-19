package com.kmnfsw.work.backstage.mqtt;


import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.internal.MemoryPersistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmnfsw.work.backstage.entity.SendLocalhostEntity;
import com.kmnfsw.work.backstage.utils.ObjectAndByte;
import com.kmnfsw.work.util.ProperTies;

import android.content.Context;
import android.util.Log;

/**
 * 位置即时通信上报mqtt
 * mqtt api指南 http://www.eclipse.org/paho/files/javadoc/index.html
 * @author YanFaBu
 *
 */
public class SendLocalhostMqtt {

	private final static String Tag = ".backstage.mqtt.SendLocalhostMqtt";

	//连接Apollo中间件
	//private String host = "tcp://182.247.238.189:61613";// http://192.168.0.55:61680
	//登录Apollo名													// 1883
	private String userName = "admin";
	//登录Apollo密码
	private String passWord = "kmnfsw_yanfabu";
	/**mqtt客服端类*/
	private MqttClient client;
	private MqttConnectOptions options;
	private ObjectMapper objectMapper = new ObjectMapper();

	public void sendMagge(SendLocalhostEntity entity,Context context) {

		try {
			Properties proper = ProperTies.getProperties(context);
			String host = proper.getProperty("mqttIp");
			
			client = new MqttClient(host, entity.getMac(), new MemoryPersistence());
			// MQTT的连接设置
			options = new MqttConnectOptions();
			// 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
			options.setCleanSession(true);
			// 设置连接的用户名
			options.setUserName(userName);
			// 设置连接的密码
			options.setPassword(passWord.toCharArray());
			// 设置超时时间 单位为秒
			options.setConnectionTimeout(10);
			// 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
			options.setKeepAliveInterval(20);
			client.setCallback(mqttCallback);//设置回调监听

			MqttTopic topic = null;
			client.connect(options);

//			if (entity.getPeopleType() == 1) {// 巡视主题
//				topic = client.getTopic("gw-tour-location");// 发布主题标识id
//			} else if (entity.getPeopleType() == 2) {// 维护主题
//				topic = client.getTopic("gw-checkAll-location");// 发布主题标识id
//			}
			topic = client.getTopic("repair_Apply_check");

			MqttMessage message = new MqttMessage();// 创建消息
			/**
			 * Qos level=0;会发生消息丢失或重复。 Qos level=1;确保消息到达，但消息重复可能会发生。 Qos
			 * level=2;确保消息到达一次
			 */
			message.setQos(0);
//			message.setRetained(true);
			
			String entityStr = "";
			try {
				entityStr = objectMapper.writeValueAsString(entity);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			message.setPayload(entityStr.getBytes());
			//text.getBytes()

			MqttDeliveryToken token = topic.publish(message);
			token.waitForCompletion();
		} catch (MqttException e) {
			Log.e(Tag, "mqtt异常："+e);
		}

	}
	/**断开mqtt连接*/
	public void disconnectMqtt(){
		if (client !=null) {
			try {
				client.disconnect();
			} catch (MqttException e) {
				Log.e(Tag, "断开mqtt连接异常");
			}
		}
	}


	private MqttCallback mqttCallback = new MqttCallback() {

		@Override
		public void messageArrived(MqttTopic paramMqttTopic, MqttMessage paramMqttMessage) throws Exception {
			// subscribe后得到的消息会执行到这里面

		}

		@Override
		public void deliveryComplete(MqttDeliveryToken paramMqttDeliveryToken) {
			// publish后会执行到这里

		}

		@Override
		public void connectionLost(Throwable paramThrowable) {
			// 连接丢失后，一般在这里面进行重连
			Log.e(Tag, "mqtt连接失败异常："+paramThrowable);

		}
	};

}
