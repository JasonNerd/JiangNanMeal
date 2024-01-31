package com.rain.reggie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class Reggie002ApplicationTests {

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        redisTemplate.opsForValue().set("name", "ket");
        redisTemplate.delete("name");
    }

}
