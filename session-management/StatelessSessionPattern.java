package com.example.session;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * STATELESS SESSION PATTERN
 * ==========================
 * 
 * Purpose:
 * - Eliminate server-side session state
 * - Store all state on client side
 * - Enable horizontal scalability
 * - Support RESTful principles
 * 
 * Key Concepts:
 * - No HttpSession usage
 * - All data in tokens/cookies
 * - Stateless authentication
 * - Request contains all context
 * 
 * Benefits:
 * - Infinite horizontal scaling
 * - No session replication needed
 * - No sticky sessions required
 * - Simplified deployment
 * - Cloud-native friendly
 * 
 * Trade-offs:
 * - Larger request/response size
 * - Can't invalidate sessions easily
 * - Token security critical
 * - Client-side storage limitations
 */

// 1. STATELESS AUTHENTICATION TOKEN
class StatelessAuthToken {
    
    private final String userId;
    private final String username;
    private final Set<String> roles;
    private final long issuedAt;
    private final long expiresAt;
    private final Map<String, String> claims;
    
    public StatelessAuthToken(String userId, String username, Set<String> roles, long validityMs) {
        this.userId = userId;
        this.username = username;
        this.roles = new HashSet<>(roles);
        this.issuedAt = System.currentTimeMillis();
        this.expiresAt = issuedAt + validityMs;
        this.claims = new HashMap<>();
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
    
    public void addClaim(String key, String value) {
        claims.put(key, value);
    }
    
    public String getClaim(String key) {
        return claims.get(key);
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public Set<String> getRoles() { return new HashSet<>(roles); }
    public long getIssuedAt() { return issuedAt; }
    public long getExpiresAt() { return expiresAt; }
    public Map<String, String> getClaims() { return new HashMap<>(claims); }
    
    @Override
    public String toString() {
        return String.format("Token[user=%s, roles=%s, expires=%d]",
            username, roles, expiresAt);
    }
}

// 2. STATELESS AUTHENTICATION PROVIDER
@Service
class StatelessAuthenticationProvider {
    
    private final Map<String, UserCredentials> userStore = new ConcurrentHashMap<>();
    private static final long TOKEN_VALIDITY = 3600000; // 1 hour
    
    public StatelessAuthenticationProvider() {
        // Initialize with demo users
        userStore.put("alice", new UserCredentials("alice", "password123", 
            Set.of("USER", "ADMIN")));
        userStore.put("bob", new UserCredentials("bob", "password456", 
            Set.of("USER")));
    }
    
    public StatelessAuthToken authenticate(String username, String password) {
        UserCredentials credentials = userStore.get(username);
        
        if (credentials == null || !credentials.password.equals(password)) {
            throw new AuthenticationException("Invalid credentials");
        }
        
        return new StatelessAuthToken(
            credentials.userId,
            username,
            credentials.roles,
            TOKEN_VALIDITY
        );
    }
    
    public boolean validateToken(StatelessAuthToken token) {
        return token != null && !token.isExpired();
    }
    
    static class UserCredentials {
        final String userId;
        final String password;
        final Set<String> roles;
        
        UserCredentials(String username, String password, Set<String> roles) {
            this.userId = UUID.randomUUID().toString();
            this.password = password;
            this.roles = roles;
        }
    }
    
    static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}

// 3. STATELESS SECURITY CONTEXT
class StatelessSecurityContext {
    
    private static final ThreadLocal<StatelessAuthToken> contextHolder = new ThreadLocal<>();
    
    public static void setAuthentication(StatelessAuthToken token) {
        contextHolder.set(token);
    }
    
    public static StatelessAuthToken getAuthentication() {
        return contextHolder.get();
    }
    
    public static void clear() {
        contextHolder.remove();
    }
    
    public static boolean isAuthenticated() {
        StatelessAuthToken token = contextHolder.get();
        return token != null && !token.isExpired();
    }
    
    public static boolean hasRole(String role) {
        StatelessAuthToken token = contextHolder.get();
        return token != null && token.getRoles().contains(role);
    }
    
    public static String getCurrentUsername() {
        StatelessAuthToken token = contextHolder.get();
        return token != null ? token.getUsername() : null;
    }
}

// 4. STATELESS REQUEST CONTEXT
class StatelessRequestContext {
    
    private final Map<String, Object> attributes = new HashMap<>();
    private final StatelessAuthToken authentication;
    private final String requestId;
    
    public StatelessRequestContext(StatelessAuthToken authentication) {
        this.authentication = authentication;
        this.requestId = UUID.randomUUID().toString();
    }
    
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }
    
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    public StatelessAuthToken getAuthentication() {
        return authentication;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public boolean isAuthenticated() {
        return authentication != null && !authentication.isExpired();
    }
}

// 5. STATELESS SESSION MANAGER
@Service
class StatelessSessionManager {
    
    // No session storage - completely stateless!
    // All state is derived from the request token
    
    public StatelessRequestContext createContext(StatelessAuthToken token) {
        if (token == null || token.isExpired()) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        return new StatelessRequestContext(token);
    }
    
    public void processRequest(StatelessAuthToken token, Runnable action) {
        try {
            StatelessSecurityContext.setAuthentication(token);
            action.run();
        } finally {
            StatelessSecurityContext.clear();
        }
    }
    
    public <T> T processRequest(StatelessAuthToken token, java.util.function.Supplier<T> action) {
        try {
            StatelessSecurityContext.setAuthentication(token);
            return action.get();
        } finally {
            StatelessSecurityContext.clear();
        }
    }
}

// 6. CLIENT-SIDE STATE MANAGER
class ClientSideStateManager {
    
    // Simulates storing state in cookies/headers/localStorage
    private final Map<String, Map<String, String>> clientStorage = new ConcurrentHashMap<>();
    
    public void storeState(String clientId, String key, String value) {
        clientStorage.computeIfAbsent(clientId, k -> new ConcurrentHashMap<>())
                    .put(key, value);
        System.out.println("Stored on client [" + clientId + "]: " + key + " = " + value);
    }
    
    public String getState(String clientId, String key) {
        Map<String, String> storage = clientStorage.get(clientId);
        return storage != null ? storage.get(key) : null;
    }
    
    public void removeState(String clientId, String key) {
        Map<String, String> storage = clientStorage.get(clientId);
        if (storage != null) {
            storage.remove(key);
        }
    }
    
    public void clearAll(String clientId) {
        clientStorage.remove(clientId);
    }
    
    public Map<String, String> getAllState(String clientId) {
        Map<String, String> storage = clientStorage.get(clientId);
        return storage != null ? new HashMap<>(storage) : new HashMap<>();
    }
}

// 7. STATELESS VS STATEFUL COMPARISON
class SessionComparison {
    
    public static void compareApproaches() {
        System.out.println("\n=== STATELESS VS STATEFUL ===\n");
        
        System.out.println("STATELESS ADVANTAGES:");
        System.out.println("   ✓ Infinite horizontal scaling");
        System.out.println("   ✓ No session replication");
        System.out.println("   ✓ No sticky sessions needed");
        System.out.println("   ✓ Simplified load balancing");
        System.out.println("   ✓ Cloud-native friendly");
        System.out.println("   ✓ Easier deployment");
        System.out.println();
        
        System.out.println("STATELESS DISADVANTAGES:");
        System.out.println("   ✗ Larger requests/responses");
        System.out.println("   ✗ Can't invalidate tokens easily");
        System.out.println("   ✗ Token security critical");
        System.out.println("   ✗ Client storage limitations");
        System.out.println("   ✗ Token size limits");
        System.out.println();
        
        System.out.println("STATEFUL ADVANTAGES:");
        System.out.println("   ✓ Smaller requests/responses");
        System.out.println("   ✓ Easy session invalidation");
        System.out.println("   ✓ More server control");
        System.out.println("   ✓ Complex state management");
        System.out.println();
        
        System.out.println("STATEFUL DISADVANTAGES:");
        System.out.println("   ✗ Scaling challenges");
        System.out.println("   ✗ Session replication overhead");
        System.out.println("   ✗ Sticky sessions required");
        System.out.println("   ✗ Memory consumption");
        System.out.println();
    }
}

/**
 * DEMONSTRATION
 */
public class StatelessSessionPattern {
    
    public static void main(String[] args) {
        System.out.println("=== STATELESS SESSION PATTERN ===\n");
        
        StatelessAuthenticationProvider authProvider = new StatelessAuthenticationProvider();
        StatelessSessionManager sessionManager = new StatelessSessionManager();
        ClientSideStateManager clientState = new ClientSideStateManager();
        
        // 1. Authenticate and get token
        System.out.println("1. AUTHENTICATION:");
        StatelessAuthToken token = authProvider.authenticate("alice", "password123");
        System.out.println("   Token issued: " + token);
        System.out.println("   Valid for: " + (token.getExpiresAt() - token.getIssuedAt()) / 1000 + "s");
        System.out.println();
        
        // 2. Store user preferences client-side
        System.out.println("2. CLIENT-SIDE STATE:");
        String clientId = "browser-session-123";
        clientState.storeState(clientId, "theme", "dark");
        clientState.storeState(clientId, "language", "en");
        clientState.storeState(clientId, "pageSize", "25");
        
        System.out.println("   All client state: " + clientState.getAllState(clientId));
        System.out.println();
        
        // 3. Process request with stateless context
        System.out.println("3. PROCESS REQUEST:");
        sessionManager.processRequest(token, () -> {
            System.out.println("   Authenticated user: " + 
                StatelessSecurityContext.getCurrentUsername());
            System.out.println("   Has ADMIN role: " + 
                StatelessSecurityContext.hasRole("ADMIN"));
            
            // Retrieve client preferences
            String theme = clientState.getState(clientId, "theme");
            System.out.println("   User theme: " + theme);
        });
        System.out.println();
        
        // 4. Validate token
        System.out.println("4. TOKEN VALIDATION:");
        System.out.println("   Token valid: " + authProvider.validateToken(token));
        System.out.println("   Is expired: " + token.isExpired());
        System.out.println();
        
        // 5. Multiple concurrent requests (no session state)
        System.out.println("5. CONCURRENT REQUESTS:");
        for (int i = 1; i <= 3; i++) {
            final int requestNum = i;
            sessionManager.processRequest(token, () -> {
                System.out.println("   Request " + requestNum + " - User: " + 
                    StatelessSecurityContext.getCurrentUsername());
            });
        }
        System.out.println("   No server-side session required!");
        System.out.println();
        
        // Comparison
        SessionComparison.compareApproaches();
        
        System.out.println("Use Cases for Stateless:");
        System.out.println("   • RESTful APIs");
        System.out.println("   • Microservices");
        System.out.println("   • Mobile applications");
        System.out.println("   • Single Page Applications (SPA)");
        System.out.println("   • High-scale web services");
        System.out.println();
        
        System.out.println("Implementation Tips:");
        System.out.println("   ✓ Use JWT for authentication");
        System.out.println("   ✓ Store minimal data in tokens");
        System.out.println("   ✓ Set appropriate expiration");
        System.out.println("   ✓ Use HTTPS for security");
        System.out.println("   ✓ Implement token refresh");
        System.out.println("   ✓ Consider token blacklisting for logout");
    }
}
