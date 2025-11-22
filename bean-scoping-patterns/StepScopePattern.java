package com.spring.patterns.scope;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Step Scope Pattern (Spring Batch)
 * 
 * Step scope creates bean instances bound to the execution context of a batch step.
 * Beans are created when step starts and destroyed when step completes.
 * 
 * Characteristics:
 * - One instance per step execution
 * - Late binding of step execution context
 * - Access to step execution parameters
 * - Lifecycle tied to step
 * - Allows parameter injection via SpEL
 * 
 * Use Cases:
 * - Parameterized ItemReaders
 * - Parameterized ItemProcessors
 * - Parameterized ItemWriters
 * - Step-specific configuration
 * - Late-bound step parameters
 * 
 * Key Features:
 * - @Value with SpEL expressions
 * - Access to JobParameters and ExecutionContext
 * - Thread-safe for multi-threaded steps
 */
@SpringBootApplication
@EnableBatchProcessing
public class StepScopePattern {

    public static void main(String[] args) {
        SpringApplication.run(StepScopePattern.class, args);
        System.out.println("\n=== Step Scope Pattern Started ===");
        System.out.println("Run batch job: POST http://localhost:8080/api/batch/run");
    }
}

/**
 * Step-scoped configuration
 */
@Configuration
class StepScopeJobConfig {
    
    /**
     * Step-scoped ItemReader with late-bound parameters
     * Parameters are injected from JobParameters via SpEL
     */
    @Bean
    @StepScope
    public ItemReader<String> stepScopedReader(
            @Value("#{jobParameters['inputFile']}") String inputFile,
            @Value("#{jobParameters['batchSize']}") Integer batchSize,
            @Value("#{stepExecution}") StepExecution stepExecution) {
        
        System.out.println("Creating StepScopedReader:");
        System.out.println("  Input file: " + inputFile);
        System.out.println("  Batch size: " + batchSize);
        System.out.println("  Step: " + stepExecution.getStepName());
        
        return new StepScopedReader(inputFile, batchSize, stepExecution);
    }
    
    /**
     * Step-scoped ItemProcessor
     */
    @Bean
    @StepScope
    public ItemProcessor<String, String> stepScopedProcessor(
            @Value("#{jobParameters['processingMode']}") String mode,
            @Value("#{stepExecutionContext['stepId']}") String stepId) {
        
        System.out.println("Creating StepScopedProcessor:");
        System.out.println("  Mode: " + mode);
        System.out.println("  Step ID: " + stepId);
        
        return new StepScopedProcessor(mode);
    }
    
    /**
     * Step-scoped ItemWriter
     */
    @Bean
    @StepScope
    public ItemWriter<String> stepScopedWriter(
            @Value("#{jobParameters['outputFile']}") String outputFile,
            @Value("#{stepExecution.jobExecution.id}") Long jobExecutionId) {
        
        System.out.println("Creating StepScopedWriter:");
        System.out.println("  Output file: " + outputFile);
        System.out.println("  Job Execution ID: " + jobExecutionId);
        
        return new StepScopedWriter(outputFile);
    }
    
    /**
     * Define batch job with step-scoped beans
     */
    @Bean
    public Job stepScopeJob(JobBuilderFactory jobBuilderFactory,
                           StepBuilderFactory stepBuilderFactory,
                           ItemReader<String> reader,
                           ItemProcessor<String, String> processor,
                           ItemWriter<String> writer) {
        
        Step step = stepBuilderFactory.get("stepScopeStep")
            .<String, String>chunk(10)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
        
        return jobBuilderFactory.get("stepScopeJob")
            .start(step)
            .build();
    }
}

/**
 * Step-scoped ItemReader implementation
 */
class StepScopedReader implements ItemReader<String> {
    private final String inputFile;
    private final Integer batchSize;
    private final StepExecution stepExecution;
    private final List<String> data = new ArrayList<>();
    private int currentIndex = 0;
    
    public StepScopedReader(String inputFile, Integer batchSize, StepExecution stepExecution) {
        this.inputFile = inputFile;
        this.batchSize = batchSize;
        this.stepExecution = stepExecution;
        
        // Simulate loading data
        for (int i = 0; i < batchSize; i++) {
            data.add("Item-" + i + "-from-" + inputFile);
        }
        
        // Update step execution context
        stepExecution.getExecutionContext().put("totalItems", data.size());
        stepExecution.getExecutionContext().put("inputFile", inputFile);
    }
    
    @Override
    public String read() throws Exception {
        if (currentIndex < data.size()) {
            String item = data.get(currentIndex++);
            stepExecution.getExecutionContext().put("lastReadItem", item);
            return item;
        }
        return null; // End of data
    }
}

/**
 * Step-scoped ItemProcessor implementation
 */
class StepScopedProcessor implements ItemProcessor<String, String> {
    private final String processingMode;
    private int processedCount = 0;
    
    public StepScopedProcessor(String processingMode) {
        this.processingMode = processingMode;
    }
    
    @Override
    public String process(String item) throws Exception {
        processedCount++;
        String result = "UPPERCASE".equalsIgnoreCase(processingMode) ? 
                       item.toUpperCase() : 
                       item.toLowerCase();
        
        System.out.println("Processing [" + processingMode + "]: " + item + " -> " + result);
        return result;
    }
    
    public int getProcessedCount() {
        return processedCount;
    }
}

/**
 * Step-scoped ItemWriter implementation
 */
class StepScopedWriter implements ItemWriter<String> {
    private final String outputFile;
    private int writtenCount = 0;
    private final List<String> writtenItems = new ArrayList<>();
    
    public StepScopedWriter(String outputFile) {
        this.outputFile = outputFile;
    }
    
    @Override
    public void write(List<? extends String> items) throws Exception {
        writtenItems.addAll(items);
        writtenCount += items.size();
        System.out.println("Writing " + items.size() + " items to " + outputFile);
        items.forEach(item -> System.out.println("  - " + item));
    }
    
    public int getWrittenCount() {
        return writtenCount;
    }
}

/**
 * Step-scoped configuration bean
 */
@Bean
@StepScope
class StepConfiguration {
    private final String stepName;
    private final LocalDateTime createdAt;
    
    public StepConfiguration(@Value("#{stepExecution.stepName}") String stepName) {
        this.stepName = stepName;
        this.createdAt = LocalDateTime.now();
        System.out.println("StepConfiguration created for step: " + stepName);
    }
    
    public String getStepName() { return stepName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

/**
 * REST Controller for batch job execution
 */
@RestController
@RequestMapping("/api/batch")
class BatchController {
    
    private final JobLauncher jobLauncher;
    private final Job stepScopeJob;
    
    public BatchController(JobLauncher jobLauncher, Job stepScopeJob) {
        this.jobLauncher = jobLauncher;
        this.stepScopeJob = stepScopeJob;
    }
    
    @PostMapping("/run")
    public String runJob(@RequestParam(defaultValue = "input.txt") String inputFile,
                        @RequestParam(defaultValue = "output.txt") String outputFile,
                        @RequestParam(defaultValue = "20") Integer batchSize,
                        @RequestParam(defaultValue = "UPPERCASE") String processingMode) {
        
        try {
            JobParameters params = new JobParametersBuilder()
                .addString("inputFile", inputFile)
                .addString("outputFile", outputFile)
                .addLong("batchSize", batchSize.longValue())
                .addString("processingMode", processingMode)
                .addLong("timestamp", System.currentTimeMillis()) // Make unique
                .toJobParameters();
            
            JobExecution execution = jobLauncher.run(stepScopeJob, params);
            
            return "Job executed:\n" +
                   "  Job ID: " + execution.getJobId() + "\n" +
                   "  Status: " + execution.getStatus() + "\n" +
                   "  Exit status: " + execution.getExitStatus().getExitCode() + "\n" +
                   "  Parameters:\n" +
                   "    - inputFile: " + inputFile + "\n" +
                   "    - outputFile: " + outputFile + "\n" +
                   "    - batchSize: " + batchSize + "\n" +
                   "    - processingMode: " + processingMode;
            
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

/**
 * Key Points:
 * 
 * 1. Step Scope Lifecycle:
 *    - Created when step starts
 *    - Destroyed when step completes
 *    - One instance per step execution
 *    - New instance for step restart
 * 
 * 2. Late Binding:
 *    - Parameters resolved at step execution time
 *    - Access to JobParameters via #{jobParameters['key']}
 *    - Access to StepExecution via #{stepExecution}
 *    - Access to ExecutionContext via #{stepExecutionContext['key']}
 * 
 * 3. SpEL Expressions:
 *    - #{jobParameters['paramName']} - job parameters
 *    - #{stepExecution} - step execution object
 *    - #{stepExecutionContext['key']} - step context
 *    - #{jobExecution} - job execution object
 *    - #{jobExecutionContext['key']} - job context
 * 
 * 4. Thread Safety:
 *    - Safe for multi-threaded steps
 *    - Each thread gets step execution context
 *    - Partition steps get separate instances
 * 
 * 5. Use Cases:
 *    ✓ Parameterized batch components
 *    ✓ File-based processing with dynamic paths
 *    ✓ Step-specific configuration
 *    ✓ Restart-aware processing
 *    ✓ Multi-threaded step processing
 * 
 * 6. Best Practices:
 *    - Use for components needing job parameters
 *    - Avoid heavy initialization
 *    - Store state in ExecutionContext
 *    - Test with different parameter combinations
 * 
 * 7. Common Patterns:
 *    - Dynamic file paths
 *    - Configurable batch sizes
 *    - Environment-specific settings
 *    - Restart handling
 */
