package com.example.workflow.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@SpringBootApplication
@EnableAsync
public class TaskExecutionPattern {
    public static void main(String[] args) {
        SpringApplication.run(TaskExecutionPattern.class, args);
    }
}

@RestController
@RequestMapping("/api/tasks")
class TaskController {
    private final TaskExecutionService service;
    
    public TaskController(TaskExecutionService service) {
        this.service = service;
    }
    
    @PostMapping("/execute")
    public ResponseEntity<TaskResult> executeTask(@RequestBody TaskRequest request) {
        return ResponseEntity.ok(service.executeTask(request));
    }
    
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResult> getTaskStatus(@PathVariable String taskId) {
        return service.getTaskStatus(taskId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}

@Service
class TaskExecutionService {
    private final Map<String, TaskResult> tasks = new ConcurrentHashMap<>();
    
    @Async
    public TaskResult executeTask(TaskRequest request) {
        String taskId = UUID.randomUUID().toString();
        TaskResult result = new TaskResult(taskId, TaskStatus.RUNNING, null, LocalDateTime.now(), null);
        tasks.put(taskId, result);
        
        try {
            Thread.sleep(2000); // Simulate work
            result.setStatus(TaskStatus.COMPLETED);
            result.setResult("Task completed: " + request.getName());
            result.setCompletedAt(LocalDateTime.now());
        } catch (Exception e) {
            result.setStatus(TaskStatus.FAILED);
            result.setResult("Error: " + e.getMessage());
        }
        
        return result;
    }
    
    public Optional<TaskResult> getTaskStatus(String taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }
}

class TaskRequest {
    private String name;
    private Map<String, Object> parameters;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}

class TaskResult {
    private String taskId;
    private TaskStatus status;
    private String result;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    public TaskResult(String taskId, TaskStatus status, String result, LocalDateTime startedAt, LocalDateTime completedAt) {
        this.taskId = taskId;
        this.status = status;
        this.result = result;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }
    
    public String getTaskId() { return taskId; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}

enum TaskStatus {
    PENDING, RUNNING, COMPLETED, FAILED
}
