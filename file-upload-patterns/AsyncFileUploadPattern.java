package com.example.fileupload.async;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;

/**
 * Async File Upload Pattern
 * 
 * Processes file uploads asynchronously.
 */
@SpringBootApplication
@EnableAsync
public class AsyncFileUploadPattern {

    public static void main(String[] args) {
        SpringApplication.run(AsyncFileUploadPattern.class, args);
    }

    @Service
    public static class AsyncUploadService {

        @Async
        public CompletableFuture<String> processUpload(MultipartFile file) {
            try {
                Path path = Paths.get("uploads/" + file.getOriginalFilename());
                Files.write(path, file.getBytes());
                
                // Simulate processing
                Thread.sleep(2000);
                
                return CompletableFuture.completedFuture("Processed: " + file.getOriginalFilename());
            } catch (IOException | InterruptedException e) {
                return CompletableFuture.failedFuture(e);
            }
        }
    }

    @RestController
    public static class AsyncController {

        private final AsyncUploadService service;

        public AsyncController(AsyncUploadService service) {
            this.service = service;
        }

        @PostMapping("/api/upload-async")
        public CompletableFuture<String> upload(@RequestParam("file") MultipartFile file) {
            return service.processUpload(file);
        }
    }
}
