package com.example.testing;

import org.junit.jupiter.api.Test;

/**
 * JDBC Test Pattern
 * ==================
 * 
 * Demonstrates the @JdbcTest annotation for testing JDBC components
 * like JdbcTemplate without full JPA infrastructure.
 * 
 * Use Cases:
 * ----------
 * 1. Test JdbcTemplate operations
 * 2. Test NamedParameterJdbcTemplate
 * 3. Test custom JDBC code
 * 4. Test database scripts
 * 5. Test stored procedures
 * 6. Lightweight database tests
 * 7. Test without JPA/Hibernate
 * 8. Test batch operations
 * 
 * Key Features:
 * -------------
 * - Auto-configures embedded database
 * - Configures JdbcTemplate
 * - Transactional by default
 * - Auto-rollback after tests
 * - No JPA/Hibernate overhead
 * - Fast test execution
 * - SQL script support
 * - DataSource auto-configuration
 * 
 * What Gets Loaded:
 * -----------------
 * Loaded:
 * - DataSource
 * - JdbcTemplate
 * - NamedParameterJdbcTemplate
 * - Transaction management
 * - Embedded database
 * 
 * Not Loaded:
 * - JPA/Hibernate components
 * - Entity managers
 * - Repositories (except JDBC)
 * - Services, Controllers
 * 
 * @author Spring Patterns
 * @version 1.0
 */
public class JdbcTestPattern {
    
    @Test
    void demonstrateJdbcTest() {
        System.out.println("\n=== JDBC Test Pattern ===");
        
        System.out.println("\nBasic Usage:");
        System.out.println("  @JdbcTest");
        System.out.println("  class UserDaoTest {");
        System.out.println("      @Autowired");
        System.out.println("      private JdbcTemplate jdbcTemplate;");
        System.out.println("      ");
        System.out.println("      @Test");
        System.out.println("      void testInsert() {");
        System.out.println("          jdbcTemplate.update(");
        System.out.println("              \"INSERT INTO users (name, email) VALUES (?, ?)\",");
        System.out.println("              \"John\", \"john@example.com\"");
        System.out.println("          );");
        System.out.println("          ");
        System.out.println("          Integer count = jdbcTemplate.queryForObject(");
        System.out.println("              \"SELECT COUNT(*) FROM users\",");
        System.out.println("              Integer.class");
        System.out.println("          );");
        System.out.println("          assertEquals(1, count);");
        System.out.println("      }");
        System.out.println("  }");
        
        System.out.println("\nWith NamedParameterJdbcTemplate:");
        System.out.println("  @JdbcTest");
        System.out.println("  class ProductDaoTest {");
        System.out.println("      @Autowired");
        System.out.println("      private NamedParameterJdbcTemplate namedTemplate;");
        System.out.println("      ");
        System.out.println("      @Test");
        System.out.println("      void testNamedParameters() {");
        System.out.println("          Map<String, Object> params = new HashMap<>();");
        System.out.println("          params.put(\"name\", \"Laptop\");");
        System.out.println("          params.put(\"price\", 999.99);");
        System.out.println("          ");
        System.out.println("          namedTemplate.update(");
        System.out.println("              \"INSERT INTO products (name, price) \"");
        System.out.println("              + \"VALUES (:name, :price)\",");
        System.out.println("              params");
        System.out.println("          );");
        System.out.println("      }");
        System.out.println("  }");
        
        System.out.println("\nQuery for Objects:");
        System.out.println("  @Test");
        System.out.println("  void testQueryForObject() {");
        System.out.println("      User user = jdbcTemplate.queryForObject(");
        System.out.println("          \"SELECT * FROM users WHERE id = ?\",");
        System.out.println("          new Object[]{1},");
        System.out.println("          (rs, rowNum) -> new User(");
        System.out.println("              rs.getLong(\"id\"),");
        System.out.println("              rs.getString(\"name\"),");
        System.out.println("              rs.getString(\"email\")");
        System.out.println("          )");
        System.out.println("      );");
        System.out.println("  }");
        
        System.out.println("\nBatch Operations:");
        System.out.println("  @Test");
        System.out.println("  void testBatchUpdate() {");
        System.out.println("      List<Object[]> batch = Arrays.asList(");
        System.out.println("          new Object[]{\"User1\", \"user1@example.com\"},");
        System.out.println("          new Object[]{\"User2\", \"user2@example.com\"}");
        System.out.println("      );");
        System.out.println("      ");
        System.out.println("      int[] updateCounts = jdbcTemplate.batchUpdate(");
        System.out.println("          \"INSERT INTO users (name, email) VALUES (?, ?)\",");
        System.out.println("          batch");
        System.out.println("      );");
        System.out.println("      assertEquals(2, updateCounts.length);");
        System.out.println("  }");
        
        System.out.println("\nWith SQL Scripts:");
        System.out.println("  @JdbcTest");
        System.out.println("  @Sql(\"/schema.sql\")");
        System.out.println("  @Sql(\"/test-data.sql\")");
        System.out.println("  class DataTest {");
        System.out.println("      // Schema and data loaded before tests");
        System.out.println("  }");
        
        System.out.println("\nWith Real Database:");
        System.out.println("  @JdbcTest");
        System.out.println("  @AutoConfigureTestDatabase(replace = Replace.NONE)");
        System.out.println("  class RealDbTest {");
        System.out.println("      // Uses configured database");
        System.out.println("  }");
        
        System.out.println("\n✓ JDBC Test pattern demonstrated");
    }
    
    public static void main(String[] args) {
        System.out.println("=== JDBC Test Pattern ===");
        System.out.println("Test JDBC operations without JPA overhead");
        System.out.println("Run tests to see pattern in action");
    }
}

/**
 * JDBC Test Summary:
 * 
 * Basic:
 * ------
 * @JdbcTest
 * class MyJdbcTest {
 *     @Autowired
 *     private JdbcTemplate jdbcTemplate;
 * }
 * 
 * Insert:
 * -------
 * jdbcTemplate.update(
 *     "INSERT INTO users (name) VALUES (?)",
 *     "John"
 * );
 * 
 * Query:
 * ------
 * String name = jdbcTemplate.queryForObject(
 *     "SELECT name FROM users WHERE id = ?",
 *     String.class,
 *     1
 * );
 * 
 * Query List:
 * -----------
 * List<User> users = jdbcTemplate.query(
 *     "SELECT * FROM users",
 *     (rs, rowNum) -> new User(
 *         rs.getLong("id"),
 *         rs.getString("name")
 *     )
 * );
 * 
 * Named Parameters:
 * -----------------
 * @Autowired
 * private NamedParameterJdbcTemplate namedTemplate;
 * 
 * Map<String, Object> params = Map.of("id", 1);
 * namedTemplate.queryForObject(
 *     "SELECT * FROM users WHERE id = :id",
 *     params,
 *     mapper
 * );
 * 
 * Batch Update:
 * -------------
 * jdbcTemplate.batchUpdate(
 *     "INSERT INTO users (name) VALUES (?)",
 *     Arrays.asList(
 *         new Object[]{"User1"},
 *         new Object[]{"User2"}
 *     )
 * );
 */
