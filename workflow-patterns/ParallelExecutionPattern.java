package com.example.workflow.parallel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class ParallelExecutionPattern {
    public static void main(String[] args) {
        SpringApplication.run(ParallelExecutionPattern.class, args);
    }
}

@RestController
@RequestMapping("/api/parallel")
class ParallelController {
    private final ParallelExecutionService service;
    
    public ParallelController(ParallelExecutionService service) {
        this.service = service;
    }
    
    @PostMapping("/execute")
    public ResponseEntity<ParallelExecutionResult> executeParallel(@RequestBody ParallelRequest request) {
        return ResponseEntity.ok(service.executeParallel(request));
    }
}

@Service
class ParallelExecutionService {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    public ParallelExecutionResult executeParallel(ParallelRequest request) {
        String executionId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        
        List<CompletableFuture<TaskResult>> futures = request.getTasks().stream()
            .map(task -> CompletableFuture.supplyAsync(() -> executeTask(task), executor))
            .collect(Collectors.toList());
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        List<TaskResult> results = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
        
        return new ParallelExecutionResult(executionId, results, startTime, LocalDateTime.now());
    }
    
    private TaskResult executeTask(String taskName) {
        try {
            Thread.sleep(1000);
            return new TaskResult(taskName, "SUCCESS", "Completed");
        } catch (Exception e) {
            return new TaskResult(taskName, "FAILED", e.getMessage());
        }
    }
}

class ParallelRequest {
    private List<String> tasks;
    
    public List<String> getTasks() { return tasks; }
    public void setTasks(List<String> tasks) { this.tasks = tasks; }
}

class ParallelExecutionResult {
    private String executionId;
    private List<TaskResult> results;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    public ParallelExecutionResult(String executionId, List<TaskResult> results, LocalDateTime startTime, LocalDateTime endTime) {
        this.executionId = executionId;
        this.results = results;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public String getExecutionId() { return executionId; }
    public List<TaskResult> getResults() { return results; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}

class TaskResult {
    private String taskName;
    private String status;
    private String result;
    
    public TaskResult(String taskName, String status, String result) {
        this.taskName = taskName;
        this.status = status;
        this.result = result;
    }
    
    public String getTaskName() { return taskName; }
    public String getStatus() { return status; }
    public String getResult() { return result; }
}
