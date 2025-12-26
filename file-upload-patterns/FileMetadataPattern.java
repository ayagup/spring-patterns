package com.example.fileupload.metadata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * File Metadata Storage Pattern
 * 
 * Stores file metadata in database.
 */
@SpringBootApplication
public class FileMetadataPattern {

    public static void main(String[] args) {
        SpringApplication.run(FileMetadataPattern.class, args);
    }

    @Entity
    @Table(name = "file_metadata")
    public static class FileMetadata {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String filename;
        private String contentType;
        private Long size;
        private String path;
        private LocalDateTime uploadedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public Long getSize() { return size; }
        public void setSize(Long size) { this.size = size; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public LocalDateTime getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    }

    public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {}

    @RestController
    public static class MetadataController {

        private final FileMetadataRepository repository;

        public MetadataController(FileMetadataRepository repository) {
            this.repository = repository;
        }

        @PostMapping("/api/upload-metadata")
        public FileMetadata upload(@RequestParam("file") MultipartFile file) {
            FileMetadata metadata = new FileMetadata();
            metadata.setFilename(file.getOriginalFilename());
            metadata.setContentType(file.getContentType());
            metadata.setSize(file.getSize());
            metadata.setPath("uploads/" + file.getOriginalFilename());
            metadata.setUploadedAt(LocalDateTime.now());
            
            return repository.save(metadata);
        }
    }
}
