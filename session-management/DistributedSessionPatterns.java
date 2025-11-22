package com.example.session;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * DISTRIBUTED SESSION PATTERNS
 * ============================
 * 
 * This file combines three related patterns:
 * 1. Session Clustering - Distribute sessions across nodes
 * 2. Sticky Session - Route requests to same server
 * 3. Session Replication - Replicate session data across nodes
 * 
 * SESSION CLUSTERING PATTERN
 * -------------------------
 * Purpose:
 * - Share session data across multiple servers
 * - Enable horizontal scalability
 * - Provide failover capability
 * - Support distributed applications
 * 
 * Key Components:
 * - Cluster Manager: Manages cluster nodes
 * - Session Store: Distributed session storage
 * - Node Discovery: Find cluster members
 * - Health Monitoring: Track node health
 * 
 * STICKY SESSION PATTERN
 * ----------------------
 * Purpose:
 * - Route user requests to same server
 * - Maintain session affinity
 * - Optimize performance
 * - Reduce session synchronization
 * 
 * Key Components:
 * - Load Balancer: Routes requests
 * - Session Routing: Cookie/IP-based routing
 * - Failover Handler: Redirect on failure
 * 
 * SESSION REPLICATION PATTERN
 * ---------------------------
 * Purpose:
 * - Replicate session data across nodes
 * - Ensure high availability
 * - Prevent session loss
 * - Support failover
 * 
 * Key Components:
 * - Replication Manager: Manages replication
 * - Sync/Async modes: Replication strategies
 * - Conflict Resolution: Handle updates
 */

// 1. CLUSTER NODE
class ClusterNode implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String nodeId;
    private final String host;
    private final int port;
    private NodeStatus status;
    private long lastHeartbeat;
    
    public ClusterNode(String nodeId, String host, int port) {
        this.nodeId = nodeId;
        this.host = host;
        this.port = port;
        this.status = NodeStatus.ACTIVE;
        this.lastHeartbeat = System.currentTimeMillis();
    }
    
    public void heartbeat() {
        this.lastHeartbeat = System.currentTimeMillis();
        this.status = NodeStatus.ACTIVE;
    }
    
    public boolean isHealthy(long timeoutMs) {
        return System.currentTimeMillis() - lastHeartbeat < timeoutMs;
    }
    
    // Getters
    public String getNodeId() { return nodeId; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public NodeStatus getStatus() { return status; }
    public void setStatus(NodeStatus status) { this.status = status; }
    public long getLastHeartbeat() { return lastHeartbeat; }
    
    @Override
    public String toString() {
        return String.format("Node[id=%s, host=%s:%d, status=%s]", 
            nodeId, host, port, status);
    }
    
    enum NodeStatus {
        ACTIVE, INACTIVE, FAILED
    }
}

// 2. CLUSTER MANAGER
@Service
class ClusterManager {
    
    private final Map<String, ClusterNode> nodes = new ConcurrentHashMap<>();
    private final String currentNodeId;
    private static final long HEARTBEAT_TIMEOUT = 30000; // 30 seconds
    
    public ClusterManager() {
        this.currentNodeId = UUID.randomUUID().toString();
    }
    
    public void registerNode(ClusterNode node) {
        nodes.put(node.getNodeId(), node);
        System.out.println("Node registered: " + node);
    }
    
    public void unregisterNode(String nodeId) {
        ClusterNode node = nodes.remove(nodeId);
        if (node != null) {
            System.out.println("Node unregistered: " + node);
        }
    }
    
    public void sendHeartbeat(String nodeId) {
        ClusterNode node = nodes.get(nodeId);
        if (node != null) {
            node.heartbeat();
        }
    }
    
    public Collection<ClusterNode> getActiveNodes() {
        return nodes.values().stream()
            .filter(node -> node.isHealthy(HEARTBEAT_TIMEOUT))
            .toList();
    }
    
    public ClusterNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }
    
    public String getCurrentNodeId() {
        return currentNodeId;
    }
    
    public void checkNodeHealth() {
        nodes.values().forEach(node -> {
            if (!node.isHealthy(HEARTBEAT_TIMEOUT)) {
                node.setStatus(ClusterNode.NodeStatus.FAILED);
                System.out.println("Node failed: " + node.getNodeId());
            }
        });
    }
}

// 3. DISTRIBUTED SESSION
class DistributedSession implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String id;
    private final Map<String, Object> attributes;
    private final long creationTime;
    private long lastAccessedTime;
    private String primaryNodeId;
    private Set<String> replicaNodeIds;
    private int version;
    
    public DistributedSession(String id, String primaryNodeId) {
        this.id = id;
        this.attributes = new ConcurrentHashMap<>();
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = System.currentTimeMillis();
        this.primaryNodeId = primaryNodeId;
        this.replicaNodeIds = ConcurrentHashMap.newKeySet();
        this.version = 0;
    }
    
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
        version++;
    }
    
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    public void touch() {
        this.lastAccessedTime = System.currentTimeMillis();
    }
    
    public DistributedSession copy() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (DistributedSession) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy session", e);
        }
    }
    
    // Getters and setters
    public String getId() { return id; }
    public Map<String, Object> getAttributes() { return new HashMap<>(attributes); }
    public long getCreationTime() { return creationTime; }
    public long getLastAccessedTime() { return lastAccessedTime; }
    public String getPrimaryNodeId() { return primaryNodeId; }
    public void setPrimaryNodeId(String primaryNodeId) { this.primaryNodeId = primaryNodeId; }
    public Set<String> getReplicaNodeIds() { return new HashSet<>(replicaNodeIds); }
    public void addReplicaNode(String nodeId) { replicaNodeIds.add(nodeId); }
    public int getVersion() { return version; }
}

// 4. SESSION REPLICATION MANAGER
@Service
class SessionReplicationManager {
    
    private final ClusterManager clusterManager;
    private final Map<String, DistributedSession> localSessions = new ConcurrentHashMap<>();
    private final ReplicationMode mode;
    
    public SessionReplicationManager(ClusterManager clusterManager, ReplicationMode mode) {
        this.clusterManager = clusterManager;
        this.mode = mode;
    }
    
    public DistributedSession createSession() {
        String sessionId = UUID.randomUUID().toString();
        String nodeId = clusterManager.getCurrentNodeId();
        DistributedSession session = new DistributedSession(sessionId, nodeId);
        
        localSessions.put(sessionId, session);
        replicateSession(session);
        
        return session;
    }
    
    public void saveSession(DistributedSession session) {
        localSessions.put(session.getId(), session);
        
        if (mode == ReplicationMode.SYNCHRONOUS) {
            replicateSessionSync(session);
        } else {
            replicateSessionAsync(session);
        }
    }
    
    private void replicateSession(DistributedSession session) {
        Collection<ClusterNode> activeNodes = clusterManager.getActiveNodes();
        
        for (ClusterNode node : activeNodes) {
            if (!node.getNodeId().equals(clusterManager.getCurrentNodeId())) {
                replicateToNode(session, node);
                session.addReplicaNode(node.getNodeId());
            }
        }
    }
    
    private void replicateSessionSync(DistributedSession session) {
        System.out.println("Synchronous replication: " + session.getId());
        replicateSession(session);
    }
    
    private void replicateSessionAsync(DistributedSession session) {
        System.out.println("Asynchronous replication: " + session.getId());
        // In real implementation, use ExecutorService
        new Thread(() -> replicateSession(session)).start();
    }
    
    private void replicateToNode(DistributedSession session, ClusterNode node) {
        // Simulate network replication
        System.out.println("  Replicating to node: " + node.getNodeId());
    }
    
    public DistributedSession getSession(String sessionId) {
        return localSessions.get(sessionId);
    }
    
    public void removeSession(String sessionId) {
        DistributedSession session = localSessions.remove(sessionId);
        if (session != null) {
            removeReplicatedSession(session);
        }
    }
    
    private void removeReplicatedSession(DistributedSession session) {
        session.getReplicaNodeIds().forEach(nodeId -> {
            System.out.println("  Removing from replica: " + nodeId);
        });
    }
    
    enum ReplicationMode {
        SYNCHRONOUS,  // Wait for replication to complete
        ASYNCHRONOUS  // Fire and forget
    }
}

// 5. STICKY SESSION LOAD BALANCER
class StickySessionLoadBalancer {
    
    private final ClusterManager clusterManager;
    private final Map<String, String> sessionToNodeMapping = new ConcurrentHashMap<>();
    private int roundRobinIndex = 0;
    
    public StickySessionLoadBalancer(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }
    
    public ClusterNode routeRequest(String sessionId) {
        // Check if session has existing node assignment
        String nodeId = sessionToNodeMapping.get(sessionId);
        
        if (nodeId != null) {
            ClusterNode node = clusterManager.getNode(nodeId);
            if (node != null && node.isHealthy(30000)) {
                return node; // Route to sticky node
            } else {
                // Node failed, reassign
                sessionToNodeMapping.remove(sessionId);
            }
        }
        
        // No assignment or node failed, use round-robin
        ClusterNode node = selectNodeRoundRobin();
        if (node != null) {
            sessionToNodeMapping.put(sessionId, node.getNodeId());
        }
        return node;
    }
    
    private ClusterNode selectNodeRoundRobin() {
        List<ClusterNode> activeNodes = new ArrayList<>(clusterManager.getActiveNodes());
        if (activeNodes.isEmpty()) {
            return null;
        }
        
        ClusterNode node = activeNodes.get(roundRobinIndex % activeNodes.size());
        roundRobinIndex++;
        return node;
    }
    
    public void removeSessionMapping(String sessionId) {
        sessionToNodeMapping.remove(sessionId);
    }
    
    public Map<String, String> getSessionMappings() {
        return new HashMap<>(sessionToNodeMapping);
    }
}

// 6. SESSION FAILOVER HANDLER
class SessionFailoverHandler {
    
    private final SessionReplicationManager replicationManager;
    private final StickySessionLoadBalancer loadBalancer;
    
    public SessionFailoverHandler(SessionReplicationManager replicationManager,
                                 StickySessionLoadBalancer loadBalancer) {
        this.replicationManager = replicationManager;
        this.loadBalancer = loadBalancer;
    }
    
    public DistributedSession handleFailover(String sessionId) {
        System.out.println("Handling failover for session: " + sessionId);
        
        // Try to retrieve from replica
        DistributedSession session = replicationManager.getSession(sessionId);
        
        if (session == null) {
            System.out.println("  Session not found in replicas, creating new session");
            return replicationManager.createSession();
        }
        
        // Reassign to new node
        ClusterNode newNode = loadBalancer.routeRequest(sessionId);
        if (newNode != null) {
            session.setPrimaryNodeId(newNode.getNodeId());
            System.out.println("  Session reassigned to: " + newNode.getNodeId());
        }
        
        return session;
    }
}

/**
 * DEMONSTRATION
 */
public class DistributedSessionPatterns {
    
    public static void main(String[] args) {
        System.out.println("=== DISTRIBUTED SESSION PATTERNS ===\n");
        
        // Setup cluster
        ClusterManager clusterManager = new ClusterManager();
        
        ClusterNode node1 = new ClusterNode("node-1", "192.168.1.1", 8080);
        ClusterNode node2 = new ClusterNode("node-2", "192.168.1.2", 8080);
        ClusterNode node3 = new ClusterNode("node-3", "192.168.1.3", 8080);
        
        clusterManager.registerNode(node1);
        clusterManager.registerNode(node2);
        clusterManager.registerNode(node3);
        
        System.out.println("1. CLUSTER SETUP:");
        clusterManager.getActiveNodes().forEach(node -> 
            System.out.println("   " + node)
        );
        System.out.println();
        
        // Session replication
        System.out.println("2. SESSION REPLICATION:");
        SessionReplicationManager replicationManager = new SessionReplicationManager(
            clusterManager, 
            SessionReplicationManager.ReplicationMode.SYNCHRONOUS
        );
        
        DistributedSession session = replicationManager.createSession();
        session.setAttribute("userId", 100L);
        session.setAttribute("username", "alice");
        replicationManager.saveSession(session);
        
        System.out.println("   Primary node: " + session.getPrimaryNodeId());
        System.out.println("   Replica nodes: " + session.getReplicaNodeIds().size());
        System.out.println();
        
        // Sticky session
        System.out.println("3. STICKY SESSION ROUTING:");
        StickySessionLoadBalancer loadBalancer = new StickySessionLoadBalancer(clusterManager);
        
        String sessionId = session.getId();
        ClusterNode target1 = loadBalancer.routeRequest(sessionId);
        ClusterNode target2 = loadBalancer.routeRequest(sessionId);
        
        System.out.println("   First request: " + target1);
        System.out.println("   Second request: " + target2);
        System.out.println("   Sticky: " + target1.getNodeId().equals(target2.getNodeId()));
        System.out.println();
        
        // Failover
        System.out.println("4. SESSION FAILOVER:");
        SessionFailoverHandler failoverHandler = new SessionFailoverHandler(
            replicationManager, 
            loadBalancer
        );
        
        // Simulate node failure
        node1.setStatus(ClusterNode.NodeStatus.FAILED);
        DistributedSession recovered = failoverHandler.handleFailover(sessionId);
        System.out.println("   Recovered session: " + recovered.getId());
        System.out.println("   New primary: " + recovered.getPrimaryNodeId());
        System.out.println();
        
        System.out.println("Configuration Examples:");
        System.out.println("\nNginx Sticky Session:");
        System.out.println("   upstream backend {");
        System.out.println("      ip_hash;");
        System.out.println("      server 192.168.1.1:8080;");
        System.out.println("      server 192.168.1.2:8080;");
        System.out.println("   }");
        System.out.println();
        
        System.out.println("HAProxy Sticky Session:");
        System.out.println("   backend servers");
        System.out.println("      balance roundrobin");
        System.out.println("      cookie SERVERID insert indirect nocache");
        System.out.println("      server node1 192.168.1.1:8080 cookie node1");
        System.out.println();
        
        System.out.println("Best Practices:");
        System.out.println("   ✓ Use session replication for high availability");
        System.out.println("   ✓ Implement sticky sessions for performance");
        System.out.println("   ✓ Monitor cluster health regularly");
        System.out.println("   ✓ Handle failover gracefully");
        System.out.println("   ✓ Minimize session data size");
    }
}
