package com.example.fileupload.streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * Streaming File Upload Pattern
 * 
 * Handles large file uploads with streaming.
 */
@SpringBootApplication
public class StreamingFileUploadPattern {

    public static void main(String[] args) {
        SpringApplication.run(StreamingFileUploadPattern.class, args);
    }

    @RestController
    public static class StreamingController {

        @PostMapping("/api/upload-stream")
        public String uploadStream(@RequestParam("file") MultipartFile file) {
            try (InputStream input = file.getInputStream();
                 OutputStream output = new FileOutputStream("uploads/" + file.getOriginalFilename())) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }

                return "Streamed successfully";
            } catch (IOException e) {
                return "Failed: " + e.getMessage();
            }
        }
    }
}
