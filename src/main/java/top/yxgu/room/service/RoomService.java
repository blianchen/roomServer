package top.yxgu.room.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import top.yxgu.room.SpringContextUtil;
import top.yxgu.room.action.RoomActor;
import top.yxgu.room.model.RoomManager;

@Service
public class RoomService {
	private static volatile AtomicInteger roomUid = new AtomicInteger(0);
	
	private List<LinkedBlockingQueue<RoomActor>> usableArr;
	
	@Resource
	private Executor roomExecutor;
	
	public RoomService() {
		usableArr = new ArrayList<>(10);
		for (int i=0; i<10; i++) {
			usableArr.add(new LinkedBlockingQueue<>());
		}
	}
	
	public int selectOrCreate(int type, int userId) throws InterruptedException {
		RoomActor actor;
		LinkedBlockingQueue<RoomActor> queue = usableArr.get(type);
		while ((actor = queue.poll()) != null && !actor.isDel) {
			actor.add(userId);
			if (actor.getNum() < RoomActor.SEAT_NUM) {
				queue.put(actor);
			}
			return actor.getId();
		}
		
		int id = roomUid.incrementAndGet();
//		actor = new RoomActor(id, type, roomExecutor);
		actor = SpringContextUtil.context.getBean(RoomActor.class, id, type, roomExecutor);
		actor.add(userId);
		
		RoomManager.add(userId, actor);
		queue.put(actor);
		return id;
	}
}
