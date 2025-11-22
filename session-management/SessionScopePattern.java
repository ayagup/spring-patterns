package com.example.session;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SESSION SCOPE PATTERN
 * ====================
 * 
 * Purpose:
 * - Create beans that are scoped to HTTP session lifecycle
 * - Each user session gets its own instance
 * - Bean lives as long as HTTP session is active
 * - Automatic cleanup when session expires
 * 
 * Key Components:
 * 1. @Scope("session") - Declares session scope
 * 2. ScopedProxyMode - Creates proxy for injection
 * 3. WebApplicationContext - Manages session-scoped beans
 * 4. Serializable - Enables session persistence
 * 
 * Use Cases:
 * - Shopping cart
 * - User preferences
 * - Wizard/multi-step forms
 * - User-specific caching
 * - Session-based state management
 */

// 1. SESSION-SCOPED SHOPPING CART
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
class ShoppingCart implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Map<String, CartItem> items = new ConcurrentHashMap<>();
    private final String sessionId = UUID.randomUUID().toString();
    private final LocalDateTime createdAt = LocalDateTime.now();
    
    public void addItem(String productId, String name, double price, int quantity) {
        items.compute(productId, (key, existing) -> {
            if (existing == null) {
                return new CartItem(productId, name, price, quantity);
            } else {
                existing.setQuantity(existing.getQuantity() + quantity);
                return existing;
            }
        });
    }
    
    public void removeItem(String productId) {
        items.remove(productId);
    }
    
    public void updateQuantity(String productId, int quantity) {
        CartItem item = items.get(productId);
        if (item != null) {
            if (quantity <= 0) {
                items.remove(productId);
            } else {
                item.setQuantity(quantity);
            }
        }
    }
    
    public void clear() {
        items.clear();
    }
    
    public double getTotal() {
        return items.values().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
    
    public int getItemCount() {
        return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
    
    public Collection<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    static class CartItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String productId;
        private String name;
        private double price;
        private int quantity;
        
        public CartItem(String productId, String name, double price, int quantity) {
            this.productId = productId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
        
        // Getters and setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        
        @Override
        public String toString() {
            return String.format("%s x%d ($%.2f)", name, quantity, price * quantity);
        }
    }
}

// 2. SESSION-SCOPED USER PREFERENCES
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
class UserPreferences implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String theme = "light";
    private String language = "en";
    private int pageSize = 10;
    private String dateFormat = "yyyy-MM-dd";
    private String timezone = "UTC";
    private boolean notifications = true;
    private Map<String, Object> customSettings = new HashMap<>();
    
    public void setTheme(String theme) {
        this.theme = theme;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }
    
    public void setCustomSetting(String key, Object value) {
        customSettings.put(key, value);
    }
    
    public Object getCustomSetting(String key) {
        return customSettings.get(key);
    }
    
    public void reset() {
        this.theme = "light";
        this.language = "en";
        this.pageSize = 10;
        this.dateFormat = "yyyy-MM-dd";
        this.timezone = "UTC";
        this.notifications = true;
        this.customSettings.clear();
    }
    
    // Getters
    public String getTheme() { return theme; }
    public String getLanguage() { return language; }
    public int getPageSize() { return pageSize; }
    public String getDateFormat() { return dateFormat; }
    public String getTimezone() { return timezone; }
    public boolean isNotifications() { return notifications; }
    public Map<String, Object> getCustomSettings() { return new HashMap<>(customSettings); }
}

// 3. SESSION-SCOPED WIZARD STATE
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
class WizardState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int currentStep = 1;
    private final int totalSteps = 5;
    private final Map<Integer, Map<String, Object>> stepData = new HashMap<>();
    private boolean completed = false;
    
    public void saveStepData(int step, Map<String, Object> data) {
        stepData.put(step, new HashMap<>(data));
    }
    
    public Map<String, Object> getStepData(int step) {
        return stepData.getOrDefault(step, new HashMap<>());
    }
    
    public boolean nextStep() {
        if (currentStep < totalSteps) {
            currentStep++;
            return true;
        }
        return false;
    }
    
    public boolean previousStep() {
        if (currentStep > 1) {
            currentStep--;
            return true;
        }
        return false;
    }
    
    public void goToStep(int step) {
        if (step >= 1 && step <= totalSteps) {
            currentStep = step;
        }
    }
    
    public boolean canProceed() {
        return stepData.containsKey(currentStep) && !stepData.get(currentStep).isEmpty();
    }
    
    public void complete() {
        if (currentStep == totalSteps) {
            completed = true;
        }
    }
    
    public void reset() {
        currentStep = 1;
        stepData.clear();
        completed = false;
    }
    
    public Map<String, Object> getAllData() {
        Map<String, Object> allData = new HashMap<>();
        stepData.forEach((step, data) -> 
            allData.put("step" + step, data)
        );
        return allData;
    }
    
    // Getters
    public int getCurrentStep() { return currentStep; }
    public int getTotalSteps() { return totalSteps; }
    public boolean isCompleted() { return completed; }
    public int getProgress() { return (currentStep * 100) / totalSteps; }
}

/**
 * DEMONSTRATION
 */
public class SessionScopePattern {
    
    public static void main(String[] args) {
        System.out.println("=== SESSION SCOPE PATTERN ===\n");
        
        // Demonstrate shopping cart
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("P001", "Laptop", 999.99, 1);
        cart.addItem("P002", "Mouse", 29.99, 2);
        cart.addItem("P003", "Keyboard", 79.99, 1);
        
        System.out.println("Shopping Cart:");
        System.out.println("  Session ID: " + cart.getSessionId());
        System.out.println("  Item Count: " + cart.getItemCount());
        System.out.println("  Total: $" + String.format("%.2f", cart.getTotal()));
        System.out.println("  Items:");
        cart.getItems().forEach(item -> System.out.println("    - " + item));
        System.out.println();
        
        // Demonstrate user preferences
        UserPreferences prefs = new UserPreferences();
        prefs.setTheme("dark");
        prefs.setLanguage("es");
        prefs.setPageSize(25);
        prefs.setCustomSetting("sidebarCollapsed", true);
        
        System.out.println("User Preferences:");
        System.out.println("  Theme: " + prefs.getTheme());
        System.out.println("  Language: " + prefs.getLanguage());
        System.out.println("  Page Size: " + prefs.getPageSize());
        System.out.println();
        
        // Demonstrate wizard
        WizardState wizard = new WizardState();
        wizard.saveStepData(1, Map.of("firstName", "John", "lastName", "Doe"));
        wizard.nextStep();
        wizard.saveStepData(2, Map.of("street", "123 Main St", "city", "NYC"));
        
        System.out.println("Wizard State:");
        System.out.println("  Current Step: " + wizard.getCurrentStep() + "/" + wizard.getTotalSteps());
        System.out.println("  Progress: " + wizard.getProgress() + "%");
        System.out.println("  Can Proceed: " + wizard.canProceed());
        System.out.println();
        
        System.out.println("Best Practices:");
        System.out.println("  ✓ Make beans Serializable");
        System.out.println("  ✓ Use ScopedProxyMode.TARGET_CLASS");
        System.out.println("  ✓ Keep session data minimal");
        System.out.println("  ✓ Use ConcurrentHashMap for thread safety");
        System.out.println("  ✓ Handle session expiration gracefully");
    }
}
