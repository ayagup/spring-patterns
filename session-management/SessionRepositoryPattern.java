package com.example.session;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SESSION REPOSITORY PATTERN
 * ==========================
 * 
 * Purpose:
 * - Abstract session storage implementation
 * - Decouple session management from storage mechanism
 * - Enable custom session persistence
 * - Support multiple storage backends
 * 
 * Key Components:
 * 1. SessionRepository - Storage abstraction
 * 2. Session - Session entity
 * 3. SessionManager - Business logic
 * 4. ExpirationPolicy - Session expiration
 * 
 * Use Cases:
 * - Custom session storage
 * - Multiple storage backends
 * - Session migration
 * - Testing and mocking
 * - Cloud-native applications
 */

// 1. SESSION ENTITY
class Session implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String id;
    private final Instant creationTime;
    private Instant lastAccessedTime;
    private Duration maxInactiveInterval;
    private final Map<String, Object> attributes;
    private boolean expired;
    
    public Session(String id) {
        this.id = id;
        this.creationTime = Instant.now();
        this.lastAccessedTime = Instant.now();
        this.maxInactiveInterval = Duration.ofMinutes(30);
        this.attributes = new ConcurrentHashMap<>();
        this.expired = false;
    }
    
    public void setAttribute(String name, Object value) {
        if (expired) {
            throw new IllegalStateException("Session has expired");
        }
        attributes.put(name, value);
    }
    
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name, Class<T> type) {
        Object value = attributes.get(name);
        return type.isInstance(value) ? (T) value : null;
    }
    
    public void removeAttribute(String name) {
        attributes.remove(name);
    }
    
    public Set<String> getAttributeNames() {
        return new HashSet<>(attributes.keySet());
    }
    
    public void touch() {
        this.lastAccessedTime = Instant.now();
    }
    
    public boolean isExpired() {
        if (expired) {
            return true;
        }
        Duration inactiveTime = Duration.between(lastAccessedTime, Instant.now());
        return inactiveTime.compareTo(maxInactiveInterval) > 0;
    }
    
    public void expire() {
        this.expired = true;
        this.attributes.clear();
    }
    
    // Getters and setters
    public String getId() { return id; }
    public Instant getCreationTime() { return creationTime; }
    public Instant getLastAccessedTime() { return lastAccessedTime; }
    public void setLastAccessedTime(Instant lastAccessedTime) { this.lastAccessedTime = lastAccessedTime; }
    public Duration getMaxInactiveInterval() { return maxInactiveInterval; }
    public void setMaxInactiveInterval(Duration maxInactiveInterval) { this.maxInactiveInterval = maxInactiveInterval; }
    public Map<String, Object> getAttributes() { return new HashMap<>(attributes); }
    
    @Override
    public String toString() {
        return String.format("Session[id=%s, created=%s, accessed=%s, attributes=%d]",
            id, creationTime, lastAccessedTime, attributes.size());
    }
}

// 2. SESSION REPOSITORY INTERFACE
interface SessionRepository {
    Session createSession();
    Session findById(String id);
    void save(Session session);
    void deleteById(String id);
    Collection<Session> findAll();
    Collection<Session> findExpiredSessions();
    void deleteExpiredSessions();
}

// 3. IN-MEMORY SESSION REPOSITORY
@Repository
class InMemorySessionRepository implements SessionRepository {
    
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    
    @Override
    public Session createSession() {
        String id = UUID.randomUUID().toString();
        Session session = new Session(id);
        sessions.put(id, session);
        return session;
    }
    
    @Override
    public Session findById(String id) {
        Session session = sessions.get(id);
        if (session != null && !session.isExpired()) {
            session.touch();
            return session;
        }
        return null;
    }
    
    @Override
    public void save(Session session) {
        if (session != null && !session.isExpired()) {
            sessions.put(session.getId(), session);
        }
    }
    
    @Override
    public void deleteById(String id) {
        Session session = sessions.remove(id);
        if (session != null) {
            session.expire();
        }
    }
    
    @Override
    public Collection<Session> findAll() {
        return new ArrayList<>(sessions.values());
    }
    
    @Override
    public Collection<Session> findExpiredSessions() {
        return sessions.values().stream()
            .filter(Session::isExpired)
            .collect(Collectors.toList());
    }
    
    @Override
    public void deleteExpiredSessions() {
        List<String> expiredIds = sessions.values().stream()
            .filter(Session::isExpired)
            .map(Session::getId)
            .collect(Collectors.toList());
        
        expiredIds.forEach(this::deleteById);
    }
    
    public int size() {
        return sessions.size();
    }
    
    public void clear() {
        sessions.clear();
    }
}

// 4. SESSION MANAGER SERVICE
@Service
class SessionManager {
    
    private final SessionRepository sessionRepository;
    
    public SessionManager(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }
    
    public Session createSession() {
        return sessionRepository.createSession();
    }
    
    public Session getSession(String sessionId) {
        Session session = sessionRepository.findById(sessionId);
        if (session == null) {
            throw new SessionNotFoundException("Session not found: " + sessionId);
        }
        return session;
    }
    
    public Session getOrCreateSession(String sessionId) {
        if (sessionId == null) {
            return createSession();
        }
        
        Session session = sessionRepository.findById(sessionId);
        return session != null ? session : createSession();
    }
    
    public void saveSession(Session session) {
        sessionRepository.save(session);
    }
    
    public void invalidateSession(String sessionId) {
        sessionRepository.deleteById(sessionId);
    }
    
    public Collection<Session> getAllActiveSessions() {
        return sessionRepository.findAll().stream()
            .filter(session -> !session.isExpired())
            .collect(Collectors.toList());
    }
    
    public int getActiveSessionCount() {
        return (int) sessionRepository.findAll().stream()
            .filter(session -> !session.isExpired())
            .count();
    }
    
    public void cleanupExpiredSessions() {
        sessionRepository.deleteExpiredSessions();
    }
    
    public SessionStatistics getStatistics() {
        Collection<Session> all = sessionRepository.findAll();
        long active = all.stream().filter(s -> !s.isExpired()).count();
        long expired = all.stream().filter(Session::isExpired).count();
        
        return new SessionStatistics(
            all.size(),
            (int) active,
            (int) expired
        );
    }
    
    static class SessionStatistics {
        private final int total;
        private final int active;
        private final int expired;
        
        public SessionStatistics(int total, int active, int expired) {
            this.total = total;
            this.active = active;
            this.expired = expired;
        }
        
        public int getTotal() { return total; }
        public int getActive() { return active; }
        public int getExpired() { return expired; }
        
        @Override
        public String toString() {
            return String.format("Statistics[total=%d, active=%d, expired=%d]",
                total, active, expired);
        }
    }
    
    static class SessionNotFoundException extends RuntimeException {
        public SessionNotFoundException(String message) {
            super(message);
        }
    }
}

// 5. SESSION EVENT PUBLISHER
@Service
class SessionEventPublisher {
    
    private final List<SessionEventListener> listeners = new ArrayList<>();
    
    public void addListener(SessionEventListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(SessionEventListener listener) {
        listeners.remove(listener);
    }
    
    public void publishCreated(Session session) {
        SessionEvent event = new SessionEvent(session, SessionEventType.CREATED);
        listeners.forEach(listener -> listener.onSessionEvent(event));
    }
    
    public void publishDestroyed(Session session) {
        SessionEvent event = new SessionEvent(session, SessionEventType.DESTROYED);
        listeners.forEach(listener -> listener.onSessionEvent(event));
    }
    
    public void publishExpired(Session session) {
        SessionEvent event = new SessionEvent(session, SessionEventType.EXPIRED);
        listeners.forEach(listener -> listener.onSessionEvent(event));
    }
    
    public void publishAttributeAdded(Session session, String attributeName, Object attributeValue) {
        SessionEvent event = new SessionEvent(session, SessionEventType.ATTRIBUTE_ADDED);
        event.setAttributeName(attributeName);
        event.setAttributeValue(attributeValue);
        listeners.forEach(listener -> listener.onSessionEvent(event));
    }
    
    interface SessionEventListener {
        void onSessionEvent(SessionEvent event);
    }
    
    enum SessionEventType {
        CREATED, DESTROYED, EXPIRED, ATTRIBUTE_ADDED, ATTRIBUTE_REMOVED
    }
    
    static class SessionEvent {
        private final Session session;
        private final SessionEventType type;
        private final Instant timestamp;
        private String attributeName;
        private Object attributeValue;
        
        public SessionEvent(Session session, SessionEventType type) {
            this.session = session;
            this.type = type;
            this.timestamp = Instant.now();
        }
        
        // Getters and setters
        public Session getSession() { return session; }
        public SessionEventType getType() { return type; }
        public Instant getTimestamp() { return timestamp; }
        public String getAttributeName() { return attributeName; }
        public void setAttributeName(String attributeName) { this.attributeName = attributeName; }
        public Object getAttributeValue() { return attributeValue; }
        public void setAttributeValue(Object attributeValue) { this.attributeValue = attributeValue; }
    }
}

/**
 * DEMONSTRATION
 */
public class SessionRepositoryPattern {
    
    public static void main(String[] args) {
        System.out.println("=== SESSION REPOSITORY PATTERN ===\n");
        
        // Create repository and manager
        InMemorySessionRepository repository = new InMemorySessionRepository();
        SessionManager manager = new SessionManager(repository);
        SessionEventPublisher eventPublisher = new SessionEventPublisher();
        
        // Add event listener
        eventPublisher.addListener(event -> 
            System.out.println("  Event: " + event.getType() + " - " + event.getSession().getId())
        );
        
        // Create sessions
        System.out.println("1. Creating Sessions:");
        Session session1 = manager.createSession();
        eventPublisher.publishCreated(session1);
        session1.setAttribute("userId", 100L);
        session1.setAttribute("username", "alice");
        
        Session session2 = manager.createSession();
        eventPublisher.publishCreated(session2);
        session2.setAttribute("userId", 200L);
        session2.setAttribute("username", "bob");
        
        Session session3 = manager.createSession();
        eventPublisher.publishCreated(session3);
        session3.setMaxInactiveInterval(Duration.ofSeconds(1)); // Expire quickly
        System.out.println();
        
        // Retrieve session
        System.out.println("2. Session Details:");
        Session retrieved = manager.getSession(session1.getId());
        System.out.println("   " + retrieved);
        System.out.println("   User ID: " + retrieved.getAttribute("userId"));
        System.out.println("   Username: " + retrieved.getAttribute("username"));
        System.out.println("   Attributes: " + retrieved.getAttributeNames());
        System.out.println();
        
        // Get statistics
        System.out.println("3. Session Statistics:");
        SessionManager.SessionStatistics stats = manager.getStatistics();
        System.out.println("   " + stats);
        System.out.println();
        
        // Wait for session to expire
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Cleanup expired sessions
        System.out.println("4. Cleanup Expired Sessions:");
        Collection<Session> expired = repository.findExpiredSessions();
        System.out.println("   Expired count: " + expired.size());
        expired.forEach(s -> eventPublisher.publishExpired(s));
        manager.cleanupExpiredSessions();
        
        System.out.println("   After cleanup:");
        stats = manager.getStatistics();
        System.out.println("   " + stats);
        System.out.println();
        
        // Invalidate session
        System.out.println("5. Invalidate Session:");
        manager.invalidateSession(session1.getId());
        eventPublisher.publishDestroyed(session1);
        System.out.println("   Active sessions: " + manager.getActiveSessionCount());
        System.out.println();
        
        System.out.println("Best Practices:");
        System.out.println("   ✓ Abstract storage with repository pattern");
        System.out.println("   ✓ Implement session expiration");
        System.out.println("   ✓ Publish session events");
        System.out.println("   ✓ Use concurrent collections");
        System.out.println("   ✓ Schedule cleanup tasks");
    }
}
