package top.yxgu.utils;

import java.io.UnsupportedEncodingException;

import com.google.protobuf.Message;

import App.Model.Net.MsgOuterClass.Msg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class CommonFun {
	public static final String charsetName = "utf-8";
	
	public static void writeStr(ByteBuf msg, String str) {
		try {
			byte[] b = str.getBytes(charsetName);
			msg.writeShort(b.length);
			msg.writeBytes(b);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendProtoBufMsg(Channel channel, int action, Message message) {
		Msg.Builder b = Msg.newBuilder();
		b.setAction(action);
		b.setMsgBody(message.toByteString());
		channel.writeAndFlush(b.build());
	}
}
