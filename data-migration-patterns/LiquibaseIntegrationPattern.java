package com.example.liquibase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import liquibase.Liquibase;
import liquibase.database.*;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

/**
 * Liquibase Integration Pattern
 * 
 * Demonstrates database schema migration using Liquibase.
 * 
 * Features:
 * - XML/YAML/JSON change logs
 * - Database-independent migrations
 * - Rollback support
 * - Preconditions and contexts
 * 
 * Configuration in application.properties:
 * spring.liquibase.enabled=true
 * spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
 * 
 * Use Cases:
 * - Complex schema changes
 * - Multi-database support
 * - Conditional migrations
 * - Change set tracking
 */
@SpringBootApplication
public class LiquibaseIntegrationPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(LiquibaseIntegrationPattern.class, args);
    }
}

/**
 * Service for Liquibase operations
 */
@Service
class LiquibaseMigrationService {
    
    private final DataSource dataSource;
    
    public LiquibaseMigrationService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Create Liquibase instance
     */
    private Liquibase createLiquibase() throws Exception {
        Connection connection = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(new JdbcConnection(connection));
        
        return new Liquibase(
            "db/changelog/db.changelog-master.xml",
            new ClassLoaderResourceAccessor(),
            database
        );
    }
    
    /**
     * Execute pending change sets
     */
    public Map<String, Object> update() {
        try (Liquibase liquibase = createLiquibase()) {
            liquibase.update("");
            return Map.of(
                "status", "success",
                "message", "Database updated successfully"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", e.getMessage()
            );
        }
    }
    
    /**
     * Update with specific context
     */
    public Map<String, Object> updateWithContext(String context) {
        try (Liquibase liquibase = createLiquibase()) {
            liquibase.update(context);
            return Map.of(
                "status", "success",
                "context", context,
                "message", "Database updated with context: " + context
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", e.getMessage()
            );
        }
    }
    
    /**
     * Rollback to specific tag
     */
    public Map<String, String> rollback(String tag) {
        try (Liquibase liquibase = createLiquibase()) {
            liquibase.rollback(tag, "");
            return Map.of(
                "status", "success",
                "tag", tag,
                "message", "Rolled back to tag: " + tag
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", e.getMessage()
            );
        }
    }
    
    /**
     * Rollback specific number of change sets
     */
    public Map<String, Object> rollbackCount(int count) {
        try (Liquibase liquibase = createLiquibase()) {
            liquibase.rollback(count, "");
            return Map.of(
                "status", "success",
                "count", count,
                "message", "Rolled back " + count + " change sets"
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", e.getMessage()
            );
        }
    }
    
    /**
     * Tag current database state
     */
    public Map<String, String> tag(String tagName) {
        try (Liquibase liquibase = createLiquibase()) {
            liquibase.tag(tagName);
            return Map.of(
                "status", "success",
                "tag", tagName,
                "message", "Database tagged as: " + tagName
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", e.getMessage()
            );
        }
    }
    
    /**
     * Generate SQL for pending changes (dry run)
     */
    public String generateSQL() {
        try (Liquibase liquibase = createLiquibase()) {
            java.io.StringWriter writer = new java.io.StringWriter();
            liquibase.update("", writer);
            return writer.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Validate change log
     */
    public Map<String, Object> validate() {
        try (Liquibase liquibase = createLiquibase()) {
            liquibase.validate();
            return Map.of(
                "valid", true,
                "message", "Change log validated successfully"
            );
        } catch (Exception e) {
            return Map.of(
                "valid", false,
                "message", e.getMessage()
            );
        }
    }
    
    /**
     * Clear all checksums
     */
    public Map<String, String> clearChecksums() {
        try (Liquibase liquibase = createLiquibase()) {
            liquibase.clearCheckSums();
            return Map.of("status", "Checksums cleared");
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }
}

/**
 * REST Controller for Liquibase operations
 */
@RestController
@RequestMapping("/api/liquibase")
class LiquibaseController {
    
    private final LiquibaseMigrationService migrationService;
    
    public LiquibaseController(LiquibaseMigrationService migrationService) {
        this.migrationService = migrationService;
    }
    
    @PostMapping("/update")
    public Map<String, Object> update() {
        return migrationService.update();
    }
    
    @PostMapping("/update/{context}")
    public Map<String, Object> updateWithContext(@PathVariable String context) {
        return migrationService.updateWithContext(context);
    }
    
    @PostMapping("/rollback/tag/{tag}")
    public Map<String, String> rollbackToTag(@PathVariable String tag) {
        return migrationService.rollback(tag);
    }
    
    @PostMapping("/rollback/count/{count}")
    public Map<String, Object> rollbackCount(@PathVariable int count) {
        return migrationService.rollbackCount(count);
    }
    
    @PostMapping("/tag/{tagName}")
    public Map<String, String> tag(@PathVariable String tagName) {
        return migrationService.tag(tagName);
    }
    
    @GetMapping("/sql")
    public Map<String, String> generateSQL() {
        return Map.of("sql", migrationService.generateSQL());
    }
    
    @GetMapping("/validate")
    public Map<String, Object> validate() {
        return migrationService.validate();
    }
    
    @PostMapping("/clear-checksums")
    public Map<String, String> clearChecksums() {
        return migrationService.clearChecksums();
    }
}

/*
 * Example Liquibase Changelog (db.changelog-master.xml):
 * 
 * <?xml version="1.0" encoding="UTF-8"?>
 * <databaseChangeLog
 *     xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
 *     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *     xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
 *     http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.0.xsd">
 *     
 *     <changeSet id="1" author="admin">
 *         <createTable tableName="users">
 *             <column name="id" type="BIGINT" autoIncrement="true">
 *                 <constraints primaryKey="true"/>
 *             </column>
 *             <column name="username" type="VARCHAR(50)">
 *                 <constraints nullable="false" unique="true"/>
 *             </column>
 *             <column name="email" type="VARCHAR(100)">
 *                 <constraints nullable="false"/>
 *             </column>
 *         </createTable>
 *     </changeSet>
 *     
 *     <changeSet id="2" author="admin" context="test">
 *         <addColumn tableName="users">
 *             <column name="status" type="VARCHAR(20)" defaultValue="ACTIVE"/>
 *         </addColumn>
 *     </changeSet>
 *     
 *     <changeSet id="3" author="admin">
 *         <rollback>
 *             <dropColumn tableName="users" columnName="status"/>
 *         </rollback>
 *     </changeSet>
 * </databaseChangeLog>
 */
