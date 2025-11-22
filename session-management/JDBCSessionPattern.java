import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDBC SESSION PATTERN
 * ====================
 * 
 * Purpose:
 * - Store session data in relational database
 * - Persistent session storage
 * - Session sharing across application instances
 * - Database-backed session management
 * 
 * Key Components:
 * 1. JDBC Session Repository
 * 2. Session Table Schema
 * 3. Session Serialization
 * 4. Cleanup Job
 * 
 * Database Schema:
 * CREATE TABLE SPRING_SESSION (
 *   PRIMARY_ID CHAR(36) NOT NULL,
 *   SESSION_ID CHAR(36) NOT NULL,
 *   CREATION_TIME BIGINT NOT NULL,
 *   LAST_ACCESS_TIME BIGINT NOT NULL,
 *   MAX_INACTIVE_INTERVAL INT NOT NULL,
 *   EXPIRY_TIME BIGINT NOT NULL,
 *   PRINCIPAL_NAME VARCHAR(100),
 *   CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
 * );
 * 
 * CREATE TABLE SPRING_SESSION_ATTRIBUTES (
 *   SESSION_PRIMARY_ID CHAR(36) NOT NULL,
 *   ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
 *   ATTRIBUTE_BYTES BLOB NOT NULL,
 *   CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
 *   CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) 
 *     REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
 * );
 * 
 * CREATE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
 * CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
 * CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);
 * 
 * Use Cases:
 * - Clustered applications
 * - Session persistence across restarts
 * - Audit trail requirements
 * - Long-lived sessions
 * - Regulatory compliance
 */

// 1. JDBC SESSION IMPLEMENTATION
class JdbcSession {
    private final String id;
    private final String primaryId;
    private final long creationTime;
    private long lastAccessTime;
    private int maxInactiveInterval;
    private String principalName;
    private final Map<String, Object> attributes;
    private boolean isNew;
    private boolean modified;
    
    public JdbcSession() {
        this.id = UUID.randomUUID().toString();
        this.primaryId = UUID.randomUUID().toString();
        this.creationTime = Instant.now().toEpochMilli();
        this.lastAccessTime = creationTime;
        this.maxInactiveInterval = 1800; // 30 minutes
        this.attributes = new ConcurrentHashMap<>();
        this.isNew = true;
        this.modified = false;
    }
    
    public JdbcSession(String id, String primaryId, long creationTime, 
                       long lastAccessTime, int maxInactiveInterval, String principalName) {
        this.id = id;
        this.primaryId = primaryId;
        this.creationTime = creationTime;
        this.lastAccessTime = lastAccessTime;
        this.maxInactiveInterval = maxInactiveInterval;
        this.principalName = principalName;
        this.attributes = new ConcurrentHashMap<>();
        this.isNew = false;
        this.modified = false;
    }
    
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
        modified = true;
    }
    
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    public void removeAttribute(String name) {
        attributes.remove(name);
        modified = true;
    }
    
    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }
    
    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
        modified = true;
    }
    
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
        modified = true;
    }
    
    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
        modified = true;
    }
    
    public long getExpiryTime() {
        return lastAccessTime + (maxInactiveInterval * 1000L);
    }
    
    public boolean isExpired() {
        return Instant.now().toEpochMilli() > getExpiryTime();
    }
    
    // Getters
    public String getId() { return id; }
    public String getPrimaryId() { return primaryId; }
    public long getCreationTime() { return creationTime; }
    public long getLastAccessTime() { return lastAccessTime; }
    public int getMaxInactiveInterval() { return maxInactiveInterval; }
    public String getPrincipalName() { return principalName; }
    public Map<String, Object> getAttributes() { return new HashMap<>(attributes); }
    public boolean isNew() { return isNew; }
    public boolean isModified() { return modified; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
}

// 2. JDBC SESSION REPOSITORY
class JdbcSessionRepository {
    private final Connection connection;
    
    public JdbcSessionRepository(Connection connection) {
        this.connection = connection;
        initializeSchema();
    }
    
    private void initializeSchema() {
        String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS SPRING_SESSION (
                PRIMARY_ID VARCHAR(36) NOT NULL,
                SESSION_ID VARCHAR(36) NOT NULL,
                CREATION_TIME BIGINT NOT NULL,
                LAST_ACCESS_TIME BIGINT NOT NULL,
                MAX_INACTIVE_INTERVAL INT NOT NULL,
                EXPIRY_TIME BIGINT NOT NULL,
                PRINCIPAL_NAME VARCHAR(100),
                CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
            )
            """,
            "CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID)",
            "CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME)",
            "CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME)",
            """
            CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (
                SESSION_PRIMARY_ID VARCHAR(36) NOT NULL,
                ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
                ATTRIBUTE_BYTES BLOB NOT NULL,
                CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME)
            )
            """
        };
        
        try (Statement stmt = connection.createStatement()) {
            for (String sql : createStatements) {
                stmt.execute(sql);
            }
            System.out.println("  [JDBC] Database schema initialized");
        } catch (SQLException e) {
            System.err.println("  [JDBC] Schema initialization failed: " + e.getMessage());
        }
    }
    
    public JdbcSession createSession() {
        JdbcSession session = new JdbcSession();
        System.out.println("  [JDBC] Created new session: " + session.getId());
        return session;
    }
    
    public void save(JdbcSession session) {
        try {
            if (session.isNew()) {
                insertSession(session);
                session.setNew(false);
            } else if (session.isModified()) {
                updateSession(session);
            }
            saveAttributes(session);
            System.out.println("  [JDBC] Saved session: " + session.getId());
        } catch (SQLException e) {
            System.err.println("  [JDBC] Failed to save session: " + e.getMessage());
        }
    }
    
    private void insertSession(JdbcSession session) throws SQLException {
        String sql = """
            INSERT INTO SPRING_SESSION 
            (PRIMARY_ID, SESSION_ID, CREATION_TIME, LAST_ACCESS_TIME, 
             MAX_INACTIVE_INTERVAL, EXPIRY_TIME, PRINCIPAL_NAME)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, session.getPrimaryId());
            ps.setString(2, session.getId());
            ps.setLong(3, session.getCreationTime());
            ps.setLong(4, session.getLastAccessTime());
            ps.setInt(5, session.getMaxInactiveInterval());
            ps.setLong(6, session.getExpiryTime());
            ps.setString(7, session.getPrincipalName());
            ps.executeUpdate();
        }
    }
    
    private void updateSession(JdbcSession session) throws SQLException {
        String sql = """
            UPDATE SPRING_SESSION 
            SET LAST_ACCESS_TIME = ?, MAX_INACTIVE_INTERVAL = ?, 
                EXPIRY_TIME = ?, PRINCIPAL_NAME = ?
            WHERE PRIMARY_ID = ?
            """;
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, session.getLastAccessTime());
            ps.setInt(2, session.getMaxInactiveInterval());
            ps.setLong(3, session.getExpiryTime());
            ps.setString(4, session.getPrincipalName());
            ps.setString(5, session.getPrimaryId());
            ps.executeUpdate();
        }
    }
    
    private void saveAttributes(JdbcSession session) throws SQLException {
        // Delete existing attributes
        String deleteSql = "DELETE FROM SPRING_SESSION_ATTRIBUTES WHERE SESSION_PRIMARY_ID = ?";
        try (PreparedStatement ps = connection.prepareStatement(deleteSql)) {
            ps.setString(1, session.getPrimaryId());
            ps.executeUpdate();
        }
        
        // Insert current attributes
        String insertSql = """
            INSERT INTO SPRING_SESSION_ATTRIBUTES 
            (SESSION_PRIMARY_ID, ATTRIBUTE_NAME, ATTRIBUTE_BYTES)
            VALUES (?, ?, ?)
            """;
        
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            for (Map.Entry<String, Object> entry : session.getAttributes().entrySet()) {
                ps.setString(1, session.getPrimaryId());
                ps.setString(2, entry.getKey());
                ps.setBytes(3, serializeAttribute(entry.getValue()));
                ps.executeUpdate();
            }
        }
    }
    
    public JdbcSession findById(String sessionId) {
        String sql = """
            SELECT PRIMARY_ID, SESSION_ID, CREATION_TIME, LAST_ACCESS_TIME,
                   MAX_INACTIVE_INTERVAL, PRINCIPAL_NAME
            FROM SPRING_SESSION
            WHERE SESSION_ID = ? AND EXPIRY_TIME > ?
            """;
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            ps.setLong(2, Instant.now().toEpochMilli());
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JdbcSession session = new JdbcSession(
                        rs.getString("SESSION_ID"),
                        rs.getString("PRIMARY_ID"),
                        rs.getLong("CREATION_TIME"),
                        rs.getLong("LAST_ACCESS_TIME"),
                        rs.getInt("MAX_INACTIVE_INTERVAL"),
                        rs.getString("PRINCIPAL_NAME")
                    );
                    loadAttributes(session);
                    System.out.println("  [JDBC] Loaded session: " + sessionId);
                    return session;
                }
            }
        } catch (SQLException e) {
            System.err.println("  [JDBC] Failed to load session: " + e.getMessage());
        }
        
        return null;
    }
    
    private void loadAttributes(JdbcSession session) throws SQLException {
        String sql = """
            SELECT ATTRIBUTE_NAME, ATTRIBUTE_BYTES
            FROM SPRING_SESSION_ATTRIBUTES
            WHERE SESSION_PRIMARY_ID = ?
            """;
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, session.getPrimaryId());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("ATTRIBUTE_NAME");
                    byte[] bytes = rs.getBytes("ATTRIBUTE_BYTES");
                    Object value = deserializeAttribute(bytes);
                    session.setAttribute(name, value);
                }
            }
        }
    }
    
    public void deleteById(String sessionId) {
        String sql = "DELETE FROM SPRING_SESSION WHERE SESSION_ID = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                System.out.println("  [JDBC] Deleted session: " + sessionId);
            }
        } catch (SQLException e) {
            System.err.println("  [JDBC] Failed to delete session: " + e.getMessage());
        }
    }
    
    public List<JdbcSession> findByPrincipalName(String principalName) {
        List<JdbcSession> sessions = new ArrayList<>();
        String sql = """
            SELECT PRIMARY_ID, SESSION_ID, CREATION_TIME, LAST_ACCESS_TIME,
                   MAX_INACTIVE_INTERVAL, PRINCIPAL_NAME
            FROM SPRING_SESSION
            WHERE PRINCIPAL_NAME = ? AND EXPIRY_TIME > ?
            """;
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, principalName);
            ps.setLong(2, Instant.now().toEpochMilli());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JdbcSession session = new JdbcSession(
                        rs.getString("SESSION_ID"),
                        rs.getString("PRIMARY_ID"),
                        rs.getLong("CREATION_TIME"),
                        rs.getLong("LAST_ACCESS_TIME"),
                        rs.getInt("MAX_INACTIVE_INTERVAL"),
                        rs.getString("PRINCIPAL_NAME")
                    );
                    loadAttributes(session);
                    sessions.add(session);
                }
            }
        } catch (SQLException e) {
            System.err.println("  [JDBC] Failed to find sessions: " + e.getMessage());
        }
        
        return sessions;
    }
    
    public int cleanupExpiredSessions() {
        String sql = "DELETE FROM SPRING_SESSION WHERE EXPIRY_TIME <= ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, Instant.now().toEpochMilli());
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                System.out.println("  [JDBC] Cleaned up " + deleted + " expired sessions");
            }
            return deleted;
        } catch (SQLException e) {
            System.err.println("  [JDBC] Cleanup failed: " + e.getMessage());
            return 0;
        }
    }
    
    public List<SessionInfo> getAllSessions() {
        List<SessionInfo> sessions = new ArrayList<>();
        String sql = """
            SELECT SESSION_ID, PRINCIPAL_NAME, CREATION_TIME, 
                   LAST_ACCESS_TIME, MAX_INACTIVE_INTERVAL, EXPIRY_TIME
            FROM SPRING_SESSION
            WHERE EXPIRY_TIME > ?
            ORDER BY LAST_ACCESS_TIME DESC
            """;
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, Instant.now().toEpochMilli());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sessions.add(new SessionInfo(
                        rs.getString("SESSION_ID"),
                        rs.getString("PRINCIPAL_NAME"),
                        Instant.ofEpochMilli(rs.getLong("CREATION_TIME")),
                        Instant.ofEpochMilli(rs.getLong("LAST_ACCESS_TIME")),
                        rs.getInt("MAX_INACTIVE_INTERVAL"),
                        Instant.ofEpochMilli(rs.getLong("EXPIRY_TIME"))
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("  [JDBC] Failed to get sessions: " + e.getMessage());
        }
        
        return sessions;
    }
    
    private byte[] serializeAttribute(Object value) {
        // Simple string serialization for demo
        return value.toString().getBytes();
    }
    
    private Object deserializeAttribute(byte[] bytes) {
        // Simple string deserialization for demo
        return new String(bytes);
    }
    
    record SessionInfo(
        String sessionId,
        String principalName,
        Instant creationTime,
        Instant lastAccessTime,
        int maxInactiveInterval,
        Instant expiryTime
    ) {}
}

// 3. SESSION CLEANUP SCHEDULER
class JdbcSessionCleanupTask implements Runnable {
    private final JdbcSessionRepository repository;
    private final long intervalMillis;
    private volatile boolean running = true;
    
    public JdbcSessionCleanupTask(JdbcSessionRepository repository, long intervalMillis) {
        this.repository = repository;
        this.intervalMillis = intervalMillis;
    }
    
    @Override
    public void run() {
        System.out.println("  [JDBC Cleanup] Task started");
        
        while (running) {
            try {
                Thread.sleep(intervalMillis);
                int deleted = repository.cleanupExpiredSessions();
                System.out.println("  [JDBC Cleanup] Deleted " + deleted + " expired sessions");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("  [JDBC Cleanup] Task stopped");
    }
    
    public void stop() {
        running = false;
    }
}

// 4. JDBC SESSION SERVICE
class JdbcSessionService {
    private final JdbcSessionRepository repository;
    private final JdbcSessionCleanupTask cleanupTask;
    private final Thread cleanupThread;
    
    public JdbcSessionService(Connection connection) {
        this.repository = new JdbcSessionRepository(connection);
        this.cleanupTask = new JdbcSessionCleanupTask(repository, 60000); // 1 minute
        this.cleanupThread = new Thread(cleanupTask, "Session-Cleanup");
        this.cleanupThread.setDaemon(true);
        this.cleanupThread.start();
    }
    
    public JdbcSession createSession(String principalName) {
        JdbcSession session = repository.createSession();
        session.setPrincipalName(principalName);
        repository.save(session);
        return session;
    }
    
    public JdbcSession getSession(String sessionId) {
        JdbcSession session = repository.findById(sessionId);
        if (session != null && !session.isExpired()) {
            session.setLastAccessTime(Instant.now().toEpochMilli());
            repository.save(session);
            return session;
        }
        return null;
    }
    
    public void updateSession(JdbcSession session) {
        session.setLastAccessTime(Instant.now().toEpochMilli());
        repository.save(session);
    }
    
    public void deleteSession(String sessionId) {
        repository.deleteById(sessionId);
    }
    
    public List<JdbcSession> findUserSessions(String principalName) {
        return repository.findByPrincipalName(principalName);
    }
    
    public void invalidateUserSessions(String principalName) {
        List<JdbcSession> sessions = repository.findByPrincipalName(principalName);
        sessions.forEach(session -> repository.deleteById(session.getId()));
        System.out.println("  [JDBC] Invalidated " + sessions.size() + 
                         " sessions for user: " + principalName);
    }
    
    public List<JdbcSessionRepository.SessionInfo> getAllActiveSessions() {
        return repository.getAllSessions();
    }
    
    public void shutdown() {
        cleanupTask.stop();
        cleanupThread.interrupt();
    }
}

/**
 * DEMONSTRATION
 */
public class JDBCSessionPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== JDBC SESSION PATTERN DEMONSTRATION ===\n");
        
        // Create in-memory database connection
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:testdb");
        JdbcSessionService sessionService = new JdbcSessionService(connection);
        
        demonstrateSessionLifecycle(sessionService);
        demonstrateSessionAttributes(sessionService);
        demonstrateUserSessions(sessionService);
        demonstrateSessionCleanup(sessionService);
        demonstrateSessionQuery(sessionService);
        printBestPractices();
        
        sessionService.shutdown();
        connection.close();
    }
    
    private static void demonstrateSessionLifecycle(JdbcSessionService service) {
        System.out.println("1. SESSION LIFECYCLE:");
        
        // Create session
        JdbcSession session = service.createSession("user123");
        System.out.println("   Created session: " + session.getId());
        System.out.println("   Principal: " + session.getPrincipalName());
        
        // Retrieve session
        JdbcSession retrieved = service.getSession(session.getId());
        System.out.println("   Retrieved session: " + (retrieved != null));
        
        // Delete session
        service.deleteSession(session.getId());
        System.out.println("   Deleted session");
        
        // Try to retrieve deleted session
        JdbcSession notFound = service.getSession(session.getId());
        System.out.println("   Session after delete: " + (notFound == null ? "null" : "found"));
        
        System.out.println();
    }
    
    private static void demonstrateSessionAttributes(JdbcSessionService service) {
        System.out.println("2. SESSION ATTRIBUTES:");
        
        JdbcSession session = service.createSession("user456");
        
        // Set attributes
        session.setAttribute("username", "john.doe");
        session.setAttribute("email", "john@example.com");
        session.setAttribute("role", "ADMIN");
        session.setAttribute("loginTime", Instant.now().toString());
        
        service.updateSession(session);
        System.out.println("   Set 4 attributes");
        
        // Retrieve session and verify attributes
        JdbcSession retrieved = service.getSession(session.getId());
        System.out.println("   Retrieved attributes:");
        retrieved.getAttributeNames().forEach(name ->
            System.out.println("     " + name + " = " + retrieved.getAttribute(name))
        );
        
        service.deleteSession(session.getId());
        System.out.println();
    }
    
    private static void demonstrateUserSessions(JdbcSessionService service) {
        System.out.println("3. USER SESSIONS:");
        
        // Create multiple sessions for same user
        String user = "user789";
        JdbcSession session1 = service.createSession(user);
        JdbcSession session2 = service.createSession(user);
        JdbcSession session3 = service.createSession(user);
        
        System.out.println("   Created 3 sessions for: " + user);
        
        // Find all user sessions
        List<JdbcSession> userSessions = service.findUserSessions(user);
        System.out.println("   Found " + userSessions.size() + " active sessions");
        
        // Invalidate all user sessions
        service.invalidateUserSessions(user);
        
        // Verify
        userSessions = service.findUserSessions(user);
        System.out.println("   Sessions after invalidation: " + userSessions.size());
        
        System.out.println();
    }
    
    private static void demonstrateSessionCleanup(JdbcSessionService service) 
            throws InterruptedException {
        System.out.println("4. SESSION CLEANUP:");
        
        // Create session with short timeout
        JdbcSession session = service.createSession("tempuser");
        session.setMaxInactiveInterval(2); // 2 seconds
        service.updateSession(session);
        
        System.out.println("   Created session with 2-second timeout");
        System.out.println("   Session ID: " + session.getId());
        
        // Wait for expiration
        System.out.println("   Waiting for session to expire...");
        Thread.sleep(3000);
        
        // Try to retrieve
        JdbcSession expired = service.getSession(session.getId());
        System.out.println("   Session after timeout: " + 
                         (expired == null ? "expired" : "still active"));
        
        System.out.println();
    }
    
    private static void demonstrateSessionQuery(JdbcSessionService service) {
        System.out.println("5. SESSION QUERY:");
        
        // Create sample sessions
        service.createSession("alice");
        service.createSession("bob");
        service.createSession("charlie");
        
        // Get all active sessions
        List<JdbcSessionRepository.SessionInfo> sessions = 
            service.getAllActiveSessions();
        
        System.out.println("   Active Sessions: " + sessions.size());
        sessions.forEach(info ->
            System.out.println("     " + info.sessionId() + 
                             " - " + info.principalName() +
                             " (expires: " + info.expiryTime() + ")")
        );
        
        System.out.println();
    }
    
    private static void printBestPractices() {
        System.out.println("BEST PRACTICES:");
        System.out.println("================");
        System.out.println("1. Create proper database indexes");
        System.out.println("2. Use connection pooling");
        System.out.println("3. Schedule regular cleanup jobs");
        System.out.println("4. Monitor database size");
        System.out.println("5. Use prepared statements");
        System.out.println("6. Handle serialization carefully");
        System.out.println("7. Set appropriate timeout values");
        System.out.println("8. Implement efficient queries");
        System.out.println("9. Use transactions for consistency");
        System.out.println("10. Archive old session data");
        
        System.out.println("\nADVANTAGES:");
        System.out.println("✓ Persistent across restarts");
        System.out.println("✓ Shared across instances");
        System.out.println("✓ SQL query capabilities");
        System.out.println("✓ ACID compliance");
        System.out.println("✓ Audit trail support");
        
        System.out.println("\nDISADVANTAGES:");
        System.out.println("✗ Slower than in-memory");
        System.out.println("✗ Database overhead");
        System.out.println("✗ Requires cleanup jobs");
        System.out.println("✗ Serialization complexity");
        System.out.println("✗ Scaling challenges");
    }
}
