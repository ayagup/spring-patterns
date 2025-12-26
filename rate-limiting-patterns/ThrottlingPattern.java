package com.example.ratelimit.throttling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.*;

/**
 * Request Throttling Pattern
 * 
 * Delays requests instead of rejecting them.
 */
@SpringBootApplication
public class ThrottlingPattern {

    public static void main(String[] args) {
        SpringApplication.run(ThrottlingPattern.class, args);
    }

    public static class RequestThrottler {
        private final Semaphore semaphore;

        public RequestThrottler(int permits) {
            this.semaphore = new Semaphore(permits);
        }

        public <T> T throttle(Callable<T> task) throws Exception {
            semaphore.acquire();
            try {
                return task.call();
            } finally {
                semaphore.release();
            }
        }
    }

    @RestController
    public static class ThrottledController {

        private final RequestThrottler throttler = new RequestThrottler(5);

        @GetMapping("/api/throttled")
        public String throttled() throws Exception {
            return throttler.throttle(() -> {
                Thread.sleep(1000); // Simulate work
                return "Success";
            });
        }
    }
}
