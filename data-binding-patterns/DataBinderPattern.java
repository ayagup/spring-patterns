package com.example.databinding;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Data Binder Pattern
 * 
 * Demonstrates using DataBinder for manual data binding and validation.
 * DataBinder provides fine-grained control over the binding process.
 * 
 * Features:
 * - Manual property binding
 * - Custom validators
 * - Allowed/disallowed fields
 * - Required fields
 * - Nested property binding
 * - Type conversion
 * - Validation integration
 * 
 * Use Cases:
 * - Custom binding logic
 * - Security (field filtering)
 * - Complex binding scenarios
 * - Programmatic validation
 */
@SpringBootApplication
public class DataBinderPattern {

    public static void main(String[] args) {
        SpringApplication.run(DataBinderPattern.class, args);
    }

    @Controller
    public static class DataBinderController {

        @GetMapping("/databinder/demo")
        public String demo() {
            return "databinder/demo";
        }

        @PostMapping("/databinder/manual")
        public String manualBinding(@RequestParam Map<String, String> params) {
            User user = new User();
            
            // Create DataBinder
            DataBinder binder = new DataBinder(user, "user");
            
            // Configure allowed fields (security)
            binder.setAllowedFields("name", "email", "age");
            
            // Set disallowed fields (e.g., id, admin)
            binder.setDisallowedFields("id", "admin");
            
            // Set required fields
            binder.setRequiredFields("name", "email");
            
            // Bind from Map
            MutablePropertyValues propertyValues = new MutablePropertyValues();
            params.forEach(propertyValues::add);
            binder.bind(propertyValues);
            
            // Validate
            binder.validate();
            BindingResult result = binder.getBindingResult();
            
            if (result.hasErrors()) {
                // Handle errors
                result.getAllErrors().forEach(error -> 
                    System.out.println("Error: " + error.getDefaultMessage())
                );
                return "error";
            }
            
            System.out.println("Bound user: " + user);
            return "success";
        }

        @PostMapping("/databinder/nested")
        public String nestedBinding(@RequestParam Map<String, String> params) {
            Employee employee = new Employee();
            
            DataBinder binder = new DataBinder(employee, "employee");
            binder.setAllowedFields("name", "email", "address.*");
            
            // Bind nested properties
            MutablePropertyValues pvs = new MutablePropertyValues();
            pvs.add("name", params.get("name"));
            pvs.add("email", params.get("email"));
            pvs.add("address.street", params.get("street"));
            pvs.add("address.city", params.get("city"));
            pvs.add("address.zipCode", params.get("zipCode"));
            
            binder.bind(pvs);
            
            if (binder.getBindingResult().hasErrors()) {
                return "error";
            }
            
            System.out.println("Employee: " + employee);
            return "success";
        }

        @PostMapping("/databinder/custom-validator")
        public String customValidator(@RequestParam Map<String, String> params) {
            User user = new User();
            
            DataBinder binder = new DataBinder(user);
            
            // Add custom validator
            binder.addValidators(new UserValidator());
            
            // Bind data
            MutablePropertyValues pvs = new MutablePropertyValues(params);
            binder.bind(pvs);
            
            // Validate
            binder.validate();
            
            if (binder.getBindingResult().hasErrors()) {
                binder.getBindingResult().getAllErrors().forEach(error ->
                    System.out.println("Validation error: " + error)
                );
                return "error";
            }
            
            return "success";
        }
    }

    /**
     * User model
     */
    public static class User {
        private Long id;
        private String name;
        private String email;
        private Integer age;
        private boolean admin;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public boolean isAdmin() { return admin; }
        public void setAdmin(boolean admin) { this.admin = admin; }

        @Override
        public String toString() {
            return "User{id=" + id + ", name='" + name + "', email='" + email + 
                   "', age=" + age + ", admin=" + admin + "}";
        }
    }

    /**
     * Employee with nested address
     */
    public static class Employee {
        private String name;
        private String email;
        private Address address;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Address getAddress() { 
            if (address == null) address = new Address();
            return address; 
        }
        public void setAddress(Address address) { this.address = address; }

        @Override
        public String toString() {
            return "Employee{name='" + name + "', email='" + email + 
                   "', address=" + address + "}";
        }
    }

    public static class Address {
        private String street;
        private String city;
        private String zipCode;

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }

        @Override
        public String toString() {
            return "Address{street='" + street + "', city='" + city + 
                   "', zipCode='" + zipCode + "'}";
        }
    }

    /**
     * Custom Validator
     */
    public static class UserValidator implements Validator {

        @Override
        public boolean supports(Class<?> clazz) {
            return User.class.equals(clazz);
        }

        @Override
        public void validate(Object target, org.springframework.validation.Errors errors) {
            User user = (User) target;
            
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                errors.rejectValue("name", "name.empty", "Name is required");
            }
            
            if (user.getEmail() == null || !user.getEmail().contains("@")) {
                errors.rejectValue("email", "email.invalid", "Invalid email format");
            }
            
            if (user.getAge() != null && (user.getAge() < 18 || user.getAge() > 120)) {
                errors.rejectValue("age", "age.invalid", "Age must be between 18 and 120");
            }
        }
    }
}

/*
 * DataBinder Features:
 * 
 * 1. Property Binding:
 *    - Simple properties
 *    - Nested properties (using dot notation)
 *    - Indexed properties (arrays/lists)
 *    - Mapped properties (maps)
 * 
 * 2. Security Features:
 *    - setAllowedFields()     - Whitelist fields
 *    - setDisallowedFields()  - Blacklist fields
 *    - setRequiredFields()    - Mark fields as required
 * 
 * 3. Validation:
 *    - addValidators()        - Add custom validators
 *    - validate()             - Run validation
 *    - getBindingResult()     - Get validation results
 * 
 * 4. Type Conversion:
 *    - Automatic type conversion
 *    - Custom PropertyEditors
 *    - ConversionService integration
 * 
 * 
 * Example Usage:
 * 
 * // Create target object
 * User user = new User();
 * 
 * // Create DataBinder
 * DataBinder binder = new DataBinder(user, "user");
 * 
 * // Security: only allow specific fields
 * binder.setAllowedFields("name", "email", "age");
 * binder.setDisallowedFields("admin", "id");
 * 
 * // Mark required fields
 * binder.setRequiredFields("name", "email");
 * 
 * // Prepare property values
 * MutablePropertyValues pvs = new MutablePropertyValues();
 * pvs.add("name", "John Doe");
 * pvs.add("email", "john@example.com");
 * pvs.add("age", 30);
 * pvs.add("admin", true); // This will be ignored
 * 
 * // Bind values
 * binder.bind(pvs);
 * 
 * // Validate
 * binder.validate();
 * 
 * // Check for errors
 * if (binder.getBindingResult().hasErrors()) {
 *     binder.getBindingResult().getAllErrors().forEach(error -> {
 *         System.out.println(error.getDefaultMessage());
 *     });
 * }
 * 
 * 
 * Nested Property Binding:
 * 
 * // Bind nested properties using dot notation
 * MutablePropertyValues pvs = new MutablePropertyValues();
 * pvs.add("name", "John Doe");
 * pvs.add("address.street", "123 Main St");
 * pvs.add("address.city", "New York");
 * pvs.add("address.zipCode", "10001");
 * 
 * DataBinder binder = new DataBinder(employee);
 * binder.setAllowedFields("name", "address.*");
 * binder.bind(pvs);
 * 
 * 
 * Programmatic Validation:
 * 
 * DataBinder binder = new DataBinder(user);
 * 
 * // Add multiple validators
 * binder.addValidators(new UserValidator(), new EmailValidator());
 * 
 * // Bind and validate
 * binder.bind(pvs);
 * binder.validate();
 * 
 * BindingResult result = binder.getBindingResult();
 * if (result.hasFieldErrors("email")) {
 *     FieldError error = result.getFieldError("email");
 *     String message = error.getDefaultMessage();
 * }
 * 
 * 
 * DataBinder vs WebDataBinder:
 * 
 * DataBinder:
 * - General-purpose binding
 * - Used anywhere in application
 * - Manual setup and configuration
 * 
 * WebDataBinder:
 * - Extends DataBinder
 * - Web-specific features
 * - HTTP parameter binding
 * - Multipart file binding
 * - Used by @InitBinder
 * 
 * 
 * Best Practices:
 * 
 * 1. Always use setAllowedFields() or setDisallowedFields() for security
 * 2. Never bind sensitive fields (id, version, admin flags)
 * 3. Use required fields validation
 * 4. Combine with JSR-303 validation
 * 5. Handle binding errors gracefully
 * 6. Use nested property binding for complex objects
 * 7. Register custom PropertyEditors when needed
 * 8. Test binding logic thoroughly
 */
