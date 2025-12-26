package com.example.workflow.sequential;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

@SpringBootApplication
public class SequentialExecutionPattern {
    public static void main(String[] args) {
        SpringApplication.run(SequentialExecutionPattern.class, args);
    }
}

@RestController
@RequestMapping("/api/sequential")
class SequentialController {
    private final SequentialExecutionService service;
    
    public SequentialController(SequentialExecutionService service) {
        this.service = service;
    }
    
    @PostMapping("/execute")
    public ResponseEntity<SequentialExecutionResult> executeSequential(@RequestBody SequentialRequest request) {
        return ResponseEntity.ok(service.executeSequential(request));
    }
}

@Service
class SequentialExecutionService {
    public SequentialExecutionResult executeSequential(SequentialRequest request) {
        String executionId = UUID.randomUUID().toString();
        List<StepResult> results = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.now();
        
        for (int i = 0; i < request.getSteps().size(); i++) {
            String step = request.getSteps().get(i);
            StepResult result = executeStep(step, i + 1);
            results.add(result);
            
            if (!result.isSuccess()) {
                break; // Stop on failure
            }
        }
        
        return new SequentialExecutionResult(executionId, results, startTime, LocalDateTime.now());
    }
    
    private StepResult executeStep(String stepName, int stepNumber) {
        try {
            Thread.sleep(500);
            return new StepResult(stepNumber, stepName, true, "Step completed");
        } catch (Exception e) {
            return new StepResult(stepNumber, stepName, false, e.getMessage());
        }
    }
}

class SequentialRequest {
    private List<String> steps;
    
    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }
}

class SequentialExecutionResult {
    private String executionId;
    private List<StepResult> results;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    public SequentialExecutionResult(String executionId, List<StepResult> results, LocalDateTime startTime, LocalDateTime endTime) {
        this.executionId = executionId;
        this.results = results;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public String getExecutionId() { return executionId; }
    public List<StepResult> getResults() { return results; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}

class StepResult {
    private int stepNumber;
    private String stepName;
    private boolean success;
    private String message;
    
    public StepResult(int stepNumber, String stepName, boolean success, String message) {
        this.stepNumber = stepNumber;
        this.stepName = stepName;
        this.success = success;
        this.message = message;
    }
    
    public int getStepNumber() { return stepNumber; }
    public String getStepName() { return stepName; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
