package com.spring.patterns.batch;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.item.*;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.*;

/**
 * Chunk-Oriented Processing Pattern - Spring Batch
 * 
 * Chunk-oriented processing is the most common pattern in Spring Batch.
 * It follows the Read-Process-Write pattern where items are read one at a time,
 * processed individually, and written in chunks (groups).
 * 
 * Processing Flow:
 * 1. Read items one by one until chunk size is reached
 * 2. Process each item individually
 * 3. Write the entire chunk at once
 * 4. Commit transaction
 * 5. Repeat until no more items
 * 
 * Key Concepts:
 * - Chunk Size: Number of items processed before commit
 * - Transaction Boundaries: Each chunk is a transaction
 * - Item-oriented Processing: Individual item transformation
 * - Bulk Writing: Efficient batch writes
 * 
 * Components:
 * - ItemReader<I>: Reads input items
 * - ItemProcessor<I,O>: Transforms items (optional)
 * - ItemWriter<O>: Writes output chunks
 * 
 * Use Cases:
 * - Large file processing
 * - Database ETL operations
 * - Data transformation pipelines
 * - Report generation
 * - Data migration
 */
public class ChunkOrientedProcessingPattern {

    /**
     * Product domain object
     */
    public static class Product {
        private Long id;
        private String name;
        private String category;
        private Double price;
        private Integer stock;
        private boolean processed;

        public Product() {}

        public Product(Long id, String name, String category, Double price, Integer stock) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
            this.processed = false;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
        public boolean isProcessed() { return processed; }
        public void setProcessed(boolean processed) { this.processed = processed; }

        @Override
        public String toString() {
            return "Product{id=" + id + ", name='" + name + "', category='" + category + 
                   "', price=" + price + ", stock=" + stock + ", processed=" + processed + "}";
        }
    }

    /**
     * Configuration for Chunk-Oriented Processing
     */
    @Configuration
    @EnableBatchProcessing
    public static class ChunkProcessingConfiguration {

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

        // ========== ItemReader Implementations ==========

        /**
         * Simple List-based ItemReader
         */
        @Bean
        public ItemReader<Product> productReader() {
            return new ItemReader<Product>() {
                private List<Product> products;
                private int index = 0;

                {
                    // Initialize product data
                    products = Arrays.asList(
                        new Product(1L, "Laptop", "Electronics", 999.99, 50),
                        new Product(2L, "Mouse", "Electronics", 29.99, 200),
                        new Product(3L, "Keyboard", "Electronics", 79.99, 150),
                        new Product(4L, "Monitor", "Electronics", 299.99, 75),
                        new Product(5L, "Desk Chair", "Furniture", 199.99, 30),
                        new Product(6L, "Desk", "Furniture", 399.99, 20),
                        new Product(7L, "Notebook", "Stationery", 4.99, 500),
                        new Product(8L, "Pen Set", "Stationery", 9.99, 300),
                        new Product(9L, "Backpack", "Accessories", 49.99, 100),
                        new Product(10L, "Water Bottle", "Accessories", 14.99, 250),
                        new Product(11L, "Headphones", "Electronics", 149.99, 120),
                        new Product(12L, "USB Cable", "Electronics", 12.99, 400),
                        new Product(13L, "Lamp", "Furniture", 39.99, 80),
                        new Product(14L, "Book", "Stationery", 19.99, 150),
                        new Product(15L, "Calculator", "Electronics", 24.99, 100)
                    );
                }

                @Override
                public Product read() {
                    if (index < products.size()) {
                        Product product = products.get(index++);
                        System.out.println("  [READ] " + product.getId() + ": " + product.getName());
                        return product;
                    }
                    return null; // Signal end of data
                }
            };
        }

        /**
         * Filtered ItemReader (reads only specific categories)
         */
        @Bean
        public ItemReader<Product> filteredProductReader() {
            return new ItemReader<Product>() {
                private final ItemReader<Product> delegate = productReader();
                
                @Override
                public Product read() throws Exception {
                    Product product;
                    while ((product = delegate.read()) != null) {
                        if ("Electronics".equals(product.getCategory())) {
                            return product;
                        }
                    }
                    return null;
                }
            };
        }

        // ========== ItemProcessor Implementations ==========

        /**
         * Price Adjustment Processor
         * Applies discounts based on business rules
         */
        @Bean
        public ItemProcessor<Product, Product> priceAdjustmentProcessor() {
            return product -> {
                System.out.println("  [PROCESS] " + product.getName() + " - Original Price: $" + product.getPrice());
                
                // Apply 10% discount to electronics
                if ("Electronics".equals(product.getCategory())) {
                    product.setPrice(product.getPrice() * 0.90);
                    System.out.println("    -> Electronics discount applied: $" + product.getPrice());
                }
                
                // Apply bulk discount if stock > 100
                if (product.getStock() > 100) {
                    product.setPrice(product.getPrice() * 0.95);
                    System.out.println("    -> Bulk discount applied: $" + product.getPrice());
                }
                
                product.setProcessed(true);
                return product;
            };
        }

        /**
         * Filtering Processor
         * Returns null to filter out items
         */
        @Bean
        public ItemProcessor<Product, Product> filteringProcessor() {
            return product -> {
                // Filter out low-stock items
                if (product.getStock() < 50) {
                    System.out.println("  [FILTER] Skipping " + product.getName() + " (low stock: " + product.getStock() + ")");
                    return null; // Null means item is filtered out
                }
                return product;
            };
        }

        /**
         * Composite Processor
         * Chains multiple processors
         */
        @Bean
        public ItemProcessor<Product, Product> compositeProcessor() {
            return product -> {
                // First: Apply price adjustment
                product = priceAdjustmentProcessor().process(product);
                
                // Second: Validate
                if (product != null && product.getPrice() > 0) {
                    return product;
                }
                return null;
            };
        }

        /**
         * Transformation Processor (changes type)
         */
        @Bean
        public ItemProcessor<Product, String> reportProcessor() {
            return product -> {
                // Transform Product to CSV line
                return String.format("%d,%s,%s,%.2f,%d", 
                    product.getId(), 
                    product.getName(), 
                    product.getCategory(), 
                    product.getPrice(), 
                    product.getStock());
            };
        }

        // ========== ItemWriter Implementations ==========

        /**
         * Console Writer
         */
        @Bean
        public ItemWriter<Product> productWriter() {
            return chunk -> {
                System.out.println("  [WRITE] Chunk of " + chunk.size() + " items:");
                for (Product product : chunk) {
                    System.out.println("    -> " + product);
                }
                System.out.println("  [COMMIT] Transaction committed\n");
            };
        }

        /**
         * Report Writer (for transformed items)
         */
        @Bean
        public ItemWriter<String> reportWriter() {
            return chunk -> {
                System.out.println("  [WRITE] Report chunk of " + chunk.size() + " lines:");
                for (String line : chunk) {
                    System.out.println("    " + line);
                }
            };
        }

        // ========== Job Configurations ==========

        /**
         * Small Chunk Size (Chunk = 3)
         * More frequent commits, smaller transactions
         */
        @Bean
        public Job smallChunkJob(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("smallChunkJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(new org.springframework.batch.core.step.builder.StepBuilder("smallChunkStep", jobRepository)
                            .<Product, Product>chunk(3, transactionManager)
                            .reader(productReader())
                            .processor(priceAdjustmentProcessor())
                            .writer(productWriter())
                            .build())
                    .build();
        }

        /**
         * Large Chunk Size (Chunk = 10)
         * Fewer commits, larger transactions, better performance
         */
        @Bean
        public Job largeChunkJob(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("largeChunkJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(new org.springframework.batch.core.step.builder.StepBuilder("largeChunkStep", jobRepository)
                            .<Product, Product>chunk(10, transactionManager)
                            .reader(productReader())
                            .processor(priceAdjustmentProcessor())
                            .writer(productWriter())
                            .build())
                    .build();
        }

        /**
         * Filtering Job
         * Demonstrates item filtering in processor
         */
        @Bean
        public Job filteringJob(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("filteringJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(new org.springframework.batch.core.step.builder.StepBuilder("filteringStep", jobRepository)
                            .<Product, Product>chunk(5, transactionManager)
                            .reader(productReader())
                            .processor(filteringProcessor())
                            .writer(productWriter())
                            .build())
                    .build();
        }

        /**
         * Transformation Job
         * Changes item type from Product to String
         */
        @Bean
        public Job transformationJob(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("transformationJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(new org.springframework.batch.core.step.builder.StepBuilder("transformStep", jobRepository)
                            .<Product, String>chunk(5, transactionManager)
                            .reader(productReader())
                            .processor(reportProcessor())
                            .writer(reportWriter())
                            .build())
                    .build();
        }

        /**
         * Reader-Writer Only (No Processor)
         */
        @Bean
        public Job simpleChunkJob(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager) {
            return new org.springframework.batch.core.job.builder.JobBuilder("simpleChunkJob", jobRepository)
                    .incrementer(new RunIdIncrementer())
                    .start(new org.springframework.batch.core.step.builder.StepBuilder("simpleStep", jobRepository)
                            .<Product, Product>chunk(7, transactionManager)
                            .reader(productReader())
                            .writer(productWriter())
                            .build())
                    .build();
        }
    }

    /**
     * Main demonstration
     */
    public static void main(String[] args) {
        System.out.println("=== Chunk-Oriented Processing Pattern Demonstration ===\n");

        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(ChunkProcessingConfiguration.class);

        JobLauncher jobLauncher = context.getBean(JobLauncher.class);

        try {
            // Demo 1: Small Chunk Size
            System.out.println("\n========== Demo 1: Small Chunk Size (3) ==========");
            System.out.println("More frequent commits, smaller transactions\n");
            Job smallChunkJob = context.getBean("smallChunkJob", Job.class);
            JobParameters params1 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .toJobParameters();
            JobExecution execution1 = jobLauncher.run(smallChunkJob, params1);
            printSummary(execution1);

            // Demo 2: Large Chunk Size
            System.out.println("\n========== Demo 2: Large Chunk Size (10) ==========");
            System.out.println("Fewer commits, better performance\n");
            Job largeChunkJob = context.getBean("largeChunkJob", Job.class);
            JobParameters params2 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .toJobParameters();
            JobExecution execution2 = jobLauncher.run(largeChunkJob, params2);
            printSummary(execution2);

            // Demo 3: Filtering
            System.out.println("\n========== Demo 3: Filtering (Remove Low Stock) ==========");
            Job filteringJob = context.getBean("filteringJob", Job.class);
            JobParameters params3 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .toJobParameters();
            JobExecution execution3 = jobLauncher.run(filteringJob, params3);
            printSummary(execution3);

            // Demo 4: Transformation
            System.out.println("\n========== Demo 4: Transformation (Product to CSV) ==========");
            Job transformationJob = context.getBean("transformationJob", Job.class);
            JobParameters params4 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .toJobParameters();
            JobExecution execution4 = jobLauncher.run(transformationJob, params4);
            printSummary(execution4);

            // Demo 5: Simple (No Processor)
            System.out.println("\n========== Demo 5: Simple Read-Write (No Processing) ==========");
            Job simpleJob = context.getBean("simpleChunkJob", Job.class);
            JobParameters params5 = new JobParametersBuilder()
                    .addDate("date", new Date())
                    .toJobParameters();
            JobExecution execution5 = jobLauncher.run(simpleJob, params5);
            printSummary(execution5);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            context.close();
        }

        System.out.println("\n=== Demonstration Complete ===");
    }

    private static void printSummary(JobExecution execution) {
        System.out.println("\n----- Execution Summary -----");
        for (StepExecution step : execution.getStepExecutions()) {
            System.out.println("Step: " + step.getStepName());
            System.out.println("  Read Count: " + step.getReadCount());
            System.out.println("  Write Count: " + step.getWriteCount());
            System.out.println("  Filter Count: " + step.getFilterCount());
            System.out.println("  Commit Count: " + step.getCommitCount());
            System.out.println("  Status: " + step.getStatus());
        }
        System.out.println("-----------------------------\n");
    }
}

/**
 * KEY TAKEAWAYS:
 * 
 * 1. Chunk-Oriented Processing Flow:
 *    - Read items one at a time
 *    - Process each item individually
 *    - Accumulate items into chunk
 *    - Write entire chunk
 *    - Commit transaction
 * 
 * 2. Chunk Size Considerations:
 *    - Small chunks (1-10): More frequent commits, safer but slower
 *    - Medium chunks (10-100): Balanced performance
 *    - Large chunks (100+): Better performance but larger transactions
 * 
 * 3. ItemReader:
 *    - Returns one item at a time
 *    - Returns null to signal end of data
 *    - Can implement filtering logic
 *    - Stateful during chunk processing
 * 
 * 4. ItemProcessor:
 *    - Optional component
 *    - Transforms items individually
 *    - Can filter items by returning null
 *    - Can change item type
 *    - Chainable for complex logic
 * 
 * 5. ItemWriter:
 *    - Receives entire chunk
 *    - Writes all items in one operation
 *    - Efficient for batch operations
 *    - Should be idempotent for restart
 * 
 * 6. Transaction Boundaries:
 *    - Each chunk is one transaction
 *    - Commit happens after write
 *    - Rollback affects entire chunk
 *    - Choose size based on data safety needs
 * 
 * 7. Filtering vs Skipping:
 *    - Filtering: Processor returns null (not counted as write)
 *    - Skipping: Exception handling (counted in statistics)
 * 
 * 8. Best Practices:
 *    - Start with chunk size 10-50 for testing
 *    - Tune chunk size based on performance
 *    - Keep processor logic simple
 *    - Make writer idempotent
 *    - Handle null returns in processor
 *    - Monitor read/write/filter counts
 */
