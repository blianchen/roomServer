package top.yxgu.room.roomScoket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

@Component
public class RoomScoketClientInitializer extends ChannelInitializer<SocketChannel> {
	
	private static final int maxFrameLength = 65536;
	private static final int headerLength = 2;
	
	@Autowired
	private HallMessageHandler hallMessageHandler;
	
	private LengthFieldPrepender lengthFieldPrepender;
	
	public RoomScoketClientInitializer() {
		lengthFieldPrepender = new LengthFieldPrepender(headerLength);
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		 ChannelPipeline pipeline = ch.pipeline();
		 pipeline.addLast(new LengthFieldBasedFrameDecoder(maxFrameLength, 0, headerLength, 0, headerLength));
		 pipeline.addLast(lengthFieldPrepender);
		 
		 pipeline.addLast(hallMessageHandler);
	}

}
