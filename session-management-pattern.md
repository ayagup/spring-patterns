### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 1:06:34 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **Session Management Patterns**
> - Session Scope Pattern
> - HTTP Session Pattern
> - Session Repository Pattern
> - Spring Session Pattern
> - Session Clustering Pattern
> - Sticky Session Pattern
> - Session Replication Pattern
> - Stateless Session Pattern
> - Token-based Session Pattern
> - JWT Session Pattern
> - Redis Session Pattern
> - JDBC Session Pattern
> ```

#### _Assistant_

> # Spring Session Management Patterns - Complete Examples
> 
> I'll create a comprehensive Spring Boot project demonstrating all 12 session management patterns.
> 
> ## Project Structure
> 
> ```plaintext
> session-patterns/
> ├── pom.xml
> ├── src/main/java/com/example/session/
> │   ├── SessionPatternsApplication.java
> │   ├── config/
> │   ├── controller/
> │   ├── model/
> │   ├── repository/
> │   └── service/
> └── src/main/resources/
>     └── application.yml
> ```
> 
> ## 1. Main Application Class
> 
> ```java src/main/java/com/example/session/SessionPatternsApplication.java
> package com.example.session;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
> 
> @SpringBootApplication
> @EnableRedisHttpSession
> public class SessionPatternsApplication {
>     public static void main(String[] args) {
>         SpringApplication.run(SessionPatternsApplication.class, args);
>     }
> }
> ```
> 
> ## 2. Session Scope Pattern
> 
> ```java src/main/java/com/example/session/config/SessionScopeConfig.java
> package com.example.session.config;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.context.annotation.Scope;
> import org.springframework.context.annotation.ScopedProxyMode;
> import org.springframework.web.context.WebApplicationContext;
> 
> @Configuration
> public class SessionScopeConfig {
>     
>     @Bean
>     @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
>     public UserPreferences sessionScopedBean() {
>         return new UserPreferences();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/session/model/UserPreferences.java
> package com.example.session.model;
> 
> import java.io.Serializable;
> 
> public class UserPreferences implements Serializable {
>     private static final long serialVersionUID = 1L;
>     
>     private String theme = "light";
>     private String language = "en";
>     private int pageSize = 10;
> 
>     // Getters and setters
>     public String getTheme() { return theme; }
>     public void setTheme(String theme) { this.theme = theme; }
>     
>     public String getLanguage() { return language; }
>     public void setLanguage(String language) { this.language = language; }
>     
>     public int getPageSize() { return pageSize; }
>     public void setPageSize(int pageSize) { this.pageSize = pageSize; }
> }
> ```
> 
> ```java src/main/java/com/example/session/controller/SessionScopeController.java
> package com.example.session.controller;
> 
> import com.example.session.model.UserPreferences;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.web.bind.annotation.*;
> 
> @RestController
> @RequestMapping("/api/session-scope")
> public class SessionScopeController {
>     
>     @Autowired
>     private UserPreferences userPreferences;
>     
>     @GetMapping("/preferences")
>     public UserPreferences getPreferences() {
>         return userPreferences;
>     }
>     
>     @PostMapping("/preferences")
>     public UserPreferences updatePreferences(@RequestBody UserPreferences prefs) {
>         userPreferences.setTheme(prefs.getTheme());
>         userPreferences.setLanguage(prefs.getLanguage());
>         userPreferences.setPageSize(prefs.getPageSize());
>         return userPreferences;
>     }
> }
> ```
> 
> ## 3. HTTP Session Pattern
> 
> ```java src/main/java/com/example/session/controller/HttpSessionController.java
> package com.example.session.controller;
> 
> import org.springframework.web.bind.annotation.*;
> import javax.servlet.http.HttpSession;
> import java.util.HashMap;
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/http-session")
> public class HttpSessionController {
>     
>     @PostMapping("/login")
>     public Map<String, Object> login(@RequestParam String username, HttpSession session) {
>         session.setAttribute("username", username);
>         session.setAttribute("loginTime", System.currentTimeMillis());
>         session.setMaxInactiveInterval(1800); // 30 minutes
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("sessionId", session.getId());
>         response.put("username", username);
>         response.put("maxInactiveInterval", session.getMaxInactiveInterval());
>         return response;
>     }
>     
>     @GetMapping("/user")
>     public Map<String, Object> getCurrentUser(HttpSession session) {
>         Map<String, Object> response = new HashMap<>();
>         response.put("username", session.getAttribute("username"));
>         response.put("loginTime", session.getAttribute("loginTime"));
>         response.put("sessionId", session.getId());
>         return response;
>     }
>     
>     @PostMapping("/logout")
>     public Map<String, String> logout(HttpSession session) {
>         session.invalidate();
>         Map<String, String> response = new HashMap<>();
>         response.put("message", "Logged out successfully");
>         return response;
>     }
> }
> ```
> 
> ## 4. Session Repository Pattern
> 
> ```java src/main/java/com/example/session/repository/SessionRepository.java
> package com.example.session.repository;
> 
> import com.example.session.model.UserSession;
> import java.util.Map;
> import java.util.Optional;
> import java.util.concurrent.ConcurrentHashMap;
> import org.springframework.stereotype.Repository;
> 
> @Repository
> public class SessionRepository {
>     
>     private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();
>     
>     public void save(String sessionId, UserSession session) {
>         sessions.put(sessionId, session);
>     }
>     
>     public Optional<UserSession> findById(String sessionId) {
>         return Optional.ofNullable(sessions.get(sessionId));
>     }
>     
>     public void deleteById(String sessionId) {
>         sessions.remove(sessionId);
>     }
>     
>     public boolean exists(String sessionId) {
>         return sessions.containsKey(sessionId);
>     }
>     
>     public void cleanExpiredSessions() {
>         long now = System.currentTimeMillis();
>         sessions.entrySet().removeIf(entry -> 
>             now - entry.getValue().getLastAccessTime() > entry.getValue().getMaxInactiveInterval()
>         );
>     }
> }
> ```
> 
> ```java src/main/java/com/example/session/model/UserSession.java
> package com.example.session.model;
> 
> import java.io.Serializable;
> import java.util.HashMap;
> import java.util.Map;
> 
> public class UserSession implements Serializable {
>     private static final long serialVersionUID = 1L;
>     
>     private String sessionId;
>     private String username;
>     private long creationTime;
>     private long lastAccessTime;
>     private int maxInactiveInterval = 1800; // 30 minutes in seconds
>     private Map<String, Object> attributes = new HashMap<>();
> 
>     public UserSession(String sessionId, String username) {
>         this.sessionId = sessionId;
>         this.username = username;
>         this.creationTime = System.currentTimeMillis();
>         this.lastAccessTime = System.currentTimeMillis();
>     }
> 
>     // Getters and setters
>     public String getSessionId() { return sessionId; }
>     public void setSessionId(String sessionId) { this.sessionId = sessionId; }
>     
>     public String getUsername() { return username; }
>     public void setUsername(String username) { this.username = username; }
>     
>     public long getCreationTime() { return creationTime; }
>     public void setCreationTime(long creationTime) { this.creationTime = creationTime; }
>     
>     public long getLastAccessTime() { return lastAccessTime; }
>     public void setLastAccessTime(long lastAccessTime) { this.lastAccessTime = lastAccessTime; }
>     
>     public int getMaxInactiveInterval() { return maxInactiveInterval; }
>     public void setMaxInactiveInterval(int maxInactiveInterval) { 
>         this.maxInactiveInterval = maxInactiveInterval; 
>     }
>     
>     public Map<String, Object> getAttributes() { return attributes; }
>     public void setAttribute(String key, Object value) { attributes.put(key, value); }
>     public Object getAttribute(String key) { return attributes.get(key); }
> }
> ```
> 
> ```java src/main/java/com/example/session/controller/SessionRepositoryController.java
> package com.example.session.controller;
> 
> import com.example.session.model.UserSession;
> import com.example.session.repository.SessionRepository;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.web.bind.annotation.*;
> import java.util.HashMap;
> import java.util.Map;
> import java.util.UUID;
> 
> @RestController
> @RequestMapping("/api/session-repository")
> public class SessionRepositoryController {
>     
>     @Autowired
>     private SessionRepository sessionRepository;
>     
>     @PostMapping("/create")
>     public Map<String, Object> createSession(@RequestParam String username) {
>         String sessionId = UUID.randomUUID().toString();
>         UserSession session = new UserSession(sessionId, username);
>         sessionRepository.save(sessionId, session);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("sessionId", sessionId);
>         response.put("username", username);
>         return response;
>     }
>     
>     @GetMapping("/{sessionId}")
>     public UserSession getSession(@PathVariable String sessionId) {
>         return sessionRepository.findById(sessionId)
>             .orElseThrow(() -> new RuntimeException("Session not found"));
>     }
>     
>     @DeleteMapping("/{sessionId}")
>     public Map<String, String> deleteSession(@PathVariable String sessionId) {
>         sessionRepository.deleteById(sessionId);
>         Map<String, String> response = new HashMap<>();
>         response.put("message", "Session deleted");
>         return response;
>     }
> }
> ```
> 
> ## 5. Spring Session Pattern
> 
> ```java src/main/java/com/example/session/config/SpringSessionConfig.java
> package com.example.session.config;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.session.MapSessionRepository;
> import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
> import java.util.concurrent.ConcurrentHashMap;
> 
> @Configuration
> @EnableSpringHttpSession
> public class SpringSessionConfig {
>     
>     @Bean
>     public MapSessionRepository sessionRepository() {
>         return new MapSessionRepository(new ConcurrentHashMap<>());
>     }
> }
> ```
> 
> ```java src/main/java/com/example/session/controller/SpringSessionController.java
> package com.example.session.controller;
> 
> import org.springframework.session.Session;
> import org.springframework.session.SessionRepository;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.web.bind.annotation.*;
> import javax.servlet.http.HttpServletRequest;
> import java.util.HashMap;
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/spring-session")
> public class SpringSessionController {
>     
>     @Autowired
>     private SessionRepository<? extends Session> sessionRepository;
>     
>     @PostMapping("/data")
>     public Map<String, Object> setSessionData(
>             @RequestParam String key, 
>             @RequestParam String value,
>             HttpServletRequest request) {
>         
>         Session session = sessionRepository.createSession();
>         session.setAttribute(key, value);
>         sessionRepository.save(session);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("sessionId", session.getId());
>         response.put("key", key);
>         response.put("value", value);
>         return response;
>     }
>     
>     @GetMapping("/data/{sessionId}")
>     public Map<String, Object> getSessionData(@PathVariable String sessionId) {
>         Session session = sessionRepository.findById(sessionId);
>         
>         Map<String, Object> response = new HashMap<>();
>         if (session != null) {
>             session.getAttributeNames().forEach(name -> 
>                 response.put(name, session.getAttribute(name))
>             );
>         }
>         return response;
>     }
> }
> ```
> 
> ## 6. Session Clustering Pattern
> 
> ```java src/main/java/com/example/session/config/SessionClusteringConfig.java
> package com.example.session.config;
> 
> import org.springframework.context.annotation.Configuration;
> import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
> import org.springframework.context.annotation.Bean;
> import org.springframework.data.redis.connection.RedisConnectionFactory;
> import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
> 
> @Configuration
> @EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
> public class SessionClusteringConfig {
>     
>     @Bean
>     public RedisConnectionFactory redisConnectionFactory() {
>         LettuceConnectionFactory factory = new LettuceConnectionFactory();
>         factory.setHostName("localhost");
>         factory.setPort(6379);
>         return factory;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/session/controller/ClusteredSessionController.java
> package com.example.session.controller;
> 
> import org.springframework.web.bind.annotation.*;
> import javax.servlet.http.HttpSession;
> import java.util.HashMap;
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/clustered-session")
> public class ClusteredSessionController {
>     
>     @PostMapping("/store")
>     public Map<String, Object> storeInCluster(
>             @RequestParam String key,
>             @RequestParam String value,
>             HttpSession session) {
>         
>         session.setAttribute(key, value);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("sessionId", session.getId());
>         response.put("stored", true);
>         response.put("key", key);
>         response.put("serverId", System.getenv("SERVER_ID")); // For clustering demo
>         return response;
>     }
>     
>     @GetMapping("/retrieve")
>     public Map<String, Object> retrieveFromCluster(
>             @RequestParam String key,
>             HttpSession session) {
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("sessionId", session.getId());
>         response.put("key", key);
>         response.put("value", session.getAttribute(key));
>         response.put("serverId", System.getenv("SERVER_ID"));
>         return response;
>     }
> }
> ```
> 
> ## 7. Sticky Session Pattern
> 
> ```java src/main/java/com/example/session/config/StickySessionConfig.java
> package com.example.session.config;
> 
> import org.springframework.context.annotation.Configuration;
> import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
> import org.springframework.boot.web.server.WebServerFactoryCustomizer;
> import org.springframework.context.annotation.Bean;
> 
> @Configuration
> public class StickySessionConfig {
>     
>     @Bean
>     public WebServerFactoryCustomizer<TomcatServletWebServerFactory> 
>         cookieProcessorCustomizer() {
>         return factory -> {
>             factory.addContextCustomizers(context -> {
>                 // Enable session cookie with jvmRoute for sticky sessions
>                 context.setSessionCookieName("JSESSIONID");
>                 // The jvmRoute would be set in server.xml or via system property
>             });
>         };
>     }
> }
> ```
> 
> ```java src/main/java/com/example/session/controller/StickySessionController.java
> package com.example.session.controller;
> 
> import org.springframework.web.bind.annotation.*;
> import javax.servlet.http.HttpSession;
> import javax.servlet.http.Cookie;
> import javax.servlet.http.HttpServletResponse;
> import java.util.HashMap;
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/sticky-session")
> public class StickySessionController {
>     
>     private static final String SERVER_ID = System.getenv().getOrDefault("SERVER_ID", "server1");
>     
>     @PostMapping("/login")
>     public Map<String, Object> stickyLogin(
>             @RequestParam String username,
>             HttpSession session,
>             HttpServletResponse response) {
>         
>         session.setAttribute("username", username);
>         session.setAttribute("serverId", SERVER_ID);
>         
>         // Add custom cookie for load balancer routing
>         Cookie cookie = new Cookie("SERVER_ROUTE", SERVER_ID);
>         cookie.setPath("/");
>         cookie.setMaxAge(1800);
>         response.addCookie(cookie);
>         
>         Map<String, Object> result = new HashMap<>();
>         result.put("sessionId", session.getId());
>         result.put("username", username);
>         result.put("routedToServer", SERVER_ID);
>         return result;
>     }
>     
>     @GetMapping("/info")
>     public Map<String, Object> getSessionInfo(HttpSession session) {
>         Map<String, Object> info = new HashMap<>();
>         info.put("sessionId", session.getId());
>         info.put("username", session.getAttribute("username"));
>         info.put("originalServer", session.getAttribute("serverId"));
>         info.put("currentServer", SERVER_ID);
>         return info;
>     }
> }
> ```
> 
> ## 8. Session Replication Pattern
> 
> ```java src/main/java/com/example/session/config/SessionReplicationConfig.java
> package com.example.session.config;
> 
> import org.springframework.context.annotation.Configuration;
> import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
> import org.springframework.context.annotation.Bean;
> import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
> import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
> import org.springframework.session.data.redis.config.ConfigureRedisAction;
> 
> @Configuration
> @EnableRedisHttpSession(redisNamespace = "replicated-session")
> public class SessionReplicationConfig {
>     
>     @Bean
>     public LettuceConnectionFactory connectionFactory() {
>         RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
>         config.setHostName("localhost");
>         config.setPort(6379);
>         return new LettuceConnectionFactory(config);
>     }
>     
>     @Bean
>     public static ConfigureRedisAction configureRedisAction() {
>         return ConfigureRedisAction.NO_OP;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/session/service/SessionReplicationService.java
> package com.example.session.service;
> 
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.session.Session;
> import org.springframework.session.SessionRepository;
> import org.springframework.stereotype.Service;
> import java.util.HashMap;
> import java.util.Map;
> 
> @Service
> public class SessionReplicationService {
>     
>     @Autowired
>     private SessionRepository<? extends Session> sessionRepository;
>     
>     public Session replicateSession(String sourceSessionId) {
>         Session sourceSession = sessionRepository.findById(sourceSessionId);
>         
>         if (sourceSession == null) {
>             throw new RuntimeException("Source session not found");
>         }
>         
>         // Create new replicated session
>         Session replicatedSession = sessionRepository.createSession();
>         
>         // Copy all attributes
>         sourceSession.getAttributeNames().forEach(name -> 
>             replicatedSession.setAttribute(name, sourceSession.getAttribute(name))
>         );
>         
>         sessionRepository.save(replicatedSession);
>         return replicatedSession;
>     }
>     
>     public Map<String, Object> getReplicationStatus(String sessionId) {
>         Session session = sessionRepository.findById(sessionId);
>         
>         Map<String, Object> status = new HashMap<>();
>         if (session != null) {
>             status.put("sessionId", session.getId());
>             status.put("creationTime", session.getCreationTime());
>             status.put("lastAccessedTime", session.getLastAccessedTime());
>             status.put("attributeCount", session.getAttributeNames().size());
>             status.put("replicated", true);
>         } else {
>             status.put("replicated", false);
>         }
>         return status;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/session/controller/SessionReplicationController.java
> package com.example.session.controller;
> 
> import com.example.session.service.SessionReplicationService;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.session.Session;
> import org.springframework.web.bind.annotation.*;
> import java.util.HashMap;
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/session-replication")
> public class SessionReplicationController {
>     
>     @Autowired
>     private SessionReplicationService replicationService;
>     
>     @PostMapping("/replicate/{sessionId}")
>     public Map<String, Object> replicateSession(@PathVariable String sessionId) {
>         Session replicated = replicationService.replicateSession(sessionId);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("originalSessionId", sessionId);
>         response.put("replicatedSessionId", replicated.getId());
>         return response;
>     }
>     
>     @GetMapping("/status/{sessionId}")
>     public Map<String, Object> getReplicationStatus(@PathVariable String sessionId) {
>         return replicationService.getReplicationStatus(sessionId);
>     }
> }
> ```
> 
> ## 9. Stateless Session Pattern
> 
> ```java src/main/java/com/example/session/model/StatelessSessionToken.java
> package com.example.session.model;
> 
> import java.io.Serializable;
> import java.util.HashMap;
> import java.util.Map;
> 
> public class StatelessSessionToken implements Serializable {
>     private static final long serialVersionUID = 1L;
>     
>     private String userId;
>     private long issuedAt;
>     private long expiresAt;
>     private Map<String, Object> claims = new HashMap<>();
> 
>     public StatelessSessionToken(String userId, long expiresAt) {
>         this.userId = userId;
>         this.issuedAt = System.currentTimeMillis();
>         this.expiresAt = expiresAt;
>     }
> 
>     public boolean isExpired() {
>         return System.currentTimeMillis() > expiresAt;
>     }
> 
>     // Getters and setters
>     public String getUserId() { return userId; }
>     public void setUserId(String userId) { this.userId = userId; }
>     
>     public long getIssuedAt() { return issuedAt; }
>     public void setIssuedAt(long issuedAt) { this.issuedAt = issuedAt; }
>     
>     public long getExpiresAt() { return expiresAt; }
>     public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
>     
>     public Map<String, Object> getClaims() { return claims; }
>     public void setClaim(String key, Object value) { claims.put(key, value); }
>     public Object getClaim(String key) { return claims.get(key); }
> }
> ```
> 
> ```java src/main/java/com/example/session/service/StatelessSessionService.java
> package com.example.session.service;
> 
> import com.example.session.model.StatelessSessionToken;
> import com.fasterxml.jackson.databind.ObjectMapper;
> import org.springframework.stereotype.Service;
> import java.util.Base64;
> 
> @Service
> public class StatelessSessionService {
>     
>     private final ObjectMapper objectMapper = new ObjectMapper();
>     private static final long SESSION_DURATION = 1800000; // 30 minutes
>     
>     public String createToken(String userId) {
>         try {
>             long expiresAt = System.currentTimeMillis() + SESSION_DURATION;
>             StatelessSessionToken token = new StatelessSessionToken(userId, expiresAt);
>             
>             String json = objectMapper.writeValueAsString(token);
>             return Base64.getEncoder().encodeToString(json.getBytes());
>         } catch (Exception e) {
>             throw new RuntimeException("Failed to create token", e);
>         }
>     }
>     
>     public StatelessSessionToken validateToken(String token) {
>         try {
>             byte[] decoded = Base64.getDecoder().decode(token);
>             StatelessSessionToken sessionToken = objectMapper.readValue(
>                 decoded, StatelessSessionToken.class
>             );
>             
>             if (sessionToken.isExpired()) {
>                 throw new RuntimeException("Token expired");
>             }
>             
>             return sessionToken;
>         } catch (Exception e) {
>             throw new RuntimeException("Invalid token", e);
>         }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/session/controller/StatelessSessionController.java
> package com.example.session.controller;
> 
> import com.example.session.model.StatelessSessionToken;
> import com.example.session.service.StatelessSessionService;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.web.bind.annotation.*;
> import java.util.HashMap;
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/stateless-session")
> public class StatelessSessionController {
>     
>     @Autowired
>     private StatelessSessionService statelessSessionService;
>     
>     @PostMapping("/login")
>     public Map<String, Object> statelessLogin(@RequestParam String userId) {
>         String token = statelessSessionService.createToken(userId);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("token", token);
>         response.put("userId", userId);
>         response.put("type", "stateless");
>         return response;
>     }
>     
>     @GetMapping("/validate")
>     public Map<String, Object> validateToken(@RequestHeader("Authorization") String token) {
>         StatelessSessionToken sessionToken = statelessSessionService.validateToken(token);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("valid", true);
>         response.put("userId", sessionToken.getUserId());
>         response.put("issuedAt", sessionToken.getIssuedAt());
>         response.put("expiresAt", sessionToken.getExpiresAt());
>         return response;
>     }
> }
> ```
> 
> ## 10. Token-based Session Pattern
> 
> ```java src/main/java/com/example/session/model/TokenSession.java
> package com.example.session.model;
> 
> import java.util.HashMap;
> import java.util.Map;
> import java.util.UUID;
> 
> public class TokenSession {
>     private String token;
>     private String userId;
>     private long createdAt;
>     private long expiresAt;
>     private Map<String, Object> metadata = new HashMap<>();
>     
>     public TokenSession(String userId, long ttlMillis) {
>         this.token = UUID.randomUUID().toString();
>         this.userId = userId;
>         this.createdAt = System.currentTimeMillis();
>         this.expiresAt = createdAt + ttlMillis;
>     }
>     
>     public boolean isValid() {
>         return System.currentTimeMillis() < expiresAt;
>     }
>     
>     public void refresh(long ttlMillis) {
>         this.expiresAt = System.currentTimeMillis() + ttlMillis;
>     }
> 
>     // Getters and setters
>     public String getToken() { return token; }
>     public String getUserId() { return userId; }
>     public long getCreatedAt() { return createdAt; }
>     public long getExpiresAt() { return expiresAt; }
>     public Map<String, Object> getMetadata() { return metadata; }
>     public void setMetadata(String key, Object value) { metadata.put(key, value); }
> }
> ```
> 
> ```java src/main/java/com/example/session/service/TokenSessionService.java
> package com.example.session.service;
> 
> import com.example.session.model.TokenSession;
> import org.springframework.stereotype.Service;
> import java.util.Map;
> import java.util.concurrent.ConcurrentHashMap;
> 
> @Service
> public class TokenSessionService {
>     
>     private final Map<String, TokenSession> tokenStore = new ConcurrentHashMap<>();
>     private static final long DEFAULT_TTL = 1800000; // 30 minutes
>     
>     public TokenSession createSession(String userId) {
>         TokenSession session = new TokenSession(userId, DEFAULT_TTL);
>         tokenStore.put(session.getToken(), session);
>         return session;
>     }
>     
>     public TokenSession validateToken(String token) {
>         TokenSession session = tokenStore.get(token);
>         
>         if (session == null) {
>             throw new RuntimeException("Invalid token");
>         }
>         
>         if (!session.isValid()) {
>             tokenStore.remove(token);
>             throw new RuntimeException("Token expired");
>         }
>         
>         return session;
>     }
>     
>     public void refreshToken(String token) {
>         TokenSession session = validateToken(token);
>         session.refresh(DEFAULT_TTL);
>     }
>     
>     public void invalidateToken(String token) {
>         tokenStore.remove(token);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/session/controller/TokenSessionController.java
> package com.example.session.controller;
> 
> import com.example.session.model.TokenSession;
> import com.example.session.service.TokenSessionService;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.web.bind.annotation.*;
> import java.util.HashMap;
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/token-session")
> public class TokenSessionController {
>     
>     @Autowired
>     private TokenSessionService tokenSessionService;
>     
>     @PostMapping("/create")
>     public Map<String, Object> createTokenSession(@RequestParam String userId) {
>         TokenSession session = tokenSessionService.createSession(userId);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("token", session.getToken());
>         response.put("userId", session.getUserId());
>         response.put("expiresAt", session.getExpiresAt());
>         return response;
>     }
>     
>     @PostMapping("/validate")
>     public Map<String, Object> validateToken(@RequestHeader("X-Auth-Token") String token) {
>         TokenSession session = tokenSessionService.validateToken(token);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("valid", true);
>         response.put("userId", session.getUserId());
>         response.put("expiresAt", session.getExpiresAt());
>         return response;
>     }
>     
>     @PostMapping("/refresh")
>     public Map<String, Object> refreshToken(@RequestHeader("X-Auth-Token") String token) {
>         tokenSessionService.refreshToken(token);
>         TokenSession session = tokenSessionService.validateToken(token);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("refreshed", true);
>         response.put("newExpiresAt", session.getExpiresAt());
>         return response;
>     }
>     
>     @PostMapping("/invalidate")
>     public Map<String, String> invalidateToken(@RequestHeader("X-Auth-Token") String token) {
>         tokenSessionService.invalidateToken(token);
>         
>         Map<String, String> response = new HashMap<>();
>         response.put("message", "Token invalidated");
>         return response;
>     }
> }
> ```
> 
> ## 11. JWT Session Pattern
> 
> ```java src/main/java/com/example/session/service/JwtSessionService.java
> package com.example.session.service;
> 
> import io.jsonwebtoken.*;
> import io.jsonwebtoken.security.Keys;
> import org.springframework.stereotype.Service;
> import javax.crypto.SecretKey;
> import java.util.Date;
> import java.util.HashMap;
> import java.util.Map;
> 
> @Service
> public class JwtSessionService {
>     
>     private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
>     private static final long EXPIRATION_TIME = 1800000; // 30 minutes
>     
>     public String createJwtToken(String userId, Map<String, Object> claims) {
>         Date now = new Date();
>         Date expiryDate = new Date(now.getTime() + EXP

#### _User_

> Continue your response exactly where you left off:

#### _Assistant_

> ```java src/main/java/com/example/session/service/JwtSessionService.java
> package com.example.session.service;
> 
> import io.jsonwebtoken.*;
> import io.jsonwebtoken.security.Keys;
> import org.springframework.stereotype.Service;
> import javax.crypto.SecretKey;
> import java.util.Date;
> import java.util.HashMap;
> import java.util.Map;
> 
> @Service
> public class JwtSessionService {
>     
>     private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
>     private static final long EXPIRATION_TIME = 1800000; // 30 minutes
>     
>     public String createJwtToken(String userId, Map<String, Object> claims) {
>         Date now = new Date();
>         Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
>         
>         return Jwts.builder()
>                 .setSubject(userId)
>                 .addClaims(claims)
>                 .setIssuedAt(now)
>                 .setExpiration(expiryDate)
>                 .signWith(secretKey)
>                 .compact();
>     }
>     
>     public Claims validateJwtToken(String token) {
>         try {
>             return Jwts.parserBuilder()
>                     .setSigningKey(secretKey)
>                     .build()
>                     .parseClaimsJws(token)
>                     .getBody();
>         } catch (JwtException e) {
>             throw new RuntimeException("Invalid JWT token", e);
>         }
>     }
>     
>     public String getUserIdFromToken(String token) {
>         Claims claims = validateJwtToken(token);
>         return claims.getSubject();
>     }
>     
>     public boolean isTokenExpired(String token) {
>         try {
>             Claims claims = validateJwtToken(token);
>             return claims.getExpiration().before(new Date());
>         } catch (Exception e) {
>             return true;
>         }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/session/controller/JwtSessionController.java
> package com.example.session.controller;
> 
> import com.example.session.service.JwtSessionService;
> import io.jsonwebtoken.Claims;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.web.bind.annotation.*;
> import java.util.HashMap;
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/jwt-session")
> public class JwtSessionController {
>     
>     @Autowired
>     private JwtSessionService jwtSessionService;
>     
>     @PostMapping("/login")
>     public Map<String, Object> jwtLogin(
>             @RequestParam String userId,
>             @RequestParam(required = false) String role) {
>         
>         Map<String, Object> claims = new HashMap<>();
>         claims.put("role", role != null ? role : "USER");
>         claims.put("loginTime", System.currentTimeMillis());
>         
>         String token = jwtSessionService.createJwtToken(userId, claims);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("token", token);
>         response.put("userId", userId);
>         response.put("tokenType", "JWT");
>         return response;
>     }
>     
>     @GetMapping("/validate")
>     public Map<String, Object> validateJwt(@RequestHeader("Authorization") String authHeader) {
>         String token = authHeader.replace("Bearer ", "");
>         Claims claims = jwtSessionService.validateJwtToken(token);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("valid", true);
>         response.put("userId", claims.getSubject());
>         response.put("role", claims.get("role"));
>         response.put("issuedAt", claims.getIssuedAt());
>         response.put("expiresAt", claims.getExpiration());
>         return response;
>     }
>     
>     @GetMapping("/user-info")
>     public Map<String, Object> getUserInfo(@RequestHeader("Authorization") String authHeader) {
>         String token = authHeader.replace("Bearer ", "");
>         Claims claims = jwtSessionService.validateJwtToken(token);
>         
>         Map<String, Object> userInfo = new HashMap<>();
>         userInfo.put("userId", claims.getSubject());
>         userInfo.put("role", claims.get("role"));
>         userInfo.put("loginTime", claims.get("loginTime"));
>         return userInfo;
>     }
> }
> ```
> 
> ## 12. Redis Session Pattern
> 
> ```java src/main/java/com/example/session/config/RedisSessionConfig.java
> package com.example.session.config;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.data.redis.connection.RedisConnectionFactory;
> import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
> import org.springframework.data.redis.core.RedisTemplate;
> import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
> import org.springframework.data.redis.serializer.StringRedisSerializer;
> import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
> 
> @Configuration
> @EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
> public class RedisSessionConfig {
>     
>     @Bean
>     public RedisConnectionFactory redisConnectionFactory() {
>         LettuceConnectionFactory factory = new LettuceConnectionFactory();
>         factory.setHostName("localhost");
>         factory.setPort(6379);
>         return factory;
>     }
>     
>     @Bean
>     public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
>         RedisTemplate<String, Object> template = new RedisTemplate<>();
>         template.setConnectionFactory(connectionFactory);
>         template.setKeySerializer(new StringRedisSerializer());
>         template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
>         template.setHashKeySerializer(new StringRedisSerializer());
>         template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
>         return template;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/session/service/RedisSessionService.java
> package com.example.session.service;
> 
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.data.redis.core.RedisTemplate;
> import org.springframework.stereotype.Service;
> import java.util.Map;
> import java.util.concurrent.TimeUnit;
> 
> @Service
> public class RedisSessionService {
>     
>     @Autowired
>     private RedisTemplate<String, Object> redisTemplate;
>     
>     private static final String SESSION_PREFIX = "session:";
>     private static final long SESSION_TTL = 1800; // 30 minutes in seconds
>     
>     public void createSession(String sessionId, Map<String, Object> sessionData) {
>         String key = SESSION_PREFIX + sessionId;
>         redisTemplate.opsForHash().putAll(key, sessionData);
>         redisTemplate.expire(key, SESSION_TTL, TimeUnit.SECONDS);
>     }
>     
>     public void setAttribute(String sessionId, String attribute, Object value) {
>         String key = SESSION_PREFIX + sessionId;
>         redisTemplate.opsForHash().put(key, attribute, value);
>         redisTemplate.expire(key, SESSION_TTL, TimeUnit.SECONDS);
>     }
>     
>     public Object getAttribute(String sessionId, String attribute) {
>         String key = SESSION_PREFIX + sessionId;
>         return redisTemplate.opsForHash().get(key, attribute);
>     }
>     
>     public Map<Object, Object> getAllAttributes(String sessionId) {
>         String key = SESSION_PREFIX + sessionId;
>         return redisTemplate.opsForHash().entries(key);
>     }
>     
>     public void removeAttribute(String sessionId, String attribute) {
>         String key = SESSION_PREFIX + sessionId;
>         redisTemplate.opsForHash().delete(key, attribute);
>     }
>     
>     public void deleteSession(String sessionId) {
>         String key = SESSION_PREFIX + sessionId;
>         redisTemplate.delete(key);
>     }
>     
>     public boolean sessionExists(String sessionId) {
>         String key = SESSION_PREFIX + sessionId;
>         return Boolean.TRUE.equals(redisTemplate.hasKey(key));
>     }
>     
>     public void refreshSession(String sessionId) {
>         String key = SESSION_PREFIX + sessionId;
>         redisTemplate.expire(key, SESSION_TTL, TimeUnit.SECONDS);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/session/controller/RedisSessionController.java
> package com.example.session.controller;
> 
> import com.example.session.service.RedisSessionService;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.web.bind.annotation.*;
> import java.util.HashMap;
> import java.util.Map;
> import java.util.UUID;
> 
> @RestController
> @RequestMapping("/api/redis-session")
> public class RedisSessionController {
>     
>     @Autowired
>     private RedisSessionService redisSessionService;
>     
>     @PostMapping("/create")
>     public Map<String, Object> createRedisSession(
>             @RequestParam String userId,
>             @RequestBody(required = false) Map<String, Object> additionalData) {
>         
>         String sessionId = UUID.randomUUID().toString();
>         
>         Map<String, Object> sessionData = new HashMap<>();
>         sessionData.put("userId", userId);
>         sessionData.put("createdAt", System.currentTimeMillis());
>         
>         if (additionalData != null) {
>             sessionData.putAll(additionalData);
>         }
>         
>         redisSessionService.createSession(sessionId, sessionData);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("sessionId", sessionId);
>         response.put("userId", userId);
>         response.put("storage", "Redis");
>         return response;
>     }
>     
>     @PostMapping("/{sessionId}/attribute")
>     public Map<String, Object> setSessionAttribute(
>             @PathVariable String sessionId,
>             @RequestParam String key,
>             @RequestParam String value) {
>         
>         redisSessionService.setAttribute(sessionId, key, value);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("sessionId", sessionId);
>         response.put("key", key);
>         response.put("value", value);
>         response.put("action", "set");
>         return response;
>     }
>     
>     @GetMapping("/{sessionId}/attribute/{key}")
>     public Map<String, Object> getSessionAttribute(
>             @PathVariable String sessionId,
>             @PathVariable String key) {
>         
>         Object value = redisSessionService.getAttribute(sessionId, key);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("sessionId", sessionId);
>         response.put("key", key);
>         response.put("value", value);
>         return response;
>     }
>     
>     @GetMapping("/{sessionId}/all")
>     public Map<String, Object> getAllSessionAttributes(@PathVariable String sessionId) {
>         Map<Object, Object> attributes = redisSessionService.getAllAttributes(sessionId);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("sessionId", sessionId);
>         response.put("attributes", attributes);
>         response.put("count", attributes.size());
>         return response;
>     }
>     
>     @DeleteMapping("/{sessionId}")
>     public Map<String, String> deleteRedisSession(@PathVariable String sessionId) {
>         redisSessionService.deleteSession(sessionId);
>         
>         Map<String, String> response = new HashMap<>();
>         response.put("message", "Session deleted from Redis");
>         response.put("sessionId", sessionId);
>         return response;
>     }
>     
>     @PostMapping("/{sessionId}/refresh")
>     public Map<String, Object> refreshSession(@PathVariable String sessionId) {
>         redisSessionService.refreshSession(sessionId);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("sessionId", sessionId);
>         response.put("action", "refreshed");
>         response.put("ttl", "1800 seconds");
>         return response;
>     }
> }
> ```
> 
> ## 13. JDBC Session Pattern
> 
> ```java src/main/java/com/example/session/config/JdbcSessionConfig.java
> package com.example.session.config;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.jdbc.datasource.DataSourceTransactionManager;
> import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
> import org.springframework.transaction.PlatformTransactionManager;
> import javax.sql.DataSource;
> 
> @Configuration
> @EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 1800)
> public class JdbcSessionConfig {
>     
>     @Bean
>     public PlatformTransactionManager transactionManager(DataSource dataSource) {
>         return new DataSourceTransactionManager(dataSource);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/session/model/JdbcSessionEntity.java
> package com.example.session.model;
> 
> import javax.persistence.*;
> import java.io.Serializable;
> 
> @Entity
> @Table(name = "spring_session")
> public class JdbcSessionEntity implements Serializable {
>     private static final long serialVersionUID = 1L;
>     
>     @Id
>     @Column(name = "primary_id", length = 36)
>     private String primaryId;
>     
>     @Column(name = "session_id", length = 36, unique = true, nullable = false)
>     private String sessionId;
>     
>     @Column(name = "creation_time", nullable = false)
>     private Long creationTime;
>     
>     @Column(name = "last_access_time", nullable = false)
>     private Long lastAccessTime;
>     
>     @Column(name = "max_inactive_interval", nullable = false)
>     private Integer maxInactiveInterval;
>     
>     @Column(name = "expiry_time", nullable = false)
>     private Long expiryTime;
>     
>     @Column(name = "principal_name", length = 100)
>     private String principalName;
> 
>     // Getters and setters
>     public String getPrimaryId() { return primaryId; }
>     public void setPrimaryId(String primaryId) { this.primaryId = primaryId; }
>     
>     public String getSessionId() { return sessionId; }
>     public void setSessionId(String sessionId) { this.sessionId = sessionId; }
>     
>     public Long getCreationTime() { return creationTime; }
>     public void setCreationTime(Long creationTime) { this.creationTime = creationTime; }
>     
>     public Long getLastAccessTime() { return lastAccessTime; }
>     public void setLastAccessTime(Long lastAccessTime) { this.lastAccessTime = lastAccessTime; }
>     
>     public Integer getMaxInactiveInterval() { return maxInactiveInterval; }
>     public void setMaxInactiveInterval(Integer maxInactiveInterval) { 
>         this.maxInactiveInterval = maxInactiveInterval; 
>     }
>     
>     public Long getExpiryTime() { return expiryTime; }
>     public void setExpiryTime(Long expiryTime) { this.expiryTime = expiryTime; }
>     
>     public String getPrincipalName() { return principalName; }
>     public void setPrincipalName(String principalName) { this.principalName = principalName; }
> }
> ```
> 
> ```java src/main/java/com/example/session/repository/JdbcSessionRepository.java
> package com.example.session.repository;
> 
> import com.example.session.model.JdbcSessionEntity;
> import org.springframework.data.jpa.repository.JpaRepository;
> import org.springframework.data.jpa.repository.Modifying;
> import org.springframework.data.jpa.repository.Query;
> import org.springframework.stereotype.Repository;
> import org.springframework.transaction.annotation.Transactional;
> import java.util.List;
> import java.util.Optional;
> 
> @Repository
> public interface JdbcSessionRepository extends JpaRepository<JdbcSessionEntity, String> {
>     
>     Optional<JdbcSessionEntity> findBySessionId(String sessionId);
>     
>     List<JdbcSessionEntity> findByPrincipalName(String principalName);
>     
>     @Modifying
>     @Transactional
>     @Query("DELETE FROM JdbcSessionEntity s WHERE s.expiryTime < :currentTime")
>     int deleteExpiredSessions(Long currentTime);
>     
>     @Query("SELECT COUNT(s) FROM JdbcSessionEntity s WHERE s.principalName = :principalName")
>     long countActiveSessionsByPrincipal(String principalName);
> }
> ```
> 
> ```java src/main/java/com/example/session/service/JdbcSessionService.java
> package com.example.session.service;
> 
> import com.example.session.model.JdbcSessionEntity;
> import com.example.session.repository.JdbcSessionRepository;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.scheduling.annotation.Scheduled;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
> import java.util.List;
> import java.util.UUID;
> 
> @Service
> public class JdbcSessionService {
>     
>     @Autowired
>     private JdbcSessionRepository jdbcSessionRepository;
>     
>     private static final int MAX_INACTIVE_INTERVAL = 1800; // 30 minutes in seconds
>     
>     @Transactional
>     public JdbcSessionEntity createSession(String principalName) {
>         JdbcSessionEntity session = new JdbcSessionEntity();
>         session.setPrimaryId(UUID.randomUUID().toString());
>         session.setSessionId(UUID.randomUUID().toString());
>         session.setPrincipalName(principalName);
>         
>         long now = System.currentTimeMillis();
>         session.setCreationTime(now);
>         session.setLastAccessTime(now);
>         session.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL);
>         session.setExpiryTime(now + (MAX_INACTIVE_INTERVAL * 1000L));
>         
>         return jdbcSessionRepository.save(session);
>     }
>     
>     @Transactional
>     public void updateLastAccessTime(String sessionId) {
>         jdbcSessionRepository.findBySessionId(sessionId).ifPresent(session -> {
>             long now = System.currentTimeMillis();
>             session.setLastAccessTime(now);
>             session.setExpiryTime(now + (session.getMaxInactiveInterval() * 1000L));
>             jdbcSessionRepository.save(session);
>         });
>     }
>     
>     public JdbcSessionEntity getSession(String sessionId) {
>         return jdbcSessionRepository.findBySessionId(sessionId)
>                 .orElseThrow(() -> new RuntimeException("Session not found"));
>     }
>     
>     public List<JdbcSessionEntity> getUserSessions(String principalName) {
>         return jdbcSessionRepository.findByPrincipalName(principalName);
>     }
>     
>     @Transactional
>     public void deleteSession(String sessionId) {
>         jdbcSessionRepository.findBySessionId(sessionId)
>                 .ifPresent(jdbcSessionRepository::delete);
>     }
>     
>     @Scheduled(fixedRate = 60000) // Run every minute
>     @Transactional
>     public void cleanupExpiredSessions() {
>         int deleted = jdbcSessionRepository.deleteExpiredSessions(System.currentTimeMillis());
>         if (deleted > 0) {
>             System.out.println("Cleaned up " + deleted + " expired sessions");
>         }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/session/controller/JdbcSessionController.java
> package com.example.session.controller;
> 
> import com.example.session.model.JdbcSessionEntity;
> import com.example.session.service.JdbcSessionService;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.web.bind.annotation.*;
> import java.util.HashMap;
> import java.util.List;
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/jdbc-session")
> public class JdbcSessionController {
>     
>     @Autowired
>     private JdbcSessionService jdbcSessionService;
>     
>     @PostMapping("/create")
>     public Map<String, Object> createJdbcSession(@RequestParam String username) {
>         JdbcSessionEntity session = jdbcSessionService.createSession(username);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("sessionId", session.getSessionId());
>         response.put("principalName", session.getPrincipalName());
>         response.put("creationTime", session.getCreationTime());
>         response.put("expiryTime", session.getExpiryTime());
>         response.put("storage", "JDBC/Database");
>         return response;
>     }
>     
>     @GetMapping("/{sessionId}")
>     public Map<String, Object> getJdbcSession(@PathVariable String sessionId) {
>         JdbcSessionEntity session = jdbcSessionService.getSession(sessionId);
>         jdbcSessionService.updateLastAccessTime(sessionId);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("sessionId", session.getSessionId());
>         response.put("principalName", session.getPrincipalName());
>         response.put("creationTime", session.getCreationTime());
>         response.put("lastAccessTime", session.getLastAccessTime());
>         response.put("expiryTime", session.getExpiryTime());
>         return response;
>     }
>     
>     @GetMapping("/user/{username}")
>     public Map<String, Object> getUserSessions(@PathVariable String username) {
>         List<JdbcSessionEntity> sessions = jdbcSessionService.getUserSessions(username);
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("username", username);
>         response.put("activeSessions", sessions.size());
>         response.put("sessions", sessions);
>         return response;
>     }
>     
>     @DeleteMapping("/{sessionId}")
>     public Map<String, String> deleteJdbcSession(@PathVariable String sessionId) {
>         jdbcSessionService.deleteSession(sessionId);
>         
>         Map<String, String> response = new HashMap<>();
>         response.put("message", "Session deleted from database");
>         response.put("sessionId", sessionId);
>         return response;
>     }
> }
> ```
> 
> ## Maven Dependencies (pom.xml)
> 
> ```xml pom.xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0"
>          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
>          http://maven.apache.org/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>2.7.14</version>
>         <relativePath/>
>     </parent>
>     
>     <groupId>com.example</groupId>
>     <artifactId>session-patterns</artifactId>
>     <version>1.0.0</version>
>     <name>Session Management Patterns</name>
>     
>     <properties>
>         <java.version>11</java.version>
>     </properties>
>     
>     <dependencies>
>         <!-- Spring Boot Web -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         
>         <!-- Spring Session Core -->
>         <dependency>
>             <groupId>org.springframework.session</groupId>
>             <artifactId>spring-session-core</artifactId>
>         </dependency>
>         
>         <!-- Spring Session JDBC -->
>         <dependency>
>             <groupId>org.springframework.session</groupId>
>             <artifactId>spring-session-jdbc</artifactId>
>         </dependency>
>         
>         <!-- Spring Session Data Redis -->
>         <dependency>
>             <groupId>org.springframework.session</groupId>
>             <artifactId>spring-session-data-redis</artifactId>
>         </dependency>
>         
>         <!-- Redis Client -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-data-redis</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>io.lettuce</groupId>
>             <artifactId>lettuce-core</artifactId>
>         </dependency>
>         
>         <!-- JDBC & Database -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-data-jpa</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>com.h2database</groupId>
>             <artifactId>h2</artifactId>
>             <scope>runtime</scope>
>         </dependency>
>         
>         <!-- JWT Support -->
>         <dependency>
>             <groupId>io.jsonwebtoken</groupId>
>             <artifactId>jjwt-api</artifactId>
>             <version>0.11.5</version>
>         </dependency>
>         
>         <dependency>
>             <groupId>io.jsonwebtoken</groupId>
>             <artifactId>jjwt-impl</artifactId>
>             <version>0.11.5</version>
>             <scope>runtime</scope>
>         </dependency>
>         
>         <dependency>
>             <groupId>io.jsonwebtoken</groupId>
>             <artifactId>jjwt-jackson</artifactId>
>             <version>0.11.5</version>
>             <scope>runtime</scope>
>         </dependency>
>         
>         <!-- Jackson for JSON -->
>         <dependency>
>             <groupId>com.fasterxml.jackson.core</groupId>
>             <artifactId>jackson-databind</artifactId>
>         </dependency>
>         
>         <!-- Spring Boot Starter Test -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-test</artifactId>
>             <scope>test</scope>
>         </dependency>
>     </dependencies>
>     
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>             </plugin>
>         </plugins>
>     </build>
> </project>
> ```
> 
> ## Application Configuration
> 
> ```yaml src/main/resources/application.yml
> spring:
>   application:
>     name: session-patterns-demo
>   
>   # Database Configuration (H2)
>   datasource:
>     url: jdbc:h2:mem:sessiondb
>     driver-class-name: org.h2.Driver
>     username: sa
>     password:
>   
>   h2:
>     console:
>       enabled: true
>       path: /h2-console
>   
>   jpa:
>     hibernate:
>       ddl-auto: create
>     show-sql: true
>     database-platform: org.hibernate.dialect.H2Dialect
>   
>   # Redis Configuration
>   redis:
>     host: localhost
>     port: 6379
>     timeout: 2000ms
>     lettuce:
>       pool:
>         max-active: 8
>         max-idle: 8
>         min-idle: 0
>   
>   # Session Configuration
>   session:
>     store-type: redis
>     timeout: 30m
>     redis:
>       namespace: spring:session
>     jdbc:
>       initialize-schema: always
>       table-name: SPRING_SESSION
> 
> server:
>   port: 8080
>   servlet:
>     session:
>       timeout: 30m
>       cookie:
>         name: SESSIONID
>         http-only: true
>         secure: false
>         max-age: 1800
> 
> logging:
>   level:
>     org.springframework.session: DEBUG
>     com.example.session: DEBUG
> ```
> 
> ## SQL Schema for JDBC Session (optional - auto-created by Spring)
> 
> ```sql src/main/resources/schema.sql
> CREATE TABLE IF NOT EXISTS SPRING_SESSION (
>     PRIMARY_ID CHAR(36) NOT NULL,
>     SESSION_ID CHAR(36) NOT NULL,
>     CREATION_TIME BIGINT NOT NULL,
>     LAST_ACCESS_TIME BIGINT NOT NULL,
>     MAX_INACTIVE_INTERVAL INT NOT NULL,
>     EXPIRY_TIME BIGINT NOT NULL,
>     PRINCIPAL_NAME VARCHAR(100),
>     CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
> );
> 
> CREATE UNIQUE INDEX IF NOT EXISTS SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
> CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
> CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);
> 
> CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (
>     SESSION_PRIMARY_ID CHAR(36) NOT NULL,
>     ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
>     ATTRIBUTE_BYTES BYTEA NOT NULL,
>     CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
>     CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) 
>         REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
> );
> ```
> 
> ---
> 
> ## Usage Examples
> 
> ### Testing Each Pattern
> 
> You can test each pattern with curl commands:
> 
> **1. Session Scope Pattern:**
> ```bash
> curl -X POST http://localhost:8080/api/session-scope/preferences \
>   -H "Content-Type: application/json" \
>   -d '{"theme":"dark","language":"es","pageSize":20}' \
>   -c cookies.txt
> 
> curl -X GET http://localhost:8080/api/session-scope/preferences \
>   -b cookies.txt
> ```
> 
> **2. HTTP Session Pattern:**
> ```bash
> curl -X POST http://localhost:8080/api/http-session/login?username=john \
>   -c cookies.txt
> 
> curl -X GET http://localhost:8080/api/http-session/user \
>   -b cookies.txt
> ```
> 
> **3. JWT Session Pattern:**
> ```bash
> curl -X POST "http://localhost:8080/api/jwt-session/login?userId=user123&role=ADMIN"
> 
> curl -X GET http://localhost:8080/api/jwt-session/validate \
>   -H "Authorization: Bearer YOUR_JWT_TOKEN"
> ```
> 
> **4. Redis Session Pattern:**
> ```bash
> curl -X POST "http://localhost:8080/api/redis-session/create?userId=user456" \
>   -H "Content-Type: application/json" \
>   -d '{"email":"user@example.com","role":"USER"}'
> ```
> 
> **5. JDBC Session Pattern:**
> ```bash
> curl -X POST "http://localhost:8080/api/jdbc-session/create?username=alice"
> 
> curl -X GET http://localhost:8080/api/jdbc-session/user/alice
> ```
> 
> All 12 session management patterns are now implemented with working code examples! Each pattern demonstrates a different approach to managing session state in Spring applications.