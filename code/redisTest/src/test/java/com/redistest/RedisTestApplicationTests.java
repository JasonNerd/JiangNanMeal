package com.redistest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class RedisTestApplicationTests {
	@Autowired
	RedisTemplate redisTemplate;

	@Test
	void testString() {
		redisTemplate.opsForValue().set("time", "noon");
		String now = (String) redisTemplate.opsForValue().get("time");
		System.out.println(now);
	}

	@Test
	void testHash(){
		redisTemplate.opsForHash().put("java", "version", 17);
		redisTemplate.opsForHash().put("java", "alg", "trace back");
		redisTemplate.opsForHash().put("java", "salary", 100);
		redisTemplate.opsForHash().put("java", "concurrency", true);
		Integer version = (Integer) redisTemplate.opsForHash().get("java", "version");
		System.out.println(version);
	}
}
