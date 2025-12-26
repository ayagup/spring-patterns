package com.example.httpclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

/**
 * RestTemplate Pattern
 * 
 * Demonstrates using Spring's RestTemplate for synchronous HTTP client operations.
 * RestTemplate provides:
 * - Synchronous HTTP requests
 * - Request/response handling
 * - Error handling
 * - URI templates
 * - Message conversion
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class RestTemplatePattern {

    public static void main(String[] args) {
        SpringApplication.run(RestTemplatePattern.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Service
    public static class UserClient {
        
        private final RestTemplate restTemplate;
        private final String baseUrl = "https://api.example.com";

        public UserClient(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        // GET request
        public User getUser(Long id) {
            String url = baseUrl + "/users/{id}";
            return restTemplate.getForObject(url, User.class, id);
        }

        // GET with ResponseEntity
        public ResponseEntity<User> getUserWithHeaders(Long id) {
            String url = baseUrl + "/users/{id}";
            return restTemplate.getForEntity(url, User.class, id);
        }

        // POST request
        public User createUser(User user) {
            String url = baseUrl + "/users";
            return restTemplate.postForObject(url, user, User.class);
        }

        // POST with ResponseEntity
        public ResponseEntity<User> createUserWithLocation(User user) {
            String url = baseUrl + "/users";
            return restTemplate.postForEntity(url, user, User.class);
        }

        // PUT request
        public void updateUser(Long id, User user) {
            String url = baseUrl + "/users/{id}";
            restTemplate.put(url, user, id);
        }

        // DELETE request
        public void deleteUser(Long id) {
            String url = baseUrl + "/users/{id}";
            restTemplate.delete(url, id);
        }

        // GET with custom headers
        public User getUserWithAuth(Long id, String token) {
            String url = baseUrl + "/users/{id}";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<User> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, User.class, id);
            
            return response.getBody();
        }

        // POST with custom headers and body
        public User createUserWithAuth(User user, String token) {
            String url = baseUrl + "/users";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            HttpEntity<User> entity = new HttpEntity<>(user, headers);
            
            ResponseEntity<User> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, User.class);
            
            return response.getBody();
        }

        // GET list
        public List<User> getAllUsers() {
            String url = baseUrl + "/users";
            User[] users = restTemplate.getForObject(url, User[].class);
            return Arrays.asList(users != null ? users : new User[0]);
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
