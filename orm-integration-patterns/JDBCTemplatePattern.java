package com.example.orm.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Template Pattern
 * 
 * Purpose:
 * - Simplified JDBC operations with Spring
 * - Exception translation
 * - Resource management
 * - Template method pattern for database access
 * 
 * Features:
 * 1. Automatic resource cleanup
 * 2. Exception translation to DataAccessException
 * 3. PreparedStatement support
 * 4. RowMapper for result mapping
 * 5. Batch operations
 * 6. Named parameters support
 * 7. CallableStatement support
 * 8. Transaction integration
 * 
 * When to Use:
 * - Simple database operations
 * - Legacy JDBC code migration
 * - No ORM overhead needed
 * - Direct SQL control
 * - Stored procedure calls
 * - Batch processing
 * 
 * Benefits:
 * - Simplified JDBC code
 * - Automatic resource management
 * - Better exception handling
 * - Transaction support
 * - Thread-safe
 * - Lightweight
 */
@SpringBootApplication
public class JDBCTemplatePattern {

    public static void main(String[] args) {
        SpringApplication.run(JDBCTemplatePattern.class, args);
        System.out.println("JDBC Template Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/jdbc/employees");
    }

    /**
     * Employee Entity
     */
    public static class Employee {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String department;
        private Double salary;
        private Date hireDate;

        // Constructors
        public Employee() {}

        public Employee(String firstName, String lastName, String email, 
                       String department, Double salary) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.department = department;
            this.salary = salary;
            this.hireDate = new Date(System.currentTimeMillis());
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
        
        public Double getSalary() { return salary; }
        public void setSalary(Double salary) { this.salary = salary; }
        
        public Date getHireDate() { return hireDate; }
        public void setHireDate(Date hireDate) { this.hireDate = hireDate; }
    }

    /**
     * Department Statistics DTO
     */
    public static class DepartmentStats {
        private String department;
        private Long employeeCount;
        private Double avgSalary;
        private Double totalSalary;

        public DepartmentStats() {}

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        
        public Long getEmployeeCount() { return employeeCount; }
        public void setEmployeeCount(Long employeeCount) { this.employeeCount = employeeCount; }
        
        public Double getAvgSalary() { return avgSalary; }
        public void setAvgSalary(Double avgSalary) { this.avgSalary = avgSalary; }
        
        public Double getTotalSalary() { return totalSalary; }
        public void setTotalSalary(Double totalSalary) { this.totalSalary = totalSalary; }
    }

    /**
     * JDBC Configuration
     */
    @Configuration
    public static class JdbcConfig {

        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        public DataSourceTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    /**
     * RowMapper for Employee
     */
    public static class EmployeeRowMapper implements RowMapper<Employee> {
        @Override
        public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
            Employee employee = new Employee();
            employee.setId(rs.getLong("id"));
            employee.setFirstName(rs.getString("first_name"));
            employee.setLastName(rs.getString("last_name"));
            employee.setEmail(rs.getString("email"));
            employee.setDepartment(rs.getString("department"));
            employee.setSalary(rs.getDouble("salary"));
            employee.setHireDate(rs.getDate("hire_date"));
            return employee;
        }
    }

    /**
     * RowMapper for DepartmentStats
     */
    public static class DepartmentStatsRowMapper implements RowMapper<DepartmentStats> {
        @Override
        public DepartmentStats mapRow(ResultSet rs, int rowNum) throws SQLException {
            DepartmentStats stats = new DepartmentStats();
            stats.setDepartment(rs.getString("department"));
            stats.setEmployeeCount(rs.getLong("employee_count"));
            stats.setAvgSalary(rs.getDouble("avg_salary"));
            stats.setTotalSalary(rs.getDouble("total_salary"));
            return stats;
        }
    }

    /**
     * Employee Repository using JdbcTemplate
     */
    @Repository
    public static class EmployeeRepository {

        private final JdbcTemplate jdbcTemplate;
        private final RowMapper<Employee> rowMapper;

        public EmployeeRepository(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
            this.rowMapper = new EmployeeRowMapper();
            initializeTable();
        }

        private void initializeTable() {
            String sql = "CREATE TABLE IF NOT EXISTS employees (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "first_name VARCHAR(50) NOT NULL, " +
                    "last_name VARCHAR(50) NOT NULL, " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "department VARCHAR(50), " +
                    "salary DECIMAL(10,2), " +
                    "hire_date DATE)";
            jdbcTemplate.execute(sql);
        }

        /**
         * Insert employee using update()
         */
        @Transactional
        public Employee save(Employee employee) {
            String sql = "INSERT INTO employees (first_name, last_name, email, department, salary, hire_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            
            KeyHolder keyHolder = new GeneratedKeyHolder();
            
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, employee.getFirstName());
                ps.setString(2, employee.getLastName());
                ps.setString(3, employee.getEmail());
                ps.setString(4, employee.getDepartment());
                ps.setDouble(5, employee.getSalary());
                ps.setDate(6, employee.getHireDate());
                return ps;
            }, keyHolder);
            
            employee.setId(keyHolder.getKey().longValue());
            return employee;
        }

        /**
         * Find by ID using queryForObject()
         */
        public Optional<Employee> findById(Long id) {
            String sql = "SELECT * FROM employees WHERE id = ?";
            try {
                Employee employee = jdbcTemplate.queryForObject(sql, rowMapper, id);
                return Optional.ofNullable(employee);
            } catch (DataAccessException e) {
                return Optional.empty();
            }
        }

        /**
         * Find all using query()
         */
        public List<Employee> findAll() {
            String sql = "SELECT * FROM employees";
            return jdbcTemplate.query(sql, rowMapper);
        }

        /**
         * Find by department
         */
        public List<Employee> findByDepartment(String department) {
            String sql = "SELECT * FROM employees WHERE department = ?";
            return jdbcTemplate.query(sql, rowMapper, department);
        }

        /**
         * Find by salary range
         */
        public List<Employee> findBySalaryRange(Double minSalary, Double maxSalary) {
            String sql = "SELECT * FROM employees WHERE salary BETWEEN ? AND ?";
            return jdbcTemplate.query(sql, rowMapper, minSalary, maxSalary);
        }

        /**
         * Update employee
         */
        @Transactional
        public int update(Employee employee) {
            String sql = "UPDATE employees SET first_name = ?, last_name = ?, email = ?, " +
                    "department = ?, salary = ? WHERE id = ?";
            return jdbcTemplate.update(sql, 
                    employee.getFirstName(), 
                    employee.getLastName(),
                    employee.getEmail(), 
                    employee.getDepartment(), 
                    employee.getSalary(),
                    employee.getId());
        }

        /**
         * Update salary
         */
        @Transactional
        public int updateSalary(Long id, Double newSalary) {
            String sql = "UPDATE employees SET salary = ? WHERE id = ?";
            return jdbcTemplate.update(sql, newSalary, id);
        }

        /**
         * Delete employee
         */
        @Transactional
        public int delete(Long id) {
            String sql = "DELETE FROM employees WHERE id = ?";
            return jdbcTemplate.update(sql, id);
        }

        /**
         * Count all employees using queryForObject()
         */
        public Long count() {
            String sql = "SELECT COUNT(*) FROM employees";
            return jdbcTemplate.queryForObject(sql, Long.class);
        }

        /**
         * Count by department
         */
        public Long countByDepartment(String department) {
            String sql = "SELECT COUNT(*) FROM employees WHERE department = ?";
            return jdbcTemplate.queryForObject(sql, Long.class, department);
        }

        /**
         * Get average salary
         */
        public Double getAverageSalary() {
            String sql = "SELECT AVG(salary) FROM employees";
            return jdbcTemplate.queryForObject(sql, Double.class);
        }

        /**
         * Get department statistics
         */
        public List<DepartmentStats> getDepartmentStatistics() {
            String sql = "SELECT department, " +
                    "COUNT(*) as employee_count, " +
                    "AVG(salary) as avg_salary, " +
                    "SUM(salary) as total_salary " +
                    "FROM employees " +
                    "GROUP BY department";
            return jdbcTemplate.query(sql, new DepartmentStatsRowMapper());
        }

        /**
         * Batch insert using batchUpdate()
         */
        @Transactional
        public int[] batchInsert(List<Employee> employees) {
            String sql = "INSERT INTO employees (first_name, last_name, email, department, salary, hire_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            
            return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Employee emp = employees.get(i);
                    ps.setString(1, emp.getFirstName());
                    ps.setString(2, emp.getLastName());
                    ps.setString(3, emp.getEmail());
                    ps.setString(4, emp.getDepartment());
                    ps.setDouble(5, emp.getSalary());
                    ps.setDate(6, emp.getHireDate());
                }

                @Override
                public int getBatchSize() {
                    return employees.size();
                }
            });
        }

        /**
         * Batch update salaries
         */
        @Transactional
        public int[] batchUpdateSalaries(List<Long> ids, List<Double> salaries) {
            String sql = "UPDATE employees SET salary = ? WHERE id = ?";
            
            List<Object[]> batchArgs = new java.util.ArrayList<>();
            for (int i = 0; i < ids.size(); i++) {
                batchArgs.add(new Object[]{salaries.get(i), ids.get(i)});
            }
            
            return jdbcTemplate.batchUpdate(sql, batchArgs);
        }

        /**
         * Execute custom SQL
         */
        public void executeCustomSql(String sql) {
            jdbcTemplate.execute(sql);
        }

        /**
         * Query with RowCallbackHandler (for large result sets)
         */
        public void processLargeResultSet(RowCallbackHandler handler) {
            String sql = "SELECT * FROM employees";
            jdbcTemplate.query(sql, handler);
        }

        /**
         * Query with ResultSetExtractor
         */
        public List<String> getEmployeeEmails() {
            String sql = "SELECT email FROM employees";
            return jdbcTemplate.query(sql, new ResultSetExtractor<List<String>>() {
                @Override
                public List<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
                    List<String> emails = new java.util.ArrayList<>();
                    while (rs.next()) {
                        emails.add(rs.getString("email"));
                    }
                    return emails;
                }
            });
        }
    }

    /**
     * Employee Service
     */
    @Service
    public static class EmployeeService {

        private final EmployeeRepository repository;

        public EmployeeService(EmployeeRepository repository) {
            this.repository = repository;
        }

        public Employee createEmployee(Employee employee) {
            return repository.save(employee);
        }

        public void batchCreateEmployees(List<Employee> employees) {
            repository.batchInsert(employees);
        }

        public Optional<Employee> getEmployee(Long id) {
            return repository.findById(id);
        }

        public List<Employee> getAllEmployees() {
            return repository.findAll();
        }

        public List<Employee> getEmployeesByDepartment(String department) {
            return repository.findByDepartment(department);
        }

        public List<Employee> getEmployeesBySalaryRange(Double min, Double max) {
            return repository.findBySalaryRange(min, max);
        }

        public void updateEmployee(Employee employee) {
            repository.update(employee);
        }

        public void updateSalary(Long id, Double salary) {
            repository.updateSalary(id, salary);
        }

        public void deleteEmployee(Long id) {
            repository.delete(id);
        }

        public Long getTotalEmployees() {
            return repository.count();
        }

        public Double getAverageSalary() {
            return repository.getAverageSalary();
        }

        public List<DepartmentStats> getDepartmentStats() {
            return repository.getDepartmentStatistics();
        }

        public List<String> getAllEmails() {
            return repository.getEmployeeEmails();
        }
    }

    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/jdbc")
    public static class EmployeeController {

        private final EmployeeService service;

        public EmployeeController(EmployeeService service) {
            this.service = service;
        }

        @PostMapping("/employees")
        public Employee createEmployee(@RequestBody Employee employee) {
            return service.createEmployee(employee);
        }

        @PostMapping("/employees/batch")
        public void batchCreate(@RequestBody List<Employee> employees) {
            service.batchCreateEmployees(employees);
        }

        @GetMapping("/employees/{id}")
        public Optional<Employee> getEmployee(@PathVariable Long id) {
            return service.getEmployee(id);
        }

        @GetMapping("/employees")
        public List<Employee> getAllEmployees() {
            return service.getAllEmployees();
        }

        @GetMapping("/employees/department/{department}")
        public List<Employee> getByDepartment(@PathVariable String department) {
            return service.getEmployeesByDepartment(department);
        }

        @GetMapping("/employees/salary-range")
        public List<Employee> getBySalaryRange(@RequestParam Double min, @RequestParam Double max) {
            return service.getEmployeesBySalaryRange(min, max);
        }

        @PutMapping("/employees/{id}")
        public void updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
            employee.setId(id);
            service.updateEmployee(employee);
        }

        @PutMapping("/employees/{id}/salary")
        public void updateSalary(@PathVariable Long id, @RequestParam Double salary) {
            service.updateSalary(id, salary);
        }

        @DeleteMapping("/employees/{id}")
        public void deleteEmployee(@PathVariable Long id) {
            service.deleteEmployee(id);
        }

        @GetMapping("/employees/count")
        public Long getCount() {
            return service.getTotalEmployees();
        }

        @GetMapping("/employees/average-salary")
        public Double getAvgSalary() {
            return service.getAverageSalary();
        }

        @GetMapping("/employees/department-stats")
        public List<DepartmentStats> getDepartmentStats() {
            return service.getDepartmentStats();
        }

        @GetMapping("/employees/emails")
        public List<String> getEmails() {
            return service.getAllEmails();
        }
    }
}

/**
 * Best Practices:
 * 
 * 1. Use PreparedStatements to prevent SQL injection
 * 2. Implement RowMapper for result set mapping
 * 3. Use KeyHolder for generated keys
 * 4. Leverage batch operations for bulk inserts/updates
 * 5. Use @Transactional for data integrity
 * 6. Handle DataAccessException appropriately
 * 7. Use RowCallbackHandler for large result sets
 * 8. Configure connection pool properly
 * 9. Monitor SQL performance
 * 10. Use NamedParameterJdbcTemplate for complex queries
 */
