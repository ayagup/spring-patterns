package com.example.r2dbc.patterns;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Database Client Pattern
 * 
 * Demonstrates DatabaseClient for low-level reactive SQL execution.
 * Provides SQL-based query execution with parameter binding.
 * 
 * Key Features:
 * - Raw SQL execution
 * - Parameter binding
 * - Result mapping
 * - Batch operations
 * - Transaction support
 */
@SpringBootApplication
public class DatabaseClientPattern implements CommandLineRunner {

    @Autowired
    private DatabaseClient databaseClient;

    public static void main(String[] args) {
        SpringApplication.run(DatabaseClientPattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Database Client Pattern ===\n");
        demonstrateDatabaseClient();
    }

    private void demonstrateDatabaseClient() {
        System.out.println("DatabaseClient provides SQL-based operations:");
        
        // SELECT query
        System.out.println("\n1. SELECT Query:");
        System.out.println("   databaseClient.sql(\"SELECT * FROM users WHERE name = :name\")");
        System.out.println("   .bind(\"name\", \"John\")");
        System.out.println("   .fetch().all()");

        // INSERT operation
        System.out.println("\n2. INSERT Operation:");
        System.out.println("   databaseClient.sql(\"INSERT INTO users (name, email) VALUES (:name, :email)\")");
        System.out.println("   .bind(\"name\", \"Jane\")");
        System.out.println("   .bind(\"email\", \"jane@example.com\")");
        System.out.println("   .fetch().rowsUpdated()");

        // UPDATE operation
        System.out.println("\n3. UPDATE Operation:");
        System.out.println("   databaseClient.sql(\"UPDATE users SET email = :email WHERE id = :id\")");
        System.out.println("   .bind(\"email\", \"newemail@example.com\")");
        System.out.println("   .bind(\"id\", 1L)");
        System.out.println("   .fetch().rowsUpdated()");

        // DELETE operation
        System.out.println("\n4. DELETE Operation:");
        System.out.println("   databaseClient.sql(\"DELETE FROM users WHERE id = :id\")");
        System.out.println("   .bind(\"id\", 1L)");
        System.out.println("   .fetch().rowsUpdated()");

        System.out.println("\n5. Benefits:");
        System.out.println("   - Full SQL control");
        System.out.println("   - Type-safe parameter binding");
        System.out.println("   - Reactive result handling");
        System.out.println("   - Native database features support");
    }

    static class User {
        private Long id;
        private String name;
        private String email;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
