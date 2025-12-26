package com.example.workflow.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Workflow Engine Pattern
 * 
 * Demonstrates:
 * - Workflow definition and execution
 * - Step-based workflow processing
 * - Workflow context/variables
 * - Conditional branching
 * - Workflow monitoring
 * - Pause/resume capabilities
 * 
 * Dependencies:
 * - Activiti or Camunda (optional for production use)
 */

@SpringBootApplication
public class WorkflowEnginePattern {
    public static void main(String[] args) {
        SpringApplication.run(WorkflowEnginePattern.class, args);
    }
}

@Configuration
class WorkflowEngineConfig {}

@RestController
@RequestMapping("/api/workflow")
class WorkflowController {
    private final WorkflowService service;
    
    public WorkflowController(WorkflowService service) {
        this.service = service;
    }
    
    @PostMapping("/define")
    public ResponseEntity<WorkflowDefinition> defineWorkflow(@Valid @RequestBody DefineWorkflowRequest request) {
        return ResponseEntity.ok(service.defineWorkflow(request));
    }
    
    @PostMapping("/start")
    public ResponseEntity<WorkflowInstance> startWorkflow(@Valid @RequestBody StartWorkflowRequest request) {
        return ResponseEntity.ok(service.startWorkflow(request));
    }
    
    @PostMapping("/instances/{instanceId}/execute")
    public ResponseEntity<WorkflowInstance> executeNextStep(@PathVariable String instanceId) {
        return service.executeNextStep(instanceId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/instances/{instanceId}")
    public ResponseEntity<WorkflowInstance> getInstance(@PathVariable String instanceId) {
        return service.getInstance(instanceId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}

@Service
class WorkflowService {
    private final Map<String, WorkflowDefinition> definitions = new ConcurrentHashMap<>();
    private final Map<String, WorkflowInstance> instances = new ConcurrentHashMap<>();
    
    public WorkflowDefinition defineWorkflow(DefineWorkflowRequest request) {
        String workflowId = UUID.randomUUID().toString();
        WorkflowDefinition definition = new WorkflowDefinition(
            workflowId, request.getName(), request.getSteps(), LocalDateTime.now()
        );
        definitions.put(workflowId, definition);
        return definition;
    }
    
    public WorkflowInstance startWorkflow(StartWorkflowRequest request) {
        WorkflowDefinition definition = definitions.get(request.getWorkflowId());
        if (definition == null) {
            throw new IllegalArgumentException("Workflow definition not found");
        }
        
        String instanceId = UUID.randomUUID().toString();
        WorkflowInstance instance = new WorkflowInstance(
            instanceId, definition.getId(), definition.getSteps(), 
            0, new HashMap<>(request.getVariables()), WorkflowStatus.RUNNING, LocalDateTime.now()
        );
        
        instances.put(instanceId, instance);
        return instance;
    }
    
    public Optional<WorkflowInstance> executeNextStep(String instanceId) {
        WorkflowInstance instance = instances.get(instanceId);
        if (instance == null || instance.getStatus() != WorkflowStatus.RUNNING) {
            return Optional.ofNullable(instance);
        }
        
        if (instance.getCurrentStep() >= instance.getSteps().size()) {
            instance.setStatus(WorkflowStatus.COMPLETED);
            return Optional.of(instance);
        }
        
        WorkflowStep step = instance.getSteps().get(instance.getCurrentStep());
        executeStep(step, instance.getVariables());
        
        instance.setCurrentStep(instance.getCurrentStep() + 1);
        
        if (instance.getCurrentStep() >= instance.getSteps().size()) {
            instance.setStatus(WorkflowStatus.COMPLETED);
        }
        
        return Optional.of(instance);
    }
    
    private void executeStep(WorkflowStep step, Map<String, Object> variables) {
        // Simulate step execution
        System.out.println("Executing step: " + step.getName());
        variables.put(step.getName() + "_executed", LocalDateTime.now());
    }
    
    public Optional<WorkflowInstance> getInstance(String instanceId) {
        return Optional.ofNullable(instances.get(instanceId));
    }
}

class DefineWorkflowRequest {
    @NotBlank
    private String name;
    private List<WorkflowStep> steps;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<WorkflowStep> getSteps() { return steps; }
    public void setSteps(List<WorkflowStep> steps) { this.steps = steps; }
}

class StartWorkflowRequest {
    @NotBlank
    private String workflowId;
    private Map<String, Object> variables = new HashMap<>();
    
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
}

class WorkflowDefinition {
    private String id;
    private String name;
    private List<WorkflowStep> steps;
    private LocalDateTime createdAt;
    
    public WorkflowDefinition(String id, String name, List<WorkflowStep> steps, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        this.createdAt = createdAt;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public List<WorkflowStep> getSteps() { return steps; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

class WorkflowInstance {
    private String id;
    private String workflowId;
    private List<WorkflowStep> steps;
    private int currentStep;
    private Map<String, Object> variables;
    private WorkflowStatus status;
    private LocalDateTime startedAt;
    
    public WorkflowInstance(String id, String workflowId, List<WorkflowStep> steps, int currentStep,
                           Map<String, Object> variables, WorkflowStatus status, LocalDateTime startedAt) {
        this.id = id;
        this.workflowId = workflowId;
        this.steps = steps;
        this.currentStep = currentStep;
        this.variables = variables;
        this.status = status;
        this.startedAt = startedAt;
    }
    
    public String getId() { return id; }
    public String getWorkflowId() { return workflowId; }
    public List<WorkflowStep> getSteps() { return steps; }
    public int getCurrentStep() { return currentStep; }
    public void setCurrentStep(int currentStep) { this.currentStep = currentStep; }
    public Map<String, Object> getVariables() { return variables; }
    public WorkflowStatus getStatus() { return status; }
    public void setStatus(WorkflowStatus status) { this.status = status; }
    public LocalDateTime getStartedAt() { return startedAt; }
}

class WorkflowStep {
    private String name;
    private String type;
    private Map<String, Object> config;
    
    public WorkflowStep() {}
    
    public WorkflowStep(String name, String type, Map<String, Object> config) {
        this.name = name;
        this.type = type;
        this.config = config;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Map<String, Object> getConfig() { return config; }
    public void setConfig(Map<String, Object> config) { this.config = config; }
}

enum WorkflowStatus {
    PENDING, RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED
}
