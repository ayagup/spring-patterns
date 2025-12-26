package com.example.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Distributed File System Pattern
 * 
 * Purpose: Integration with distributed file systems like HDFS for big data storage.
 * Provides distributed file operations with replication and fault tolerance.
 * 
 * Key Features:
 * - HDFS integration
 * - Distributed file storage
 * - Data replication
 * - Fault tolerance
 * - Block-based storage
 * - High throughput
 * - Rack awareness
 * - Data locality optimization
 * - Append operations
 * - File permissions (POSIX-like)
 * 
 * Use Cases:
 * - Big data storage
 * - Data lake implementations
 * - Hadoop ecosystem integration
 * - Large-scale data processing
 * - Log aggregation
 * - Machine learning datasets
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.apache.hadoop</groupId>
 *     <artifactId>hadoop-client</artifactId>
 *     <version>3.3.4</version>
 * </dependency>
 * <dependency>
 *     <groupId>org.apache.hadoop</groupId>
 *     <artifactId>hadoop-hdfs</artifactId>
 *     <version>3.3.4</version>
 * </dependency>
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class DistributedFileSystemPattern {

    public static void main(String[] args) {
        SpringApplication.run(DistributedFileSystemPattern.class, args);
    }

    /**
     * HDFS Configuration
     */
    @Configuration
    public static class HDFSConfig {
        
        @Bean
        public HDFSProperties hdfsProperties() {
            HDFSProperties properties = new HDFSProperties();
            properties.setNameNodeUri("hdfs://localhost:9000");
            properties.setReplicationFactor(3);
            properties.setBlockSize(128 * 1024 * 1024L); // 128 MB
            properties.setUser("hadoop");
            return properties;
        }

        @Bean
        public HDFSClientWrapper hdfsClientWrapper(HDFSProperties properties) {
            return new HDFSClientWrapper(properties);
        }
    }

    /**
     * HDFS Controller
     */
    @RestController
    @RequestMapping("/api/hdfs")
    public static class HDFSController {

        private final HDFSService hdfsService;

        public HDFSController(HDFSService hdfsService) {
            this.hdfsService = hdfsService;
        }

        /**
         * Upload file to HDFS
         */
        @PostMapping("/upload")
        public ResponseEntity<HDFSFileInfo> uploadFile(
                @RequestParam("file") MultipartFile file,
                @RequestParam String path,
                @RequestParam(required = false, defaultValue = "false") boolean overwrite) {
            
            try {
                HDFSFileInfo fileInfo = hdfsService.uploadFile(path, file, overwrite);
                return ResponseEntity.ok(fileInfo);
            } catch (IOException e) {
                throw new HDFSException("Upload failed", e);
            }
        }

        /**
         * Upload with replication factor
         */
        @PostMapping("/upload/replication")
        public ResponseEntity<HDFSFileInfo> uploadWithReplication(
                @RequestParam("file") MultipartFile file,
                @RequestParam String path,
                @RequestParam(defaultValue = "3") short replication) {
            
            try {
                HDFSFileInfo fileInfo = hdfsService.uploadWithReplication(
                    path, file, replication
                );
                return ResponseEntity.ok(fileInfo);
            } catch (IOException e) {
                throw new HDFSException("Upload failed", e);
            }
        }

        /**
         * Download file from HDFS
         */
        @GetMapping("/download")
        public ResponseEntity<byte[]> downloadFile(@RequestParam String path) {
            try {
                HDFSFileData data = hdfsService.downloadFile(path);
                
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + extractFilename(path) + "\"")
                    .body(data.getData());
            } catch (IOException e) {
                throw new HDFSException("Download failed", e);
            }
        }

        /**
         * Append to file
         */
        @PostMapping("/append")
        public ResponseEntity<Map<String, String>> appendToFile(
                @RequestParam String path,
                @RequestParam String content) {
            
            try {
                hdfsService.appendToFile(path, content.getBytes());
                
                Map<String, String> response = new HashMap<>();
                response.put("message", "Content appended successfully");
                response.put("path", path);
                
                return ResponseEntity.ok(response);
            } catch (IOException e) {
                throw new HDFSException("Append failed", e);
            }
        }

        /**
         * List directory
         */
        @GetMapping("/list")
        public ResponseEntity<List<HDFSFileInfo>> listDirectory(
                @RequestParam String path,
                @RequestParam(required = false, defaultValue = "false") boolean recursive) {
            
            try {
                List<HDFSFileInfo> files = hdfsService.listDirectory(path, recursive);
                return ResponseEntity.ok(files);
            } catch (IOException e) {
                throw new HDFSException("List failed", e);
            }
        }

        /**
         * Get file status
         */
        @GetMapping("/status")
        public ResponseEntity<HDFSFileInfo> getFileStatus(@RequestParam String path) {
            try {
                HDFSFileInfo info = hdfsService.getFileStatus(path);
                return ResponseEntity.ok(info);
            } catch (IOException e) {
                throw new HDFSException("Status check failed", e);
            }
        }

        /**
         * Create directory
         */
        @PostMapping("/mkdir")
        public ResponseEntity<Map<String, String>> createDirectory(@RequestParam String path) {
            try {
                hdfsService.createDirectory(path);
                
                Map<String, String> response = new HashMap<>();
                response.put("message", "Directory created successfully");
                response.put("path", path);
                
                return ResponseEntity.ok(response);
            } catch (IOException e) {
                throw new HDFSException("Directory creation failed", e);
            }
        }

        /**
         * Delete file or directory
         */
        @DeleteMapping("/delete")
        public ResponseEntity<Map<String, String>> delete(
                @RequestParam String path,
                @RequestParam(required = false, defaultValue = "false") boolean recursive) {
            
            try {
                hdfsService.delete(path, recursive);
                
                Map<String, String> response = new HashMap<>();
                response.put("message", "Deleted successfully");
                response.put("path", path);
                
                return ResponseEntity.ok(response);
            } catch (IOException e) {
                throw new HDFSException("Delete failed", e);
            }
        }

        /**
         * Rename file or directory
         */
        @PutMapping("/rename")
        public ResponseEntity<Map<String, String>> rename(
                @RequestParam String source,
                @RequestParam String destination) {
            
            try {
                hdfsService.rename(source, destination);
                
                Map<String, String> response = new HashMap<>();
                response.put("message", "Renamed successfully");
                response.put("source", source);
                response.put("destination", destination);
                
                return ResponseEntity.ok(response);
            } catch (IOException e) {
                throw new HDFSException("Rename failed", e);
            }
        }

        /**
         * Set replication factor
         */
        @PutMapping("/replication")
        public ResponseEntity<Map<String, Object>> setReplication(
                @RequestParam String path,
                @RequestParam short replication) {
            
            try {
                hdfsService.setReplication(path, replication);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Replication factor updated");
                response.put("path", path);
                response.put("replication", replication);
                
                return ResponseEntity.ok(response);
            } catch (IOException e) {
                throw new HDFSException("Replication update failed", e);
            }
        }

        /**
         * Set permissions
         */
        @PutMapping("/permissions")
        public ResponseEntity<Map<String, String>> setPermissions(
                @RequestParam String path,
                @RequestParam String permissions) {
            
            try {
                hdfsService.setPermissions(path, permissions);
                
                Map<String, String> response = new HashMap<>();
                response.put("message", "Permissions updated");
                response.put("path", path);
                response.put("permissions", permissions);
                
                return ResponseEntity.ok(response);
            } catch (IOException e) {
                throw new HDFSException("Permission update failed", e);
            }
        }

        /**
         * Get file block locations
         */
        @GetMapping("/blocks")
        public ResponseEntity<List<BlockLocation>> getBlockLocations(@RequestParam String path) {
            try {
                List<BlockLocation> blocks = hdfsService.getBlockLocations(path);
                return ResponseEntity.ok(blocks);
            } catch (IOException e) {
                throw new HDFSException("Block location query failed", e);
            }
        }

        /**
         * Get cluster statistics
         */
        @GetMapping("/stats")
        public ResponseEntity<ClusterStats> getClusterStats() {
            try {
                return ResponseEntity.ok(hdfsService.getClusterStats());
            } catch (IOException e) {
                throw new HDFSException("Stats retrieval failed", e);
            }
        }

        /**
         * Check file exists
         */
        @GetMapping("/exists")
        public ResponseEntity<Map<String, Boolean>> exists(@RequestParam String path) {
            try {
                boolean exists = hdfsService.exists(path);
                
                Map<String, Boolean> response = new HashMap<>();
                response.put("exists", exists);
                
                return ResponseEntity.ok(response);
            } catch (IOException e) {
                throw new HDFSException("Existence check failed", e);
            }
        }

        private String extractFilename(String path) {
            return path.substring(path.lastIndexOf('/') + 1);
        }
    }

    /**
     * HDFS Service
     */
    @Service
    public static class HDFSService {

        private final HDFSClientWrapper hdfsClient;

        public HDFSService(HDFSClientWrapper hdfsClient) {
            this.hdfsClient = hdfsClient;
        }

        public HDFSFileInfo uploadFile(String path, MultipartFile file, boolean overwrite) 
                throws IOException {
            try (InputStream inputStream = file.getInputStream()) {
                return hdfsClient.createFile(path, inputStream, overwrite);
            }
        }

        public HDFSFileInfo uploadWithReplication(String path, MultipartFile file, 
                                                 short replication) throws IOException {
            try (InputStream inputStream = file.getInputStream()) {
                return hdfsClient.createFileWithReplication(path, inputStream, replication);
            }
        }

        public HDFSFileData downloadFile(String path) throws IOException {
            return hdfsClient.readFile(path);
        }

        public void appendToFile(String path, byte[] data) throws IOException {
            hdfsClient.appendToFile(path, data);
        }

        public List<HDFSFileInfo> listDirectory(String path, boolean recursive) 
                throws IOException {
            return hdfsClient.listStatus(path, recursive);
        }

        public HDFSFileInfo getFileStatus(String path) throws IOException {
            return hdfsClient.getFileStatus(path);
        }

        public void createDirectory(String path) throws IOException {
            hdfsClient.mkdirs(path);
        }

        public void delete(String path, boolean recursive) throws IOException {
            hdfsClient.delete(path, recursive);
        }

        public void rename(String source, String destination) throws IOException {
            hdfsClient.rename(source, destination);
        }

        public void setReplication(String path, short replication) throws IOException {
            hdfsClient.setReplication(path, replication);
        }

        public void setPermissions(String path, String permissions) throws IOException {
            hdfsClient.setPermission(path, permissions);
        }

        public List<BlockLocation> getBlockLocations(String path) throws IOException {
            return hdfsClient.getBlockLocations(path);
        }

        public ClusterStats getClusterStats() throws IOException {
            return hdfsClient.getClusterStats();
        }

        public boolean exists(String path) throws IOException {
            return hdfsClient.exists(path);
        }
    }

    /**
     * HDFS Client Wrapper (Mock implementation - replace with actual Hadoop HDFS)
     */
    public static class HDFSClientWrapper {
        
        private final HDFSProperties properties;
        private final Map<String, MockHDFSFile> fileSystem = new ConcurrentHashMap<>();
        private long totalWrites = 0;
        private long totalReads = 0;
        private long totalBytes = 0;

        public HDFSClientWrapper(HDFSProperties properties) {
            this.properties = properties;
        }

        @PostConstruct
        public void init() {
            System.out.println("HDFS Client initialized");
            System.out.println("NameNode: " + properties.getNameNodeUri());
            System.out.println("Default Replication: " + properties.getReplicationFactor());
            System.out.println("Block Size: " + properties.getBlockSize() + " bytes");
            
            // Create root directory
            fileSystem.put("/", new MockHDFSFile(
                "/", true, 0, LocalDateTime.now(), "755", 
                properties.getReplicationFactor()
            ));
        }

        /**
         * Create file
         */
        public HDFSFileInfo createFile(String path, InputStream data, boolean overwrite) 
                throws IOException {
            
            if (fileSystem.containsKey(path) && !overwrite) {
                throw new HDFSException("File already exists: " + path);
            }
            
            byte[] bytes = data.readAllBytes();
            
            MockHDFSFile file = new MockHDFSFile(
                path,
                false,
                bytes.length,
                LocalDateTime.now(),
                "644",
                properties.getReplicationFactor()
            );
            file.setData(bytes);
            
            fileSystem.put(path, file);
            totalWrites++;
            totalBytes += bytes.length;
            
            return toFileInfo(file);
        }

        /**
         * Create file with specific replication
         */
        public HDFSFileInfo createFileWithReplication(String path, InputStream data, 
                                                     short replication) throws IOException {
            
            byte[] bytes = data.readAllBytes();
            
            MockHDFSFile file = new MockHDFSFile(
                path,
                false,
                bytes.length,
                LocalDateTime.now(),
                "644",
                replication
            );
            file.setData(bytes);
            
            fileSystem.put(path, file);
            totalWrites++;
            totalBytes += bytes.length;
            
            return toFileInfo(file);
        }

        /**
         * Read file
         */
        public HDFSFileData readFile(String path) throws IOException {
            MockHDFSFile file = fileSystem.get(path);
            
            if (file == null) {
                throw new HDFSException("File not found: " + path);
            }
            
            if (file.isDirectory()) {
                throw new HDFSException("Cannot read directory: " + path);
            }
            
            totalReads++;
            
            return new HDFSFileData(path, file.getData());
        }

        /**
         * Append to file
         */
        public void appendToFile(String path, byte[] data) throws IOException {
            MockHDFSFile file = fileSystem.get(path);
            
            if (file == null) {
                throw new HDFSException("File not found: " + path);
            }
            
            if (file.isDirectory()) {
                throw new HDFSException("Cannot append to directory: " + path);
            }
            
            byte[] existingData = file.getData();
            byte[] newData = new byte[existingData.length + data.length];
            System.arraycopy(existingData, 0, newData, 0, existingData.length);
            System.arraycopy(data, 0, newData, existingData.length, data.length);
            
            file.setData(newData);
            file.setLength(newData.length);
            file.setModificationTime(LocalDateTime.now());
            
            totalWrites++;
            totalBytes += data.length;
        }

        /**
         * List directory
         */
        public List<HDFSFileInfo> listStatus(String path, boolean recursive) throws IOException {
            if (!path.endsWith("/")) {
                path += "/";
            }
            
            final String dirPath = path;
            
            return fileSystem.entrySet().stream()
                .filter(entry -> {
                    String entryPath = entry.getKey();
                    if (entryPath.equals(dirPath.substring(0, dirPath.length() - 1))) {
                        return false;
                    }
                    
                    if (recursive) {
                        return entryPath.startsWith(dirPath);
                    } else {
                        return entryPath.startsWith(dirPath) && 
                               !entryPath.substring(dirPath.length()).contains("/");
                    }
                })
                .map(entry -> toFileInfo(entry.getValue()))
                .collect(Collectors.toList());
        }

        /**
         * Get file status
         */
        public HDFSFileInfo getFileStatus(String path) throws IOException {
            MockHDFSFile file = fileSystem.get(path);
            
            if (file == null) {
                throw new HDFSException("File not found: " + path);
            }
            
            return toFileInfo(file);
        }

        /**
         * Create directory
         */
        public void mkdirs(String path) throws IOException {
            MockHDFSFile dir = new MockHDFSFile(
                path,
                true,
                0,
                LocalDateTime.now(),
                "755",
                properties.getReplicationFactor()
            );
            
            fileSystem.put(path, dir);
        }

        /**
         * Delete file or directory
         */
        public void delete(String path, boolean recursive) throws IOException {
            MockHDFSFile file = fileSystem.get(path);
            
            if (file == null) {
                throw new HDFSException("File not found: " + path);
            }
            
            if (file.isDirectory() && !recursive) {
                // Check if directory is empty
                long childCount = fileSystem.keySet().stream()
                    .filter(p -> p.startsWith(path + "/"))
                    .count();
                
                if (childCount > 0) {
                    throw new HDFSException("Directory not empty: " + path);
                }
            }
            
            // Delete file and all children if recursive
            List<String> toDelete = new ArrayList<>();
            toDelete.add(path);
            
            if (recursive) {
                fileSystem.keySet().stream()
                    .filter(p -> p.startsWith(path + "/"))
                    .forEach(toDelete::add);
            }
            
            toDelete.forEach(fileSystem::remove);
        }

        /**
         * Rename file or directory
         */
        public void rename(String source, String destination) throws IOException {
            MockHDFSFile file = fileSystem.remove(source);
            
            if (file == null) {
                throw new HDFSException("Source not found: " + source);
            }
            
            file.setPath(destination);
            fileSystem.put(destination, file);
        }

        /**
         * Set replication factor
         */
        public void setReplication(String path, short replication) throws IOException {
            MockHDFSFile file = fileSystem.get(path);
            
            if (file == null) {
                throw new HDFSException("File not found: " + path);
            }
            
            file.setReplication(replication);
        }

        /**
         * Set permissions
         */
        public void setPermission(String path, String permission) throws IOException {
            MockHDFSFile file = fileSystem.get(path);
            
            if (file == null) {
                throw new HDFSException("File not found: " + path);
            }
            
            file.setPermission(permission);
        }

        /**
         * Get block locations
         */
        public List<BlockLocation> getBlockLocations(String path) throws IOException {
            MockHDFSFile file = fileSystem.get(path);
            
            if (file == null) {
                throw new HDFSException("File not found: " + path);
            }
            
            if (file.isDirectory()) {
                return Collections.emptyList();
            }
            
            // Calculate number of blocks
            long blockSize = properties.getBlockSize();
            long fileSize = file.getLength();
            int numBlocks = (int) Math.ceil((double) fileSize / blockSize);
            
            List<BlockLocation> blocks = new ArrayList<>();
            for (int i = 0; i < numBlocks; i++) {
                long offset = i * blockSize;
                long length = Math.min(blockSize, fileSize - offset);
                
                blocks.add(new BlockLocation(
                    i,
                    offset,
                    length,
                    Arrays.asList("host1", "host2", "host3")
                ));
            }
            
            return blocks;
        }

        /**
         * Get cluster statistics
         */
        public ClusterStats getClusterStats() {
            long totalFiles = fileSystem.values().stream()
                .filter(f -> !f.isDirectory())
                .count();
            
            long totalDirs = fileSystem.values().stream()
                .filter(MockHDFSFile::isDirectory)
                .count();
            
            return new ClusterStats(
                totalFiles,
                totalDirs,
                totalBytes,
                totalBytes * properties.getReplicationFactor(),
                totalWrites,
                totalReads,
                LocalDateTime.now()
            );
        }

        /**
         * Check if file exists
         */
        public boolean exists(String path) {
            return fileSystem.containsKey(path);
        }

        /**
         * Convert to HDFSFileInfo
         */
        private HDFSFileInfo toFileInfo(MockHDFSFile file) {
            return new HDFSFileInfo(
                file.getPath(),
                file.isDirectory(),
                file.getLength(),
                file.getModificationTime(),
                file.getPermission(),
                file.getReplication()
            );
        }
    }

    // Model Classes

    public static class HDFSFileInfo {
        private String path;
        private boolean isDirectory;
        private long length;
        private LocalDateTime modificationTime;
        private String permission;
        private short replication;

        public HDFSFileInfo(String path, boolean isDirectory, long length,
                           LocalDateTime modificationTime, String permission, 
                           short replication) {
            this.path = path;
            this.isDirectory = isDirectory;
            this.length = length;
            this.modificationTime = modificationTime;
            this.permission = permission;
            this.replication = replication;
        }

        // Getters
        public String getPath() { return path; }
        public boolean isDirectory() { return isDirectory; }
        public long getLength() { return length; }
        public LocalDateTime getModificationTime() { return modificationTime; }
        public String getPermission() { return permission; }
        public short getReplication() { return replication; }
    }

    public static class HDFSFileData {
        private String path;
        private byte[] data;

        public HDFSFileData(String path, byte[] data) {
            this.path = path;
            this.data = data;
        }

        // Getters
        public String getPath() { return path; }
        public byte[] getData() { return data; }
    }

    public static class MockHDFSFile {
        private String path;
        private boolean isDirectory;
        private long length;
        private LocalDateTime modificationTime;
        private String permission;
        private short replication;
        private byte[] data;

        public MockHDFSFile(String path, boolean isDirectory, long length,
                           LocalDateTime modificationTime, String permission,
                           short replication) {
            this.path = path;
            this.isDirectory = isDirectory;
            this.length = length;
            this.modificationTime = modificationTime;
            this.permission = permission;
            this.replication = replication;
            this.data = new byte[0];
        }

        // Getters and Setters
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public boolean isDirectory() { return isDirectory; }
        public long getLength() { return length; }
        public void setLength(long length) { this.length = length; }
        public LocalDateTime getModificationTime() { return modificationTime; }
        public void setModificationTime(LocalDateTime modificationTime) { 
            this.modificationTime = modificationTime; 
        }
        public String getPermission() { return permission; }
        public void setPermission(String permission) { this.permission = permission; }
        public short getReplication() { return replication; }
        public void setReplication(short replication) { this.replication = replication; }
        public byte[] getData() { return data; }
        public void setData(byte[] data) { this.data = data; }
    }

    public static class HDFSProperties {
        private String nameNodeUri;
        private short replicationFactor;
        private long blockSize;
        private String user;

        // Getters and Setters
        public String getNameNodeUri() { return nameNodeUri; }
        public void setNameNodeUri(String nameNodeUri) { this.nameNodeUri = nameNodeUri; }
        public short getReplicationFactor() { return replicationFactor; }
        public void setReplicationFactor(short replicationFactor) { 
            this.replicationFactor = replicationFactor; 
        }
        public long getBlockSize() { return blockSize; }
        public void setBlockSize(long blockSize) { this.blockSize = blockSize; }
        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }
    }

    public static class BlockLocation {
        private int blockNumber;
        private long offset;
        private long length;
        private List<String> hosts;

        public BlockLocation(int blockNumber, long offset, long length, List<String> hosts) {
            this.blockNumber = blockNumber;
            this.offset = offset;
            this.length = length;
            this.hosts = hosts;
        }

        // Getters
        public int getBlockNumber() { return blockNumber; }
        public long getOffset() { return offset; }
        public long getLength() { return length; }
        public List<String> getHosts() { return hosts; }
    }

    public static class ClusterStats {
        private long totalFiles;
        private long totalDirectories;
        private long totalSize;
        private long totalSizeWithReplication;
        private long totalWrites;
        private long totalReads;
        private LocalDateTime timestamp;

        public ClusterStats(long totalFiles, long totalDirectories, long totalSize,
                           long totalSizeWithReplication, long totalWrites, 
                           long totalReads, LocalDateTime timestamp) {
            this.totalFiles = totalFiles;
            this.totalDirectories = totalDirectories;
            this.totalSize = totalSize;
            this.totalSizeWithReplication = totalSizeWithReplication;
            this.totalWrites = totalWrites;
            this.totalReads = totalReads;
            this.timestamp = timestamp;
        }

        // Getters
        public long getTotalFiles() { return totalFiles; }
        public long getTotalDirectories() { return totalDirectories; }
        public long getTotalSize() { return totalSize; }
        public long getTotalSizeWithReplication() { return totalSizeWithReplication; }
        public long getTotalWrites() { return totalWrites; }
        public long getTotalReads() { return totalReads; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class HDFSException extends RuntimeException {
        public HDFSException(String message) {
            super(message);
        }

        public HDFSException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

/*
 * Real HDFS Implementation Example:
 * 
 * import org.apache.hadoop.conf.Configuration;
 * import org.apache.hadoop.fs.FileSystem;
 * import org.apache.hadoop.fs.Path;
 * import org.apache.hadoop.fs.FSDataOutputStream;
 * import org.apache.hadoop.fs.FSDataInputStream;
 * 
 * @Bean
 * public FileSystem fileSystem(HDFSProperties properties) throws IOException {
 *     Configuration conf = new Configuration();
 *     conf.set("fs.defaultFS", properties.getNameNodeUri());
 *     conf.set("dfs.replication", String.valueOf(properties.getReplicationFactor()));
 *     conf.set("dfs.block.size", String.valueOf(properties.getBlockSize()));
 *     
 *     return FileSystem.get(conf);
 * }
 * 
 * // Create file
 * Path path = new Path("/user/data/file.txt");
 * FSDataOutputStream out = fileSystem.create(path, true);
 * out.write(data);
 * out.close();
 * 
 * // Read file
 * FSDataInputStream in = fileSystem.open(path);
 * byte[] buffer = new byte[4096];
 * in.read(buffer);
 * in.close();
 */

/*
 * Application Properties:
 * 
 * # HDFS Configuration
 * hdfs.namenode.uri=hdfs://localhost:9000
 * hdfs.replication.factor=3
 * hdfs.block.size=134217728
 * hdfs.user=hadoop
 * 
 * # Hadoop Core Configuration
 * hadoop.home.dir=/usr/local/hadoop
 */
