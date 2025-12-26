package com.example.cloudstream;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Channel Pattern
 * ===============
 * 
 * Demonstrates Message Channels in Spring Cloud Stream, which are the
 * core abstraction for message passing between producers and consumers.
 * Channels decouple message senders from receivers.
 * 
 * Key Concepts:
 * ------------
 * 1. MessageChannel - Base interface for all channels
 * 2. SubscribableChannel - Supports message handlers
 * 3. PollableChannel - Supports polling for messages
 * 4. Channel Types - Direct, Publish-Subscribe, Queue, Executor
 * 5. Channel Interceptors - Pre/post message processing
 * 
 * How It Works:
 * ------------
 * - Channels are Spring Integration message channels
 * - Producers send messages to channels
 * - Consumers receive messages from channels
 * - Binders connect channels to external systems
 * - Supports both point-to-point and publish-subscribe
 * 
 * Channel Types:
 * -------------
 * - DirectChannel - Point-to-point, single thread
 * - PublishSubscribeChannel - Broadcast to multiple subscribers
 * - QueueChannel - Buffered, pollable
 * - ExecutorChannel - Async execution
 * - FluxMessageChannel - Reactive
 * 
 * Naming Convention:
 * -----------------
 * Functional model:
 * - <functionName>-in-<index> (input channel)
 * - <functionName>-out-<index> (output channel)
 * 
 * Legacy model:
 * - Custom names via @Input/@Output
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Direct Channel (Default)
 */
@Configuration
class DirectChannelConfiguration {
    
    /**
     * Direct channel for point-to-point communication
     * 
     * Characteristics:
     * - Single subscriber receives message
     * - Synchronous execution
     * - Sender thread executes handler
     * - Load balancing if multiple subscribers
     */
    @Bean
    public MessageChannel orderChannel() {
        System.out.println("Creating Direct Channel: orderChannel");
        System.out.println("  Type: Point-to-point");
        System.out.println("  Execution: Synchronous");
        System.out.println("  Thread: Sender's thread");
        return new DirectChannel();
    }
}

/**
 * Example 2: Publish-Subscribe Channel
 */
@Configuration
class PublishSubscribeChannelConfiguration {
    
    /**
     * Publish-Subscribe channel for broadcasting
     * 
     * Characteristics:
     * - Multiple subscribers receive same message
     * - All handlers invoked
     * - Synchronous by default
     * - Fan-out pattern
     */
    @Bean
    public SubscribableChannel eventChannel() {
        System.out.println("Creating Publish-Subscribe Channel: eventChannel");
        System.out.println("  Type: Broadcast");
        System.out.println("  Subscribers: Multiple");
        System.out.println("  Pattern: Fan-out");
        return new PublishSubscribeChannel();
    }
}

/**
 * Example 3: Queue Channel
 */
@Configuration
class QueueChannelConfiguration {
    
    /**
     * Queue channel for buffered messaging
     * 
     * Characteristics:
     * - Buffered messages (capacity limit)
     * - Pollable (consumers pull messages)
     * - Decouples sender from receiver
     * - Supports backpressure
     */
    @Bean
    public QueueChannel taskChannel() {
        System.out.println("Creating Queue Channel: taskChannel");
        System.out.println("  Type: Pollable");
        System.out.println("  Capacity: 100 messages");
        System.out.println("  Backpressure: Supported");
        return new QueueChannel(100);
    }
}

/**
 * Example 4: Executor Channel (Async)
 */
@Configuration
class ExecutorChannelConfiguration {
    
    /**
     * Executor channel for asynchronous processing
     * 
     * Characteristics:
     * - Async message handling
     * - Uses thread pool
     * - Non-blocking send
     * - Parallel processing
     */
    @Bean
    public MessageChannel asyncChannel() {
        System.out.println("Creating Executor Channel: asyncChannel");
        System.out.println("  Type: Asynchronous");
        System.out.println("  Executor: Thread pool");
        System.out.println("  Blocking: No");
        
        java.util.concurrent.Executor executor = 
            java.util.concurrent.Executors.newFixedThreadPool(10);
        return new ExecutorChannel(executor);
    }
}

/**
 * Example 5: Input Channel (Functional)
 */
@Configuration
class InputChannelConfiguration {
    
    /**
     * Input channel created automatically for function
     * 
     * Function: processMessage
     * Input channel: processMessage-in-0
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       bindings:
     *         processMessage-in-0:
     *           destination: input-topic
     */
    @Bean
    public java.util.function.Consumer<String> processMessage() {
        return message -> {
            System.out.println("Processing message on input channel");
            System.out.println("  Channel: processMessage-in-0");
            System.out.println("  Message: " + message);
        };
    }
}

/**
 * Example 6: Output Channel (Functional)
 */
@Configuration
class OutputChannelConfiguration {
    
    /**
     * Output channel created automatically for supplier
     * 
     * Supplier: generateData
     * Output channel: generateData-out-0
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       bindings:
     *         generateData-out-0:
     *           destination: output-topic
     */
    @Bean
    public java.util.function.Supplier<String> generateData() {
        return () -> {
            String data = "Data-" + System.currentTimeMillis();
            System.out.println("Generating data on output channel");
            System.out.println("  Channel: generateData-out-0");
            System.out.println("  Data: " + data);
            return data;
        };
    }
}

/**
 * Example 7: Bidirectional Channel (Function)
 */
@Configuration
class BidirectionalChannelConfiguration {
    
    /**
     * Both input and output channels for function
     * 
     * Function: transform
     * Input channel: transform-in-0
     * Output channel: transform-out-0
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       bindings:
     *         transform-in-0:
     *           destination: raw-data
     *         transform-out-0:
     *           destination: transformed-data
     */
    @Bean
    public java.util.function.Function<String, String> transform() {
        return input -> {
            System.out.println("Transform using bidirectional channels");
            System.out.println("  Input channel: transform-in-0");
            System.out.println("  Output channel: transform-out-0");
            System.out.println("  Input: " + input);
            return input.toUpperCase();
        };
    }
}

/**
 * Example 8: Multiple Input Channels
 */
@Configuration
class MultipleInputChannelsConfiguration {
    
    /**
     * Function with multiple input channels
     * 
     * Channels created:
     * - merge-in-0 (first input)
     * - merge-in-1 (second input)
     * - merge-out-0 (output)
     */
    @Bean
    public java.util.function.BiFunction<String, String, String> merge() {
        return (input1, input2) -> {
            System.out.println("Using multiple input channels");
            System.out.println("  Channel 1: merge-in-0 = " + input1);
            System.out.println("  Channel 2: merge-in-1 = " + input2);
            return input1 + "-" + input2;
        };
    }
}

/**
 * Example 9: Channel Interceptor
 */
@Configuration
class ChannelInterceptorConfiguration {
    
    /**
     * Add interceptor to channel for cross-cutting concerns
     * 
     * Interceptors can:
     * - Log messages
     * - Add headers
     * - Transform messages
     * - Implement security
     * - Track metrics
     */
    @Bean
    public MessageChannel interceptedChannel() {
        DirectChannel channel = new DirectChannel();
        
        channel.addInterceptor(new org.springframework.messaging.support.ChannelInterceptor() {
            @Override
            public org.springframework.messaging.Message<?> preSend(
                    org.springframework.messaging.Message<?> message,
                    MessageChannel channel) {
                System.out.println("Channel Interceptor - Pre-send");
                System.out.println("  Message: " + message.getPayload());
                System.out.println("  Can modify message or headers");
                return message;
            }
            
            @Override
            public void afterSendCompletion(
                    org.springframework.messaging.Message<?> message,
                    MessageChannel channel,
                    boolean sent,
                    Exception ex) {
                System.out.println("Channel Interceptor - After-send");
                System.out.println("  Sent: " + sent);
                if (ex != null) {
                    System.out.println("  Error: " + ex.getMessage());
                }
            }
        });
        
        System.out.println("Created intercepted channel");
        return channel;
    }
}

/**
 * Example 10: Error Channel
 */
@Configuration
class ErrorChannelConfiguration {
    
    /**
     * Error channel for handling failures
     * 
     * Default error channel: errorChannel
     * Custom error channels per binding
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     stream:
     *       bindings:
     *         process-in-0:
     *           destination: data
     *           consumer:
     *             maxAttempts: 1
     *         process-in-0.errors:
     *           destination: errors
     */
    @Bean
    public java.util.function.Consumer<String> handleError() {
        return error -> {
            System.out.println("Handling error on error channel");
            System.out.println("  Error: " + error);
            System.out.println("  Can route to DLQ or retry");
        };
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class ChannelPattern {
    
    /**
     * Example: Channel overview
     */
    @Bean
    public String channelInfo() {
        System.out.println("Spring Cloud Stream Channel Pattern");
        System.out.println("===================================");
        System.out.println("  Purpose: Message passing abstraction");
        System.out.println("  Types: Direct, Pub-Sub, Queue, Executor");
        System.out.println("  Integration: Spring Integration channels");
        return "Channel Info";
    }
}

/**
 * Usage Examples and Best Practices
 */
class ChannelUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Channel Pattern - Spring Cloud Stream");
        System.out.println("=====================================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Decouple message senders from receivers");
        System.out.println("- Abstract message passing");
        System.out.println("- Support multiple messaging patterns\n");
        
        System.out.println("Channel Types:");
        System.out.println("1. DirectChannel - Point-to-point, sync");
        System.out.println("2. PublishSubscribeChannel - Broadcast");
        System.out.println("3. QueueChannel - Buffered, pollable");
        System.out.println("4. ExecutorChannel - Async with thread pool");
        System.out.println("5. FluxMessageChannel - Reactive (Project Reactor)\n");
        
        System.out.println("Naming Convention (Functional):");
        System.out.println("- <functionName>-in-<index> - Input");
        System.out.println("- <functionName>-out-<index> - Output");
        System.out.println("- Index starts at 0\n");
        
        System.out.println("Channel Features:");
        System.out.println("- Interceptors - Pre/post processing");
        System.out.println("- Error handling - errorChannel");
        System.out.println("- Metrics - Message throughput");
        System.out.println("- Datatype channels - Type-safe");
        System.out.println("- Wire tap - Monitor messages\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Use functional model (auto channels)");
        System.out.println("- Add interceptors for cross-cutting concerns");
        System.out.println("- Configure error channels");
        System.out.println("- Use ExecutorChannel for async");
        System.out.println("- Monitor channel metrics");
        System.out.println("- Document channel contracts");
        System.out.println("- Use descriptive function names\n");
        
        System.out.println("Example:");
        System.out.println("@Bean");
        System.out.println("public Function<Order, Receipt> processOrder() {");
        System.out.println("  return order -> {");
        System.out.println("    // Input: processOrder-in-0");
        System.out.println("    // Output: processOrder-out-0");
        System.out.println("    return new Receipt(order);");
        System.out.println("  };");
        System.out.println("}");
    }
}
