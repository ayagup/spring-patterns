package com.example.grpc.serverstreaming;

import io.grpc.stub.StreamObserver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Server Streaming Pattern (gRPC)
 * 
 * Server sends a stream of responses for a single client request.
 * 
 * Proto definition:
 * rpc ListBooksStream(ListRequest) returns (stream Book);
 */
@SpringBootApplication
public class ServerStreamingPattern {

    public static void main(String[] args) {
        SpringApplication.run(ServerStreamingPattern.class, args);
    }

    public static class BookStreamServiceImpl {
        
        public void listBooksStream(ListRequest request, StreamObserver<Book> responseObserver) {
            // Stream multiple books to client
            for (int i = 1; i <= 100; i++) {
                Book book = new Book(String.valueOf(i), "Book " + i);
                responseObserver.onNext(book);
                
                try {
                    Thread.sleep(100); // Simulate delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            responseObserver.onCompleted();
        }
    }

    record Book(String id, String title) {}
    static class ListRequest {}
}
