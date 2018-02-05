package top.yxgu.room.roomScoket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@Controller
@Sharable
public class HallMessageHandler extends SimpleChannelInboundHandler<ByteBuf> {
	
	private static final Logger log = LoggerFactory.getLogger(HallMessageHandler.class);
	
	@Value("${websocket.server.host}")
	private String host;
	@Value("${websocket.server.port}")
	private int port;
	@Value("${room.server.maxRoomNum}")
	private int maxRoomNum;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ByteBuf msg = ctx.alloc().buffer();
		msg.writeShort(1);					//action
		byte[] b = host.getBytes();
		msg.writeShort(b.length);
		msg.writeBytes(b);					//host
		msg.writeInt(port);					//port
		msg.writeInt(maxRoomNum);			//可容纳的最大房间数
		ctx.writeAndFlush(msg);
		log.info("Connect in HallServer.");
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
	}
	
	 @Override
	 public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		 ctx.flush();
	 }
	 
	 @Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		 cause.printStackTrace();
		 ctx.close();
	}
}
