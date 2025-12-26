package com.example.nativeimage.patterns;

import org.springframework.aot.hint.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 💡 SPRING BOOT NATIVE IMAGE - RUNTIME HINTS PATTERN 💡
 * =======================================================
 * 
 * Demonstrates Spring Boot Runtime Hints for GraalVM Native Image.
 * Runtime hints inform the native image compiler about code that requires
 * special handling (reflection, resources, proxies, serialization) that
 * cannot be automatically detected through static analysis.
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ RUNTIME HINTS:
 *    - Inform native image about dynamic features
 *    - Register reflection needs
 *    - Register resources to include
 *    - Register proxies and serialization
 * 
 * 2️⃣ RUNTIME HINTS REGISTRAR:
 *    - Implement RuntimeHintsRegistrar
 *    - Called during AOT processing
 *    - Programmatic hint registration
 *    - Type-safe API
 * 
 * 3️⃣ @IMPORTRUNTIMEHINTS:
 *    - Annotation to import hints
 *    - Applied to @Configuration classes
 *    - Multiple registrars supported
 * 
 * 4️⃣ HINT TYPES:
 *    - Reflection hints (classes, methods, fields)
 *    - Resource hints (files, patterns)
 *    - Proxy hints (interfaces)
 *    - Serialization hints (classes)
 *    - JNI hints (native methods)
 * 
 * 📦 DEPENDENCIES:
 * ===============
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-web</artifactId>
 * </dependency>
 * 
 * 🔧 RUNTIME HINTS API:
 * ====================
 * 
 * Reflection Hints:
 * -----------------
 * hints.reflection()
 *     .registerType(MyClass.class, 
 *         MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
 *         MemberCategory.INVOKE_DECLARED_METHODS,
 *         MemberCategory.DECLARED_FIELDS)
 *     .registerMethod(MyClass.class.getDeclaredMethod("myMethod"));
 * 
 * Resource Hints:
 * ---------------
 * hints.resources()
 *     .registerPattern("config/*.json")
 *     .registerPattern("templates/**")
 *     .registerType(MyClass.class);  // Include MyClass as resource
 * 
 * Proxy Hints:
 * ------------
 * hints.proxies()
 *     .registerJdkProxy(MyInterface1.class, MyInterface2.class);
 * 
 * Serialization Hints:
 * --------------------
 * hints.serialization()
 *     .registerType(MySerializableClass.class);
 * 
 * JNI Hints:
 * ----------
 * hints.jni()
 *     .registerType(MyClass.class,
 *         MemberCategory.INVOKE_DECLARED_METHODS);
 * 
 * 🎯 MEMBER CATEGORIES:
 * ====================
 * 
 * MemberCategory.INVOKE_DECLARED_CONSTRUCTORS:
 * - Allow reflective constructor invocation
 * - Required for @Autowired constructors
 * 
 * MemberCategory.INVOKE_DECLARED_METHODS:
 * - Allow reflective method invocation
 * - Required for @Transactional, @Cacheable, etc.
 * 
 * MemberCategory.INVOKE_PUBLIC_METHODS:
 * - Allow public method invocation only
 * 
 * MemberCategory.DECLARED_FIELDS:
 * - Access to declared fields
 * - Required for @Value, @Autowired fields
 * 
 * MemberCategory.PUBLIC_FIELDS:
 * - Access to public fields only
 * 
 * MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS:
 * - Metadata access to constructors
 * 
 * MemberCategory.INTROSPECT_DECLARED_METHODS:
 * - Metadata access to methods
 * 
 * MemberCategory.INTROSPECT_PUBLIC_METHODS:
 * - Metadata access to public methods
 * 
 * 📝 REFLECTION HINTS EXAMPLE:
 * ===========================
 * 
 * @Component
 * @Reflective  // Spring 6+ shortcut annotation
 * public class MyService {
 *     // Automatically registered for reflection
 * }
 * 
 * Or manual registration:
 * -----------------------
 * public class MyRuntimeHints implements RuntimeHintsRegistrar {
 *     @Override
 *     public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
 *         hints.reflection().registerType(MyService.class,
 *             MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
 *             MemberCategory.INVOKE_DECLARED_METHODS);
 *     }
 * }
 * 
 * 💡 WHEN TO USE RUNTIME HINTS:
 * ============================
 * ✅ Reflection-based frameworks (Jackson, Hibernate)
 * ✅ Dynamic class instantiation
 * ✅ Annotation processing at runtime
 * ✅ Resource loading (JSON, XML, properties)
 * ✅ JDK proxies (interfaces)
 * ✅ Serialization/deserialization
 * ✅ Third-party libraries without native support
 * 
 * ❌ WHEN NOT NEEDED:
 * ==================
 * ❌ Simple Spring Boot apps (auto-configured)
 * ❌ No reflection usage
 * ❌ No resource loading
 * ❌ No proxies
 * ❌ Libraries with native metadata
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
@ImportRuntimeHints({
    NativeHintsPattern.ReflectionRuntimeHints.class,
    NativeHintsPattern.ResourceRuntimeHints.class,
    NativeHintsPattern.ProxyRuntimeHints.class,
    NativeHintsPattern.SerializationRuntimeHints.class
})
public class NativeHintsPattern {

    public static void main(String[] args) {
        SpringApplication.run(NativeHintsPattern.class, args);
    }

    /**
     * REFLECTION RUNTIME HINTS
     * Registers classes, methods, fields for reflective access
     */
    static class ReflectionRuntimeHints implements RuntimeHintsRegistrar {
        
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Register entire class with all members
            hints.reflection().registerType(
                ReflectiveEntity.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.DECLARED_FIELDS
            );

            // Register specific method
            try {
                Method method = ReflectiveService.class
                    .getDeclaredMethod("processData", String.class);
                hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
                
                System.out.println("✅ Reflection hints registered: ReflectiveEntity, ReflectiveService");
            } catch (NoSuchMethodException e) {
                System.err.println("❌ Failed to register method hint: " + e.getMessage());
            }

            // Register for introspection (metadata only)
            hints.reflection().registerType(
                MetadataOnlyClass.class,
                MemberCategory.INTROSPECT_DECLARED_METHODS
            );

            // Register constructor specifically
            try {
                hints.reflection().registerConstructor(
                    ConstructorInjectedService.class.getConstructor(String.class),
                    ExecutableMode.INVOKE
                );
            } catch (NoSuchMethodException e) {
                System.err.println("❌ Failed to register constructor hint: " + e.getMessage());
            }
        }
    }

    /**
     * RESOURCE RUNTIME HINTS
     * Registers resources (files) to include in native image
     */
    static class ResourceRuntimeHints implements RuntimeHintsRegistrar {
        
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Register specific files
            hints.resources()
                .registerPattern("config/application.json")
                .registerPattern("config/database.properties")
                .registerPattern("data/initial-data.csv");

            // Register patterns (wildcards)
            hints.resources()
                .registerPattern("static/**")           // All static resources
                .registerPattern("templates/**")        // All templates
                .registerPattern("META-INF/resources/**")
                .registerPattern("i18n/*.properties");  // Internationalization

            // Register class as resource (for reading source code, etc.)
            hints.resources()
                .registerType(ResourceClass.class);

            // Register type pattern
            hints.resources()
                .registerTypeIfPresent(classLoader, 
                    "com.example.config.ExternalConfig",
                    hint -> hint.withMembers(MemberCategory.DECLARED_FIELDS));

            System.out.println("✅ Resource hints registered: config/*, templates/*, static/*");
        }
    }

    /**
     * PROXY RUNTIME HINTS
     * Registers interfaces for JDK proxy generation
     */
    static class ProxyRuntimeHints implements RuntimeHintsRegistrar {
        
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Register JDK proxy for single interface
            hints.proxies().registerJdkProxy(ProxyInterface1.class);

            // Register JDK proxy for multiple interfaces
            hints.proxies().registerJdkProxy(
                ProxyInterface1.class,
                ProxyInterface2.class
            );

            // Common Spring proxies
            hints.proxies().registerJdkProxy(
                org.springframework.aop.SpringProxy.class,
                org.springframework.aop.framework.Advised.class,
                org.springframework.core.DecoratingProxy.class
            );

            System.out.println("✅ Proxy hints registered: ProxyInterface1, ProxyInterface2");
        }
    }

    /**
     * SERIALIZATION RUNTIME HINTS
     * Registers classes for serialization/deserialization
     */
    static class SerializationRuntimeHints implements RuntimeHintsRegistrar {
        
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Register serializable classes
            hints.serialization()
                .registerType(SerializableEntity.class)
                .registerType(SerializableDTO.class);

            // Register for JSON serialization (Jackson)
            hints.reflection().registerType(
                JsonSerializableClass.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.DECLARED_FIELDS
            );

            System.out.println("✅ Serialization hints registered: SerializableEntity, SerializableDTO");
        }
    }
}

/**
 * Native Hints Configuration
 */
@Configuration
class NativeHintsConfiguration {

    @Bean
    public RuntimeHintsService runtimeHintsService() {
        return new RuntimeHintsService();
    }

    @Bean
    public ReflectionTestService reflectionTestService() {
        return new ReflectionTestService();
    }
}

/**
 * Reflective Entity (requires reflection hints)
 */
class ReflectiveEntity {
    private Long id;
    private String name;
    private String description;

    public ReflectiveEntity() {}

    public ReflectiveEntity(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return String.format("ReflectiveEntity{id=%d, name='%s', description='%s'}", 
            id, name, description);
    }
}

/**
 * Reflective Service (requires method reflection hints)
 */
@Service
class ReflectiveService {
    
    public String processData(String input) {
        return "Processed: " + input;
    }

    public Map<String, Object> getData() {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("timestamp", System.currentTimeMillis());
        data.put("processed", true);
        return data;
    }
}

/**
 * Constructor Injected Service (requires constructor reflection)
 */
class ConstructorInjectedService {
    private final String config;

    public ConstructorInjectedService(String config) {
        this.config = config;
    }

    public String getConfig() {
        return config;
    }
}

/**
 * Metadata Only Class (introspection only, no invocation)
 */
class MetadataOnlyClass {
    
    public void someMethod() {
        // Method metadata accessible, but invocation may not be
    }
}

/**
 * Resource Class (included as resource)
 */
class ResourceClass {
    public static final String RESOURCE_DATA = "Resource content";
}

/**
 * Proxy Interfaces
 */
interface ProxyInterface1 {
    String method1();
}

interface ProxyInterface2 {
    String method2();
}

/**
 * Serializable Entity
 */
class SerializableEntity implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String data;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}

/**
 * Serializable DTO
 */
class SerializableDTO implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private Integer value;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
}

/**
 * JSON Serializable Class (Jackson)
 */
class JsonSerializableClass {
    private String field1;
    private Integer field2;

    public JsonSerializableClass() {}

    public String getField1() { return field1; }
    public void setField1(String field1) { this.field1 = field1; }
    public Integer getField2() { return field2; }
    public void setField2(Integer field2) { this.field2 = field2; }
}

/**
 * Runtime Hints Information Service
 */
@Service
class RuntimeHintsService {

    /**
     * Get registered hint types
     */
    public Map<String, List<String>> getRegisteredHints() {
        Map<String, List<String>> hints = new LinkedHashMap<>();
        
        List<String> reflectionHints = new ArrayList<>();
        reflectionHints.add("ReflectiveEntity (all members)");
        reflectionHints.add("ReflectiveService.processData()");
        reflectionHints.add("ConstructorInjectedService (constructor)");
        reflectionHints.add("MetadataOnlyClass (introspection only)");
        hints.put("Reflection Hints", reflectionHints);
        
        List<String> resourceHints = new ArrayList<>();
        resourceHints.add("config/*.json");
        resourceHints.add("config/*.properties");
        resourceHints.add("data/*.csv");
        resourceHints.add("static/**");
        resourceHints.add("templates/**");
        resourceHints.add("i18n/*.properties");
        hints.put("Resource Hints", resourceHints);
        
        List<String> proxyHints = new ArrayList<>();
        proxyHints.add("ProxyInterface1");
        proxyHints.add("ProxyInterface1 + ProxyInterface2");
        proxyHints.add("Spring AOP proxies");
        hints.put("Proxy Hints", proxyHints);
        
        List<String> serializationHints = new ArrayList<>();
        serializationHints.add("SerializableEntity");
        serializationHints.add("SerializableDTO");
        serializationHints.add("JsonSerializableClass (Jackson)");
        hints.put("Serialization Hints", serializationHints);
        
        return hints;
    }

    /**
     * Get member categories explanation
     */
    public Map<String, String> getMemberCategories() {
        Map<String, String> categories = new LinkedHashMap<>();
        
        categories.put("INVOKE_DECLARED_CONSTRUCTORS", "Allow reflective constructor invocation");
        categories.put("INVOKE_DECLARED_METHODS", "Allow reflective method invocation");
        categories.put("INVOKE_PUBLIC_METHODS", "Allow public method invocation only");
        categories.put("DECLARED_FIELDS", "Access to declared fields");
        categories.put("PUBLIC_FIELDS", "Access to public fields only");
        categories.put("INTROSPECT_DECLARED_CONSTRUCTORS", "Metadata access to constructors");
        categories.put("INTROSPECT_DECLARED_METHODS", "Metadata access to methods");
        categories.put("INTROSPECT_PUBLIC_METHODS", "Metadata access to public methods");
        
        return categories;
    }

    /**
     * Get runtime hints best practices
     */
    public List<String> getBestPractices() {
        List<String> practices = new ArrayList<>();
        
        practices.add("✅ Register only what's needed (minimize hints)");
        practices.add("✅ Use @Reflective annotation when possible");
        practices.add("✅ Group related hints in same registrar");
        practices.add("✅ Document why each hint is needed");
        practices.add("✅ Test native image thoroughly");
        practices.add("✅ Use specific member categories");
        practices.add("⚠️ Avoid wildcards in reflection hints");
        practices.add("⚠️ Don't register entire packages");
        
        return practices;
    }
}

/**
 * Reflection Test Service
 * Tests reflective access in native image
 */
@Service
class ReflectionTestService {

    /**
     * Test reflective instantiation
     */
    public Object testReflectiveInstantiation() {
        try {
            // This requires runtime hints!
            Class<?> clazz = ReflectiveEntity.class;
            Object instance = clazz.getDeclaredConstructor().newInstance();
            return Map.of("success", true, "instance", instance.toString());
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * Test reflective method invocation
     */
    public Object testReflectiveMethodInvocation() {
        try {
            ReflectiveService service = new ReflectiveService();
            Method method = service.getClass().getDeclaredMethod("processData", String.class);
            Object result = method.invoke(service, "test-data");
            return Map.of("success", true, "result", result);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * Test resource loading
     */
    public Object testResourceLoading() {
        try {
            // This requires resource hints!
            ClassPathResource resource = new ClassPathResource("application.properties");
            boolean exists = resource.exists();
            return Map.of("success", true, "resourceExists", exists);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
}

/**
 * Native Hints REST Controller
 */
@RestController
@RequestMapping("/api/native-hints")
class NativeHintsController {

    private final RuntimeHintsService runtimeHintsService;
    private final ReflectionTestService reflectionTestService;

    public NativeHintsController(RuntimeHintsService runtimeHintsService,
                                 ReflectionTestService reflectionTestService) {
        this.runtimeHintsService = runtimeHintsService;
        this.reflectionTestService = reflectionTestService;
    }

    /**
     * GET /api/native-hints/registered
     * Get all registered runtime hints
     */
    @GetMapping("/registered")
    public Map<String, List<String>> getRegisteredHints() {
        return runtimeHintsService.getRegisteredHints();
    }

    /**
     * GET /api/native-hints/member-categories
     * Get member categories explanation
     */
    @GetMapping("/member-categories")
    public Map<String, String> getMemberCategories() {
        return runtimeHintsService.getMemberCategories();
    }

    /**
     * GET /api/native-hints/best-practices
     * Get runtime hints best practices
     */
    @GetMapping("/best-practices")
    public List<String> getBestPractices() {
        return runtimeHintsService.getBestPractices();
    }

    /**
     * GET /api/native-hints/test/reflection-instantiation
     * Test reflective instantiation
     */
    @GetMapping("/test/reflection-instantiation")
    public Object testReflectionInstantiation() {
        return reflectionTestService.testReflectiveInstantiation();
    }

    /**
     * GET /api/native-hints/test/reflection-method
     * Test reflective method invocation
     */
    @GetMapping("/test/reflection-method")
    public Object testReflectionMethod() {
        return reflectionTestService.testReflectiveMethodInvocation();
    }

    /**
     * GET /api/native-hints/test/resource-loading
     * Test resource loading
     */
    @GetMapping("/test/resource-loading")
    public Object testResourceLoading() {
        return reflectionTestService.testResourceLoading();
    }

    /**
     * POST /api/native-hints/test/json
     * Test JSON serialization (Jackson)
     */
    @PostMapping("/test/json")
    public JsonSerializableClass testJsonSerialization(@RequestBody JsonSerializableClass input) {
        // This requires reflection hints for Jackson!
        input.setField1("Modified: " + input.getField1());
        input.setField2(input.getField2() != null ? input.getField2() + 100 : 100);
        return input;
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ REGISTER REFLECTION HINTS:
 * ------------------------------
 * public class MyRuntimeHints implements RuntimeHintsRegistrar {
 *     @Override
 *     public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
 *         hints.reflection().registerType(
 *             MyClass.class,
 *             MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
 *             MemberCategory.INVOKE_DECLARED_METHODS
 *         );
 *     }
 * }
 * 
 * @ImportRuntimeHints(MyRuntimeHints.class)
 * @SpringBootApplication
 * public class MyApplication { }
 * 
 * 2️⃣ REGISTER RESOURCE HINTS:
 * ----------------------------
 * hints.resources()
 *     .registerPattern("config/*.json")
 *     .registerPattern("templates/**");
 * 
 * 3️⃣ REGISTER PROXY HINTS:
 * -------------------------
 * hints.proxies()
 *     .registerJdkProxy(MyInterface1.class, MyInterface2.class);
 * 
 * 4️⃣ TEST RUNTIME HINTS:
 * -----------------------
 * curl http://localhost:8080/api/native-hints/registered
 * curl http://localhost:8080/api/native-hints/test/reflection-instantiation
 * curl http://localhost:8080/api/native-hints/test/reflection-method
 * curl http://localhost:8080/api/native-hints/test/resource-loading
 * 
 * 5️⃣ TEST JSON SERIALIZATION:
 * ----------------------------
 * curl -X POST http://localhost:8080/api/native-hints/test/json \
 *   -H "Content-Type: application/json" \
 *   -d '{"field1":"test","field2":42}'
 * 
 * 6️⃣ GET BEST PRACTICES:
 * -----------------------
 * curl http://localhost:8080/api/native-hints/best-practices
 * curl http://localhost:8080/api/native-hints/member-categories
 */
