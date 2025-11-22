package com.example.api.resource;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Resource-Oriented Pattern & API Versioning & Pagination Pattern
 * 
 * Purpose: Design APIs around resources with proper URI structure,
 * versioning strategies, and pagination for large datasets.
 * 
 * Key Components:
 * 1. Resource-Oriented Design:
 *    - Resources as nouns (not verbs)
 *    - Hierarchical URI structure
 *    - Collection and instance resources
 *    - Sub-resources for relationships
 * 
 * 2. API Versioning:
 *    - URI versioning: /v1/users, /v2/users
 *    - Header versioning: Accept: application/vnd.api.v1+json
 *    - Query parameter: /users?version=1
 * 
 * 3. Pagination:
 *    - Offset-based: page=1&size=20
 *    - Cursor-based: cursor=abc123&limit=20
 *    - Keyset pagination: id>100&limit=20
 * 
 * Features:
 * - Intuitive resource naming
 * - Nested resources for relationships
 * - Multiple versioning strategies
 * - Efficient pagination for large datasets
 */

// User Entity
class User {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    
    public User() {}
    
    public User(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

// Post Entity (sub-resource of User)
class Post {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private LocalDateTime publishedAt;
    
    public Post() {}
    
    public Post(Long id, Long userId, String title, String content) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.publishedAt = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public LocalDateTime getPublishedAt() { return publishedAt; }
}

// Pagination Response
class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private String nextCursor;
    
    public PagedResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.hasNext = page < totalPages - 1;
        this.hasPrevious = page > 0;
    }
    
    // Getters
    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isHasNext() { return hasNext; }
    public boolean isHasPrevious() { return hasPrevious; }
    public String getNextCursor() { return nextCursor; }
    
    public void setNextCursor(String nextCursor) { this.nextCursor = nextCursor; }
}

// Resource Service
class ResourceService {
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Map<Long, Post> posts = new ConcurrentHashMap<>();
    private Long nextUserId = 1L;
    private Long nextPostId = 1L;
    
    public ResourceService() {
        // Initialize sample data
        for (int i = 1; i <= 50; i++) {
            User user = new User(nextUserId++, "user" + i, "user" + i + "@example.com");
            users.put(user.getId(), user);
            
            // Create posts for each user
            for (int j = 1; j <= 3; j++) {
                Post post = new Post(nextPostId++, user.getId(), 
                    "Post " + j + " by " + user.getUsername(),
                    "Content of post " + j);
                posts.put(post.getId(), post);
            }
        }
    }
    
    // Offset-based pagination
    public PagedResponse<User> getUsers(int page, int size) {
        List<User> allUsers = new ArrayList<>(users.values());
        int start = page * size;
        int end = Math.min(start + size, allUsers.size());
        
        List<User> pageContent = start < allUsers.size() ? 
            allUsers.subList(start, end) : new ArrayList<>();
        
        return new PagedResponse<>(pageContent, page, size, allUsers.size());
    }
    
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }
    
    // Get sub-resource: user's posts
    public PagedResponse<Post> getUserPosts(Long userId, int page, int size) {
        List<Post> userPosts = posts.values().stream()
            .filter(p -> p.getUserId().equals(userId))
            .collect(Collectors.toList());
        
        int start = page * size;
        int end = Math.min(start + size, userPosts.size());
        
        List<Post> pageContent = start < userPosts.size() ? 
            userPosts.subList(start, end) : new ArrayList<>();
        
        return new PagedResponse<>(pageContent, page, size, userPosts.size());
    }
    
    public Optional<Post> getUserPost(Long userId, Long postId) {
        Post post = posts.get(postId);
        if (post != null && post.getUserId().equals(userId)) {
            return Optional.of(post);
        }
        return Optional.empty();
    }
    
    // Cursor-based pagination
    public PagedResponse<User> getUsersByCursor(String cursor, int limit) {
        List<User> allUsers = new ArrayList<>(users.values());
        allUsers.sort(Comparator.comparing(User::getId));
        
        int startIndex = 0;
        if (cursor != null && !cursor.isEmpty()) {
            Long cursorId = Long.parseLong(cursor);
            for (int i = 0; i < allUsers.size(); i++) {
                if (allUsers.get(i).getId() > cursorId) {
                    startIndex = i;
                    break;
                }
            }
        }
        
        int endIndex = Math.min(startIndex + limit, allUsers.size());
        List<User> pageContent = allUsers.subList(startIndex, endIndex);
        
        PagedResponse<User> response = new PagedResponse<>(pageContent, 0, limit, allUsers.size());
        if (endIndex < allUsers.size()) {
            response.setNextCursor(String.valueOf(pageContent.get(pageContent.size() - 1).getId()));
        }
        
        return response;
    }
}

/**
 * Version 1 API - Resource-Oriented with Basic Features
 */
@RestController
@RequestMapping("/v1")
class ResourceControllerV1 {
    private final ResourceService service = new ResourceService();
    
    // Collection resource: GET /v1/users
    @GetMapping("/users")
    public ResponseEntity<PagedResponse<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<User> response = service.getUsers(page, size);
        return ResponseEntity.ok(response);
    }
    
    // Instance resource: GET /v1/users/{userId}
    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        return service.getUserById(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Sub-resource collection: GET /v1/users/{userId}/posts
    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<PagedResponse<Post>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        PagedResponse<Post> response = service.getUserPosts(userId, page, size);
        return ResponseEntity.ok(response);
    }
    
    // Sub-resource instance: GET /v1/users/{userId}/posts/{postId}
    @GetMapping("/users/{userId}/posts/{postId}")
    public ResponseEntity<Post> getUserPost(
            @PathVariable Long userId,
            @PathVariable Long postId) {
        return service.getUserPost(userId, postId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}

/**
 * Version 2 API - Enhanced with Cursor Pagination
 */
@RestController
@RequestMapping("/v2")
class ResourceControllerV2 {
    private final ResourceService service = new ResourceService();
    
    // Cursor-based pagination: GET /v2/users?cursor=xyz&limit=20
    @GetMapping("/users")
    public ResponseEntity<PagedResponse<User>> getUsers(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit) {
        PagedResponse<User> response = service.getUsersByCursor(cursor, limit);
        return ResponseEntity.ok()
            .header("X-Next-Cursor", response.getNextCursor())
            .body(response);
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        return service.getUserById(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}

/**
 * Header-based Versioning Controller
 */
@RestController
@RequestMapping("/api")
class VersionedByHeaderController {
    private final ResourceService service = new ResourceService();
    
    // Accept: application/vnd.api.v1+json
    @GetMapping(value = "/users", headers = "X-API-Version=1")
    public ResponseEntity<List<User>> getUsersV1() {
        return ResponseEntity.ok(
            service.getUsers(0, 100).getContent()
        );
    }
    
    // Accept: application/vnd.api.v2+json
    @GetMapping(value = "/users", headers = "X-API-Version=2")
    public ResponseEntity<PagedResponse<User>> getUsersV2(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(
            service.getUsersByCursor(cursor, limit)
        );
    }
}

/**
 * Demonstration of Resource-Oriented, Versioning, and Pagination Patterns
 */
public class ResourceOrientedAndPaginationPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Resource-Oriented API Design ===\n");
        
        ResourceService service = new ResourceService();
        
        System.out.println("1. Collection Resources (nouns, not verbs):");
        System.out.println("   ✓ GET /v1/users             - List users");
        System.out.println("   ✓ POST /v1/users            - Create user");
        System.out.println("   ✗ GET /v1/getUsers          - Wrong (verb)");
        System.out.println("   ✗ POST /v1/createUser       - Wrong (verb)");
        
        System.out.println("\n2. Instance Resources:");
        System.out.println("   ✓ GET /v1/users/123         - Get user");
        System.out.println("   ✓ PUT /v1/users/123         - Update user");
        System.out.println("   ✓ DELETE /v1/users/123      - Delete user");
        
        System.out.println("\n3. Sub-Resources (hierarchical):");
        System.out.println("   ✓ GET /v1/users/123/posts            - User's posts");
        System.out.println("   ✓ GET /v1/users/123/posts/456        - Specific post");
        System.out.println("   ✓ POST /v1/users/123/posts           - Create post for user");
        System.out.println("   ✓ GET /v1/users/123/posts/456/comments - Post's comments");
        
        System.out.println("\n=== API Versioning Strategies ===\n");
        
        System.out.println("1. URI Versioning (Most Common):");
        System.out.println("   /v1/users - Version 1");
        System.out.println("   /v2/users - Version 2");
        System.out.println("   Pros: Simple, explicit, cacheable");
        System.out.println("   Cons: URI changes per version");
        
        System.out.println("\n2. Header Versioning:");
        System.out.println("   GET /api/users");
        System.out.println("   X-API-Version: 1");
        System.out.println("   or");
        System.out.println("   Accept: application/vnd.api.v1+json");
        System.out.println("   Pros: Clean URIs, flexible");
        System.out.println("   Cons: Less visible, harder to test");
        
        System.out.println("\n3. Query Parameter Versioning:");
        System.out.println("   /api/users?version=1");
        System.out.println("   Pros: Simple, explicit");
        System.out.println("   Cons: Pollutes query string");
        
        System.out.println("\n=== Pagination Strategies ===\n");
        
        System.out.println("1. Offset-based Pagination (Traditional):");
        System.out.println("   GET /v1/users?page=0&size=10");
        PagedResponse<User> page1 = service.getUsers(0, 10);
        System.out.println("   Page: " + page1.getPage());
        System.out.println("   Size: " + page1.getSize());
        System.out.println("   Total: " + page1.getTotalElements());
        System.out.println("   Total Pages: " + page1.getTotalPages());
        System.out.println("   Has Next: " + page1.isHasNext());
        System.out.println("   Pros: Simple, can jump to any page");
        System.out.println("   Cons: Performance issues with large offsets");
        System.out.println("   Cons: Inconsistent results if data changes");
        
        System.out.println("\n2. Cursor-based Pagination (Recommended):");
        System.out.println("   GET /v2/users?cursor=abc123&limit=10");
        PagedResponse<User> cursor1 = service.getUsersByCursor(null, 10);
        System.out.println("   Items: " + cursor1.getContent().size());
        System.out.println("   Next Cursor: " + cursor1.getNextCursor());
        System.out.println("   Pros: Consistent results, good performance");
        System.out.println("   Pros: Handles real-time data changes");
        System.out.println("   Cons: Can't jump to specific page");
        System.out.println("   Cons: More complex implementation");
        
        System.out.println("\n3. Keyset Pagination (Database-optimized):");
        System.out.println("   GET /v1/users?lastId=100&limit=10");
        System.out.println("   SQL: SELECT * FROM users WHERE id > 100 LIMIT 10");
        System.out.println("   Pros: Very efficient with indexes");
        System.out.println("   Pros: Consistent results");
        System.out.println("   Cons: Requires sortable unique key");
        
        System.out.println("\n=== Best Practices ===");
        System.out.println("✓ Use nouns for resources, not verbs");
        System.out.println("✓ Use plural nouns for collections (/users, not /user)");
        System.out.println("✓ Keep URLs lowercase with hyphens (/user-profiles)");
        System.out.println("✓ Limit nesting to 2-3 levels maximum");
        System.out.println("✓ Version your API from the start");
        System.out.println("✓ Use cursor pagination for large datasets");
        System.out.println("✓ Include pagination metadata in responses");
        System.out.println("✓ Provide default page size (e.g., 10-50)");
        System.out.println("✓ Set maximum page size (e.g., 100) to prevent abuse");
        
        System.out.println("\n=== URI Structure Examples ===");
        System.out.println("Good Resource URIs:");
        System.out.println("  /v1/users                      - Users collection");
        System.out.println("  /v1/users/123                  - Specific user");
        System.out.println("  /v1/users/123/posts            - User's posts");
        System.out.println("  /v1/posts?userId=123           - Alternative: filter");
        System.out.println("  /v1/users/123/posts/456        - Specific post");
        System.out.println("  /v1/organizations/1/users      - Org's users");
        System.out.println("\nBad Resource URIs:");
        System.out.println("  ✗ /v1/getAllUsers              - Verb in URI");
        System.out.println("  ✗ /v1/user                     - Singular for collection");
        System.out.println("  ✗ /v1/users/123/posts/456/comments/789/likes - Too deep");
    }
}
