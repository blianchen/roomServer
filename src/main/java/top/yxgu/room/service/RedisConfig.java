package top.yxgu.room.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;

@Configuration
public class RedisConfig {
	@Value("${redis.host}")
	private String host;
	@Value("${redis.port}")
	private int port;
	
	@Value("${redis.masterName}")  
    private String masterName;  
  
    @Value("${redis.sentinels}")  
    private Set<String> sentinels;  
  
    @Value("${redis.pool.max-idle}")  
    private int maxIdle;  
  
    @Value("${redis.pool.max-wait}")  
    private long maxWaitMillis;  
  
//    @Value("${redis.password}")  
//    private String password;
    
    @Bean(name="jedisPool")
    public Pool<Jedis> jedisPool() {
    	JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, host, port);
//        JedisSentinelPool jedisPool = new JedisSentinelPool(masterName, sentinels, jedisPoolConfig);
        return jedisPool;
    }
}
