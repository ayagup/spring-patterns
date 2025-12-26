package com.example.requestresponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Entity Pattern
 * 
 * Demonstrates how to use HttpEntity for accessing request/response
 * with headers and body together. HttpEntity is the parent of ResponseEntity.
 */
@SpringBootApplication
public class HttpEntityPattern {

    public static void main(String[] args) {
        SpringApplication.run(HttpEntityPattern.class, args);
    }

    @RestController
    @RequestMapping("/api/http-entity")
    static class HttpEntityController {

        /**
         * HttpEntity as request parameter - access headers and body
         */
        @PostMapping("/process")
        public ResponseEntity<Map<String, Object>> processRequest(HttpEntity<String> httpEntity) {
            // Access headers
            HttpHeaders headers = httpEntity.getHeaders();
            String contentType = headers.getFirst("Content-Type");
            
            // Access body
            String body = httpEntity.getBody();
            
            Map<String, Object> response = new HashMap<>();
            response.put("receivedContentType", contentType);
            response.put("receivedBody", body);
            response.put("headerCount", headers.size());
            
            return ResponseEntity.ok(response);
        }

        /**
         * HttpEntity with custom headers
         */
        @PostMapping("/with-headers")
        public ResponseEntity<String> processWithHeaders(HttpEntity<String> httpEntity) {
            HttpHeaders requestHeaders = httpEntity.getHeaders();
            
            StringBuilder info = new StringBuilder("Request Headers:\n");
            requestHeaders.forEach((key, values) -> 
                info.append(key).append(": ").append(values).append("\n"));
            info.append("\nBody: ").append(httpEntity.getBody());
            
            return ResponseEntity.ok(info.toString());
        }

        /**
         * Return HttpEntity (without status code - defaults to 200)
         */
        @GetMapping("/return-entity")
        public HttpEntity<Map<String, String>> returnHttpEntity() {
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Custom-Header", "CustomValue");
            headers.add("X-Response-ID", "RESP-123");
            
            Map<String, String> body = new HashMap<>();
            body.put("message", "HttpEntity response");
            body.put("status", "success");
            
            return new HttpEntity<>(body, headers);
        }

        /**
         * Process request with specific headers
         */
        @PostMapping("/validate-headers")
        public ResponseEntity<Map<String, Object>> validateHeaders(HttpEntity<String> httpEntity) {
            HttpHeaders headers = httpEntity.getHeaders();
            
            Map<String, Object> validation = new HashMap<>();
            validation.put("hasContentType", headers.containsKey("Content-Type"));
            validation.put("hasAuthorization", headers.containsKey("Authorization"));
            validation.put("hasUserAgent", headers.containsKey("User-Agent"));
            validation.put("body", httpEntity.getBody());
            
            return ResponseEntity.ok(validation);
        }

        /**
         * Forward request to another service (proxy pattern)
         */
        @PostMapping("/proxy")
        public ResponseEntity<String> proxy(HttpEntity<String> httpEntity) {
            // In real scenario, forward to another service using RestTemplate/WebClient
            HttpHeaders requestHeaders = httpEntity.getHeaders();
            String requestBody = httpEntity.getBody();
            
            // Simulate processing
            String result = String.format("Proxied request - Headers: %d, Body length: %d",
                    requestHeaders.size(),
                    requestBody != null ? requestBody.length() : 0);
            
            return ResponseEntity.ok(result);
        }

        /**
         * Extract specific headers from HttpEntity
         */
        @PostMapping("/extract-auth")
        public ResponseEntity<Map<String, Object>> extractAuth(HttpEntity<Map<String, String>> httpEntity) {
            HttpHeaders headers = httpEntity.getHeaders();
            String authorization = headers.getFirst("Authorization");
            String apiKey = headers.getFirst("X-API-Key");
            
            Map<String, Object> authInfo = new HashMap<>();
            authInfo.put("hasAuthorization", authorization != null);
            authInfo.put("hasApiKey", apiKey != null);
            authInfo.put("bodyData", httpEntity.getBody());
            
            return ResponseEntity.ok(authInfo);
        }

        /**
         * Check if body is present
         */
        @PostMapping("/check-body")
        public ResponseEntity<Map<String, Object>> checkBody(HttpEntity<String> httpEntity) {
            boolean hasBody = httpEntity.hasBody();
            String body = httpEntity.getBody();
            
            Map<String, Object> info = new HashMap<>();
            info.put("hasBody", hasBody);
            info.put("bodyLength", body != null ? body.length() : 0);
            info.put("isEmpty", body == null || body.isEmpty());
            
            return ResponseEntity.ok(info);
        }

        /**
         * Process typed request body with HttpEntity
         */
        @PostMapping("/typed")
        public ResponseEntity<Map<String, Object>> typedEntity(HttpEntity<UserRequest> httpEntity) {
            HttpHeaders headers = httpEntity.getHeaders();
            UserRequest user = httpEntity.getBody();
            
            Map<String, Object> response = new HashMap<>();
            response.put("contentType", headers.getFirst("Content-Type"));
            response.put("userName", user != null ? user.getName() : null);
            response.put("userEmail", user != null ? user.getEmail() : null);
            
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Controller demonstrating HttpEntity with RestTemplate usage
     */
    @RestController
    @RequestMapping("/api/client")
    static class HttpEntityClientController {

        /**
         * Prepare HttpEntity for outgoing request
         */
        @PostMapping("/prepare")
        public ResponseEntity<Map<String, Object>> prepareOutgoingRequest(@RequestBody String data) {
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("Authorization", "Bearer token123");
            headers.add("X-Custom-Header", "CustomValue");
            
            // Create HttpEntity
            HttpEntity<String> requestEntity = new HttpEntity<>(data, headers);
            
            // In real scenario: restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class)
            
            Map<String, Object> info = new HashMap<>();
            info.put("headers", headers.toSingleValueMap());
            info.put("body", requestEntity.getBody());
            info.put("ready", true);
            
            return ResponseEntity.ok(info);
        }

        /**
         * HttpEntity without body (headers only)
         */
        @GetMapping("/headers-only")
        public ResponseEntity<String> headersOnly() {
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Request-ID", "REQ-456");
            headers.add("Accept", "application/json");
            
            // HttpEntity with headers but no body
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            return ResponseEntity.ok("HttpEntity created with headers only");
        }
    }

    /**
     * User request DTO
     */
    static class UserRequest {
        private String name;
        private String email;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
