package com.example.rsocket.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 RSOCKET REQUEST-RESPONSE PATTERN 💡
 * =======================================
 * 
 * RSocket request-response is a bidirectional communication pattern
 * where client sends a request and waits for exactly one response.
 * 
 * 🎯 KEY FEATURES:
 * - Bidirectional communication
 * - Single request → single response
 * - Backpressure support
 * - Binary protocol (efficient)
 * - Connection multiplexing
 * - Resume capability
 * 
 * 📦 RSOCKET CONTROLLER (Spring):
 * ===============================
 * 
 * @Controller
 * class RSocketController {
 *     @MessageMapping("request-response")
 *     fun handleRequest(request: Request): Mono<Response> {
 *         return Mono.just(Response(request.data))
 *     }
 * }
 * 
 * 🔧 RSOCKET CLIENT:
 * ==================
 * 
 * @Service
 * class RSocketClient(
 *     private val requester: RSocketRequester
 * ) {
 *     fun sendRequest(data: String): Mono<Response> {
 *         return requester
 *             .route("request-response")
 *             .data(Request(data))
 *             .retrieveMono(Response::class.java)
 *     }
 * }
 * 
 * @author Spring Patterns
 * @since 2024-01-20
 */
@SpringBootApplication
public class RequestResponsePattern {
    public static void main(String[] args) {
        SpringApplication.run(RequestResponsePattern.class, args);
    }
}

@Service
class RequestResponseService {
    public Map<String, Object> getPatternInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("pattern", "Request-Response");
        info.put("type", "Bidirectional");
        info.put("cardinality", "1:1 (one request, one response)");
        info.put("protocol", "RSocket binary protocol");
        info.put("features", Arrays.asList(
            "Backpressure support",
            "Connection multiplexing",
            "Resume capability",
            "Metadata support",
            "Reactive streams"
        ));
        return info;
    }
}

@RestController
@RequestMapping("/api/rsocket/request-response")
class RequestResponseController {
    private final RequestResponseService service;
    
    public RequestResponseController(RequestResponseService service) {
        this.service = service;
    }
    
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        return service.getPatternInfo();
    }
}
