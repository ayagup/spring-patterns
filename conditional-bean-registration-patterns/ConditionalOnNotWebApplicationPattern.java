package com.example.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditional On Not Web Application Pattern
 * ==========================================
 * 
 * Demonstrates @ConditionalOnNotWebApplication annotation that creates beans
 * only when the application is NOT running as a web application. This is used
 * for batch, CLI, messaging, and other non-web applications.
 * 
 * Key Concepts:
 * ------------
 * 1. @ConditionalOnNotWebApplication - Bean registration for non-web apps
 * 2. Application Type Detection - Detect non-web contexts
 * 3. Batch/CLI Beans - Command-line and batch-specific beans
 * 4. Non-Web Infrastructure - Features without HTTP server
 * 5. Standalone Applications - Desktop, batch, scheduled jobs
 * 
 * How It Works:
 * ------------
 * - Checks if application is NOT running as a web application
 * - Opposite of @ConditionalOnWebApplication
 * - Creates beans for:
 *   * Batch processing applications
 *   * Command-line tools
 *   * Scheduled job applications
 *   * Message consumers (Kafka, RabbitMQ)
 *   * Background workers
 * - Evaluated at configuration processing time
 * 
 * Non-Web Application Types:
 * --------------------------
 * - Spring Batch applications
 * - Command-line applications (CommandLineRunner)
 * - Scheduled job applications (@Scheduled)
 * - Message consumer applications
 * - Background processing workers
 * - Data migration tools
 * - Cron jobs
 * 
 * Common Use Cases:
 * ----------------
 * - Batch job configuration
 * - CLI argument processing
 * - Scheduled task setup
 * - Message queue consumers
 * - Background workers
 * - ETL processes
 * - Database migration tools
 * - File processing applications
 * 
 * Syntax:
 * ------
 * @ConditionalOnNotWebApplication
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Batch Processing Configuration
 */
@Configuration
@ConditionalOnNotWebApplication
class BatchProcessingConfiguration {
    
    /**
     * Configure batch job infrastructure
     */
    @Bean
    public String batchJobLauncher() {
        System.out.println("Creating Batch Job Launcher (non-web application)");
        System.out.println("  Batch processing mode detected");
        return "Batch Job Launcher";
    }
    
    @Bean
    public String jobRepository() {
        System.out.println("Creating Job Repository");
        return "Job Repository";
    }
    
    @Bean
    public String stepExecutor() {
        System.out.println("Creating Step Executor");
        return "Step Executor";
    }
}

/**
 * Example 2: Command-Line Application Configuration
 */
@Configuration
@ConditionalOnNotWebApplication
class CommandLineConfiguration {
    
    /**
     * CLI argument parser
     */
    @Bean
    public String commandLineArgumentParser() {
        System.out.println("Creating Command Line Argument Parser");
        System.out.println("  CLI application detected");
        return "Command Line Argument Parser";
    }
    
    @Bean
    public String consoleOutput() {
        System.out.println("Creating Console Output Handler");
        return "Console Output Handler";
    }
    
    @Bean
    public String interactiveShell() {
        System.out.println("Creating Interactive Shell");
        return "Interactive Shell";
    }
}

/**
 * Example 3: Scheduled Jobs Configuration
 */
@Configuration
@ConditionalOnNotWebApplication
class ScheduledJobsConfiguration {
    
    /**
     * Configure scheduled tasks
     */
    @Bean
    public String scheduledTaskExecutor() {
        System.out.println("Creating Scheduled Task Executor");
        System.out.println("  Scheduled job application detected");
        return "Scheduled Task Executor";
    }
    
    @Bean
    public String cronJobRegistry() {
        System.out.println("Creating Cron Job Registry");
        return "Cron Job Registry";
    }
    
    @Bean
    public String taskScheduler() {
        System.out.println("Creating Task Scheduler");
        return "Task Scheduler";
    }
}

/**
 * Example 4: Message Consumer Configuration
 */
@Configuration
@ConditionalOnNotWebApplication
class MessageConsumerConfiguration {
    
    /**
     * Configure Kafka consumers
     */
    @Bean
    public String kafkaMessageConsumer() {
        System.out.println("Creating Kafka Message Consumer");
        System.out.println("  Message-driven application detected");
        return "Kafka Message Consumer";
    }
    
    @Bean
    public String messageListenerContainer() {
        System.out.println("Creating Message Listener Container");
        return "Message Listener Container";
    }
    
    @Bean
    public String deadLetterQueueHandler() {
        System.out.println("Creating Dead Letter Queue Handler");
        return "Dead Letter Queue Handler";
    }
}

/**
 * Example 5: Background Worker Configuration
 */
@Configuration
@ConditionalOnNotWebApplication
class BackgroundWorkerConfiguration {
    
    /**
     * Configure background processing
     */
    @Bean
    public String workerThreadPool() {
        System.out.println("Creating Worker Thread Pool");
        System.out.println("  Background worker application");
        return "Worker Thread Pool";
    }
    
    @Bean
    public String taskQueue() {
        System.out.println("Creating Task Queue");
        return "Task Queue";
    }
    
    @Bean
    public String workerMonitor() {
        System.out.println("Creating Worker Monitor");
        return "Worker Monitor";
    }
}

/**
 * Example 6: ETL Process Configuration
 */
@Configuration
@ConditionalOnNotWebApplication
class ETLProcessConfiguration {
    
    /**
     * Configure ETL pipeline
     */
    @Bean
    public String dataExtractor() {
        System.out.println("Creating Data Extractor");
        System.out.println("  ETL application detected");
        return "Data Extractor";
    }
    
    @Bean
    public String dataTransformer() {
        System.out.println("Creating Data Transformer");
        return "Data Transformer";
    }
    
    @Bean
    public String dataLoader() {
        System.out.println("Creating Data Loader");
        return "Data Loader";
    }
}

/**
 * Example 7: Database Migration Configuration
 */
@Configuration
@ConditionalOnNotWebApplication
class DatabaseMigrationConfiguration {
    
    /**
     * Configure database migration tool
     */
    @Bean
    public String migrationExecutor() {
        System.out.println("Creating Migration Executor");
        System.out.println("  Database migration tool detected");
        return "Migration Executor";
    }
    
    @Bean
    public String schemaValidator() {
        System.out.println("Creating Schema Validator");
        return "Schema Validator";
    }
}

/**
 * Example 8: File Processing Configuration
 */
@Configuration
@ConditionalOnNotWebApplication
class FileProcessingConfiguration {
    
    /**
     * Configure file batch processor
     */
    @Bean
    public String fileWatcher() {
        System.out.println("Creating File Watcher");
        System.out.println("  File processing application");
        return "File Watcher";
    }
    
    @Bean
    public String fileProcessor() {
        System.out.println("Creating File Processor");
        return "File Processor";
    }
    
    @Bean
    public String fileArchiver() {
        System.out.println("Creating File Archiver");
        return "File Archiver";
    }
}

/**
 * Example 9: Data Synchronization Configuration
 */
@Configuration
@ConditionalOnNotWebApplication
class DataSyncConfiguration {
    
    /**
     * Configure data sync service
     */
    @Bean
    public String dataSynchronizer() {
        System.out.println("Creating Data Synchronizer");
        System.out.println("  Data sync application");
        return "Data Synchronizer";
    }
    
    @Bean
    public String conflictResolver() {
        System.out.println("Creating Conflict Resolver");
        return "Conflict Resolver";
    }
}

/**
 * Example 10: Report Generation Configuration
 */
@Configuration
@ConditionalOnNotWebApplication
class ReportGenerationConfiguration {
    
    /**
     * Configure report generator
     */
    @Bean
    public String reportGenerator() {
        System.out.println("Creating Report Generator");
        System.out.println("  Report generation application");
        return "Report Generator";
    }
    
    @Bean
    public String pdfRenderer() {
        System.out.println("Creating PDF Renderer");
        return "PDF Renderer";
    }
    
    @Bean
    public String emailSender() {
        System.out.println("Creating Email Sender (for report delivery)");
        return "Email Sender";
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class ConditionalOnNotWebApplicationPattern {
    
    /**
     * Example: General non-web application bean
     */
    @Bean
    @ConditionalOnNotWebApplication
    public String nonWebApplicationService() {
        System.out.println("Creating Non-Web Application Service");
        System.out.println("  Application Type: NON-WEB");
        System.out.println("  Use cases: Batch, CLI, Scheduled jobs, Workers");
        return "Non-Web Application Service";
    }
    
    /**
     * Example: Command-line runner
     */
    @Bean
    @ConditionalOnNotWebApplication
    public String commandLineRunner() {
        System.out.println("Creating Command Line Runner");
        System.out.println("  Executes on application startup");
        return "Command Line Runner";
    }
    
    /**
     * Example: Application event listener
     */
    @Bean
    @ConditionalOnNotWebApplication
    public String applicationReadyListener() {
        System.out.println("Creating Application Ready Listener");
        System.out.println("  Triggers when application context is ready");
        return "Application Ready Listener";
    }
}

/**
 * Usage Examples and Best Practices
 */
class ConditionalOnNotWebApplicationUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Conditional On Not Web Application Pattern");
        System.out.println("===========================================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Create beans only for non-web applications");
        System.out.println("- Configure batch, CLI, and worker applications");
        System.out.println("- Exclude beans from web contexts\n");
        
        System.out.println("Syntax:");
        System.out.println("@ConditionalOnNotWebApplication\n");
        
        System.out.println("Non-Web Application Types:");
        System.out.println("1. Spring Batch - Job processing");
        System.out.println("2. Command-Line - CLI tools");
        System.out.println("3. Scheduled Jobs - Cron/timer tasks");
        System.out.println("4. Message Consumers - Kafka/RabbitMQ");
        System.out.println("5. Background Workers - Async processing");
        System.out.println("6. ETL Processes - Data pipelines");
        System.out.println("7. Database Migrations - Schema updates");
        System.out.println("8. File Processors - Batch file handling");
        System.out.println("9. Data Sync - Replication services");
        System.out.println("10. Report Generation - Scheduled reports\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. Batch job launchers");
        System.out.println("2. CLI argument parsers");
        System.out.println("3. Scheduled task executors");
        System.out.println("4. Message queue consumers");
        System.out.println("5. Worker thread pools");
        System.out.println("6. ETL pipeline components");
        System.out.println("7. Migration executors");
        System.out.println("8. File watchers");
        System.out.println("9. Data synchronizers");
        System.out.println("10. Report generators\n");
        
        System.out.println("When Application is NON-WEB:");
        System.out.println("- No embedded web server");
        System.out.println("- No HTTP endpoints");
        System.out.println("- No @RestController or @Controller");
        System.out.println("- Uses CommandLineRunner or ApplicationRunner");
        System.out.println("- Typically exits after completing task\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Use for batch/CLI-specific beans");
        System.out.println("- Configure worker infrastructure");
        System.out.println("- Set up scheduled tasks");
        System.out.println("- Handle graceful shutdown");
        System.out.println("- Log to file instead of HTTP endpoints");
        System.out.println("- Use CommandLineRunner for startup logic");
        System.out.println("- Implement proper error handling");
        System.out.println("- Configure retry mechanisms\n");
        
        System.out.println("Example Pattern:");
        System.out.println("@Configuration");
        System.out.println("@ConditionalOnNotWebApplication");
        System.out.println("public class BatchConfiguration {");
        System.out.println("  ");
        System.out.println("  @Bean");
        System.out.println("  public JobLauncher jobLauncher() {");
        System.out.println("    return new SimpleJobLauncher();");
        System.out.println("  }");
        System.out.println("  ");
        System.out.println("  @Bean");
        System.out.println("  public CommandLineRunner runner() {");
        System.out.println("    return args -> {");
        System.out.println("      // Execute batch job");
        System.out.println("    };");
        System.out.println("  }");
        System.out.println("}");
    }
}
