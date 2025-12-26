package com.example.kotlin.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 SPRING BOOT KOTLIN - COROUTINE SUPPORT PATTERN 💡
 * =====================================================
 * 
 * Demonstrates Kotlin Coroutine integration with Spring Framework.
 * Coroutines provide lightweight, non-blocking concurrency that's more
 * efficient and easier to use than traditional reactive programming.
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ SUSPEND FUNCTIONS:
 *    - Non-blocking operations
 *    - Sequential code style
 *    - Exception handling
 *    - Cancellation support
 * 
 * 2️⃣ FLOW:
 *    - Reactive streams alternative
 *    - Cold streams
 *    - Backpressure support
 *    - Operator functions
 * 
 * 3️⃣ COROUTINE SCOPE:
 *    - Structured concurrency
 *    - Context propagation
 *    - Lifecycle management
 *    - Exception boundaries
 * 
 * 4️⃣ SPRING INTEGRATION:
 *    - @Controller suspend endpoints
 *    - Reactive repository suspend methods
 *    - Transaction support
 *    - WebFlux coRouter
 * 
 * 📦 SUSPEND FUNCTIONS (Kotlin):
 * ==============================
 * 
 * @RestController
 * @RequestMapping("/api/users")
 * class UserController(private val userService: UserService) {
 *     
 *     @GetMapping("/{id}")
 *     suspend fun findById(@PathVariable id: String): User {
 *         return userService.findById(id)  // suspend function
 *     }
 *     
 *     @PostMapping
 *     suspend fun create(@RequestBody user: User): User {
 *         return userService.create(user)  // suspend function
 *     }
 * }
 * 
 * @Service
 * class UserService(private val repository: UserRepository) {
 *     
 *     suspend fun findById(id: String): User {
 *         return repository.findById(id)
 *             ?: throw NotFoundException("User not found")
 *     }
 *     
 *     suspend fun create(user: User): User {
 *         // Non-blocking operations in sequential style
 *         validateUser(user)
 *         val saved = repository.save(user)
 *         sendWelcomeEmail(saved.email)
 *         return saved
 *     }
 * }
 * 
 * 🔧 FLOW (Reactive Streams):
 * ===========================
 * 
 * @RestController
 * class StreamController(private val service: StreamService) {
 *     
 *     @GetMapping("/stream/users", produces = [APPLICATION_NDJSON_VALUE])
 *     fun streamUsers(): Flow<User> {
 *         return service.streamUsers()  // Flow<User>
 *     }
 * }
 * 
 * @Service
 * class StreamService(private val repository: UserRepository) {
 *     
 *     fun streamUsers(): Flow<User> = flow {
 *         repository.findAll().collect { user ->
 *             emit(user)
 *             delay(100)  // Backpressure simulation
 *         }
 *     }
 *     
 *     fun processStream(): Flow<Result> = flow {
 *         repository.findAll()
 *             .map { user -> processUser(user) }
 *             .filter { result -> result.isValid }
 *             .collect { result -> emit(result) }
 *     }
 * }
 * 
 * 🎯 COROUTINE SCOPE & CONTEXT:
 * =============================
 * 
 * @Service
 * class AsyncService(
 *     private val externalService: ExternalService
 * ) : CoroutineScope {
 *     
 *     override val coroutineContext: CoroutineContext =
 *         SupervisorJob() + Dispatchers.IO
 *     
 *     suspend fun fetchData(ids: List<String>): List<Data> {
 *         // Parallel execution with structured concurrency
 *         return coroutineScope {
 *             ids.map { id ->
 *                 async {
 *                     externalService.fetch(id)
 *                 }
 *             }.awaitAll()
 *         }
 *     }
 *     
 *     suspend fun processWithTimeout(id: String): Result {
 *         return withTimeout(5000) {
 *             externalService.process(id)
 *         }
 *     }
 * }
 * 
 * 💡 REACTIVE REPOSITORY WITH COROUTINES:
 * =======================================
 * 
 * interface UserRepository : CoroutineCrudRepository<User, String> {
 *     suspend fun findByEmail(email: String): User?
 *     
 *     fun findAllByActive(active: Boolean): Flow<User>
 *     
 *     suspend fun countByRole(role: String): Long
 * }
 * 
 * 🔄 ADVANTAGES OF COROUTINES:
 * ============================
 * ✅ Sequential code style (easier to read)
 * ✅ Lightweight (1M+ coroutines possible)
 * ✅ Structured concurrency
 * ✅ Exception handling with try-catch
 * ✅ Cancellation support
 * ✅ Context propagation
 * ✅ Better performance than threads
 * ✅ No callback hell
 * ✅ Type-safe
 * ✅ Better IDE support than reactive
 * 
 * 🔄 COMPARISON: REACTIVE vs COROUTINES:
 * ======================================
 * 
 * Reactive (Mono/Flux):
 * ---------------------
 * fun findById(id: String): Mono<User> {
 *     return repository.findById(id)
 *         .switchIfEmpty(Mono.error(NotFoundException()))
 *         .flatMap { user ->
 *             externalService.enrich(user)
 *         }
 *         .doOnNext { user ->
 *             logger.info("Found: $user")
 *         }
 * }
 * 
 * Coroutines:
 * -----------
 * suspend fun findById(id: String): User {
 *     val user = repository.findById(id)
 *         ?: throw NotFoundException()
 *     
 *     val enriched = externalService.enrich(user)
 *     logger.info("Found: $enriched")
 *     return enriched
 * }
 * 
 * 🎯 WHEN TO USE:
 * ==============
 * ✅ Kotlin applications
 * ✅ WebFlux applications
 * ✅ Reactive data access
 * ✅ Need sequential code style
 * ✅ Team prefers imperative code
 * ✅ High concurrency requirements
 * ✅ Streaming responses
 * 
 * ❌ WHEN NOT TO USE:
 * ==================
 * ❌ Java-only projects
 * ❌ Traditional blocking I/O
 * ❌ Team unfamiliar with coroutines
 * ❌ Simple CRUD operations
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
public class CoroutineSupportPattern {

    public static void main(String[] args) {
        SpringApplication.run(CoroutineSupportPattern.class, args);
    }
}

/**
 * Coroutine Support Documentation Service
 */
@Service
class CoroutineSupportService {

    public Map<String, String> getCoroutineExamples() {
        Map<String, String> examples = new LinkedHashMap<>();
        
        examples.put("Suspend Function", 
            "suspend fun findById(id: String): User {\n" +
            "    return repository.findById(id)\n" +
            "}");
        
        examples.put("Flow Stream", 
            "fun streamUsers(): Flow<User> = flow {\n" +
            "    repository.findAll().collect { emit(it) }\n" +
            "}");
        
        examples.put("Parallel Execution", 
            "suspend fun fetchAll(ids: List<String>): List<Data> {\n" +
            "    return coroutineScope {\n" +
            "        ids.map { async { fetch(it) } }.awaitAll()\n" +
            "    }\n" +
            "}");
        
        examples.put("Timeout", 
            "suspend fun fetchWithTimeout(id: String): Data {\n" +
            "    return withTimeout(5000) { fetch(id) }\n" +
            "}");
        
        examples.put("Error Handling", 
            "suspend fun fetchSafe(id: String): Data? {\n" +
            "    return try {\n" +
            "        fetch(id)\n" +
            "    } catch (e: Exception) {\n" +
            "        null\n" +
            "    }\n" +
            "}");
        
        examples.put("Flow Operators", 
            "fun processStream(): Flow<Result> {\n" +
            "    return flow { ... }\n" +
            "        .map { transform(it) }\n" +
            "        .filter { it.isValid }\n" +
            "        .take(10)\n" +
            "}");
        
        return examples;
    }

    public List<String> getCoroutineAdvantages() {
        List<String> advantages = new ArrayList<>();
        
        advantages.add("✅ Sequential code style (easier to read/debug)");
        advantages.add("✅ Lightweight concurrency (1M+ coroutines)");
        advantages.add("✅ Structured concurrency (automatic cleanup)");
        advantages.add("✅ Exception handling with try-catch");
        advantages.add("✅ Cancellation support out-of-the-box");
        advantages.add("✅ Context propagation (MDC, transactions)");
        advantages.add("✅ Better performance than threads");
        advantages.add("✅ No callback hell or chaining");
        advantages.add("✅ Type-safe operations");
        advantages.add("✅ Better IDE support than reactive");
        advantages.add("💡 ~2x more readable than reactive code");
        
        return advantages;
    }

    public Map<String, Map<String, String>> getComparisonTable() {
        Map<String, Map<String, String>> comparison = new LinkedHashMap<>();
        
        Map<String, String> readability = new LinkedHashMap<>();
        readability.put("Reactive", "Chained operators, callback-based");
        readability.put("Coroutines", "Sequential, imperative style");
        readability.put("Winner", "Coroutines (2x more readable)");
        comparison.put("Readability", readability);
        
        Map<String, String> errorHandling = new LinkedHashMap<>();
        errorHandling.put("Reactive", "onError*, switchIfEmpty, retry");
        errorHandling.put("Coroutines", "try-catch, Elvis operator");
        errorHandling.put("Winner", "Coroutines (familiar patterns)");
        comparison.put("Error Handling", errorHandling);
        
        Map<String, String> performance = new LinkedHashMap<>();
        performance.put("Reactive", "Good (non-blocking)");
        performance.put("Coroutines", "Excellent (lighter overhead)");
        performance.put("Winner", "Coroutines (10-20% faster)");
        comparison.put("Performance", performance);
        
        Map<String, String> testing = new LinkedHashMap<>();
        testing.put("Reactive", "StepVerifier, custom test utils");
        testing.put("Coroutines", "runTest, standard test frameworks");
        testing.put("Winner", "Coroutines (easier testing)");
        comparison.put("Testing", testing);
        
        Map<String, String> learning = new LinkedHashMap<>();
        learning.put("Reactive", "Steep learning curve");
        learning.put("Coroutines", "Moderate learning curve");
        learning.put("Winner", "Coroutines (familiar concepts)");
        comparison.put("Learning Curve", learning);
        
        return comparison;
    }

    public List<String> getFlowOperators() {
        List<String> operators = new ArrayList<>();
        
        operators.add("map { } - Transform elements");
        operators.add("filter { } - Filter elements");
        operators.add("take(n) - Take first n elements");
        operators.add("drop(n) - Skip first n elements");
        operators.add("collect { } - Terminal operator");
        operators.add("toList() - Convert to List");
        operators.add("first() - Get first element");
        operators.add("fold() - Reduce to single value");
        operators.add("zip() - Combine two flows");
        operators.add("combine() - Combine latest values");
        operators.add("flatMapConcat() - Flatten flows");
        operators.add("buffer() - Buffer elements");
        operators.add("conflate() - Keep latest only");
        operators.add("debounce() - Delay emissions");
        
        return operators;
    }

    public List<String> getBestPractices() {
        List<String> practices = new ArrayList<>();
        
        practices.add("✅ Use suspend functions for single values");
        practices.add("✅ Use Flow<T> for multiple values");
        practices.add("✅ Use coroutineScope for parallel operations");
        practices.add("✅ Use withTimeout for timeout handling");
        practices.add("✅ Use try-catch for error handling");
        practices.add("✅ Propagate coroutine context properly");
        practices.add("✅ Use SupervisorJob for independent coroutines");
        practices.add("✅ Cancel coroutines when no longer needed");
        practices.add("✅ Test with runTest and virtual time");
        practices.add("⚠️ Don't block in suspend functions");
        practices.add("⚠️ Don't use GlobalScope (use structured concurrency)");
        practices.add("💡 Prefer coroutines over reactive for new code");
        
        return practices;
    }
}

/**
 * Coroutine Support REST Controller
 */
@RestController
@RequestMapping("/api/coroutine-support")
class CoroutineSupportController {

    private final CoroutineSupportService coroutineSupportService;

    public CoroutineSupportController(CoroutineSupportService coroutineSupportService) {
        this.coroutineSupportService = coroutineSupportService;
    }

    @GetMapping("/examples")
    public Map<String, String> getExamples() {
        return coroutineSupportService.getCoroutineExamples();
    }

    @GetMapping("/advantages")
    public List<String> getAdvantages() {
        return coroutineSupportService.getCoroutineAdvantages();
    }

    @GetMapping("/comparison")
    public Map<String, Map<String, String>> getComparison() {
        return coroutineSupportService.getComparisonTable();
    }

    @GetMapping("/flow-operators")
    public List<String> getFlowOperators() {
        return coroutineSupportService.getFlowOperators();
    }

    @GetMapping("/best-practices")
    public List<String> getBestPractices() {
        return coroutineSupportService.getBestPractices();
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * Example 1: Suspend Controller (Kotlin)
 * Example 2: Flow Streaming (Kotlin)
 * Example 3: Parallel Operations (Kotlin)
 * Example 4: Error Handling (Kotlin)
 * Example 5: Testing (Kotlin)
 * 
 * See full examples in Kotlin documentation.
 */
