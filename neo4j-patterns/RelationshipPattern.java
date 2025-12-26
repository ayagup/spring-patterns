package com.example.neo4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Relationship Pattern
 *
 * Demonstrates how to model and manage relationships between nodes in Spring Data Neo4j.
 * Relationships can be simple references or complex entities with properties.
 *
 * Key Features:
 * - @Relationship annotation to define connections between nodes.
 * - Modeling relationships as simple Sets or Lists for basic connections.
 * - Using @RelationshipProperties for relationships with attributes (e.g., roles, timestamps).
 * - Directionality (INCOMING, OUTGOING) to define the nature of the relationship.
 *
 * Use Cases:
 * - Social networks (e.g., FRIEND_OF, FOLLOWS).
 * - Organizational charts (e.g., REPORTS_TO).
 * - Product recommendations (e.g., CUSTOMER_BOUGHT, VIEWED).
 * - Any domain where the connections between entities are as important as the entities themselves.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class RelationshipPattern {

    public static void main(String[] args) {
        SpringApplication.run(RelationshipPattern.class, args);
    }
}

// Domain Models
@Node
class Employee {
    @Id @GeneratedValue
    private Long id;
    private String name;

    // Simple relationship to a single node
    @Relationship(type = "REPORTS_TO")
    private Employee manager;

    // Simple relationship to multiple nodes
    @Relationship(type = "MENTORS")
    private Set<Employee> mentees = new HashSet<>();

    // Relationship with properties
    @Relationship(type = "WORKS_ON")
    private Set<ProjectAssignment> assignments = new HashSet<>();

    public Employee() {}

    public Employee(String name) {
        this.name = name;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Employee getManager() { return manager; }
    public void setManager(Employee manager) { this.manager = manager; }
    public Set<Employee> getMentees() { return mentees; }
    public void setMentees(Set<Employee> mentees) { this.mentees = mentees; }
    public Set<ProjectAssignment> getAssignments() { return assignments; }
    public void setAssignments(Set<ProjectAssignment> assignments) { this.assignments = assignments; }
}

@Node
class Project {
    @Id @GeneratedValue
    private Long id;
    private String name;

    public Project() {}

    public Project(String name) {
        this.name = name;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

@RelationshipProperties
class ProjectAssignment {
    @Id @GeneratedValue
    private Long id;

    @TargetNode
    private Project project;

    private String role;
    private int allocation; // Percentage of time allocated

    public ProjectAssignment(Project project, String role, int allocation) {
        this.project = project;
        this.role = role;
        this.allocation = allocation;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public int getAllocation() { return allocation; }
    public void setAllocation(int allocation) { this.allocation = allocation; }
}

// Service Layer
@Service
class OrgChartService {
    private final Neo4jTemplate neo4jTemplate;

    public OrgChartService(Neo4jTemplate neo4jTemplate) {
        this.neo4jTemplate = neo4jTemplate;
    }

    public Employee createEmployee(String name) {
        return neo4jTemplate.save(new Employee(name));
    }

    public Project createProject(String name) {
        return neo4jTemplate.save(new Project(name));
    }

    public Employee setManager(Long employeeId, Long managerId) {
        Employee employee = neo4jTemplate.findById(employeeId, Employee.class).orElseThrow();
        Employee manager = neo4jTemplate.findById(managerId, Employee.class).orElseThrow();
        employee.setManager(manager);
        return neo4jTemplate.save(employee);
    }

    public Employee addMentee(Long mentorId, Long menteeId) {
        Employee mentor = neo4jTemplate.findById(mentorId, Employee.class).orElseThrow();
        Employee mentee = neo4jTemplate.findById(menteeId, Employee.class).orElseThrow();
        mentor.getMentees().add(mentee);
        return neo4jTemplate.save(mentor);
    }

    public Employee assignToProject(Long employeeId, Long projectId, String role, int allocation) {
        Employee employee = neo4jTemplate.findById(employeeId, Employee.class).orElseThrow();
        Project project = neo4jTemplate.findById(projectId, Project.class).orElseThrow();
        ProjectAssignment assignment = new ProjectAssignment(project, role, allocation);
        employee.getAssignments().add(assignment);
        return neo4jTemplate.save(employee);
    }

    public List<Map<String, Object>> getTeam(Long managerId) {
        String cypher = """
            MATCH (manager:Employee)<-[:REPORTS_TO]-(employee:Employee)
            WHERE id(manager) = $managerId
            RETURN employee.name as employeeName, id(employee) as employeeId
            """;
        return neo4jTemplate.findAll(cypher, Map.of("managerId", managerId), Map.class);
    }

    public List<Map<String, Object>> getProjectTeam(String projectName) {
        String cypher = """
            MATCH (e:Employee)-[r:WORKS_ON]->(p:Project {name: $projectName})
            RETURN e.name as employeeName, r.role as projectRole, r.allocation as allocation
            """;
        return neo4jTemplate.findAll(cypher, Map.of("projectName", projectName), Map.class);
    }
    
    public Employee findEmployeeById(Long id) {
        return neo4jTemplate.findById(id, Employee.class).orElse(null);
    }
}

// REST Controller
@RestController
@RequestMapping("/api/relationships")
class OrgChartController {
    private final OrgChartService service;

    public OrgChartController(OrgChartService service) {
        this.service = service;
    }

    @PostMapping("/employees")
    public Employee createEmployee(@RequestParam String name) {
        return service.createEmployee(name);
    }

    @PostMapping("/projects")
    public Project createProject(@RequestParam String name) {
        return service.createProject(name);
    }

    @PutMapping("/employees/{employeeId}/manager/{managerId}")
    public Employee setManager(@PathVariable Long employeeId, @PathVariable Long managerId) {
        return service.setManager(employeeId, managerId);
    }

    @PutMapping("/employees/{mentorId}/mentee/{menteeId}")
    public Employee addMentee(@PathVariable Long mentorId, @PathVariable Long menteeId) {
        return service.addMentee(mentorId, menteeId);
    }

    @PostMapping("/employees/{employeeId}/assign")
    public Employee assignToProject(@PathVariable Long employeeId, @RequestParam Long projectId, @RequestParam String role, @RequestParam int allocation) {
        return service.assignToProject(employeeId, projectId, role, allocation);
    }

    @GetMapping("/teams/{managerId}")
    public List<Map<String, Object>> getTeam(@PathVariable Long managerId) {
        return service.getTeam(managerId);
    }

    @GetMapping("/projects/{projectName}/team")
    public List<Map<String, Object>> getProjectTeam(@PathVariable String projectName) {
        return service.getProjectTeam(projectName);
    }
    
    @GetMapping("/employees/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable Long id) {
        Employee employee = service.findEmployeeById(id);
        return employee != null ? ResponseEntity.ok(employee) : ResponseEntity.notFound().build();
    }

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Relationship Pattern",
            "description", "Models simple and complex relationships between nodes.",
            "features", "@Relationship, @RelationshipProperties, directionality.",
            "endpoints", "8 REST endpoints for managing an organizational chart."
        );
    }
}
