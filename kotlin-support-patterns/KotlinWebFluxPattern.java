package com.example.kotlin.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 KOTLIN WEBFLUX PATTERN 💡
 * =============================
 * 
 * Kotlin WebFlux with coroutines, Flow, and functional routing.
 * Combines WebFlux reactive capabilities with Kotlin's coroutine syntax.
 * 
 * 🎯 KEY FEATURES:
 * - Suspend @GetMapping/@PostMapping
 * - Flow<T> instead of Flux<T>
 * - coRouter for functional routes
 * - awaitBody() for request body
 * - bodyValueAndAwait() for response
 * - Coroutine exception handlers
 * 
 * Example (Kotlin):
 * -----------------
 * @RestController
 * class UserController(val service: UserService) {
 *     @GetMapping("/users/{id}")
 *     suspend fun findById(@PathVariable id: String): User {
 *         return service.findById(id)
 *     }
 *     
 *     @GetMapping("/users/stream")
 *     fun streamUsers(): Flow<User> {
 *         return service.streamUsers()
 *     }
 * }
 * 
 * @author Spring Patterns
 * @since 2024-01-20
 */
@SpringBootApplication
public class KotlinWebFluxPattern {
    public static void main(String[] args) {
        SpringApplication.run(KotlinWebFluxPattern.class, args);
    }
}

@Service
class KotlinWebFluxService {
    public List<String> getWebFluxFeatures() {
        return Arrays.asList(
            "suspend fun endpoints - Sequential async code",
            "Flow<T> streaming - Kotlin's reactive streams",
            "coRouter { } - Coroutine router DSL",
            "awaitBody<User>() - Suspend request body",
            "bodyValueAndAwait(user) - Suspend response",
            "WebClient.awaitBody<T>() - Suspend HTTP client",
            "R2DBC with suspend functions - Reactive SQL",
            "Exception handling with try-catch"
        );
    }
}

@RestController
@RequestMapping("/api/kotlin-webflux")
class KotlinWebFluxController {
    private final KotlinWebFluxService service;
    
    public KotlinWebFluxController(KotlinWebFluxService service) {
        this.service = service;
    }
    
    @GetMapping("/features")
    public List<String> getFeatures() {
        return service.getWebFluxFeatures();
    }
}
