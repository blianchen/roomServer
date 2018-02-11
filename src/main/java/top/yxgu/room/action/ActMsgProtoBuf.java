package top.yxgu.room.action;

import com.google.protobuf.Message;

public class ActMsgProtoBuf implements ActMsg {
	public int action;
	public Message msg;
	
	public ActMsgProtoBuf(int action, Message msg) {
		this.action = action;
		this.msg = msg;
	}
	
	public int getAction() {
		return action;
	}
	
	public Message getMessage() {
		return msg;
	}
}
