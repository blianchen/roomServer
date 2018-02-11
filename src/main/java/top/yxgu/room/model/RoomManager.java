package top.yxgu.room.model;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import top.yxgu.room.action.RoomActor;

public class RoomManager {
	public static int groupId = 0;
	public static int roomServerId = 0;
	
	private static final ConcurrentMap<Integer, RoomActor> roomActorMap = new ConcurrentHashMap<>();
	
	public static void add(int userId, RoomActor ra) {
		int roomId = ra.getId();
		roomActorMap.put(roomId, ra);
	}
	
	public static void remove(RoomActor ra) {
		ra.isDel = true;
		roomActorMap.remove(ra.getId());
		int[] userIds = ra.getCurrUsers();
		UserData ud;
		for (int i=0; i<userIds.length; i++) {
			if (userIds[i] > 0) {
				ud = UserManager.get(userIds[i]);
				ud.roomId = 0;
			}
		}
	}
	
	public static void removeById(int id) {
		RoomActor ra = roomActorMap.remove(id);
		if (ra == null) return;
		ra.isDel = true;
		int[] userIds = ra.getCurrUsers();
		UserData ud;
		for (int i=0; i<userIds.length; i++) {
			if (userIds[i] > 0) {
				ud = UserManager.get(userIds[i]);
				ud.roomId = 0;
			}
		}
	}
	
	public static RoomActor get(int id) {
		return roomActorMap.get(id);
	}
	
	public static Iterator<RoomActor> getIterator() {
		return roomActorMap.values().iterator();
	}
	
	public static int size() {
		return roomActorMap.size();
	}
}
