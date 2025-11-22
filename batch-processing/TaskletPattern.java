package com.spring.patterns.batch;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Tasklet Pattern - Spring Batch
 * 
 * A Tasklet is a simple interface with one method: execute().
 * It's used for steps that perform a single operation rather than
 * item-oriented processing.
 * 
 * Key Characteristics:
 * - Single transaction operation
 * - Full control over execution logic
 * - Can be executed repeatedly or once
 * - Returns RepeatStatus to control repetition
 * 
 * RepeatStatus:
 * - FINISHED: Tasklet completed, move to next step
 * - CONTINUABLE: Execute tasklet again
 * 
 * Common Use Cases:
 * - File operations (cleanup, archive, move)
 * - Database operations (stored procedures, cleanup)
 * - Resource initialization/cleanup
 * - Validation checks
 * - Email notifications
 * - API calls
 * - Report generation
 * 
 * Advantages:
 * - Simple and straightforward
 * - Complete control over logic
 * - Easy to unit test
 * - Good for non-item oriented tasks
 */
public class TaskletPattern {

    /**
     * Configuration for Tasklet-based jobs
     */
    @Configuration
    @EnableBatchProcessing
    public static class TaskletConfiguration {

        @Bean
        public PlatformTransactionManager transactionManager() {
            return new ResourcelessTransactionManager();
        }

        @Bean
        public JobRepository jobRepository(PlatformTransactionManager transactionManager) throws Exception {
            MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean(transactionManager);
            factory.afterPropertiesSet();
            return factory.getObject();
        }

        @Bean
        public JobLauncher jobLauncher(JobRepository jobRepository) {
            SimpleJobLauncher launcher = new SimpleJobLauncher();
            launcher.setJobRepository(jobRepository);
            return launcher;
        }

        // ========== Tasklet Bean Definitions ==========

        @Bean
        public Tasklet fileCleanupTasklet() {
            return new FileCleanupTasklet();
        }

        @Bean
        public Tasklet dataValidationTasklet() {
            return new DataValidationTasklet();
        }

        @Bean
        public Tasklet reportGenerationTasklet() {
            return new ReportGenerationTasklet();
        }

        @Bean
        public Tasklet notificationTasklet() {
            return new NotificationTasklet();
        }

        @Bean
        public Tasklet databaseMaintenanceTasklet() {
            return new DatabaseMaintenanceTasklet();
        }

        // ========== Step Definitions ==========

        @Bean
        public Step fileCleanupStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.step.builder.StepBuilder("fileCleanupStep", jobRepository)
                    .tasklet(fileCleanupTasklet(), transactionManager)
                    .build();
        }

        @Bean
        public Step dataValidationStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.step.builder.StepBuilder("dataValidationStep", jobRepository)
                    .tasklet(dataValidationTasklet(), transactionManager)
                    .allowStartIfComplete(true) // Can run multiple times
                    .build();
        }

        @Bean
        public Step reportGenerationStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.step.builder.StepBuilder("reportGenerationStep", jobRepository)
                    .tasklet(reportGenerationTasklet(), transactionManager)
                    .build();
        }

        @Bean
        public Step notificationStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.step.builder.StepBuilder("notificationStep", jobRepository)
                    .tasklet(notificationTasklet(), transactionManager)
                    .build();
        }

        @Bean
        public Step databaseMaintenanceStep(JobRepository jobRepository,
                                           PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.step.builder.StepBuilder("databaseMaintenanceStep", jobRepository)
                    .tasklet(databaseMaintenanceTasklet(), transactionManager)
                    .build();
        }

        // Inline Lambda Tasklet
        @Bean
        public Step lambdaTaskletStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.step.builder.StepBuilder("lambdaTaskletStep", jobRepository)
                    .tasklet((contribution, chunkContext) -> {
                        System.out.println("Executing inline lambda tasklet");
                        System.out.println("Simple operation completed");
                        return RepeatStatus.FINISHED;
                    }, transactionManager)
                    .build();
        }

        // ========== Job Definitions ==========

        @Bean
        public Job taskletWorkflowJob(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("taskletWorkflowJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(dataValidationStep(jobRepository, transactionManager))
                    .next(reportGenerationStep(jobRepository, transactionManager))
                    .next(fileCleanupStep(jobRepository, transactionManager))
                    .next(notificationStep(jobRepository, transactionManager))
                    .build();
        }

        @Bean
        public Job maintenanceJob(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("maintenanceJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(databaseMaintenanceStep(jobRepository, transactionManager))
                    .next(fileCleanupStep(jobRepository, transactionManager))
                    .build();
        }
    }

    // ========== Tasklet Implementations ==========

    /**
     * File Cleanup Tasklet
     * Removes temporary and old files
     */
    public static class FileCleanupTasklet implements Tasklet {
        
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            System.out.println("\n=== File Cleanup Tasklet ===");
            
            // Simulate file cleanup
            String[] directories = {"/tmp", "/temp", "/cache"};
            int filesDeleted = 0;
            
            for (String dir : directories) {
                System.out.println("Scanning directory: " + dir);
                // Simulate finding and deleting files
                int count = (int) (Math.random() * 10);
                filesDeleted += count;
                System.out.println("  Deleted " + count + " files");
            }
            
            System.out.println("Total files deleted: " + filesDeleted);
            
            // Store result in execution context
            contribution.incrementWriteCount(filesDeleted);
            chunkContext.getStepContext()
                       .getStepExecution()
                       .getExecutionContext()
                       .put("filesDeleted", filesDeleted);
            
            return RepeatStatus.FINISHED;
        }
    }

    /**
     * Data Validation Tasklet
     * Validates data integrity and business rules
     */
    public static class DataValidationTasklet implements Tasklet {
        
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            System.out.println("\n=== Data Validation Tasklet ===");
            
            // Simulate validation checks
            Map<String, Boolean> validations = new LinkedHashMap<>();
            validations.put("Database connection", true);
            validations.put("Required tables exist", true);
            validations.put("Data integrity check", true);
            validations.put("Foreign key constraints", true);
            validations.put("Business rules validation", true);
            
            int passed = 0;
            int failed = 0;
            
            for (Map.Entry<String, Boolean> entry : validations.entrySet()) {
                boolean result = entry.getValue() && Math.random() > 0.1; // 90% pass rate
                String status = result ? "✓ PASS" : "✗ FAIL";
                System.out.println(status + " - " + entry.getKey());
                
                if (result) {
                    passed++;
                } else {
                    failed++;
                }
            }
            
            System.out.println("\nValidation Summary:");
            System.out.println("  Passed: " + passed);
            System.out.println("  Failed: " + failed);
            
            // Store results
            ExecutionContext context = chunkContext.getStepContext()
                                                  .getStepExecution()
                                                  .getExecutionContext();
            context.put("validationsPassed", passed);
            context.put("validationsFailed", failed);
            
            if (failed > 0) {
                System.out.println("⚠ Warning: Some validations failed!");
                // Could throw exception to fail the step
                // throw new RuntimeException("Validation failed");
            }
            
            return RepeatStatus.FINISHED;
        }
    }

    /**
     * Report Generation Tasklet
     * Generates summary reports
     */
    public static class ReportGenerationTasklet implements Tasklet {
        
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            System.out.println("\n=== Report Generation Tasklet ===");
            
            // Get previous step results
            ExecutionContext context = chunkContext.getStepContext()
                                                  .getStepExecution()
                                                  .getExecutionContext();
            
            System.out.println("Generating daily summary report...");
            System.out.println("Report Date: " + new Date());
            
            // Simulate report generation
            System.out.println("\n--- Report Content ---");
            System.out.println("1. Records Processed: 1,245");
            System.out.println("2. Success Rate: 98.5%");
            System.out.println("3. Errors: 19");
            System.out.println("4. Warnings: 53");
            System.out.println("5. Processing Time: 5m 32s");
            
            if (context.containsKey("validationsPassed")) {
                System.out.println("6. Validations Passed: " + context.get("validationsPassed"));
            }
            
            System.out.println("----------------------");
            System.out.println("\nReport saved to: /reports/summary_" + System.currentTimeMillis() + ".pdf");
            
            return RepeatStatus.FINISHED;
        }
    }

    /**
     * Notification Tasklet
     * Sends notifications after job completion
     */
    public static class NotificationTasklet implements Tasklet {
        
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            System.out.println("\n=== Notification Tasklet ===");
            
            JobExecution jobExecution = chunkContext.getStepContext()
                                                   .getStepExecution()
                                                   .getJobExecution();
            
            String jobName = jobExecution.getJobInstance().getJobName();
            String status = jobExecution.getStatus().toString();
            
            System.out.println("Sending notification...");
            System.out.println("  To: admin@example.com");
            System.out.println("  Subject: Job Completion - " + jobName);
            System.out.println("  Body:");
            System.out.println("    Job: " + jobName);
            System.out.println("    Status: " + status);
            System.out.println("    Completion Time: " + new Date());
            System.out.println("    Steps Completed: " + jobExecution.getStepExecutions().size());
            
            // Simulate different notification channels
            System.out.println("\nNotification Channels:");
            System.out.println("  ✓ Email sent");
            System.out.println("  ✓ Slack message posted");
            System.out.println("  ✓ Log entry created");
            
            return RepeatStatus.FINISHED;
        }
    }

    /**
     * Database Maintenance Tasklet
     * Performs database cleanup and optimization
     */
    public static class DatabaseMaintenanceTasklet implements Tasklet {
        
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            System.out.println("\n=== Database Maintenance Tasklet ===");
            
            List<String> tasks = Arrays.asList(
                "Analyzing tables",
                "Optimizing indices",
                "Cleaning up old records",
                "Updating statistics",
                "Vacuuming database",
                "Rebuilding fragmented indices"
            );
            
            for (String task : tasks) {
                System.out.println("• " + task + "...");
                try {
                    Thread.sleep(200); // Simulate work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("  ✓ Completed");
            }
            
            System.out.println("\nDatabase maintenance completed successfully");
            
            return RepeatStatus.FINISHED;
        }
    }

    /**
     * Main demonstration
     */
    public static void main(String[] args) {
        System.out.println("=== Tasklet Pattern Demonstration ===\n");

        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(TaskletConfiguration.class);

        JobLauncher jobLauncher = context.getBean(JobLauncher.class);

        try {
            // Demo 1: Complete Workflow Job
            System.out.println("\n########## Demo 1: Tasklet Workflow Job ##########");
            Job workflowJob = context.getBean("taskletWorkflowJob", Job.class);
            JobParameters params1 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .toJobParameters();
            JobExecution execution1 = jobLauncher.run(workflowJob, params1);
            
            System.out.println("\n--- Job Execution Summary ---");
            System.out.println("Status: " + execution1.getStatus());
            System.out.println("Steps Executed: " + execution1.getStepExecutions().size());

            // Demo 2: Maintenance Job
            System.out.println("\n\n########## Demo 2: Maintenance Job ##########");
            Job maintenanceJob = context.getBean("maintenanceJob", Job.class);
            JobParameters params2 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .toJobParameters();
            JobExecution execution2 = jobLauncher.run(maintenanceJob, params2);
            
            System.out.println("\n--- Maintenance Job Summary ---");
            System.out.println("Status: " + execution2.getStatus());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            context.close();
        }

        System.out.println("\n=== Demonstration Complete ===");
    }
}

/**
 * KEY TAKEAWAYS:
 * 
 * 1. Tasklet Interface:
 *    - Single method: execute(StepContribution, ChunkContext)
 *    - Returns RepeatStatus (FINISHED or CONTINUABLE)
 *    - Full transaction control
 * 
 * 2. When to Use Tasklet:
 *    - Single operation tasks
 *    - File system operations
 *    - Stored procedure calls
 *    - Cleanup/maintenance tasks
 *    - Validation checks
 *    - Non-item oriented processing
 * 
 * 3. StepContribution:
 *    - Track read/write/skip counts
 *    - Control step execution
 *    - Update execution metrics
 * 
 * 4. ChunkContext:
 *    - Access step/job execution context
 *    - Share data between steps
 *    - Access job parameters
 * 
 * 5. ExecutionContext:
 *    - Store step state
 *    - Pass data between steps
 *    - Restart support
 * 
 * 6. RepeatStatus:
 *    - FINISHED: Complete, proceed to next step
 *    - CONTINUABLE: Execute again (loop)
 * 
 * 7. Best Practices:
 *    - Keep tasklets focused (single responsibility)
 *    - Make idempotent for restart scenarios
 *    - Use execution context for state
 *    - Handle exceptions appropriately
 *    - Log progress and results
 *    - Return FINISHED for one-time execution
 * 
 * 8. Advantages vs Chunk Processing:
 *    - Simpler for single operations
 *    - Full control over transaction
 *    - Easier to understand and maintain
 *    - Better for non-repeating tasks
 * 
 * 9. Common Patterns:
 *    - Initialize -> Process -> Cleanup
 *    - Validate -> Execute -> Notify
 *    - Backup -> Maintain -> Verify
 * 
 * 10. Testing:
 *     - Easy to unit test
 *     - Mock StepContribution and ChunkContext
 *     - Test RepeatStatus return value
 */
