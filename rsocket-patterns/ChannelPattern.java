package com.example.rsocket;

import io.rsocket.Payload;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

/**
 * RSocket Channel Pattern (Bidirectional Streaming)
 * 
 * Demonstrates the channel interaction model where:
 * - Client sends a STREAM of requests
 * - Server responds with a STREAM of responses
 * - Both streams flow independently
 * - Full-duplex bidirectional communication
 * 
 * Use Cases:
 * - Chat applications (messages flowing both ways)
 * - Real-time collaboration tools
 * - Gaming (player actions and game state updates)
 * - Voice/Video streaming
 * - Data synchronization
 * - Distributed transactions
 * 
 * Key Characteristics:
 * - N:M interaction model
 * - Bidirectional streaming
 * - Independent flow control on each side
 * - Complex backpressure handling
 * 
 * @author Spring Patterns
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
class ChatMessage {
    private String user;
    private String message;
    private Long timestamp;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GameAction {
    private String playerId;
    private String action;
    private Integer value;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GameState {
    private String gameId;
    private String state;
    private Integer score;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class SyncData {
    private String id;
    private String operation;
    private String data;
}

/**
 * RSocket Server - Handles bidirectional channel interactions
 */
@Controller
@Slf4j
class ChannelServerController {
    
    /**
     * Chat channel - bidirectional message streaming
     * Receives messages from client, sends messages to client
     */
    @MessageMapping("chat.channel")
    public Flux<ChatMessage> chatChannel(Flux<ChatMessage> messages) {
        log.info("Chat channel established");
        
        // Process incoming messages and generate responses
        return messages
                .doOnNext(msg -> log.info("Received chat message from {}: {}", 
                        msg.getUser(), msg.getMessage()))
                .flatMap(incomingMsg -> {
                    // Echo the message
                    ChatMessage echo = new ChatMessage(
                            "Server",
                            "Echo: " + incomingMsg.getMessage(),
                            System.currentTimeMillis()
                    );
                    
                    // Generate additional server messages
                    ChatMessage response = new ChatMessage(
                            "Server",
                            "Processed message from " + incomingMsg.getUser(),
                            System.currentTimeMillis()
                    );
                    
                    return Flux.just(echo, response);
                })
                .doOnComplete(() -> log.info("Chat channel closed"))
                .doOnError(error -> log.error("Chat channel error", error));
    }
    
    /**
     * Game channel - bidirectional game actions and state updates
     */
    @MessageMapping("game.channel")
    public Flux<GameState> gameChannel(Flux<GameAction> actions) {
        log.info("Game channel established");
        
        return actions
                .doOnNext(action -> log.info("Received game action: {} from player {}", 
                        action.getAction(), action.getPlayerId()))
                .scan(new GameState("game-1", "RUNNING", 0), (state, action) -> {
                    // Update game state based on action
                    int newScore = state.getScore() + action.getValue();
                    return new GameState(state.getGameId(), "UPDATED", newScore);
                })
                .delayElements(Duration.ofMillis(100));
    }
    
    /**
     * Data sync channel - bidirectional data synchronization
     */
    @MessageMapping("sync.channel")
    public Flux<SyncData> syncChannel(Flux<SyncData> clientData) {
        log.info("Sync channel established");
        
        return clientData
                .doOnNext(data -> log.info("Syncing data: {} - {}", data.getId(), data.getOperation()))
                .flatMap(data -> {
                    // Process sync operation
                    SyncData ack = new SyncData(
                            data.getId(),
                            "ACK",
                            "Acknowledged: " + data.getOperation()
                    );
                    
                    // Generate server-side sync data
                    SyncData serverData = new SyncData(
                            "server-" + System.currentTimeMillis(),
                            "PUSH",
                            "Server data update"
                    );
                    
                    return Flux.just(ack, serverData);
                });
    }
    
    /**
     * Collaborative editing channel
     */
    @MessageMapping("collab.channel")
    public Flux<String> collaborativeEditChannel(Flux<String> edits) {
        log.info("Collaborative edit channel established");
        
        return edits
                .doOnNext(edit -> log.info("Received edit: {}", edit))
                .map(edit -> "Processed: " + edit)
                .mergeWith(
                        // Simulate other users' edits
                        Flux.interval(Duration.ofSeconds(2))
                                .map(i -> "Remote edit #" + i)
                                .take(5)
                );
    }
}

/**
 * RSocket Client Service for Channel Pattern
 */
@Service
@Slf4j
class ChannelClientService {
    
    private final RSocketRequester requester;
    
    public ChannelClientService(RSocketRequester.Builder builder) {
        this.requester = builder
                .tcp("localhost", 7000);
    }
    
    /**
     * Establish chat channel
     */
    public Flux<ChatMessage> establishChatChannel() {
        log.info("Establishing chat channel");
        
        // Create stream of outgoing messages
        Flux<ChatMessage> outgoingMessages = Flux.interval(Duration.ofSeconds(1))
                .take(5)
                .map(i -> new ChatMessage(
                        "User-" + System.currentTimeMillis() % 100,
                        "Message #" + i,
                        System.currentTimeMillis()
                ));
        
        // Send messages and receive responses
        return requester
                .route("chat.channel")
                .data(outgoingMessages)
                .retrieveFlux(ChatMessage.class)
                .doOnNext(msg -> log.info("Received from server: {} - {}", 
                        msg.getUser(), msg.getMessage()));
    }
    
    /**
     * Establish game channel
     */
    public Flux<GameState> establishGameChannel() {
        log.info("Establishing game channel");
        
        // Create stream of game actions
        Flux<GameAction> actions = Flux.interval(Duration.ofMillis(500))
                .take(10)
                .map(i -> new GameAction(
                        "player-1",
                        "ACTION_" + i,
                        (int) (i * 10)
                ));
        
        return requester
                .route("game.channel")
                .data(actions)
                .retrieveFlux(GameState.class)
                .doOnNext(state -> log.info("Game state update: score = {}", state.getScore()));
    }
    
    /**
     * Establish sync channel
     */
    public Flux<SyncData> establishSyncChannel() {
        log.info("Establishing sync channel");
        
        // Create stream of sync operations
        Flux<SyncData> syncData = Flux.interval(Duration.ofSeconds(1))
                .take(3)
                .map(i -> new SyncData(
                        "client-" + i,
                        "UPDATE",
                        "Data #" + i
                ));
        
        return requester
                .route("sync.channel")
                .data(syncData)
                .retrieveFlux(SyncData.class)
                .doOnNext(data -> log.info("Sync response: {} - {}", 
                        data.getOperation(), data.getData()));
    }
}

/**
 * Channel Pattern Information Service
 */
@Service
class ChannelInfoService {
    
    public String getPatternInfo() {
        return """
                RSocket Channel Pattern (Bidirectional Streaming)
                ================================================
                
                Interaction Model:
                - Client sends: N requests (stream)
                - Server sends: M responses (stream)
                - Communication: Many-to-many
                - Flow Control: Independent on each side
                
                Use Cases:
                1. Real-Time Communication
                   - Chat applications
                   - Instant messaging
                   - Voice/Video calls
                
                2. Collaborative Tools
                   - Real-time document editing
                   - Whiteboard sharing
                   - Code pair programming
                
                3. Gaming
                   - Player actions streaming
                   - Game state synchronization
                   - Real-time multiplayer
                
                4. Data Synchronization
                   - Database replication
                   - Cache synchronization
                   - Distributed state management
                
                5. IoT Communication
                   - Sensor data exchange
                   - Command and control
                   - Telemetry bidirectional flow
                
                Advantages:
                - Full-duplex communication
                - Independent flow control
                - Efficient for bidirectional data
                - Lower latency than request-response
                - Built-in backpressure on both sides
                
                Challenges:
                - Complex flow control
                - State management
                - Error handling on both streams
                - Resource management
                - Stream lifecycle coordination
                
                Best Practices:
                1. Use doOnNext() for logging both directions
                2. Implement proper error handling on both streams
                3. Handle stream completion gracefully
                4. Monitor both input and output streams
                5. Use flatMap() for request-response per item
                6. Implement timeout strategies
                7. Use delayElements() for controlled flow
                8. Test backpressure scenarios
                """;
    }
    
    public List<String> getUseCases() {
        return List.of(
                "Chat Applications: Bidirectional message exchange",
                "Gaming: Player actions and game state updates",
                "Collaboration: Real-time document editing",
                "Data Sync: Bidirectional data synchronization",
                "Trading: Order streams and market data",
                "IoT: Sensor data and control commands",
                "Monitoring: Metrics collection and alerts",
                "Transactions: Distributed transaction coordination"
        );
    }
    
    public List<String> getFlowControlStrategies() {
        return List.of(
                "Independent: Each stream controls its own flow",
                "Coordinated: Synchronize flow on both sides",
                "Buffered: Buffer data on slow consumer",
                "Windowed: Process data in windows",
                "Priority-Based: Prioritize certain messages"
        );
    }
}

/**
 * REST Controller for testing Channel pattern
 */
@RestController
@RequestMapping("/rsocket/channel")
@Slf4j
class ChannelController {
    
    private final ChannelClientService clientService;
    private final ChannelInfoService infoService;
    
    public ChannelController(ChannelClientService clientService,
                            ChannelInfoService infoService) {
        this.clientService = clientService;
        this.infoService = infoService;
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return infoService.getPatternInfo();
    }
    
    @GetMapping("/use-cases")
    public List<String> getUseCases() {
        return infoService.getUseCases();
    }
    
    @GetMapping("/flow-control")
    public List<String> getFlowControlStrategies() {
        return infoService.getFlowControlStrategies();
    }
    
    @GetMapping("/test/chat")
    public Flux<ChatMessage> testChatChannel() {
        log.info("Testing chat channel");
        return clientService.establishChatChannel();
    }
    
    @GetMapping("/test/game")
    public Flux<GameState> testGameChannel() {
        log.info("Testing game channel");
        return clientService.establishGameChannel();
    }
    
    @GetMapping("/test/sync")
    public Flux<SyncData> testSyncChannel() {
        log.info("Testing sync channel");
        return clientService.establishSyncChannel();
    }
}

/**
 * Configuration for Channel Pattern
 */
@Configuration
class ChannelConfig {
    
    @Bean
    public RSocketStrategies rSocketStrategies() {
        return RSocketStrategies.builder()
                .build();
    }
}

@SpringBootApplication
public class ChannelPattern {
    public static void main(String[] args) {
        SpringApplication.run(ChannelPattern.class, args);
    }
}
