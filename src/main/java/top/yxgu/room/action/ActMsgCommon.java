package top.yxgu.room.action;

import java.util.HashMap;
import java.util.Map;

public class ActMsgCommon implements ActMsg {
	public static final int REMOVE_USER = 101;
	public static final int POND_FISH = 102;
	
	public int action;
	public Map<String, Object> msg;
	
	public ActMsgCommon(int action) {
		this.action = action;
		this.msg = new HashMap<>();
	}
	
	@Override
	public int getAction() {
		return action;
	}

	@Override
	public Map<String, Object> getMessage() {
		return msg;
	}

	public void addData(String key, Object v) {
		msg.put(key, v);
	}
	
	public Object getData(String key) {
		return msg.get(key);
	}
}
