package com.danbro.redisdistrubutedlockdemo;

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

    private final static String REDIS_LOCK = "goods:101";

    @Value("${server.port}")
    private String port;

    private final static String LUA_DELETE_SUCCESS = "1";

    private final static String DELETE_SCRIPT = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
            "then\n" +
            "    return redis.call(\"del\",KEYS[1])\n" +
            "else\n" +
            "    return 0\n" +
            "end";


    @GetMapping("/buy")
    public String buy() throws Exception {
        UUID uuid = UUID.randomUUID();
        // 先判断是否成功获取到钥匙，获取成功就设置过期时间，value 为一个随机字符串防止被其他线程删除。
        if (!redisTemplate.opsForValue().setIfAbsent(REDIS_LOCK, uuid.toString(), 10L, TimeUnit.SECONDS)) {
            return "失败";
        }
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
            // 判断是不是当前线程设置的锁，通过 UUID 来判断。
            if (Objects.requireNonNull(redisTemplate.opsForValue().get(REDIS_LOCK)).equalsIgnoreCase(uuid.toString())) {
                try (Jedis jedis = RedisUtils.getJedis()) {
                    // 使用 Lua 脚本删除分布式锁
                    Object result = jedis.eval(DELETE_SCRIPT, Collections.singletonList(REDIS_LOCK), Collections.singletonList(uuid.toString()));
                    if (LUA_DELETE_SUCCESS.equals(result)) {
                        System.out.println("删除成功！");
                    } else {
                        System.out.println("删除失败！");
                    }
                }
            }

        }
        return "失败";


    }
}
