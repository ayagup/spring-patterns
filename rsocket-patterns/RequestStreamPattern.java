package com.example.rsocket;

import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

/**
 * RSocket Request-Stream Pattern
 * 
 * Demonstrates the request-stream interaction model where:
 * - Client sends ONE request
 * - Server responds with a STREAM of multiple responses
 * - Useful for: streaming data, real-time updates, continuous queries
 * 
 * Use Cases:
 * - Live data feeds (stock prices, sensor data)
 * - Real-time monitoring and metrics
 * - Paginated results streaming
 * - Event streams and notifications
 * - Log tailing
 * 
 * Key Characteristics:
 * - 1:N interaction model
 * - Backpressure support
 * - Client controls flow with reactive streams
 * - Server streams data reactively
 * 
 * @author Spring Patterns
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
class StockPrice {
    private String symbol;
    private Double price;
    private Long timestamp;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class LogEntry {
    private String level;
    private String message;
    private Long timestamp;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class MetricData {
    private String name;
    private Double value;
    private String unit;
    private Long timestamp;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class StreamRequest {
    private String resource;
    private Integer limit;
}

/**
 * RSocket Server - Handles request-stream interactions
 */
@Controller
@Slf4j
class RequestStreamServerController {
    
    /**
     * Stream stock prices
     * Client sends stock symbol, server streams price updates
     */
    @MessageMapping("stock.stream")
    public Flux<StockPrice> streamStockPrices(String symbol) {
        log.info("Streaming stock prices for: {}", symbol);
        
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> {
                    double price = 100.0 + Math.random() * 50;
                    return new StockPrice(symbol, price, System.currentTimeMillis());
                })
                .take(10); // Stream 10 updates
    }
    
    /**
     * Stream log entries
     * Client requests logs, server streams log entries
     */
    @MessageMapping("logs.stream")
    public Flux<LogEntry> streamLogs(String application) {
        log.info("Streaming logs for application: {}", application);
        
        String[] levels = {"INFO", "WARN", "ERROR", "DEBUG"};
        
        return Flux.interval(Duration.ofMillis(500))
                .map(i -> {
                    String level = levels[(int) (i % levels.length)];
                    String message = String.format("%s - Log entry #%d", application, i);
                    return new LogEntry(level, message, System.currentTimeMillis());
                })
                .take(20);
    }
    
    /**
     * Stream metrics
     * Client requests metric name, server streams metric values
     */
    @MessageMapping("metrics.stream")
    public Flux<MetricData> streamMetrics(String metricName) {
        log.info("Streaming metrics: {}", metricName);
        
        return Flux.interval(Duration.ofSeconds(2))
                .map(i -> {
                    double value = Math.random() * 100;
                    return new MetricData(metricName, value, "%", System.currentTimeMillis());
                })
                .take(5);
    }
    
    /**
     * Stream paginated data
     * Client requests resource with limit, server streams pages
     */
    @MessageMapping("data.stream.paginated")
    public Flux<List<String>> streamPaginatedData(StreamRequest request) {
        log.info("Streaming paginated data: {}, limit: {}", request.getResource(), request.getLimit());
        
        return Flux.range(1, 5)
                .delayElements(Duration.ofMillis(300))
                .map(page -> {
                    return Stream.iterate(1, n -> n + 1)
                            .limit(request.getLimit())
                            .map(n -> String.format("%s-page%d-item%d", request.getResource(), page, n))
                            .toList();
                });
    }
}

/**
 * RSocket Client Service
 */
@Service
@Slf4j
class RequestStreamClientService {
    
    private final RSocketRequester requester;
    
    public RequestStreamClientService(RSocketRequester.Builder builder) {
        this.requester = builder
                .tcp("localhost", 7000);
    }
    
    /**
     * Request stream of stock prices
     */
    public Flux<StockPrice> requestStockStream(String symbol) {
        log.info("Requesting stock stream for: {}", symbol);
        
        return requester
                .route("stock.stream")
                .data(symbol)
                .retrieveFlux(StockPrice.class)
                .doOnNext(price -> log.info("Received stock price: {}", price))
                .doOnError(error -> log.error("Error in stock stream", error))
                .doOnComplete(() -> log.info("Stock stream completed"));
    }
    
    /**
     * Request stream of logs
     */
    public Flux<LogEntry> requestLogStream(String application) {
        log.info("Requesting log stream for: {}", application);
        
        return requester
                .route("logs.stream")
                .data(application)
                .retrieveFlux(LogEntry.class)
                .doOnNext(log -> this.log.info("Received log: {} - {}", log.getLevel(), log.getMessage()));
    }
    
    /**
     * Request stream of metrics
     */
    public Flux<MetricData> requestMetricStream(String metricName) {
        log.info("Requesting metric stream: {}", metricName);
        
        return requester
                .route("metrics.stream")
                .data(metricName)
                .retrieveFlux(MetricData.class)
                .doOnNext(metric -> log.info("Received metric: {} = {}{}", 
                        metric.getName(), metric.getValue(), metric.getUnit()));
    }
    
    /**
     * Request paginated data stream
     */
    public Flux<List<String>> requestPaginatedStream(String resource, Integer limit) {
        log.info("Requesting paginated stream: {}, limit: {}", resource, limit);
        
        StreamRequest request = new StreamRequest(resource, limit);
        
        return requester
                .route("data.stream.paginated")
                .data(request)
                .retrieveFlux(List.class)
                .doOnNext(page -> log.info("Received page with {} items", page.size()));
    }
}

/**
 * Request-Stream Pattern Information Service
 */
@Service
class RequestStreamInfoService {
    
    public String getPatternInfo() {
        return """
                RSocket Request-Stream Pattern
                ==============================
                
                Interaction Model:
                - Client sends: 1 request
                - Server sends: N responses (stream)
                - Communication: One-to-many
                - Flow Control: Reactive Streams backpressure
                
                Use Cases:
                1. Live Data Feeds
                   - Stock prices streaming
                   - Sensor data streaming
                   - Real-time updates
                
                2. Continuous Queries
                   - Database result streaming
                   - Search results streaming
                   - Paginated data streaming
                
                3. Event Streams
                   - Application events
                   - System notifications
                   - Log tailing
                
                4. Monitoring & Metrics
                   - Performance metrics
                   - Health status updates
                   - System telemetry
                
                Advantages:
                - Efficient for large datasets
                - Built-in backpressure
                - Lower latency than polling
                - Reduced network overhead
                - Memory efficient streaming
                
                Best Practices:
                1. Use Flux.interval() for time-based streaming
                2. Implement proper error handling
                3. Use take() or takeUntil() to limit stream
                4. Log stream lifecycle events
                5. Handle client cancellation gracefully
                6. Use delayElements() for controlled streaming
                7. Implement backpressure strategies
                8. Monitor stream performance
                """;
    }
    
    public List<String> getStreamingStrategies() {
        return List.of(
                "Time-Based: Stream data at regular intervals",
                "Event-Based: Stream data when events occur",
                "Pull-Based: Client requests each chunk",
                "Push-Based: Server pushes data continuously",
                "Hybrid: Combination of push and pull",
                "Buffered: Buffer data before streaming",
                "Windowed: Stream in windows/batches",
                "Conditional: Stream based on conditions"
        );
    }
    
    public List<String> getBackpressureStrategies() {
        return List.of(
                "Buffer: Buffer excess items",
                "Drop: Drop items when overwhelmed",
                "Latest: Keep only latest items",
                "Error: Error when overwhelmed",
                "Request: Request-based flow control"
        );
    }
}

/**
 * REST Controller for testing Request-Stream pattern
 */
@RestController
@RequestMapping("/rsocket/request-stream")
@Slf4j
class RequestStreamController {
    
    private final RequestStreamClientService clientService;
    private final RequestStreamInfoService infoService;
    
    public RequestStreamController(RequestStreamClientService clientService,
                                   RequestStreamInfoService infoService) {
        this.clientService = clientService;
        this.infoService = infoService;
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return infoService.getPatternInfo();
    }
    
    @GetMapping("/strategies/streaming")
    public List<String> getStreamingStrategies() {
        return infoService.getStreamingStrategies();
    }
    
    @GetMapping("/strategies/backpressure")
    public List<String> getBackpressureStrategies() {
        return infoService.getBackpressureStrategies();
    }
    
    @GetMapping("/test/stock/{symbol}")
    public Flux<StockPrice> testStockStream(@PathVariable String symbol) {
        log.info("Testing stock stream for: {}", symbol);
        return clientService.requestStockStream(symbol);
    }
    
    @GetMapping("/test/logs/{application}")
    public Flux<LogEntry> testLogStream(@PathVariable String application) {
        log.info("Testing log stream for: {}", application);
        return clientService.requestLogStream(application);
    }
    
    @GetMapping("/test/metrics/{metricName}")
    public Flux<MetricData> testMetricStream(@PathVariable String metricName) {
        log.info("Testing metric stream: {}", metricName);
        return clientService.requestMetricStream(metricName);
    }
    
    @GetMapping("/test/paginated/{resource}/{limit}")
    public Flux<List<String>> testPaginatedStream(@PathVariable String resource,
                                                   @PathVariable Integer limit) {
        log.info("Testing paginated stream: {}, limit: {}", resource, limit);
        return clientService.requestPaginatedStream(resource, limit);
    }
}

/**
 * Configuration for Request-Stream Pattern
 */
@Configuration
class RequestStreamConfig {
    
    @Bean
    public RSocketStrategies rSocketStrategies() {
        return RSocketStrategies.builder()
                .build();
    }
}

@SpringBootApplication
public class RequestStreamPattern {
    public static void main(String[] args) {
        SpringApplication.run(RequestStreamPattern.class, args);
    }
}
