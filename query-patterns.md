### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 1:56:21 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **Query Patterns**
> - Query DSL Pattern
> - Criteria API Pattern
> - Named Query Pattern
> - Native Query Pattern
> - JPQL Pattern
> - Query by Example Pattern
> - Specification Pattern
> - Querydsl Predicate Pattern
> - Dynamic Query Pattern
> - Stored Procedure Pattern
> ```

#### _Assistant_

> I'll create a comprehensive Spring Boot application demonstrating all these query patterns using JPA/Hibernate.
> 
> ## Project Structure
> 
> ```java src/main/java/com/example/querypatterns/QueryPatternsApplication.java
> package com.example.querypatterns;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> 
> @SpringBootApplication
> public class QueryPatternsApplication {
>     public static void main(String[] args) {
>         SpringApplication.run(QueryPatternsApplication.class, args);
>     }
> }
> ```
> 
> ## Entity
> 
> ```java src/main/java/com/example/querypatterns/entity/Employee.java
> package com.example.querypatterns.entity;
> 
> import jakarta.persistence.*;
> import lombok.AllArgsConstructor;
> import lombok.Data;
> import lombok.NoArgsConstructor;
> 
> import java.math.BigDecimal;
> import java.time.LocalDate;
> 
> @Entity
> @Table(name = "employees")
> @Data
> @NoArgsConstructor
> @AllArgsConstructor
> @NamedQueries({
>     @NamedQuery(
>         name = "Employee.findByDepartment",
>         query = "SELECT e FROM Employee e WHERE e.department = :department"
>     ),
>     @NamedQuery(
>         name = "Employee.findHighEarners",
>         query = "SELECT e FROM Employee e WHERE e.salary > :minSalary ORDER BY e.salary DESC"
>     )
> })
> @NamedStoredProcedureQuery(
>     name = "Employee.getEmployeesByDepartment",
>     procedureName = "get_employees_by_department",
>     parameters = {
>         @StoredProcedureParameter(mode = ParameterMode.IN, name = "dept_name", type = String.class),
>         @StoredProcedureParameter(mode = ParameterMode.REF_CURSOR, name = "result", type = void.class)
>     }
> )
> public class Employee {
>     
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>     
>     @Column(nullable = false)
>     private String firstName;
>     
>     @Column(nullable = false)
>     private String lastName;
>     
>     private String email;
>     
>     private String department;
>     
>     private BigDecimal salary;
>     
>     private LocalDate hireDate;
>     
>     private String position;
>     
>     private Boolean active;
> }
> ```
> 
> ## 1. Query DSL Pattern (Querydsl)
> 
> ```java src/main/java/com/example/querypatterns/repository/QueryDslEmployeeRepository.java
> package com.example.querypatterns.repository;
> 
> import com.example.querypatterns.entity.Employee;
> import com.example.querypatterns.entity.QEmployee;
> import com.querydsl.core.types.dsl.BooleanExpression;
> import com.querydsl.jpa.impl.JPAQueryFactory;
> import jakarta.persistence.EntityManager;
> import lombok.RequiredArgsConstructor;
> import org.springframework.stereotype.Repository;
> 
> import java.math.BigDecimal;
> import java.time.LocalDate;
> import java.util.List;
> 
> @Repository
> @RequiredArgsConstructor
> public class QueryDslEmployeeRepository {
>     
>     private final JPAQueryFactory queryFactory;
>     private final QEmployee qEmployee = QEmployee.employee;
>     
>     public List<Employee> findByComplexCriteria(String department, BigDecimal minSalary, LocalDate fromDate) {
>         BooleanExpression predicate = qEmployee.active.isTrue();
>         
>         if (department != null) {
>             predicate = predicate.and(qEmployee.department.eq(department));
>         }
>         
>         if (minSalary != null) {
>             predicate = predicate.and(qEmployee.salary.goe(minSalary));
>         }
>         
>         if (fromDate != null) {
>             predicate = predicate.and(qEmployee.hireDate.goe(fromDate));
>         }
>         
>         return queryFactory
>             .selectFrom(qEmployee)
>             .where(predicate)
>             .orderBy(qEmployee.salary.desc())
>             .fetch();
>     }
>     
>     public List<String> findDepartmentsWithHighEarners(BigDecimal threshold) {
>         return queryFactory
>             .select(qEmployee.department)
>             .from(qEmployee)
>             .where(qEmployee.salary.gt(threshold))
>             .groupBy(qEmployee.department)
>             .having(qEmployee.count().gt(5))
>             .fetch();
>     }
> }
> ```
> 
> ## 2. Criteria API Pattern
> 
> ```java src/main/java/com/example/querypatterns/repository/CriteriaApiEmployeeRepository.java
> package com.example.querypatterns.repository;
> 
> import com.example.querypatterns.entity.Employee;
> import jakarta.persistence.EntityManager;
> import jakarta.persistence.TypedQuery;
> import jakarta.persistence.criteria.*;
> import lombok.RequiredArgsConstructor;
> import org.springframework.stereotype.Repository;
> 
> import java.math.BigDecimal;
> import java.time.LocalDate;
> import java.util.ArrayList;
> import java.util.List;
> 
> @Repository
> @RequiredArgsConstructor
> public class CriteriaApiEmployeeRepository {
>     
>     private final EntityManager entityManager;
>     
>     public List<Employee> findByCriteria(String department, BigDecimal minSalary, 
>                                          LocalDate fromDate, String namePattern) {
>         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
>         CriteriaQuery<Employee> query = cb.createQuery(Employee.class);
>         Root<Employee> employee = query.from(Employee.class);
>         
>         List<Predicate> predicates = new ArrayList<>();
>         
>         if (department != null) {
>             predicates.add(cb.equal(employee.get("department"), department));
>         }
>         
>         if (minSalary != null) {
>             predicates.add(cb.greaterThanOrEqualTo(employee.get("salary"), minSalary));
>         }
>         
>         if (fromDate != null) {
>             predicates.add(cb.greaterThanOrEqualTo(employee.get("hireDate"), fromDate));
>         }
>         
>         if (namePattern != null) {
>             predicates.add(cb.or(
>                 cb.like(employee.get("firstName"), "%" + namePattern + "%"),
>                 cb.like(employee.get("lastName"), "%" + namePattern + "%")
>             ));
>         }
>         
>         query.where(predicates.toArray(new Predicate[0]))
>              .orderBy(cb.desc(employee.get("salary")));
>         
>         return entityManager.createQuery(query).getResultList();
>     }
>     
>     public List<Object[]> getDepartmentStatistics() {
>         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
>         CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
>         Root<Employee> employee = query.from(Employee.class);
>         
>         query.multiselect(
>             employee.get("department"),
>             cb.count(employee),
>             cb.avg(employee.get("salary")),
>             cb.max(employee.get("salary")),
>             cb.min(employee.get("salary"))
>         )
>         .groupBy(employee.get("department"))
>         .having(cb.gt(cb.count(employee), 0));
>         
>         return entityManager.createQuery(query).getResultList();
>     }
> }
> ```
> 
> ## 3. Named Query Pattern
> 
> ```java src/main/java/com/example/querypatterns/repository/NamedQueryEmployeeRepository.java
> package com.example.querypatterns.repository;
> 
> import com.example.querypatterns.entity.Employee;
> import jakarta.persistence.EntityManager;
> import lombok.RequiredArgsConstructor;
> import org.springframework.stereotype.Repository;
> 
> import java.math.BigDecimal;
> import java.util.List;
> 
> @Repository
> @RequiredArgsConstructor
> public class NamedQueryEmployeeRepository {
>     
>     private final EntityManager entityManager;
>     
>     public List<Employee> findByDepartment(String department) {
>         return entityManager
>             .createNamedQuery("Employee.findByDepartment", Employee.class)
>             .setParameter("department", department)
>             .getResultList();
>     }
>     
>     public List<Employee> findHighEarners(BigDecimal minSalary) {
>         return entityManager
>             .createNamedQuery("Employee.findHighEarners", Employee.class)
>             .setParameter("minSalary", minSalary)
>             .setMaxResults(10)
>             .getResultList();
>     }
> }
> ```
> 
> ## 4. Native Query Pattern
> 
> ```java src/main/java/com/example/querypatterns/repository/NativeQueryEmployeeRepository.java
> package com.example.querypatterns.repository;
> 
> import com.example.querypatterns.entity.Employee;
> import jakarta.persistence.EntityManager;
> import jakarta.persistence.Query;
> import lombok.RequiredArgsConstructor;
> import org.springframework.stereotype.Repository;
> 
> import java.math.BigDecimal;
> import java.util.List;
> 
> @Repository
> @RequiredArgsConstructor
> public class NativeQueryEmployeeRepository {
>     
>     private final EntityManager entityManager;
>     
>     @SuppressWarnings("unchecked")
>     public List<Employee> findByDepartmentNative(String department) {
>         String sql = "SELECT * FROM employees e WHERE e.department = :department";
>         
>         Query query = entityManager.createNativeQuery(sql, Employee.class);
>         query.setParameter("department", department);
>         
>         return query.getResultList();
>     }
>     
>     @SuppressWarnings("unchecked")
>     public List<Object[]> getDepartmentSalaryReport() {
>         String sql = """
>             SELECT 
>                 e.department,
>                 COUNT(*) as employee_count,
>                 AVG(e.salary) as avg_salary,
>                 MAX(e.salary) as max_salary,
>                 MIN(e.salary) as min_salary
>             FROM employees e
>             WHERE e.active = true
>             GROUP BY e.department
>             HAVING COUNT(*) > 2
>             ORDER BY avg_salary DESC
>         """;
>         
>         return entityManager.createNativeQuery(sql).getResultList();
>     }
>     
>     public int updateSalariesByDepartment(String department, BigDecimal increasePercentage) {
>         String sql = """
>             UPDATE employees 
>             SET salary = salary * (1 + :percentage / 100)
>             WHERE department = :department
>         """;
>         
>         Query query = entityManager.createNativeQuery(sql);
>         query.setParameter("percentage", increasePercentage);
>         query.setParameter("department", department);
>         
>         return query.executeUpdate();
>     }
> }
> ```
> 
> ## 5. JPQL Pattern
> 
> ```java src/main/java/com/example/querypatterns/repository/JpqlEmployeeRepository.java
> package com.example.querypatterns.repository;
> 
> import com.example.querypatterns.entity.Employee;
> import jakarta.persistence.EntityManager;
> import jakarta.persistence.TypedQuery;
> import lombok.RequiredArgsConstructor;
> import org.springframework.stereotype.Repository;
> 
> import java.math.BigDecimal;
> import java.time.LocalDate;
> import java.util.List;
> 
> @Repository
> @RequiredArgsConstructor
> public class JpqlEmployeeRepository {
>     
>     private final EntityManager entityManager;
>     
>     public List<Employee> findActiveEmployees() {
>         String jpql = "SELECT e FROM Employee e WHERE e.active = true ORDER BY e.hireDate DESC";
>         
>         TypedQuery<Employee> query = entityManager.createQuery(jpql, Employee.class);
>         return query.getResultList();
>     }
>     
>     public List<Employee> findByDepartmentAndSalaryRange(String department, 
>                                                           BigDecimal minSalary, 
>                                                           BigDecimal maxSalary) {
>         String jpql = """
>             SELECT e FROM Employee e 
>             WHERE e.department = :department 
>             AND e.salary BETWEEN :minSalary AND :maxSalary
>             ORDER BY e.salary ASC
>         """;
>         
>         return entityManager.createQuery(jpql, Employee.class)
>             .setParameter("department", department)
>             .setParameter("minSalary", minSalary)
>             .setParameter("maxSalary", maxSalary)
>             .getResultList();
>     }
>     
>     public List<Object[]> getEmployeeCountByDepartment() {
>         String jpql = """
>             SELECT e.department, COUNT(e) 
>             FROM Employee e 
>             WHERE e.active = true
>             GROUP BY e.department
>             HAVING COUNT(e) > 1
>         """;
>         
>         return entityManager.createQuery(jpql, Object[].class).getResultList();
>     }
>     
>     public List<Employee> findRecentHires(int days) {
>         String jpql = """
>             SELECT e FROM Employee e 
>             WHERE e.hireDate >= :fromDate
>             ORDER BY e.hireDate DESC
>         """;
>         
>         LocalDate fromDate = LocalDate.now().minusDays(days);
>         
>         return entityManager.createQuery(jpql, Employee.class)
>             .setParameter("fromDate", fromDate)
>             .getResultList();
>     }
> }
> ```
> 
> ## 6. Query by Example Pattern
> 
> ```java src/main/java/com/example/querypatterns/repository/QueryByExampleEmployeeRepository.java
> package com.example.querypatterns.repository;
> 
> import com.example.querypatterns.entity.Employee;
> import org.springframework.data.domain.Example;
> import org.springframework.data.domain.ExampleMatcher;
> import org.springframework.data.jpa.repository.JpaRepository;
> import org.springframework.stereotype.Repository;
> 
> import java.util.List;
> 
> @Repository
> public interface QueryByExampleEmployeeRepository extends JpaRepository<Employee, Long> {
>     
>     default List<Employee> findByExample(Employee probe) {
>         ExampleMatcher matcher = ExampleMatcher.matching()
>             .withIgnoreNullValues()
>             .withIgnoreCase()
>             .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
>         
>         Example<Employee> example = Example.of(probe, matcher);
>         return findAll(example);
>     }
>     
>     default List<Employee> findByExactMatch(Employee probe) {
>         ExampleMatcher matcher = ExampleMatcher.matching()
>             .withIgnoreNullValues()
>             .withMatcher("department", ExampleMatcher.GenericPropertyMatchers.exact())
>             .withMatcher("position", ExampleMatcher.GenericPropertyMatchers.exact());
>         
>         Example<Employee> example = Example.of(probe, matcher);
>         return findAll(example);
>     }
> }
> ```
> 
> ## 7. Specification Pattern
> 
> ```java src/main/java/com/example/querypatterns/specification/EmployeeSpecification.java
> package com.example.querypatterns.specification;
> 
> import com.example.querypatterns.entity.Employee;
> import jakarta.persistence.criteria.Predicate;
> import org.springframework.data.jpa.domain.Specification;
> 
> import java.math.BigDecimal;
> import java.time.LocalDate;
> import java.util.ArrayList;
> import java.util.List;
> 
> public class EmployeeSpecification {
>     
>     public static Specification<Employee> hasDepartment(String department) {
>         return (root, query, cb) -> 
>             department == null ? null : cb.equal(root.get("department"), department);
>     }
>     
>     public static Specification<Employee> salaryGreaterThan(BigDecimal salary) {
>         return (root, query, cb) -> 
>             salary == null ? null : cb.greaterThan(root.get("salary"), salary);
>     }
>     
>     public static Specification<Employee> salaryBetween(BigDecimal min, BigDecimal max) {
>         return (root, query, cb) -> 
>             (min == null || max == null) ? null : cb.between(root.get("salary"), min, max);
>     }
>     
>     public static Specification<Employee> hiredAfter(LocalDate date) {
>         return (root, query, cb) -> 
>             date == null ? null : cb.greaterThanOrEqualTo(root.get("hireDate"), date);
>     }
>     
>     public static Specification<Employee> nameContains(String name) {
>         return (root, query, cb) -> {
>             if (name == null) return null;
>             
>             String pattern = "%" + name.toLowerCase() + "%";
>             return cb.or(
>                 cb.like(cb.lower(root.get("firstName")), pattern),
>                 cb.like(cb.lower(root.get("lastName")), pattern)
>             );
>         };
>     }
>     
>     public static Specification<Employee> isActive() {
>         return (root, query, cb) -> cb.isTrue(root.get("active"));
>     }
>     
>     public static Specification<Employee> searchEmployees(String department, 
>                                                           BigDecimal minSalary,
>                                                           LocalDate fromDate,
>                                                           String name,
>                                                           Boolean active) {
>         return (root, query, cb) -> {
>             List<Predicate> predicates = new ArrayList<>();
>             
>             if (department != null) {
>                 predicates.add(cb.equal(root.get("department"), department));
>             }
>             
>             if (minSalary != null) {
>                 predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), minSalary));
>             }
>             
>             if (fromDate != null) {
>                 predicates.add(cb.greaterThanOrEqualTo(root.get("hireDate"), fromDate));
>             }
>             
>             if (name != null) {
>                 String pattern = "%" + name.toLowerCase() + "%";
>                 predicates.add(cb.or(
>                     cb.like(cb.lower(root.get("firstName")), pattern),
>                     cb.like(cb.lower(root.get("lastName")), pattern)
>                 ));
>             }
>             
>             if (active != null) {
>                 predicates.add(cb.equal(root.get("active"), active));
>             }
>             
>             return cb.and(predicates.toArray(new Predicate[0]));
>         };
>     }
> }
> ```
> 
> ```java src/main/java/com/example/querypatterns/repository/SpecificationEmployeeRepository.java
> package com.example.querypatterns.repository;
> 
> import com.example.querypatterns.entity.Employee;
> import org.springframework.data.jpa.repository.JpaRepository;
> import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
> import org.springframework.stereotype.Repository;
> 
> @Repository
> public interface SpecificationEmployeeRepository extends 
>     JpaRepository<Employee, Long>, 
>     JpaSpecificationExecutor<Employee> {
> }
> ```
> 
> ## 8. Querydsl Predicate Pattern
> 
> ```java src/main/java/com/example/querypatterns/repository/QuerydslPredicateEmployeeRepository.java
> package com.example.querypatterns.repository;
> 
> import com.example.querypatterns.entity.Employee;
> import org.springframework.data.jpa.repository.JpaRepository;
> import org.springframework.data.querydsl.QuerydslPredicateExecutor;
> import org.springframework.stereotype.Repository;
> 
> @Repository
> public interface QuerydslPredicateEmployeeRepository extends 
>     JpaRepository<Employee, Long>, 
>     QuerydslPredicateExecutor<Employee> {
> }
> ```
> 
> ```java src/main/java/com/example/querypatterns/predicate/EmployeePredicate.java
> package com.example.querypatterns.predicate;
> 
> import com.example.querypatterns.entity.QEmployee;
> import com.querydsl.core.types.dsl.BooleanExpression;
> 
> import java.math.BigDecimal;
> import java.time.LocalDate;
> 
> public class EmployeePredicate {
>     
>     private static final QEmployee employee = QEmployee.employee;
>     
>     public static BooleanExpression hasDepartment(String department) {
>         return department != null ? employee.department.eq(department) : null;
>     }
>     
>     public static BooleanExpression salaryGreaterThan(BigDecimal salary) {
>         return salary != null ? employee.salary.gt(salary) : null;
>     }
>     
>     public static BooleanExpression salaryBetween(BigDecimal min, BigDecimal max) {
>         return (min != null && max != null) ? employee.salary.between(min, max) : null;
>     }
>     
>     public static BooleanExpression hiredAfter(LocalDate date) {
>         return date != null ? employee.hireDate.goe(date) : null;
>     }
>     
>     public static BooleanExpression nameContains(String name) {
>         return name != null ? employee.firstName.containsIgnoreCase(name)
>             .or(employee.lastName.containsIgnoreCase(name)) : null;
>     }
>     
>     public static BooleanExpression isActive() {
>         return employee.active.isTrue();
>     }
>     
>     public static BooleanExpression buildPredicate(String department, 
>                                                     BigDecimal minSalary,
>                                                     LocalDate fromDate,
>                                                     String name) {
>         BooleanExpression predicate = employee.isNotNull();
>         
>         if (department != null) {
>             predicate = predicate.and(hasDepartment(department));
>         }
>         
>         if (minSalary != null) {
>             predicate = predicate.and(salaryGreaterThan(minSalary));
>         }
>         
>         if (fromDate != null) {
>             predicate = predicate.and(hiredAfter(fromDate));
>         }
>         
>         if (name != null) {
>             predicate = predicate.and(nameContains(name));
>         }
>         
>         return predicate;
>     }
> }
> ```
> 
> ## 9. Dynamic Query Pattern
> 
> ```java src/main/java/com/example/querypatterns/repository/DynamicQueryEmployeeRepository.java
> package com.example.querypatterns.repository;
> 
> import com.example.querypatterns.entity.Employee;
> import jakarta.persistence.EntityManager;
> import jakarta.persistence.TypedQuery;
> import lombok.RequiredArgsConstructor;
> import org.springframework.stereotype.Repository;
> 
> import java.math.BigDecimal;
> import java.time.LocalDate;
> import java.util.HashMap;
> import java.util.List;
> import java.util.Map;
> 
> @Repository
> @RequiredArgsConstructor
> public class DynamicQueryEmployeeRepository {
>     
>     private final EntityManager entityManager;
>     
>     public List<Employee> findByDynamicCriteria(String department, 
>                                                  BigDecimal minSalary,
>                                                  BigDecimal maxSalary,
>                                                  LocalDate fromDate,
>                                                  LocalDate toDate,
>                                                  String position,
>                                                  Boolean active) {
>         StringBuilder jpql = new StringBuilder("SELECT e FROM Employee e WHERE 1=1");
>         Map<String, Object> parameters = new HashMap<>();
>         
>         if (department != null) {
>             jpql.append(" AND e.department = :department");
>             parameters.put("department", department);
>         }
>         
>         if (minSalary != null) {
>             jpql.append(" AND e.salary >= :minSalary");
>             parameters.put("minSalary", minSalary);
>         }
>         
>         if (maxSalary != null) {
>             jpql.append(" AND e.salary <= :maxSalary");
>             parameters.put("maxSalary", maxSalary);
>         }
>         
>         if (fromDate != null) {
>             jpql.append(" AND e.hireDate >= :fromDate");
>             parameters.put("fromDate", fromDate);
>         }
>         
>         if (toDate != null) {
>             jpql.append(" AND e.hireDate <= :toDate");
>             parameters.put("toDate", toDate);
>         }
>         
>         if (position != null) {
>             jpql.append(" AND e.position = :position");
>             parameters.put("position", position);
>         }
>         
>         if (active != null) {
>             jpql.append(" AND e.active = :active");
>             parameters.put("active", active);
>         }
>         
>         jpql.append(" ORDER BY e.salary DESC");
>         
>         TypedQuery<Employee> query = entityManager.createQuery(jpql.toString(), Employee.class);
>         parameters.forEach(query::setParameter);
>         
>         return query.getResultList();
>     }
>     
>     public List<Employee> searchWithPagination(String searchTerm, 
>                                                 String sortBy, 
>                                                 String sortOrder,
>                                                 int page, 
>                                                 int size) {
>         StringBuilder jpql = new StringBuilder("SELECT e FROM Employee e WHERE 1=1");
>         Map<String, Object> parameters = new HashMap<>();
>         
>         if (searchTerm != null && !searchTerm.isEmpty()) {
>             jpql.append(" AND (LOWER(e.firstName) LIKE :searchTerm")
>                 .append(" OR LOWER(e.lastName) LIKE :searchTerm")
>                 .append(" OR LOWER(e.email) LIKE :searchTerm")
>                 .append(" OR LOWER(e.department) LIKE :searchTerm)");
>             parameters.put("searchTerm", "%" + searchTerm.toLowerCase() + "%");
>         }
>         
>         if (sortBy != null && !sortBy.isEmpty()) {
>             jpql.append(" ORDER BY e.").append(sortBy);
>             if ("DESC".equalsIgnoreCase(sortOrder)) {
>                 jpql.append(" DESC");
>             } else {
>                 jpql.append(" ASC");
>             }
>         }
>         
>         TypedQuery<Employee> query = entityManager.createQuery(jpql.toString(), Employee.class);
>         parameters.forEach(query::setParameter);
>         
>         query.setFirstResult(page * size);
>         query.setMaxResults(size);
>         
>         return query.getResultList();
>     }
> }
> ```
> 
> ## 10. Stored Procedure Pattern
> 
> ```java src/main/java/com/example/querypatterns/repository/StoredProcedureEmployeeRepository.java
> package com.example.querypatterns.repository;
> 
> import com.example.querypatterns.entity.Employee;
> import jakarta.persistence.EntityManager;
> import jakarta.persistence.ParameterMode;
> import jakarta.persistence.StoredProcedureQuery;
> import lombok.RequiredArgsConstructor;
> import org.springframework.stereotype.Repository;
> 
> import java.math.BigDecimal;
> import java.util.List;
> 
> @Repository
> @RequiredArgsConstructor
> public class StoredProcedureEmployeeRepository {
>     
>     private final EntityManager entityManager;
>     
>     @SuppressWarnings("unchecked")
>     public List<Employee> getEmployeesByDepartmentProcedure(String department) {
>         StoredProcedureQuery query = entityManager
>             .createStoredProcedureQuery("get_employees_by_department", Employee.class)
>             .registerStoredProcedureParameter("dept_name", String.class, ParameterMode.IN)
>             .setParameter("dept_name", department);
>         
>         query.execute();
>         return query.getResultList();
>     }
>     
>     public void updateSalariesProcedure(String department, BigDecimal percentage) {
>         StoredProcedureQuery query = entityManager
>             .createStoredProcedureQuery("update_department_salaries")
>             .registerStoredProcedureParameter("dept_name", String.class, ParameterMode.IN)
>             .registerStoredProcedureParameter("increase_percentage", BigDecimal.class, ParameterMode.IN)
>             .setParameter("dept_name", department)
>             .setParameter("increase_percentage", percentage);
>         
>         query.execute();
>     }
>     
>     public Integer getEmployeeCountByDepartment(String department) {
>         StoredProcedureQuery query = entityManager
>             .createStoredProcedureQuery("get_employee_count")
>             .registerStoredProcedureParameter("dept_name", String.class, ParameterMode.IN)
>             .registerStoredProcedureParameter("emp_count", Integer.class, ParameterMode.OUT)
>             .setParameter("dept_name", department);
>         
>         query.execute();
>         return (Integer) query.getOutputParameterValue("emp_count");
>     }
>     
>     // Using Named Stored Procedure
>     @SuppressWarnings("unchecked")
>     public List<Employee> getEmployeesByDepartmentNamed(String department) {
>         StoredProcedureQuery query = entityManager
>             .createNamedStoredProcedureQuery("Employee.getEmployeesByDepartment")
>             .setParameter("dept_name", department);
>         
>         query.execute();
>         return query.getResultList();
>     }
> }
> ```
> 
> ## Service Layer Example
> 
> ```java src/main/java/com/example/querypatterns/service/EmployeeSearchService.java
> package com.example.querypatterns.service;
> 
> import com.example.querypatterns.entity.Employee;
> import com.example.querypatterns.predicate.EmployeePredicate;
> import com.example.querypatterns.repository.*;
> import com.example.querypatterns.specification.EmployeeSpecification;
> import com.querydsl.core.types.dsl.BooleanExpression;
> import lombok.RequiredArgsConstructor;
> import org.springframework.data.domain.Page;
> import org.springframework.data.domain.PageRequest;
> import org.springframework.data.domain.Sort;
> import org.springframework.data.jpa.domain.Specification;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
> 
> import java.math.BigDecimal;
> import java.time.LocalDate;
> import java.util.List;
> 
> @Service
> @RequiredArgsConstructor
> @Transactional(readOnly = true)
> public class EmployeeSearchService {
>     
>     private final QueryDslEmployeeRepository queryDslRepo;
>     private final CriteriaApiEmployeeRepository criteriaApiRepo;
>     private final NamedQueryEmployeeRepository namedQueryRepo;
>     private final NativeQueryEmployeeRepository nativeQueryRepo;
>     private final JpqlEmployeeRepository jpqlRepo;
>     private final QueryByExampleEmployeeRepository qbeRepo;
>     private final SpecificationEmployeeRepository specRepo;
>     private final QuerydslPredicateEmployeeRepository predicateRepo;
>     private final DynamicQueryEmployeeRepository dynamicQueryRepo;
>     private final StoredProcedureEmployeeRepository storedProcRepo;
>     
>     // Query DSL Pattern
>     public List<Employee> searchWithQueryDsl(String department, BigDecimal minSalary, LocalDate fromDate) {
>         return queryDslRepo.findByComplexCriteria(department, minSalary, fromDate);
>     }
>     
>     // Criteria API Pattern
>     public List<Employee> searchWithCriteriaApi(String department, BigDecimal minSalary, 
>                                                  LocalDate fromDate, String namePattern) {
>         return criteriaApiRepo.findByCriteria(department, minSalary, fromDate, namePattern);
>     }
>     
>     // Named Query Pattern
>     public List<Employee> searchWithNamedQuery(String department) {
>         return namedQueryRepo.findByDepartment(department);
>     }
>     
>     // Native Query Pattern
>     public List<Employee> searchWithNativeQuery(String department) {
>         return nativeQueryRepo.findByDepartmentNative(department);
>     }
>     
>     // JPQL Pattern
>     public List<Employee> searchWithJpql(String department, BigDecimal min, BigDecimal max) {
>         return jpqlRepo.findByDepartmentAndSalaryRange(department, min, max);
>     }
>     
>     // Query by Example Pattern
>     public List<Employee> searchWithQueryByExample(Employee probe) {
>         return qbeRepo.findByExample(probe);
>     }
>     
>     // Specification Pattern
>     public List<Employee> searchWithSpecification(String department, BigDecimal minSalary, 
>                                                    LocalDate fromDate, String name) {
>         Specification<Employee> spec = EmployeeSpecification.searchEmployees(
>             department, minSalary, fromDate, name, true
>         );
>         return specRepo.findAll(spec);
>     }
>     
>     // Specification with Pagination
>     public Page<Employee

#### _User_

> Continue your response exactly where you left off:

#### _Assistant_

> ```java src/main/java/com/example/querypatterns/service/EmployeeSearchService.java
> // ... existing code ...
> 
>     // Specification with Pagination
>     public Page<Employee> searchWithSpecificationPaginated(String department, BigDecimal minSalary, 
>                                                             LocalDate fromDate, String name,
>                                                             int page, int size) {
>         Specification<Employee> spec = Specification.where(EmployeeSpecification.isActive());
>         
>         if (department != null) {
>             spec = spec.and(EmployeeSpecification.hasDepartment(department));
>         }
>         if (minSalary != null) {
>             spec = spec.and(EmployeeSpecification.salaryGreaterThan(minSalary));
>         }
>         if (fromDate != null) {
>             spec = spec.and(EmployeeSpecification.hiredAfter(fromDate));
>         }
>         if (name != null) {
>             spec = spec.and(EmployeeSpecification.nameContains(name));
>         }
>         
>         PageRequest pageRequest = PageRequest.of(page, size, Sort.by("salary").descending());
>         return specRepo.findAll(spec, pageRequest);
>     }
>     
>     // Querydsl Predicate Pattern
>     public List<Employee> searchWithQuerydslPredicate(String department, BigDecimal minSalary, 
>                                                        LocalDate fromDate, String name) {
>         BooleanExpression predicate = EmployeePredicate.buildPredicate(
>             department, minSalary, fromDate, name
>         );
>         
>         return (List<Employee>) predicateRepo.findAll(predicate);
>     }
>     
>     // Dynamic Query Pattern
>     public List<Employee> searchWithDynamicQuery(String department, BigDecimal minSalary,
>                                                   BigDecimal maxSalary, LocalDate fromDate,
>                                                   LocalDate toDate, String position, Boolean active) {
>         return dynamicQueryRepo.findByDynamicCriteria(
>             department, minSalary, maxSalary, fromDate, toDate, position, active
>         );
>     }
>     
>     // Stored Procedure Pattern
>     public List<Employee> searchWithStoredProcedure(String department) {
>         return storedProcRepo.getEmployeesByDepartmentProcedure(department);
>     }
>     
>     @Transactional
>     public void updateSalariesViaProcedure(String department, BigDecimal percentage) {
>         storedProcRepo.updateSalariesProcedure(department, percentage);
>     }
> }
> ```
> 
> ## Controller Example
> 
> ```java src/main/java/com/example/querypatterns/controller/EmployeeController.java
> package com.example.querypatterns.controller;
> 
> import com.example.querypatterns.dto.EmployeeSearchRequest;
> import com.example.querypatterns.entity.Employee;
> import com.example.querypatterns.service.EmployeeSearchService;
> import lombok.RequiredArgsConstructor;
> import org.springframework.data.domain.Page;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> 
> import java.util.List;
> 
> @RestController
> @RequestMapping("/api/employees")
> @RequiredArgsConstructor
> public class EmployeeController {
>     
>     private final EmployeeSearchService searchService;
>     
>     @GetMapping("/query-dsl")
>     public ResponseEntity<List<Employee>> searchWithQueryDsl(
>             @RequestParam(required = false) String department,
>             @RequestParam(required = false) java.math.BigDecimal minSalary,
>             @RequestParam(required = false) java.time.LocalDate fromDate) {
>         return ResponseEntity.ok(searchService.searchWithQueryDsl(department, minSalary, fromDate));
>     }
>     
>     @GetMapping("/criteria-api")
>     public ResponseEntity<List<Employee>> searchWithCriteriaApi(
>             @RequestParam(required = false) String department,
>             @RequestParam(required = false) java.math.BigDecimal minSalary,
>             @RequestParam(required = false) java.time.LocalDate fromDate,
>             @RequestParam(required = false) String namePattern) {
>         return ResponseEntity.ok(searchService.searchWithCriteriaApi(
>             department, minSalary, fromDate, namePattern));
>     }
>     
>     @GetMapping("/named-query/{department}")
>     public ResponseEntity<List<Employee>> searchWithNamedQuery(@PathVariable String department) {
>         return ResponseEntity.ok(searchService.searchWithNamedQuery(department));
>     }
>     
>     @GetMapping("/native-query/{department}")
>     public ResponseEntity<List<Employee>> searchWithNativeQuery(@PathVariable String department) {
>         return ResponseEntity.ok(searchService.searchWithNativeQuery(department));
>     }
>     
>     @GetMapping("/jpql")
>     public ResponseEntity<List<Employee>> searchWithJpql(
>             @RequestParam String department,
>             @RequestParam java.math.BigDecimal minSalary,
>             @RequestParam java.math.BigDecimal maxSalary) {
>         return ResponseEntity.ok(searchService.searchWithJpql(department, minSalary, maxSalary));
>     }
>     
>     @PostMapping("/query-by-example")
>     public ResponseEntity<List<Employee>> searchWithQueryByExample(@RequestBody Employee probe) {
>         return ResponseEntity.ok(searchService.searchWithQueryByExample(probe));
>     }
>     
>     @GetMapping("/specification")
>     public ResponseEntity<List<Employee>> searchWithSpecification(
>             @RequestParam(required = false) String department,
>             @RequestParam(required = false) java.math.BigDecimal minSalary,
>             @RequestParam(required = false) java.time.LocalDate fromDate,
>             @RequestParam(required = false) String name) {
>         return ResponseEntity.ok(searchService.searchWithSpecification(
>             department, minSalary, fromDate, name));
>     }
>     
>     @GetMapping("/specification/paginated")
>     public ResponseEntity<Page<Employee>> searchWithSpecificationPaginated(
>             @RequestParam(required = false) String department,
>             @RequestParam(required = false) java.math.BigDecimal minSalary,
>             @RequestParam(required = false) java.time.LocalDate fromDate,
>             @RequestParam(required = false) String name,
>             @RequestParam(defaultValue = "0") int page,
>             @RequestParam(defaultValue = "10") int size) {
>         return ResponseEntity.ok(searchService.searchWithSpecificationPaginated(
>             department, minSalary, fromDate, name, page, size));
>     }
>     
>     @GetMapping("/querydsl-predicate")
>     public ResponseEntity<List<Employee>> searchWithQuerydslPredicate(
>             @RequestParam(required = false) String department,
>             @RequestParam(required = false) java.math.BigDecimal minSalary,
>             @RequestParam(required = false) java.time.LocalDate fromDate,
>             @RequestParam(required = false) String name) {
>         return ResponseEntity.ok(searchService.searchWithQuerydslPredicate(
>             department, minSalary, fromDate, name));
>     }
>     
>     @GetMapping("/dynamic-query")
>     public ResponseEntity<List<Employee>> searchWithDynamicQuery(
>             @RequestParam(required = false) String department,
>             @RequestParam(required = false) java.math.BigDecimal minSalary,
>             @RequestParam(required = false) java.math.BigDecimal maxSalary,
>             @RequestParam(required = false) java.time.LocalDate fromDate,
>             @RequestParam(required = false) java.time.LocalDate toDate,
>             @RequestParam(required = false) String position,
>             @RequestParam(required = false) Boolean active) {
>         return ResponseEntity.ok(searchService.searchWithDynamicQuery(
>             department, minSalary, maxSalary, fromDate, toDate, position, active));
>     }
>     
>     @GetMapping("/stored-procedure/{department}")
>     public ResponseEntity<List<Employee>> searchWithStoredProcedure(@PathVariable String department) {
>         return ResponseEntity.ok(searchService.searchWithStoredProcedure(department));
>     }
>     
>     @PutMapping("/stored-procedure/update-salaries")
>     public ResponseEntity<Void> updateSalariesViaProcedure(
>             @RequestParam String department,
>             @RequestParam java.math.BigDecimal percentage) {
>         searchService.updateSalariesViaProcedure(department, percentage);
>         return ResponseEntity.ok().build();
>     }
> }
> ```
> 
> ## Configuration Classes
> 
> ```java src/main/java/com/example/querypatterns/config/QueryDslConfig.java
> package com.example.querypatterns.config;
> 
> import com.querydsl.jpa.impl.JPAQueryFactory;
> import jakarta.persistence.EntityManager;
> import jakarta.persistence.PersistenceContext;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> 
> @Configuration
> public class QueryDslConfig {
>     
>     @PersistenceContext
>     private EntityManager entityManager;
>     
>     @Bean
>     public JPAQueryFactory jpaQueryFactory() {
>         return new JPAQueryFactory(entityManager);
>     }
> }
> ```
> 
> ## DTO
> 
> ```java src/main/java/com/example/querypatterns/dto/EmployeeSearchRequest.java
> package com.example.querypatterns.dto;
> 
> import lombok.Data;
> 
> import java.math.BigDecimal;
> import java.time.LocalDate;
> 
> @Data
> public class EmployeeSearchRequest {
>     private String department;
>     private BigDecimal minSalary;
>     private BigDecimal maxSalary;
>     private LocalDate fromDate;
>     private LocalDate toDate;
>     private String position;
>     private Boolean active;
>     private String namePattern;
>     private String sortBy;
>     private String sortOrder;
>     private Integer page;
>     private Integer size;
> }
> ```
> 
> ## Database Schema & Stored Procedures (SQL)
> 
> ```sql src/main/resources/db/migration/V1__create_employee_table.sql
> CREATE TABLE employees (
>     id BIGSERIAL PRIMARY KEY,
>     first_name VARCHAR(100) NOT NULL,
>     last_name VARCHAR(100) NOT NULL,
>     email VARCHAR(150),
>     department VARCHAR(100),
>     salary DECIMAL(12, 2),
>     hire_date DATE,
>     position VARCHAR(100),
>     active BOOLEAN DEFAULT true
> );
> 
> CREATE INDEX idx_employees_department ON employees(department);
> CREATE INDEX idx_employees_salary ON employees(salary);
> CREATE INDEX idx_employees_hire_date ON employees(hire_date);
> CREATE INDEX idx_employees_active ON employees(active);
> 
> -- Sample data
> INSERT INTO employees (first_name, last_name, email, department, salary, hire_date, position, active)
> VALUES 
>     ('John', 'Doe', 'john.doe@example.com', 'Engineering', 85000.00, '2020-01-15', 'Senior Developer', true),
>     ('Jane', 'Smith', 'jane.smith@example.com', 'Engineering', 92000.00, '2019-03-22', 'Lead Developer', true),
>     ('Bob', 'Johnson', 'bob.johnson@example.com', 'Sales', 65000.00, '2021-06-10', 'Sales Representative', true),
>     ('Alice', 'Williams', 'alice.williams@example.com', 'HR', 70000.00, '2020-11-05', 'HR Manager', true),
>     ('Charlie', 'Brown', 'charlie.brown@example.com', 'Engineering', 78000.00, '2022-02-14', 'Developer', true),
>     ('Diana', 'Davis', 'diana.davis@example.com', 'Marketing', 68000.00, '2021-09-20', 'Marketing Specialist', true),
>     ('Eve', 'Miller', 'eve.miller@example.com', 'Engineering', 95000.00, '2018-07-11', 'Architect', true),
>     ('Frank', 'Wilson', 'frank.wilson@example.com', 'Sales', 72000.00, '2020-04-18', 'Sales Manager', true);
> ```
> 
> ```sql src/main/resources/db/migration/V2__create_stored_procedures.sql
> -- PostgreSQL Stored Procedure Examples
> 
> CREATE OR REPLACE FUNCTION get_employees_by_department(dept_name VARCHAR)
> RETURNS TABLE (
>     id BIGINT,
>     first_name VARCHAR,
>     last_name VARCHAR,
>     email VARCHAR,
>     department VARCHAR,
>     salary DECIMAL,
>     hire_date DATE,
>     position VARCHAR,
>     active BOOLEAN
> ) AS $$
> BEGIN
>     RETURN QUERY
>     SELECT e.id, e.first_name, e.last_name, e.email, e.department, 
>            e.salary, e.hire_date, e.position, e.active
>     FROM employees e
>     WHERE e.department = dept_name
>     ORDER BY e.salary DESC;
> END;
> $$ LANGUAGE plpgsql;
> 
> CREATE OR REPLACE PROCEDURE update_department_salaries(
>     dept_name VARCHAR,
>     increase_percentage DECIMAL
> )
> LANGUAGE plpgsql
> AS $$
> BEGIN
>     UPDATE employees
>     SET salary = salary * (1 + increase_percentage / 100)
>     WHERE department = dept_name;
> END;
> $$;
> 
> CREATE OR REPLACE FUNCTION get_employee_count(dept_name VARCHAR)
> RETURNS INTEGER AS $$
> DECLARE
>     emp_count INTEGER;
> BEGIN
>     SELECT COUNT(*) INTO emp_count
>     FROM employees
>     WHERE department = dept_name;
>     
>     RETURN emp_count;
> END;
> $$ LANGUAGE plpgsql;
> ```
> 
> ## Application Properties
> 
> ```properties src/main/resources/application.properties
> spring.application.name=query-patterns-demo
> 
> # Database Configuration
> spring.datasource.url=jdbc:postgresql://localhost:5432/querypatterns
> spring.datasource.username=postgres
> spring.datasource.password=postgres
> spring.datasource.driver-class-name=org.postgresql.Driver
> 
> # JPA Configuration
> spring.jpa.hibernate.ddl-auto=validate
> spring.jpa.show-sql=true
> spring.jpa.properties.hibernate.format_sql=true
> spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
> 
> # Flyway Configuration
> spring.flyway.enabled=true
> spring.flyway.locations=classpath:db/migration
> 
> # Logging
> logging.level.org.hibernate.SQL=DEBUG
> logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
> logging.level.com.querydsl.sql=DEBUG
> ```
> 
> ## Maven Dependencies
> 
> ```xml pom.xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0"
>          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
>          https://maven.apache.org/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>3.2.0</version>
>         <relativePath/>
>     </parent>
>     
>     <groupId>com.example</groupId>
>     <artifactId>query-patterns</artifactId>
>     <version>1.0.0</version>
>     <name>Query Patterns Demo</name>
>     <description>Demo project for various JPA query patterns</description>
>     
>     <properties>
>         <java.version>17</java.version>
>         <querydsl.version>5.0.0</querydsl.version>
>     </properties>
>     
>     <dependencies>
>         <!-- Spring Boot Starters -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-data-jpa</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         
>         <!-- Database -->
>         <dependency>
>             <groupId>org.postgresql</groupId>
>             <artifactId>postgresql</artifactId>
>             <scope>runtime</scope>
>         </dependency>
>         
>         <!-- Flyway for database migrations -->
>         <dependency>
>             <groupId>org.flywaydb</groupId>
>             <artifactId>flyway-core</artifactId>
>         </dependency>
>         
>         <!-- QueryDSL -->
>         <dependency>
>             <groupId>com.querydsl</groupId>
>             <artifactId>querydsl-jpa</artifactId>
>             <version>${querydsl.version}</version>
>             <classifier>jakarta</classifier>
>         </dependency>
>         
>         <dependency>
>             <groupId>com.querydsl</groupId>
>             <artifactId>querydsl-apt</artifactId>
>             <version>${querydsl.version}</version>
>             <classifier>jakarta</classifier>
>             <scope>provided</scope>
>         </dependency>
>         
>         <!-- Lombok -->
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
>         </dependency>
>         
>         <!-- Test Dependencies -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-test</artifactId>
>             <scope>test</scope>
>         </dependency>
>         
>         <dependency>
>             <groupId>com.h2database</groupId>
>             <artifactId>h2</artifactId>
>             <scope>test</scope>
>         </dependency>
>     </dependencies>
>     
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>                 <configuration>
>                     <excludes>
>                         <exclude>
>                             <groupId>org.projectlombok</groupId>
>                             <artifactId>lombok</artifactId>
>                         </exclude>
>                     </excludes>
>                 </configuration>
>             </plugin>
>             
>             <!-- QueryDSL APT Plugin -->
>             <plugin>
>                 <groupId>com.mysema.maven</groupId>
>                 <artifactId>apt-maven-plugin</artifactId>
>                 <version>1.1.3</version>
>                 <executions>
>                     <execution>
>                         <goals>
>                             <goal>process</goal>
>                         </goals>
>                         <configuration>
>                             <outputDirectory>target/generated-sources/java</outputDirectory>
>                             <processor>com.querydsl.apt.jpa.JPAAnnotationProcessor</processor>
>                         </configuration>
>                     </execution>
>                 </executions>
>                 <dependencies>
>                     <dependency>
>                         <groupId>com.querydsl</groupId>
>                         <artifactId>querydsl-apt</artifactId>
>                         <version>${querydsl.version}</version>
>                         <classifier>jakarta</classifier>
>                     </dependency>
>                 </dependencies>
>             </plugin>
>         </plugins>
>     </build>
> </project>
> ```
> 
> ## README
> 
> ```markdown README.md
> # Spring JPA Query Patterns Demo
> 
> This project demonstrates 10 different query patterns in Spring Data JPA:
> 
> ## Query Patterns Implemented
> 
> 1. **Query DSL Pattern** - Type-safe queries using QueryDSL
> 2. **Criteria API Pattern** - JPA Criteria API for dynamic queries
> 3. **Named Query Pattern** - Pre-defined queries in entity annotations
> 4. **Native Query Pattern** - Direct SQL queries
> 5. **JPQL Pattern** - Java Persistence Query Language
> 6. **Query by Example Pattern** - Query using example entities
> 7. **Specification Pattern** - Reusable query specifications
> 8. **Querydsl Predicate Pattern** - Predicate-based queries with QueryDSL
> 9. **Dynamic Query Pattern** - Runtime query construction
> 10. **Stored Procedure Pattern** - Database stored procedure execution
> 
> ## Setup
> 
> 1. Install PostgreSQL
> 2. Create database: `CREATE DATABASE querypatterns;`
> 3. Update `application.properties` with your credentials
> 4. Run: `mvn clean install`
> 5. Start application: `mvn spring-boot:run`
> 
> ## API Endpoints
> 
> - GET `/api/employees/query-dsl`
> - GET `/api/employees/criteria-api`
> - GET `/api/employees/named-query/{department}`
> - GET `/api/employees/native-query/{department}`
> - GET `/api/employees/jpql`
> - POST `/api/employees/query-by-example`
> - GET `/api/employees/specification`
> - GET `/api/employees/specification/paginated`
> - GET `/api/employees/querydsl-predicate`
> - GET `/api/employees/dynamic-query`
> - GET `/api/employees/stored-procedure/{department}`
> 
> ## When to Use Each Pattern
> 
> - **QueryDSL**: Type-safe, complex queries with IDE support
> - **Criteria API**: Dynamic queries with type safety
> - **Named Queries**: Frequently used, static queries
> - **Native Queries**: Database-specific features or optimization
> - **JPQL**: Standard, portable queries
> - **Query by Example**: Simple, prototype-based searches
> - **Specification**: Composable, reusable query logic
> - **Querydsl Predicate**: Simple predicates with type safety
> - **Dynamic Query**: Runtime-determined query structure
> - **Stored Procedures**: Complex business logic in database
> ```
> 
> This comprehensive implementation covers all 10 query patterns with working examples, configuration, and a complete Spring Boot application structure!