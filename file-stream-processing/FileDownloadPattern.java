package com.spring.patterns.filestream;

import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.*;

/**
 * File Download Pattern
 * 
 * Demonstrates various file download techniques in Spring applications:
 * - Basic file download
 * - Streaming large files
 * - Content-type handling
 * - Content-disposition headers
 * - Range/partial content support
 * - Zip file creation and download
 * - In-memory file generation
 * - Cached downloads
 * - Download progress tracking
 * 
 * Use Cases:
 * - Document downloads
 * - Report generation and download
 * - Image/video streaming
 * - File export functionality
 * - Backup file downloads
 * - Log file access
 * 
 * Dependencies:
 * - spring-boot-starter-web
 */

/**
 * Download Service
 */
@Service
class FileDownloadService {
    
    private final Path fileStoragePath;
    
    public FileDownloadService() throws IOException {
        this.fileStoragePath = Paths.get("downloads");
        Files.createDirectories(fileStoragePath);
    }
    
    /**
     * Get file as Resource
     */
    public Resource loadFileAsResource(String filename) throws IOException {
        Path filePath = fileStoragePath.resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new FileNotFoundException("File not found: " + filename);
        }
    }
    
    /**
     * Get file from classpath
     */
    public Resource loadClasspathResource(String path) {
        return new ClassPathResource(path);
    }
    
    /**
     * Create and return byte array resource
     */
    public Resource createInMemoryResource(String content, String filename) {
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayResource(data);
    }
    
    /**
     * Get file metadata
     */
    public FileMetadata getFileMetadata(String filename) throws IOException {
        Path filePath = fileStoragePath.resolve(filename);
        
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found: " + filename);
        }
        
        return new FileMetadata(
                filename,
                Files.size(filePath),
                Files.getLastModifiedTime(filePath).toMillis(),
                Files.probeContentType(filePath)
        );
    }
}

/**
 * File Metadata
 */
record FileMetadata(
        String filename,
        long size,
        long lastModified,
        String contentType
) {
    public String getFormattedSize() {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        }
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
    
    public String getFormattedDate() {
        return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(lastModified),
                java.time.ZoneId.systemDefault()
        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}

/**
 * Streaming File Service
 */
@Service
class StreamingFileService {
    
    /**
     * Create streaming response for large files
     */
    public StreamingResponseBody createStreamingResponse(Path filePath) {
        return outputStream -> {
            try (InputStream inputStream = Files.newInputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        };
    }
    
    /**
     * Stream with progress callback
     */
    public StreamingResponseBody createStreamingResponseWithProgress(
            Path filePath,
            ProgressCallback callback) {
        
        return outputStream -> {
            long fileSize = Files.size(filePath);
            long bytesWritten = 0;
            
            try (InputStream inputStream = Files.newInputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    bytesWritten += bytesRead;
                    
                    if (callback != null) {
                        callback.onProgress(bytesWritten, fileSize);
                    }
                }
                outputStream.flush();
            }
        };
    }
    
    @FunctionalInterface
    interface ProgressCallback {
        void onProgress(long bytesWritten, long totalBytes);
    }
}

/**
 * Range Request Support Service
 */
@Service
class RangeRequestService {
    
    /**
     * Handle range request for partial content
     */
    public ResponseEntity<Resource> handleRangeRequest(
            Resource resource,
            HttpHeaders headers) throws IOException {
        
        String rangeHeader = headers.getFirst(HttpHeaders.RANGE);
        
        if (rangeHeader == null || !rangeHeader.startsWith("bytes=")) {
            // Return full content
            return ResponseEntity.ok()
                    .contentLength(resource.contentLength())
                    .body(resource);
        }
        
        // Parse range
        String[] ranges = rangeHeader.substring(6).split("-");
        long start = Long.parseLong(ranges[0]);
        long end = ranges.length > 1 && !ranges[1].isEmpty() 
                ? Long.parseLong(ranges[1]) 
                : resource.contentLength() - 1;
        
        long rangeLength = end - start + 1;
        
        // Create range resource
        InputStream inputStream = resource.getInputStream();
        inputStream.skip(start);
        
        byte[] data = new byte[(int) rangeLength];
        inputStream.read(data);
        
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_RANGE, 
                String.format("bytes %d-%d/%d", start, end, resource.contentLength()));
        responseHeaders.setContentLength(rangeLength);
        
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .headers(responseHeaders)
                .body(new ByteArrayResource(data));
    }
}

/**
 * Zip File Service
 */
@Service
class ZipFileService {
    
    /**
     * Create zip file from multiple files
     */
    public byte[] createZipArchive(Map<String, byte[]> files) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, byte[]> entry : files.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zos.putNextEntry(zipEntry);
                zos.write(entry.getValue());
                zos.closeEntry();
            }
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Create zip from directory
     */
    public byte[] createZipFromDirectory(Path sourceDir) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            Files.walk(sourceDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            String zipEntryName = sourceDir.relativize(path).toString();
                            ZipEntry zipEntry = new ZipEntry(zipEntryName);
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Stream zip creation for large archives
     */
    public StreamingResponseBody streamZipArchive(List<Path> files) {
        return outputStream -> {
            try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                for (Path file : files) {
                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                }
            }
        };
    }
}

/**
 * Report Generator Service
 */
@Service
class ReportGeneratorService {
    
    /**
     * Generate CSV report
     */
    public byte[] generateCsvReport(List<Map<String, Object>> data) {
        StringBuilder csv = new StringBuilder();
        
        if (!data.isEmpty()) {
            // Header
            Map<String, Object> first = data.get(0);
            csv.append(String.join(",", first.keySet())).append("\n");
            
            // Data rows
            for (Map<String, Object> row : data) {
                csv.append(String.join(",", 
                        row.values().stream()
                                .map(Object::toString)
                                .toArray(String[]::new)))
                        .append("\n");
            }
        }
        
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Generate text report
     */
    public byte[] generateTextReport(String title, List<String> lines) {
        StringBuilder report = new StringBuilder();
        
        // Title
        report.append("=".repeat(60)).append("\n");
        report.append(title).append("\n");
        report.append("Generated: ").append(LocalDateTime.now()).append("\n");
        report.append("=".repeat(60)).append("\n\n");
        
        // Content
        for (String line : lines) {
            report.append(line).append("\n");
        }
        
        return report.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Generate JSON report
     */
    public byte[] generateJsonReport(Object data) {
        // Simple JSON serialization (use Jackson in real apps)
        String json = convertToJson(data);
        return json.getBytes(StandardCharsets.UTF_8);
    }
    
    private String convertToJson(Object obj) {
        // Simplified JSON conversion for demo
        return "{\"data\": \"" + obj.toString() + "\"}";
    }
}

/**
 * File Download REST Controller
 */
@RestController
@RequestMapping("/api/download")
class FileDownloadController {
    
    private final FileDownloadService downloadService;
    private final StreamingFileService streamingService;
    private final ZipFileService zipService;
    private final ReportGeneratorService reportService;
    
    public FileDownloadController(FileDownloadService downloadService,
                                 StreamingFileService streamingService,
                                 ZipFileService zipService,
                                 ReportGeneratorService reportService) {
        this.downloadService = downloadService;
        this.streamingService = streamingService;
        this.zipService = zipService;
        this.reportService = reportService;
    }
    
    /**
     * Basic file download
     */
    @GetMapping("/file/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) 
            throws IOException {
        
        Resource resource = downloadService.loadFileAsResource(filename);
        FileMetadata metadata = downloadService.getFileMetadata(filename);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + encodeFilename(filename) + "\"")
                .contentLength(metadata.size())
                .body(resource);
    }
    
    /**
     * Inline file display (for images, PDFs)
     */
    @GetMapping("/view/{filename:.+}")
    public ResponseEntity<Resource> viewFile(@PathVariable String filename) 
            throws IOException {
        
        Resource resource = downloadService.loadFileAsResource(filename);
        FileMetadata metadata = downloadService.getFileMetadata(filename);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "inline; filename=\"" + encodeFilename(filename) + "\"")
                .body(resource);
    }
    
    /**
     * Streaming download for large files
     */
    @GetMapping("/stream/{filename:.+}")
    public ResponseEntity<StreamingResponseBody> streamFile(@PathVariable String filename) 
            throws IOException {
        
        Path filePath = Paths.get("downloads").resolve(filename);
        FileMetadata metadata = downloadService.getFileMetadata(filename);
        
        StreamingResponseBody stream = streamingService.createStreamingResponse(filePath);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + encodeFilename(filename) + "\"")
                .body(stream);
    }
    
    /**
     * Download multiple files as zip
     */
    @GetMapping("/zip")
    public ResponseEntity<byte[]> downloadAsZip(
            @RequestParam List<String> filenames) throws IOException {
        
        Map<String, byte[]> files = new HashMap<>();
        
        for (String filename : filenames) {
            Resource resource = downloadService.loadFileAsResource(filename);
            files.put(filename, resource.getInputStream().readAllBytes());
        }
        
        byte[] zipData = zipService.createZipArchive(files);
        
        String zipFilename = "download_" + System.currentTimeMillis() + ".zip";
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + zipFilename + "\"")
                .body(zipData);
    }
    
    /**
     * Generate and download CSV report
     */
    @GetMapping("/report/csv")
    public ResponseEntity<byte[]> downloadCsvReport() {
        List<Map<String, Object>> data = Arrays.asList(
                Map.of("id", "1", "name", "Item 1", "value", "100"),
                Map.of("id", "2", "name", "Item 2", "value", "200"),
                Map.of("id", "3", "name", "Item 3", "value", "300")
        );
        
        byte[] csv = reportService.generateCsvReport(data);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"report.csv\"")
                .body(csv);
    }
    
    /**
     * Generate and download text report
     */
    @GetMapping("/report/text")
    public ResponseEntity<byte[]> downloadTextReport() {
        List<String> lines = Arrays.asList(
                "Summary Report",
                "",
                "Total Items: 100",
                "Total Value: $10,000",
                "Average: $100",
                "",
                "Details:",
                "- Category A: 40 items",
                "- Category B: 35 items",
                "- Category C: 25 items"
        );
        
        byte[] report = reportService.generateTextReport("Monthly Report", lines);
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"report.txt\"")
                .body(report);
    }
    
    /**
     * Download with custom response
     */
    @GetMapping("/custom/{filename:.+}")
    public void downloadWithServletResponse(
            @PathVariable String filename,
            HttpServletResponse response) throws IOException {
        
        Resource resource = downloadService.loadFileAsResource(filename);
        FileMetadata metadata = downloadService.getFileMetadata(filename);
        
        response.setContentType(metadata.contentType());
        response.setContentLengthLong(metadata.size());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + encodeFilename(filename) + "\"");
        
        try (InputStream is = resource.getInputStream();
             OutputStream os = response.getOutputStream()) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }
    
    /**
     * Get file metadata without downloading
     */
    @GetMapping("/info/{filename:.+}")
    public ResponseEntity<FileMetadata> getFileInfo(@PathVariable String filename) 
            throws IOException {
        
        FileMetadata metadata = downloadService.getFileMetadata(filename);
        return ResponseEntity.ok(metadata);
    }
    
    private String encodeFilename(String filename) {
        try {
            return URLEncoder.encode(filename, StandardCharsets.UTF_8)
                    .replace("+", "%20");
        } catch (Exception e) {
            return filename;
        }
    }
}

/**
 * Content Type Resolver
 */
@Service
class ContentTypeResolver {
    
    private static final Map<String, String> CONTENT_TYPES = Map.ofEntries(
            Map.entry("txt", "text/plain"),
            Map.entry("pdf", "application/pdf"),
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"),
            Map.entry("gif", "image/gif"),
            Map.entry("csv", "text/csv"),
            Map.entry("json", "application/json"),
            Map.entry("xml", "application/xml"),
            Map.entry("zip", "application/zip"),
            Map.entry("doc", "application/msword"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("xls", "application/vnd.ms-excel"),
            Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    );
    
    public String getContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return CONTENT_TYPES.getOrDefault(extension, "application/octet-stream");
    }
    
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }
}

/**
 * File Download Pattern - Main Demonstration
 */
public class FileDownloadPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== File Download Pattern Demo ===\n");
        
        // 1. Basic File Download
        demonstrateBasicDownload();
        
        // 2. Streaming Download
        demonstrateStreamingDownload();
        
        // 3. Zip Archive Download
        demonstrateZipDownload();
        
        // 4. Report Generation
        demonstrateReportGeneration();
        
        // 5. Content Type Handling
        demonstrateContentTypes();
        
        // 6. File Metadata
        demonstrateMetadata();
    }
    
    private static void demonstrateBasicDownload() throws IOException {
        System.out.println("1. Basic File Download:");
        
        FileDownloadService service = new FileDownloadService();
        
        // Create test file
        Path testFile = Paths.get("downloads/test.txt");
        Files.createDirectories(testFile.getParent());
        Files.write(testFile, "Hello, World!".getBytes());
        
        Resource resource = service.loadFileAsResource("test.txt");
        System.out.println("File loaded: " + resource.getFilename());
        System.out.println("File exists: " + resource.exists());
        System.out.println("Content length: " + resource.contentLength());
        
        System.out.println();
    }
    
    private static void demonstrateStreamingDownload() {
        System.out.println("2. Streaming Download:");
        
        System.out.println("Streaming is ideal for:");
        System.out.println("- Large files (>100MB)");
        System.out.println("- Video/audio files");
        System.out.println("- Database exports");
        System.out.println("- Log files");
        System.out.println();
        System.out.println("Benefits:");
        System.out.println("- Low memory footprint");
        System.out.println("- Supports progress tracking");
        System.out.println("- Can be resumed");
        
        System.out.println();
    }
    
    private static void demonstrateZipDownload() throws IOException {
        System.out.println("3. Zip Archive Download:");
        
        ZipFileService zipService = new ZipFileService();
        
        Map<String, byte[]> files = Map.of(
                "file1.txt", "Content 1".getBytes(),
                "file2.txt", "Content 2".getBytes(),
                "folder/file3.txt", "Content 3".getBytes()
        );
        
        byte[] zipData = zipService.createZipArchive(files);
        System.out.println("Created zip archive: " + zipData.length + " bytes");
        System.out.println("Contains " + files.size() + " files");
        
        System.out.println();
    }
    
    private static void demonstrateReportGeneration() {
        System.out.println("4. Report Generation:");
        
        ReportGeneratorService reportService = new ReportGeneratorService();
        
        List<Map<String, Object>> data = Arrays.asList(
                Map.of("name", "Product A", "price", "100", "stock", "50"),
                Map.of("name", "Product B", "price", "200", "stock", "30")
        );
        
        byte[] csv = reportService.generateCsvReport(data);
        System.out.println("CSV Report generated: " + csv.length + " bytes");
        System.out.println("Content preview:");
        System.out.println(new String(csv, StandardCharsets.UTF_8));
        
        System.out.println();
    }
    
    private static void demonstrateContentTypes() {
        System.out.println("5. Content Type Handling:");
        
        ContentTypeResolver resolver = new ContentTypeResolver();
        
        String[] files = {"document.pdf", "image.jpg", "data.csv", "archive.zip"};
        
        for (String file : files) {
            String contentType = resolver.getContentType(file);
            System.out.println(file + " -> " + contentType);
        }
        
        System.out.println();
    }
    
    private static void demonstrateMetadata() throws IOException {
        System.out.println("6. File Metadata:");
        
        FileDownloadService service = new FileDownloadService();
        
        Path testFile = Paths.get("downloads/test.txt");
        if (Files.exists(testFile)) {
            FileMetadata metadata = service.getFileMetadata("test.txt");
            
            System.out.println("Filename: " + metadata.filename());
            System.out.println("Size: " + metadata.getFormattedSize());
            System.out.println("Last Modified: " + metadata.getFormattedDate());
            System.out.println("Content Type: " + metadata.contentType());
        }
        
        System.out.println("\n=== Demo Complete ===");
    }
}
