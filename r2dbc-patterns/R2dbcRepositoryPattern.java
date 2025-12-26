package com.example.r2dbc.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * R2DBC Repository Pattern
 * 
 * Demonstrates ReactiveCrudRepository for reactive database access.
 * Spring Data R2DBC provides repository abstraction with reactive types.
 * 
 * Key Features:
 * - Reactive repository interface
 * - Automatic query derivation
 * - Custom query methods
 * - Pagination support
 * - Non-blocking operations
 */
@SpringBootApplication
public class R2dbcRepositoryPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(R2dbcRepositoryPattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== R2DBC Repository Pattern ===\n");
        System.out.println("ReactiveCrudRepository provides:");
        System.out.println("- save(entity) → Mono<Entity>");
        System.out.println("- findById(id) → Mono<Entity>");
        System.out.println("- findAll() → Flux<Entity>");
        System.out.println("- deleteById(id) → Mono<Void>");
        System.out.println("- Custom query methods with reactive return types");
    }

    @Repository
    interface UserRepository extends ReactiveCrudRepository<User, Long> {
        Flux<User> findByName(String name);
        Mono<User> findByEmail(String email);
        Flux<User> findByNameContaining(String keyword);
    }

    @Table("users")
    static class User {
        @Id
        private Long id;
        private String name;
        private String email;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
