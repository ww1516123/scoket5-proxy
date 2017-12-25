package com.geccocrawler.socks5.auth;

import java.io.IOException;
import java.util.Properties;
/**
 * 配置文件读取
 * @author MapleRan
 *
 */
public class PropertiesPasswordAuth implements PasswordAuth {

	private static Properties properties;
	
	static {
		properties = new Properties();
		try {
			properties.load(PropertiesPasswordAuth.class.getResourceAsStream("/password.properties"));
		} catch (IOException e) {
			//文件不存在
			e.printStackTrace();
		}
	}
	/**
	 * 验证权限
	 */
	public boolean auth(String user, String password) {
		String configPasword = properties.getProperty(user);
		if(configPasword != null) {
			if(password.equals(configPasword)) {
				return true;
			}
		}
		return false;
	}

}
