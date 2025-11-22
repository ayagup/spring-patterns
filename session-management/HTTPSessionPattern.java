package com.example.session;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP SESSION PATTERN
 * ====================
 * 
 * Purpose:
 * - Direct manipulation of HTTP session using Servlet API
 * - Store and retrieve session attributes
 * - Manage session lifecycle
 * - Track session metadata
 * 
 * Key Components:
 * 1. HttpSession - Core session interface
 * 2. HttpServletRequest - Access to session
 * 3. Session attributes - Key-value storage
 * 4. Session metadata - ID, creation time, timeout
 * 
 * Use Cases:
 * - Custom session management
 * - Session attribute manipulation
 * - Session tracking and monitoring
 * - Legacy integration
 * - Fine-grained control
 */

// 1. HTTP SESSION SERVICE
@Service
class HttpSessionService {
    
    // Get or create session
    public HttpSession getSession(HttpServletRequest request) {
        return request.getSession(); // Creates if doesn't exist
    }
    
    public HttpSession getSession(HttpServletRequest request, boolean create) {
        return request.getSession(create);
    }
    
    // Session attribute operations
    public void setAttribute(HttpSession session, String name, Object value) {
        session.setAttribute(name, value);
    }
    
    public Object getAttribute(HttpSession session, String name) {
        return session.getAttribute(name);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(HttpSession session, String name, Class<T> type) {
        Object value = session.getAttribute(name);
        return type.isInstance(value) ? (T) value : null;
    }
    
    public void removeAttribute(HttpSession session, String name) {
        session.removeAttribute(name);
    }
    
    // Session metadata
    public String getSessionId(HttpSession session) {
        return session.getId();
    }
    
    public long getCreationTime(HttpSession session) {
        return session.getCreationTime();
    }
    
    public long getLastAccessedTime(HttpSession session) {
        return session.getLastAccessedTime();
    }
    
    public int getMaxInactiveInterval(HttpSession session) {
        return session.getMaxInactiveInterval();
    }
    
    public void setMaxInactiveInterval(HttpSession session, int interval) {
        session.setMaxInactiveInterval(interval);
    }
    
    // Session lifecycle
    public void invalidateSession(HttpSession session) {
        session.invalidate();
    }
    
    public boolean isSessionNew(HttpSession session) {
        return session.isNew();
    }
    
    // Get all attribute names
    public List<String> getAttributeNames(HttpSession session) {
        List<String> names = new ArrayList<>();
        Enumeration<String> enumeration = session.getAttributeNames();
        while (enumeration.hasMoreElements()) {
            names.add(enumeration.nextElement());
        }
        return names;
    }
    
    // Get all attributes as map
    public Map<String, Object> getAllAttributes(HttpSession session) {
        Map<String, Object> attributes = new HashMap<>();
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            attributes.put(name, session.getAttribute(name));
        }
        return attributes;
    }
}

// 2. SESSION TRACKING SERVICE
@Service
class SessionTrackingService {
    
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    
    public void trackSession(HttpSession session) {
        String sessionId = session.getId();
        SessionInfo info = new SessionInfo(
            sessionId,
            session.getCreationTime(),
            session.getLastAccessedTime(),
            session.getMaxInactiveInterval()
        );
        activeSessions.put(sessionId, info);
    }
    
    public void updateLastAccess(HttpSession session) {
        String sessionId = session.getId();
        SessionInfo info = activeSessions.get(sessionId);
        if (info != null) {
            info.setLastAccessedTime(session.getLastAccessedTime());
            info.incrementAccessCount();
        }
    }
    
    public void removeSession(String sessionId) {
        activeSessions.remove(sessionId);
    }
    
    public SessionInfo getSessionInfo(String sessionId) {
        return activeSessions.get(sessionId);
    }
    
    public Collection<SessionInfo> getAllActiveSessions() {
        return new ArrayList<>(activeSessions.values());
    }
    
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        activeSessions.entrySet().removeIf(entry -> {
            SessionInfo info = entry.getValue();
            long inactiveTime = now - info.getLastAccessedTime();
            long maxInactive = info.getMaxInactiveInterval() * 1000L;
            return inactiveTime > maxInactive;
        });
    }
    
    static class SessionInfo {
        private final String sessionId;
        private final long creationTime;
        private long lastAccessedTime;
        private final int maxInactiveInterval;
        private int accessCount;
        
        public SessionInfo(String sessionId, long creationTime, long lastAccessedTime, int maxInactiveInterval) {
            this.sessionId = sessionId;
            this.creationTime = creationTime;
            this.lastAccessedTime = lastAccessedTime;
            this.maxInactiveInterval = maxInactiveInterval;
            this.accessCount = 1;
        }
        
        public void incrementAccessCount() {
            this.accessCount++;
        }
        
        public void setLastAccessedTime(long lastAccessedTime) {
            this.lastAccessedTime = lastAccessedTime;
        }
        
        // Getters
        public String getSessionId() { return sessionId; }
        public long getCreationTime() { return creationTime; }
        public long getLastAccessedTime() { return lastAccessedTime; }
        public int getMaxInactiveInterval() { return maxInactiveInterval; }
        public int getAccessCount() { return accessCount; }
        
        @Override
        public String toString() {
            return String.format("Session[id=%s, created=%d, accessed=%d, count=%d]",
                sessionId, creationTime, lastAccessedTime, accessCount);
        }
    }
}

// 3. USER SESSION MANAGER
@Service
class UserSessionManager {
    
    private static final String USER_KEY = "userId";
    private static final String USERNAME_KEY = "username";
    private static final String ROLES_KEY = "userRoles";
    private static final String LOGIN_TIME_KEY = "loginTime";
    
    public void login(HttpSession session, Long userId, String username, Set<String> roles) {
        session.setAttribute(USER_KEY, userId);
        session.setAttribute(USERNAME_KEY, username);
        session.setAttribute(ROLES_KEY, roles);
        session.setAttribute(LOGIN_TIME_KEY, System.currentTimeMillis());
    }
    
    public void logout(HttpSession session) {
        session.invalidate();
    }
    
    public boolean isLoggedIn(HttpSession session) {
        return session.getAttribute(USER_KEY) != null;
    }
    
    public Long getUserId(HttpSession session) {
        return (Long) session.getAttribute(USER_KEY);
    }
    
    public String getUsername(HttpSession session) {
        return (String) session.getAttribute(USERNAME_KEY);
    }
    
    @SuppressWarnings("unchecked")
    public Set<String> getUserRoles(HttpSession session) {
        Object roles = session.getAttribute(ROLES_KEY);
        return roles instanceof Set ? (Set<String>) roles : Collections.emptySet();
    }
    
    public Long getLoginTime(HttpSession session) {
        return (Long) session.getAttribute(LOGIN_TIME_KEY);
    }
    
    public boolean hasRole(HttpSession session, String role) {
        return getUserRoles(session).contains(role);
    }
    
    public void updateLastActivity(HttpSession session) {
        session.setAttribute("lastActivity", System.currentTimeMillis());
    }
}

// 4. SESSION CONFIGURATION
class SessionConfiguration {
    
    // Session timeout in seconds (30 minutes)
    public static final int SESSION_TIMEOUT = 30 * 60;
    
    // Session cookie configuration
    public static final String COOKIE_NAME = "JSESSIONID";
    public static final boolean COOKIE_HTTP_ONLY = true;
    public static final boolean COOKIE_SECURE = true; // Only over HTTPS
    public static final String COOKIE_SAME_SITE = "Lax";
    
    public static void configureSession(HttpSession session) {
        session.setMaxInactiveInterval(SESSION_TIMEOUT);
    }
}

/**
 * DEMONSTRATION
 */
public class HTTPSessionPattern {
    
    public static void main(String[] args) {
        System.out.println("=== HTTP SESSION PATTERN ===\n");
        
        // Simulate HTTP session
        MockHttpSession session = new MockHttpSession();
        HttpSessionService sessionService = new HttpSessionService();
        UserSessionManager userManager = new UserSessionManager();
        
        // Configure session
        session.setMaxInactiveInterval(SessionConfiguration.SESSION_TIMEOUT);
        
        System.out.println("1. Session Metadata:");
        System.out.println("   Session ID: " + session.getId());
        System.out.println("   Creation Time: " + new Date(session.getCreationTime()));
        System.out.println("   Max Inactive: " + session.getMaxInactiveInterval() + "s");
        System.out.println("   Is New: " + session.isNew());
        System.out.println();
        
        // User login
        Set<String> roles = new HashSet<>(Arrays.asList("USER", "ADMIN"));
        userManager.login(session, 12345L, "john.doe", roles);
        
        System.out.println("2. User Session:");
        System.out.println("   Logged In: " + userManager.isLoggedIn(session));
        System.out.println("   User ID: " + userManager.getUserId(session));
        System.out.println("   Username: " + userManager.getUsername(session));
        System.out.println("   Roles: " + userManager.getUserRoles(session));
        System.out.println("   Has ADMIN role: " + userManager.hasRole(session, "ADMIN"));
        System.out.println();
        
        // Store session attributes
        session.setAttribute("cartItems", 5);
        session.setAttribute("theme", "dark");
        session.setAttribute("language", "en");
        
        System.out.println("3. Session Attributes:");
        Map<String, Object> attributes = sessionService.getAllAttributes(session);
        attributes.forEach((key, value) -> 
            System.out.println("   " + key + ": " + value)
        );
        System.out.println();
        
        // Session tracking
        SessionTrackingService tracker = new SessionTrackingService();
        tracker.trackSession(session);
        tracker.updateLastAccess(session);
        
        System.out.println("4. Session Tracking:");
        SessionTrackingService.SessionInfo info = tracker.getSessionInfo(session.getId());
        System.out.println("   " + info);
        System.out.println("   Active Sessions: " + tracker.getActiveSessionCount());
        System.out.println();
        
        System.out.println("Best Practices:");
        System.out.println("   ✓ Set appropriate session timeout");
        System.out.println("   ✓ Use HttpOnly and Secure cookies");
        System.out.println("   ✓ Invalidate sessions on logout");
        System.out.println("   ✓ Track session activity");
        System.out.println("   ✓ Implement session fixation protection");
    }
    
    // Mock HttpSession for demonstration
    static class MockHttpSession implements HttpSession {
        private final String id = UUID.randomUUID().toString();
        private final long creationTime = System.currentTimeMillis();
        private long lastAccessedTime = System.currentTimeMillis();
        private int maxInactiveInterval = 1800;
        private final Map<String, Object> attributes = new ConcurrentHashMap<>();
        private boolean isNew = true;
        
        @Override public String getId() { return id; }
        @Override public long getCreationTime() { return creationTime; }
        @Override public long getLastAccessedTime() { return lastAccessedTime; }
        @Override public void setMaxInactiveInterval(int interval) { this.maxInactiveInterval = interval; }
        @Override public int getMaxInactiveInterval() { return maxInactiveInterval; }
        @Override public Object getAttribute(String name) { return attributes.get(name); }
        @Override public Enumeration<String> getAttributeNames() { return Collections.enumeration(attributes.keySet()); }
        @Override public void setAttribute(String name, Object value) { attributes.put(name, value); }
        @Override public void removeAttribute(String name) { attributes.remove(name); }
        @Override public void invalidate() { attributes.clear(); }
        @Override public boolean isNew() { return isNew; }
        
        // Unused methods
        @Override public javax.servlet.ServletContext getServletContext() { return null; }
        @Override public javax.servlet.http.HttpSessionContext getSessionContext() { return null; }
        @Override public Object getValue(String name) { return getAttribute(name); }
        @Override public String[] getValueNames() { return attributes.keySet().toArray(new String[0]); }
        @Override public void putValue(String name, Object value) { setAttribute(name, value); }
        @Override public void removeValue(String name) { removeAttribute(name); }
    }
}
