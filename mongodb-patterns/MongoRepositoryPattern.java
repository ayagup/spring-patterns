package com.example.mongodb;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Mongo Repository Pattern
 * 
 * Demonstrates Spring Data MongoDB repository for simplified data access.
 * 
 * Repository Features:
 * - Automatic CRUD operations
 * - Query methods by method naming
 * - Custom queries with @Query
 * - Pagination and sorting
 * - Derived queries
 * - Count and exists queries
 * - Delete operations
 * 
 * Method Naming Patterns:
 * - findBy...
 * - findAllBy...
 * - countBy...
 * - deleteBy...
 * - existsBy...
 * - ...OrderBy...
 * - ...And...
 * - ...Or...
 * 
 * Use Cases:
 * - Simple CRUD operations
 * - Standard queries without custom code
 * - Pagination and sorting
 * - Type-safe query methods
 * - Automatic query derivation
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class MongoRepositoryPattern {
}

@Document(collection = "users")
record User(
    @Id String id,
    String username,
    String email,
    String firstName,
    String lastName,
    int age,
    String city,
    String country,
    boolean active,
    LocalDateTime registeredAt,
    LocalDateTime lastLoginAt
) {}

interface UserRepository extends MongoRepository<User, String> {
    
    // Find by single field
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    // Find by multiple fields
    List<User> findByFirstNameAndLastName(String firstName, String lastName);
    
    List<User> findByAgeGreaterThan(int age);
    
    List<User> findByAgeBetween(int minAge, int maxAge);
    
    // Find by boolean
    List<User> findByActive(boolean active);
    
    // Find by location
    List<User> findByCity(String city);
    
    List<User> findByCityAndCountry(String city, String country);
    
    // Find with ordering
    List<User> findByCountryOrderByAgeDesc(String country);
    
    List<User> findByActiveOrderByRegisteredAtDesc(boolean active);
    
    // Custom queries
    @Query("{'age': {$gte: ?0, $lte: ?1}}")
    List<User> findUsersInAgeRange(int minAge, int maxAge);
    
    @Query("{'email': {$regex: ?0, $options: 'i'}}")
    List<User> findByEmailPattern(String pattern);
    
    @Query("{'registeredAt': {$gte: ?0}}")
    List<User> findRegisteredAfter(LocalDateTime date);
    
    @Query(value = "{'active': true}", fields = "{'username': 1, 'email': 1}")
    List<User> findActiveUsersProjection();
    
    // Count queries
    long countByCountry(String country);
    
    long countByActive(boolean active);
    
    long countByAgeGreaterThan(int age);
    
    // Exists queries
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    // Delete queries
    long deleteByActive(boolean active);
    
    void deleteByCountry(String country);
    
    // Pagination
    Page<User> findByCountry(String country, Pageable pageable);
    
    Page<User> findByActive(boolean active, Pageable pageable);
    
    // Find all with sorting
    List<User> findByCity(String city, Sort sort);
}

@RestController
@RequestMapping("/api/mongo/users")
class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        return userRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getByEmail(@PathVariable String email) {
        return userRepository.findByEmail(email)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name")
    public ResponseEntity<List<User>> getByName(
            @RequestParam String firstName,
            @RequestParam String lastName) {
        return ResponseEntity.ok(
            userRepository.findByFirstNameAndLastName(firstName, lastName)
        );
    }

    @GetMapping("/age-greater/{age}")
    public ResponseEntity<List<User>> getByAgeGreater(@PathVariable int age) {
        return ResponseEntity.ok(userRepository.findByAgeGreaterThan(age));
    }

    @GetMapping("/age-range")
    public ResponseEntity<List<User>> getByAgeRange(
            @RequestParam int min,
            @RequestParam int max) {
        return ResponseEntity.ok(userRepository.findByAgeBetween(min, max));
    }

    @GetMapping("/active/{active}")
    public ResponseEntity<List<User>> getByActive(@PathVariable boolean active) {
        return ResponseEntity.ok(userRepository.findByActive(active));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<User>> getByCity(@PathVariable String city) {
        return ResponseEntity.ok(userRepository.findByCity(city));
    }

    @GetMapping("/location")
    public ResponseEntity<List<User>> getByLocation(
            @RequestParam String city,
            @RequestParam String country) {
        return ResponseEntity.ok(userRepository.findByCityAndCountry(city, country));
    }

    @GetMapping("/country/{country}/sorted")
    public ResponseEntity<List<User>> getByCountrySorted(@PathVariable String country) {
        return ResponseEntity.ok(userRepository.findByCountryOrderByAgeDesc(country));
    }

    @GetMapping("/active-sorted")
    public ResponseEntity<List<User>> getActiveSorted(@RequestParam boolean active) {
        return ResponseEntity.ok(
            userRepository.findByActiveOrderByRegisteredAtDesc(active)
        );
    }

    @GetMapping("/age-range-custom")
    public ResponseEntity<List<User>> getAgeRangeCustom(
            @RequestParam int min,
            @RequestParam int max) {
        return ResponseEntity.ok(userRepository.findUsersInAgeRange(min, max));
    }

    @GetMapping("/email-pattern")
    public ResponseEntity<List<User>> getByEmailPattern(@RequestParam String pattern) {
        return ResponseEntity.ok(userRepository.findByEmailPattern(pattern));
    }

    @GetMapping("/registered-after")
    public ResponseEntity<List<User>> getRegisteredAfter(@RequestParam String date) {
        LocalDateTime dateTime = LocalDateTime.parse(date);
        return ResponseEntity.ok(userRepository.findRegisteredAfter(dateTime));
    }

    @GetMapping("/active-projection")
    public ResponseEntity<List<User>> getActiveUsersProjection() {
        return ResponseEntity.ok(userRepository.findActiveUsersProjection());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countAll() {
        return ResponseEntity.ok(userRepository.count());
    }

    @GetMapping("/count/country/{country}")
    public ResponseEntity<Long> countByCountry(@PathVariable String country) {
        return ResponseEntity.ok(userRepository.countByCountry(country));
    }

    @GetMapping("/count/active/{active}")
    public ResponseEntity<Long> countByActive(@PathVariable boolean active) {
        return ResponseEntity.ok(userRepository.countByActive(active));
    }

    @GetMapping("/count/age-greater/{age}")
    public ResponseEntity<Long> countByAgeGreater(@PathVariable int age) {
        return ResponseEntity.ok(userRepository.countByAgeGreaterThan(age));
    }

    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> existsByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userRepository.existsByUsername(username));
    }

    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userRepository.existsByEmail(email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/inactive")
    public ResponseEntity<Long> deleteInactiveUsers() {
        long deleted = userRepository.deleteByActive(false);
        return ResponseEntity.ok(deleted);
    }

    @GetMapping("/country/{country}/page")
    public ResponseEntity<Page<User>> getByCountryPaged(
            @PathVariable String country,
            Pageable pageable) {
        return ResponseEntity.ok(userRepository.findByCountry(country, pageable));
    }

    @GetMapping("/active-page")
    public ResponseEntity<Page<User>> getActivePaged(
            @RequestParam boolean active,
            Pageable pageable) {
        return ResponseEntity.ok(userRepository.findByActive(active, pageable));
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getPatternInfo() {
        return ResponseEntity.ok(new PatternInfo(
            "Mongo Repository Pattern",
            "Simplified data access using Spring Data MongoDB Repository",
            "1.0",
            List.of("CRUD operations", "Query methods", "Custom queries", "Pagination", "Sorting"),
            List.of("Simple CRUD", "Standard queries", "Type-safe methods", "Automatic derivation")
        ));
    }

    record PatternInfo(String name, String description, String version, 
                      List<String> features, List<String> useCases) {}
}
