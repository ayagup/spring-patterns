package com.example.neo4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Neo4j Template Pattern
 * 
 * Demonstrates the use of Neo4jTemplate for direct Cypher queries and graph operations.
 * Neo4jTemplate provides a low-level API for executing custom queries and operations
 * on Neo4j graph database without repository abstractions.
 * 
 * Key Features:
 * - Direct Cypher query execution
 * - Node and relationship CRUD operations
 * - Custom projections and result mapping
 * - Transaction management
 * - Flexible query building
 * 
 * Use Cases:
 * - Complex graph traversals
 * - Custom aggregations
 * - Performance-critical queries
 * - Dynamic query construction
 * - Bulk operations
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class Neo4jTemplatePattern {

    public static void main(String[] args) {
        SpringApplication.run(Neo4jTemplatePattern.class, args);
    }
}

// Domain Models
@Node
class Person {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private Integer age;
    private String email;
    
    @Relationship(type = "KNOWS")
    private Set<Person> friends = new HashSet<>();
    
    @Relationship(type = "WORKS_AT")
    private Company company;

    public Person() {}
    
    public Person(String name, Integer age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Set<Person> getFriends() { return friends; }
    public void setFriends(Set<Person> friends) { this.friends = friends; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
}

@Node
class Company {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String industry;
    private Integer employeeCount;

    public Company() {}
    
    public Company(String name, String industry, Integer employeeCount) {
        this.name = name;
        this.industry = industry;
        this.employeeCount = employeeCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public Integer getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(Integer employeeCount) { this.employeeCount = employeeCount; }
}

// Service Layer
@Service
class PersonGraphService {
    
    private final Neo4jTemplate neo4jTemplate;

    public PersonGraphService(Neo4jTemplate neo4jTemplate) {
        this.neo4jTemplate = neo4jTemplate;
    }

    /**
     * Save node using template
     */
    public Person savePerson(Person person) {
        return neo4jTemplate.save(person);
    }

    /**
     * Find by ID
     */
    public Optional<Person> findById(Long id) {
        return Optional.ofNullable(neo4jTemplate.findById(id, Person.class));
    }

    /**
     * Custom Cypher query - find by age range
     */
    public Collection<Person> findPeopleByAgeRange(int minAge, int maxAge) {
        String cypher = "MATCH (p:Person) WHERE p.age >= $minAge AND p.age <= $maxAge RETURN p";
        Map<String, Object> params = Map.of("minAge", minAge, "maxAge", maxAge);
        return neo4jTemplate.findAll(cypher, params, Person.class);
    }

    /**
     * Create relationship
     */
    public void createFriendship(Long personId1, Long personId2) {
        String cypher = """
            MATCH (p1:Person), (p2:Person)
            WHERE id(p1) = $id1 AND id(p2) = $id2
            MERGE (p1)-[:KNOWS]->(p2)
            """;
        Map<String, Object> params = Map.of("id1", personId1, "id2", personId2);
        neo4jTemplate.findAll(cypher, params, Person.class);
    }

    /**
     * Find friends of friends
     */
    public Collection<Person> findFriendsOfFriends(Long personId) {
        String cypher = """
            MATCH (p:Person)-[:KNOWS]->(:Person)-[:KNOWS]->(fof:Person)
            WHERE id(p) = $personId AND id(fof) <> $personId
            RETURN DISTINCT fof
            """;
        return neo4jTemplate.findAll(cypher, Map.of("personId", personId), Person.class);
    }

    /**
     * Count nodes
     */
    public long countPeople() {
        return neo4jTemplate.count(Person.class);
    }

    /**
     * Delete by ID
     */
    public void deletePerson(Long id) {
        neo4jTemplate.deleteById(id, Person.class);
    }

    /**
     * Custom projection query
     */
    public Collection<Map<String, Object>> getPersonStatistics() {
        String cypher = """
            MATCH (p:Person)
            RETURN 
                AVG(p.age) as avgAge,
                MIN(p.age) as minAge,
                MAX(p.age) as maxAge,
                COUNT(p) as totalPeople
            """;
        return neo4jTemplate.findAll(cypher, Map.of(), Map.class);
    }

    /**
     * Find shortest path
     */
    public Collection<Map<String, Object>> findShortestPath(Long fromId, Long toId) {
        String cypher = """
            MATCH path = shortestPath((from:Person)-[:KNOWS*]-(to:Person))
            WHERE id(from) = $fromId AND id(to) = $toId
            RETURN path
            """;
        return neo4jTemplate.findAll(cypher, 
            Map.of("fromId", fromId, "toId", toId), Map.class);
    }

    /**
     * Bulk update
     */
    public void updateAgeByCompany(String companyName, int ageIncrement) {
        String cypher = """
            MATCH (p:Person)-[:WORKS_AT]->(c:Company {name: $companyName})
            SET p.age = p.age + $increment
            RETURN count(p) as updated
            """;
        neo4jTemplate.findAll(cypher, 
            Map.of("companyName", companyName, "increment", ageIncrement), 
            Map.class);
    }
}

// REST Controller
@RestController
@RequestMapping("/api/neo4j-template")
class PersonGraphController {
    
    private final PersonGraphService service;

    public PersonGraphController(PersonGraphService service) {
        this.service = service;
    }

    @PostMapping("/persons")
    public ResponseEntity<Person> createPerson(@RequestBody Person person) {
        return ResponseEntity.ok(service.savePerson(person));
    }

    @GetMapping("/persons/{id}")
    public ResponseEntity<Person> getPerson(@PathVariable Long id) {
        return service.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/persons/age-range")
    public ResponseEntity<Collection<Person>> findByAgeRange(
            @RequestParam int minAge, 
            @RequestParam int maxAge) {
        return ResponseEntity.ok(service.findPeopleByAgeRange(minAge, maxAge));
    }

    @PostMapping("/persons/{id1}/friend/{id2}")
    public ResponseEntity<Void> createFriendship(
            @PathVariable Long id1, 
            @PathVariable Long id2) {
        service.createFriendship(id1, id2);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/persons/{id}/fof")
    public ResponseEntity<Collection<Person>> getFriendsOfFriends(@PathVariable Long id) {
        return ResponseEntity.ok(service.findFriendsOfFriends(id));
    }

    @GetMapping("/persons/count")
    public ResponseEntity<Long> countPeople() {
        return ResponseEntity.ok(service.countPeople());
    }

    @DeleteMapping("/persons/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        service.deletePerson(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/persons/statistics")
    public ResponseEntity<Collection<Map<String, Object>>> getStatistics() {
        return ResponseEntity.ok(service.getPersonStatistics());
    }

    @GetMapping("/persons/shortest-path")
    public ResponseEntity<Collection<Map<String, Object>>> getShortestPath(
            @RequestParam Long from, 
            @RequestParam Long to) {
        return ResponseEntity.ok(service.findShortestPath(from, to));
    }

    @PutMapping("/persons/update-age")
    public ResponseEntity<Void> updateAgeByCompany(
            @RequestParam String companyName, 
            @RequestParam int increment) {
        service.updateAgeByCompany(companyName, increment);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getInfo() {
        return ResponseEntity.ok(Map.of(
            "pattern", "Neo4j Template Pattern",
            "description", "Direct Cypher queries using Neo4jTemplate",
            "features", "Custom queries, projections, relationships, bulk operations",
            "endpoints", "10 REST endpoints for graph operations"
        ));
    }
}
