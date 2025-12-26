package com.example.grpc.service;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

/**
 * gRPC Service Pattern
 * 
 * Demonstrates creating a gRPC service with Spring Boot.
 * Shows service definition, server setup, and unary RPC implementation.
 * 
 * Key Concepts:
 * - Service Implementation
 * - Server Configuration  
 * - Unary RPC
 * - Proto definition
 * 
 * Dependencies:
 * - grpc-spring-boot-starter
 * - grpc-stub
 * - grpc-protobuf
 * - protobuf-java
 * 
 * Proto definition (book.proto):
 * service BookService {
 *   rpc GetBook(GetBookRequest) returns (Book Response);
 *   rpc ListBooks(ListBooksRequest) returns (ListBooksResponse);
 *   rpc CreateBook(CreateBookRequest) returns (BookResponse);
 * }
 */
@SpringBootApplication
public class GRPCServicePattern {

    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(GRPCServicePattern.class, args);
        
        // Start gRPC server
        Server server = ServerBuilder
            .forPort(9090)
            .addService(new BookServiceImpl())
            .build()
            .start();
        
        System.out.println("gRPC Server started on port 9090");
        server.awaitTermination();
    }

    /**
     * gRPC Service Implementation
     * Implements the BookService defined in proto file
     */
    public static class BookServiceImpl extends BookServiceGrpc.BookServiceImplBase {
        
        @Override
        public void getBook(GetBookRequest request, StreamObserver<BookResponse> responseObserver) {
            // Unary RPC: single request, single response
            String bookId = request.getId();
            
            Book book = Book.newBuilder()
                .setId(bookId)
                .setTitle("Sample Book")
                .setAuthor("John Doe")
                .setIsbn("978-1234567890")
                .build();
            
            BookResponse response = BookResponse.newBuilder()
                .setBook(book)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        
        @Override
        public void listBooks(ListBooksRequest request, StreamObserver<ListBooksResponse> responseObserver) {
            // Return list of books
            ListBooksResponse.Builder responseBuilder = ListBooksResponse.newBuilder();
            
            for (int i = 1; i <= 10; i++) {
                Book book = Book.newBuilder()
                    .setId(String.valueOf(i))
                    .setTitle("Book " + i)
                    .setAuthor("Author " + i)
                    .build();
                responseBuilder.addBooks(book);
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        }
        
        @Override
        public void createBook(CreateBookRequest request, StreamObserver<BookResponse> responseObserver) {
            // Create new book
            Book book = Book.newBuilder()
                .setId(java.util.UUID.randomUUID().toString())
                .setTitle(request.getTitle())
                .setAuthor(request.getAuthor())
                .setIsbn(request.getIsbn())
                .build();
            
            BookResponse response = BookResponse.newBuilder()
                .setBook(book)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    // Placeholder classes (would be generated from proto files)
    static class BookServiceGrpc {
        static class BookServiceImplBase {}
    }
    static class Book {
        static Builder newBuilder() { return new Builder(); }
        static class Builder {
            Builder setId(String id) { return this; }
            Builder setTitle(String title) { return this; }
            Builder setAuthor(String author) { return this; }
            Builder setIsbn(String isbn) { return this; }
            Book build() { return new Book(); }
        }
    }
    static class GetBookRequest {
        String getId() { return "1"; }
    }
    static class BookResponse {
        static Builder newBuilder() { return new Builder(); }
        static class Builder {
            Builder setBook(Book book) { return this; }
            BookResponse build() { return new BookResponse(); }
        }
    }
    static class ListBooksRequest {}
    static class ListBooksResponse {
        static Builder newBuilder() { return new Builder(); }
        static class Builder {
            Builder addBooks(Book book) { return this; }
            ListBooksResponse build() { return new ListBooksResponse(); }
        }
    }
    static class CreateBookRequest {
        String getTitle() { return ""; }
        String getAuthor() { return ""; }
        String getIsbn() { return ""; }
    }
}
