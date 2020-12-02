package com.danbro.redisdistrubutedlockdemo;

import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Classname JedisUtil
 * @Description TODO
 * @Date 2020/12/2 11:08
 * @Author Danrbo
 */
public class RedisUtils {

    @Value("${spring.redis.host}")
    private static String redisHost;
    @Value("${spring.redis.port}")
    private static Integer redisPort;

    private final static JedisPool jedisPool;

    static {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMaxTotal(20);
        jedisPool = new JedisPool(jedisPoolConfig, redisHost, redisPort);
    }

    public static Jedis getJedis() throws Exception {
        if (jedisPool != null) {
            return jedisPool.getResource();
        }
        throw new Exception("jedisPool is not ok!");
    }
}
