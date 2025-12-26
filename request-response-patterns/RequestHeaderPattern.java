package com.example.requestresponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

/**
 * Request Header Pattern
 * 
 * Demonstrates how to access HTTP request headers in Spring MVC.
 * @RequestHeader annotation binds request headers to method parameters.
 */
@SpringBootApplication
public class RequestHeaderPattern {

    public static void main(String[] args) {
        SpringApplication.run(RequestHeaderPattern.class, args);
    }

    @RestController
    @RequestMapping("/api/headers")
    static class HeaderController {

        /**
         * Simple request header
         */
        @GetMapping("/user-agent")
        public String getUserAgent(@RequestHeader("User-Agent") String userAgent) {
            return "User-Agent: " + userAgent;
        }

        /**
         * Multiple request headers
         */
        @GetMapping("/info")
        public String getHeaderInfo(
                @RequestHeader("Host") String host,
                @RequestHeader("Accept") String accept,
                @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {
            return String.format("Host: %s, Accept: %s, Language: %s", host, accept, language);
        }

        /**
         * Optional request header
         */
        @GetMapping("/optional")
        public String getOptionalHeader(
                @RequestHeader(value = "X-Custom-Header", required = false) String customHeader) {
            return customHeader != null
                    ? "Custom Header: " + customHeader
                    : "Custom Header not provided";
        }

        /**
         * Authorization header
         */
        @GetMapping("/auth")
        public String getAuthHeader(@RequestHeader("Authorization") String authorization) {
            if (authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                return "Token: " + token;
            }
            return "Invalid authorization format";
        }

        /**
         * Custom headers for API versioning
         */
        @GetMapping("/versioned")
        public String getVersionedApi(
                @RequestHeader(value = "X-API-Version", defaultValue = "1.0") String apiVersion) {
            return "API Version: " + apiVersion;
        }

        /**
         * Content negotiation headers
         */
        @GetMapping("/content-type")
        public String getContentType(
                @RequestHeader("Content-Type") String contentType,
                @RequestHeader(value = "Accept", defaultValue = "*/*") String accept) {
            return String.format("Content-Type: %s, Accept: %s", contentType, accept);
        }

        /**
         * Correlation ID for request tracing
         */
        @GetMapping("/trace")
        public String traceRequest(
                @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
                @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
            StringBuilder result = new StringBuilder("Tracing info: ");
            if (correlationId != null) result.append("Correlation: ").append(correlationId);
            if (requestId != null) result.append(", Request: ").append(requestId);
            return result.toString();
        }

        /**
         * Cache control headers
         */
        @GetMapping("/cache")
        public String getCacheHeaders(
                @RequestHeader(value = "Cache-Control", required = false) String cacheControl,
                @RequestHeader(value = "If-None-Match", required = false) String etag) {
            StringBuilder result = new StringBuilder("Cache info: ");
            if (cacheControl != null) result.append("Cache-Control: ").append(cacheControl);
            if (etag != null) result.append(", ETag: ").append(etag);
            return result.toString();
        }

        /**
         * Custom business headers
         */
        @PostMapping("/business")
        public String processBusinessRequest(
                @RequestHeader("X-Tenant-ID") String tenantId,
                @RequestHeader(value = "X-User-ID", required = false) String userId,
                @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
                @RequestBody String data) {
            return String.format("Tenant: %s, User: %s, Session: %s, Data: %s",
                    tenantId, userId, sessionId, data);
        }

        /**
         * All headers as Map
         */
        @GetMapping("/all")
        public String getAllHeaders(@RequestHeader java.util.Map<String, String> headers) {
            StringBuilder result = new StringBuilder("All Headers:\n");
            headers.forEach((key, value) -> 
                result.append(key).append(": ").append(value).append("\n"));
            return result.toString();
        }
    }

    /**
     * Controller demonstrating security headers
     */
    @RestController
    @RequestMapping("/api/secure")
    static class SecurityHeaderController {

        /**
         * API Key authentication
         */
        @GetMapping("/protected")
        public String protectedResource(@RequestHeader("X-API-Key") String apiKey) {
            // In real scenario, validate apiKey against database
            return "Access granted with API Key: " + apiKey.substring(0, 8) + "...";
        }

        /**
         * Client certificate information
         */
        @GetMapping("/certificate")
        public String getCertificateInfo(
                @RequestHeader(value = "X-Client-Cert", required = false) String clientCert) {
            return clientCert != null
                    ? "Client Certificate: " + clientCert
                    : "No client certificate";
        }

        /**
         * Origin and Referer headers
         */
        @GetMapping("/origin")
        public String checkOrigin(
                @RequestHeader(value = "Origin", required = false) String origin,
                @RequestHeader(value = "Referer", required = false) String referer) {
            return String.format("Origin: %s, Referer: %s",
                    origin != null ? origin : "N/A",
                    referer != null ? referer : "N/A");
        }
    }
}
