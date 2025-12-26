package com.example.kotlin.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 KOTLIN EXTENSION FUNCTIONS PATTERN 💡
 * =========================================
 * 
 * Extension functions for Spring Framework APIs providing
 * Kotlin-idiomatic interfaces and reducing boilerplate.
 * 
 * 🎯 KEY FEATURES:
 * - RestTemplate extensions (exchange, getForEntity)
 * - WebClient extensions (retrieve, awaitBody)
 * - Repository extensions (findById, save)
 * - ResponseEntity builders (ok, created)
 * - Type-safe inline reified functions
 * 
 * @author Spring Patterns  
 * @since 2024-01-20
 */
@SpringBootApplication
public class KotlinExtensionFunctionsPattern {
    public static void main(String[] args) {
        SpringApplication.run(KotlinExtensionFunctionsPattern.class, args);
    }
}

@Service
class ExtensionFunctionsService {
    public List<String> getExtensions() {
        return Arrays.asList(
            "RestTemplate.getForObject<T>(url) - Reified type parameter",
            "WebClient.awaitBody<T>() - Suspend extension",
            "repository.findByIdOrNull(id) - Nullable extension",
            "ResponseEntity.ok { body } - Builder DSL",
            "Flow<T>.asFlux() - Flow to Flux conversion",
            "Mono<T>.awaitSingle() - Mono to coroutine",
            "ServerResponse.bodyValueAndAwait(value) - Suspend response"
        );
    }
}

@RestController
@RequestMapping("/api/extension-functions")
class ExtensionFunctionsController {
    private final ExtensionFunctionsService service;
    
    public ExtensionFunctionsController(ExtensionFunctionsService service) {
        this.service = service;
    }
    
    @GetMapping("/list")
    public List<String> getExtensions() {
        return service.getExtensions();
    }
}
