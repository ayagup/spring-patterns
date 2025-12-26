package com.example.testing;

import org.junit.jupiter.api.Test;

/**
 * Data JPA Test Pattern
 * ======================
 * 
 * Demonstrates the @DataJpaTest annotation for testing JPA repositories
 * and database access layer in isolation.
 * 
 * Use Cases:
 * ----------
 * 1. Test JPA repositories
 * 2. Test custom queries
 * 3. Test entity relationships
 * 4. Test database operations
 * 5. Test JPA specifications
 * 6. Test query methods
 * 7. Fast persistence layer tests
 * 8. Integration with test database
 * 
 * Key Features:
 * -------------
 * - Auto-configures embedded database
 * - Configures JPA components only
 * - Transactional by default
 * - Auto-rollback after tests
 * - TestEntityManager support
 * - No web components loaded
 * - Fast test execution
 * - H2/HSQL/Derby support
 * 
 * What Gets Loaded:
 * -----------------
 * Loaded:
 * - @Repository
 * - JPA entities (@Entity)
 * - DataSource
 * - EntityManager
 * - TestEntityManager
 * - Transaction management
 * 
 * Not Loaded:
 * - @Service, @Controller, @Component
 * - Web components
 * - Business logic
 * - Full application context
 * 
 * @author Spring Patterns
 * @version 1.0
 */
public class DataJpaTestPattern {
    
    @Test
    void demonstrateDataJpaTest() {
        System.out.println("\n=== Data JPA Test Pattern ===");
        
        System.out.println("\nBasic Usage:");
        System.out.println("  @DataJpaTest");
        System.out.println("  class UserRepositoryTest {");
        System.out.println("      @Autowired");
        System.out.println("      private UserRepository userRepository;");
        System.out.println("      ");
        System.out.println("      @Autowired");
        System.out.println("      private TestEntityManager entityManager;");
        System.out.println("      ");
        System.out.println("      @Test");
        System.out.println("      void testFindByEmail() {");
        System.out.println("          User user = new User(\"john@example.com\", \"John\");");
        System.out.println("          entityManager.persist(user);");
        System.out.println("          entityManager.flush();");
        System.out.println("          ");
        System.out.println("          User found = userRepository");
        System.out.println("              .findByEmail(\"john@example.com\");");
        System.out.println("          assertEquals(\"John\", found.getName());");
        System.out.println("      }");
        System.out.println("  }");
        
        System.out.println("\nWith TestEntityManager:");
        System.out.println("  @DataJpaTest");
        System.out.println("  class ProductRepositoryTest {");
        System.out.println("      @Autowired");
        System.out.println("      private TestEntityManager em;");
        System.out.println("      ");
        System.out.println("      @Test");
        System.out.println("      void testPersistAndFind() {");
        System.out.println("          Product product = new Product(\"Laptop\", 999);");
        System.out.println("          Product saved = em.persistAndFlush(product);");
        System.out.println("          ");
        System.out.println("          Product found = em.find(Product.class, saved.getId());");
        System.out.println("          assertNotNull(found);");
        System.out.println("      }");
        System.out.println("  }");
        
        System.out.println("\nCustom Query Testing:");
        System.out.println("  @Test");
        System.out.println("  void testCustomQuery() {");
        System.out.println("      List<User> users = userRepository");
        System.out.println("          .findByAgeGreaterThan(18);");
        System.out.println("      assertTrue(users.size() > 0);");
        System.out.println("  }");
        
        System.out.println("\nWith Real Database:");
        System.out.println("  @DataJpaTest");
        System.out.println("  @AutoConfigureTestDatabase(");
        System.out.println("      replace = AutoConfigureTestDatabase.Replace.NONE");
        System.out.println("  )");
        System.out.println("  class UserRepositoryIntegrationTest {");
        System.out.println("      // Uses configured database instead of embedded");
        System.out.println("  }");
        
        System.out.println("\nWith SQL Scripts:");
        System.out.println("  @DataJpaTest");
        System.out.println("  @Sql(\"/test-data.sql\")");
        System.out.println("  class DataTest {");
        System.out.println("      // Executes SQL before tests");
        System.out.println("  }");
        
        System.out.println("\nRelationship Testing:");
        System.out.println("  @Test");
        System.out.println("  void testOneToMany() {");
        System.out.println("      Order order = new Order();");
        System.out.println("      order.addItem(new OrderItem(\"Item 1\"));");
        System.out.println("      order.addItem(new OrderItem(\"Item 2\"));");
        System.out.println("      ");
        System.out.println("      Order saved = em.persistAndFlush(order);");
        System.out.println("      assertEquals(2, saved.getItems().size());");
        System.out.println("  }");
        
        System.out.println("\n✓ Data JPA Test pattern demonstrated");
    }
    
    public static void main(String[] args) {
        System.out.println("=== Data JPA Test Pattern ===");
        System.out.println("Test JPA repositories and database operations");
        System.out.println("Run tests to see pattern in action");
    }
}

/**
 * Data JPA Test Summary:
 * 
 * Basic:
 * ------
 * @DataJpaTest
 * class RepositoryTest {
 *     @Autowired
 *     private UserRepository repository;
 *     
 *     @Autowired
 *     private TestEntityManager entityManager;
 * }
 * 
 * With TestEntityManager:
 * -----------------------
 * User user = entityManager.persist(new User());
 * entityManager.flush();
 * entityManager.clear();
 * User found = entityManager.find(User.class, user.getId());
 * 
 * Real Database:
 * --------------
 * @DataJpaTest
 * @AutoConfigureTestDatabase(replace = Replace.NONE)
 * 
 * Without Transaction:
 * --------------------
 * @DataJpaTest
 * @Transactional(propagation = Propagation.NOT_SUPPORTED)
 * 
 * With SQL Scripts:
 * -----------------
 * @DataJpaTest
 * @Sql({"/schema.sql", "/data.sql"})
 * 
 * Show SQL:
 * ---------
 * @DataJpaTest
 * @TestPropertySource(properties = {
 *     "spring.jpa.show-sql=true",
 *     "spring.jpa.properties.hibernate.format_sql=true"
 * })
 */
