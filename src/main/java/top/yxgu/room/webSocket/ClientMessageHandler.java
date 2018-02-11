package top.yxgu.room.webSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.google.protobuf.ByteString;

import App.Model.Net.MsgActionDefine;
import App.Model.Net.MsgOuterClass.IntoRoomReq;
import App.Model.Net.MsgOuterClass.Msg;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import top.yxgu.room.action.ActMsg;
import top.yxgu.room.action.ActMsgCommon;
import top.yxgu.room.action.ActMsgProtoBuf;
import top.yxgu.room.action.RoomActor;
import top.yxgu.room.model.RoomManager;
import top.yxgu.room.model.UserData;
import top.yxgu.room.model.UserManager;
import top.yxgu.room.service.PlayerInfoService;


@Controller
@Sharable
public class ClientMessageHandler extends SimpleChannelInboundHandler<Msg> {
	private static final Logger log = LoggerFactory.getLogger(ClientMessageHandler.class);
	
	@Autowired
	public PlayerInfoService playerInfoService;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//		Channel channel = ctx.channel();
//		Attribute<Integer> att = channel.attr(UserManager.CH_ID);
//		int userId = att.get();
//		UserData ud = UserManager.get(userId);
//		RoomActor ra = RoomManager.get(ud.roomId);
//		ActMsgCommon ac = new ActMsgCommon(ActMsgCommon.REMOVE_USER);
//		ac.addData("userId", userId);
//		ac.addData("isSendNotify", false);
//		ra.addAction(ac);
//		ud.channel = null;
//		UserManager.removeById(userId);
		// 断线保留，等待重连
		log.info("Closed: " + ctx.channel().remoteAddress().toString());
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception {
		Channel channel = ctx.channel();
		int action = msg.getAction();
		switch (action) {
			case MsgActionDefine.INTOROOMREQ: {
				ByteString bs = msg.getMsgBody();
				IntoRoomReq req = IntoRoomReq.parseFrom(bs);
				int userId = req.getUserId();
				int roomId = req.getRoomId();
				int type = req.getType();
				
				UserData ud = UserManager.get(userId);
				if (ud == null) {
					//TODO 发重新登录消息
					break;
				}
				
				RoomActor ra = null;
				if (roomId == 0 && type == 0) {
					//TODO 自动选
				} else {
					ra = RoomManager.get(roomId);
					if (ra == null || ra.getType() != type || ud.roomId != roomId) {
						//TODO 发送进入房间类型错误
						break;
					}
				}
				if (ra != null) {
					ud.channel = channel;
					Attribute<Integer> userIdAttr = channel.attr(UserManager.CH_ID);
					userIdAttr.setIfAbsent(userId);
					
					ActMsg am = new ActMsgProtoBuf(action, req);
					ra.addAction(am);
					log.info("Connect in: " + ctx.channel().remoteAddress().toString()+", userId="+userId);
				}
				break;
			}
//			case MsgActionDefine.ROOMINFOREQ: {
//				ByteString bs = msg.getMsgBody();
//				RoomInfoReq req = RoomInfoReq.parseFrom(bs);
//				int userId = req.getUserId();
//				int roomId = req.getRoomId();
//				int type = req.getType();
//			}
			case MsgActionDefine.HEARTBEAT: {
				break;
			}
			default: {
				log.warn("Unkonw action:"+msg.getAction());
			}
		}
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;  
			if (e.state() == IdleState.READER_IDLE) {
				Channel channel = ctx.channel();
				Attribute<Integer> att = channel.attr(UserManager.CH_ID);
				Integer userId = att.get();
				ctx.close();
				
				if (userId == null) {
					return;
				}
				UserData ud = UserManager.get(userId);
				if (ud == null) {
					return;
				}
				
				ud.channel = null;
				if (ud.roomId == 0) {
					UserManager.removeById(userId);
					return;
				}
				RoomActor ra = RoomManager.get(ud.roomId);
				if (ra == null) {
					UserManager.removeById(userId);
					return;
				}
				
				ActMsgCommon ac = new ActMsgCommon(ActMsgCommon.REMOVE_USER);
				ac.addData("userId", userId);
				ac.addData("isSendNotify", false);
				ra.addAction(ac);
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
