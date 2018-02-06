package top.yxgu.room.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoomManager {
	private static volatile AtomicInteger roomUid = new AtomicInteger(0);
	
	public static final ConcurrentMap<Integer, RoomData> roomMap = new ConcurrentHashMap<>();
	
	
	public static RoomData create(int type) {
		RoomData room = new RoomData();
		room.id = roomUid.incrementAndGet();
		room.type = type;
		roomMap.put(room.id, room);
		return room;
	}
}
