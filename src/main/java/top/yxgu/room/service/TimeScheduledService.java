package top.yxgu.room.service;

import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import top.yxgu.room.action.ActMsgCommon;
import top.yxgu.room.action.RoomActor;
import top.yxgu.room.model.RoomManager;
import top.yxgu.room.model.UserManager;
import top.yxgu.room.roomScoket.RoomMessageDefine;
import top.yxgu.room.roomScoket.RoomSocketClient;

@Service
public class TimeScheduledService {
	private ScheduledExecutorService executor;
	
	@Resource
	private RoomSocketClient roomSocketClient;
	
	public void start() {
		executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(new RoomScheduleThread(), 1, 1, TimeUnit.SECONDS);
	}
	
	class RoomScheduleThread implements Runnable {
		private ActMsgCommon pondFish = new ActMsgCommon(ActMsgCommon.POND_FISH);
		
		@Override
		public void run() {
			try {
				long now = System.currentTimeMillis() >> 10;
				
				Iterator<RoomActor> it = RoomManager.getIterator();
				RoomActor ra;
				while (it.hasNext()) {
					ra = it.next();
					if (now - ra.pondFishTime >= 2) {
						ra.pondFishTime = now;
						ra.addAction(pondFish);
					}
				}
				
				if (now - roomSocketClient.heartbeatTime >= 29) {
					roomSocketClient.heartbeatTime = now;
					Channel channel = roomSocketClient.channel;
					ByteBuf msg = channel.alloc().buffer();
					msg.writeShort(RoomMessageDefine.SYNC_ROOM_INF);		//action
					msg.writeInt(RoomManager.roomServerId);
					msg.writeInt(RoomManager.size());
					msg.writeInt(UserManager.size());
					channel.writeAndFlush(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

