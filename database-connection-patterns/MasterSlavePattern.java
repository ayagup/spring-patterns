package com.example.database.connection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Master-Slave Pattern
 * 
 * Purpose:
 * - One master database handles writes
 * - Multiple slave databases handle reads
 * - Automatic failover and load balancing
 * - High availability and scalability
 * 
 * Architecture:
 * - Master: Single write node
 * - Slaves: Multiple read replicas
 * - Replication: Master → Slaves
 * - Load Balancing: Round-robin or weighted distribution
 * 
 * Features:
 * 1. Write operations → Master only
 * 2. Read operations → Distributed across slaves
 * 3. Load balancing among slaves
 * 4. Slave health monitoring
 * 5. Automatic slave failover
 * 6. Replication lag monitoring
 * 
 * When to Use:
 * - High read-to-write ratio (90%+ reads)
 * - Need horizontal read scaling
 * - Geographic distribution of reads
 * - High availability requirements
 * - Reporting/analytics workloads
 * 
 * Benefits:
 * - Horizontal read scalability
 * - Reduced master load
 * - Better read performance
 * - Geographic data distribution
 * - Fault tolerance for reads
 */
@SpringBootApplication
public class MasterSlavePattern {

    public static void main(String[] args) {
        SpringApplication.run(MasterSlavePattern.class, args);
        System.out.println("Master-Slave Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/ms/users (load-balanced reads)");
    }

    /**
     * DataSource Type
     */
    public enum DataSourceType {
        MASTER,
        SLAVE_1,
        SLAVE_2,
        SLAVE_3
    }

    /**
     * DataSource Context Holder
     */
    public static class DataSourceContextHolder {
        private static final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<>();

        public static void setDataSourceType(DataSourceType type) {
            contextHolder.set(type);
        }

        public static DataSourceType getDataSourceType() {
            return contextHolder.get();
        }

        public static void clearDataSourceType() {
            contextHolder.remove();
        }
    }

    /**
     * Master-Slave Routing DataSource with Load Balancing
     */
    public static class MasterSlaveRoutingDataSource extends AbstractRoutingDataSource {

        private final LoadBalancer loadBalancer;

        public MasterSlaveRoutingDataSource(LoadBalancer loadBalancer) {
            this.loadBalancer = loadBalancer;
        }

        @Override
        protected Object determineCurrentLookupKey() {
            DataSourceType type = DataSourceContextHolder.getDataSourceType();
            
            if (type == DataSourceType.MASTER) {
                System.out.println("Routing to MASTER");
                return DataSourceType.MASTER;
            }
            
            // Load balance among slaves
            DataSourceType slave = loadBalancer.selectSlave();
            System.out.println("Routing to " + slave);
            return slave;
        }
    }

    /**
     * Load Balancer for Slave Selection
     */
    public interface LoadBalancer {
        DataSourceType selectSlave();
        void markSlaveUnhealthy(DataSourceType slave);
        void markSlaveHealthy(DataSourceType slave);
    }

    /**
     * Round-Robin Load Balancer
     */
    public static class RoundRobinLoadBalancer implements LoadBalancer {

        private final List<DataSourceType> slaves;
        private final Set<DataSourceType> unhealthySlaves;
        private final AtomicInteger counter;

        public RoundRobinLoadBalancer() {
            this.slaves = Arrays.asList(
                DataSourceType.SLAVE_1,
                DataSourceType.SLAVE_2,
                DataSourceType.SLAVE_3
            );
            this.unhealthySlaves = Collections.synchronizedSet(new HashSet<>());
            this.counter = new AtomicInteger(0);
        }

        @Override
        public DataSourceType selectSlave() {
            List<DataSourceType> healthySlaves = new ArrayList<>(slaves);
            healthySlaves.removeAll(unhealthySlaves);
            
            if (healthySlaves.isEmpty()) {
                // Fallback to master if all slaves unhealthy
                return DataSourceType.MASTER;
            }
            
            int index = counter.getAndIncrement() % healthySlaves.size();
            return healthySlaves.get(index);
        }

        @Override
        public void markSlaveUnhealthy(DataSourceType slave) {
            unhealthySlaves.add(slave);
        }

        @Override
        public void markSlaveHealthy(DataSourceType slave) {
            unhealthySlaves.remove(slave);
        }
    }

    /**
     * Weighted Load Balancer
     */
    public static class WeightedLoadBalancer implements LoadBalancer {

        private final Map<DataSourceType, Integer> weights;
        private final Set<DataSourceType> unhealthySlaves;
        private final Random random;

        public WeightedLoadBalancer() {
            this.weights = new HashMap<>();
            weights.put(DataSourceType.SLAVE_1, 50);  // 50% of traffic
            weights.put(DataSourceType.SLAVE_2, 30);  // 30% of traffic
            weights.put(DataSourceType.SLAVE_3, 20);  // 20% of traffic
            this.unhealthySlaves = Collections.synchronizedSet(new HashSet<>());
            this.random = new Random();
        }

        @Override
        public DataSourceType selectSlave() {
            Map<DataSourceType, Integer> healthyWeights = new HashMap<>(weights);
            unhealthySlaves.forEach(healthyWeights::remove);
            
            if (healthyWeights.isEmpty()) {
                return DataSourceType.MASTER;
            }
            
            int totalWeight = healthyWeights.values().stream().mapToInt(Integer::intValue).sum();
            int randomValue = random.nextInt(totalWeight);
            
            int cumulativeWeight = 0;
            for (Map.Entry<DataSourceType, Integer> entry : healthyWeights.entrySet()) {
                cumulativeWeight += entry.getValue();
                if (randomValue < cumulativeWeight) {
                    return entry.getKey();
                }
            }
            
            return healthyWeights.keySet().iterator().next();
        }

        @Override
        public void markSlaveUnhealthy(DataSourceType slave) {
            unhealthySlaves.add(slave);
        }

        @Override
        public void markSlaveHealthy(DataSourceType slave) {
            unhealthySlaves.remove(slave);
        }
    }

    /**
     * Configuration
     */
    @Configuration
    public static class MasterSlaveConfig {

        @Bean
        public LoadBalancer loadBalancer() {
            return new RoundRobinLoadBalancer();
        }

        @Bean(name = "masterDataSource")
        public DataSource masterDataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:master;DB_CLOSE_DELAY=-1");
            ds.setUsername("sa");
            ds.setPassword("");
            return ds;
        }

        @Bean(name = "slave1DataSource")
        public DataSource slave1DataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:slave1;DB_CLOSE_DELAY=-1");
            ds.setUsername("sa");
            ds.setPassword("");
            return ds;
        }

        @Bean(name = "slave2DataSource")
        public DataSource slave2DataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:slave2;DB_CLOSE_DELAY=-1");
            ds.setUsername("sa");
            ds.setPassword("");
            return ds;
        }

        @Bean(name = "slave3DataSource")
        public DataSource slave3DataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:slave3;DB_CLOSE_DELAY=-1");
            ds.setUsername("sa");
            ds.setPassword("");
            return ds;
        }

        @Bean
        public DataSource routingDataSource(LoadBalancer loadBalancer) {
            MasterSlaveRoutingDataSource routingDataSource = 
                    new MasterSlaveRoutingDataSource(loadBalancer);
            
            Map<Object, Object> targetDataSources = new HashMap<>();
            targetDataSources.put(DataSourceType.MASTER, masterDataSource());
            targetDataSources.put(DataSourceType.SLAVE_1, slave1DataSource());
            targetDataSources.put(DataSourceType.SLAVE_2, slave2DataSource());
            targetDataSources.put(DataSourceType.SLAVE_3, slave3DataSource());
            
            routingDataSource.setTargetDataSources(targetDataSources);
            routingDataSource.setDefaultTargetDataSource(masterDataSource());
            
            return routingDataSource;
        }

        @Bean
        public JdbcTemplate jdbcTemplate(DataSource routingDataSource) {
            return new JdbcTemplate(routingDataSource);
        }
    }

    /**
     * User Service
     */
    @Service
    public static class UserService {

        private final JdbcTemplate jdbcTemplate;

        public UserService(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
            initializeDatabases();
        }

        private void initializeDatabases() {
            // Initialize master
            DataSourceContextHolder.setDataSourceType(DataSourceType.MASTER);
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "username VARCHAR(50), " +
                        "email VARCHAR(100))");
                
                jdbcTemplate.update("INSERT INTO users (username, email) VALUES (?, ?)",
                        "john", "john@example.com");
                jdbcTemplate.update("INSERT INTO users (username, email) VALUES (?, ?)",
                        "jane", "jane@example.com");
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }

            // Replicate to all slaves
            for (DataSourceType slave : Arrays.asList(
                    DataSourceType.SLAVE_1, DataSourceType.SLAVE_2, DataSourceType.SLAVE_3)) {
                DataSourceContextHolder.setDataSourceType(slave);
                try {
                    jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "username VARCHAR(50), " +
                            "email VARCHAR(100))");
                    
                    jdbcTemplate.update("INSERT INTO users (username, email) VALUES (?, ?)",
                            "john", "john@example.com");
                    jdbcTemplate.update("INSERT INTO users (username, email) VALUES (?, ?)",
                            "jane", "jane@example.com");
                } finally {
                    DataSourceContextHolder.clearDataSourceType();
                }
            }
        }

        /**
         * Read from slaves (load balanced)
         */
        public List<Map<String, Object>> getAllUsers() {
            // Context holder will be set by load balancer
            return jdbcTemplate.queryForList("SELECT * FROM users");
        }

        /**
         * Write to master
         */
        public void addUser(String username, String email) {
            DataSourceContextHolder.setDataSourceType(DataSourceType.MASTER);
            try {
                jdbcTemplate.update("INSERT INTO users (username, email) VALUES (?, ?)",
                        username, email);
                
                // Simulate replication to slaves
                replicateToSlaves(username, email);
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }

        private void replicateToSlaves(String username, String email) {
            for (DataSourceType slave : Arrays.asList(
                    DataSourceType.SLAVE_1, DataSourceType.SLAVE_2, DataSourceType.SLAVE_3)) {
                DataSourceContextHolder.setDataSourceType(slave);
                try {
                    jdbcTemplate.update("INSERT INTO users (username, email) VALUES (?, ?)",
                            username, email);
                } catch (Exception e) {
                    System.err.println("Replication error to " + slave);
                } finally {
                    DataSourceContextHolder.clearDataSourceType();
                }
            }
        }
    }

    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/ms")
    public static class MasterSlaveController {

        private final UserService userService;
        private final LoadBalancer loadBalancer;

        public MasterSlaveController(UserService userService, LoadBalancer loadBalancer) {
            this.userService = userService;
            this.loadBalancer = loadBalancer;
        }

        @GetMapping("/users")
        public List<Map<String, Object>> getUsers() {
            return userService.getAllUsers();
        }

        @PostMapping("/users")
        public Map<String, Object> addUser(@RequestBody Map<String, String> user) {
            userService.addUser(user.get("username"), user.get("email"));
            return Map.of("status", "created", "dataSource", "MASTER");
        }

        @GetMapping("/lb/status")
        public Map<String, Object> getLoadBalancerStatus() {
            return Map.of("type", loadBalancer.getClass().getSimpleName());
        }
    }
}
