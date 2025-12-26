package com.example.mongodb;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * GridFS Pattern
 * 
 * Demonstrates MongoDB GridFS for storing large files.
 * 
 * GridFS Features:
 * - Store files larger than 16MB BSON limit
 * - File metadata storage
 * - File versioning
 * - Streaming support
 * - Chunk-based storage
 * 
 * Use Cases:
 * - File storage and retrieval
 * - Image/video storage
 * - Document management
 * - Large binary data
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class GridFSPattern {

    @Bean
    public FileStorageService fileStorageService(GridFsTemplate gridFsTemplate) {
        return new FileStorageService(gridFsTemplate);
    }
}

@RestController
@RequestMapping("/api/mongo/files")
class FileStorageService {

    private final GridFsTemplate gridFsTemplate;

    public FileStorageService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public String storeFile(MultipartFile file) throws IOException {
        return gridFsTemplate.store(
            file.getInputStream(),
            file.getOriginalFilename(),
            file.getContentType()
        ).toString();
    }

    public GridFSFile findFile(String fileId) {
        return gridFsTemplate.findOne(
            org.springframework.data.mongodb.core.query.Query.query(
                org.springframework.data.mongodb.core.query.Criteria.where("_id").is(fileId)
            )
        );
    }

    public InputStream getFileStream(String fileId) {
        GridFSFile file = findFile(fileId);
        if (file != null) {
            return gridFsTemplate.getResource(file).getInputStream();
        }
        return null;
    }

    public void deleteFile(String fileId) {
        gridFsTemplate.delete(
            org.springframework.data.mongodb.core.query.Query.query(
                org.springframework.data.mongodb.core.query.Criteria.where("_id").is(fileId)
            )
        );
    }
}

@RestController
@RequestMapping("/api/mongo/files")
class FileController {

    private final FileStorageService fileService;

    public FileController(FileStorageService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileId = fileService.storeFile(file);
            return ResponseEntity.ok(fileId);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Upload failed");
        }
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<GridFSFile> getFileInfo(@PathVariable String fileId) {
        GridFSFile file = fileService.findFile(fileId);
        return file != null ? ResponseEntity.ok(file) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getInfo() {
        return ResponseEntity.ok(new PatternInfo(
            "GridFS Pattern",
            "MongoDB GridFS for storing large files",
            "1.0",
            List.of("Large file storage", "Streaming", "Metadata", "Chunking"),
            List.of("File storage", "Image/video storage", "Document management")
        ));
    }

    record PatternInfo(String name, String description, String version,
                      List<String> features, List<String> useCases) {}
}
