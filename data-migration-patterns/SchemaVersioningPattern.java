package com.example.schemaversioning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Schema Versioning Pattern, Migration Script Pattern,
 * Rollback Pattern, Baseline Pattern, Incremental Migration Pattern
 * 
 * Demonstrates comprehensive database schema versioning and migration management.
 * 
 * Features:
 * - Schema version tracking
 * - Migration script execution
 * - Rollback capabilities
 * - Baseline creation
 * - Incremental migrations
 * 
 * Use Cases:
 * - Database evolution management
 * - Version control for schemas
 * - Safe rollback procedures
 * - Environment synchronization
 * - Progressive schema updates
 */
@SpringBootApplication
public class SchemaVersioningPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(SchemaVersioningPattern.class, args);
    }
}

/**
 * Schema version model
 */
class SchemaVersion {
    private int version;
    private String description;
    private String script;
    private String rollbackScript;
    private LocalDateTime appliedAt;
    private String appliedBy;
    private boolean success;
    
    public SchemaVersion(int version, String description, String script) {
        this.version = version;
        this.description = description;
        this.script = script;
        this.appliedAt = LocalDateTime.now();
        this.appliedBy = "system";
        this.success = false;
    }
    
    // Getters and setters
    public int getVersion() { return version; }
    public String getDescription() { return description; }
    public String getScript() { return script; }
    public String getRollbackScript() { return rollbackScript; }
    public void setRollbackScript(String rollbackScript) { this.rollbackScript = rollbackScript; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public String getAppliedBy() { return appliedBy; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}

/**
 * Service for schema versioning and migrations
 */
@Service
class SchemaVersioningService {
    
    private final JdbcTemplate jdbcTemplate;
    private final List<SchemaVersion> migrationHistory = new ArrayList<>();
    private int currentVersion = 0;
    
    public SchemaVersioningService(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        initializeVersionTable();
    }
    
    /**
     * Initialize schema version tracking table
     */
    private void initializeVersionTable() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS schema_version (
                version INT PRIMARY KEY,
                description VARCHAR(255),
                script TEXT,
                rollback_script TEXT,
                applied_at TIMESTAMP,
                applied_by VARCHAR(50),
                success BOOLEAN
            )
            """;
        
        try {
            jdbcTemplate.execute(createTableSQL);
        } catch (Exception e) {
            System.out.println("Version table already exists or error: " + e.getMessage());
        }
    }
    
    /**
     * Apply migration script
     */
    public Map<String, Object> applyMigration(int version, String description, String script, String rollbackScript) {
        if (version <= currentVersion) {
            return Map.of(
                "success", false,
                "message", "Version " + version + " is not higher than current version " + currentVersion
            );
        }
        
        SchemaVersion migration = new SchemaVersion(version, description, script);
        migration.setRollbackScript(rollbackScript);
        
        try {
            // Execute migration script
            jdbcTemplate.execute(script);
            migration.setSuccess(true);
            currentVersion = version;
            
            // Record migration
            recordMigration(migration);
            migrationHistory.add(migration);
            
            return Map.of(
                "success", true,
                "version", version,
                "description", description,
                "message", "Migration applied successfully"
            );
            
        } catch (Exception e) {
            migration.setSuccess(false);
            migrationHistory.add(migration);
            
            return Map.of(
                "success", false,
                "version", version,
                "error", e.getMessage()
            );
        }
    }
    
    /**
     * Rollback to specific version
     */
    public Map<String, Object> rollbackToVersion(int targetVersion) {
        if (targetVersion >= currentVersion) {
            return Map.of(
                "success", false,
                "message", "Target version must be lower than current version"
            );
        }
        
        List<String> rollbacksExecuted = new ArrayList<>();
        
        // Execute rollback scripts in reverse order
        for (int i = migrationHistory.size() - 1; i >= 0; i--) {
            SchemaVersion migration = migrationHistory.get(i);
            
            if (migration.getVersion() > targetVersion && migration.isSuccess()) {
                try {
                    if (migration.getRollbackScript() != null) {
                        jdbcTemplate.execute(migration.getRollbackScript());
                        rollbacksExecuted.add("v" + migration.getVersion());
                    }
                } catch (Exception e) {
                    return Map.of(
                        "success", false,
                        "error", "Rollback failed at version " + migration.getVersion(),
                        "message", e.getMessage()
                    );
                }
            }
        }
        
        currentVersion = targetVersion;
        
        return Map.of(
            "success", true,
            "rollbacksExecuted", rollbacksExecuted,
            "currentVersion", currentVersion
        );
    }
    
    /**
     * Create baseline at current state
     */
    public Map<String, Object> createBaseline(String description) {
        String baselineScript = "-- Baseline created at version " + currentVersion;
        
        SchemaVersion baseline = new SchemaVersion(currentVersion, "BASELINE: " + description, baselineScript);
        baseline.setSuccess(true);
        
        recordMigration(baseline);
        
        return Map.of(
            "success", true,
            "version", currentVersion,
            "description", description,
            "message", "Baseline created"
        );
    }
    
    /**
     * Apply incremental migration
     */
    public Map<String, Object> applyIncrementalMigration(String description, String script) {
        int nextVersion = currentVersion + 1;
        return applyMigration(nextVersion, description, script, null);
    }
    
    /**
     * Get current schema version
     */
    public Map<String, Object> getCurrentVersion() {
        return Map.of(
            "version", currentVersion,
            "totalMigrations", migrationHistory.size(),
            "successfulMigrations", migrationHistory.stream().filter(SchemaVersion::isSuccess).count()
        );
    }
    
    /**
     * Get migration history
     */
    public List<Map<String, Object>> getMigrationHistory() {
        return migrationHistory.stream()
            .map(m -> Map.of(
                "version", (Object) m.getVersion(),
                "description", m.getDescription(),
                "appliedAt", m.getAppliedAt().toString(),
                "success", m.isSuccess()
            ))
            .toList();
    }
    
    /**
     * Validate schema version consistency
     */
    public Map<String, Object> validateSchema() {
        try {
            List<Map<String, Object>> dbVersions = jdbcTemplate.queryForList(
                "SELECT version, description, success FROM schema_version ORDER BY version"
            );
            
            return Map.of(
                "valid", true,
                "databaseVersions", dbVersions,
                "currentVersion", currentVersion
            );
            
        } catch (Exception e) {
            return Map.of(
                "valid", false,
                "error", e.getMessage()
            );
        }
    }
    
    /**
     * Record migration in database
     */
    private void recordMigration(SchemaVersion migration) {
        String insertSQL = """
            INSERT INTO schema_version 
            (version, description, script, rollback_script, applied_at, applied_by, success)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try {
            jdbcTemplate.update(insertSQL,
                migration.getVersion(),
                migration.getDescription(),
                migration.getScript(),
                migration.getRollbackScript(),
                migration.getAppliedAt(),
                migration.getAppliedBy(),
                migration.isSuccess()
            );
        } catch (Exception e) {
            System.err.println("Failed to record migration: " + e.getMessage());
        }
    }
}

/**
 * REST Controller for schema versioning operations
 */
@RestController
@RequestMapping("/api/schema")
class SchemaVersioningController {
    
    private final SchemaVersioningService versioningService;
    
    public SchemaVersioningController(SchemaVersioningService versioningService) {
        this.versioningService = versioningService;
    }
    
    /**
     * Apply migration
     */
    @PostMapping("/migrate")
    public Map<String, Object> applyMigration(@RequestBody Map<String, Object> request) {
        int version = ((Number) request.get("version")).intValue();
        String description = (String) request.get("description");
        String script = (String) request.get("script");
        String rollbackScript = (String) request.getOrDefault("rollbackScript", "");
        
        return versioningService.applyMigration(version, description, script, rollbackScript);
    }
    
    /**
     * Apply incremental migration (auto-version)
     */
    @PostMapping("/migrate/incremental")
    public Map<String, Object> applyIncrementalMigration(@RequestBody Map<String, String> request) {
        String description = request.get("description");
        String script = request.get("script");
        
        return versioningService.applyIncrementalMigration(description, script);
    }
    
    /**
     * Rollback to version
     */
    @PostMapping("/rollback/{targetVersion}")
    public Map<String, Object> rollback(@PathVariable int targetVersion) {
        return versioningService.rollbackToVersion(targetVersion);
    }
    
    /**
     * Create baseline
     */
    @PostMapping("/baseline")
    public Map<String, Object> createBaseline(@RequestBody Map<String, String> request) {
        String description = request.get("description");
        return versioningService.createBaseline(description);
    }
    
    /**
     * Get current version
     */
    @GetMapping("/version")
    public Map<String, Object> getCurrentVersion() {
        return versioningService.getCurrentVersion();
    }
    
    /**
     * Get migration history
     */
    @GetMapping("/history")
    public List<Map<String, Object>> getHistory() {
        return versioningService.getMigrationHistory();
    }
    
    /**
     * Validate schema
     */
    @GetMapping("/validate")
    public Map<String, Object> validateSchema() {
        return versioningService.validateSchema();
    }
}
