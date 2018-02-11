package top.yxgu.room.service;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import App.Model.Net.MsgOuterClass.PlayerInfo;
import redis.clients.jedis.Jedis;

@Service
public class PlayerInfoService {
	
	private static final String userKeyPrefix = "user.info.";
	
	@Resource
	private RedisPool redisPool;
	
	public PlayerInfoService() {
	}
	
	public PlayerInfo getPlayer(int userId, int pos) {
		String uid = String.valueOf(userId);
		try (Jedis jedis = redisPool.getResource()) {
			Map<String, String> map = jedis.hgetAll(userKeyPrefix+uid);
			if (map.isEmpty()) {
				return null;
			}
			
			PlayerInfo.Builder pb = mapToPlayer(map);
			pb.setPosition(pos);
			return pb.build();
		}
	}
	
	public PlayerInfo.Builder mapToPlayer(Map<String, String> map) {
		PlayerInfo.Builder pb = PlayerInfo.newBuilder();
		pb.setUserId(Integer.parseInt(map.get("userId")));
		pb.setNickName(map.get("nickName"));
		pb.setIconUrl(map.get("iconUrl"));
		pb.setGems(Integer.parseInt(map.get("gems")));
		pb.setCoins(Integer.parseInt(map.get("coins")));
		//uint32 position
		pb.setGunId(Integer.parseInt(map.get("gunId")));
		pb.setMaxGunId(Integer.parseInt(map.get("maxGunId")));
//		repeated uint32 items //正在使用的锁定道具ID
//		repeated LockRelation lockRelation
		pb.setRoleLevel(Integer.parseInt(map.get("roleLevel")));
		pb.setRoleExp(Integer.parseInt(map.get("roleExp")));
//		uint32 vipLevel
		pb.setBatterySkinId(Integer.parseInt(map.get("batterySkinId")));//炮台皮肤
		pb.setGunrestSkinId(Integer.parseInt(map.get("gunrestSkinId")));//炮座皮肤
//	    uint32 coupon = 16;//点券
//	    uint32 totalChargeRMB = 17;//累计充值金额(单位是分)
//	    uint32 monthEndTime = 18;//月卡过期时间
//	    uint32 gunPow = 19; //威力
		return pb;
	}
}
 