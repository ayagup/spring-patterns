package com.example.sse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Streaming Response Body Pattern
 * 
 * Purpose: Stream large amounts of data or continuous updates using StreamingResponseBody.
 * Provides direct control over the output stream for efficient data transfer.
 * 
 * Key Features:
 * - Direct access to output stream
 * - Efficient for large file transfers
 * - Asynchronous streaming
 * - Memory-efficient chunk processing
 * - Custom content type support
 * - Progress tracking capability
 * 
 * Use Cases:
 * - File downloads (large files)
 * - Video/audio streaming
 * - CSV/Excel export
 * - Log file streaming
 * - Database export
 * - Real-time data export
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class StreamingResponseBodyPattern {

    public static void main(String[] args) {
        SpringApplication.run(StreamingResponseBodyPattern.class, args);
    }

    /**
     * Streaming Controller - Endpoints for streaming responses
     */
    @RestController
    @RequestMapping("/api/stream")
    public static class StreamingController {

        private final StreamingService streamingService;

        public StreamingController(StreamingService streamingService) {
            this.streamingService = streamingService;
        }

        /**
         * Stream text data
         */
        @GetMapping("/text")
        public ResponseEntity<StreamingResponseBody> streamText() {
            StreamingResponseBody stream = outputStream -> {
                try {
                    for (int i = 1; i <= 100; i++) {
                        String line = "Line " + i + " - " + 
                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + "\n";
                        outputStream.write(line.getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                        
                        // Simulate processing delay
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Streaming interrupted", e);
                }
            };

            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(stream);
        }

        /**
         * Stream CSV data
         */
        @GetMapping("/csv")
        public ResponseEntity<StreamingResponseBody> streamCSV(
                @RequestParam(defaultValue = "1000") int recordCount) {
            
            StreamingResponseBody stream = outputStream -> {
                try {
                    // Write CSV header
                    String header = "ID,Name,Email,CreatedAt\n";
                    outputStream.write(header.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();

                    // Stream data in chunks
                    int chunkSize = 100;
                    for (int i = 0; i < recordCount; i++) {
                        String record = streamingService.generateCSVRecord(i + 1);
                        outputStream.write(record.getBytes(StandardCharsets.UTF_8));
                        
                        if ((i + 1) % chunkSize == 0) {
                            outputStream.flush();
                            Thread.sleep(10); // Small delay between chunks
                        }
                    }
                    
                    outputStream.flush();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("CSV streaming interrupted", e);
                }
            };

            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(stream);
        }

        /**
         * Stream JSON data
         */
        @GetMapping("/json")
        public ResponseEntity<StreamingResponseBody> streamJSON(
                @RequestParam(defaultValue = "100") int itemCount) {
            
            StreamingResponseBody stream = outputStream -> {
                try {
                    // Start JSON array
                    outputStream.write("[".getBytes(StandardCharsets.UTF_8));
                    
                    for (int i = 0; i < itemCount; i++) {
                        String jsonItem = streamingService.generateJSONItem(i + 1);
                        outputStream.write(jsonItem.getBytes(StandardCharsets.UTF_8));
                        
                        if (i < itemCount - 1) {
                            outputStream.write(",".getBytes(StandardCharsets.UTF_8));
                        }
                        
                        if ((i + 1) % 10 == 0) {
                            outputStream.flush();
                        }
                    }
                    
                    // Close JSON array
                    outputStream.write("]".getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                } catch (Exception e) {
                    throw new IOException("JSON streaming error", e);
                }
            };

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(stream);
        }

        /**
         * Stream large file
         */
        @GetMapping("/file")
        public ResponseEntity<StreamingResponseBody> streamFile(
                @RequestParam(defaultValue = "10") int sizeMB) {
            
            StreamingResponseBody stream = outputStream -> {
                try {
                    int chunkSize = 8192; // 8KB chunks
                    byte[] buffer = new byte[chunkSize];
                    long totalBytes = (long) sizeMB * 1024 * 1024;
                    long written = 0;

                    // Fill buffer with sample data
                    String sampleText = "This is sample file content. ";
                    byte[] sampleBytes = sampleText.getBytes(StandardCharsets.UTF_8);
                    Arrays.fill(buffer, (byte) 0);
                    System.arraycopy(sampleBytes, 0, buffer, 0, 
                        Math.min(sampleBytes.length, buffer.length));

                    while (written < totalBytes) {
                        int toWrite = (int) Math.min(chunkSize, totalBytes - written);
                        outputStream.write(buffer, 0, toWrite);
                        written += toWrite;
                        
                        // Flush periodically
                        if (written % (chunkSize * 10) == 0) {
                            outputStream.flush();
                        }
                    }
                    
                    outputStream.flush();
                } catch (Exception e) {
                    throw new IOException("File streaming error", e);
                }
            };

            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=large-file.txt")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(stream);
        }

        /**
         * Stream log data
         */
        @GetMapping("/logs")
        public ResponseEntity<StreamingResponseBody> streamLogs(
                @RequestParam(defaultValue = "50") int lineCount) {
            
            StreamingResponseBody stream = outputStream -> {
                try {
                    for (int i = 0; i < lineCount; i++) {
                        LogEntry logEntry = streamingService.generateLogEntry(i + 1);
                        String logLine = logEntry.toString() + "\n";
                        outputStream.write(logLine.getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                        
                        Thread.sleep(50); // Simulate real-time log generation
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Log streaming interrupted", e);
                }
            };

            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header("X-Content-Type-Options", "nosniff")
                .body(stream);
        }

        /**
         * Stream database export
         */
        @GetMapping("/export")
        public ResponseEntity<StreamingResponseBody> exportData(
                @RequestParam String format,
                @RequestParam(defaultValue = "1000") int recordCount) {
            
            StreamingResponseBody stream = outputStream -> {
                try {
                    if ("csv".equalsIgnoreCase(format)) {
                        streamingService.exportAsCSV(outputStream, recordCount);
                    } else if ("json".equalsIgnoreCase(format)) {
                        streamingService.exportAsJSON(outputStream, recordCount);
                    } else if ("xml".equalsIgnoreCase(format)) {
                        streamingService.exportAsXML(outputStream, recordCount);
                    } else {
                        throw new IllegalArgumentException("Unsupported format: " + format);
                    }
                } catch (Exception e) {
                    throw new IOException("Export streaming error", e);
                }
            };

            String filename = "export." + format.toLowerCase();
            MediaType mediaType = getMediaTypeForFormat(format);

            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .contentType(mediaType)
                .body(stream);
        }

        /**
         * Stream with progress updates (via custom headers)
         */
        @GetMapping("/progress")
        public ResponseEntity<StreamingResponseBody> streamWithProgress(
                @RequestParam(defaultValue = "100") int totalItems) {
            
            StreamingResponseBody stream = outputStream -> {
                try {
                    for (int i = 0; i < totalItems; i++) {
                        String item = "Item " + (i + 1) + "\n";
                        outputStream.write(item.getBytes(StandardCharsets.UTF_8));
                        
                        // Calculate progress
                        int progress = (int) (((i + 1) / (double) totalItems) * 100);
                        
                        // Note: Cannot set headers after streaming starts
                        // Progress tracking should be done via separate endpoint or WebSocket
                        
                        outputStream.flush();
                        Thread.sleep(50);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Progress streaming interrupted", e);
                }
            };

            return ResponseEntity.ok()
                .header("X-Total-Items", String.valueOf(totalItems))
                .contentType(MediaType.TEXT_PLAIN)
                .body(stream);
        }

        private MediaType getMediaTypeForFormat(String format) {
            switch (format.toLowerCase()) {
                case "csv":
                    return MediaType.parseMediaType("text/csv");
                case "json":
                    return MediaType.APPLICATION_JSON;
                case "xml":
                    return MediaType.APPLICATION_XML;
                default:
                    return MediaType.APPLICATION_OCTET_STREAM;
            }
        }
    }

    /**
     * Streaming Service - Business logic for streaming operations
     */
    @Service
    public static class StreamingService {

        private final Random random = new Random();

        /**
         * Generate CSV record
         */
        public String generateCSVRecord(int id) {
            return String.format("%d,User%d,user%d@example.com,%s\n",
                id,
                id,
                id,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
        }

        /**
         * Generate JSON item
         */
        public String generateJSONItem(int id) {
            return String.format(
                "{\"id\":%d,\"name\":\"User%d\",\"email\":\"user%d@example.com\",\"createdAt\":\"%s\"}",
                id,
                id,
                id,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
        }

        /**
         * Generate log entry
         */
        public LogEntry generateLogEntry(int sequence) {
            String[] levels = {"INFO", "DEBUG", "WARN", "ERROR"};
            String[] messages = {
                "Application started successfully",
                "Processing request",
                "Database connection established",
                "Cache updated",
                "Background job completed"
            };

            return new LogEntry(
                LocalDateTime.now(),
                levels[random.nextInt(levels.length)],
                "Service" + (random.nextInt(5) + 1),
                messages[random.nextInt(messages.length)] + " [" + sequence + "]"
            );
        }

        /**
         * Export as CSV
         */
        public void exportAsCSV(OutputStream outputStream, int recordCount) throws IOException {
            // Header
            String header = "ID,Name,Email,Status,CreatedAt\n";
            outputStream.write(header.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            // Data rows
            for (int i = 0; i < recordCount; i++) {
                String row = String.format("%d,User%d,user%d@example.com,%s,%s\n",
                    i + 1,
                    i + 1,
                    i + 1,
                    random.nextBoolean() ? "ACTIVE" : "INACTIVE",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                );
                outputStream.write(row.getBytes(StandardCharsets.UTF_8));
                
                if ((i + 1) % 100 == 0) {
                    outputStream.flush();
                }
            }
            
            outputStream.flush();
        }

        /**
         * Export as JSON
         */
        public void exportAsJSON(OutputStream outputStream, int recordCount) throws IOException {
            outputStream.write("{\"data\":[".getBytes(StandardCharsets.UTF_8));

            for (int i = 0; i < recordCount; i++) {
                String item = String.format(
                    "{\"id\":%d,\"name\":\"User%d\",\"email\":\"user%d@example.com\",\"status\":\"%s\",\"createdAt\":\"%s\"}",
                    i + 1,
                    i + 1,
                    i + 1,
                    random.nextBoolean() ? "ACTIVE" : "INACTIVE",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                );
                outputStream.write(item.getBytes(StandardCharsets.UTF_8));
                
                if (i < recordCount - 1) {
                    outputStream.write(",".getBytes(StandardCharsets.UTF_8));
                }
                
                if ((i + 1) % 100 == 0) {
                    outputStream.flush();
                }
            }

            outputStream.write("]}".getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }

        /**
         * Export as XML
         */
        public void exportAsXML(OutputStream outputStream, int recordCount) throws IOException {
            outputStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<users>\n"
                .getBytes(StandardCharsets.UTF_8));

            for (int i = 0; i < recordCount; i++) {
                String user = String.format(
                    "  <user>\n" +
                    "    <id>%d</id>\n" +
                    "    <name>User%d</name>\n" +
                    "    <email>user%d@example.com</email>\n" +
                    "    <status>%s</status>\n" +
                    "    <createdAt>%s</createdAt>\n" +
                    "  </user>\n",
                    i + 1,
                    i + 1,
                    i + 1,
                    random.nextBoolean() ? "ACTIVE" : "INACTIVE",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                );
                outputStream.write(user.getBytes(StandardCharsets.UTF_8));
                
                if ((i + 1) % 100 == 0) {
                    outputStream.flush();
                }
            }

            outputStream.write("</users>".getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
    }

    /**
     * Log Entry Model
     */
    public static class LogEntry {
        private LocalDateTime timestamp;
        private String level;
        private String service;
        private String message;

        public LogEntry() {}

        public LogEntry(LocalDateTime timestamp, String level, String service, String message) {
            this.timestamp = timestamp;
            this.level = level;
            this.service = service;
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("[%s] [%s] [%s] %s",
                timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                level,
                service,
                message
            );
        }

        // Getters and Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }

        public String getService() { return service; }
        public void setService(String service) { this.service = service; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * HTML Demo Page
     */
    @Controller
    public static class WebController {
        
        @GetMapping("/")
        public String index() {
            return "streaming-demo";
        }
    }
}

/*
 * Client-Side JavaScript Example:
 * 
 * // Stream text data
 * fetch('/api/stream/text')
 *     .then(response => {
 *         const reader = response.body.getReader();
 *         const decoder = new TextDecoder();
 *         
 *         function read() {
 *             reader.read().then(({done, value}) => {
 *                 if (done) {
 *                     console.log('Stream complete');
 *                     return;
 *                 }
 *                 
 *                 const chunk = decoder.decode(value, {stream: true});
 *                 console.log('Received chunk:', chunk);
 *                 
 *                 read();
 *             });
 *         }
 *         
 *         read();
 *     });
 * 
 * // Download streamed file
 * function downloadStream(url, filename) {
 *     fetch(url)
 *         .then(response => response.blob())
 *         .then(blob => {
 *             const url = window.URL.createObjectURL(blob);
 *             const a = document.createElement('a');
 *             a.href = url;
 *             a.download = filename;
 *             document.body.appendChild(a);
 *             a.click();
 *             window.URL.revokeObjectURL(url);
 *             a.remove();
 *         });
 * }
 * 
 * downloadStream('/api/stream/csv?recordCount=1000', 'export.csv');
 * 
 * // Stream JSON with progress
 * async function streamJSON() {
 *     const response = await fetch('/api/stream/json?itemCount=100');
 *     const reader = response.body.getReader();
 *     const decoder = new TextDecoder();
 *     let result = '';
 *     
 *     while (true) {
 *         const {done, value} = await reader.read();
 *         if (done) break;
 *         
 *         result += decoder.decode(value, {stream: true});
 *     }
 *     
 *     const data = JSON.parse(result);
 *     console.log('Received data:', data);
 * }
 */

/*
 * Application Properties:
 * 
 * # Server configuration
 * server.port=8080
 * 
 * # Async configuration
 * spring.mvc.async.request-timeout=600000
 * 
 * # Max file size for uploads (if needed)
 * spring.servlet.multipart.max-file-size=100MB
 * spring.servlet.multipart.max-request-size=100MB
 */
