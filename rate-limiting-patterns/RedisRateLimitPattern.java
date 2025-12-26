package com.example.ratelimit.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * Redis-based Rate Limiting Pattern
 * 
 * Distributed rate limiting using Redis.
 * Dependencies: spring-boot-starter-data-redis
 */
@SpringBootApplication
public class RedisRateLimitPattern {

    public static void main(String[] args) {
        SpringApplication.run(RedisRateLimitPattern.class, args);
    }

    @RestController
    public static class RedisController {

        private final RedisTemplate<String, String> redisTemplate;

        public RedisController(RedisTemplate<String, String> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        @GetMapping("/api/redis-limit")
        public String limited(@RequestHeader("X-User-ID") String userId) {
            String key = "rate_limit:" + userId;
            Long count = redisTemplate.opsForValue().increment(key);

            if (count == 1) {
                redisTemplate.expire(key, 1, TimeUnit.MINUTES);
            }

            if (count > 10) {
                return "Rate limit exceeded";
            }

            return "Success";
        }
    }
}
