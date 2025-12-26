package com.example.fileupload.multiple;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Multiple File Upload Pattern
 * 
 * Handles upload of multiple files in single request.
 */
@SpringBootApplication
public class MultipleFileUploadPattern {

    public static void main(String[] args) {
        SpringApplication.run(MultipleFileUploadPattern.class, args);
    }

    @RestController
    @RequestMapping("/api/files")
    public static class MultiFileController {

        @PostMapping("/upload-multiple")
        public List<String> uploadMultiple(@RequestParam("files") MultipartFile[] files) {
            List<String> uploaded = new ArrayList<>();

            for (MultipartFile file : files) {
                try {
                    Path path = Paths.get("uploads/" + file.getOriginalFilename());
                    Files.createDirectories(path.getParent());
                    Files.write(path, file.getBytes());
                    uploaded.add(file.getOriginalFilename());
                } catch (IOException e) {
                    uploaded.add("Failed: " + file.getOriginalFilename());
                }
            }

            return uploaded;
        }
    }
}
