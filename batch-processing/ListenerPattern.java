package com.spring.patterns.batch;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.*;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.*;

/**
 * Job and Step Execution Listener Pattern - Spring Batch
 * 
 * Listeners provide lifecycle hooks to execute custom logic at specific points
 * during job and step execution.
 * 
 * JobExecutionListener:
 * - beforeJob(JobExecution): Called before job starts
 * - afterJob(JobExecution): Called after job completes (success or failure)
 * 
 * StepExecutionListener:
 * - beforeStep(StepExecution): Called before step starts
 * - afterStep(StepExecution): Called after step completes
 * 
 * Additional Listeners:
 * - ItemReadListener: Before/after read, on read error
 * - ItemProcessListener: Before/after process, on process error
 * - ItemWriteListener: Before/after write, on write error
 * - ChunkListener: Before/after chunk, on error
 * - SkipListener: On skip events
 * - RetryListener: On retry events
 * 
 * Use Cases:
 * - Logging and monitoring
 * - Metrics collection
 * - Resource initialization/cleanup
 * - Notifications
 * - Performance tracking
 * - Error handling
 */
public class ListenerPattern {

    /**
     * Domain object for demonstration
     */
    public static class Order {
        private Long id;
        private String customerName;
        private Double amount;
        private String status;

        public Order() {}

        public Order(Long id, String customerName, Double amount, String status) {
            this.id = id;
            this.customerName = customerName;
            this.amount = amount;
            this.status = status;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        @Override
        public String toString() {
            return "Order{id=" + id + ", customer='" + customerName + "', amount=" + amount + ", status='" + status + "'}";
        }
    }

    // ========== JobExecutionListener Implementations ==========

    /**
     * Custom JobExecutionListener
     * Tracks job lifecycle and collects metrics
     */
    public static class CustomJobExecutionListener implements JobExecutionListener {
        private long startTime;

        @Override
        public void beforeJob(JobExecution jobExecution) {
            startTime = System.currentTimeMillis();
            String jobName = jobExecution.getJobInstance().getJobName();
            
            System.out.println("\n========================================");
            System.out.println("=== JOB STARTED: " + jobName + " ===");
            System.out.println("========================================");
            System.out.println("Job ID: " + jobExecution.getId());
            System.out.println("Job Instance ID: " + jobExecution.getJobInstance().getId());
            System.out.println("Start Time: " + new Date());
            System.out.println("Parameters: " + jobExecution.getJobParameters());
            System.out.println("========================================\n");
        }

        @Override
        public void afterJob(JobExecution jobExecution) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            String jobName = jobExecution.getJobInstance().getJobName();
            BatchStatus status = jobExecution.getStatus();
            
            System.out.println("\n========================================");
            System.out.println("=== JOB COMPLETED: " + jobName + " ===");
            System.out.println("========================================");
            System.out.println("Status: " + status);
            System.out.println("Exit Code: " + jobExecution.getExitStatus().getExitCode());
            System.out.println("Duration: " + duration + " ms");
            System.out.println("End Time: " + new Date());
            
            // Print step summaries
            System.out.println("\n--- Step Summaries ---");
            for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
                System.out.println("Step: " + stepExecution.getStepName());
                System.out.println("  Read: " + stepExecution.getReadCount());
                System.out.println("  Write: " + stepExecution.getWriteCount());
                System.out.println("  Skip: " + stepExecution.getSkipCount());
                System.out.println("  Status: " + stepExecution.getStatus());
            }
            
            // Print failures if any
            if (jobExecution.getStatus() == BatchStatus.FAILED) {
                System.out.println("\n--- Failures ---");
                for (Throwable throwable : jobExecution.getAllFailureExceptions()) {
                    System.out.println("Error: " + throwable.getMessage());
                }
            }
            
            System.out.println("========================================\n");
        }
    }

    // ========== StepExecutionListener Implementations ==========

    /**
     * Custom StepExecutionListener
     * Monitors step execution and collects statistics
     */
    public static class CustomStepExecutionListener implements StepExecutionListener {

        @Override
        public void beforeStep(StepExecution stepExecution) {
            String stepName = stepExecution.getStepName();
            System.out.println("\n--- STEP STARTED: " + stepName + " ---");
            System.out.println("Step Execution ID: " + stepExecution.getId());
            System.out.println("Start Time: " + stepExecution.getStartTime());
            
            // Initialize custom metrics in execution context
            ExecutionContext context = stepExecution.getExecutionContext();
            context.put("step.start.time", System.currentTimeMillis());
            context.put("step.custom.metric", 0);
        }

        @Override
        public ExitStatus afterStep(StepExecution stepExecution) {
            String stepName = stepExecution.getStepName();
            ExecutionContext context = stepExecution.getExecutionContext();
            
            Long startTime = (Long) context.get("step.start.time");
            long duration = System.currentTimeMillis() - startTime;
            
            System.out.println("\n--- STEP COMPLETED: " + stepName + " ---");
            System.out.println("Status: " + stepExecution.getStatus());
            System.out.println("Exit Code: " + stepExecution.getExitStatus().getExitCode());
            System.out.println("Duration: " + duration + " ms");
            System.out.println("Read Count: " + stepExecution.getReadCount());
            System.out.println("Write Count: " + stepExecution.getWriteCount());
            System.out.println("Filter Count: " + stepExecution.getFilterCount());
            System.out.println("Commit Count: " + stepExecution.getCommitCount());
            System.out.println("Rollback Count: " + stepExecution.getRollbackCount());
            System.out.println("Skip Count: " + stepExecution.getSkipCount());
            
            // Can modify exit status
            if (stepExecution.getReadCount() == 0) {
                System.out.println("⚠ Warning: No items were read!");
                return new ExitStatus("COMPLETED_WITH_WARNINGS");
            }
            
            return stepExecution.getExitStatus();
        }
    }

    // ========== Item-Level Listeners ==========

    /**
     * ItemReadListener
     * Monitors read operations
     */
    public static class CustomItemReadListener implements ItemReadListener<Order> {

        @Override
        public void beforeRead() {
            // Called before each read
            // System.out.println("  [LISTENER] Before read");
        }

        @Override
        public void afterRead(Order item) {
            System.out.println("  [LISTENER] After read: " + item.getId());
        }

        @Override
        public void onReadError(Exception ex) {
            System.err.println("  [LISTENER] Read error: " + ex.getMessage());
        }
    }

    /**
     * ItemProcessListener
     * Monitors process operations
     */
    public static class CustomItemProcessListener implements ItemProcessListener<Order, Order> {

        @Override
        public void beforeProcess(Order item) {
            System.out.println("  [LISTENER] Before process: " + item.getId());
        }

        @Override
        public void afterProcess(Order item, Order result) {
            if (result == null) {
                System.out.println("  [LISTENER] Item filtered: " + item.getId());
            } else {
                System.out.println("  [LISTENER] After process: " + result.getId());
            }
        }

        @Override
        public void onProcessError(Order item, Exception e) {
            System.err.println("  [LISTENER] Process error for " + item.getId() + ": " + e.getMessage());
        }
    }

    /**
     * ItemWriteListener
     * Monitors write operations
     */
    public static class CustomItemWriteListener implements ItemWriteListener<Order> {

        @Override
        public void beforeWrite(Chunk<? extends Order> items) {
            System.out.println("  [LISTENER] Before write: " + items.size() + " items");
        }

        @Override
        public void afterWrite(Chunk<? extends Order> items) {
            System.out.println("  [LISTENER] After write: " + items.size() + " items written");
        }

        @Override
        public void onWriteError(Exception exception, Chunk<? extends Order> items) {
            System.err.println("  [LISTENER] Write error for " + items.size() + " items: " + 
                             exception.getMessage());
        }
    }

    /**
     * ChunkListener
     * Monitors chunk processing
     */
    public static class CustomChunkListener implements ChunkListener {

        @Override
        public void beforeChunk(ChunkContext context) {
            System.out.println("\n[CHUNK] Starting new chunk");
        }

        @Override
        public void afterChunk(ChunkContext context) {
            System.out.println("[CHUNK] Chunk completed successfully\n");
        }

        @Override
        public void afterChunkError(ChunkContext context) {
            System.err.println("[CHUNK] Chunk failed with error\n");
        }
    }

    // ========== Configuration ==========

    @Configuration
    @EnableBatchProcessing
    public static class ListenerConfiguration {

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

        @Bean
        public ItemReader<Order> orderReader() {
            return new ItemReader<Order>() {
                private final List<Order> orders = Arrays.asList(
                    new Order(1L, "John Doe", 100.0, "NEW"),
                    new Order(2L, "Jane Smith", 250.0, "NEW"),
                    new Order(3L, "Bob Johnson", 175.0, "NEW"),
                    new Order(4L, "Alice Williams", 300.0, "NEW"),
                    new Order(5L, "Charlie Brown", 125.0, "NEW")
                );
                private int index = 0;

                @Override
                public Order read() {
                    if (index < orders.size()) {
                        return orders.get(index++);
                    }
                    return null;
                }
            };
        }

        @Bean
        public ItemProcessor<Order, Order> orderProcessor() {
            return order -> {
                // Simple processing: mark as processed
                order.setStatus("PROCESSED");
                return order;
            };
        }

        @Bean
        public ItemWriter<Order> orderWriter() {
            return chunk -> {
                for (Order order : chunk) {
                    System.out.println("    Writing: " + order);
                }
            };
        }

        @Bean
        public Job listenerJob(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("listenerJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .listener(new CustomJobExecutionListener())
                    .start(new org.springframework.batch.core.step.builder.StepBuilder("listenerStep", jobRepository)
                            .<Order, Order>chunk(2, transactionManager)
                            .reader(orderReader())
                            .processor(orderProcessor())
                            .writer(orderWriter())
                            .listener((StepExecutionListener) new CustomStepExecutionListener())
                            .listener((ItemReadListener) new CustomItemReadListener())
                            .listener((ItemProcessListener) new CustomItemProcessListener())
                            .listener((ItemWriteListener) new CustomItemWriteListener())
                            .listener((ChunkListener) new CustomChunkListener())
                            .build())
                    .build();
        }
    }

    /**
     * Main demonstration
     */
    public static void main(String[] args) {
        System.out.println("=== Job and Step Execution Listener Pattern ===\n");

        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(ListenerConfiguration.class);

        JobLauncher jobLauncher = context.getBean(JobLauncher.class);

        try {
            Job job = context.getBean("listenerJob", Job.class);
            JobParameters params = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .toJobParameters();
            
            jobLauncher.run(job, params);

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
 * 1. Listener Types:
 *    - JobExecutionListener: Job lifecycle
 *    - StepExecutionListener: Step lifecycle
 *    - ItemReadListener: Read operations
 *    - ItemProcessListener: Process operations
 *    - ItemWriteListener: Write operations
 *    - ChunkListener: Chunk processing
 *    - SkipListener: Skip events
 *    - RetryListener: Retry events
 * 
 * 2. Execution Order:
 *    - beforeJob
 *      - beforeStep
 *        - beforeChunk
 *          - beforeRead -> afterRead
 *          - beforeProcess -> afterProcess
 *          - beforeWrite -> afterWrite
 *        - afterChunk
 *      - afterStep
 *    - afterJob
 * 
 * 3. Use Cases:
 *    - Logging and auditing
 *    - Metrics collection
 *    - Performance monitoring
 *    - Resource management
 *    - Notifications
 *    - Error handling
 *    - Custom exit codes
 * 
 * 4. Job Execution Listener:
 *    - Initialize resources (beforeJob)
 *    - Cleanup resources (afterJob)
 *    - Send notifications
 *    - Collect job-level metrics
 *    - Always called even on failure
 * 
 * 5. Step Execution Listener:
 *    - Step-specific initialization
 *    - Step-specific cleanup
 *    - Modify exit status
 *    - Store step context
 *    - Always called even on failure
 * 
 * 6. Item Listeners:
 *    - Fine-grained monitoring
 *    - Performance profiling
 *    - Error tracking
 *    - Custom logging
 *    - Called for each item
 * 
 * 7. Chunk Listener:
 *    - Track transaction boundaries
 *    - Monitor chunk commits
 *    - Debug transaction issues
 *    - Performance analysis
 * 
 * 8. Best Practices:
 *    - Keep listener logic lightweight
 *    - Don't throw exceptions in afterJob/afterStep
 *    - Use for cross-cutting concerns
 *    - Avoid business logic in listeners
 *    - Use execution context for state
 *    - Log meaningful information
 * 
 * 9. Performance Impact:
 *    - Item listeners called frequently
 *    - Avoid heavy operations
 *    - Use conditionally (debug mode)
 *    - Batch logging operations
 * 
 * 10. Testing:
 *     - Easy to unit test
 *     - Verify listener called
 *     - Test error scenarios
 *     - Check metrics collection
 */
