package com.example.rsocket;

import io.rsocket.metadata.WellKnownMimeType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * RSocket Metadata Push Pattern
 * 
 * Demonstrates metadata-only communication:
 * - Sending metadata without payload
 * - Configuration updates
 * - Control messages
 * - Routing information
 * 
 * @author Spring Patterns
 */

@Data
class MetadataMessage {
    private String type;
    private String content;
}

@Controller
@Slf4j
class MetadataPushController {
    
    @MessageMapping("metadata.receive")
    public Mono<Void> receiveMetadata(String metadata) {
        log.info("Received metadata: {}", metadata);
        return Mono.empty();
    }
}

@Service
@Slf4j
class MetadataPushService {
    
    private final RSocketRequester requester;
    
    public MetadataPushService(RSocketRequester.Builder builder) {
        this.requester = builder.tcp("localhost", 7000);
    }
    
    public Mono<Void> pushMetadata(String metadata) {
        return requester
                .route("metadata.receive")
                .metadata(metadata, MimeTypeUtils.TEXT_PLAIN)
                .send();
    }
}

/**
 * RSocket Resume Pattern
 * 
 * Demonstrates session resumption:
 * - Reconnect after connection loss
 * - Resume from last position
 * - Maintain session state
 * 
 * @author Spring Patterns
 */

@Service
@Slf4j
class ResumeService {
    
    public RSocketRequester createResumableRequester(RSocketRequester.Builder builder) {
        return builder
                .rsocketConnector(connector -> connector
                        .resume()
                        .resumeSessionDuration(java.time.Duration.ofMinutes(5))
                        .resumeStreamTimeout(java.time.Duration.ofSeconds(10))
                )
                .tcp("localhost", 7000);
    }
    
    public String getResumeInfo() {
        return """
                Resume Strategy enables:
                - Automatic reconnection
                - Session state preservation
                - Stream continuation
                - Data loss prevention
                """;
    }
}

/**
 * RSocket Lease Pattern
 * 
 * Demonstrates lease-based flow control:
 * - Server grants leases to clients
 * - Rate limiting
 * - Load management
 * 
 * @author Spring Patterns
 */

@Service
@Slf4j
class LeaseService {
    
    public RSocketRequester createLeasedRequester(RSocketRequester.Builder builder) {
        return builder
                .rsocketConnector(connector -> connector
                        .lease()
                )
                .tcp("localhost", 7000);
    }
    
    public String getLeaseInfo() {
        return """
                Lease Pattern provides:
                - Server-controlled rate limiting
                - Load-based backpressure
                - Request quotas
                - Time-to-live for requests
                """;
    }
}

/**
 * Composite Metadata Pattern
 * 
 * Demonstrates multiple metadata entries:
 * - Authentication tokens
 * - Tracing information
 * - Custom headers
 * 
 * @author Spring Patterns
 */

@Service
@Slf4j
class CompositeMetadataService {
    
    private final RSocketRequester requester;
    
    public CompositeMetadataService(RSocketRequester.Builder builder) {
        this.requester = builder.tcp("localhost", 7000);
    }
    
    public <T> Mono<T> requestWithCompositeMetadata(String route, Object data, Class<T> responseType) {
        return requester
                .route(route)
                .metadata(metadataSpec -> {
                    metadataSpec.metadata("Bearer token123", 
                            MimeType.valueOf(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString()));
                    metadataSpec.metadata("trace-id-123", 
                            MimeType.valueOf(WellKnownMimeType.MESSAGE_RSOCKET_TRACING_ZIPKIN.getString()));
                    metadataSpec.metadata("custom-header-value", MimeTypeUtils.TEXT_PLAIN);
                })
                .data(data)
                .retrieveMono(responseType);
    }
}

@Service
class AdvancedPatternsInfoService {
    
    public String getAllPatternsInfo() {
        return """
                Advanced RSocket Patterns
                ========================
                
                1. Metadata Push
                   - Send metadata without payload
                   - Control messages
                   - Configuration updates
                   
                2. Resume
                   - Session resumption after disconnect
                   - State preservation
                   - Stream continuation
                   
                3. Lease
                   - Server-controlled rate limiting
                   - Request quotas
                   - Load management
                   
                4. Composite Metadata
                   - Multiple metadata entries
                   - Authentication + Tracing
                   - Custom headers
                """;
    }
}

@RestController
@RequestMapping("/rsocket/advanced")
class AdvancedPatternsController {
    
    private final MetadataPushService metadataService;
    private final ResumeService resumeService;
    private final LeaseService leaseService;
    private final CompositeMetadataService compositeService;
    private final AdvancedPatternsInfoService infoService;
    
    public AdvancedPatternsController(MetadataPushService metadataService,
                                     ResumeService resumeService,
                                     LeaseService leaseService,
                                     CompositeMetadataService compositeService,
                                     AdvancedPatternsInfoService infoService) {
        this.metadataService = metadataService;
        this.resumeService = resumeService;
        this.leaseService = leaseService;
        this.compositeService = compositeService;
        this.infoService = infoService;
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return infoService.getAllPatternsInfo();
    }
    
    @PostMapping("/metadata/push")
    public Mono<Void> testMetadataPush(@RequestBody String metadata) {
        return metadataService.pushMetadata(metadata);
    }
    
    @GetMapping("/resume/info")
    public String getResumeInfo() {
        return resumeService.getResumeInfo();
    }
    
    @GetMapping("/lease/info")
    public String getLeaseInfo() {
        return leaseService.getLeaseInfo();
    }
}

@SpringBootApplication
public class AdvancedRSocketPatterns {
    public static void main(String[] args) {
        SpringApplication.run(AdvancedRSocketPatterns.class, args);
    }
}
