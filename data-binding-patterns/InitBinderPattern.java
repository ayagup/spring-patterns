package com.example.databinding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.beans.PropertyEditorSupport;
import java.time.LocalDate;

/**
 * Init Binder Pattern
 * 
 * Demonstrates using @InitBinder to customize data binding per controller.
 * @InitBinder methods configure WebDataBinder for request handling.
 * 
 * Features:
 * - Custom PropertyEditors
 * - Allowed/disallowed fields
 * - Required fields
 * - Field prefixes
 * - Validators
 * - Date formatting
 * 
 * Use Cases:
 * - Controller-specific binding rules
 * - Security (prevent mass assignment)
 * - Custom type conversion
 * - Field validation
 */
@SpringBootApplication
public class InitBinderPattern {

    public static void main(String[] args) {
        SpringApplication.run(InitBinderPattern.class, args);
    }

    /**
     * Controller with basic @InitBinder
     */
    @Controller
    @RequestMapping("/initbinder")
    public static class UserController {

        /**
         * Initialize data binder for all requests
         */
        @InitBinder
        public void initBinder(WebDataBinder binder) {
            // Security: only allow specific fields
            binder.setAllowedFields("username", "email", "password", "birthDate");
            
            // Prevent binding of sensitive fields
            binder.setDisallowedFields("id", "role", "enabled");
            
            // Mark required fields
            binder.setRequiredFields("username", "email");
            
            // Register custom editor for trimming strings
            binder.registerCustomEditor(String.class, new StringTrimEditor());
        }

        @GetMapping("/register")
        public String showRegistrationForm(Model model) {
            model.addAttribute("user", new User());
            return "register";
        }

        @PostMapping("/register")
        public String registerUser(@Valid @ModelAttribute User user, 
                                   BindingResult result) {
            if (result.hasErrors()) {
                return "register";
            }
            // User's id, role, enabled fields cannot be set via binding
            return "redirect:/success";
        }
    }

    /**
     * Controller with multiple @InitBinder methods
     */
    @Controller
    @RequestMapping("/profile")
    public static class ProfileController {

        /**
         * InitBinder for User objects
         */
        @InitBinder("user")
        public void initUserBinder(WebDataBinder binder) {
            binder.setAllowedFields("firstName", "lastName", "email");
        }

        /**
         * InitBinder for Address objects
         */
        @InitBinder("address")
        public void initAddressBinder(WebDataBinder binder) {
            binder.setAllowedFields("street", "city", "state", "zipCode");
        }

        /**
         * Global InitBinder for all model attributes
         */
        @InitBinder
        public void initGlobalBinder(WebDataBinder binder) {
            // Apply to all bindings
            binder.registerCustomEditor(String.class, new StringTrimEditor());
        }

        @PostMapping("/update")
        public String updateProfile(@ModelAttribute("user") User user,
                                   @ModelAttribute("address") Address address) {
            // Both user and address use their respective InitBinder configurations
            return "profile";
        }
    }

    /**
     * Controller with custom validators
     */
    @Controller
    @RequestMapping("/account")
    public static class AccountController {

        @InitBinder
        public void initBinder(WebDataBinder binder) {
            // Add custom validator
            binder.addValidators(new UserValidator());
            
            // Set allowed fields
            binder.setAllowedFields("username", "email", "password");
        }

        @PostMapping("/create")
        public String createAccount(@Valid @ModelAttribute User user,
                                   BindingResult result) {
            if (result.hasErrors()) {
                return "account/form";
            }
            return "account/success";
        }
    }

    /**
     * Controller with field prefix
     */
    @Controller
    @RequestMapping("/settings")
    public static class SettingsController {

        @InitBinder("user")
        public void initUserBinder(WebDataBinder binder) {
            // Field prefix: user.username, user.email, etc.
            binder.setFieldDefaultPrefix("user.");
        }

        @InitBinder("preferences")
        public void initPreferencesBinder(WebDataBinder binder) {
            // Field prefix: pref.theme, pref.language, etc.
            binder.setFieldDefaultPrefix("pref.");
        }

        @PostMapping("/save")
        public String saveSettings(@ModelAttribute("user") User user,
                                  @ModelAttribute("preferences") Preferences preferences) {
            return "settings/saved";
        }
    }

    /**
     * Custom String Trimmer Editor
     */
    public static class StringTrimEditor extends PropertyEditorSupport {
        private final boolean emptyAsNull;

        public StringTrimEditor(boolean emptyAsNull) {
            this.emptyAsNull = emptyAsNull;
        }

        public StringTrimEditor() {
            this(true);
        }

        @Override
        public void setAsText(String text) {
            if (text == null) {
                setValue(null);
            } else {
                String trimmed = text.trim();
                setValue(emptyAsNull && trimmed.isEmpty() ? null : trimmed);
            }
        }

        @Override
        public String getAsText() {
            Object value = getValue();
            return value != null ? value.toString() : "";
        }
    }

    /**
     * Custom User Validator
     */
    public static class UserValidator implements org.springframework.validation.Validator {

        @Override
        public boolean supports(Class<?> clazz) {
            return User.class.equals(clazz);
        }

        @Override
        public void validate(Object target, org.springframework.validation.Errors errors) {
            User user = (User) target;
            
            if (user.getUsername() != null && user.getUsername().length() < 3) {
                errors.rejectValue("username", "username.tooShort", 
                    "Username must be at least 3 characters");
            }
            
            if (user.getPassword() != null && user.getPassword().length() < 8) {
                errors.rejectValue("password", "password.tooShort",
                    "Password must be at least 8 characters");
            }
        }
    }

    /**
     * User model
     */
    public static class User {
        private Long id;
        
        @NotBlank
        @Size(min = 3, max = 50)
        private String username;
        
        @Email
        @NotBlank
        private String email;
        
        @NotBlank
        private String password;
        
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate birthDate;
        
        private String role;
        private boolean enabled;
        
        private String firstName;
        private String lastName;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    public static class Address {
        private String street;
        private String city;
        private String state;
        private String zipCode;

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    }

    public static class Preferences {
        private String theme;
        private String language;

        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }
}

/*
 * @InitBinder Usage:
 * 
 * // For all model attributes in this controller
 * @InitBinder
 * public void initBinder(WebDataBinder binder) { }
 * 
 * // For specific model attribute name
 * @InitBinder("user")
 * public void initUserBinder(WebDataBinder binder) { }
 * 
 * // For multiple specific attributes
 * @InitBinder({"user", "account"})
 * public void initBinder(WebDataBinder binder) { }
 * 
 * 
 * Security with @InitBinder:
 * 
 * @InitBinder
 * public void initBinder(WebDataBinder binder) {
 *     // Whitelist approach (recommended)
 *     binder.setAllowedFields("username", "email", "password");
 *     
 *     // Blacklist approach
 *     binder.setDisallowedFields("id", "role", "admin");
 *     
 *     // Required fields
 *     binder.setRequiredFields("username", "email");
 * }
 * 
 * 
 * Best Practices:
 * 
 * 1. Always use setAllowedFields() or setDisallowedFields()
 * 2. Never bind sensitive fields (id, role, enabled)
 * 3. Use model attribute names for targeted binding
 * 4. Register custom validators in @InitBinder
 * 5. Keep @InitBinder methods focused
 * 6. Document binding restrictions
 * 7. Test mass assignment vulnerabilities
 */
