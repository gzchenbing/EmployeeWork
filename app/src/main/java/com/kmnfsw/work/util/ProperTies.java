package com.kmnfsw.work.util;

import java.io.InputStream;
import java.util.Properties;

import android.content.Context;

/**
 * 读取配置文件工具类
 * @author YanFaBu
 *
 */
public class ProperTies {
	public static Properties getProperties(Context c){
        Properties urlProps;
        Properties props = new Properties();
        try {
            //方法一：通过activity中的context攻取setting.properties的FileInputStream
            //注意这地方的参数appConfig在eclipse中应该是appConfig.properties才对,但在studio中不用写后缀
            InputStream in = c.getAssets().open("appConfig.properties");
            //方法二：通过class获取setting.properties的FileInputStream
            //InputStream in = PropertiesUtill.class.getResourceAsStream("/assets/  setting.properties "));
            props.load(in);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        urlProps = props;
        return urlProps;
    }
}
