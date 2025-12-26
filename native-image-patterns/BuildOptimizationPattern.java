package com.example.nativeimage.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 SPRING BOOT NATIVE IMAGE - BUILD OPTIMIZATION PATTERN 💡
 * ===========================================================
 * 
 * Demonstrates build optimization techniques for GraalVM Native Image.
 * Optimizing native image builds reduces binary size, improves startup time,
 * and decreases memory footprint for production deployments.
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ BINARY SIZE OPTIMIZATION:
 *    - Remove unused code
 *    - Exclude unnecessary dependencies
 *    - Optimize resources
 *    - Compression techniques
 * 
 * 2️⃣ BUILD TIME OPTIMIZATION:
 *    - Incremental builds
 *    - Build caching
 *    - Parallel compilation
 *    - Resource pre-processing
 * 
 * 3️⃣ RUNTIME OPTIMIZATION:
 *    - Memory configuration
 *    - GC tuning
 *    - Performance profiling
 *    - PGO (Profile-Guided Optimization)
 * 
 * 4️⃣ STARTUP TIME OPTIMIZATION:
 *    - Class initialization
 *    - Bean creation optimization
 *    - Lazy initialization
 *    - Configuration tuning
 * 
 * 📦 BUILD CONFIGURATION:
 * ======================
 * 
 * Maven (pom.xml):
 * ----------------
 * <plugin>
 *     <groupId>org.graalvm.buildtools</groupId>
 *     <artifactId>native-maven-plugin</artifactId>
 *     <configuration>
 *         <buildArgs>
 *             <!-- Optimization level -->
 *             <arg>-O3</arg>
 *             
 *             <!-- Size optimization -->
 *             <arg>-H:+RemoveUnusedSymbols</arg>
 *             <arg>-H:+ReportExceptionStackTraces</arg>
 *             
 *             <!-- Compression -->
 *             <arg>-H:+CompressedReferences</arg>
 *             
 *             <!-- Memory -->
 *             <arg>-J-Xmx8g</arg>
 *             
 *             <!-- Quick build (development) -->
 *             <arg>-Ob</arg>
 *         </buildArgs>
 *     </configuration>
 * </plugin>
 * 
 * Gradle (build.gradle):
 * ----------------------
 * graalvmNative {
 *     binaries {
 *         main {
 *             buildArgs.add("-O3")
 *             buildArgs.add("-H:+RemoveUnusedSymbols")
 *             buildArgs.add("-H:+CompressedReferences")
 *             buildArgs.add("-J-Xmx8g")
 *         }
 *     }
 * }
 * 
 * 🔧 OPTIMIZATION FLAGS:
 * =====================
 * 
 * Size Optimization:
 * ------------------
 * -H:+RemoveUnusedSymbols         # Remove unused code
 * -H:+StripDebugInfo              # Remove debug info
 * -H:+CompressedReferences        # Compress object references
 * --gc=serial                     # Use serial GC (smaller)
 * --no-fallback                   # No fallback image
 * 
 * Build Time Optimization:
 * ------------------------
 * -Ob                             # Quick build (dev)
 * -O0                             # No optimization (fastest build)
 * -J-Xmx8g                        # Increase build memory
 * --parallelism=4                 # Parallel compilation threads
 * 
 * Runtime Optimization:
 * ---------------------
 * -O3                             # Max optimization (prod)
 * -march=native                   # CPU-specific optimization
 * --gc=G1                         # Use G1 GC (better performance)
 * -H:+UnlockExperimentalVMOptions # Experimental features
 * 
 * PGO (Profile-Guided Optimization):
 * -----------------------------------
 * 1. Build instrumented image:
 *    --pgo-instrument
 * 
 * 2. Run with workload:
 *    ./app (generates profile data)
 * 
 * 3. Rebuild with profile:
 *    --pgo=default.iprof
 * 
 * 🎯 SIZE REDUCTION TECHNIQUES:
 * ============================
 * 
 * 1. Exclude Unused Dependencies:
 * --------------------------------
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-web</artifactId>
 *     <exclusions>
 *         <exclusion>
 *             <groupId>org.springframework.boot</groupId>
 *             <artifactId>spring-boot-starter-tomcat</artifactId>
 *         </exclusion>
 *     </exclusions>
 * </dependency>
 * 
 * 2. Minimize Resources:
 * ----------------------
 * - Remove unused static files
 * - Compress images
 * - Minimize CSS/JS
 * - Exclude test resources
 * 
 * 3. Strip Debug Info:
 * --------------------
 * -H:+StripDebugInfo
 * -H:-IncludeDebugInfo
 * 
 * 4. Use Compressed References:
 * -----------------------------
 * -H:+CompressedReferences  # 30-40% memory reduction
 * 
 * 💡 TYPICAL SIZES:
 * ================
 * 
 * Unoptimized:     150-200 MB
 * Optimized:       80-120 MB
 * Highly Optimized: 50-80 MB
 * Minimal:         30-50 MB (no Spring Boot)
 * 
 * 🚀 PERFORMANCE COMPARISON:
 * =========================
 * 
 * ┌─────────────────┬──────────┬───────────┬────────┐
 * │ Metric          │ JVM      │ Native    │ Gain   │
 * ├─────────────────┼──────────┼───────────┼────────┤
 * │ Startup Time    │ 3-10s    │ 50-100ms  │ 50-100x│
 * │ Memory (Idle)   │ 200-500M │ 20-50MB   │ 10x    │
 * │ Binary Size     │ 300-500M │ 80-120MB  │ 3-4x   │
 * │ First Request   │ 1-2s     │ 10-50ms   │ 20-40x │
 * │ Build Time      │ 30s      │ 3-5min    │ 6-10x  │
 * └─────────────────┴──────────┴───────────┴────────┘
 * 
 * 💡 WHEN TO OPTIMIZE:
 * ===================
 * ✅ Production deployments
 * ✅ Serverless functions (Lambda, Cloud Functions)
 * ✅ Container environments (K8s)
 * ✅ Edge computing
 * ✅ CLI applications
 * ✅ Memory-constrained systems
 * ✅ Cost optimization (cloud)
 * 
 * ❌ WHEN TO SKIP:
 * ===============
 * ❌ Development builds (use -Ob)
 * ❌ Rapid iteration
 * ❌ Debugging sessions
 * ❌ Local testing
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
public class BuildOptimizationPattern {

    public static void main(String[] args) {
        SpringApplication.run(BuildOptimizationPattern.class, args);
    }
}

/**
 * Build Optimization Service
 */
@Service
class BuildOptimizationService {

    /**
     * Get optimization strategies
     */
    public Map<String, List<String>> getOptimizationStrategies() {
        Map<String, List<String>> strategies = new LinkedHashMap<>();
        
        List<String> sizeOptimization = new ArrayList<>();
        sizeOptimization.add("-H:+RemoveUnusedSymbols - Remove dead code");
        sizeOptimization.add("-H:+StripDebugInfo - Remove debug symbols");
        sizeOptimization.add("-H:+CompressedReferences - 30-40% memory reduction");
        sizeOptimization.add("--gc=serial - Smaller GC footprint");
        sizeOptimization.add("--no-fallback - No JVM fallback image");
        strategies.put("Size Optimization", sizeOptimization);
        
        List<String> buildTimeOptimization = new ArrayList<>();
        buildTimeOptimization.add("-Ob - Quick build mode (development)");
        buildTimeOptimization.add("-O0 - No optimization (fastest build)");
        buildTimeOptimization.add("-J-Xmx8g - Increase build heap size");
        buildTimeOptimization.add("--parallelism=4 - Parallel compilation");
        buildTimeOptimization.add("--enable-native-access=ALL-UNNAMED - Enable native access");
        strategies.put("Build Time Optimization", buildTimeOptimization);
        
        List<String> runtimeOptimization = new ArrayList<>();
        runtimeOptimization.add("-O3 - Maximum optimization level");
        runtimeOptimization.add("-march=native - CPU-specific optimizations");
        runtimeOptimization.add("--gc=G1 - Better GC performance");
        runtimeOptimization.add("-H:+UnlockExperimentalVMOptions - Experimental features");
        runtimeOptimization.add("--enable-monitoring=heapdump,jfr - Enable monitoring");
        strategies.put("Runtime Optimization", runtimeOptimization);
        
        List<String> pgoOptimization = new ArrayList<>();
        pgoOptimization.add("1. Build instrumented: --pgo-instrument");
        pgoOptimization.add("2. Run with workload to generate profile");
        pgoOptimization.add("3. Rebuild with profile: --pgo=default.iprof");
        pgoOptimization.add("Result: 10-30% performance improvement");
        strategies.put("PGO (Profile-Guided Optimization)", pgoOptimization);
        
        return strategies;
    }

    /**
     * Get size reduction techniques
     */
    public List<String> getSizeReductionTechniques() {
        List<String> techniques = new ArrayList<>();
        
        techniques.add("✅ Exclude unused Spring Boot starters");
        techniques.add("✅ Remove unnecessary dependencies");
        techniques.add("✅ Strip debug information");
        techniques.add("✅ Use compressed references");
        techniques.add("✅ Minimize static resources");
        techniques.add("✅ Use serial GC (smaller footprint)");
        techniques.add("✅ Remove unused reflection hints");
        techniques.add("✅ Exclude test dependencies");
        techniques.add("✅ Use UPX compression (30-50% reduction)");
        techniques.add("💡 Typical reduction: 150MB → 80MB");
        
        return techniques;
    }

    /**
     * Get build configuration examples
     */
    public Map<String, String> getBuildConfigurations() {
        Map<String, String> configs = new LinkedHashMap<>();
        
        configs.put("Development Build", 
            "-Ob -O0 -J-Xmx4g --no-fallback");
        
        configs.put("Production Build", 
            "-O3 -march=native -H:+RemoveUnusedSymbols -H:+StripDebugInfo -H:+CompressedReferences --gc=G1 -J-Xmx8g");
        
        configs.put("Size-Optimized Build", 
            "-O3 -H:+RemoveUnusedSymbols -H:+StripDebugInfo -H:+CompressedReferences --gc=serial --no-fallback");
        
        configs.put("Fast Build (CI)", 
            "-Ob -O1 -J-Xmx6g --parallelism=4 --no-fallback");
        
        configs.put("PGO Build Step 1", 
            "-O3 --pgo-instrument -J-Xmx8g");
        
        configs.put("PGO Build Step 2", 
            "-O3 --pgo=default.iprof -J-Xmx8g");
        
        return configs;
    }

    /**
     * Get performance metrics
     */
    public Map<String, Map<String, String>> getPerformanceMetrics() {
        Map<String, Map<String, String>> metrics = new LinkedHashMap<>();
        
        Map<String, String> jvm = new LinkedHashMap<>();
        jvm.put("Startup Time", "3-10 seconds");
        jvm.put("Memory (Idle)", "200-500 MB");
        jvm.put("Binary Size", "300-500 MB");
        jvm.put("First Request", "1-2 seconds");
        jvm.put("Build Time", "30 seconds");
        metrics.put("JVM", jvm);
        
        Map<String, String> nativeUnoptimized = new LinkedHashMap<>();
        nativeUnoptimized.put("Startup Time", "100-200 ms");
        nativeUnoptimized.put("Memory (Idle)", "40-70 MB");
        nativeUnoptimized.put("Binary Size", "150-200 MB");
        nativeUnoptimized.put("First Request", "20-50 ms");
        nativeUnoptimized.put("Build Time", "5-7 minutes");
        metrics.put("Native (Unoptimized)", nativeUnoptimized);
        
        Map<String, String> nativeOptimized = new LinkedHashMap<>();
        nativeOptimized.put("Startup Time", "50-100 ms");
        nativeOptimized.put("Memory (Idle)", "20-50 MB");
        nativeOptimized.put("Binary Size", "80-120 MB");
        nativeOptimized.put("First Request", "10-30 ms");
        nativeOptimized.put("Build Time", "3-5 minutes");
        metrics.put("Native (Optimized)", nativeOptimized);
        
        Map<String, String> gains = new LinkedHashMap<>();
        gains.put("Startup Time", "50-100x faster");
        gains.put("Memory", "10x reduction");
        gains.put("Binary Size", "3-4x smaller");
        gains.put("First Request", "20-40x faster");
        gains.put("Build Time", "6-10x slower");
        metrics.put("Improvement (JVM → Native)", gains);
        
        return metrics;
    }

    /**
     * Get best practices
     */
    public List<String> getBestPractices() {
        List<String> practices = new ArrayList<>();
        
        practices.add("✅ Use -Ob for development builds (fast iteration)");
        practices.add("✅ Use -O3 for production builds (max performance)");
        practices.add("✅ Increase build memory: -J-Xmx8g");
        practices.add("✅ Enable parallel compilation: --parallelism=4");
        practices.add("✅ Profile production workload (PGO)");
        practices.add("✅ Strip debug info in production");
        practices.add("✅ Use compressed references");
        practices.add("✅ Remove unused dependencies");
        practices.add("✅ Test native image thoroughly");
        practices.add("✅ Monitor binary size growth");
        practices.add("⚠️ Don't over-optimize development builds");
        practices.add("⚠️ Balance build time vs binary size");
        practices.add("💡 Use CI/CD for production builds");
        
        return practices;
    }

    /**
     * Get Docker optimization tips
     */
    public List<String> getDockerOptimizations() {
        List<String> tips = new ArrayList<>();
        
        tips.add("✅ Multi-stage build (GraalVM builder + minimal runtime)");
        tips.add("✅ Use distroless or scratch base image");
        tips.add("✅ Copy only native binary (no JVM needed)");
        tips.add("✅ Set USER to non-root");
        tips.add("✅ Use .dockerignore to exclude build artifacts");
        tips.add("💡 Typical image size: 80-150 MB vs 300-500 MB JVM");
        tips.add("💡 Startup: <1 second vs 10-30 seconds JVM");
        
        return tips;
    }
}

/**
 * Build Optimization REST Controller
 */
@RestController
@RequestMapping("/api/build-optimization")
class BuildOptimizationController {

    private final BuildOptimizationService buildOptimizationService;

    public BuildOptimizationController(BuildOptimizationService buildOptimizationService) {
        this.buildOptimizationService = buildOptimizationService;
    }

    /**
     * GET /api/build-optimization/strategies
     * Get optimization strategies
     */
    @GetMapping("/strategies")
    public Map<String, List<String>> getStrategies() {
        return buildOptimizationService.getOptimizationStrategies();
    }

    /**
     * GET /api/build-optimization/size-reduction
     * Get size reduction techniques
     */
    @GetMapping("/size-reduction")
    public List<String> getSizeReduction() {
        return buildOptimizationService.getSizeReductionTechniques();
    }

    /**
     * GET /api/build-optimization/configurations
     * Get build configuration examples
     */
    @GetMapping("/configurations")
    public Map<String, String> getConfigurations() {
        return buildOptimizationService.getBuildConfigurations();
    }

    /**
     * GET /api/build-optimization/metrics
     * Get performance metrics
     */
    @GetMapping("/metrics")
    public Map<String, Map<String, String>> getMetrics() {
        return buildOptimizationService.getPerformanceMetrics();
    }

    /**
     * GET /api/build-optimization/best-practices
     * Get best practices
     */
    @GetMapping("/best-practices")
    public List<String> getBestPractices() {
        return buildOptimizationService.getBestPractices();
    }

    /**
     * GET /api/build-optimization/docker
     * Get Docker optimization tips
     */
    @GetMapping("/docker")
    public List<String> getDockerOptimizations() {
        return buildOptimizationService.getDockerOptimizations();
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ DEVELOPMENT BUILD (FAST):
 * -----------------------------
 * mvn -Pnative native:compile -Dspring-boot.build-image.buildpacks=paketobuildpacks/builder:base \
 *   -Dnative-image.arg.-Ob \
 *   -Dnative-image.arg.-O0 \
 *   -Dnative-image.arg.-J-Xmx4g
 * 
 * Result: 5-7 min build, 150-200 MB binary
 * 
 * 2️⃣ PRODUCTION BUILD (OPTIMIZED):
 * ---------------------------------
 * mvn -Pnative native:compile \
 *   -Dnative-image.arg.-O3 \
 *   -Dnative-image.arg.-march=native \
 *   -Dnative-image.arg.-H:+RemoveUnusedSymbols \
 *   -Dnative-image.arg.-H:+StripDebugInfo \
 *   -Dnative-image.arg.-H:+CompressedReferences \
 *   -Dnative-image.arg.--gc=G1 \
 *   -Dnative-image.arg.-J-Xmx8g
 * 
 * Result: 3-5 min build, 80-120 MB binary
 * 
 * 3️⃣ SIZE-OPTIMIZED BUILD:
 * -------------------------
 * mvn -Pnative native:compile \
 *   -Dnative-image.arg.-O3 \
 *   -Dnative-image.arg.-H:+RemoveUnusedSymbols \
 *   -Dnative-image.arg.-H:+StripDebugInfo \
 *   -Dnative-image.arg.-H:+CompressedReferences \
 *   -Dnative-image.arg.--gc=serial \
 *   -Dnative-image.arg.--no-fallback
 * 
 * Result: 3-4 min build, 50-80 MB binary
 * 
 * 4️⃣ PGO BUILD (2-STEP PROCESS):
 * -------------------------------
 * # Step 1: Build instrumented image
 * mvn -Pnative native:compile \
 *   -Dnative-image.arg.-O3 \
 *   -Dnative-image.arg.--pgo-instrument
 * 
 * # Step 2: Run with production workload
 * ./target/myapp
 * # (generates default.iprof)
 * 
 * # Step 3: Rebuild with profile
 * mvn -Pnative native:compile \
 *   -Dnative-image.arg.-O3 \
 *   -Dnative-image.arg.--pgo=default.iprof
 * 
 * Result: 10-30% performance improvement
 * 
 * 5️⃣ DOCKER MULTI-STAGE BUILD:
 * -----------------------------
 * FROM ghcr.io/graalvm/graalvm-ce:ol9-java17 AS builder
 * WORKDIR /app
 * COPY . .
 * RUN ./mvnw -Pnative native:compile -DskipTests \
 *     -Dnative-image.arg.-O3 \
 *     -Dnative-image.arg.-H:+RemoveUnusedSymbols \
 *     -Dnative-image.arg.-H:+StripDebugInfo \
 *     -Dnative-image.arg.-J-Xmx8g
 * 
 * FROM gcr.io/distroless/base
 * COPY --from=builder /app/target/myapp /myapp
 * ENTRYPOINT ["/myapp"]
 * 
 * Result: 80-150 MB image vs 300-500 MB JVM image
 * 
 * 6️⃣ GET OPTIMIZATION INFO:
 * --------------------------
 * curl http://localhost:8080/api/build-optimization/strategies
 * curl http://localhost:8080/api/build-optimization/metrics
 * curl http://localhost:8080/api/build-optimization/best-practices
 */
