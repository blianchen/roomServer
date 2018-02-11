package top.yxgu.room.action;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ActorSimulator implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(ActorSimulator.class);
	
	public static final int maxTimeInOneRun = 100; //ms
	
	public boolean isDel = false;
	
	private int id;
	
	private BlockingQueue<ActMsg> queue;
	private Executor executor;
	
	private volatile boolean isInQueue = false;
	private byte[] lock = new byte[0];
	
	public ActorSimulator(int id, Executor executor) {
		this.id = id;
		this.executor = executor;
		this.queue = new LinkedBlockingQueue<>(512);
	}
	
	public void addAction(ActMsg am) {
		try {
			queue.put(am);		//阻塞
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		addToExecutor();
	}
	
	public void addToExecutor() {
		if (!isInQueue) {
			synchronized (lock) {  //同时只能有一个在执行队列，以保证单线程执行
				if (!isInQueue) {
					isInQueue = true;
					this.executor.execute(this);
				}
			}
		}
	}

	@Override
	public void run() {
		long time = System.currentTimeMillis();
		ActMsg am;
		try {
			while ((am = queue.poll()) != null) { //不阻塞取
				this.processAction(am);
//				System.out.println("**********"+(System.currentTimeMillis() - time));
				if (System.currentTimeMillis() - time > maxTimeInOneRun) {
					break;
				}
			}
		} catch (Exception e) {
			isInQueue = false;
			log.error("Err->(id=:"+id+")"+e.toString());
		}
		
		if (!queue.isEmpty()) { //继续加到执行队列
			addToExecutor();
		} else {
			isInQueue = false;
		}
	}
	
	protected abstract void processAction(ActMsg am);
	
	public boolean isInQueue() {
		return isInQueue;
	}

	public void setInQueue(boolean isInQueue) {
		this.isInQueue = isInQueue;
	}

	public Executor getExecutor() {
		return executor;
	}
	
	public int getId() {
		return id;
	}
}
