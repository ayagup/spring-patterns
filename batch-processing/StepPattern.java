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
import org.springframework.batch.item.*;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Step Pattern - Spring Batch Processing
 * 
 * The Step Pattern represents an independent, sequential phase of a batch job.
 * Each step can be executed independently and contributes to the overall job flow.
 * 
 * Types of Steps:
 * 1. Tasklet Step - Single operation execution
 * 2. Chunk-Oriented Step - Read-Process-Write pattern
 * 3. Partitioned Step - Parallel processing
 * 4. Flow Step - Nested flow execution
 * 
 * Key Components:
 * - ItemReader: Reads data from source
 * - ItemProcessor: Transforms/validates data
 * - ItemWriter: Writes data to destination
 * - Tasklet: Single transaction operation
 * 
 * Step Configuration:
 * - Chunk size
 * - Transaction management
 * - Skip/Retry policies
 * - Listeners
 * 
 * Use Cases:
 * - Data transformation stages
 * - File processing phases
 * - Database migration steps
 * - Report generation stages
 * - Validation and cleanup operations
 */
public class StepPattern {

    /**
     * Domain object for demonstration
     */
    public static class Employee {
        private Long id;
        private String name;
        private String department;
        private Double salary;

        public Employee() {}

        public Employee(Long id, String name, String department, Double salary) {
            this.id = id;
            this.name = name;
            this.department = department;
            this.salary = salary;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Double getSalary() { return salary; }
        public void setSalary(Double salary) { this.salary = salary; }

        @Override
        public String toString() {
            return "Employee{id=" + id + ", name='" + name + "', dept='" + department + 
                   "', salary=" + salary + "}";
        }
    }

    /**
     * Configuration for Spring Batch Steps
     */
    @Configuration
    @EnableBatchProcessing
    public static class BatchStepConfiguration {

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

        // ========== Type 1: Tasklet Step ==========
        
        /**
         * Simple Tasklet Step
         * Executes a single operation in a transaction
         */
        @Bean
        public Step simpleTaskletStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.step.builder.StepBuilder("simpleTaskletStep", jobRepository)
                    .tasklet(new Tasklet() {
                        @Override
                        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
                            System.out.println("Executing simple tasklet step");
                            System.out.println("Step Execution ID: " + 
                                             chunkContext.getStepContext().getStepExecution().getId());
                            // Perform single operation
                            return RepeatStatus.FINISHED;
                        }
                    }, transactionManager)
                    .build();
        }

        /**
         * Tasklet Step with Custom Logic
         */
        @Bean
        public Step customTaskletStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.step.builder.StepBuilder("customTaskletStep", jobRepository)
                    .tasklet(new FileCleanupTasklet(), transactionManager)
                    .build();
        }

        // ========== Type 2: Chunk-Oriented Step ==========
        
        /**
         * Chunk-Oriented Processing Step
         * Read-Process-Write pattern with configurable chunk size
         */
        @Bean
        public Step chunkOrientedStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.step.builder.StepBuilder("chunkOrientedStep", jobRepository)
                    .<Employee, Employee>chunk(3, transactionManager) // Chunk size = 3
                    .reader(employeeReader())
                    .processor(employeeProcessor())
                    .writer(employeeWriter())
                    .build();
        }

        /**
         * Chunk Step with Fault Tolerance
         */
        @Bean
        public Step faultTolerantChunkStep(JobRepository jobRepository,
                                          PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.step.builder.StepBuilder("faultTolerantChunkStep", jobRepository)
                    .<Employee, Employee>chunk(5, transactionManager)
                    .reader(employeeReader())
                    .processor(employeeProcessor())
                    .writer(employeeWriter())
                    .faultTolerant()
                    .skip(Exception.class)
                    .skipLimit(3)
                    .retry(Exception.class)
                    .retryLimit(2)
                    .build();
        }

        /**
         * Chunk Step with Listeners
         */
        @Bean
        public Step listenerEnabledStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.step.builder.StepBuilder("listenerEnabledStep", jobRepository)
                    .<Employee, Employee>chunk(10, transactionManager)
                    .reader(employeeReader())
                    .processor(employeeProcessor())
                    .writer(employeeWriter())
                    .listener(new StepExecutionListener() {
                        @Override
                        public void beforeStep(StepExecution stepExecution) {
                            System.out.println("Before Step: " + stepExecution.getStepName());
                        }

                        @Override
                        public ExitStatus afterStep(StepExecution stepExecution) {
                            System.out.println("After Step: " + stepExecution.getStepName());
                            System.out.println("Read Count: " + stepExecution.getReadCount());
                            System.out.println("Write Count: " + stepExecution.getWriteCount());
                            return stepExecution.getExitStatus();
                        }
                    })
                    .build();
        }

        // ========== Type 3: Flow Step ==========
        
        /**
         * Step that starts another flow
         */
        @Bean
        public Step flowDelegateStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.step.builder.StepBuilder("flowDelegateStep", jobRepository)
                    .tasklet((contribution, chunkContext) -> {
                        System.out.println("Starting flow from step");
                        return RepeatStatus.FINISHED;
                    }, transactionManager)
                    .build();
        }

        // ========== Helper Beans ==========

        @Bean
        public ItemReader<Employee> employeeReader() {
            return new ItemReader<Employee>() {
                private final List<Employee> employees = Arrays.asList(
                    new Employee(1L, "John Doe", "Engineering", 75000.0),
                    new Employee(2L, "Jane Smith", "Marketing", 65000.0),
                    new Employee(3L, "Bob Johnson", "Engineering", 80000.0),
                    new Employee(4L, "Alice Williams", "HR", 60000.0),
                    new Employee(5L, "Charlie Brown", "Engineering", 85000.0),
                    new Employee(6L, "Diana Prince", "Marketing", 70000.0),
                    new Employee(7L, "Eve Davis", "Finance", 90000.0),
                    new Employee(8L, "Frank Miller", "Engineering", 78000.0),
                    new Employee(9L, "Grace Lee", "HR", 62000.0),
                    new Employee(10L, "Henry Wilson", "Finance", 95000.0)
                );
                private int index = 0;

                @Override
                public Employee read() {
                    if (index < employees.size()) {
                        Employee emp = employees.get(index++);
                        System.out.println("Reading: " + emp);
                        return emp;
                    }
                    return null; // End of data
                }
            };
        }

        @Bean
        public ItemProcessor<Employee, Employee> employeeProcessor() {
            return new ItemProcessor<Employee, Employee>() {
                @Override
                public Employee process(Employee employee) throws Exception {
                    // Apply 10% raise to Engineering department
                    if ("Engineering".equals(employee.getDepartment())) {
                        employee.setSalary(employee.getSalary() * 1.10);
                        System.out.println("Processing (with raise): " + employee);
                    } else {
                        System.out.println("Processing: " + employee);
                    }
                    return employee;
                }
            };
        }

        @Bean
        public ItemWriter<Employee> employeeWriter() {
            return new ItemWriter<Employee>() {
                @Override
                public void write(Chunk<? extends Employee> chunk) throws Exception {
                    System.out.println("Writing chunk of " + chunk.size() + " employees:");
                    for (Employee employee : chunk) {
                        System.out.println("  Written: " + employee);
                    }
                }
            };
        }

        // ========== Jobs Demonstrating Different Steps ==========

        @Bean
        public Job taskletStepJob(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("taskletStepJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(simpleTaskletStep(jobRepository, transactionManager))
                    .next(customTaskletStep(jobRepository, transactionManager))
                    .build();
        }

        @Bean
        public Job chunkStepJob(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("chunkStepJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(chunkOrientedStep(jobRepository, transactionManager))
                    .build();
        }

        @Bean
        public Job faultTolerantJob(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("faultTolerantJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(faultTolerantChunkStep(jobRepository, transactionManager))
                    .build();
        }

        @Bean
        public Job listenerJob(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("listenerJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(listenerEnabledStep(jobRepository, transactionManager))
                    .build();
        }

        @Bean
        public Job mixedStepJob(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("mixedStepJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(simpleTaskletStep(jobRepository, transactionManager))
                    .next(chunkOrientedStep(jobRepository, transactionManager))
                    .next(customTaskletStep(jobRepository, transactionManager))
                    .build();
        }
    }

    /**
     * Custom Tasklet Implementation
     */
    public static class FileCleanupTasklet implements Tasklet {
        
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            System.out.println("\n--- Executing File Cleanup Tasklet ---");
            System.out.println("Cleaning up temporary files...");
            System.out.println("Job Name: " + chunkContext.getStepContext().getJobName());
            System.out.println("Step Name: " + chunkContext.getStepContext().getStepName());
            
            // Simulate cleanup operation
            String[] tempFiles = {"temp1.tmp", "temp2.tmp", "cache.dat"};
            for (String file : tempFiles) {
                System.out.println("  Deleted: " + file);
            }
            
            System.out.println("Cleanup completed successfully");
            return RepeatStatus.FINISHED;
        }
    }

    /**
     * Step Execution Statistics
     */
    public static class StepStatistics {
        
        public static void printStepStatistics(StepExecution stepExecution) {
            System.out.println("\n========== Step Statistics ==========");
            System.out.println("Step Name: " + stepExecution.getStepName());
            System.out.println("Status: " + stepExecution.getStatus());
            System.out.println("Exit Status: " + stepExecution.getExitStatus().getExitCode());
            System.out.println("Read Count: " + stepExecution.getReadCount());
            System.out.println("Write Count: " + stepExecution.getWriteCount());
            System.out.println("Commit Count: " + stepExecution.getCommitCount());
            System.out.println("Rollback Count: " + stepExecution.getRollbackCount());
            System.out.println("Skip Count (Read): " + stepExecution.getReadSkipCount());
            System.out.println("Skip Count (Write): " + stepExecution.getWriteSkipCount());
            System.out.println("Skip Count (Process): " + stepExecution.getProcessSkipCount());
            System.out.println("Filter Count: " + stepExecution.getFilterCount());
            System.out.println("Start Time: " + stepExecution.getStartTime());
            System.out.println("End Time: " + stepExecution.getEndTime());
            
            if (stepExecution.getEndTime() != null && stepExecution.getStartTime() != null) {
                long duration = stepExecution.getEndTime().getTime() - 
                               stepExecution.getStartTime().getTime();
                System.out.println("Duration: " + duration + " ms");
            }
            System.out.println("=====================================\n");
        }
    }

    /**
     * Demonstration of different Step patterns
     */
    public static void main(String[] args) {
        System.out.println("=== Spring Batch Step Pattern Demonstration ===\n");

        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(BatchStepConfiguration.class);

        JobLauncher jobLauncher = context.getBean(JobLauncher.class);

        try {
            // Demo 1: Tasklet Steps
            System.out.println("\n--- Demo 1: Tasklet Steps ---");
            Job taskletJob = context.getBean("taskletStepJob", Job.class);
            JobParameters params1 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .toJobParameters();
            JobExecution execution1 = jobLauncher.run(taskletJob, params1);
            
            System.out.println("\nTasklet Step Results:");
            for (StepExecution stepExecution : execution1.getStepExecutions()) {
                StepStatistics.printStepStatistics(stepExecution);
            }

            // Demo 2: Chunk-Oriented Steps
            System.out.println("\n--- Demo 2: Chunk-Oriented Processing ---");
            Job chunkJob = context.getBean("chunkStepJob", Job.class);
            JobParameters params2 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .toJobParameters();
            JobExecution execution2 = jobLauncher.run(chunkJob, params2);
            
            System.out.println("\nChunk Step Results:");
            for (StepExecution stepExecution : execution2.getStepExecutions()) {
                StepStatistics.printStepStatistics(stepExecution);
            }

            // Demo 3: Steps with Listeners
            System.out.println("\n--- Demo 3: Steps with Listeners ---");
            Job listenerJob = context.getBean("listenerJob", Job.class);
            JobParameters params3 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .toJobParameters();
            JobExecution execution3 = jobLauncher.run(listenerJob, params3);

            // Demo 4: Mixed Step Types
            System.out.println("\n--- Demo 4: Mixed Step Types ---");
            Job mixedJob = context.getBean("mixedStepJob", Job.class);
            JobParameters params4 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .toJobParameters();
            JobExecution execution4 = jobLauncher.run(mixedJob, params4);
            
            System.out.println("\nMixed Job Step Summary:");
            for (StepExecution stepExecution : execution4.getStepExecutions()) {
                System.out.println("  " + stepExecution.getStepName() + " - " + 
                                 stepExecution.getStatus());
            }

        } catch (Exception e) {
            System.err.println("Error executing jobs: " + e.getMessage());
            e.printStackTrace();
        } finally {
            context.close();
        }

        System.out.println("\n=== Step Pattern Demonstration Complete ===");
    }
}

/**
 * KEY TAKEAWAYS:
 * 
 * 1. Step Types:
 *    - Tasklet Step: Single operation, full control
 *    - Chunk-Oriented Step: Read-Process-Write pattern
 *    - Partitioned Step: Parallel execution
 *    - Flow Step: Nested flow execution
 * 
 * 2. Chunk-Oriented Processing:
 *    - Chunk size determines transaction boundaries
 *    - Read -> Process -> Write cycle
 *    - Automatic transaction management
 *    - Efficient for large data processing
 * 
 * 3. Tasklet Pattern:
 *    - Single method execution
 *    - Full control over transaction
 *    - Ideal for cleanup, initialization, validation
 *    - RepeatStatus controls repetition
 * 
 * 4. Step Configuration:
 *    - chunk(size): Sets chunk/transaction size
 *    - reader(): Data source
 *    - processor(): Transformation logic
 *    - writer(): Data destination
 *    - tasklet(): Single operation
 *    - listener(): Lifecycle callbacks
 * 
 * 5. Fault Tolerance:
 *    - faultTolerant(): Enables fault tolerance
 *    - skip(): Skip on exceptions
 *    - skipLimit(): Max skips allowed
 *    - retry(): Retry on failure
 *    - retryLimit(): Max retry attempts
 * 
 * 6. Step Execution Context:
 *    - Maintains step state
 *    - Statistics tracking
 *    - Read/write counts
 *    - Commit/rollback tracking
 * 
 * 7. Best Practices:
 *    - Use chunk processing for large datasets
 *    - Use tasklets for simple operations
 *    - Choose appropriate chunk size (10-100 typically)
 *    - Implement proper error handling
 *    - Add listeners for monitoring
 *    - Keep steps independent and reusable
 * 
 * 8. Common Patterns:
 *    - Initialize -> Process -> Cleanup
 *    - Validate -> Transform -> Load
 *    - Extract -> Enrich -> Export
 *    - Read -> Filter -> Aggregate -> Write
 */
