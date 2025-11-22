package com.example.miscellaneous.templatecallback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

/**
 * Template Callback Pattern - Demonstrates Spring's Template-Callback Pattern
 * 
 * This pattern shows how to:
 * 1. Implement template methods with callbacks
 * 2. Create JdbcTemplate-style patterns
 * 3. Use ResultSetExtractor callbacks
 * 4. Implement RowMapper callbacks
 * 5. Handle resource management in templates
 * 6. Create reusable template operations
 * 7. Implement connection callbacks
 * 8. Use statement callbacks
 * 9. Handle transaction management
 * 10. Create custom template classes
 * 
 * Key Concepts:
 * - Template Method: Defines algorithm skeleton
 * - Callback: Pluggable behavior injected into template
 * - Resource Management: Template handles resource lifecycle
 * - Exception Translation: Template translates checked exceptions
 * - Reusability: Template code reused across operations
 * 
 * Template-Callback Benefits:
 * 1. Eliminates boilerplate code
 * 2. Consistent resource management
 * 3. Centralized exception handling
 * 4. Transaction boundary management
 * 5. Testability
 * 
 * Common Template Patterns:
 * - JdbcTemplate: Database operations
 * - RestTemplate: HTTP operations
 * - JmsTemplate: Messaging operations
 * - RedisTemplate: Redis operations
 * - MongoTemplate: MongoDB operations
 * 
 * Dependencies:
 * - spring-context
 * - spring-jdbc
 * - spring-boot-starter-web
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class TemplateCallbackPattern {
    
    public static void main(String[] args) {
        var context = SpringApplication.run(TemplateCallbackPattern.class, args);
        demonstrateTemplateCallback(context);
    }
    
    /**
     * Demonstrates template-callback patterns
     */
    private static void demonstrateTemplateCallback(org.springframework.context.ApplicationContext context) {
        System.out.println("=== Template Callback Pattern Demonstrations ===\n");
        
        DataAccessTemplate template = context.getBean(DataAccessTemplate.class);
        
        // Demo 1: Simple query with RowMapper
        demonstrateRowMapper(template);
        
        // Demo 2: Query with ResultSetExtractor
        demonstrateResultSetExtractor(template);
        
        // Demo 3: Execute with ConnectionCallback
        demonstrateConnectionCallback(template);
        
        // Demo 4: Update with PreparedStatementCallback
        demonstratePreparedStatementCallback(template);
    }
    
    /**
     * Demonstrates RowMapper callback
     */
    private static void demonstrateRowMapper(DataAccessTemplate template) {
        System.out.println("1. RowMapper Callback:");
        
        List<User> users = template.query("SELECT * FROM users", rs -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            return user;
        });
        
        System.out.println("   Retrieved " + users.size() + " users");
        System.out.println();
    }
    
    /**
     * Demonstrates ResultSetExtractor callback
     */
    private static void demonstrateResultSetExtractor(DataAccessTemplate template) {
        System.out.println("2. ResultSetExtractor Callback:");
        
        Map<Long, User> userMap = template.query("SELECT * FROM users", rs -> {
            Map<Long, User> map = new HashMap<>();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                map.put(user.getId(), user);
            }
            return map;
        });
        
        System.out.println("   Retrieved map with " + userMap.size() + " entries");
        System.out.println();
    }
    
    /**
     * Demonstrates ConnectionCallback
     */
    private static void demonstrateConnectionCallback(DataAccessTemplate template) {
        System.out.println("3. ConnectionCallback:");
        
        DatabaseInfo info = template.execute(conn -> {
            DatabaseMetaData metaData = conn.getMetaData();
            DatabaseInfo dbInfo = new DatabaseInfo();
            dbInfo.setProductName(metaData.getDatabaseProductName());
            dbInfo.setProductVersion(metaData.getDatabaseProductVersion());
            dbInfo.setDriverName(metaData.getDriverName());
            return dbInfo;
        });
        
        System.out.println("   Database: " + info.getProductName());
        System.out.println();
    }
    
    /**
     * Demonstrates PreparedStatementCallback
     */
    private static void demonstratePreparedStatementCallback(DataAccessTemplate template) {
        System.out.println("4. PreparedStatementCallback:");
        
        int rowsAffected = template.update("UPDATE users SET email = ? WHERE id = ?", 
            stmt -> {
                stmt.setString(1, "new@example.com");
                stmt.setLong(2, 1L);
                return stmt.executeUpdate();
            });
        
        System.out.println("   Rows affected: " + rowsAffected);
        System.out.println();
    }
}

// ============================================================================
// Callback Interfaces
// ============================================================================

/**
 * RowMapper callback - maps one row to an object
 */
@FunctionalInterface
interface RowMapper<T> {
    T mapRow(ResultSet rs) throws SQLException;
}

/**
 * ResultSetExtractor callback - processes entire ResultSet
 */
@FunctionalInterface
interface ResultSetExtractor<T> {
    T extractData(ResultSet rs) throws SQLException;
}

/**
 * ConnectionCallback - executes actions on a connection
 */
@FunctionalInterface
interface ConnectionCallback<T> {
    T doInConnection(Connection conn) throws SQLException;
}

/**
 * StatementCallback - executes actions with a statement
 */
@FunctionalInterface
interface StatementCallback<T> {
    T doInStatement(Statement stmt) throws SQLException;
}

/**
 * PreparedStatementCallback - executes actions with prepared statement
 */
@FunctionalInterface
interface PreparedStatementCallback<T> {
    T doInPreparedStatement(PreparedStatement ps) throws SQLException;
}

/**
 * PreparedStatementCreator - creates prepared statements
 */
@FunctionalInterface
interface PreparedStatementCreator {
    PreparedStatement createPreparedStatement(Connection conn) throws SQLException;
}

/**
 * PreparedStatementSetter - sets parameters on prepared statement
 */
@FunctionalInterface
interface PreparedStatementSetter {
    void setValues(PreparedStatement ps) throws SQLException;
}

// ============================================================================
// Template Classes
// ============================================================================

/**
 * Data access template demonstrating template-callback pattern
 */
@Service
class DataAccessTemplate {
    
    // Simulated connection (in real app, use DataSource)
    private Connection getConnection() {
        // Return mock connection for demonstration
        return null;
    }
    
    private void releaseConnection(Connection conn) {
        // Release connection back to pool
    }
    
    /**
     * Query for list of objects using RowMapper
     */
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("Query failed: " + sql, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(stmt);
            releaseConnection(conn);
        }
    }
    
    /**
     * Query using ResultSetExtractor
     */
    public <T> T query(String sql, ResultSetExtractor<T> extractor) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            return extractor.extractData(rs);
        } catch (SQLException e) {
            throw new DataAccessException("Query failed: " + sql, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(stmt);
            releaseConnection(conn);
        }
    }
    
    /**
     * Execute with ConnectionCallback
     */
    public <T> T execute(ConnectionCallback<T> callback) {
        Connection conn = null;
        
        try {
            conn = getConnection();
            return callback.doInConnection(conn);
        } catch (SQLException e) {
            throw new DataAccessException("Execution failed", e);
        } finally {
            releaseConnection(conn);
        }
    }
    
    /**
     * Execute with StatementCallback
     */
    public <T> T execute(StatementCallback<T> callback) {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            return callback.doInStatement(stmt);
        } catch (SQLException e) {
            throw new DataAccessException("Statement execution failed", e);
        } finally {
            closeQuietly(stmt);
            releaseConnection(conn);
        }
    }
    
    /**
     * Update using PreparedStatementCallback
     */
    public <T> T update(String sql, PreparedStatementCallback<T> callback) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            return callback.doInPreparedStatement(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Update failed: " + sql, e);
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }
    
    /**
     * Query for single object
     */
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) {
        List<T> results = query(sql, rowMapper);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new DataAccessException("Expected 1 result, got " + results.size());
        }
        return results.get(0);
    }
    
    /**
     * Batch update
     */
    public int[] batchUpdate(String sql, List<PreparedStatementSetter> setters) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            
            for (PreparedStatementSetter setter : setters) {
                setter.setValues(ps);
                ps.addBatch();
            }
            
            return ps.executeBatch();
        } catch (SQLException e) {
            throw new DataAccessException("Batch update failed", e);
        } finally {
            closeQuietly(ps);
            releaseConnection(conn);
        }
    }
    
    private void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                // Log but don't throw
            }
        }
    }
}

/**
 * Generic template class demonstrating template method pattern
 */
abstract class OperationTemplate<T> {
    
    /**
     * Template method defining the algorithm
     */
    public final T execute() {
        beforeOperation();
        
        T result = null;
        try {
            result = doExecute();
            afterSuccess(result);
        } catch (Exception e) {
            afterFailure(e);
            throw e;
        } finally {
            afterOperation();
        }
        
        return result;
    }
    
    /**
     * Hook: called before operation
     */
    protected void beforeOperation() {
        // Default: do nothing
    }
    
    /**
     * Abstract method: must be implemented by subclasses
     */
    protected abstract T doExecute();
    
    /**
     * Hook: called after successful operation
     */
    protected void afterSuccess(T result) {
        // Default: do nothing
    }
    
    /**
     * Hook: called after failed operation
     */
    protected void afterFailure(Exception e) {
        // Default: do nothing
    }
    
    /**
     * Hook: called after operation (always)
     */
    protected void afterOperation() {
        // Default: do nothing
    }
}

/**
 * HTTP operation template
 */
class HttpTemplate {
    
    /**
     * GET request with callback
     */
    public <T> T getForObject(String url, ResponseExtractor<T> extractor) {
        try {
            // Simulate HTTP GET
            HttpResponse response = performGet(url);
            return extractor.extractData(response);
        } catch (Exception e) {
            throw new HttpException("GET failed: " + url, e);
        }
    }
    
    /**
     * POST request with callback
     */
    public <T> T postForObject(String url, Object request, ResponseExtractor<T> extractor) {
        try {
            // Simulate HTTP POST
            HttpResponse response = performPost(url, request);
            return extractor.extractData(response);
        } catch (Exception e) {
            throw new HttpException("POST failed: " + url, e);
        }
    }
    
    /**
     * Execute with interceptors
     */
    public <T> T execute(String url, HttpMethod method, 
                        RequestCallback requestCallback,
                        ResponseExtractor<T> responseExtractor) {
        try {
            // Prepare request
            if (requestCallback != null) {
                requestCallback.doWithRequest(null);
            }
            
            // Execute request
            HttpResponse response = performRequest(url, method);
            
            // Extract response
            if (responseExtractor != null) {
                return responseExtractor.extractData(response);
            }
            
            return null;
        } catch (Exception e) {
            throw new HttpException("Request failed", e);
        }
    }
    
    private HttpResponse performGet(String url) {
        return new HttpResponse(200, "GET response");
    }
    
    private HttpResponse performPost(String url, Object request) {
        return new HttpResponse(201, "POST response");
    }
    
    private HttpResponse performRequest(String url, HttpMethod method) {
        return new HttpResponse(200, method + " response");
    }
}

/**
 * Response extractor callback
 */
@FunctionalInterface
interface ResponseExtractor<T> {
    T extractData(HttpResponse response);
}

/**
 * Request callback
 */
@FunctionalInterface
interface RequestCallback {
    void doWithRequest(HttpRequest request);
}

// ============================================================================
// Domain Models
// ============================================================================

/**
 * User entity
 */
class User {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

/**
 * Database info
 */
class DatabaseInfo {
    private String productName;
    private String productVersion;
    private String driverName;
    
    // Getters and setters
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getProductVersion() { return productVersion; }
    public void setProductVersion(String productVersion) { this.productVersion = productVersion; }
    
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
}

/**
 * HTTP response
 */
class HttpResponse {
    private final int statusCode;
    private final String body;
    
    public HttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }
    
    public int getStatusCode() { return statusCode; }
    public String getBody() { return body; }
}

/**
 * HTTP request
 */
class HttpRequest {
    private Map<String, String> headers = new HashMap<>();
    private String body;
    
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }
    
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}

/**
 * HTTP method enum
 */
enum HttpMethod {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
}

// ============================================================================
// Exceptions
// ============================================================================

/**
 * Data access exception
 */
class DataAccessException extends RuntimeException {
    public DataAccessException(String message) {
        super(message);
    }
    
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * HTTP exception
 */
class HttpException extends RuntimeException {
    public HttpException(String message) {
        super(message);
    }
    
    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller demonstrating template-callback pattern
 */
@RestController
@RequestMapping("/api/template-callback")
class TemplateCallbackController {
    
    private final DataAccessTemplate dataAccessTemplate;
    
    public TemplateCallbackController(DataAccessTemplate dataAccessTemplate) {
        this.dataAccessTemplate = dataAccessTemplate;
    }
    
    /**
     * Query users using RowMapper
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = dataAccessTemplate.query("SELECT * FROM users", rs -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            return user;
        });
        
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get single user
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = dataAccessTemplate.queryForObject(
            "SELECT * FROM users WHERE id = " + id, 
            rs -> {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                return u;
            });
        
        return ResponseEntity.ok(user);
    }
    
    /**
     * Execute custom database operation
     */
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeCustom() {
        DatabaseInfo info = dataAccessTemplate.execute(conn -> {
            DatabaseMetaData metaData = conn.getMetaData();
            DatabaseInfo dbInfo = new DatabaseInfo();
            dbInfo.setProductName(metaData.getDatabaseProductName());
            dbInfo.setProductVersion(metaData.getDatabaseProductVersion());
            return dbInfo;
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("database", info.getProductName());
        response.put("version", info.getProductVersion());
        
        return ResponseEntity.ok(response);
    }
}
