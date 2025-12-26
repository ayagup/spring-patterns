package com.example.requestresponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Response Header Pattern
 * 
 * Demonstrates how to set HTTP response headers in Spring MVC.
 * Headers can be added using ResponseEntity, HttpServletResponse, or @ResponseHeader.
 */
@SpringBootApplication
public class ResponseHeaderPattern {

    public static void main(String[] args) {
        SpringApplication.run(ResponseHeaderPattern.class, args);
    }

    @RestController
    @RequestMapping("/api/response")
    static class ResponseHeaderController {

        /**
         * Simple response header using ResponseEntity
         */
        @GetMapping("/simple")
        public ResponseEntity<String> simpleHeader() {
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Custom-Header", "Custom Value");
            return new ResponseEntity<>("Response with custom header", headers, HttpStatus.OK);
        }

        /**
         * Multiple response headers
         */
        @GetMapping("/multiple")
        public ResponseEntity<String> multipleHeaders() {
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Request-ID", "REQ-12345");
            headers.add("X-Response-Time", "150ms");
            headers.add("X-Server-ID", "Server-01");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body("Response with multiple headers");
        }

        /**
         * Cache control headers
         */
        @GetMapping("/cache")
        public ResponseEntity<String> cacheControl() {
            return ResponseEntity.ok()
                    .header("Cache-Control", "max-age=3600, must-revalidate")
                    .header("Expires", "Wed, 21 Oct 2025 07:28:00 GMT")
                    .body("Cacheable response");
        }

        /**
         * Content-Type header
         */
        @GetMapping("/content-type")
        public ResponseEntity<String> contentType() {
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .body("{\"message\": \"JSON response\"}");
        }

        /**
         * Custom business headers
         */
        @GetMapping("/business")
        public ResponseEntity<String> businessHeaders() {
            return ResponseEntity.ok()
                    .header("X-Transaction-ID", "TXN-" + System.currentTimeMillis())
                    .header("X-API-Version", "v2.0")
                    .header("X-Rate-Limit", "100")
                    .header("X-Rate-Remaining", "95")
                    .body("Business response");
        }

        /**
         * Location header for created resources
         */
        @PostMapping("/create")
        public ResponseEntity<String> createResource() {
            String resourceId = "12345";
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header("Location", "/api/resource/" + resourceId)
                    .body("Resource created with ID: " + resourceId);
        }

        /**
         * ETag header for caching
         */
        @GetMapping("/etag")
        public ResponseEntity<String> etagHeader() {
            String contentHash = "33a64df551425fcc55e4d42a148795d9f25f89d4";
            return ResponseEntity.ok()
                    .header("ETag", "\"" + contentHash + "\"")
                    .header("Cache-Control", "max-age=3600")
                    .body("Content with ETag");
        }

        /**
         * CORS headers
         */
        @GetMapping("/cors")
        public ResponseEntity<String> corsHeaders() {
            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE")
                    .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                    .header("Access-Control-Max-Age", "3600")
                    .body("CORS enabled response");
        }

        /**
         * Content-Disposition header for file downloads
         */
        @GetMapping("/download")
        public ResponseEntity<String> downloadFile() {
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"document.pdf\"")
                    .header("Content-Type", "application/pdf")
                    .body("File content here");
        }

        /**
         * Security headers
         */
        @GetMapping("/secure")
        public ResponseEntity<String> securityHeaders() {
            return ResponseEntity.ok()
                    .header("X-Content-Type-Options", "nosniff")
                    .header("X-Frame-Options", "DENY")
                    .header("X-XSS-Protection", "1; mode=block")
                    .header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
                    .header("Content-Security-Policy", "default-src 'self'")
                    .body("Secure response");
        }

        /**
         * Custom pagination headers
         */
        @GetMapping("/paginated")
        public ResponseEntity<String> paginationHeaders() {
            return ResponseEntity.ok()
                    .header("X-Total-Count", "100")
                    .header("X-Page-Number", "1")
                    .header("X-Page-Size", "10")
                    .header("X-Total-Pages", "10")
                    .header("Link", "</api/items?page=2>; rel=\"next\", </api/items?page=10>; rel=\"last\"")
                    .body("Page 1 of 10");
        }

        /**
         * Rate limiting headers
         */
        @GetMapping("/rate-limit")
        public ResponseEntity<String> rateLimitHeaders() {
            return ResponseEntity.ok()
                    .header("X-RateLimit-Limit", "1000")
                    .header("X-RateLimit-Remaining", "999")
                    .header("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 3600000))
                    .body("Rate limit info in headers");
        }

        /**
         * Vary header for content negotiation
         */
        @GetMapping("/vary")
        public ResponseEntity<String> varyHeader() {
            return ResponseEntity.ok()
                    .header("Vary", "Accept-Encoding, User-Agent")
                    .header("Content-Encoding", "gzip")
                    .body("Response varies by Accept-Encoding and User-Agent");
        }
    }
}
