package com.geccocrawler.socks5.handler.ss5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

public class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket>{
	private static final Logger logger = LoggerFactory.getLogger(UdpClientHandler.class);
	private ChannelHandlerContext localCtx;
	public UdpClientHandler(ChannelHandlerContext localCtx) {
		this.localCtx=localCtx;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
		// TODO Auto-generated method stub
		 String response = msg.content().toString(CharsetUtil.UTF_8);
		 localCtx.writeAndFlush(msg);
	}
	@Override
	public void channelInactive(ChannelHandlerContext ctx2) throws Exception {
		logger.info("目标服务器断开连接");
		localCtx.channel().close();
	}
	
		
}
