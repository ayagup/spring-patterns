package com.spring.patterns.scope;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.annotation.SessionScope;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Session Scope Pattern
 * 
 * Session scope creates a single bean instance per HTTP session.
 * The bean persists across multiple requests within the same session.
 * 
 * Characteristics:
 * - One instance per HTTP session
 * - Persists across multiple requests
 * - Destroyed when session expires or invalidates
 * - Available only in web-aware ApplicationContext
 * - Session-bound lifecycle
 * 
 * Use Cases:
 * - User session data
 * - Shopping carts
 * - User preferences
 * - Authentication state
 * - Multi-step workflows
 * - Session-level caching
 * 
 * Configuration:
 * - @SessionScope or @Scope(WebApplicationContext.SCOPE_SESSION)
 * - Requires web application context
 * - Use proxyMode for injection into singletons
 */
@SpringBootApplication
public class SessionScopePattern {

    public static void main(String[] args) {
        SpringApplication.run(SessionScopePattern.class, args);
        System.out.println("\n=== Session Scope Pattern Started ===");
        System.out.println("Test endpoints:");
        System.out.println("  GET http://localhost:8080/api/session/cart/add?item=Laptop");
        System.out.println("  GET http://localhost:8080/api/session/cart/view");
        System.out.println("  GET http://localhost:8080/api/session/user/profile");
    }
}

/**
 * Configuration for session-scoped beans
 */
@Configuration
class SessionScopedConfig {
    
    @Bean
    @SessionScope
    public ShoppingCart shoppingCart() {
        return new ShoppingCart();
    }
    
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public UserSession userSession() {
        return new UserSession();
    }
    
    @Bean
    @SessionScope
    public UserPreferences userPreferences() {
        return new UserPreferences();
    }
}

/**
 * Session-scoped shopping cart
 */
@Component
@SessionScope
class ShoppingCart {
    private final String cartId;
    private final LocalDateTime createdAt;
    private final List<CartItem> items = new ArrayList<>();
    private double totalAmount = 0.0;
    
    public ShoppingCart() {
        this.cartId = "CART-" + UUID.randomUUID().toString().substring(0, 8);
        this.createdAt = LocalDateTime.now();
        System.out.println("ShoppingCart created for session: " + cartId);
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("ShoppingCart destroyed: " + cartId + 
                         " (items: " + items.size() + 
                         ", total: $" + totalAmount + ")");
    }
    
    public void addItem(String productName, double price) {
        CartItem item = new CartItem(productName, price);
        items.add(item);
        totalAmount += price;
        System.out.println("Item added to cart " + cartId + ": " + productName);
    }
    
    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            CartItem removed = items.remove(index);
            totalAmount -= removed.getPrice();
        }
    }
    
    public void clear() {
        items.clear();
        totalAmount = 0.0;
    }
    
    public String getCartId() { return cartId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<CartItem> getItems() { return new ArrayList<>(items); }
    public double getTotalAmount() { return totalAmount; }
    public int getItemCount() { return items.size(); }
}

/**
 * Cart item class
 */
class CartItem {
    private final String productName;
    private final double price;
    private final LocalDateTime addedAt;
    
    public CartItem(String productName, double price) {
        this.productName = productName;
        this.price = price;
        this.addedAt = LocalDateTime.now();
    }
    
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public LocalDateTime getAddedAt() { return addedAt; }
    
    @Override
    public String toString() {
        return productName + " - $" + price;
    }
}

/**
 * Session-scoped user session
 */
class UserSession {
    private final String sessionId;
    private final LocalDateTime loginTime;
    private String username;
    private String role;
    private int pageViews = 0;
    private LocalDateTime lastAccessTime;
    private final Map<String, Object> attributes = new HashMap<>();
    
    public UserSession() {
        this.sessionId = "SESSION-" + UUID.randomUUID().toString().substring(0, 8);
        this.loginTime = LocalDateTime.now();
        this.lastAccessTime = LocalDateTime.now();
        System.out.println("UserSession created: " + sessionId);
    }
    
    @PreDestroy
    public void cleanup() {
        long durationMinutes = java.time.Duration.between(loginTime, LocalDateTime.now()).toMinutes();
        System.out.println("UserSession destroyed: " + sessionId + 
                         " (user: " + username + 
                         ", duration: " + durationMinutes + " minutes" +
                         ", page views: " + pageViews + ")");
    }
    
    public void login(String username, String role) {
        this.username = username;
        this.role = role;
        System.out.println("User logged in: " + username + " [" + sessionId + "]");
    }
    
    public void incrementPageViews() {
        pageViews++;
        lastAccessTime = LocalDateTime.now();
    }
    
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    // Getters
    public String getSessionId() { return sessionId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public int getPageViews() { return pageViews; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public LocalDateTime getLastAccessTime() { return lastAccessTime; }
}

/**
 * Session-scoped user preferences
 */
@Component
@SessionScope
class UserPreferences {
    private String theme = "light";
    private String language = "en";
    private int pageSize = 10;
    private boolean notificationsEnabled = true;
    private final Map<String, String> customSettings = new HashMap<>();
    
    public UserPreferences() {
        System.out.println("UserPreferences created");
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("UserPreferences destroyed (theme: " + theme + 
                         ", language: " + language + ")");
    }
    
    // Getters and setters
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean enabled) { this.notificationsEnabled = enabled; }
    
    public void setCustomSetting(String key, String value) {
        customSettings.put(key, value);
    }
    
    public String getCustomSetting(String key) {
        return customSettings.get(key);
    }
}

/**
 * REST Controller demonstrating session scope
 */
@RestController
@RequestMapping("/api/session")
class SessionScopeController {
    
    private final ShoppingCart shoppingCart;
    private final UserSession userSession;
    private final UserPreferences userPreferences;
    
    public SessionScopeController(ShoppingCart shoppingCart,
                                 UserSession userSession,
                                 UserPreferences userPreferences) {
        this.shoppingCart = shoppingCart;
        this.userSession = userSession;
        this.userPreferences = userPreferences;
    }
    
    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam(defaultValue = "USER") String role,
                       HttpSession session) {
        userSession.login(username, role);
        return "User logged in: " + username + 
               "\n  Session ID: " + userSession.getSessionId() +
               "\n  HTTP Session ID: " + session.getId() +
               "\n  Login time: " + userSession.getLoginTime();
    }
    
    @GetMapping("/cart/add")
    public String addToCart(@RequestParam String item,
                           @RequestParam(defaultValue = "99.99") double price) {
        shoppingCart.addItem(item, price);
        userSession.incrementPageViews();
        
        return "Item added to cart: " + item + 
               "\n  Cart ID: " + shoppingCart.getCartId() +
               "\n  Total items: " + shoppingCart.getItemCount() +
               "\n  Total amount: $" + shoppingCart.getTotalAmount();
    }
    
    @GetMapping("/cart/view")
    public String viewCart() {
        userSession.incrementPageViews();
        
        StringBuilder sb = new StringBuilder();
        sb.append("Shopping Cart: ").append(shoppingCart.getCartId()).append("\n");
        sb.append("Created: ").append(shoppingCart.getCreatedAt()).append("\n");
        sb.append("Items (").append(shoppingCart.getItemCount()).append("):\n");
        
        for (int i = 0; i < shoppingCart.getItems().size(); i++) {
            sb.append("  ").append(i + 1).append(". ")
              .append(shoppingCart.getItems().get(i)).append("\n");
        }
        
        sb.append("Total: $").append(shoppingCart.getTotalAmount());
        return sb.toString();
    }
    
    @DeleteMapping("/cart/clear")
    public String clearCart() {
        int itemCount = shoppingCart.getItemCount();
        shoppingCart.clear();
        return "Cart cleared (" + itemCount + " items removed)";
    }
    
    @GetMapping("/user/profile")
    public String getUserProfile() {
        userSession.incrementPageViews();
        
        return "User Profile:" +
               "\n  Session ID: " + userSession.getSessionId() +
               "\n  Username: " + userSession.getUsername() +
               "\n  Role: " + userSession.getRole() +
               "\n  Login time: " + userSession.getLoginTime() +
               "\n  Last access: " + userSession.getLastAccessTime() +
               "\n  Page views: " + userSession.getPageViews();
    }
    
    @PutMapping("/preferences")
    public String updatePreferences(@RequestParam(required = false) String theme,
                                   @RequestParam(required = false) String language,
                                   @RequestParam(required = false) Integer pageSize) {
        if (theme != null) userPreferences.setTheme(theme);
        if (language != null) userPreferences.setLanguage(language);
        if (pageSize != null) userPreferences.setPageSize(pageSize);
        
        return "Preferences updated:" +
               "\n  Theme: " + userPreferences.getTheme() +
               "\n  Language: " + userPreferences.getLanguage() +
               "\n  Page size: " + userPreferences.getPageSize();
    }
    
    @GetMapping("/preferences")
    public String getPreferences() {
        userSession.incrementPageViews();
        
        return "User Preferences:" +
               "\n  Theme: " + userPreferences.getTheme() +
               "\n  Language: " + userPreferences.getLanguage() +
               "\n  Page size: " + userPreferences.getPageSize() +
               "\n  Notifications: " + userPreferences.isNotificationsEnabled();
    }
    
    @GetMapping("/info")
    public String getSessionInfo(HttpSession session) {
        userSession.incrementPageViews();
        
        return "Session Information:" +
               "\n  HTTP Session ID: " + session.getId() +
               "\n  Bean Session ID: " + userSession.getSessionId() +
               "\n  Cart ID: " + shoppingCart.getCartId() +
               "\n  Cart items: " + shoppingCart.getItemCount() +
               "\n  Page views: " + userSession.getPageViews() +
               "\n  Session created: " + new Date(session.getCreationTime()) +
               "\n  Last accessed: " + new Date(session.getLastAccessedTime()) +
               "\n  Max inactive interval: " + session.getMaxInactiveInterval() + "s";
    }
}

/**
 * Key Points:
 * 
 * 1. Lifecycle:
 *    - Created on first access within session
 *    - Persists for entire session duration
 *    - Destroyed when session expires or invalidates
 *    - @PreDestroy called on session destruction
 * 
 * 2. Session Management:
 *    - One instance per HTTP session
 *    - Shared across multiple requests in same session
 *    - Different sessions have different instances
 *    - Session timeout configured in application properties
 * 
 * 3. Scoped Proxy:
 *    - Required for injection into singletons
 *    - Proxy delegates to current session's instance
 *    - proxyMode = TARGET_CLASS for classes
 *    - proxyMode = INTERFACES for interfaces
 * 
 * 4. Use Cases:
 *    ✓ Shopping carts
 *    ✓ User authentication state
 *    ✓ User preferences
 *    ✓ Multi-step wizards
 *    ✓ Session-level caching
 *    ✓ User activity tracking
 * 
 * 5. Memory Considerations:
 *    - One instance per active session
 *    - Can consume significant memory
 *    - Monitor session count
 *    - Set appropriate timeout
 *    - Consider session clustering
 * 
 * 6. Session Configuration:
 *    server.servlet.session.timeout=30m
 *    server.servlet.session.cookie.name=JSESSIONID
 *    server.servlet.session.cookie.http-only=true
 *    server.servlet.session.cookie.secure=true
 * 
 * 7. Session Storage:
 *    - In-memory (default)
 *    - Redis (Spring Session)
 *    - JDBC (Spring Session)
 *    - Hazelcast
 * 
 * 8. Best Practices:
 *    - Keep session beans lightweight
 *    - Clean up resources in @PreDestroy
 *    - Use appropriate session timeout
 *    - Consider serialization for clustering
 *    - Monitor memory usage
 *    - Implement session fixation protection
 */
