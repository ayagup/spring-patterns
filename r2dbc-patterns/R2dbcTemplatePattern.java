package com.example.r2dbc.patterns;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * R2DBC Template Pattern
 * 
 * Demonstrates R2dbcEntityTemplate for reactive database operations.
 * Provides a high-level API for CRUD operations with reactive types.
 * 
 * Key Features:
 * - Reactive database access
 * - Entity mapping support
 * - Query building
 * - Transaction support
 * - Non-blocking I/O
 */
@SpringBootApplication
public class R2dbcTemplatePattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(R2dbcTemplatePattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== R2DBC Template Pattern ===\n");
        System.out.println("R2dbcEntityTemplate provides:");
        System.out.println("- Reactive CRUD operations");
        System.out.println("- Entity-based queries");
        System.out.println("- Type-safe database access");
        System.out.println("- Non-blocking I/O with Reactor types (Mono/Flux)");
    }

    @Configuration
    static class R2dbcConfig {
        @Bean
        public R2dbcEntityTemplate r2dbcEntityTemplate(ConnectionFactory connectionFactory) {
            return new R2dbcEntityTemplate(connectionFactory);
        }
    }

    static class User {
        private Long id;
        private String name;
        private String email;

        public User() {}
        public User(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
