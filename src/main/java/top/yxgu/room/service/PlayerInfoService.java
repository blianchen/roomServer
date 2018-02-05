package top.yxgu.room.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import App.Model.Net.MsgOuterClass.LoginRes;
import App.Model.Net.MsgOuterClass.PlayerInfo;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

@Service
public class PlayerInfoService {
	
	private static Map<String, String> defaultPlayerInfo;
	static {
		defaultPlayerInfo = new HashMap<String, String>();
		defaultPlayerInfo.put("gems", String.valueOf(55555));
		defaultPlayerInfo.put("coins", String.valueOf(88889999));
		defaultPlayerInfo.put("gunId", String.valueOf(19));
		defaultPlayerInfo.put("maxGunId", String.valueOf(1));
		defaultPlayerInfo.put("roleLevel", String.valueOf(1));
		defaultPlayerInfo.put("roleExp", String.valueOf(100));
		defaultPlayerInfo.put("batterySkinId", String.valueOf(20000));
		defaultPlayerInfo.put("gunrestSkinId", String.valueOf(80001));
	}
	
	
	@Autowired
	private Pool<Jedis> jedisPool;
	
	public PlayerInfoService() {
	}
	
	public LoginRes getLoginRes(int userId) {
		PlayerInfo pi = getPlayer(userId);
		LoginRes.Builder lb = LoginRes.newBuilder();
		lb.setPlayerInfo(pi);
		lb.setSystemTime( (int)(System.currentTimeMillis()/1000) );
		LoginRes res = lb.build();
		return res;
	}
	
	public PlayerInfo getPlayer(int userId) {
		String uid = String.valueOf(userId);
		try (Jedis jedis = jedisPool.getResource()) {
			Map<String, String> map = jedis.hgetAll(uid);
			if (map.isEmpty()) {
				map = defaultPlayerInfo;
				map.put("userId", uid);
				map.put("nickName", "rand"+uid);
				jedis.hmset(uid, map);
			}
			return mapToPlayer(map);
		}
	}
	
	public PlayerInfo mapToPlayer(Map<String, String> map) {
		PlayerInfo.Builder pb = PlayerInfo.newBuilder();
		pb.setUserId(Integer.parseInt(map.get("userId")));
		pb.setNickName(map.get("nickName"));
		pb.setGems(Integer.parseInt(map.get("gems")));
		pb.setCoins(Integer.parseInt(map.get("coins")));
		pb.setGunId(Integer.parseInt(map.get("gunId")));
		pb.setMaxGunId(Integer.parseInt(map.get("maxGunId")));
		pb.setRoleLevel(Integer.parseInt(map.get("roleLevel")));
		pb.setRoleExp(Integer.parseInt(map.get("roleExp")));
		pb.setBatterySkinId(Integer.parseInt(map.get("batterySkinId")));
		pb.setGunrestSkinId(Integer.parseInt(map.get("gunrestSkinId")));
		return pb.build();
	}
}
 