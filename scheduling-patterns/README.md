# Spring Scheduling Patterns

Comprehensive guide to Spring Framework scheduling patterns with working Java implementations.

## Table of Contents

1. [Overview](#overview)
2. [Patterns Included](#patterns-included)
3. [Dependencies](#dependencies)
4. [Quick Start](#quick-start)
5. [Pattern Details](#pattern-details)
6. [Configuration Guide](#configuration-guide)
7. [Best Practices](#best-practices)
8. [Common Use Cases](#common-use-cases)
9. [Performance Tuning](#performance-tuning)
10. [Troubleshooting](#troubleshooting)

## Overview

This project demonstrates 8 essential Spring scheduling patterns used in production applications:

- **Task Scheduler Pattern**: Programmatic task scheduling with full control
- **Cron Trigger Pattern**: Calendar-based scheduling with cron expressions
- **Fixed Rate Pattern**: Execute tasks at regular intervals
- **Fixed Delay Pattern**: Execute tasks with delay between completions
- **Initial Delay Pattern**: Postpone first execution for warmup
- **Async Execution Pattern**: Non-blocking scheduled task execution
- **Task Executor Pattern**: Asynchronous task execution abstraction
- **Thread Pool Pattern**: Optimized thread pool configuration

## Patterns Included

### 1. Task Scheduler Pattern
**File**: `TaskSchedulerPattern.java`

Programmatic scheduling with `TaskScheduler` interface.

```java
@Bean
public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(10);
    scheduler.setThreadNamePrefix("scheduled-");
    scheduler.initialize();
    return scheduler;
}

// Schedule at fixed time
taskScheduler.schedule(task, Instant.now().plusSeconds(10));

// Schedule with cron
taskScheduler.schedule(task, new CronTrigger("0 * * * * *"));

// Schedule with fixed rate
taskScheduler.scheduleAtFixedRate(task, Duration.ofSeconds(5));
```

**When to use**: Dynamic scheduling, cancellable tasks, programmatic control

### 2. Cron Trigger Pattern
**File**: `CronTriggerPattern.java`

Schedule tasks using cron expressions.

```java
// Every 5 seconds
@Scheduled(cron = "*/5 * * * * *")
public void everyFiveSeconds() { }

// Business hours (9 AM - 5 PM, Mon-Fri)
@Scheduled(cron = "0 0 9-17 * * MON-FRI")
public void duringBusinessHours() { }

// Daily at midnight
@Scheduled(cron = "0 0 0 * * *")
public void dailyMidnight() { }

// First day of every month
@Scheduled(cron = "0 0 0 1 * *")
public void monthlyReport() { }
```

**Cron Format**: `second minute hour day month weekday`

**When to use**: Calendar-based scheduling, business hours, specific times

### 3. Fixed Rate Pattern
**File**: `FixedRatePattern.java`

Execute tasks at fixed intervals from START of previous execution.

```java
// Every 5 seconds (time between starts)
@Scheduled(fixedRate = 5000)
public void fixedRateTask() {
    // Task execution
}

// Configurable rate
@Scheduled(fixedRate = ${app.task.rate:10000})
public void configurableRate() { }
```

**Timeline Example** (2s rate, 1s execution):
```
00:00 - Start execution 1
00:01 - End execution 1
00:02 - Start execution 2 (2s from previous start)
00:03 - End execution 2
00:04 - Start execution 3
```

**When to use**: Regular intervals, monitoring, time-sensitive polling

### 4. Fixed Delay Pattern
**File**: `FixedDelayPattern.java`

Execute tasks with fixed delay between END and next START.

```java
// 3 seconds after completion
@Scheduled(fixedDelay = 3000)
public void fixedDelayTask() {
    // Task execution
}

// Configurable delay
@Scheduled(fixedDelayString = "${app.task.delay:5000}")
public void configurableDelay() { }
```

**Timeline Example** (2s delay, 1s execution):
```
00:00 - Start execution 1
00:01 - End execution 1
00:03 - Start execution 2 (2s delay after end)
00:04 - End execution 2
00:06 - Start execution 3
```

**When to use**: Sequential processing, resource-intensive tasks, variable execution times

### 5. Initial Delay Pattern
**File**: `InitialDelayPattern.java`

Postpone first execution to allow application warmup.

```java
// Wait 10 seconds, then run every 5 seconds
@Scheduled(fixedRate = 5000, initialDelay = 10000)
public void delayedStart() { }

// Wait 30 seconds, then 15 second delay between executions
@Scheduled(fixedDelay = 15000, initialDelay = 30000)
public void dataSync() { }
```

**Staggered Startup Example**:
```java
@Scheduled(fixedDelay = 10000, initialDelay = 1000)   // +1s
public void criticalTask() { }

@Scheduled(fixedDelay = 15000, initialDelay = 5000)   // +5s
public void normalTask() { }

@Scheduled(fixedDelay = 30000, initialDelay = 15000)  // +15s
public void heavyTask() { }
```

**When to use**: Application warmup, staggered startup, dependency initialization

### 6. Async Execution Pattern
**File**: `AsyncExecutionPattern.java`

Non-blocking scheduled execution using `@Async`.

```java
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.initialize();
        return executor;
    }
}

@Async
@Scheduled(fixedRate = 5000)
public void asyncTask() {
    // Long-running task doesn't block scheduler
}

@Async
@Scheduled(fixedRate = 10000)
public CompletableFuture<Result> asyncWithResult() {
    Result result = expensiveOperation();
    return CompletableFuture.completedFuture(result);
}
```

**When to use**: I/O operations, long-running tasks, parallel execution needed

### 7. Task Executor Pattern
**File**: `TaskExecutorPattern.java`

Spring's abstraction for asynchronous task execution.

```java
@Bean(name = "ioExecutor")
public TaskExecutor ioExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(20);
    executor.setMaxPoolSize(50);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("io-");
    executor.initialize();
    return executor;
}

// Execute task asynchronously
taskExecutor.execute(() -> {
    // Background processing
});
```

**When to use**: Background processing, parallel computations, async operations

### 8. Thread Pool Pattern
**File**: `ThreadPoolPattern.java`

Optimized thread pool configuration for different workloads.

```java
// CPU-bound tasks
@Bean
public ThreadPoolTaskExecutor cpuBoundPool() {
    int cores = Runtime.getRuntime().availableProcessors();
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(cores);
    executor.setMaxPoolSize(cores * 2);
    executor.setQueueCapacity(cores * 10);
    executor.initialize();
    return executor;
}

// I/O-bound tasks
@Bean
public ThreadPoolTaskExecutor ioBoundPool() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(50);
    executor.setMaxPoolSize(200);
    executor.setQueueCapacity(500);
    executor.initialize();
    return executor;
}
```

**Thread Pool Sizing**:
- **CPU-bound**: threads = CPU cores
- **I/O-bound**: threads = cores × (1 + wait time / compute time)
- **Mixed**: Start with cores, adjust based on monitoring

**When to use**: High-throughput applications, resource optimization, scalability

## Dependencies

### Maven
```xml
<dependencies>
    <!-- Spring Context -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>6.0.0</version>
    </dependency>
    
    <!-- Spring Context Support -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context-support</artifactId>
        <version>6.0.0</version>
    </dependency>
</dependencies>
```

### Gradle
```gradle
dependencies {
    implementation 'org.springframework:spring-context:6.0.0'
    implementation 'org.springframework:spring-context-support:6.0.0'
}
```

## Quick Start

### 1. Enable Scheduling

```java
@Configuration
@EnableScheduling
public class SchedulingConfig {
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("scheduler-");
        scheduler.initialize();
        return scheduler;
    }
}
```

### 2. Create Scheduled Task

```java
@Component
public class MyScheduledTasks {
    
    @Scheduled(fixedRate = 5000)
    public void simpleTask() {
        System.out.println("Task executed at: " + LocalDateTime.now());
    }
    
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void businessDayTask() {
        System.out.println("Business day task");
    }
}
```

### 3. Run Application

```java
public class Application {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = 
            new AnnotationConfigApplicationContext(SchedulingConfig.class);
        
        // Application runs with scheduled tasks
        Thread.sleep(60000);
        
        context.close();
    }
}
```

## Configuration Guide

### Basic Configuration

```java
@Configuration
@EnableScheduling
public class SchedulingConfig {
    
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // Pool configuration
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("scheduled-");
        
        // Shutdown configuration
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        
        // Error handling
        scheduler.setRejectedExecutionHandler(
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        scheduler.initialize();
        return scheduler;
    }
}
```

### Advanced Configuration

```java
@Configuration
@EnableScheduling
@EnableAsync
public class AdvancedSchedulingConfig implements SchedulingConfigurer {
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
    }
    
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(20);
        scheduler.setThreadNamePrefix("advanced-");
        scheduler.setDaemon(false);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();
        return scheduler;
    }
    
    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

### Property-Based Configuration

**application.properties**:
```properties
# Scheduling Configuration
app.scheduling.pool-size=10
app.scheduling.thread-name-prefix=scheduled-

# Task Configuration
app.task.rate=5000
app.task.delay=3000
app.task.initial-delay=10000

# Cron Expressions
app.cron.daily-report=0 0 2 * * *
app.cron.hourly-sync=0 0 * * * *
```

**Java Configuration**:
```java
@Configuration
@EnableScheduling
@PropertySource("classpath:application.properties")
public class PropertiesConfig {
    
    @Value("${app.scheduling.pool-size}")
    private int poolSize;
    
    @Value("${app.scheduling.thread-name-prefix}")
    private String threadNamePrefix;
    
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix(threadNamePrefix);
        scheduler.initialize();
        return scheduler;
    }
}

@Component
public class ConfigurableTasks {
    
    @Scheduled(fixedRateString = "${app.task.rate}")
    public void configurableRate() { }
    
    @Scheduled(cron = "${app.cron.daily-report}")
    public void dailyReport() { }
}
```

## Best Practices

### 1. Choose the Right Pattern

| Pattern | Use Case |
|---------|----------|
| Fixed Rate | Regular intervals, monitoring, polling |
| Fixed Delay | Sequential processing, variable execution time |
| Cron | Calendar-based, business hours, specific times |
| Initial Delay | Warmup, staggered startup |
| Async | Long-running, I/O operations, parallelism |

### 2. Thread Pool Sizing

```java
// CPU-bound: cores
int cpuThreads = Runtime.getRuntime().availableProcessors();

// I/O-bound: cores * (1 + wait/compute ratio)
// If 90% waiting: 8 cores * (1 + 9) = 80 threads
int ioThreads = cpuThreads * 10;

// Mixed: Start with cores, monitor, adjust
int mixedThreads = cpuThreads;
```

### 3. Error Handling

```java
@Scheduled(fixedRate = 5000)
public void taskWithErrorHandling() {
    try {
        // Task logic
        performOperation();
    } catch (Exception e) {
        logger.error("Task failed", e);
        // Handle failure (retry, alert, etc.)
    }
}
```

### 4. Monitoring

```java
@Scheduled(fixedRate = 60000)
public void monitoredTask() {
    long startTime = System.currentTimeMillis();
    
    try {
        performTask();
    } finally {
        long duration = System.currentTimeMillis() - startTime;
        logger.info("Task completed in {}ms", duration);
        metrics.recordExecutionTime(duration);
    }
}
```

### 5. Graceful Shutdown

```java
@Bean
public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(10);
    
    // Wait for tasks to complete
    scheduler.setWaitForTasksToCompleteOnShutdown(true);
    scheduler.setAwaitTerminationSeconds(60);
    
    scheduler.initialize();
    return scheduler;
}
```

### 6. Avoid Common Pitfalls

❌ **Don't**:
```java
// Task takes longer than rate - causes queuing
@Scheduled(fixedRate = 1000)
public void slowTask() {
    Thread.sleep(5000); // Takes 5 seconds!
}

// Blocking operation without error handling
@Scheduled(fixedRate = 5000)
public void dangerousTask() {
    externalApi.call(); // What if it hangs?
}
```

✅ **Do**:
```java
// Use fixed delay for variable execution time
@Scheduled(fixedDelay = 1000)
public void betterSlowTask() {
    Thread.sleep(5000); // OK - waits after completion
}

// Use async + timeout for blocking operations
@Async
@Scheduled(fixedRate = 5000)
public void safeTask() {
    try {
        CompletableFuture.supplyAsync(() -> externalApi.call())
            .orTimeout(3, TimeUnit.SECONDS)
            .get();
    } catch (TimeoutException e) {
        logger.warn("API call timed out");
    }
}
```

## Common Use Cases

### 1. Database Cleanup

```java
@Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
public void cleanupExpiredRecords() {
    logger.info("Starting database cleanup");
    int deleted = repository.deleteExpiredRecords();
    logger.info("Deleted {} expired records", deleted);
}
```

### 2. Cache Refresh

```java
@Scheduled(fixedDelay = 300000, initialDelay = 60000) // Every 5 min, start after 1 min
public void refreshCache() {
    logger.info("Refreshing cache");
    List<Item> items = repository.findActiveItems();
    cache.putAll(items);
    logger.info("Cache refreshed with {} items", items.size());
}
```

### 3. Health Monitoring

```java
@Scheduled(fixedRate = 30000) // Every 30 seconds
public void healthCheck() {
    boolean healthy = performHealthCheck();
    if (!healthy) {
        alertService.sendAlert("System health check failed");
    }
}
```

### 4. Report Generation

```java
@Scheduled(cron = "0 0 8 * * MON") // Every Monday at 8 AM
public void generateWeeklyReport() {
    logger.info("Generating weekly report");
    Report report = reportService.generateWeeklyReport();
    emailService.sendReport(report);
    logger.info("Weekly report sent");
}
```

### 5. Data Synchronization

```java
@Async
@Scheduled(fixedDelay = 60000) // 1 minute after completion
public void syncExternalData() {
    logger.info("Starting data sync");
    List<Data> newData = externalApi.fetchNewData();
    repository.saveAll(newData);
    logger.info("Synced {} records", newData.size());
}
```

## Performance Tuning

### 1. Pool Size Optimization

Monitor and adjust based on metrics:

```java
@Scheduled(fixedRate = 60000)
public void monitorThreadPool() {
    ThreadPoolExecutor executor = (ThreadPoolExecutor) taskScheduler;
    
    int activeCount = executor.getActiveCount();
    int poolSize = executor.getPoolSize();
    int queueSize = executor.getQueue().size();
    
    logger.info("Pool stats - Active: {}, Pool: {}, Queue: {}", 
                activeCount, poolSize, queueSize);
    
    // Alert if pool is saturated
    if (activeCount == poolSize && queueSize > 0) {
        logger.warn("Thread pool saturated!");
    }
}
```

### 2. Task Duration Monitoring

```java
@Aspect
@Component
public class SchedulingMonitoringAspect {
    
    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object monitorExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            logger.info("Task {} took {}ms", methodName, duration);
            metrics.recordTaskDuration(methodName, duration);
        }
    }
}
```

### 3. Memory Management

```java
@Scheduled(fixedRate = 300000) // Every 5 minutes
public void logMemoryUsage() {
    Runtime runtime = Runtime.getRuntime();
    long maxMemory = runtime.maxMemory() / 1024 / 1024;
    long totalMemory = runtime.totalMemory() / 1024 / 1024;
    long freeMemory = runtime.freeMemory() / 1024 / 1024;
    long usedMemory = totalMemory - freeMemory;
    
    logger.info("Memory - Max: {}MB, Total: {}MB, Used: {}MB, Free: {}MB",
                maxMemory, totalMemory, usedMemory, freeMemory);
}
```

## Troubleshooting

### Problem: Tasks Not Executing

**Symptoms**: Scheduled methods never run

**Solutions**:
1. Verify `@EnableScheduling` on configuration class
2. Ensure class with `@Scheduled` is a Spring bean (`@Component`, `@Service`, etc.)
3. Check method is `public` (not `private` or `protected`)
4. Verify cron expression syntax
5. Check application context is running

### Problem: Tasks Running Concurrently

**Symptoms**: Same task executes multiple times simultaneously

**Solutions**:
```java
// Use @Async with single-threaded executor
@Bean(name = "singleThreadExecutor")
public Executor singleThreadExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(1);
    executor.setMaxPoolSize(1);
    executor.setQueueCapacity(10);
    executor.initialize();
    return executor;
}

@Async("singleThreadExecutor")
@Scheduled(fixedRate = 5000)
public void sequentialTask() {
    // Will never run concurrently
}
```

### Problem: Thread Pool Exhaustion

**Symptoms**: Tasks queue up, slow execution

**Solutions**:
1. Increase pool size
2. Use separate pools for different workloads
3. Make tasks async with `@Async`
4. Optimize task execution time
5. Monitor pool metrics

### Problem: Slow Application Shutdown

**Symptoms**: Application takes long time to stop

**Solutions**:
```java
@Bean
public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(10);
    
    // Set reasonable timeout
    scheduler.setAwaitTerminationSeconds(30);
    scheduler.setWaitForTasksToCompleteOnShutdown(true);
    
    scheduler.initialize();
    return scheduler;
}
```

### Problem: Cron Expression Not Working

**Common Mistakes**:
```java
// ❌ Wrong: Missing seconds field
@Scheduled(cron = "0 * * * *")

// ✅ Correct: Spring requires 6 fields (includes seconds)
@Scheduled(cron = "0 0 * * * *")

// ❌ Wrong: Day of week starts at 0
@Scheduled(cron = "0 0 9 * * 1") // Not Monday!

// ✅ Correct: Use MON or 2 for Monday
@Scheduled(cron = "0 0 9 * * MON")
```

## Additional Resources

- [Spring Framework Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling)
- [Cron Expression Generator](https://crontab.guru/)
- [Spring Boot Scheduling](https://spring.io/guides/gs/scheduling-tasks/)

## License

This project is provided as educational material for learning Spring scheduling patterns.

---

**Created by**: Spring Patterns Team  
**Last Updated**: 2024
