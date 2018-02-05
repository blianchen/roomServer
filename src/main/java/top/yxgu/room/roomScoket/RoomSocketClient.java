package top.yxgu.room.roomScoket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;

@Service
public class RoomSocketClient {
	@Value("${hallSocket.server.host}")
	private String host;
	@Value("${hallSocket.server.port}")
	private int port;
	
	@Autowired
	private RoomScoketClientInitializer roomScoketServerInitializer;
	
    private static final Logger log = LoggerFactory.getLogger(RoomSocketClient.class);
    private EventLoopGroup workGroup;
	
	public RoomSocketClient() {
	}
	
	public void run() {
		workGroup = new NioEventLoopGroup();
		
		try {
			Bootstrap client = new Bootstrap();
			client.group(workGroup).channel(NioSocketChannel.class)
//				  .handler(new LoggingHandler(LogLevel.INFO))
				  .handler(roomScoketServerInitializer);
			
			Channel ch = client.connect(host, port).sync().channel();
			
			log.info("Connect to RoomSocket Server :"+host+":"+port);
			ch.closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			workGroup.shutdownGracefully();
		}
	}
	
	public void stop(String reason) {
		log.info("Close connection to RoomSocket Server："+reason);
		workGroup.shutdownGracefully();
	}
}
