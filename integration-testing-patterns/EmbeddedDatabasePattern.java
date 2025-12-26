package com.example.testing;

import org.junit.jupiter.api.Test;

/**
 * Embedded Database Pattern
 * ==========================
 * 
 * Demonstrates the Embedded Database pattern for fast, isolated
 * testing with in-memory databases.
 * 
 * Use Cases:
 * ----------
 * 1. Fast database testing
 * 2. Isolated test execution
 * 3. No external dependencies
 * 4. Continuous integration
 * 5. Unit/integration testing
 * 6. Schema validation
 * 7. Data access layer testing
 * 8. Rapid development
 * 
 * Key Features:
 * -------------
 * - In-memory execution
 * - Fast startup/shutdown
 * - Test isolation
 * - Auto cleanup
 * - SQL script support
 * - Multiple database types
 * - No installation required
 * - Deterministic tests
 * 
 * Supported Databases:
 * --------------------
 * - H2 (recommended)
 * - HSQLDB
 * - Apache Derby
 * 
 * H2 Modes:
 * ---------
 * - In-memory (mem:)
 * - File-based (file:)
 * - Server mode
 * - PostgreSQL compatibility
 * - MySQL compatibility
 * 
 * @author Spring Patterns
 * @version 1.0
 */
public class EmbeddedDatabasePattern {
    
    @Test
    void demonstrateEmbeddedDatabase() {
        System.out.println("\n=== Embedded Database Pattern ===");
        
        System.out.println("\nH2 In-Memory (Default):");
        System.out.println("  # application-test.properties");
        System.out.println("  spring.datasource.url=jdbc:h2:mem:testdb");
        System.out.println("  spring.datasource.driverClassName=org.h2.Driver");
        System.out.println("  spring.jpa.database-platform=org.hibernate.dialect.H2Dialect");
        
        System.out.println("\nH2 with Console:");
        System.out.println("  spring.h2.console.enabled=true");
        System.out.println("  spring.h2.console.path=/h2-console");
        System.out.println("  # Access at: http://localhost:8080/h2-console");
        System.out.println("  # JDBC URL: jdbc:h2:mem:testdb");
        
        System.out.println("\nH2 PostgreSQL Mode:");
        System.out.println("  spring.datasource.url=jdbc:h2:mem:testdb;");
        System.out.println("      MODE=PostgreSQL;");
        System.out.println("      DATABASE_TO_LOWER=TRUE;");
        System.out.println("      DEFAULT_NULL_ORDERING=HIGH");
        
        System.out.println("\nH2 MySQL Mode:");
        System.out.println("  spring.datasource.url=jdbc:h2:mem:testdb;");
        System.out.println("      MODE=MySQL;");
        System.out.println("      DATABASE_TO_LOWER=TRUE");
        
        System.out.println("\nProgrammatic Configuration:");
        System.out.println("  @Configuration");
        System.out.println("  class EmbeddedDbConfig {");
        System.out.println("      @Bean");
        System.out.println("      public DataSource dataSource() {");
        System.out.println("          return new EmbeddedDatabaseBuilder()");
        System.out.println("              .setType(EmbeddedDatabaseType.H2)");
        System.out.println("              .setName(\"testdb\")");
        System.out.println("              .addScript(\"schema.sql\")");
        System.out.println("              .addScript(\"data.sql\")");
        System.out.println("              .build();");
        System.out.println("      }");
        System.out.println("  }");
        
        System.out.println("\nWith Schema Initialization:");
        System.out.println("  spring.sql.init.mode=always");
        System.out.println("  spring.sql.init.schema-locations=classpath:schema.sql");
        System.out.println("  spring.sql.init.data-locations=classpath:data.sql");
        
        System.out.println("\nHibernate DDL Auto:");
        System.out.println("  spring.jpa.hibernate.ddl-auto=create-drop");
        System.out.println("  # Options:");
        System.out.println("  # - create: Create schema, drop previous data");
        System.out.println("  # - create-drop: Create schema, drop on session close");
        System.out.println("  # - update: Update schema");
        System.out.println("  # - validate: Validate schema");
        System.out.println("  # - none: Disable");
        
        System.out.println("\nHSQLDB Configuration:");
        System.out.println("  spring.datasource.url=jdbc:hsqldb:mem:testdb");
        System.out.println("  spring.datasource.driverClassName=org.hsqldb.jdbc.JDBCDriver");
        System.out.println("  spring.jpa.database-platform=org.hibernate.dialect.HSQLDialect");
        
        System.out.println("\nDerby Configuration:");
        System.out.println("  spring.datasource.url=jdbc:derby:memory:testdb;create=true");
        System.out.println("  spring.datasource.driverClassName=");
        System.out.println("      org.apache.derby.jdbc.EmbeddedDriver");
        
        System.out.println("\nIn Test:");
        System.out.println("  @DataJpaTest");
        System.out.println("  class UserRepositoryTest {");
        System.out.println("      // Automatically uses H2 embedded database");
        System.out.println("      ");
        System.out.println("      @Autowired");
        System.out.println("      private UserRepository userRepository;");
        System.out.println("      ");
        System.out.println("      @Test");
        System.out.println("      void testSaveUser() {");
        System.out.println("          User user = new User(\"John\", \"john@example.com\");");
        System.out.println("          User saved = userRepository.save(user);");
        System.out.println("          assertNotNull(saved.getId());");
        System.out.println("      }");
        System.out.println("  }");
        
        System.out.println("\nWith SQL Scripts in Test:");
        System.out.println("  @DataJpaTest");
        System.out.println("  @Sql(\"/test-schema.sql\")");
        System.out.println("  @Sql(\"/test-data.sql\")");
        System.out.println("  class DataTest {");
        System.out.println("      // Scripts executed before tests");
        System.out.println("  }");
        
        System.out.println("\n✓ Embedded Database pattern demonstrated");
    }
    
    public static void main(String[] args) {
        System.out.println("=== Embedded Database Pattern ===");
        System.out.println("Fast, isolated testing with in-memory databases");
        System.out.println("Run tests to see pattern in action");
    }
}

/**
 * Embedded Database Summary:
 * 
 * H2 Dependencies (Gradle):
 * -------------------------
 * testImplementation 'com.h2database:h2'
 * 
 * H2 In-Memory:
 * -------------
 * spring.datasource.url=jdbc:h2:mem:testdb
 * spring.datasource.driverClassName=org.h2.Driver
 * spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
 * 
 * H2 File-Based:
 * --------------
 * spring.datasource.url=jdbc:h2:file:./data/testdb
 * 
 * H2 Options:
 * -----------
 * jdbc:h2:mem:testdb                    # In-memory
 * jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1  # Keep alive
 * jdbc:h2:mem:testdb;MODE=PostgreSQL    # PostgreSQL mode
 * jdbc:h2:mem:testdb;MODE=MySQL         # MySQL mode
 * 
 * Programmatic Builder:
 * ---------------------
 * EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
 *     .setType(EmbeddedDatabaseType.H2)
 *     .setName("testdb")
 *     .addScript("schema.sql")
 *     .addScript("data.sql")
 *     .build();
 * 
 * Schema Init:
 * ------------
 * spring.sql.init.mode=always
 * spring.sql.init.schema-locations=classpath:schema.sql
 * spring.sql.init.data-locations=classpath:data.sql
 * spring.sql.init.continue-on-error=false
 * 
 * JPA DDL:
 * --------
 * spring.jpa.hibernate.ddl-auto=create-drop
 * spring.jpa.show-sql=true
 * spring.jpa.properties.hibernate.format_sql=true
 * 
 * H2 Console:
 * -----------
 * spring.h2.console.enabled=true
 * spring.h2.console.path=/h2-console
 * spring.h2.console.settings.web-allow-others=false
 */
