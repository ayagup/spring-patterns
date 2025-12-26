package com.example.neo4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Neo4j Repository Pattern
 * 
 * Demonstrates the use of Spring Data Neo4j repositories for graph data access.
 * Repositories provide a high-level abstraction over Neo4j, simplifying CRUD operations
 * and enabling query derivation from method names.
 * 
 * Key Features:
 * - CRUD operations out of the box
 * - Query derivation from method names
 * - Custom Cypher queries with @Query annotation
 * - Pagination and sorting support
 * - Transaction management integration
 * 
 * Use Cases:
 * - Standard data access layers for graph-based applications
 * - Rapid development of APIs for graph data
 * - Domain-driven design with graph entities
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class Neo4jRepositoryPattern {

    public static void main(String[] args) {
        SpringApplication.run(Neo4jRepositoryPattern.class, args);
    }
}

// Domain Models
@Node("User")
class User {
    @Id @GeneratedValue
    private Long id;
    private String username;
    private String email;
    
    @Relationship(type = "FOLLOWS")
    private Set<User> following = new HashSet<>();

    public User() {}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Set<User> getFollowing() { return following; }
    public void setFollowing(Set<User> following) { this.following = following; }
}

// Repository Interface
@Repository
interface UserRepository extends Neo4jRepository<User, Long> {

    /**
     * Derived query: find a user by their username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Derived query: find users with emails matching a pattern.
     */
    List<User> findByEmailContaining(String pattern);

    /**
     * Custom Cypher query: find followers of a user.
     */
    @Query("MATCH (u:User)-[:FOLLOWS]->(target:User) WHERE id(target) = $userId RETURN u")
    Collection<User> findFollowers(@Param("userId") Long userId);

    /**
     * Custom Cypher query: find suggested users to follow (not already following).
     */
    @Query("""
        MATCH (currentUser:User), (otherUser:User)
        WHERE id(currentUser) = $userId AND id(otherUser) <> $userId
        AND NOT (currentUser)-[:FOLLOWS]->(otherUser)
        RETURN otherUser
        LIMIT $limit
    """)
    Collection<User> suggestUsersToFollow(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * Custom Cypher query with projection: get a count of followers for a user.
     */
    @Query("MATCH (:User)-[:FOLLOWS]->(u:User) WHERE id(u) = $userId RETURN count(*) as followerCount")
    Map<String, Object> getFollowerCount(@Param("userId") Long userId);
    
    /**
     * Derived query: count users by email domain.
     */
    long countByEmailEndsWith(String domain);
}

// Service Layer
@Service
class UserGraphService {
    
    private final UserRepository userRepository;

    public UserGraphService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findByEmailPattern(String pattern) {
        return userRepository.findByEmailContaining(pattern);
    }

    public void followUser(Long followerId, Long followedId) {
        userRepository.findById(followerId).ifPresent(follower -> {
            userRepository.findById(followedId).ifPresent(followed -> {
                follower.getFollowing().add(followed);
                userRepository.save(follower);
            });
        });
    }

    public Collection<User> getFollowers(Long userId) {
        return userRepository.findFollowers(userId);
    }


    public Collection<User> suggestUsers(Long userId, int limit) {
        return userRepository.suggestUsersToFollow(userId, limit);
    }

    public Map<String, Object> countFollowers(Long userId) {
        return userRepository.getFollowerCount(userId);
    }
    
    public long countUsersByDomain(String domain) {
        return userRepository.countByEmailEndsWith(domain);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}

// REST Controller
@RestController
@RequestMapping("/api/neo4j-repository")
class UserGraphController {
    
    private final UserGraphService service;

    public UserGraphController(UserGraphService service) {
        this.service = service;
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(service.saveUser(user));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return service.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return service.findByUsername(username)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/email")
    public ResponseEntity<List<User>> findUsersByEmail(@RequestParam String pattern) {
        return ResponseEntity.ok(service.findByEmailPattern(pattern));
    }

    @PostMapping("/users/{followerId}/follow/{followedId}")
    public ResponseEntity<Void> followUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        service.followUser(followerId, followedId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}/followers")
    public ResponseEntity<Collection<User>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getFollowers(userId));
    }

    @GetMapping("/users/{userId}/suggestions")
    public ResponseEntity<Collection<User>> getSuggestions(@PathVariable Long userId, @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(service.suggestUsers(userId, limit));
    }

    @GetMapping("/users/{userId}/follower-count")
    public ResponseEntity<Map<String, Object>> getFollowerCount(@PathVariable Long userId) {
        return ResponseEntity.ok(service.countFollowers(userId));
    }
    
    @GetMapping("/users/count-by-domain")
    public ResponseEntity<Long> countByDomain(@RequestParam String domain) {
        return ResponseEntity.ok(service.countUsersByDomain(domain));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getInfo() {
        return ResponseEntity.ok(Map.of(
            "pattern", "Neo4j Repository Pattern",
            "description", "High-level graph data access using Spring Data Neo4j repositories",
            "features", "CRUD, derived queries, custom @Query, projections",
            "endpoints", "10 REST endpoints for user and relationship management"
        ));
    }
}
