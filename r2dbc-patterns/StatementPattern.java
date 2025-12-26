package com.example.r2dbc.patterns;

import io.r2dbc.spi.Statement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;

/**
 * Statement Pattern
 * 
 * Demonstrates R2DBC Statement for direct SQL execution.
 * Statement provides low-level access to SQL execution.
 * 
 * Key Features:
 * - Direct SQL execution
 * - Parameter binding
 * - Batch execution
 * - Return generated keys
 * - Transaction control
 */
@SpringBootApplication
public class StatementPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(StatementPattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Statement Pattern ===\n");
        demonstrateStatement();
    }

    private void demonstrateStatement() {
        System.out.println("R2DBC Statement provides:");
        
        System.out.println("\n1. SQL Execution:");
        System.out.println("   connection.createStatement(sql)");
        System.out.println("   .bind(index, value)");
        System.out.println("   .execute()");

        System.out.println("\n2. Batch Operations:");
        System.out.println("   statement.add()");
        System.out.println("   Multiple statements executed in batch");

        System.out.println("\n3. Parameter Binding:");
        System.out.println("   - Positional: bind(0, value)");
        System.out.println("   - Named: bind(\"name\", value)");
        System.out.println("   - bindNull for NULL values");

        System.out.println("\n4. Return Generated Keys:");
        System.out.println("   statement.returnGeneratedValues(\"id\")");

        System.out.println("\n5. Example:");
        System.out.println("   Statement stmt = connection.createStatement(");
        System.out.println("       \"INSERT INTO users (name, email) VALUES ($1, $2)\");");
        System.out.println("   stmt.bind(0, \"John\").bind(1, \"john@example.com\");");
        System.out.println("   stmt.execute()");
        System.out.println("       .flatMap(result -> result.getRowsUpdated())");
        System.out.println("       .subscribe(count -> System.out.println(\"Inserted: \" + count));");
    }
}
