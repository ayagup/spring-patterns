package com.example.fileupload.single;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;

/**
 * Single File Upload Pattern
 * 
 * Basic file upload handling with MultipartFile.
 */
@SpringBootApplication
public class SingleFileUploadPattern {

    public static void main(String[] args) {
        SpringApplication.run(SingleFileUploadPattern.class, args);
    }

    @RestController
    @RequestMapping("/api/files")
    public static class FileUploadController {

        private static final String UPLOAD_DIR = "uploads/";

        @PostMapping("/upload")
        public String uploadFile(@RequestParam("file") MultipartFile file) {
            try {
                if (file.isEmpty()) {
                    return "File is empty";
                }

                Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());

                return "File uploaded: " + file.getOriginalFilename();
            } catch (IOException e) {
                return "Upload failed: " + e.getMessage();
            }
        }
    }
}
