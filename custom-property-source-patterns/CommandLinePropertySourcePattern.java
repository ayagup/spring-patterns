package com.example.propertysource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Command Line Property Source Pattern
 * 
 * Demonstrates using command line arguments as a property source.
 * This pattern is essential for overriding configuration at runtime
 * and for containerized deployments.
 * 
 * Key Concepts:
 * - CommandLinePropertySource
 * - SimpleCommandLinePropertySource
 * - Argument parsing
 * - Runtime configuration override
 * - Bootstrap properties
 * 
 * Use Cases:
 * - Runtime configuration override
 * - Script-based deployments
 * - Testing with different configurations
 * - One-off configuration changes
 * - Development and debugging
 */
@SpringBootApplication
public class CommandLinePropertySourcePattern {

    public static void main(String[] args) {
        // Example command line arguments:
        // --server.port=9090 --spring.profiles.active=dev --custom.prop=value
        SpringApplication app = new SpringApplication(CommandLinePropertySourcePattern.class);
        app.run(args);
    }
}

/**
 * Service to access command line properties
 */
@org.springframework.stereotype.Service
class CommandLinePropertyService {

    private final ConfigurableEnvironment environment;
    private final String[] args;

    public CommandLinePropertyService(ConfigurableEnvironment environment,
                                     ApplicationContext applicationContext) {
        this.environment = environment;
        // Get original command line args if available
        this.args = extractCommandLineArgs(environment);
    }

    /**
     * Extract command line arguments from environment
     */
    private String[] extractCommandLineArgs(ConfigurableEnvironment environment) {
        org.springframework.core.env.PropertySource<?> commandLineSource = 
                environment.getPropertySources().get("commandLineArgs");
        
        if (commandLineSource instanceof CommandLinePropertySource) {
            CommandLinePropertySource<?> clps = (CommandLinePropertySource<?>) commandLineSource;
            return clps.getSource();
        }
        return new String[0];
    }

    /**
     * Get property from command line
     */
    public String getCommandLineProperty(String key) {
        return environment.getProperty(key);
    }

    /**
     * Get all command line arguments
     */
    public String[] getCommandLineArgs() {
        return args;
    }

    /**
     * Get parsed command line properties
     */
    public Map<String, String> getParsedCommandLineProperties() {
        Map<String, String> properties = new HashMap<>();
        
        org.springframework.core.env.PropertySource<?> commandLineSource = 
                environment.getPropertySources().get("commandLineArgs");
        
        if (commandLineSource instanceof org.springframework.core.env.EnumerablePropertySource) {
            org.springframework.core.env.EnumerablePropertySource<?> eps = 
                    (org.springframework.core.env.EnumerablePropertySource<?>) commandLineSource;
            
            for (String propertyName : eps.getPropertyNames()) {
                Object value = eps.getProperty(propertyName);
                properties.put(propertyName, value != null ? value.toString() : "null");
            }
        }
        
        return properties;
    }

    /**
     * Check if running with specific argument
     */
    public boolean hasArgument(String arg) {
        return Arrays.asList(args).contains(arg);
    }
}

/**
 * Controller to expose command line properties
 */
@RestController
@RequestMapping("/api/commandline")
class CommandLineController {

    private final CommandLinePropertyService commandLineService;

    public CommandLineController(CommandLinePropertyService commandLineService) {
        this.commandLineService = commandLineService;
    }

    @GetMapping("/args")
    public Map<String, Object> getCommandLineArgs() {
        return Map.of(
                "args", commandLineService.getCommandLineArgs(),
                "count", commandLineService.getCommandLineArgs().length
        );
    }

    @GetMapping("/properties")
    public Map<String, String> getCommandLineProperties() {
        return commandLineService.getParsedCommandLineProperties();
    }

    @GetMapping("/property")
    public Map<String, String> getProperty(String key) {
        return Map.of(
                "key", key,
                "value", commandLineService.getCommandLineProperty(key) != null ? 
                        commandLineService.getCommandLineProperty(key) : "Not found"
        );
    }
}

/**
 * Application initializer for custom command line processing
 */
class CustomCommandLineInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        // Custom processing of command line arguments
        org.springframework.core.env.PropertySource<?> commandLineSource = 
                environment.getPropertySources().get("commandLineArgs");
        
        if (commandLineSource != null) {
            // Can manipulate or add additional property sources based on command line args
            System.out.println("Command line arguments processed");
        }
    }
}

/**
 * Documentation:
 * 
 * Command Line Argument Formats:
 * 
 * 1. Option Arguments (--key=value):
 *    java -jar app.jar --server.port=8080 --spring.profiles.active=dev
 *    - Parsed as properties
 *    - Accessible via Environment
 *    - Highest precedence
 * 
 * 2. Non-Option Arguments (values without --):
 *    java -jar app.jar arg1 arg2 arg3
 *    - Available as "nonOptionArgs" property
 *    - Accessed as List<String>
 * 
 * 3. Mixed Arguments:
 *    java -jar app.jar --port=8080 file1.txt --debug file2.txt
 *    - Options parsed as properties
 *    - Non-options collected separately
 * 
 * Spring Boot Command Line Argument Parsing:
 * - Automatically creates CommandLinePropertySource
 * - Added with highest precedence
 * - Overrides any other property source
 * - Can be disabled: app.setAddCommandLineProperties(false)
 * 
 * Accessing Command Line Properties:
 * 
 * 1. Via Environment:
 *    @Autowired
 *    Environment env;
 *    String port = env.getProperty("server.port");
 * 
 * 2. Via @Value:
 *    @Value("${server.port}")
 *    String port;
 * 
 * 3. Via ApplicationArguments:
 *    @Autowired
 *    ApplicationArguments args;
 *    List<String> nonOptions = args.getNonOptionArgs();
 *    boolean debug = args.containsOption("debug");
 *    List<String> files = args.getOptionValues("file");
 * 
 * Common Use Cases:
 * 
 * 1. Override Server Port:
 *    --server.port=9090
 * 
 * 2. Set Active Profile:
 *    --spring.profiles.active=dev,local
 * 
 * 3. Set Log Level:
 *    --logging.level.com.example=DEBUG
 * 
 * 4. Override Database URL:
 *    --spring.datasource.url=jdbc:mysql://localhost:3306/mydb
 * 
 * 5. Enable/Disable Features:
 *    --feature.newui.enabled=true
 * 
 * Property Name Formats:
 * - Dot notation: --server.port=8080
 * - Camel case: --serverPort=8080
 * - Underscore: --server_port=8080
 * - All are equivalent due to relaxed binding
 * 
 * Best Practices:
 * - Use --key=value format for properties
 * - Document all command line options
 * - Provide sensible defaults
 * - Validate command line input
 * - Use for environment-specific overrides
 * - Prefer environment variables for containers
 * - Use profiles for major configuration changes
 * 
 * Priority:
 * - Command line args have HIGHEST precedence
 * - Override ALL other property sources
 * - Useful for quick testing and debugging
 * - Can override Spring Boot defaults
 * 
 * Programmatic Access:
 * 
 * 1. In main method:
 *    public static void main(String[] args) {
 *        for (String arg : args) {
 *            System.out.println(arg);
 *        }
 *        SpringApplication.run(App.class, args);
 *    }
 * 
 * 2. Via ApplicationArguments bean:
 *    @Component
 *    class MyBean {
 *        @Autowired
 *        public MyBean(ApplicationArguments args) {
 *            boolean debug = args.containsOption("debug");
 *            List<String> files = args.getOptionValues("file");
 *        }
 *    }
 * 
 * 3. Via ApplicationRunner or CommandLineRunner:
 *    @Component
 *    class MyRunner implements ApplicationRunner {
 *        public void run(ApplicationArguments args) {
 *            // Process arguments after startup
 *        }
 *    }
 * 
 * Disabling Command Line Properties:
 * SpringApplication app = new SpringApplication(MyApp.class);
 * app.setAddCommandLineProperties(false);
 * app.run(args);
 * 
 * Testing:
 * - Pass args in @SpringBootTest:
 *   @SpringBootTest(args = {"--server.port=9999"})
 * 
 * - Programmatically:
 *   SpringApplication.run(App.class, new String[]{"--debug"});
 * 
 * Security:
 * - Command line args visible in process listings
 * - Avoid passing sensitive data
 * - Use environment variables or files for secrets
 * - Consider using Spring Cloud Config for sensitive config
 */
