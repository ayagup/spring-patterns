package com.example.grpc.clientstreaming;

import io.grpc.stub.StreamObserver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Client Streaming Pattern (gRPC)
 * 
 * Client sends a stream of messages and receives a single response.
 * 
 * Proto definition:
 * rpc UploadBooks(stream Book) returns (UploadResponse);
 */
@SpringBootApplication
public class ClientStreamingPattern {

    public static void main(String[] args) {
        SpringApplication.run(ClientStreamingPattern.class, args);
    }

    public static class BookUploadServiceImpl {
        
        public StreamObserver<Book> uploadBooks(StreamObserver<UploadResponse> responseObserver) {
            return new StreamObserver<Book>() {
                private final List<Book> books = new ArrayList<>();

                @Override
                public void onNext(Book book) {
                    books.add(book);
                    System.out.println("Received book: " + book.title());
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    UploadResponse response = new UploadResponse(
                        books.size(),
                        "Successfully uploaded " + books.size() + " books"
                    );
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }
            };
        }
    }

    record Book(String id, String title) {}
    record UploadResponse(int count, String message) {}
}
