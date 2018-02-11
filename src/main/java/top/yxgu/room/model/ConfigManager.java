package top.yxgu.room.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import redis.clients.jedis.Jedis;
import top.yxgu.room.model.config.ConfCv1;
import top.yxgu.room.model.config.ConfCv2;
import top.yxgu.room.model.config.ConfData;
import top.yxgu.room.model.config.ConfFish;
import top.yxgu.room.model.config.ConfFishBoom;
import top.yxgu.room.model.config.ConfFishContain;
import top.yxgu.room.model.config.ConfFishGroup;
import top.yxgu.room.model.config.ConfFishPos;
import top.yxgu.room.model.config.ConfFishStandard;
import top.yxgu.room.model.config.ConfGun;
import top.yxgu.room.model.config.ConfGunSkin;
import top.yxgu.room.model.config.ConfItem;
import top.yxgu.room.model.config.ConfLanguage;
import top.yxgu.room.model.config.ConfLottery;
import top.yxgu.room.model.config.ConfPath;
import top.yxgu.room.model.config.ConfRoomType;
import top.yxgu.room.model.config.ConfSv;
import top.yxgu.room.model.config.ConfWorldDrop;
import top.yxgu.room.service.RedisPool;

@Component
public class ConfigManager {
	public static final String CONFIG_JSON_PREFIX = "config.";
	public static final String[] sendRoomConfig = 
				{"fish.json", 		"fish_boom.json", 		"fish_contain.json", 	"fish_group.json",
				 "fish_pos.json", 	"fish_standard.json", 	"gun.json", 			"gun_skin.json",
				 "item.json", 		"Lottery.json", 		"path.json", 			"room_type.json", 
				 "world_drop.json"
				};
	
	public static Map<Integer, ConfCv1> cv1Map;
	public static Map<Integer, ConfCv2> cv2Map;
	public static Map<Integer, ConfFish> fishMap;
	public static Map<Integer, ConfFishBoom> fishBoomMap;
	public static Map<Integer, ConfFishContain> fishContainMap;
	public static Map<Integer, ConfFishGroup> fishGroupMap;
	public static Map<Integer, ConfFishPos> fishPosMap;
	public static Map<Integer, ConfFishStandard> fishStandardMap;
	public static Map<Integer, ConfGun> gunMap;
	public static Map<Integer, ConfGunSkin> gunSkinMap;
	public static Map<Integer, ConfItem> itemMap;
	public static Map<Integer, ConfLanguage> languageMap;
	public static Map<Integer, ConfLottery> lotteryMap;
	public static Map<Integer, ConfPath> pathMap;
	public static Map<Integer, ConfRoomType> roomTypeMap;
	public static Map<Integer, ConfSv> svMap;
	public static Map<Integer, ConfWorldDrop> worldDropMap;
	
//	private static String configDir;
//	@Value("${config.dir}")
//	public void setConfigDir(String v) {
//		ConfigManager.configDir = v;
//	}
	
	private static RedisPool redisPool;
	@Resource
	public void setRedisPool(RedisPool v) {
		ConfigManager.redisPool = v;
	}
	
	public static void loadJsonConfig() {
//		cv1Map = loadConfigFromFile(dir, "Cv1.json", new TypeReference<ArrayList<ConfCv1>>() {});
//		cv2Map = loadConfigFromFile(dir, "Cv2.json", new TypeReference<ArrayList<ConfCv2>>() {});
		fishMap = loadConfigFromRedis("fish.json", new TypeReference<ArrayList<ConfFish>>() {});
		fishBoomMap = loadConfigFromRedis("fish_boom.json", new TypeReference<ArrayList<ConfFishBoom>>() {});
		fishContainMap = loadConfigFromRedis("fish_contain.json", new TypeReference<ArrayList<ConfFishContain>>() {});
		fishGroupMap = loadConfigFromRedis("fish_group.json", new TypeReference<ArrayList<ConfFishGroup>>() {});
		fishPosMap = loadConfigFromRedis("fish_pos.json", new TypeReference<ArrayList<ConfFishPos>>() {});
		fishStandardMap = loadConfigFromRedis("fish_standard.json", new TypeReference<ArrayList<ConfFishStandard>>() {});
		gunMap = loadConfigFromRedis("gun.json", new TypeReference<ArrayList<ConfGun>>() {});
		gunSkinMap = loadConfigFromRedis("gun_skin.json", new TypeReference<ArrayList<ConfGunSkin>>() {});
		itemMap = loadConfigFromRedis("item.json", new TypeReference<ArrayList<ConfItem>>() {});
//		languageMap = loadConfigFromFile(dir, "language.json", new TypeReference<ArrayList<ConfLanguage>>() {});
		lotteryMap = loadConfigFromRedis("Lottery.json", new TypeReference<ArrayList<ConfLottery>>() {});
		pathMap = loadConfigFromRedis("path.json", new TypeReference<ArrayList<ConfPath>>() {});
		roomTypeMap = loadConfigFromRedis("room_type.json", new TypeReference<ArrayList<ConfRoomType>>() {});
//		svMap = loadConfigFromFile(dir, "sv.json", new TypeReference<ArrayList<ConfSv>>() {});
		worldDropMap = loadConfigFromRedis("world_drop.json", new TypeReference<ArrayList<ConfWorldDrop>>() {});
	}
	
	private static <T extends ConfData> Map<Integer, T> loadConfigFromRedis(String fileName, TypeReference<ArrayList<T>> clazz) {
		Map<Integer, T> map = new HashMap<>();
		String s = null;
		try (Jedis jedis = redisPool.getResource()) {
			s = jedis.get(CONFIG_JSON_PREFIX+fileName);
		}
		if (s == null) return null;
		ArrayList<T> list = JSON.parseObject(s, clazz);
		for (int i=0; i<list.size(); i++) {
			map.put(list.get(i).id, list.get(i));
		}
		return map;
	}
}
