package com.spring.patterns.filestream;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * FTP and SFTP Integration Patterns
 * 
 * Demonstrates FTP and SFTP file transfer operations:
 * 
 * FTP Pattern:
 * - Apache Commons Net FTP client
 * - Connection management
 * - File upload/download
 * - Directory operations
 * - Passive/Active mode
 * - Binary/ASCII transfer modes
 * 
 * SFTP Pattern:
 * - JSch library for SFTP
 * - SSH authentication
 * - Secure file transfer
 * - Key-based authentication
 * - Password authentication
 * - File permissions
 * 
 * Use Cases:
 * - File synchronization
 * - Backup uploads
 * - Remote file access
 * - Secure file transfers
 * - Automated file distribution
 * - Log file collection
 * 
 * Dependencies:
 * - commons-net:commons-net:3.9.0 (FTP)
 * - com.jcraft:jsch:0.1.55 (SFTP)
 */

/**
 * FTP Configuration
 */
class FTPConfig {
    private String host;
    private int port = 21;
    private String username;
    private String password;
    private boolean passiveMode = true;
    private boolean binaryTransfer = true;
    private int timeout = 30000; // 30 seconds
    private int bufferSize = 8192;
    
    // Getters and setters
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isPassiveMode() { return passiveMode; }
    public void setPassiveMode(boolean passiveMode) { this.passiveMode = passiveMode; }
    public boolean isBinaryTransfer() { return binaryTransfer; }
    public void setBinaryTransfer(boolean binaryTransfer) { 
        this.binaryTransfer = binaryTransfer; 
    }
    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
    public int getBufferSize() { return bufferSize; }
    public void setBufferSize(int bufferSize) { this.bufferSize = bufferSize; }
}

/**
 * SFTP Configuration
 */
class SFTPConfig {
    private String host;
    private int port = 22;
    private String username;
    private String password;
    private String privateKeyPath;
    private String passphrase;
    private int timeout = 30000; // 30 seconds
    private boolean strictHostKeyChecking = false;
    
    // Getters and setters
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPrivateKeyPath() { return privateKeyPath; }
    public void setPrivateKeyPath(String privateKeyPath) { 
        this.privateKeyPath = privateKeyPath; 
    }
    public String getPassphrase() { return passphrase; }
    public void setPassphrase(String passphrase) { this.passphrase = passphrase; }
    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
    public boolean isStrictHostKeyChecking() { return strictHostKeyChecking; }
    public void setStrictHostKeyChecking(boolean strictHostKeyChecking) {
        this.strictHostKeyChecking = strictHostKeyChecking;
    }
}

/**
 * FTP Client Service (Mock Implementation)
 * In production, use Apache Commons Net FTPClient
 */
@Service
class FTPClientService {
    
    private final FTPConfig config;
    private boolean connected = false;
    
    public FTPClientService() {
        this.config = new FTPConfig();
        this.config.setHost("ftp.example.com");
        this.config.setUsername("user");
        this.config.setPassword("password");
    }
    
    /**
     * Connect to FTP server
     * 
     * Real implementation would use:
     * FTPClient ftpClient = new FTPClient();
     * ftpClient.connect(config.getHost(), config.getPort());
     * ftpClient.login(config.getUsername(), config.getPassword());
     * 
     * if (config.isPassiveMode()) {
     *     ftpClient.enterLocalPassiveMode();
     * }
     * 
     * if (config.isBinaryTransfer()) {
     *     ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
     * }
     */
    public void connect() throws IOException {
        System.out.println("Connecting to FTP server: " + config.getHost());
        this.connected = true;
    }
    
    /**
     * Disconnect from FTP server
     * 
     * Real implementation:
     * if (ftpClient.isConnected()) {
     *     ftpClient.logout();
     *     ftpClient.disconnect();
     * }
     */
    public void disconnect() throws IOException {
        System.out.println("Disconnecting from FTP server");
        this.connected = false;
    }
    
    /**
     * Upload file to FTP server
     * 
     * Real implementation:
     * try (InputStream input = new FileInputStream(localFile)) {
     *     ftpClient.storeFile(remoteFileName, input);
     * }
     */
    public boolean uploadFile(Path localFile, String remoteFileName) 
            throws IOException {
        
        if (!connected) {
            throw new IOException("Not connected to FTP server");
        }
        
        System.out.println("Uploading file: " + localFile + " to " + remoteFileName);
        
        // Simulate upload
        long fileSize = Files.size(localFile);
        System.out.println("File size: " + fileSize + " bytes");
        
        return true;
    }
    
    /**
     * Download file from FTP server
     * 
     * Real implementation:
     * try (OutputStream output = new FileOutputStream(localFile)) {
     *     ftpClient.retrieveFile(remoteFileName, output);
     * }
     */
    public boolean downloadFile(String remoteFileName, Path localFile) 
            throws IOException {
        
        if (!connected) {
            throw new IOException("Not connected to FTP server");
        }
        
        System.out.println("Downloading file: " + remoteFileName + " to " + localFile);
        
        // Simulate download
        Files.createDirectories(localFile.getParent());
        Files.writeString(localFile, "Downloaded content from " + remoteFileName);
        
        return true;
    }
    
    /**
     * List files in directory
     * 
     * Real implementation:
     * FTPFile[] files = ftpClient.listFiles(directory);
     */
    public List<String> listFiles(String directory) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to FTP server");
        }
        
        System.out.println("Listing files in directory: " + directory);
        
        // Simulate file list
        return Arrays.asList("file1.txt", "file2.pdf", "document.doc");
    }
    
    /**
     * Create directory
     * 
     * Real implementation:
     * ftpClient.makeDirectory(directory);
     */
    public boolean createDirectory(String directory) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to FTP server");
        }
        
        System.out.println("Creating directory: " + directory);
        return true;
    }
    
    /**
     * Delete file
     * 
     * Real implementation:
     * ftpClient.deleteFile(remoteFileName);
     */
    public boolean deleteFile(String remoteFileName) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to FTP server");
        }
        
        System.out.println("Deleting file: " + remoteFileName);
        return true;
    }
    
    /**
     * Rename file
     * 
     * Real implementation:
     * ftpClient.rename(oldName, newName);
     */
    public boolean renameFile(String oldName, String newName) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to FTP server");
        }
        
        System.out.println("Renaming file: " + oldName + " to " + newName);
        return true;
    }
    
    /**
     * Change working directory
     * 
     * Real implementation:
     * ftpClient.changeWorkingDirectory(directory);
     */
    public boolean changeDirectory(String directory) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to FTP server");
        }
        
        System.out.println("Changing to directory: " + directory);
        return true;
    }
}

/**
 * SFTP Client Service (Mock Implementation)
 * In production, use JSch library
 */
@Service
class SFTPClientService {
    
    private final SFTPConfig config;
    private boolean connected = false;
    
    public SFTPClientService() {
        this.config = new SFTPConfig();
        this.config.setHost("sftp.example.com");
        this.config.setUsername("user");
        this.config.setPassword("password");
    }
    
    /**
     * Connect to SFTP server
     * 
     * Real implementation using JSch:
     * JSch jsch = new JSch();
     * 
     * if (config.getPrivateKeyPath() != null) {
     *     if (config.getPassphrase() != null) {
     *         jsch.addIdentity(config.getPrivateKeyPath(), config.getPassphrase());
     *     } else {
     *         jsch.addIdentity(config.getPrivateKeyPath());
     *     }
     * }
     * 
     * Session session = jsch.getSession(config.getUsername(), 
     *                                   config.getHost(), 
     *                                   config.getPort());
     * 
     * if (config.getPassword() != null) {
     *     session.setPassword(config.getPassword());
     * }
     * 
     * Properties properties = new Properties();
     * if (!config.isStrictHostKeyChecking()) {
     *     properties.put("StrictHostKeyChecking", "no");
     * }
     * session.setConfig(properties);
     * session.setTimeout(config.getTimeout());
     * session.connect();
     * 
     * Channel channel = session.openChannel("sftp");
     * channel.connect();
     * ChannelSftp sftpChannel = (ChannelSftp) channel;
     */
    public void connect() throws Exception {
        System.out.println("Connecting to SFTP server: " + config.getHost());
        this.connected = true;
    }
    
    /**
     * Disconnect from SFTP server
     * 
     * Real implementation:
     * if (sftpChannel != null && sftpChannel.isConnected()) {
     *     sftpChannel.disconnect();
     * }
     * if (session != null && session.isConnected()) {
     *     session.disconnect();
     * }
     */
    public void disconnect() {
        System.out.println("Disconnecting from SFTP server");
        this.connected = false;
    }
    
    /**
     * Upload file to SFTP server
     * 
     * Real implementation:
     * sftpChannel.put(localFile.toString(), remoteFileName);
     */
    public void uploadFile(Path localFile, String remoteFileName) throws Exception {
        if (!connected) {
            throw new Exception("Not connected to SFTP server");
        }
        
        System.out.println("Uploading file via SFTP: " + localFile + " to " + remoteFileName);
        long fileSize = Files.size(localFile);
        System.out.println("File size: " + fileSize + " bytes");
    }
    
    /**
     * Download file from SFTP server
     * 
     * Real implementation:
     * sftpChannel.get(remoteFileName, localFile.toString());
     */
    public void downloadFile(String remoteFileName, Path localFile) throws Exception {
        if (!connected) {
            throw new Exception("Not connected to SFTP server");
        }
        
        System.out.println("Downloading file via SFTP: " + remoteFileName + " to " + localFile);
        Files.createDirectories(localFile.getParent());
        Files.writeString(localFile, "Downloaded content from " + remoteFileName);
    }
    
    /**
     * List files in directory
     * 
     * Real implementation:
     * Vector<ChannelSftp.LsEntry> fileList = sftpChannel.ls(directory);
     */
    public List<String> listFiles(String directory) throws Exception {
        if (!connected) {
            throw new Exception("Not connected to SFTP server");
        }
        
        System.out.println("Listing files in directory: " + directory);
        return Arrays.asList("secure-file1.txt", "secure-file2.pdf", "secure-doc.doc");
    }
    
    /**
     * Create directory
     * 
     * Real implementation:
     * sftpChannel.mkdir(directory);
     */
    public void createDirectory(String directory) throws Exception {
        if (!connected) {
            throw new Exception("Not connected to SFTP server");
        }
        
        System.out.println("Creating directory via SFTP: " + directory);
    }
    
    /**
     * Delete file
     * 
     * Real implementation:
     * sftpChannel.rm(remoteFileName);
     */
    public void deleteFile(String remoteFileName) throws Exception {
        if (!connected) {
            throw new Exception("Not connected to SFTP server");
        }
        
        System.out.println("Deleting file via SFTP: " + remoteFileName);
    }
    
    /**
     * Rename file
     * 
     * Real implementation:
     * sftpChannel.rename(oldName, newName);
     */
    public void renameFile(String oldName, String newName) throws Exception {
        if (!connected) {
            throw new Exception("Not connected to SFTP server");
        }
        
        System.out.println("Renaming file via SFTP: " + oldName + " to " + newName);
    }
    
    /**
     * Change directory
     * 
     * Real implementation:
     * sftpChannel.cd(directory);
     */
    public void changeDirectory(String directory) throws Exception {
        if (!connected) {
            throw new Exception("Not connected to SFTP server");
        }
        
        System.out.println("Changing to directory via SFTP: " + directory);
    }
    
    /**
     * Set file permissions (Unix)
     * 
     * Real implementation:
     * sftpChannel.chmod(permissions, remoteFileName);
     */
    public void setPermissions(String remoteFileName, int permissions) throws Exception {
        if (!connected) {
            throw new Exception("Not connected to SFTP server");
        }
        
        System.out.println("Setting permissions for file: " + remoteFileName + 
                " to " + Integer.toOctalString(permissions));
    }
}

/**
 * File Transfer Manager
 * High-level service for managing file transfers
 */
@Service
class FileTransferManager {
    
    private final FTPClientService ftpClient;
    private final SFTPClientService sftpClient;
    
    public FileTransferManager(FTPClientService ftpClient, 
                              SFTPClientService sftpClient) {
        this.ftpClient = ftpClient;
        this.sftpClient = sftpClient;
    }
    
    /**
     * Transfer file using FTP
     */
    public TransferResult transferViaFTP(Path localFile, String remoteFileName) {
        try {
            ftpClient.connect();
            boolean success = ftpClient.uploadFile(localFile, remoteFileName);
            ftpClient.disconnect();
            
            return new TransferResult(success, "FTP", 
                    success ? "Transfer completed" : "Transfer failed");
        } catch (IOException e) {
            return new TransferResult(false, "FTP", "Error: " + e.getMessage());
        }
    }
    
    /**
     * Transfer file using SFTP
     */
    public TransferResult transferViaSFTP(Path localFile, String remoteFileName) {
        try {
            sftpClient.connect();
            sftpClient.uploadFile(localFile, remoteFileName);
            sftpClient.disconnect();
            
            return new TransferResult(true, "SFTP", "Transfer completed");
        } catch (Exception e) {
            return new TransferResult(false, "SFTP", "Error: " + e.getMessage());
        }
    }
    
    /**
     * Sync directory via FTP
     */
    public SyncResult syncDirectoryFTP(Path localDirectory, String remoteDirectory) 
            throws IOException {
        
        ftpClient.connect();
        
        int uploaded = 0;
        int failed = 0;
        
        try (var stream = Files.walk(localDirectory)) {
            List<Path> files = stream.filter(Files::isRegularFile).toList();
            
            for (Path file : files) {
                Path relative = localDirectory.relativize(file);
                String remotePath = remoteDirectory + "/" + relative.toString()
                        .replace('\\', '/');
                
                try {
                    if (ftpClient.uploadFile(file, remotePath)) {
                        uploaded++;
                    } else {
                        failed++;
                    }
                } catch (IOException e) {
                    failed++;
                }
            }
        }
        
        ftpClient.disconnect();
        
        return new SyncResult(uploaded, failed, "FTP");
    }
}

record TransferResult(boolean success, String protocol, String message) {}

record SyncResult(int uploaded, int failed, String protocol) {
    public int total() {
        return uploaded + failed;
    }
    
    public double successRate() {
        return total() > 0 ? (uploaded * 100.0 / total()) : 0.0;
    }
}

/**
 * FTP/SFTP Integration Pattern - Main Demonstration
 */
public class FTPSFTPIntegrationPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== FTP/SFTP Integration Patterns Demo ===\n");
        
        // 1. FTP Operations
        demonstrateFTPOperations();
        
        // 2. SFTP Operations
        demonstrateSFTPOperations();
        
        // 3. File Transfer Manager
        demonstrateFileTransferManager();
        
        // 4. Configuration
        demonstrateConfiguration();
        
        // 5. Best Practices
        demonstrateBestPractices();
    }
    
    private static void demonstrateFTPOperations() throws IOException {
        System.out.println("1. FTP Operations:");
        
        FTPClientService ftpClient = new FTPClientService();
        
        ftpClient.connect();
        
        // Upload
        Path localFile = Files.createTempFile("upload-test", ".txt");
        Files.writeString(localFile, "Test content for FTP upload");
        ftpClient.uploadFile(localFile, "remote-file.txt");
        
        // List files
        List<String> files = ftpClient.listFiles("/");
        System.out.println("Files on FTP server: " + files);
        
        // Download
        Path downloadPath = Files.createTempFile("download-test", ".txt");
        ftpClient.downloadFile("remote-file.txt", downloadPath);
        
        ftpClient.disconnect();
        
        Files.deleteIfExists(localFile);
        Files.deleteIfExists(downloadPath);
        
        System.out.println();
    }
    
    private static void demonstrateSFTPOperations() throws Exception {
        System.out.println("2. SFTP Operations:");
        
        SFTPClientService sftpClient = new SFTPClientService();
        
        sftpClient.connect();
        
        // Upload
        Path localFile = Files.createTempFile("sftp-upload-test", ".txt");
        Files.writeString(localFile, "Test content for SFTP upload");
        sftpClient.uploadFile(localFile, "secure-file.txt");
        
        // Set permissions
        sftpClient.setPermissions("secure-file.txt", 0644);
        
        // List files
        List<String> files = sftpClient.listFiles("/");
        System.out.println("Files on SFTP server: " + files);
        
        // Download
        Path downloadPath = Files.createTempFile("sftp-download-test", ".txt");
        sftpClient.downloadFile("secure-file.txt", downloadPath);
        
        sftpClient.disconnect();
        
        Files.deleteIfExists(localFile);
        Files.deleteIfExists(downloadPath);
        
        System.out.println();
    }
    
    private static void demonstrateFileTransferManager() throws IOException {
        System.out.println("3. File Transfer Manager:");
        
        FTPClientService ftpClient = new FTPClientService();
        SFTPClientService sftpClient = new SFTPClientService();
        FileTransferManager manager = new FileTransferManager(ftpClient, sftpClient);
        
        Path testFile = Files.createTempFile("transfer-test", ".txt");
        Files.writeString(testFile, "Test content for transfer");
        
        // Transfer via FTP
        TransferResult ftpResult = manager.transferViaFTP(testFile, "file-via-ftp.txt");
        System.out.println("FTP Transfer: " + ftpResult.message());
        
        // Transfer via SFTP
        TransferResult sftpResult = manager.transferViaSFTP(testFile, "file-via-sftp.txt");
        System.out.println("SFTP Transfer: " + sftpResult.message());
        
        Files.deleteIfExists(testFile);
        
        System.out.println();
    }
    
    private static void demonstrateConfiguration() {
        System.out.println("4. Configuration:");
        
        System.out.println("FTP Configuration:");
        FTPConfig ftpConfig = new FTPConfig();
        ftpConfig.setHost("ftp.example.com");
        ftpConfig.setPort(21);
        ftpConfig.setPassiveMode(true);
        ftpConfig.setBinaryTransfer(true);
        System.out.println("  Host: " + ftpConfig.getHost());
        System.out.println("  Passive Mode: " + ftpConfig.isPassiveMode());
        System.out.println("  Binary Transfer: " + ftpConfig.isBinaryTransfer());
        
        System.out.println("\nSFTP Configuration:");
        SFTPConfig sftpConfig = new SFTPConfig();
        sftpConfig.setHost("sftp.example.com");
        sftpConfig.setPort(22);
        sftpConfig.setStrictHostKeyChecking(false);
        System.out.println("  Host: " + sftpConfig.getHost());
        System.out.println("  Port: " + sftpConfig.getPort());
        System.out.println("  Strict Host Key Checking: " + 
                sftpConfig.isStrictHostKeyChecking());
        
        System.out.println();
    }
    
    private static void demonstrateBestPractices() {
        System.out.println("5. Best Practices:");
        
        System.out.println("FTP Best Practices:");
        System.out.println("  - Use passive mode for firewall compatibility");
        System.out.println("  - Set appropriate timeouts");
        System.out.println("  - Use binary mode for non-text files");
        System.out.println("  - Always close connections in finally block");
        System.out.println("  - Handle connection failures gracefully");
        
        System.out.println("\nSFTP Best Practices:");
        System.out.println("  - Use key-based authentication when possible");
        System.out.println("  - Keep private keys secure");
        System.out.println("  - Set appropriate file permissions");
        System.out.println("  - Verify host keys in production");
        System.out.println("  - Use connection pooling for high throughput");
        
        System.out.println("\nGeneral Best Practices:");
        System.out.println("  - Implement retry logic for network failures");
        System.out.println("  - Log all transfer operations");
        System.out.println("  - Monitor transfer success/failure rates");
        System.out.println("  - Use checksums to verify file integrity");
        System.out.println("  - Implement progress tracking for large files");
        
        System.out.println("\n=== Demo Complete ===");
    }
}
