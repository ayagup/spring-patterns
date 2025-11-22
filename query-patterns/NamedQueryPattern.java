package com.example.querypatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Named Query Pattern Implementation
 * 
 * Demonstrates static named queries using @NamedQuery and @NamedQueries.
 * 
 * Key Components:
 * - @NamedQuery annotation for static queries
 * - @NamedQueries for multiple queries
 * - @NamedNativeQuery for native SQL
 * - Pre-compiled at startup for performance
 * - Repository method naming to invoke named queries
 * 
 * Benefits:
 * - Queries validated at startup
 * - Centralized query definitions
 * - Better performance (pre-compiled)
 * - Reusable across repositories
 * - Clear separation of queries from code
 * 
 * Use Cases:
 * - Frequently used queries
 * - Complex static queries
 * - Standardized data access patterns
 * - Performance-critical queries
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class NamedQueryPattern {

    public static void main(String[] args) {
        SpringApplication.run(NamedQueryPattern.class, args);
    }

    /**
     * Employee Entity with Named Queries
     * 
     * Named queries are defined at the entity level using @NamedQuery
     * and can be referenced by name in repositories.
     */
    @Entity
    @Table(name = "employees")
    @NamedQueries({
        @NamedQuery(
            name = "Employee.findByDepartment",
            query = "SELECT e FROM Employee e WHERE e.department = :department"
        ),
        @NamedQuery(
            name = "Employee.findActivesupport",
            query = "SELECT e FROM Employee e WHERE e.active = true ORDER BY e.lastName"
        ),
        @NamedQuery(
            name = "Employee.findByLastNameContaining",
            query = "SELECT e FROM Employee e WHERE LOWER(e.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))"
        ),
        @NamedQuery(
            name = "Employee.findBySalaryRange",
            query = "SELECT e FROM Employee e WHERE e.salary BETWEEN :minSalary AND :maxSalary ORDER BY e.salary DESC"
        ),
        @NamedQuery(
            name = "Employee.countByDepartment",
            query = "SELECT COUNT(e) FROM Employee e WHERE e.department = :department"
        ),
        @NamedQuery(
            name = "Employee.findTopEarners",
            query = "SELECT e FROM Employee e WHERE e.active = true ORDER BY e.salary DESC"
        ),
        @NamedQuery(
            name = "Employee.findByDepartmentAndActive",
            query = "SELECT e FROM Employee e WHERE e.department = :department AND e.active = :active"
        ),
        @NamedQuery(
            name = "Employee.updateSalaryByDepartment",
            query = "UPDATE Employee e SET e.salary = e.salary * :multiplier WHERE e.department = :department"
        )
    })
    @NamedNativeQueries({
        @NamedNativeQuery(
            name = "Employee.findAllNative",
            query = "SELECT * FROM employees WHERE active = true",
            resultClass = Employee.class
        ),
        @NamedNativeQuery(
            name = "Employee.findAverageSalaryByDepartment",
            query = "SELECT department, AVG(salary) as avgSalary FROM employees GROUP BY department",
            resultSetMapping = "DepartmentSalaryMapping"
        )
    })
    @SqlResultSetMappings({
        @SqlResultSetMapping(
            name = "DepartmentSalaryMapping",
            classes = @ConstructorResult(
                targetClass = DepartmentSalary.class,
                columns = {
                    @ColumnResult(name = "department", type = String.class),
                    @ColumnResult(name = "avgSalary", type = Double.class)
                }
            )
        )
    })
    public static class Employee {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @Column(name = "first_name", nullable = false)
        private String firstName;
        
        @Column(name = "last_name", nullable = false)
        private String lastName;
        
        @Column(nullable = false, unique = true)
        private String email;
        
        @Column(nullable = false)
        private String department;
        
        @Column(nullable = false)
        private BigDecimal salary;
        
        @Column(name = "hire_date", nullable = false)
        private LocalDateTime hireDate;
        
        @Column(nullable = false)
        private Boolean active;
        
        private String position;
        
        @Column(name = "manager_id")
        private Long managerId;
        
        // Constructors
        public Employee() {
            this.active = true;
            this.hireDate = LocalDateTime.now();
        }
        
        public Employee(String firstName, String lastName, String email, 
                       String department, BigDecimal salary, String position) {
            this();
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.department = department;
            this.salary = salary;
            this.position = position;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        
        public BigDecimal getSalary() { return salary; }
        public void setSalary(BigDecimal salary) { this.salary = salary; }
        
        public LocalDateTime getHireDate() { return hireDate; }
        public void setHireDate(LocalDateTime hireDate) { this.hireDate = hireDate; }
        
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        
        public Long getManagerId() { return managerId; }
        public void setManagerId(Long managerId) { this.managerId = managerId; }
    }
    
    /**
     * DTO for department salary statistics
     */
    public static class DepartmentSalary {
        private String department;
        private Double avgSalary;
        
        public DepartmentSalary(String department, Double avgSalary) {
            this.department = department;
            this.avgSalary = avgSalary;
        }
        
        // Getters
        public String getDepartment() { return department; }
        public Double getAvgSalary() { return avgSalary; }
    }
    
    /**
     * Repository using Named Queries
     * 
     * Spring Data JPA automatically detects and uses named queries
     * when method names match the pattern: EntityName.methodName
     */
    @Repository
    public interface EmployeeRepository extends JpaRepository<Employee, Long> {
        
        // Automatically uses Employee.findByDepartment named query
        List<Employee> findByDepartment(String department);
        
        // Automatically uses Employee.findActiveEmployees named query
        List<Employee> findActiveEmployees();
        
        // Automatically uses Employee.findByLastNameContaining named query
        List<Employee> findByLastNameContaining(String lastName);
        
        // Automatically uses Employee.findBySalaryRange named query
        List<Employee> findBySalaryRange(BigDecimal minSalary, BigDecimal maxSalary);
        
        // Automatically uses Employee.countByDepartment named query
        Long countByDepartment(String department);
        
        // Automatically uses Employee.findTopEarners named query
        List<Employee> findTopEarners();
        
        // Automatically uses Employee.findByDepartmentAndActive named query
        List<Employee> findByDepartmentAndActive(String department, Boolean active);
    }
    
    /**
     * Service using Named Queries
     */
    @Service
    @Transactional
    public static class EmployeeNamedQueryService {
        
        @PersistenceContext
        private EntityManager entityManager;
        
        private final EmployeeRepository employeeRepository;
        
        public EmployeeNamedQueryService(EmployeeRepository employeeRepository) {
            this.employeeRepository = employeeRepository;
        }
        
        /**
         * Using named query through repository
         */
        public List<Employee> getEmployeesByDepartment(String department) {
            return employeeRepository.findByDepartment(department);
        }
        
        /**
         * Using named query directly with EntityManager
         */
        public List<Employee> getActiveEmployees() {
            TypedQuery<Employee> query = entityManager.createNamedQuery(
                "Employee.findActiveEmployees", Employee.class);
            return query.getResultList();
        }
        
        /**
         * Named query with parameter
         */
        public List<Employee> searchByLastName(String lastName) {
            return employeeRepository.findByLastNameContaining(lastName);
        }
        
        /**
         * Named query with multiple parameters
         */
        public List<Employee> getEmployeesBySalaryRange(BigDecimal minSalary, BigDecimal maxSalary) {
            return employeeRepository.findBySalaryRange(minSalary, maxSalary);
        }
        
        /**
         * Named query with pagination
         */
        public List<Employee> getTopEarners(int limit) {
            TypedQuery<Employee> query = entityManager.createNamedQuery(
                "Employee.findTopEarners", Employee.class);
            query.setMaxResults(limit);
            return query.getResultList();
        }
        
        /**
         * Named count query
         */
        public Long countDepartmentEmployees(String department) {
            return employeeRepository.countByDepartment(department);
        }
        
        /**
         * Named native query
         */
        public List<Employee> getAllEmployeesNative() {
            Query query = entityManager.createNamedQuery("Employee.findAllNative");
            return query.getResultList();
        }
        
        /**
         * Named native query with result set mapping
         */
        public List<DepartmentSalary> getAverageSalaryByDepartment() {
            Query query = entityManager.createNamedQuery("Employee.findAverageSalaryByDepartment");
            return query.getResultList();
        }
        
        /**
         * Named update query
         */
        public int increaseSalaryByDepartment(String department, BigDecimal multiplier) {
            Query query = entityManager.createNamedQuery("Employee.updateSalaryByDepartment");
            query.setParameter("department", department);
            query.setParameter("multiplier", multiplier);
            return query.executeUpdate();
        }
        
        /**
         * Combining named query with additional filtering
         */
        public List<Employee> getActiveDepartmentEmployees(String department) {
            return employeeRepository.findByDepartmentAndActive(department, true);
        }
        
        /**
         * Dynamic execution of named queries
         */
        public List<Employee> executeNamedQuery(String queryName) {
            TypedQuery<Employee> query = entityManager.createNamedQuery(queryName, Employee.class);
            return query.getResultList();
        }
        
        /**
         * Named query with cache hint
         */
        public List<Employee> getCachedDepartmentEmployees(String department) {
            TypedQuery<Employee> query = entityManager.createNamedQuery(
                "Employee.findByDepartment", Employee.class);
            query.setParameter("department", department);
            query.setHint("org.hibernate.cacheable", true);
            return query.getResultList();
        }
    }
    
    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/namedquery/employees")
    public static class EmployeeNamedQueryController {
        
        private final EmployeeNamedQueryService namedQueryService;
        private final EmployeeRepository employeeRepository;
        
        public EmployeeNamedQueryController(EmployeeNamedQueryService namedQueryService,
                                           EmployeeRepository employeeRepository) {
            this.namedQueryService = namedQueryService;
            this.employeeRepository = employeeRepository;
        }
        
        @PostMapping
        public Employee create(@RequestBody Employee employee) {
            return employeeRepository.save(employee);
        }
        
        @GetMapping("/department/{department}")
        public List<Employee> getByDepartment(@PathVariable String department) {
            return namedQueryService.getEmployeesByDepartment(department);
        }
        
        @GetMapping("/active")
        public List<Employee> getActive() {
            return namedQueryService.getActiveEmployees();
        }
        
        @GetMapping("/search/lastname/{lastName}")
        public List<Employee> searchByLastName(@PathVariable String lastName) {
            return namedQueryService.searchByLastName(lastName);
        }
        
        @GetMapping("/salary-range")
        public List<Employee> getBySalaryRange(
                @RequestParam BigDecimal minSalary,
                @RequestParam BigDecimal maxSalary) {
            return namedQueryService.getEmployeesBySalaryRange(minSalary, maxSalary);
        }
        
        @GetMapping("/top-earners")
        public List<Employee> getTopEarners(@RequestParam(defaultValue = "10") int limit) {
            return namedQueryService.getTopEarners(limit);
        }
        
        @GetMapping("/count/department/{department}")
        public Long countByDepartment(@PathVariable String department) {
            return namedQueryService.countDepartmentEmployees(department);
        }
        
        @GetMapping("/stats/salary-by-department")
        public List<DepartmentSalary> getSalaryStats() {
            return namedQueryService.getAverageSalaryByDepartment();
        }
        
        @PutMapping("/increase-salary/{department}")
        public int increaseSalary(
                @PathVariable String department,
                @RequestParam BigDecimal multiplier) {
            return namedQueryService.increaseSalaryByDepartment(department, multiplier);
        }
    }
}

/**
 * Configuration (application.properties):
 * 
 * spring.datasource.url=jdbc:h2:mem:namedquerydb
 * spring.datasource.driver-class-name=org.h2.Driver
 * spring.jpa.hibernate.ddl-auto=create-drop
 * spring.jpa.show-sql=true
 * spring.jpa.properties.hibernate.format_sql=true
 * 
 * # Enable query caching for named queries
 * spring.jpa.properties.hibernate.cache.use_query_cache=true
 * spring.jpa.properties.hibernate.cache.use_second_level_cache=true
 * 
 * Best Practices:
 * 1. Define named queries close to the entity they query
 * 2. Use meaningful query names (EntityName.operation)
 * 3. Leverage query validation at startup
 * 4. Use @NamedNativeQuery for database-specific optimizations
 * 5. Combine with query caching for better performance
 * 6. Document query purpose and parameters
 * 7. Use result set mappings for complex native queries
 * 8. Test named queries with various parameter combinations
 * 9. Externalize large queries to orm.xml if needed
 * 10. Monitor query performance in production
 * 
 * Advantages over JPQL in repository methods:
 * - Queries validated at application startup
 * - Centralized query definitions
 * - Better for complex, reusable queries
 * - Can be cached more effectively
 * - Easier to maintain large queries
 * 
 * Disadvantages:
 * - Less flexible than dynamic queries
 * - More verbose setup
 * - Harder to modify at runtime
 * - Entity class can become cluttered
 */
