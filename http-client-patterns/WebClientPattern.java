package com.example.httpclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * WebClient Pattern
 * 
 * Demonstrates Spring WebClient for reactive HTTP client operations.
 * Supports both synchronous and asynchronous requests.
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class WebClientPattern {

    public static void main(String[] args) {
        SpringApplication.run(WebClientPattern.class, args);
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.example.com")
                .defaultHeader("User-Agent", "Spring WebClient")
                .build();
    }

    @Service
    public static class UserClient {
        
        private final WebClient webClient;

        public UserClient(WebClient webClient) {
            this.webClient = webClient;
        }

        // GET - Reactive
        public Mono<User> getUser(Long id) {
            return webClient.get()
                    .uri("/users/{id}", id)
                    .retrieve()
                    .bodyToMono(User.class);
        }

        // GET - Blocking
        public User getUserBlocking(Long id) {
            return webClient.get()
                    .uri("/users/{id}", id)
                    .retrieve()
                    .bodyToMono(User.class)
                    .block(Duration.ofSeconds(5));
        }

        // GET List
        public Flux<User> getAllUsers() {
            return webClient.get()
                    .uri("/users")
                    .retrieve()
                    .bodyToFlux(User.class);
        }

        // POST
        public Mono<User> createUser(User user) {
            return webClient.post()
                    .uri("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(user)
                    .retrieve()
                    .bodyToMono(User.class);
        }

        // PUT
        public Mono<User> updateUser(Long id, User user) {
            return webClient.put()
                    .uri("/users/{id}", id)
                    .bodyValue(user)
                    .retrieve()
                    .bodyToMono(User.class);
        }

        // DELETE
        public Mono<Void> deleteUser(Long id) {
            return webClient.delete()
                    .uri("/users/{id}", id)
                    .retrieve()
                    .bodyToMono(Void.class);
        }

        // With custom headers
        public Mono<User> getUserWithAuth(Long id, String token) {
            return webClient.get()
                    .uri("/users/{id}", id)
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(User.class);
        }
    }

    public static class User {
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
