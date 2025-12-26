# Workflow Patterns

This collection demonstrates various workflow orchestration patterns for building complex business process automation in Spring.

## Patterns Overview

### 1. State Machine Pattern (`StateMachinePattern.java`)
**Purpose:** State-based workflow with defined transitions  
**Use Case:** Order processing, approval workflows  
**Key Features:**
- Finite state machine implementation
- State transition guards/validation
- State history tracking
- Event-driven state changes
- Transition rules enforcement

**States:** CREATED → PENDING → APPROVED → SHIPPED → DELIVERED → COMPLETED  
**Events:** CREATE, SUBMIT, APPROVE, REJECT, SHIP, DELIVER, COMPLETE, CANCEL

**Example Usage:**
```java
// Create order
POST /api/statemachine/orders
{
  "customerName": "John Doe",
  "amount": 199.99
}

// Transition order state
POST /api/statemachine/orders/{orderId}/transition
{
  "event": "SUBMIT"
}

// Get state history
GET /api/statemachine/orders/{orderId}/history
```

### 2. Workflow Engine Pattern (`WorkflowEnginePattern.java`)
**Purpose:** Step-based workflow execution engine  
**Use Case:** Multi-step business processes  
**Key Features:**
- Workflow definition (steps, sequence)
- Workflow instance management
- Context/variable passing between steps
- Conditional branching
- Pause/resume capabilities
- Step execution monitoring

**Example Usage:**
```java
// Define workflow
POST /api/workflow/define
{
  "name": "Customer Onboarding",
  "steps": [
    {"name": "ValidateData", "type": "validation"},
    {"name": "CreateAccount", "type": "action"},
    {"name": "SendWelcomeEmail", "type": "notification"}
  ]
}

// Start workflow
POST /api/workflow/start
{
  "workflowId": "workflow-uuid",
  "variables": {
    "customerEmail": "user@example.com",
    "customerName": "John Doe"
  }
}

// Execute next step
POST /api/workflow/instances/{instanceId}/execute
```

### 3. Task Execution Pattern (`TaskExecutionPattern.java`)
**Purpose:** Asynchronous task execution  
**Use Case:** Background jobs, long-running operations  
**Key Features:**
- @Async task execution
- Task status tracking
- Non-blocking operation
- Result retrieval
- Timeout handling

**Example Usage:**
```java
POST /api/tasks/execute
{
  "name": "DataExport",
  "parameters": {
    "format": "CSV",
    "dateRange": "2024-01-01 to 2024-12-31"
  }
}

// Returns immediately with taskId
// Check status later
GET /api/tasks/{taskId}
```

### 4. Parallel Execution Pattern (`ParallelExecutionPattern.java`)
**Purpose:** Concurrent task execution  
**Use Case:** Independent tasks that can run simultaneously  
**Key Features:**
- CompletableFuture-based parallelism
- ExecutorService thread pool
- Batch task execution
- Result aggregation
- Error handling per task

**Example Usage:**
```java
POST /api/parallel/execute
{
  "tasks": [
    "GenerateReport",
    "SendNotifications",
    "UpdateDatabase",
    "RefreshCache"
  ]
}

// All tasks execute in parallel
// Response after all complete:
{
  "executionId": "exec-uuid",
  "results": [
    {"taskName": "GenerateReport", "status": "SUCCESS"},
    {"taskName": "SendNotifications", "status": "SUCCESS"},
    ...
  ],
  "startTime": "2024-01-15T10:00:00",
  "endTime": "2024-01-15T10:00:05"
}
```

### 5. Sequential Execution Pattern (`SequentialExecutionPattern.java`)
**Purpose:** Ordered step-by-step execution  
**Use Case:** Dependent tasks that must execute in order  
**Key Features:**
- Sequential step processing
- Stop-on-failure behavior
- Step numbering
- Execution history
- Result passing between steps

**Example Usage:**
```java
POST /api/sequential/execute
{
  "steps": [
    "ValidateInput",
    "ProcessPayment",
    "UpdateInventory",
    "GenerateInvoice",
    "SendConfirmation"
  ]
}

// Steps execute in order
// If any step fails, execution stops
```

### 6. Conditional Flow Pattern (`ConditionalFlowPattern.java`)
**Purpose:** Decision-based workflow routing  
**Use Case:** Branching workflows, rule-based processing  
**Key Features:**
- Condition evaluation
- Branch selection based on context
- Multiple branch support
- Continue-on-match vs stop-on-match
- Context variable access

**Example Usage:**
```java
POST /api/conditional/execute
{
  "branches": [
    {
      "name": "HighValueCustomer",
      "condition": "orderAmount > 1000",
      "continueOnMatch": false
    },
    {
      "name": "StandardCustomer",
      "condition": "orderAmount > 100",
      "continueOnMatch": false
    },
    {
      "name": "BudgetCustomer",
      "condition": "orderAmount > 0",
      "continueOnMatch": false
    }
  ],
  "context": {
    "orderAmount": 1500
  }
}

// Only "HighValueCustomer" branch executes
```

### 7. Compensation Pattern (`CompensationPattern.java`)
**Purpose:** Saga pattern for distributed transactions  
**Use Case:** Rollback operations, error recovery  
**Key Features:**
- Forward transaction execution
- Automatic compensation on failure
- Reverse-order rollback
- Action history tracking
- Idempotent compensation

**Example Usage:**
```java
POST /api/compensation/execute
{
  "actions": [
    {"name": "ReserveInventory", "parameters": {"productId": "P123", "quantity": 5}},
    {"name": "ChargeCustomer", "parameters": {"amount": 99.99}},
    {"name": "CreateShipment", "parameters": {"address": "123 Main St"}}
  ]
}

// If any action fails, all completed actions are compensated in reverse order
// Response includes compensation details:
{
  "executionId": "exec-uuid",
  "completedActions": [...],
  "compensatedActions": [...],  // If failure occurred
  "success": false
}
```

## Pattern Comparison Matrix

| Pattern | Execution Model | Use Case | Complexity | Error Handling | State Management |
|---------|----------------|----------|------------|----------------|------------------|
| State Machine | Event-driven | Order processing | Medium | Transition validation | Explicit states |
| Workflow Engine | Step-based | Business processes | High | Per-step errors | Variables/context |
| Task Execution | Async | Background jobs | Low | Task-level | Simple status |
| Parallel Execution | Concurrent | Independent tasks | Medium | Per-task errors | Result aggregation |
| Sequential Execution | Ordered | Dependent tasks | Low | Stop-on-failure | Step history |
| Conditional Flow | Decision-based | Branching logic | Medium | Branch-level | Context variables |
| Compensation | Saga | Distributed transactions | High | Automatic rollback | Action history |

## When to Use Each Pattern

### Choose State Machine Pattern when:
- Workflow has clearly defined states
- State transitions follow specific rules
- Need to enforce valid state changes
- Tracking state history is important
- Examples: Order status, approval flows

### Choose Workflow Engine Pattern when:
- Complex multi-step business processes
- Steps need to share data/context
- Workflow definition is dynamic
- Need pause/resume capabilities
- Examples: Onboarding, loan processing

### Choose Task Execution Pattern when:
- Long-running background operations
- Non-blocking execution required
- Simple async job processing
- Examples: Report generation, data export

### Choose Parallel Execution Pattern when:
- Multiple independent tasks
- Tasks can run simultaneously
- Need to reduce total execution time
- Examples: Multi-source data fetching, batch operations

### Choose Sequential Execution Pattern when:
- Tasks have dependencies
- Order of execution matters
- One task's output is next task's input
- Examples: Data pipeline, deployment steps

### Choose Conditional Flow Pattern when:
- Decision-based routing needed
- Multiple execution paths
- Rules-based processing
- Examples: Approval routing, pricing rules

### Choose Compensation Pattern when:
- Distributed transactions
- Need rollback capability
- Multi-service coordination
- Examples: E-commerce checkout, booking systems

## Integration Scenarios

### Scenario 1: E-Commerce Order Processing
**Combine:** State Machine + Workflow Engine + Compensation
```
1. State Machine: Track order states (CREATED → PAID → SHIPPED → DELIVERED)
2. Workflow Engine: Define checkout process steps
3. Compensation: Handle payment/inventory failures
```

### Scenario 2: Approval Workflow
**Combine:** Conditional Flow + State Machine
```
1. Conditional Flow: Route to appropriate approver based on amount
2. State Machine: Track approval states (PENDING → APPROVED/REJECTED)
```

### Scenario 3: Data Processing Pipeline
**Combine:** Sequential Execution + Task Execution
```
1. Sequential: Process data through transform steps
2. Task Execution: Run expensive operations async
```

### Scenario 4: Microservices Orchestration
**Combine:** Compensation + Parallel Execution
```
1. Parallel: Call multiple services concurrently
2. Compensation: Rollback on any service failure
```

## Configuration

### application.properties
```properties
# Async Task Execution
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=20
spring.task.execution.pool.queue-capacity=100

# Workflow Engine
workflow.max-concurrent-instances=50
workflow.step-timeout-seconds=300

# State Machine
statemachine.enable-history=true
statemachine.max-history-size=1000

# Compensation
compensation.retry-attempts=3
compensation.retry-delay-ms=1000
```

## Dependencies

```xml
<!-- Spring State Machine (optional) -->
<dependency>
    <groupId>org.springframework.statemachine</groupId>
    <artifactId>spring-statemachine-core</artifactId>
    <version>3.2.0</version>
</dependency>

<!-- Activiti Workflow Engine (optional) -->
<dependency>
    <groupId>org.activiti</groupId>
    <artifactId>activiti-spring-boot-starter</artifactId>
    <version>7.1.0</version>
</dependency>

<!-- Spring Boot Async -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## Best Practices

1. **Keep workflows simple** - break complex flows into smaller workflows
2. **Make steps idempotent** - support retry safely
3. **Handle failures gracefully** - implement compensation where needed
4. **Log state transitions** - maintain audit trail
5. **Use timeouts** - prevent hanging workflows
6. **Version workflows** - support schema evolution
7. **Monitor execution** - track performance and failures
8. **Test edge cases** - especially error and compensation paths
9. **Document transitions** - clearly define valid state changes
10. **Consider persistence** - use database for long-running workflows

## Advanced Patterns

### Saga Pattern (using Compensation)
```java
// Orchestration-based Saga
1. Reserve inventory → Compensate: Release inventory
2. Charge customer → Compensate: Refund customer
3. Create shipment → Compensate: Cancel shipment
```

### Fork-Join Pattern (using Parallel + Sequential)
```java
// Fork: Parallel tasks
Parallel: [TaskA, TaskB, TaskC]
// Join: Wait for all, then continue
Sequential: [Aggregate Results, Send Notification]
```

### Pipeline Pattern (using Sequential)
```java
Sequential: [Extract, Transform, Validate, Load, Index]
```

## Performance Considerations

- **State Machine**: O(1) state lookup, very fast
- **Sequential**: Execution time = sum of all steps
- **Parallel**: Execution time ≈ longest task
- **Workflow Engine**: Overhead ~10-50ms per step
- **Compensation**: 2x execution time (forward + compensate on failure)

## Error Handling Strategies

### Retry with Exponential Backoff
```java
int retries = 0;
while (retries < maxRetries) {
    try {
        executeStep();
        break;
    } catch (Exception e) {
        Thread.sleep(delay * Math.pow(2, retries));
        retries++;
    }
}
```

### Circuit Breaker
```java
if (circuitOpen) {
    return fallbackResponse();
}
try {
    return executeStep();
} catch (Exception e) {
    openCircuit();
    throw e;
}
```

## Testing

```bash
# Test state machine
curl -X POST http://localhost:8080/api/statemachine/orders \
  -H "Content-Type: application/json" \
  -d '{"customerName":"John","amount":99.99}'

# Test parallel execution
curl -X POST http://localhost:8080/api/parallel/execute \
  -H "Content-Type: application/json" \
  -d '{"tasks":["Task1","Task2","Task3"]}'

# Test compensation
curl -X POST http://localhost:8080/api/compensation/execute \
  -H "Content-Type: application/json" \
  -d '{"actions":[{"name":"Step1"},{"name":"Step2"}]}'
```

## License

These patterns are provided as educational examples for Spring Boot workflow orchestration.
