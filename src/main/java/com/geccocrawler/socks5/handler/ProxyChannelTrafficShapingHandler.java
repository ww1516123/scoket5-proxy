package com.geccocrawler.socks5.handler;

import com.geccocrawler.socks5.log.ProxyFlowLog;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
/**
 * 流量统计
 * @author MapleRan
 *
 */
public class ProxyChannelTrafficShapingHandler extends ChannelTrafficShapingHandler {
	
	public static final String PROXY_TRAFFIC = "ProxyChannelTrafficShapingHandler";
	
	private long beginTime;
	
	private long endTime;
	
	private String username = "anonymous";
	
	private ProxyFlowLog proxyFlowLog;
	
	public static ProxyChannelTrafficShapingHandler get(ChannelHandlerContext ctx) {
		return (ProxyChannelTrafficShapingHandler)ctx.pipeline().get(PROXY_TRAFFIC);
	}
	
	public ProxyChannelTrafficShapingHandler(long checkInterval, ProxyFlowLog proxyFlowLog) {
		super(checkInterval);
		this.proxyFlowLog = proxyFlowLog;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		beginTime = System.currentTimeMillis();
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		endTime = System.currentTimeMillis();
		proxyFlowLog.log(ctx);
		super.channelInactive(ctx);
	}

	public long getBeginTime() {
		return beginTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public static void username(ChannelHandlerContext ctx, String username) {
		get(ctx).username = username;
	}
	
	public String getUsername() {
		return username;
	}

}
