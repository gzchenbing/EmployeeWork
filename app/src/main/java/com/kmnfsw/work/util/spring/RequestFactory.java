package com.kmnfsw.work.util.spring;

import java.util.List;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RequestFactory {

	/**
	 * 配置RestTemplate超时时间,通过反射强制获取factry设置属性
	 *
	 * @param restTemplate
	 * @param readTimeout
	 *            读取超时，如果小于等于0，则不设置
	 * @param connectTimeout
	 *            连接超时时间，如果小于等于0，则不设置
	 */
	public static void configTimeoutByReflect(RestTemplate restTemplate, int readTimeout, int connectTimeout) {
		ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
		boolean updateReadTimeout = readTimeout >= 0;
		boolean updateConnectTimeout = connectTimeout >= 0;
		if (factory instanceof InterceptingClientHttpRequestFactory) {
			try {
				factory = (ClientHttpRequestFactory) ReflectUtils.getFieledValue(factory, "requestFactory");
				if (factory == null)
					return;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		if (factory instanceof SimpleClientHttpRequestFactory) {
			SimpleClientHttpRequestFactory simple = (SimpleClientHttpRequestFactory) factory;
			if (updateReadTimeout)
				simple.setReadTimeout(readTimeout);
			if (updateConnectTimeout)
				simple.setConnectTimeout(connectTimeout);
		} else if (factory instanceof HttpComponentsClientHttpRequestFactory) {
			HttpComponentsClientHttpRequestFactory client = (HttpComponentsClientHttpRequestFactory) factory;
			if (updateReadTimeout)
				client.setReadTimeout(readTimeout);
			if (updateConnectTimeout)
				client.setConnectTimeout(connectTimeout);
		}
	}
//
//	public static void configTimeout(RestTemplate template, int readTimeout, int connectTimeout) {
//		boolean updateReadTimeout = readTimeout >= 0;
//		boolean updateConnectTimeout = connectTimeout >= 0;
//		if (template == null || (!updateReadTimeout && !updateReadTimeout))
//			return;
//		// 这里是为了解决spring requestfactory不能获取到真实factory的问题，先设置过滤器为空，设置后超时后
//		// 再重新设置回去
//		List<ClientHttpRequestInterceptor> interceptors = template.getInterceptors();
//		template.setInterceptors(null);
//		try {
//			ClientHttpRequestFactory factory = template.getRequestFactory();
//			// InterceptingClientHttpRequestFactory
//			// interceptingClientHttpRequestFactory;
//			if (factory instanceof SimpleClientHttpRequestFactory) {
//				SimpleClientHttpRequestFactory simple = (SimpleClientHttpRequestFactory) factory;
//				if (updateReadTimeout)
//					simple.setReadTimeout(readTimeout);
//				if (updateConnectTimeout)
//					simple.setConnectTimeout(connectTimeout);
//			} else if (factory instanceof HttpComponentsClientHttpRequestFactory) {
//				HttpComponentsClientHttpRequestFactory client = (HttpComponentsClientHttpRequestFactory) factory;
//				if (updateReadTimeout)
//					client.setReadTimeout(readTimeout);
//				if (updateConnectTimeout)
//					client.setConnectTimeout(connectTimeout);
//			}
//		} finally {
//			// 重新设置回去
//			template.setInterceptors(interceptors);
//		}
//	}
}
