package com.example.querypatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic Query Pattern Implementation
 * 
 * Demonstrates building queries dynamically at runtime using JPA Criteria API.
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class DynamicQueryPattern {

    public static void main(String[] args) {
        SpringApplication.run(DynamicQueryPattern.class, args);
    }

    @Entity
    @Table(name = "projects")
    public static class Project {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        private String name;
        private String description;
        private String status;
        private BigDecimal budget;
        private LocalDate startDate;
        private LocalDate endDate;
        private String manager;
        private String department;
        private Integer teamSize;
        
        public Project() {}
        
        public Project(String name, String status, BigDecimal budget) {
            this.name = name;
            this.status = status;
            this.budget = budget;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BigDecimal getBudget() { return budget; }
        public void setBudget(BigDecimal budget) { this.budget = budget; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getManager() { return manager; }
        public void setManager(String manager) { this.manager = manager; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Integer getTeamSize() { return teamSize; }
        public void setTeamSize(Integer teamSize) { this.teamSize = teamSize; }
    }
    
    @Repository
    public interface ProjectRepository extends JpaRepository<Project, Long> {
    }
    
    @Service
    @Transactional
    public static class ProjectDynamicQueryService {
        
        @PersistenceContext
        private EntityManager entityManager;
        
        /**
         * Dynamic query builder based on non-null parameters
         */
        public List<Project> findProjects(String name, String status, String department,
                                         BigDecimal minBudget, BigDecimal maxBudget,
                                         LocalDate startDate, LocalDate endDate) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Project> query = cb.createQuery(Project.class);
            Root<Project> root = query.from(Project.class);
            
            List<Predicate> predicates = new ArrayList<>();
            
            // Add predicates dynamically based on parameters
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            if (department != null && !department.isEmpty()) {
                predicates.add(cb.equal(root.get("department"), department));
            }
            
            if (minBudget != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("budget"), minBudget));
            }
            
            if (maxBudget != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("budget"), maxBudget));
            }
            
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), endDate));
            }
            
            // Combine all predicates with AND
            query.where(cb.and(predicates.toArray(new Predicate[0])));
            
            return entityManager.createQuery(query).getResultList();
        }
        
        /**
         * Dynamic query with sorting
         */
        public List<Project> findProjectsSorted(String sortBy, String sortDirection) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Project> query = cb.createQuery(Project.class);
            Root<Project> root = query.from(Project.class);
            
            query.select(root);
            
            if (sortBy != null && sortDirection != null) {
                if ("desc".equalsIgnoreCase(sortDirection)) {
                    query.orderBy(cb.desc(root.get(sortBy)));
                } else {
                    query.orderBy(cb.asc(root.get(sortBy)));
                }
            }
            
            return entityManager.createQuery(query).getResultList();
        }
    }
    
    @RestController
    @RequestMapping("/api/dynamic/projects")
    public static class ProjectDynamicController {
        
        private final ProjectDynamicQueryService dynamicService;
        private final ProjectRepository projectRepository;
        
        public ProjectDynamicController(ProjectDynamicQueryService dynamicService,
                                       ProjectRepository projectRepository) {
            this.dynamicService = dynamicService;
            this.projectRepository = projectRepository;
        }
        
        @PostMapping
        public Project create(@RequestBody Project project) {
            return projectRepository.save(project);
        }
        
        @GetMapping("/search")
        public List<Project> search(
                @RequestParam(required = false) String name,
                @RequestParam(required = false) String status,
                @RequestParam(required = false) String department,
                @RequestParam(required = false) BigDecimal minBudget,
                @RequestParam(required = false) BigDecimal maxBudget,
                @RequestParam(required = false) LocalDate startDate,
                @RequestParam(required = false) LocalDate endDate) {
            return dynamicService.findProjects(name, status, department, 
                                              minBudget, maxBudget, startDate, endDate);
        }
        
        @GetMapping
        public List<Project> getAll(
                @RequestParam(required = false) String sortBy,
                @RequestParam(required = false) String sortDirection) {
            return dynamicService.findProjectsSorted(sortBy, sortDirection);
        }
    }
}

/**
 * Best Practices:
 * 1. Build predicates only for non-null parameters
 * 2. Use List<Predicate> to accumulate conditions
 * 3. Combine predicates with AND/OR as needed
 * 4. Validate sort field names to prevent injection
 * 5. Consider using Specification pattern for reusability
 * 6. Test with various parameter combinations
 * 7. Add pagination for large result sets
 * 8. Document dynamic query logic clearly
 */
