package com.example.nativeimage.patterns;

import org.springframework.aot.hint.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 💡 SPRING BOOT NATIVE IMAGE - REFLECTION CONFIG PATTERN 💡
 * ===========================================================
 * 
 * Demonstrates comprehensive reflection configuration for GraalVM Native Image.
 * Reflection is heavily restricted in native images due to closed-world assumption.
 * All reflection usage must be explicitly declared via runtime hints.
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ REFLECTION RESTRICTIONS:
 *    - Static analysis cannot detect all reflection
 *    - Must explicitly register classes, methods, fields
 *    - Member categories control access levels
 * 
 * 2️⃣ MEMBER CATEGORIES:
 *    - INVOKE_DECLARED_CONSTRUCTORS: Instantiation
 *    - INVOKE_DECLARED_METHODS: Method execution
 *    - DECLARED_FIELDS: Field access
 *    - INTROSPECT_*: Metadata only (no invocation)
 * 
 * 3️⃣ REGISTRATION STRATEGIES:
 *    - RuntimeHintsRegistrar (programmatic)
 *    - @Reflective annotation (declarative)
 *    - @RegisterReflectionForBinding (binding)
 *    - reflect-config.json (manual)
 * 
 * 4️⃣ COMMON USE CASES:
 *    - Jackson JSON serialization
 *    - JPA entities
 *    - Spring annotations (@Autowired, @Value)
 *    - Dynamic proxies
 *    - Third-party libraries
 * 
 * 📦 DEPENDENCIES:
 * ===============
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-web</artifactId>
 * </dependency>
 * 
 * 🔧 REFLECTION CONFIG API:
 * ========================
 * 
 * Register Type with All Members:
 * --------------------------------
 * hints.reflection().registerType(
 *     MyClass.class,
 *     MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
 *     MemberCategory.INVOKE_DECLARED_METHODS,
 *     MemberCategory.DECLARED_FIELDS
 * );
 * 
 * Register Specific Constructor:
 * -------------------------------
 * Constructor<?> constructor = MyClass.class
 *     .getDeclaredConstructor(String.class, Integer.class);
 * hints.reflection().registerConstructor(constructor, ExecutableMode.INVOKE);
 * 
 * Register Specific Method:
 * -------------------------
 * Method method = MyClass.class.getDeclaredMethod("myMethod", String.class);
 * hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
 * 
 * Register Specific Field:
 * ------------------------
 * Field field = MyClass.class.getDeclaredField("myField");
 * hints.reflection().registerField(field);
 * 
 * Register for Introspection Only:
 * ---------------------------------
 * hints.reflection().registerType(
 *     MyClass.class,
 *     MemberCategory.INTROSPECT_DECLARED_METHODS
 * );
 * 
 * Conditional Registration:
 * -------------------------
 * hints.reflection().registerTypeIfPresent(classLoader,
 *     "com.example.OptionalClass",
 *     typeHint -> typeHint.withMembers(
 *         MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
 *     )
 * );
 * 
 * 🎯 MEMBER CATEGORY MATRIX:
 * ==========================
 * 
 * ┌─────────────────────────────────────┬──────────────┬──────────────┐
 * │ Category                            │ Access Level │ Invocation   │
 * ├─────────────────────────────────────┼──────────────┼──────────────┤
 * │ INVOKE_DECLARED_CONSTRUCTORS        │ All          │ Yes          │
 * │ INVOKE_PUBLIC_CONSTRUCTORS          │ Public       │ Yes          │
 * │ INTROSPECT_DECLARED_CONSTRUCTORS    │ All          │ No (metadata)│
 * │ INTROSPECT_PUBLIC_CONSTRUCTORS      │ Public       │ No (metadata)│
 * ├─────────────────────────────────────┼──────────────┼──────────────┤
 * │ INVOKE_DECLARED_METHODS             │ All          │ Yes          │
 * │ INVOKE_PUBLIC_METHODS               │ Public       │ Yes          │
 * │ INTROSPECT_DECLARED_METHODS         │ All          │ No (metadata)│
 * │ INTROSPECT_PUBLIC_METHODS           │ Public       │ No (metadata)│
 * ├─────────────────────────────────────┼──────────────┼──────────────┤
 * │ DECLARED_FIELDS                     │ All          │ Read/Write   │
 * │ PUBLIC_FIELDS                       │ Public       │ Read/Write   │
 * └─────────────────────────────────────┴──────────────┴──────────────┘
 * 
 * 💡 WHEN TO USE REFLECTION CONFIG:
 * =================================
 * ✅ Jackson JSON processing
 * ✅ JPA/Hibernate entities
 * ✅ Spring @Autowired fields
 * ✅ Spring @Value properties
 * ✅ Dynamic bean instantiation
 * ✅ Annotation scanning at runtime
 * ✅ Third-party libraries
 * 
 * ❌ ALTERNATIVES:
 * ===============
 * ❌ Use interfaces instead of reflection
 * ❌ Use @Reflective annotation
 * ❌ Use static factory methods
 * ❌ Refactor to avoid reflection
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
@ImportRuntimeHints(ReflectionConfigPattern.ReflectionConfigHints.class)
public class ReflectionConfigPattern {

    public static void main(String[] args) {
        SpringApplication.run(ReflectionConfigPattern.class, args);
    }

    /**
     * Comprehensive Reflection Configuration
     */
    static class ReflectionConfigHints implements RuntimeHintsRegistrar {
        
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // 1. Register entity with all reflection capabilities
            registerEntityClass(hints);
            
            // 2. Register service with method invocation
            registerServiceClass(hints);
            
            // 3. Register DTO with field access
            registerDtoClass(hints);
            
            // 4. Register specific constructors
            registerConstructors(hints);
            
            // 5. Register specific methods
            registerMethods(hints);
            
            // 6. Register specific fields
            registerFields(hints);
            
            // 7. Register for introspection only
            registerIntrospectionOnly(hints);
            
            // 8. Conditional registration
            registerConditionally(hints, classLoader);
            
            System.out.println("✅ Reflection configuration registered successfully");
        }

        private void registerEntityClass(RuntimeHints hints) {
            // Full reflection access for JPA entity
            hints.reflection().registerType(
                ReflectiveEntity.class,
                builder -> builder
                    .withMembers(
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.DECLARED_FIELDS
                    )
            );
        }

        private void registerServiceClass(RuntimeHints hints) {
            // Register service with method invocation
            hints.reflection().registerType(
                ReflectiveService.class,
                builder -> builder
                    .withMembers(
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS
                    )
            );
        }

        private void registerDtoClass(RuntimeHints hints) {
            // Register DTO for Jackson serialization
            hints.reflection().registerType(
                UserDTO.class,
                builder -> builder
                    .withMembers(
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.DECLARED_FIELDS
                    )
            );

            hints.reflection().registerType(
                ProductDTO.class,
                builder -> builder
                    .withMembers(
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.DECLARED_FIELDS
                    )
            );
        }

        private void registerConstructors(RuntimeHints hints) {
            try {
                // Register default constructor
                Constructor<?> defaultConstructor = 
                    ReflectiveEntity.class.getDeclaredConstructor();
                hints.reflection().registerConstructor(
                    defaultConstructor, 
                    ExecutableMode.INVOKE
                );

                // Register parameterized constructor
                Constructor<?> paramConstructor = 
                    ReflectiveEntity.class.getDeclaredConstructor(
                        Long.class, String.class, String.class
                    );
                hints.reflection().registerConstructor(
                    paramConstructor, 
                    ExecutableMode.INVOKE
                );
                
            } catch (NoSuchMethodException e) {
                System.err.println("⚠️ Constructor not found: " + e.getMessage());
            }
        }

        private void registerMethods(RuntimeHints hints) {
            try {
                // Register specific business method
                Method processMethod = ReflectiveService.class
                    .getDeclaredMethod("processData", String.class);
                hints.reflection().registerMethod(
                    processMethod, 
                    ExecutableMode.INVOKE
                );

                // Register getter/setter methods
                Method getId = ReflectiveEntity.class.getDeclaredMethod("getId");
                hints.reflection().registerMethod(getId, ExecutableMode.INVOKE);

                Method setId = ReflectiveEntity.class
                    .getDeclaredMethod("setId", Long.class);
                hints.reflection().registerMethod(setId, ExecutableMode.INVOKE);
                
            } catch (NoSuchMethodException e) {
                System.err.println("⚠️ Method not found: " + e.getMessage());
            }
        }

        private void registerFields(RuntimeHints hints) {
            try {
                // Register specific fields
                Field idField = ReflectiveEntity.class.getDeclaredField("id");
                hints.reflection().registerField(idField);

                Field nameField = ReflectiveEntity.class.getDeclaredField("name");
                hints.reflection().registerField(nameField);
                
            } catch (NoSuchFieldException e) {
                System.err.println("⚠️ Field not found: " + e.getMessage());
            }
        }

        private void registerIntrospectionOnly(RuntimeHints hints) {
            // Register for metadata access only (no invocation)
            hints.reflection().registerType(
                MetadataClass.class,
                builder -> builder
                    .withMembers(
                        MemberCategory.INTROSPECT_DECLARED_METHODS,
                        MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS
                    )
            );
        }

        private void registerConditionally(RuntimeHints hints, ClassLoader classLoader) {
            // Register only if class is present
            hints.reflection().registerTypeIfPresent(
                classLoader,
                "com.example.optional.OptionalFeature",
                typeHint -> typeHint.withMembers(
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
                )
            );
        }
    }
}

/**
 * Reflective Entity (Full Reflection Access)
 */
class ReflectiveEntity {
    private Long id;
    private String name;
    private String description;
    private Date createdAt;

    // Default constructor (required for reflection)
    public ReflectiveEntity() {
        this.createdAt = new Date();
    }

    // Parameterized constructor
    public ReflectiveEntity(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = new Date();
    }

    // Getters and setters (accessed via reflection)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("ReflectiveEntity{id=%d, name='%s', description='%s'}", 
            id, name, description);
    }
}

/**
 * Reflective Service (Method Invocation)
 */
@Service
class ReflectiveService {
    
    public String processData(String input) {
        return "Processed via reflection: " + input;
    }

    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("service", "ReflectiveService");
        metadata.put("timestamp", System.currentTimeMillis());
        return metadata;
    }
}

/**
 * User DTO (Jackson Serialization)
 */
class UserDTO {
    private Long id;
    private String username;
    private String email;
    private boolean active;

    public UserDTO() {}

    public UserDTO(Long id, String username, String email, boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.active = active;
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
}

/**
 * Product DTO (Jackson Serialization)
 */
class ProductDTO {
    private Long id;
    private String name;
    private Double price;
    private Integer stock;

    public ProductDTO() {}

    // Getters and setters
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
 * Metadata Class (Introspection Only)
 */
class MetadataClass {
    
    public void someMethod() {
        // Metadata accessible, but invocation may fail
    }

    public String anotherMethod() {
        return "metadata";
    }
}

/**
 * Reflection Configuration Service
 */
@Service
class ReflectionConfigService {

    /**
     * Get all registered reflection configurations
     */
    public Map<String, Object> getReflectionConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        
        // Entity classes
        List<Map<String, Object>> entities = new ArrayList<>();
        entities.add(createClassConfig("ReflectiveEntity", 
            "Full reflection access",
            List.of("INVOKE_DECLARED_CONSTRUCTORS", "INVOKE_DECLARED_METHODS", "DECLARED_FIELDS")));
        config.put("entities", entities);
        
        // Service classes
        List<Map<String, Object>> services = new ArrayList<>();
        services.add(createClassConfig("ReflectiveService",
            "Method invocation only",
            List.of("INVOKE_DECLARED_CONSTRUCTORS", "INVOKE_PUBLIC_METHODS")));
        config.put("services", services);
        
        // DTO classes
        List<Map<String, Object>> dtos = new ArrayList<>();
        dtos.add(createClassConfig("UserDTO",
            "Jackson serialization",
            List.of("INVOKE_DECLARED_CONSTRUCTORS", "DECLARED_FIELDS")));
        dtos.add(createClassConfig("ProductDTO",
            "Jackson serialization",
            List.of("INVOKE_DECLARED_CONSTRUCTORS", "DECLARED_FIELDS")));
        config.put("dtos", dtos);
        
        // Metadata classes
        List<Map<String, Object>> metadata = new ArrayList<>();
        metadata.add(createClassConfig("MetadataClass",
            "Introspection only (no invocation)",
            List.of("INTROSPECT_DECLARED_METHODS", "INTROSPECT_DECLARED_CONSTRUCTORS")));
        config.put("metadata", metadata);
        
        return config;
    }

    private Map<String, Object> createClassConfig(String className, String purpose, List<String> categories) {
        Map<String, Object> classConfig = new LinkedHashMap<>();
        classConfig.put("className", className);
        classConfig.put("purpose", purpose);
        classConfig.put("memberCategories", categories);
        return classConfig;
    }

    /**
     * Get member category descriptions
     */
    public Map<String, Map<String, String>> getMemberCategoryDescriptions() {
        Map<String, Map<String, String>> categories = new LinkedHashMap<>();
        
        Map<String, String> constructors = new LinkedHashMap<>();
        constructors.put("INVOKE_DECLARED_CONSTRUCTORS", "Invoke all constructors (public, protected, private)");
        constructors.put("INVOKE_PUBLIC_CONSTRUCTORS", "Invoke public constructors only");
        constructors.put("INTROSPECT_DECLARED_CONSTRUCTORS", "Metadata access to all constructors (no invocation)");
        constructors.put("INTROSPECT_PUBLIC_CONSTRUCTORS", "Metadata access to public constructors (no invocation)");
        categories.put("Constructors", constructors);
        
        Map<String, String> methods = new LinkedHashMap<>();
        methods.put("INVOKE_DECLARED_METHODS", "Invoke all methods (public, protected, private)");
        methods.put("INVOKE_PUBLIC_METHODS", "Invoke public methods only");
        methods.put("INTROSPECT_DECLARED_METHODS", "Metadata access to all methods (no invocation)");
        methods.put("INTROSPECT_PUBLIC_METHODS", "Metadata access to public methods (no invocation)");
        categories.put("Methods", methods);
        
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("DECLARED_FIELDS", "Access all fields (public, protected, private) for read/write");
        fields.put("PUBLIC_FIELDS", "Access public fields only for read/write");
        categories.put("Fields", fields);
        
        return categories;
    }

    /**
     * Get reflection best practices
     */
    public List<String> getReflectionBestPractices() {
        List<String> practices = new ArrayList<>();
        
        practices.add("✅ Register only required members (minimize overhead)");
        practices.add("✅ Use specific member categories (don't over-register)");
        practices.add("✅ Test reflection in native image thoroughly");
        practices.add("✅ Document why each class needs reflection");
        practices.add("✅ Prefer interfaces over reflection when possible");
        practices.add("✅ Use @Reflective annotation for simple cases");
        practices.add("⚠️ Avoid wildcards (register specific classes)");
        practices.add("⚠️ Don't register entire packages");
        practices.add("⚠️ Minimize DECLARED_FIELDS usage (use getters/setters)");
        practices.add("⚠️ Consider refactoring to eliminate reflection");
        
        return practices;
    }
}

/**
 * Reflection Test Service
 */
@Service
class ReflectionTestService {

    /**
     * Test constructor invocation via reflection
     */
    public Map<String, Object> testConstructorInvocation() {
        try {
            // Test default constructor
            Constructor<ReflectiveEntity> defaultConstructor = 
                ReflectiveEntity.class.getDeclaredConstructor();
            ReflectiveEntity instance1 = defaultConstructor.newInstance();
            
            // Test parameterized constructor
            Constructor<ReflectiveEntity> paramConstructor = 
                ReflectiveEntity.class.getDeclaredConstructor(
                    Long.class, String.class, String.class
                );
            ReflectiveEntity instance2 = paramConstructor.newInstance(
                1L, "Test", "Description"
            );
            
            return Map.of(
                "success", true,
                "defaultConstructor", instance1.toString(),
                "paramConstructor", instance2.toString()
            );
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * Test method invocation via reflection
     */
    public Map<String, Object> testMethodInvocation() {
        try {
            ReflectiveService service = new ReflectiveService();
            Method method = service.getClass().getDeclaredMethod("processData", String.class);
            Object result = method.invoke(service, "test-data");
            
            return Map.of(
                "success", true,
                "method", "processData",
                "result", result
            );
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * Test field access via reflection
     */
    public Map<String, Object> testFieldAccess() {
        try {
            ReflectiveEntity entity = new ReflectiveEntity(1L, "Original", "Description");
            
            // Get field value
            Field nameField = ReflectiveEntity.class.getDeclaredField("name");
            nameField.setAccessible(true);
            String originalName = (String) nameField.get(entity);
            
            // Set field value
            nameField.set(entity, "Modified via reflection");
            String modifiedName = (String) nameField.get(entity);
            
            return Map.of(
                "success", true,
                "originalName", originalName,
                "modifiedName", modifiedName
            );
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * Get class metadata via reflection
     */
    public Map<String, Object> getClassMetadata(String className) {
        try {
            Class<?> clazz = Class.forName("com.example.nativeimage.patterns." + className);
            
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("className", clazz.getName());
            metadata.put("simpleName", clazz.getSimpleName());
            metadata.put("package", clazz.getPackage().getName());
            
            // Constructors
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            List<String> constructorNames = Arrays.stream(constructors)
                .map(c -> c.getName() + Arrays.toString(c.getParameterTypes()))
                .collect(Collectors.toList());
            metadata.put("constructors", constructorNames);
            
            // Methods
            Method[] methods = clazz.getDeclaredMethods();
            List<String> methodNames = Arrays.stream(methods)
                .map(Method::getName)
                .collect(Collectors.toList());
            metadata.put("methods", methodNames);
            
            // Fields
            Field[] fields = clazz.getDeclaredFields();
            List<String> fieldNames = Arrays.stream(fields)
                .map(f -> f.getType().getSimpleName() + " " + f.getName())
                .collect(Collectors.toList());
            metadata.put("fields", fieldNames);
            
            return Map.of("success", true, "metadata", metadata);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
}

/**
 * Reflection Configuration REST Controller
 */
@RestController
@RequestMapping("/api/reflection-config")
class ReflectionConfigController {

    private final ReflectionConfigService reflectionConfigService;
    private final ReflectionTestService reflectionTestService;

    public ReflectionConfigController(ReflectionConfigService reflectionConfigService,
                                       ReflectionTestService reflectionTestService) {
        this.reflectionConfigService = reflectionConfigService;
        this.reflectionTestService = reflectionTestService;
    }

    /**
     * GET /api/reflection-config/registered
     * Get all registered reflection configurations
     */
    @GetMapping("/registered")
    public Map<String, Object> getRegisteredConfig() {
        return reflectionConfigService.getReflectionConfig();
    }

    /**
     * GET /api/reflection-config/member-categories
     * Get member category descriptions
     */
    @GetMapping("/member-categories")
    public Map<String, Map<String, String>> getMemberCategories() {
        return reflectionConfigService.getMemberCategoryDescriptions();
    }

    /**
     * GET /api/reflection-config/best-practices
     * Get reflection best practices
     */
    @GetMapping("/best-practices")
    public List<String> getBestPractices() {
        return reflectionConfigService.getReflectionBestPractices();
    }

    /**
     * GET /api/reflection-config/test/constructor
     * Test constructor invocation
     */
    @GetMapping("/test/constructor")
    public Map<String, Object> testConstructor() {
        return reflectionTestService.testConstructorInvocation();
    }

    /**
     * GET /api/reflection-config/test/method
     * Test method invocation
     */
    @GetMapping("/test/method")
    public Map<String, Object> testMethod() {
        return reflectionTestService.testMethodInvocation();
    }

    /**
     * GET /api/reflection-config/test/field
     * Test field access
     */
    @GetMapping("/test/field")
    public Map<String, Object> testField() {
        return reflectionTestService.testFieldAccess();
    }

    /**
     * GET /api/reflection-config/metadata/{className}
     * Get class metadata
     */
    @GetMapping("/metadata/{className}")
    public Map<String, Object> getMetadata(@PathVariable String className) {
        return reflectionTestService.getClassMetadata(className);
    }

    /**
     * POST /api/reflection-config/test/json
     * Test JSON serialization (Jackson reflection)
     */
    @PostMapping("/test/json")
    public UserDTO testJsonSerialization(@RequestBody UserDTO user) {
        user.setUsername("Modified: " + user.getUsername());
        user.setActive(!user.isActive());
        return user;
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ GET REFLECTION CONFIG:
 * --------------------------
 * curl http://localhost:8080/api/reflection-config/registered
 * 
 * Response:
 * {
 *   "entities": [{"className": "ReflectiveEntity", "purpose": "Full reflection access", ...}],
 *   "services": [...],
 *   "dtos": [...]
 * }
 * 
 * 2️⃣ TEST CONSTRUCTOR INVOCATION:
 * --------------------------------
 * curl http://localhost:8080/api/reflection-config/test/constructor
 * 
 * Response:
 * {
 *   "success": true,
 *   "defaultConstructor": "ReflectiveEntity{...}",
 *   "paramConstructor": "ReflectiveEntity{id=1, name='Test', ...}"
 * }
 * 
 * 3️⃣ TEST METHOD INVOCATION:
 * ---------------------------
 * curl http://localhost:8080/api/reflection-config/test/method
 * 
 * Response:
 * {
 *   "success": true,
 *   "method": "processData",
 *   "result": "Processed via reflection: test-data"
 * }
 * 
 * 4️⃣ TEST FIELD ACCESS:
 * ----------------------
 * curl http://localhost:8080/api/reflection-config/test/field
 * 
 * Response:
 * {
 *   "success": true,
 *   "originalName": "Original",
 *   "modifiedName": "Modified via reflection"
 * }
 * 
 * 5️⃣ GET CLASS METADATA:
 * -----------------------
 * curl http://localhost:8080/api/reflection-config/metadata/ReflectiveEntity
 * 
 * Response:
 * {
 *   "success": true,
 *   "metadata": {
 *     "className": "com.example.nativeimage.patterns.ReflectiveEntity",
 *     "constructors": [...],
 *     "methods": [...],
 *     "fields": [...]
 *   }
 * }
 * 
 * 6️⃣ TEST JSON SERIALIZATION:
 * ----------------------------
 * curl -X POST http://localhost:8080/api/reflection-config/test/json \
 *   -H "Content-Type: application/json" \
 *   -d '{"id":1,"username":"john","email":"john@example.com","active":true}'
 * 
 * Response:
 * {
 *   "id": 1,
 *   "username": "Modified: john",
 *   "email": "john@example.com",
 *   "active": false
 * }
 */
