package com.example.orm.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Simple JDBC Insert Pattern
 * 
 * Purpose:
 * - Simplified insert operations
 * - Automatic key generation handling
 * - Metadata-driven inserts
 * - Batch insert support
 * 
 * Features:
 * 1. Automatic column detection
 * 2. Generated key retrieval
 * 3. Named parameter support
 * 4. Batch insert operations
 * 5. Table metadata caching
 * 6. Type conversion
 * 7. No SQL writing for simple inserts
 * 
 * When to Use:
 * - Simple insert operations
 * - Need auto-generated keys
 * - Minimize boilerplate code
 * - Batch insertions
 * - Metadata-driven approach
 * 
 * Benefits:
 * - No SQL for basic inserts
 * - Automatic key handling
 * - Less code to maintain
 * - Metadata caching
 * - Batch support
 */
@SpringBootApplication
public class SimpleJDBCInsertPattern {

    public static void main(String[] args) {
        SpringApplication.run(SimpleJDBCInsertPattern.class, args);
        System.out.println("Simple JDBC Insert Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/simple-insert/students");
    }

    /**
     * Student Entity
     */
    public static class Student {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String major;
        private Integer enrollmentYear;
        private Double gpa;

        // Constructors
        public Student() {}

        public Student(String firstName, String lastName, String email, 
                      String major, Integer enrollmentYear, Double gpa) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.major = major;
            this.enrollmentYear = enrollmentYear;
            this.gpa = gpa;
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
        
        public String getMajor() { return major; }
        public void setMajor(String major) { this.major = major; }
        
        public Integer getEnrollmentYear() { return enrollmentYear; }
        public void setEnrollmentYear(Integer enrollmentYear) { this.enrollmentYear = enrollmentYear; }
        
        public Double getGpa() { return gpa; }
        public void setGpa(Double gpa) { this.gpa = gpa; }
    }

    /**
     * Configuration
     */
    @Configuration
    public static class SimpleJdbcInsertConfig {

        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        public SimpleJdbcInsert simpleJdbcInsert(DataSource dataSource) {
            return new SimpleJdbcInsert(dataSource)
                    .withTableName("students")
                    .usingGeneratedKeyColumns("id");
        }

        @Bean
        public DataSourceTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    /**
     * Student Repository using SimpleJdbcInsert
     */
    @Repository
    public static class StudentRepository {

        private final SimpleJdbcInsert simpleJdbcInsert;
        private final JdbcTemplate jdbcTemplate;

        public StudentRepository(SimpleJdbcInsert simpleJdbcInsert, JdbcTemplate jdbcTemplate) {
            this.simpleJdbcInsert = simpleJdbcInsert;
            this.jdbcTemplate = jdbcTemplate;
            initializeTable();
        }

        private void initializeTable() {
            String sql = "CREATE TABLE IF NOT EXISTS students (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "first_name VARCHAR(50) NOT NULL, " +
                    "last_name VARCHAR(50) NOT NULL, " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "major VARCHAR(50), " +
                    "enrollment_year INT, " +
                    "gpa DECIMAL(3,2))";
            jdbcTemplate.execute(sql);
        }

        /**
         * Insert using Map
         */
        @Transactional
        public Student save(Student student) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("first_name", student.getFirstName());
            parameters.put("last_name", student.getLastName());
            parameters.put("email", student.getEmail());
            parameters.put("major", student.getMajor());
            parameters.put("enrollment_year", student.getEnrollmentYear());
            parameters.put("gpa", student.getGpa());
            
            // Execute insert and get generated key
            Number newId = simpleJdbcInsert.executeAndReturnKey(parameters);
            student.setId(newId.longValue());
            
            return student;
        }

        /**
         * Insert with specific columns
         */
        @Transactional
        public Long insertWithColumns(Student student) {
            SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("students")
                    .usingColumns("first_name", "last_name", "email", "major")
                    .usingGeneratedKeyColumns("id");
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("first_name", student.getFirstName());
            parameters.put("last_name", student.getLastName());
            parameters.put("email", student.getEmail());
            parameters.put("major", student.getMajor());
            
            Number newId = insert.executeAndReturnKey(parameters);
            return newId.longValue();
        }

        /**
         * Batch insert
         */
        @Transactional
        public int[] batchInsert(List<Student> students) {
            List<Map<String, Object>> batchValues = new ArrayList<>();
            
            for (Student student : students) {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("first_name", student.getFirstName());
                parameters.put("last_name", student.getLastName());
                parameters.put("email", student.getEmail());
                parameters.put("major", student.getMajor());
                parameters.put("enrollment_year", student.getEnrollmentYear());
                parameters.put("gpa", student.getGpa());
                batchValues.add(parameters);
            }
            
            return simpleJdbcInsert.executeBatch(batchValues.toArray(new Map[0]));
        }

        /**
         * Insert using SqlParameterSource
         */
        @Transactional
        public Long insertWithParamSource(Student student) {
            org.springframework.jdbc.core.namedparam.MapSqlParameterSource params = 
                    new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                    .addValue("first_name", student.getFirstName())
                    .addValue("last_name", student.getLastName())
                    .addValue("email", student.getEmail())
                    .addValue("major", student.getMajor())
                    .addValue("enrollment_year", student.getEnrollmentYear())
                    .addValue("gpa", student.getGpa());
            
            Number newId = simpleJdbcInsert.executeAndReturnKey(params);
            return newId.longValue();
        }

        /**
         * Insert and return multiple generated keys
         */
        @Transactional
        public Map<String, Object> insertAndReturnKeys(Student student) {
            SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("students")
                    .usingGeneratedKeyColumns("id");
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("first_name", student.getFirstName());
            parameters.put("last_name", student.getLastName());
            parameters.put("email", student.getEmail());
            parameters.put("major", student.getMajor());
            parameters.put("enrollment_year", student.getEnrollmentYear());
            parameters.put("gpa", student.getGpa());
            
            return insert.executeAndReturnKeyHolder(parameters).getKeys();
        }

        /**
         * Find by ID (using regular JdbcTemplate)
         */
        public Optional<Student> findById(Long id) {
            String sql = "SELECT * FROM students WHERE id = ?";
            try {
                Student student = jdbcTemplate.queryForObject(sql, this::mapRow, id);
                return Optional.ofNullable(student);
            } catch (Exception e) {
                return Optional.empty();
            }
        }

        /**
         * Find all
         */
        public List<Student> findAll() {
            String sql = "SELECT * FROM students";
            return jdbcTemplate.query(sql, this::mapRow);
        }

        /**
         * Find by major
         */
        public List<Student> findByMajor(String major) {
            String sql = "SELECT * FROM students WHERE major = ?";
            return jdbcTemplate.query(sql, this::mapRow, major);
        }

        /**
         * Find top students
         */
        public List<Student> findTopStudents(int limit) {
            String sql = "SELECT * FROM students ORDER BY gpa DESC LIMIT ?";
            return jdbcTemplate.query(sql, this::mapRow, limit);
        }

        /**
         * Update student
         */
        @Transactional
        public int update(Student student) {
            String sql = "UPDATE students SET first_name = ?, last_name = ?, " +
                    "email = ?, major = ?, enrollment_year = ?, gpa = ? WHERE id = ?";
            return jdbcTemplate.update(sql, 
                    student.getFirstName(), 
                    student.getLastName(),
                    student.getEmail(), 
                    student.getMajor(), 
                    student.getEnrollmentYear(),
                    student.getGpa(),
                    student.getId());
        }

        /**
         * Delete student
         */
        @Transactional
        public int delete(Long id) {
            String sql = "DELETE FROM students WHERE id = ?";
            return jdbcTemplate.update(sql, id);
        }

        /**
         * Count all
         */
        public Long count() {
            String sql = "SELECT COUNT(*) FROM students";
            return jdbcTemplate.queryForObject(sql, Long.class);
        }

        /**
         * Get average GPA
         */
        public Double getAverageGpa() {
            String sql = "SELECT AVG(gpa) FROM students";
            return jdbcTemplate.queryForObject(sql, Double.class);
        }

        /**
         * Row Mapper
         */
        private Student mapRow(ResultSet rs, int rowNum) throws SQLException {
            Student student = new Student();
            student.setId(rs.getLong("id"));
            student.setFirstName(rs.getString("first_name"));
            student.setLastName(rs.getString("last_name"));
            student.setEmail(rs.getString("email"));
            student.setMajor(rs.getString("major"));
            student.setEnrollmentYear(rs.getInt("enrollment_year"));
            student.setGpa(rs.getDouble("gpa"));
            return student;
        }
    }

    /**
     * Student Service
     */
    @Service
    public static class StudentService {

        private final StudentRepository repository;

        public StudentService(StudentRepository repository) {
            this.repository = repository;
        }

        public Student createStudent(Student student) {
            return repository.save(student);
        }

        public void batchCreateStudents(List<Student> students) {
            repository.batchInsert(students);
        }

        public Optional<Student> getStudent(Long id) {
            return repository.findById(id);
        }

        public List<Student> getAllStudents() {
            return repository.findAll();
        }

        public List<Student> getStudentsByMajor(String major) {
            return repository.findByMajor(major);
        }

        public List<Student> getTopStudents(int limit) {
            return repository.findTopStudents(limit);
        }

        public void updateStudent(Student student) {
            repository.update(student);
        }

        public void deleteStudent(Long id) {
            repository.delete(id);
        }

        public Long getTotalStudents() {
            return repository.count();
        }

        public Double getAverageGpa() {
            return repository.getAverageGpa();
        }
    }

    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/simple-insert")
    public static class StudentController {

        private final StudentService service;

        public StudentController(StudentService service) {
            this.service = service;
        }

        @PostMapping("/students")
        public Student createStudent(@RequestBody Student student) {
            return service.createStudent(student);
        }

        @PostMapping("/students/batch")
        public void batchCreate(@RequestBody List<Student> students) {
            service.batchCreateStudents(students);
        }

        @GetMapping("/students/{id}")
        public Optional<Student> getStudent(@PathVariable Long id) {
            return service.getStudent(id);
        }

        @GetMapping("/students")
        public List<Student> getAllStudents() {
            return service.getAllStudents();
        }

        @GetMapping("/students/major/{major}")
        public List<Student> getByMajor(@PathVariable String major) {
            return service.getStudentsByMajor(major);
        }

        @GetMapping("/students/top")
        public List<Student> getTopStudents(@RequestParam(defaultValue = "10") int limit) {
            return service.getTopStudents(limit);
        }

        @PutMapping("/students/{id}")
        public void updateStudent(@PathVariable Long id, @RequestBody Student student) {
            student.setId(id);
            service.updateStudent(student);
        }

        @DeleteMapping("/students/{id}")
        public void deleteStudent(@PathVariable Long id) {
            service.deleteStudent(id);
        }

        @GetMapping("/students/count")
        public Long getCount() {
            return service.getTotalStudents();
        }

        @GetMapping("/students/average-gpa")
        public Double getAverageGpa() {
            return service.getAverageGpa();
        }
    }
}

/**
 * Best Practices:
 * 
 * 1. Use SimpleJdbcInsert for simple insert operations
 * 2. Configure table name and key columns once
 * 3. Use executeBatch for bulk inserts
 * 4. Leverage metadata caching
 * 5. Use usingColumns() to limit columns
 * 6. Handle generated keys appropriately
 * 7. Combine with regular JdbcTemplate for complex operations
 * 8. Monitor metadata cache performance
 */
