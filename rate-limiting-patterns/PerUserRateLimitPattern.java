package com.example.ratelimit.peruser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.*;

/**
 * Per-User Rate Limiting Pattern
 * 
 * Different rate limits for different users or tiers.
 */
@SpringBootApplication
public class PerUserRateLimitPattern {

    public static void main(String[] args) {
        SpringApplication.run(PerUserRateLimitPattern.class, args);
    }

    public enum Tier {
        FREE(10), PREMIUM(100), ENTERPRISE(1000);

        private final int limit;

        Tier(int limit) {
            this.limit = limit;
        }

        public int getLimit() { return limit; }
    }

    public static class UserRateLimiter {
        private final ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();

        public boolean allow(String userId, Tier tier) {
            int count = counts.merge(userId, 1, Integer::sum);
            return count <= tier.getLimit();
        }
    }

    @RestController
    public static class UserController {

        private final UserRateLimiter limiter = new UserRateLimiter();

        @GetMapping("/api/user")
        public String userEndpoint(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Tier") Tier tier
        ) {
            if (limiter.allow(userId, tier)) {
                return "Success";
            }
            return "Rate limit exceeded for tier: " + tier;
        }
    }
}
