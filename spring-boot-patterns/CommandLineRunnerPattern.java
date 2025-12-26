package com.example.springboot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Command Line Runner Pattern
 * ============================
 * 
 * Demonstrates the Command Line Runner pattern in Spring Boot for executing code
 * after the application starts with access to command line arguments as String array.
 * 
 * Use Cases:
 * ----------
 * 1. Executing startup tasks that need raw command line arguments
 * 2. Data initialization and seeding
 * 3. Running batch jobs on startup
 * 4. Performing system checks and validations
 * 5. Loading cache or warming up connections
 * 6. Executing migration scripts
 * 7. Setting up application state based on arguments
 * 8. Running one-time setup tasks
 * 
 * Key Features:
 * -------------
 * - Functional interface with single run method
 * - Receives String[] args from command line
 * - Executed after ApplicationContext is loaded
 * - Multiple runners can be ordered using @Order
 * - Can be defined as @Component or @Bean
 * - Exceptions will prevent application startup
 * - Runs before ApplicationReadyEvent
 * - Simpler than ApplicationRunner for basic cases
 * 
 * CommandLineRunner vs ApplicationRunner:
 * ----------------------------------------
 * CommandLineRunner:
 *   - Receives String[] args (raw arguments)
 *   - Simpler for basic string processing
 *   - Direct access to command line strings
 *   - Less parsing capability
 * 
 * ApplicationRunner:
 *   - Receives ApplicationArguments (parsed)
 *   - Better for complex argument parsing
 *   - Distinguishes option vs non-option args
 *   - Supports --key=value format
 * 
 * Execution Order:
 * ----------------
 * 1. ApplicationContext refresh complete
 * 2. CommandLineRunner.run() executed (ordered by @Order)
 * 3. ApplicationRunner.run() executed (ordered by @Order)
 * 4. ApplicationReadyEvent published
 * 5. Application fully started
 * 
 * Best Practices:
 * ---------------
 * 1. Use @Order for execution sequence control
 * 2. Handle exceptions gracefully
 * 3. Keep startup tasks lightweight
 * 4. Log execution progress
 * 5. Use ApplicationRunner for complex args
 * 6. Consider async execution for long tasks
 * 7. Make runners conditionally enabled
 * 8. Test runners independently
 * 
 * Configuration Properties:
 * -------------------------
 * # Disable specific runners conditionally
 * app.runners.database-init.enabled=true
 * app.runners.cache-warmup.enabled=false
 * 
 * Common Patterns:
 * ----------------
 * 1. Database seeding
 * 2. Cache warming
 * 3. Connection pool initialization
 * 4. Configuration validation
 * 5. File system setup
 * 6. External service health checks
 * 7. License validation
 * 8. Feature flag initialization
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class CommandLineRunnerPattern {

    public static void main(String[] args) {
        // Run with arguments: --env=prod --debug data1 data2
        SpringApplication.run(CommandLineRunnerPattern.class, args);
    }

    /**
     * Basic CommandLineRunner as Bean
     * Demonstrates simple argument printing
     */
    @Bean
    @Order(1)
    public CommandLineRunner basicRunner() {
        return args -> {
            System.out.println("=== Basic CommandLineRunner Execution ===");
            System.out.println("Received " + args.length + " arguments");
            System.out.println("Arguments: " + Arrays.toString(args));
            System.out.println("=========================================\n");
        };
    }

    /**
     * Database Initialization Runner
     * Demonstrates ordered execution for setup tasks
     */
    @Component
    @Order(2)
    static class DatabaseInitRunner implements CommandLineRunner {
        
        @Override
        public void run(String... args) throws Exception {
            System.out.println("=== Database Initialization Runner ===");
            
            // Check for specific arguments
            boolean resetDb = Arrays.asList(args).contains("--reset-db");
            boolean seedData = Arrays.asList(args).contains("--seed-data");
            
            System.out.println("Reset Database: " + resetDb);
            System.out.println("Seed Data: " + seedData);
            
            if (resetDb) {
                System.out.println("Resetting database schema...");
                // Simulate database reset
                Thread.sleep(500);
                System.out.println("Database reset complete");
            }
            
            if (seedData) {
                System.out.println("Seeding initial data...");
                // Simulate data seeding
                Thread.sleep(500);
                System.out.println("Data seeding complete");
            }
            
            System.out.println("Database initialization finished");
            System.out.println("======================================\n");
        }
    }

    /**
     * Configuration Validation Runner
     * Demonstrates validation and error handling
     */
    @Component
    @Order(3)
    static class ConfigValidationRunner implements CommandLineRunner {
        
        @Override
        public void run(String... args) throws Exception {
            System.out.println("=== Configuration Validation Runner ===");
            
            // Parse environment from args
            String environment = "development";
            for (String arg : args) {
                if (arg.startsWith("--env=")) {
                    environment = arg.substring(6);
                }
            }
            
            System.out.println("Environment: " + environment);
            
            // Validate configuration
            validateConfiguration(environment);
            
            System.out.println("Configuration validation passed");
            System.out.println("=======================================\n");
        }
        
        private void validateConfiguration(String env) throws Exception {
            System.out.println("Validating configuration for: " + env);
            
            // Simulate validation checks
            if ("production".equalsIgnoreCase(env)) {
                System.out.println("  ✓ SSL certificate check");
                System.out.println("  ✓ Database connection pool size");
                System.out.println("  ✓ External API credentials");
                System.out.println("  ✓ Cache configuration");
            } else {
                System.out.println("  ✓ Basic configuration check");
            }
        }
    }

    /**
     * Argument Parser Runner
     * Demonstrates manual argument parsing
     */
    @Component
    @Order(4)
    static class ArgumentParserRunner implements CommandLineRunner {
        
        @Override
        public void run(String... args) throws Exception {
            System.out.println("=== Argument Parser Runner ===");
            
            ParsedArguments parsed = parseArguments(args);
            
            System.out.println("Options:");
            parsed.options.forEach((key, value) -> 
                System.out.println("  " + key + " = " + value));
            
            System.out.println("Non-option arguments:");
            parsed.nonOptionArgs.forEach(arg -> 
                System.out.println("  - " + arg));
            
            System.out.println("Debug mode: " + parsed.options.containsKey("debug"));
            System.out.println("==============================\n");
        }
        
        private ParsedArguments parseArguments(String... args) {
            ParsedArguments result = new ParsedArguments();
            
            for (String arg : args) {
                if (arg.startsWith("--")) {
                    // Option argument
                    String option = arg.substring(2);
                    int equalsIndex = option.indexOf('=');
                    
                    if (equalsIndex > 0) {
                        String key = option.substring(0, equalsIndex);
                        String value = option.substring(equalsIndex + 1);
                        result.options.put(key, value);
                    } else {
                        result.options.put(option, "true");
                    }
                } else if (arg.startsWith("-")) {
                    // Short option
                    String option = arg.substring(1);
                    result.options.put(option, "true");
                } else {
                    // Non-option argument
                    result.nonOptionArgs.add(arg);
                }
            }
            
            return result;
        }
        
        static class ParsedArguments {
            java.util.Map<String, String> options = new java.util.HashMap<>();
            java.util.List<String> nonOptionArgs = new java.util.ArrayList<>();
        }
    }

    /**
     * Conditional Runner
     * Demonstrates conditional execution based on arguments
     */
    @Component
    @Order(5)
    static class ConditionalRunner implements CommandLineRunner {
        
        @Override
        public void run(String... args) throws Exception {
            System.out.println("=== Conditional Runner ===");
            
            boolean isTestMode = Arrays.asList(args).contains("--test-mode");
            boolean isDebugMode = Arrays.asList(args).contains("--debug");
            
            if (isTestMode) {
                System.out.println("Running in TEST mode");
                runTestModeTasks();
            } else {
                System.out.println("Running in NORMAL mode");
                runNormalModeTasks();
            }
            
            if (isDebugMode) {
                System.out.println("Debug logging enabled");
                printDebugInfo(args);
            }
            
            System.out.println("==========================\n");
        }
        
        private void runTestModeTasks() {
            System.out.println("  - Loading test data");
            System.out.println("  - Enabling debug endpoints");
            System.out.println("  - Disabling external integrations");
        }
        
        private void runNormalModeTasks() {
            System.out.println("  - Loading production data");
            System.out.println("  - Connecting to external services");
            System.out.println("  - Initializing monitoring");
        }
        
        private void printDebugInfo(String... args) {
            System.out.println("\n  Debug Information:");
            System.out.println("  Total arguments: " + args.length);
            System.out.println("  Java version: " + System.getProperty("java.version"));
            System.out.println("  OS: " + System.getProperty("os.name"));
            System.out.println("  User: " + System.getProperty("user.name"));
        }
    }

    /**
     * Lambda-based Runners
     * Demonstrates concise runner definition
     */
    @Bean
    @Order(6)
    public CommandLineRunner lambdaRunner1() {
        return args -> {
            System.out.println("=== Lambda Runner 1 (Logging) ===");
            System.out.println("Application started at: " + new java.util.Date());
            System.out.println("=================================\n");
        };
    }

    @Bean
    @Order(7)
    public CommandLineRunner lambdaRunner2() {
        return args -> {
            System.out.println("=== Lambda Runner 2 (Metrics) ===");
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            System.out.println("Memory Usage:");
            System.out.println("  Total: " + (totalMemory / 1024 / 1024) + " MB");
            System.out.println("  Used: " + (usedMemory / 1024 / 1024) + " MB");
            System.out.println("  Free: " + (freeMemory / 1024 / 1024) + " MB");
            System.out.println("=================================\n");
        };
    }

    /**
     * Error Handling Runner
     * Demonstrates exception handling patterns
     */
    @Bean
    @Order(8)
    public CommandLineRunner errorHandlingRunner() {
        return args -> {
            System.out.println("=== Error Handling Runner ===");
            
            try {
                // Simulate a task that might fail
                boolean failMode = Arrays.asList(args).contains("--fail");
                
                if (failMode) {
                    throw new RuntimeException("Simulated startup failure");
                }
                
                System.out.println("All startup checks passed ✓");
                
            } catch (Exception e) {
                System.err.println("ERROR: " + e.getMessage());
                System.err.println("Application startup failed!");
                // Re-throw to prevent application startup
                throw e;
            }
            
            System.out.println("=============================\n");
        };
    }
}
