package com.example.grpc.protobuf;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Protocol Buffer Pattern
 * 
 * Demonstrates Protocol Buffers usage for efficient serialization.
 * 
 * Proto file example (book.proto):
 * 
 * syntax = "proto3";
 * 
 * package com.example.grpc;
 * 
 * message Book {
 *   string id = 1;
 *   string title = 2;
 *   string author = 3;
 *   repeated string tags = 4;
 *   int32 pages = 5;
 *   double price = 6;
 * }
 * 
 * message BookRequest {
 *   string id = 1;
 * }
 * 
 * message BookList {
 *   repeated Book books = 1;
 * }
 */
@SpringBootApplication
public class ProtocolBufferPattern {

    public static void main(String[] args) {
        SpringApplication.run(ProtocolBufferPattern.class, args);
        
        // Example usage (would use generated protobuf classes)
        System.out.println("Protocol Buffer Pattern - Efficient binary serialization");
        System.out.println("Advantages:");
        System.out.println("- Smaller message size");
        System.out.println("- Faster serialization/deserialization");
        System.out.println("- Strongly typed");
        System.out.println("- Language agnostic");
        System.out.println("- Backward/forward compatible");
    }
}
