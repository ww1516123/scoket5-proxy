package com.geccocrawler.socks5.log;

import io.netty.channel.ChannelHandlerContext;

/**
 * 代理跟踪日志
 * @author MapleRan
 *
 */
public interface ProxyFlowLog {

	public void log(ChannelHandlerContext ctx);
	
}
