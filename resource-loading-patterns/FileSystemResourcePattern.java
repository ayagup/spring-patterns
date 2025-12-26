package com.example.resource;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * File System Resource Pattern
 * =============================
 * 
 * Demonstrates FileSystemResource for loading resources from the
 * file system using absolute or relative paths.
 * 
 * Key Concepts:
 * ------------
 * 1. FileSystemResource - File system-based resource access
 * 2. Absolute Paths - Full path from root
 * 3. Relative Paths - Relative to working directory
 * 4. Read/Write Support - Both read and write operations
 * 5. File Metadata - Access file properties
 * 
 * Path Types:
 * ----------
 * - Absolute: "/var/app/config.properties", "C:/data/file.txt"
 * - Relative: "config/app.properties", "../data/file.txt"
 * - User Home: "~/config/settings.json"
 * 
 * When to Use:
 * -----------
 * - External configuration files
 * - Log file processing
 * - User-uploaded files
 * - Temporary file operations
 * - Write operations needed
 * - Mutable resources
 * 
 * Advantages:
 * ----------
 * - Direct file system access
 * - Read and write support
 * - File metadata access
 * - Works with external files
 * - Directory operations
 * 
 * Best Practices:
 * --------------
 * - Use absolute paths in production
 * - Check file existence first
 * - Handle file permissions
 * - Close streams properly
 * - Use Path API for path manipulation
 * - Validate file locations
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class FileSystemResourcePattern {
    
    /**
     * Load file from absolute path
     */
    public String loadFromAbsolutePath(String absolutePath) throws IOException {
        FileSystemResource resource = new FileSystemResource(absolutePath);
        
        if (!resource.exists()) {
            throw new IOException("File not found: " + absolutePath);
        }
        
        return readResource(resource);
    }
    
    /**
     * Load file from relative path
     */
    public String loadFromRelativePath(String relativePath) throws IOException {
        FileSystemResource resource = new FileSystemResource(relativePath);
        
        if (!resource.exists()) {
            throw new IOException("File not found: " + relativePath);
        }
        
        return readResource(resource);
    }
    
    /**
     * Load file using Path object
     */
    public String loadFromPath(Path path) throws IOException {
        FileSystemResource resource = new FileSystemResource(path);
        
        if (!resource.exists()) {
            throw new IOException("File not found: " + path);
        }
        
        return readResource(resource);
    }
    
    /**
     * Load file using File object
     */
    public String loadFromFile(File file) throws IOException {
        FileSystemResource resource = new FileSystemResource(file);
        
        if (!resource.exists()) {
            throw new IOException("File not found: " + file.getAbsolutePath());
        }
        
        return readResource(resource);
    }
    
    private String readResource(FileSystemResource resource) throws IOException {
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}

/**
 * Example 2: Configuration File Manager
 */
@Component
class ConfigurationFileManager {
    
    private final Path configDir;
    
    public ConfigurationFileManager() {
        // Default to user's home directory
        this.configDir = Paths.get(System.getProperty("user.home"), ".myapp");
    }
    
    public ConfigurationFileManager(String configDirectory) {
        this.configDir = Paths.get(configDirectory);
    }
    
    /**
     * Load configuration file
     */
    public String loadConfig(String filename) throws IOException {
        Path configPath = configDir.resolve(filename);
        FileSystemResource resource = new FileSystemResource(configPath);
        
        if (!resource.exists()) {
            throw new IOException("Configuration not found: " + filename);
        }
        
        return readFile(resource);
    }
    
    /**
     * Save configuration file
     */
    public void saveConfig(String filename, String content) throws IOException {
        Path configPath = configDir.resolve(filename);
        
        // Create directory if it doesn't exist
        Files.createDirectories(configPath.getParent());
        
        FileSystemResource resource = new FileSystemResource(configPath);
        
        try (OutputStream os = resource.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }
    
    /**
     * Delete configuration file
     */
    public boolean deleteConfig(String filename) throws IOException {
        Path configPath = configDir.resolve(filename);
        return Files.deleteIfExists(configPath);
    }
    
    private String readFile(FileSystemResource resource) throws IOException {
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}

/**
 * Example 3: Log File Reader
 */
@Component
class LogFileReader {
    
    /**
     * Read entire log file
     */
    public List<String> readLogFile(String logPath) throws IOException {
        FileSystemResource resource = new FileSystemResource(logPath);
        
        if (!resource.exists()) {
            throw new IOException("Log file not found: " + logPath);
        }
        
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.toList());
        }
    }
    
    /**
     * Read last N lines from log file
     */
    public List<String> readLastLines(String logPath, int n) throws IOException {
        FileSystemResource resource = new FileSystemResource(logPath);
        
        List<String> allLines = readLogFile(logPath);
        int start = Math.max(0, allLines.size() - n);
        
        return allLines.subList(start, allLines.size());
    }
    
    /**
     * Filter log entries by level
     */
    public List<String> filterByLevel(String logPath, String level) throws IOException {
        return readLogFile(logPath).stream()
            .filter(line -> line.contains("[" + level + "]"))
            .collect(Collectors.toList());
    }
}

/**
 * Example 4: File Upload Handler
 */
@Component
class FileUploadHandler {
    
    private final Path uploadDir;
    
    public FileUploadHandler() {
        this.uploadDir = Paths.get("uploads");
    }
    
    public FileUploadHandler(String uploadDirectory) {
        this.uploadDir = Paths.get(uploadDirectory);
    }
    
    /**
     * Save uploaded file
     */
    public String saveUpload(String filename, byte[] content) throws IOException {
        // Create upload directory if needed
        Files.createDirectories(uploadDir);
        
        Path filePath = uploadDir.resolve(filename);
        FileSystemResource resource = new FileSystemResource(filePath);
        
        try (OutputStream os = resource.getOutputStream()) {
            os.write(content);
        }
        
        return filePath.toAbsolutePath().toString();
    }
    
    /**
     * Read uploaded file
     */
    public byte[] readUpload(String filename) throws IOException {
        Path filePath = uploadDir.resolve(filename);
        FileSystemResource resource = new FileSystemResource(filePath);
        
        if (!resource.exists()) {
            throw new IOException("Upload not found: " + filename);
        }
        
        try (InputStream is = resource.getInputStream()) {
            return is.readAllBytes();
        }
    }
    
    /**
     * Delete uploaded file
     */
    public boolean deleteUpload(String filename) throws IOException {
        Path filePath = uploadDir.resolve(filename);
        return Files.deleteIfExists(filePath);
    }
}

/**
 * Example 5: Temporary File Manager
 */
@Component
class TemporaryFileManager {
    
    /**
     * Create temporary file with content
     */
    public FileSystemResource createTempFile(String prefix, String suffix, 
                                            String content) throws IOException {
        Path tempFile = Files.createTempFile(prefix, suffix);
        FileSystemResource resource = new FileSystemResource(tempFile);
        
        try (OutputStream os = resource.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            writer.write(content);
        }
        
        return resource;
    }
    
    /**
     * Create temporary binary file
     */
    public FileSystemResource createTempBinaryFile(String prefix, String suffix, 
                                                   byte[] content) throws IOException {
        Path tempFile = Files.createTempFile(prefix, suffix);
        FileSystemResource resource = new FileSystemResource(tempFile);
        
        try (OutputStream os = resource.getOutputStream()) {
            os.write(content);
        }
        
        return resource;
    }
    
    /**
     * Delete temporary file
     */
    public void deleteTempFile(FileSystemResource resource) throws IOException {
        if (resource.exists()) {
            Files.delete(resource.getFile().toPath());
        }
    }
}

/**
 * Example 6: File Metadata Reader
 */
@Component
class FileMetadataReader {
    
    /**
     * Get file metadata
     */
    public FileMetadata getMetadata(String filePath) throws IOException {
        FileSystemResource resource = new FileSystemResource(filePath);
        
        if (!resource.exists()) {
            throw new IOException("File not found: " + filePath);
        }
        
        File file = resource.getFile();
        
        return new FileMetadata(
            file.getName(),
            file.getAbsolutePath(),
            file.length(),
            file.lastModified(),
            file.canRead(),
            file.canWrite(),
            file.isFile(),
            file.isDirectory()
        );
    }
    
    /**
     * Check if file is writable
     */
    public boolean isWritable(String filePath) {
        FileSystemResource resource = new FileSystemResource(filePath);
        return resource.exists() && resource.isWritable();
    }
}

/**
 * File metadata holder
 */
class FileMetadata {
    private final String name;
    private final String absolutePath;
    private final long size;
    private final long lastModified;
    private final boolean readable;
    private final boolean writable;
    private final boolean isFile;
    private final boolean isDirectory;
    
    public FileMetadata(String name, String absolutePath, long size, 
                       long lastModified, boolean readable, boolean writable,
                       boolean isFile, boolean isDirectory) {
        this.name = name;
        this.absolutePath = absolutePath;
        this.size = size;
        this.lastModified = lastModified;
        this.readable = readable;
        this.writable = writable;
        this.isFile = isFile;
        this.isDirectory = isDirectory;
    }
    
    @Override
    public String toString() {
        return String.format("FileMetadata{name='%s', path='%s', size=%d, lastModified=%d, " +
                           "readable=%s, writable=%s, isFile=%s, isDir=%s}",
                           name, absolutePath, size, lastModified, readable, writable, 
                           isFile, isDirectory);
    }
}

/**
 * Example 7: Directory Scanner
 */
@Component
class DirectoryScanner {
    
    /**
     * List files in directory
     */
    public List<FileSystemResource> listFiles(String directoryPath) throws IOException {
        Path dir = Paths.get(directoryPath);
        
        if (!Files.isDirectory(dir)) {
            throw new IOException("Not a directory: " + directoryPath);
        }
        
        return Files.list(dir)
            .filter(Files::isRegularFile)
            .map(FileSystemResource::new)
            .collect(Collectors.toList());
    }
    
    /**
     * List files with extension
     */
    public List<FileSystemResource> listFilesByExtension(String directoryPath, 
                                                         String extension) 
                                                         throws IOException {
        return listFiles(directoryPath).stream()
            .filter(resource -> resource.getFilename() != null && 
                              resource.getFilename().endsWith(extension))
            .collect(Collectors.toList());
    }
}

/**
 * Usage Examples
 */
class FileSystemResourceUsageExamples {
    
    public static void main(String[] args) throws IOException {
        // Example 1: Load file
        FileSystemResourcePattern loader = new FileSystemResourcePattern();
        // String content = loader.loadFromAbsolutePath("/etc/config/app.properties");
        // String relative = loader.loadFromRelativePath("config/settings.json");
        
        // Example 2: Configuration management
        ConfigurationFileManager configManager = 
            new ConfigurationFileManager("/etc/myapp");
        // String config = configManager.loadConfig("application.properties");
        // configManager.saveConfig("custom.properties", "key=value");
        
        // Example 3: Log file reading
        LogFileReader logReader = new LogFileReader();
        // List<String> logs = logReader.readLogFile("/var/log/app.log");
        // List<String> errors = logReader.filterByLevel("/var/log/app.log", "ERROR");
        
        // Example 4: File upload
        FileUploadHandler uploadHandler = new FileUploadHandler("uploads");
        // String path = uploadHandler.saveUpload("document.pdf", bytes);
        // byte[] data = uploadHandler.readUpload("document.pdf");
        
        // Example 5: Temporary files
        TemporaryFileManager tempManager = new TemporaryFileManager();
        // FileSystemResource temp = tempManager.createTempFile("data", ".txt", "content");
        // tempManager.deleteTempFile(temp);
        
        System.out.println("FileSystemResource Pattern Demonstration");
        System.out.println("Supports:");
        System.out.println("- Reading from file system");
        System.out.println("- Writing to file system");
        System.out.println("- File metadata access");
        System.out.println("- Directory operations");
    }
}
