package com.example.api.graphql;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * GraphQL Integration & OpenAPI/Swagger Pattern
 * 
 * Purpose: Demonstrates GraphQL for flexible querying and OpenAPI for API documentation
 * 
 * GraphQL Features:
 * - Client specifies exact data requirements
 * - Single endpoint for all queries
 * - Strongly typed schema
 * - Nested queries and mutations
 * - No over-fetching or under-fetching
 * 
 * OpenAPI/Swagger Features:
 * - Standardized API documentation
 * - Interactive API explorer
 * - Code generation
 * - Schema validation
 * 
 * Note: This is a simplified implementation for demonstration
 */

// GraphQL Schema Representation (simplified)
class GraphQLSchema {
    private String schema;
    
    public GraphQLSchema() {
        this.schema = "type Query {\n" +
            "  user(id: ID!): User\n" +
            "  users: [User]\n" +
            "  post(id: ID!): Post\n" +
            "}\n\n" +
            "type Mutation {\n" +
            "  createUser(input: UserInput!): User\n" +
            "  createPost(input: PostInput!): Post\n" +
            "}\n\n" +
            "type User {\n" +
            "  id: ID!\n" +
            "  name: String!\n" +
            "  email: String!\n" +
            "  posts: [Post]\n" +
            "}\n\n" +
            "type Post {\n" +
            "  id: ID!\n" +
            "  title: String!\n" +
            "  content: String!\n" +
            "  author: User\n" +
            "}\n\n" +
            "input UserInput {\n" +
            "  name: String!\n" +
            "  email: String!\n" +
            "}\n\n" +
            "input PostInput {\n" +
            "  userId: ID!\n" +
            "  title: String!\n" +
            "  content: String!\n" +
            "}";
    }
    
    public String getSchema() { return schema; }
}

// GraphQL Request/Response Models
class GraphQLRequest {
    private String query;
    private Map<String, Object> variables;
    private String operationName;
    
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
    
    public String getOperationName() { return operationName; }
    public void setOperationName(String operationName) { this.operationName = operationName; }
}

class GraphQLResponse {
    private Object data;
    private List<GraphQLError> errors;
    
    public GraphQLResponse(Object data) {
        this.data = data;
    }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    
    public List<GraphQLError> getErrors() { return errors; }
    public void setErrors(List<GraphQLError> errors) { this.errors = errors; }
}

class GraphQLError {
    private String message;
    private String path;
    
    public GraphQLError(String message, String path) {
        this.message = message;
        this.path = path;
    }
    
    public String getMessage() { return message; }
    public String getPath() { return path; }
}

// Domain Models
class UserGraphQL {
    private Long id;
    private String name;
    private String email;
    
    public UserGraphQL(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}

class PostGraphQL {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    
    public PostGraphQL(Long id, Long userId, String title, String content) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
    }
    
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
}

// GraphQL Resolver (Simplified)
class GraphQLResolver {
    private final Map<Long, UserGraphQL> users = new ConcurrentHashMap<>();
    private final Map<Long, PostGraphQL> posts = new ConcurrentHashMap<>();
    private Long nextUserId = 1L;
    private Long nextPostId = 1L;
    
    public GraphQLResolver() {
        // Sample data
        users.put(nextUserId, new UserGraphQL(nextUserId++, "Alice", "alice@example.com"));
        users.put(nextUserId, new UserGraphQL(nextUserId++, "Bob", "bob@example.com"));
        
        posts.put(nextPostId, new PostGraphQL(nextPostId++, 1L, "GraphQL Basics", "Introduction to GraphQL"));
        posts.put(nextPostId, new PostGraphQL(nextPostId++, 1L, "Advanced GraphQL", "Advanced topics"));
        posts.put(nextPostId, new PostGraphQL(nextPostId++, 2L, "REST vs GraphQL", "Comparison"));
    }
    
    public Object executeQuery(GraphQLRequest request) {
        String query = request.getQuery().trim();
        
        // Simple query parsing (real implementation would use a proper parser)
        if (query.contains("user(id:")) {
            Long id = extractId(query, "user(id:");
            return Map.of("user", getUserWithPosts(id));
        } else if (query.contains("users")) {
            List<Map<String, Object>> userList = users.values().stream()
                .map(this::userToMap)
                .collect(Collectors.toList());
            return Map.of("users", userList);
        } else if (query.contains("createUser")) {
            Map<String, Object> input = (Map<String, Object>) request.getVariables().get("input");
            UserGraphQL user = new UserGraphQL(nextUserId++, 
                (String) input.get("name"), 
                (String) input.get("email"));
            users.put(user.getId(), user);
            return Map.of("createUser", userToMap(user));
        }
        
        return Map.of("error", "Query not recognized");
    }
    
    private Map<String, Object> getUserWithPosts(Long userId) {
        UserGraphQL user = users.get(userId);
        if (user == null) return null;
        
        Map<String, Object> result = userToMap(user);
        
        List<Map<String, Object>> userPosts = posts.values().stream()
            .filter(p -> p.getUserId().equals(userId))
            .map(this::postToMap)
            .collect(Collectors.toList());
        
        result.put("posts", userPosts);
        return result;
    }
    
    private Map<String, Object> userToMap(UserGraphQL user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        return map;
    }
    
    private Map<String, Object> postToMap(PostGraphQL post) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", post.getId());
        map.put("title", post.getTitle());
        map.put("content", post.getContent());
        map.put("userId", post.getUserId());
        return map;
    }
    
    private Long extractId(String query, String prefix) {
        int start = query.indexOf(prefix) + prefix.length();
        int end = query.indexOf(")", start);
        String idStr = query.substring(start, end).trim();
        return Long.parseLong(idStr);
    }
}

// GraphQL Controller
@RestController
@RequestMapping("/graphql")
class GraphQLController {
    private final GraphQLResolver resolver = new GraphQLResolver();
    private final GraphQLSchema schema = new GraphQLSchema();
    
    @PostMapping
    public ResponseEntity<GraphQLResponse> executeGraphQL(@RequestBody GraphQLRequest request) {
        try {
            Object result = resolver.executeQuery(request);
            GraphQLResponse response = new GraphQLResponse(result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GraphQLResponse response = new GraphQLResponse(null);
            response.setErrors(Arrays.asList(new GraphQLError(e.getMessage(), "query")));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @GetMapping("/schema")
    public ResponseEntity<String> getSchema() {
        return ResponseEntity.ok(schema.getSchema());
    }
}

// OpenAPI/Swagger Models
class OpenAPISpec {
    private String openapi = "3.0.0";
    private Info info;
    private Map<String, Path> paths;
    private Map<String, Schema> components;
    
    public OpenAPISpec() {
        this.info = new Info("User API", "1.0.0", "API for managing users");
        this.paths = new HashMap<>();
        this.components = new HashMap<>();
        
        // Define paths
        Path userPath = new Path();
        userPath.addOperation("get", new Operation("Get all users", "Returns list of users"));
        userPath.addOperation("post", new Operation("Create user", "Creates a new user"));
        paths.put("/api/users", userPath);
        
        // Define schemas
        Schema userSchema = new Schema("User");
        userSchema.addProperty("id", "integer", "User ID");
        userSchema.addProperty("name", "string", "User name");
        userSchema.addProperty("email", "string", "User email");
        components.put("User", userSchema);
    }
    
    public String getOpenapi() { return openapi; }
    public Info getInfo() { return info; }
    public Map<String, Path> getPaths() { return paths; }
    public Map<String, Schema> getComponents() { return components; }
}

class Info {
    private String title;
    private String version;
    private String description;
    
    public Info(String title, String version, String description) {
        this.title = title;
        this.version = version;
        this.description = description;
    }
    
    public String getTitle() { return title; }
    public String getVersion() { return version; }
    public String getDescription() { return description; }
}

class Path {
    private Map<String, Operation> operations = new HashMap<>();
    
    public void addOperation(String method, Operation operation) {
        operations.put(method, operation);
    }
    
    public Map<String, Operation> getOperations() { return operations; }
}

class Operation {
    private String summary;
    private String description;
    
    public Operation(String summary, String description) {
        this.summary = summary;
        this.description = description;
    }
    
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
}

class Schema {
    private String type = "object";
    private Map<String, Property> properties = new HashMap<>();
    
    public Schema(String type) {
        this.type = type;
    }
    
    public void addProperty(String name, String type, String description) {
        properties.put(name, new Property(type, description));
    }
    
    public String getType() { return type; }
    public Map<String, Property> getProperties() { return properties; }
}

class Property {
    private String type;
    private String description;
    
    public Property(String type, String description) {
        this.type = type;
        this.description = description;
    }
    
    public String getType() { return type; }
    public String getDescription() { return description; }
}

// OpenAPI Controller
@RestController
@RequestMapping("/api-docs")
class OpenAPIController {
    private final OpenAPISpec spec = new OpenAPISpec();
    
    @GetMapping("/openapi.json")
    public ResponseEntity<OpenAPISpec> getOpenAPISpec() {
        return ResponseEntity.ok(spec);
    }
}

/**
 * Demonstration of GraphQL and OpenAPI Patterns
 */
public class GraphQLAndOpenAPIPattern {
    
    public static void main(String[] args) {
        System.out.println("=== GraphQL Pattern Demo ===\n");
        
        GraphQLResolver resolver = new GraphQLResolver();
        
        System.out.println("1. GraphQL Schema:");
        GraphQLSchema schema = new GraphQLSchema();
        System.out.println(schema.getSchema());
        
        System.out.println("\n2. Query Single User:");
        GraphQLRequest request1 = new GraphQLRequest();
        request1.setQuery("{ user(id: 1) { id name email } }");
        Object result1 = resolver.executeQuery(request1);
        System.out.println("   Result: " + result1);
        
        System.out.println("\n3. Query User with Posts (Nested):");
        GraphQLRequest request2 = new GraphQLRequest();
        request2.setQuery("{ user(id: 1) { id name posts { id title } } }");
        Object result2 = resolver.executeQuery(request2);
        System.out.println("   Result: " + result2);
        
        System.out.println("\n4. Query Multiple Users:");
        GraphQLRequest request3 = new GraphQLRequest();
        request3.setQuery("{ users { id name email } }");
        Object result3 = resolver.executeQuery(request3);
        System.out.println("   Result: " + result3);
        
        System.out.println("\n5. Mutation - Create User:");
        GraphQLRequest request4 = new GraphQLRequest();
        request4.setQuery("mutation CreateUser($input: UserInput!) { createUser(input: $input) { id name email } }");
        Map<String, Object> variables = new HashMap<>();
        Map<String, Object> input = new HashMap<>();
        input.put("name", "Charlie");
        input.put("email", "charlie@example.com");
        variables.put("input", input);
        request4.setVariables(variables);
        Object result4 = resolver.executeQuery(request4);
        System.out.println("   Result: " + result4);
        
        System.out.println("\n=== GraphQL Advantages ===");
        System.out.println("✓ Client specifies exact fields needed (no over-fetching)");
        System.out.println("✓ Single endpoint for all queries");
        System.out.println("✓ Nested queries in single request");
        System.out.println("✓ Strongly typed schema");
        System.out.println("✓ Introspection for documentation");
        System.out.println("✓ Real-time subscriptions");
        
        System.out.println("\n=== OpenAPI/Swagger Pattern Demo ===\n");
        
        OpenAPISpec openapi = new OpenAPISpec();
        
        System.out.println("OpenAPI Specification:");
        System.out.println("  Version: " + openapi.getOpenapi());
        System.out.println("  Title: " + openapi.getInfo().getTitle());
        System.out.println("  API Version: " + openapi.getInfo().getVersion());
        System.out.println("  Description: " + openapi.getInfo().getDescription());
        
        System.out.println("\n  Paths:");
        openapi.getPaths().forEach((path, pathItem) -> {
            System.out.println("    " + path);
            pathItem.getOperations().forEach((method, operation) -> {
                System.out.println("      " + method.toUpperCase() + ": " + operation.getSummary());
            });
        });
        
        System.out.println("\n  Schemas:");
        openapi.getComponents().forEach((name, schemaObj) -> {
            System.out.println("    " + name + ":");
            schemaObj.getProperties().forEach((propName, prop) -> {
                System.out.println("      - " + propName + " (" + prop.getType() + "): " + prop.getDescription());
            });
        });
        
        System.out.println("\n=== OpenAPI/Swagger Advantages ===");
        System.out.println("✓ Standardized API documentation (OpenAPI 3.0)");
        System.out.println("✓ Interactive API explorer (Swagger UI)");
        System.out.println("✓ Code generation for clients and servers");
        System.out.println("✓ Request/response validation");
        System.out.println("✓ API testing and mocking");
        System.out.println("✓ Language-agnostic specification");
        
        System.out.println("\n=== When to Use ===");
        System.out.println("GraphQL:");
        System.out.println("  ✓ Complex, nested data requirements");
        System.out.println("  ✓ Mobile apps (minimize data transfer)");
        System.out.println("  ✓ Aggregating multiple data sources");
        System.out.println("  ✓ Rapidly evolving client requirements");
        
        System.out.println("\nOpenAPI/Swagger:");
        System.out.println("  ✓ RESTful API documentation");
        System.out.println("  ✓ Public APIs requiring clear documentation");
        System.out.println("  ✓ Code generation needs");
        System.out.println("  ✓ Contract-first API development");
    }
}
