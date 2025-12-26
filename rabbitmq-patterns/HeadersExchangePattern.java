package com.example.rabbitmq.patterns;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;

/**
 * Headers Exchange Pattern
 * 
 * Demonstrates the Headers Exchange routing mechanism where messages are
 * routed based on message header attributes rather than routing keys.
 * Supports matching all or any header criteria.
 * 
 * Key Features:
 * - Header-based routing
 * - Match all headers (x-match: all)
 * - Match any header (x-match: any)
 * - Multiple header criteria
 * - Complex routing logic
 */
@SpringBootApplication
public class HeadersExchangePattern implements CommandLineRunner {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String HEADERS_EXCHANGE = "headers-exchange-demo";

    public static void main(String[] args) {
        SpringApplication.run(HeadersExchangePattern.class, args);
    }

    /**
     * Headers exchange configuration
     */
    @Bean
    public HeadersExchange documentExchange() {
        return ExchangeBuilder
                .headersExchange(HEADERS_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * Queues for different document processing
     */
    @Bean
    public Queue pdfReportQueue() {
        return new Queue("headers-pdf-report", true);
    }

    @Bean
    public Queue urgentDocQueue() {
        return new Queue("headers-urgent-doc", true);
    }

    @Bean
    public Queue largeFileQueue() {
        return new Queue("headers-large-file", true);
    }

    @Bean
    public Queue archiveQueue() {
        return new Queue("headers-archive", true);
    }

    /**
     * Binding with "all" match - must match ALL headers
     */
    @Bean
    public Binding pdfReportBinding() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("format", "pdf");
        headers.put("type", "report");
        
        return BindingBuilder
                .bind(pdfReportQueue())
                .to(documentExchange())
                .whereAll(headers)
                .match();
    }

    /**
     * Binding with "any" match - must match ANY header
     */
    @Bean
    public Binding urgentDocBinding() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("priority", "urgent");
        headers.put("priority", "critical");
        
        return BindingBuilder
                .bind(urgentDocQueue())
                .to(documentExchange())
                .whereAny(headers)
                .match();
    }

    /**
     * Binding with specific header value
     */
    @Bean
    public Binding largeFileBinding() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("size", "large");
        
        return BindingBuilder
                .bind(largeFileQueue())
                .to(documentExchange())
                .whereAll(headers)
                .match();
    }

    /**
     * Binding with multiple criteria
     */
    @Bean
    public Binding archiveBinding() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("archive", "true");
        headers.put("retention", "long-term");
        
        return BindingBuilder
                .bind(archiveQueue())
                .to(documentExchange())
                .whereAll(headers)
                .match();
    }

    @Override
    public void run(String... args) throws InterruptedException {
        demonstrateHeadersExchange();
        Thread.sleep(2000);
    }

    private void demonstrateHeadersExchange() {
        System.out.println("=== Headers Exchange Pattern ===\n");

        System.out.println("Binding Rules:");
        System.out.println("1. pdf-report: format=pdf AND type=report (match ALL)");
        System.out.println("2. urgent-doc: priority=urgent OR priority=critical (match ANY)");
        System.out.println("3. large-file: size=large (match ALL)");
        System.out.println("4. archive: archive=true AND retention=long-term (match ALL)\n");

        // Test 1: PDF Report - matches binding 1
        System.out.println("1. Sending PDF Report:");
        sendDocument("Monthly Report", headers -> {
            headers.put("format", "pdf");
            headers.put("type", "report");
        });
        System.out.println("   Routes to: pdf-report queue\n");

        // Test 2: Urgent document - matches binding 2
        System.out.println("2. Sending Urgent Document:");
        sendDocument("Urgent Memo", headers -> {
            headers.put("priority", "urgent");
            headers.put("format", "docx");
        });
        System.out.println("   Routes to: urgent-doc queue\n");

        // Test 3: Large file - matches binding 3
        System.out.println("3. Sending Large File:");
        sendDocument("Video File", headers -> {
            headers.put("size", "large");
            headers.put("format", "mp4");
        });
        System.out.println("   Routes to: large-file queue\n");

        // Test 4: Archive document - matches binding 4
        System.out.println("4. Sending Archive Document:");
        sendDocument("Legal Contract", headers -> {
            headers.put("archive", "true");
            headers.put("retention", "long-term");
        });
        System.out.println("   Routes to: archive queue\n");

        // Test 5: Multiple matches
        System.out.println("5. Sending PDF Report (Large & Urgent):");
        sendDocument("Annual Report", headers -> {
            headers.put("format", "pdf");
            headers.put("type", "report");
            headers.put("priority", "urgent");
            headers.put("size", "large");
        });
        System.out.println("   Routes to: pdf-report, urgent-doc, large-file queues\n");

        // Test 6: No match
        System.out.println("6. Sending Regular Document:");
        sendDocument("Regular File", headers -> {
            headers.put("format", "txt");
            headers.put("type", "note");
        });
        System.out.println("   Routes to: NONE (no matching bindings)\n");

        System.out.println("Headers Exchange Characteristics:");
        System.out.println("- Routes based on message headers");
        System.out.println("- Ignores routing key completely");
        System.out.println("- Supports 'all' and 'any' matching");
        System.out.println("- Enables complex routing logic");
        System.out.println("- Ideal for content-based routing");
    }

    private void sendDocument(String name, java.util.function.Consumer<Map<String, Object>> headerBuilder) {
        Document doc = new Document(name, System.currentTimeMillis());
        
        rabbitTemplate.convertAndSend(HEADERS_EXCHANGE, "", doc, message -> {
            Map<String, Object> headers = new HashMap<>();
            headerBuilder.accept(headers);
            
            MessageProperties props = message.getMessageProperties();
            headers.forEach(props::setHeader);
            
            System.out.println("   Headers: " + headers);
            return message;
        });
    }

    /**
     * Document listeners
     */
    @Component
    static class DocumentListeners {

        @RabbitListener(queues = "headers-pdf-report")
        public void handlePdfReport(Document doc,
                @org.springframework.messaging.handler.annotation.Headers Map<String, Object> headers) {
            System.out.println("  [PDF Report Processor] " + doc.getName());
            System.out.println("    Headers: " + headers);
        }

        @RabbitListener(queues = "headers-urgent-doc")
        public void handleUrgentDoc(Document doc,
                @org.springframework.messaging.handler.annotation.Headers Map<String, Object> headers) {
            System.out.println("  [Urgent Handler] " + doc.getName());
            System.out.println("    Headers: " + headers);
        }

        @RabbitListener(queues = "headers-large-file")
        public void handleLargeFile(Document doc,
                @org.springframework.messaging.handler.annotation.Headers Map<String, Object> headers) {
            System.out.println("  [Large File Handler] " + doc.getName());
            System.out.println("    Headers: " + headers);
        }

        @RabbitListener(queues = "headers-archive")
        public void handleArchive(Document doc,
                @org.springframework.messaging.handler.annotation.Headers Map<String, Object> headers) {
            System.out.println("  [Archive Service] " + doc.getName());
            System.out.println("    Headers: " + headers);
        }
    }

    /**
     * Document class
     */
    static class Document implements java.io.Serializable {
        private String name;
        private long timestamp;

        public Document() {}

        public Document(String name, long timestamp) {
            this.name = name;
            this.timestamp = timestamp;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
