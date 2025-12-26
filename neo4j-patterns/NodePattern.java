package com.example.neo4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Node Pattern
 *
 * Demonstrates the use of the @Node annotation to map Java objects to nodes in the Neo4j graph.
 * The @Node annotation is fundamental for defining the entities in your graph domain model.
 *
 * Key Features:
 * - @Node: Marks a class as a graph node entity.
 * - @Id: Specifies the primary key for the node. Can be internally generated (@GeneratedValue) or assigned.
 * - @Property: Customizes the mapping of a field to a node property (optional).
 * - Labels: By default, the simple class name is used as the node label. This can be customized (e.g., @Node("CustomLabel")).
 *
 * Use Cases:
 * - Defining the core entities of any graph-based domain model.
 * - Mapping legacy data models to a graph structure.
 * - Creating a clear and type-safe representation of graph nodes in Java.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class NodePattern {

    public static void main(String[] args) {
        SpringApplication.run(NodePattern.class, args);
    }
}

// Domain Models
@Node("Document") // Custom label for the node
class DocumentNode {
    @Id @GeneratedValue
    private Long id;

    @Property("document_title") // Custom property name in the graph
    private String title;

    private String author;
    private int version;
    private boolean published;

    // This field will not be persisted to the graph
    @Transient
    private String transientState;

    public DocumentNode() {}

    public DocumentNode(String title, String author, int version, boolean published) {
        this.title = title;
        this.author = author;
        this.version = version;
        this.published = published;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public String getTransientState() { return transientState; }
    public void setTransientState(String transientState) { this.transientState = transientState; }
}

@Node // Default label "Device" will be used
class DeviceNode {
    @Id // Using an assigned ID (e.g., a natural key like a MAC address)
    private String deviceId;

    private String model;
    private String manufacturer;

    public DeviceNode() {}

    public DeviceNode(String deviceId, String model, String manufacturer) {
        this.deviceId = deviceId;
        this.model = model;
        this.manufacturer = manufacturer;
    }

    // Getters and setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
}

// Repositories
@Repository
interface DocumentNodeRepository extends Neo4jRepository<DocumentNode, Long> {
    List<DocumentNode> findByAuthor(String author);
    List<DocumentNode> findByPublished(boolean published);
}

@Repository
interface DeviceNodeRepository extends Neo4jRepository<DeviceNode, String> {
    List<DeviceNode> findByManufacturer(String manufacturer);
}

// REST Controller
@RestController
@RequestMapping("/api/nodes")
class NodeController {

    private final DocumentNodeRepository documentRepo;
    private final DeviceNodeRepository deviceRepo;

    public NodeController(DocumentNodeRepository documentRepo, DeviceNodeRepository deviceRepo) {
        this.documentRepo = documentRepo;
        this.deviceRepo = deviceRepo;
    }

    // --- DocumentNode Endpoints ---

    @PostMapping("/documents")
    public DocumentNode createDocument(@RequestBody DocumentNode document) {
        return documentRepo.save(document);
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<DocumentNode> getDocument(@PathVariable Long id) {
        return documentRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/documents/by-author")
    public List<DocumentNode> getDocumentsByAuthor(@RequestParam String author) {
        return documentRepo.findByAuthor(author);
    }

    @GetMapping("/documents/published")
    public List<DocumentNode> getPublishedDocuments() {
        return documentRepo.findByPublished(true);
    }

    // --- DeviceNode Endpoints ---

    @PostMapping("/devices")
    public DeviceNode createDevice(@RequestBody DeviceNode device) {
        return deviceRepo.save(device);
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<DeviceNode> getDevice(@PathVariable String id) {
        return deviceRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/devices/by-manufacturer")
    public List<DeviceNode> getDevicesByManufacturer(@RequestParam String manufacturer) {
        return deviceRepo.findByManufacturer(manufacturer);
    }

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Node Pattern",
            "description", "Demonstrates mapping Java classes to graph nodes using @Node.",
            "features", "@Node with custom labels, @Id with generated and assigned values, @Property, @Transient.",
            "endpoints", "7 REST endpoints for CRUD operations on different node types."
        );
    }
}
