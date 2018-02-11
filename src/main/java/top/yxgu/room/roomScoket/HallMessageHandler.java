package top.yxgu.room.roomScoket;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import top.yxgu.room.action.RoomActor;
import top.yxgu.room.model.ConfigManager;
import top.yxgu.room.model.RoomManager;
import top.yxgu.room.model.UserData;
import top.yxgu.room.model.UserManager;
import top.yxgu.room.service.RoomService;
import top.yxgu.utils.CommonFun;

@Controller
@Sharable
public class HallMessageHandler extends SimpleChannelInboundHandler<ByteBuf> {
	
	private static final Logger log = LoggerFactory.getLogger(HallMessageHandler.class);
	
	@Value("${websocket.server.ssl}")
	private boolean ssl;
	@Value("${websocket.server.host}")
	private String host;
	@Value("${websocket.server.port}")
	private int port;
	@Value("${websocket.server.path}")
	private String path;
	@Value("${room.server.maxRoomNum}")
	private int maxRoomNum;
	
	@Resource
	private RoomService roomService;
	
	@Resource 
	private RoomSocketClient roomSocketClient;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		roomSocketClient.channel = ctx.channel();
		
		ByteBuf msg = ctx.alloc().buffer();
		msg.writeShort(RoomMessageDefine.REGISTER_REQ);		//action
		String url = (ssl?"wss://":"ws://") + host + ":" + port + path;
		CommonFun.writeStr(msg, url);		//url
//		msg.writeInt(port);					//port
		msg.writeInt(maxRoomNum);			//可容纳的最大房间数
		msg.writeInt(RoomManager.size());
		msg.writeInt(UserManager.size());
		ctx.writeAndFlush(msg);
		log.info("Connected to HallServer.");
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		roomSocketClient.channel = null;
		ctx.channel().eventLoop().schedule(new Runnable() {
			@Override
			public void run() {
				roomSocketClient.connect();
			}
		}, RoomSocketClient.RECONNECT_DELAY, TimeUnit.SECONDS);
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		int action = msg.readShort();
		switch (action) {
			case RoomMessageDefine.REQUEST_ROOM_REQ: { // 3
				int userId = msg.readInt();
				int type = msg.readInt();
				int roomId = 0;
				UserData ud = UserManager.get(userId); //已经存在
				if (ud != null) {
					if (ud.roomId > 0) {
						RoomActor ra = RoomManager.get(ud.roomId);
						if (ra != null && ra.contain(userId)) {
							roomId = ud.roomId;
						}
					}
				} else {
					ud = new UserData(userId);//先注册用户通道
					ud.roomType = type;
					UserManager.add(ud);
				}
				
				if (roomId <= 0) {
					roomId = roomService.selectOrCreate(type, userId);
				}
				
				if (roomId <= 0) {
					//TODO 发送重新获取房间消息
				} else {
					ud.roomId = roomId;
					
					ByteBuf sndMsg = ctx.alloc().buffer();
					sndMsg.writeShort(RoomMessageDefine.REQUEST_ROOM_RES);
					sndMsg.writeInt(RoomManager.roomServerId);
					sndMsg.writeInt(userId);
					sndMsg.writeInt(type);
					sndMsg.writeInt(roomId);
					sndMsg.writeInt(RoomManager.size());
					sndMsg.writeInt(UserManager.size());
					ctx.writeAndFlush(sndMsg);
				}
				break;
			}
			case RoomMessageDefine.REGISTER_RES: {	// 1
				int groupId = msg.readInt();
				int roomServerId = msg.readInt();
				RoomManager.groupId = groupId;
				RoomManager.roomServerId = roomServerId;
				break;
			}
			case RoomMessageDefine.SYNC_CONFIG_REQ: { // 2
				ConfigManager.loadJsonConfig();
				
				ByteBuf sndMsg = ctx.alloc().buffer();
				sndMsg.writeShort(RoomMessageDefine.SYNC_CONFIG_RES);
				ctx.writeAndFlush(sndMsg);
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
