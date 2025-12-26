package com.example.kotlin.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 SPRING BOOT KOTLIN - BEAN DSL PATTERN 💡
 * ============================================
 * 
 * Demonstrates Kotlin Bean Definition DSL for functional bean registration.
 * The Kotlin DSL provides a type-safe, concise way to define beans programmatically
 * without using annotations, enabling more flexible and testable configurations.
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ KOTLIN BEAN DSL:
 *    - Type-safe bean definitions
 *    - Functional bean registration
 *    - Lambda-based configuration
 *    - No reflection overhead
 * 
 * 2️⃣ FUNCTIONAL BEAN REGISTRATION:
 *    - ApplicationContextInitializer
 *    - beans { } DSL block
 *    - Bean builder functions
 *    - Conditional registration
 * 
 * 3️⃣ BEAN SCOPES IN DSL:
 *    - Singleton (default)
 *    - Prototype
 *    - Request/Session (web)
 *    - Custom scopes
 * 
 * 4️⃣ DEPENDENCY INJECTION IN DSL:
 *    - Constructor injection
 *    - ref() function for dependencies
 *    - env() for environment properties
 *    - profile() for profile-specific beans
 * 
 * 📦 KOTLIN BEAN DSL (Kotlin code):
 * =================================
 * 
 * import org.springframework.context.support.beans
 * 
 * val beans = beans {
 *     // Simple bean definition
 *     bean<UserService>()
 *     
 *     // Bean with custom name
 *     bean("myCustomService") {
 *         UserService()
 *     }
 *     
 *     // Bean with dependencies using ref()
 *     bean {
 *         UserController(ref(), ref())
 *     }
 *     
 *     // Bean with environment properties
 *     bean {
 *         DataSource().apply {
 *             url = env["db.url"]
 *             username = env["db.username"]
 *         }
 *     }
 *     
 *     // Conditional bean registration
 *     profile("dev") {
 *         bean<DevDataSource>()
 *     }
 *     
 *     profile("prod") {
 *         bean<ProdDataSource>()
 *     }
 * }
 * 
 * // Register beans in application
 * class MyApplication : ApplicationContextInitializer<GenericApplicationContext> {
 *     override fun initialize(context: GenericApplicationContext) {
 *         beans.initialize(context)
 *     }
 * }
 * 
 * 🔧 BEAN BUILDER FUNCTIONS (Kotlin):
 * ====================================
 * 
 * // Bean with prototype scope
 * bean(scope = BeanDefinitionDsl.Scope.PROTOTYPE) {
 *     PrototypeService()
 * }
 * 
 * // Bean with initialization
 * bean {
 *     UserService().apply {
 *         init()
 *     }
 * }
 * 
 * // Bean with destroy method
 * bean(isLazyInit = false) {
 *     ConnectionPool().also { pool ->
 *         context.registerShutdownHook { pool.close() }
 *     }
 * }
 * 
 * 🎯 FUNCTIONAL CONFIGURATION (Kotlin):
 * =====================================
 * 
 * @SpringBootApplication
 * class MyApplication
 * 
 * fun main(args: Array<String>) {
 *     runApplication<MyApplication>(*args) {
 *         addInitializers(beans {
 *             bean<UserService>()
 *             bean<ProductService>()
 *             bean { UserController(ref(), ref()) }
 *         })
 *     }
 * }
 * 
 * 💡 ADVANTAGES OF BEAN DSL:
 * =========================
 * ✅ Type-safe bean definitions
 * ✅ No reflection overhead (faster startup)
 * ✅ More testable (pure functions)
 * ✅ Conditional registration
 * ✅ Profile-specific beans
 * ✅ Environment-aware configuration
 * ✅ Better IDE support
 * ✅ Compile-time checking
 * ✅ Functional programming style
 * ✅ Less boilerplate than Java Config
 * 
 * 🔄 COMPARISON: ANNOTATION vs DSL:
 * =================================
 * 
 * Annotation-based (Traditional):
 * --------------------------------
 * @Configuration
 * class AppConfig {
 *     @Bean
 *     fun userService() = UserService()
 *     
 *     @Bean
 *     fun userController(
 *         userService: UserService,
 *         productService: ProductService
 *     ) = UserController(userService, productService)
 * }
 * 
 * DSL-based (Functional):
 * -----------------------
 * val beans = beans {
 *     bean<UserService>()
 *     bean { UserController(ref(), ref()) }
 * }
 * 
 * 🎯 WHEN TO USE:
 * ==============
 * ✅ Kotlin-only applications
 * ✅ Need faster startup (no reflection)
 * ✅ Dynamic bean registration
 * ✅ Conditional configuration
 * ✅ Testing scenarios
 * ✅ Functional programming preference
 * ✅ Type-safe configuration
 * 
 * ❌ WHEN NOT TO USE:
 * ==================
 * ❌ Java-only projects
 * ❌ Mixed Java/Kotlin (prefer consistency)
 * ❌ Team unfamiliar with Kotlin
 * ❌ Need annotation scanning
 * ❌ Component scanning preferred
 * 
 * 💡 REAL-WORLD EXAMPLES:
 * ======================
 * 
 * Example 1: Database Configuration
 * ----------------------------------
 * beans {
 *     bean {
 *         HikariDataSource().apply {
 *             jdbcUrl = env["db.url"]
 *             username = env["db.username"]
 *             password = env["db.password"]
 *             maximumPoolSize = env.getProperty("db.pool.size", Int::class.java, 10)
 *         }
 *     }
 *     
 *     bean {
 *         JdbcTemplate(ref())
 *     }
 * }
 * 
 * Example 2: Profile-Specific Beans
 * ----------------------------------
 * beans {
 *     profile("dev") {
 *         bean<MockEmailService>()
 *     }
 *     
 *     profile("prod") {
 *         bean {
 *             SmtpEmailService(
 *                 host = env["mail.host"],
 *                 port = env.getProperty("mail.port", Int::class.java, 587)
 *             )
 *         }
 *     }
 * }
 * 
 * Example 3: Conditional Registration
 * ------------------------------------
 * beans {
 *     // Only register if Redis is available
 *     if (env.getProperty("redis.enabled", Boolean::class.java, false)) {
 *         bean {
 *             RedisTemplate<String, Any>().apply {
 *                 setConnectionFactory(ref())
 *             }
 *         }
 *     }
 * }
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
public class KotlinBeanDslPattern {

    public static void main(String[] args) {
        SpringApplication.run(KotlinBeanDslPattern.class, args);
    }
}

/**
 * Kotlin Bean DSL Documentation Service
 */
@Service
class KotlinBeanDslService {

    /**
     * Get DSL examples
     */
    public Map<String, String> getDslExamples() {
        Map<String, String> examples = new LinkedHashMap<>();
        
        examples.put("Simple Bean", 
            "val beans = beans {\n" +
            "    bean<UserService>()\n" +
            "}");
        
        examples.put("Bean with Name", 
            "bean(\"myService\") {\n" +
            "    UserService()\n" +
            "}");
        
        examples.put("Bean with Dependencies", 
            "bean {\n" +
            "    UserController(ref(), ref())\n" +
            "}");
        
        examples.put("Bean with Environment", 
            "bean {\n" +
            "    DataSource().apply {\n" +
            "        url = env[\"db.url\"]\n" +
            "        username = env[\"db.username\"]\n" +
            "    }\n" +
            "}");
        
        examples.put("Profile-Specific Bean", 
            "profile(\"dev\") {\n" +
            "    bean<MockService>()\n" +
            "}\n" +
            "profile(\"prod\") {\n" +
            "    bean<RealService>()\n" +
            "}");
        
        examples.put("Prototype Scope", 
            "bean(scope = BeanDefinitionDsl.Scope.PROTOTYPE) {\n" +
            "    PrototypeService()\n" +
            "}");
        
        examples.put("Lazy Initialization", 
            "bean(isLazyInit = true) {\n" +
            "    ExpensiveService()\n" +
            "}");
        
        examples.put("Conditional Registration", 
            "if (env.getProperty(\"feature.enabled\", Boolean::class.java, false)) {\n" +
            "    bean<FeatureService>()\n" +
            "}");
        
        return examples;
    }

    /**
     * Get DSL advantages
     */
    public List<String> getDslAdvantages() {
        List<String> advantages = new ArrayList<>();
        
        advantages.add("✅ Type-safe bean definitions");
        advantages.add("✅ No reflection overhead (faster startup)");
        advantages.add("✅ Compile-time checking");
        advantages.add("✅ Better IDE support and autocomplete");
        advantages.add("✅ Functional programming style");
        advantages.add("✅ Less boilerplate than Java Config");
        advantages.add("✅ More testable (pure functions)");
        advantages.add("✅ Conditional registration without @Conditional");
        advantages.add("✅ Profile-specific beans easily");
        advantages.add("✅ Environment-aware configuration");
        advantages.add("💡 ~20-30% faster startup vs annotation scanning");
        
        return advantages;
    }

    /**
     * Get bean registration patterns
     */
    public Map<String, List<String>> getBeanRegistrationPatterns() {
        Map<String, List<String>> patterns = new LinkedHashMap<>();
        
        List<String> simpleRegistration = new ArrayList<>();
        simpleRegistration.add("bean<UserService>() - Simple bean");
        simpleRegistration.add("bean(\"name\") { UserService() } - Named bean");
        simpleRegistration.add("bean { UserService() } - Lambda-based");
        patterns.put("Simple Registration", simpleRegistration);
        
        List<String> dependencyInjection = new ArrayList<>();
        dependencyInjection.add("bean { UserController(ref()) } - Single dependency");
        dependencyInjection.add("bean { Controller(ref(), ref()) } - Multiple dependencies");
        dependencyInjection.add("bean { Service(ref<UserRepo>()) } - Type-specific ref");
        patterns.put("Dependency Injection", dependencyInjection);
        
        List<String> environmentAccess = new ArrayList<>();
        environmentAccess.add("env[\"property\"] - String property");
        environmentAccess.add("env.getProperty(\"port\", Int::class.java, 8080) - Typed property");
        environmentAccess.add("env.activeProfiles - Active profiles");
        patterns.put("Environment Access", environmentAccess);
        
        List<String> scopeConfiguration = new ArrayList<>();
        scopeConfiguration.add("scope = Scope.SINGLETON - Default scope");
        scopeConfiguration.add("scope = Scope.PROTOTYPE - New instance per request");
        scopeConfiguration.add("scope = Scope.REQUEST - Web request scope");
        scopeConfiguration.add("scope = Scope.SESSION - Web session scope");
        patterns.put("Scope Configuration", scopeConfiguration);
        
        List<String> conditionalRegistration = new ArrayList<>();
        conditionalRegistration.add("profile(\"dev\") { bean<MockService>() }");
        conditionalRegistration.add("if (condition) { bean<Service>() }");
        conditionalRegistration.add("when { profile matches \"prod\" -> bean<ProdService>() }");
        patterns.put("Conditional Registration", conditionalRegistration);
        
        return patterns;
    }

    /**
     * Get DSL vs Annotation comparison
     */
    public Map<String, Map<String, String>> getComparisonTable() {
        Map<String, Map<String, String>> comparison = new LinkedHashMap<>();
        
        Map<String, String> startup = new LinkedHashMap<>();
        startup.put("Annotation", "~3-5s (reflection + classpath scanning)");
        startup.put("Bean DSL", "~2-3s (no reflection, direct registration)");
        startup.put("Improvement", "20-40% faster");
        comparison.put("Startup Time", startup);
        
        Map<String, String> memory = new LinkedHashMap<>();
        memory.put("Annotation", "Higher (metadata storage, reflection)");
        memory.put("Bean DSL", "Lower (direct bean definitions)");
        memory.put("Improvement", "5-10% memory reduction");
        comparison.put("Memory Usage", memory);
        
        Map<String, String> typeSafety = new LinkedHashMap<>();
        typeSafety.put("Annotation", "Runtime errors (incorrect names, types)");
        typeSafety.put("Bean DSL", "Compile-time errors (type-safe)");
        typeSafety.put("Improvement", "Catch errors at compile time");
        comparison.put("Type Safety", typeSafety);
        
        Map<String, String> testability = new LinkedHashMap<>();
        testability.put("Annotation", "Need Spring context for testing");
        testability.put("Bean DSL", "Pure functions, easy to test");
        testability.put("Improvement", "Faster, isolated unit tests");
        comparison.put("Testability", testability);
        
        Map<String, String> flexibility = new LinkedHashMap<>();
        flexibility.put("Annotation", "Static configuration, limited conditionals");
        flexibility.put("Bean DSL", "Dynamic registration, full Kotlin power");
        flexibility.put("Improvement", "Complex conditional logic");
        comparison.put("Flexibility", flexibility);
        
        return comparison;
    }

    /**
     * Get real-world use cases
     */
    public List<String> getRealWorldUseCases() {
        List<String> useCases = new ArrayList<>();
        
        useCases.add("🔧 Database Configuration - Dynamic datasource creation");
        useCases.add("🔧 Microservices - Fast startup for serverless/containers");
        useCases.add("🔧 Multi-tenant Apps - Runtime bean registration per tenant");
        useCases.add("🔧 Feature Flags - Conditional service registration");
        useCases.add("🔧 Testing - Mock bean registration without Spring context");
        useCases.add("🔧 Cloud Native - Environment-specific configuration");
        useCases.add("🔧 Plugin Systems - Dynamic plugin bean registration");
        useCases.add("🔧 A/B Testing - Variant-specific service beans");
        useCases.add("💡 Used by: Spring Fu, Spring Cloud Function");
        
        return useCases;
    }

    /**
     * Get best practices
     */
    public List<String> getBestPractices() {
        List<String> practices = new ArrayList<>();
        
        practices.add("✅ Use bean DSL for Kotlin-only projects");
        practices.add("✅ Combine with annotations for gradual migration");
        practices.add("✅ Keep bean definitions close to usage");
        practices.add("✅ Use ref<Type>() for type-safe dependency injection");
        practices.add("✅ Leverage profile() for environment-specific beans");
        practices.add("✅ Use env for external configuration");
        practices.add("✅ Prefer functional style over imperative");
        practices.add("✅ Test bean initialization separately");
        practices.add("✅ Document conditional logic clearly");
        practices.add("⚠️ Don't mix DSL and @Configuration in same module");
        practices.add("⚠️ Avoid complex logic in bean lambdas");
        practices.add("💡 Use for fast-starting microservices");
        
        return practices;
    }
}

/**
 * Kotlin Bean DSL REST Controller
 */
@RestController
@RequestMapping("/api/kotlin-bean-dsl")
class KotlinBeanDslController {

    private final KotlinBeanDslService kotlinBeanDslService;

    public KotlinBeanDslController(KotlinBeanDslService kotlinBeanDslService) {
        this.kotlinBeanDslService = kotlinBeanDslService;
    }

    /**
     * GET /api/kotlin-bean-dsl/examples
     * Get DSL examples
     */
    @GetMapping("/examples")
    public Map<String, String> getExamples() {
        return kotlinBeanDslService.getDslExamples();
    }

    /**
     * GET /api/kotlin-bean-dsl/advantages
     * Get DSL advantages
     */
    @GetMapping("/advantages")
    public List<String> getAdvantages() {
        return kotlinBeanDslService.getDslAdvantages();
    }

    /**
     * GET /api/kotlin-bean-dsl/patterns
     * Get bean registration patterns
     */
    @GetMapping("/patterns")
    public Map<String, List<String>> getPatterns() {
        return kotlinBeanDslService.getBeanRegistrationPatterns();
    }

    /**
     * GET /api/kotlin-bean-dsl/comparison
     * Get DSL vs Annotation comparison
     */
    @GetMapping("/comparison")
    public Map<String, Map<String, String>> getComparison() {
        return kotlinBeanDslService.getComparisonTable();
    }

    /**
     * GET /api/kotlin-bean-dsl/use-cases
     * Get real-world use cases
     */
    @GetMapping("/use-cases")
    public List<String> getUseCases() {
        return kotlinBeanDslService.getRealWorldUseCases();
    }

    /**
     * GET /api/kotlin-bean-dsl/best-practices
     * Get best practices
     */
    @GetMapping("/best-practices")
    public List<String> getBestPractices() {
        return kotlinBeanDslService.getBestPractices();
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ COMPLETE APPLICATION WITH BEAN DSL (Kotlin):
 * ------------------------------------------------
 * import org.springframework.boot.autoconfigure.SpringBootApplication
 * import org.springframework.boot.runApplication
 * import org.springframework.context.support.beans
 * 
 * @SpringBootApplication
 * class MyApplication
 * 
 * fun main(args: Array<String>) {
 *     runApplication<MyApplication>(*args) {
 *         addInitializers(beans {
 *             // Services
 *             bean<UserRepository>()
 *             bean<UserService>()
 *             bean<ProductService>()
 *             
 *             // Controllers with dependencies
 *             bean {
 *                 UserController(ref(), ref())
 *             }
 *             
 *             // Configuration beans
 *             bean {
 *                 RestTemplate().apply {
 *                     interceptors.add(LoggingInterceptor())
 *                 }
 *             }
 *             
 *             // Profile-specific
 *             profile("dev") {
 *                 bean<H2DataSource>()
 *             }
 *             
 *             profile("prod") {
 *                 bean {
 *                     HikariDataSource().apply {
 *                         jdbcUrl = env["db.url"]
 *                         username = env["db.username"]
 *                         password = env["db.password"]
 *                     }
 *                 }
 *             }
 *         })
 *     }
 * }
 * 
 * 2️⃣ SEPARATE BEAN DEFINITIONS FILE (Kotlin):
 * --------------------------------------------
 * // beans.kt
 * import org.springframework.context.support.beans
 * 
 * val dataSourceBeans = beans {
 *     bean {
 *         HikariDataSource().apply {
 *             jdbcUrl = env["db.url"]
 *             username = env["db.username"]
 *             password = env["db.password"]
 *             maximumPoolSize = env.getProperty("db.pool.size", Int::class.java, 10)
 *         }
 *     }
 *     
 *     bean {
 *         JdbcTemplate(ref())
 *     }
 * }
 * 
 * val serviceBeans = beans {
 *     bean<UserService>()
 *     bean<ProductService>()
 *     bean<OrderService>()
 * }
 * 
 * val webBeans = beans {
 *     bean { UserController(ref(), ref()) }
 *     bean { ProductController(ref()) }
 * }
 * 
 * // Application.kt
 * fun main(args: Array<String>) {
 *     runApplication<MyApplication>(*args) {
 *         addInitializers(
 *             dataSourceBeans,
 *             serviceBeans,
 *             webBeans
 *         )
 *     }
 * }
 * 
 * 3️⃣ TESTING WITH BEAN DSL (Kotlin):
 * -----------------------------------
 * import org.junit.jupiter.api.Test
 * import org.springframework.context.support.beans
 * import org.springframework.context.support.GenericApplicationContext
 * 
 * class UserServiceTest {
 *     @Test
 *     fun `test user service`() {
 *         // Create test context with bean DSL
 *         val context = GenericApplicationContext().apply {
 *             beans {
 *                 bean { MockUserRepository() }
 *                 bean { UserService(ref()) }
 *             }.initialize(this)
 *             refresh()
 *         }
 *         
 *         val service = context.getBean<UserService>()
 *         assertNotNull(service)
 *         
 *         context.close()
 *     }
 * }
 * 
 * 4️⃣ CONDITIONAL REGISTRATION (Kotlin):
 * --------------------------------------
 * beans {
 *     // Register based on property
 *     if (env.getProperty("cache.enabled", Boolean::class.java, false)) {
 *         bean {
 *             RedisCacheManager(ref()).apply {
 *                 setDefaultExpiration(3600)
 *             }
 *         }
 *     }
 *     
 *     // Register based on active profiles
 *     when {
 *         "redis" in env.activeProfiles -> bean<RedisCache>()
 *         "memcached" in env.activeProfiles -> bean<MemcachedCache>()
 *         else -> bean<InMemoryCache>()
 *     }
 *     
 *     // Register based on class presence
 *     try {
 *         Class.forName("com.example.FeatureService")
 *         bean<FeatureService>()
 *     } catch (e: ClassNotFoundException) {
 *         // Skip registration
 *     }
 * }
 * 
 * 5️⃣ GET DSL INFORMATION VIA REST API:
 * -------------------------------------
 * curl http://localhost:8080/api/kotlin-bean-dsl/examples
 * curl http://localhost:8080/api/kotlin-bean-dsl/advantages
 * curl http://localhost:8080/api/kotlin-bean-dsl/patterns
 * curl http://localhost:8080/api/kotlin-bean-dsl/comparison
 * curl http://localhost:8080/api/kotlin-bean-dsl/use-cases
 * curl http://localhost:8080/api/kotlin-bean-dsl/best-practices
 */
