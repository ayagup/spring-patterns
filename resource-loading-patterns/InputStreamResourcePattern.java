package com.example.resource;

import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Input Stream Resource Pattern
 * ==============================
 * 
 * Demonstrates InputStreamResource for wrapping an existing InputStream
 * as a Spring Resource.
 * 
 * Key Concepts:
 * ------------
 * 1. InputStreamResource - Wrap InputStream as Resource
 * 2. One-Time Read - Can only be read once
 * 3. Custom Sources - Wrap any InputStream
 * 4. Dynamic Content - Generate resources on-the-fly
 * 5. No Reset Support - Single-use resource
 * 
 * When to Use:
 * -----------
 * - Wrap existing InputStream
 * - Dynamic resource generation
 * - Database BLOB data
 * - Network stream wrapping
 * - Custom data sources
 * - Temporary data streams
 * 
 * Important Notes:
 * ---------------
 * - Can only be read ONCE
 * - Does not support exists() check
 * - Does not support contentLength()
 * - Descriptor should indicate single-use
 * - Not recommended for repeated access
 * 
 * When NOT to Use:
 * ---------------
 * - Resources need multiple reads
 * - Need exists() or contentLength()
 * - Better alternatives available:
 *   * ByteArrayResource for byte arrays
 *   * ClassPathResource for classpath
 *   * FileSystemResource for files
 *   * UrlResource for URLs
 * 
 * Best Practices:
 * --------------
 * - Use for single-read scenarios only
 * - Close underlying stream properly
 * - Provide meaningful description
 * - Consider ByteArrayResource for reusable data
 * - Document single-use nature
 * - Handle stream exhaustion gracefully
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class InputStreamResourcePattern {
    
    /**
     * Create resource from InputStream
     */
    public InputStreamResource createFromInputStream(InputStream inputStream, 
                                                     String description) {
        return new InputStreamResource(inputStream, description);
    }
    
    /**
     * Create resource from String
     */
    public InputStreamResource createFromString(String content, String description) {
        ByteArrayInputStream bais = new ByteArrayInputStream(
            content.getBytes(StandardCharsets.UTF_8));
        return new InputStreamResource(bais, description);
    }
    
    /**
     * Create resource from byte array
     */
    public InputStreamResource createFromBytes(byte[] data, String description) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        return new InputStreamResource(bais, description);
    }
    
    /**
     * Read resource content (can only be done ONCE)
     */
    public String readResource(InputStreamResource resource) throws IOException {
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}

/**
 * Example 2: Database BLOB Wrapper
 */
@Component
class DatabaseBlobResourceWrapper {
    
    /**
     * Wrap database BLOB as resource
     */
    public InputStreamResource wrapBlob(InputStream blobStream, String filename) {
        return new InputStreamResource(blobStream, "Database BLOB: " + filename);
    }
    
    /**
     * Read BLOB data
     * Note: Can only be read once
     */
    public byte[] readBlob(InputStreamResource resource) throws IOException {
        try (InputStream is = resource.getInputStream()) {
            return is.readAllBytes();
        }
    }
    
    /**
     * Example: Load image from database
     */
    public InputStreamResource loadImageFromDatabase(long imageId) {
        // Simulated database call
        // In real app: resultSet.getBinaryStream("image_data")
        byte[] imageData = new byte[0]; // Simulated
        
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        return new InputStreamResource(bais, "Image ID: " + imageId);
    }
}

/**
 * Example 3: Dynamic Content Generator
 */
@Component
class DynamicContentGenerator {
    
    /**
     * Generate CSV report as resource
     */
    public InputStreamResource generateCsvReport(java.util.List<String[]> data) {
        StringBuilder csv = new StringBuilder();
        
        for (String[] row : data) {
            csv.append(String.join(",", row)).append("\n");
        }
        
        ByteArrayInputStream bais = new ByteArrayInputStream(
            csv.toString().getBytes(StandardCharsets.UTF_8));
        
        return new InputStreamResource(bais, "Generated CSV Report");
    }
    
    /**
     * Generate JSON response as resource
     */
    public InputStreamResource generateJsonResponse(java.util.Map<String, Object> data) {
        // Simple JSON serialization
        StringBuilder json = new StringBuilder("{");
        
        int i = 0;
        for (java.util.Map.Entry<String, Object> entry : data.entrySet()) {
            if (i++ > 0) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":\"")
                .append(entry.getValue()).append("\"");
        }
        json.append("}");
        
        ByteArrayInputStream bais = new ByteArrayInputStream(
            json.toString().getBytes(StandardCharsets.UTF_8));
        
        return new InputStreamResource(bais, "Generated JSON");
    }
    
    /**
     * Generate text report
     */
    public InputStreamResource generateTextReport(String title, 
                                                  java.util.List<String> lines) {
        StringBuilder report = new StringBuilder();
        report.append(title).append("\n");
        report.append("=".repeat(title.length())).append("\n\n");
        
        for (String line : lines) {
            report.append(line).append("\n");
        }
        
        ByteArrayInputStream bais = new ByteArrayInputStream(
            report.toString().getBytes(StandardCharsets.UTF_8));
        
        return new InputStreamResource(bais, "Text Report: " + title);
    }
}

/**
 * Example 4: Network Stream Wrapper
 */
@Component
class NetworkStreamWrapper {
    
    /**
     * Wrap network stream as resource
     */
    public InputStreamResource wrapNetworkStream(InputStream networkStream, 
                                                String description) {
        return new InputStreamResource(networkStream, 
            "Network Stream: " + description);
    }
    
    /**
     * Example: Wrap HTTP response stream
     */
    public InputStreamResource wrapHttpResponse(java.net.URLConnection connection) 
                                                throws IOException {
        InputStream stream = connection.getInputStream();
        String url = connection.getURL().toString();
        
        return new InputStreamResource(stream, "HTTP Response: " + url);
    }
}

/**
 * Example 5: Compressed Stream Wrapper
 */
@Component
class CompressedStreamWrapper {
    
    /**
     * Wrap GZIP stream as resource
     */
    public InputStreamResource wrapGzipStream(InputStream gzipStream) {
        try {
            java.util.zip.GZIPInputStream gis = 
                new java.util.zip.GZIPInputStream(gzipStream);
            return new InputStreamResource(gis, "GZIP Stream");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create GZIP stream", e);
        }
    }
    
    /**
     * Wrap ZIP entry as resource
     */
    public InputStreamResource wrapZipEntry(java.util.zip.ZipInputStream zis, 
                                           String entryName) {
        return new InputStreamResource(zis, "ZIP Entry: " + entryName);
    }
}

/**
 * Example 6: In-Memory Data Wrapper
 */
@Component
class InMemoryDataWrapper {
    
    /**
     * Wrap serialized object as resource
     */
    public InputStreamResource wrapSerializedObject(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return new InputStreamResource(bais, 
            "Serialized: " + obj.getClass().getSimpleName());
    }
    
    /**
     * Wrap properties as resource
     */
    public InputStreamResource wrapProperties(java.util.Properties props) 
                                             throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.store(baos, "Generated Properties");
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return new InputStreamResource(bais, "Properties Resource");
    }
}

/**
 * Example 7: Piped Stream Resource
 */
@Component
class PipedStreamResource {
    
    /**
     * Create resource from piped streams
     */
    public InputStreamResource createPipedResource() throws IOException {
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream(pis);
        
        // Write data in separate thread
        new Thread(() -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(pos, StandardCharsets.UTF_8)) {
                writer.write("Line 1\n");
                writer.write("Line 2\n");
                writer.write("Line 3\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        
        return new InputStreamResource(pis, "Piped Stream");
    }
}

/**
 * Example 8: Resource Adapter
 */
@Component
class ResourceAdapter {
    
    /**
     * Adapt any InputStream source to Resource
     */
    public InputStreamResource adapt(InputStreamSupplier supplier, 
                                     String description) throws IOException {
        InputStream stream = supplier.get();
        return new InputStreamResource(stream, description);
    }
}

/**
 * Functional interface for InputStream suppliers
 */
@FunctionalInterface
interface InputStreamSupplier {
    InputStream get() throws IOException;
}

/**
 * Usage Examples
 */
class InputStreamResourceUsageExamples {
    
    public static void main(String[] args) throws IOException {
        InputStreamResourcePattern pattern = new InputStreamResourcePattern();
        
        // Example 1: From String
        InputStreamResource stringResource = pattern.createFromString(
            "Hello, World!", "String Content");
        
        // Can only read ONCE
        String content = pattern.readResource(stringResource);
        System.out.println("Content: " + content);
        
        // Example 2: From bytes
        byte[] data = "Binary data".getBytes(StandardCharsets.UTF_8);
        InputStreamResource byteResource = pattern.createFromBytes(data, "Binary Data");
        
        // Example 3: Dynamic CSV generation
        DynamicContentGenerator generator = new DynamicContentGenerator();
        
        java.util.List<String[]> csvData = java.util.Arrays.asList(
            new String[]{"Name", "Age", "City"},
            new String[]{"John", "30", "New York"},
            new String[]{"Jane", "25", "Boston"}
        );
        
        InputStreamResource csvResource = generator.generateCsvReport(csvData);
        System.out.println("CSV Description: " + csvResource.getDescription());
        
        // Example 4: JSON generation
        java.util.Map<String, Object> jsonData = new java.util.HashMap<>();
        jsonData.put("status", "success");
        jsonData.put("message", "Operation completed");
        
        InputStreamResource jsonResource = generator.generateJsonResponse(jsonData);
        
        // Example 5: Database BLOB
        DatabaseBlobResourceWrapper blobWrapper = new DatabaseBlobResourceWrapper();
        // InputStreamResource imageResource = blobWrapper.loadImageFromDatabase(123L);
        
        System.out.println("\nInputStreamResource Important Notes:");
        System.out.println("1. Can only be read ONCE");
        System.out.println("2. Does not support exists() check");
        System.out.println("3. Does not support contentLength()");
        System.out.println("4. Use ByteArrayResource for multiple reads");
        System.out.println("5. Perfect for streaming scenarios");
    }
}
