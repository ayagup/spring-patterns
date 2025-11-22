# API Design Patterns - Spring Implementation

Comprehensive collection of 14 API Design Patterns implemented in Spring, demonstrating best practices for designing robust, scalable, and maintainable REST APIs.

## Table of Contents

1. [RESTful API Pattern](#1-restful-api-pattern)
2. [HATEOAS Pattern](#2-hateoas-pattern)
3. [Richardson Maturity Model](#3-richardson-maturity-model)
4. [Resource-Oriented Pattern](#4-resource-oriented-pattern)
5. [API Versioning Pattern](#5-api-versioning-pattern)
6. [Pagination Pattern](#6-pagination-pattern)
7. [Filtering Pattern](#7-filtering-pattern)
8. [Sorting Pattern](#8-sorting-pattern)
9. [Searching Pattern](#9-searching-pattern)
10. [Batch Request Pattern](#10-batch-request-pattern)
11. [GraphQL Integration Pattern](#11-graphql-integration-pattern)
12. [OpenAPI/Swagger Pattern](#12-openapiswagger-pattern)
13. [API Composition Pattern](#13-api-composition-pattern)
14. [API Gateway Aggregation Pattern](#14-api-gateway-aggregation-pattern)

---

## 1. RESTful API Pattern

### Purpose
Implement REST architectural constraints for building scalable, stateless web services using standard HTTP methods and status codes.

### Key Components
- Resource representations (JSON/XML)
- HTTP methods (GET, POST, PUT, PATCH, DELETE)
- Proper HTTP status codes
- Stateless communication
- URI-based resource identification

### Implementation Example
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product created = productService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED)
            .header("Location", "/api/products/" + created.getId())
            .body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.update(id, product)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
```

### HTTP Status Codes
- **200 OK**: Successful GET, PUT, PATCH
- **201 Created**: Successful POST
- **204 No Content**: Successful DELETE
- **400 Bad Request**: Validation error
- **404 Not Found**: Resource doesn't exist
- **500 Internal Server Error**: Server error

### Best Practices
✓ Use nouns for resources, not verbs  
✓ Use plural nouns for collections (/products, not /product)  
✓ Return appropriate HTTP status codes  
✓ Include Location header for created resources  
✓ Keep APIs stateless  
✓ Version your API from the start

---

## 2. HATEOAS Pattern

### Purpose
Hypermedia as the Engine of Application State - clients interact with application entirely through hypermedia provided dynamically by servers.

### Key Components
- Resource representations with embedded links
- Link relations (self, next, prev, collection)
- HAL (Hypertext Application Language) format
- Affordances (available actions)
- Resource assemblers

### Implementation Example
```java
public class OrderResourceAssembler {
    
    public OrderResource toResource(Order order) {
        OrderResource resource = new OrderResource(order);
        
        // Self link
        resource.add(Link.of("/api/orders/" + order.getId()).withSelfRel());
        
        // Collection link
        resource.add(Link.of("/api/orders").withRel("collection"));
        
        // State-based links (affordances)
        if ("PENDING".equals(order.getStatus())) {
            resource.add(Link.of("/api/orders/" + order.getId() + "/process").withRel("process"));
            resource.add(Link.of("/api/orders/" + order.getId() + "/cancel").withRel("cancel"));
        } else if ("SHIPPED".equals(order.getStatus())) {
            resource.add(Link.of("/api/orders/" + order.getId() + "/deliver").withRel("deliver"));
        }
        
        return resource;
    }
}
```

### HAL Format Example
```json
{
  "orderId": 1,
  "customerName": "John Doe",
  "status": "PENDING",
  "totalAmount": 299.99,
  "_links": {
    "self": { "href": "/api/orders/1" },
    "collection": { "href": "/api/orders" },
    "process": { "href": "/api/orders/1/process" },
    "cancel": { "href": "/api/orders/1/cancel" },
    "customer": { "href": "/api/customers/john-doe" }
  }
}
```

### Benefits
✓ Discoverability - clients discover available actions through links  
✓ Loose Coupling - clients don't hardcode URIs  
✓ Evolvability - server can change URIs without breaking clients  
✓ State Transitions - links represent valid state transitions  
✓ Self-Documenting - available actions are clear from the response

---

## 3. Richardson Maturity Model

### Purpose
Defines 4 levels of REST maturity, from basic HTTP to full HATEOAS.

### Maturity Levels

#### Level 0: The Swamp of POX (Plain Old XML)
- Single URI, single HTTP method (usually POST)
- RPC-style, tunneling everything through HTTP
```
POST /api
{ "action": "getProducts" }
```

#### Level 1: Resources
- Multiple URIs, but single HTTP method
- Each resource has its own endpoint
```
POST /api/products/list
POST /api/products/get
POST /api/products/create
```

#### Level 2: HTTP Verbs
- Multiple URIs, multiple HTTP methods
- Proper use of GET, POST, PUT, DELETE
- HTTP status codes used correctly
```
GET    /api/products
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
```

#### Level 3: Hypermedia Controls (HATEOAS)
- Responses include links to related resources
- Self-describing, discoverable API
```json
{
  "id": 1,
  "name": "Product",
  "_links": {
    "self": "/api/products/1",
    "update": "/api/products/1",
    "delete": "/api/products/1"
  }
}
```

### Comparison Table
```
┌────────┬──────────────┬─────────────┬──────────────┬──────────┐
│ Level  │ URIs         │ HTTP Verbs  │ Status Codes │ HATEOAS  │
├────────┼──────────────┼─────────────┼──────────────┼──────────┤
│ 0      │ Single       │ POST only   │ Usually 200  │ No       │
│ 1      │ Multiple     │ POST only   │ Usually 200  │ No       │
│ 2      │ Multiple     │ GET/POST/   │ Proper codes │ No       │
│        │              │ PUT/DELETE  │              │          │
│ 3      │ Multiple     │ GET/POST/   │ Proper codes │ Yes      │
│        │              │ PUT/DELETE  │              │          │
└────────┴──────────────┴─────────────┴──────────────┴──────────┘
```

### Recommendations
✓ **Minimum**: Implement Level 2 for any RESTful API  
✓ **Ideal**: Implement Level 3 for public APIs  
✓ **Avoid**: Levels 0 and 1 for new APIs

---

## 4. Resource-Oriented Pattern

### Purpose
Design APIs around resources with proper URI structure.

### URI Design Principles

#### Good Resource URIs
```
✓ /v1/users                      - Users collection
✓ /v1/users/123                  - Specific user
✓ /v1/users/123/posts            - User's posts (sub-resource)
✓ /v1/users/123/posts/456        - Specific post
✓ /v1/organizations/1/users      - Organization's users
```

#### Bad Resource URIs
```
✗ /v1/getAllUsers                - Verb in URI
✗ /v1/user                       - Singular for collection
✗ /v1/users/123/posts/456/       - Trailing slash
   comments/789/likes            - Too deep (>3 levels)
```

### Hierarchical Structure
```
Collection Resource:     /users
Instance Resource:       /users/{userId}
Sub-resource Collection: /users/{userId}/posts
Sub-resource Instance:   /users/{userId}/posts/{postId}
```

### Best Practices
✓ Use nouns for resources, not verbs  
✓ Use plural nouns for collections  
✓ Keep URLs lowercase with hyphens  
✓ Limit nesting to 2-3 levels maximum  
✓ Use query parameters for filtering, not URL paths

---

## 5. API Versioning Pattern

### Purpose
Manage API evolution without breaking existing clients.

### Versioning Strategies

#### 1. URI Versioning (Most Common)
```
GET /v1/users
GET /v2/users
```
**Pros**: Simple, explicit, cacheable  
**Cons**: URI changes per version

#### 2. Header Versioning
```
GET /api/users
X-API-Version: 2

or

Accept: application/vnd.api.v2+json
```
**Pros**: Clean URIs, flexible  
**Cons**: Less visible, harder to test

#### 3. Query Parameter Versioning
```
GET /api/users?version=2
```
**Pros**: Simple, explicit  
**Cons**: Pollutes query string

### Implementation Example
```java
// URI Versioning
@RestController
@RequestMapping("/v1")
public class UserControllerV1 {
    @GetMapping("/users")
    public List<UserV1> getUsers() { ... }
}

@RestController
@RequestMapping("/v2")
public class UserControllerV2 {
    @GetMapping("/users")
    public PagedResponse<UserV2> getUsers() { ... }
}

// Header Versioning
@GetMapping(value = "/users", headers = "X-API-Version=1")
public List<UserV1> getUsersV1() { ... }

@GetMapping(value = "/users", headers = "X-API-Version=2")
public PagedResponse<UserV2> getUsersV2() { ... }
```

### Best Practices
✓ Version from day one  
✓ Support at least 2 versions simultaneously  
✓ Deprecate old versions gracefully (6-12 months notice)  
✓ Document version differences clearly  
✓ Use semantic versioning (major.minor.patch)

---

## 6. Pagination Pattern

### Purpose
Enable efficient handling of large datasets by breaking results into pages.

### Pagination Strategies

#### 1. Offset-based Pagination (Traditional)
```
GET /api/users?page=0&size=20
```
```java
public PagedResponse<User> getUsers(int page, int size) {
    int start = page * size;
    int end = Math.min(start + size, total);
    List<User> pageContent = allUsers.subList(start, end);
    
    return new PagedResponse<>(pageContent, page, size, total);
}
```

**Response**:
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 1000,
  "totalPages": 50,
  "hasNext": true,
  "hasPrevious": false
}
```

**Pros**: Simple, can jump to any page  
**Cons**: Performance issues with large offsets, inconsistent results if data changes

#### 2. Cursor-based Pagination (Recommended)
```
GET /api/users?cursor=abc123&limit=20
```
```java
public PagedResponse<User> getUsersByCursor(String cursor, int limit) {
    List<User> allUsers = getAllUsersSorted();
    int startIndex = findIndexByCursor(cursor);
    int endIndex = Math.min(startIndex + limit, allUsers.size());
    
    List<User> pageContent = allUsers.subList(startIndex, endIndex);
    String nextCursor = encodeNextCursor(pageContent);
    
    PagedResponse<User> response = new PagedResponse<>(pageContent, limit);
    response.setNextCursor(nextCursor);
    return response;
}
```

**Response**:
```json
{
  "content": [ ... ],
  "limit": 20,
  "nextCursor": "def456",
  "hasNext": true
}
```

**Pros**: Consistent results, good performance, handles real-time data changes  
**Cons**: Can't jump to specific page, more complex implementation

#### 3. Keyset Pagination (Database-optimized)
```
GET /api/users?lastId=100&limit=20
```
```sql
SELECT * FROM users WHERE id > 100 ORDER BY id LIMIT 20
```

**Pros**: Very efficient with indexes, consistent results  
**Cons**: Requires sortable unique key

### Best Practices
✓ Use cursor pagination for large datasets  
✓ Provide default page size (10-50)  
✓ Set maximum page size (100-1000) to prevent abuse  
✓ Include pagination metadata in responses  
✓ Support both page-based and cursor-based for flexibility

---

## 7. Filtering Pattern

### Purpose
Enable clients to query and filter collections based on criteria.

### Filter Operators

```
eq   - Equals (price:eq:99.99)
ne   - Not equals (category:ne:Electronics)
gt   - Greater than (price:gt:100)
lt   - Less than (stock:lt:50)
gte  - Greater than or equal (price:gte:50)
lte  - Less than or equal (stock:lte:100)
in   - In list (category:in:Electronics,Furniture)
like - Contains (name:like:laptop)
```

### Implementation Examples

#### Simple Filtering
```
GET /api/products?category=Electronics&active=true&minPrice=50&maxPrice=200
```

```java
@GetMapping("/api/products")
public List<Product> getProducts(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice) {
    
    List<FilterCriteria> criteria = new ArrayList<>();
    if (category != null) criteria.add(new FilterCriteria("category", "eq", category));
    if (active != null) criteria.add(new FilterCriteria("active", "eq", active.toString()));
    if (minPrice != null) criteria.add(new FilterCriteria("price", "gte", minPrice.toString()));
    if (maxPrice != null) criteria.add(new FilterCriteria("price", "lte", maxPrice.toString()));
    
    return filterService.filter(criteria);
}
```

#### Advanced Filtering
```
GET /api/products/filter?filters=category:eq:Electronics,price:gte:100,price:lte:500
```

```java
private boolean matchesCriterion(Product product, FilterCriteria criterion) {
    String field = criterion.getField();
    String operator = criterion.getOperator();
    String value = criterion.getValue();
    
    switch (field) {
        case "price":
            return compareDouble(product.getPrice(), operator, Double.valueOf(value));
        case "category":
            return compareString(product.getCategory(), operator, value);
        case "name":
            if ("like".equals(operator)) {
                return product.getName().toLowerCase().contains(value.toLowerCase());
            }
            return compareString(product.getName(), operator, value);
    }
}
```

### Best Practices
✓ Use query parameters for simple filters  
✓ Support common operators: eq, ne, gt, lt, gte, lte, in, like  
✓ Validate filter values and provide clear error messages  
✓ Document available filter fields and operators  
✓ Consider performance: index filtered fields  
✓ Set limits on result sets to prevent abuse

---

## 8. Sorting Pattern

### Purpose
Enable clients to control the order of results.

### Sorting Examples

#### Single Field Sorting
```
GET /api/products?sort=price,asc
GET /api/products?sort=name,desc
```

#### Multi-field Sorting
```
GET /api/products?sort=category,asc&sort=price,desc
```

### Implementation
```java
public List<Product> sortProducts(List<Product> products, List<SortCriteria> sortCriteria) {
    Comparator<Product> comparator = null;
    
    for (SortCriteria criteria : sortCriteria) {
        Comparator<Product> fieldComparator = getComparator(criteria.getField());
        if ("desc".equalsIgnoreCase(criteria.getDirection())) {
            fieldComparator = fieldComparator.reversed();
        }
        
        comparator = (comparator == null) ? 
            fieldComparator : comparator.thenComparing(fieldComparator);
    }
    
    if (comparator != null) {
        products.sort(comparator);
    }
    
    return products;
}

private Comparator<Product> getComparator(String field) {
    switch (field) {
        case "name": return Comparator.comparing(Product::getName);
        case "price": return Comparator.comparing(Product::getPrice);
        case "createdAt": return Comparator.comparing(Product::getCreatedAt);
        default: return Comparator.comparing(Product::getId);
    }
}
```

### Best Practices
✓ Support multi-field sorting  
✓ Default sort order (e.g., by ID or createdAt)  
✓ Validate sort fields against allowed list  
✓ Index sorted fields for performance  
✓ Combine with filtering and pagination

---

## 9. Searching Pattern

### Purpose
Enable full-text search across multiple fields.

### Search Examples

#### Simple Search
```
GET /api/products/search?q=laptop
```

#### Advanced Search with Filters
```
GET /api/products/search?q=laptop&category=Electronics&minPrice=500
```

### Implementation
```java
public List<Product> searchProducts(String query) {
    String lowerQuery = query.toLowerCase();
    return products.values().stream()
        .filter(p -> 
            p.getName().toLowerCase().contains(lowerQuery) ||
            p.getDescription().toLowerCase().contains(lowerQuery) ||
            p.getCategory().toLowerCase().contains(lowerQuery) ||
            p.getBrand().toLowerCase().contains(lowerQuery)
        )
        .collect(Collectors.toList());
}
```

### Search with Relevance Scoring
```java
public class SearchResult {
    private Product product;
    private double relevanceScore;
    
    // Calculate score based on field matches
    private double calculateRelevance(Product product, String query) {
        double score = 0.0;
        if (product.getName().toLowerCase().contains(query)) score += 10.0;
        if (product.getDescription().toLowerCase().contains(query)) score += 5.0;
        if (product.getCategory().toLowerCase().contains(query)) score += 3.0;
        return score;
    }
}
```

### Best Practices
✓ Search across relevant fields (name, description, tags)  
✓ Implement relevance scoring  
✓ Support wildcards and partial matches  
✓ Consider using Elasticsearch for advanced search  
✓ Combine search with filtering and sorting  
✓ Implement search suggestions/autocomplete  
✓ Cache frequent search queries

---

## 10. Batch Request Pattern

### Purpose
Allow clients to send multiple operations in a single HTTP request to reduce network overhead.

### Batch Request Format
```json
{
  "atomic": false,
  "parallel": true,
  "operations": [
    {
      "id": "op1",
      "method": "GET",
      "path": "/items/1"
    },
    {
      "id": "op2",
      "method": "POST",
      "path": "/items",
      "body": {
        "name": "New Item",
        "description": "Item description"
      }
    },
    {
      "id": "op3",
      "method": "DELETE",
      "path": "/items/3"
    }
  ]
}
```

### Batch Response Format
```json
{
  "success": true,
  "executionTimeMs": 150,
  "results": [
    {
      "id": "op1",
      "status": 200,
      "body": { "id": 1, "name": "Item 1" }
    },
    {
      "id": "op2",
      "status": 201,
      "body": { "id": 4, "name": "New Item" }
    },
    {
      "id": "op3",
      "status": 204,
      "body": null
    }
  ]
}
```

### Implementation
```java
public BatchResponse processBatch(BatchRequest batchRequest) {
    List<OperationResponse> results;
    
    if (batchRequest.isParallel()) {
        // Parallel execution
        results = batchRequest.getOperations().stream()
            .map(operation -> CompletableFuture.supplyAsync(
                () -> processOperation(operation), executor
            ))
            .collect(Collectors.toList())
            .stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    } else {
        // Sequential execution
        results = batchRequest.getOperations().stream()
            .map(this::processOperation)
            .collect(Collectors.toList());
    }
    
    // Handle atomic transactions
    if (batchRequest.isAtomic()) {
        boolean anyFailed = results.stream().anyMatch(r -> r.getStatus() >= 400);
        if (anyFailed) {
            // Rollback all operations
            return rollbackBatch(results);
        }
    }
    
    return new BatchResponse(results, true);
}
```

### Best Practices
✓ Limit batch size (e.g., max 100 operations)  
✓ Support both sequential and parallel execution  
✓ Provide atomic transaction option  
✓ Return HTTP 207 Multi-Status for partial success  
✓ Include operation IDs for request/response correlation  
✓ Set timeout for batch execution  
✓ Validate operations before execution  
✓ Consider rate limiting per batch

---

## 11. GraphQL Integration Pattern

### Purpose
Provide flexible data querying where clients specify exact data requirements.

### GraphQL Schema
```graphql
type Query {
  user(id: ID!): User
  users: [User]
  post(id: ID!): Post
}

type Mutation {
  createUser(input: UserInput!): User
  createPost(input: PostInput!): Post
}

type User {
  id: ID!
  name: String!
  email: String!
  posts: [Post]
}

type Post {
  id: ID!
  title: String!
  content: String!
  author: User
}
```

### GraphQL Queries

#### Query Single User
```graphql
{
  user(id: 1) {
    id
    name
    email
  }
}
```

#### Nested Query (User with Posts)
```graphql
{
  user(id: 1) {
    id
    name
    posts {
      id
      title
      content
    }
  }
}
```

#### Mutation
```graphql
mutation CreateUser($input: UserInput!) {
  createUser(input: $input) {
    id
    name
    email
  }
}
```

### GraphQL Advantages
✓ Client specifies exact fields needed (no over-fetching)  
✓ Single endpoint for all queries  
✓ Nested queries in single request  
✓ Strongly typed schema  
✓ Introspection for documentation  
✓ Real-time subscriptions

### When to Use GraphQL
- Complex, nested data requirements
- Mobile apps (minimize data transfer)
- Aggregating multiple data sources
- Rapidly evolving client requirements

---

## 12. OpenAPI/Swagger Pattern

### Purpose
Provide standardized API documentation and interactive API explorer.

### OpenAPI Specification (3.0)
```yaml
openapi: 3.0.0
info:
  title: User API
  version: 1.0.0
  description: API for managing users

paths:
  /api/users:
    get:
      summary: Get all users
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/User'
    post:
      summary: Create user
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UserInput'
      responses:
        '201':
          description: User created

components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: integer
        name:
          type: string
        email:
          type: string
```

### Spring Integration
```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management APIs")
public class UserController {
    
    @Operation(summary = "Get all users", description = "Returns list of all users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }
    
    @Operation(summary = "Create user")
    @PostMapping
    public ResponseEntity<User> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User to create",
                required = true
            )
            @RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```

### OpenAPI Advantages
✓ Standardized API documentation (OpenAPI 3.0)  
✓ Interactive API explorer (Swagger UI)  
✓ Code generation for clients and servers  
✓ Request/response validation  
✓ API testing and mocking  
✓ Language-agnostic specification

---

## 13. API Composition Pattern

### Purpose
Aggregate data from multiple microservices into unified responses.

### Parallel Composition
```java
public CompletableFuture<UserProfile> getUserProfile(Long userId) {
    CompletableFuture<UserData> userFuture = userService.getUserAsync(userId);
    CompletableFuture<List<OrderData>> ordersFuture = orderService.getUserOrdersAsync(userId);
    CompletableFuture<Map<String, Object>> prefsFuture = prefsService.getUserPreferencesAsync(userId);
    
    // Combine all futures in parallel
    return CompletableFuture.allOf(userFuture, ordersFuture, prefsFuture)
        .thenApply(v -> {
            UserProfile profile = new UserProfile();
            profile.setUser(userFuture.join());
            profile.setOrders(ordersFuture.join());
            profile.setPreferences(prefsFuture.join());
            return profile;
        });
}
```

### Sequential Composition
```java
public CompletableFuture<OrderSummary> getOrderSummary(Long orderId) {
    return orderService.getOrderAsync(orderId)
        .thenCompose(order -> {
            // Get user based on order's userId
            CompletableFuture<UserData> userFuture = userService.getUserAsync(order.getUserId());
            CompletableFuture<PaymentData> paymentFuture = paymentService.getPaymentAsync(orderId);
            
            return CompletableFuture.allOf(userFuture, paymentFuture)
                .thenApply(v -> {
                    OrderSummary summary = new OrderSummary();
                    summary.setOrder(order);
                    summary.setUser(userFuture.join());
                    summary.setPayment(paymentFuture.join());
                    return summary;
                });
        });
}
```

### Fallback Composition
```java
public UserProfile getUserProfileWithFallback(Long userId) {
    UserProfile profile = new UserProfile();
    
    // Required data
    profile.setUser(userService.getUser(userId));
    
    // Optional data with fallback
    try {
        profile.setOrders(orderService.getUserOrders(userId));
    } catch (Exception e) {
        profile.setOrders(new ArrayList<>()); // Fallback to empty list
    }
    
    try {
        profile.setPreferences(prefsService.getPreferences(userId));
    } catch (Exception e) {
        profile.setPreferences(getDefaultPreferences()); // Fallback to defaults
    }
    
    return profile;
}
```

### Best Practices
✓ Use parallel calls when services are independent  
✓ Set timeouts for all service calls  
✓ Implement circuit breakers for downstream services  
✓ Provide fallback values for non-critical data  
✓ Cache aggregated responses when possible

---

## 14. API Gateway Aggregation Pattern

### Purpose
Central entry point for all client requests with routing and aggregation capabilities.

### Gateway Responsibilities

1. **Request Routing**
   - Route to appropriate microservice
   - Path-based routing
   - Header-based routing

2. **Request Aggregation**
   - Combine multiple service calls
   - Parallel execution
   - Response merging

3. **Cross-cutting Concerns**
   - Authentication & Authorization
   - Rate Limiting
   - Logging & Monitoring
   - Caching
   - Circuit Breaking
   - Load Balancing

### Implementation Example
```java
public class ApiGateway {
    
    public CompletableFuture<OrderSummary> getOrderSummary(Long orderId) {
        return orderService.getOrderAsync(orderId)
            .thenCompose(order -> {
                CompletableFuture<UserData> userFuture = userService.getUserAsync(order.getUserId());
                CompletableFuture<PaymentData> paymentFuture = paymentService.getPaymentAsync(orderId);
                
                return CompletableFuture.allOf(userFuture, paymentFuture)
                    .thenApply(v -> createOrderSummary(order, userFuture.join(), paymentFuture.join()));
            });
    }
    
    public Object routeRequest(String path, Map<String, String> params) {
        if (path.startsWith("/users/")) {
            return handleUserRequest(path, params);
        } else if (path.startsWith("/orders/")) {
            return handleOrderRequest(path, params);
        } else if (path.startsWith("/products/")) {
            return handleProductRequest(path, params);
        }
        throw new NotFoundException("Resource not found");
    }
}
```

### Gateway Pattern Benefits
✓ Single entry point for all clients  
✓ Reduces client-to-service communication  
✓ Simplifies client code  
✓ Centralized security and monitoring  
✓ Protocol translation (REST → gRPC)  
✓ Backend for Frontend (BFF) pattern

### Best Practices
✓ Use established gateway frameworks (Spring Cloud Gateway, Kong, etc.)  
✓ Implement circuit breakers and timeouts  
✓ Cache responses where appropriate  
✓ Monitor gateway performance  
✓ Use bulkhead pattern to isolate failures  
✓ Implement rate limiting per client  
✓ Keep gateway stateless

---

## Dependencies

### Maven Dependencies
```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring HATEOAS -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-hateoas</artifactId>
    </dependency>
    
    <!-- SpringDoc OpenAPI (Swagger) -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-ui</artifactId>
        <version>1.7.0</version>
    </dependency>
    
    <!-- GraphQL (Optional) -->
    <dependency>
        <groupId>com.graphql-java</groupId>
        <artifactId>graphql-spring-boot-starter</artifactId>
        <version>11.1.0</version>
    </dependency>
    
    <!-- Spring Cloud Gateway (Optional) -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
</dependencies>
```

---

## Best Practices Summary

### API Design
✓ Follow REST principles (Level 2+ maturity)  
✓ Use meaningful resource names  
✓ Version your API from the start  
✓ Provide comprehensive documentation  
✓ Use standard HTTP status codes  
✓ Implement HATEOAS for public APIs

### Performance
✓ Implement pagination for large datasets  
✓ Use cursor-based pagination for real-time data  
✓ Cache responses where appropriate  
✓ Use parallel composition for independent services  
✓ Implement bulk/batch operations  
✓ Index database fields used in filtering/sorting

### Reliability
✓ Implement circuit breakers  
✓ Set timeouts for all external calls  
✓ Provide fallback values  
✓ Use retry logic with exponential backoff  
✓ Handle partial failures gracefully  
✓ Monitor and alert on errors

### Security
✓ Implement authentication and authorization  
✓ Use HTTPS for all endpoints  
✓ Validate all input data  
✓ Implement rate limiting  
✓ Use API keys or OAuth 2.0  
✓ Sanitize error messages

### Documentation
✓ Use OpenAPI/Swagger for documentation  
✓ Provide examples for all endpoints  
✓ Document error responses  
✓ Keep documentation up-to-date  
✓ Provide SDK/client libraries  
✓ Include getting started guides

---

## Testing Strategies

### Unit Tests
```java
@Test
public void testGetProduct() {
    Product product = new Product(1L, "Laptop", "Electronics", 999.99, 10, "Dell", true);
    when(productService.findById(1L)).thenReturn(Optional.of(product));
    
    ResponseEntity<Product> response = controller.getProduct(1L);
    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Laptop", response.getBody().getName());
}
```

### Integration Tests
```java
@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testGetAllProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }
}
```

---

## Production Checklist

- [ ] API versioning implemented
- [ ] Authentication and authorization configured
- [ ] Rate limiting enabled
- [ ] CORS configured properly
- [ ] Input validation on all endpoints
- [ ] Error handling and meaningful error messages
- [ ] Pagination for large datasets
- [ ] Caching strategy implemented
- [ ] Logging and monitoring configured
- [ ] OpenAPI/Swagger documentation
- [ ] Performance testing completed
- [ ] Security audit passed
- [ ] Backup and recovery plan
- [ ] API deprecation policy defined

---

## References

- [RESTful API Design Best Practices](https://restfulapi.net/)
- [Richardson Maturity Model](https://martinfowler.com/articles/richardsonMaturityModel.html)
- [Spring HATEOAS Documentation](https://spring.io/projects/spring-hateoas)
- [OpenAPI Specification](https://swagger.io/specification/)
- [GraphQL Documentation](https://graphql.org/)
- [API Gateway Pattern](https://microservices.io/patterns/apigateway.html)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)

---

## License

MIT License - see LICENSE file for details
