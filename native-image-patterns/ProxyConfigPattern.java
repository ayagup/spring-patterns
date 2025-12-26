package com.example.nativeimage.patterns;

import org.springframework.aot.hint.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Proxy;
import java.util.*;

/**
 * 💡 SPRING BOOT NATIVE IMAGE - PROXY CONFIG PATTERN 💡
 * ======================================================
 * 
 * Demonstrates proxy configuration for GraalVM Native Image.
 * Dynamic proxies (JDK and CGLib) require special handling in native images
 * because proxy classes are generated at runtime. All proxy interfaces must
 * be explicitly registered during AOT processing.
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ JDK DYNAMIC PROXIES:
 *    - Interface-based proxies
 *    - java.lang.reflect.Proxy
 *    - InvocationHandler pattern
 *    - Must register interfaces
 * 
 * 2️⃣ CGLIB PROXIES:
 *    - Class-based proxies
 *    - Spring AOP default
 *    - Subclass generation
 *    - Runtime code generation
 * 
 * 3️⃣ SPRING AOP PROXIES:
 *    - @Transactional uses proxies
 *    - @Cacheable uses proxies
 *    - @Async uses proxies
 *    - Auto-registered by Spring
 * 
 * 4️⃣ PROXY REGISTRATION:
 *    - Register interface combinations
 *    - Spring framework interfaces
 *    - Custom business interfaces
 *    - Proxy chains
 * 
 * 📦 DEPENDENCIES:
 * ===============
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-web</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-aop</artifactId>
 * </dependency>
 * 
 * 🔧 PROXY CONFIG API:
 * ===================
 * 
 * Register JDK Proxy (Single Interface):
 * ---------------------------------------
 * hints.proxies().registerJdkProxy(MyInterface.class);
 * 
 * Register JDK Proxy (Multiple Interfaces):
 * ------------------------------------------
 * hints.proxies().registerJdkProxy(
 *     Interface1.class,
 *     Interface2.class,
 *     Interface3.class
 * );
 * 
 * Register Spring AOP Proxy:
 * --------------------------
 * hints.proxies().registerJdkProxy(
 *     MyBusinessInterface.class,
 *     org.springframework.aop.SpringProxy.class,
 *     org.springframework.aop.framework.Advised.class,
 *     org.springframework.core.DecoratingProxy.class
 * );
 * 
 * 🎯 COMMON PROXY SCENARIOS:
 * =========================
 * 
 * @Transactional Service:
 * -----------------------
 * @Service
 * @Transactional
 * public class UserService implements UserRepository {
 *     // Spring creates AOP proxy for transaction management
 * }
 * 
 * Registration:
 * hints.proxies().registerJdkProxy(
 *     UserRepository.class,
 *     SpringProxy.class,
 *     Advised.class,
 *     DecoratingProxy.class
 * );
 * 
 * @Cacheable Service:
 * ------------------
 * @Service
 * public class ProductService implements ProductRepository {
 *     @Cacheable("products")
 *     public Product findById(Long id) { ... }
 * }
 * 
 * Registration:
 * hints.proxies().registerJdkProxy(
 *     ProductRepository.class,
 *     SpringProxy.class,
 *     Advised.class,
 *     DecoratingProxy.class
 * );
 * 
 * Custom Proxy:
 * -------------
 * MyInterface proxy = (MyInterface) Proxy.newProxyInstance(
 *     classLoader,
 *     new Class[]{MyInterface.class},
 *     invocationHandler
 * );
 * 
 * Registration:
 * hints.proxies().registerJdkProxy(MyInterface.class);
 * 
 * 💡 WHEN TO USE PROXY CONFIG:
 * ===========================
 * ✅ Spring AOP (@Transactional, @Cacheable, @Async)
 * ✅ Spring Data repositories
 * ✅ Feign clients
 * ✅ Custom JDK proxies
 * ✅ Interface-based services
 * ✅ Aspect-oriented programming
 * ✅ Dynamic interface implementations
 * 
 * ❌ ALTERNATIVES:
 * ===============
 * ❌ Use concrete classes (avoid interfaces)
 * ❌ Disable proxying (proxyTargetClass=false)
 * ❌ Use AspectJ compile-time weaving
 * ❌ Manual proxy registration in reflect-config.json
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
@ImportRuntimeHints(ProxyConfigPattern.ProxyConfigHints.class)
public class ProxyConfigPattern {

    public static void main(String[] args) {
        SpringApplication.run(ProxyConfigPattern.class, args);
    }

    /**
     * Comprehensive Proxy Configuration
     */
    static class ProxyConfigHints implements RuntimeHintsRegistrar {
        
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // 1. Business interfaces (for AOP proxies)
            registerBusinessProxies(hints);
            
            // 2. Spring AOP proxies
            registerSpringAopProxies(hints);
            
            // 3. Custom proxies
            registerCustomProxies(hints);
            
            // 4. Third-party proxies
            registerThirdPartyProxies(hints);
            
            System.out.println("✅ Proxy configuration registered successfully");
        }

        private void registerBusinessProxies(RuntimeHints hints) {
            // User service proxy (for @Transactional)
            hints.proxies().registerJdkProxy(
                UserService.class,
                org.springframework.aop.SpringProxy.class,
                org.springframework.aop.framework.Advised.class,
                org.springframework.core.DecoratingProxy.class
            );

            // Product service proxy (for @Cacheable)
            hints.proxies().registerJdkProxy(
                ProductService.class,
                org.springframework.aop.SpringProxy.class,
                org.springframework.aop.framework.Advised.class,
                org.springframework.core.DecoratingProxy.class
            );

            // Order service proxy (for @Async)
            hints.proxies().registerJdkProxy(
                OrderService.class,
                org.springframework.aop.SpringProxy.class,
                org.springframework.aop.framework.Advised.class,
                org.springframework.core.DecoratingProxy.class
            );
        }

        private void registerSpringAopProxies(RuntimeHints hints) {
            // Standard Spring AOP proxy interfaces
            hints.proxies().registerJdkProxy(
                org.springframework.aop.SpringProxy.class,
                org.springframework.aop.framework.Advised.class,
                org.springframework.core.DecoratingProxy.class
            );
        }

        private void registerCustomProxies(hints) {
            // Custom business interfaces
            hints.proxies().registerJdkProxy(PaymentProcessor.class);
            hints.proxies().registerJdkProxy(NotificationSender.class);
            hints.proxies().registerJdkProxy(DataValidator.class);
            
            // Multiple interfaces
            hints.proxies().registerJdkProxy(
                Auditable.class,
                Traceable.class
            );
        }

        private void registerThirdPartyProxies(RuntimeHints hints) {
            // Common third-party proxy interfaces
            // Example: Feign clients, Spring Data repositories
            // These are often auto-registered by frameworks
        }
    }
}

// ============================================================================
// BUSINESS INTERFACES
// ============================================================================

/**
 * User Service Interface (for AOP proxy)
 */
interface UserService {
    UserEntity createUser(String username, String email);
    UserEntity findById(Long id);
    List<UserEntity> findAll();
    void deleteUser(Long id);
}

/**
 * Product Service Interface (for AOP proxy)
 */
interface ProductService {
    ProductEntity createProduct(String name, Double price);
    ProductEntity findById(Long id);
    List<ProductEntity> findAll();
    void updateStock(Long id, Integer quantity);
}

/**
 * Order Service Interface (for AOP proxy)
 */
interface OrderService {
    OrderEntity createOrder(Long userId, List<Long> productIds);
    OrderEntity findById(Long id);
    void processOrder(Long orderId);
    void cancelOrder(Long orderId);
}

// ============================================================================
// CUSTOM INTERFACES
// ============================================================================

/**
 * Payment Processor Interface
 */
interface PaymentProcessor {
    boolean processPayment(Long orderId, Double amount);
    void refundPayment(Long paymentId);
}

/**
 * Notification Sender Interface
 */
interface NotificationSender {
    void sendEmail(String to, String subject, String body);
    void sendSMS(String phoneNumber, String message);
}

/**
 * Data Validator Interface
 */
interface DataValidator {
    boolean validate(Object data);
    List<String> getValidationErrors();
}

/**
 * Auditable Interface
 */
interface Auditable {
    String getAuditInfo();
    void recordAudit(String action);
}

/**
 * Traceable Interface
 */
interface Traceable {
    String getTraceId();
    void setTraceId(String traceId);
}

// ============================================================================
// ENTITY CLASSES
// ============================================================================

/**
 * User Entity
 */
class UserEntity {
    private Long id;
    private String username;
    private String email;

    public UserEntity() {}
    public UserEntity(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

/**
 * Product Entity
 */
class ProductEntity {
    private Long id;
    private String name;
    private Double price;
    private Integer stock;

    public ProductEntity() {}
    public ProductEntity(Long id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}

/**
 * Order Entity
 */
class OrderEntity {
    private Long id;
    private Long userId;
    private List<Long> productIds;
    private String status;

    public OrderEntity() {
        this.productIds = new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public List<Long> getProductIds() { return productIds; }
    public void setProductIds(List<Long> productIds) { this.productIds = productIds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

// ============================================================================
// SERVICES
// ============================================================================

/**
 * Proxy Configuration Service
 */
@Service
class ProxyConfigService {

    /**
     * Get all registered proxy configurations
     */
    public Map<String, List<String>> getRegisteredProxies() {
        Map<String, List<String>> proxies = new LinkedHashMap<>();
        
        List<String> businessProxies = new ArrayList<>();
        businessProxies.add("UserService + Spring AOP interfaces");
        businessProxies.add("ProductService + Spring AOP interfaces");
        businessProxies.add("OrderService + Spring AOP interfaces");
        proxies.put("Business Service Proxies", businessProxies);
        
        List<String> springProxies = new ArrayList<>();
        springProxies.add("SpringProxy");
        springProxies.add("Advised");
        springProxies.add("DecoratingProxy");
        proxies.put("Spring AOP Proxies", springProxies);
        
        List<String> customProxies = new ArrayList<>();
        customProxies.add("PaymentProcessor");
        customProxies.add("NotificationSender");
        customProxies.add("DataValidator");
        customProxies.add("Auditable + Traceable");
        proxies.put("Custom Proxies", customProxies);
        
        return proxies;
    }

    /**
     * Get proxy types explanation
     */
    public Map<String, String> getProxyTypes() {
        Map<String, String> types = new LinkedHashMap<>();
        
        types.put("JDK Dynamic Proxy", "Interface-based, uses java.lang.reflect.Proxy");
        types.put("CGLib Proxy", "Class-based, generates subclasses at runtime");
        types.put("Spring AOP Proxy", "Either JDK or CGLib, adds AOP behavior");
        types.put("Custom Proxy", "Application-specific proxy implementations");
        
        return types;
    }

    /**
     * Get proxy use cases
     */
    public List<String> getProxyUseCases() {
        List<String> useCases = new ArrayList<>();
        
        useCases.add("✅ @Transactional - Transaction management");
        useCases.add("✅ @Cacheable - Caching operations");
        useCases.add("✅ @Async - Asynchronous execution");
        useCases.add("✅ @Secured - Security enforcement");
        useCases.add("✅ Spring Data Repositories - Database access");
        useCases.add("✅ Feign Clients - HTTP clients");
        useCases.add("✅ Custom AOP Aspects - Cross-cutting concerns");
        useCases.add("✅ Interface-based Design - Loose coupling");
        
        return useCases;
    }

    /**
     * Get proxy best practices
     */
    public List<String> getProxyBestPractices() {
        List<String> practices = new ArrayList<>();
        
        practices.add("✅ Prefer interface-based design");
        practices.add("✅ Register all proxied interfaces");
        practices.add("✅ Test proxies in native image");
        practices.add("✅ Document proxy requirements");
        practices.add("✅ Use Spring AOP annotations");
        practices.add("⚠️ Avoid excessive proxy chains");
        practices.add("⚠️ Be aware of proxy performance overhead");
        practices.add("⚠️ Don't call proxied methods within same class");
        practices.add("💡 Consider AspectJ for complex AOP");
        
        return practices;
    }
}

/**
 * Proxy Test Service
 */
@Service
class ProxyTestService {

    /**
     * Test JDK proxy creation
     */
    public Map<String, Object> testJdkProxyCreation() {
        try {
            // Create a simple JDK proxy
            PaymentProcessor proxy = (PaymentProcessor) Proxy.newProxyInstance(
                PaymentProcessor.class.getClassLoader(),
                new Class[]{PaymentProcessor.class},
                (proxyObj, method, args) -> {
                    if ("processPayment".equals(method.getName())) {
                        return true; // Mock implementation
                    }
                    return null;
                }
            );
            
            boolean result = proxy.processPayment(1L, 100.0);
            
            return Map.of(
                "success", true,
                "proxyClass", proxy.getClass().getName(),
                "isProxy", Proxy.isProxyClass(proxy.getClass()),
                "result", result
            );
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * Check if class is proxied
     */
    public Map<String, Object> checkIfProxied(Object obj) {
        boolean isProxy = Proxy.isProxyClass(obj.getClass());
        boolean isSpringProxy = org.springframework.aop.support.AopUtils.isAopProxy(obj);
        boolean isJdkProxy = org.springframework.aop.support.AopUtils.isJdkDynamicProxy(obj);
        boolean isCglibProxy = org.springframework.aop.support.AopUtils.isCglibProxy(obj);
        
        return Map.of(
            "className", obj.getClass().getName(),
            "isJdkProxy", isProxy || isJdkProxy,
            "isCglibProxy", isCglibProxy,
            "isSpringAopProxy", isSpringProxy
        );
    }
}

/**
 * Proxy Configuration REST Controller
 */
@RestController
@RequestMapping("/api/proxy-config")
class ProxyConfigController {

    private final ProxyConfigService proxyConfigService;
    private final ProxyTestService proxyTestService;

    public ProxyConfigController(ProxyConfigService proxyConfigService,
                                  ProxyTestService proxyTestService) {
        this.proxyConfigService = proxyConfigService;
        this.proxyTestService = proxyTestService;
    }

    /**
     * GET /api/proxy-config/registered
     * Get all registered proxy configurations
     */
    @GetMapping("/registered")
    public Map<String, List<String>> getRegisteredProxies() {
        return proxyConfigService.getRegisteredProxies();
    }

    /**
     * GET /api/proxy-config/types
     * Get proxy types explanation
     */
    @GetMapping("/types")
    public Map<String, String> getProxyTypes() {
        return proxyConfigService.getProxyTypes();
    }

    /**
     * GET /api/proxy-config/use-cases
     * Get proxy use cases
     */
    @GetMapping("/use-cases")
    public List<String> getProxyUseCases() {
        return proxyConfigService.getProxyUseCases();
    }

    /**
     * GET /api/proxy-config/best-practices
     * Get proxy best practices
     */
    @GetMapping("/best-practices")
    public List<String> getProxyBestPractices() {
        return proxyConfigService.getProxyBestPractices();
    }

    /**
     * GET /api/proxy-config/test/jdk-proxy
     * Test JDK proxy creation
     */
    @GetMapping("/test/jdk-proxy")
    public Map<String, Object> testJdkProxy() {
        return proxyTestService.testJdkProxyCreation();
    }

    /**
     * GET /api/proxy-config/test/check-proxy
     * Check if service is proxied
     */
    @GetMapping("/test/check-proxy")
    public Map<String, Object> checkProxy() {
        return proxyTestService.checkIfProxied(proxyConfigService);
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ GET REGISTERED PROXIES:
 * ---------------------------
 * curl http://localhost:8080/api/proxy-config/registered
 * 
 * Response:
 * {
 *   "Business Service Proxies": [
 *     "UserService + Spring AOP interfaces",
 *     "ProductService + Spring AOP interfaces",
 *     "OrderService + Spring AOP interfaces"
 *   ],
 *   "Spring AOP Proxies": ["SpringProxy", "Advised", "DecoratingProxy"],
 *   "Custom Proxies": ["PaymentProcessor", "NotificationSender", ...]
 * }
 * 
 * 2️⃣ GET PROXY TYPES:
 * --------------------
 * curl http://localhost:8080/api/proxy-config/types
 * 
 * Response:
 * {
 *   "JDK Dynamic Proxy": "Interface-based, uses java.lang.reflect.Proxy",
 *   "CGLib Proxy": "Class-based, generates subclasses at runtime",
 *   "Spring AOP Proxy": "Either JDK or CGLib, adds AOP behavior",
 *   "Custom Proxy": "Application-specific proxy implementations"
 * }
 * 
 * 3️⃣ TEST JDK PROXY CREATION:
 * ----------------------------
 * curl http://localhost:8080/api/proxy-config/test/jdk-proxy
 * 
 * Response:
 * {
 *   "success": true,
 *   "proxyClass": "com.sun.proxy.$Proxy123",
 *   "isProxy": true,
 *   "result": true
 * }
 * 
 * 4️⃣ CHECK IF SERVICE IS PROXIED:
 * --------------------------------
 * curl http://localhost:8080/api/proxy-config/test/check-proxy
 * 
 * Response:
 * {
 *   "className": "com.example.ProxyConfigService$$EnhancerBySpringCGLIB$$12345",
 *   "isJdkProxy": false,
 *   "isCglibProxy": true,
 *   "isSpringAopProxy": true
 * }
 * 
 * 5️⃣ REGISTER CUSTOM PROXY:
 * --------------------------
 * public class MyRuntimeHints implements RuntimeHintsRegistrar {
 *     @Override
 *     public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
 *         // Single interface
 *         hints.proxies().registerJdkProxy(MyInterface.class);
 *         
 *         // Multiple interfaces
 *         hints.proxies().registerJdkProxy(
 *             MyInterface1.class,
 *             MyInterface2.class
 *         );
 *         
 *         // Spring AOP proxy
 *         hints.proxies().registerJdkProxy(
 *             MyBusinessInterface.class,
 *             SpringProxy.class,
 *             Advised.class,
 *             DecoratingProxy.class
 *         );
 *     }
 * }
 * 
 * 6️⃣ CREATE JDK PROXY:
 * ---------------------
 * MyInterface proxy = (MyInterface) Proxy.newProxyInstance(
 *     MyInterface.class.getClassLoader(),
 *     new Class[]{MyInterface.class},
 *     (proxyObj, method, args) -> {
 *         System.out.println("Method called: " + method.getName());
 *         // Custom logic here
 *         return null;
 *     }
 * );
 */
