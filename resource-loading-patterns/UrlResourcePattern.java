package com.example.resource;

import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * URL Resource Pattern
 * ====================
 * 
 * Demonstrates UrlResource for loading resources from URLs
 * (HTTP, HTTPS, FTP, FILE, JAR protocols).
 * 
 * Key Concepts:
 * ------------
 * 1. UrlResource - URL-based resource access
 * 2. Protocol Support - HTTP, HTTPS, FTP, FILE, JAR
 * 3. Remote Resources - Load from internet
 * 4. Connection Management - Handle timeouts
 * 5. Stream-Based - Read-only access
 * 
 * Supported Protocols:
 * -------------------
 * - http:// - HTTP resources
 * - https:// - Secure HTTP resources
 * - ftp:// - FTP resources
 * - file:// - Local file URLs
 * - jar:// - Resources inside JAR files
 * 
 * When to Use:
 * -----------
 * - Load from remote servers
 * - Download configuration
 * - Fetch external data
 * - Access web APIs
 * - Read from network shares
 * - JAR resource access
 * 
 * Advantages:
 * ----------
 * - Multiple protocol support
 * - Unified API for all URLs
 * - Connection abstraction
 * - Streaming support
 * - Standard Java URL integration
 * 
 * Best Practices:
 * --------------
 * - Set connection timeouts
 * - Handle network errors
 * - Validate URLs before use
 * - Close streams properly
 * - Use HTTPS for sensitive data
 * - Cache downloaded resources
 * - Handle redirects appropriately
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class UrlResourcePattern {
    
    /**
     * Load resource from HTTP URL
     */
    public String loadFromHttp(String urlString) throws IOException {
        UrlResource resource = new UrlResource(urlString);
        
        if (!resource.exists()) {
            throw new IOException("Resource not found: " + urlString);
        }
        
        return readResource(resource);
    }
    
    /**
     * Load resource from HTTPS URL
     */
    public String loadFromHttps(String urlString) throws IOException {
        UrlResource resource = new UrlResource(urlString);
        return readResource(resource);
    }
    
    /**
     * Load resource from FTP URL
     */
    public String loadFromFtp(String urlString) throws IOException {
        UrlResource resource = new UrlResource(urlString);
        return readResource(resource);
    }
    
    /**
     * Load resource from file URL
     */
    public String loadFromFileUrl(String filePath) throws IOException {
        String urlString = "file://" + filePath;
        UrlResource resource = new UrlResource(urlString);
        return readResource(resource);
    }
    
    /**
     * Load resource from JAR URL
     */
    public String loadFromJar(String jarPath, String resourcePath) throws IOException {
        String urlString = "jar:file:" + jarPath + "!/" + resourcePath;
        UrlResource resource = new UrlResource(urlString);
        return readResource(resource);
    }
    
    /**
     * Load resource from URL object
     */
    public String loadFromUrl(URL url) throws IOException {
        UrlResource resource = new UrlResource(url);
        return readResource(resource);
    }
    
    private String readResource(UrlResource resource) throws IOException {
        try (InputStream is = resource.getInputStream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}

/**
 * Example 2: Configuration Downloader
 */
@Component
class ConfigurationDownloader {
    
    /**
     * Download configuration from URL
     */
    public String downloadConfig(String configUrl) throws IOException {
        UrlResource resource = new UrlResource(configUrl);
        
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    /**
     * Download JSON configuration
     */
    public String downloadJsonConfig(String baseUrl, String filename) throws IOException {
        String url = baseUrl.endsWith("/") ? baseUrl + filename : baseUrl + "/" + filename;
        return downloadConfig(url);
    }
    
    /**
     * Download with fallback URLs
     */
    public String downloadWithFallback(String... urls) throws IOException {
        IOException lastException = null;
        
        for (String url : urls) {
            try {
                return downloadConfig(url);
            } catch (IOException e) {
                lastException = e;
                // Try next URL
            }
        }
        
        throw new IOException("All URLs failed", lastException);
    }
}

/**
 * Example 3: Remote Data Fetcher
 */
@Component
class RemoteDataFetcher {
    
    /**
     * Fetch JSON data from API
     */
    public String fetchJson(String apiUrl) throws IOException {
        UrlResource resource = new UrlResource(apiUrl);
        
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    /**
     * Fetch XML data
     */
    public String fetchXml(String xmlUrl) throws IOException {
        UrlResource resource = new UrlResource(xmlUrl);
        
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    /**
     * Fetch binary data
     */
    public byte[] fetchBinary(String url) throws IOException {
        UrlResource resource = new UrlResource(url);
        
        try (InputStream is = resource.getInputStream()) {
            return is.readAllBytes();
        }
    }
}

/**
 * Example 4: Resource Validator
 */
@Component
class UrlResourceValidator {
    
    /**
     * Check if URL resource exists
     */
    public boolean resourceExists(String urlString) {
        try {
            UrlResource resource = new UrlResource(urlString);
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validate URL format
     */
    public boolean isValidUrl(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
    
    /**
     * Get resource metadata
     */
    public UrlResourceMetadata getMetadata(String urlString) throws IOException {
        UrlResource resource = new UrlResource(urlString);
        
        return new UrlResourceMetadata(
            resource.getURL().toString(),
            resource.exists(),
            resource.isReadable(),
            resource.contentLength(),
            resource.getFilename()
        );
    }
}

/**
 * URL resource metadata
 */
class UrlResourceMetadata {
    private final String url;
    private final boolean exists;
    private final boolean readable;
    private final long contentLength;
    private final String filename;
    
    public UrlResourceMetadata(String url, boolean exists, boolean readable,
                              long contentLength, String filename) {
        this.url = url;
        this.exists = exists;
        this.readable = readable;
        this.contentLength = contentLength;
        this.filename = filename;
    }
    
    @Override
    public String toString() {
        return String.format("UrlResourceMetadata{url='%s', exists=%s, readable=%s, " +
                           "contentLength=%d, filename='%s'}",
                           url, exists, readable, contentLength, filename);
    }
}

/**
 * Example 5: File Downloader
 */
@Component
class FileDownloader {
    
    /**
     * Download file to byte array
     */
    public byte[] download(String url) throws IOException {
        UrlResource resource = new UrlResource(url);
        
        try (InputStream is = resource.getInputStream()) {
            return is.readAllBytes();
        }
    }
    
    /**
     * Download file with progress tracking
     */
    public byte[] downloadWithProgress(String url, ProgressListener listener) 
                                      throws IOException {
        UrlResource resource = new UrlResource(url);
        
        long totalSize = resource.contentLength();
        byte[] buffer = new byte[8192];
        int bytesRead;
        long totalRead = 0;
        
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        
        try (InputStream is = resource.getInputStream()) {
            while ((bytesRead = is.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
                
                if (listener != null && totalSize > 0) {
                    int progress = (int) ((totalRead * 100) / totalSize);
                    listener.onProgress(progress, totalRead, totalSize);
                }
            }
        }
        
        return output.toByteArray();
    }
}

/**
 * Progress listener interface
 */
interface ProgressListener {
    void onProgress(int percentage, long bytesRead, long totalBytes);
}

/**
 * Example 6: JAR Resource Loader
 */
@Component
class JarResourceLoader {
    
    /**
     * Load resource from JAR file
     */
    public String loadFromJar(String jarFilePath, String resourcePath) throws IOException {
        String jarUrl = "jar:file:" + jarFilePath + "!/" + resourcePath;
        UrlResource resource = new UrlResource(jarUrl);
        
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    /**
     * Load properties from JAR
     */
    public java.util.Properties loadPropertiesFromJar(String jarFilePath, 
                                                      String propertiesPath) 
                                                      throws IOException {
        String jarUrl = "jar:file:" + jarFilePath + "!/" + propertiesPath;
        UrlResource resource = new UrlResource(jarUrl);
        
        java.util.Properties props = new java.util.Properties();
        try (InputStream is = resource.getInputStream()) {
            props.load(is);
        }
        
        return props;
    }
}

/**
 * Example 7: Multi-Protocol Loader
 */
@Component
class MultiProtocolLoader {
    
    /**
     * Load resource from any protocol
     */
    public String loadResource(String urlString) throws IOException {
        UrlResource resource = new UrlResource(urlString);
        
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    /**
     * Determine protocol and load appropriately
     */
    public String loadWithProtocolDetection(String urlString) throws IOException {
        URL url = new URL(urlString);
        String protocol = url.getProtocol();
        
        System.out.println("Loading from protocol: " + protocol);
        
        return loadResource(urlString);
    }
    
    /**
     * Load with custom headers (for HTTP/HTTPS)
     */
    public String loadWithHeaders(String urlString, 
                                  java.util.Map<String, String> headers) 
                                  throws IOException {
        URL url = new URL(urlString);
        java.net.URLConnection connection = url.openConnection();
        
        // Set headers
        for (java.util.Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        
        try (InputStream is = connection.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}

/**
 * Usage Examples
 */
class UrlResourceUsageExamples {
    
    public static void main(String[] args) throws IOException {
        UrlResourcePattern loader = new UrlResourcePattern();
        
        // Example 1: Load from HTTP
        // String html = loader.loadFromHttp("http://example.com/data.json");
        
        // Example 2: Load from HTTPS
        // String config = loader.loadFromHttps("https://api.example.com/config.json");
        
        // Example 3: Load from FTP
        // String ftpData = loader.loadFromFtp("ftp://ftp.example.com/data.txt");
        
        // Example 4: Load from file URL
        // String fileData = loader.loadFromFileUrl("/path/to/file.txt");
        
        // Example 5: Load from JAR
        // String jarResource = loader.loadFromJar("/path/to/app.jar", "config/settings.properties");
        
        // Configuration download
        ConfigurationDownloader downloader = new ConfigurationDownloader();
        // String config = downloader.downloadConfig("https://config.example.com/app.json");
        // String withFallback = downloader.downloadWithFallback(
        //     "https://primary.com/config.json",
        //     "https://backup.com/config.json"
        // );
        
        // Remote data fetching
        RemoteDataFetcher fetcher = new RemoteDataFetcher();
        // String json = fetcher.fetchJson("https://api.example.com/data");
        // byte[] image = fetcher.fetchBinary("https://example.com/image.png");
        
        // File download with progress
        FileDownloader fileDownloader = new FileDownloader();
        // byte[] file = fileDownloader.downloadWithProgress(
        //     "https://example.com/large-file.zip",
        //     (progress, read, total) -> System.out.println("Progress: " + progress + "%")
        // );
        
        System.out.println("UrlResource Pattern Demonstration");
        System.out.println("Supported protocols:");
        System.out.println("- HTTP/HTTPS: https://example.com/data.json");
        System.out.println("- FTP: ftp://ftp.example.com/file.txt");
        System.out.println("- FILE: file:///path/to/file.txt");
        System.out.println("- JAR: jar:file:/path/to/app.jar!/config.properties");
    }
}
