package com.example.flyway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Flyway Integration Pattern
 * 
 * Demonstrates database schema migration using Flyway.
 * 
 * Features:
 * - Version-based migrations
 * - Automatic schema evolution
 * - Migration validation
 * - Rollback support (Flyway Teams)
 * 
 * Configuration in application.properties:
 * spring.flyway.enabled=true
 * spring.flyway.baseline-on-migrate=true
 * spring.flyway.locations=classpath:db/migration
 * 
 * Migration naming: V1__Initial_schema.sql, V2__Add_users_table.sql
 * 
 * Use Cases:
 * - Database version control
 * - Continuous deployment
 * - Schema evolution
 * - Environment synchronization
 */
@SpringBootApplication
public class FlywayIntegrationPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(FlywayIntegrationPattern.class, args);
    }
}

/**
 * Service for Flyway operations
 */
@Service
class FlywayMigrationService {
    
    private final Flyway flyway;
    
    public FlywayMigrationService(DataSource dataSource) {
        // Configure Flyway programmatically
        this.flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load();
    }
    
    /**
     * Execute pending migrations
     */
    public Map<String, Object> migrate() {
        int migrationsExecuted = flyway.migrate().migrationsExecuted;
        
        return Map.of(
            "migrationsExecuted", migrationsExecuted,
            "status", "success"
        );
    }
    
    /**
     * Get migration info/status
     */
    public List<Map<String, Object>> getMigrationInfo() {
        MigrationInfoService infoService = flyway.info();
        MigrationInfo[] migrations = infoService.all();
        
        return Stream.of(migrations)
            .map(info -> Map.of(
                "version", info.getVersion() != null ? info.getVersion().toString() : "baseline",
                "description", info.getDescription(),
                "type", info.getType().toString(),
                "state", info.getState().toString(),
                "installedOn", info.getInstalledOn() != null ? info.getInstalledOn().toString() : "N/A",
                "executionTime", info.getExecutionTime() != null ? info.getExecutionTime() + "ms" : "N/A"
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Validate migrations
     */
    public Map<String, Object> validate() {
        try {
            flyway.validate();
            return Map.of(
                "valid", true,
                "message", "All migrations validated successfully"
            );
        } catch (Exception e) {
            return Map.of(
                "valid", false,
                "message", e.getMessage()
            );
        }
    }
    
    /**
     * Get current schema version
     */
    public Map<String, String> getCurrentVersion() {
        MigrationInfo current = flyway.info().current();
        
        if (current == null) {
            return Map.of("version", "No migrations applied", "status", "empty");
        }
        
        return Map.of(
            "version", current.getVersion().toString(),
            "description", current.getDescription(),
            "state", current.getState().toString()
        );
    }
    
    /**
     * Get pending migrations
     */
    public List<Map<String, String>> getPendingMigrations() {
        MigrationInfo[] pending = flyway.info().pending();
        
        return Stream.of(pending)
            .map(info -> Map.of(
                "version", info.getVersion().toString(),
                "description", info.getDescription(),
                "type", info.getType().toString()
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Repair schema history (remove failed migrations)
     */
    public Map<String, String> repair() {
        flyway.repair();
        return Map.of("status", "Schema history table repaired");
    }
    
    /**
     * Baseline existing database
     */
    public Map<String, String> baseline() {
        flyway.baseline();
        return Map.of("status", "Baseline created");
    }
    
    /**
     * Clean database (use with caution!)
     */
    public Map<String, String> clean() {
        // WARNING: This deletes all database objects
        flyway.clean();
        return Map.of("status", "Database cleaned", "warning", "All data deleted!");
    }
}

/**
 * REST Controller for Flyway operations
 */
@RestController
@RequestMapping("/api/flyway")
class FlywayController {
    
    private final FlywayMigrationService migrationService;
    
    public FlywayController(FlywayMigrationService migrationService) {
        this.migrationService = migrationService;
    }
    
    /**
     * Execute migrations
     */
    @PostMapping("/migrate")
    public Map<String, Object> migrate() {
        return migrationService.migrate();
    }
    
    /**
     * Get all migrations status
     */
    @GetMapping("/info")
    public List<Map<String, Object>> getMigrationInfo() {
        return migrationService.getMigrationInfo();
    }
    
    /**
     * Validate migrations
     */
    @GetMapping("/validate")
    public Map<String, Object> validate() {
        return migrationService.validate();
    }
    
    /**
     * Get current version
     */
    @GetMapping("/version")
    public Map<String, String> getCurrentVersion() {
        return migrationService.getCurrentVersion();
    }
    
    /**
     * Get pending migrations
     */
    @GetMapping("/pending")
    public List<Map<String, String>> getPendingMigrations() {
        return migrationService.getPendingMigrations();
    }
    
    /**
     * Repair schema history
     */
    @PostMapping("/repair")
    public Map<String, String> repair() {
        return migrationService.repair();
    }
    
    /**
     * Create baseline
     */
    @PostMapping("/baseline")
    public Map<String, String> baseline() {
        return migrationService.baseline();
    }
    
    /**
     * Clean database (dangerous!)
     */
    @DeleteMapping("/clean")
    public Map<String, String> clean(@RequestParam(required = true) String confirm) {
        if (!"CONFIRM_CLEAN".equals(confirm)) {
            return Map.of("error", "Confirmation required. Use confirm=CONFIRM_CLEAN");
        }
        return migrationService.clean();
    }
}

/*
 * Example Migration Scripts (place in src/main/resources/db/migration/):
 * 
 * V1__Initial_schema.sql:
 * CREATE TABLE users (
 *     id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *     username VARCHAR(50) NOT NULL UNIQUE,
 *     email VARCHAR(100) NOT NULL,
 *     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 * );
 * 
 * V2__Add_users_status.sql:
 * ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';
 * CREATE INDEX idx_users_status ON users(status);
 * 
 * V3__Create_orders_table.sql:
 * CREATE TABLE orders (
 *     id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *     user_id BIGINT NOT NULL,
 *     total_amount DECIMAL(10, 2),
 *     order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *     FOREIGN KEY (user_id) REFERENCES users(id)
 * );
 */
