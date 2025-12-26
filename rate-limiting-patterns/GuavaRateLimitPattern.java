package com.example.ratelimit.guava;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

/**
 * Guava Rate Limiting Pattern
 * 
 * Uses Guava's RateLimiter for rate limiting.
 * Dependencies: guava
 */
@SpringBootApplication
public class GuavaRateLimitPattern {

    public static void main(String[] args) {
        SpringApplication.run(GuavaRateLimitPattern.class, args);
    }

    @RestController
    public static class GuavaController {

        private final RateLimiter rateLimiter = RateLimiter.create(10.0); // 10 requests per second

        @GetMapping("/api/guava-limit")
        public String limited() {
            if (rateLimiter.tryAcquire()) {
                return "Success";
            }
            return "Rate limit exceeded";
        }
    }
}
