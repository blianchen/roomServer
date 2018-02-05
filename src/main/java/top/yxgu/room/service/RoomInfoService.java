package top.yxgu.room.service;

import org.springframework.stereotype.Service;

import App.Model.Net.MsgOuterClass.RoomInfoRes;
import top.yxgu.room.model.RoomData;

@Service
public class RoomInfoService {
//	@Autowired
//	private Pool<Jedis> jedisPool;
	
	public RoomInfoRes getRoomInfo(int userId, int type, int roomId) {
		RoomData rd = selectRoom(userId, type, roomId);
		
		RoomInfoRes.Builder bd = RoomInfoRes.newBuilder();
		bd.setRoomId(rd.id);
		bd.setType(rd.type);
		bd.setIp(rd.host);
		bd.setPort(rd.port);
		return bd.build();
	}
	
	private RoomData selectRoom(int userId, int type, int roomId) {
		return null;
	}
}
