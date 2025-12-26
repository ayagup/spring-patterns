package com.example.springboot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationRunner;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Application Runner Pattern
 * 
 * Demonstrates using CommandLineRunner and ApplicationRunner to execute
 * code after Spring Boot application startup.
 * 
 * Key Concepts:
 * - CommandLineRunner
 * - ApplicationRunner
 * - Execution order with @Order
 * - Application arguments
 * - Startup tasks
 * 
 * Use Cases:
 * - Database initialization
 * - Cache warming
 * - Health checks
 * - Data migration
 * - Startup validation
 */
@SpringBootApplication
public class ApplicationRunnerPattern {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationRunnerPattern.class, args);
    }
}

/**
 * Command line runner with high priority
 */
@Component
@Order(1)
class DatabaseInitRunner implements CommandLineRunner {

    private static final List<String> executionLog = new ArrayList<>();

    @Override
    public void run(String... args) throws Exception {
        String msg = "DatabaseInitRunner: Initializing database...";
        executionLog.add(msg);
        System.out.println(msg);
        
        // Simulate database initialization
        Thread.sleep(100);
        
        msg = "DatabaseInitRunner: Database ready";
        executionLog.add(msg);
        System.out.println(msg);
    }

    public static List<String> getExecutionLog() {
        return new ArrayList<>(executionLog);
    }
}

/**
 * Application runner with medium priority
 */
@Component
@Order(2)
class CacheWarmupRunner implements ApplicationRunner {

    private static final List<String> executionLog = new ArrayList<>();

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        String msg = "CacheWarmupRunner: Warming up caches...";
        executionLog.add(msg);
        System.out.println(msg);
        
        // Access application arguments
        if (args.containsOption("cache")) {
            msg = "CacheWarmupRunner: Cache option detected - " + 
                  Arrays.toString(args.getOptionValues("cache").toArray());
            executionLog.add(msg);
            System.out.println(msg);
        }
        
        // Simulate cache warmup
        Thread.sleep(100);
        
        msg = "CacheWarmupRunner: Caches ready";
        executionLog.add(msg);
        System.out.println(msg);
    }

    public static List<String> getExecutionLog() {
        return new ArrayList<>(executionLog);
    }
}

/**
 * Command line runner with low priority
 */
@Component
@Order(3)
class HealthCheckRunner implements CommandLineRunner {

    private static final List<String> executionLog = new ArrayList<>();

    @Override
    public void run(String... args) throws Exception {
        String msg = "HealthCheckRunner: Performing health checks...";
        executionLog.add(msg);
        System.out.println(msg);
        
        // Check system health
        boolean healthy = checkSystemHealth();
        
        msg = "HealthCheckRunner: System health - " + (healthy ? "OK" : "FAILED");
        executionLog.add(msg);
        System.out.println(msg);
    }

    private boolean checkSystemHealth() {
        // Simulate health check
        return true;
    }

    public static List<String> getExecutionLog() {
        return new ArrayList<>(executionLog);
    }
}

/**
 * Event listener for application ready
 */
@Component
class ReadyEventListener {

    private static final List<String> eventLog = new ArrayList<>();

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        String msg = "Application fully started and ready to serve requests";
        eventLog.add(msg);
        System.out.println(msg);
    }

    public static List<String> getEventLog() {
        return new ArrayList<>(eventLog);
    }
}

/**
 * Controller exposing runner information
 */
@RestController
class RunnerInfoController {

    @GetMapping("/runner/execution-log")
    public Map<String, Object> getExecutionLog() {
        return Map.of(
                "databaseInit", DatabaseInitRunner.getExecutionLog(),
                "cacheWarmup", CacheWarmupRunner.getExecutionLog(),
                "healthCheck", HealthCheckRunner.getExecutionLog(),
                "readyEvent", ReadyEventListener.getEventLog()
        );
    }

    @GetMapping("/runner/info")
    public Map<String, Object> getRunnerInfo() {
        return Map.of(
                "runners", List.of(
                        "DatabaseInitRunner (Order: 1)",
                        "CacheWarmupRunner (Order: 2)",
                        "HealthCheckRunner (Order: 3)"
                ),
                "totalExecutions", 
                    DatabaseInitRunner.getExecutionLog().size() +
                    CacheWarmupRunner.getExecutionLog().size() +
                    HealthCheckRunner.getExecutionLog().size()
        );
    }
}

/**
 * Documentation:
 * 
 * CommandLineRunner vs ApplicationRunner:
 * 
 * CommandLineRunner:
 * @Component
 * class MyRunner implements CommandLineRunner {
 *     @Override
 *     public void run(String... args) {
 *         // Raw string arguments
 *         System.out.println(Arrays.toString(args));
 *     }
 * }
 * 
 * ApplicationRunner:
 * @Component
 * class MyRunner implements ApplicationRunner {
 *     @Override
 *     public void run(ApplicationArguments args) {
 *         // Parsed arguments with options
 *         if (args.containsOption("foo")) {
 *             List<String> values = args.getOptionValues("foo");
 *         }
 *     }
 * }
 * 
 * Execution Order:
 * @Order(1) - Highest priority (executes first)
 * @Order(2)
 * @Order(3) - Lowest priority (executes last)
 * 
 * Running with Arguments:
 * java -jar app.jar --foo=bar --cache=redis arg1 arg2
 * 
 * ApplicationArguments Methods:
 * - getSourceArgs() - All arguments
 * - getOptionNames() - Option names
 * - getOptionValues(name) - Values for option
 * - containsOption(name) - Check if option exists
 * - getNonOptionArgs() - Non-option arguments
 * 
 * Bean-based Runner:
 * @Bean
 * public CommandLineRunner runner() {
 *     return args -> {
 *         System.out.println("Running...");
 *     };
 * }
 * 
 * Conditional Execution:
 * @Component
 * @ConditionalOnProperty("runner.enabled")
 * class ConditionalRunner implements CommandLineRunner {
 *     @Override
 *     public void run(String... args) {
 *         // Only runs if runner.enabled=true
 *     }
 * }
 * 
 * Exception Handling:
 * @Override
 * public void run(String... args) throws Exception {
 *     try {
 *         // Startup logic
 *     } catch (Exception e) {
 *         // If exception thrown, app won't start
 *         throw new RuntimeException("Startup failed", e);
 *     }
 * }
 * 
 * Use Cases:
 * 1. Database Migration: Run Flyway/Liquibase
 * 2. Cache Warmup: Preload frequently used data
 * 3. Validation: Check configuration/dependencies
 * 4. Initial Data: Create admin user
 * 5. Cleanup: Remove old files/data
 * 
 * Best Practices:
 * - Use ApplicationRunner for complex args
 * - Order runners with @Order
 * - Handle exceptions properly
 * - Keep startup tasks fast
 * - Make runners idempotent
 * - Log execution progress
 */
