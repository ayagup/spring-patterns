package com.spring.patterns.filestream;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.*;

/**
 * Input/Output Stream Patterns
 * 
 * Demonstrates comprehensive I/O stream handling:
 * - InputStream operations
 * - OutputStream operations
 * - Buffered streams
 * - Data streams
 * - Object streams
 * - Filtering streams
 * - Stream decorators
 * - NIO channels
 * - Stream compression
 * - Stream encryption
 * - Stream transformation
 * 
 * Use Cases:
 * - File reading/writing
 * - Network I/O
 * - Binary data processing
 * - Object serialization
 * - Data transformation
 * - Compression/decompression
 * 
 * Dependencies:
 * - Java I/O API
 * - Java NIO
 */

/**
 * Input Stream Handler
 */
@Service
class InputStreamHandler {
    
    /**
     * Read entire stream to byte array
     */
    public byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(data)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        
        return buffer.toByteArray();
    }
    
    /**
     * Read stream as string
     */
    public String readAsString(InputStream inputStream) throws IOException {
        return new String(readAllBytes(inputStream), StandardCharsets.UTF_8);
    }
    
    /**
     * Read stream line by line
     */
    public List<String> readLines(InputStream inputStream) throws IOException {
        List<String> lines = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        
        return lines;
    }
    
    /**
     * Read with buffer
     */
    public void readWithBuffer(InputStream inputStream, 
                              Consumer<byte[]> processor) throws IOException {
        
        byte[] buffer = new byte[8192];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byte[] data = Arrays.copyOf(buffer, bytesRead);
            processor.accept(data);
        }
    }
    
    /**
     * Skip bytes
     */
    public long skipBytes(InputStream inputStream, long n) throws IOException {
        return inputStream.skip(n);
    }
    
    /**
     * Check available bytes
     */
    public int getAvailableBytes(InputStream inputStream) throws IOException {
        return inputStream.available();
    }
    
    /**
     * Mark and reset support
     */
    public boolean supportsMarkReset(InputStream inputStream) {
        return inputStream.markSupported();
    }
}

/**
 * Output Stream Handler
 */
@Service
class OutputStreamHandler {
    
    /**
     * Write byte array to stream
     */
    public void writeBytes(OutputStream outputStream, byte[] data) 
            throws IOException {
        outputStream.write(data);
        outputStream.flush();
    }
    
    /**
     * Write string to stream
     */
    public void writeString(OutputStream outputStream, String text) 
            throws IOException {
        writeBytes(outputStream, text.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Write lines to stream
     */
    public void writeLines(OutputStream outputStream, List<String> lines) 
            throws IOException {
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        }
    }
    
    /**
     * Write with buffering
     */
    public void writeWithBuffer(OutputStream outputStream, 
                               InputStream inputStream) throws IOException {
        
        byte[] buffer = new byte[8192];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
    }
}

/**
 * Buffered Stream Handler
 */
@Service
class BufferedStreamHandler {
    
    /**
     * Create buffered input stream
     */
    public BufferedInputStream createBufferedInput(InputStream inputStream) {
        return new BufferedInputStream(inputStream, 8192);
    }
    
    /**
     * Create buffered output stream
     */
    public BufferedOutputStream createBufferedOutput(OutputStream outputStream) {
        return new BufferedOutputStream(outputStream, 8192);
    }
    
    /**
     * Copy with buffering
     */
    public long copyWithBuffer(InputStream input, OutputStream output) 
            throws IOException {
        
        long totalBytes = 0;
        byte[] buffer = new byte[8192];
        int bytesRead;
        
        try (BufferedInputStream bis = createBufferedInput(input);
             BufferedOutputStream bos = createBufferedOutput(output)) {
            
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            bos.flush();
        }
        
        return totalBytes;
    }
}

/**
 * Data Stream Handler
 * For reading/writing primitive types
 */
@Service
class DataStreamHandler {
    
    /**
     * Write primitive data
     */
    public void writePrimitives(OutputStream outputStream) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(outputStream)) {
            dos.writeBoolean(true);
            dos.writeByte(127);
            dos.writeShort(32767);
            dos.writeInt(2147483647);
            dos.writeLong(9223372036854775807L);
            dos.writeFloat(3.14f);
            dos.writeDouble(3.14159265359);
            dos.writeUTF("Hello, World!");
            dos.flush();
        }
    }
    
    /**
     * Read primitive data
     */
    public PrimitiveData readPrimitives(InputStream inputStream) throws IOException {
        try (DataInputStream dis = new DataInputStream(inputStream)) {
            return new PrimitiveData(
                    dis.readBoolean(),
                    dis.readByte(),
                    dis.readShort(),
                    dis.readInt(),
                    dis.readLong(),
                    dis.readFloat(),
                    dis.readDouble(),
                    dis.readUTF()
            );
        }
    }
}

record PrimitiveData(
        boolean booleanValue,
        byte byteValue,
        short shortValue,
        int intValue,
        long longValue,
        float floatValue,
        double doubleValue,
        String stringValue
) {}

/**
 * Object Stream Handler
 * For object serialization
 */
@Service
class ObjectStreamHandler {
    
    /**
     * Write object
     */
    public void writeObject(OutputStream outputStream, Serializable object) 
            throws IOException {
        
        try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
            oos.writeObject(object);
            oos.flush();
        }
    }
    
    /**
     * Read object
     */
    public Object readObject(InputStream inputStream) 
            throws IOException, ClassNotFoundException {
        
        try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
            return ois.readObject();
        }
    }
    
    /**
     * Write multiple objects
     */
    public void writeObjects(OutputStream outputStream, List<Serializable> objects) 
            throws IOException {
        
        try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
            oos.writeInt(objects.size());
            for (Serializable object : objects) {
                oos.writeObject(object);
            }
            oos.flush();
        }
    }
    
    /**
     * Read multiple objects
     */
    public List<Object> readObjects(InputStream inputStream) 
            throws IOException, ClassNotFoundException {
        
        List<Object> objects = new ArrayList<>();
        
        try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
            int count = ois.readInt();
            for (int i = 0; i < count; i++) {
                objects.add(ois.readObject());
            }
        }
        
        return objects;
    }
}

/**
 * Filtering Stream Handler
 */
@Service
class FilteringStreamHandler {
    
    /**
     * Filter stream with custom logic
     */
    public InputStream createFilteredInputStream(InputStream inputStream, 
                                                Predicate predicate) {
        return new FilterInputStream(inputStream) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int bytesRead = super.read(b, off, len);
                if (bytesRead > 0 && !predicate.test(b, off, bytesRead)) {
                    return 0; // Filter out
                }
                return bytesRead;
            }
        };
    }
    
    /**
     * Transform stream data
     */
    public OutputStream createTransformingOutputStream(OutputStream outputStream,
                                                      Transformer transformer) {
        return new FilterOutputStream(outputStream) {
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                byte[] transformed = transformer.transform(b, off, len);
                super.write(transformed, 0, transformed.length);
            }
        };
    }
    
    @FunctionalInterface
    interface Predicate {
        boolean test(byte[] data, int offset, int length);
    }
    
    @FunctionalInterface
    interface Transformer {
        byte[] transform(byte[] data, int offset, int length);
    }
}

/**
 * Compression Stream Handler
 */
@Service
class CompressionStreamHandler {
    
    /**
     * Compress with GZIP
     */
    public byte[] compressGzip(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(data);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Decompress GZIP
     */
    public byte[] decompressGzip(byte[] compressedData) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
        
        try (GZIPInputStream gzipIn = new GZIPInputStream(bais)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Compress with ZIP
     */
    public void compressZip(Map<String, byte[]> files, OutputStream outputStream) 
            throws IOException {
        
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            for (Map.Entry<String, byte[]> entry : files.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zos.putNextEntry(zipEntry);
                zos.write(entry.getValue());
                zos.closeEntry();
            }
        }
    }
    
    /**
     * Decompress ZIP
     */
    public Map<String, byte[]> decompressZip(InputStream inputStream) 
            throws IOException {
        
        Map<String, byte[]> files = new HashMap<>();
        
        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                }
                files.put(entry.getName(), baos.toByteArray());
                zis.closeEntry();
            }
        }
        
        return files;
    }
}

/**
 * NIO Channel Handler
 */
@Service
class NIOChannelHandler {
    
    /**
     * Read file using NIO channels
     */
    public byte[] readWithChannel(Path filePath) throws IOException {
        try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();
            
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            return data;
        }
    }
    
    /**
     * Write file using NIO channels
     */
    public void writeWithChannel(Path filePath, byte[] data) throws IOException {
        try (FileChannel channel = FileChannel.open(filePath, 
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            
            ByteBuffer buffer = ByteBuffer.wrap(data);
            channel.write(buffer);
        }
    }
    
    /**
     * Copy using channels (zero-copy)
     */
    public long transferBetweenChannels(Path source, Path target) throws IOException {
        try (FileChannel sourceChannel = FileChannel.open(source, StandardOpenOption.READ);
             FileChannel targetChannel = FileChannel.open(target, 
                     StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            
            return sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
        }
    }
    
    /**
     * Memory-mapped file reading
     */
    public ByteBuffer readMemoryMapped(Path filePath) throws IOException {
        try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
    }
}

/**
 * Stream Utility Service
 */
@Service
class StreamUtilityService {
    
    /**
     * Copy stream
     */
    public long copyStream(InputStream input, OutputStream output) 
            throws IOException {
        
        long totalBytes = 0;
        byte[] buffer = new byte[8192];
        int bytesRead;
        
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
            totalBytes += bytesRead;
        }
        
        return totalBytes;
    }
    
    /**
     * Copy stream with progress
     */
    public void copyWithProgress(InputStream input, OutputStream output,
                                 long totalSize, ProgressListener listener) 
            throws IOException {
        
        long bytesTransferred = 0;
        byte[] buffer = new byte[8192];
        int bytesRead;
        
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
            bytesTransferred += bytesRead;
            
            if (listener != null) {
                listener.onProgress(bytesTransferred, totalSize);
            }
        }
    }
    
    /**
     * Read stream to byte array with size limit
     */
    public byte[] readLimited(InputStream input, int maxSize) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;
        int totalRead = 0;
        
        while ((bytesRead = input.read(data)) != -1) {
            int toWrite = Math.min(bytesRead, maxSize - totalRead);
            buffer.write(data, 0, toWrite);
            totalRead += toWrite;
            
            if (totalRead >= maxSize) {
                break;
            }
        }
        
        return buffer.toByteArray();
    }
    
    @FunctionalInterface
    interface ProgressListener {
        void onProgress(long bytesTransferred, long totalSize);
    }
}

/**
 * Pipe Stream Handler
 */
@Service
class PipeStreamHandler {
    
    /**
     * Create piped streams for thread communication
     */
    public PipedStreams createPipedStreams() throws IOException {
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        
        return new PipedStreams(inputStream, outputStream);
    }
    
    /**
     * Example: Producer-Consumer pattern
     */
    public void demonstratePipedStreams() throws IOException {
        PipedStreams pipes = createPipedStreams();
        
        // Producer thread
        Thread producer = new Thread(() -> {
            try (PipedOutputStream out = pipes.outputStream()) {
                for (int i = 0; i < 10; i++) {
                    out.write(("Message " + i + "\n").getBytes());
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        // Consumer thread
        Thread consumer = new Thread(() -> {
            try (PipedInputStream in = pipes.inputStream()) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Received: " + line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        producer.start();
        consumer.start();
    }
}

record PipedStreams(PipedInputStream inputStream, PipedOutputStream outputStream) {}

/**
 * Stream I/O Pattern - Main Demonstration
 */
public class StreamIOPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Stream I/O Patterns Demo ===\n");
        
        // 1. Input Stream Operations
        demonstrateInputStreams();
        
        // 2. Output Stream Operations
        demonstrateOutputStreams();
        
        // 3. Buffered Streams
        demonstrateBufferedStreams();
        
        // 4. Data Streams
        demonstrateDataStreams();
        
        // 5. Compression
        demonstrateCompression();
        
        // 6. NIO Channels
        demonstrateNIOChannels();
    }
    
    private static void demonstrateInputStreams() throws IOException {
        System.out.println("1. Input Stream Operations:");
        
        InputStreamHandler handler = new InputStreamHandler();
        
        byte[] testData = "Hello, Stream!".getBytes();
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        
        String content = handler.readAsString(input);
        System.out.println("Read string: " + content);
        
        System.out.println();
    }
    
    private static void demonstrateOutputStreams() throws IOException {
        System.out.println("2. Output Stream Operations:");
        
        OutputStreamHandler handler = new OutputStreamHandler();
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.writeString(output, "Test output");
        
        System.out.println("Written bytes: " + output.size());
        System.out.println("Content: " + output.toString());
        
        System.out.println();
    }
    
    private static void demonstrateBufferedStreams() throws IOException {
        System.out.println("3. Buffered Streams:");
        
        BufferedStreamHandler handler = new BufferedStreamHandler();
        
        byte[] data = "Large data content...".getBytes();
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        long bytesCopied = handler.copyWithBuffer(input, output);
        System.out.println("Bytes copied with buffer: " + bytesCopied);
        
        System.out.println();
    }
    
    private static void demonstrateDataStreams() throws IOException {
        System.out.println("4. Data Streams:");
        
        DataStreamHandler handler = new DataStreamHandler();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        handler.writePrimitives(baos);
        
        System.out.println("Written primitive data: " + baos.size() + " bytes");
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        PrimitiveData data = handler.readPrimitives(bais);
        
        System.out.println("Read data: " + data);
        
        System.out.println();
    }
    
    private static void demonstrateCompression() throws IOException {
        System.out.println("5. Compression:");
        
        CompressionStreamHandler handler = new CompressionStreamHandler();
        
        String text = "This is a test string that will be compressed.";
        byte[] original = text.getBytes();
        
        byte[] compressed = handler.compressGzip(original);
        byte[] decompressed = handler.decompressGzip(compressed);
        
        System.out.println("Original size: " + original.length);
        System.out.println("Compressed size: " + compressed.length);
        System.out.println("Compression ratio: " + 
                String.format("%.2f%%", (1 - (double)compressed.length / original.length) * 100));
        System.out.println("Decompressed matches: " + 
                Arrays.equals(original, decompressed));
        
        System.out.println();
    }
    
    private static void demonstrateNIOChannels() throws IOException {
        System.out.println("6. NIO Channels:");
        
        NIOChannelHandler handler = new NIOChannelHandler();
        
        Path testFile = Files.createTempFile("nio-test", ".txt");
        byte[] data = "NIO Channel Test".getBytes();
        
        handler.writeWithChannel(testFile, data);
        System.out.println("Written with NIO channel");
        
        byte[] readData = handler.readWithChannel(testFile);
        System.out.println("Read with NIO channel: " + new String(readData));
        
        Files.deleteIfExists(testFile);
        
        System.out.println("\n=== Demo Complete ===");
    }
}
