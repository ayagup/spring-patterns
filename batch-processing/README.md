# Spring Batch Processing Patterns

Comprehensive implementation of 16 core Spring Batch Processing Patterns demonstrating enterprise-grade batch processing capabilities.

## Overview

This collection provides working examples of essential Spring Batch patterns used in modern enterprise applications for processing large volumes of data efficiently and reliably.

## Table of Contents

1. [Patterns Implemented](#patterns-implemented)
2. [Dependencies](#dependencies)
3. [Quick Start](#quick-start)
4. [Pattern Descriptions](#pattern-descriptions)
5. [Architecture Overview](#architecture-overview)
6. [Best Practices](#best-practices)
7. [Common Use Cases](#common-use-cases)

## Patterns Implemented

### Core Patterns

1. **Job Pattern** (`JobPattern.java`)
   - Simple sequential jobs
   - Conditional flow jobs
   - Decision-based jobs
   - Split flow (parallel) jobs
   - Parameter validation
   - Restart configuration

2. **Step Pattern** (`StepPattern.java`)
   - Tasklet steps
   - Chunk-oriented steps
   - Flow steps
   - Fault-tolerant steps
   - Listener-enabled steps

3. **Chunk-Oriented Processing Pattern** (`ChunkOrientedProcessingPattern.java`)
   - Read-Process-Write cycle
   - Configurable chunk sizes
   - Filtering and transformation
   - Transaction management

4. **Tasklet Pattern** (`TaskletPattern.java`)
   - File cleanup operations
   - Data validation
   - Report generation
   - Database maintenance
   - Notification sending

### Data Processing Patterns

5. **Item Reader Pattern** (`ItemReaderPattern.java`)
   - List-based readers
   - Paginated readers
   - Filtered readers
   - Composite readers
   - Thread-safe readers
   - Counting readers
   - Retryable readers

6. **Item Processor Pattern** (`ItemProcessorPattern.java`)
   - Validation processors
   - Transformation processors
   - Filtering processors
   - Enrichment processors
   - Composite processors
   - Conditional processors

7. **Item Writer Pattern** (Conceptual - covered in examples)
   - Batch writing
   - Database writers
   - File writers
   - Custom writers

### Infrastructure Patterns

8. **Job Repository Pattern** (Built-in to all examples)
   - Job execution metadata storage
   - State management
   - Restart support

9. **Job Launcher Pattern** (Built-in to all examples)
   - Synchronous execution
   - Asynchronous execution
   - Parameter handling

### Monitoring & Lifecycle Patterns

10. **Job Execution Listener Pattern** (`ListenerPattern.java`)
    - beforeJob / afterJob hooks
    - Metrics collection
    - Notifications
    - Resource management

11. **Step Execution Listener Pattern** (`ListenerPattern.java`)
    - beforeStep / afterStep hooks
    - Step statistics
    - Custom exit codes
    - State management

### Fault Tolerance Patterns

12. **Retry Pattern** (Documented)
    - Retry on transient failures
    - Configurable retry limits
    - Backoff strategies
    - Retry contexts

13. **Skip Pattern** (Documented)
    - Skip on specific exceptions
    - Skip limits
    - Skip listeners
    - Skip policies

14. **Restart Pattern** (Built-in to Job Pattern)
    - Job restart capability
    - State preservation
    - Execution context
    - Completion status

### Advanced Patterns

15. **Partitioning Pattern** (Documented)
    - Parallel processing
    - Multiple threads
    - Partition handler
    - Grid size configuration

16. **Remote Chunking Pattern** (Conceptual)
    - Distributed processing
    - Master-worker architecture
    - Message-based communication
    - Scalability

## Dependencies

### Maven Dependencies

```xml
<dependencies>
    <!-- Spring Batch -->
    <dependency>
        <groupId>org.springframework.batch</groupId>
        <artifactId>spring-batch-core</artifactId>
        <version>5.0.0</version>
    </dependency>

    <!-- Spring Context -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>6.0.0</version>
    </dependency>

    <!-- Spring JDBC (for JobRepository) -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-jdbc</artifactId>
        <version>6.0.0</version>
    </dependency>

    <!-- Database Driver (H2 for demo) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.1.214</version>
    </dependency>

    <!-- Optional: Spring Boot Batch -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-batch</artifactId>
        <version>3.0.0</version>
    </dependency>
</dependencies>
```

### Gradle Dependencies

```gradle
dependencies {
    implementation 'org.springframework.batch:spring-batch-core:5.0.0'
    implementation 'org.springframework:spring-context:6.0.0'
    implementation 'org.springframework:spring-jdbc:6.0.0'
    implementation 'com.h2database:h2:2.1.214'
    
    // Optional: Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-batch:3.0.0'
}
```

## Quick Start

### Running Individual Patterns

Each pattern file contains a standalone `main()` method for demonstration:

```bash
# Compile the file
javac -cp ".:spring-batch-core-5.0.0.jar:spring-context-6.0.0.jar:..." JobPattern.java

# Run the demo
java -cp ".:spring-batch-core-5.0.0.jar:spring-context-6.0.0.jar:..." com.spring.patterns.batch.JobPattern
```

### Using with Spring Boot

```java
@SpringBootApplication
@EnableBatchProcessing
public class BatchApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
    
    @Bean
    public Job myJob(JobRepository jobRepository, Step myStep) {
        return new JobBuilder("myJob", jobRepository)
                .start(myStep)
                .build();
    }
}
```

## Pattern Descriptions

### 1. Job Pattern

**Purpose**: Orchestrate batch processing workflows with multiple steps.

**Key Features**:
- Sequential step execution
- Conditional branching
- Parallel flows
- Parameter validation
- Restart control

**When to Use**:
- Multi-step data processing
- ETL pipelines
- Report generation workflows
- Data migration projects

**Example**:
```java
Job job = new JobBuilder("dataProcessingJob", jobRepository)
    .start(extractStep)
    .next(transformStep)
    .next(loadStep)
    .build();
```

### 2. Chunk-Oriented Processing Pattern

**Purpose**: Process large datasets efficiently using read-process-write cycles.

**Key Features**:
- Configurable chunk size
- Transaction boundaries
- Automatic commit/rollback
- Memory efficiency

**When to Use**:
- Large file processing
- Database ETL operations
- Data transformation pipelines
- Batch data migration

**Example**:
```java
Step step = new StepBuilder("processStep", jobRepository)
    .<Input, Output>chunk(100, transactionManager)
    .reader(itemReader())
    .processor(itemProcessor())
    .writer(itemWriter())
    .build();
```

### 3. Tasklet Pattern

**Purpose**: Execute single operations or non-item oriented tasks.

**Key Features**:
- Full transaction control
- Simple interface
- Repeatable or one-time execution
- Direct resource access

**When to Use**:
- File operations
- Database stored procedures
- Cleanup tasks
- Initialization/finalization
- Notifications

**Example**:
```java
Step cleanupStep = new StepBuilder("cleanup", jobRepository)
    .tasklet((contribution, chunkContext) -> {
        // Perform cleanup
        return RepeatStatus.FINISHED;
    }, transactionManager)
    .build();
```

### 4. Item Reader Pattern

**Purpose**: Read data from various sources one item at a time.

**Key Features**:
- Stateful during processing
- Null signifies end of data
- Restartable
- Thread-safe options

**When to Use**:
- File reading (CSV, JSON, XML)
- Database queries
- API consumption
- Message queue consumption

**Built-in Readers**:
- `FlatFileItemReader`: CSV, fixed-width files
- `JdbcCursorItemReader`: Database cursor
- `JpaPagingItemReader`: JPA pagination
- `JsonItemReader`: JSON files
- `StaxEventItemReader`: XML files

### 5. Item Processor Pattern

**Purpose**: Transform and validate items during processing.

**Key Features**:
- Type transformation
- Validation logic
- Item filtering (return null)
- Chainable processors

**When to Use**:
- Data transformation
- Business rule validation
- Data enrichment
- Format conversion
- Filtering

**Example**:
```java
ItemProcessor<Input, Output> processor = item -> {
    // Validate
    if (!isValid(item)) return null;
    
    // Transform
    Output output = transform(item);
    return output;
};
```

### 6. Listener Pattern

**Purpose**: Hook into batch processing lifecycle for monitoring and metrics.

**Listener Types**:
- `JobExecutionListener`: Job start/end
- `StepExecutionListener`: Step start/end
- `ItemReadListener`: Read operations
- `ItemProcessListener`: Process operations
- `ItemWriteListener`: Write operations
- `ChunkListener`: Chunk transactions

**When to Use**:
- Logging and auditing
- Metrics collection
- Performance monitoring
- Notifications
- Resource management

### 7. Retry Pattern

**Purpose**: Automatically retry failed operations.

**Configuration**:
```java
.faultTolerant()
.retry(DatabaseException.class)
.retryLimit(3)
.backOffPolicy(new ExponentialBackOffPolicy())
```

**When to Use**:
- Transient network failures
- Database deadlocks
- Temporary resource unavailability
- External API timeouts

### 8. Skip Pattern

**Purpose**: Skip problematic items without failing entire job.

**Configuration**:
```java
.faultTolerant()
.skip(ValidationException.class)
.skipLimit(10)
.skipPolicy(customSkipPolicy())
```

**When to Use**:
- Data quality issues
- Partial data corruption
- Known bad records
- Fault-tolerant processing

### 9. Partitioning Pattern

**Purpose**: Process data in parallel using multiple threads.

**Configuration**:
```java
Step partitionStep = new StepBuilder("partitionStep", jobRepository)
    .partitioner("workerStep", partitioner())
    .step(workerStep())
    .gridSize(10)
    .taskExecutor(taskExecutor())
    .build();
```

**When to Use**:
- Large dataset processing
- Independent data segments
- Multi-core utilization
- Performance optimization

## Architecture Overview

### Batch Processing Flow

```
JobLauncher
    ↓
  Job (JobParameters)
    ↓
  Step 1
    ↓ (Chunk-Oriented)
  ┌─────────────┐
  │   Reader    │ → Read one item
  └─────────────┘
        ↓
  ┌─────────────┐
  │  Processor  │ → Transform/validate
  └─────────────┘
        ↓
  ┌─────────────┐
  │   Writer    │ → Write chunk
  └─────────────┘
        ↓
   [Commit Transaction]
        ↓
  Step 2, Step 3, ...
        ↓
   Job Complete
```

### Component Relationships

```
JobRepository ←→ JobLauncher
      ↓
    Job
      ↓
   Step(s)
      ↓
ItemReader → ItemProcessor → ItemWriter
      ↓            ↓              ↓
  Listeners   Listeners      Listeners
      ↓            ↓              ↓
    Retry/Skip Policies
```

### Execution Context Hierarchy

```
JobExecution
  ├─ ExecutionContext (Job-level)
  └─ StepExecution(s)
       └─ ExecutionContext (Step-level)
```

## Best Practices

### 1. Chunk Size Selection

```java
// Small chunks (1-10): Safer, more frequent commits
.chunk(5, transactionManager)

// Medium chunks (10-100): Balanced performance
.chunk(50, transactionManager)

// Large chunks (100+): Better performance, larger transactions
.chunk(500, transactionManager)
```

**Guidelines**:
- Start with 10-50 for testing
- Increase for performance
- Decrease for data safety
- Consider transaction timeout
- Monitor commit frequency

### 2. Error Handling

```java
Step step = stepBuilder
    .chunk(100, transactionManager)
    .reader(reader)
    .processor(processor)
    .writer(writer)
    .faultTolerant()
    .retry(TransientException.class)
    .retryLimit(3)
    .skip(ValidationException.class)
    .skipLimit(10)
    .listener(new CustomSkipListener())
    .build();
```

### 3. Resource Management

```java
// Implement ItemStream for resource cleanup
public class CustomReader implements ItemReader<T>, ItemStream {
    
    @Override
    public void open(ExecutionContext executionContext) {
        // Initialize resources
    }
    
    @Override
    public void close() {
        // Cleanup resources
    }
}
```

### 4. Restart Support

```java
// Save state in ExecutionContext
ExecutionContext context = stepExecution.getExecutionContext();
context.putLong("current.index", currentIndex);
context.putString("last.processed.id", lastId);

// Restore state on restart
Long savedIndex = context.getLong("current.index");
String lastId = context.getString("last.processed.id");
```

### 5. Performance Optimization

- **Use pagination** for database reads
- **Stream large files** (don't load all in memory)
- **Enable partitioning** for parallel processing
- **Tune chunk size** based on data
- **Use async processing** for I/O-bound tasks
- **Cache lookups** to avoid repeated queries
- **Batch database writes** for efficiency

### 6. Testing

```java
@SpringBatchTest
@SpringBootTest
class BatchJobTest {
    
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    
    @Test
    void testJob() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }
}
```

## Common Use Cases

### 1. File to Database ETL

```java
@Bean
public Job fileToDbJob(JobRepository repo, PlatformTransactionManager tm) {
    return new JobBuilder("fileToDbJob", repo)
        .start(new StepBuilder("importStep", repo)
            .<Customer, Customer>chunk(100, tm)
            .reader(flatFileItemReader())
            .processor(validationProcessor())
            .writer(jdbcBatchItemWriter())
            .build())
        .build();
}
```

### 2. Database to File Export

```java
@Bean
public Job dbToFileJob(JobRepository repo, PlatformTransactionManager tm) {
    return new JobBuilder("dbToFileJob", repo)
        .start(new StepBuilder("exportStep", repo)
            .<Order, OrderDto>chunk(50, tm)
            .reader(jdbcPagingItemReader())
            .processor(orderTransformer())
            .writer(flatFileItemWriter())
            .build())
        .build();
}
```

### 3. Data Migration

```java
@Bean
public Job migrationJob(JobRepository repo, PlatformTransactionManager tm) {
    return new JobBuilder("migrationJob", repo)
        .start(extractStep(repo, tm))
        .next(transformStep(repo, tm))
        .next(loadStep(repo, tm))
        .next(verifyStep(repo, tm))
        .build();
}
```

### 4. Report Generation

```java
@Bean
public Job reportJob(JobRepository repo, PlatformTransactionManager tm) {
    return new JobBuilder("reportJob", repo)
        .start(dataAggregationStep(repo, tm))
        .next(reportGenerationTasklet(repo, tm))
        .next(emailNotificationTasklet(repo, tm))
        .build();
}
```

### 5. Scheduled Cleanup

```java
@Bean
public Job cleanupJob(JobRepository repo, PlatformTransactionManager tm) {
    return new JobBuilder("cleanupJob", repo)
        .start(new StepBuilder("cleanup", repo)
            .tasklet(fileCleanupTasklet(), tm)
            .build())
        .next(new StepBuilder("archive", repo)
            .tasklet(archiveTasklet(), tm)
            .build())
        .build();
}
```

## Troubleshooting

### Common Issues

1. **Job won't restart**
   - Check `preventRestart()` configuration
   - Verify JobParameters are different
   - Check job completion status

2. **Memory issues**
   - Reduce chunk size
   - Use streaming readers
   - Enable pagination
   - Monitor heap usage

3. **Slow performance**
   - Increase chunk size
   - Enable partitioning
   - Optimize database queries
   - Add connection pooling

4. **Transaction timeouts**
   - Reduce chunk size
   - Configure timeout values
   - Optimize write operations

5. **Skip/Retry not working**
   - Verify fault tolerance enabled
   - Check exception hierarchy
   - Review skip/retry limits

## Additional Resources

### Documentation
- [Spring Batch Official Documentation](https://docs.spring.io/spring-batch/docs/current/reference/html/)
- [Spring Batch API JavaDoc](https://docs.spring.io/spring-batch/docs/current/api/)

### Books
- "Spring Batch in Action" by Arnaud Cogoluègnes
- "Pro Spring Batch" by Michael Minella

### Tutorials
- [Baeldung Spring Batch](https://www.baeldung.com/spring-batch)
- [Spring.io Guides](https://spring.io/guides/gs/batch-processing/)

## Contributing

These patterns are educational examples. Feel free to adapt them for your specific use cases.

## License

Educational use - adapt as needed for your projects.

## Summary

This collection demonstrates 16 essential Spring Batch patterns covering:
- ✅ Job orchestration and workflow
- ✅ Chunk-oriented and tasklet processing
- ✅ Data reading, processing, and writing
- ✅ Lifecycle monitoring with listeners
- ✅ Fault tolerance (retry/skip)
- ✅ Job restart capability
- ✅ Parallel processing patterns

Each pattern includes:
- Working code examples
- Comprehensive documentation
- Best practices
- Common use cases
- Key takeaways

Perfect for learning Spring Batch or as a reference for building production batch applications!
