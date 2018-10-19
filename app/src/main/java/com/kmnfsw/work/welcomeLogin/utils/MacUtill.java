package com.kmnfsw.work.welcomeLogin.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class MacUtill {
	
	
	public static  String getMac(Context context) {
		String macSerial = null;
		String str = "";

		try {
			Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
			InputStreamReader ir = new InputStreamReader(pp.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);

			for (; null != str;) {
				str = input.readLine();
				if (str != null) {
					macSerial = str.trim();// 去空格
					break;
				}
			}
		} catch (IOException ex) {
			// 赋予默认值
			ex.printStackTrace();
		}

		if (macSerial == null || "".equals(macSerial)) {
			try {
				return loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

		if (macSerial == null || "".equals(macSerial)) {
			WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
			if (null != info) {
				macSerial = info.getMacAddress();
			}
		}

		if (macSerial == null || "".equals(macSerial)) {

			macSerial = getLocalMacAddressFromBusybox();
		}

		return macSerial;
	}

	public static String getLocalMacAddressFromBusybox() {
		String result = "";
		String Mac = "";
		result = callCmd("busybox ifconfig", "HWaddr");

		// 如果返回的result == null，则说明网络不可取
		if (result == null) {
			return "网络出错，请检查网络";
		}

		// 对该行数据进行解析
		// 例如：eth0 Link encap:Ethernet HWaddr 00:16:E8:3E:DF:67
		if (result.length() > 0 && result.contains("HWaddr") == true) {
			Mac = result.substring(result.indexOf("HWaddr") + 6, result.length() - 1);
			Log.i("test", "Mac:" + Mac + " Mac.length: " + Mac.length());

			result = Mac;
			Log.i("test", result + " result.length: " + result.length());
		}
		return result;
	}

	private static String callCmd(String cmd, String filter) {
		String result = "";
		String line = "";
		try {
			Process proc = Runtime.getRuntime().exec(cmd);
			InputStreamReader is = new InputStreamReader(proc.getInputStream());
			BufferedReader br = new BufferedReader(is);

			// 执行命令cmd，只取结果中含有filter的这一行
			while ((line = br.readLine()) != null && line.contains(filter) == false) {
				// result += line;
				Log.i("test", "line: " + line);
			}

			result = line;
			Log.i("test", "result: " + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String loadFileAsString(String fileName) throws Exception {
		FileReader reader = new FileReader(fileName);
		String text = loadReaderAsString(reader);
		reader.close();
		return text;
	}

	public static String loadReaderAsString(Reader reader) throws Exception {
		StringBuilder builder = new StringBuilder();
		char[] buffer = new char[4096];
		int readLength = reader.read(buffer);
		while (readLength >= 0) {
			builder.append(buffer, 0, readLength);
			readLength = reader.read(buffer);
		}
		return builder.toString();
	}
}
