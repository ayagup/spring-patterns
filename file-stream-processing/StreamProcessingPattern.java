package com.spring.patterns.filestream;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Stream Processing Pattern
 * 
 * Demonstrates various stream processing techniques in Spring applications:
 * - Java 8+ Stream API for data processing
 * - Parallel stream processing
 * - Stream pipelines and transformations
 * - File stream processing
 * - Reactive stream processing concepts
 * - Custom stream collectors
 * - Stream performance optimization
 * 
 * Use Cases:
 * - Data transformation and filtering
 * - Bulk data processing
 * - ETL operations
 * - Real-time data processing
 * - Log file analysis
 * - CSV/JSON data processing
 * 
 * Dependencies:
 * - Java 8+ (Stream API)
 * - Spring Framework
 */

/**
 * Basic Stream Processing Service
 * Demonstrates fundamental stream operations
 */
@Service
class StreamProcessingService {
    
    /**
     * Process collection with filtering and mapping
     */
    public <T, R> List<R> processWithFilter(
            Collection<T> items,
            Predicate<T> filter,
            Function<T, R> mapper) {
        
        return items.stream()
                .filter(filter)
                .map(mapper)
                .collect(Collectors.toList());
    }
    
    /**
     * Parallel processing for large datasets
     */
    public <T> long countWithParallel(Collection<T> items, Predicate<T> condition) {
        return items.parallelStream()
                .filter(condition)
                .count();
    }
    
    /**
     * Group and aggregate data
     */
    public <T, K> Map<K, Long> groupAndCount(
            Collection<T> items,
            Function<T, K> classifier) {
        
        return items.stream()
                .collect(Collectors.groupingBy(
                        classifier,
                        Collectors.counting()
                ));
    }
    
    /**
     * Complex stream pipeline with multiple operations
     */
    public <T extends Comparable<T>> Optional<T> findTopElement(
            Collection<T> items,
            Predicate<T> filter,
            int limit) {
        
        return items.stream()
                .filter(filter)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .limit(limit)
                .findFirst();
    }
}

/**
 * File Stream Processor
 * Handles file-based stream processing
 */
@Service
class FileStreamProcessor {
    
    /**
     * Read and process text file line by line
     */
    public List<String> processTextFile(Path filePath, Predicate<String> filter) 
            throws IOException {
        
        try (Stream<String> lines = Files.lines(filePath)) {
            return lines
                    .filter(filter)
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * Process large file with streaming to avoid memory issues
     */
    public void processLargeFile(
            Path inputFile,
            Path outputFile,
            Function<String, String> transformer) throws IOException {
        
        try (Stream<String> lines = Files.lines(inputFile);
             BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            
            lines.map(transformer)
                    .forEach(line -> {
                        try {
                            writer.write(line);
                            writer.newLine();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }
    
    /**
     * Process CSV file and convert to objects
     */
    public <T> List<T> processCsvFile(
            Path csvFile,
            Function<String[], T> rowMapper,
            boolean skipHeader) throws IOException {
        
        try (Stream<String> lines = Files.lines(csvFile)) {
            Stream<String> stream = skipHeader ? lines.skip(1) : lines;
            
            return stream
                    .map(line -> line.split(","))
                    .map(rowMapper)
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * Find files in directory tree using stream
     */
    public List<Path> findFiles(
            Path startPath,
            String pattern,
            int maxDepth) throws IOException {
        
        try (Stream<Path> paths = Files.walk(startPath, maxDepth)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().contains(pattern))
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * Calculate directory size using stream
     */
    public long calculateDirectorySize(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        }
    }
}

/**
 * Custom Stream Collectors
 */
class CustomCollectors {
    
    /**
     * Collector that joins strings with custom delimiter and wrapper
     */
    public static Collector<String, ?, String> toWrappedString(
            String delimiter,
            String prefix,
            String suffix) {
        
        return Collectors.collectingAndThen(
                Collectors.joining(delimiter),
                s -> prefix + s + suffix
        );
    }
    
    /**
     * Collector that creates immutable list
     */
    public static <T> Collector<T, ?, List<T>> toImmutableList() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                Collections::unmodifiableList
        );
    }
    
    /**
     * Collector that partitions and processes in batches
     */
    public static <T> Collector<T, ?, List<List<T>>> toBatches(int batchSize) {
        return Collector.of(
                ArrayList::new,
                (batches, element) -> {
                    if (batches.isEmpty() || 
                        batches.get(batches.size() - 1).size() >= batchSize) {
                        batches.add(new ArrayList<>());
                    }
                    batches.get(batches.size() - 1).add(element);
                },
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }
        );
    }
    
    /**
     * Collector for statistical summary
     */
    public static <T> Collector<T, ?, Statistics> toStatistics(
            ToDoubleFunction<T> mapper) {
        
        return Collector.of(
                Statistics::new,
                (stats, item) -> stats.accept(mapper.applyAsDouble(item)),
                Statistics::combine
        );
    }
    
    static class Statistics {
        private long count;
        private double sum;
        private double min = Double.MAX_VALUE;
        private double max = Double.MIN_VALUE;
        
        void accept(double value) {
            count++;
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        
        Statistics combine(Statistics other) {
            count += other.count;
            sum += other.sum;
            min = Math.min(min, other.min);
            max = Math.max(max, other.max);
            return this;
        }
        
        public double getAverage() {
            return count > 0 ? sum / count : 0.0;
        }
        
        public long getCount() { return count; }
        public double getSum() { return sum; }
        public double getMin() { return min; }
        public double getMax() { return max; }
    }
}

/**
 * Parallel Stream Processing
 */
@Service
class ParallelStreamProcessor {
    
    private final ForkJoinPool customThreadPool;
    
    public ParallelStreamProcessor() {
        // Custom thread pool for parallel streams
        this.customThreadPool = new ForkJoinPool(
                Runtime.getRuntime().availableProcessors()
        );
    }
    
    /**
     * Process with custom thread pool
     */
    public <T, R> List<R> processWithCustomPool(
            Collection<T> items,
            Function<T, R> processor) throws Exception {
        
        return customThreadPool.submit(() ->
                items.parallelStream()
                        .map(processor)
                        .collect(Collectors.toList())
        ).get();
    }
    
    /**
     * Parallel processing with error handling
     */
    public <T> Map<Boolean, List<T>> processWithErrorHandling(
            Collection<T> items,
            Predicate<T> validator) {
        
        return items.parallelStream()
                .collect(Collectors.partitioningBy(validator));
    }
    
    /**
     * Process in batches using parallel streams
     */
    public <T, R> List<R> processBatchesParallel(
            List<T> items,
            int batchSize,
            Function<List<T>, R> batchProcessor) {
        
        return IntStream.range(0, (items.size() + batchSize - 1) / batchSize)
                .parallel()
                .mapToObj(i -> {
                    int start = i * batchSize;
                    int end = Math.min(start + batchSize, items.size());
                    return items.subList(start, end);
                })
                .map(batchProcessor)
                .collect(Collectors.toList());
    }
}

/**
 * Reactive-style Stream Processing
 */
@Service
class ReactiveStreamProcessor {
    
    /**
     * Process with backpressure simulation
     */
    public <T> void processWithBackpressure(
            Stream<T> stream,
            Consumer<T> consumer,
            int bufferSize) {
        
        Iterator<T> iterator = stream.iterator();
        List<T> buffer = new ArrayList<>(bufferSize);
        
        while (iterator.hasNext()) {
            buffer.add(iterator.next());
            
            if (buffer.size() >= bufferSize) {
                buffer.forEach(consumer);
                buffer.clear();
            }
        }
        
        // Process remaining items
        buffer.forEach(consumer);
    }
    
    /**
     * Lazy evaluation with stream generation
     */
    public Stream<Integer> generateInfiniteStream(int seed) {
        return Stream.iterate(seed, n -> n + 1);
    }
    
    /**
     * Custom stream builder
     */
    public <T> Stream<T> buildCustomStream(Supplier<T> supplier, int limit) {
        return Stream.generate(supplier).limit(limit);
    }
}

/**
 * Stream Performance Optimizer
 */
class StreamOptimizer {
    
    /**
     * Optimize stream operations by ordering them efficiently
     */
    public <T> List<T> optimizedPipeline(
            Collection<T> items,
            Predicate<T> filter,
            Function<T, T> mapper,
            int limit) {
        
        // Filter before map to reduce transformations
        // Limit before collect to reduce memory
        return items.stream()
                .filter(filter)      // Reduce dataset early
                .limit(limit)        // Limit before expensive operations
                .map(mapper)         // Transform only needed items
                .collect(Collectors.toList());
    }
    
    /**
     * Use primitive streams for better performance
     */
    public double calculateAverage(List<Integer> numbers) {
        return numbers.stream()
                .mapToInt(Integer::intValue)  // Use IntStream
                .average()
                .orElse(0.0);
    }
    
    /**
     * Avoid repeated stream creation
     */
    public <T> Map<String, Object> getMultipleStatistics(Collection<T> items) {
        // Collect to list once, then reuse
        List<T> list = new ArrayList<>(items);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("count", list.size());
        stats.put("first", list.stream().findFirst().orElse(null));
        stats.put("hasElements", !list.isEmpty());
        
        return stats;
    }
}

/**
 * Data Models
 */
record Transaction(String id, double amount, String category, long timestamp) {
    public boolean isLargeTransaction() {
        return amount > 1000;
    }
}

record LogEntry(String level, String message, long timestamp) {}

record Customer(String id, String name, String tier, double totalPurchases) {}

/**
 * Business Logic Stream Processors
 */
@Service
class TransactionProcessor {
    
    /**
     * Analyze transactions by category
     */
    public Map<String, Double> getTotalByCategory(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::category,
                        Collectors.summingDouble(Transaction::amount)
                ));
    }
    
    /**
     * Find top transactions
     */
    public List<Transaction> getTopTransactions(
            List<Transaction> transactions,
            int limit) {
        
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::amount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Filter and transform transactions
     */
    public List<String> getLargeTransactionIds(List<Transaction> transactions) {
        return transactions.stream()
                .filter(Transaction::isLargeTransaction)
                .map(Transaction::id)
                .collect(Collectors.toList());
    }
    
    /**
     * Group by time period
     */
    public Map<String, List<Transaction>> groupByTimeRange(
            List<Transaction> transactions,
            long rangeMillis) {
        
        return transactions.stream()
                .collect(Collectors.groupingBy(t -> {
                    long period = t.timestamp() / rangeMillis;
                    return "Period-" + period;
                }));
    }
}

@Service
class LogAnalyzer {
    
    /**
     * Analyze log entries
     */
    public Map<String, Long> getLogCountsByLevel(List<LogEntry> logs) {
        return logs.stream()
                .collect(Collectors.groupingBy(
                        LogEntry::level,
                        Collectors.counting()
                ));
    }
    
    /**
     * Find error patterns
     */
    public List<String> findErrorPatterns(
            List<LogEntry> logs,
            String pattern) {
        
        return logs.stream()
                .filter(log -> "ERROR".equals(log.level()))
                .map(LogEntry::message)
                .filter(msg -> msg.contains(pattern))
                .distinct()
                .collect(Collectors.toList());
    }
}

/**
 * Stream Processing Pattern - Main Demonstration
 */
public class StreamProcessingPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Stream Processing Pattern Demo ===\n");
        
        // 1. Basic Stream Operations
        demonstrateBasicStreams();
        
        // 2. File Stream Processing
        demonstrateFileStreams();
        
        // 3. Parallel Stream Processing
        demonstrateParallelStreams();
        
        // 4. Custom Collectors
        demonstrateCustomCollectors();
        
        // 5. Business Logic Processing
        demonstrateBusinessProcessing();
        
        // 6. Performance Optimization
        demonstrateOptimization();
    }
    
    private static void demonstrateBasicStreams() {
        System.out.println("1. Basic Stream Operations:");
        
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        // Filtering and mapping
        List<Integer> evenSquares = numbers.stream()
                .filter(n -> n % 2 == 0)
                .map(n -> n * n)
                .collect(Collectors.toList());
        System.out.println("Even squares: " + evenSquares);
        
        // Reduction
        int sum = numbers.stream()
                .reduce(0, Integer::sum);
        System.out.println("Sum: " + sum);
        
        // Grouping
        Map<Boolean, List<Integer>> partitioned = numbers.stream()
                .collect(Collectors.partitioningBy(n -> n > 5));
        System.out.println("Partitioned: " + partitioned);
        
        System.out.println();
    }
    
    private static void demonstrateFileStreams() throws IOException {
        System.out.println("2. File Stream Processing:");
        
        // Create temporary file
        Path tempFile = Files.createTempFile("stream-demo", ".txt");
        Files.write(tempFile, Arrays.asList(
                "Line 1: Hello",
                "Line 2: World",
                "Line 3: Stream",
                "Line 4: Processing"
        ));
        
        FileStreamProcessor processor = new FileStreamProcessor();
        
        // Process lines
        List<String> filtered = processor.processTextFile(
                tempFile,
                line -> line.contains(":")
        );
        System.out.println("Filtered lines: " + filtered);
        
        // Clean up
        Files.deleteIfExists(tempFile);
        
        System.out.println();
    }
    
    private static void demonstrateParallelStreams() throws Exception {
        System.out.println("3. Parallel Stream Processing:");
        
        List<Integer> largeList = IntStream.range(1, 1000)
                .boxed()
                .collect(Collectors.toList());
        
        // Sequential vs Parallel
        long startSeq = System.currentTimeMillis();
        long countSeq = largeList.stream()
                .filter(n -> n % 2 == 0)
                .count();
        long timeSeq = System.currentTimeMillis() - startSeq;
        
        long startPar = System.currentTimeMillis();
        long countPar = largeList.parallelStream()
                .filter(n -> n % 2 == 0)
                .count();
        long timePar = System.currentTimeMillis() - startPar;
        
        System.out.println("Sequential: " + countSeq + " (" + timeSeq + "ms)");
        System.out.println("Parallel: " + countPar + " (" + timePar + "ms)");
        
        System.out.println();
    }
    
    private static void demonstrateCustomCollectors() {
        System.out.println("4. Custom Collectors:");
        
        List<String> words = Arrays.asList("Java", "Stream", "API", "Custom", "Collector");
        
        // Custom joining
        String wrapped = words.stream()
                .collect(CustomCollectors.toWrappedString(", ", "[", "]"));
        System.out.println("Wrapped: " + wrapped);
        
        // Batching
        List<Integer> numbers = IntStream.range(1, 11)
                .boxed()
                .collect(Collectors.toList());
        
        List<List<Integer>> batches = numbers.stream()
                .collect(CustomCollectors.toBatches(3));
        System.out.println("Batches: " + batches);
        
        System.out.println();
    }
    
    private static void demonstrateBusinessProcessing() {
        System.out.println("5. Business Logic Processing:");
        
        List<Transaction> transactions = Arrays.asList(
                new Transaction("T1", 1500.0, "Electronics", System.currentTimeMillis()),
                new Transaction("T2", 500.0, "Groceries", System.currentTimeMillis()),
                new Transaction("T3", 2000.0, "Electronics", System.currentTimeMillis()),
                new Transaction("T4", 300.0, "Groceries", System.currentTimeMillis())
        );
        
        TransactionProcessor processor = new TransactionProcessor();
        
        // Total by category
        Map<String, Double> totals = processor.getTotalByCategory(transactions);
        System.out.println("Totals by category: " + totals);
        
        // Large transaction IDs
        List<String> largeIds = processor.getLargeTransactionIds(transactions);
        System.out.println("Large transaction IDs: " + largeIds);
        
        System.out.println();
    }
    
    private static void demonstrateOptimization() {
        System.out.println("6. Stream Optimization:");
        
        List<Integer> numbers = IntStream.range(1, 10000)
                .boxed()
                .collect(Collectors.toList());
        
        StreamOptimizer optimizer = new StreamOptimizer();
        
        // Optimized pipeline
        List<Integer> result = optimizer.optimizedPipeline(
                numbers,
                n -> n % 2 == 0,
                n -> n * 2,
                10
        );
        System.out.println("Optimized result (first 10): " + result);
        
        // Primitive stream optimization
        double avg = optimizer.calculateAverage(numbers);
        System.out.println("Average: " + avg);
        
        System.out.println("\n=== Demo Complete ===");
    }
}
