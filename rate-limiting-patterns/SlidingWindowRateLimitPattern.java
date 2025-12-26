package com.example.ratelimit.slidingwindow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding Window Rate Limiting Pattern
 * 
 * Sliding window algorithm for accurate rate limiting.
 */
@SpringBootApplication
public class SlidingWindowRateLimitPattern {

    public static void main(String[] args) {
        SpringApplication.run(SlidingWindowRateLimitPattern.class, args);
    }

    public static class SlidingWindowLimiter {
        private final Map<String, Queue<Long>> windows = new ConcurrentHashMap<>();
        private final int limit;
        private final long windowMs;

        public SlidingWindowLimiter(int limit, long windowMs) {
            this.limit = limit;
            this.windowMs = windowMs;
        }

        public boolean allowRequest(String key) {
            long now = System.currentTimeMillis();
            Queue<Long> window = windows.computeIfAbsent(key, k -> new LinkedList<>());

            // Remove old timestamps
            while (!window.isEmpty() && now - window.peek() > windowMs) {
                window.poll();
            }

            if (window.size() < limit) {
                window.offer(now);
                return true;
            }

            return false;
        }
    }

    @RestController
    public static class SlidingController {

        private final SlidingWindowLimiter limiter = new SlidingWindowLimiter(10, 60000);

        @GetMapping("/api/sliding")
        public String limited() {
            if (limiter.allowRequest("default")) {
                return "Success";
            }
            return "Rate limit exceeded";
        }
    }
}
