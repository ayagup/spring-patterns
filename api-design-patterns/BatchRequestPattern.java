package com.example.api.batch;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Batch Request Pattern
 * 
 * Purpose: Allow clients to send multiple operations in a single HTTP request
 * to reduce network overhead and improve performance.
 * 
 * Key Components:
 * 1. Batch request envelope
 * 2. Individual operation specifications
 * 3. Batch response with per-operation results
 * 4. Parallel execution support
 * 5. Transaction support (all-or-nothing)
 * 
 * Features:
 * - Multiple operations in single request
 * - Parallel processing
 * - Partial success handling
 * - Atomic transactions option
 * - Order preservation
 */

// Batch Request Models
class BatchRequest {
    private List<OperationRequest> operations;
    private boolean atomic; // All-or-nothing
    private boolean parallel; // Execute in parallel
    
    public BatchRequest() {}
    
    public List<OperationRequest> getOperations() { return operations; }
    public void setOperations(List<OperationRequest> operations) { this.operations = operations; }
    
    public boolean isAtomic() { return atomic; }
    public void setAtomic(boolean atomic) { this.atomic = atomic; }
    
    public boolean isParallel() { return parallel; }
    public void setParallel(boolean parallel) { this.parallel = parallel; }
}

class OperationRequest {
    private String id; // Operation identifier
    private String method; // GET, POST, PUT, DELETE, PATCH
    private String path; // Resource path
    private Map<String, Object> body; // Request body
    private Map<String, String> headers; // Custom headers
    
    public OperationRequest() {}
    
    public OperationRequest(String id, String method, String path) {
        this.id = id;
        this.method = method;
        this.path = path;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    
    public Map<String, Object> getBody() { return body; }
    public void setBody(Map<String, Object> body) { this.body = body; }
    
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
}

// Batch Response Models
class BatchResponse {
    private List<OperationResponse> results;
    private boolean success;
    private String message;
    private long executionTimeMs;
    
    public BatchResponse(List<OperationResponse> results, boolean success) {
        this.results = results;
        this.success = success;
    }
    
    public List<OperationResponse> getResults() { return results; }
    public void setResults(List<OperationResponse> results) { this.results = results; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
}

class OperationResponse {
    private String id; // Matches operation request ID
    private int status; // HTTP status code
    private Object body; // Response body
    private String error; // Error message if failed
    
    public OperationResponse(String id, int status, Object body) {
        this.id = id;
        this.status = status;
        this.body = body;
    }
    
    public OperationResponse(String id, int status, String error) {
        this.id = id;
        this.status = status;
        this.error = error;
    }
    
    public String getId() { return id; }
    public int getStatus() { return status; }
    public Object getBody() { return body; }
    public String getError() { return error; }
}

// Simple Entity for Demo
class Item {
    private Long id;
    private String name;
    private String description;
    
    public Item() {}
    
    public Item(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

// Batch Processor Service
class BatchProcessor {
    private final Map<Long, Item> items = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    public BatchProcessor() {
        // Initialize sample data
        items.put(nextId.get(), new Item(nextId.getAndIncrement(), "Item 1", "First item"));
        items.put(nextId.get(), new Item(nextId.getAndIncrement(), "Item 2", "Second item"));
        items.put(nextId.get(), new Item(nextId.getAndIncrement(), "Item 3", "Third item"));
    }
    
    public BatchResponse processBatch(BatchRequest batchRequest) {
        long startTime = System.currentTimeMillis();
        List<OperationResponse> results;
        boolean success = true;
        
        if (batchRequest.isParallel()) {
            results = processParallel(batchRequest);
        } else {
            results = processSequential(batchRequest);
        }
        
        // Check for failures in atomic mode
        if (batchRequest.isAtomic()) {
            boolean anyFailed = results.stream().anyMatch(r -> r.getStatus() >= 400);
            if (anyFailed) {
                success = false;
                // Rollback logic would go here
                results.forEach(r -> r.status = 500);
                for (OperationResponse r : results) {
                    r.error = "Transaction rolled back due to failure";
                }
            }
        }
        
        BatchResponse response = new BatchResponse(results, success);
        response.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        
        return response;
    }
    
    private List<OperationResponse> processSequential(BatchRequest batchRequest) {
        List<OperationResponse> results = new ArrayList<>();
        
        for (OperationRequest operation : batchRequest.getOperations()) {
            try {
                OperationResponse result = processOperation(operation);
                results.add(result);
            } catch (Exception e) {
                results.add(new OperationResponse(operation.getId(), 500, e.getMessage()));
            }
        }
        
        return results;
    }
    
    private List<OperationResponse> processParallel(BatchRequest batchRequest) {
        List<CompletableFuture<OperationResponse>> futures = batchRequest.getOperations().stream()
            .map(operation -> CompletableFuture.supplyAsync(
                () -> processOperation(operation), executor
            ))
            .collect(Collectors.toList());
        
        // Wait for all to complete
        return futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }
    
    private OperationResponse processOperation(OperationRequest operation) {
        try {
            String method = operation.getMethod().toUpperCase();
            String path = operation.getPath();
            
            // Parse resource ID from path (e.g., /items/123)
            Long resourceId = extractResourceId(path);
            
            switch (method) {
                case "GET":
                    return handleGet(operation.getId(), resourceId);
                case "POST":
                    return handlePost(operation.getId(), operation.getBody());
                case "PUT":
                    return handlePut(operation.getId(), resourceId, operation.getBody());
                case "DELETE":
                    return handleDelete(operation.getId(), resourceId);
                default:
                    return new OperationResponse(operation.getId(), 405, "Method not allowed");
            }
        } catch (Exception e) {
            return new OperationResponse(operation.getId(), 500, e.getMessage());
        }
    }
    
    private Long extractResourceId(String path) {
        String[] parts = path.split("/");
        if (parts.length > 1) {
            try {
                return Long.parseLong(parts[parts.length - 1]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    private OperationResponse handleGet(String opId, Long id) {
        if (id == null) {
            return new OperationResponse(opId, 200, new ArrayList<>(items.values()));
        }
        
        Item item = items.get(id);
        if (item == null) {
            return new OperationResponse(opId, 404, "Item not found");
        }
        return new OperationResponse(opId, 200, item);
    }
    
    private OperationResponse handlePost(String opId, Map<String, Object> body) {
        if (body == null) {
            return new OperationResponse(opId, 400, "Body required");
        }
        
        Long id = nextId.getAndIncrement();
        Item item = new Item(id, (String) body.get("name"), (String) body.get("description"));
        items.put(id, item);
        
        return new OperationResponse(opId, 201, item);
    }
    
    private OperationResponse handlePut(String opId, Long id, Map<String, Object> body) {
        if (id == null || body == null) {
            return new OperationResponse(opId, 400, "ID and body required");
        }
        
        Item item = items.get(id);
        if (item == null) {
            return new OperationResponse(opId, 404, "Item not found");
        }
        
        item.setName((String) body.get("name"));
        item.setDescription((String) body.get("description"));
        
        return new OperationResponse(opId, 200, item);
    }
    
    private OperationResponse handleDelete(String opId, Long id) {
        if (id == null) {
            return new OperationResponse(opId, 400, "ID required");
        }
        
        Item removed = items.remove(id);
        if (removed == null) {
            return new OperationResponse(opId, 404, "Item not found");
        }
        
        return new OperationResponse(opId, 204, null);
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

/**
 * Batch Request Controller
 */
@RestController
@RequestMapping("/api/batch")
class BatchController {
    private final BatchProcessor processor = new BatchProcessor();
    
    @PostMapping
    public ResponseEntity<BatchResponse> executeBatch(@RequestBody BatchRequest batchRequest) {
        BatchResponse response = processor.processBatch(batchRequest);
        
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.MULTI_STATUS;
        return ResponseEntity.status(status).body(response);
    }
}

/**
 * Demonstration of Batch Request Pattern
 */
public class BatchRequestPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Batch Request Pattern Demo ===\n");
        
        BatchProcessor processor = new BatchProcessor();
        
        System.out.println("1. Sequential Batch Request");
        BatchRequest sequentialBatch = new BatchRequest();
        sequentialBatch.setAtomic(false);
        sequentialBatch.setParallel(false);
        
        List<OperationRequest> operations1 = new ArrayList<>();
        operations1.add(new OperationRequest("op1", "GET", "/items/1"));
        operations1.add(new OperationRequest("op2", "GET", "/items/2"));
        operations1.add(new OperationRequest("op3", "GET", "/items/3"));
        sequentialBatch.setOperations(operations1);
        
        BatchResponse response1 = processor.processBatch(sequentialBatch);
        System.out.println("   Success: " + response1.isSuccess());
        System.out.println("   Operations: " + response1.getResults().size());
        System.out.println("   Execution time: " + response1.getExecutionTimeMs() + "ms");
        response1.getResults().forEach(r -> 
            System.out.println("   - " + r.getId() + ": " + r.getStatus() + " " + r.getBody())
        );
        
        System.out.println("\n2. Parallel Batch Request");
        BatchRequest parallelBatch = new BatchRequest();
        parallelBatch.setAtomic(false);
        parallelBatch.setParallel(true);
        parallelBatch.setOperations(operations1);
        
        BatchResponse response2 = processor.processBatch(parallelBatch);
        System.out.println("   Success: " + response2.isSuccess());
        System.out.println("   Execution time: " + response2.getExecutionTimeMs() + "ms (faster!)");
        
        System.out.println("\n3. Mixed Operations (CRUD)");
        BatchRequest mixedBatch = new BatchRequest();
        mixedBatch.setAtomic(false);
        mixedBatch.setParallel(false);
        
        List<OperationRequest> operations2 = new ArrayList<>();
        
        // Create
        OperationRequest create = new OperationRequest("create1", "POST", "/items");
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("name", "New Item");
        createBody.put("description", "Created via batch");
        create.setBody(createBody);
        operations2.add(create);
        
        // Read
        operations2.add(new OperationRequest("read1", "GET", "/items/1"));
        
        // Update
        OperationRequest update = new OperationRequest("update1", "PUT", "/items/1");
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("name", "Updated Item");
        updateBody.put("description", "Updated via batch");
        update.setBody(updateBody);
        operations2.add(update);
        
        // Delete
        operations2.add(new OperationRequest("delete1", "DELETE", "/items/3"));
        
        mixedBatch.setOperations(operations2);
        BatchResponse response3 = processor.processBatch(mixedBatch);
        
        System.out.println("   Results:");
        response3.getResults().forEach(r -> {
            System.out.println("   - " + r.getId() + ": " + r.getStatus() + 
                (r.getBody() != null ? " " + r.getBody() : ""));
        });
        
        System.out.println("\n4. Atomic Batch (All-or-Nothing)");
        BatchRequest atomicBatch = new BatchRequest();
        atomicBatch.setAtomic(true);
        atomicBatch.setParallel(false);
        
        List<OperationRequest> operations3 = new ArrayList<>();
        operations3.add(new OperationRequest("op1", "GET", "/items/1"));
        operations3.add(new OperationRequest("op2", "GET", "/items/999")); // Will fail
        operations3.add(new OperationRequest("op3", "GET", "/items/2"));
        atomicBatch.setOperations(operations3);
        
        BatchResponse response4 = processor.processBatch(atomicBatch);
        System.out.println("   Success: " + response4.isSuccess());
        System.out.println("   Note: All operations rolled back due to one failure");
        response4.getResults().forEach(r -> 
            System.out.println("   - " + r.getId() + ": " + r.getStatus() + 
                (r.getError() != null ? " - " + r.getError() : ""))
        );
        
        System.out.println("\n=== Batch Request Format ===");
        System.out.println("{");
        System.out.println("  \"atomic\": false,");
        System.out.println("  \"parallel\": true,");
        System.out.println("  \"operations\": [");
        System.out.println("    {");
        System.out.println("      \"id\": \"op1\",");
        System.out.println("      \"method\": \"GET\",");
        System.out.println("      \"path\": \"/items/1\"");
        System.out.println("    },");
        System.out.println("    {");
        System.out.println("      \"id\": \"op2\",");
        System.out.println("      \"method\": \"POST\",");
        System.out.println("      \"path\": \"/items\",");
        System.out.println("      \"body\": {");
        System.out.println("        \"name\": \"New Item\",");
        System.out.println("        \"description\": \"Item description\"");
        System.out.println("      }");
        System.out.println("    }");
        System.out.println("  ]");
        System.out.println("}");
        
        System.out.println("\n=== Best Practices ===");
        System.out.println("✓ Limit batch size (e.g., max 100 operations)");
        System.out.println("✓ Support both sequential and parallel execution");
        System.out.println("✓ Provide atomic transaction option");
        System.out.println("✓ Return HTTP 207 Multi-Status for partial success");
        System.out.println("✓ Include operation IDs for request/response correlation");
        System.out.println("✓ Set timeout for batch execution");
        System.out.println("✓ Validate operations before execution");
        System.out.println("✓ Consider rate limiting per batch");
        System.out.println("✓ Log batch operations for auditing");
        
        processor.shutdown();
    }
}
