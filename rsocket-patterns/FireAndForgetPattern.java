package com.example.rsocket.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 RSOCKET FIRE-AND-FORGET PATTERN 💡
 * ======================================
 * 
 * RSocket fire-and-forget is a one-way communication pattern
 * where client sends a message without expecting a response.
 * 
 * 🎯 KEY FEATURES:
 * - One-way messaging
 * - No response expected
 * - Fire-and-forget semantics
 * - Efficient for notifications
 * - Event publishing
 * - Logging/metrics
 * 
 * 📦 RSOCKET CONTROLLER:
 * ======================
 * 
 * @Controller
 * class RSocketController {
 *     @MessageMapping("fire-and-forget")
 *     fun handleFireAndForget(message: Message): Mono<Void> {
 *         logger.info("Received: $message")
 *         return Mono.empty()
 *     }
 * }
 * 
 * 🔧 RSOCKET CLIENT:
 * ==================
 * 
 * fun sendFireAndForget(message: Message): Mono<Void> {
 *     return requester
 *         .route("fire-and-forget")
 *         .data(message)
 *         .send()  // No response expected
 * }
 * 
 * @author Spring Patterns
 * @since 2024-01-20
 */
@SpringBootApplication
public class FireAndForgetPattern {
    public static void main(String[] args) {
        SpringApplication.run(FireAndForgetPattern.class, args);
    }
}

@Service
class FireAndForgetService {
    public Map<String, Object> getPatternInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("pattern", "Fire-and-Forget");
        info.put("type", "One-way");
        info.put("cardinality", "1:0 (one request, no response)");
        info.put("use_cases", Arrays.asList(
            "Event notifications",
            "Logging",
            "Metrics collection",
            "Telemetry data",
            "Audit logs"
        ));
        return info;
    }
}

@RestController
@RequestMapping("/api/rsocket/fire-and-forget")
class FireAndForgetController {
    private final FireAndForgetService service;
    
    public FireAndForgetController(FireAndForgetService service) {
        this.service = service;
    }
    
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        return service.getPatternInfo();
    }
}
