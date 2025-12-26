package com.example.workflow.conditional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

@SpringBootApplication
public class ConditionalFlowPattern {
    public static void main(String[] args) {
        SpringApplication.run(ConditionalFlowPattern.class, args);
    }
}

@RestController
@RequestMapping("/api/conditional")
class ConditionalController {
    private final ConditionalFlowService service;
    
    public ConditionalController(ConditionalFlowService service) {
        this.service = service;
    }
    
    @PostMapping("/execute")
    public ResponseEntity<ConditionalFlowResult> executeFlow(@RequestBody ConditionalFlowRequest request) {
        return ResponseEntity.ok(service.executeFlow(request));
    }
}

@Service
class ConditionalFlowService {
    public ConditionalFlowResult executeFlow(ConditionalFlowRequest request) {
        String executionId = UUID.randomUUID().toString();
        List<String> executedBranches = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.now();
        
        for (ConditionalBranch branch : request.getBranches()) {
            if (evaluateCondition(branch.getCondition(), request.getContext())) {
                executedBranches.add(branch.getName());
                executeBranch(branch);
                
                if (!branch.isContinueOnMatch()) {
                    break;
                }
            }
        }
        
        return new ConditionalFlowResult(executionId, executedBranches, startTime, LocalDateTime.now());
    }
    
    private boolean evaluateCondition(String condition, Map<String, Object> context) {
        // Simple condition evaluation
        if (condition.contains(">")) {
            String[] parts = condition.split(">");
            int value = (int) context.get(parts[0].trim());
            int threshold = Integer.parseInt(parts[1].trim());
            return value > threshold;
        }
        return true;
    }
    
    private void executeBranch(ConditionalBranch branch) {
        System.out.println("Executing branch: " + branch.getName());
    }
}

class ConditionalFlowRequest {
    private List<ConditionalBranch> branches;
    private Map<String, Object> context;
    
    public List<ConditionalBranch> getBranches() { return branches; }
    public void setBranches(List<ConditionalBranch> branches) { this.branches = branches; }
    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
}

class ConditionalBranch {
    private String name;
    private String condition;
    private boolean continueOnMatch = false;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public boolean isContinueOnMatch() { return continueOnMatch; }
    public void setContinueOnMatch(boolean continueOnMatch) { this.continueOnMatch = continueOnMatch; }
}

class ConditionalFlowResult {
    private String executionId;
    private List<String> executedBranches;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    public ConditionalFlowResult(String executionId, List<String> executedBranches, LocalDateTime startTime, LocalDateTime endTime) {
        this.executionId = executionId;
        this.executedBranches = executedBranches;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public String getExecutionId() { return executionId; }
    public List<String> getExecutedBranches() { return executedBranches; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}
