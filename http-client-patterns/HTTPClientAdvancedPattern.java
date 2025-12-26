package com.example.httpclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

/**
 * HTTP Client Advanced Patterns
 * 
 * Combined pattern demonstrating:
 * - HTTP Interface (Spring 6 declarative clients)
 * - HTTP Request Factory (connection configuration)
 * - Client HTTP Request Interceptor (request/response modification)
 * - Error Handler (custom error handling)
 * - URI Builder (dynamic URL construction)
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class HTTPClientAdvancedPattern {

    public static void main(String[] args) {
        SpringApplication.run(HTTPClientAdvancedPattern.class, args);
    }

    // =========================================================================
    // HTTP INTERFACE (Spring 6+)
    // =========================================================================

    /**
     * Declarative HTTP interface - Spring 6+
     */
    public interface UserServiceInterface {
        User getUser(Long id);
        User createUser(User user);
        void updateUser(Long id, User user);
        void deleteUser(Long id);
    }

    // =========================================================================
    // REQUEST INTERCEPTOR
    // =========================================================================

    /**
     * Logging interceptor
     */
    public static class LoggingInterceptor implements ClientHttpRequestInterceptor {
        
        @Override
        public ClientHttpResponse intercept(
                org.springframework.http.HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution) throws IOException {
            
            System.out.println("=== Request ===");
            System.out.println("Method: " + request.getMethod());
            System.out.println("URI: " + request.getURI());
            System.out.println("Headers: " + request.getHeaders());
            
            ClientHttpResponse response = execution.execute(request, body);
            
            System.out.println("=== Response ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Headers: " + response.getHeaders());
            
            return response;
        }
    }

    /**
     * Authentication interceptor
     */
    public static class AuthInterceptor implements ClientHttpRequestInterceptor {
        
        private final String token;

        public AuthInterceptor(String token) {
            this.token = token;
        }

        @Override
        public ClientHttpResponse intercept(
                org.springframework.http.HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution) throws IOException {
            
            request.getHeaders().setBearerAuth(token);
            System.out.println("Added authorization header");
            
            return execution.execute(request, body);
        }
    }

    /**
     * Retry interceptor
     */
    public static class RetryInterceptor implements ClientHttpRequestInterceptor {
        
        private final int maxRetries;

        public RetryInterceptor(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        @Override
        public ClientHttpResponse intercept(
                org.springframework.http.HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution) throws IOException {
            
            IOException lastException = null;
            
            for (int i = 0; i <= maxRetries; i++) {
                try {
                    if (i > 0) {
                        System.out.println("Retry attempt " + i + " of " + maxRetries);
                    }
                    return execution.execute(request, body);
                } catch (IOException e) {
                    lastException = e;
                    if (i < maxRetries) {
                        try {
                            Thread.sleep(1000 * (i + 1)); // Exponential backoff
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw e;
                        }
                    }
                }
            }
            
            throw lastException;
        }
    }

    // =========================================================================
    // ERROR HANDLER
    // =========================================================================

    /**
     * Custom error handler
     */
    public static class CustomErrorHandler implements ResponseErrorHandler {
        
        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            HttpStatus.Series series = response.getStatusCode().series();
            return series == HttpStatus.Series.CLIENT_ERROR || 
                   series == HttpStatus.Series.SERVER_ERROR;
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            HttpStatus statusCode = response.getStatusCode();
            
            System.out.println("Error occurred: " + statusCode);
            
            switch (statusCode.series()) {
                case CLIENT_ERROR:
                    if (statusCode == HttpStatus.NOT_FOUND) {
                        throw new ResourceNotFoundException("Resource not found");
                    } else if (statusCode == HttpStatus.BAD_REQUEST) {
                        throw new BadRequestException("Bad request");
                    }
                    break;
                    
                case SERVER_ERROR:
                    throw new ServerErrorException("Server error: " + statusCode);
                    
                default:
                    throw new IOException("Unknown error: " + statusCode);
            }
        }
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }

    public static class ServerErrorException extends RuntimeException {
        public ServerErrorException(String message) {
            super(message);
        }
    }

    // =========================================================================
    // URI BUILDER
    // =========================================================================

    @Service
    public static class URIBuilderService {
        
        /**
         * Build URI with path variables
         */
        public URI buildUserUri(String baseUrl, Long userId) {
            return org.springframework.web.util.UriComponentsBuilder
                    .fromHttpUrl(baseUrl)
                    .path("/users/{id}")
                    .buildAndExpand(userId)
                    .toUri();
        }

        /**
         * Build URI with query parameters
         */
        public URI buildSearchUri(String baseUrl, String query, int page, int size) {
            return org.springframework.web.util.UriComponentsBuilder
                    .fromHttpUrl(baseUrl)
                    .path("/users/search")
                    .queryParam("q", query)
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .build()
                    .toUri();
        }

        /**
         * Build URI with multiple filters
         */
        public URI buildFilteredUri(String baseUrl, java.util.Map<String, Object> filters) {
            org.springframework.web.util.UriComponentsBuilder builder = 
                    org.springframework.web.util.UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/users");
            
            filters.forEach((key, value) -> {
                if (value != null) {
                    builder.queryParam(key, value);
                }
            });
            
            return builder.build().toUri();
        }

        /**
         * Build URI with encoded path segments
         */
        public URI buildEncodedUri(String baseUrl, String segment1, String segment2) {
            return org.springframework.web.util.UriComponentsBuilder
                    .fromHttpUrl(baseUrl)
                    .pathSegment(segment1, segment2)
                    .build()
                    .encode()
                    .toUri();
        }
    }

    // =========================================================================
    // HTTP REQUEST FACTORY
    // =========================================================================

    @Bean
    public org.springframework.http.client.ClientHttpRequestFactory requestFactory() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        
        factory.setConnectTimeout(5000); // 5 seconds
        factory.setReadTimeout(5000);    // 5 seconds
        factory.setBufferRequestBody(true);
        
        return factory;
    }

    // =========================================================================
    // REACTIVE WEB CLIENT (Alternative)
    // =========================================================================

    @Bean
    public org.springframework.web.reactive.function.client.WebClient reactiveWebClient() {
        return org.springframework.web.reactive.function.client.WebClient.builder()
                .baseUrl("https://api.example.com")
                .defaultHeader("User-Agent", "Reactive Client")
                .filter((request, next) -> {
                    System.out.println("Reactive Filter: " + request.method() + " " + request.url());
                    return next.exchange(request);
                })
                .build();
    }

    // =========================================================================
    // HTTP MESSAGE CONVERTER
    // =========================================================================

    /**
     * Custom message converter example
     */
    public static class CustomMessageConverter 
            extends org.springframework.http.converter.json.MappingJackson2HttpMessageConverter {
        
        public CustomMessageConverter() {
            super();
            System.out.println("Custom message converter initialized");
        }
    }

    // =========================================================================
    // COMPLETE REST TEMPLATE CONFIGURATION
    // =========================================================================

    @Bean
    public RestTemplate restTemplate(
            org.springframework.http.client.ClientHttpRequestFactory requestFactory) {
        
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        
        // Add interceptors
        restTemplate.getInterceptors().add(new LoggingInterceptor());
        restTemplate.getInterceptors().add(new AuthInterceptor("sample-token"));
        restTemplate.getInterceptors().add(new RetryInterceptor(3));
        
        // Set error handler
        restTemplate.setErrorHandler(new CustomErrorHandler());
        
        // Add custom message converter
        restTemplate.getMessageConverters().add(new CustomMessageConverter());
        
        return restTemplate;
    }

    // Domain Classes

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

/**
 * DOCUMENTATION
 * 
 * This combined pattern covers:
 * 
 * 1. HTTP Interface (Spring 6):
 *    - Declarative HTTP clients
 *    - @HttpExchange annotations
 *    - Automatic implementation
 * 
 * 2. HTTP Request Factory:
 *    - Connection pooling
 *    - Timeout configuration
 *    - Request buffering
 * 
 * 3. Client HTTP Request Interceptor:
 *    - Request/response modification
 *    - Logging
 *    - Authentication
 *    - Retry logic
 * 
 * 4. Error Handler:
 *    - Custom error processing
 *    - Status code handling
 *    - Exception mapping
 * 
 * 5. URI Builder:
 *    - Dynamic URL construction
 *    - Path variables
 *    - Query parameters
 *    - Encoding
 * 
 * 6. Reactive Web Client:
 *    - Non-blocking HTTP calls
 *    - Reactive streams
 *    - Filters
 * 
 * 7. HTTP Message Converter:
 *    - Request/response serialization
 *    - Custom converters
 *    - Content negotiation
 */
