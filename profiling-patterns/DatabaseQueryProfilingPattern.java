package com.example.databaseprofiling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database Query Profiling Pattern
 * 
 * Demonstrates profiling of database queries for performance optimization.
 * 
 * Features:
 * - Query execution time tracking
 * - Slow query detection
 * - Query count monitoring
 * - N+1 query detection
 * 
 * Use Cases:
 * - Database performance tuning
 * - Slow query identification
 * - ORM optimization
 * - Query caching decisions
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class DatabaseQueryProfilingPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(DatabaseQueryProfilingPattern.class, args);
    }
}

/**
 * Query statistics model
 */
class QueryStatistics {
    private String query;
    private long executionTime;
    private int rowCount;
    private long timestamp;
    
    public QueryStatistics(String query, long executionTime, int rowCount) {
        this.query = query;
        this.executionTime = executionTime;
        this.rowCount = rowCount;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getQuery() { return query; }
    public long getExecutionTime() { return executionTime; }
    public int getRowCount() { return rowCount; }
    public long getTimestamp() { return timestamp; }
}

/**
 * Service for database query profiling
 */
@Service
class QueryProfilingService {
    
    private static final Logger logger = LoggerFactory.getLogger(QueryProfilingService.class);
    private static final long SLOW_QUERY_THRESHOLD_MS = 100;
    
    private final Map<String, List<QueryStatistics>> queryStats = new ConcurrentHashMap<>();
    private final ThreadLocal<Integer> queryCountPerRequest = ThreadLocal.withInitial(() -> 0);
    
    /**
     * Record query execution
     */
    public void recordQuery(String query, long executionTimeMs, int rowCount) {
        QueryStatistics stats = new QueryStatistics(query, executionTimeMs, rowCount);
        
        queryStats.computeIfAbsent(query, k -> new ArrayList<>()).add(stats);
        queryCountPerRequest.set(queryCountPerRequest.get() + 1);
        
        if (executionTimeMs > SLOW_QUERY_THRESHOLD_MS) {
            logger.warn("SLOW QUERY ({}ms): {} | Rows: {}", executionTimeMs, query, rowCount);
        } else {
            logger.debug("Query executed ({}ms): {}", executionTimeMs, query);
        }
    }
    
    /**
     * Get statistics for a specific query
     */
    public Map<String, Object> getQueryStatistics(String query) {
        List<QueryStatistics> stats = queryStats.get(query);
        if (stats == null || stats.isEmpty()) {
            return Map.of("error", "No statistics found for query");
        }
        
        LongSummaryStatistics timingStats = stats.stream()
            .mapToLong(QueryStatistics::getExecutionTime)
            .summaryStatistics();
        
        return Map.of(
            "query", query,
            "executionCount", stats.size(),
            "avgTime", timingStats.getAverage(),
            "minTime", timingStats.getMin(),
            "maxTime", timingStats.getMax(),
            "totalTime", timingStats.getSum()
        );
    }
    
    /**
     * Get slow queries
     */
    public List<Map<String, Object>> getSlowQueries() {
        List<Map<String, Object>> slowQueries = new ArrayList<>();
        
        queryStats.forEach((query, statsList) -> {
            long avgTime = (long) statsList.stream()
                .mapToLong(QueryStatistics::getExecutionTime)
                .average()
                .orElse(0.0);
            
            if (avgTime > SLOW_QUERY_THRESHOLD_MS) {
                slowQueries.add(Map.of(
                    "query", query,
                    "avgTime", avgTime,
                    "executionCount", statsList.size()
                ));
            }
        });
        
        slowQueries.sort((a, b) -> 
            Long.compare((long) b.get("avgTime"), (long) a.get("avgTime")));
        
        return slowQueries;
    }
    
    /**
     * Detect N+1 query problems
     */
    public void startRequestProfiling() {
        queryCountPerRequest.set(0);
    }
    
    public Map<String, Object> endRequestProfiling() {
        int queryCount = queryCountPerRequest.get();
        queryCountPerRequest.remove();
        
        if (queryCount > 10) {
            logger.warn("Potential N+1 problem detected: {} queries in single request", queryCount);
        }
        
        return Map.of("queriesExecuted", queryCount);
    }
    
    /**
     * Get all query statistics
     */
    public Map<String, Object> getAllStatistics() {
        Map<String, Object> allStats = new HashMap<>();
        
        queryStats.keySet().forEach(query -> {
            allStats.put(query, getQueryStatistics(query));
        });
        
        return allStats;
    }
}

/**
 * Aspect for automatic query profiling
 */
@Aspect
@Component
class QueryProfilingAspect {
    
    private final QueryProfilingService profilingService;
    
    public QueryProfilingAspect(QueryProfilingService profilingService) {
        this.profilingService = profilingService;
    }
    
    /**
     * Profile repository methods
     */
    @Around("execution(* com.example.databaseprofiling.*Repository.*(..))")
    public Object profileRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String query = "QUERY: " + methodName; // In real scenario, extract actual SQL
        
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;
        
        int rowCount = result instanceof List ? ((List<?>) result).size() : 1;
        profilingService.recordQuery(query, duration, rowCount);
        
        return result;
    }
    
    /**
     * Track queries per request
     */
    @Around("execution(* com.example.databaseprofiling.*Controller.*(..))")
    public Object profileRequestQueries(ProceedingJoinPoint joinPoint) throws Throwable {
        profilingService.startRequestProfiling();
        try {
            return joinPoint.proceed();
        } finally {
            profilingService.endRequestProfiling();
        }
    }
}

/**
 * Mock repository for demonstration
 */
@Service
class UserRepository {
    
    public List<Map<String, Object>> findAll() throws InterruptedException {
        Thread.sleep(50); // Simulate database query
        return List.of(
            Map.of("id", 1, "name", "User 1"),
            Map.of("id", 2, "name", "User 2"),
            Map.of("id", 3, "name", "User 3")
        );
    }
    
    public Map<String, Object> findById(int id) throws InterruptedException {
        Thread.sleep(120); // Simulate slow query
        return Map.of("id", id, "name", "User " + id);
    }
    
    public List<Map<String, Object>> findByComplexCriteria(String criteria) throws InterruptedException {
        Thread.sleep(200); // Simulate complex query
        return List.of(Map.of("id", 1, "name", "Match", "criteria", criteria));
    }
}

/**
 * REST Controller demonstrating database profiling
 */
@RestController
@RequestMapping("/api/db-profiling")
class DatabaseProfilingController {
    
    private final UserRepository userRepository;
    private final QueryProfilingService profilingService;
    
    public DatabaseProfilingController(UserRepository userRepository,
                                      QueryProfilingService profilingService) {
        this.userRepository = userRepository;
        this.profilingService = profilingService;
    }
    
    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() throws InterruptedException {
        return userRepository.findAll();
    }
    
    @GetMapping("/users/{id}")
    public Map<String, Object> getUser(@PathVariable int id) throws InterruptedException {
        return userRepository.findById(id);
    }
    
    @GetMapping("/users/search")
    public List<Map<String, Object>> searchUsers(@RequestParam String criteria) throws InterruptedException {
        return userRepository.findByComplexCriteria(criteria);
    }
    
    /**
     * Simulate N+1 query problem
     */
    @GetMapping("/users-with-details")
    public List<Map<String, Object>> getUsersWithDetails() throws InterruptedException {
        List<Map<String, Object>> users = userRepository.findAll();
        
        // N+1 problem: query for each user
        for (Map<String, Object> user : users) {
            int id = (int) user.get("id");
            Map<String, Object> details = userRepository.findById(id);
            user.put("details", details);
        }
        
        return users;
    }
    
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        return profilingService.getAllStatistics();
    }
    
    @GetMapping("/slow-queries")
    public List<Map<String, Object>> getSlowQueries() {
        return profilingService.getSlowQueries();
    }
}
