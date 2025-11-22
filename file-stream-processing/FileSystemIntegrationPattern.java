package com.spring.patterns.filestream;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File System Integration Pattern
 * 
 * Demonstrates comprehensive NIO.2 file system operations:
 * - File and directory operations
 * - File watching (WatchService)
 * - File attributes and metadata
 * - File permissions
 * - Symbolic links
 * - Directory traversal
 * - File copying and moving
 * - Temporary files and directories
 * - File system providers
 * - Path operations
 * 
 * Use Cases:
 * - File monitoring systems
 * - Configuration file watching
 * - Directory synchronization
 * - File metadata management
 * - Automated file processing
 * - File system utilities
 * 
 * Dependencies:
 * - Java NIO.2 (java.nio.file)
 */

/**
 * File System Service
 */
@Service
class FileSystemService {
    
    /**
     * Create directory structure
     */
    public Path createDirectoryStructure(Path basePath, String... subdirs) 
            throws IOException {
        
        Path current = basePath;
        for (String subdir : subdirs) {
            current = current.resolve(subdir);
        }
        
        return Files.createDirectories(current);
    }
    
    /**
     * Create file with content
     */
    public Path createFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        return Files.writeString(path, content, StandardCharsets.UTF_8);
    }
    
    /**
     * Copy file or directory
     */
    public Path copy(Path source, Path target, CopyOption... options) 
            throws IOException {
        
        if (Files.isDirectory(source)) {
            return copyDirectory(source, target);
        } else {
            return Files.copy(source, target, options);
        }
    }
    
    /**
     * Copy directory recursively
     */
    private Path copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) 
                    throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
                    throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)),
                        StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
        
        return target;
    }
    
    /**
     * Move file or directory
     */
    public Path move(Path source, Path target, CopyOption... options) 
            throws IOException {
        return Files.move(source, target, options);
    }
    
    /**
     * Delete file or directory recursively
     */
    public void delete(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) 
                        throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.deleteIfExists(path);
        }
    }
    
    /**
     * List directory contents
     */
    public List<Path> listDirectory(Path directory) throws IOException {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.collect(Collectors.toList());
        }
    }
    
    /**
     * Find files by pattern
     */
    public List<Path> findFiles(Path startPath, String pattern, int maxDepth) 
            throws IOException {
        
        PathMatcher matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + pattern);
        
        List<Path> matchedPaths = new ArrayList<>();
        
        Files.walkFileTree(startPath, 
                EnumSet.noneOf(FileVisitOption.class),
                maxDepth,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (matcher.matches(file.getFileName())) {
                            matchedPaths.add(file);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
        
        return matchedPaths;
    }
    
    /**
     * Get directory size
     */
    public long getDirectorySize(Path directory) throws IOException {
        final long[] size = {0};
        
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                size[0] += attrs.size();
                return FileVisitResult.CONTINUE;
            }
        });
        
        return size[0];
    }
}

/**
 * File Watcher Service
 */
@Service
class FileWatcherService {
    
    private final Map<Path, WatcherThread> watchers = new ConcurrentHashMap<>();
    
    /**
     * Watch directory for changes
     */
    public void watchDirectory(Path directory, FileChangeListener listener) 
            throws IOException {
        
        WatcherThread watcher = new WatcherThread(directory, listener);
        watchers.put(directory, watcher);
        watcher.start();
    }
    
    /**
     * Stop watching directory
     */
    public void stopWatching(Path directory) {
        WatcherThread watcher = watchers.remove(directory);
        if (watcher != null) {
            watcher.stopWatching();
        }
    }
    
    /**
     * Stop all watchers
     */
    public void stopAll() {
        watchers.values().forEach(WatcherThread::stopWatching);
        watchers.clear();
    }
    
    /**
     * File change listener interface
     */
    @FunctionalInterface
    public interface FileChangeListener {
        void onChange(WatchEvent.Kind<?> kind, Path path);
    }
    
    /**
     * Watcher thread
     */
    private static class WatcherThread extends Thread {
        private final Path directory;
        private final FileChangeListener listener;
        private volatile boolean running = true;
        private WatchService watchService;
        
        public WatcherThread(Path directory, FileChangeListener listener) 
                throws IOException {
            this.directory = directory;
            this.listener = listener;
            this.watchService = FileSystems.getDefault().newWatchService();
            
            directory.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
        }
        
        @Override
        public void run() {
            while (running) {
                WatchKey key;
                try {
                    key = watchService.poll(1, TimeUnit.SECONDS);
                    if (key == null) continue;
                    
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }
                        
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                        Path filename = pathEvent.context();
                        Path fullPath = directory.resolve(filename);
                        
                        listener.onChange(kind, fullPath);
                    }
                    
                    key.reset();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        public void stopWatching() {
            running = false;
            try {
                if (watchService != null) {
                    watchService.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * File Attributes Service
 */
@Service
class FileAttributesService {
    
    /**
     * Get basic file attributes
     */
    public BasicFileAttributes getBasicAttributes(Path path) throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class);
    }
    
    /**
     * Get file metadata
     */
    public FileMetadataInfo getMetadata(Path path) throws IOException {
        BasicFileAttributes attrs = getBasicAttributes(path);
        
        return new FileMetadataInfo(
                path.getFileName().toString(),
                attrs.size(),
                attrs.creationTime().toInstant(),
                attrs.lastModifiedTime().toInstant(),
                attrs.lastAccessTime().toInstant(),
                attrs.isRegularFile(),
                attrs.isDirectory(),
                attrs.isSymbolicLink()
        );
    }
    
    /**
     * Set file times
     */
    public void setFileTimes(Path path, FileTime created, FileTime modified, 
                            FileTime accessed) throws IOException {
        
        BasicFileAttributeView view = Files.getFileAttributeView(
                path, BasicFileAttributeView.class);
        
        view.setTimes(modified, accessed, created);
    }
    
    /**
     * Get POSIX file permissions (Unix/Linux)
     */
    public Set<PosixFilePermission> getPosixPermissions(Path path) 
            throws IOException {
        
        try {
            return Files.getPosixFilePermissions(path);
        } catch (UnsupportedOperationException e) {
            return null; // Not supported on Windows
        }
    }
    
    /**
     * Set POSIX file permissions
     */
    public void setPosixPermissions(Path path, Set<PosixFilePermission> permissions) 
            throws IOException {
        
        try {
            Files.setPosixFilePermissions(path, permissions);
        } catch (UnsupportedOperationException e) {
            // Not supported on Windows
        }
    }
    
    /**
     * Get file owner
     */
    public UserPrincipal getOwner(Path path) throws IOException {
        return Files.getOwner(path);
    }
    
    /**
     * Set file owner
     */
    public void setOwner(Path path, UserPrincipal owner) throws IOException {
        Files.setOwner(path, owner);
    }
    
    /**
     * Check if file is hidden
     */
    public boolean isHidden(Path path) throws IOException {
        return Files.isHidden(path);
    }
    
    /**
     * Get DOS attributes (Windows)
     */
    public DosFileAttributes getDosAttributes(Path path) throws IOException {
        try {
            return Files.readAttributes(path, DosFileAttributes.class);
        } catch (UnsupportedOperationException e) {
            return null; // Not supported on Unix/Linux
        }
    }
}

record FileMetadataInfo(
        String filename,
        long size,
        Instant created,
        Instant modified,
        Instant accessed,
        boolean isFile,
        boolean isDirectory,
        boolean isSymbolicLink
) {
    public String getFormattedSize() {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        }
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
    
    public LocalDateTime getCreatedDateTime() {
        return LocalDateTime.ofInstant(created, ZoneId.systemDefault());
    }
}

/**
 * Symbolic Link Handler
 */
@Service
class SymbolicLinkHandler {
    
    /**
     * Create symbolic link
     */
    public Path createSymbolicLink(Path link, Path target) throws IOException {
        return Files.createSymbolicLink(link, target);
    }
    
    /**
     * Create hard link
     */
    public Path createHardLink(Path link, Path existing) throws IOException {
        return Files.createLink(link, existing);
    }
    
    /**
     * Check if path is symbolic link
     */
    public boolean isSymbolicLink(Path path) {
        return Files.isSymbolicLink(path);
    }
    
    /**
     * Read symbolic link target
     */
    public Path readSymbolicLink(Path link) throws IOException {
        return Files.readSymbolicLink(link);
    }
}

/**
 * Temporary File Service
 */
@Service
class TemporaryFileService {
    
    /**
     * Create temporary file
     */
    public Path createTempFile(String prefix, String suffix) throws IOException {
        return Files.createTempFile(prefix, suffix);
    }
    
    /**
     * Create temporary file in directory
     */
    public Path createTempFileInDir(Path directory, String prefix, String suffix) 
            throws IOException {
        return Files.createTempFile(directory, prefix, suffix);
    }
    
    /**
     * Create temporary directory
     */
    public Path createTempDirectory(String prefix) throws IOException {
        return Files.createTempDirectory(prefix);
    }
    
    /**
     * Create temporary directory in directory
     */
    public Path createTempDirectoryInDir(Path directory, String prefix) 
            throws IOException {
        return Files.createTempDirectory(directory, prefix);
    }
    
    /**
     * Auto-delete temporary file on JVM exit
     */
    public Path createAutoDeleteTempFile(String prefix, String suffix) 
            throws IOException {
        
        Path tempFile = createTempFile(prefix, suffix);
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }
}

/**
 * Path Utility Service
 */
@Service
class PathUtilityService {
    
    /**
     * Normalize path
     */
    public Path normalize(Path path) {
        return path.normalize();
    }
    
    /**
     * Resolve path against another
     */
    public Path resolve(Path base, String other) {
        return base.resolve(other);
    }
    
    /**
     * Resolve sibling path
     */
    public Path resolveSibling(Path path, String other) {
        return path.resolveSibling(other);
    }
    
    /**
     * Relativize path
     */
    public Path relativize(Path base, Path other) {
        return base.relativize(other);
    }
    
    /**
     * Get absolute path
     */
    public Path toAbsolutePath(Path path) {
        return path.toAbsolutePath();
    }
    
    /**
     * Get real path (resolves symlinks)
     */
    public Path toRealPath(Path path) throws IOException {
        return path.toRealPath();
    }
    
    /**
     * Get parent path
     */
    public Path getParent(Path path) {
        return path.getParent();
    }
    
    /**
     * Get filename
     */
    public Path getFileName(Path path) {
        return path.getFileName();
    }
    
    /**
     * Get root
     */
    public Path getRoot(Path path) {
        return path.getRoot();
    }
    
    /**
     * Start with check
     */
    public boolean startsWith(Path path, String prefix) {
        return path.startsWith(prefix);
    }
    
    /**
     * Ends with check
     */
    public boolean endsWith(Path path, String suffix) {
        return path.endsWith(suffix);
    }
}

/**
 * File Comparison Service
 */
@Service
class FileComparisonService {
    
    /**
     * Compare files by content
     */
    public boolean areFilesEqual(Path file1, Path file2) throws IOException {
        if (!Files.exists(file1) || !Files.exists(file2)) {
            return false;
        }
        
        if (Files.size(file1) != Files.size(file2)) {
            return false;
        }
        
        return Arrays.equals(
                Files.readAllBytes(file1),
                Files.readAllBytes(file2)
        );
    }
    
    /**
     * Compare files by checksum
     */
    public boolean areFilesSameChecksum(Path file1, Path file2) throws Exception {
        return calculateChecksum(file1).equals(calculateChecksum(file2));
    }
    
    /**
     * Calculate file checksum
     */
    private String calculateChecksum(Path file) throws Exception {
        var md = java.security.MessageDigest.getInstance("MD5");
        try (var is = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                md.update(buffer, 0, read);
            }
        }
        
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

/**
 * File System Integration Pattern - Main Demonstration
 */
public class FileSystemIntegrationPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== File System Integration Pattern Demo ===\n");
        
        // 1. Basic File Operations
        demonstrateFileOperations();
        
        // 2. Directory Operations
        demonstrateDirectoryOperations();
        
        // 3. File Watching
        demonstrateFileWatching();
        
        // 4. File Attributes
        demonstrateFileAttributes();
        
        // 5. Path Operations
        demonstratePathOperations();
        
        // 6. Temporary Files
        demonstrateTemporaryFiles();
    }
    
    private static void demonstrateFileOperations() throws IOException {
        System.out.println("1. Basic File Operations:");
        
        FileSystemService fsService = new FileSystemService();
        
        Path testFile = Paths.get("test.txt");
        fsService.createFile(testFile, "Hello, NIO.2!");
        System.out.println("Created file: " + testFile);
        
        String content = Files.readString(testFile);
        System.out.println("Content: " + content);
        
        Files.deleteIfExists(testFile);
        System.out.println("Deleted file");
        
        System.out.println();
    }
    
    private static void demonstrateDirectoryOperations() throws IOException {
        System.out.println("2. Directory Operations:");
        
        FileSystemService fsService = new FileSystemService();
        
        Path testDir = Paths.get("test-directory");
        fsService.createDirectoryStructure(testDir, "sub1", "sub2");
        System.out.println("Created directory structure: " + testDir);
        
        List<Path> contents = fsService.listDirectory(testDir);
        System.out.println("Directory contents: " + contents.size() + " items");
        
        fsService.delete(testDir);
        System.out.println("Deleted directory");
        
        System.out.println();
    }
    
    private static void demonstrateFileWatching() throws IOException, InterruptedException {
        System.out.println("3. File Watching:");
        
        Path watchDir = Files.createTempDirectory("watch-test");
        
        FileWatcherService watcher = new FileWatcherService();
        watcher.watchDirectory(watchDir, (kind, path) -> {
            System.out.println("Event: " + kind.name() + " - " + path.getFileName());
        });
        
        System.out.println("Watching directory: " + watchDir);
        
        // Create test file
        Files.createFile(watchDir.resolve("test.txt"));
        Thread.sleep(500);
        
        // Modify file
        Files.writeString(watchDir.resolve("test.txt"), "Modified");
        Thread.sleep(500);
        
        // Delete file
        Files.delete(watchDir.resolve("test.txt"));
        Thread.sleep(500);
        
        watcher.stopAll();
        Files.deleteIfExists(watchDir);
        
        System.out.println();
    }
    
    private static void demonstrateFileAttributes() throws IOException {
        System.out.println("4. File Attributes:");
        
        FileAttributesService attrService = new FileAttributesService();
        
        Path testFile = Files.createTempFile("attr-test", ".txt");
        Files.writeString(testFile, "Test content for attributes");
        
        FileMetadataInfo metadata = attrService.getMetadata(testFile);
        System.out.println("Filename: " + metadata.filename());
        System.out.println("Size: " + metadata.getFormattedSize());
        System.out.println("Created: " + metadata.getCreatedDateTime());
        System.out.println("Is File: " + metadata.isFile());
        System.out.println("Is Directory: " + metadata.isDirectory());
        
        Files.deleteIfExists(testFile);
        
        System.out.println();
    }
    
    private static void demonstratePathOperations() {
        System.out.println("5. Path Operations:");
        
        PathUtilityService pathService = new PathUtilityService();
        
        Path path = Paths.get("/home/user/documents/../files/./test.txt");
        System.out.println("Original path: " + path);
        System.out.println("Normalized: " + pathService.normalize(path));
        System.out.println("Filename: " + pathService.getFileName(path));
        System.out.println("Parent: " + pathService.getParent(path));
        
        System.out.println();
    }
    
    private static void demonstrateTemporaryFiles() throws IOException {
        System.out.println("6. Temporary Files:");
        
        TemporaryFileService tempService = new TemporaryFileService();
        
        Path tempFile = tempService.createTempFile("demo", ".tmp");
        System.out.println("Created temp file: " + tempFile);
        System.out.println("Exists: " + Files.exists(tempFile));
        
        Path tempDir = tempService.createTempDirectory("demo-dir");
        System.out.println("Created temp directory: " + tempDir);
        
        Files.deleteIfExists(tempFile);
        Files.deleteIfExists(tempDir);
        
        System.out.println("\n=== Demo Complete ===");
    }
}
