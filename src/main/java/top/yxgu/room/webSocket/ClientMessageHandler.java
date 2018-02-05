package top.yxgu.room.webSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.google.protobuf.ByteString;

import App.Model.Net.MsgActionDefine;
import App.Model.Net.MsgOuterClass.LoginReq;
import App.Model.Net.MsgOuterClass.LoginRes;
import App.Model.Net.MsgOuterClass.Msg;
import App.Model.Net.MsgOuterClass.RoomInfoReq;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
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
		ChannelManager.remove(ctx.channel());
		log.info("Closed: " + ctx.channel().remoteAddress().toString());
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception {
		Channel channel = ctx.channel();
		switch (msg.getAction()) {
			case MsgActionDefine.LOGINREQ: {
				ByteString bs = msg.getMsgBody();
				LoginReq req = LoginReq.parseFrom(bs);
				int userId = req.getUserId();
				
				LoginRes res = this.playerInfoService.getLoginRes(userId);
				
				Msg.Builder mb = Msg.newBuilder();
				mb.setAction(MsgActionDefine.LOGINRES);
				mb.setMsgBody(res.toByteString());
				channel.writeAndFlush(mb.build());
				
				if (ChannelManager.containsById(userId)) {
					Channel c = ChannelManager.getById(userId);
					//TODO 踢人
				}
				ChannelManager.add(ctx.channel(), userId);
				log.info("Connect in: " + ctx.channel().remoteAddress().toString()+", userId="+userId);
				break;
			}
			case MsgActionDefine.ROOMINFOREQ: {
				ByteString bs = msg.getMsgBody();
				RoomInfoReq req = RoomInfoReq.parseFrom(bs);
				int userId = req.getUserId();
				int roomId = req.getRoomId();
				int type = req.getType();
			}
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
				ChannelManager.remove(ctx.channel());
                ctx.close();
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
