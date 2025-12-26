package com.example.devtools.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

/**
 * 🐛 SPRING BOOT DEVTOOLS - REMOTE DEBUGGING PATTERN 🐛
 * ======================================================
 * 
 * Demonstrates Spring Boot DevTools remote debugging and remote update
 * capabilities. Allows developers to connect to remote applications for
 * debugging and hot-swapping code changes without restart.
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ REMOTE DEBUGGING:
 *    - JDWP (Java Debug Wire Protocol)
 *    - Attach debugger to running application
 *    - Set breakpoints remotely
 *    - Step through code execution
 *    - Inspect variables and stack traces
 * 
 * 2️⃣ REMOTE DEVTOOLS CONNECTION:
 *    - HTTP tunnel between local and remote
 *    - Secure connection with secret token
 *    - Enables remote restart
 *    - Enables remote LiveReload
 * 
 * 3️⃣ REMOTE UPDATE:
 *    - Upload local changes to remote server
 *    - Trigger remote restart with new classes
 *    - Fast iteration on remote environments
 * 
 * 4️⃣ SECURITY:
 *    - Secret token authentication
 *    - HTTPS recommended
 *    - Firewall configuration
 *    - Only for dev/staging environments
 * 
 * 📦 DEPENDENCIES:
 * ===============
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-devtools</artifactId>
 *     <optional>true</optional>
 * </dependency>
 * 
 * ⚙️ REMOTE APPLICATION CONFIGURATION (application.yml):
 * ======================================================
 * spring:
 *   devtools:
 *     remote:
 *       secret: mysecret123          # Secret token for authentication
 *       context-path: /.~~spring-boot!~
 *       restart:
 *         enabled: true               # Enable remote restart
 *       proxy:
 *         host: proxy.example.com     # Optional HTTP proxy
 *         port: 8080
 * 
 * # Enable JDWP for remote debugging
 * # Add JVM arguments when starting remote application:
 * java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar app.jar
 * 
 * 🔧 LOCAL DEVTOOLS CLIENT SETUP:
 * ===============================
 * # Run DevTools client to connect to remote application
 * mvn spring-boot:run -Dspring-boot.run.arguments=--spring.devtools.remote.secret=mysecret123
 * 
 * # Or via IDE:
 * Run configuration:
 * Main class: org.springframework.boot.devtools.RemoteSpringApplication
 * Arguments: https://my-remote-app.com --spring.devtools.remote.secret=mysecret123
 * 
 * 🐛 IDE REMOTE DEBUGGING SETUP:
 * ==============================
 * 
 * IntelliJ IDEA:
 * 1. Run → Edit Configurations → Add New → Remote JVM Debug
 * 2. Host: my-remote-app.com
 * 3. Port: 5005 (JDWP port)
 * 4. Debugger mode: Attach to remote JVM
 * 5. Run → Debug 'Remote Debug'
 * 
 * Eclipse/STS:
 * 1. Debug → Debug Configurations → Remote Java Application
 * 2. Project: Select your project
 * 3. Connection Type: Standard (Socket Attach)
 * 4. Host: my-remote-app.com
 * 5. Port: 5005
 * 6. Click Debug
 * 
 * VS Code:
 * {
 *   "type": "java",
 *   "name": "Remote Debug",
 *   "request": "attach",
 *   "hostName": "my-remote-app.com",
 *   "port": 5005
 * }
 * 
 * 🔐 SSH TUNNELING (Recommended):
 * ===============================
 * # Create SSH tunnel for JDWP port
 * ssh -L 5005:localhost:5005 user@remote-server.com
 * 
 * # Then connect debugger to localhost:5005
 * # This provides encrypted connection and doesn't expose JDWP port publicly
 * 
 * 🌐 REMOTE APPLICATION SERVER SETUP:
 * ===================================
 * # 1. Build application with DevTools
 * mvn clean package -Dspring-boot.repackage.excludeDevtools=false
 * 
 * # 2. Start with remote debugging and DevTools enabled
 * java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
 *      -Dspring.devtools.remote.secret=mysecret123 \
 *      -jar myapp.jar
 * 
 * # 3. Firewall rules (if needed)
 * # Open port 5005 for JDWP
 * # Open port 8080 for HTTP (application)
 * 
 * ⚠️ SECURITY WARNINGS:
 * ====================
 * ❌ NEVER enable remote debugging in production
 * ❌ NEVER expose JDWP port (5005) to public internet
 * ❌ NEVER use weak/default secret tokens
 * ❌ NEVER enable DevTools in production JAR
 * 
 * ✅ Always use SSH tunneling for JDWP
 * ✅ Always use HTTPS for DevTools remote connection
 * ✅ Always use strong secret tokens
 * ✅ Always restrict access with firewall rules
 * ✅ Only use in dev/staging environments
 * 
 * 🔄 REMOTE UPDATE WORKFLOW:
 * =========================
 * 1. Make code changes locally
 * 2. IDE auto-compiles (or mvn compile)
 * 3. DevTools client detects changes
 * 4. Uploads changed classes to remote server
 * 5. Remote server restarts with new classes
 * 6. Total time: 2-5 seconds
 * 
 * 💡 WHEN TO USE:
 * ==============
 * ✅ Debugging issues on staging environment
 * ✅ Debugging cloud-deployed applications (dev)
 * ✅ Debugging Docker containers
 * ✅ Debugging Kubernetes pods (dev namespace)
 * ✅ Quick fixes on remote dev environments
 * ✅ Performance profiling on remote systems
 * 
 * ❌ WHEN NOT TO USE:
 * ==================
 * ❌ Production environments (SECURITY RISK)
 * ❌ Public-facing applications
 * ❌ Systems with sensitive data
 * ❌ High-traffic applications (performance impact)
 * ❌ When proper CI/CD is available
 * 
 * 🐳 DOCKER REMOTE DEBUGGING:
 * ===========================
 * # Dockerfile
 * FROM openjdk:17-jdk
 * COPY target/myapp.jar app.jar
 * EXPOSE 8080 5005
 * ENTRYPOINT ["java", \
 *   "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", \
 *   "-jar", "app.jar"]
 * 
 * # docker-compose.yml
 * services:
 *   app:
 *     build: .
 *     ports:
 *       - "8080:8080"
 *       - "5005:5005"  # JDWP port
 *     environment:
 *       - SPRING_DEVTOOLS_REMOTE_SECRET=mysecret123
 * 
 * # Run and connect debugger to localhost:5005
 * 
 * ☸️ KUBERNETES REMOTE DEBUGGING:
 * ===============================
 * # Deployment with debug port
 * apiVersion: apps/v1
 * kind: Deployment
 * metadata:
 *   name: myapp-dev
 * spec:
 *   template:
 *     spec:
 *       containers:
 *       - name: myapp
 *         image: myapp:latest
 *         ports:
 *         - containerPort: 8080
 *           name: http
 *         - containerPort: 5005
 *           name: jdwp
 *         env:
 *         - name: JAVA_TOOL_OPTIONS
 *           value: "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
 *         - name: SPRING_DEVTOOLS_REMOTE_SECRET
 *           value: "mysecret123"
 * 
 * # Port-forward for debugging
 * kubectl port-forward deployment/myapp-dev 5005:5005
 * 
 * # Connect debugger to localhost:5005
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
public class RemoteDebuggingPattern {

    public static void main(String[] args) {
        SpringApplication.run(RemoteDebuggingPattern.class, args);
    }
}

/**
 * Remote Debugging Configuration
 */
@Configuration
@Profile("dev")
class RemoteDebuggingConfiguration {

    /**
     * Remote debugging information service
     */
    @Bean
    public RemoteDebugInfoService remoteDebugInfoService() {
        return new RemoteDebugInfoService();
    }

    /**
     * Remote connection monitor
     */
    @Bean
    public RemoteConnectionMonitor remoteConnectionMonitor() {
        return new RemoteConnectionMonitor();
    }
}

/**
 * Remote Debug Information Service
 * Provides information about remote debugging configuration
 */
@Service
class RemoteDebugInfoService {

    /**
     * Get JDWP (Java Debug Wire Protocol) configuration
     */
    public Map<String, Object> getJdwpConfig() {
        Map<String, Object> config = new ConcurrentHashMap<>();
        
        String javaToolOptions = System.getenv("JAVA_TOOL_OPTIONS");
        String jdwpOptions = getJdwpOptionsFromSystemProperties();
        
        config.put("javaToolOptions", javaToolOptions != null ? javaToolOptions : "Not set");
        config.put("jdwpFromProperties", jdwpOptions != null ? jdwpOptions : "Not set");
        config.put("isDebugEnabled", isDebugEnabled());
        config.put("debugPort", getDebugPort());
        
        return config;
    }

    private String getJdwpOptionsFromSystemProperties() {
        // Check if JDWP agent is loaded
        List<String> inputArguments = java.lang.management.ManagementFactory
            .getRuntimeMXBean()
            .getInputArguments();
        
        return inputArguments.stream()
            .filter(arg -> arg.contains("jdwp"))
            .findFirst()
            .orElse(null);
    }

    private boolean isDebugEnabled() {
        return getJdwpOptionsFromSystemProperties() != null ||
               (System.getenv("JAVA_TOOL_OPTIONS") != null && 
                System.getenv("JAVA_TOOL_OPTIONS").contains("jdwp"));
    }

    private String getDebugPort() {
        String jdwpOptions = getJdwpOptionsFromSystemProperties();
        if (jdwpOptions == null) {
            jdwpOptions = System.getenv("JAVA_TOOL_OPTIONS");
        }
        
        if (jdwpOptions != null && jdwpOptions.contains("address=")) {
            int startIdx = jdwpOptions.indexOf("address=") + 8;
            int endIdx = jdwpOptions.indexOf(",", startIdx);
            if (endIdx == -1) endIdx = jdwpOptions.length();
            
            String address = jdwpOptions.substring(startIdx, endIdx);
            // Extract port from address like "*:5005" or "5005"
            if (address.contains(":")) {
                return address.substring(address.indexOf(":") + 1);
            }
            return address;
        }
        
        return "Unknown";
    }

    /**
     * Get DevTools remote configuration
     */
    public Map<String, Object> getDevToolsRemoteConfig() {
        Map<String, Object> config = new ConcurrentHashMap<>();
        
        // Note: These properties may not be accessible at runtime
        // This is for demonstration purposes
        config.put("remoteSecret", "****** (hidden for security)");
        config.put("remoteContextPath", "/.~~spring-boot!~");
        config.put("remoteRestartEnabled", true);
        config.put("note", "DevTools remote must be configured in application.yml");
        
        return config;
    }

    /**
     * Get remote debugging instructions
     */
    public Map<String, Object> getRemoteDebugInstructions() {
        Map<String, Object> instructions = new ConcurrentHashMap<>();
        
        Map<String, String> intellij = new ConcurrentHashMap<>();
        intellij.put("step1", "Run → Edit Configurations → Add New → Remote JVM Debug");
        intellij.put("step2", "Host: <remote-host>");
        intellij.put("step3", "Port: " + getDebugPort());
        intellij.put("step4", "Run → Debug 'Remote Debug'");
        
        Map<String, String> eclipse = new ConcurrentHashMap<>();
        eclipse.put("step1", "Debug → Debug Configurations → Remote Java Application");
        eclipse.put("step2", "Connection Type: Standard (Socket Attach)");
        eclipse.put("step3", "Host: <remote-host>, Port: " + getDebugPort());
        eclipse.put("step4", "Click Debug");
        
        Map<String, String> vscode = new ConcurrentHashMap<>();
        vscode.put("launch.json", "{\n" +
            "  \"type\": \"java\",\n" +
            "  \"name\": \"Remote Debug\",\n" +
            "  \"request\": \"attach\",\n" +
            "  \"hostName\": \"<remote-host>\",\n" +
            "  \"port\": " + getDebugPort() + "\n" +
            "}");
        
        instructions.put("IntelliJ IDEA", intellij);
        instructions.put("Eclipse/STS", eclipse);
        instructions.put("VS Code", vscode);
        
        return instructions;
    }

    /**
     * Get SSH tunnel command
     */
    public String getSshTunnelCommand() {
        return String.format("ssh -L %s:localhost:%s user@remote-server.com", 
            getDebugPort(), getDebugPort());
    }

    /**
     * Get security recommendations
     */
    public List<String> getSecurityRecommendations() {
        List<String> recommendations = new ArrayList<>();
        recommendations.add("❌ NEVER enable remote debugging in production");
        recommendations.add("❌ NEVER expose JDWP port to public internet");
        recommendations.add("❌ NEVER use weak secret tokens");
        recommendations.add("✅ ALWAYS use SSH tunneling for JDWP");
        recommendations.add("✅ ALWAYS use HTTPS for DevTools remote");
        recommendations.add("✅ ALWAYS use strong secret tokens");
        recommendations.add("✅ ALWAYS restrict access with firewall rules");
        recommendations.add("✅ ONLY use in dev/staging environments");
        return recommendations;
    }
}

/**
 * Remote Connection Monitor
 * Monitors remote debugging connections (simulated)
 */
@Service
class RemoteConnectionMonitor {

    private final List<RemoteConnectionEvent> connectionHistory = new ArrayList<>();
    private LocalDateTime lastConnectionTime;
    private int activeConnections = 0;

    public void recordConnection(String source, String type) {
        RemoteConnectionEvent event = new RemoteConnectionEvent(
            source,
            type,
            LocalDateTime.now()
        );
        
        synchronized (connectionHistory) {
            connectionHistory.add(event);
            if (connectionHistory.size() > 100) {
                connectionHistory.remove(0);
            }
        }
        
        lastConnectionTime = LocalDateTime.now();
        activeConnections++;
        
        System.out.println("🔌 Remote connection: " + event);
    }

    public void recordDisconnection(String source) {
        activeConnections = Math.max(0, activeConnections - 1);
        System.out.println("🔌 Remote disconnection: " + source);
    }

    public List<RemoteConnectionEvent> getConnectionHistory() {
        synchronized (connectionHistory) {
            return new ArrayList<>(connectionHistory);
        }
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("activeConnections", activeConnections);
        status.put("lastConnectionTime", lastConnectionTime);
        status.put("totalConnections", connectionHistory.size());
        return status;
    }
}

/**
 * Remote Connection Event
 */
class RemoteConnectionEvent {
    private final String source;
    private final String type;
    private final LocalDateTime timestamp;

    public RemoteConnectionEvent(String source, String type, LocalDateTime timestamp) {
        this.source = source;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getSource() { return source; }
    public String getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("RemoteConnection{source='%s', type=%s, time=%s}",
            source, type, timestamp);
    }
}

/**
 * Remote Debugging REST Controller
 */
@RestController
@RequestMapping("/api/remote-debug")
class RemoteDebuggingController {

    private final RemoteDebugInfoService debugInfoService;
    private final RemoteConnectionMonitor connectionMonitor;

    public RemoteDebuggingController(RemoteDebugInfoService debugInfoService,
                                     RemoteConnectionMonitor connectionMonitor) {
        this.debugInfoService = debugInfoService;
        this.connectionMonitor = connectionMonitor;
    }

    /**
     * GET /api/remote-debug/jdwp-config
     * Get JDWP configuration
     */
    @GetMapping("/jdwp-config")
    public Map<String, Object> getJdwpConfig() {
        return debugInfoService.getJdwpConfig();
    }

    /**
     * GET /api/remote-debug/devtools-config
     * Get DevTools remote configuration
     */
    @GetMapping("/devtools-config")
    public Map<String, Object> getDevToolsConfig() {
        return debugInfoService.getDevToolsRemoteConfig();
    }

    /**
     * GET /api/remote-debug/instructions
     * Get IDE-specific debugging instructions
     */
    @GetMapping("/instructions")
    public Map<String, Object> getInstructions() {
        return debugInfoService.getRemoteDebugInstructions();
    }

    /**
     * GET /api/remote-debug/ssh-tunnel
     * Get SSH tunnel command
     */
    @GetMapping("/ssh-tunnel")
    public Map<String, String> getSshTunnelCommand() {
        Map<String, String> response = new ConcurrentHashMap<>();
        response.put("command", debugInfoService.getSshTunnelCommand());
        response.put("description", "Create SSH tunnel for secure JDWP connection");
        return response;
    }

    /**
     * GET /api/remote-debug/security
     * Get security recommendations
     */
    @GetMapping("/security")
    public List<String> getSecurityRecommendations() {
        return debugInfoService.getSecurityRecommendations();
    }

    /**
     * GET /api/remote-debug/connections
     * Get connection history
     */
    @GetMapping("/connections")
    public List<RemoteConnectionEvent> getConnections() {
        return connectionMonitor.getConnectionHistory();
    }

    /**
     * GET /api/remote-debug/status
     * Get remote debugging status
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return connectionMonitor.getStatus();
    }

    /**
     * POST /api/remote-debug/simulate-connection
     * Simulate remote connection (for testing)
     */
    @PostMapping("/simulate-connection")
    public String simulateConnection(@RequestParam String source, @RequestParam String type) {
        connectionMonitor.recordConnection(source, type);
        return "✅ Connection recorded: " + source + " (" + type + ")";
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ START REMOTE APPLICATION WITH JDWP:
 * ----------------------------------------
 * java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
 *      -Dspring.devtools.remote.secret=mysecret123 \
 *      -jar myapp.jar
 * 
 * 2️⃣ SSH TUNNEL FOR SECURE CONNECTION:
 * -------------------------------------
 * ssh -L 5005:localhost:5005 user@remote-server.com
 * # Now connect debugger to localhost:5005
 * 
 * 3️⃣ DEVTOOLS REMOTE CLIENT:
 * ---------------------------
 * mvn spring-boot:run \
 *   -Dspring-boot.run.arguments="https://remote-app.com --spring.devtools.remote.secret=mysecret123"
 * 
 * 4️⃣ DOCKER REMOTE DEBUGGING:
 * ----------------------------
 * docker run -p 8080:8080 -p 5005:5005 \
 *   -e JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
 *   myapp:latest
 * 
 * 5️⃣ KUBERNETES PORT FORWARD:
 * ----------------------------
 * kubectl port-forward deployment/myapp-dev 5005:5005
 * # Connect debugger to localhost:5005
 * 
 * 6️⃣ GET DEBUGGING INFO:
 * -----------------------
 * curl http://localhost:8080/api/remote-debug/jdwp-config
 * curl http://localhost:8080/api/remote-debug/instructions
 * curl http://localhost:8080/api/remote-debug/ssh-tunnel
 * curl http://localhost:8080/api/remote-debug/security
 */
