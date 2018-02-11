package top.yxgu.room.roomScoket;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

@Service
public class RoomSocketClient {
	public static final int RECONNECT_DELAY = 5;
	
	public long heartbeatTime;
	public Channel channel;
	
	@Value("${hallSocket.server.host}")
	private String host;
	@Value("${hallSocket.server.port}")
	private int port;
	
	@Resource
	private RoomScoketClientInitializer roomScoketServerInitializer;
	
    private static final Logger log = LoggerFactory.getLogger(RoomSocketClient.class);
    private EventLoopGroup workGroup;
    private Bootstrap client;
    
	public RoomSocketClient() {
	}
	
	public void init() {
		workGroup = new NioEventLoopGroup(1);
		client = new Bootstrap();
		client.group(workGroup).channel(NioSocketChannel.class)
//			  .handler(new LoggingHandler(LogLevel.INFO))
			  .handler(roomScoketServerInitializer);
	}
	
	public void connect() {
		client.connect(host, port).addListener(new ConnectListener());
//		Channel ch = client.connect(host, port).sync().channel();
		log.info("Connect to RoomSocket Server :"+host+":"+port);
	}
	
	
	
	public void stop(String reason) {
		log.info("Close connection to RoomSocket Server："+reason);
		workGroup.shutdownGracefully();
	}
	
	class ConnectListener implements ChannelFutureListener {
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				heartbeatTime = System.currentTimeMillis();
			} else {
				workGroup.schedule(new Runnable() {
					@Override
					public void run() {
						connect();
					}
				}, RECONNECT_DELAY, TimeUnit.SECONDS);
			}
		}
	}
}


