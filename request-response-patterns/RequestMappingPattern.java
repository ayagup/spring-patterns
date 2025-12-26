package com.example.requestresponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Request Mapping Pattern
 * 
 * Demonstrates various ways to map HTTP requests to handler methods.
 * @RequestMapping and specialized variants (@GetMapping, @PostMapping, etc.)
 */
@SpringBootApplication
public class RequestMappingPattern {

    public static void main(String[] args) {
        SpringApplication.run(RequestMappingPattern.class, args);
    }

    /**
     * Basic request mappings
     */
    @RestController
    @RequestMapping("/api/basic")
    static class BasicMappingController {

        /**
         * Simple GET mapping
         */
        @GetMapping("/hello")
        public String hello() {
            return "Hello World";
        }

        /**
         * Multiple paths for same handler
         */
        @GetMapping({"/home", "/index", "/"})
        public String home() {
            return "Home page";
        }

        /**
         * All HTTP methods
         */
        @RequestMapping(value = "/all-methods", method = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE
        })
        public String allMethods() {
            return "Handles all HTTP methods";
        }
    }

    /**
     * HTTP method-specific mappings
     */
    @RestController
    @RequestMapping("/api/methods")
    static class HttpMethodController {

        @GetMapping("/resource")
        public String getResource() {
            return "GET request";
        }

        @PostMapping("/resource")
        public String createResource(@RequestBody String data) {
            return "POST request: " + data;
        }

        @PutMapping("/resource")
        public String updateResource(@RequestBody String data) {
            return "PUT request: " + data;
        }

        @PatchMapping("/resource")
        public String patchResource(@RequestBody String data) {
            return "PATCH request: " + data;
        }

        @DeleteMapping("/resource")
        public String deleteResource() {
            return "DELETE request";
        }
    }

    /**
     * Content type mappings
     */
    @RestController
    @RequestMapping("/api/content")
    static class ContentTypeController {

        /**
         * Produces JSON
         */
        @GetMapping(value = "/json", produces = MediaType.APPLICATION_JSON_VALUE)
        public String getJson() {
            return "{\"message\": \"JSON response\"}";
        }

        /**
         * Produces XML
         */
        @GetMapping(value = "/xml", produces = MediaType.APPLICATION_XML_VALUE)
        public String getXml() {
            return "<message>XML response</message>";
        }

        /**
         * Produces plain text
         */
        @GetMapping(value = "/text", produces = MediaType.TEXT_PLAIN_VALUE)
        public String getText() {
            return "Plain text response";
        }

        /**
         * Consumes JSON
         */
        @PostMapping(value = "/data", consumes = MediaType.APPLICATION_JSON_VALUE)
        public String acceptJson(@RequestBody String json) {
            return "Received JSON: " + json;
        }

        /**
         * Multiple content types
         */
        @GetMapping(value = "/multi", produces = {
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_XML_VALUE
        })
        public String multipleProduces() {
            return "{\"message\": \"Supports JSON and XML\"}";
        }
    }

    /**
     * Parameter mappings
     */
    @RestController
    @RequestMapping("/api/params")
    static class ParameterMappingController {

        /**
         * Required parameter
         */
        @GetMapping(value = "/search", params = "query")
        public String searchWithQuery(@RequestParam String query) {
            return "Searching: " + query;
        }

        /**
         * Multiple required parameters
         */
        @GetMapping(value = "/filter", params = {"type", "status"})
        public String filter(@RequestParam String type, @RequestParam String status) {
            return String.format("Type: %s, Status: %s", type, status);
        }

        /**
         * Parameter with specific value
         */
        @GetMapping(value = "/action", params = "mode=advanced")
        public String advancedMode() {
            return "Advanced mode activated";
        }

        /**
         * Parameter NOT present
         */
        @GetMapping(value = "/simple", params = "!advanced")
        public String simpleMode() {
            return "Simple mode";
        }
    }

    /**
     * Header mappings
     */
    @RestController
    @RequestMapping("/api/headers")
    static class HeaderMappingController {

        /**
         * Required header
         */
        @GetMapping(value = "/auth", headers = "X-API-Key")
        public String withApiKey(@RequestHeader("X-API-Key") String apiKey) {
            return "API Key: " + apiKey;
        }

        /**
         * Header with specific value
         */
        @GetMapping(value = "/version", headers = "X-API-Version=2.0")
        public String version2() {
            return "API Version 2.0";
        }

        /**
         * Multiple headers
         */
        @GetMapping(value = "/secure", headers = {"X-Auth-Token", "X-User-Id"})
        public String secureEndpoint(
                @RequestHeader("X-Auth-Token") String token,
                @RequestHeader("X-User-Id") String userId) {
            return String.format("Token: %s, User: %s", token, userId);
        }

        /**
         * Content-Type header matching
         */
        @PostMapping(value = "/upload", headers = "Content-Type=multipart/form-data")
        public String uploadFile() {
            return "File upload endpoint";
        }
    }

    /**
     * Path pattern mappings
     */
    @RestController
    @RequestMapping("/api/patterns")
    static class PathPatternController {

        /**
         * Wildcard pattern
         */
        @GetMapping("/files/*")
        public String singleWildcard() {
            return "Matches /files/document.pdf";
        }

        /**
         * Double wildcard pattern
         */
        @GetMapping("/docs/**")
        public String multipleWildcard() {
            return "Matches /docs/spring/guides/rest.html";
        }

        /**
         * Regex pattern
         */
        @GetMapping("/items/{id:[0-9]+}")
        public String numericId(@PathVariable String id) {
            return "Numeric ID: " + id;
        }

        /**
         * Multiple path variables with patterns
         */
        @GetMapping("/archive/{year:[0-9]{4}}/{month:[0-9]{2}}")
        public String archive(@PathVariable int year, @PathVariable int month) {
            return String.format("Archive: %d-%02d", year, month);
        }
    }

    /**
     * Composed mappings
     */
    @RestController
    @RequestMapping(
            value = "/api/composed",
            produces = MediaType.APPLICATION_JSON_VALUE,
            headers = "X-API-Version=1.0"
    )
    static class ComposedMappingController {

        @GetMapping("/info")
        public String info() {
            return "{\"api\": \"composed\", \"version\": \"1.0\"}";
        }
    }
}
