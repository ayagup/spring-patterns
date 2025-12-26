package com.example.grpc.streaming;

import io.grpc.stub.StreamObserver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bidirectional Streaming Pattern (gRPC)
 * 
 * Demonstrates bidirectional streaming where both client and server
 * send a stream of messages to each other.
 * 
 * Proto definition:
 * rpc Chat(stream ChatMessage) returns (stream ChatMessage);
 */
@SpringBootApplication
public class BidirectionalStreamingPattern {

    public static void main(String[] args) {
        SpringApplication.run(BidirectionalStreamingPattern.class, args);
    }

    public static class ChatServiceImpl {
        
        public StreamObserver<ChatMessage> chat(StreamObserver<ChatMessage> responseObserver) {
            return new StreamObserver<ChatMessage>() {
                @Override
                public void onNext(ChatMessage message) {
                    // Process incoming message and send response
                    ChatMessage response = new ChatMessage(
                        "Echo: " + message.content,
                        "Server"
                    );
                    responseObserver.onNext(response);
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }
    }

    record ChatMessage(String content, String sender) {}
}
