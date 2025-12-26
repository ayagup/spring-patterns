package com.example.neo4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Graph Traversal Pattern
 *
 * Demonstrates advanced graph traversal techniques using custom Cypher queries.
 * This pattern focuses on queries that explore the graph's structure, such as
 * finding paths, identifying neighbors, and performing complex traversals.
 *
 * Key Features:
 * - Variable-length path traversals (e.g., `-[*..5]->`).
 * - Shortest path algorithms (`shortestPath()`).
 * - Finding nodes at a specific depth or distance.
 * - Combining traversals with filtering and aggregation.
 *
 * Use Cases:
 * - Social network analysis (e.g., finding connection paths between two people).
 * - Supply chain and logistics (e.g., tracing a product's journey).
 * - Recommendation engines (e.g., "users who bought this also bought...").
 * - Fraud detection (e.g., identifying suspicious rings of activity).
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class GraphTraversalPattern {

    public static void main(String[] args) {
        SpringApplication.run(GraphTraversalPattern.class, args);
    }
}

// Domain Models
@Node
class Location {
    @Id @GeneratedValue
    private Long id;
    private String name;

    public Location() {}
    public Location(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

@RelationshipProperties
class ConnectedTo {
    @Id @GeneratedValue
    private Long id;
    @TargetNode
    private Location location;
    private int distance;

    public ConnectedTo(Location location, int distance) {
        this.location = location;
        this.distance = distance;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public int getDistance() { return distance; }
    public void setDistance(int distance) { this.distance = distance; }
}

// Service Layer
@Service
class NavigationService {
    private final Neo4jTemplate neo4jTemplate;

    public NavigationService(Neo4jTemplate neo4jTemplate) {
        this.neo4jTemplate = neo4jTemplate;
    }

    public Location createLocation(String name) {
        return neo4jTemplate.save(new Location(name));
    }

    public void addConnection(String from, String to, int distance) {
        String cypher = """
            MATCH (l1:Location {name: $from}), (l2:Location {name: $to})
            MERGE (l1)-[:CONNECTED_TO {distance: $distance}]->(l2)
            """;
        neo4jTemplate.findAll(cypher, Map.of("from", from, "to", to, "distance", distance), Void.class);
    }

    /**
     * Find all locations reachable within a certain number of hops.
     */
    public Collection<Location> findReachableLocations(String startLocation, int maxHops) {
        String cypher = """
            MATCH (start:Location {name: $startLocation})-[:CONNECTED_TO*1..""" + maxHops + """]->(reachable)
            RETURN DISTINCT reachable
            """;
        return neo4jTemplate.findAll(cypher, Map.of("startLocation", startLocation), Location.class);
    }

    /**
     * Find the shortest path between two locations.
     */
    public List<Map<String, Object>> findShortestPath(String from, String to) {
        String cypher = """
            MATCH (from:Location {name: $from}), (to:Location {name: $to}),
                  path = shortestPath((from)-[:CONNECTED_TO*]-(to))
            RETURN path
            """;
        return neo4jTemplate.findAll(cypher, Map.of("from", from, "to", to), Map.class);
    }

    /**
     * Find the shortest path considering weights (distance).
     */
    public List<Map<String, Object>> findShortestPathByDistance(String from, String to) {
        String cypher = """
            MATCH (from:Location {name: $from}), (to:Location {name: $to})
            CALL gds.graph.project.cypher(
              'myGraph',
              'MATCH (n:Location) RETURN id(n) AS id',
              'MATCH (n1:Location)-[r:CONNECTED_TO]->(n2:Location) RETURN id(n1) AS source, id(n2) AS target, r.distance as weight'
            )
            YIELD graphName
            CALL gds.shortestPath.dijkstra.stream(graphName, {
              sourceNode: id(from),
              targetNode: id(to),
              relationshipWeightProperty: 'weight'
            })
            YIELD path
            RETURN path
            """;
        // Note: This requires the Graph Data Science library to be installed in Neo4j.
        // A simpler, non-GDS version would be more complex.
        // For demonstration, we'll use a simplified query that returns nodes and total distance.
        String simplifiedCypher = """
            MATCH path = (from:Location {name: $from})-[:CONNECTED_TO*]-(to:Location {name: $to})
            WITH path, reduce(total = 0, r IN relationships(path) | total + r.distance) AS totalDistance
            RETURN nodes(path) as locations, totalDistance
            ORDER BY totalDistance ASC
            LIMIT 1
            """;
        return neo4jTemplate.findAll(simplifiedCypher, Map.of("from", from, "to", to), Map.class);
    }

    /**
     * Find all neighbors of a location.
     */
    public Collection<Location> findNeighbors(String locationName) {
        String cypher = """
            MATCH (l:Location {name: $locationName})-[:CONNECTED_TO]-(neighbor)
            RETURN DISTINCT neighbor
            """;
        return neo4jTemplate.findAll(cypher, Map.of("locationName", locationName), Location.class);
    }
    
    /**
     * Find locations that are part of a cycle.
     */
    public Collection<Location> findCyclicalPaths(String locationName) {
        String cypher = """
            MATCH path = (start:Location {name: $locationName})-[:CONNECTED_TO*]->(start)
            UNWIND nodes(path) as nodeInCycle
            RETURN DISTINCT nodeInCycle
            """;
        return neo4jTemplate.findAll(cypher, Map.of("locationName", locationName), Location.class);
    }
}

// REST Controller
@RestController
@RequestMapping("/api/traversal")
class NavigationController {
    private final NavigationService service;

    public NavigationController(NavigationService service) {
        this.service = service;
    }

    @PostMapping("/locations")
    public Location createLocation(@RequestParam String name) {
        return service.createLocation(name);
    }

    @PostMapping("/connections")
    public ResponseEntity<Void> addConnection(@RequestParam String from, @RequestParam String to, @RequestParam int distance) {
        service.addConnection(from, to, distance);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reachable")
    public Collection<Location> getReachableLocations(@RequestParam String from, @RequestParam int maxHops) {
        return service.findReachableLocations(from, maxHops);
    }

    @GetMapping("/shortest-path")
    public List<Map<String, Object>> getShortestPath(@RequestParam String from, @RequestParam String to) {
        return service.findShortestPath(from, to);
    }

    @GetMapping("/shortest-path/by-distance")
    public List<Map<String, Object>> getShortestPathByDistance(@RequestParam String from, @RequestParam String to) {
        return service.findShortestPathByDistance(from, to);
    }

    @GetMapping("/neighbors")
    public Collection<Location> getNeighbors(@RequestParam String location) {
        return service.findNeighbors(location);
    }
    
    @GetMapping("/cycles")
    public Collection<Location> getCycles(@RequestParam String location) {
        return service.findCyclicalPaths(location);
    }

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Graph Traversal Pattern",
            "description", "Demonstrates advanced graph traversals like pathfinding and neighbor discovery.",
            "features", "Variable-length paths, shortestPath, weighted paths (Dijkstra concept).",
            "endpoints", "7 REST endpoints for exploring a location graph."
        );
    }
}
