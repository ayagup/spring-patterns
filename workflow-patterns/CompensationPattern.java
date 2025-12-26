package com.example.workflow.compensation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

@SpringBootApplication
public class CompensationPattern {
    public static void main(String[] args) {
        SpringApplication.run(CompensationPattern.class, args);
    }
}

@RestController
@RequestMapping("/api/compensation")
class CompensationController {
    private final CompensationService service;
    
    public CompensationController(CompensationService service) {
        this.service = service;
    }
    
    @PostMapping("/execute")
    public ResponseEntity<CompensationResult> executeWithCompensation(@RequestBody CompensationRequest request) {
        return ResponseEntity.ok(service.executeWithCompensation(request));
    }
}

@Service
class CompensationService {
    public CompensationResult executeWithCompensation(CompensationRequest request) {
        String executionId = UUID.randomUUID().toString();
        List<CompensationAction> completedActions = new ArrayList<>();
        List<CompensationAction> compensatedActions = new ArrayList<>();
        boolean failed = false;
        LocalDateTime startTime = LocalDateTime.now();
        
        // Execute actions
        for (TransactionAction action : request.getActions()) {
            try {
                executeAction(action);
                completedActions.add(new CompensationAction(action.getName(), "COMPLETED", LocalDateTime.now()));
            } catch (Exception e) {
                failed = true;
                completedActions.add(new CompensationAction(action.getName(), "FAILED", LocalDateTime.now()));
                break;
            }
        }
        
        // Compensate if failed
        if (failed) {
            Collections.reverse(completedActions);
            for (CompensationAction action : completedActions) {
                if ("COMPLETED".equals(action.getStatus())) {
                    compensateAction(action.getActionName());
                    compensatedActions.add(action);
                }
            }
        }
        
        return new CompensationResult(executionId, completedActions, compensatedActions, 
            !failed, startTime, LocalDateTime.now());
    }
    
    private void executeAction(TransactionAction action) throws Exception {
        if (Math.random() > 0.8) {
            throw new Exception("Simulated failure");
        }
        System.out.println("Executing: " + action.getName());
    }
    
    private void compensateAction(String actionName) {
        System.out.println("Compensating: " + actionName);
    }
}

class CompensationRequest {
    private List<TransactionAction> actions;
    
    public List<TransactionAction> getActions() { return actions; }
    public void setActions(List<TransactionAction> actions) { this.actions = actions; }
}

class TransactionAction {
    private String name;
    private Map<String, Object> parameters;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}

class CompensationAction {
    private String actionName;
    private String status;
    private LocalDateTime executedAt;
    
    public CompensationAction(String actionName, String status, LocalDateTime executedAt) {
        this.actionName = actionName;
        this.status = status;
        this.executedAt = executedAt;
    }
    
    public String getActionName() { return actionName; }
    public String getStatus() { return status; }
    public LocalDateTime getExecutedAt() { return executedAt; }
}

class CompensationResult {
    private String executionId;
    private List<CompensationAction> completedActions;
    private List<CompensationAction> compensatedActions;
    private boolean success;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    public CompensationResult(String executionId, List<CompensationAction> completedActions, 
                             List<CompensationAction> compensatedActions, boolean success,
                             LocalDateTime startTime, LocalDateTime endTime) {
        this.executionId = executionId;
        this.completedActions = completedActions;
        this.compensatedActions = compensatedActions;
        this.success = success;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public String getExecutionId() { return executionId; }
    public List<CompensationAction> getCompletedActions() { return completedActions; }
    public List<CompensationAction> getCompensatedActions() { return compensatedActions; }
    public boolean isSuccess() { return success; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}
