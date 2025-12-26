package com.example.grpc.unary;

import io.grpc.stub.StreamObserver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Unary RPC Pattern (gRPC)
 * 
 * Single request, single response - most common gRPC pattern.
 * 
 * Proto definition:
 * rpc GetBook(GetBookRequest) returns (BookResponse);
 */
@SpringBootApplication
public class UnaryRPCPattern {

    public static void main(String[] args) {
        SpringApplication.run(UnaryRPCPattern.class, args);
    }

    public static class UnaryBookServiceImpl {
        
        public void getBook(GetBookRequest request, StreamObserver<BookResponse> responseObserver) {
            // Single request-response
            Book book = new Book(
                request.id(),
                "Spring Boot Guide",
                "John Doe"
            );
            
            BookResponse response = new BookResponse(book, "SUCCESS");
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    record GetBookRequest(String id) {}
    record Book(String id, String title, String author) {}
    record BookResponse(Book book, String status) {}
}
