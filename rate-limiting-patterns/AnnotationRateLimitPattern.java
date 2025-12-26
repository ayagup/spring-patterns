package com.example.ratelimit.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Annotation-based Rate Limiting Pattern
 * 
 * Custom annotation with AOP for rate limiting.
 */
@SpringBootApplication
public class AnnotationRateLimitPattern {

    public static void main(String[] args) {
        SpringApplication.run(AnnotationRateLimitPattern.class, args);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface RateLimit {
        int value() default 10;
    }

    @Aspect
    @Component
    public static class RateLimitAspect {

        private final ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();

        @Around("@annotation(rateLimit)")
        public Object enforce(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
            String key = joinPoint.getSignature().toString();
            int count = counts.merge(key, 1, Integer::sum);

            if (count > rateLimit.value()) {
                throw new RuntimeException("Rate limit exceeded");
            }

            return joinPoint.proceed();
        }
    }

    @RestController
    public static class LimitedController {

        @GetMapping("/api/limited")
        @RateLimit(value = 5)
        public String limited() {
            return "Success";
        }
    }
}
