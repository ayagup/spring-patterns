package com.example.cloudstream;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Configuration;

/**
 * Enable Binding Pattern
 * ======================
 * 
 * Demonstrates @EnableBinding annotation that enables Spring Cloud Stream
 * binding infrastructure and declares the interfaces that define input
 * and output channels.
 * 
 * Key Concepts:
 * ------------
 * 1. @EnableBinding - Activate binding infrastructure
 * 2. Interface Declaration - Define channels
 * 3. Auto-Configuration - Automatic channel setup
 * 4. Bean Creation - Channels as Spring beans
 * 5. Multiple Interfaces - Combine multiple bindings
 * 
 * How It Works:
 * ------------
 * - Scans for @Input and @Output annotations
 * - Creates MessageChannel beans
 * - Configures channel bindings
 * - Connects to middleware via binder
 * - Enables dependency injection of channels
 * 
 * Usage Patterns:
 * --------------
 * @EnableBinding(Source.class) - Producer
 * @EnableBinding(Sink.class) - Consumer
 * @EnableBinding(Processor.class) - Transformer
 * @EnableBinding({Source.class, Sink.class}) - Multiple
 * @EnableBinding(CustomChannels.class) - Custom
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Enable Source Binding (Producer)
 */
@Configuration
@EnableBinding(Source.class)
class EnableSourceBindingExample {
    
    /**
     * Enable Source interface
     * 
     * Provides:
     * - output MessageChannel
     * 
     * Usage:
     * @Autowired
     * private Source source;
     * source.output().send(message);
     */
    
    public void demonstrateEnableSource() {
        System.out.println("Enable Source Binding");
        System.out.println("  Interface: Source.class");
        System.out.println("  Channels: output");
        System.out.println("  Type: Producer only");
        System.out.println("  Bean: Source instance injectable");
    }
}

/**
 * Example 2: Enable Sink Binding (Consumer)
 */
@Configuration
@EnableBinding(Sink.class)
class EnableSinkBindingExample {
    
    /**
     * Enable Sink interface
     * 
     * Provides:
     * - input SubscribableChannel
     * 
     * Usage:
     * @StreamListener(Sink.INPUT)
     * public void handle(String message) { }
     */
    
    public void demonstrateEnableSink() {
        System.out.println("Enable Sink Binding");
        System.out.println("  Interface: Sink.class");
        System.out.println("  Channels: input");
        System.out.println("  Type: Consumer only");
        System.out.println("  Bean: Sink instance injectable");
    }
}

/**
 * Example 3: Enable Processor Binding (Transformer)
 */
@Configuration
@EnableBinding(Processor.class)
class EnableProcessorBindingExample {
    
    /**
     * Enable Processor interface
     * 
     * Provides:
     * - input SubscribableChannel
     * - output MessageChannel
     * 
     * Usage:
     * @StreamListener(Processor.INPUT)
     * @SendTo(Processor.OUTPUT)
     * public String transform(String input) { }
     */
    
    public void demonstrateEnableProcessor() {
        System.out.println("Enable Processor Binding");
        System.out.println("  Interface: Processor.class");
        System.out.println("  Channels: input, output");
        System.out.println("  Type: Transformer");
        System.out.println("  Bean: Processor instance injectable");
    }
}

/**
 * Example 4: Enable Multiple Bindings
 */
@Configuration
@EnableBinding({Source.class, Sink.class})
class EnableMultipleBindingsExample {
    
    /**
     * Enable multiple interfaces
     * 
     * Provides:
     * - Source with output
     * - Sink with input
     * 
     * Use case:
     * - Independent input/output
     * - Different topics
     * - Separate processing
     */
    
    public void demonstrateEnableMultiple() {
        System.out.println("Enable Multiple Bindings");
        System.out.println("  Interfaces: Source.class, Sink.class");
        System.out.println("  Input: From Sink");
        System.out.println("  Output: From Source");
        System.out.println("  Use case: Independent channels");
    }
}

/**
 * Example 5: Enable Custom Binding Interface
 */
interface OrderChannels {
    String ORDER_INPUT = "orderInput";
    String ORDER_OUTPUT = "orderOutput";
    String NOTIFICATION_OUTPUT = "notificationOutput";
    
    @org.springframework.cloud.stream.annotation.Input(ORDER_INPUT)
    org.springframework.messaging.SubscribableChannel orderInput();
    
    @org.springframework.cloud.stream.annotation.Output(ORDER_OUTPUT)
    org.springframework.messaging.MessageChannel orderOutput();
    
    @org.springframework.cloud.stream.annotation.Output(NOTIFICATION_OUTPUT)
    org.springframework.messaging.MessageChannel notificationOutput();
}

@Configuration
@EnableBinding(OrderChannels.class)
class EnableCustomBindingExample {
    
    /**
     * Enable custom interface
     * 
     * Provides:
     * - orderInput channel
     * - orderOutput channel
     * - notificationOutput channel
     * 
     * Configuration:
     * spring.cloud.stream.bindings:
     *   orderInput.destination: orders
     *   orderOutput.destination: processed-orders
     *   notificationOutput.destination: notifications
     */
    
    public void demonstrateEnableCustom() {
        System.out.println("Enable Custom Binding");
        System.out.println("  Interface: OrderChannels.class");
        System.out.println("  Channels: orderInput, orderOutput, notificationOutput");
        System.out.println("  Type: Custom multi-channel");
    }
}

/**
 * Example 6: Enable Binding with Auto-Configuration
 */
@Configuration
@EnableBinding(Processor.class)
class AutoConfigurationBindingExample {
    
    /**
     * @EnableBinding triggers auto-configuration
     * 
     * Auto-configured:
     * - Binder selection
     * - Channel creation
     * - Message converters
     * - Error handlers
     * - Health indicators
     */
    
    public void demonstrateAutoConfiguration() {
        System.out.println("Auto-Configuration with @EnableBinding");
        System.out.println("  Binder: Auto-detected (Kafka/RabbitMQ)");
        System.out.println("  Channels: Auto-created");
        System.out.println("  Converters: Auto-registered");
        System.out.println("  Health: Auto-enabled");
    }
}

/**
 * Example 7: Enable Binding with Dependency Injection
 */
@Configuration
@EnableBinding(Processor.class)
class DependencyInjectionBindingExample {
    
    /**
     * Inject channels into beans
     * 
     * Usage:
     * @Autowired
     * private Processor processor;
     * 
     * @Autowired
     * @Qualifier(Processor.INPUT)
     * private SubscribableChannel input;
     * 
     * @Autowired
     * @Qualifier(Processor.OUTPUT)
     * private MessageChannel output;
     */
    
    public void demonstrateDependencyInjection() {
        System.out.println("Dependency Injection with @EnableBinding");
        System.out.println("  Inject: Processor interface");
        System.out.println("  Inject: Individual channels");
        System.out.println("  Qualifier: Channel names");
    }
}

/**
 * Example 8: Enable Binding with Functional Style
 */
@Configuration
@EnableBinding(Source.class)
class FunctionalStyleBindingExample {
    
    /**
     * Combine with functional endpoints
     * 
     * Note: Spring Cloud Stream 3.x+ prefers
     * functional programming model over @EnableBinding
     * 
     * Legacy:
     * @EnableBinding + @StreamListener
     * 
     * Modern:
     * @Bean Function<String, String>
     */
    
    public void demonstrateFunctionalStyle() {
        System.out.println("Functional Style (Legacy @EnableBinding)");
        System.out.println("  Legacy: @EnableBinding + @StreamListener");
        System.out.println("  Modern: @Bean Function<T, R>");
        System.out.println("  Migration: Gradual transition supported");
    }
}

/**
 * Example 9: Enable Binding with Testing
 */
@Configuration
@EnableBinding(Processor.class)
class TestingBindingExample {
    
    /**
     * Testing with @EnableBinding
     * 
     * Test annotation:
     * @SpringBootTest
     * @AutoConfigureMessageVerifier
     * 
     * Usage:
     * @Autowired
     * private InputDestination input;
     * @Autowired
     * private OutputDestination output;
     */
    
    public void demonstrateTesting() {
        System.out.println("Testing with @EnableBinding");
        System.out.println("  Test channels: InputDestination, OutputDestination");
        System.out.println("  Verification: Message assertions");
        System.out.println("  No broker: In-memory channels");
    }
}

/**
 * Example 10: Enable Binding Migration Path
 */
@Configuration
@EnableBinding(Processor.class)
class MigrationPathExample {
    
    /**
     * Migration from @EnableBinding to functional
     * 
     * Before (Spring Cloud Stream 2.x):
     * @EnableBinding(Processor.class)
     * @StreamListener(Processor.INPUT)
     * @SendTo(Processor.OUTPUT)
     * public String transform(String input) { }
     * 
     * After (Spring Cloud Stream 3.x+):
     * @Bean
     * public Function<String, String> transform() {
     *   return input -> input.toUpperCase();
     * }
     */
    
    public void demonstrateMigration() {
        System.out.println("Migration Path");
        System.out.println("  From: @EnableBinding + @StreamListener");
        System.out.println("  To: @Bean Function<T, R>");
        System.out.println("  Benefits: Simpler, testable, less annotations");
        System.out.println("  Support: Both models work in 3.x");
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class EnableBindingPattern {
    
    /**
     * Core @EnableBinding concepts
     */
    public void demonstrateEnableBindingPattern() {
        System.out.println("\n=== @EnableBinding Pattern ===");
        System.out.println("Activate Spring Cloud Stream binding infrastructure");
        System.out.println("\nCommon Interfaces:");
        System.out.println("  - Source: Producer");
        System.out.println("  - Sink: Consumer");
        System.out.println("  - Processor: Transformer");
        System.out.println("\nFeatures:");
        System.out.println("  - Auto-configuration");
        System.out.println("  - Dependency injection");
        System.out.println("  - Multiple interfaces");
        System.out.println("  - Custom channels");
    }
}

/**
 * Usage Examples and Best Practices
 */
class EnableBindingPatternUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("@EnableBinding Pattern");
        System.out.println("=======================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Enable Spring Cloud Stream");
        System.out.println("- Declare channel interfaces");
        System.out.println("- Auto-configure bindings\n");
        
        System.out.println("Usage:");
        System.out.println("@Configuration");
        System.out.println("@EnableBinding(Processor.class)");
        System.out.println("public class StreamConfig { }\n");
        
        System.out.println("Built-in Interfaces:");
        System.out.println("- Source.class - Output only");
        System.out.println("- Sink.class - Input only");
        System.out.println("- Processor.class - Input + Output\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Use built-in interfaces when possible");
        System.out.println("- Create custom interfaces for complex scenarios");
        System.out.println("- Configure bindings in application.yml");
        System.out.println("- Inject channels via @Autowired");
        System.out.println("- Consider migrating to functional model (3.x+)");
        System.out.println("- Test with InputDestination/OutputDestination");
    }
}
