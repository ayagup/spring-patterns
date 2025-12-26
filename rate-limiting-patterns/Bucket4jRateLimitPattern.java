package com.example.ratelimit.bucket4j;

import io.github.bucket4j.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * Bucket4j Rate Limiting Pattern
 * 
 * Token bucket algorithm using Bucket4j library.
 * Dependencies: bucket4j-core
 */
@SpringBootApplication
public class Bucket4jRateLimitPattern {

    public static void main(String[] args) {
        SpringApplication.run(Bucket4jRateLimitPattern.class, args);
    }

    @RestController
    public static class RateLimitController {

        private final Bucket bucket;

        public RateLimitController() {
            Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
            this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
        }

        @GetMapping("/api/limited")
        public String limited() {
            if (bucket.tryConsume(1)) {
                return "Success";
            }
            return "Rate limit exceeded";
        }
    }
}
