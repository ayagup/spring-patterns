package com.example.testing;

import org.junit.jupiter.api.Test;

/**
 * Testcontainers Pattern
 * =======================
 * 
 * Demonstrates the Testcontainers pattern for running tests with
 * real database instances in Docker containers.
 * 
 * Use Cases:
 * ----------
 * 1. Test with real databases
 * 2. Integration testing
 * 3. Test database-specific features
 * 4. Test with exact production database
 * 5. Test migrations
 * 6. Test with message brokers
 * 7. Test with Redis/Elasticsearch
 * 8. Reproducible test environments
 * 
 * Key Features:
 * -------------
 * - Real database instances
 * - Docker-based containers
 * - Automatic cleanup
 * - Multiple database support
 * - Network isolation
 * - Container reuse
 * - Custom initialization
 * - Version-specific testing
 * 
 * Supported Databases:
 * --------------------
 * - PostgreSQL
 * - MySQL/MariaDB
 * - MongoDB
 * - Redis
 * - Elasticsearch
 * - Cassandra
 * - Kafka
 * - RabbitMQ
 * 
 * Container Lifecycle:
 * --------------------
 * 1. Container started before tests
 * 2. Database initialized
 * 3. Tests execute
 * 4. Container stopped after tests
 * 5. Automatic cleanup
 * 
 * @author Spring Patterns
 * @version 1.0
 */
public class TestcontainersPattern {
    
    @Test
    void demonstrateTestcontainers() {
        System.out.println("\n=== Testcontainers Pattern ===");
        
        System.out.println("\nBasic PostgreSQL Container:");
        System.out.println("  @SpringBootTest");
        System.out.println("  @Testcontainers");
        System.out.println("  class PostgresIntegrationTest {");
        System.out.println("      ");
        System.out.println("      @Container");
        System.out.println("      static PostgreSQLContainer<?> postgres = ");
        System.out.println("          new PostgreSQLContainer<>(\"postgres:15-alpine\");");
        System.out.println("      ");
        System.out.println("      @DynamicPropertySource");
        System.out.println("      static void configureProperties(");
        System.out.println("          DynamicPropertyRegistry registry) {");
        System.out.println("          registry.add(\"spring.datasource.url\", ");
        System.out.println("              postgres::getJdbcUrl);");
        System.out.println("          registry.add(\"spring.datasource.username\", ");
        System.out.println("              postgres::getUsername);");
        System.out.println("          registry.add(\"spring.datasource.password\", ");
        System.out.println("              postgres::getPassword);");
        System.out.println("      }");
        System.out.println("  }");
        
        System.out.println("\nMySQL Container:");
        System.out.println("  @Container");
        System.out.println("  static MySQLContainer<?> mysql = ");
        System.out.println("      new MySQLContainer<>(\"mysql:8.0\")");
        System.out.println("          .withDatabaseName(\"testdb\")");
        System.out.println("          .withUsername(\"test\")");
        System.out.println("          .withPassword(\"test\");");
        
        System.out.println("\nMongoDB Container:");
        System.out.println("  @Container");
        System.out.println("  static MongoDBContainer mongodb = ");
        System.out.println("      new MongoDBContainer(\"mongo:6.0\");");
        System.out.println("  ");
        System.out.println("  @DynamicPropertySource");
        System.out.println("  static void setProperties(DynamicPropertyRegistry registry) {");
        System.out.println("      registry.add(\"spring.data.mongodb.uri\", ");
        System.out.println("          mongodb::getReplicaSetUrl);");
        System.out.println("  }");
        
        System.out.println("\nRedis Container:");
        System.out.println("  @Container");
        System.out.println("  static GenericContainer<?> redis = ");
        System.out.println("      new GenericContainer<>(\"redis:7-alpine\")");
        System.out.println("          .withExposedPorts(6379);");
        System.out.println("  ");
        System.out.println("  @DynamicPropertySource");
        System.out.println("  static void redisProperties(DynamicPropertyRegistry registry) {");
        System.out.println("      registry.add(\"spring.redis.host\", redis::getHost);");
        System.out.println("      registry.add(\"spring.redis.port\", redis::getFirstMappedPort);");
        System.out.println("  }");
        
        System.out.println("\nKafka Container:");
        System.out.println("  @Container");
        System.out.println("  static KafkaContainer kafka = ");
        System.out.println("      new KafkaContainer(");
        System.out.println("          DockerImageName.parse(\"confluentinc/cp-kafka:7.4.0\")");
        System.out.println("      );");
        
        System.out.println("\nWith Init Script:");
        System.out.println("  @Container");
        System.out.println("  static PostgreSQLContainer<?> postgres = ");
        System.out.println("      new PostgreSQLContainer<>(\"postgres:15\")");
        System.out.println("          .withInitScript(\"init-schema.sql\");");
        
        System.out.println("\nReusable Container:");
        System.out.println("  @Container");
        System.out.println("  static PostgreSQLContainer<?> postgres = ");
        System.out.println("      new PostgreSQLContainer<>(\"postgres:15\")");
        System.out.println("          .withReuse(true);");
        System.out.println("  ");
        System.out.println("  // Add to ~/.testcontainers.properties:");
        System.out.println("  // testcontainers.reuse.enable=true");
        
        System.out.println("\nCompose File:");
        System.out.println("  @Container");
        System.out.println("  static ComposeContainer environment =");
        System.out.println("      new ComposeContainer(new File(\"docker-compose.yml\"))");
        System.out.println("          .withExposedService(\"db\", 5432)");
        System.out.println("          .withExposedService(\"redis\", 6379);");
        
        System.out.println("\nNetwork Configuration:");
        System.out.println("  static Network network = Network.newNetwork();");
        System.out.println("  ");
        System.out.println("  @Container");
        System.out.println("  static PostgreSQLContainer<?> postgres = ");
        System.out.println("      new PostgreSQLContainer<>(\"postgres:15\")");
        System.out.println("          .withNetwork(network)");
        System.out.println("          .withNetworkAliases(\"database\");");
        
        System.out.println("\n✓ Testcontainers pattern demonstrated");
    }
    
    public static void main(String[] args) {
        System.out.println("=== Testcontainers Pattern ===");
        System.out.println("Test with real databases in Docker containers");
        System.out.println("Run tests to see pattern in action");
    }
}

/**
 * Testcontainers Summary:
 * 
 * Dependencies (Gradle):
 * ----------------------
 * testImplementation 'org.testcontainers:testcontainers:1.19.0'
 * testImplementation 'org.testcontainers:postgresql:1.19.0'
 * testImplementation 'org.testcontainers:mysql:1.19.0'
 * testImplementation 'org.testcontainers:mongodb:1.19.0'
 * testImplementation 'org.testcontainers:junit-jupiter:1.19.0'
 * 
 * Basic Setup:
 * ------------
 * @SpringBootTest
 * @Testcontainers
 * class IntegrationTest {
 *     @Container
 *     static PostgreSQLContainer<?> postgres = 
 *         new PostgreSQLContainer<>("postgres:15");
 *     
 *     @DynamicPropertySource
 *     static void properties(DynamicPropertyRegistry registry) {
 *         registry.add("spring.datasource.url", postgres::getJdbcUrl);
 *         registry.add("spring.datasource.username", postgres::getUsername);
 *         registry.add("spring.datasource.password", postgres::getPassword);
 *     }
 * }
 * 
 * Singleton Container:
 * --------------------
 * abstract class AbstractIntegrationTest {
 *     static PostgreSQLContainer<?> postgres;
 *     
 *     static {
 *         postgres = new PostgreSQLContainer<>("postgres:15");
 *         postgres.start();
 *     }
 * }
 * 
 * Custom Image:
 * -------------
 * @Container
 * static GenericContainer<?> app = new GenericContainer<>(
 *     DockerImageName.parse("myapp:latest")
 * )
 * .withExposedPorts(8080)
 * .withEnv("SPRING_PROFILES_ACTIVE", "test");
 */
