package com.danbro.redisdistrubutedlockdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Value("${server.port}")
    private String port;


    @GetMapping("/buy")
    public String buy() {
        String result = redisTemplate.opsForValue().get("goods:101");
        int num = result == null ? 0 : Integer.parseInt(result);
        if (num > 0) {
            num -= 1;
            redisTemplate.opsForValue().set("goods:101", Integer.toString(num));
            System.out.println(String.format("成功买到商品：%s,提供服务的端口：%s", num, port));
            return String.format("成功买到商品：%s,提供服务的端口:%s", num, port);
        } else {
            System.out.println(String.format("商品已经卖完,提供服务的端口：%s", num, port));
            return String.format("商品已经卖完,提供服务的端口：%s", num, port);
        }
    }
}
