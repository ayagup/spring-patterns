package com.example.nativeimage.patterns;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🚀 SPRING BOOT NATIVE IMAGE - COMPILATION PATTERN 🚀
 * =====================================================
 * 
 * Demonstrates Spring Boot Native Image compilation using GraalVM Native Image.
 * Native images compile Spring Boot applications to native executables with:
 * - Instant startup (milliseconds vs seconds)
 * - Reduced memory footprint (10-20x less)
 * - No JVM required at runtime
 * - Ahead-of-Time (AOT) compilation
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ GRAALVM NATIVE IMAGE:
 *    - Compiles Java bytecode to native machine code
 *    - Closed-world assumption (all code known at build time)
 *    - Static analysis for reachability
 *    - AOT compilation replaces JIT
 * 
 * 2️⃣ SPRING NATIVE:
 *    - Spring Boot 3.0+ native support
 *    - Automatic configuration for native
 *    - AOT processing during build
 *    - Runtime hints for reflection/resources
 * 
 * 3️⃣ BUILD PROCESS:
 *    - Maven/Gradle native profile
 *    - Spring AOT processing
 *    - GraalVM Native Image compilation
 *    - Native executable output
 * 
 * 4️⃣ LIMITATIONS:
 *    - No dynamic class loading
 *    - Limited reflection
 *    - No runtime bytecode generation
 *    - Longer build times
 * 
 * 📦 DEPENDENCIES (pom.xml):
 * ==========================
 * <parent>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-parent</artifactId>
 *     <version>3.2.0</version>
 * </parent>
 * 
 * <dependencies>
 *     <dependency>
 *         <groupId>org.springframework.boot</groupId>
 *         <artifactId>spring-boot-starter-web</artifactId>
 *     </dependency>
 * </dependencies>
 * 
 * <build>
 *     <plugins>
 *         <plugin>
 *             <groupId>org.graalvm.buildtools</groupId>
 *             <artifactId>native-maven-plugin</artifactId>
 *         </plugin>
 *         <plugin>
 *             <groupId>org.springframework.boot</groupId>
 *             <artifactId>spring-boot-maven-plugin</artifactId>
 *         </plugin>
 *     </plugins>
 * </build>
 * 
 * 🔧 BUILD NATIVE IMAGE:
 * ======================
 * 
 * Prerequisites:
 * --------------
 * 1. Install GraalVM:
 *    - Download from https://www.graalvm.org/downloads/
 *    - Or use SDKMAN: sdk install java 21-graal
 * 
 * 2. Install Native Image:
 *    gu install native-image
 * 
 * 3. Set JAVA_HOME:
 *    export JAVA_HOME=/path/to/graalvm
 * 
 * Maven Build:
 * ------------
 * # Build native image
 * mvn -Pnative native:compile
 * 
 * # Run native executable
 * ./target/myapp
 * 
 * Gradle Build:
 * -------------
 * # Build native image
 * ./gradlew nativeCompile
 * 
 * # Run native executable
 * ./build/native/nativeCompile/myapp
 * 
 * 🚀 NATIVE IMAGE BENEFITS:
 * =========================
 * 
 * 1️⃣ INSTANT STARTUP:
 *    - JVM JAR: 3-10 seconds
 *    - Native: 0.05-0.1 seconds (50-100ms)
 *    - Perfect for serverless, CLI tools
 * 
 * 2️⃣ REDUCED MEMORY:
 *    - JVM JAR: 200-500 MB heap
 *    - Native: 20-50 MB RSS
 *    - Better container density
 * 
 * 3️⃣ SMALL EXECUTABLE:
 *    - Includes only reachable code
 *    - No JVM overhead
 *    - Typical size: 50-100 MB
 * 
 * 4️⃣ PEAK PERFORMANCE:
 *    - AOT compilation (no warmup)
 *    - Predictable performance
 *    - No JIT overhead
 * 
 * ⚙️ NATIVE IMAGE CONFIGURATION:
 * ==============================
 * 
 * application.properties:
 * -----------------------
 * # Native image specific settings
 * spring.native.remove-unused-autoconfig=true
 * spring.native.remove-yaml-support=false
 * 
 * # Optimize for binary size
 * spring.jmx.enabled=false
 * management.endpoints.enabled-by-default=false
 * 
 * Native Build Arguments:
 * -----------------------
 * # In pom.xml or build.gradle
 * <buildArgs>
 *     <buildArg>--verbose</buildArg>
 *     <buildArg>--no-fallback</buildArg>
 *     <buildArg>-H:+ReportExceptionStackTraces</buildArg>
 *     <buildArg>-H:+AddAllCharsets</buildArg>
 *     <buildArg>-H:ResourceConfigurationFiles=resource-config.json</buildArg>
 * </buildArgs>
 * 
 * 🐳 DOCKER NATIVE IMAGE:
 * =======================
 * 
 * Multi-stage Dockerfile:
 * -----------------------
 * FROM ghcr.io/graalvm/graalvm-ce:latest AS builder
 * WORKDIR /app
 * COPY . .
 * RUN ./mvnw -Pnative native:compile
 * 
 * FROM ubuntu:22.04
 * WORKDIR /app
 * COPY --from=builder /app/target/myapp .
 * EXPOSE 8080
 * ENTRYPOINT ["./myapp"]
 * 
 * # Build: docker build -t myapp-native .
 * # Run: docker run -p 8080:8080 myapp-native
 * # Image size: ~80-120 MB (vs 300-500 MB for JVM)
 * 
 * ☸️ KUBERNETES DEPLOYMENT:
 * =========================
 * 
 * Deployment manifest:
 * --------------------
 * apiVersion: apps/v1
 * kind: Deployment
 * metadata:
 *   name: myapp-native
 * spec:
 *   replicas: 3
 *   template:
 *     spec:
 *       containers:
 *       - name: myapp
 *         image: myapp-native:latest
 *         resources:
 *           requests:
 *             memory: "32Mi"    # 10x less than JVM
 *             cpu: "100m"
 *           limits:
 *             memory: "128Mi"
 *             cpu: "500m"
 *         readinessProbe:
 *           httpGet:
 *             path: /actuator/health
 *             port: 8080
 *           initialDelaySeconds: 1  # Much faster than JVM (10-30s)
 *           periodSeconds: 5
 * 
 * 📊 PERFORMANCE COMPARISON:
 * =========================
 * 
 * Metric              | JVM JAR    | Native Image | Improvement
 * --------------------|------------|--------------|------------
 * Startup Time        | 5-10s      | 0.05-0.1s    | 50-100x
 * Memory (RSS)        | 200-500 MB | 20-50 MB     | 10x
 * Image Size          | 300-500 MB | 80-120 MB    | 3-4x
 * First Request       | Slow (JIT) | Fast (AOT)   | 5-10x
 * Peak Throughput     | High       | Good         | Similar
 * Build Time          | 30-60s     | 3-5 min      | 3-6x slower
 * 
 * ⚠️ LIMITATIONS & TRADE-OFFS:
 * ===========================
 * 
 * 1️⃣ REFLECTION LIMITATIONS:
 *    - Must register reflective access
 *    - No dynamic class loading
 *    - Requires runtime hints
 * 
 * 2️⃣ RESOURCE LIMITATIONS:
 *    - Resources must be registered
 *    - No classpath scanning at runtime
 *    - Static resource configuration
 * 
 * 3️⃣ PROXY LIMITATIONS:
 *    - JDK/CGLib proxies must be registered
 *    - No dynamic proxy generation
 *    - AOT proxy generation
 * 
 * 4️⃣ BUILD TIME:
 *    - Much longer builds (3-5 min vs 30-60s)
 *    - High CPU/memory for compilation
 *    - CI/CD pipeline impact
 * 
 * 5️⃣ DEBUGGING:
 *    - Limited debugger support
 *    - No JVM tools (VisualVM, JMX)
 *    - Different profiling approach
 * 
 * 💡 WHEN TO USE NATIVE IMAGE:
 * ===========================
 * ✅ Serverless functions (AWS Lambda, Google Cloud Functions)
 * ✅ CLI tools and utilities
 * ✅ Microservices with fast startup needs
 * ✅ Container environments (Kubernetes)
 * ✅ Memory-constrained environments
 * ✅ Edge computing
 * ✅ IoT applications
 * 
 * ❌ WHEN NOT TO USE:
 * ==================
 * ❌ Heavy reflection-based frameworks
 * ❌ Dynamic class loading requirements
 * ❌ Runtime bytecode generation
 * ❌ Long-running services (JIT benefits)
 * ❌ Frequent deployments (long build times)
 * ❌ Development environments (slow feedback)
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
@ImportRuntimeHints(NativeImageCompilationPattern.AppRuntimeHints.class)
public class NativeImageCompilationPattern {

    public static void main(String[] args) {
        // Record startup time
        long startTime = System.currentTimeMillis();
        
        SpringApplication.run(NativeImageCompilationPattern.class, args);
        
        long startupTime = System.currentTimeMillis() - startTime;
        System.out.println("🚀 Application started in " + startupTime + "ms");
        
        // In native image: ~50-100ms
        // In JVM: ~3000-10000ms
    }

    /**
     * Runtime Hints for Native Image
     * Registers reflection, resources, proxies needed at runtime
     */
    static class AppRuntimeHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Register reflection hints
            hints.reflection()
                .registerType(User.class)
                .registerType(Product.class);
            
            // Register resource hints
            hints.resources()
                .registerPattern("application*.properties")
                .registerPattern("application*.yml")
                .registerPattern("static/**")
                .registerPattern("templates/**");
            
            System.out.println("✅ Runtime hints registered for native image");
        }
    }
}

/**
 * Native Image Configuration
 */
@Configuration
class NativeImageConfiguration {

    @Bean
    public NativeImageInfoService nativeImageInfoService() {
        return new NativeImageInfoService();
    }

    @Bean
    public NativeImageMetricsService nativeImageMetricsService() {
        return new NativeImageMetricsService();
    }
}

/**
 * Native Image Information Service
 */
@Service
class NativeImageInfoService {

    private final LocalDateTime applicationStartTime = LocalDateTime.now();
    private final Runtime runtime = Runtime.getRuntime();

    /**
     * Check if running in native image
     */
    public boolean isNativeImage() {
        return System.getProperty("org.graalvm.nativeimage.imagecode") != null;
    }

    /**
     * Get runtime information
     */
    public Map<String, Object> getRuntimeInfo() {
        Map<String, Object> info = new ConcurrentHashMap<>();
        
        info.put("isNativeImage", isNativeImage());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaVendor", System.getProperty("java.vendor"));
        info.put("osName", System.getProperty("os.name"));
        info.put("osArch", System.getProperty("os.arch"));
        info.put("availableProcessors", runtime.availableProcessors());
        
        return info;
    }

    /**
     * Get memory information
     */
    public Map<String, Object> getMemoryInfo() {
        Map<String, Object> memory = new ConcurrentHashMap<>();
        
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        memory.put("totalMemoryMB", totalMemory / (1024 * 1024));
        memory.put("usedMemoryMB", usedMemory / (1024 * 1024));
        memory.put("freeMemoryMB", freeMemory / (1024 * 1024));
        memory.put("maxMemoryMB", maxMemory / (1024 * 1024));
        
        // Native images typically use 10-20x less memory
        if (isNativeImage()) {
            memory.put("note", "Native image: Low memory footprint (~20-50 MB RSS)");
        } else {
            memory.put("note", "JVM: Higher memory footprint (~200-500 MB heap)");
        }
        
        return memory;
    }

    /**
     * Get startup information
     */
    public Map<String, Object> getStartupInfo() {
        Map<String, Object> startup = new ConcurrentHashMap<>();
        
        startup.put("startTime", applicationStartTime);
        startup.put("uptime", java.time.Duration.between(applicationStartTime, LocalDateTime.now()).getSeconds() + "s");
        
        if (isNativeImage()) {
            startup.put("note", "Native image: Instant startup (~50-100ms)");
        } else {
            startup.put("note", "JVM: Slower startup (~3-10 seconds)");
        }
        
        return startup;
    }

    /**
     * Get native image build info (if available)
     */
    public Map<String, String> getNativeImageBuildInfo() {
        Map<String, String> buildInfo = new LinkedHashMap<>();
        
        if (isNativeImage()) {
            buildInfo.put("imageCode", System.getProperty("org.graalvm.nativeimage.imagecode", "unknown"));
            buildInfo.put("buildTime", "AOT compiled");
            buildInfo.put("compiler", "GraalVM Native Image");
        } else {
            buildInfo.put("runtime", "JVM with JIT compilation");
            buildInfo.put("compiler", "HotSpot JIT");
        }
        
        return buildInfo;
    }

    /**
     * Get native image benefits
     */
    public Map<String, String> getNativeImageBenefits() {
        Map<String, String> benefits = new LinkedHashMap<>();
        
        benefits.put("⚡ Instant Startup", "50-100ms vs 3-10s (50-100x faster)");
        benefits.put("💾 Low Memory", "20-50 MB vs 200-500 MB (10x less)");
        benefits.put("📦 Small Size", "80-120 MB vs 300-500 MB (3-4x smaller)");
        benefits.put("🎯 Peak Performance", "No JIT warmup, predictable performance");
        benefits.put("🐳 Container Friendly", "Better density, faster scaling");
        benefits.put("☁️ Serverless Ready", "Perfect for AWS Lambda, Cloud Functions");
        
        return benefits;
    }

    /**
     * Get native image limitations
     */
    public List<String> getNativeImageLimitations() {
        List<String> limitations = new ArrayList<>();
        
        limitations.add("❌ No dynamic class loading");
        limitations.add("❌ Limited reflection (must register)");
        limitations.add("❌ No runtime bytecode generation");
        limitations.add("❌ Longer build times (3-5 min)");
        limitations.add("❌ Limited debugger support");
        limitations.add("❌ No JVM tools (VisualVM, JMX)");
        limitations.add("⚠️ Closed-world assumption");
        
        return limitations;
    }
}

/**
 * Native Image Metrics Service
 */
@Service
class NativeImageMetricsService {

    private long requestCount = 0;
    private long totalResponseTime = 0;
    private final LocalDateTime startTime = LocalDateTime.now();

    public void recordRequest(long responseTimeMs) {
        requestCount++;
        totalResponseTime += responseTimeMs;
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        
        metrics.put("totalRequests", requestCount);
        metrics.put("averageResponseTimeMs", 
            requestCount > 0 ? totalResponseTime / requestCount : 0);
        metrics.put("uptimeSeconds", 
            java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds());
        metrics.put("requestsPerSecond", 
            requestCount / Math.max(1, java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds()));
        
        return metrics;
    }

    public void reset() {
        requestCount = 0;
        totalResponseTime = 0;
    }
}

/**
 * Sample entities for native image
 */
class User {
    private Long id;
    private String username;
    private String email;

    public User() {}
    
    public User(Long id, String username, String email) {
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

class Product {
    private Long id;
    private String name;
    private Double price;

    public Product() {}
    
    public Product(Long id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}

/**
 * Native Image REST Controller
 */
@RestController
@RequestMapping("/api/native-image")
class NativeImageController {

    private final NativeImageInfoService infoService;
    private final NativeImageMetricsService metricsService;

    public NativeImageController(NativeImageInfoService infoService,
                                 NativeImageMetricsService metricsService) {
        this.infoService = infoService;
        this.metricsService = metricsService;
    }

    /**
     * GET /api/native-image/info
     * Get native image runtime information
     */
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        long startTime = System.currentTimeMillis();
        Map<String, Object> info = infoService.getRuntimeInfo();
        long responseTime = System.currentTimeMillis() - startTime;
        
        metricsService.recordRequest(responseTime);
        return info;
    }

    /**
     * GET /api/native-image/memory
     * Get memory information
     */
    @GetMapping("/memory")
    public Map<String, Object> getMemory() {
        return infoService.getMemoryInfo();
    }

    /**
     * GET /api/native-image/startup
     * Get startup information
     */
    @GetMapping("/startup")
    public Map<String, Object> getStartup() {
        return infoService.getStartupInfo();
    }

    /**
     * GET /api/native-image/build-info
     * Get native image build information
     */
    @GetMapping("/build-info")
    public Map<String, String> getBuildInfo() {
        return infoService.getNativeImageBuildInfo();
    }

    /**
     * GET /api/native-image/benefits
     * Get native image benefits
     */
    @GetMapping("/benefits")
    public Map<String, String> getBenefits() {
        return infoService.getNativeImageBenefits();
    }

    /**
     * GET /api/native-image/limitations
     * Get native image limitations
     */
    @GetMapping("/limitations")
    public List<String> getLimitations() {
        return infoService.getNativeImageLimitations();
    }

    /**
     * GET /api/native-image/metrics
     * Get performance metrics
     */
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        return metricsService.getMetrics();
    }

    /**
     * GET /api/native-image/hello
     * Simple endpoint for testing
     */
    @GetMapping("/hello")
    public Map<String, Object> hello() {
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> response = new ConcurrentHashMap<>();
        response.put("message", "Hello from " + 
            (infoService.isNativeImage() ? "Native Image! 🚀" : "JVM! ☕"));
        response.put("timestamp", LocalDateTime.now());
        response.put("isNativeImage", infoService.isNativeImage());
        
        long responseTime = System.currentTimeMillis() - startTime;
        response.put("responseTimeMs", responseTime);
        
        metricsService.recordRequest(responseTime);
        return response;
    }

    /**
     * POST /api/native-image/user
     * Create user (tests reflection)
     */
    @PostMapping("/user")
    public User createUser(@RequestBody User user) {
        user.setId(System.currentTimeMillis());
        return user;
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ BUILD NATIVE IMAGE (Maven):
 * -------------------------------
 * # Install GraalVM
 * sdk install java 21-graal
 * sdk use java 21-graal
 * gu install native-image
 * 
 * # Build native image
 * mvn -Pnative clean native:compile
 * 
 * # Run native executable
 * ./target/myapp
 * # Startup: ~50-100ms 🚀
 * 
 * 2️⃣ BUILD NATIVE IMAGE (Gradle):
 * --------------------------------
 * ./gradlew nativeCompile
 * ./build/native/nativeCompile/myapp
 * 
 * 3️⃣ TEST NATIVE IMAGE:
 * ----------------------
 * # Check if native
 * curl http://localhost:8080/api/native-image/info
 * 
 * # Compare startup
 * curl http://localhost:8080/api/native-image/startup
 * 
 * # Check memory
 * curl http://localhost:8080/api/native-image/memory
 * 
 * # Get benefits
 * curl http://localhost:8080/api/native-image/benefits
 * 
 * 4️⃣ DOCKER NATIVE IMAGE:
 * ------------------------
 * docker build -t myapp-native -f Dockerfile.native .
 * docker run -p 8080:8080 myapp-native
 * # Container starts in <1 second! 🚀
 * 
 * 5️⃣ COMPARE JVM VS NATIVE:
 * --------------------------
 * # JVM
 * java -jar target/myapp.jar
 * # Startup: 5-10 seconds
 * # Memory: 200-500 MB
 * 
 * # Native
 * ./target/myapp
 * # Startup: 50-100ms
 * # Memory: 20-50 MB
 * 
 * 6️⃣ AWS LAMBDA (Native):
 * ------------------------
 * # Package as AWS Lambda
 * mvn -Pnative spring-boot:build-image
 * 
 * # Deploy to AWS Lambda
 * # Cold start: ~500ms vs 5-10s (JVM)
 * 
 * 7️⃣ KUBERNETES (Native):
 * ------------------------
 * # Deploy native image
 * kubectl apply -f k8s/deployment-native.yaml
 * 
 * # Scale up quickly
 * kubectl scale deployment myapp-native --replicas=10
 * # All pods ready in seconds!
 * 
 * 8️⃣ PERFORMANCE METRICS:
 * ------------------------
 * curl http://localhost:8080/api/native-image/metrics
 * curl http://localhost:8080/api/native-image/hello
 */
