package top.yxgu.room.action;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import App.Model.Net.MsgActionDefine;
import App.Model.Net.MsgOuterClass.AddFish;
import App.Model.Net.MsgOuterClass.Coordinate;
import App.Model.Net.MsgOuterClass.IntoRoomReq;
import App.Model.Net.MsgOuterClass.IntoRoomRes;
import App.Model.Net.MsgOuterClass.Msg;
import App.Model.Net.MsgOuterClass.PlayerInfo;
import App.Model.Net.MsgOuterClass.PondFishes;
import io.netty.channel.Channel;
import top.yxgu.room.model.ConfigManager;
import top.yxgu.room.model.FishData;
import top.yxgu.room.model.UserData;
import top.yxgu.room.model.UserManager;
import top.yxgu.room.model.config.ConfFish;
import top.yxgu.room.model.config.ConfFishPos;
import top.yxgu.room.model.config.ConfPath;
import top.yxgu.room.service.PlayerInfoService;
import top.yxgu.utils.CommonFun;

@Component
@Scope("prototype")
public class RoomActor extends ActorSimulator {
	private static final Logger log = LoggerFactory.getLogger(RoomActor.class);
	
	public static final int SEAT_NUM = 4;
	
	public long pondFishTime;
	private int fishUid = 0;
	
	private int type;
	private int num = 0;
	
	private int[] currUsers;
	private Map<Integer, FishData> fishs;
	
	@Resource
	private PlayerInfoService playerInfoService;
	
	public RoomActor(int id, int type, Executor executor) {
		super(id, executor);
		this.type = type;
		this.currUsers = new int[SEAT_NUM];
		fishs = new HashMap<>();
		pondFishTime = System.currentTimeMillis() >> 10;
	}
	
	public int add(int userId) {
		int idx = -1;
		for (int i=0; i<SEAT_NUM; i++) {
			if (currUsers[i] == userId) {
				idx = i;
				UserData ud = UserManager.get(userId);
				if (ud != null && ud.channel != null) {
					//TODO 踢掉
				}
				break;
			} else if (idx == -1 && currUsers[i] == 0) {
				idx = i;
			}
		}
		if (idx != -1) {
			num++;
			currUsers[idx] = userId;
		}
		return -1;
	}
	
	private void remove(int userId) {
		for (int i=0; i<SEAT_NUM; i++) {
			if (currUsers[i] == userId) {
				currUsers[i] = 0;
				num--;
			}
		}
	}
	
	public boolean contain(int userId) {
		for (int i=0; i<SEAT_NUM; i++) {
			if (currUsers[i] == userId) {
				return true;
			}
		}
		return false;
	}
	
	public int getType() {
		return type;
	}

	public int getNum() {
		return this.num;
	}

	public int[] getCurrUsers() {
		return currUsers;
	}
	
	private int getPos(int userId) {
		for (int i=0; i<SEAT_NUM; i++) {
			if (currUsers[i] == userId) {
				return i;
			}
		}
		return -1;
	}

	@Override
	protected void processAction(ActMsg am) {
		if (am instanceof ActMsgProtoBuf) {
			ActMsgProtoBuf mp = (ActMsgProtoBuf)am;
			int action  = mp.getAction();
			switch (action) {
				case MsgActionDefine.INTOROOMREQ: {
					processIntoRoomReq(mp.getMessage());
					break;
				}
				default: {
					log.warn("No reg action="+action);
				}
			}
		} else if (am instanceof ActMsgCommon) {
			ActMsgCommon mc = (ActMsgCommon)am;
			int action  = mc.getAction();
			switch (action) {
				case ActMsgCommon.REMOVE_USER: {
					int uid = (int)mc.getData("userId");
					boolean isSendNotify = (boolean)mc.getData("isSendNotify");
					remove(uid);
					//TODO 给其他人发离开消息
					break;
				}
				case ActMsgCommon.POND_FISH: {
					pondFish();
					break;
				}
				default: {
					log.warn("No reg action="+action);
				}
			}
		} else {
			log.warn("No reg Msg Class="+am.getClass().getName());
		}
	}
	
	private void pondFish() {
		ConfFish fish = ConfigManager.fishMap.get(1);
		ConfPath path = ConfigManager.pathMap.get(2001);
		ConfFishPos pos = ConfigManager.fishPosMap.get(20401);
		FishData fd = new FishData();
		fd.uuid = ++fishUid;
		fd.fishId = fish.id;
		fd.pathId = path.id;
		fd.type = fish.type;
		fd.x = pos.min_x;
		fd.y = pos.min_y;
		fd.aliveTime = path.during;
		fishs.put(fd.uuid, fd);
		
		AddFish.Builder addFishBuilder = AddFish.newBuilder();
		addFishBuilder.setFishId(fd.fishId);
		addFishBuilder.setType(fd.type);
		addFishBuilder.setPathId(fd.pathId);
		addFishBuilder.addUniId(fd.uuid);
		addFishBuilder.setAliveTime(fd.aliveTime);
		Coordinate.Builder cb = Coordinate.newBuilder();
		cb.setXvalue(fd.x);
		cb.setYvalue(fd.y);
		addFishBuilder.setCoordinate(cb);
		
		PondFishes.Builder pd = PondFishes.newBuilder();
		pd.addFishes(addFishBuilder);
		sendMsgToAll(MsgActionDefine.PONDFISHES, pd.build(), -1);
	}
	
	private void processIntoRoomReq(Message protoMsg) {
		IntoRoomReq req = (IntoRoomReq)protoMsg;
		int userId = req.getUserId();
		int pos = this.getPos(userId);
		if (pos < 0) {
			//TODO 房间id错误，发重新登录消息
			return;
		} else {
			UserData user = UserManager.get(userId);
			PlayerInfo myPlayinfo = playerInfoService.getPlayer(userId, pos);
			if (user == null || myPlayinfo == null) { //illegal user, close.
				closeIllegalUser(pos);
				return;
			}
			Channel channel = user.channel;
			
			// IntoRoomRes
			IntoRoomRes.Builder intoRoomBd = IntoRoomRes.newBuilder();
			intoRoomBd.setFlag(1);
			intoRoomBd.addPlayerInfo(myPlayinfo);
			PlayerInfo pi = null;
			int uid;
			for (int i=0; i<SEAT_NUM; i++) {
				uid = currUsers[i];
				if (uid > 0 && uid != userId) {
					pi = playerInfoService.getPlayer(uid, i);
					if (pi == null) { // Not find user info, illegal user, close.
						closeIllegalUser(i);
						continue;
					}
					intoRoomBd.addPlayerInfo(pi);
				}
			}
			
			IntoRoomRes tmp = intoRoomBd.build();
			CommonFun.sendProtoBufMsg(channel, MsgActionDefine.INTOROOMRES, tmp);
			
			// PondFishes
			PondFishes.Builder pondFishBd = PondFishes.newBuilder();
			CommonFun.sendProtoBufMsg(channel, MsgActionDefine.PONDFISHES, pondFishBd.build());
			
			// send IntoRoomRes to other user
			intoRoomBd = IntoRoomRes.newBuilder();
			intoRoomBd.setFlag(1);
			intoRoomBd.addPlayerInfo(myPlayinfo);
			sendMsgToAll(MsgActionDefine.INTOROOMRES, intoRoomBd.build(), userId);
		}
	}
	
	private void closeIllegalUser(int pos) {
		int userId = this.currUsers[pos];
		if (userId == 0) return;
		UserData ud = UserManager.get(userId);
		if (ud != null) {
			if (ud.channel != null) ud.channel.close();
			UserManager.removeById(userId);
		}
		currUsers[pos] = 0;
		this.num--;
	}
	
	private void sendMsgToAll(int action, Message msg, int excludeUid) {
		Msg.Builder mb = Msg.newBuilder();
		mb.setAction(action);
		mb.setMsgBody(msg.toByteString());
		Msg res = mb.build();
		
		int userId;
		UserData user;
		for (int i=0; i<SEAT_NUM; i++) {
			userId = currUsers[i];
			if (userId == 0 || userId == excludeUid) continue;
			user = UserManager.get(userId);
			Channel channel = user.channel;
			if (channel != null) channel.writeAndFlush(res);
		}
	}
	
}
