package com.spring.patterns.filestream;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.*;
import org.springframework.batch.item.file.mapping.*;
import org.springframework.batch.item.file.transform.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.io.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Batch File Processing Pattern
 * 
 * Demonstrates Spring Batch framework for processing large files:
 * - Chunk-oriented processing
 * - ItemReader, ItemProcessor, ItemWriter
 * - CSV file reading and writing
 * - XML file processing
 * - JSON file processing
 * - Multi-line record processing
 * - Error handling and skip logic
 * - Job execution and monitoring
 * 
 * Use Cases:
 * - Large file imports/exports
 * - Data migration
 * - Report generation
 * - Data transformation
 * - ETL operations
 * - Scheduled batch jobs
 * 
 * Dependencies:
 * - spring-boot-starter-batch
 * - spring-batch-core
 * - H2 or other database for job repository
 */

/**
 * Domain Models
 */
record CustomerRecord(
        String id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String status
) {}

record TransactionRecord(
        String transactionId,
        String customerId,
        double amount,
        String type,
        LocalDateTime timestamp
) {
    public String toCSV() {
        return String.join(",",
                transactionId,
                customerId,
                String.valueOf(amount),
                type,
                timestamp.format(DateTimeFormatter.ISO_DATE_TIME)
        );
    }
}

record ProcessedOrder(
        String orderId,
        String customerId,
        double totalAmount,
        String status,
        int itemCount
) {}

/**
 * CSV File Item Reader
 */
class CustomerCsvReader {
    
    public FlatFileItemReader<CustomerRecord> createReader(Resource resource) {
        FlatFileItemReader<CustomerRecord> reader = new FlatFileItemReader<>();
        reader.setResource(resource);
        reader.setLinesToSkip(1); // Skip header
        reader.setLineMapper(createCustomerLineMapper());
        return reader;
    }
    
    private LineMapper<CustomerRecord> createCustomerLineMapper() {
        DefaultLineMapper<CustomerRecord> lineMapper = new DefaultLineMapper<>();
        
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("id", "firstName", "lastName", "email", "phone", "status");
        tokenizer.setDelimiter(",");
        
        BeanWrapperFieldSetMapper<CustomerRecord> fieldSetMapper = 
                new BeanWrapperFieldSetMapper<>() {
            @Override
            public CustomerRecord mapFieldSet(FieldSet fieldSet) {
                return new CustomerRecord(
                        fieldSet.readString("id"),
                        fieldSet.readString("firstName"),
                        fieldSet.readString("lastName"),
                        fieldSet.readString("email"),
                        fieldSet.readString("phone"),
                        fieldSet.readString("status")
                );
            }
        };
        
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        
        return lineMapper;
    }
}

/**
 * CSV File Item Writer
 */
class TransactionCsvWriter {
    
    public FlatFileItemWriter<TransactionRecord> createWriter(Resource resource) {
        FlatFileItemWriter<TransactionRecord> writer = new FlatFileItemWriter<>();
        writer.setResource(resource);
        writer.setLineAggregator(createLineAggregator());
        writer.setShouldDeleteIfExists(true);
        
        // Add header
        writer.setHeaderCallback(w -> w.write(
                "TransactionID,CustomerID,Amount,Type,Timestamp"
        ));
        
        return writer;
    }
    
    private LineAggregator<TransactionRecord> createLineAggregator() {
        DelimitedLineAggregator<TransactionRecord> lineAggregator = 
                new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        
        BeanWrapperFieldExtractor<TransactionRecord> fieldExtractor = 
                new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{
                "transactionId", "customerId", "amount", "type", "timestamp"
        });
        
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }
}

/**
 * Item Processor for business logic
 */
@Component
class CustomerProcessor implements ItemProcessor<CustomerRecord, CustomerRecord> {
    
    @Override
    public CustomerRecord process(CustomerRecord customer) throws Exception {
        // Business logic: filter inactive customers
        if ("INACTIVE".equals(customer.status())) {
            return null; // Skip this record
        }
        
        // Transform data
        return new CustomerRecord(
                customer.id(),
                customer.firstName().toUpperCase(),
                customer.lastName().toUpperCase(),
                customer.email().toLowerCase(),
                customer.phone(),
                customer.status()
        );
    }
}

/**
 * Custom Item Processor with validation
 */
@Component
class ValidatingTransactionProcessor 
        implements ItemProcessor<TransactionRecord, TransactionRecord> {
    
    @Override
    public TransactionRecord process(TransactionRecord transaction) throws Exception {
        // Validate
        if (transaction.amount() < 0) {
            throw new IllegalArgumentException(
                    "Invalid amount: " + transaction.transactionId()
            );
        }
        
        if (transaction.amount() > 10000) {
            System.out.println("Large transaction flagged: " + transaction.transactionId());
        }
        
        return transaction;
    }
}

/**
 * Multi-line Record Reader
 * For processing records that span multiple lines
 */
class MultiLineRecordReader implements ItemReader<String> {
    
    private final BufferedReader reader;
    private final String recordDelimiter;
    private boolean exhausted = false;
    
    public MultiLineRecordReader(Path filePath, String recordDelimiter) 
            throws IOException {
        this.reader = Files.newBufferedReader(filePath);
        this.recordDelimiter = recordDelimiter;
    }
    
    @Override
    public String read() throws Exception {
        if (exhausted) {
            return null;
        }
        
        StringBuilder record = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            if (line.equals(recordDelimiter)) {
                if (record.length() > 0) {
                    return record.toString();
                }
            } else {
                if (record.length() > 0) {
                    record.append("\n");
                }
                record.append(line);
            }
        }
        
        exhausted = true;
        reader.close();
        
        return record.length() > 0 ? record.toString() : null;
    }
}

/**
 * Custom Item Writer with buffering
 */
class BufferedFileWriter<T> implements ItemWriter<T> {
    
    private final Path outputPath;
    private final Function<T, String> converter;
    private BufferedWriter writer;
    
    public BufferedFileWriter(Path outputPath, Function<T, String> converter) {
        this.outputPath = outputPath;
        this.converter = converter;
    }
    
    public void open() throws IOException {
        this.writer = Files.newBufferedWriter(outputPath);
    }
    
    @Override
    public void write(Chunk<? extends T> chunk) throws Exception {
        for (T item : chunk) {
            writer.write(converter.apply(item));
            writer.newLine();
        }
        writer.flush();
    }
    
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }
}

/**
 * Batch Job Configuration
 */
@Configuration
@EnableBatchProcessing
class BatchJobConfig {
    
    @Bean
    public Job customerImportJob(
            JobRepository jobRepository,
            Step customerImportStep) {
        
        return new JobBuilder("customerImportJob", jobRepository)
                .start(customerImportStep)
                .build();
    }
    
    @Bean
    public Step customerImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<CustomerRecord> reader,
            ItemProcessor<CustomerRecord, CustomerRecord> processor,
            ItemWriter<CustomerRecord> writer) {
        
        return new StepBuilder("customerImportStep", jobRepository)
                .<CustomerRecord, CustomerRecord>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }
}

/**
 * Job Execution Listener
 */
@Component
class JobCompletionListener implements JobExecutionListener {
    
    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("Job started: " + jobExecution.getJobInstance().getJobName());
        System.out.println("Start time: " + jobExecution.getStartTime());
    }
    
    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("Job completed: " + jobExecution.getJobInstance().getJobName());
        System.out.println("Status: " + jobExecution.getStatus());
        System.out.println("End time: " + jobExecution.getEndTime());
        System.out.println("Duration: " + 
                (jobExecution.getEndTime().getTime() - 
                 jobExecution.getStartTime().getTime()) + "ms");
    }
}

/**
 * Step Execution Listener
 */
@Component
class StepExecutionNotificationListener implements StepExecutionListener {
    
    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("Step starting: " + stepExecution.getStepName());
    }
    
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("Step completed: " + stepExecution.getStepName());
        System.out.println("Read count: " + stepExecution.getReadCount());
        System.out.println("Write count: " + stepExecution.getWriteCount());
        System.out.println("Skip count: " + stepExecution.getSkipCount());
        
        return stepExecution.getExitStatus();
    }
}

/**
 * Chunk Listener
 */
@Component
class ChunkCountListener implements ChunkListener {
    
    private int chunkCount = 0;
    
    @Override
    public void beforeChunk(ChunkContext context) {
        chunkCount++;
    }
    
    @Override
    public void afterChunk(ChunkContext context) {
        if (chunkCount % 10 == 0) {
            System.out.println("Processed " + chunkCount + " chunks");
        }
    }
    
    @Override
    public void afterChunkError(ChunkContext context) {
        System.err.println("Error in chunk " + chunkCount);
    }
}

/**
 * Tasklet for custom processing
 */
@Component
class FileCleanupTasklet implements Tasklet {
    
    private final Path directoryPath;
    
    public FileCleanupTasklet(Path directoryPath) {
        this.directoryPath = directoryPath;
    }
    
    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) throws Exception {
        
        // Clean up old files
        Files.walk(directoryPath)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    try {
                        long lastModified = Files.getLastModifiedTime(path)
                                .toMillis();
                        long ageInDays = (System.currentTimeMillis() - lastModified) 
                                / (1000 * 60 * 60 * 24);
                        return ageInDays > 7;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        System.out.println("Deleted old file: " + path);
                    } catch (IOException e) {
                        System.err.println("Failed to delete: " + path);
                    }
                });
        
        return RepeatStatus.FINISHED;
    }
}

/**
 * Partitioned Step for parallel processing
 */
class FilePartitioner implements Partitioner {
    
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitions = new HashMap<>();
        
        // Create partitions based on file ranges or different files
        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            context.putInt("partitionNumber", i);
            context.putInt("fromLine", i * 1000);
            context.putInt("toLine", (i + 1) * 1000);
            partitions.put("partition" + i, context);
        }
        
        return partitions;
    }
}

/**
 * Retry Policy for failed items
 */
class CustomRetryPolicy implements org.springframework.retry.RetryPolicy {
    
    private final int maxAttempts;
    
    public CustomRetryPolicy(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
    
    @Override
    public boolean canRetry(org.springframework.retry.RetryContext context) {
        return context.getRetryCount() < maxAttempts;
    }
    
    @Override
    public org.springframework.retry.RetryContext open(
            org.springframework.retry.RetryContext parent) {
        return new org.springframework.retry.context.RetryContextSupport(parent);
    }
    
    @Override
    public void close(org.springframework.retry.RetryContext context) {
        // Cleanup if needed
    }
    
    @Override
    public void registerThrowable(
            org.springframework.retry.RetryContext context,
            Throwable throwable) {
        // Log retry attempt
        System.out.println("Retry attempt " + context.getRetryCount());
    }
}

/**
 * Job Launcher Service
 */
@Component
class BatchJobLauncher {
    
    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    private Job customerImportJob;
    
    public JobExecution runJob(String inputFile) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", inputFile)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        
        return jobLauncher.run(customerImportJob, jobParameters);
    }
}

/**
 * Batch File Processing Pattern - Main Demonstration
 */
public class BatchFileProcessingPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Batch File Processing Pattern Demo ===\n");
        
        // 1. CSV File Reading
        demonstrateCsvReading();
        
        // 2. CSV File Writing
        demonstrateCsvWriting();
        
        // 3. Item Processing
        demonstrateItemProcessing();
        
        // 4. Multi-line Records
        demonstrateMultiLineProcessing();
        
        // 5. Error Handling
        demonstrateErrorHandling();
        
        // 6. Performance Monitoring
        demonstratePerformanceMonitoring();
    }
    
    private static void demonstrateCsvReading() throws Exception {
        System.out.println("1. CSV File Reading:");
        
        // Create sample CSV file
        Path csvFile = Files.createTempFile("customers", ".csv");
        Files.write(csvFile, Arrays.asList(
                "ID,FirstName,LastName,Email,Phone,Status",
                "C001,John,Doe,john@example.com,555-0001,ACTIVE",
                "C002,Jane,Smith,jane@example.com,555-0002,ACTIVE",
                "C003,Bob,Johnson,bob@example.com,555-0003,INACTIVE"
        ));
        
        CustomerCsvReader readerFactory = new CustomerCsvReader();
        FlatFileItemReader<CustomerRecord> reader = 
                readerFactory.createReader(new FileSystemResource(csvFile));
        reader.open(new ExecutionContext());
        
        CustomerRecord customer;
        int count = 0;
        while ((customer = reader.read()) != null) {
            System.out.println("Read: " + customer);
            count++;
        }
        reader.close();
        
        System.out.println("Total records read: " + count);
        Files.deleteIfExists(csvFile);
        
        System.out.println();
    }
    
    private static void demonstrateCsvWriting() throws Exception {
        System.out.println("2. CSV File Writing:");
        
        Path outputFile = Files.createTempFile("transactions", ".csv");
        
        TransactionCsvWriter writerFactory = new TransactionCsvWriter();
        FlatFileItemWriter<TransactionRecord> writer = 
                writerFactory.createWriter(new FileSystemResource(outputFile));
        writer.open(new ExecutionContext());
        
        List<TransactionRecord> transactions = Arrays.asList(
                new TransactionRecord("T001", "C001", 100.50, "PURCHASE", 
                        LocalDateTime.now()),
                new TransactionRecord("T002", "C002", 250.75, "PURCHASE", 
                        LocalDateTime.now()),
                new TransactionRecord("T003", "C001", 50.00, "REFUND", 
                        LocalDateTime.now())
        );
        
        writer.write(new Chunk<>(transactions));
        writer.close();
        
        System.out.println("Written " + transactions.size() + " records to: " 
                + outputFile);
        
        // Read and display
        List<String> lines = Files.readAllLines(outputFile);
        lines.forEach(System.out::println);
        
        Files.deleteIfExists(outputFile);
        
        System.out.println();
    }
    
    private static void demonstrateItemProcessing() throws Exception {
        System.out.println("3. Item Processing:");
        
        CustomerProcessor processor = new CustomerProcessor();
        
        List<CustomerRecord> customers = Arrays.asList(
                new CustomerRecord("C001", "john", "doe", "JOHN@EXAMPLE.COM", 
                        "555-0001", "ACTIVE"),
                new CustomerRecord("C002", "jane", "smith", "JANE@EXAMPLE.COM", 
                        "555-0002", "INACTIVE"),
                new CustomerRecord("C003", "bob", "johnson", "BOB@EXAMPLE.COM", 
                        "555-0003", "ACTIVE")
        );
        
        for (CustomerRecord customer : customers) {
            CustomerRecord processed = processor.process(customer);
            if (processed != null) {
                System.out.println("Processed: " + processed);
            } else {
                System.out.println("Skipped: " + customer.id() + " (inactive)");
            }
        }
        
        System.out.println();
    }
    
    private static void demonstrateMultiLineProcessing() throws Exception {
        System.out.println("4. Multi-line Record Processing:");
        
        Path multiLineFile = Files.createTempFile("multiline", ".txt");
        Files.write(multiLineFile, Arrays.asList(
                "Record 1 Line 1",
                "Record 1 Line 2",
                "---",
                "Record 2 Line 1",
                "Record 2 Line 2",
                "Record 2 Line 3",
                "---"
        ));
        
        MultiLineRecordReader reader = new MultiLineRecordReader(multiLineFile, "---");
        
        String record;
        int count = 0;
        while ((record = reader.read()) != null) {
            count++;
            System.out.println("Record " + count + ":");
            System.out.println(record);
            System.out.println();
        }
        
        Files.deleteIfExists(multiLineFile);
        
        System.out.println();
    }
    
    private static void demonstrateErrorHandling() {
        System.out.println("5. Error Handling:");
        
        ValidatingTransactionProcessor processor = 
                new ValidatingTransactionProcessor();
        
        List<TransactionRecord> transactions = Arrays.asList(
                new TransactionRecord("T001", "C001", 100.0, "PURCHASE", 
                        LocalDateTime.now()),
                new TransactionRecord("T002", "C002", -50.0, "REFUND", 
                        LocalDateTime.now()),
                new TransactionRecord("T003", "C003", 15000.0, "PURCHASE", 
                        LocalDateTime.now())
        );
        
        for (TransactionRecord transaction : transactions) {
            try {
                TransactionRecord processed = processor.process(transaction);
                System.out.println("Valid: " + processed.transactionId());
            } catch (Exception e) {
                System.err.println("Invalid: " + transaction.transactionId() 
                        + " - " + e.getMessage());
            }
        }
        
        System.out.println();
    }
    
    private static void demonstratePerformanceMonitoring() {
        System.out.println("6. Performance Monitoring:");
        
        StepExecution stepExecution = new StepExecution("demoStep", 
                new JobExecution(1L));
        stepExecution.setReadCount(1000);
        stepExecution.setWriteCount(950);
        stepExecution.setSkipCount(50);
        stepExecution.setCommitCount(10);
        
        System.out.println("Step Name: " + stepExecution.getStepName());
        System.out.println("Read Count: " + stepExecution.getReadCount());
        System.out.println("Write Count: " + stepExecution.getWriteCount());
        System.out.println("Skip Count: " + stepExecution.getSkipCount());
        System.out.println("Commit Count: " + stepExecution.getCommitCount());
        
        double successRate = (double) stepExecution.getWriteCount() / 
                stepExecution.getReadCount() * 100;
        System.out.printf("Success Rate: %.2f%%\n", successRate);
        
        System.out.println("\n=== Demo Complete ===");
    }
}

/**
 * Additional utility imports needed
 */
import java.util.function.Function;
import org.springframework.batch.item.Chunk;
