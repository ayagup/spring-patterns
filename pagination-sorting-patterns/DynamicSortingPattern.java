package com.example.pagination.sorting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.*;
import java.util.*;

/**
 * Dynamic Sorting Pattern
 * 
 * Allows clients to specify multiple sort fields and directions.
 * Supports complex sorting scenarios.
 */
@SpringBootApplication
public class DynamicSortingPattern {

    public static void main(String[] args) {
        SpringApplication.run(DynamicSortingPattern.class, args);
    }

    @Entity
    @Table(name = "employees")
    public static class Employee {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String firstName;
        private String lastName;
        private String department;
        private Double salary;
        private Integer age;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Double getSalary() { return salary; }
        public void setSalary(Double salary) { this.salary = salary; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }

    public interface EmployeeRepository extends JpaRepository<Employee, Long> {}

    @RestController
    @RequestMapping("/api/employees")
    public static class EmployeeController {

        private final EmployeeRepository repository;
        private static final Set<String> ALLOWED_SORT_FIELDS = 
            Set.of("firstName", "lastName", "department", "salary", "age");

        public EmployeeController(EmployeeRepository repository) {
            this.repository = repository;
        }

        /**
         * Single sort field
         * GET /api/employees?sortBy=salary&direction=DESC
         */
        @GetMapping("/simple")
        public Page<Employee> getEmployeesSimpleSort(
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            Pageable pageable
        ) {
            if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
                sortBy = "lastName";
            }
            
            Sort sort = Sort.by(direction, sortBy);
            PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
            );
            
            return repository.findAll(pageRequest);
        }

        /**
         * Multiple sort fields
         * GET /api/employees?sort=department,asc&sort=salary,desc
         */
        @GetMapping("/multi")
        public Page<Employee> getEmployeesMultiSort(Pageable pageable) {
            return repository.findAll(pageable);
        }

        /**
         * Custom complex sorting
         * GET /api/employees/custom?sortFields=department:asc,salary:desc,lastName:asc
         */
        @GetMapping("/custom")
        public Page<Employee> getEmployeesCustomSort(
            @RequestParam(required = false) String sortFields,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
        ) {
            Sort sort = parseSortFields(sortFields);
            Pageable pageable = PageRequest.of(page, size, sort);
            return repository.findAll(pageable);
        }

        private Sort parseSortFields(String sortFields) {
            if (sortFields == null || sortFields.isEmpty()) {
                return Sort.by(Sort.Direction.ASC, "lastName");
            }

            List<Sort.Order> orders = new ArrayList<>();
            String[] fields = sortFields.split(",");
            
            for (String field : fields) {
                String[] parts = field.split(":");
                String fieldName = parts[0].trim();
                
                if (!ALLOWED_SORT_FIELDS.contains(fieldName)) {
                    continue;
                }
                
                Sort.Direction direction = parts.length > 1 && 
                    parts[1].trim().equalsIgnoreCase("desc") ? 
                    Sort.Direction.DESC : Sort.Direction.ASC;
                
                orders.add(new Sort.Order(direction, fieldName));
            }
            
            return orders.isEmpty() ? 
                Sort.by(Sort.Direction.ASC, "lastName") : 
                Sort.by(orders);
        }
    }
}
