package com.geccocrawler.socks5.test;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpProxyClient {
	
	public static void main(String[] args) throws Exception {
		final String user = "test";
		final String password = "test";
		
		Proxy proxyTest = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 11080));
		
		java.net.Authenticator.setDefault(new java.net.Authenticator()
		{
			private PasswordAuthentication authentication = new PasswordAuthentication(user, password.toCharArray());

			@Override
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return authentication;
			}
		});

		
		OkHttpClient client = new OkHttpClient.Builder().proxy(proxyTest).build();
		for (int i = 0; i < 10; i++) {
			Request request = new Request.Builder().url("http://fanyi.baidu.com/#zh/en/"+i).build();
			Response response = client.newCall(request).execute();
			System.out.println(response.code());
			System.out.println(response.body().string());
			
			Thread.sleep(1000L);
		}
		
		client.dispatcher().executorService().shutdown();
		client.connectionPool().evictAll();
	}

}
