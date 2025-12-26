package com.example.r2dbc.patterns;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import java.util.function.BiFunction;

/**
 * Row Mapping Pattern
 * 
 * Demonstrates row mapping from database results to Java objects.
 * Maps R2DBC Row to domain entities with custom mapping logic.
 * 
 * Key Features:
 * - Custom row mappers
 * - Type conversion
 * - Null handling
 * - Complex object mapping
 * - Metadata access
 */
@SpringBootApplication
public class RowMappingPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(RowMappingPattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Row Mapping Pattern ===\n");
        demonstrateRowMapping();
    }

    private void demonstrateRowMapping() {
        System.out.println("Row Mapping Strategies:");

        System.out.println("\n1. BiFunction Mapper:");
        System.out.println("   BiFunction<Row, RowMetadata, User> mapper = (row, meta) -> {");
        System.out.println("       User user = new User();");
        System.out.println("       user.setId(row.get(\"id\", Long.class));");
        System.out.println("       user.setName(row.get(\"name\", String.class));");
        System.out.println("       return user;");
        System.out.println("   };");

        System.out.println("\n2. Using with DatabaseClient:");
        System.out.println("   databaseClient.sql(\"SELECT * FROM users\")");
        System.out.println("       .map(userMapper)");
        System.out.println("       .all()");

        System.out.println("\n3. Handling Nulls:");
        System.out.println("   String email = row.get(\"email\", String.class);");
        System.out.println("   user.setEmail(email != null ? email : \"N/A\");");

        System.out.println("\n4. Type Conversion:");
        System.out.println("   LocalDateTime created = row.get(\"created_at\", LocalDateTime.class);");
        System.out.println("   BigDecimal amount = row.get(\"amount\", BigDecimal.class);");

        System.out.println("\n5. Complex Mapping:");
        System.out.println("   Address address = new Address();");
        System.out.println("   address.setStreet(row.get(\"street\", String.class));");
        System.out.println("   user.setAddress(address);");
    }

    // Example row mapper
    static BiFunction<Row, RowMetadata, User> userMapper = (row, metadata) -> {
        User user = new User();
        user.setId(row.get("id", Long.class));
        user.setName(row.get("name", String.class));
        user.setEmail(row.get("email", String.class));
        return user;
    };

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
