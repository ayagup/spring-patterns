package com.example.database.connection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sharding Pattern (Database Sharding)
 * 
 * Purpose:
 * - Horizontal partitioning across multiple databases
 * - Distribute data based on sharding key
 * - Scale storage and throughput horizontally
 * - Reduce database size and improve performance
 * 
 * Architecture:
 * - Multiple physical databases (shards)
 * - Sharding key determines data placement
 * - Application-level routing logic
 * - Possible metadata database for shard mapping
 * 
 * Sharding Strategies:
 * 1. Hash-based: hash(key) % shard_count
 * 2. Range-based: key ranges to specific shards
 * 3. Geographic: location-based sharding
 * 4. Directory-based: lookup table for key→shard mapping
 * 5. Composite: combination of strategies
 * 
 * Features:
 * - Automatic shard selection
 * - Multi-shard queries with result aggregation
 * - Shard rebalancing support
 * - Cross-shard transaction handling
 * - Consistent hashing for minimal redistribution
 * 
 * When to Use:
 * - Very large datasets (TB+)
 * - High write throughput requirements
 * - Geographic data distribution
 * - Multi-tenant applications
 * - Database size limits reached
 * 
 * Benefits:
 * - Horizontal scalability
 * - Improved performance per shard
 * - Better resource utilization
 * - Geographic data locality
 * - Reduced single database load
 * 
 * Considerations:
 * - Complex query routing
 * - Cross-shard joins difficult
 * - Rebalancing overhead
 * - Increased operational complexity
 * - Application-level awareness required
 */
@SpringBootApplication
public class ShardingPattern {

    public static void main(String[] args) {
        SpringApplication.run(ShardingPattern.class, args);
        System.out.println("Sharding Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/shard/orders");
    }

    /**
     * Sharding Strategy Interface
     */
    public interface ShardingStrategy {
        int getShardIndex(Object shardingKey, int totalShards);
    }

    /**
     * Hash-based Sharding Strategy
     */
    public static class HashBasedSharding implements ShardingStrategy {
        @Override
        public int getShardIndex(Object shardingKey, int totalShards) {
            int hash = Objects.hashCode(shardingKey);
            return Math.abs(hash % totalShards);
        }
    }

    /**
     * Range-based Sharding Strategy
     */
    public static class RangeBasedSharding implements ShardingStrategy {
        
        private final Map<Integer, Range> shardRanges;

        public RangeBasedSharding() {
            this.shardRanges = new HashMap<>();
            shardRanges.put(0, new Range(0, 1000));
            shardRanges.put(1, new Range(1001, 2000));
            shardRanges.put(2, new Range(2001, 3000));
            shardRanges.put(3, new Range(3001, Integer.MAX_VALUE));
        }

        @Override
        public int getShardIndex(Object shardingKey, int totalShards) {
            if (!(shardingKey instanceof Number)) {
                throw new IllegalArgumentException("Range-based sharding requires numeric key");
            }
            
            int value = ((Number) shardingKey).intValue();
            
            for (Map.Entry<Integer, Range> entry : shardRanges.entrySet()) {
                if (entry.getValue().contains(value)) {
                    return entry.getKey();
                }
            }
            
            return totalShards - 1; // Default to last shard
        }

        static class Range {
            final int min;
            final int max;

            Range(int min, int max) {
                this.min = min;
                this.max = max;
            }

            boolean contains(int value) {
                return value >= min && value <= max;
            }
        }
    }

    /**
     * Consistent Hashing Strategy
     */
    public static class ConsistentHashingSharding implements ShardingStrategy {
        
        private final int virtualNodes;
        private final TreeMap<Integer, Integer> ring;

        public ConsistentHashingSharding(int totalShards, int virtualNodes) {
            this.virtualNodes = virtualNodes;
            this.ring = new TreeMap<>();
            
            // Create virtual nodes for each shard
            for (int shard = 0; shard < totalShards; shard++) {
                for (int v = 0; v < virtualNodes; v++) {
                    String virtualKey = "shard-" + shard + "-vnode-" + v;
                    int hash = virtualKey.hashCode();
                    ring.put(hash, shard);
                }
            }
        }

        @Override
        public int getShardIndex(Object shardingKey, int totalShards) {
            int hash = Objects.hashCode(shardingKey);
            
            // Find the first node >= hash
            Map.Entry<Integer, Integer> entry = ring.ceilingEntry(hash);
            
            if (entry == null) {
                // Wrap around to first node
                entry = ring.firstEntry();
            }
            
            return entry.getValue();
        }
    }

    /**
     * Geographic Sharding Strategy
     */
    public static class GeographicSharding implements ShardingStrategy {
        
        private final Map<String, Integer> regionShardMap;

        public GeographicSharding() {
            this.regionShardMap = new HashMap<>();
            regionShardMap.put("US", 0);
            regionShardMap.put("EU", 1);
            regionShardMap.put("ASIA", 2);
            regionShardMap.put("OTHER", 3);
        }

        @Override
        public int getShardIndex(Object shardingKey, int totalShards) {
            String region = shardingKey.toString().toUpperCase();
            return regionShardMap.getOrDefault(region, 3);
        }
    }

    /**
     * Shard Context Holder
     */
    public static class ShardContextHolder {
        private static final ThreadLocal<Integer> contextHolder = new ThreadLocal<>();

        public static void setShardIndex(Integer shardIndex) {
            contextHolder.set(shardIndex);
        }

        public static Integer getShardIndex() {
            return contextHolder.get();
        }

        public static void clearShardIndex() {
            contextHolder.remove();
        }
    }

    /**
     * Sharding Manager
     */
    public static class ShardingManager {
        
        private final List<DataSource> shards;
        private final List<JdbcTemplate> shardTemplates;
        private final ShardingStrategy strategy;

        public ShardingManager(List<DataSource> shards, ShardingStrategy strategy) {
            this.shards = shards;
            this.strategy = strategy;
            this.shardTemplates = shards.stream()
                    .map(JdbcTemplate::new)
                    .collect(Collectors.toList());
        }

        public JdbcTemplate getShardTemplate(Object shardingKey) {
            int index = strategy.getShardIndex(shardingKey, shards.size());
            return shardTemplates.get(index);
        }

        public List<JdbcTemplate> getAllShardTemplates() {
            return new ArrayList<>(shardTemplates);
        }

        public int getShardCount() {
            return shards.size();
        }

        public int getShardIndex(Object shardingKey) {
            return strategy.getShardIndex(shardingKey, shards.size());
        }
    }

    /**
     * Configuration
     */
    @Configuration
    public static class ShardingConfig {

        @Bean
        public DataSource shard0DataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:shard0;DB_CLOSE_DELAY=-1");
            ds.setUsername("sa");
            ds.setPassword("");
            return ds;
        }

        @Bean
        public DataSource shard1DataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:shard1;DB_CLOSE_DELAY=-1");
            ds.setUsername("sa");
            ds.setPassword("");
            return ds;
        }

        @Bean
        public DataSource shard2DataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:shard2;DB_CLOSE_DELAY=-1");
            ds.setUsername("sa");
            ds.setPassword("");
            return ds;
        }

        @Bean
        public DataSource shard3DataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:shard3;DB_CLOSE_DELAY=-1");
            ds.setUsername("sa");
            ds.setPassword("");
            return ds;
        }

        @Bean
        public ShardingStrategy shardingStrategy() {
            // Use hash-based sharding by default
            return new HashBasedSharding();
            
            // Or use consistent hashing:
            // return new ConsistentHashingSharding(4, 100);
            
            // Or use range-based:
            // return new RangeBasedSharding();
            
            // Or use geographic:
            // return new GeographicSharding();
        }

        @Bean
        public ShardingManager shardingManager(ShardingStrategy strategy) {
            List<DataSource> shards = Arrays.asList(
                    shard0DataSource(),
                    shard1DataSource(),
                    shard2DataSource(),
                    shard3DataSource()
            );
            return new ShardingManager(shards, strategy);
        }
    }

    /**
     * Order Service with Sharding
     */
    @Service
    public static class OrderService {

        private final ShardingManager shardingManager;

        public OrderService(ShardingManager shardingManager) {
            this.shardingManager = shardingManager;
            initializeShards();
        }

        private void initializeShards() {
            for (JdbcTemplate template : shardingManager.getAllShardTemplates()) {
                template.execute("CREATE TABLE IF NOT EXISTS orders (" +
                        "id INT PRIMARY KEY, " +
                        "customer_id INT, " +
                        "product VARCHAR(100), " +
                        "amount DECIMAL(10,2))");
            }
        }

        /**
         * Insert order to appropriate shard
         */
        public void createOrder(int orderId, int customerId, String product, double amount) {
            // Shard by customer_id
            JdbcTemplate template = shardingManager.getShardTemplate(customerId);
            int shardIndex = shardingManager.getShardIndex(customerId);
            
            template.update("INSERT INTO orders (id, customer_id, product, amount) VALUES (?, ?, ?, ?)",
                    orderId, customerId, product, amount);
            
            System.out.println("Order " + orderId + " created on shard " + shardIndex);
        }

        /**
         * Get orders for specific customer (single shard query)
         */
        public List<Map<String, Object>> getOrdersByCustomer(int customerId) {
            JdbcTemplate template = shardingManager.getShardTemplate(customerId);
            int shardIndex = shardingManager.getShardIndex(customerId);
            
            System.out.println("Querying customer " + customerId + " orders from shard " + shardIndex);
            
            return template.queryForList(
                    "SELECT * FROM orders WHERE customer_id = ?", customerId);
        }

        /**
         * Get all orders (multi-shard query with aggregation)
         */
        public List<Map<String, Object>> getAllOrders() {
            System.out.println("Executing multi-shard query across " + 
                    shardingManager.getShardCount() + " shards");
            
            List<Map<String, Object>> allOrders = new ArrayList<>();
            
            for (int i = 0; i < shardingManager.getShardCount(); i++) {
                JdbcTemplate template = shardingManager.getAllShardTemplates().get(i);
                List<Map<String, Object>> shardOrders = template.queryForList("SELECT * FROM orders");
                
                // Add shard info to each result
                shardOrders.forEach(order -> order.put("_shard", i));
                
                allOrders.addAll(shardOrders);
            }
            
            return allOrders;
        }

        /**
         * Get total order amount (aggregation across shards)
         */
        public double getTotalOrderAmount() {
            double total = 0.0;
            
            for (JdbcTemplate template : shardingManager.getAllShardTemplates()) {
                Double shardTotal = template.queryForObject(
                        "SELECT COALESCE(SUM(amount), 0) FROM orders", Double.class);
                total += (shardTotal != null ? shardTotal : 0.0);
            }
            
            return total;
        }

        /**
         * Get shard distribution statistics
         */
        public Map<String, Object> getShardDistribution() {
            Map<String, Object> distribution = new HashMap<>();
            
            for (int i = 0; i < shardingManager.getShardCount(); i++) {
                JdbcTemplate template = shardingManager.getAllShardTemplates().get(i);
                Integer count = template.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
                distribution.put("shard_" + i, count != null ? count : 0);
            }
            
            return distribution;
        }
    }

    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/shard")
    public static class ShardingController {

        private final OrderService orderService;
        private final ShardingManager shardingManager;

        public ShardingController(OrderService orderService, ShardingManager shardingManager) {
            this.orderService = orderService;
            this.shardingManager = shardingManager;
        }

        @PostMapping("/orders")
        public Map<String, Object> createOrder(@RequestBody Map<String, Object> order) {
            int orderId = (int) order.get("orderId");
            int customerId = (int) order.get("customerId");
            String product = (String) order.get("product");
            double amount = ((Number) order.get("amount")).doubleValue();
            
            orderService.createOrder(orderId, customerId, product, amount);
            
            int shardIndex = shardingManager.getShardIndex(customerId);
            
            return Map.of(
                    "status", "created",
                    "orderId", orderId,
                    "shard", shardIndex,
                    "strategy", shardingManager.strategy.getClass().getSimpleName()
            );
        }

        @GetMapping("/orders")
        public List<Map<String, Object>> getAllOrders() {
            return orderService.getAllOrders();
        }

        @GetMapping("/orders/customer/{customerId}")
        public List<Map<String, Object>> getCustomerOrders(@PathVariable int customerId) {
            return orderService.getOrdersByCustomer(customerId);
        }

        @GetMapping("/orders/total")
        public Map<String, Object> getTotalAmount() {
            double total = orderService.getTotalOrderAmount();
            return Map.of("totalAmount", total);
        }

        @GetMapping("/shards/distribution")
        public Map<String, Object> getDistribution() {
            return orderService.getShardDistribution();
        }

        @GetMapping("/shards/info")
        public Map<String, Object> getShardInfo() {
            return Map.of(
                    "shardCount", shardingManager.getShardCount(),
                    "strategy", shardingManager.strategy.getClass().getSimpleName(),
                    "distribution", orderService.getShardDistribution()
            );
        }
    }
}

/**
 * Configuration Examples:
 * 
 * application.properties:
 * 
 * # Shard 0
 * spring.datasource.shard0.url=jdbc:mysql://db-shard-0:3306/shard0
 * spring.datasource.shard0.username=user
 * spring.datasource.shard0.password=pass
 * 
 * # Shard 1
 * spring.datasource.shard1.url=jdbc:mysql://db-shard-1:3306/shard1
 * spring.datasource.shard1.username=user
 * spring.datasource.shard1.password=pass
 * 
 * # Shard 2
 * spring.datasource.shard2.url=jdbc:mysql://db-shard-2:3306/shard2
 * spring.datasource.shard2.username=user
 * spring.datasource.shard2.password=pass
 * 
 * # Shard 3
 * spring.datasource.shard3.url=jdbc:mysql://db-shard-3:3306/shard3
 * spring.datasource.shard3.username=user
 * spring.datasource.shard3.password=pass
 * 
 * 
 * Usage Examples:
 * 
 * 1. Create Order (automatically routed to shard):
 * POST /api/shard/orders
 * {
 *   "orderId": 1001,
 *   "customerId": 12345,
 *   "product": "Laptop",
 *   "amount": 999.99
 * }
 * 
 * 2. Get customer orders (single shard):
 * GET /api/shard/orders/customer/12345
 * 
 * 3. Get all orders (multi-shard aggregation):
 * GET /api/shard/orders
 * 
 * 4. Get shard distribution:
 * GET /api/shard/shards/distribution
 * 
 * 
 * Best Practices:
 * 
 * 1. Choose sharding key carefully:
 *    - High cardinality
 *    - Even distribution
 *    - Query pattern aligned
 *    - Stable over time
 * 
 * 2. Avoid cross-shard operations:
 *    - Minimize joins across shards
 *    - Denormalize when necessary
 *    - Use aggregation judiciously
 * 
 * 3. Plan for rebalancing:
 *    - Consistent hashing for minimal data movement
 *    - Virtual nodes for better distribution
 *    - Gradual migration strategies
 * 
 * 4. Monitor shard health:
 *    - Track data distribution
 *    - Monitor shard performance
 *    - Identify hot spots
 * 
 * 5. Handle failures gracefully:
 *    - Circuit breakers per shard
 *    - Fallback strategies
 *    - Retry mechanisms
 */
