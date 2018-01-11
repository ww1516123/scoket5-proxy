package com.geccocrawler.socks5.handler.ss5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
/**
 * 负责目标服务器的连接建立，返回建立是否成功，会将消息解码为DefaultSocks5CommandRequest对象
 * @author MapleRan
 *
 */
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest>{
	
	private static final Logger logger = LoggerFactory.getLogger(Socks5CommandRequestHandler.class);
	
	@Override
	protected void channelRead0(final ChannelHandlerContext clientChannelContext, DefaultSocks5CommandRequest msg) throws Exception {
		//clientChannelContext 当前请求上下文
		//DefaultSocks5CommandRequest scoket5 请求包  需要代理的信息在此
		logger.info("目标服务器  : " + msg.type() + "," + msg.dstAddr() + "," + msg.dstPort());
		//这里仅实现的TCP代理方式 还有UDP(相对复杂一些)
		if(msg.type().equals(Socks5CommandType.CONNECT)) {
			logger.info("准备连接目标服务器");
			
			doProxy(clientChannelContext,msg);
		} else if(msg.type().equals(Socks5CommandType.UDP_ASSOCIATE)){
			logger.info("UDP 代理");
			String addr=msg.dstAddr();
			Integer port=msg.dstPort();
			logger.info("目标地址   {}:{}",addr,port);
//			doProxy(clientChannelContext,msg);
			//new UPDProxy(addr,port,clientChannelContext).run(port, msg);
//			clientChannelContext.fireChannelRead(msg);
			clientChannelContext.channel().close();
		}else if(msg.type().equals(Socks5CommandType.BIND)){
			logger.info("暂不支持BIND");
			
			//clientChannelContext.fireChannelRead(msg);
			clientChannelContext.channel().close();
		}else {
			//转发到下一个handler
			clientChannelContext.fireChannelRead(msg);
		}
	}
	
	private void doProxy(final ChannelHandlerContext clientChannelContext, DefaultSocks5CommandRequest msg) {
		//每次都会启动一个
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(bossGroup)
		.channel(NioSocketChannel.class)
		//及时发送 这里涉及 Nagle算法 宽带较小 或者请求量大可以关闭TCP_NODELAY
		.option(ChannelOption.TCP_NODELAY, true)
		.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				//ch.pipeline().addLast(new LoggingHandler());//in out
				logger.info("返回数据");
				//将目标服务器信息转发给客户端
				ch.pipeline().addLast(new Dest2ClientHandler(clientChannelContext));
			}
		});
		logger.trace("连接目标服务器");
		String addr=msg.dstAddr();
		Integer port=msg.dstPort();
		logger.info("目标地址   {}:{}",addr,port);
		//连接对应的地址与端口
		ChannelFuture future = bootstrap.connect(addr,port);
		
		//
		future.addListener(new ChannelFutureListener() {
			public void operationComplete(final ChannelFuture future) throws Exception {
				if(future.isSuccess()) {
					logger.trace("成功连接目标服务器");
					clientChannelContext.pipeline().addLast(new Client2DestHandler(future));
					logger.info("代理操作完成 返回相应的成功消息");
					Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
					clientChannelContext.writeAndFlush(commandResponse);
				} else {
					Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
					clientChannelContext.writeAndFlush(commandResponse);
				}
			}
			
		});
	}

	/**
	 * 将目标服务器信息转发给客户端
	 * 
	 * @author huchengyi
	 *
	 */
	private static class Dest2ClientHandler extends ChannelInboundHandlerAdapter {
		
		private ChannelHandlerContext clientChannelContext;
		
		public Dest2ClientHandler(ChannelHandlerContext clientChannelContext) {
			this.clientChannelContext = clientChannelContext;
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx2, Object destMsg) throws Exception {
			logger.info("将目标服务器信息转发给客户端");
			clientChannelContext.writeAndFlush(destMsg);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx2) throws Exception {
			logger.info("目标服务器断开连接");
			clientChannelContext.channel().close();
		}
	}
	
	/**
	 * 将客户端的消息转发给目标服务器端
	 * 
	 * @author huchengyi
	 *
	 */
	private static class Client2DestHandler extends ChannelInboundHandlerAdapter {
		
		private ChannelFuture destChannelFuture;
		
		public Client2DestHandler(ChannelFuture destChannelFuture) {
			this.destChannelFuture = destChannelFuture;
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			logger.trace("将客户端的消息转发给目标服务器端");
			destChannelFuture.channel().writeAndFlush(msg);
		}
		
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			logger.trace("客户端断开连接");
			destChannelFuture.channel().close();
		}
	}
}
