package com.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Failure Analyzer Pattern
 * 
 * Demonstrates creating custom failure analyzers to provide helpful
 * error messages and solutions when application startup fails.
 * 
 * Key Concepts:
 * - FailureAnalyzer interface
 * - AbstractFailureAnalyzer
 * - Failure analysis
 * - Error reporting
 * - META-INF/spring.factories
 * 
 * Use Cases:
 * - Custom error messages
 * - Configuration problems
 * - Dependency issues
 * - Startup failures
 * - Developer guidance
 */
@SpringBootApplication
public class FailureAnalyzerPattern {

    public static void main(String[] args) {
        SpringApplication.run(FailureAnalyzerPattern.class, args);
    }
}

/**
 * Custom exception for database connection
 */
class DatabaseConnectionException extends RuntimeException {
    
    private final String databaseUrl;
    private final String username;

    public DatabaseConnectionException(String message, String databaseUrl, String username) {
        super(message);
        this.databaseUrl = databaseUrl;
        this.username = username;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getUsername() {
        return username;
    }
}

/**
 * Custom failure analyzer
 */
class DatabaseConnectionFailureAnalyzer extends AbstractFailureAnalyzer<DatabaseConnectionException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, DatabaseConnectionException cause) {
        String description = String.format(
                "Failed to connect to database at '%s' with username '%s'",
                cause.getDatabaseUrl(),
                cause.getUsername()
        );

        String action = String.format(
                "Review your database configuration:\n\n" +
                "1. Verify database URL: %s\n" +
                "2. Check username: %s\n" +
                "3. Ensure database server is running\n" +
                "4. Verify credentials in application.properties:\n" +
                "   spring.datasource.url=%s\n" +
                "   spring.datasource.username=%s\n" +
                "   spring.datasource.password=***\n\n" +
                "5. Check database driver is in classpath\n" +
                "6. Verify network connectivity",
                cause.getDatabaseUrl(),
                cause.getUsername(),
                cause.getDatabaseUrl(),
                cause.getUsername()
        );

        return new FailureAnalysis(description, action, cause);
    }
}

/**
 * Another custom failure analyzer example
 */
class ConfigurationPropertyFailureAnalyzer extends AbstractFailureAnalyzer<IllegalArgumentException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, IllegalArgumentException cause) {
        if (cause.getMessage() != null && cause.getMessage().contains("property")) {
            String description = "Invalid configuration property: " + cause.getMessage();
            
            String action = "Review your application.properties or application.yml:\n\n" +
                    "1. Check property syntax\n" +
                    "2. Verify property values\n" +
                    "3. Check for typos\n" +
                    "4. Consult documentation for valid values\n" +
                    "5. Use IDE autocomplete for property names";
            
            return new FailureAnalysis(description, action, cause);
        }
        return null;
    }
}

/**
 * Controller providing failure analyzer information
 */
@Controller
class FailureAnalyzerController {

    @GetMapping("/failure-analyzer/info")
    @ResponseBody
    public Map<String, Object> getInfo() {
        return Map.of(
                "customAnalyzers", new String[]{
                        "DatabaseConnectionFailureAnalyzer",
                        "ConfigurationPropertyFailureAnalyzer"
                },
                "purpose", "Provide helpful error messages",
                "registration", "META-INF/spring.factories"
        );
    }

    @GetMapping("/failure-analyzer/example")
    @ResponseBody
    public Map<String, String> getExample() {
        return Map.of(
                "scenario", "Database connection failure",
                "analyzer", "DatabaseConnectionFailureAnalyzer",
                "output", "Detailed error with connection URL and username"
        );
    }
}

/**
 * Documentation:
 * 
 * FailureAnalyzer Interface:
 * public interface FailureAnalyzer {
 *     FailureAnalysis analyze(Throwable failure);
 * }
 * 
 * AbstractFailureAnalyzer:
 * public class MyFailureAnalyzer 
 *         extends AbstractFailureAnalyzer<MyException> {
 *     
 *     @Override
 *     protected FailureAnalysis analyze(Throwable rootFailure, 
 *                                      MyException cause) {
 *         String description = "What went wrong";
 *         String action = "How to fix it";
 *         return new FailureAnalysis(description, action, cause);
 *     }
 * }
 * 
 * Registration (META-INF/spring.factories):
 * org.springframework.boot.diagnostics.FailureAnalyzer=\
 * com.example.MyFailureAnalyzer
 * 
 * Built-in Failure Analyzers:
 * - BeanCurrentlyInCreationFailureAnalyzer
 * - BeanNotOfRequiredTypeFailureAnalyzer
 * - BindFailureAnalyzer
 * - BindValidationFailureAnalyzer
 * - DataSourceBeanCreationFailureAnalyzer
 * - NoSuchBeanDefinitionFailureAnalyzer
 * - NoUniqueBeanDefinitionFailureAnalyzer
 * - PortInUseFailureAnalyzer
 * - ValidationExceptionFailureAnalyzer
 * 
 * Example Output:
 * ***************************
 * APPLICATION FAILED TO START
 * ***************************
 * 
 * Description:
 * 
 * Failed to connect to database at 'jdbc:mysql://localhost:3306/mydb' 
 * with username 'root'
 * 
 * Action:
 * 
 * Review your database configuration:
 * 
 * 1. Verify database URL: jdbc:mysql://localhost:3306/mydb
 * 2. Check username: root
 * 3. Ensure database server is running
 * ...
 * 
 * Best Practices:
 * - Provide clear descriptions
 * - Suggest specific actions
 * - Include relevant context
 * - Make messages actionable
 * - Test failure scenarios
 * - Document analyzers
 * - Handle null cases
 * - Return null if not applicable
 * 
 * Testing:
 * @Test
 * void testFailureAnalyzer() {
 *     MyException exception = new MyException("test");
 *     MyFailureAnalyzer analyzer = new MyFailureAnalyzer();
 *     FailureAnalysis analysis = analyzer.analyze(exception);
 *     
 *     assertThat(analysis.getDescription())
 *         .contains("expected text");
 *     assertThat(analysis.getAction())
 *         .contains("suggested fix");
 * }
 */
