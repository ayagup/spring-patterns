# File and Stream Processing Patterns

Comprehensive examples of Spring file and stream processing patterns with practical implementations.

## 📋 Patterns Included

### 1. Stream Processing Pattern
**File:** `StreamProcessingPattern.java`

Demonstrates Java 8+ Stream API for data processing:
- Collection stream operations
- Parallel stream processing
- File stream processing (CSV, text files)
- Custom stream collectors
- Stream performance optimization
- Reactive-style processing

**Key Features:**
```java
// Filter and transform data
List<Integer> evenSquares = numbers.stream()
    .filter(n -> n % 2 == 0)
    .map(n -> n * n)
    .collect(Collectors.toList());

// Process large files
try (Stream<String> lines = Files.lines(filePath)) {
    lines.filter(filter)
         .map(transformer)
         .forEach(processor);
}
```

**Use Cases:**
- Data transformation and filtering
- Bulk data processing
- ETL operations
- Log file analysis

---

### 2. Batch File Processing Pattern
**File:** `BatchFileProcessingPattern.java`

Spring Batch framework for large-scale file processing:
- Chunk-oriented processing
- ItemReader, ItemProcessor, ItemWriter
- CSV/XML/JSON file processing
- Multi-line record handling
- Job execution monitoring
- Error handling and skip logic

**Key Features:**
```java
@Bean
public Step processStep(JobRepository jobRepository,
                       PlatformTransactionManager transactionManager) {
    return new StepBuilder("processStep", jobRepository)
        .<CustomerRecord, CustomerRecord>chunk(100, transactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .faultTolerant()
        .skipLimit(10)
        .build();
}
```

**Dependencies:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-batch</artifactId>
</dependency>
```

---

### 3. File Upload Pattern
**File:** `FileUploadPattern.java`

Comprehensive file upload handling:
- Multipart file upload
- File validation (size, type, content)
- Storage strategies (filesystem, cloud, database)
- Chunked upload for large files
- Progress tracking
- Security measures

**Key Features:**
```java
@PostMapping("/upload")
public ResponseEntity<UploadResponse> uploadFile(
        @RequestParam("file") MultipartFile file) {
    
    validator.validate(file);
    UploadedFile result = storageService.store(file, username);
    
    return ResponseEntity.ok(
        UploadResponse.success(result.id(), result.filename())
    );
}
```

**Configuration:**
```java
@Bean
public MultipartConfigElement multipartConfigElement() {
    factory.setMaxFileSize(DataSize.ofMegabytes(10));
    factory.setMaxRequestSize(DataSize.ofMegabytes(50));
    return factory.createMultipartConfig();
}
```

---

### 4. File Download Pattern
**File:** `FileDownloadPattern.java`

Various file download techniques:
- Basic file download
- Streaming large files
- Content-type handling
- Range/partial content support
- Zip archive generation
- In-memory file generation

**Key Features:**
```java
@GetMapping("/download/{filename}")
public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
    Resource resource = service.loadFileAsResource(filename);
    
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + filename + "\"")
        .body(resource);
}

// Streaming for large files
@GetMapping("/stream/{filename}")
public ResponseEntity<StreamingResponseBody> streamFile(
        @PathVariable String filename) {
    
    StreamingResponseBody stream = 
        outputStream -> copyFileToStream(filename, outputStream);
    
    return ResponseEntity.ok().body(stream);
}
```

---

### 5. Multipart and Resource Handling Pattern
**File:** `MultipartResourcePattern.java`

Spring Resource abstraction and multipart handling:
- ClassPathResource
- FileSystemResource
- UrlResource
- ByteArrayResource
- Multipart configuration
- Resource path resolution

**Key Features:**
```java
// Load different resource types
Resource classpathResource = new ClassPathResource("config/app.properties");
Resource fileResource = new FileSystemResource("/path/to/file.txt");
Resource urlResource = new UrlResource("https://example.com/resource.txt");

// Load resources by pattern
Resource[] resources = resourceLoader.getResources("classpath*:config/*.xml");

// Multipart validation
ValidationResult result = handler.validate(
    file, 
    Set.of("image/jpeg", "image/png"), 
    10 * 1024 * 1024 // 10MB
);
```

---

### 6. Stream I/O Pattern
**File:** `StreamIOPattern.java`

Comprehensive I/O stream operations:
- InputStream/OutputStream operations
- Buffered streams
- Data streams (primitive types)
- Object streams (serialization)
- Compression (GZIP, ZIP)
- NIO channels
- Stream filtering and transformation

**Key Features:**
```java
// Buffered copy
try (BufferedInputStream bis = new BufferedInputStream(input);
     BufferedOutputStream bos = new BufferedOutputStream(output)) {
    
    byte[] buffer = new byte[8192];
    int bytesRead;
    while ((bytesRead = bis.read(buffer)) != -1) {
        bos.write(buffer, 0, bytesRead);
    }
}

// GZIP compression
byte[] compressed = compressGzip(originalData);
byte[] decompressed = decompressGzip(compressed);

// NIO channel (zero-copy)
sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
```

---

### 7. File System Integration Pattern
**File:** `FileSystemIntegrationPattern.java`

Java NIO.2 file system operations:
- File and directory operations
- File watching (WatchService)
- File attributes and metadata
- File permissions (POSIX)
- Symbolic links
- Directory traversal
- Path operations

**Key Features:**
```java
// Watch directory for changes
watchService.watchDirectory(directory, (kind, path) -> {
    System.out.println("File " + kind.name() + ": " + path);
});

// File attributes
BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
FileTime created = attrs.creationTime();
FileTime modified = attrs.lastModifiedTime();

// Copy directory recursively
Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        Files.copy(file, target.resolve(source.relativize(file)));
        return FileVisitResult.CONTINUE;
    }
});

// Find files by pattern
PathMatcher matcher = FileSystems.getDefault()
    .getPathMatcher("glob:*.txt");
```

---

### 8. FTP/SFTP Integration Pattern
**File:** `FTPSFTPIntegrationPattern.java`

FTP and SFTP file transfer operations:
- FTP client (Apache Commons Net)
- SFTP client (JSch)
- File upload/download
- Directory operations
- Secure authentication
- File permissions

**Key Features:**
```java
// FTP upload
FTPClient ftpClient = new FTPClient();
ftpClient.connect(host, port);
ftpClient.login(username, password);
ftpClient.enterLocalPassiveMode();

try (InputStream input = new FileInputStream(localFile)) {
    ftpClient.storeFile(remoteFileName, input);
}

// SFTP with key authentication
JSch jsch = new JSch();
jsch.addIdentity(privateKeyPath, passphrase);

Session session = jsch.getSession(username, host, port);
session.connect();

ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
sftpChannel.put(localFile, remoteFile);
```

**Dependencies:**
```xml
<!-- FTP -->
<dependency>
    <groupId>commons-net</groupId>
    <artifactId>commons-net</artifactId>
    <version>3.9.0</version>
</dependency>

<!-- SFTP -->
<dependency>
    <groupId>com.jcraft</groupId>
    <artifactId>jsch</artifactId>
    <version>0.1.55</version>
</dependency>
```

---

## 🎯 Pattern Comparison

| Pattern | Primary Use | Complexity | Performance | Best For |
|---------|------------|------------|-------------|----------|
| Stream Processing | Data transformation | Low | High | In-memory data processing |
| Batch File Processing | Large file processing | High | High | ETL, bulk imports |
| File Upload | User file uploads | Medium | Medium | Web applications |
| File Download | File delivery | Low | High | Content delivery |
| Multipart/Resource | Resource loading | Low | High | Configuration, templates |
| Stream I/O | Binary data | Medium | High | File operations |
| File System | File management | Medium | High | File utilities |
| FTP/SFTP | Remote transfer | High | Medium | Integration, backups |

---

## 🚀 Quick Start

### 1. Stream Processing
```java
// Process CSV file
List<Customer> customers = Files.lines(csvFile)
    .skip(1) // Skip header
    .map(line -> line.split(","))
    .map(Customer::fromArray)
    .filter(c -> c.isActive())
    .collect(Collectors.toList());
```

### 2. Batch Processing
```java
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    @Bean
    public Job importJob(Step step1) {
        return jobBuilderFactory.get("importJob")
            .start(step1)
            .build();
    }
}
```

### 3. File Upload
```java
@PostMapping("/upload")
public String handleFileUpload(@RequestParam("file") MultipartFile file) {
    storageService.store(file);
    return "redirect:/files";
}
```

### 4. File Download
```java
@GetMapping("/files/{filename:.+}")
public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
    Resource file = storageService.loadAsResource(filename);
    return ResponseEntity.ok().body(file);
}
```

---

## 📊 Performance Tips

### Stream Processing
- Use parallel streams for CPU-intensive operations
- Apply filters early in the pipeline
- Use primitive streams (IntStream, LongStream) when possible
- Limit stream size before expensive operations

### Batch Processing
- Choose appropriate chunk size (100-1000 items)
- Use parallel processing for independent items
- Implement skip/retry logic for failures
- Monitor job execution metrics

### File Upload/Download
- Stream large files instead of loading into memory
- Implement chunked upload for files > 100MB
- Use Content-Range for resumable downloads
- Validate files asynchronously

### I/O Streams
- Always use buffered streams
- Use NIO channels for large file transfers
- Compress data before transmission
- Close streams in try-with-resources

### File System
- Use Files.walk() with try-with-resources
- Set appropriate max depth for directory traversal
- Use parallel streams for independent file operations
- Cache file attributes when processing many files

---

## 🔒 Security Best Practices

### File Upload
1. **Validate file types** - Check MIME type and extension
2. **Limit file size** - Prevent denial of service
3. **Sanitize filenames** - Prevent path traversal attacks
4. **Scan for malware** - Integrate antivirus scanning
5. **Store securely** - Use randomized filenames

### File Download
1. **Validate file paths** - Prevent directory traversal
2. **Check permissions** - Verify user access rights
3. **Set Content-Type** - Prevent XSS attacks
4. **Use HTTPS** - Encrypt file transmission
5. **Log downloads** - Audit trail for compliance

### FTP/SFTP
1. **Use SFTP over FTP** - Encrypted transmission
2. **Key-based auth** - More secure than passwords
3. **Verify host keys** - Prevent man-in-the-middle attacks
4. **Set file permissions** - Restrict access appropriately
5. **Use VPN/firewall** - Network-level security

---

## 📖 Usage Examples

### Example 1: CSV Import with Validation
```java
public class CSVImporter {
    public List<Customer> importCustomers(Path csvFile) throws IOException {
        return Files.lines(csvFile)
            .skip(1) // Skip header
            .map(line -> line.split(","))
            .map(this::parseCustomer)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }
    
    private Optional<Customer> parseCustomer(String[] fields) {
        try {
            return Optional.of(new Customer(
                fields[0], fields[1], fields[2]
            ));
        } catch (Exception e) {
            log.error("Invalid customer data", e);
            return Optional.empty();
        }
    }
}
```

### Example 2: Batch File Processing with Spring Batch
```java
@Bean
public FlatFileItemReader<Customer> reader() {
    return new FlatFileItemReaderBuilder<Customer>()
        .name("customerReader")
        .resource(new FileSystemResource("customers.csv"))
        .delimited()
        .names("id", "firstName", "lastName", "email")
        .targetType(Customer.class)
        .build();
}

@Bean
public ItemProcessor<Customer, Customer> processor() {
    return customer -> {
        // Transform and validate
        if (customer.getEmail() == null) {
            return null; // Skip invalid records
        }
        return customer;
    };
}
```

### Example 3: Multipart File Upload with Validation
```java
@PostMapping("/upload")
public ResponseEntity<?> uploadFile(
        @RequestParam("file") MultipartFile file) {
    
    // Validate
    if (!isValidFileType(file.getContentType())) {
        return ResponseEntity.badRequest()
            .body("Invalid file type");
    }
    
    if (file.getSize() > MAX_FILE_SIZE) {
        return ResponseEntity.badRequest()
            .body("File too large");
    }
    
    // Store
    String filename = storageService.store(file);
    
    return ResponseEntity.ok()
        .body(Map.of("filename", filename));
}
```

### Example 4: File Watcher for Auto-Processing
```java
public class FileWatcher {
    public void watchAndProcess(Path directory) throws IOException {
        WatchService watchService = FileSystems.getDefault()
            .newWatchService();
        
        directory.register(watchService,
            StandardWatchEventKinds.ENTRY_CREATE);
        
        while (true) {
            WatchKey key = watchService.take();
            
            for (WatchEvent<?> event : key.pollEvents()) {
                Path file = directory.resolve(
                    (Path) event.context()
                );
                
                processFile(file);
            }
            
            key.reset();
        }
    }
}
```

---

## 🛠️ Configuration

### application.yml
```yaml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 50MB
      file-size-threshold: 2MB
      location: ${java.io.tmpdir}
      
  batch:
    job:
      enabled: false
    initialize-schema: always

file:
  upload:
    directory: uploads
  download:
    directory: downloads
    
ftp:
  host: ftp.example.com
  port: 21
  username: user
  password: ${FTP_PASSWORD}
  passive-mode: true
  
sftp:
  host: sftp.example.com
  port: 22
  username: user
  private-key: classpath:keys/id_rsa
  known-hosts: classpath:keys/known_hosts
```

---

## 📚 Additional Resources

### Documentation
- [Java NIO.2 Tutorial](https://docs.oracle.com/javase/tutorial/essential/io/fileio.html)
- [Spring Batch Reference](https://docs.spring.io/spring-batch/docs/current/reference/html/)
- [Apache Commons Net](https://commons.apache.org/proper/commons-net/)
- [JSch Documentation](http://www.jcraft.com/jsch/)

### Best Practices
- Use try-with-resources for automatic resource management
- Implement proper error handling and logging
- Monitor file system usage and cleanup old files
- Use async processing for long-running operations
- Implement retry logic for network operations

---

## 🤝 Contributing

Suggestions and improvements are welcome! Please follow these guidelines:
1. Add comprehensive Javadoc comments
2. Include usage examples in main() method
3. Follow Spring coding conventions
4. Add appropriate error handling

---

## 📝 License

These patterns are provided as educational examples for Spring Framework development.

---

**Note:** All patterns include complete, runnable demonstrations in their respective Java files. Mock implementations are used where external dependencies (databases, FTP servers) would be required.
