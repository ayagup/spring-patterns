package com.example.redis.session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Session Pattern
 * 
 * Demonstrates Spring Session with Redis for distributed session management.
 * Redis Session provides:
 * - Distributed session storage
 * - Session replication across servers
 * - Configurable session timeout
 * - Session events
 * - Multiple session repositories
 * - RESTful session management
 * 
 * Use cases:
 * - Clustered web applications
 * - Microservices session sharing
 * - Stateless application scaling
 * - Session failover
 * - Load-balanced applications
 */

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800) // 30 minutes
class RedisSessionConfig {
    
    // Use header-based session ID resolution for RESTful APIs
    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return HeaderHttpSessionIdResolver.xAuthToken();
    }
}

record UserInfo(String userId, String username, String email, String role) {}

record SessionData(
    String sessionId,
    UserInfo user,
    Map<String, Object> attributes,
    long creationTime,
    long lastAccessedTime,
    int maxInactiveInterval,
    boolean isNew
) {}

@RestController
@RequestMapping("/api/redis/session")
class RedisSessionController {
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody UserInfo userInfo, HttpSession session) {
        // Store user info in session
        session.setAttribute("user", userInfo);
        session.setAttribute("loginTime", Instant.now().toString());
        
        return Map.of(
            "message", "Login successful",
            "sessionId", session.getId(),
            "user", userInfo
        );
    }
    
    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        String sessionId = session.getId();
        session.invalidate();
        
        return Map.of(
            "message", "Logout successful",
            "sessionId", sessionId
        );
    }
    
    @GetMapping("/current")
    public SessionData getCurrentSession(HttpSession session) {
        UserInfo user = (UserInfo) session.getAttribute("user");
        
        Map<String, Object> attributes = new HashMap<>();
        session.getAttributeNames().asIterator().forEachRemaining(name -> 
            attributes.put(name, session.getAttribute(name))
        );
        
        return new SessionData(
            session.getId(),
            user,
            attributes,
            session.getCreationTime(),
            session.getLastAccessedTime(),
            session.getMaxInactiveInterval(),
            session.isNew()
        );
    }
    
    @GetMapping("/user")
    public UserInfo getCurrentUser(HttpSession session) {
        return (UserInfo) session.getAttribute("user");
    }
    
    @PostMapping("/attributes/{key}")
    public Map<String, String> setAttribute(@PathVariable String key, 
                                           @RequestBody String value,
                                           HttpSession session) {
        session.setAttribute(key, value);
        return Map.of("message", "Attribute set", "key", key);
    }
    
    @GetMapping("/attributes/{key}")
    public Object getAttribute(@PathVariable String key, HttpSession session) {
        return session.getAttribute(key);
    }
    
    @DeleteMapping("/attributes/{key}")
    public Map<String, String> removeAttribute(@PathVariable String key, HttpSession session) {
        session.removeAttribute(key);
        return Map.of("message", "Attribute removed", "key", key);
    }
    
    @GetMapping("/attributes")
    public Map<String, Object> getAllAttributes(HttpSession session) {
        Map<String, Object> attributes = new HashMap<>();
        session.getAttributeNames().asIterator().forEachRemaining(name -> 
            attributes.put(name, session.getAttribute(name))
        );
        return attributes;
    }
    
    @PutMapping("/timeout")
    public Map<String, Object> setSessionTimeout(@RequestParam int seconds, HttpSession session) {
        session.setMaxInactiveInterval(seconds);
        return Map.of(
            "message", "Session timeout updated",
            "maxInactiveInterval", seconds
        );
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return """
                Redis Session Pattern
                ====================
                Features:
                - Distributed session storage in Redis
                - Session replication across servers
                - Configurable timeout (default: 30 minutes)
                - Header-based session ID (X-Auth-Token)
                - Session attributes storage
                - Session invalidation
                
                Configuration:
                - @EnableRedisHttpSession enables Redis session
                - maxInactiveIntervalInSeconds sets timeout
                - HeaderHttpSessionIdResolver for RESTful APIs
                
                Operations:
                - POST /login: Create session with user info
                - POST /logout: Invalidate session
                - GET /current: Get session details
                - GET /user: Get current user from session
                - POST/GET/DELETE /attributes/{key}: Manage attributes
                - PUT /timeout: Update session timeout
                
                Benefits:
                - Enables horizontal scaling
                - Session failover support
                - No sticky sessions required
                - Centralized session management
                - RESTful API support
                
                Note: Include X-Auth-Token header in requests
                      to maintain session across requests.
                """;
    }
}

@Controller
class SessionTestController {
    
    @GetMapping("/session/test")
    @ResponseBody
    public String testSession(HttpSession session) {
        Integer counter = (Integer) session.getAttribute("counter");
        if (counter == null) {
            counter = 0;
        }
        counter++;
        session.setAttribute("counter", counter);
        
        return "Session ID: " + session.getId() + ", Counter: " + counter;
    }
}
