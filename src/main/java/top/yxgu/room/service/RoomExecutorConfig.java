package top.yxgu.room.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import top.yxgu.room.action.ActorSimulator;

@Configuration
public class RoomExecutorConfig {
	private static final Logger log = LoggerFactory.getLogger(RoomExecutorConfig.class);
	
	@Bean
	public Executor roomExecutor() {
		int corePoolSize = Runtime.getRuntime().availableProcessors();
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(64 * corePoolSize);
		RejectedExecutionHandler rejectedHandler = new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				log.warn("执行队列已满，cont="+executor.getQueue().size());
				//TODO 停止接收客户端，踢掉部分，调低支持的房间数量并更新到大厅
				if (!executor.isShutdown()) {
	                r.run();
	                ((ActorSimulator)r).setInQueue(false);
	            }
			}
		};
		ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, corePoolSize * 2, 60, 
													TimeUnit.SECONDS, queue, rejectedHandler);
		return pool;
	}

}
