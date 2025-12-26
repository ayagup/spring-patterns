package com.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Spring Factories Pattern
 * 
 * Demonstrates using META-INF/spring.factories for loading classes
 * and enabling Spring Boot's auto-configuration mechanism.
 * 
 * Key Concepts:
 * - META-INF/spring.factories file
 * - SpringFactoriesLoader
 * - Auto-configuration registration
 * - Listener registration
 * - Initializer registration
 * 
 * Use Cases:
 * - Auto-configuration
 * - Custom starters
 * - Event listeners
 * - Context initializers
 * - Failure analyzers
 */
@SpringBootApplication
public class SpringFactoriesPattern {

    public static void main(String[] args) {
        SpringApplication.run(SpringFactoriesPattern.class, args);
    }
}

/**
 * Custom auto-configuration
 */
@Configuration
class CustomFactoryConfiguration {
    
    public CustomFactoryConfiguration() {
        System.out.println("CustomFactoryConfiguration loaded via spring.factories");
    }
}

/**
 * Custom application listener
 */
class CustomApplicationListener 
        implements org.springframework.context.ApplicationListener<
            org.springframework.boot.context.event.ApplicationStartedEvent> {

    @Override
    public void onApplicationEvent(
            org.springframework.boot.context.event.ApplicationStartedEvent event) {
        System.out.println("CustomApplicationListener: Application started");
    }
}

/**
 * Controller demonstrating spring.factories
 */
@Controller
class SpringFactoriesController {

    @GetMapping("/factories/info")
    @ResponseBody
    public Map<String, Object> getFactoriesInfo() {
        return Map.of(
                "file", "META-INF/spring.factories",
                "purpose", "Register auto-configurations and components",
                "loader", "SpringFactoriesLoader"
        );
    }

    @GetMapping("/factories/types")
    @ResponseBody
    public Map<String, List<String>> getFactoryTypes() {
        return Map.of(
                "autoConfiguration", List.of(
                        "org.springframework.boot.autoconfigure.EnableAutoConfiguration"
                ),
                "listeners", List.of(
                        "org.springframework.context.ApplicationListener"
                ),
                "initializers", List.of(
                        "org.springframework.context.ApplicationContextInitializer"
                ),
                "failureAnalyzers", List.of(
                        "org.springframework.boot.diagnostics.FailureAnalyzer"
                )
        );
    }
}

/**
 * Documentation:
 * 
 * spring.factories File Location:
 * src/main/resources/META-INF/spring.factories
 * 
 * Format (Properties file):
 * # Comment
 * interface.FullyQualifiedName=\
 * implementation1.FullyQualifiedName,\
 * implementation2.FullyQualifiedName
 * 
 * Auto-Configuration Example:
 * # META-INF/spring.factories
 * org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
 * com.example.MyAutoConfiguration,\
 * com.example.AnotherAutoConfiguration
 * 
 * Application Listener Example:
 * org.springframework.context.ApplicationListener=\
 * com.example.MyApplicationListener
 * 
 * Context Initializer Example:
 * org.springframework.context.ApplicationContextInitializer=\
 * com.example.MyContextInitializer
 * 
 * Failure Analyzer Example:
 * org.springframework.boot.diagnostics.FailureAnalyzer=\
 * com.example.MyFailureAnalyzer
 * 
 * Environment Post Processor:
 * org.springframework.boot.env.EnvironmentPostProcessor=\
 * com.example.MyEnvironmentPostProcessor
 * 
 * Run Listener:
 * org.springframework.boot.SpringApplicationRunListener=\
 * com.example.MyRunListener
 * 
 * Property Source Loader:
 * org.springframework.boot.env.PropertySourceLoader=\
 * com.example.MyPropertySourceLoader
 * 
 * Loading Factories Programmatically:
 * List<MyInterface> implementations = 
 *     SpringFactoriesLoader.loadFactories(
 *         MyInterface.class, 
 *         classLoader
 *     );
 * 
 * Load Factory Names:
 * List<String> factoryNames = 
 *     SpringFactoriesLoader.loadFactoryNames(
 *         MyInterface.class,
 *         classLoader
 *     );
 * 
 * Common Factory Types:
 * 
 * 1. EnableAutoConfiguration:
 *    - Automatically configured beans
 *    - Starter auto-configurations
 * 
 * 2. ApplicationListener:
 *    - Event listeners
 *    - Lifecycle callbacks
 * 
 * 3. ApplicationContextInitializer:
 *    - Early initialization
 *    - Context customization
 * 
 * 4. FailureAnalyzer:
 *    - Custom error analysis
 *    - Helpful error messages
 * 
 * 5. EnvironmentPostProcessor:
 *    - Environment customization
 *    - Property source manipulation
 * 
 * 6. BeanFactoryPostProcessor:
 *    - Bean definition modification
 *    - Early bean processing
 * 
 * Complete Example:
 * 
 * # META-INF/spring.factories
 * # Auto-Configuration
 * org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
 * com.example.starter.MyAutoConfiguration
 * 
 * # Application Listeners
 * org.springframework.context.ApplicationListener=\
 * com.example.starter.MyApplicationListener
 * 
 * # Context Initializers
 * org.springframework.context.ApplicationContextInitializer=\
 * com.example.starter.MyContextInitializer
 * 
 * # Failure Analyzers
 * org.springframework.boot.diagnostics.FailureAnalyzer=\
 * com.example.starter.MyFailureAnalyzer
 * 
 * # Environment Post Processors
 * org.springframework.boot.env.EnvironmentPostProcessor=\
 * com.example.starter.MyEnvironmentPostProcessor
 * 
 * Migration Note:
 * Spring Boot 2.7+ also supports spring.factories
 * Spring Boot 3.0+ prefers META-INF/spring/
 *   org.springframework.boot.autoconfigure.AutoConfiguration.imports
 * 
 * AutoConfiguration.imports (Spring Boot 3.0+):
 * # One class per line, no key required
 * com.example.MyAutoConfiguration
 * com.example.AnotherAutoConfiguration
 * 
 * Benefits:
 * - Declarative registration
 * - No code scanning needed
 * - Explicit dependencies
 * - Better performance
 * - Clear documentation
 * 
 * Best Practices:
 * - One factory type per section
 * - Use fully qualified names
 * - Add comments
 * - Keep organized
 * - Test registrations
 * - Document purpose
 * - Follow conventions
 */
