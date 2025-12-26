package com.example.demo.patterns.metrics;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Distribution Summary Pattern - Track Distribution of Values
 * 
 * Purpose:
 * - Measure distribution of values (not time-based)
 * - Track statistical summaries (count, total, max)
 * - Monitor value distributions (payload size, batch size)
 * - Calculate percentiles for non-time metrics
 * - Track event magnitudes
 * 
 * Use Cases:
 * - HTTP request/response payload sizes
 * - Batch processing record counts
 * - Message queue message sizes
 * - File upload/download sizes
 * - Database result set sizes
 * - Cache entry sizes
 * - Collection sizes (list, map)
 * - Transaction amounts
 * - User session durations (in events, not time)
 * - API response body sizes
 * 
 * Key Features:
 * - count(): Number of recorded values
 * - totalAmount(): Sum of all recorded values
 * - mean(): Average value
 * - max(): Maximum recorded value
 * - Percentiles: P50, P95, P99, P99.9
 * - Histogram buckets: Service Level Objectives
 * - Tags for dimensional metrics
 * 
 * vs Timer:
 * - DistributionSummary: For non-time values (sizes, counts, amounts)
 * - Timer: For time-based measurements (duration, latency)
 * 
 * Configuration (application.yml):
 * management:
 *   metrics:
 *     distribution:
 *       percentiles-histogram:
 *         http.request.size: true
 *         http.response.size: true
 *       percentiles:
 *         http.request.size: 0.5,0.95,0.99
 *       slo:
 *         http.request.size: 1KB,10KB,100KB,1MB
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>io.micrometer</groupId>
 *     <artifactId>micrometer-core</artifactId>
 * </dependency>
 * 
 * Warnings:
 * - Percentile histograms increase memory usage
 * - High-cardinality tags cause performance issues
 * - Avoid recording unbounded values
 * - Set reasonable min/max expected values
 * - Monitor metric collection overhead
 * - Consider sampling for high-frequency events
 * 
 * Best Practices:
 * - Use base units (bytes, not KB/MB)
 * - Set appropriate SLO buckets
 * - Add meaningful tags
 * - Document expected value ranges
 * - Monitor count and total amount
 * - Use percentiles for capacity planning
 * - Create alerts on anomalies
 * - Track trends over time
 */
@SpringBootApplication
public class DistributionSummaryPattern {

    public static void main(String[] args) {
        SpringApplication.run(DistributionSummaryPattern.class, args);
    }

    // ============================================
    // Example 1: HTTP Request/Response Size Tracking
    // ============================================
    
    @Service
    public static class HttpMetricsService {
        
        private final DistributionSummary requestSize;
        private final DistributionSummary responseSize;
        private final Map<String, DistributionSummary> endpointRequestSizes = new ConcurrentHashMap<>();
        private final MeterRegistry registry;
        
        public HttpMetricsService(MeterRegistry registry) {
            this.registry = registry;
            
            // Track request payload sizes
            this.requestSize = DistributionSummary.builder("http.request.size")
                .description("HTTP request payload size in bytes")
                .baseUnit("bytes")
                .tags("direction", "inbound")
                .serviceLevelObjectives(
                    1024,           // 1 KB
                    10240,          // 10 KB
                    102400,         // 100 KB
                    1048576,        // 1 MB
                    10485760        // 10 MB
                )
                .minimumExpectedValue(1.0)
                .maximumExpectedValue(104857600.0)  // 100 MB
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
            
            // Track response payload sizes
            this.responseSize = DistributionSummary.builder("http.response.size")
                .description("HTTP response payload size in bytes")
                .baseUnit("bytes")
                .tags("direction", "outbound")
                .serviceLevelObjectives(1024, 10240, 102400, 1048576)
                .minimumExpectedValue(1.0)
                .maximumExpectedValue(104857600.0)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
        }
        
        public void recordRequest(String endpoint, int payloadBytes, int statusCode) {
            requestSize.record(payloadBytes);
            
            // Per-endpoint metrics
            String key = endpoint + ":" + statusCode;
            endpointRequestSizes.computeIfAbsent(key, k ->
                DistributionSummary.builder("http.request.size.endpoint")
                    .description("Request size per endpoint")
                    .baseUnit("bytes")
                    .tag("endpoint", endpoint)
                    .tag("status", String.valueOf(statusCode))
                    .register(registry)
            ).record(payloadBytes);
        }
        
        public void recordResponse(int payloadBytes, int statusCode) {
            responseSize.record(payloadBytes);
        }
        
        public Map<String, Object> getRequestSizeStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("count", requestSize.count());
            stats.put("total_bytes", requestSize.totalAmount());
            stats.put("mean_bytes", requestSize.mean());
            stats.put("max_bytes", requestSize.max());
            return stats;
        }
        
        public Map<String, Object> getResponseSizeStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("count", responseSize.count());
            stats.put("total_bytes", responseSize.totalAmount());
            stats.put("mean_bytes", responseSize.mean());
            stats.put("max_bytes", responseSize.max());
            return stats;
        }
    }

    // ============================================
    // Example 2: Database Result Set Size Tracking
    // ============================================
    
    @Service
    public static class DatabaseMetricsService {
        
        private final DistributionSummary resultSetSize;
        private final DistributionSummary batchSize;
        private final DistributionSummary rowSize;
        
        public DatabaseMetricsService(MeterRegistry registry) {
            // Track query result set sizes
            this.resultSetSize = DistributionSummary.builder("db.resultset.size")
                .description("Number of rows in query result set")
                .baseUnit("rows")
                .tags("database", "postgres")
                .serviceLevelObjectives(10, 100, 1000, 10000)
                .register(registry);
            
            // Track batch operation sizes
            this.batchSize = DistributionSummary.builder("db.batch.size")
                .description("Number of records in batch operation")
                .baseUnit("records")
                .serviceLevelObjectives(10, 50, 100, 500, 1000)
                .register(registry);
            
            // Track individual row sizes (bytes)
            this.rowSize = DistributionSummary.builder("db.row.size")
                .description("Size of database row in bytes")
                .baseUnit("bytes")
                .serviceLevelObjectives(100, 1000, 10000, 100000)
                .register(registry);
        }
        
        public void recordQueryResult(String query, int rowCount, String operation) {
            resultSetSize.record(rowCount);
            System.out.println("Query returned " + rowCount + " rows: " + query);
        }
        
        public void recordBatchOperation(int recordCount, String operation) {
            batchSize.record(recordCount);
            System.out.println("Batch " + operation + " with " + recordCount + " records");
        }
        
        public void recordRowSize(int bytes) {
            rowSize.record(bytes);
        }
        
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("query_count", resultSetSize.count());
            stats.put("avg_result_size", resultSetSize.mean());
            stats.put("max_result_size", resultSetSize.max());
            stats.put("batch_count", batchSize.count());
            stats.put("avg_batch_size", batchSize.mean());
            return stats;
        }
    }

    // ============================================
    // Example 3: Message Queue Size Tracking
    // ============================================
    
    @Service
    public static class MessageQueueMetricsService {
        
        private final DistributionSummary messageSize;
        private final DistributionSummary queueDepth;
        private final Map<String, DistributionSummary> topicSizes = new ConcurrentHashMap<>();
        private final MeterRegistry registry;
        
        public MessageQueueMetricsService(MeterRegistry registry) {
            this.registry = registry;
            
            // Track message sizes
            this.messageSize = DistributionSummary.builder("queue.message.size")
                .description("Message payload size in bytes")
                .baseUnit("bytes")
                .tags("queue", "default")
                .serviceLevelObjectives(
                    1024,      // 1 KB
                    10240,     // 10 KB
                    102400,    // 100 KB
                    1048576    // 1 MB
                )
                .minimumExpectedValue(1.0)
                .maximumExpectedValue(10485760.0)  // 10 MB
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
            
            // Track queue depth (number of messages)
            this.queueDepth = DistributionSummary.builder("queue.depth")
                .description("Number of messages in queue")
                .baseUnit("messages")
                .serviceLevelObjectives(10, 100, 1000, 10000)
                .register(registry);
        }
        
        public void recordMessagePublished(String topic, int payloadBytes) {
            messageSize.record(payloadBytes);
            
            // Per-topic metrics
            topicSizes.computeIfAbsent(topic, t ->
                DistributionSummary.builder("queue.message.size.topic")
                    .description("Message size per topic")
                    .baseUnit("bytes")
                    .tag("topic", t)
                    .register(registry)
            ).record(payloadBytes);
            
            System.out.println("Message published to " + topic + ": " + payloadBytes + " bytes");
        }
        
        public void recordQueueDepth(String queueName, int messageCount) {
            queueDepth.record(messageCount);
        }
        
        public Map<String, Object> getMessageSizeStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("message_count", messageSize.count());
            stats.put("total_bytes", messageSize.totalAmount());
            stats.put("avg_message_size", messageSize.mean());
            stats.put("max_message_size", messageSize.max());
            return stats;
        }
    }

    // ============================================
    // Example 4: File Operation Size Tracking
    // ============================================
    
    @Service
    public static class FileMetricsService {
        
        private final DistributionSummary uploadSize;
        private final DistributionSummary downloadSize;
        private final DistributionSummary fileCount;
        
        public FileMetricsService(MeterRegistry registry) {
            // Track file upload sizes
            this.uploadSize = DistributionSummary.builder("file.upload.size")
                .description("File upload size in bytes")
                .baseUnit("bytes")
                .tags("operation", "upload")
                .serviceLevelObjectives(
                    1048576,       // 1 MB
                    10485760,      // 10 MB
                    104857600,     // 100 MB
                    1073741824     // 1 GB
                )
                .minimumExpectedValue(1.0)
                .maximumExpectedValue(5368709120.0)  // 5 GB
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
            
            // Track file download sizes
            this.downloadSize = DistributionSummary.builder("file.download.size")
                .description("File download size in bytes")
                .baseUnit("bytes")
                .tags("operation", "download")
                .serviceLevelObjectives(1048576, 10485760, 104857600, 1073741824)
                .register(registry);
            
            // Track number of files per operation
            this.fileCount = DistributionSummary.builder("file.operation.count")
                .description("Number of files per operation")
                .baseUnit("files")
                .serviceLevelObjectives(1, 5, 10, 50, 100)
                .register(registry);
        }
        
        public void recordFileUpload(String filename, long bytes) {
            uploadSize.record(bytes);
            System.out.println("File uploaded: " + filename + " (" + bytes + " bytes)");
        }
        
        public void recordFileDownload(String filename, long bytes) {
            downloadSize.record(bytes);
            System.out.println("File downloaded: " + filename + " (" + bytes + " bytes)");
        }
        
        public void recordBatchFileOperation(int fileCount) {
            this.fileCount.record(fileCount);
        }
        
        public Map<String, Object> getUploadStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("upload_count", uploadSize.count());
            stats.put("total_bytes_uploaded", uploadSize.totalAmount());
            stats.put("avg_upload_size", uploadSize.mean());
            stats.put("max_upload_size", uploadSize.max());
            return stats;
        }
        
        public Map<String, Object> getDownloadStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("download_count", downloadSize.count());
            stats.put("total_bytes_downloaded", downloadSize.totalAmount());
            stats.put("avg_download_size", downloadSize.mean());
            stats.put("max_download_size", downloadSize.max());
            return stats;
        }
    }

    // ============================================
    // Example 5: Cache Entry Size Tracking
    // ============================================
    
    @Service
    public static class CacheMetricsService {
        
        private final DistributionSummary entrySize;
        private final DistributionSummary cacheSize;
        private final Map<String, DistributionSummary> cacheSizes = new ConcurrentHashMap<>();
        private final MeterRegistry registry;
        
        public CacheMetricsService(MeterRegistry registry) {
            this.registry = registry;
            
            // Track cache entry sizes
            this.entrySize = DistributionSummary.builder("cache.entry.size")
                .description("Size of cache entry in bytes")
                .baseUnit("bytes")
                .serviceLevelObjectives(100, 1000, 10000, 100000)
                .register(registry);
            
            // Track overall cache size
            this.cacheSize = DistributionSummary.builder("cache.size")
                .description("Total cache size in bytes")
                .baseUnit("bytes")
                .serviceLevelObjectives(
                    1048576,      // 1 MB
                    10485760,     // 10 MB
                    104857600,    // 100 MB
                    1073741824    // 1 GB
                )
                .register(registry);
        }
        
        public void recordCacheEntry(String cacheName, String key, int valueBytes) {
            entrySize.record(valueBytes);
            
            cacheSizes.computeIfAbsent(cacheName, c ->
                DistributionSummary.builder("cache.entry.size.cache")
                    .description("Entry size per cache")
                    .baseUnit("bytes")
                    .tag("cache", c)
                    .register(registry)
            ).record(valueBytes);
        }
        
        public void recordCacheSize(String cacheName, long totalBytes) {
            cacheSize.record(totalBytes);
        }
        
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("entry_count", entrySize.count());
            stats.put("avg_entry_size", entrySize.mean());
            stats.put("max_entry_size", entrySize.max());
            stats.put("cache_size_samples", cacheSize.count());
            stats.put("avg_cache_size", cacheSize.mean());
            return stats;
        }
    }

    // ============================================
    // Example 6: Transaction Amount Tracking
    // ============================================
    
    @Service
    public static class TransactionMetricsService {
        
        private final DistributionSummary transactionAmount;
        private final DistributionSummary itemsPerTransaction;
        private final Map<String, DistributionSummary> currencyAmounts = new ConcurrentHashMap<>();
        private final MeterRegistry registry;
        
        public TransactionMetricsService(MeterRegistry registry) {
            this.registry = registry;
            
            // Track transaction amounts
            this.transactionAmount = DistributionSummary.builder("transaction.amount")
                .description("Transaction amount in dollars")
                .baseUnit("dollars")
                .tags("currency", "USD")
                .serviceLevelObjectives(
                    10.0,
                    50.0,
                    100.0,
                    500.0,
                    1000.0,
                    5000.0,
                    10000.0
                )
                .minimumExpectedValue(0.01)
                .maximumExpectedValue(100000.0)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
            
            // Track number of items per transaction
            this.itemsPerTransaction = DistributionSummary.builder("transaction.items")
                .description("Number of items per transaction")
                .baseUnit("items")
                .serviceLevelObjectives(1, 5, 10, 20, 50)
                .register(registry);
        }
        
        public void recordTransaction(String transactionId, double amount, 
                                      String currency, int itemCount) {
            transactionAmount.record(amount);
            itemsPerTransaction.record(itemCount);
            
            // Per-currency metrics
            currencyAmounts.computeIfAbsent(currency, c ->
                DistributionSummary.builder("transaction.amount.currency")
                    .description("Transaction amount per currency")
                    .baseUnit("units")
                    .tag("currency", c)
                    .register(registry)
            ).record(amount);
            
            System.out.println("Transaction recorded: " + transactionId + 
                " - " + currency + " " + amount + " (" + itemCount + " items)");
        }
        
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("transaction_count", transactionAmount.count());
            stats.put("total_amount", transactionAmount.totalAmount());
            stats.put("avg_transaction_amount", transactionAmount.mean());
            stats.put("max_transaction_amount", transactionAmount.max());
            stats.put("avg_items_per_transaction", itemsPerTransaction.mean());
            return stats;
        }
    }

    // ============================================
    // Example 7: Collection Size Tracking
    // ============================================
    
    @Service
    public static class CollectionMetricsService {
        
        private final DistributionSummary listSize;
        private final DistributionSummary mapSize;
        private final DistributionSummary setSize;
        
        public CollectionMetricsService(MeterRegistry registry) {
            this.listSize = DistributionSummary.builder("collection.list.size")
                .description("Number of elements in list")
                .baseUnit("elements")
                .serviceLevelObjectives(10, 100, 1000, 10000)
                .register(registry);
            
            this.mapSize = DistributionSummary.builder("collection.map.size")
                .description("Number of entries in map")
                .baseUnit("entries")
                .serviceLevelObjectives(10, 100, 1000, 10000)
                .register(registry);
            
            this.setSize = DistributionSummary.builder("collection.set.size")
                .description("Number of elements in set")
                .baseUnit("elements")
                .serviceLevelObjectives(10, 100, 1000, 10000)
                .register(registry);
        }
        
        public void recordListSize(int size, String operation) {
            listSize.record(size);
        }
        
        public void recordMapSize(int size, String operation) {
            mapSize.record(size);
        }
        
        public void recordSetSize(int size, String operation) {
            setSize.record(size);
        }
        
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("avg_list_size", listSize.mean());
            stats.put("avg_map_size", mapSize.mean());
            stats.put("avg_set_size", setSize.mean());
            return stats;
        }
    }

    // ============================================
    // Example 8: Distribution Summary REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/distribution")
    public static class DistributionController {
        
        private final HttpMetricsService httpMetrics;
        private final DatabaseMetricsService dbMetrics;
        private final MessageQueueMetricsService queueMetrics;
        private final FileMetricsService fileMetrics;
        private final CacheMetricsService cacheMetrics;
        private final TransactionMetricsService transactionMetrics;
        private final CollectionMetricsService collectionMetrics;
        
        public DistributionController(
                HttpMetricsService httpMetrics,
                DatabaseMetricsService dbMetrics,
                MessageQueueMetricsService queueMetrics,
                FileMetricsService fileMetrics,
                CacheMetricsService cacheMetrics,
                TransactionMetricsService transactionMetrics,
                CollectionMetricsService collectionMetrics) {
            this.httpMetrics = httpMetrics;
            this.dbMetrics = dbMetrics;
            this.queueMetrics = queueMetrics;
            this.fileMetrics = fileMetrics;
            this.cacheMetrics = cacheMetrics;
            this.transactionMetrics = transactionMetrics;
            this.collectionMetrics = collectionMetrics;
        }
        
        @PostMapping("/http/request")
        public Map<String, Object> recordHttpRequest(
                @RequestParam String endpoint,
                @RequestParam int bytes,
                @RequestParam(defaultValue = "200") int status) {
            httpMetrics.recordRequest(endpoint, bytes, status);
            return httpMetrics.getRequestSizeStats();
        }
        
        @PostMapping("/http/response")
        public Map<String, Object> recordHttpResponse(
                @RequestParam int bytes,
                @RequestParam(defaultValue = "200") int status) {
            httpMetrics.recordResponse(bytes, status);
            return httpMetrics.getResponseSizeStats();
        }
        
        @PostMapping("/db/query")
        public Map<String, Object> recordQuery(
                @RequestParam String query,
                @RequestParam int rowCount,
                @RequestParam(defaultValue = "SELECT") String operation) {
            dbMetrics.recordQueryResult(query, rowCount, operation);
            return dbMetrics.getStats();
        }
        
        @PostMapping("/db/batch")
        public Map<String, String> recordBatch(
                @RequestParam int recordCount,
                @RequestParam(defaultValue = "INSERT") String operation) {
            dbMetrics.recordBatchOperation(recordCount, operation);
            return Collections.singletonMap("status", "recorded");
        }
        
        @PostMapping("/queue/message")
        public Map<String, Object> publishMessage(
                @RequestParam String topic,
                @RequestParam int bytes) {
            queueMetrics.recordMessagePublished(topic, bytes);
            return queueMetrics.getMessageSizeStats();
        }
        
        @PostMapping("/file/upload")
        public Map<String, Object> uploadFile(
                @RequestParam String filename,
                @RequestParam long bytes) {
            fileMetrics.recordFileUpload(filename, bytes);
            return fileMetrics.getUploadStats();
        }
        
        @PostMapping("/file/download")
        public Map<String, Object> downloadFile(
                @RequestParam String filename,
                @RequestParam long bytes) {
            fileMetrics.recordFileDownload(filename, bytes);
            return fileMetrics.getDownloadStats();
        }
        
        @PostMapping("/cache/entry")
        public Map<String, Object> cacheEntry(
                @RequestParam String cacheName,
                @RequestParam String key,
                @RequestParam int bytes) {
            cacheMetrics.recordCacheEntry(cacheName, key, bytes);
            return cacheMetrics.getStats();
        }
        
        @PostMapping("/transaction")
        public Map<String, Object> recordTransaction(
                @RequestParam String transactionId,
                @RequestParam double amount,
                @RequestParam(defaultValue = "USD") String currency,
                @RequestParam(defaultValue = "1") int items) {
            transactionMetrics.recordTransaction(transactionId, amount, currency, items);
            return transactionMetrics.getStats();
        }
    }
}
