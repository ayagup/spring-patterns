package com.example.databinding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Model Attribute Pattern
 * 
 * Demonstrates Spring's @ModelAttribute annotation for:
 * - Pre-populating model attributes before handler methods
 * - Binding request parameters to model objects
 * - Storing attributes in session scope
 * - Providing common data to multiple views
 * 
 * Key Features:
 * - Method-level @ModelAttribute for data population
 * - Parameter-level @ModelAttribute for binding
 * - @SessionAttributes for multi-request workflows
 * - Attribute naming conventions
 * 
 * Use Cases:
 * - Form submission handling
 * - Multi-step wizards
 * - Common reference data (dropdown lists)
 * - Shopping cart scenarios
 * - User registration flows
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class ModelAttributePattern {

    public static void main(String[] args) {
        SpringApplication.run(ModelAttributePattern.class, args);
    }

    /**
     * Basic @ModelAttribute usage for form handling
     */
    @Controller
    @RequestMapping("/users")
    public static class UserController {

        /**
         * Method-level @ModelAttribute
         * Executed before all handler methods in this controller
         * Adds common data to the model
         */
        @ModelAttribute("countries")
        public List<String> populateCountries() {
            List<String> countries = new ArrayList<>();
            countries.add("USA");
            countries.add("Canada");
            countries.add("UK");
            countries.add("Australia");
            return countries;
        }

        @ModelAttribute("roles")
        public List<String> populateRoles() {
            List<String> roles = new ArrayList<>();
            roles.add("ADMIN");
            roles.add("USER");
            roles.add("MODERATOR");
            return roles;
        }

        /**
         * Display registration form
         * The @ModelAttribute methods above will populate countries and roles
         */
        @GetMapping("/register")
        public String showRegistrationForm(Model model) {
            model.addAttribute("user", new User());
            return "registration";
        }

        /**
         * Handle form submission
         * @ModelAttribute on parameter binds request data to User object
         */
        @PostMapping("/register")
        public String registerUser(@ModelAttribute("user") User user, Model model) {
            // Process registration
            System.out.println("Registering user: " + user.getUsername());
            model.addAttribute("message", "User registered successfully!");
            return "success";
        }

        /**
         * Edit user - pre-populate form
         */
        @GetMapping("/edit/{id}")
        public String editUser(@PathVariable Long id, Model model) {
            // In real app, fetch from database
            User user = new User();
            user.setId(id);
            user.setUsername("john_doe");
            user.setEmail("john@example.com");
            user.setCountry("USA");
            
            model.addAttribute("user", user);
            return "edit-user";
        }

        /**
         * Update user
         */
        @PostMapping("/update")
        public String updateUser(@ModelAttribute User user) {
            System.out.println("Updating user: " + user.getId());
            return "redirect:/users/list";
        }
    }

    /**
     * Multi-step wizard using @SessionAttributes
     * Stores model attributes in session across multiple requests
     */
    @Controller
    @RequestMapping("/wizard")
    @SessionAttributes("wizardData")
    public static class WizardController {

        /**
         * Initialize wizard data and store in session
         */
        @ModelAttribute("wizardData")
        public WizardData createWizardData() {
            return new WizardData();
        }

        /**
         * Step 1: Personal Information
         */
        @GetMapping("/step1")
        public String step1(Model model) {
            return "wizard/step1";
        }

        @PostMapping("/step1")
        public String processStep1(@ModelAttribute("wizardData") WizardData wizardData,
                                   @RequestParam String firstName,
                                   @RequestParam String lastName) {
            wizardData.setFirstName(firstName);
            wizardData.setLastName(lastName);
            return "redirect:/wizard/step2";
        }

        /**
         * Step 2: Contact Information
         */
        @GetMapping("/step2")
        public String step2(Model model) {
            return "wizard/step2";
        }

        @PostMapping("/step2")
        public String processStep2(@ModelAttribute("wizardData") WizardData wizardData,
                                   @RequestParam String email,
                                   @RequestParam String phone) {
            wizardData.setEmail(email);
            wizardData.setPhone(phone);
            return "redirect:/wizard/step3";
        }

        /**
         * Step 3: Review and Submit
         */
        @GetMapping("/step3")
        public String step3(Model model) {
            return "wizard/step3";
        }

        @PostMapping("/submit")
        public String submit(@ModelAttribute("wizardData") WizardData wizardData,
                           SessionStatus sessionStatus) {
            // Process the complete wizard data
            System.out.println("Wizard completed: " + wizardData.getFirstName() + " " + wizardData.getLastName());
            
            // Clear session attributes
            sessionStatus.setComplete();
            
            return "wizard/success";
        }

        /**
         * Cancel wizard - clear session
         */
        @GetMapping("/cancel")
        public String cancel(SessionStatus sessionStatus) {
            sessionStatus.setComplete();
            return "redirect:/";
        }
    }

    /**
     * Shopping cart with session storage
     */
    @Controller
    @RequestMapping("/cart")
    @SessionAttributes("shoppingCart")
    public static class ShoppingCartController {

        @ModelAttribute("shoppingCart")
        public ShoppingCart createCart() {
            return new ShoppingCart();
        }

        @GetMapping("/view")
        public String viewCart(Model model) {
            return "cart/view";
        }

        @PostMapping("/add")
        public String addItem(@ModelAttribute("shoppingCart") ShoppingCart cart,
                            @RequestParam Long productId,
                            @RequestParam String productName,
                            @RequestParam Double price,
                            @RequestParam(defaultValue = "1") Integer quantity) {
            CartItem item = new CartItem();
            item.setProductId(productId);
            item.setProductName(productName);
            item.setPrice(price);
            item.setQuantity(quantity);
            
            cart.addItem(item);
            
            return "redirect:/cart/view";
        }

        @PostMapping("/remove/{productId}")
        public String removeItem(@ModelAttribute("shoppingCart") ShoppingCart cart,
                                @PathVariable Long productId) {
            cart.removeItem(productId);
            return "redirect:/cart/view";
        }

        @PostMapping("/clear")
        public String clearCart(@ModelAttribute("shoppingCart") ShoppingCart cart) {
            cart.clear();
            return "redirect:/cart/view";
        }

        @PostMapping("/checkout")
        public String checkout(@ModelAttribute("shoppingCart") ShoppingCart cart,
                             SessionStatus sessionStatus) {
            // Process checkout
            System.out.println("Checkout total: $" + cart.getTotal());
            
            // Clear cart from session
            sessionStatus.setComplete();
            
            return "cart/success";
        }
    }

    /**
     * Advanced @ModelAttribute with request attributes
     */
    @Controller
    @RequestMapping("/advanced")
    public static class AdvancedController {

        /**
         * @ModelAttribute without explicit name
         * Name is derived from class name (user)
         */
        @ModelAttribute
        public User defaultUser() {
            User user = new User();
            user.setCountry("USA"); // Default value
            return user;
        }

        /**
         * @ModelAttribute with HttpServletRequest
         */
        @ModelAttribute("requestInfo")
        public RequestInfo getRequestInfo(HttpServletRequest request) {
            RequestInfo info = new RequestInfo();
            info.setMethod(request.getMethod());
            info.setUri(request.getRequestURI());
            info.setRemoteAddr(request.getRemoteAddr());
            return info;
        }

        /**
         * Handler method - model attributes automatically available
         */
        @GetMapping("/demo")
        public String demo(Model model) {
            // user and requestInfo are already in the model
            return "advanced/demo";
        }

        /**
         * Conditional @ModelAttribute based on request parameter
         */
        @ModelAttribute
        public void addAttributes(Model model, @RequestParam(required = false) String theme) {
            if (theme != null) {
                model.addAttribute("theme", theme);
            } else {
                model.addAttribute("theme", "default");
            }
        }
    }

    // Domain Classes

    public static class User {
        private Long id;
        private String username;
        private String password;
        private String email;
        private String country;
        private String role;
        private LocalDate birthDate;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    }

    public static class WizardData {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;
        private String city;
        private String country;

        // Getters and setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }

    public static class ShoppingCart {
        private List<CartItem> items = new ArrayList<>();

        public void addItem(CartItem item) {
            items.add(item);
        }

        public void removeItem(Long productId) {
            items.removeIf(item -> item.getProductId().equals(productId));
        }

        public void clear() {
            items.clear();
        }

        public double getTotal() {
            return items.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
        }

        public List<CartItem> getItems() { return items; }
        public void setItems(List<CartItem> items) { this.items = items; }
    }

    public static class CartItem {
        private Long productId;
        private String productName;
        private Double price;
        private Integer quantity;

        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class RequestInfo {
        private String method;
        private String uri;
        private String remoteAddr;

        // Getters and setters
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        
        public String getUri() { return uri; }
        public void setUri(String uri) { this.uri = uri; }
        
        public String getRemoteAddr() { return remoteAddr; }
        public void setRemoteAddr(String remoteAddr) { this.remoteAddr = remoteAddr; }
    }
}

/**
 * DOCUMENTATION
 * 
 * @ModelAttribute Annotation Usage:
 * 
 * 1. Method-level @ModelAttribute:
 *    - Executed before handler methods
 *    - Populates model with common data
 *    - Return value added to model
 *    - Name can be explicit or derived from return type
 * 
 * 2. Parameter-level @ModelAttribute:
 *    - Binds request parameters to object
 *    - Creates object if not in model
 *    - Retrieves from session if @SessionAttributes used
 *    - Applies data binding and validation
 * 
 * 3. @SessionAttributes:
 *    - Stores model attributes in HTTP session
 *    - Persists across multiple requests
 *    - Useful for multi-step workflows
 *    - Must call sessionStatus.setComplete() to clear
 * 
 * Best Practices:
 * - Use method-level for reference data (dropdowns, lookups)
 * - Use parameter-level for form submissions
 * - Use @SessionAttributes for multi-step processes
 * - Always clear session when workflow completes
 * - Validate model attributes
 * - Use meaningful attribute names
 * 
 * Common Patterns:
 * - Form pre-population
 * - Wizard workflows
 * - Shopping carts
 * - User registration
 * - Dropdown lists
 * 
 * Integration:
 * - Works with validation (@Valid)
 * - Supports type conversion
 * - Integrates with view templates
 * - Compatible with REST endpoints (though less common)
 */
