package com.danbro.redisdistrubutedlockdemo;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class RedisController {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private Redisson redisson;

    private final static String LUA_DELETE_SUCCESS = "1";

    private final static String REDIS_LOCK = "goods:101";

    @Value("${server.port}")
    private String port;

    private final static String DELETE_SCRIPT = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
            "then\n" +
            "    return redis.call(\"del\",KEYS[1])\n" +
            "else\n" +
            "    return 0\n" +
            "end";


    @GetMapping("/buy")
    public String buy() {
        UUID uuid = UUID.randomUUID();
        RLock lock = redisson.getLock(REDIS_LOCK);
        lock.lock();
        try {
            String result = redisTemplate.opsForValue().get(REDIS_LOCK);
            int num = result == null ? 0 : Integer.parseInt(result);
            if (num > 0) {
                num -= 1;
                redisTemplate.opsForValue().set("goods:101", Integer.toString(num));
                System.out.printf("成功买到商品：%s,提供服务的端口：%s%n", num, port);
                return String.format("成功买到商品：%s,提供服务的端口:%s", num, port);
            } else {
                System.out.printf("商品已经卖完,提供服务的端口：%s%n", num, port);
                return String.format("商品已经卖完,提供服务的端口：%s", num, port);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return "失败";


    }
}
