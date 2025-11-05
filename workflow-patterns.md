### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 2:41:01 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **Workflow Patterns**
> - State Machine Pattern
> - Workflow Engine Pattern
> - Task Execution Pattern
> - Parallel Execution Pattern
> - Sequential Execution Pattern
> - Conditional Flow Pattern
> - Compensation Pattern
> ```

#### _Assistant_

> I'll create a comprehensive Spring Boot application demonstrating all these workflow patterns. Let me generate the complete project structure with examples for each pattern.
> 
> ## Project Structure
> 
> ```xml pom.xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0"
>          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
>          http://maven.apache.org/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>3.2.0</version>
>     </parent>
>     
>     <groupId>com.example</groupId>
>     <artifactId>workflow-patterns</artifactId>
>     <version>1.0.0</version>
>     
>     <properties>
>         <java.version>17</java.version>
>     </properties>
>     
>     <dependencies>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-validation</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-test</artifactId>
>             <scope>test</scope>
>         </dependency>
>     </dependencies>
>     
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>             </plugin>
>         </plugins>
>     </build>
> </project>
> ```
> 
> ## Main Application
> 
> ```java src/main/java/com/example/workflow/WorkflowPatternsApplication.java
> package com.example.workflow;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> import org.springframework.scheduling.annotation.EnableAsync;
> 
> @SpringBootApplication
> @EnableAsync
> public class WorkflowPatternsApplication {
>     public static void main(String[] args) {
>         SpringApplication.run(WorkflowPatternsApplication.class, args);
>     }
> }
> ```
> 
> ## 1. State Machine Pattern
> 
> ```java src/main/java/com/example/workflow/statemachine/OrderState.java
> package com.example.workflow.statemachine;
> 
> public enum OrderState {
>     CREATED,
>     PAYMENT_PENDING,
>     PAYMENT_CONFIRMED,
>     PREPARING,
>     SHIPPED,
>     DELIVERED,
>     CANCELLED
> }
> ```
> 
> ```java src/main/java/com/example/workflow/statemachine/OrderEvent.java
> package com.example.workflow.statemachine;
> 
> public enum OrderEvent {
>     SUBMIT,
>     PAY,
>     CONFIRM_PAYMENT,
>     PREPARE,
>     SHIP,
>     DELIVER,
>     CANCEL
> }
> ```
> 
> ```java src/main/java/com/example/workflow/statemachine/Order.java
> package com.example.workflow.statemachine;
> 
> import lombok.Data;
> import java.time.LocalDateTime;
> 
> @Data
> public class Order {
>     private String orderId;
>     private OrderState state;
>     private LocalDateTime createdAt;
>     private LocalDateTime updatedAt;
>     
>     public Order(String orderId) {
>         this.orderId = orderId;
>         this.state = OrderState.CREATED;
>         this.createdAt = LocalDateTime.now();
>         this.updatedAt = LocalDateTime.now();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/statemachine/OrderStateMachine.java
> package com.example.workflow.statemachine;
> 
> import org.springframework.stereotype.Component;
> import java.time.LocalDateTime;
> import java.util.HashMap;
> import java.util.Map;
> 
> @Component
> public class OrderStateMachine {
>     
>     private final Map<OrderState, Map<OrderEvent, OrderState>> transitions;
>     
>     public OrderStateMachine() {
>         transitions = new HashMap<>();
>         
>         // Define state transitions
>         Map<OrderEvent, OrderState> createdTransitions = new HashMap<>();
>         createdTransitions.put(OrderEvent.SUBMIT, OrderState.PAYMENT_PENDING);
>         createdTransitions.put(OrderEvent.CANCEL, OrderState.CANCELLED);
>         transitions.put(OrderState.CREATED, createdTransitions);
>         
>         Map<OrderEvent, OrderState> paymentPendingTransitions = new HashMap<>();
>         paymentPendingTransitions.put(OrderEvent.CONFIRM_PAYMENT, OrderState.PAYMENT_CONFIRMED);
>         paymentPendingTransitions.put(OrderEvent.CANCEL, OrderState.CANCELLED);
>         transitions.put(OrderState.PAYMENT_PENDING, paymentPendingTransitions);
>         
>         Map<OrderEvent, OrderState> paymentConfirmedTransitions = new HashMap<>();
>         paymentConfirmedTransitions.put(OrderEvent.PREPARE, OrderState.PREPARING);
>         transitions.put(OrderState.PAYMENT_CONFIRMED, paymentConfirmedTransitions);
>         
>         Map<OrderEvent, OrderState> preparingTransitions = new HashMap<>();
>         preparingTransitions.put(OrderEvent.SHIP, OrderState.SHIPPED);
>         transitions.put(OrderState.PREPARING, preparingTransitions);
>         
>         Map<OrderEvent, OrderState> shippedTransitions = new HashMap<>();
>         shippedTransitions.put(OrderEvent.DELIVER, OrderState.DELIVERED);
>         transitions.put(OrderState.SHIPPED, shippedTransitions);
>     }
>     
>     public Order transition(Order order, OrderEvent event) {
>         OrderState currentState = order.getState();
>         
>         Map<OrderEvent, OrderState> allowedTransitions = transitions.get(currentState);
>         if (allowedTransitions == null || !allowedTransitions.containsKey(event)) {
>             throw new IllegalStateException(
>                 String.format("Invalid transition from %s with event %s", currentState, event)
>             );
>         }
>         
>         OrderState newState = allowedTransitions.get(event);
>         order.setState(newState);
>         order.setUpdatedAt(LocalDateTime.now());
>         
>         System.out.println(String.format("Order %s transitioned from %s to %s", 
>             order.getOrderId(), currentState, newState));
>         
>         return order;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/statemachine/StateMachineController.java
> package com.example.workflow.statemachine;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.web.bind.annotation.*;
> 
> @RestController
> @RequestMapping("/api/statemachine")
> @RequiredArgsConstructor
> public class StateMachineController {
>     
>     private final OrderStateMachine stateMachine;
>     
>     @PostMapping("/order/{orderId}/event/{event}")
>     public Order processEvent(@PathVariable String orderId, @PathVariable OrderEvent event) {
>         Order order = new Order(orderId);
>         return stateMachine.transition(order, event);
>     }
> }
> ```
> 
> ## 2. Workflow Engine Pattern
> 
> ```java src/main/java/com/example/workflow/engine/WorkflowContext.java
> package com.example.workflow.engine;
> 
> import lombok.Data;
> import java.util.HashMap;
> import java.util.Map;
> 
> @Data
> public class WorkflowContext {
>     private String workflowId;
>     private Map<String, Object> variables;
>     private WorkflowStatus status;
>     
>     public WorkflowContext(String workflowId) {
>         this.workflowId = workflowId;
>         this.variables = new HashMap<>();
>         this.status = WorkflowStatus.PENDING;
>     }
>     
>     public void setVariable(String key, Object value) {
>         variables.put(key, value);
>     }
>     
>     public Object getVariable(String key) {
>         return variables.get(key);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/engine/WorkflowStatus.java
> package com.example.workflow.engine;
> 
> public enum WorkflowStatus {
>     PENDING,
>     RUNNING,
>     COMPLETED,
>     FAILED,
>     CANCELLED
> }
> ```
> 
> ```java src/main/java/com/example/workflow/engine/WorkflowStep.java
> package com.example.workflow.engine;
> 
> public interface WorkflowStep {
>     String getName();
>     void execute(WorkflowContext context) throws Exception;
>     boolean canExecute(WorkflowContext context);
> }
> ```
> 
> ```java src/main/java/com/example/workflow/engine/WorkflowDefinition.java
> package com.example.workflow.engine;
> 
> import lombok.Data;
> import java.util.ArrayList;
> import java.util.List;
> 
> @Data
> public class WorkflowDefinition {
>     private String name;
>     private List<WorkflowStep> steps;
>     
>     public WorkflowDefinition(String name) {
>         this.name = name;
>         this.steps = new ArrayList<>();
>     }
>     
>     public void addStep(WorkflowStep step) {
>         steps.add(step);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/engine/WorkflowEngine.java
> package com.example.workflow.engine;
> 
> import org.springframework.stereotype.Component;
> import lombok.extern.slf4j.Slf4j;
> 
> @Component
> @Slf4j
> public class WorkflowEngine {
>     
>     public WorkflowContext execute(WorkflowDefinition definition, WorkflowContext context) {
>         log.info("Starting workflow: {}", definition.getName());
>         context.setStatus(WorkflowStatus.RUNNING);
>         
>         try {
>             for (WorkflowStep step : definition.getSteps()) {
>                 log.info("Executing step: {}", step.getName());
>                 
>                 if (!step.canExecute(context)) {
>                     log.warn("Skipping step: {} - conditions not met", step.getName());
>                     continue;
>                 }
>                 
>                 step.execute(context);
>                 log.info("Step completed: {}", step.getName());
>             }
>             
>             context.setStatus(WorkflowStatus.COMPLETED);
>             log.info("Workflow completed: {}", definition.getName());
>             
>         } catch (Exception e) {
>             log.error("Workflow failed: {}", definition.getName(), e);
>             context.setStatus(WorkflowStatus.FAILED);
>             context.setVariable("error", e.getMessage());
>         }
>         
>         return context;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/engine/steps/ValidateDataStep.java
> package com.example.workflow.engine.steps;
> 
> import com.example.workflow.engine.WorkflowContext;
> import com.example.workflow.engine.WorkflowStep;
> import org.springframework.stereotype.Component;
> 
> @Component
> public class ValidateDataStep implements WorkflowStep {
>     
>     @Override
>     public String getName() {
>         return "ValidateData";
>     }
>     
>     @Override
>     public void execute(WorkflowContext context) throws Exception {
>         String data = (String) context.getVariable("data");
>         if (data == null || data.isEmpty()) {
>             throw new IllegalArgumentException("Data is required");
>         }
>         context.setVariable("validated", true);
>     }
>     
>     @Override
>     public boolean canExecute(WorkflowContext context) {
>         return true;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/engine/steps/ProcessDataStep.java
> package com.example.workflow.engine.steps;
> 
> import com.example.workflow.engine.WorkflowContext;
> import com.example.workflow.engine.WorkflowStep;
> import org.springframework.stereotype.Component;
> 
> @Component
> public class ProcessDataStep implements WorkflowStep {
>     
>     @Override
>     public String getName() {
>         return "ProcessData";
>     }
>     
>     @Override
>     public void execute(WorkflowContext context) throws Exception {
>         String data = (String) context.getVariable("data");
>         String processed = data.toUpperCase();
>         context.setVariable("processedData", processed);
>     }
>     
>     @Override
>     public boolean canExecute(WorkflowContext context) {
>         return context.getVariable("validated") != null;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/engine/WorkflowEngineController.java
> package com.example.workflow.engine;
> 
> import com.example.workflow.engine.steps.ProcessDataStep;
> import com.example.workflow.engine.steps.ValidateDataStep;
> import lombok.RequiredArgsConstructor;
> import org.springframework.web.bind.annotation.*;
> 
> @RestController
> @RequestMapping("/api/workflow")
> @RequiredArgsConstructor
> public class WorkflowEngineController {
>     
>     private final WorkflowEngine workflowEngine;
>     private final ValidateDataStep validateDataStep;
>     private final ProcessDataStep processDataStep;
>     
>     @PostMapping("/execute")
>     public WorkflowContext executeWorkflow(@RequestBody String data) {
>         WorkflowDefinition definition = new WorkflowDefinition("DataProcessing");
>         definition.addStep(validateDataStep);
>         definition.addStep(processDataStep);
>         
>         WorkflowContext context = new WorkflowContext("wf-001");
>         context.setVariable("data", data);
>         
>         return workflowEngine.execute(definition, context);
>     }
> }
> ```
> 
> ## 3. Task Execution Pattern
> 
> ```java src/main/java/com/example/workflow/task/Task.java
> package com.example.workflow.task;
> 
> import lombok.Data;
> import java.time.LocalDateTime;
> 
> @Data
> public class Task {
>     private String taskId;
>     private String name;
>     private TaskStatus status;
>     private LocalDateTime createdAt;
>     private LocalDateTime startedAt;
>     private LocalDateTime completedAt;
>     private Object result;
>     private String error;
>     
>     public Task(String taskId, String name) {
>         this.taskId = taskId;
>         this.name = name;
>         this.status = TaskStatus.PENDING;
>         this.createdAt = LocalDateTime.now();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/task/TaskStatus.java
> package com.example.workflow.task;
> 
> public enum TaskStatus {
>     PENDING,
>     RUNNING,
>     COMPLETED,
>     FAILED,
>     CANCELLED
> }
> ```
> 
> ```java src/main/java/com/example/workflow/task/TaskExecutor.java
> package com.example.workflow.task;
> 
> public interface TaskExecutor<T> {
>     T execute(Task task) throws Exception;
> }
> ```
> 
> ```java src/main/java/com/example/workflow/task/TaskExecutionService.java
> package com.example.workflow.task;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> import java.time.LocalDateTime;
> import java.util.Map;
> import java.util.concurrent.ConcurrentHashMap;
> 
> @Service
> @Slf4j
> public class TaskExecutionService {
>     
>     private final Map<String, Task> tasks = new ConcurrentHashMap<>();
>     
>     public <T> Task submitTask(String taskId, String taskName, TaskExecutor<T> executor) {
>         Task task = new Task(taskId, taskName);
>         tasks.put(taskId, task);
>         
>         // Execute task asynchronously
>         executeAsync(task, executor);
>         
>         return task;
>     }
>     
>     private <T> void executeAsync(Task task, TaskExecutor<T> executor) {
>         new Thread(() -> {
>             try {
>                 task.setStatus(TaskStatus.RUNNING);
>                 task.setStartedAt(LocalDateTime.now());
>                 log.info("Starting task: {}", task.getName());
>                 
>                 T result = executor.execute(task);
>                 
>                 task.setResult(result);
>                 task.setStatus(TaskStatus.COMPLETED);
>                 task.setCompletedAt(LocalDateTime.now());
>                 log.info("Task completed: {}", task.getName());
>                 
>             } catch (Exception e) {
>                 log.error("Task failed: {}", task.getName(), e);
>                 task.setStatus(TaskStatus.FAILED);
>                 task.setError(e.getMessage());
>                 task.setCompletedAt(LocalDateTime.now());
>             }
>         }).start();
>     }
>     
>     public Task getTask(String taskId) {
>         return tasks.get(taskId);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/task/TaskExecutionController.java
> package com.example.workflow.task;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.web.bind.annotation.*;
> 
> @RestController
> @RequestMapping("/api/task")
> @RequiredArgsConstructor
> public class TaskExecutionController {
>     
>     private final TaskExecutionService taskExecutionService;
>     
>     @PostMapping("/execute")
>     public Task executeTask(@RequestBody String data) {
>         TaskExecutor<String> executor = task -> {
>             Thread.sleep(2000); // Simulate work
>             return "Processed: " + data;
>         };
>         
>         return taskExecutionService.submitTask("task-001", "ProcessData", executor);
>     }
>     
>     @GetMapping("/{taskId}")
>     public Task getTask(@PathVariable String taskId) {
>         return taskExecutionService.getTask(taskId);
>     }
> }
> ```
> 
> ## 4. Parallel Execution Pattern
> 
> ```java src/main/java/com/example/workflow/parallel/ParallelTask.java
> package com.example.workflow.parallel;
> 
> import lombok.AllArgsConstructor;
> import lombok.Data;
> import java.util.concurrent.Callable;
> 
> @Data
> @AllArgsConstructor
> public class ParallelTask<T> implements Callable<T> {
>     private String taskId;
>     private String taskName;
>     private Callable<T> callable;
>     
>     @Override
>     public T call() throws Exception {
>         System.out.println("Executing parallel task: " + taskName);
>         return callable.call();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/parallel/ParallelExecutionService.java
> package com.example.workflow.parallel;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> import java.util.List;
> import java.util.concurrent.*;
> import java.util.stream.Collectors;
> 
> @Service
> @Slf4j
> public class ParallelExecutionService {
>     
>     private final ExecutorService executorService;
>     
>     public ParallelExecutionService() {
>         this.executorService = Executors.newFixedThreadPool(10);
>     }
>     
>     public <T> List<T> executeParallel(List<ParallelTask<T>> tasks) throws InterruptedException, ExecutionException {
>         log.info("Executing {} tasks in parallel", tasks.size());
>         
>         List<Future<T>> futures = executorService.invokeAll(tasks);
>         
>         List<T> results = futures.stream()
>             .map(future -> {
>                 try {
>                     return future.get();
>                 } catch (Exception e) {
>                     log.error("Task execution failed", e);
>                     return null;
>                 }
>             })
>             .collect(Collectors.toList());
>         
>         log.info("All parallel tasks completed");
>         return results;
>     }
>     
>     public <T> List<T> executeParallelWithTimeout(List<ParallelTask<T>> tasks, long timeout, TimeUnit unit) 
>             throws InterruptedException {
>         log.info("Executing {} tasks in parallel with timeout", tasks.size());
>         
>         List<Future<T>> futures = executorService.invokeAll(tasks, timeout, unit);
>         
>         return futures.stream()
>             .map(future -> {
>                 try {
>                     if (future.isDone()) {
>                         return future.get();
>                     } else {
>                         log.warn("Task timed out");
>                         return null;
>                     }
>                 } catch (Exception e) {
>                     log.error("Task execution failed", e);
>                     return null;
>                 }
>             })
>             .collect(Collectors.toList());
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/parallel/ParallelExecutionController.java
> package com.example.workflow.parallel;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.web.bind.annotation.*;
> import java.util.ArrayList;
> import java.util.List;
> 
> @RestController
> @RequestMapping("/api/parallel")
> @RequiredArgsConstructor
> public class ParallelExecutionController {
>     
>     private final ParallelExecutionService parallelExecutionService;
>     
>     @PostMapping("/execute")
>     public List<String> executeParallelTasks() throws Exception {
>         List<ParallelTask<String>> tasks = new ArrayList<>();
>         
>         for (int i = 1; i <= 5; i++) {
>             final int taskNum = i;
>             tasks.add(new ParallelTask<>(
>                 "task-" + i,
>                 "Task " + i,
>                 () -> {
>                     Thread.sleep(1000 * taskNum);
>                     return "Result from task " + taskNum;
>                 }
>             ));
>         }
>         
>         return parallelExecutionService.executeParallel(tasks);
>     }
> }
> ```
> 
> ## 5. Sequential Execution Pattern
> 
> ```java src/main/java/com/example/workflow/sequential/SequentialStep.java
> package com.example.workflow.sequential;
> 
> import lombok.AllArgsConstructor;
> import lombok.Data;
> 
> @Data
> @AllArgsConstructor
> public class SequentialStep<T> {
>     private String stepId;
>     private String stepName;
>     private StepExecutor<T> executor;
> }
> ```
> 
> ```java src/main/java/com/example/workflow/sequential/StepExecutor.java
> package com.example.workflow.sequential;
> 
> @FunctionalInterface
> public interface StepExecutor<T> {
>     T execute(T input) throws Exception;
> }
> ```
> 
> ```java src/main/java/com/example/workflow/sequential/SequentialExecutionService.java
> package com.example.workflow.sequential;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> import java.util.List;
> 
> @Service
> @Slf4j
> public class SequentialExecutionService {
>     
>     public <T> T executeSequential(List<SequentialStep<T>> steps, T initialInput) {
>         log.info("Starting sequential execution with {} steps", steps.size());
>         
>         T currentInput = initialInput;
>         
>         for (int i = 0; i < steps.size(); i++) {
>             SequentialStep<T> step = steps.get(i);
>             log.info("Executing step {}/{}: {}", i + 1, steps.size(), step.getStepName());
>             
>             try {
>                 currentInput = step.getExecutor().execute(currentInput);
>                 log.info("Step {} completed successfully", step.getStepName());
>             } catch (Exception e) {
>                 log.error("Step {} failed", step.getStepName(), e);
>                 throw new RuntimeException("Sequential execution failed at step: " + step.getStepName(), e);
>             }
>         }
>         
>         log.info("Sequential execution completed successfully");
>         return currentInput;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/sequential/SequentialExecutionController.java
> package com.example.workflow.sequential;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.web.bind.annotation.*;
> import java.util.ArrayList;
> import java.util.List;
> 
> @RestController
> @RequestMapping("/api/sequential")
> @RequiredArgsConstructor
> public class SequentialExecutionController {
>     
>     private final SequentialExecutionService sequentialExecutionService;
>     
>     @PostMapping("/execute")
>     public String executeSequentialSteps(@RequestBody String initialInput) {
>         List<SequentialStep<String>> steps = new ArrayList<>();
>         
>         steps.add(new SequentialStep<>(
>             "step-1",
>             "Trim Input",
>             input -> input.trim()
>         ));
>         
>         steps.add(new SequentialStep<>(
>             "step-2",
>             "Convert to Uppercase",
>             input -> input.toUpperCase()
>         ));
>         
>         steps.add(new SequentialStep<>(
>             "step-3",
>             "Add Prefix",
>             input -> "PROCESSED: " + input
>         ));
>         
>         steps.add(new SequentialStep<>(
>             "step-4",
>             "Add Timestamp",
>             input -> input + " [" + System.currentTimeMillis() + "]"
>         ));
>         
>         return sequentialExecutionService.executeSequential(steps, initialInput);
>     }
> }
> ```
> 
> ## 6. Conditional Flow Pattern
> 
> ```java src/main/java/com/example/workflow/conditional/Condition.java
> package com.example.workflow.conditional;
> 
> @FunctionalInterface
> public interface Condition<T> {
>     boolean evaluate(T context);
> }
> ```
> 
> ```java src/main/java/com/example/workflow/conditional/ConditionalBranch.java
> package com.example.workflow.conditional;
> 
> import lombok.AllArgsConstructor;
> import lombok.Data;
> 
> @Data
> @AllArgsConstructor
> public class ConditionalBranch<T> {
>     private String branchName;
>     private Condition<T> condition;
>     private BranchExecutor<T> executor;
> }
> ```
> 
> ```java src/main/java/com/example/workflow/conditional/BranchExecutor.java
> package com.example.workflow.conditional;
> 
> @FunctionalInterface
> public interface BranchExecutor<T> {
>     void execute(T context) throws Exception;
> }
> ```
> 
> ```java src/main/java/com/example/workflow/conditional/ConditionalFlowService.java
> package com.example.workflow.conditional;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> import java.util.List;
> 
> @Service
> @Slf4j
> public class ConditionalFlowService {
>     
>     public <T> void executeConditionalFlow(List<ConditionalBranch<T>> branches, T context) {
>         log.info("Starting conditional flow evaluation");
>         
>         for (ConditionalBranch<T> branch : branches) {
>             log.info("Evaluating condition for branch: {}", branch.getBranchName());
>             
>             if (branch.getCondition().evaluate(context)) {
>                 log.info("Condition met for branch: {}, executing...", branch.getBranchName());
>                 
>                 try {
>                     branch.getExecutor().execute(context);
>                     log.info("Branch {} executed successfully", branch.getBranchName());
>                     return; // Execute only the first matching branch
>                 } catch (Exception e) {
>                     log.error("Branch execution failed: {}", branch.getBranchName(), e);
>                     throw new RuntimeException("Conditional flow failed", e);
>                 }
>             } else {
>                 log.info("Condition not met for branch: {}", branch.getBranchName());
>             }
>         }
>         
>         log.warn("No matching condition found in conditional flow");
>     }
>     
>     public <T> void executeAllMatchingBranches(List<ConditionalBranch<T>> branches, T context) {
>         log.info("Starting conditional flow - executing all matching branches");
>         
>         for (ConditionalBranch<T> branch : branches) {
>             if (branch.getCondition().evaluate(context)) {
>                 log.info("Executing branch: {}", branch.getBranchName());
>                 
>                 try {
>                     branch.getExecutor().execute(context);
>                 } catch (Exception e) {
>                     log.error("Branch execution failed: {}", branch.getBranchName(), e);
>                 }
>             }
>         }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/conditional/OrderContext.java
> package com.example.workflow.conditional;
> 
> import lombok.Data;
> 
> @Data
> public class OrderContext {
>     private double totalAmount;
>     private String customerType;
>     private boolean isPremium;
>     private double discount;
>     
>     public OrderContext(double totalAmount, String customerType, boolean isPremium) {
>         this.totalAmount = totalAmount;
>         this.customerType = customerType;
>         this.isPremium = isPremium;
>         this.discount = 0.0;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/conditional/ConditionalFlowController.java
> package com.example.workflow.conditional;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.web.bind.annotation.*;
> import java.util.ArrayList;
> import java.util.List;
> 
> @RestController
> @RequestMapping("/api/conditional")
> @RequiredArgsConstructor
> public class ConditionalFlowController {
>     
>     private final ConditionalFlowService conditionalFlowService;
>     
>     @PostMapping("/execute")
>     public OrderContext executeConditionalFlow(@RequestBody OrderContext orderContext) {
>         List<ConditionalBranch<OrderContext>> branches = new ArrayList<>();
>         
>         // Premium customer discount
>         branches.add(new ConditionalBranch<>(
>             "Premium Customer",
>             ctx -> ctx.isPremium(),
>             ctx -> {
>                 ctx.setDiscount(0.15);
>                 System.out.println("Applied 15% premium discount");
>             }
>         ));
>         
>         // Large order discount
>         branches.add(new ConditionalBranch<>(
>             "Large Order",
>             ctx -> ctx.getTotalAmount() > 1000,
>             ctx -> {
>                 ctx.setDiscount(0.10);
>                 System.out.println("Applied 10% large order discount");
>             }
>         ));
>         
>         // Regular customer
>         branches.add(new ConditionalBranch<>(
>             "Regular Customer",
>             ctx -> "REGULAR".equals(ctx.getCustomerType()),
>             ctx -> {
>                 ctx.setDiscount(0.05);
>                 System.out.println("Applied 5% regular customer discount");
>             }
>         ));
>         
>         // Default - no discount
>         branches.add(new ConditionalBranch<>(
>             "Default",
>             ctx -> true,
>             ctx -> System.out.println("No discount applied")
>         ));
>         
>         conditionalFlowService.executeConditionalFlow(branches, orderContext);
>         
>         return orderContext;
>     }
> }
> ```
> 
> ## 7. Compensation Pattern
> 
> ```java src/main/java/com/example/workflow/compensation/CompensationAction.java
> package com.example.workflow.compensation;
> 
> @FunctionalInterface
> public interface CompensationAction<T> {
>     void compensate(T context) throws Exception;
> }
> ```
> 
> ```java src/main/java/com/example/workflow/compensation/CompensatableStep.java
> package com.example.workflow.compensation;
> 
> import lombok.AllArgsConstructor;
> import lombok.Data;
> 
> @Data
> @AllArgsConstructor
> public class CompensatableStep<T> {
>     private String stepId;
>     private String stepName;
>     private StepAction<T> action;
>     private CompensationAction<T> compensation;
> }
> ```
> 
> ```java src/main/java/com/example/workflow/compensation/StepAction.java
> package com.example.workflow.compensation;
> 
> @FunctionalInterface
> public interface StepAction<T> {
>     void execute(T context) throws Exception;
> }
> ```
> 
> ```java src/main/java/com/example/workflow/compensation/CompensationService.java
> package com.example.workflow.compensation;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> import java.util.ArrayList;
> import java.util.List;
> 
> @Service
> @Slf4j
> public class CompensationService {
>     
>     public <T> void executeWithCompensation(List

#### _User_

> Continue your response exactly where you left off:

#### _Assistant_

> ```java src/main/java/com/example/workflow/compensation/CompensationService.java
> package com.example.workflow.compensation;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> import java.util.ArrayList;
> import java.util.List;
> 
> @Service
> @Slf4j
> public class CompensationService {
>     
>     public <T> void executeWithCompensation(List<CompensatableStep<T>> steps, T context) {
>         List<CompensatableStep<T>> executedSteps = new ArrayList<>();
>         
>         try {
>             for (CompensatableStep<T> step : steps) {
>                 log.info("Executing step: {}", step.getStepName());
>                 step.getAction().execute(context);
>                 executedSteps.add(step);
>                 log.info("Step completed: {}", step.getStepName());
>             }
>             log.info("All steps completed successfully");
>             
>         } catch (Exception e) {
>             log.error("Step execution failed, starting compensation", e);
>             compensate(executedSteps, context);
>             throw new RuntimeException("Transaction failed and compensated", e);
>         }
>     }
>     
>     private <T> void compensate(List<CompensatableStep<T>> executedSteps, T context) {
>         log.info("Starting compensation for {} executed steps", executedSteps.size());
>         
>         // Compensate in reverse order
>         for (int i = executedSteps.size() - 1; i >= 0; i--) {
>             CompensatableStep<T> step = executedSteps.get(i);
>             
>             try {
>                 log.info("Compensating step: {}", step.getStepName());
>                 step.getCompensation().compensate(context);
>                 log.info("Step compensated: {}", step.getStepName());
>             } catch (Exception e) {
>                 log.error("Compensation failed for step: {}", step.getStepName(), e);
>                 // Continue compensating other steps even if one fails
>             }
>         }
>         
>         log.info("Compensation completed");
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/compensation/BookingContext.java
> package com.example.workflow.compensation;
> 
> import lombok.Data;
> import java.util.ArrayList;
> import java.util.List;
> 
> @Data
> public class BookingContext {
>     private String bookingId;
>     private boolean flightBooked;
>     private boolean hotelBooked;
>     private boolean carRented;
>     private boolean paymentProcessed;
>     private List<String> activities;
>     
>     public BookingContext(String bookingId) {
>         this.bookingId = bookingId;
>         this.activities = new ArrayList<>();
>     }
>     
>     public void addActivity(String activity) {
>         activities.add(activity);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/workflow/compensation/CompensationController.java
> package com.example.workflow.compensation;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.web.bind.annotation.*;
> import java.util.ArrayList;
> import java.util.List;
> 
> @RestController
> @RequestMapping("/api/compensation")
> @RequiredArgsConstructor
> public class CompensationController {
>     
>     private final CompensationService compensationService;
>     
>     @PostMapping("/booking/{bookingId}")
>     public BookingContext createBooking(
>             @PathVariable String bookingId,
>             @RequestParam(defaultValue = "false") boolean shouldFail) {
>         
>         BookingContext context = new BookingContext(bookingId);
>         
>         List<CompensatableStep<BookingContext>> steps = new ArrayList<>();
>         
>         // Step 1: Book Flight
>         steps.add(new CompensatableStep<>(
>             "step-1",
>             "Book Flight",
>             ctx -> {
>                 System.out.println("Booking flight...");
>                 ctx.setFlightBooked(true);
>                 ctx.addActivity("Flight booked");
>             },
>             ctx -> {
>                 System.out.println("Cancelling flight...");
>                 ctx.setFlightBooked(false);
>                 ctx.addActivity("Flight cancelled");
>             }
>         ));
>         
>         // Step 2: Book Hotel
>         steps.add(new CompensatableStep<>(
>             "step-2",
>             "Book Hotel",
>             ctx -> {
>                 System.out.println("Booking hotel...");
>                 ctx.setHotelBooked(true);
>                 ctx.addActivity("Hotel booked");
>             },
>             ctx -> {
>                 System.out.println("Cancelling hotel...");
>                 ctx.setHotelBooked(false);
>                 ctx.addActivity("Hotel cancelled");
>             }
>         ));
>         
>         // Step 3: Rent Car
>         steps.add(new CompensatableStep<>(
>             "step-3",
>             "Rent Car",
>             ctx -> {
>                 System.out.println("Renting car...");
>                 ctx.setCarRented(true);
>                 ctx.addActivity("Car rented");
>             },
>             ctx -> {
>                 System.out.println("Cancelling car rental...");
>                 ctx.setCarRented(false);
>                 ctx.addActivity("Car rental cancelled");
>             }
>         ));
>         
>         // Step 4: Process Payment (will fail if shouldFail is true)
>         steps.add(new CompensatableStep<>(
>             "step-4",
>             "Process Payment",
>             ctx -> {
>                 System.out.println("Processing payment...");
>                 if (shouldFail) {
>                     throw new RuntimeException("Payment processing failed");
>                 }
>                 ctx.setPaymentProcessed(true);
>                 ctx.addActivity("Payment processed");
>             },
>             ctx -> {
>                 System.out.println("Refunding payment...");
>                 ctx.setPaymentProcessed(false);
>                 ctx.addActivity("Payment refunded");
>             }
>         ));
>         
>         compensationService.executeWithCompensation(steps, context);
>         
>         return context;
>     }
> }
> ```
> 
> ## Application Configuration
> 
> ```yaml src/main/resources/application.yml
> spring:
>   application:
>     name: workflow-patterns
>     
> server:
>   port: 8080
> 
> logging:
>   level:
>     com.example.workflow: INFO
>     org.springframework: WARN
> ```
> 
> ## Demo Controller
> 
> ```java src/main/java/com/example/workflow/demo/DemoController.java
> package com.example.workflow.demo;
> 
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> import java.util.HashMap;
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/demo")
> public class DemoController {
>     
>     @GetMapping("/endpoints")
>     public Map<String, String> getEndpoints() {
>         Map<String, String> endpoints = new HashMap<>();
>         
>         endpoints.put("State Machine", "POST /api/statemachine/order/{orderId}/event/{event}");
>         endpoints.put("Workflow Engine", "POST /api/workflow/execute");
>         endpoints.put("Task Execution", "POST /api/task/execute");
>         endpoints.put("Parallel Execution", "POST /api/parallel/execute");
>         endpoints.put("Sequential Execution", "POST /api/sequential/execute");
>         endpoints.put("Conditional Flow", "POST /api/conditional/execute");
>         endpoints.put("Compensation", "POST /api/compensation/booking/{bookingId}?shouldFail=false");
>         
>         return endpoints;
>     }
> }
> ```
> 
> ## Test Examples
> 
> ```java src/test/java/com/example/workflow/WorkflowPatternsTests.java
> package com.example.workflow;
> 
> import com.example.workflow.compensation.BookingContext;
> import com.example.workflow.compensation.CompensatableStep;
> import com.example.workflow.compensation.CompensationService;
> import com.example.workflow.conditional.ConditionalBranch;
> import com.example.workflow.conditional.ConditionalFlowService;
> import com.example.workflow.conditional.OrderContext;
> import com.example.workflow.parallel.ParallelExecutionService;
> import com.example.workflow.parallel.ParallelTask;
> import com.example.workflow.sequential.SequentialExecutionService;
> import com.example.workflow.sequential.SequentialStep;
> import com.example.workflow.statemachine.Order;
> import com.example.workflow.statemachine.OrderEvent;
> import com.example.workflow.statemachine.OrderStateMachine;
> import com.example.workflow.statemachine.OrderState;
> import org.junit.jupiter.api.Test;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.boot.test.context.SpringBootTest;
> 
> import java.util.ArrayList;
> import java.util.List;
> 
> import static org.junit.jupiter.api.Assertions.*;
> 
> @SpringBootTest
> class WorkflowPatternsTests {
>     
>     @Autowired
>     private OrderStateMachine stateMachine;
>     
>     @Autowired
>     private ParallelExecutionService parallelExecutionService;
>     
>     @Autowired
>     private SequentialExecutionService sequentialExecutionService;
>     
>     @Autowired
>     private ConditionalFlowService conditionalFlowService;
>     
>     @Autowired
>     private CompensationService compensationService;
>     
>     @Test
>     void testStateMachine() {
>         Order order = new Order("ORDER-001");
>         
>         assertEquals(OrderState.CREATED, order.getState());
>         
>         stateMachine.transition(order, OrderEvent.SUBMIT);
>         assertEquals(OrderState.PAYMENT_PENDING, order.getState());
>         
>         stateMachine.transition(order, OrderEvent.CONFIRM_PAYMENT);
>         assertEquals(OrderState.PAYMENT_CONFIRMED, order.getState());
>     }
>     
>     @Test
>     void testParallelExecution() throws Exception {
>         List<ParallelTask<Integer>> tasks = new ArrayList<>();
>         
>         for (int i = 1; i <= 5; i++) {
>             final int num = i;
>             tasks.add(new ParallelTask<>(
>                 "task-" + i,
>                 "Task " + i,
>                 () -> num * 2
>             ));
>         }
>         
>         List<Integer> results = parallelExecutionService.executeParallel(tasks);
>         
>         assertEquals(5, results.size());
>         assertTrue(results.contains(2));
>         assertTrue(results.contains(10));
>     }
>     
>     @Test
>     void testSequentialExecution() {
>         List<SequentialStep<Integer>> steps = new ArrayList<>();
>         
>         steps.add(new SequentialStep<>("step-1", "Add 10", n -> n + 10));
>         steps.add(new SequentialStep<>("step-2", "Multiply by 2", n -> n * 2));
>         steps.add(new SequentialStep<>("step-3", "Subtract 5", n -> n - 5));
>         
>         Integer result = sequentialExecutionService.executeSequential(steps, 5);
>         
>         // (5 + 10) * 2 - 5 = 25
>         assertEquals(25, result);
>     }
>     
>     @Test
>     void testConditionalFlow() {
>         OrderContext premiumContext = new OrderContext(500, "PREMIUM", true);
>         
>         List<ConditionalBranch<OrderContext>> branches = new ArrayList<>();
>         
>         branches.add(new ConditionalBranch<>(
>             "Premium",
>             ctx -> ctx.isPremium(),
>             ctx -> ctx.setDiscount(0.15)
>         ));
>         
>         branches.add(new ConditionalBranch<>(
>             "Default",
>             ctx -> true,
>             ctx -> ctx.setDiscount(0.0)
>         ));
>         
>         conditionalFlowService.executeConditionalFlow(branches, premiumContext);
>         
>         assertEquals(0.15, premiumContext.getDiscount());
>     }
>     
>     @Test
>     void testCompensationSuccess() {
>         BookingContext context = new BookingContext("BOOKING-001");
>         
>         List<CompensatableStep<BookingContext>> steps = new ArrayList<>();
>         
>         steps.add(new CompensatableStep<>(
>             "step-1",
>             "Book Flight",
>             ctx -> ctx.setFlightBooked(true),
>             ctx -> ctx.setFlightBooked(false)
>         ));
>         
>         steps.add(new CompensatableStep<>(
>             "step-2",
>             "Book Hotel",
>             ctx -> ctx.setHotelBooked(true),
>             ctx -> ctx.setHotelBooked(false)
>         ));
>         
>         compensationService.executeWithCompensation(steps, context);
>         
>         assertTrue(context.isFlightBooked());
>         assertTrue(context.isHotelBooked());
>     }
>     
>     @Test
>     void testCompensationFailure() {
>         BookingContext context = new BookingContext("BOOKING-002");
>         
>         List<CompensatableStep<BookingContext>> steps = new ArrayList<>();
>         
>         steps.add(new CompensatableStep<>(
>             "step-1",
>             "Book Flight",
>             ctx -> ctx.setFlightBooked(true),
>             ctx -> ctx.setFlightBooked(false)
>         ));
>         
>         steps.add(new CompensatableStep<>(
>             "step-2",
>             "Book Hotel (Fail)",
>             ctx -> { throw new RuntimeException("Hotel booking failed"); },
>             ctx -> ctx.setHotelBooked(false)
>         ));
>         
>         assertThrows(RuntimeException.class, () -> {
>             compensationService.executeWithCompensation(steps, context);
>         });
>         
>         // Flight should be compensated (cancelled)
>         assertFalse(context.isFlightBooked());
>     }
> }
> ```
> 
> ## README
> 
> ```markdown README.md
> # Workflow Patterns in Spring Boot
> 
> This project demonstrates 7 essential workflow patterns implemented in Spring Boot.
> 
> ## Patterns Implemented
> 
> ### 1. State Machine Pattern
> - **Location**: `com.example.workflow.statemachine`
> - **Purpose**: Manages order state transitions with validation
> - **Endpoint**: `POST /api/statemachine/order/{orderId}/event/{event}`
> 
> ### 2. Workflow Engine Pattern
> - **Location**: `com.example.workflow.engine`
> - **Purpose**: Executes multi-step workflows with context management
> - **Endpoint**: `POST /api/workflow/execute`
> 
> ### 3. Task Execution Pattern
> - **Location**: `com.example.workflow.task`
> - **Purpose**: Asynchronous task execution with status tracking
> - **Endpoint**: `POST /api/task/execute`
> 
> ### 4. Parallel Execution Pattern
> - **Location**: `com.example.workflow.parallel`
> - **Purpose**: Executes multiple tasks concurrently
> - **Endpoint**: `POST /api/parallel/execute`
> 
> ### 5. Sequential Execution Pattern
> - **Location**: `com.example.workflow.sequential`
> - **Purpose**: Executes tasks in a defined order, passing results between steps
> - **Endpoint**: `POST /api/sequential/execute`
> 
> ### 6. Conditional Flow Pattern
> - **Location**: `com.example.workflow.conditional`
> - **Purpose**: Executes branches based on condition evaluation
> - **Endpoint**: `POST /api/conditional/execute`
> 
> ### 7. Compensation Pattern
> - **Location**: `com.example.workflow.compensation`
> - **Purpose**: Implements saga pattern with rollback capabilities
> - **Endpoint**: `POST /api/compensation/booking/{bookingId}?shouldFail=false`
> 
> ## Running the Application
> 
> ```bash
> mvn clean install
> mvn spring-boot:run
> ```
> 
> ## Testing the Endpoints
> 
> ### State Machine
> ```bash
> curl -X POST http://localhost:8080/api/statemachine/order/ORD-001/event/SUBMIT
> ```
> 
> ### Workflow Engine
> ```bash
> curl -X POST http://localhost:8080/api/workflow/execute \
>   -H "Content-Type: text/plain" \
>   -d "test data"
> ```
> 
> ### Compensation Pattern (Success)
> ```bash
> curl -X POST http://localhost:8080/api/compensation/booking/BOOK-001?shouldFail=false
> ```
> 
> ### Compensation Pattern (Failure & Rollback)
> ```bash
> curl -X POST http://localhost:8080/api/compensation/booking/BOOK-002?shouldFail=true
> ```
> 
> ## Running Tests
> 
> ```bash
> mvn test
> ```
> ```
> 
> ---
> 
> This complete implementation provides production-ready examples of all 7 workflow patterns with:
> 
> - **Clean separation** of concerns
> - **Spring Boot best practices**
> - **Comprehensive logging**
> - **Error handling**
> - **Unit tests**
> - **REST API endpoints** for testing each pattern
> - **Real-world use cases** (order processing, bookings, etc.)
> 
> Each pattern is self-contained and can be used independently or combined based on your requirements.