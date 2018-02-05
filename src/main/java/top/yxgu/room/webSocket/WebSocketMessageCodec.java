package top.yxgu.room.webSocket;

import java.util.List;

import org.springframework.stereotype.Component;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

@Component
@Sharable
public class WebSocketMessageCodec extends MessageToMessageCodec<WebSocketFrame, ByteBuf> {
	@Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
        if (frame instanceof BinaryWebSocketFrame) {
            frame.retain();
            out.add(frame.content());
        } else {
        	System.out.println("Not BinaryWebSocketFrame: "+frame.getClass().getName());
        }
    }

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(msg);
		frame.retain();
		out.add(frame);
	}
}
