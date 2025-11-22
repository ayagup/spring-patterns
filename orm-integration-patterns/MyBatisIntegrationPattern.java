package com.example.orm.integration;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.List;

/**
 * MyBatis Integration Pattern
 * 
 * Purpose:
 * - MyBatis SQL mapper framework integration
 * - SQL-centric persistence layer
 * - Dynamic SQL generation
 * - Result mapping flexibility
 * 
 * Features:
 * 1. Annotation-based SQL mapping
 * 2. XML-based SQL mapping
 * 3. Dynamic SQL support
 * 4. Result mapping
 * 5. Type handlers
 * 6. Plugin support
 * 7. First-level caching
 * 8. Batch operations
 * 
 * When to Use:
 * - Need fine-grained SQL control
 * - Complex SQL queries
 * - Legacy database schemas
 * - Stored procedures
 * - Performance-critical queries
 * - Mix of ORM and SQL
 * 
 * Benefits:
 * - Full SQL control
 * - Easy to optimize
 * - Simple learning curve
 * - Flexible mapping
 * - Great for complex queries
 * - Stored procedure support
 */
@SpringBootApplication
@MapperScan("com.example.orm.integration.HibernateIntegrationPattern")
public class MyBatisIntegrationPattern {

    public static void main(String[] args) {
        SpringApplication.run(MyBatisIntegrationPattern.class, args);
        System.out.println("MyBatis Integration Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/mybatis/orders");
    }

    /**
     * Order Entity
     */
    public static class Order {
        private Long id;
        private Long customerId;
        private String customerName;
        private String productName;
        private Integer quantity;
        private Double totalPrice;
        private String status;
        private java.util.Date orderDate;

        // Constructors
        public Order() {}

        public Order(Long customerId, String productName, Integer quantity, Double totalPrice) {
            this.customerId = customerId;
            this.productName = productName;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
            this.status = "PENDING";
            this.orderDate = new java.util.Date();
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public Double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public java.util.Date getOrderDate() { return orderDate; }
        public void setOrderDate(java.util.Date orderDate) { this.orderDate = orderDate; }
    }

    /**
     * Order Statistics DTO
     */
    public static class OrderStats {
        private String status;
        private Long count;
        private Double totalAmount;

        public OrderStats() {}

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
        
        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    }

    /**
     * MyBatis Mapper Interface
     */
    @Mapper
    public interface OrderMapper {

        /**
         * Insert order
         */
        @Insert("INSERT INTO orders (customer_id, product_name, quantity, total_price, status, order_date) " +
                "VALUES (#{customerId}, #{productName}, #{quantity}, #{totalPrice}, #{status}, #{orderDate})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(Order order);

        /**
         * Find by ID
         */
        @Select("SELECT * FROM orders WHERE id = #{id}")
        @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "customerId", column = "customer_id"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "totalPrice", column = "total_price"),
            @Result(property = "status", column = "status"),
            @Result(property = "orderDate", column = "order_date")
        })
        Order findById(Long id);

        /**
         * Find all orders
         */
        @Select("SELECT * FROM orders")
        @Results(id = "orderResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "customerId", column = "customer_id"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "totalPrice", column = "total_price"),
            @Result(property = "status", column = "status"),
            @Result(property = "orderDate", column = "order_date")
        })
        List<Order> findAll();

        /**
         * Find by customer ID
         */
        @Select("SELECT * FROM orders WHERE customer_id = #{customerId}")
        @ResultMap("orderResultMap")
        List<Order> findByCustomerId(Long customerId);

        /**
         * Find by status
         */
        @Select("SELECT * FROM orders WHERE status = #{status}")
        @ResultMap("orderResultMap")
        List<Order> findByStatus(String status);

        /**
         * Update order status
         */
        @Update("UPDATE orders SET status = #{status} WHERE id = #{id}")
        int updateStatus(@Param("id") Long id, @Param("status") String status);

        /**
         * Update order
         */
        @Update("UPDATE orders SET customer_id = #{customerId}, product_name = #{productName}, " +
                "quantity = #{quantity}, total_price = #{totalPrice}, status = #{status} WHERE id = #{id}")
        int update(Order order);

        /**
         * Delete order
         */
        @Delete("DELETE FROM orders WHERE id = #{id}")
        int delete(Long id);

        /**
         * Count orders
         */
        @Select("SELECT COUNT(*) FROM orders")
        Long count();

        /**
         * Count by status
         */
        @Select("SELECT COUNT(*) FROM orders WHERE status = #{status}")
        Long countByStatus(String status);

        /**
         * Get total revenue
         */
        @Select("SELECT SUM(total_price) FROM orders WHERE status = 'COMPLETED'")
        Double getTotalRevenue();

        /**
         * Find orders with joins (complex query)
         */
        @Select("SELECT o.*, c.name as customer_name " +
                "FROM orders o " +
                "LEFT JOIN customers c ON o.customer_id = c.id " +
                "WHERE o.id = #{id}")
        @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "customerId", column = "customer_id"),
            @Result(property = "customerName", column = "customer_name"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "totalPrice", column = "total_price"),
            @Result(property = "status", column = "status"),
            @Result(property = "orderDate", column = "order_date")
        })
        Order findByIdWithCustomer(Long id);

        /**
         * Get order statistics grouped by status
         */
        @Select("SELECT status, COUNT(*) as count, SUM(total_price) as total_amount " +
                "FROM orders GROUP BY status")
        @Results({
            @Result(property = "status", column = "status"),
            @Result(property = "count", column = "count"),
            @Result(property = "totalAmount", column = "total_amount")
        })
        List<OrderStats> getOrderStatistics();

        /**
         * Dynamic SQL - Search orders
         */
        @SelectProvider(type = OrderSqlProvider.class, method = "searchOrders")
        @ResultMap("orderResultMap")
        List<Order> searchOrders(@Param("customerId") Long customerId, 
                                  @Param("status") String status,
                                  @Param("minPrice") Double minPrice,
                                  @Param("maxPrice") Double maxPrice);

        /**
         * Batch insert
         */
        @Insert({
            "<script>",
            "INSERT INTO orders (customer_id, product_name, quantity, total_price, status, order_date) VALUES ",
            "<foreach collection='list' item='order' separator=','>",
            "(#{order.customerId}, #{order.productName}, #{order.quantity}, #{order.totalPrice}, #{order.status}, #{order.orderDate})",
            "</foreach>",
            "</script>"
        })
        int batchInsert(List<Order> orders);
    }

    /**
     * SQL Provider for Dynamic SQL
     */
    public static class OrderSqlProvider {
        
        public String searchOrders(@Param("customerId") Long customerId, 
                                   @Param("status") String status,
                                   @Param("minPrice") Double minPrice,
                                   @Param("maxPrice") Double maxPrice) {
            StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE 1=1");
            
            if (customerId != null) {
                sql.append(" AND customer_id = #{customerId}");
            }
            if (status != null && !status.isEmpty()) {
                sql.append(" AND status = #{status}");
            }
            if (minPrice != null) {
                sql.append(" AND total_price >= #{minPrice}");
            }
            if (maxPrice != null) {
                sql.append(" AND total_price <= #{maxPrice}");
            }
            
            return sql.toString();
        }
    }

    /**
     * MyBatis Configuration
     */
    @Configuration
    public static class MyBatisConfig {

        @Bean
        public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
            SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
            sessionFactory.setDataSource(dataSource);
            
            // MyBatis configuration
            org.apache.ibatis.session.Configuration configuration = 
                    new org.apache.ibatis.session.Configuration();
            configuration.setMapUnderscoreToCamelCase(true);
            configuration.setCacheEnabled(true);
            configuration.setLazyLoadingEnabled(true);
            configuration.setAggressiveLazyLoading(false);
            configuration.setMultipleResultSetsEnabled(true);
            configuration.setUseGeneratedKeys(true);
            configuration.setDefaultExecutorType(org.apache.ibatis.session.ExecutorType.REUSE);
            
            sessionFactory.setConfiguration(configuration);
            
            return sessionFactory.getObject();
        }
    }

    /**
     * Order Service
     */
    @Service
    public static class OrderService {

        private final OrderMapper orderMapper;

        public OrderService(OrderMapper orderMapper) {
            this.orderMapper = orderMapper;
        }

        public Order createOrder(Order order) {
            orderMapper.insert(order);
            return order;
        }

        public void batchCreateOrders(List<Order> orders) {
            orderMapper.batchInsert(orders);
        }

        public Order getOrder(Long id) {
            return orderMapper.findById(id);
        }

        public Order getOrderWithCustomer(Long id) {
            return orderMapper.findByIdWithCustomer(id);
        }

        public List<Order> getAllOrders() {
            return orderMapper.findAll();
        }

        public List<Order> getOrdersByCustomer(Long customerId) {
            return orderMapper.findByCustomerId(customerId);
        }

        public List<Order> getOrdersByStatus(String status) {
            return orderMapper.findByStatus(status);
        }

        public void updateOrderStatus(Long id, String status) {
            orderMapper.updateStatus(id, status);
        }

        public void updateOrder(Order order) {
            orderMapper.update(order);
        }

        public void deleteOrder(Long id) {
            orderMapper.delete(id);
        }

        public Long getTotalOrders() {
            return orderMapper.count();
        }

        public Long getOrderCountByStatus(String status) {
            return orderMapper.countByStatus(status);
        }

        public Double getTotalRevenue() {
            return orderMapper.getTotalRevenue();
        }

        public List<OrderStats> getStatistics() {
            return orderMapper.getOrderStatistics();
        }

        public List<Order> searchOrders(Long customerId, String status, 
                                       Double minPrice, Double maxPrice) {
            return orderMapper.searchOrders(customerId, status, minPrice, maxPrice);
        }
    }

    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/mybatis")
    public static class OrderController {

        private final OrderService service;

        public OrderController(OrderService service) {
            this.service = service;
        }

        @PostMapping("/orders")
        public Order createOrder(@RequestBody Order order) {
            return service.createOrder(order);
        }

        @PostMapping("/orders/batch")
        public void batchCreate(@RequestBody List<Order> orders) {
            service.batchCreateOrders(orders);
        }

        @GetMapping("/orders/{id}")
        public Order getOrder(@PathVariable Long id) {
            return service.getOrder(id);
        }

        @GetMapping("/orders/{id}/with-customer")
        public Order getOrderWithCustomer(@PathVariable Long id) {
            return service.getOrderWithCustomer(id);
        }

        @GetMapping("/orders")
        public List<Order> getAllOrders() {
            return service.getAllOrders();
        }

        @GetMapping("/orders/customer/{customerId}")
        public List<Order> getOrdersByCustomer(@PathVariable Long customerId) {
            return service.getOrdersByCustomer(customerId);
        }

        @GetMapping("/orders/status/{status}")
        public List<Order> getOrdersByStatus(@PathVariable String status) {
            return service.getOrdersByStatus(status);
        }

        @GetMapping("/orders/search")
        public List<Order> searchOrders(
                @RequestParam(required = false) Long customerId,
                @RequestParam(required = false) String status,
                @RequestParam(required = false) Double minPrice,
                @RequestParam(required = false) Double maxPrice) {
            return service.searchOrders(customerId, status, minPrice, maxPrice);
        }

        @PutMapping("/orders/{id}/status")
        public void updateStatus(@PathVariable Long id, @RequestParam String status) {
            service.updateOrderStatus(id, status);
        }

        @PutMapping("/orders/{id}")
        public void updateOrder(@PathVariable Long id, @RequestBody Order order) {
            order.setId(id);
            service.updateOrder(order);
        }

        @DeleteMapping("/orders/{id}")
        public void deleteOrder(@PathVariable Long id) {
            service.deleteOrder(id);
        }

        @GetMapping("/orders/count")
        public Long getCount() {
            return service.getTotalOrders();
        }

        @GetMapping("/orders/revenue")
        public Double getRevenue() {
            return service.getTotalRevenue();
        }

        @GetMapping("/orders/statistics")
        public List<OrderStats> getStatistics() {
            return service.getStatistics();
        }
    }
}

/**
 * Configuration Examples:
 * 
 * application.properties:
 * 
 * # MyBatis Configuration
 * mybatis.configuration.map-underscore-to-camel-case=true
 * mybatis.configuration.cache-enabled=true
 * mybatis.configuration.lazy-loading-enabled=true
 * mybatis.configuration.aggressive-lazy-loading=false
 * mybatis.configuration.multiple-result-sets-enabled=true
 * mybatis.configuration.use-generated-keys=true
 * mybatis.configuration.default-executor-type=reuse
 * mybatis.configuration.default-statement-timeout=25
 * 
 * # MyBatis Mapper Locations (if using XML)
 * mybatis.mapper-locations=classpath:mapper/*.xml
 * mybatis.type-aliases-package=com.example.orm.integration
 * 
 * # Logging
 * logging.level.com.example.orm.integration=DEBUG
 * 
 * 
 * XML Mapper Example (OrderMapper.xml):
 * 
 * <?xml version="1.0" encoding="UTF-8" ?>
 * <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
 *   "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
 * 
 * <mapper namespace="com.example.orm.integration.MyBatisIntegrationPattern.OrderMapper">
 *   
 *   <resultMap id="orderResultMap" type="Order">
 *     <id property="id" column="id"/>
 *     <result property="customerId" column="customer_id"/>
 *     <result property="productName" column="product_name"/>
 *     <result property="quantity" column="quantity"/>
 *     <result property="totalPrice" column="total_price"/>
 *     <result property="status" column="status"/>
 *     <result property="orderDate" column="order_date"/>
 *   </resultMap>
 *   
 *   <select id="findById" resultMap="orderResultMap">
 *     SELECT * FROM orders WHERE id = #{id}
 *   </select>
 *   
 *   <select id="searchOrders" resultMap="orderResultMap">
 *     SELECT * FROM orders
 *     <where>
 *       <if test="customerId != null">
 *         AND customer_id = #{customerId}
 *       </if>
 *       <if test="status != null">
 *         AND status = #{status}
 *       </if>
 *       <if test="minPrice != null">
 *         AND total_price &gt;= #{minPrice}
 *       </if>
 *       <if test="maxPrice != null">
 *         AND total_price &lt;= #{maxPrice}
 *       </if>
 *     </where>
 *   </select>
 *   
 * </mapper>
 * 
 * 
 * Best Practices:
 * 
 * 1. Use @Mapper or @MapperScan for mapper discovery
 * 2. Leverage dynamic SQL for flexible queries
 * 3. Use result maps for complex mappings
 * 4. Enable caching for read-heavy operations
 * 5. Use batch operations for bulk inserts
 * 6. Configure appropriate fetch strategies
 * 7. Use SQL providers for dynamic SQL in Java
 * 8. Monitor SQL performance
 */
