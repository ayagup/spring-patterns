package com.example.fileupload.chunked;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;

/**
 * Chunked File Upload Pattern
 * 
 * Supports resumable uploads via chunking.
 */
@SpringBootApplication
public class ChunkedFileUploadPattern {

    public static void main(String[] args) {
        SpringApplication.run(ChunkedFileUploadPattern.class, args);
    }

    @RestController
    public static class ChunkedController {

        @PostMapping("/api/upload-chunk")
        public String uploadChunk(
            @RequestParam("file") MultipartFile chunk,
            @RequestParam("chunkIndex") int index,
            @RequestParam("totalChunks") int total,
            @RequestParam("filename") String filename
        ) {
            try {
                String tempPath = "temp/" + filename + ".part" + index;
                Files.write(Paths.get(tempPath), chunk.getBytes());

                if (index == total - 1) {
                    // Last chunk - merge all
                    mergeChunks(filename, total);
                    return "Upload complete";
                }

                return "Chunk " + index + " uploaded";
            } catch (IOException e) {
                return "Failed: " + e.getMessage();
            }
        }

        private void mergeChunks(String filename, int total) throws IOException {
            Path outputPath = Paths.get("uploads/" + filename);
            try (OutputStream out = Files.newOutputStream(outputPath)) {
                for (int i = 0; i < total; i++) {
                    Path chunkPath = Paths.get("temp/" + filename + ".part" + i);
                    Files.copy(chunkPath, out);
                    Files.delete(chunkPath);
                }
            }
        }
    }
}
