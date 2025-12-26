package com.example.ratelimit.interceptor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.*;
import java.util.concurrent.*;

/**
 * Interceptor-based Rate Limiting Pattern
 * 
 * Uses Spring MVC interceptor for rate limiting.
 */
@SpringBootApplication
public class InterceptorRateLimitPattern {

    public static void main(String[] args) {
        SpringApplication.run(InterceptorRateLimitPattern.class, args);
    }

    @Component
    public static class RateLimitInterceptor implements HandlerInterceptor {

        private final ConcurrentHashMap<String, Long> requestCounts = new ConcurrentHashMap<>();
        private static final int MAX_REQUESTS = 10;
        private static final long TIME_WINDOW = 60000; // 1 minute

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                                Object handler) throws Exception {
            String clientIP = request.getRemoteAddr();
            long currentTime = System.currentTimeMillis();
            String key = clientIP + ":" + (currentTime / TIME_WINDOW);

            long count = requestCounts.merge(key, 1L, Long::sum);

            if (count > MAX_REQUESTS) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate limit exceeded");
                return false;
            }

            return true;
        }
    }
}
