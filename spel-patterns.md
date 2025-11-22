### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 1:26:08 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **Expression Language Patterns**
> - SpEL (Spring Expression Language) Pattern
> - Property Placeholder Pattern
> - Bean Reference Pattern
> - Method Invocation Pattern
> - Collection Selection Pattern
> - Collection Projection Pattern
> - Template Expression Pattern
> ```

#### _Assistant_

> I'll create a comprehensive Spring Boot application demonstrating all these Expression Language patterns.
> 
> ## Project Structure
> 
> ```
> spring-expression-patterns/
> ├── pom.xml
> ├── src/main/
> │   ├── java/com/example/spel/
> │   │   ├── SpelPatternsApplication.java
> │   │   ├── config/
> │   │   │   ├── SpelConfiguration.java
> │   │   │   └── PropertyConfiguration.java
> │   │   ├── model/
> │   │   │   ├── Employee.java
> │   │   │   ├── Department.java
> │   │   │   └── Product.java
> │   │   ├── service/
> │   │   │   ├── SpelPatternService.java
> │   │   │   ├── EmployeeService.java
> │   │   │   └── MathService.java
> │   │   └── controller/
> │   │       └── SpelDemoController.java
> │   └── resources/
> │       └── application.properties
> ```
> 
> ## 1. Main Application
> 
> ```java src/main/java/com/example/spel/SpelPatternsApplication.java
> package com.example.spel;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> 
> @SpringBootApplication
> public class SpelPatternsApplication {
>     public static void main(String[] args) {
>         SpringApplication.run(SpelPatternsApplication.class, args);
>     }
> }
> ```
> 
> ## 2. Model Classes
> 
> ```java src/main/java/com/example/spel/model/Employee.java
> package com.example.spel.model;
> 
> public class Employee {
>     private Long id;
>     private String name;
>     private String department;
>     private double salary;
>     private int yearsOfExperience;
>     private boolean active;
> 
>     public Employee() {}
> 
>     public Employee(Long id, String name, String department, double salary, int yearsOfExperience, boolean active) {
>         this.id = id;
>         this.name = name;
>         this.department = department;
>         this.salary = salary;
>         this.yearsOfExperience = yearsOfExperience;
>         this.active = active;
>     }
> 
>     // Getters and Setters
>     public Long getId() { return id; }
>     public void setId(Long id) { this.id = id; }
>     
>     public String getName() { return name; }
>     public void setName(String name) { this.name = name; }
>     
>     public String getDepartment() { return department; }
>     public void setDepartment(String department) { this.department = department; }
>     
>     public double getSalary() { return salary; }
>     public void setSalary(double salary) { this.salary = salary; }
>     
>     public int getYearsOfExperience() { return yearsOfExperience; }
>     public void setYearsOfExperience(int yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
>     
>     public boolean isActive() { return active; }
>     public void setActive(boolean active) { this.active = active; }
> 
>     @Override
>     public String toString() {
>         return "Employee{id=" + id + ", name='" + name + "', department='" + department + 
>                "', salary=" + salary + ", yearsOfExperience=" + yearsOfExperience + 
>                ", active=" + active + '}';
>     }
> }
> ```
> 
> ```java src/main/java/com/example/spel/model/Department.java
> package com.example.spel.model;
> 
> import java.util.List;
> 
> public class Department {
>     private String name;
>     private String location;
>     private int employeeCount;
>     private List<Employee> employees;
> 
>     public Department() {}
> 
>     public Department(String name, String location, int employeeCount) {
>         this.name = name;
>         this.location = location;
>         this.employeeCount = employeeCount;
>     }
> 
>     // Getters and Setters
>     public String getName() { return name; }
>     public void setName(String name) { this.name = name; }
>     
>     public String getLocation() { return location; }
>     public void setLocation(String location) { this.location = location; }
>     
>     public int getEmployeeCount() { return employeeCount; }
>     public void setEmployeeCount(int employeeCount) { this.employeeCount = employeeCount; }
>     
>     public List<Employee> getEmployees() { return employees; }
>     public void setEmployees(List<Employee> employees) { this.employees = employees; }
> 
>     @Override
>     public String toString() {
>         return "Department{name='" + name + "', location='" + location + 
>                "', employeeCount=" + employeeCount + '}';
>     }
> }
> ```
> 
> ```java src/main/java/com/example/spel/model/Product.java
> package com.example.spel.model;
> 
> public class Product {
>     private Long id;
>     private String name;
>     private double price;
>     private int stock;
> 
>     public Product() {}
> 
>     public Product(Long id, String name, double price, int stock) {
>         this.id = id;
>         this.name = name;
>         this.price = price;
>         this.stock = stock;
>     }
> 
>     // Getters and Setters
>     public Long getId() { return id; }
>     public void setId(Long id) { this.id = id; }
>     
>     public String getName() { return name; }
>     public void setName(String name) { this.name = name; }
>     
>     public double getPrice() { return price; }
>     public void setPrice(double price) { this.price = price; }
>     
>     public int getStock() { return stock; }
>     public void setStock(int stock) { this.stock = stock; }
> 
>     @Override
>     public String toString() {
>         return "Product{id=" + id + ", name='" + name + "', price=" + price + ", stock=" + stock + '}';
>     }
> }
> ```
> 
> ## 3. Service Classes
> 
> ```java src/main/java/com/example/spel/service/MathService.java
> package com.example.spel.service;
> 
> import org.springframework.stereotype.Service;
> 
> @Service("mathService")
> public class MathService {
>     
>     public double calculateBonus(double salary, int yearsOfExperience) {
>         return salary * 0.1 * yearsOfExperience;
>     }
>     
>     public double add(double a, double b) {
>         return a + b;
>     }
>     
>     public double multiply(double a, double b) {
>         return a * b;
>     }
>     
>     public int max(int a, int b) {
>         return Math.max(a, b);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/spel/service/EmployeeService.java
> package com.example.spel.service;
> 
> import com.example.spel.model.Employee;
> import org.springframework.stereotype.Service;
> import java.util.ArrayList;
> import java.util.List;
> 
> @Service("employeeService")
> public class EmployeeService {
>     
>     private List<Employee> employees;
>     
>     public EmployeeService() {
>         this.employees = new ArrayList<>();
>         initializeEmployees();
>     }
>     
>     private void initializeEmployees() {
>         employees.add(new Employee(1L, "John Doe", "Engineering", 75000, 5, true));
>         employees.add(new Employee(2L, "Jane Smith", "Engineering", 95000, 8, true));
>         employees.add(new Employee(3L, "Bob Johnson", "Sales", 60000, 3, true));
>         employees.add(new Employee(4L, "Alice Williams", "Marketing", 55000, 2, false));
>         employees.add(new Employee(5L, "Charlie Brown", "Engineering", 120000, 12, true));
>     }
>     
>     public List<Employee> getAllEmployees() {
>         return employees;
>     }
>     
>     public Employee getEmployeeById(Long id) {
>         return employees.stream()
>                 .filter(e -> e.getId().equals(id))
>                 .findFirst()
>                 .orElse(null);
>     }
>     
>     public String getCompanyName() {
>         return "TechCorp Inc.";
>     }
> }
> ```
> 
> ```java src/main/java/com/example/spel/service/SpelPatternService.java
> package com.example.spel.service;
> 
> import com.example.spel.model.Employee;
> import com.example.spel.model.Product;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.expression.Expression;
> import org.springframework.expression.ExpressionParser;
> import org.springframework.expression.spel.standard.SpelExpressionParser;
> import org.springframework.expression.spel.support.StandardEvaluationContext;
> import org.springframework.stereotype.Service;
> 
> import java.util.*;
> 
> @Service
> public class SpelPatternService {
> 
>     @Autowired
>     private EmployeeService employeeService;
> 
>     @Autowired
>     private MathService mathService;
> 
>     // 1. SpEL Pattern - Basic expression evaluation
>     @Value("#{2 + 3 * 4}")
>     private int calculatedValue;
> 
>     @Value("#{'Hello ' + 'SpEL'}")
>     private String concatenatedString;
> 
>     @Value("#{T(java.lang.Math).PI}")
>     private double piValue;
> 
>     // 2. Property Placeholder Pattern
>     @Value("${app.name:DefaultApp}")
>     private String appName;
> 
>     @Value("${app.version:1.0}")
>     private String appVersion;
> 
>     @Value("${app.max.users:100}")
>     private int maxUsers;
> 
>     // 3. Bean Reference Pattern
>     @Value("#{employeeService.companyName}")
>     private String companyName;
> 
>     @Value("#{mathService.add(10, 20)}")
>     private double sumResult;
> 
>     // 4. Method Invocation Pattern
>     @Value("#{'spring expression language'.toUpperCase()}")
>     private String upperCaseString;
> 
>     @Value("#{employeeService.getEmployeeById(1L).name}")
>     private String firstEmployeeName;
> 
>     public Map<String, Object> demonstrateSpelPattern() {
>         Map<String, Object> results = new HashMap<>();
>         
>         ExpressionParser parser = new SpelExpressionParser();
>         
>         // Basic arithmetic
>         Expression exp1 = parser.parseExpression("10 + 5");
>         results.put("arithmetic", exp1.getValue());
>         
>         // String operations
>         Expression exp2 = parser.parseExpression("'Hello'.concat(' World')");
>         results.put("stringConcat", exp2.getValue());
>         
>         // Logical operations
>         Expression exp3 = parser.parseExpression("10 > 5 && 3 < 5");
>         results.put("logicalOperation", exp3.getValue());
>         
>         // Static method invocation
>         Expression exp4 = parser.parseExpression("T(java.lang.Math).random()");
>         results.put("randomNumber", exp4.getValue());
>         
>         // Ternary operator
>         Expression exp5 = parser.parseExpression("10 > 5 ? 'Yes' : 'No'");
>         results.put("ternaryOperator", exp5.getValue());
>         
>         // Injected values
>         results.put("calculatedValue", calculatedValue);
>         results.put("concatenatedString", concatenatedString);
>         results.put("piValue", piValue);
>         
>         return results;
>     }
> 
>     public Map<String, Object> demonstratePropertyPlaceholder() {
>         Map<String, Object> results = new HashMap<>();
>         
>         results.put("appName", appName);
>         results.put("appVersion", appVersion);
>         results.put("maxUsers", maxUsers);
>         
>         // Combining SpEL with properties
>         ExpressionParser parser = new SpelExpressionParser();
>         StandardEvaluationContext context = new StandardEvaluationContext();
>         context.setVariable("maxUsers", maxUsers);
>         
>         Expression exp = parser.parseExpression("#maxUsers * 2");
>         results.put("doubleMaxUsers", exp.getValue(context));
>         
>         return results;
>     }
> 
>     public Map<String, Object> demonstrateBeanReference() {
>         Map<String, Object> results = new HashMap<>();
>         
>         results.put("companyName", companyName);
>         results.put("sumResult", sumResult);
>         
>         ExpressionParser parser = new SpelExpressionParser();
>         StandardEvaluationContext context = new StandardEvaluationContext();
>         context.setBeanResolver((ctx, beanName) -> {
>             if ("employeeService".equals(beanName)) return employeeService;
>             if ("mathService".equals(beanName)) return mathService;
>             return null;
>         });
>         
>         // Reference bean methods
>         Expression exp1 = parser.parseExpression("@employeeService.getAllEmployees().size()");
>         results.put("totalEmployees", exp1.getValue(context));
>         
>         Expression exp2 = parser.parseExpression("@mathService.multiply(5, 6)");
>         results.put("multiplicationResult", exp2.getValue(context));
>         
>         return results;
>     }
> 
>     public Map<String, Object> demonstrateMethodInvocation() {
>         Map<String, Object> results = new HashMap<>();
>         
>         results.put("upperCaseString", upperCaseString);
>         results.put("firstEmployeeName", firstEmployeeName);
>         
>         ExpressionParser parser = new SpelExpressionParser();
>         
>         Employee employee = new Employee(1L, "John Doe", "Engineering", 75000, 5, true);
>         StandardEvaluationContext context = new StandardEvaluationContext(employee);
>         
>         // Method invocation on objects
>         Expression exp1 = parser.parseExpression("name.length()");
>         results.put("nameLength", exp1.getValue(context));
>         
>         Expression exp2 = parser.parseExpression("department.toLowerCase()");
>         results.put("departmentLower", exp2.getValue(context));
>         
>         // Safe navigation operator
>         Expression exp3 = parser.parseExpression("name?.toUpperCase()");
>         results.put("safeNavigation", exp3.getValue(context));
>         
>         // Method chaining
>         Expression exp4 = parser.parseExpression("name.substring(0, 4).concat('...')");
>         results.put("methodChaining", exp4.getValue(context));
>         
>         return results;
>     }
> 
>     public Map<String, Object> demonstrateCollectionSelection() {
>         Map<String, Object> results = new HashMap<>();
>         
>         List<Employee> employees = employeeService.getAllEmployees();
>         
>         ExpressionParser parser = new SpelExpressionParser();
>         StandardEvaluationContext context = new StandardEvaluationContext();
>         context.setVariable("employees", employees);
>         
>         // Selection - filter active employees
>         Expression exp1 = parser.parseExpression("#employees.?[active == true]");
>         List<Employee> activeEmployees = (List<Employee>) exp1.getValue(context);
>         results.put("activeEmployees", activeEmployees);
>         
>         // Selection - filter by department
>         Expression exp2 = parser.parseExpression("#employees.?[department == 'Engineering']");
>         List<Employee> engineers = (List<Employee>) exp2.getValue(context);
>         results.put("engineers", engineers);
>         
>         // Selection - filter by salary
>         Expression exp3 = parser.parseExpression("#employees.?[salary > 70000]");
>         List<Employee> highEarners = (List<Employee>) exp3.getValue(context);
>         results.put("highEarners", highEarners);
>         
>         // First match
>         Expression exp4 = parser.parseExpression("#employees.^[yearsOfExperience > 7]");
>         Employee firstSenior = (Employee) exp4.getValue(context);
>         results.put("firstSeniorEmployee", firstSenior);
>         
>         // Last match
>         Expression exp5 = parser.parseExpression("#employees.$[active == true]");
>         Employee lastActive = (Employee) exp5.getValue(context);
>         results.put("lastActiveEmployee", lastActive);
>         
>         return results;
>     }
> 
>     public Map<String, Object> demonstrateCollectionProjection() {
>         Map<String, Object> results = new HashMap<>();
>         
>         List<Employee> employees = employeeService.getAllEmployees();
>         
>         ExpressionParser parser = new SpelExpressionParser();
>         StandardEvaluationContext context = new StandardEvaluationContext();
>         context.setVariable("employees", employees);
>         
>         // Projection - extract names
>         Expression exp1 = parser.parseExpression("#employees.![name]");
>         List<String> names = (List<String>) exp1.getValue(context);
>         results.put("employeeNames", names);
>         
>         // Projection - extract salaries
>         Expression exp2 = parser.parseExpression("#employees.![salary]");
>         List<Double> salaries = (List<Double>) exp2.getValue(context);
>         results.put("salaries", salaries);
>         
>         // Combined selection and projection
>         Expression exp3 = parser.parseExpression("#employees.?[active == true].![name]");
>         List<String> activeEmployeeNames = (List<String>) exp3.getValue(context);
>         results.put("activeEmployeeNames", activeEmployeeNames);
>         
>         // Projection with concatenation
>         Expression exp4 = parser.parseExpression("#employees.![name + ' - ' + department]");
>         List<String> nameWithDept = (List<String>) exp4.getValue(context);
>         results.put("nameWithDepartment", nameWithDept);
>         
>         // Complex projection
>         Expression exp5 = parser.parseExpression("#employees.?[salary > 70000].![name + ' earns $' + salary]");
>         List<String> highEarnerInfo = (List<String>) exp5.getValue(context);
>         results.put("highEarnerInfo", highEarnerInfo);
>         
>         return results;
>     }
> 
>     public Map<String, Object> demonstrateTemplateExpression() {
>         Map<String, Object> results = new HashMap<>();
>         
>         ExpressionParser parser = new SpelExpressionParser();
>         
>         // Template expressions using #{}
>         Map<String, Object> variables = new HashMap<>();
>         variables.put("name", "John");
>         variables.put("age", 30);
>         variables.put("city", "New York");
>         
>         StandardEvaluationContext context = new StandardEvaluationContext();
>         context.setVariables(variables);
>         
>         // Simple template
>         String template1 = "Hello #{#name}, you are #{#age} years old";
>         Expression exp1 = parser.parseExpression(template1, new org.springframework.expression.common.TemplateParserContext());
>         results.put("simpleTemplate", exp1.getValue(context, String.class));
>         
>         // Template with operations
>         String template2 = "In 5 years, #{#name} will be #{#age + 5} years old";
>         Expression exp2 = parser.parseExpression(template2, new org.springframework.expression.common.TemplateParserContext());
>         results.put("templateWithOperation", exp2.getValue(context, String.class));
>         
>         // Template with multiple expressions
>         String template3 = "Person: #{#name.toUpperCase()}, Age: #{#age}, City: #{#city.toLowerCase()}";
>         Expression exp3 = parser.parseExpression(template3, new org.springframework.expression.common.TemplateParserContext());
>         results.put("multiExpressionTemplate", exp3.getValue(context, String.class));
>         
>         // Template with conditional
>         String template4 = "#{#age >= 18 ? 'Adult' : 'Minor'} living in #{#city}";
>         Expression exp4 = parser.parseExpression(template4, new org.springframework.expression.common.TemplateParserContext());
>         results.put("conditionalTemplate", exp4.getValue(context, String.class));
>         
>         // Employee template
>         Employee employee = employeeService.getEmployeeById(1L);
>         StandardEvaluationContext empContext = new StandardEvaluationContext(employee);
>         String template5 = "Employee #{name} works in #{department} and earns $#{salary}";
>         Expression exp5 = parser.parseExpression(template5, new org.springframework.expression.common.TemplateParserContext());
>         results.put("employeeTemplate", exp5.getValue(empContext, String.class));
>         
>         return results;
>     }
> 
>     public Map<String, Object> getAllPatternDemonstrations() {
>         Map<String, Object> allResults = new HashMap<>();
>         
>         allResults.put("1_SpEL_Pattern", demonstrateSpelPattern());
>         allResults.put("2_Property_Placeholder_Pattern", demonstratePropertyPlaceholder());
>         allResults.put("3_Bean_Reference_Pattern", demonstrateBeanReference());
>         allResults.put("4_Method_Invocation_Pattern", demonstrateMethodInvocation());
>         allResults.put("5_Collection_Selection_Pattern", demonstrateCollectionSelection());
>         allResults.put("6_Collection_Projection_Pattern", demonstrateCollectionProjection());
>         allResults.put("7_Template_Expression_Pattern", demonstrateTemplateExpression());
>         
>         return allResults;
>     }
> }
> ```
> 
> ## 4. Configuration Classes
> 
> ```java src/main/java/com/example/spel/config/SpelConfiguration.java
> package com.example.spel.config;
> 
> import com.example.spel.model.Department;
> import com.example.spel.model.Product;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> 
> import java.util.Arrays;
> import java.util.List;
> 
> @Configuration
> public class SpelConfiguration {
> 
>     // SpEL with mathematical operations
>     @Bean
>     public String configMessage(
>             @Value("#{T(java.lang.String).format('Application started at %tF %<tT', new java.util.Date())}") 
>             String timestamp) {
>         return timestamp;
>     }
> 
>     // Bean with SpEL evaluation
>     @Bean
>     public Department engineeringDept(
>             @Value("#{employeeService.getAllEmployees().?[department == 'Engineering'].size()}") 
>             int employeeCount) {
>         Department dept = new Department("Engineering", "Building A", employeeCount);
>         return dept;
>     }
> 
>     // Collection with SpEL
>     @Bean
>     public List<Product> productCatalog() {
>         return Arrays.asList(
>             new Product(1L, "Laptop", 999.99, 50),
>             new Product(2L, "Mouse", 29.99, 200),
>             new Product(3L, "Keyboard", 79.99, 150),
>             new Product(4L, "Monitor", 299.99, 75),
>             new Product(5L, "Headphones", 149.99, 100)
>         );
>     }
> 
>     // SpEL with system properties
>     @Bean
>     public String systemInfo(
>             @Value("#{T(java.lang.System).getProperty('java.version')}") 
>             String javaVersion,
>             @Value("#{T(java.lang.System).getProperty('os.name')}") 
>             String osName) {
>         return String.format("Running on %s with Java %s", osName, javaVersion);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/spel/config/PropertyConfiguration.java
> package com.example.spel.config;
> 
> import org.springframework.boot.context.properties.ConfigurationProperties;
> import org.springframework.context.annotation.Configuration;
> 
> @Configuration
> @ConfigurationProperties(prefix = "app")
> public class PropertyConfiguration {
>     
>     private String name;
>     private String version;
>     private int maxUsers;
>     private Database database = new Database();
>     
>     public static class Database {
>         private String url;
>         private String username;
>         private int maxConnections;
> 
>         // Getters and Setters
>         public String getUrl() { return url; }
>         public void setUrl(String url) { this.url = url; }
>         
>         public String getUsername() { return username; }
>         public void setUsername(String username) { this.username = username; }
>         
>         public int getMaxConnections() { return maxConnections; }
>         public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
>     }
> 
>     // Getters and Setters
>     public String getName() { return name; }
>     public void setName(String name) { this.name = name; }
>     
>     public String getVersion() { return version; }
>     public void setVersion(String version) { this.version = version; }
>     
>     public int getMaxUsers() { return maxUsers; }
>     public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }
>     
>     public Database getDatabase() { return database; }
>     public void setDatabase(Database database) { this.database = database; }
> }
> ```
> 
> ## 5. REST Controller
> 
> ```java src/main/java/com/example/spel/controller/SpelDemoController.java
> package com.example.spel.controller;
> 
> import com.example.spel.service.SpelPatternService;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> 
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/spel")
> public class SpelDemoController {
> 
>     @Autowired
>     private SpelPatternService spelPatternService;
> 
>     @GetMapping("/all-patterns")
>     public Map<String, Object> getAllPatterns() {
>         return spelPatternService.getAllPatternDemonstrations();
>     }
> 
>     @GetMapping("/spel-pattern")
>     public Map<String, Object> getSpelPattern() {
>         return spelPatternService.demonstrateSpelPattern();
>     }
> 
>     @GetMapping("/property-placeholder")
>     public Map<String, Object> getPropertyPlaceholder() {
>         return spelPatternService.demonstratePropertyPlaceholder();
>     }
> 
>     @GetMapping("/bean-reference")
>     public Map<String, Object> getBeanReference() {
>         return spelPatternService.demonstrateBeanReference();
>     }
> 
>     @GetMapping("/method-invocation")
>     public Map<String, Object> getMethodInvocation() {
>         return spelPatternService.demonstrateMethodInvocation();
>     }
> 
>     @GetMapping("/collection-selection")
>     public Map<String, Object> getCollectionSelection() {
>         return spelPatternService.demonstrateCollectionSelection();
>     }
> 
>     @GetMapping("/collection-projection")
>     public Map<String, Object> getCollectionProjection() {
>         return spelPatternService.demonstrateCollectionProjection();
>     }
> 
>     @GetMapping("/template-expression")
>     public Map<String, Object> getTemplateExpression() {
>         return spelPatternService.demonstrateTemplateExpression();
>     }
> }
> ```
> 
> ## 6. Application Properties
> 
> ```properties src/main/resources/application.properties
> # Application Properties
> app.name=SpEL Patterns Demo
> app.version=2.0.1
> app.max.users=500
> 
> # Database Properties
> app.database.url=jdbc:mysql://localhost:3306/speldb
> app.database.username=admin
> app.database.max-connections=50
> 
> # Server Configuration
> server.port=8080
> 
> # Logging
> logging.level.com.example.spel=DEBUG
> ```
> 
> ## 7. Maven POM
> 
> ```xml pom.xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0"
>          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
>          http://maven.apache.org/xsd/maven-4.0.0.xsd">
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
>     <artifactId>spring-expression-patterns</artifactId>
>     <version>1.0.0</version>
>     <name>Spring Expression Language Patterns</name>
>     <description>Demo project for Spring Expression Language patterns</description>
> 
>     <properties>
>         <java.version>17</java.version>
>     </properties>
> 
>     <dependencies>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
> 
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-test</artifactId>
>             <scope>test</scope>
>         </dependency>
> 
>         <dependency>
>             <groupId>org.springframework</groupId>
>             <artifactId>spring-expression</artifactId>
>         </dependency>
>     </dependencies>
> 
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>             </plugin>
>         </plugins>
>     </build>
> </project>
> ```
> 
> ## How to Run
> 
> 1. **Build the project:**
>    ```bash
>    mvn clean install
>    ```
> 
> 2. **Run the application:**
>    ```bash
>    mvn spring-boot:run
>    ```
> 
> 3. **Test the endpoints:**
>    ```bash
>    # All patterns
>    curl http://localhost:8080/api/spel/all-patterns
> 
>    # Individual patterns
>    curl http://localhost:8080/api/spel/spel-pattern
>    curl http://localhost:8080/api/spel/property-placeholder
>    curl http://localhost:8080/api/spel/bean-reference
>    curl http://localhost:8080/api/spel/method-invocation
>    curl http://localhost:8080/api/spel/collection-selection
>    curl http://localhost:8080/api/spel/collection-projection
>    curl http://localhost:8080/api/spel/template-expression
>    ```
> 
> ## Pattern Summary
> 
> - **SpEL Pattern**: Basic expression evaluation (arithmetic, logical, ternary)
> - **Property Placeholder Pattern**: Injecting values from application.properties
> - **Bean Reference Pattern**: Referencing and invoking other Spring beans
> - **Method Invocation Pattern**: Calling methods on objects and beans
> - **Collection Selection Pattern**: Filtering collections using `.?[]`, `.^[]`, `.$[]`
> - **Collection Projection Pattern**: Extracting properties from collections using `.![]`
> - **Template Expression Pattern**: Embedding expressions in strings using `#{}`
> 
> This comprehensive demo showcases all seven SpEL patterns with practical examples!