package com.spring.patterns.batch;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Date;

/**
 * Job Pattern - Spring Batch Processing
 * 
 * The Job Pattern represents a batch process that encapsulates an entire batch process.
 * A Job is composed of one or more Steps and defines the execution flow.
 * 
 * Key Concepts:
 * 1. Simple Job - Sequential step execution
 * 2. Flow Job - Conditional execution with decision logic
 * 3. Job Parameters - Runtime parameters for job execution
 * 4. Job Instance - Unique execution context
 * 5. Job Execution - Physical execution of a job instance
 * 
 * Types of Jobs:
 * - SimpleJob: Sequential step execution
 * - FlowJob: Complex flows with conditional logic
 * - Partitioned Job: Parallel processing
 * 
 * Use Cases:
 * - Data migration and ETL processes
 * - Report generation
 * - Batch data processing
 * - File processing workflows
 * - Scheduled data synchronization
 */
public class JobPattern {

    /**
     * Configuration for Spring Batch Jobs
     */
    @Configuration
    @EnableBatchProcessing
    public static class BatchJobConfiguration {

        // Note: In a real application, use a proper DataSource
        // This example uses an in-memory repository for demonstration

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

        /**
         * Simple Job - Sequential Execution
         * Executes steps in order: step1 -> step2 -> step3
         */
        @Bean
        public Job simpleSequentialJob(JobRepository jobRepository, 
                                       PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("simpleSequentialJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(createSimpleStep("step1", jobRepository, transactionManager))
                    .next(createSimpleStep("step2", jobRepository, transactionManager))
                    .next(createSimpleStep("step3", jobRepository, transactionManager))
                    .build();
        }

        /**
         * Flow Job with Conditional Execution
         * Demonstrates conditional execution based on step status
         */
        @Bean
        public Job conditionalFlowJob(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("conditionalFlowJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(createSimpleStep("initStep", jobRepository, transactionManager))
                    .on("COMPLETED").to(createSimpleStep("successStep", jobRepository, transactionManager))
                    .from(createSimpleStep("initStep", jobRepository, transactionManager))
                    .on("FAILED").to(createSimpleStep("errorStep", jobRepository, transactionManager))
                    .end()
                    .build();
        }

        /**
         * Job with Decision Logic
         * Uses JobExecutionDecider for runtime decisions
         */
        @Bean
        public Job decisionBasedJob(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("decisionBasedJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(createSimpleStep("startStep", jobRepository, transactionManager))
                    .next(businessDecider())
                    .on("PROCESS_A").to(createSimpleStep("processAStep", jobRepository, transactionManager))
                    .from(businessDecider())
                    .on("PROCESS_B").to(createSimpleStep("processBStep", jobRepository, transactionManager))
                    .from(businessDecider())
                    .on("*").to(createSimpleStep("defaultStep", jobRepository, transactionManager))
                    .end()
                    .build();
        }

        /**
         * Split Flow Job - Parallel Execution
         * Demonstrates parallel execution of flows
         */
        @Bean
        public Job splitFlowJob(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager) {
            Flow flow1 = new FlowBuilder<Flow>("flow1")
                    .start(createSimpleStep("flow1Step1", jobRepository, transactionManager))
                    .next(createSimpleStep("flow1Step2", jobRepository, transactionManager))
                    .build();

            Flow flow2 = new FlowBuilder<Flow>("flow2")
                    .start(createSimpleStep("flow2Step1", jobRepository, transactionManager))
                    .next(createSimpleStep("flow2Step2", jobRepository, transactionManager))
                    .build();

            return new org.springframework.batch.core.job.builder.JobBuilder("splitFlowJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(flow1)
                    .split(new org.springframework.core.task.SimpleAsyncTaskExecutor())
                    .add(flow2)
                    .end()
                    .build();
        }

        /**
         * Job with Parameters Validator
         */
        @Bean
        public Job validatedParameterJob(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("validatedParameterJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .validator(new JobParametersValidator() {
                        @Override
                        public void validate(JobParameters parameters) throws JobParametersInvalidException {
                            if (!parameters.getParameters().containsKey("inputFile")) {
                                throw new JobParametersInvalidException("inputFile parameter is required!");
                            }
                        }
                    })
                    .start(createSimpleStep("validateStep", jobRepository, transactionManager))
                    .build();
        }

        /**
         * Preventable/Restartable Job Configuration
         */
        @Bean
        public Job restartableJob(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("restartableJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .preventRestart() // or .allowStartIfComplete(true)
                    .start(createSimpleStep("processStep", jobRepository, transactionManager))
                    .build();
        }

        /**
         * Helper method to create simple steps
         */
        private Step createSimpleStep(String stepName, JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.step.builder.StepBuilder(stepName, jobRepository)
                    .tasklet((contribution, chunkContext) -> {
                        System.out.println("Executing step: " + stepName);
                        System.out.println("  Job Name: " + chunkContext.getStepContext().getJobName());
                        System.out.println("  Step Name: " + chunkContext.getStepContext().getStepName());
                        
                        // Simulate some processing
                        Thread.sleep(100);
                        
                        return RepeatStatus.FINISHED;
                    }, transactionManager)
                    .build();
        }

        /**
         * Business logic decider
         */
        private JobExecutionDecider businessDecider() {
            return (jobExecution, stepExecution) -> {
                // Business logic to determine flow
                int randomValue = (int) (Math.random() * 3);
                
                if (randomValue == 0) {
                    System.out.println("Decider chose: PROCESS_A");
                    return new FlowExecutionStatus("PROCESS_A");
                } else if (randomValue == 1) {
                    System.out.println("Decider chose: PROCESS_B");
                    return new FlowExecutionStatus("PROCESS_B");
                } else {
                    System.out.println("Decider chose: DEFAULT");
                    return new FlowExecutionStatus("DEFAULT");
                }
            };
        }
    }

    /**
     * Job Execution Monitor
     */
    public static class JobExecutionMonitor {
        
        public static void printJobExecutionDetails(JobExecution execution) {
            System.out.println("\n========== Job Execution Details ==========");
            System.out.println("Job Name: " + execution.getJobInstance().getJobName());
            System.out.println("Job Instance ID: " + execution.getJobInstance().getId());
            System.out.println("Job Execution ID: " + execution.getId());
            System.out.println("Status: " + execution.getStatus());
            System.out.println("Exit Status: " + execution.getExitStatus().getExitCode());
            System.out.println("Start Time: " + execution.getStartTime());
            System.out.println("End Time: " + execution.getEndTime());
            
            if (execution.getEndTime() != null && execution.getStartTime() != null) {
                long duration = execution.getEndTime().getTime() - execution.getStartTime().getTime();
                System.out.println("Duration: " + duration + " ms");
            }
            
            System.out.println("\nStep Executions:");
            for (StepExecution stepExecution : execution.getStepExecutions()) {
                System.out.println("  - " + stepExecution.getStepName() + 
                                 " [Status: " + stepExecution.getStatus() + 
                                 ", Exit: " + stepExecution.getExitStatus().getExitCode() + "]");
            }
            
            if (!execution.getAllFailureExceptions().isEmpty()) {
                System.out.println("\nFailures:");
                for (Throwable throwable : execution.getAllFailureExceptions()) {
                    System.out.println("  - " + throwable.getMessage());
                }
            }
            System.out.println("===========================================\n");
        }
    }

    /**
     * Demonstration of different Job patterns
     */
    public static void main(String[] args) {
        System.out.println("=== Spring Batch Job Pattern Demonstration ===\n");

        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(BatchJobConfiguration.class);

        JobLauncher jobLauncher = context.getBean(JobLauncher.class);

        try {
            // Demo 1: Simple Sequential Job
            System.out.println("\n--- Demo 1: Simple Sequential Job ---");
            Job simpleJob = context.getBean("simpleSequentialJob", Job.class);
            JobParameters jobParams1 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .addString("jobType", "sequential")
                    .toJobParameters();
            JobExecution execution1 = jobLauncher.run(simpleJob, jobParams1);
            JobExecutionMonitor.printJobExecutionDetails(execution1);

            // Demo 2: Conditional Flow Job
            System.out.println("\n--- Demo 2: Conditional Flow Job ---");
            Job conditionalJob = context.getBean("conditionalFlowJob", Job.class);
            JobParameters jobParams2 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .addString("jobType", "conditional")
                    .toJobParameters();
            JobExecution execution2 = jobLauncher.run(conditionalJob, jobParams2);
            JobExecutionMonitor.printJobExecutionDetails(execution2);

            // Demo 3: Decision Based Job
            System.out.println("\n--- Demo 3: Decision Based Job ---");
            Job decisionJob = context.getBean("decisionBasedJob", Job.class);
            JobParameters jobParams3 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .addString("jobType", "decision")
                    .toJobParameters();
            JobExecution execution3 = jobLauncher.run(decisionJob, jobParams3);
            JobExecutionMonitor.printJobExecutionDetails(execution3);

            // Demo 4: Split Flow Job (Parallel)
            System.out.println("\n--- Demo 4: Split Flow Job (Parallel) ---");
            Job splitJob = context.getBean("splitFlowJob", Job.class);
            JobParameters jobParams4 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .addString("jobType", "parallel")
                    .toJobParameters();
            JobExecution execution4 = jobLauncher.run(splitJob, jobParams4);
            Thread.sleep(2000); // Wait for async execution
            JobExecutionMonitor.printJobExecutionDetails(execution4);

            // Demo 5: Validated Parameter Job
            System.out.println("\n--- Demo 5: Validated Parameter Job ---");
            Job validatedJob = context.getBean("validatedParameterJob", Job.class);
            JobParameters jobParams5 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .addString("inputFile", "/path/to/input.csv")
                    .toJobParameters();
            JobExecution execution5 = jobLauncher.run(validatedJob, jobParams5);
            JobExecutionMonitor.printJobExecutionDetails(execution5);

            // Demo 6: Job with missing required parameter (validation failure)
            System.out.println("\n--- Demo 6: Validation Failure Demo ---");
            try {
                JobParameters jobParams6 = new JobParametersBuilder()
                        .addDate("date", new Date())
                        .toJobParameters();
                jobLauncher.run(validatedJob, jobParams6);
            } catch (Exception e) {
                System.out.println("Expected validation error: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Error executing jobs: " + e.getMessage());
            e.printStackTrace();
        } finally {
            context.close();
        }

        System.out.println("\n=== Job Pattern Demonstration Complete ===");
    }
}

/**
 * KEY TAKEAWAYS:
 * 
 * 1. Job Structure:
 *    - Job is composed of one or more steps
 *    - Steps execute sequentially by default
 *    - Flows enable parallel and conditional execution
 * 
 * 2. Job Types:
 *    - SimpleJob: Linear step execution
 *    - FlowJob: Conditional and parallel flows
 *    - Partitioned: Parallel partition processing
 * 
 * 3. Job Configuration:
 *    - incrementer(): Generates unique job parameters
 *    - validator(): Validates job parameters before execution
 *    - preventRestart(): Controls restart behavior
 *    - listener(): Adds job lifecycle listeners
 * 
 * 4. Conditional Execution:
 *    - on().to(): Conditional transitions based on status
 *    - JobExecutionDecider: Custom decision logic
 *    - split(): Parallel flow execution
 * 
 * 5. Job Parameters:
 *    - Unique job instance identification
 *    - Runtime configuration
 *    - Parameter validation
 * 
 * 6. Job Execution:
 *    - JobInstance: Logical run (unique parameters)
 *    - JobExecution: Physical run attempt
 *    - ExecutionContext: Shared state between steps
 * 
 * 7. Best Practices:
 *    - Use meaningful job and step names
 *    - Implement proper parameter validation
 *    - Add job execution listeners for monitoring
 *    - Handle failures with proper exit codes
 *    - Use flow logic for complex workflows
 *    - Configure restart behavior appropriately
 * 
 * 8. Common Use Cases:
 *    - ETL pipelines with multiple stages
 *    - Report generation workflows
 *    - Data migration jobs
 *    - File processing pipelines
 *    - Scheduled batch operations
 */
