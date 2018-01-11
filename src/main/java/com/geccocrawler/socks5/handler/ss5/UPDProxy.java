package com.geccocrawler.socks5.handler.ss5;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.DatagramPacketEncoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.util.CharsetUtil;

public class UPDProxy  {
	private String inetHost;
	private String port;
	private ChannelHandlerContext localCtx;

	
	public UPDProxy(String inetHost, String port, ChannelHandlerContext localCtx) {
		super();
		this.inetHost = inetHost;
		this.port = port;
		this.localCtx = localCtx;
	}
	public void run(int port,Object object)throws Exception{
	        EventLoopGroup group = new NioEventLoopGroup();
	        try {
	            Bootstrap b = new Bootstrap();
	            b.group(group).channel(NioDatagramChannel.class)
	                    .option(ChannelOption.SO_BROADCAST,true)
	                    .handler(new UdpClientHandler(localCtx));
	            ChannelFuture future = b.connect(inetHost, port);
	    		future.addListener(new ChannelFutureListener() {
	    			public void operationComplete(final ChannelFuture future) throws Exception {
	    				if(future.isSuccess()) {
	    					
	    					Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
	    					localCtx.writeAndFlush(commandResponse);
	    				} else {
	    					Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
	    					localCtx.writeAndFlush(commandResponse);
	    				}
	    			}
	    			
	    		});
	            // 向网段类所有机器广播发UDP
//	            ch.writeAndFlush().sync();
//	            if(!ch.closeFuture().await(15000)){
//	                System.out.println("查询超时！！！");
//	            }
	        }
	        finally {
	            group.shutdownGracefully();
	        }
	    }

	

}
