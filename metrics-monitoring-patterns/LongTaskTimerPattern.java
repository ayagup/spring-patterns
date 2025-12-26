package com.example.demo.patterns.metrics;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Long Task Timer Pattern - Track Long-Running Tasks
 * 
 * Purpose:
 * - Measure duration of tasks WHILE they're running
 * - Track concurrent long-running operations
 * - Monitor in-progress task durations
 * - Detect hanging or stuck tasks
 * - Measure active task count
 * 
 * Use Cases:
 * - Batch job processing monitoring
 * - File upload/download progress
 * - Database backup operations
 * - Report generation
 * - Data migration tasks
 * - Video/audio transcoding
 * - Machine learning model training
 * - Large data exports
 * - Scheduled maintenance tasks
 * - Long-running API calls
 * 
 * vs Regular Timer:
 * - LongTaskTimer: Records WHILE task is running, shows active duration
 * - Timer: Records AFTER task completes, shows historical duration
 * 
 * Key Metrics:
 * - activeTasks(): Number of currently running tasks
 * - duration(): Total duration of all active tasks
 * - max(): Duration of longest active task
 * - mean(): Average duration of active tasks
 * 
 * Configuration (application.yml):
 * management:
 *   metrics:
 *     enable:
 *       jvm: true
 *       process: true
 *     tags:
 *       application: ${spring.application.name}
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
 * - Must call stop() on sample, or use try-with-resources
 * - Forgetting to stop samples causes memory leaks
 * - High task count impacts performance
 * - Don't use for short-duration tasks
 * - Monitor active task count
 * - Set alerts for stuck tasks
 * 
 * Best Practices:
 * - Always stop timer samples
 * - Use try-finally or try-with-resources
 * - Add meaningful tags (task type, user, etc.)
 * - Monitor max duration for stuck detection
 * - Track active task count
 * - Set timeout alerts
 * - Log long-running tasks
 * - Combine with regular Timer for completion metrics
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class LongTaskTimerPattern {

    public static void main(String[] args) {
        SpringApplication.run(LongTaskTimerPattern.class, args);
    }

    // ============================================
    // Example 1: Batch Job Processing
    // ============================================
    
    @Service
    public static class BatchProcessingService {
        
        private final LongTaskTimer batchTimer;
        private final Map<String, LongTaskTimer.Sample> activeSamples = new ConcurrentHashMap<>();
        
        public BatchProcessingService(MeterRegistry registry) {
            this.batchTimer = LongTaskTimer.builder("batch.processing")
                .description("Batch job processing duration")
                .tags("type", "batch")
                .register(registry);
        }
        
        public String processBatch(String batchId, int recordCount) {
            LongTaskTimer.Sample sample = batchTimer.start();
            activeSamples.put(batchId, sample);
            
            try {
                System.out.println("Starting batch: " + batchId + 
                    " with " + recordCount + " records");
                
                // Simulate batch processing
                for (int i = 0; i < recordCount; i++) {
                    processRecord(i);
                    
                    // Log progress for long batches
                    if (i % 100 == 0) {
                        System.out.println("Batch " + batchId + " progress: " + 
                            i + "/" + recordCount + 
                            " (duration: " + sample.duration(TimeUnit.SECONDS) + "s)");
                    }
                }
                
                return "Batch " + batchId + " completed: " + recordCount + " records";
                
            } finally {
                long duration = sample.stop();
                activeSamples.remove(batchId);
                System.out.println("Batch " + batchId + " finished in " + 
                    TimeUnit.NANOSECONDS.toSeconds(duration) + " seconds");
            }
        }
        
        public Map<String, Object> getBatchStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("active_batches", batchTimer.activeTasks());
            stats.put("total_duration_seconds", 
                TimeUnit.NANOSECONDS.toSeconds((long) batchTimer.duration(TimeUnit.NANOSECONDS)));
            stats.put("max_duration_seconds", 
                TimeUnit.NANOSECONDS.toSeconds((long) batchTimer.max(TimeUnit.NANOSECONDS)));
            stats.put("active_batch_ids", new ArrayList<>(activeSamples.keySet()));
            return stats;
        }
        
        private void processRecord(int recordId) {
            try {
                Thread.sleep(10); // Simulate work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ============================================
    // Example 2: File Upload/Download Tracking
    // ============================================
    
    @Service
    public static class FileTransferService {
        
        private final LongTaskTimer uploadTimer;
        private final LongTaskTimer downloadTimer;
        private final Map<String, TransferProgress> activeTransfers = new ConcurrentHashMap<>();
        
        public FileTransferService(MeterRegistry registry) {
            this.uploadTimer = LongTaskTimer.builder("file.upload")
                .description("File upload duration")
                .tags("operation", "upload")
                .register(registry);
            
            this.downloadTimer = LongTaskTimer.builder("file.download")
                .description("File download duration")
                .tags("operation", "download")
                .register(registry);
        }
        
        public String uploadFile(String filename, long sizeBytes) {
            LongTaskTimer.Sample sample = uploadTimer.start();
            String transferId = "upload-" + filename + "-" + System.currentTimeMillis();
            
            activeTransfers.put(transferId, new TransferProgress(
                transferId, filename, sizeBytes, "upload", sample));
            
            try {
                System.out.println("Starting upload: " + filename + 
                    " (" + sizeBytes + " bytes)");
                
                // Simulate chunked upload
                int chunkSize = 1024 * 1024; // 1 MB chunks
                long bytesTransferred = 0;
                
                while (bytesTransferred < sizeBytes) {
                    long chunkBytes = Math.min(chunkSize, sizeBytes - bytesTransferred);
                    uploadChunk(chunkBytes);
                    bytesTransferred += chunkBytes;
                    
                    // Update progress
                    TransferProgress progress = activeTransfers.get(transferId);
                    progress.bytesTransferred = bytesTransferred;
                    
                    // Log progress every 10 MB
                    if (bytesTransferred % (10 * 1024 * 1024) == 0) {
                        long durationSec = sample.duration(TimeUnit.SECONDS);
                        double mbPerSec = (bytesTransferred / 1024.0 / 1024.0) / Math.max(durationSec, 1);
                        System.out.println(String.format(
                            "Upload progress: %s - %.2f MB / %.2f MB (%.2f MB/s)",
                            filename,
                            bytesTransferred / 1024.0 / 1024.0,
                            sizeBytes / 1024.0 / 1024.0,
                            mbPerSec));
                    }
                }
                
                return "File uploaded: " + filename;
                
            } finally {
                sample.stop();
                activeTransfers.remove(transferId);
            }
        }
        
        public String downloadFile(String filename, long sizeBytes) {
            LongTaskTimer.Sample sample = downloadTimer.start();
            String transferId = "download-" + filename + "-" + System.currentTimeMillis();
            
            activeTransfers.put(transferId, new TransferProgress(
                transferId, filename, sizeBytes, "download", sample));
            
            try {
                System.out.println("Starting download: " + filename + 
                    " (" + sizeBytes + " bytes)");
                
                // Simulate download
                Thread.sleep(sizeBytes / (1024 * 100)); // Simulate time based on size
                
                return "File downloaded: " + filename;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Download interrupted: " + filename;
            } finally {
                sample.stop();
                activeTransfers.remove(transferId);
            }
        }
        
        public Map<String, Object> getTransferStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("active_uploads", uploadTimer.activeTasks());
            stats.put("active_downloads", downloadTimer.activeTasks());
            stats.put("upload_duration_seconds", 
                TimeUnit.NANOSECONDS.toSeconds((long) uploadTimer.duration(TimeUnit.NANOSECONDS)));
            stats.put("download_duration_seconds", 
                TimeUnit.NANOSECONDS.toSeconds((long) downloadTimer.duration(TimeUnit.NANOSECONDS)));
            
            List<Map<String, Object>> transfers = new ArrayList<>();
            for (TransferProgress progress : activeTransfers.values()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", progress.transferId);
                p.put("filename", progress.filename);
                p.put("total_bytes", progress.totalBytes);
                p.put("transferred_bytes", progress.bytesTransferred);
                p.put("operation", progress.operation);
                p.put("duration_seconds", progress.sample.duration(TimeUnit.SECONDS));
                transfers.add(p);
            }
            stats.put("active_transfers", transfers);
            
            return stats;
        }
        
        private void uploadChunk(long bytes) {
            try {
                Thread.sleep(bytes / 10000); // Simulate upload time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        private static class TransferProgress {
            String transferId;
            String filename;
            long totalBytes;
            long bytesTransferred;
            String operation;
            LongTaskTimer.Sample sample;
            
            TransferProgress(String transferId, String filename, long totalBytes, 
                           String operation, LongTaskTimer.Sample sample) {
                this.transferId = transferId;
                this.filename = filename;
                this.totalBytes = totalBytes;
                this.bytesTransferred = 0;
                this.operation = operation;
                this.sample = sample;
            }
        }
    }

    // ============================================
    // Example 3: Report Generation
    // ============================================
    
    @Service
    public static class ReportGenerationService {
        
        private final LongTaskTimer reportTimer;
        private final Map<String, LongTaskTimer.Sample> activeReports = new ConcurrentHashMap<>();
        
        public ReportGenerationService(MeterRegistry registry) {
            this.reportTimer = LongTaskTimer.builder("report.generation")
                .description("Report generation duration")
                .tags("type", "report")
                .register(registry);
        }
        
        public String generateReport(String reportType, int recordCount) {
            LongTaskTimer.Sample sample = reportTimer.start();
            String reportId = reportType + "-" + System.currentTimeMillis();
            activeReports.put(reportId, sample);
            
            try {
                System.out.println("Starting report generation: " + reportType + 
                    " with " + recordCount + " records");
                
                // Phase 1: Data extraction
                System.out.println("Phase 1: Extracting data...");
                Thread.sleep(recordCount * 5); // Simulate data extraction
                
                // Phase 2: Data processing
                System.out.println("Phase 2: Processing data...");
                Thread.sleep(recordCount * 3); // Simulate processing
                
                // Phase 3: Report formatting
                System.out.println("Phase 3: Formatting report...");
                Thread.sleep(recordCount * 2); // Simulate formatting
                
                return "Report generated: " + reportId;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Report generation interrupted: " + reportId;
            } finally {
                long duration = sample.stop();
                activeReports.remove(reportId);
                System.out.println("Report completed in " + 
                    TimeUnit.NANOSECONDS.toMillis(duration) + " ms");
            }
        }
        
        public Map<String, Object> getReportStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("active_reports", reportTimer.activeTasks());
            stats.put("total_duration_seconds", 
                TimeUnit.NANOSECONDS.toSeconds((long) reportTimer.duration(TimeUnit.NANOSECONDS)));
            stats.put("max_duration_seconds", 
                TimeUnit.NANOSECONDS.toSeconds((long) reportTimer.max(TimeUnit.NANOSECONDS)));
            stats.put("active_report_ids", new ArrayList<>(activeReports.keySet()));
            return stats;
        }
    }

    // ============================================
    // Example 4: Data Migration
    // ============================================
    
    @Service
    public static class DataMigrationService {
        
        private final LongTaskTimer migrationTimer;
        private final MeterRegistry registry;
        
        public DataMigrationService(MeterRegistry registry) {
            this.registry = registry;
            this.migrationTimer = LongTaskTimer.builder("data.migration")
                .description("Data migration duration")
                .tags("type", "migration")
                .register(registry);
        }
        
        public String migrateData(String sourceTable, String targetTable, int recordCount) {
            // Create migration-specific timer with tags
            LongTaskTimer timer = LongTaskTimer.builder("data.migration.table")
                .description("Table migration duration")
                .tag("source", sourceTable)
                .tag("target", targetTable)
                .register(registry);
            
            LongTaskTimer.Sample sample = timer.start();
            
            try {
                System.out.println("Starting migration: " + sourceTable + 
                    " -> " + targetTable + " (" + recordCount + " records)");
                
                for (int i = 0; i < recordCount; i++) {
                    migrateRecord(i);
                    
                    if (i % 1000 == 0 && i > 0) {
                        long durationSec = sample.duration(TimeUnit.SECONDS);
                        double recordsPerSec = (double) i / Math.max(durationSec, 1);
                        System.out.println(String.format(
                            "Migration progress: %d/%d records (%.2f records/sec)",
                            i, recordCount, recordsPerSec));
                    }
                }
                
                return "Migration completed: " + sourceTable + " -> " + targetTable;
                
            } finally {
                sample.stop();
            }
        }
        
        private void migrateRecord(int recordId) {
            try {
                Thread.sleep(5); // Simulate migration work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ============================================
    // Example 5: Scheduled Background Tasks
    // ============================================
    
    @Service
    public static class ScheduledTaskService {
        
        private final LongTaskTimer scheduledTimer;
        
        public ScheduledTaskService(MeterRegistry registry) {
            this.scheduledTimer = LongTaskTimer.builder("scheduled.task")
                .description("Scheduled background task duration")
                .tags("type", "scheduled")
                .register(registry);
        }
        
        @Scheduled(fixedDelay = 60000) // Run every minute
        public void performMaintenance() {
            LongTaskTimer.Sample sample = scheduledTimer.start();
            
            try {
                System.out.println("Starting scheduled maintenance...");
                
                // Cleanup old records
                cleanupOldRecords();
                
                // Update statistics
                updateStatistics();
                
                // Optimize indexes
                optimizeIndexes();
                
                System.out.println("Scheduled maintenance completed");
                
            } finally {
                sample.stop();
            }
        }
        
        private void cleanupOldRecords() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        private void updateStatistics() {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        private void optimizeIndexes() {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ============================================
    // Example 6: Async Long-Running Operations
    // ============================================
    
    @Service
    public static class AsyncOperationService {
        
        private final LongTaskTimer asyncTimer;
        
        public AsyncOperationService(MeterRegistry registry) {
            this.asyncTimer = LongTaskTimer.builder("async.operation")
                .description("Async operation duration")
                .tags("type", "async")
                .register(registry);
        }
        
        @Async
        public CompletableFuture<String> processLargeDataset(String datasetId, int size) {
            LongTaskTimer.Sample sample = asyncTimer.start();
            
            try {
                System.out.println("Processing dataset async: " + datasetId);
                
                // Simulate processing
                Thread.sleep(size * 10);
                
                return CompletableFuture.completedFuture(
                    "Dataset processed: " + datasetId);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return CompletableFuture.completedFuture(
                    "Dataset processing interrupted: " + datasetId);
            } finally {
                sample.stop();
            }
        }
    }

    // ============================================
    // Example 7: Long Task Timer REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/longtask")
    public static class LongTaskController {
        
        private final BatchProcessingService batchService;
        private final FileTransferService fileService;
        private final ReportGenerationService reportService;
        private final DataMigrationService migrationService;
        private final AsyncOperationService asyncService;
        
        public LongTaskController(
                BatchProcessingService batchService,
                FileTransferService fileService,
                ReportGenerationService reportService,
                DataMigrationService migrationService,
                AsyncOperationService asyncService) {
            this.batchService = batchService;
            this.fileService = fileService;
            this.reportService = reportService;
            this.migrationService = migrationService;
            this.asyncService = asyncService;
        }
        
        @PostMapping("/batch")
        public Map<String, Object> startBatch(
                @RequestParam String batchId,
                @RequestParam(defaultValue = "1000") int recordCount) {
            String result = batchService.processBatch(batchId, recordCount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("result", result);
            response.putAll(batchService.getBatchStats());
            return response;
        }
        
        @GetMapping("/batch/stats")
        public Map<String, Object> getBatchStats() {
            return batchService.getBatchStats();
        }
        
        @PostMapping("/upload")
        public String uploadFile(
                @RequestParam String filename,
                @RequestParam(defaultValue = "10485760") long bytes) {
            return fileService.uploadFile(filename, bytes);
        }
        
        @PostMapping("/download")
        public String downloadFile(
                @RequestParam String filename,
                @RequestParam(defaultValue = "10485760") long bytes) {
            return fileService.downloadFile(filename, bytes);
        }
        
        @GetMapping("/transfer/stats")
        public Map<String, Object> getTransferStats() {
            return fileService.getTransferStats();
        }
        
        @PostMapping("/report")
        public String generateReport(
                @RequestParam String reportType,
                @RequestParam(defaultValue = "1000") int recordCount) {
            return reportService.generateReport(reportType, recordCount);
        }
        
        @GetMapping("/report/stats")
        public Map<String, Object> getReportStats() {
            return reportService.getReportStats();
        }
        
        @PostMapping("/migrate")
        public String migrateData(
                @RequestParam String sourceTable,
                @RequestParam String targetTable,
                @RequestParam(defaultValue = "10000") int recordCount) {
            return migrationService.migrateData(sourceTable, targetTable, recordCount);
        }
        
        @PostMapping("/async")
        public CompletableFuture<String> processAsync(
                @RequestParam String datasetId,
                @RequestParam(defaultValue = "100") int size) {
            return asyncService.processLargeDataset(datasetId, size);
        }
    }
}
