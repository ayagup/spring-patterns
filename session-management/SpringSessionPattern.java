package com.example.session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.http.HttpSessionEventPublisher;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPRING SESSION PATTERN
 * ======================
 * 
 * Purpose:
 * - Replace container-managed HTTP sessions
 * - Abstract session storage implementation
 * - Support multiple storage backends
 * - Enable session sharing across instances
 * 
 * Key Components:
 * 1. @EnableSpringHttpSession - Enable Spring Session
 * 2. SessionRepository - Storage abstraction
 * 3. Session - Spring Session interface
 * 4. HttpSessionEventPublisher - Session events
 * 
 * Features:
 * - Pluggable session storage
 * - RESTful session management
 * - Session events
 * - Multiple concurrent sessions per user
 * - Session attribute indexing
 * 
 * Use Cases:
 * - Clustered applications
 * - Cloud-native apps
 * - Microservices
 * - Custom session storage
 * - Session sharing
 */

// 1. SPRING SESSION CONFIGURATION
@Configuration
@EnableSpringHttpSession
class SpringSessionConfiguration {
    
    @Bean
    public SessionRepository<MapSession> sessionRepository() {
        return new MapSessionRepository();
    }
    
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}

// 2. CUSTOM MAP SESSION IMPLEMENTATION
class MapSession implements Session {
    
    private final String id;
    private final Instant creationTime;
    private Instant lastAccessedTime;
    private Duration maxInactiveInterval;
    private final Map<String, Object> attributes;
    
    public MapSession() {
        this(UUID.randomUUID().toString());
    }
    
    public MapSession(String id) {
        this.id = id;
        this.creationTime = Instant.now();
        this.lastAccessedTime = Instant.now();
        this.maxInactiveInterval = Duration.ofMinutes(30);
        this.attributes = new ConcurrentHashMap<>();
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String changeSessionId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public <T> T getAttribute(String attributeName) {
        @SuppressWarnings("unchecked")
        T value = (T) attributes.get(attributeName);
        return value;
    }
    
    @Override
    public Set<String> getAttributeNames() {
        return new HashSet<>(attributes.keySet());
    }
    
    @Override
    public void setAttribute(String attributeName, Object attributeValue) {
        if (attributeValue == null) {
            removeAttribute(attributeName);
        } else {
            attributes.put(attributeName, attributeValue);
        }
    }
    
    @Override
    public void removeAttribute(String attributeName) {
        attributes.remove(attributeName);
    }
    
    @Override
    public Instant getCreationTime() {
        return creationTime;
    }
    
    @Override
    public void setLastAccessedTime(Instant lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }
    
    @Override
    public Instant getLastAccessedTime() {
        return lastAccessedTime;
    }
    
    @Override
    public void setMaxInactiveInterval(Duration interval) {
        this.maxInactiveInterval = interval;
    }
    
    @Override
    public Duration getMaxInactiveInterval() {
        return maxInactiveInterval;
    }
    
    @Override
    public boolean isExpired() {
        Duration inactiveTime = Duration.between(lastAccessedTime, Instant.now());
        return inactiveTime.compareTo(maxInactiveInterval) > 0;
    }
    
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }
}

// 3. MAP SESSION REPOSITORY
class MapSessionRepository implements SessionRepository<MapSession> {
    
    private final Map<String, MapSession> sessions = new ConcurrentHashMap<>();
    
    @Override
    public MapSession createSession() {
        MapSession session = new MapSession();
        return session;
    }
    
    @Override
    public void save(MapSession session) {
        if (!session.isExpired()) {
            sessions.put(session.getId(), session);
        }
    }
    
    @Override
    public MapSession findById(String id) {
        MapSession session = sessions.get(id);
        if (session != null && !session.isExpired()) {
            session.setLastAccessedTime(Instant.now());
            return session;
        }
        if (session != null) {
            sessions.remove(id);
        }
        return null;
    }
    
    @Override
    public void deleteById(String id) {
        sessions.remove(id);
    }
    
    public Collection<MapSession> findAll() {
        return new ArrayList<>(sessions.values());
    }
    
    public void deleteExpiredSessions() {
        List<String> expiredIds = new ArrayList<>();
        sessions.forEach((id, session) -> {
            if (session.isExpired()) {
                expiredIds.add(id);
            }
        });
        expiredIds.forEach(sessions::remove);
    }
    
    public int size() {
        return sessions.size();
    }
}

// 4. SESSION EVENT LISTENER
class SessionEventListener {
    
    public void handleSessionCreated(Session session) {
        System.out.println("Session created: " + session.getId());
    }
    
    public void handleSessionDestroyed(Session session) {
        System.out.println("Session destroyed: " + session.getId());
    }
    
    public void handleSessionExpired(Session session) {
        System.out.println("Session expired: " + session.getId());
    }
    
    public void handleAttributeAdded(Session session, String name, Object value) {
        System.out.println("Attribute added to session " + session.getId() + 
                         ": " + name + " = " + value);
    }
    
    public void handleAttributeRemoved(Session session, String name) {
        System.out.println("Attribute removed from session " + session.getId() + 
                         ": " + name);
    }
}

// 5. SESSION MANAGER WITH SPRING SESSION
class SpringSessionManager {
    
    private final SessionRepository<MapSession> sessionRepository;
    private final SessionEventListener eventListener;
    
    public SpringSessionManager(SessionRepository<MapSession> sessionRepository,
                               SessionEventListener eventListener) {
        this.sessionRepository = sessionRepository;
        this.eventListener = eventListener;
    }
    
    public MapSession createSession() {
        MapSession session = sessionRepository.createSession();
        eventListener.handleSessionCreated(session);
        return session;
    }
    
    public MapSession getSession(String sessionId) {
        return sessionRepository.findById(sessionId);
    }
    
    public void saveSession(MapSession session) {
        sessionRepository.save(session);
    }
    
    public void deleteSession(String sessionId) {
        MapSession session = sessionRepository.findById(sessionId);
        if (session != null) {
            sessionRepository.deleteById(sessionId);
            eventListener.handleSessionDestroyed(session);
        }
    }
    
    public void setAttribute(MapSession session, String name, Object value) {
        session.setAttribute(name, value);
        sessionRepository.save(session);
        eventListener.handleAttributeAdded(session, name, value);
    }
    
    public void removeAttribute(MapSession session, String name) {
        session.removeAttribute(name);
        sessionRepository.save(session);
        eventListener.handleAttributeRemoved(session, name);
    }
}

// 6. INDEXED SESSION REPOSITORY (Find sessions by attribute)
class IndexedSessionRepository {
    
    private final MapSessionRepository baseRepository;
    private final Map<String, Map<Object, Set<String>>> attributeIndex;
    
    public IndexedSessionRepository(MapSessionRepository baseRepository) {
        this.baseRepository = baseRepository;
        this.attributeIndex = new ConcurrentHashMap<>();
    }
    
    public void save(MapSession session) {
        baseRepository.save(session);
        updateIndex(session);
    }
    
    public Collection<MapSession> findByAttribute(String attributeName, Object attributeValue) {
        Map<Object, Set<String>> index = attributeIndex.get(attributeName);
        if (index == null) {
            return Collections.emptyList();
        }
        
        Set<String> sessionIds = index.get(attributeValue);
        if (sessionIds == null) {
            return Collections.emptyList();
        }
        
        return sessionIds.stream()
            .map(baseRepository::findById)
            .filter(Objects::nonNull)
            .toList();
    }
    
    public Collection<MapSession> findByPrincipal(String principalName) {
        return findByAttribute("SPRING_SECURITY_CONTEXT", principalName);
    }
    
    private void updateIndex(MapSession session) {
        session.getAttributeNames().forEach(attributeName -> {
            Object attributeValue = session.getAttribute(attributeName);
            
            attributeIndex.computeIfAbsent(attributeName, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(attributeValue, k -> ConcurrentHashMap.newKeySet())
                .add(session.getId());
        });
    }
    
    public void removeFromIndex(String sessionId) {
        attributeIndex.values().forEach(index ->
            index.values().forEach(sessionIds -> sessionIds.remove(sessionId))
        );
    }
}

// 7. SESSION ID GENERATOR
interface SessionIdGenerator {
    String generateId();
}

class UuidSessionIdGenerator implements SessionIdGenerator {
    @Override
    public String generateId() {
        return UUID.randomUUID().toString();
    }
}

class Base64SessionIdGenerator implements SessionIdGenerator {
    @Override
    public String generateId() {
        byte[] bytes = new byte[16];
        new Random().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}

/**
 * DEMONSTRATION
 */
public class SpringSessionPattern {
    
    public static void main(String[] args) {
        System.out.println("=== SPRING SESSION PATTERN ===\n");
        
        // Create repository and manager
        MapSessionRepository repository = new MapSessionRepository();
        SessionEventListener eventListener = new SessionEventListener();
        SpringSessionManager manager = new SpringSessionManager(repository, eventListener);
        
        System.out.println("1. Creating Spring Sessions:\n");
        
        // Create session 1
        MapSession session1 = manager.createSession();
        session1.setAttribute("userId", 100L);
        session1.setAttribute("username", "alice");
        session1.setAttribute("role", "ADMIN");
        manager.saveSession(session1);
        System.out.println();
        
        // Create session 2
        MapSession session2 = manager.createSession();
        session2.setAttribute("userId", 200L);
        session2.setAttribute("username", "bob");
        session2.setAttribute("role", "USER");
        manager.saveSession(session2);
        System.out.println();
        
        // Retrieve session
        System.out.println("2. Session Details:");
        MapSession retrieved = manager.getSession(session1.getId());
        System.out.println("   Session ID: " + retrieved.getId());
        System.out.println("   Created: " + retrieved.getCreationTime());
        System.out.println("   Last Accessed: " + retrieved.getLastAccessedTime());
        System.out.println("   Max Inactive: " + retrieved.getMaxInactiveInterval().toMinutes() + " minutes");
        System.out.println("   Attributes: " + retrieved.getAttributes());
        System.out.println();
        
        // Indexed repository
        System.out.println("3. Indexed Session Repository:\n");
        IndexedSessionRepository indexedRepo = new IndexedSessionRepository(repository);
        indexedRepo.save(session1);
        indexedRepo.save(session2);
        
        Collection<MapSession> adminSessions = indexedRepo.findByAttribute("role", "ADMIN");
        System.out.println("   Admin sessions: " + adminSessions.size());
        adminSessions.forEach(s -> 
            System.out.println("     - " + s.getAttribute("username"))
        );
        System.out.println();
        
        // Session expiration
        System.out.println("4. Session Expiration:");
        MapSession expirableSession = manager.createSession();
        expirableSession.setMaxInactiveInterval(Duration.ofSeconds(1));
        manager.saveSession(expirableSession);
        
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("   Is expired: " + expirableSession.isExpired());
        repository.deleteExpiredSessions();
        System.out.println("   Active sessions after cleanup: " + repository.size());
        System.out.println();
        
        // Session ID generators
        System.out.println("5. Session ID Generators:");
        UuidSessionIdGenerator uuidGen = new UuidSessionIdGenerator();
        Base64SessionIdGenerator base64Gen = new Base64SessionIdGenerator();
        
        System.out.println("   UUID: " + uuidGen.generateId());
        System.out.println("   Base64: " + base64Gen.generateId());
        System.out.println();
        
        System.out.println("Advantages:");
        System.out.println("   ✓ Pluggable session storage");
        System.out.println("   ✓ Container-independent sessions");
        System.out.println("   ✓ RESTful session management");
        System.out.println("   ✓ Session attribute indexing");
        System.out.println("   ✓ Multiple concurrent sessions");
        System.out.println();
        
        System.out.println("Storage Options:");
        System.out.println("   • Redis (distributed)");
        System.out.println("   • JDBC (database)");
        System.out.println("   • Hazelcast (in-memory grid)");
        System.out.println("   • MongoDB (document store)");
    }
}
