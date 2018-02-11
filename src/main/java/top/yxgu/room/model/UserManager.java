package top.yxgu.room.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

public class UserManager {
	public static final AttributeKey<Integer> CH_ID = AttributeKey.valueOf("ch_id");
	
//	private static ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	private static ConcurrentMap<Integer, UserData> userMap = new ConcurrentHashMap<>();
	
	public static void add(UserData data) {
		Channel c = data.channel;
//		allChannels.add(c);
		UserData rc = userMap.put(data.getId(), data);
		if (rc!= null && rc != c) {
//			allChannels.remove(c);
			c.close();
		}
	}
	
//	public static boolean contains(Channel c) {
//		return userMap.
//	}
	
	public static boolean containsById(int id) {
		return userMap.containsKey(id);
	}
	
	public static void remove(Channel c) {
//		allChannels.remove(c);
		Attribute<Integer> userIdAttr = c.attr(CH_ID);
		int userId = userIdAttr.get();
		userMap.remove(userId);
	}
	
	public static void removeById(int id) {
		UserData data = userMap.get(id);
//		allChannels.remove(data.channel);
		userMap.remove(id);
	}
	
	public static UserData get(int id) {
		return userMap.get(id);
	}
	
//	public static void writeAndFlush(Object message) {
//		allChannels.writeAndFlush(message);
//	}
	
//	public static ChannelGroupFuture close() {
//		return allChannels.close();
//	}
	
	public static int size() {
		return userMap.size();
	}
}
