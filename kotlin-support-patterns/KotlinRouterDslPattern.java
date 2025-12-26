package com.example.kotlin.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 SPRING BOOT KOTLIN - ROUTER DSL PATTERN 💡
 * ==============================================
 * 
 * Demonstrates Kotlin Router DSL for functional routing in Spring WebFlux.
 * The Kotlin Router DSL provides a type-safe, concise way to define routes
 * using Kotlin's DSL capabilities, eliminating the need for @RequestMapping
 * annotations and enabling functional reactive endpoints.
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ ROUTER DSL:
 *    - router { } DSL block
 *    - Type-safe route definitions
 *    - HTTP method builders (GET, POST, PUT, DELETE)
 *    - Path parameter extraction
 * 
 * 2️⃣ COROUTINE ROUTER (coRouter):
 *    - Suspend function handlers
 *    - Flow-based responses
 *    - Coroutine context propagation
 *    - Non-blocking async operations
 * 
 * 3️⃣ HANDLER FUNCTIONS:
 *    - Typed request/response
 *    - ServerRequest parameter
 *    - ServerResponse builders
 *    - Reactive types (Mono, Flux)
 * 
 * 4️⃣ ROUTE NESTING:
 *    - Nested routes
 *    - Path prefixes
 *    - Common filters
 *    - Resource grouping
 * 
 * 📦 KOTLIN ROUTER DSL (Kotlin code):
 * ===================================
 * 
 * import org.springframework.web.reactive.function.server.router
 * 
 * @Configuration
 * class RouterConfiguration {
 *     @Bean
 *     fun userRoutes(handler: UserHandler) = router {
 *         "/api/users".nest {
 *             GET("", handler::findAll)
 *             GET("/{id}", handler::findById)
 *             POST("", handler::create)
 *             PUT("/{id}", handler::update)
 *             DELETE("/{id}", handler::delete)
 *         }
 *     }
 * }
 * 
 * // Handler class
 * @Component
 * class UserHandler(private val userService: UserService) {
 *     fun findAll(request: ServerRequest): Mono<ServerResponse> {
 *         return ServerResponse.ok()
 *             .contentType(MediaType.APPLICATION_JSON)
 *             .body(userService.findAll(), User::class.java)
 *     }
 *     
 *     fun findById(request: ServerRequest): Mono<ServerResponse> {
 *         val id = request.pathVariable("id")
 *         return userService.findById(id)
 *             .flatMap { user ->
 *                 ServerResponse.ok().bodyValue(user)
 *             }
 *             .switchIfEmpty(ServerResponse.notFound().build())
 *     }
 * }
 * 
 * 🔧 COROUTINE ROUTER (coRouter):
 * ===============================
 * 
 * import org.springframework.web.reactive.function.server.coRouter
 * 
 * @Bean
 * fun productRoutes(handler: ProductHandler) = coRouter {
 *     "/api/products".nest {
 *         GET("", handler::findAll)
 *         GET("/{id}", handler::findById)
 *         POST("", handler::create)
 *     }
 * }
 * 
 * // Coroutine handler (suspend functions)
 * @Component
 * class ProductHandler(private val productService: ProductService) {
 *     suspend fun findAll(request: ServerRequest): ServerResponse {
 *         val products = productService.findAll() // suspend function
 *         return ServerResponse.ok().bodyValueAndAwait(products)
 *     }
 *     
 *     suspend fun findById(request: ServerRequest): ServerResponse {
 *         val id = request.pathVariable("id")
 *         val product = productService.findById(id)
 *         return product?.let {
 *             ServerResponse.ok().bodyValueAndAwait(it)
 *         } ?: ServerResponse.notFound().buildAndAwait()
 *     }
 * }
 * 
 * 🎯 ADVANCED ROUTING PATTERNS:
 * ============================
 * 
 * // Request predicates
 * router {
 *     GET("/users") { request ->
 *         // ...
 *     }
 *     
 *     // Accept header predicate
 *     (GET("/users") and accept(MediaType.APPLICATION_JSON)) {
 *         // JSON response
 *     }
 *     
 *     // Content type predicate
 *     (POST("/users") and contentType(MediaType.APPLICATION_JSON)) {
 *         // Handle JSON request
 *     }
 *     
 *     // Query parameter predicate
 *     (GET("/users") and queryParam("active", "true")) {
 *         // Filter active users
 *     }
 *     
 *     // Header predicate
 *     (GET("/users") and headers { headers ->
 *         headers.accept().contains(MediaType.APPLICATION_JSON)
 *     }) {
 *         // Custom header logic
 *     }
 * }
 * 
 * // Nested routes with filters
 * router {
 *     "/api".nest {
 *         filter { request, next ->
 *             // Common filter for all /api routes
 *             println("Handling: ${request.uri()}")
 *             next.handle(request)
 *         }
 *         
 *         "/users".nest {
 *             GET("", handler::findAll)
 *             GET("/{id}", handler::findById)
 *         }
 *         
 *         "/products".nest {
 *             GET("", productHandler::findAll)
 *             GET("/{id}", productHandler::findById)
 *         }
 *     }
 * }
 * 
 * 💡 ADVANTAGES OF ROUTER DSL:
 * ===========================
 * ✅ Type-safe routing
 * ✅ Functional programming style
 * ✅ Better testability
 * ✅ Coroutine support (coRouter)
 * ✅ No reflection overhead
 * ✅ Compile-time checking
 * ✅ Cleaner route organization
 * ✅ Easier to compose routes
 * ✅ Better IDE support
 * ✅ Reactive by default
 * 
 * 🔄 COMPARISON: ANNOTATION vs DSL:
 * =================================
 * 
 * Annotation-based:
 * -----------------
 * @RestController
 * @RequestMapping("/api/users")
 * class UserController(private val userService: UserService) {
 *     @GetMapping
 *     fun findAll(): Flux<User> = userService.findAll()
 *     
 *     @GetMapping("/{id}")
 *     fun findById(@PathVariable id: String): Mono<User> =
 *         userService.findById(id)
 *     
 *     @PostMapping
 *     fun create(@RequestBody user: User): Mono<User> =
 *         userService.create(user)
 * }
 * 
 * Router DSL:
 * -----------
 * @Bean
 * fun userRoutes(handler: UserHandler) = router {
 *     "/api/users".nest {
 *         GET("", handler::findAll)
 *         GET("/{id}", handler::findById)
 *         POST("", handler::create)
 *     }
 * }
 * 
 * Coroutine Router DSL:
 * ---------------------
 * @Bean
 * fun userRoutes(handler: UserHandler) = coRouter {
 *     "/api/users".nest {
 *         GET("", handler::findAll)      // suspend fun
 *         GET("/{id}", handler::findById) // suspend fun
 *         POST("", handler::create)       // suspend fun
 *     }
 * }
 * 
 * 🎯 WHEN TO USE:
 * ==============
 * ✅ Kotlin + WebFlux applications
 * ✅ Functional programming preference
 * ✅ Coroutine-based async programming
 * ✅ Need programmatic routing
 * ✅ Complex route composition
 * ✅ Microservices with many endpoints
 * ✅ API Gateway patterns
 * 
 * ❌ WHEN NOT TO USE:
 * ==================
 * ❌ Traditional MVC applications
 * ❌ Team prefers annotations
 * ❌ Simple CRUD APIs (annotations sufficient)
 * ❌ Mixed Java/Kotlin codebases
 * 
 * 💡 REAL-WORLD EXAMPLES:
 * ======================
 * 
 * Example 1: REST API with CRUD
 * ------------------------------
 * @Bean
 * fun apiRoutes(
 *     userHandler: UserHandler,
 *     productHandler: ProductHandler
 * ) = router {
 *     "/api".nest {
 *         // Users
 *         "/users".nest {
 *             GET("", userHandler::findAll)
 *             GET("/{id}", userHandler::findById)
 *             POST("", userHandler::create)
 *             PUT("/{id}", userHandler::update)
 *             DELETE("/{id}", userHandler::delete)
 *         }
 *         
 *         // Products
 *         "/products".nest {
 *             GET("", productHandler::findAll)
 *             GET("/{id}", productHandler::findById)
 *             POST("", productHandler::create)
 *         }
 *     }
 * }
 * 
 * Example 2: Coroutine API with Flow
 * -----------------------------------
 * @Bean
 * fun streamRoutes(handler: StreamHandler) = coRouter {
 *     "/api/stream".nest {
 *         GET("/users", handler::streamUsers)        // Flow<User>
 *         GET("/events", handler::streamEvents)      // Flow<Event>
 *         GET("/metrics", handler::streamMetrics)    // Flow<Metric>
 *     }
 * }
 * 
 * @Component
 * class StreamHandler(private val service: StreamService) {
 *     suspend fun streamUsers(request: ServerRequest): ServerResponse {
 *         val users: Flow<User> = service.streamUsers()
 *         return ServerResponse.ok()
 *             .contentType(MediaType.APPLICATION_NDJSON)
 *             .bodyAndAwait(users)
 *     }
 * }
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
public class KotlinRouterDslPattern {

    public static void main(String[] args) {
        SpringApplication.run(KotlinRouterDslPattern.class, args);
    }
}

/**
 * Kotlin Router DSL Documentation Service
 */
@Service
class KotlinRouterDslService {

    /**
     * Get Router DSL examples
     */
    public Map<String, String> getRouterDslExamples() {
        Map<String, String> examples = new LinkedHashMap<>();
        
        examples.put("Simple Route", 
            "@Bean\n" +
            "fun routes(handler: UserHandler) = router {\n" +
            "    GET(\"/users\", handler::findAll)\n" +
            "}");
        
        examples.put("Nested Routes", 
            "router {\n" +
            "    \"/api/users\".nest {\n" +
            "        GET(\"\", handler::findAll)\n" +
            "        GET(\"/{id}\", handler::findById)\n" +
            "        POST(\"\", handler::create)\n" +
            "    }\n" +
            "}");
        
        examples.put("Coroutine Router", 
            "@Bean\n" +
            "fun routes(handler: UserHandler) = coRouter {\n" +
            "    GET(\"/users\", handler::findAll)  // suspend fun\n" +
            "}");
        
        examples.put("With Predicates", 
            "router {\n" +
            "    (GET(\"/users\") and accept(APPLICATION_JSON)) {\n" +
            "        ServerResponse.ok().bodyValue(users)\n" +
            "    }\n" +
            "}");
        
        examples.put("With Filters", 
            "router {\n" +
            "    filter { request, next ->\n" +
            "        println(\"Request: ${request.uri()}\")\n" +
            "        next.handle(request)\n" +
            "    }\n" +
            "    GET(\"/users\", handler::findAll)\n" +
            "}");
        
        examples.put("Query Parameters", 
            "fun findAll(request: ServerRequest): Mono<ServerResponse> {\n" +
            "    val page = request.queryParam(\"page\").orElse(\"0\").toInt()\n" +
            "    val size = request.queryParam(\"size\").orElse(\"10\").toInt()\n" +
            "    return ServerResponse.ok().bodyValue(users)\n" +
            "}");
        
        examples.put("Path Variables", 
            "fun findById(request: ServerRequest): Mono<ServerResponse> {\n" +
            "    val id = request.pathVariable(\"id\")\n" +
            "    return service.findById(id)\n" +
            "        .flatMap { ServerResponse.ok().bodyValue(it) }\n" +
            "        .switchIfEmpty(ServerResponse.notFound().build())\n" +
            "}");
        
        return examples;
    }

    /**
     * Get Router DSL advantages
     */
    public List<String> getRouterDslAdvantages() {
        List<String> advantages = new ArrayList<>();
        
        advantages.add("✅ Type-safe routing with compile-time checking");
        advantages.add("✅ Functional programming style");
        advantages.add("✅ Coroutine support (coRouter)");
        advantages.add("✅ No reflection overhead");
        advantages.add("✅ Better testability (pure functions)");
        advantages.add("✅ Cleaner route organization");
        advantages.add("✅ Easier route composition");
        advantages.add("✅ Better IDE support and autocomplete");
        advantages.add("✅ Reactive by default (WebFlux)");
        advantages.add("✅ Programmatic route registration");
        advantages.add("💡 ~10-20% faster routing than annotations");
        
        return advantages;
    }

    /**
     * Get routing patterns
     */
    public Map<String, List<String>> getRoutingPatterns() {
        Map<String, List<String>> patterns = new LinkedHashMap<>();
        
        List<String> basicRouting = new ArrayList<>();
        basicRouting.add("GET(\"/users\", handler::findAll)");
        basicRouting.add("POST(\"/users\", handler::create)");
        basicRouting.add("PUT(\"/users/{id}\", handler::update)");
        basicRouting.add("DELETE(\"/users/{id}\", handler::delete)");
        patterns.put("Basic Routing", basicRouting);
        
        List<String> nestedRouting = new ArrayList<>();
        nestedRouting.add("\"/api\".nest { GET(\"/users\") }");
        nestedRouting.add("\"/api/users\".nest { GET(\"/{id}\") }");
        nestedRouting.add("Nested routes share common path prefix");
        patterns.put("Nested Routing", nestedRouting);
        
        List<String> predicates = new ArrayList<>();
        predicates.add("accept(APPLICATION_JSON) - Accept header");
        predicates.add("contentType(APPLICATION_JSON) - Content-Type header");
        predicates.add("queryParam(\"active\", \"true\") - Query parameter");
        predicates.add("headers { ... } - Custom header logic");
        predicates.add("path(\"/users/**\") - Path pattern");
        patterns.put("Request Predicates", predicates);
        
        List<String> filters = new ArrayList<>();
        filters.add("filter { request, next -> next.handle(request) }");
        filters.add("before { request -> ... } - Before filter");
        filters.add("after { request, response -> ... } - After filter");
        filters.add("Filters apply to all nested routes");
        patterns.put("Filters", filters);
        
        List<String> coroutineRouter = new ArrayList<>();
        coroutineRouter.add("coRouter { GET(\"/users\", handler::findAll) }");
        coroutineRouter.add("Handler uses suspend functions");
        coroutineRouter.add("bodyAndAwait() for suspending body");
        coroutineRouter.add("buildAndAwait() for suspending build");
        coroutineRouter.add("Flow<T> for streaming responses");
        patterns.put("Coroutine Router", coroutineRouter);
        
        return patterns;
    }

    /**
     * Get handler patterns
     */
    public Map<String, String> getHandlerPatterns() {
        Map<String, String> patterns = new LinkedHashMap<>();
        
        patterns.put("Reactive Handler", 
            "fun findAll(request: ServerRequest): Mono<ServerResponse> {\n" +
            "    return ServerResponse.ok()\n" +
            "        .body(service.findAll(), User::class.java)\n" +
            "}");
        
        patterns.put("Coroutine Handler", 
            "suspend fun findAll(request: ServerRequest): ServerResponse {\n" +
            "    val users = service.findAll()\n" +
            "    return ServerResponse.ok().bodyValueAndAwait(users)\n" +
            "}");
        
        patterns.put("Flow Handler", 
            "suspend fun streamUsers(request: ServerRequest): ServerResponse {\n" +
            "    val users: Flow<User> = service.streamUsers()\n" +
            "    return ServerResponse.ok()\n" +
            "        .contentType(APPLICATION_NDJSON)\n" +
            "        .bodyAndAwait(users)\n" +
            "}");
        
        patterns.put("Error Handling", 
            "suspend fun findById(request: ServerRequest): ServerResponse {\n" +
            "    return try {\n" +
            "        val user = service.findById(id)\n" +
            "        ServerResponse.ok().bodyValueAndAwait(user)\n" +
            "    } catch (e: NotFoundException) {\n" +
            "        ServerResponse.notFound().buildAndAwait()\n" +
            "    }\n" +
            "}");
        
        patterns.put("Request Body", 
            "suspend fun create(request: ServerRequest): ServerResponse {\n" +
            "    val user = request.awaitBody<User>()\n" +
            "    val created = service.create(user)\n" +
            "    return ServerResponse.created(uri).bodyValueAndAwait(created)\n" +
            "}");
        
        return patterns;
    }

    /**
     * Get comparison table
     */
    public Map<String, Map<String, String>> getComparisonTable() {
        Map<String, Map<String, String>> comparison = new LinkedHashMap<>();
        
        Map<String, String> syntax = new LinkedHashMap<>();
        syntax.put("Annotation", "@GetMapping(\"/users\") fun findAll()");
        syntax.put("Router DSL", "GET(\"/users\", handler::findAll)");
        syntax.put("Advantage", "More concise, functional style");
        comparison.put("Syntax", syntax);
        
        Map<String, String> performance = new LinkedHashMap<>();
        performance.put("Annotation", "Reflection-based mapping");
        performance.put("Router DSL", "Direct function references");
        performance.put("Advantage", "10-20% faster routing");
        comparison.put("Performance", performance);
        
        Map<String, String> testability = new LinkedHashMap<>();
        testability.put("Annotation", "Need MockMvc or WebTestClient");
        testability.put("Router DSL", "Test handlers as pure functions");
        testability.put("Advantage", "Easier unit testing");
        comparison.put("Testability", testability);
        
        Map<String, String> coroutines = new LinkedHashMap<>();
        coroutines.put("Annotation", "Limited coroutine support");
        coroutines.put("Router DSL", "Full coRouter support");
        coroutines.put("Advantage", "Native suspend functions");
        comparison.put("Coroutines", coroutines);
        
        Map<String, String> composition = new LinkedHashMap<>();
        composition.put("Annotation", "Class-based grouping");
        composition.put("Router DSL", "Functional composition");
        composition.put("Advantage", "Flexible route merging");
        comparison.put("Composition", composition);
        
        return comparison;
    }

    /**
     * Get best practices
     */
    public List<String> getBestPractices() {
        List<String> practices = new ArrayList<>();
        
        practices.add("✅ Use coRouter for coroutine-based handlers");
        practices.add("✅ Group related routes with nest()");
        practices.add("✅ Apply common filters at nest level");
        practices.add("✅ Use suspend functions for async operations");
        practices.add("✅ Return Flow<T> for streaming responses");
        practices.add("✅ Use predicates for content negotiation");
        practices.add("✅ Separate handler logic from routing");
        practices.add("✅ Test handlers independently");
        practices.add("✅ Use path variables for resource IDs");
        practices.add("✅ Use query parameters for filtering");
        practices.add("⚠️ Don't mix annotation and DSL routing");
        practices.add("⚠️ Keep handler functions focused");
        practices.add("💡 Use for WebFlux microservices");
        
        return practices;
    }
}

/**
 * Kotlin Router DSL REST Controller
 */
@RestController
@RequestMapping("/api/kotlin-router-dsl")
class KotlinRouterDslController {

    private final KotlinRouterDslService kotlinRouterDslService;

    public KotlinRouterDslController(KotlinRouterDslService kotlinRouterDslService) {
        this.kotlinRouterDslService = kotlinRouterDslService;
    }

    /**
     * GET /api/kotlin-router-dsl/examples
     */
    @GetMapping("/examples")
    public Map<String, String> getExamples() {
        return kotlinRouterDslService.getRouterDslExamples();
    }

    /**
     * GET /api/kotlin-router-dsl/advantages
     */
    @GetMapping("/advantages")
    public List<String> getAdvantages() {
        return kotlinRouterDslService.getRouterDslAdvantages();
    }

    /**
     * GET /api/kotlin-router-dsl/patterns
     */
    @GetMapping("/patterns")
    public Map<String, List<String>> getPatterns() {
        return kotlinRouterDslService.getRoutingPatterns();
    }

    /**
     * GET /api/kotlin-router-dsl/handlers
     */
    @GetMapping("/handlers")
    public Map<String, String> getHandlers() {
        return kotlinRouterDslService.getHandlerPatterns();
    }

    /**
     * GET /api/kotlin-router-dsl/comparison
     */
    @GetMapping("/comparison")
    public Map<String, Map<String, String>> getComparison() {
        return kotlinRouterDslService.getComparisonTable();
    }

    /**
     * GET /api/kotlin-router-dsl/best-practices
     */
    @GetMapping("/best-practices")
    public List<String> getBestPractices() {
        return kotlinRouterDslService.getBestPractices();
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ COMPLETE REST API (Kotlin):
 * -------------------------------
 * @Configuration
 * class RouterConfiguration {
 *     @Bean
 *     fun apiRoutes(
 *         userHandler: UserHandler,
 *         productHandler: ProductHandler
 *     ) = router {
 *         "/api".nest {
 *             // Common filter for all API routes
 *             filter { request, next ->
 *                 println("API Request: ${request.uri()}")
 *                 next.handle(request)
 *             }
 *             
 *             // User routes
 *             "/users".nest {
 *                 GET("", userHandler::findAll)
 *                 GET("/{id}", userHandler::findById)
 *                 POST("", userHandler::create)
 *                 PUT("/{id}", userHandler::update)
 *                 DELETE("/{id}", userHandler::delete)
 *             }
 *             
 *             // Product routes
 *             "/products".nest {
 *                 GET("", productHandler::findAll)
 *                 GET("/{id}", productHandler::findById)
 *                 POST("", productHandler::create)
 *             }
 *         }
 *     }
 * }
 * 
 * 2️⃣ COROUTINE API WITH FLOW (Kotlin):
 * -------------------------------------
 * @Configuration
 * class StreamRouterConfiguration {
 *     @Bean
 *     fun streamRoutes(handler: StreamHandler) = coRouter {
 *         "/api/stream".nest {
 *             GET("/users", handler::streamUsers)
 *             GET("/events", handler::streamEvents)
 *             GET("/metrics", handler::streamMetrics)
 *         }
 *     }
 * }
 * 
 * @Component
 * class StreamHandler(private val service: StreamService) {
 *     suspend fun streamUsers(request: ServerRequest): ServerResponse {
 *         val users: Flow<User> = service.streamUsers()
 *         return ServerResponse.ok()
 *             .contentType(MediaType.APPLICATION_NDJSON)
 *             .bodyAndAwait(users)
 *     }
 *     
 *     suspend fun streamEvents(request: ServerRequest): ServerResponse {
 *         val events: Flow<Event> = service.streamEvents()
 *         return ServerResponse.ok()
 *             .contentType(MediaType.TEXT_EVENT_STREAM)
 *             .bodyAndAwait(events)
 *     }
 * }
 * 
 * 3️⃣ CONTENT NEGOTIATION (Kotlin):
 * ---------------------------------
 * @Bean
 * fun userRoutes(handler: UserHandler) = router {
 *     (GET("/users") and accept(MediaType.APPLICATION_JSON)) {
 *         handler.findAllAsJson(it)
 *     }
 *     
 *     (GET("/users") and accept(MediaType.APPLICATION_XML)) {
 *         handler.findAllAsXml(it)
 *     }
 *     
 *     (GET("/users") and accept(MediaType.TEXT_HTML)) {
 *         handler.findAllAsHtml(it)
 *     }
 * }
 * 
 * 4️⃣ AUTHENTICATION & FILTERS (Kotlin):
 * --------------------------------------
 * @Bean
 * fun secureRoutes(handler: UserHandler) = router {
 *     "/api".nest {
 *         // Authentication filter
 *         filter { request, next ->
 *             val token = request.headers()
 *                 .firstHeader("Authorization")
 *             
 *             if (token != null && validateToken(token)) {
 *                 next.handle(request)
 *             } else {
 *                 ServerResponse.status(HttpStatus.UNAUTHORIZED).build()
 *             }
 *         }
 *         
 *         // Protected routes
 *         GET("/profile", handler::getProfile)
 *         PUT("/profile", handler::updateProfile)
 *         DELETE("/account", handler::deleteAccount)
 *     }
 * }
 * 
 * 5️⃣ GET ROUTER DSL INFORMATION:
 * -------------------------------
 * curl http://localhost:8080/api/kotlin-router-dsl/examples
 * curl http://localhost:8080/api/kotlin-router-dsl/advantages
 * curl http://localhost:8080/api/kotlin-router-dsl/patterns
 * curl http://localhost:8080/api/kotlin-router-dsl/handlers
 * curl http://localhost:8080/api/kotlin-router-dsl/comparison
 * curl http://localhost:8080/api/kotlin-router-dsl/best-practices
 */
