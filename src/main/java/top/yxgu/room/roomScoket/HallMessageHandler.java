package top.yxgu.room.roomScoket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import top.yxgu.room.model.RoomData;
import top.yxgu.room.service.RoomService;

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
	
	@Autowired
	private RoomService roomService;
	
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
		int action = msg.readShort();
		switch (action) {
			case RoomMessageDefine.REGISTER_RES: {
				
				break;
			}
			case RoomMessageDefine.REQUEST_ROOM_REQ: {
				int userId = msg.readInt();
				int type = msg.readInt();
				RoomData room = roomService.selectOrCreate(type);
				
				if (room == null) {
					//TODO 发送重新获取房间消息
				} else {
					 ByteBuf sndMsg = ctx.alloc().buffer();
					 sndMsg.writeShort(RoomMessageDefine.REQUEST_ROOM_RES);
					 sndMsg.writeInt(userId);
					 sndMsg.writeInt(type);
					 sndMsg.writeInt(room.id);
					 ctx.writeAndFlush(msg);
				}
				break;
			}
			default: {
				log.warn("Unkonw RoomMessage action:"+action);
			}
		}
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
