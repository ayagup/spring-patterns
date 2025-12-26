package com.example.nativeimage.patterns;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.aot.hint.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

/**
 * 💡 SPRING BOOT NATIVE IMAGE - SERIALIZATION CONFIG PATTERN 💡
 * =============================================================
 * 
 * Demonstrates serialization configuration for GraalVM Native Image.
 * Serialization requires special handling in native images because:
 * - Java serialization uses reflection extensively
 * - Jackson JSON serialization needs class metadata
 * - All serializable classes must be registered
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ JAVA SERIALIZATION:
 *    - Implements Serializable interface
 *    - Requires serialization hints
 *    - ObjectInputStream/ObjectOutputStream
 *    - serialVersionUID recommended
 * 
 * 2️⃣ JACKSON JSON SERIALIZATION:
 *    - Requires reflection hints
 *    - Field access for getters/setters
 *    - Constructor access for instantiation
 *    - Custom serializers/deserializers
 * 
 * 3️⃣ SERIALIZATION HINTS:
 *    - Register serializable classes
 *    - Register for reflection (JSON)
 *    - Register custom serializers
 *    - Register type hierarchies
 * 
 * 4️⃣ COMMON USE CASES:
 *    - REST API JSON responses
 *    - Session serialization
 *    - Cache serialization
 *    - Message queue payloads
 *    - Event serialization
 * 
 * 📦 DEPENDENCIES:
 * ===============
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-web</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>com.fasterxml.jackson.core</groupId>
 *     <artifactId>jackson-databind</artifactId>
 * </dependency>
 * 
 * 🔧 SERIALIZATION CONFIG API:
 * ===========================
 * 
 * Register Java Serializable:
 * ---------------------------
 * hints.serialization().registerType(MySerializableClass.class);
 * 
 * Register for Jackson JSON:
 * --------------------------
 * hints.reflection().registerType(
 *     MyJsonClass.class,
 *     MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
 *     MemberCategory.DECLARED_FIELDS
 * );
 * 
 * Register Type Hierarchy:
 * ------------------------
 * hints.serialization().registerType(
 *     BaseClass.class,
 *     hint -> hint.onReachableType(BaseClass.class)
 * );
 * 
 * Register Custom Serializer:
 * ---------------------------
 * hints.reflection().registerType(
 *     CustomSerializer.class,
 *     MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
 *     MemberCategory.INVOKE_DECLARED_METHODS
 * );
 * 
 * 🎯 SERIALIZATION SCENARIOS:
 * ==========================
 * 
 * REST API Response:
 * ------------------
 * @RestController
 * public class UserController {
 *     @GetMapping("/users/{id}")
 *     public UserDTO getUser(@PathVariable Long id) {
 *         return userService.findById(id);  // Jackson serialization
 *     }
 * }
 * 
 * Session Serialization:
 * ----------------------
 * @SessionScope
 * public class ShoppingCart implements Serializable {
 *     private List<CartItem> items;
 *     // Java serialization
 * }
 * 
 * Cache Serialization:
 * --------------------
 * @Cacheable("products")
 * public Product getProduct(Long id) {
 *     return productRepository.findById(id);  // Needs serialization
 * }
 * 
 * Message Queue:
 * --------------
 * @RabbitListener(queues = "orders")
 * public void handleOrder(OrderEvent event) {
 *     // Deserialization from queue
 * }
 * 
 * 💡 WHEN TO USE SERIALIZATION CONFIG:
 * ===================================
 * ✅ REST API JSON responses
 * ✅ Session management
 * ✅ Distributed caching (Redis, Hazelcast)
 * ✅ Message queues (RabbitMQ, Kafka)
 * ✅ Event sourcing
 * ✅ RMI/RPC calls
 * ✅ WebSocket messages
 * 
 * ❌ ALTERNATIVES:
 * ===============
 * ❌ Use protocol buffers (Protobuf)
 * ❌ Use Apache Avro
 * ❌ Use MessagePack
 * ❌ Avoid serialization (use DTOs)
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
@ImportRuntimeHints(SerializationConfigPattern.SerializationConfigHints.class)
public class SerializationConfigPattern {

    public static void main(String[] args) {
        SpringApplication.run(SerializationConfigPattern.class, args);
    }

    /**
     * Comprehensive Serialization Configuration
     */
    static class SerializationConfigHints implements RuntimeHintsRegistrar {
        
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // 1. Register Java Serializable classes
            registerJavaSerializable(hints);
            
            // 2. Register Jackson JSON classes
            registerJacksonClasses(hints);
            
            // 3. Register type hierarchies
            registerTypeHierarchies(hints);
            
            // 4. Register custom serializers
            registerCustomSerializers(hints);
            
            // 5. Register enums
            registerEnums(hints);
            
            System.out.println("✅ Serialization configuration registered successfully");
        }

        private void registerJavaSerializable(RuntimeHints hints) {
            // Register Java Serializable classes
            hints.serialization()
                .registerType(SerializableEntity.class)
                .registerType(SerializableDTO.class)
                .registerType(SerializableEvent.class)
                .registerType(CacheableObject.class)
                .registerType(SessionData.class);
        }

        private void registerJacksonClasses(RuntimeHints hints) {
            // Register for Jackson JSON serialization/deserialization
            
            // User entity
            hints.reflection().registerType(
                UserEntity.class,
                builder -> builder
                    .withMembers(
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.DECLARED_FIELDS
                    )
            );

            // Product entity
            hints.reflection().registerType(
                ProductEntity.class,
                builder -> builder
                    .withMembers(
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.DECLARED_FIELDS
                    )
            );

            // Order entity
            hints.reflection().registerType(
                OrderEntity.class,
                builder -> builder
                    .withMembers(
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.DECLARED_FIELDS
                    )
            );

            // API Response wrapper
            hints.reflection().registerType(
                ApiResponse.class,
                builder -> builder
                    .withMembers(
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.DECLARED_FIELDS
                    )
            );
        }

        private void registerTypeHierarchies(RuntimeHints hints) {
            // Register base class and subclasses
            hints.serialization()
                .registerType(BaseEvent.class)
                .registerType(UserCreatedEvent.class)
                .registerType(OrderPlacedEvent.class)
                .registerType(PaymentProcessedEvent.class);
            
            // Register for reflection too (for JSON)
            hints.reflection().registerType(
                BaseEvent.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.DECLARED_FIELDS
            );
            hints.reflection().registerType(
                UserCreatedEvent.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.DECLARED_FIELDS
            );
        }

        private void registerCustomSerializers(RuntimeHints hints) {
            // Register custom Jackson serializers/deserializers
            hints.reflection().registerType(
                CustomDateSerializer.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS
            );
        }

        private void registerEnums(RuntimeHints hints) {
            // Register enums for serialization
            hints.serialization()
                .registerType(OrderStatus.class)
                .registerType(PaymentMethod.class);
            
            hints.reflection().registerType(
                OrderStatus.class,
                MemberCategory.INVOKE_DECLARED_METHODS
            );
        }
    }
}

// ============================================================================
// JAVA SERIALIZABLE CLASSES
// ============================================================================

/**
 * Serializable Entity (Java Serialization)
 */
class SerializableEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private Date createdAt;

    public SerializableEntity() {
        this.createdAt = new Date();
    }

    public SerializableEntity(Long id, String name) {
        this.id = id;
        this.name = name;
        this.createdAt = new Date();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

/**
 * Serializable DTO
 */
class SerializableDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String value;
    private Integer count;

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}

/**
 * Serializable Event
 */
class SerializableEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String eventType;
    private String payload;
    private long timestamp;

    public SerializableEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public long getTimestamp() { return timestamp; }
}

/**
 * Cacheable Object
 */
class CacheableObject implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String cacheKey;
    private Object data;
    private long expiryTime;

    public String getCacheKey() { return cacheKey; }
    public void setCacheKey(String cacheKey) { this.cacheKey = cacheKey; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    public long getExpiryTime() { return expiryTime; }
    public void setExpiryTime(long expiryTime) { this.expiryTime = expiryTime; }
}

/**
 * Session Data
 */
class SessionData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String sessionId;
    private String userId;
    private Map<String, Object> attributes;

    public SessionData() {
        this.attributes = new HashMap<>();
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
}

// ============================================================================
// JACKSON JSON CLASSES
// ============================================================================

/**
 * User Entity (Jackson JSON)
 */
class UserEntity {
    private Long id;
    private String username;
    private String email;
    private boolean active;
    private Date registeredAt;

    public UserEntity() {}

    public UserEntity(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.active = true;
        this.registeredAt = new Date();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Date getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Date registeredAt) { this.registeredAt = registeredAt; }
}

/**
 * Product Entity (Jackson JSON)
 */
class ProductEntity {
    private Long id;
    private String name;
    private Double price;
    private Integer stock;
    private String category;

    public ProductEntity() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}

/**
 * Order Entity (Jackson JSON)
 */
class OrderEntity {
    private Long id;
    private Long userId;
    private List<OrderItem> items;
    private OrderStatus status;
    private Double total;
    private Date createdAt;

    public OrderEntity() {
        this.items = new ArrayList<>();
        this.createdAt = new Date();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

/**
 * Order Item
 */
class OrderItem {
    private Long productId;
    private Integer quantity;
    private Double price;

    public OrderItem() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}

/**
 * API Response Wrapper
 */
class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private long timestamp;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

// ============================================================================
// TYPE HIERARCHIES
// ============================================================================

/**
 * Base Event Class
 */
abstract class BaseEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected String eventId;
    protected String eventType;
    protected long timestamp;

    public BaseEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public long getTimestamp() { return timestamp; }
}

/**
 * User Created Event
 */
class UserCreatedEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private String username;

    public UserCreatedEvent() {
        this.eventType = "USER_CREATED";
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}

/**
 * Order Placed Event
 */
class OrderPlacedEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;
    
    private Long orderId;
    private Double amount;

    public OrderPlacedEvent() {
        this.eventType = "ORDER_PLACED";
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}

/**
 * Payment Processed Event
 */
class PaymentProcessedEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;
    
    private Long paymentId;
    private PaymentMethod method;

    public PaymentProcessedEvent() {
        this.eventType = "PAYMENT_PROCESSED";
    }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
}

// ============================================================================
// ENUMS
// ============================================================================

/**
 * Order Status Enum
 */
enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

/**
 * Payment Method Enum
 */
enum PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    PAYPAL,
    BANK_TRANSFER,
    CASH
}

// ============================================================================
// CUSTOM SERIALIZERS
// ============================================================================

/**
 * Custom Date Serializer (placeholder)
 */
class CustomDateSerializer {
    // Custom serialization logic would go here
    public String serialize(Date date) {
        return date != null ? String.valueOf(date.getTime()) : null;
    }
}

// ============================================================================
// SERVICES
// ============================================================================

/**
 * Serialization Test Service
 */
@Service
class SerializationTestService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Test Java serialization
     */
    public Map<String, Object> testJavaSerialization() {
        try {
            SerializableEntity entity = new SerializableEntity(1L, "Test Entity");
            
            // Serialize
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(entity);
            oos.close();
            
            byte[] serialized = baos.toByteArray();
            
            // Deserialize
            ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
            ObjectInputStream ois = new ObjectInputStream(bais);
            SerializableEntity deserialized = (SerializableEntity) ois.readObject();
            ois.close();
            
            return Map.of(
                "success", true,
                "original", entity.getName(),
                "deserialized", deserialized.getName(),
                "serializedSize", serialized.length
            );
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * Test Jackson JSON serialization
     */
    public Map<String, Object> testJacksonSerialization() {
        try {
            UserEntity user = new UserEntity(1L, "john_doe", "john@example.com");
            
            // Serialize to JSON
            String json = objectMapper.writeValueAsString(user);
            
            // Deserialize from JSON
            UserEntity deserialized = objectMapper.readValue(json, UserEntity.class);
            
            return Map.of(
                "success", true,
                "original", user.getUsername(),
                "deserialized", deserialized.getUsername(),
                "json", json
            );
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * Test event hierarchy serialization
     */
    public Map<String, Object> testEventSerialization() {
        try {
            UserCreatedEvent event = new UserCreatedEvent();
            event.setUserId(123L);
            event.setUsername("new_user");
            
            String json = objectMapper.writeValueAsString(event);
            
            return Map.of(
                "success", true,
                "eventType", event.getEventType(),
                "json", json
            );
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
}

/**
 * Serialization Configuration REST Controller
 */
@RestController
@RequestMapping("/api/serialization-config")
class SerializationConfigController {

    private final SerializationTestService serializationTestService;

    public SerializationConfigController(SerializationTestService serializationTestService) {
        this.serializationTestService = serializationTestService;
    }

    /**
     * GET /api/serialization-config/test/java
     * Test Java serialization
     */
    @GetMapping("/test/java")
    public Map<String, Object> testJavaSerialization() {
        return serializationTestService.testJavaSerialization();
    }

    /**
     * GET /api/serialization-config/test/jackson
     * Test Jackson JSON serialization
     */
    @GetMapping("/test/jackson")
    public Map<String, Object> testJacksonSerialization() {
        return serializationTestService.testJacksonSerialization();
    }

    /**
     * GET /api/serialization-config/test/event
     * Test event hierarchy serialization
     */
    @GetMapping("/test/event")
    public Map<String, Object> testEventSerialization() {
        return serializationTestService.testEventSerialization();
    }

    /**
     * POST /api/serialization-config/test/user
     * Test user entity JSON round-trip
     */
    @PostMapping("/test/user")
    public ApiResponse<UserEntity> testUserSerialization(@RequestBody UserEntity user) {
        user.setUsername("Modified: " + user.getUsername());
        return new ApiResponse<>(true, "User serialization successful", user);
    }

    /**
     * POST /api/serialization-config/test/order
     * Test order entity JSON round-trip
     */
    @PostMapping("/test/order")
    public ApiResponse<OrderEntity> testOrderSerialization(@RequestBody OrderEntity order) {
        order.setStatus(OrderStatus.CONFIRMED);
        return new ApiResponse<>(true, "Order serialization successful", order);
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ TEST JAVA SERIALIZATION:
 * ----------------------------
 * curl http://localhost:8080/api/serialization-config/test/java
 * 
 * Response:
 * {
 *   "success": true,
 *   "original": "Test Entity",
 *   "deserialized": "Test Entity",
 *   "serializedSize": 123
 * }
 * 
 * 2️⃣ TEST JACKSON JSON SERIALIZATION:
 * ------------------------------------
 * curl http://localhost:8080/api/serialization-config/test/jackson
 * 
 * Response:
 * {
 *   "success": true,
 *   "original": "john_doe",
 *   "deserialized": "john_doe",
 *   "json": "{\"id\":1,\"username\":\"john_doe\",\"email\":\"john@example.com\",...}"
 * }
 * 
 * 3️⃣ TEST USER ENTITY:
 * ---------------------
 * curl -X POST http://localhost:8080/api/serialization-config/test/user \
 *   -H "Content-Type: application/json" \
 *   -d '{"id":1,"username":"alice","email":"alice@example.com","active":true}'
 * 
 * Response:
 * {
 *   "success": true,
 *   "message": "User serialization successful",
 *   "data": {"id":1,"username":"Modified: alice","email":"alice@example.com",...},
 *   "timestamp": 1234567890
 * }
 * 
 * 4️⃣ TEST ORDER ENTITY:
 * ----------------------
 * curl -X POST http://localhost:8080/api/serialization-config/test/order \
 *   -H "Content-Type: application/json" \
 *   -d '{"id":1,"userId":123,"items":[{"productId":1,"quantity":2,"price":29.99}],"status":"PENDING","total":59.98}'
 * 
 * Response:
 * {
 *   "success": true,
 *   "message": "Order serialization successful",
 *   "data": {"id":1,"userId":123,"status":"CONFIRMED",...},
 *   "timestamp": 1234567890
 * }
 */
