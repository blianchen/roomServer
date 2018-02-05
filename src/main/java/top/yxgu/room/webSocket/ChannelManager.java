package top.yxgu.room.webSocket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChannelManager {
	public static final AttributeKey<Integer> CH_ID = AttributeKey.valueOf("ch_id");
	
	private static ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	private static ConcurrentMap<Integer, Channel> idToChannelMap = new ConcurrentHashMap<>();
	
	public static void add(Channel c, int userId) {
		allChannels.add(c);
		Channel rc = idToChannelMap.put(userId, c);
		if (rc!= null && rc != c) {
			remove(rc);
			rc.close();
		}
		Attribute<Integer> userIdAttr = c.attr(CH_ID);
		userIdAttr.setIfAbsent(userId);
	}
	
	public static boolean contains(Channel c) {
		return allChannels.contains(c);
	}
	
	public static boolean containsById(int id) {
		return idToChannelMap.containsKey(id);
	}
	
	public static void remove(Channel c) {
		allChannels.remove(c);
		Attribute<Integer> userIdAttr = c.attr(CH_ID);
		int userId = userIdAttr.get();
		idToChannelMap.remove(userId);
	}
	
	public static void removeById(int id) {
		Channel c = idToChannelMap.get(id);
		allChannels.remove(c);
		idToChannelMap.remove(id);
	}
	
	public static Channel getById(int id) {
		return idToChannelMap.get(id);
	}
	
	public static void writeAndFlush(Object message) {
		allChannels.writeAndFlush(message);
	}
	
	public static ChannelGroupFuture close() {
		return allChannels.close();
	}
	
	public static int size() {
		return allChannels.size();
	}
}
