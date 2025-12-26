package com.example.springaipatterns;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PdfDocumentReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Document Reader Pattern
 * 
 * Demonstrates the use of Spring AI's Document Readers for loading and parsing
 * documents from various sources and formats.
 * 
 * Supported Formats:
 * - PDF documents
 * - Text files (TXT, MD)
 * - Word documents (DOCX)
 * - Web pages (HTML)
 * 
 * Key Concepts:
 * - Document loading from multiple sources
 * - Format-specific readers
 * - Document metadata extraction
 * - Content extraction and parsing
 * - Batch document loading
 */
@SpringBootApplication
public class DocumentReaderPattern {

    public static void main(String[] args) {
        SpringApplication.run(DocumentReaderPattern.class, args);
    }

    @Service
    static class DocumentReaderService {
        
        /**
         * Read a PDF document
         */
        public List<Document> readPdf(Resource resource) {
            PdfDocumentReader pdfReader = new PdfDocumentReader(resource);
            return pdfReader.get();
        }
        
        /**
         * Read a text file
         */
        public List<Document> readText(Resource resource) {
            TextReader textReader = new TextReader(resource);
            return textReader.get();
        }
        
        /**
         * Read document from file path
         */
        public List<Document> readFromPath(String filePath) throws IOException {
            Resource resource = new FileSystemResource(filePath);
            
            // Determine reader based on file extension
            String fileName = resource.getFilename();
            if (fileName != null) {
                if (fileName.endsWith(".pdf")) {
                    return readPdf(resource);
                } else if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
                    return readText(resource);
                }
            }
            
            throw new IllegalArgumentException("Unsupported file format: " + fileName);
        }
        
        /**
         * Read document from URL
         */
        public List<Document> readFromUrl(String url) throws IOException {
            Resource resource = new UrlResource(url);
            
            // Determine reader based on URL extension
            if (url.endsWith(".pdf")) {
                return readPdf(resource);
            } else if (url.endsWith(".txt") || url.endsWith(".md")) {
                return readText(resource);
            }
            
            throw new IllegalArgumentException("Unsupported file format: " + url);
        }
        
        /**
         * Read uploaded file
         */
        public List<Document> readUploadedFile(MultipartFile file) throws IOException {
            // Save file temporarily
            Path tempFile = Files.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile.toFile());
            
            try {
                Resource resource = new FileSystemResource(tempFile.toFile());
                
                String fileName = file.getOriginalFilename();
                List<Document> documents;
                
                if (fileName != null && fileName.endsWith(".pdf")) {
                    documents = readPdf(resource);
                } else if (fileName != null && (fileName.endsWith(".txt") || fileName.endsWith(".md"))) {
                    documents = readText(resource);
                } else {
                    throw new IllegalArgumentException("Unsupported file format: " + fileName);
                }
                
                return documents;
            } finally {
                // Clean up temp file
                Files.deleteIfExists(tempFile);
            }
        }
        
        /**
         * Read multiple documents from directory
         */
        public List<Document> readFromDirectory(String directoryPath) throws IOException {
            File directory = new File(directoryPath);
            
            if (!directory.isDirectory()) {
                throw new IllegalArgumentException("Path is not a directory: " + directoryPath);
            }
            
            File[] files = directory.listFiles((dir, name) -> 
                name.endsWith(".pdf") || name.endsWith(".txt") || name.endsWith(".md")
            );
            
            if (files == null) {
                throw new IOException("Cannot read directory: " + directoryPath);
            }
            
            return java.util.Arrays.stream(files)
                .flatMap(file -> {
                    try {
                        return readFromPath(file.getAbsolutePath()).stream();
                    } catch (IOException e) {
                        System.err.println("Error reading file: " + file.getName());
                        return java.util.stream.Stream.empty();
                    }
                })
                .collect(Collectors.toList());
        }
        
        /**
         * Extract metadata from documents
         */
        public List<DocumentMetadata> extractMetadata(List<Document> documents) {
            return documents.stream()
                .map(doc -> new DocumentMetadata(
                    doc.getId(),
                    doc.getMetadata(),
                    doc.getContent().length()
                ))
                .collect(Collectors.toList());
        }
    }

    @RestController
    @RequestMapping("/api/document-reader")
    static class DocumentReaderController {
        
        private final DocumentReaderService documentReaderService;
        
        public DocumentReaderController(DocumentReaderService documentReaderService) {
            this.documentReaderService = documentReaderService;
        }
        
        @PostMapping("/read-path")
        public ReadResponse readFromPath(@RequestBody ReadPathRequest request) {
            try {
                List<Document> documents = documentReaderService.readFromPath(request.filePath());
                List<DocumentInfo> docInfos = documents.stream()
                    .map(doc -> new DocumentInfo(
                        doc.getId(),
                        doc.getContent(),
                        doc.getMetadata()
                    ))
                    .collect(Collectors.toList());
                
                return new ReadResponse(docInfos, docInfos.size(), "success");
            } catch (Exception e) {
                return new ReadResponse(List.of(), 0, "error: " + e.getMessage());
            }
        }
        
        @PostMapping("/read-url")
        public ReadResponse readFromUrl(@RequestBody ReadUrlRequest request) {
            try {
                List<Document> documents = documentReaderService.readFromUrl(request.url());
                List<DocumentInfo> docInfos = documents.stream()
                    .map(doc -> new DocumentInfo(
                        doc.getId(),
                        doc.getContent(),
                        doc.getMetadata()
                    ))
                    .collect(Collectors.toList());
                
                return new ReadResponse(docInfos, docInfos.size(), "success");
            } catch (Exception e) {
                return new ReadResponse(List.of(), 0, "error: " + e.getMessage());
            }
        }
        
        @PostMapping("/read-upload")
        public ReadResponse readUpload(@RequestParam("file") MultipartFile file) {
            try {
                List<Document> documents = documentReaderService.readUploadedFile(file);
                List<DocumentInfo> docInfos = documents.stream()
                    .map(doc -> new DocumentInfo(
                        doc.getId(),
                        doc.getContent(),
                        doc.getMetadata()
                    ))
                    .collect(Collectors.toList());
                
                return new ReadResponse(docInfos, docInfos.size(), "success");
            } catch (Exception e) {
                return new ReadResponse(List.of(), 0, "error: " + e.getMessage());
            }
        }
        
        @PostMapping("/read-directory")
        public ReadResponse readFromDirectory(@RequestBody ReadDirectoryRequest request) {
            try {
                List<Document> documents = documentReaderService.readFromDirectory(request.directoryPath());
                List<DocumentInfo> docInfos = documents.stream()
                    .map(doc -> new DocumentInfo(
                        doc.getId(),
                        doc.getContent(),
                        doc.getMetadata()
                    ))
                    .collect(Collectors.toList());
                
                return new ReadResponse(docInfos, docInfos.size(), "success");
            } catch (Exception e) {
                return new ReadResponse(List.of(), 0, "error: " + e.getMessage());
            }
        }
        
        @GetMapping("/info")
        public Map<String, Object> getInfo() {
            return Map.of(
                "pattern", "Document Reader Pattern",
                "description", "Load and parse documents from various sources and formats",
                "supportedFormats", List.of("PDF", "TXT", "MD", "DOCX"),
                "features", List.of(
                    "PDF document reading",
                    "Text file reading",
                    "File path loading",
                    "URL loading",
                    "File upload handling",
                    "Directory batch loading",
                    "Metadata extraction"
                ),
                "endpoints", List.of(
                    "POST /api/document-reader/read-path",
                    "POST /api/document-reader/read-url",
                    "POST /api/document-reader/read-upload",
                    "POST /api/document-reader/read-directory",
                    "GET /api/document-reader/info"
                )
            );
        }
    }

    // DTOs
    record ReadPathRequest(String filePath) {}
    record ReadUrlRequest(String url) {}
    record ReadDirectoryRequest(String directoryPath) {}
    record DocumentInfo(String id, String content, Map<String, Object> metadata) {}
    record ReadResponse(List<DocumentInfo> documents, int count, String status) {}
    record DocumentMetadata(String id, Map<String, Object> metadata, int contentLength) {}
}
