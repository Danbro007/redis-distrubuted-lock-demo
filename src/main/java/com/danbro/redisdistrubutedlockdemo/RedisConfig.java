package com.danbro.redisdistrubutedlockdemo;

import java.io.Serializable;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {


    private static String REDIS_HOST = "192.168.0.119";

    private static String REDIS_PORT = "6379";

    private final static String REDIS_ADDRESS = String.format("redis://%s:%s", REDIS_HOST, REDIS_PORT);

    @Bean
    public RedisTemplate<String, Serializable> getRedis(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public Redisson getRedisson() {
        Config config = new Config();
        config.useSingleServer().setAddress(REDIS_ADDRESS).setDatabase(0);
        return (Redisson) Redisson.create(config);
    }

}
