package com.example.testing;

import org.junit.jupiter.api.Test;

/**
 * Auto-Configure Test Database Pattern
 * =====================================
 * 
 * Demonstrates the @AutoConfigureTestDatabase annotation for controlling
 * database configuration in tests.
 * 
 * Use Cases:
 * ----------
 * 1. Use embedded test database
 * 2. Use real database for tests
 * 3. Replace database in tests
 * 4. Test database initialization
 * 5. Test with different databases
 * 6. Integration testing with DB
 * 7. Test schema creation
 * 8. Test data migration
 * 
 * Key Features:
 * -------------
 * - Auto database replacement
 * - Embedded database support
 * - Real database testing
 * - Connection configuration
 * - Schema initialization
 * - Multiple database types
 * - Test isolation
 * - Cleanup support
 * 
 * Replace Options:
 * ----------------
 * 1. ANY (default):
 *    - Replaces any DataSource with embedded
 *    - H2, HSQLDB, Derby supported
 * 
 * 2. AUTO_CONFIGURED:
 *    - Replaces only auto-configured DataSource
 *    - Custom DataSource beans untouched
 * 
 * 3. NONE:
 *    - No replacement
 *    - Uses configured DataSource
 *    - For real database testing
 * 
 * Embedded Databases:
 * -------------------
 * - H2 (recommended)
 * - HSQLDB
 * - Derby
 * 
 * @author Spring Patterns
 * @version 1.0
 */
public class AutoConfigureTestDatabasePattern {
    
    @Test
    void demonstrateAutoConfigureTestDatabase() {
        System.out.println("\n=== Auto-Configure Test Database Pattern ===");
        
        System.out.println("\nDefault Behavior (Replace ANY):");
        System.out.println("  @DataJpaTest");
        System.out.println("  class UserRepositoryTest {");
        System.out.println("      // Uses embedded H2 database by default");
        System.out.println("      @Autowired");
        System.out.println("      private UserRepository userRepository;");
        System.out.println("  }");
        
        System.out.println("\nUse Real Database (Replace NONE):");
        System.out.println("  @DataJpaTest");
        System.out.println("  @AutoConfigureTestDatabase(replace = Replace.NONE)");
        System.out.println("  class UserRepositoryIntegrationTest {");
        System.out.println("      // Uses database from application.properties");
        System.out.println("      @Autowired");
        System.out.println("      private UserRepository userRepository;");
        System.out.println("      ");
        System.out.println("      @Test");
        System.out.println("      void testWithRealDatabase() {");
        System.out.println("          // Tests against MySQL/PostgreSQL/etc");
        System.out.println("      }");
        System.out.println("  }");
        
        System.out.println("\nReplace AUTO_CONFIGURED:");
        System.out.println("  @DataJpaTest");
        System.out.println("  @AutoConfigureTestDatabase(");
        System.out.println("      replace = Replace.AUTO_CONFIGURED");
        System.out.println("  )");
        System.out.println("  class RepositoryTest {");
        System.out.println("      // Replaces only auto-configured DataSource");
        System.out.println("      // Custom DataSource beans remain");
        System.out.println("  }");
        
        System.out.println("\nSpecific Embedded Database:");
        System.out.println("  @DataJpaTest");
        System.out.println("  @AutoConfigureTestDatabase(");
        System.out.println("      connection = EmbeddedDatabaseConnection.H2");
        System.out.println("  )");
        System.out.println("  class H2Test {");
        System.out.println("      // Explicitly use H2");
        System.out.println("  }");
        
        System.out.println("\nWith Test Properties:");
        System.out.println("  @DataJpaTest");
        System.out.println("  @AutoConfigureTestDatabase(replace = Replace.NONE)");
        System.out.println("  @TestPropertySource(properties = {");
        System.out.println("      \"spring.datasource.url=jdbc:postgresql://localhost/testdb\",");
        System.out.println("      \"spring.datasource.username=test\",");
        System.out.println("      \"spring.datasource.password=test\"");
        System.out.println("  })");
        System.out.println("  class PostgresTest {");
        System.out.println("      // Uses PostgreSQL for testing");
        System.out.println("  }");
        
        System.out.println("\nWith SQL Scripts:");
        System.out.println("  @DataJpaTest");
        System.out.println("  @AutoConfigureTestDatabase(replace = Replace.NONE)");
        System.out.println("  @Sql(\"/schema.sql\")");
        System.out.println("  @Sql(\"/test-data.sql\")");
        System.out.println("  class SchemaTest {");
        System.out.println("      // Executes SQL scripts on real database");
        System.out.println("  }");
        
        System.out.println("\nH2 In-Memory Configuration:");
        System.out.println("  # application-test.properties");
        System.out.println("  spring.datasource.url=jdbc:h2:mem:testdb");
        System.out.println("  spring.datasource.driverClassName=org.h2.Driver");
        System.out.println("  spring.jpa.database-platform=org.hibernate.dialect.H2Dialect");
        
        System.out.println("\nTestcontainers Integration:");
        System.out.println("  @DataJpaTest");
        System.out.println("  @AutoConfigureTestDatabase(replace = Replace.NONE)");
        System.out.println("  @Testcontainers");
        System.out.println("  class TestcontainersTest {");
        System.out.println("      @Container");
        System.out.println("      static PostgreSQLContainer<?> postgres = ");
        System.out.println("          new PostgreSQLContainer<>(\"postgres:14\");");
        System.out.println("      ");
        System.out.println("      @DynamicPropertySource");
        System.out.println("      static void properties(DynamicPropertyRegistry registry) {");
        System.out.println("          registry.add(\"spring.datasource.url\", ");
        System.out.println("              postgres::getJdbcUrl);");
        System.out.println("      }");
        System.out.println("  }");
        
        System.out.println("\n✓ Auto-Configure Test Database pattern demonstrated");
    }
    
    public static void main(String[] args) {
        System.out.println("=== Auto-Configure Test Database Pattern ===");
        System.out.println("Control database configuration in tests");
        System.out.println("Run tests to see pattern in action");
    }
}

/**
 * Auto-Configure Test Database Summary:
 * 
 * Default (Embedded):
 * -------------------
 * @DataJpaTest
 * // Uses embedded H2 database automatically
 * 
 * Use Real Database:
 * ------------------
 * @DataJpaTest
 * @AutoConfigureTestDatabase(replace = Replace.NONE)
 * 
 * Replace Options:
 * ----------------
 * Replace.ANY             // Replace any DataSource (default)
 * Replace.AUTO_CONFIGURED // Replace only auto-configured
 * Replace.NONE            // Don't replace, use configured
 * 
 * Embedded Database Types:
 * ------------------------
 * @AutoConfigureTestDatabase(
 *     connection = EmbeddedDatabaseConnection.H2
 * )
 * @AutoConfigureTestDatabase(
 *     connection = EmbeddedDatabaseConnection.HSQLDB
 * )
 * @AutoConfigureTestDatabase(
 *     connection = EmbeddedDatabaseConnection.DERBY
 * )
 * 
 * With Properties:
 * ----------------
 * @AutoConfigureTestDatabase(replace = Replace.NONE)
 * @TestPropertySource(properties = {
 *     "spring.datasource.url=jdbc:mysql://localhost/testdb"
 * })
 * 
 * H2 Configuration:
 * -----------------
 * spring.datasource.url=jdbc:h2:mem:testdb
 * spring.datasource.driverClassName=org.h2.Driver
 * spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
 * spring.h2.console.enabled=true
 */
