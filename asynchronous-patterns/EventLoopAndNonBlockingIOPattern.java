package com.example.async.eventloop;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Event Loop and Non-blocking I/O Pattern
 * 
 * Purpose: Handle many connections efficiently with single thread
 * 
 * Key Concepts:
 * 1. Event Loop - Single-threaded event processing
 * 2. Non-blocking I/O - Operations that don't block threads
 * 3. Selector - Multiplexing I/O events
 * 4. Reactor Pattern - Event demultiplexing and dispatching
 * 
 * Components:
 * - Event Queue
 * - Event Loop
 * - Event Handlers
 * - Selector (for NIO)
 * - Channels (for NIO)
 * 
 * Benefits:
 * - High scalability
 * - Efficient resource usage
 * - Low latency
 * - Single-threaded simplicity
 */

// Event System
class Event {
    private final String type;
    private final Object data;
    private final long timestamp;
    
    public Event(String type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getType() { return type; }
    public Object getData() { return data; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("Event[type=%s, data=%s, timestamp=%d]", type, data, timestamp);
    }
}

// Event Handler Interface
interface EventHandler {
    void handle(Event event);
    boolean canHandle(String eventType);
}

// Simple Event Loop
class SimpleEventLoop {
    private final BlockingQueue<Event> eventQueue;
    private final Map<String, List<EventHandler>> handlers;
    private volatile boolean running;
    private Thread loopThread;
    
    public SimpleEventLoop() {
        this.eventQueue = new LinkedBlockingQueue<>();
        this.handlers = new ConcurrentHashMap<>();
        this.running = false;
    }
    
    /**
     * Register event handler
     */
    public void on(String eventType, EventHandler handler) {
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
        System.out.println("  [EventLoop] Registered handler for: " + eventType);
    }
    
    /**
     * Emit event
     */
    public void emit(Event event) {
        try {
            eventQueue.put(event);
            System.out.println("  [EventLoop] Emitted: " + event);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Start event loop
     */
    public void start() {
        if (running) {
            return;
        }
        
        running = true;
        loopThread = new Thread(() -> {
            System.out.println("  [EventLoop] Started on thread: " + Thread.currentThread().getName());
            
            while (running) {
                try {
                    Event event = eventQueue.poll(100, TimeUnit.MILLISECONDS);
                    
                    if (event != null) {
                        processEvent(event);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            System.out.println("  [EventLoop] Stopped");
        }, "EventLoop-Thread");
        
        loopThread.start();
    }
    
    /**
     * Stop event loop
     */
    public void stop() {
        running = false;
        if (loopThread != null) {
            try {
                loopThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void processEvent(Event event) {
        List<EventHandler> eventHandlers = handlers.get(event.getType());
        
        if (eventHandlers != null) {
            for (EventHandler handler : eventHandlers) {
                try {
                    handler.handle(event);
                } catch (Exception e) {
                    System.err.println("  [EventLoop] Handler error: " + e.getMessage());
                }
            }
        } else {
            System.out.println("  [EventLoop] No handler for: " + event.getType());
        }
    }
    
    public int getQueueSize() {
        return eventQueue.size();
    }
}

// Event-driven Application Example
class ChatApplication {
    private final SimpleEventLoop eventLoop;
    
    public ChatApplication() {
        this.eventLoop = new SimpleEventLoop();
        setupHandlers();
    }
    
    private void setupHandlers() {
        // User join handler
        eventLoop.on("user.join", new EventHandler() {
            @Override
            public void handle(Event event) {
                String username = (String) event.getData();
                System.out.println("  [Chat] " + username + " joined the chat");
            }
            
            @Override
            public boolean canHandle(String eventType) {
                return "user.join".equals(eventType);
            }
        });
        
        // Message handler
        eventLoop.on("message", new EventHandler() {
            @Override
            public void handle(Event event) {
                String message = (String) event.getData();
                System.out.println("  [Chat] New message: " + message);
            }
            
            @Override
            public boolean canHandle(String eventType) {
                return "message".equals(eventType);
            }
        });
        
        // User leave handler
        eventLoop.on("user.leave", new EventHandler() {
            @Override
            public void handle(Event event) {
                String username = (String) event.getData();
                System.out.println("  [Chat] " + username + " left the chat");
            }
            
            @Override
            public boolean canHandle(String eventType) {
                return "user.leave".equals(eventType);
            }
        });
    }
    
    public void start() {
        eventLoop.start();
    }
    
    public void userJoin(String username) {
        eventLoop.emit(new Event("user.join", username));
    }
    
    public void sendMessage(String message) {
        eventLoop.emit(new Event("message", message));
    }
    
    public void userLeave(String username) {
        eventLoop.emit(new Event("user.leave", username));
    }
    
    public void stop() {
        eventLoop.stop();
    }
}

// Non-blocking I/O Server
class NonBlockingEchoServer {
    private final int port;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private volatile boolean running;
    
    public NonBlockingEchoServer(int port) {
        this.port = port;
    }
    
    /**
     * Start non-blocking server
     */
    public void start() throws IOException {
        System.out.println("  [NIO Server] Starting on port " + port);
        
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);
        
        // Register for ACCEPT events
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        running = true;
        
        new Thread(() -> {
            try {
                eventLoop();
            } catch (IOException e) {
                System.err.println("  [NIO Server] Error: " + e.getMessage());
            }
        }, "NIO-Server-Thread").start();
        
        System.out.println("  [NIO Server] Started successfully");
    }
    
    /**
     * Event loop for non-blocking I/O
     */
    private void eventLoop() throws IOException {
        System.out.println("  [NIO Server] Event loop started on: " + Thread.currentThread().getName());
        
        while (running) {
            // Select ready channels (non-blocking with timeout)
            int readyChannels = selector.select(1000);
            
            if (readyChannels == 0) {
                continue;
            }
            
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                
                try {
                    if (!key.isValid()) {
                        continue;
                    }
                    
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    } else if (key.isWritable()) {
                        handleWrite(key);
                    }
                } catch (IOException e) {
                    System.err.println("  [NIO Server] Error processing key: " + e.getMessage());
                    key.cancel();
                    key.channel().close();
                }
            }
        }
    }
    
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        
        System.out.println("  [NIO Server] Accepted connection from: " + 
            clientChannel.getRemoteAddress());
        
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }
    
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        
        int bytesRead = channel.read(buffer);
        
        if (bytesRead == -1) {
            System.out.println("  [NIO Server] Client disconnected: " + channel.getRemoteAddress());
            channel.close();
            key.cancel();
            return;
        }
        
        if (bytesRead > 0) {
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String message = new String(data, StandardCharsets.UTF_8);
            
            System.out.println("  [NIO Server] Received: " + message.trim());
            
            // Echo back
            buffer.rewind();
            key.attach(buffer);
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }
    
    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        
        if (buffer != null) {
            channel.write(buffer);
            
            if (!buffer.hasRemaining()) {
                System.out.println("  [NIO Server] Echo sent to: " + channel.getRemoteAddress());
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }
    
    public void stop() throws IOException {
        running = false;
        
        if (selector != null) {
            selector.close();
        }
        
        if (serverChannel != null) {
            serverChannel.close();
        }
        
        System.out.println("  [NIO Server] Stopped");
    }
}

// Reactor Pattern Implementation
class Reactor {
    private final SimpleEventLoop eventLoop;
    private final Map<String, Consumer<Object>> reactors;
    
    public Reactor() {
        this.eventLoop = new SimpleEventLoop();
        this.reactors = new ConcurrentHashMap<>();
    }
    
    /**
     * Register reactor for event type
     */
    public void register(String eventType, Consumer<Object> reactor) {
        reactors.put(eventType, reactor);
        
        eventLoop.on(eventType, new EventHandler() {
            @Override
            public void handle(Event event) {
                Consumer<Object> consumer = reactors.get(event.getType());
                if (consumer != null) {
                    consumer.accept(event.getData());
                }
            }
            
            @Override
            public boolean canHandle(String type) {
                return eventType.equals(type);
            }
        });
    }
    
    /**
     * Dispatch event
     */
    public void dispatch(String eventType, Object data) {
        eventLoop.emit(new Event(eventType, data));
    }
    
    public void start() {
        eventLoop.start();
    }
    
    public void stop() {
        eventLoop.stop();
    }
}

// Data Processing with Reactor
class DataProcessor {
    private final Reactor reactor;
    
    public DataProcessor() {
        this.reactor = new Reactor();
        setupReactors();
    }
    
    private void setupReactors() {
        // Data received reactor
        reactor.register("data.received", data -> {
            System.out.println("  [Reactor] Processing data: " + data);
            reactor.dispatch("data.validated", data);
        });
        
        // Data validated reactor
        reactor.register("data.validated", data -> {
            System.out.println("  [Reactor] Data validated: " + data);
            reactor.dispatch("data.transformed", ((String) data).toUpperCase());
        });
        
        // Data transformed reactor
        reactor.register("data.transformed", data -> {
            System.out.println("  [Reactor] Data transformed: " + data);
            reactor.dispatch("data.stored", data);
        });
        
        // Data stored reactor
        reactor.register("data.stored", data -> {
            System.out.println("  [Reactor] Data stored: " + data);
        });
    }
    
    public void start() {
        reactor.start();
    }
    
    public void processData(String data) {
        reactor.dispatch("data.received", data);
    }
    
    public void stop() {
        reactor.stop();
    }
}

/**
 * Demonstration of Event Loop and Non-blocking I/O Pattern
 */
public class EventLoopAndNonBlockingIOPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Event Loop and Non-blocking I/O Pattern Demo ===\n");
        
        // 1. Simple Event Loop
        System.out.println("1. Simple Event Loop:");
        SimpleEventLoop eventLoop = new SimpleEventLoop();
        
        eventLoop.on("test", new EventHandler() {
            @Override
            public void handle(Event event) {
                System.out.println("  Handler received: " + event.getData());
            }
            
            @Override
            public boolean canHandle(String eventType) {
                return "test".equals(eventType);
            }
        });
        
        eventLoop.start();
        
        eventLoop.emit(new Event("test", "Hello Event Loop!"));
        eventLoop.emit(new Event("test", "Second event"));
        
        Thread.sleep(1000);
        eventLoop.stop();
        
        // 2. Chat Application
        System.out.println("\n2. Event-Driven Chat Application:");
        ChatApplication chat = new ChatApplication();
        chat.start();
        
        chat.userJoin("Alice");
        Thread.sleep(200);
        chat.sendMessage("Hello everyone!");
        Thread.sleep(200);
        chat.userJoin("Bob");
        Thread.sleep(200);
        chat.sendMessage("Hi Alice!");
        Thread.sleep(200);
        chat.userLeave("Alice");
        Thread.sleep(200);
        
        chat.stop();
        
        // 3. Reactor Pattern
        System.out.println("\n3. Reactor Pattern:");
        DataProcessor processor = new DataProcessor();
        processor.start();
        
        processor.processData("sample data");
        Thread.sleep(500);
        
        processor.stop();
        
        // 4. Non-blocking I/O Server (demonstration)
        System.out.println("\n4. Non-blocking I/O Server:");
        System.out.println("  Starting NIO echo server on port 8080...");
        
        NonBlockingEchoServer server = new NonBlockingEchoServer(8080);
        server.start();
        
        Thread.sleep(1000);
        
        // Simulate client connection
        System.out.println("\n  Simulating client connection...");
        new Thread(() -> {
            try (Socket client = new Socket("localhost", 8080);
                 PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                
                out.println("Hello NIO Server!");
                String response = in.readLine();
                System.out.println("  [Client] Received echo: " + response);
                
            } catch (IOException e) {
                System.err.println("  [Client] Error: " + e.getMessage());
            }
        }).start();
        
        Thread.sleep(2000);
        server.stop();
        
        System.out.println("\n=== Event Loop Concepts ===");
        System.out.println("Components:");
        System.out.println("  - Event Queue: Stores pending events");
        System.out.println("  - Event Loop: Processes events sequentially");
        System.out.println("  - Event Handlers: React to specific events");
        System.out.println("  - Single Thread: All processing in one thread");
        
        System.out.println("\nFlow:");
        System.out.println("  1. Events added to queue");
        System.out.println("  2. Loop picks next event");
        System.out.println("  3. Dispatches to handler");
        System.out.println("  4. Handler processes event");
        System.out.println("  5. Repeat");
        
        System.out.println("\n=== Non-blocking I/O (NIO) ===");
        System.out.println("Key Components:");
        System.out.println("  - Selector: Multiplexes multiple channels");
        System.out.println("  - Channel: Connection to I/O device");
        System.out.println("  - Buffer: Container for data");
        System.out.println("  - SelectionKey: Channel registration");
        
        System.out.println("\nOperations:");
        System.out.println("  - OP_ACCEPT: Accept new connections");
        System.out.println("  - OP_CONNECT: Connection established");
        System.out.println("  - OP_READ: Data ready to read");
        System.out.println("  - OP_WRITE: Ready to write data");
        
        System.out.println("\n=== Reactor Pattern ===");
        System.out.println("Purpose: Demultiplex and dispatch events");
        System.out.println("\nComponents:");
        System.out.println("  - Resources: I/O sources (sockets, files)");
        System.out.println("  - Synchronous Event Demultiplexer: Selector");
        System.out.println("  - Dispatcher: Event loop");
        System.out.println("  - Request Handlers: Process events");
        
        System.out.println("\n=== Benefits ===");
        System.out.println("✓ High scalability (1000s of connections)");
        System.out.println("✓ Low resource usage");
        System.out.println("✓ No thread-per-request overhead");
        System.out.println("✓ Predictable latency");
        System.out.println("✓ Simple concurrency model");
        System.out.println("✓ Efficient for I/O-bound workloads");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("✓ Web servers (Node.js, Netty)");
        System.out.println("✓ Chat servers");
        System.out.println("✓ Real-time applications");
        System.out.println("✓ Proxy servers");
        System.out.println("✓ Message brokers");
        System.out.println("✓ IoT gateways");
        
        System.out.println("\n=== Best Practices ===");
        System.out.println("✓ Don't block the event loop");
        System.out.println("✓ Keep handlers lightweight");
        System.out.println("✓ Use worker threads for CPU-intensive tasks");
        System.out.println("✓ Handle errors in handlers");
        System.out.println("✓ Monitor queue size");
        System.out.println("✓ Set appropriate buffer sizes");
        System.out.println("✓ Use timeouts for operations");
        
        System.out.println("\n=== Limitations ===");
        System.out.println("✗ Single-threaded (one CPU core)");
        System.out.println("✗ Not ideal for CPU-intensive tasks");
        System.out.println("✗ Callback complexity");
        System.out.println("✗ Debugging can be harder");
        System.out.println("✗ Error in handler blocks loop");
        
        System.out.println("\n=== Blocking vs Non-blocking ===");
        System.out.println("Blocking I/O:");
        System.out.println("  - Thread per connection");
        System.out.println("  - Thread blocks on I/O");
        System.out.println("  - Simple programming model");
        System.out.println("  - Limited scalability");
        
        System.out.println("\nNon-blocking I/O:");
        System.out.println("  - Single thread, many connections");
        System.out.println("  - No blocking on I/O");
        System.out.println("  - Event-driven programming");
        System.out.println("  - High scalability");
        
        System.out.println("\n=== Real-world Frameworks ===");
        System.out.println("Node.js - JavaScript event loop");
        System.out.println("Netty - Java NIO framework");
        System.out.println("Vert.x - Reactive applications");
        System.out.println("Reactor - Spring reactive framework");
        System.out.println("Project Loom - Virtual threads (Java 19+)");
    }
}
