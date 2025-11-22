package com.spring.patterns.scope;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Job Scope Pattern (Spring Batch)
 * 
 * Job scope creates bean instances bound to the execution context of a batch job.
 * Beans are created when job starts and destroyed when job completes.
 * 
 * Characteristics:
 * - One instance per job execution
 * - Late binding of job execution context
 * - Access to job execution parameters
 * - Lifecycle tied to job
 * - Shared across all steps in job
 * 
 * Difference from Step Scope:
 * - Step Scope: One per step execution
 * - Job Scope: One per job execution (shared across steps)
 * 
 * Use Cases:
 * - Job-level configuration
 * - Shared state across steps
 * - Job-wide resources
 * - Job execution tracking
 * - Cross-step data sharing
 */
@SpringBootApplication
@EnableBatchProcessing
public class JobScopePattern {

    public static void main(String[] args) {
        SpringApplication.run(JobScopePattern.class, args);
        System.out.println("\n=== Job Scope Pattern Started ===");
        System.out.println("Run job: POST http://localhost:8080/api/job/execute");
    }
}

/**
 * Job-scoped configuration
 */
@Configuration
class JobScopeJobConfig {
    
    /**
     * Job-scoped bean with late-bound job parameters
     */
    @Bean
    @JobScope
    public JobConfiguration jobConfiguration(
            @Value("#{jobParameters['environment']}") String environment,
            @Value("#{jobParameters['runMode']}") String runMode,
            @Value("#{jobExecution}") JobExecution jobExecution) {
        
        System.out.println("Creating JobConfiguration:");
        System.out.println("  Environment: " + environment);
        System.out.println("  Run mode: " + runMode);
        System.out.println("  Job: " + jobExecution.getJobInstance().getJobName());
        
        return new JobConfiguration(environment, runMode, jobExecution);
    }
    
    /**
     * Job-scoped data collector (shared across steps)
     */
    @Bean
    @JobScope
    public JobDataCollector jobDataCollector(
            @Value("#{jobParameters['jobId']}") String jobId) {
        
        System.out.println("Creating JobDataCollector for job: " + jobId);
        return new JobDataCollector(jobId);
    }
    
    /**
     * Job-scoped metrics collector
     */
    @Bean
    @JobScope
    public JobMetrics jobMetrics(
            @Value("#{jobExecution.jobId}") Long jobId,
            @Value("#{jobExecution.jobInstance.jobName}") String jobName) {
        
        System.out.println("Creating JobMetrics for job: " + jobName + " (ID: " + jobId + ")");
        return new JobMetrics(jobId, jobName);
    }
    
    /**
     * Define batch job using job-scoped beans
     */
    @Bean
    public Job multiStepJob(JobBuilderFactory jobBuilderFactory,
                           StepBuilderFactory stepBuilderFactory,
                           JobConfiguration jobConfig,
                           JobDataCollector dataCollector,
                           JobMetrics metrics) {
        
        // Step 1: Initialize
        Step initStep = stepBuilderFactory.get("initStep")
            .tasklet((contribution, chunkContext) -> {
                System.out.println("Step 1: Initializing with config: " + 
                                 jobConfig.getEnvironment());
                dataCollector.addData("step1", "Initialized");
                metrics.recordStepStart("initStep");
                
                Thread.sleep(100); // Simulate work
                
                metrics.recordStepComplete("initStep");
                return RepeatStatus.FINISHED;
            })
            .build();
        
        // Step 2: Process
        Step processStep = stepBuilderFactory.get("processStep")
            .tasklet((contribution, chunkContext) -> {
                System.out.println("Step 2: Processing in mode: " + 
                                 jobConfig.getRunMode());
                dataCollector.addData("step2", "Processed 100 items");
                metrics.recordStepStart("processStep");
                metrics.incrementItemsProcessed(100);
                
                Thread.sleep(200); // Simulate work
                
                metrics.recordStepComplete("processStep");
                return RepeatStatus.FINISHED;
            })
            .build();
        
        // Step 3: Finalize
        Step finalizeStep = stepBuilderFactory.get("finalizeStep")
            .tasklet((contribution, chunkContext) -> {
                System.out.println("Step 3: Finalizing");
                System.out.println("Collected data: " + dataCollector.getAllData());
                System.out.println("Metrics: " + metrics.getSummary());
                
                dataCollector.addData("step3", "Finalized");
                metrics.recordStepStart("finalizeStep");
                
                Thread.sleep(100); // Simulate work
                
                metrics.recordStepComplete("finalizeStep");
                return RepeatStatus.FINISHED;
            })
            .build();
        
        return jobBuilderFactory.get("multiStepJob")
            .listener(new JobExecutionListener(metrics))
            .start(initStep)
            .next(processStep)
            .next(finalizeStep)
            .build();
    }
}

/**
 * Job-scoped configuration bean
 */
class JobConfiguration {
    private final String environment;
    private final String runMode;
    private final Long jobExecutionId;
    private final String jobName;
    private final LocalDateTime createdAt;
    private final Map<String, String> properties = new HashMap<>();
    
    public JobConfiguration(String environment, String runMode, JobExecution jobExecution) {
        this.environment = environment;
        this.runMode = runMode;
        this.jobExecutionId = jobExecution.getId();
        this.jobName = jobExecution.getJobInstance().getJobName();
        this.createdAt = LocalDateTime.now();
        
        // Initialize environment-specific properties
        initializeProperties();
    }
    
    private void initializeProperties() {
        if ("production".equalsIgnoreCase(environment)) {
            properties.put("timeout", "60000");
            properties.put("retryCount", "3");
            properties.put("batchSize", "1000");
        } else {
            properties.put("timeout", "30000");
            properties.put("retryCount", "1");
            properties.put("batchSize", "100");
        }
    }
    
    public String getEnvironment() { return environment; }
    public String getRunMode() { return runMode; }
    public Long getJobExecutionId() { return jobExecutionId; }
    public String getJobName() { return jobName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getProperty(String key) { return properties.get(key); }
    public Map<String, String> getAllProperties() { return new HashMap<>(properties); }
}

/**
 * Job-scoped data collector (shared across steps)
 */
class JobDataCollector {
    private final String jobId;
    private final Map<String, List<String>> stepData = new ConcurrentHashMap<>();
    private final LocalDateTime createdAt;
    
    public JobDataCollector(String jobId) {
        this.jobId = jobId;
        this.createdAt = LocalDateTime.now();
    }
    
    public void addData(String stepName, String data) {
        stepData.computeIfAbsent(stepName, k -> new ArrayList<>()).add(data);
        System.out.println("Data collected from " + stepName + ": " + data);
    }
    
    public List<String> getStepData(String stepName) {
        return stepData.getOrDefault(stepName, new ArrayList<>());
    }
    
    public Map<String, List<String>> getAllData() {
        return new HashMap<>(stepData);
    }
    
    public String getJobId() { return jobId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

/**
 * Job-scoped metrics collector
 */
class JobMetrics {
    private final Long jobId;
    private final String jobName;
    private final LocalDateTime startTime;
    private final Map<String, LocalDateTime> stepStartTimes = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> stepEndTimes = new ConcurrentHashMap<>();
    private long itemsProcessed = 0;
    private long itemsFailed = 0;
    
    public JobMetrics(Long jobId, String jobName) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.startTime = LocalDateTime.now();
    }
    
    public void recordStepStart(String stepName) {
        stepStartTimes.put(stepName, LocalDateTime.now());
        System.out.println("Step '" + stepName + "' started");
    }
    
    public void recordStepComplete(String stepName) {
        stepEndTimes.put(stepName, LocalDateTime.now());
        
        if (stepStartTimes.containsKey(stepName)) {
            long duration = java.time.Duration.between(
                stepStartTimes.get(stepName), 
                stepEndTimes.get(stepName)
            ).toMillis();
            System.out.println("Step '" + stepName + "' completed in " + duration + "ms");
        }
    }
    
    public void incrementItemsProcessed(long count) {
        itemsProcessed += count;
    }
    
    public void incrementItemsFailed(long count) {
        itemsFailed += count;
    }
    
    public String getSummary() {
        long totalDuration = java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
        return String.format(
            "Job: %s (ID: %d), Duration: %dms, Items: %d processed / %d failed, Steps: %d",
            jobName, jobId, totalDuration, itemsProcessed, itemsFailed, stepEndTimes.size()
        );
    }
    
    public Long getJobId() { return jobId; }
    public String getJobName() { return jobName; }
    public LocalDateTime getStartTime() { return startTime; }
    public long getItemsProcessed() { return itemsProcessed; }
    public long getItemsFailed() { return itemsFailed; }
}

/**
 * Job execution listener using job-scoped metrics
 */
class JobExecutionListener extends JobExecutionListenerSupport {
    
    private final JobMetrics metrics;
    
    public JobExecutionListener(JobMetrics metrics) {
        this.metrics = metrics;
    }
    
    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("\n=== Job Starting: " + jobExecution.getJobInstance().getJobName() + " ===");
        System.out.println("Job Execution ID: " + jobExecution.getId());
    }
    
    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("\n=== Job Completed: " + jobExecution.getJobInstance().getJobName() + " ===");
        System.out.println("Status: " + jobExecution.getStatus());
        System.out.println("Exit Code: " + jobExecution.getExitStatus().getExitCode());
        System.out.println(metrics.getSummary());
        System.out.println("===================================\n");
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/job")
class JobController {
    
    private final JobLauncher jobLauncher;
    private final Job multiStepJob;
    
    public JobController(JobLauncher jobLauncher, Job multiStepJob) {
        this.jobLauncher = jobLauncher;
        this.multiStepJob = multiStepJob;
    }
    
    @PostMapping("/execute")
    public String executeJob(
            @RequestParam(defaultValue = "development") String environment,
            @RequestParam(defaultValue = "full") String runMode,
            @RequestParam(defaultValue = "JOB-001") String jobId) {
        
        try {
            JobParameters params = new JobParametersBuilder()
                .addString("environment", environment)
                .addString("runMode", runMode)
                .addString("jobId", jobId)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
            
            JobExecution execution = jobLauncher.run(multiStepJob, params);
            
            return "Job executed:\n" +
                   "  Job ID: " + execution.getJobId() + "\n" +
                   "  Job Name: " + execution.getJobInstance().getJobName() + "\n" +
                   "  Status: " + execution.getStatus() + "\n" +
                   "  Exit Code: " + execution.getExitStatus().getExitCode() + "\n" +
                   "  Parameters:\n" +
                   "    - environment: " + environment + "\n" +
                   "    - runMode: " + runMode + "\n" +
                   "    - jobId: " + jobId;
            
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

/**
 * Key Points:
 * 
 * 1. Job Scope Lifecycle:
 *    - Created when job starts
 *    - Destroyed when job completes
 *    - Shared across all steps in job
 *    - One instance per job execution
 * 
 * 2. Scope Comparison:
 *    - Singleton: Application-wide
 *    - Job Scope: Per job execution
 *    - Step Scope: Per step execution
 *    - Request/Session: Web-specific
 * 
 * 3. Late Binding:
 *    - #{jobParameters['key']} - job parameters
 *    - #{jobExecution} - job execution object
 *    - #{jobExecutionContext['key']} - job context
 *    - #{jobExecution.jobId} - job ID
 *    - #{jobExecution.jobInstance.jobName} - job name
 * 
 * 4. Cross-Step Sharing:
 *    - Share data between steps
 *    - Aggregate metrics across steps
 *    - Maintain job-level state
 *    - Coordinate step execution
 * 
 * 5. Use Cases:
 *    ✓ Job-level configuration
 *    ✓ Cross-step data collection
 *    ✓ Job-wide metrics
 *    ✓ Shared resources
 *    ✓ Job execution tracking
 * 
 * 6. Best Practices:
 *    - Use for job-wide state
 *    - Thread-safe if using parallel steps
 *    - Clean up in @PreDestroy
 *    - Store state in ExecutionContext for restart
 * 
 * 7. When to Use Job vs Step Scope:
 *    - Job Scope: Share across steps, job-level config
 *    - Step Scope: Step-specific, parameterized components
 */
