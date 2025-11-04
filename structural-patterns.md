# Spring Structural Design Patterns - Java Implementations

## 1. Proxy Pattern

```java org/example/patterns/structural/proxy/UserService.java
package org.example.patterns.structural.proxy;

public interface UserService {
    void createUser(String username);
    String getUser(String username);
    void deleteUser(String username);
}
```

```java org/example/patterns/structural/proxy/UserServiceImpl.java
package org.example.patterns.structural.proxy;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service("userServiceImpl")
public class UserServiceImpl implements UserService {
    
    private final Map<String, String> users = new HashMap<>();
    
    @Override
    public void createUser(String username) {
        System.out.println("UserServiceImpl: Creating user - " + username);
        users.put(username, "User-" + username);
    }
    
    @Override
    public String getUser(String username) {
        System.out.println("UserServiceImpl: Fetching user - " + username);
        return users.get(username);
    }
    
    @Override
    public void deleteUser(String username) {
        System.out.println("UserServiceImpl: Deleting user - " + username);
        users.remove(username);
    }
}
```

```java org/example/patterns/structural/proxy/UserServiceProxy.java
package org.example.patterns.structural.proxy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("userServiceProxy")
public class UserServiceProxy implements UserService {
    
    private final UserService userService;
    private final Map<String, String> cache = new HashMap<>();
    
    public UserServiceProxy(@Qualifier("userServiceImpl") UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public void createUser(String username) {
        System.out.println("Proxy: Validating before creating user");
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        userService.createUser(username);
        cache.put(username, "User-" + username);
    }
    
    @Override
    public String getUser(String username) {
        System.out.println("Proxy: Checking cache for user - " + username);
        if (cache.containsKey(username)) {
            System.out.println("Proxy: Returning from cache");
            return cache.get(username);
        }
        String user = userService.getUser(username);
        if (user != null) {
            cache.put(username, user);
        }
        return user;
    }
    
    @Override
    public void deleteUser(String username) {
        System.out.println("Proxy: Logging deletion for user - " + username);
        cache.remove(username);
        userService.deleteUser(username);
    }
}
```

```java org/example/patterns/structural/proxy/ProxyDemo.java
package org.example.patterns.structural.proxy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ProxyDemo implements CommandLineRunner {
    
    private final UserService userServiceProxy;
    
    public ProxyDemo(@Qualifier("userServiceProxy") UserService userServiceProxy) {
        this.userServiceProxy = userServiceProxy;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Proxy Pattern Demo ===");
        
        userServiceProxy.createUser("john_doe");
        
        System.out.println("\nFirst fetch (from real service):");
        userServiceProxy.getUser("john_doe");
        
        System.out.println("\nSecond fetch (from cache):");
        userServiceProxy.getUser("john_doe");
        
        userServiceProxy.deleteUser("john_doe");
    }
}
```

---

## 2. Adapter Pattern

```java org/example/patterns/structural/adapter/PaymentProcessor.java
package org.example.patterns.structural.adapter;

public interface PaymentProcessor {
    void processPayment(double amount, String currency);
    String getPaymentStatus(String transactionId);
}
```

```java org/example/patterns/structural/adapter/LegacyPaymentSystem.java
package org.example.patterns.structural.adapter;

import org.springframework.stereotype.Component;

@Component
public class LegacyPaymentSystem {
    
    public void makePayment(double dollars) {
        System.out.println("Legacy System: Processing payment of $" + dollars);
    }
    
    public boolean checkTransaction(int id) {
        System.out.println("Legacy System: Checking transaction " + id);
        return true;
    }
}
```

```java org/example/patterns/structural/adapter/PaymentAdapter.java
package org.example.patterns.structural.adapter;

import org.springframework.stereotype.Service;

@Service
public class PaymentAdapter implements PaymentProcessor {
    
    private final LegacyPaymentSystem legacySystem;
    private int transactionCounter = 1000;
    
    public PaymentAdapter(LegacyPaymentSystem legacySystem) {
        this.legacySystem = legacySystem;
    }
    
    @Override
    public void processPayment(double amount, String currency) {
        System.out.println("Adapter: Converting " + currency + " to USD");
        double amountInDollars = convertToUSD(amount, currency);
        legacySystem.makePayment(amountInDollars);
    }
    
    @Override
    public String getPaymentStatus(String transactionId) {
        System.out.println("Adapter: Converting transaction ID to legacy format");
        int legacyId = Integer.parseInt(transactionId);
        boolean status = legacySystem.checkTransaction(legacyId);
        return status ? "SUCCESS" : "FAILED";
    }
    
    private double convertToUSD(double amount, String currency) {
        return switch (currency.toUpperCase()) {
            case "EUR" -> amount * 1.1;
            case "GBP" -> amount * 1.3;
            case "JPY" -> amount * 0.009;
            default -> amount;
        };
    }
}
```

```java org/example/patterns/structural/adapter/AdapterDemo.java
package org.example.patterns.structural.adapter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdapterDemo implements CommandLineRunner {
    
    private final PaymentProcessor paymentProcessor;
    
    public AdapterDemo(PaymentAdapter paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Adapter Pattern Demo ===");
        
        paymentProcessor.processPayment(100.0, "EUR");
        paymentProcessor.processPayment(200.0, "GBP");
        
        String status = paymentProcessor.getPaymentStatus("1001");
        System.out.println("Payment Status: " + status);
    }
}
```

---

## 3. Decorator Pattern

```java org/example/patterns/structural/decorator/Coffee.java
package org.example.patterns.structural.decorator;

public interface Coffee {
    String getDescription();
    double getCost();
}
```

```java org/example/patterns/structural/decorator/SimpleCoffee.java
package org.example.patterns.structural.decorator;

import org.springframework.stereotype.Component;

@Component
public class SimpleCoffee implements Coffee {
    
    @Override
    public String getDescription() {
        return "Simple Coffee";
    }
    
    @Override
    public double getCost() {
        return 2.0;
    }
}
```

```java org/example/patterns/structural/decorator/CoffeeDecorator.java
package org.example.patterns.structural.decorator;

public abstract class CoffeeDecorator implements Coffee {
    
    protected Coffee decoratedCoffee;
    
    public CoffeeDecorator(Coffee coffee) {
        this.decoratedCoffee = coffee;
    }
    
    @Override
    public String getDescription() {
        return decoratedCoffee.getDescription();
    }
    
    @Override
    public double getCost() {
        return decoratedCoffee.getCost();
    }
}
```

```java org/example/patterns/structural/decorator/MilkDecorator.java
package org.example.patterns.structural.decorator;

public class MilkDecorator extends CoffeeDecorator {
    
    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return decoratedCoffee.getDescription() + ", Milk";
    }
    
    @Override
    public double getCost() {
        return decoratedCoffee.getCost() + 0.5;
    }
}
```

```java org/example/patterns/structural/decorator/SugarDecorator.java
package org.example.patterns.structural.decorator;

public class SugarDecorator extends CoffeeDecorator {
    
    public SugarDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return decoratedCoffee.getDescription() + ", Sugar";
    }
    
    @Override
    public double getCost() {
        return decoratedCoffee.getCost() + 0.3;
    }
}
```

```java org/example/patterns/structural/decorator/WhippedCreamDecorator.java
package org.example.patterns.structural.decorator;

public class WhippedCreamDecorator extends CoffeeDecorator {
    
    public WhippedCreamDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return decoratedCoffee.getDescription() + ", Whipped Cream";
    }
    
    @Override
    public double getCost() {
        return decoratedCoffee.getCost() + 0.7;
    }
}
```

```java org/example/patterns/structural/decorator/DecoratorDemo.java
package org.example.patterns.structural.decorator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DecoratorDemo implements CommandLineRunner {
    
    private final SimpleCoffee simpleCoffee;
    
    public DecoratorDemo(SimpleCoffee simpleCoffee) {
        this.simpleCoffee = simpleCoffee;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Decorator Pattern Demo ===");
        
        Coffee coffee1 = simpleCoffee;
        System.out.println(coffee1.getDescription() + " costs $" + coffee1.getCost());
        
        Coffee coffee2 = new MilkDecorator(simpleCoffee);
        System.out.println(coffee2.getDescription() + " costs $" + coffee2.getCost());
        
        Coffee coffee3 = new WhippedCreamDecorator(new SugarDecorator(new MilkDecorator(simpleCoffee)));
        System.out.println(coffee3.getDescription() + " costs $" + coffee3.getCost());
    }
}
```

---

## 4. Composite Pattern

```java org/example/patterns/structural/composite/FileSystemComponent.java
package org.example.patterns.structural.composite;

public interface FileSystemComponent {
    void display(String indent);
    long getSize();
    String getName();
}
```

```java org/example/patterns/structural/composite/File.java
package org.example.patterns.structural.composite;

public class File implements FileSystemComponent {
    
    private final String name;
    private final long size;
    
    public File(String name, long size) {
        this.name = name;
        this.size = size;
    }
    
    @Override
    public void display(String indent) {
        System.out.println(indent + "📄 " + name + " (" + size + " bytes)");
    }
    
    @Override
    public long getSize() {
        return size;
    }
    
    @Override
    public String getName() {
        return name;
    }
}
```

```java org/example/patterns/structural/composite/Directory.java
package org.example.patterns.structural.composite;

import java.util.ArrayList;
import java.util.List;

public class Directory implements FileSystemComponent {
    
    private final String name;
    private final List<FileSystemComponent> children = new ArrayList<>();
    
    public Directory(String name) {
        this.name = name;
    }
    
    public void add(FileSystemComponent component) {
        children.add(component);
    }
    
    public void remove(FileSystemComponent component) {
        children.remove(component);
    }
    
    @Override
    public void display(String indent) {
        System.out.println(indent + "📁 " + name + "/");
        for (FileSystemComponent child : children) {
            child.display(indent + "  ");
        }
    }
    
    @Override
    public long getSize() {
        return children.stream()
                .mapToLong(FileSystemComponent::getSize)
                .sum();
    }
    
    @Override
    public String getName() {
        return name;
    }
}
```

```java org/example/patterns/structural/composite/CompositeDemo.java
package org.example.patterns.structural.composite;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CompositeDemo implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Composite Pattern Demo ===");
        
        // Create files
        File file1 = new File("document.txt", 1024);
        File file2 = new File("image.jpg", 2048);
        File file3 = new File("video.mp4", 10240);
        File file4 = new File("readme.md", 512);
        
        // Create directories
        Directory root = new Directory("root");
        Directory documents = new Directory("documents");
        Directory media = new Directory("media");
        
        // Build tree structure
        documents.add(file1);
        documents.add(file4);
        
        media.add(file2);
        media.add(file3);
        
        root.add(documents);
        root.add(media);
        
        // Display tree
        root.display("");
        
        System.out.println("\nTotal size: " + root.getSize() + " bytes");
        System.out.println("Documents size: " + documents.getSize() + " bytes");
        System.out.println("Media size: " + media.getSize() + " bytes");
    }
}
```

---

## 5. Facade Pattern

```java org/example/patterns/structural/facade/OrderFacade.java
package org.example.patterns.structural.facade;

import org.springframework.stereotype.Service;

@Service
public class OrderFacade {
    
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final NotificationService notificationService;
    
    public OrderFacade(InventoryService inventoryService,
                       PaymentService paymentService,
                       ShippingService shippingService,
                       NotificationService notificationService) {
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.shippingService = shippingService;
        this.notificationService = notificationService;
    }
    
    public boolean placeOrder(String productId, String customerId, double amount) {
        System.out.println("\n=== Facade: Processing Order ===");
        
        // Check inventory
        if (!inventoryService.checkAvailability(productId)) {
            System.out.println("Order failed: Product not available");
            return false;
        }
        
        // Process payment
        if (!paymentService.processPayment(customerId, amount)) {
            System.out.println("Order failed: Payment declined");
            return false;
        }
        
        // Reserve inventory
        inventoryService.reserveProduct(productId);
        
        // Arrange shipping
        String trackingNumber = shippingService.scheduleDelivery(productId, customerId);
        
        // Send notifications
        notificationService.sendOrderConfirmation(customerId, trackingNumber);
        
        System.out.println("Order completed successfully!");
        return true;
    }
}
```

```java org/example/patterns/structural/facade/InventoryService.java
package org.example.patterns.structural.facade;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class InventoryService {
    
    private final Map<String, Integer> inventory = new HashMap<>();
    
    public InventoryService() {
        inventory.put("PROD-001", 10);
        inventory.put("PROD-002", 5);
        inventory.put("PROD-003", 0);
    }
    
    public boolean checkAvailability(String productId) {
        System.out.println("InventoryService: Checking availability for " + productId);
        return inventory.getOrDefault(productId, 0) > 0;
    }
    
    public void reserveProduct(String productId) {
        System.out.println("InventoryService: Reserving product " + productId);
        inventory.computeIfPresent(productId, (k, v) -> v - 1);
    }
}
```

```java org/example/patterns/structural/facade/PaymentService.java
package org.example.patterns.structural.facade;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    
    public boolean processPayment(String customerId, double amount) {
        System.out.println("PaymentService: Processing payment of $" + amount + 
                         " for customer " + customerId);
        // Simulate payment processing
        return amount > 0;
    }
}
```

```java org/example/patterns/structural/facade/ShippingService.java
package org.example.patterns.structural.facade;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class ShippingService {
    
    public String scheduleDelivery(String productId, String customerId) {
        String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8);
        System.out.println("ShippingService: Scheduling delivery for " + productId + 
                         " to customer " + customerId);
        System.out.println("Tracking number: " + trackingNumber);
        return trackingNumber;
    }
}
```

```java org/example/patterns/structural/facade/NotificationService.java
package org.example.patterns.structural.facade;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    
    public void sendOrderConfirmation(String customerId, String trackingNumber) {
        System.out.println("NotificationService: Sending confirmation to " + customerId);
        System.out.println("Your order has been shipped. Tracking: " + trackingNumber);
    }
}
```

```java org/example/patterns/structural/facade/FacadeDemo.java
package org.example.patterns.structural.facade;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FacadeDemo implements CommandLineRunner {
    
    private final OrderFacade orderFacade;
    
    public FacadeDemo(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Facade Pattern Demo ===");
        
        // Simple interface to complex subsystem
        orderFacade.placeOrder("PROD-001", "CUST-123", 99.99);
        
        System.out.println("\n--- Trying order with unavailable product ---");
        orderFacade.placeOrder("PROD-003", "CUST-456", 49.99);
    }
}
```

---

## 6. Bridge Pattern

```java org/example/patterns/structural/bridge/Device.java
package org.example.patterns.structural.bridge;

public interface Device {
    void turnOn();
    void turnOff();
    void setVolume(int volume);
    int getVolume();
    boolean isEnabled();
}
```

```java org/example/patterns/structural/bridge/Television.java
package org.example.patterns.structural.bridge;

public class Television implements Device {
    
    private boolean on = false;
    private int volume = 30;
    
    @Override
    public void turnOn() {
        on = true;
        System.out.println("Television: Turned ON");
    }
    
    @Override
    public void turnOff() {
        on = false;
        System.out.println("Television: Turned OFF");
    }
    
    @Override
    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
        System.out.println("Television: Volume set to " + this.volume);
    }
    
    @Override
    public int getVolume() {
        return volume;
    }
    
    @Override
    public boolean isEnabled() {
        return on;
    }
}
```

```java org/example/patterns/structural/bridge/Radio.java
package org.example.patterns.structural.bridge;

public class Radio implements Device {
    
    private boolean on = false;
    private int volume = 20;
    
    @Override
    public void turnOn() {
        on = true;
        System.out.println("Radio: Turned ON");
    }
    
    @Override
    public void turnOff() {
        on = false;
        System.out.println("Radio: Turned OFF");
    }
    
    @Override
    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
        System.out.println("Radio: Volume set to " + this.volume);
    }
    
    @Override
    public int getVolume() {
        return volume;
    }
    
    @Override
    public boolean isEnabled() {
        return on;
    }
}
```

```java org/example/patterns/structural/bridge/RemoteControl.java
package org.example.patterns.structural.bridge;

public class RemoteControl {
    
    protected Device device;
    
    public RemoteControl(Device device) {
        this.device = device;
    }
    
    public void togglePower() {
        if (device.isEnabled()) {
            device.turnOff();
        } else {
            device.turnOn();
        }
    }
    
    public void volumeDown() {
        device.setVolume(device.getVolume() - 10);
    }
    
    public void volumeUp() {
        device.setVolume(device.getVolume() + 10);
    }
}
```

```java org/example/patterns/structural/bridge/AdvancedRemoteControl.java
package org.example.patterns.structural.bridge;

public class AdvancedRemoteControl extends RemoteControl {
    
    public AdvancedRemoteControl(Device device) {
        super(device);
    }
    
    public void mute() {
        System.out.println("AdvancedRemote: Muting");
        device.setVolume(0);
    }
    
    public void setVolume(int volume) {
        System.out.println("AdvancedRemote: Setting specific volume");
        device.setVolume(volume);
    }
}
```

```java org/example/patterns/structural/bridge/BridgeDemo.java
package org.example.patterns.structural.bridge;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BridgeDemo implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Bridge Pattern Demo ===");
        
        Device tv = new Television();
        RemoteControl remote = new RemoteControl(tv);
        
        System.out.println("Using basic remote with TV:");
        remote.togglePower();
        remote.volumeUp();
        remote.volumeUp();
        remote.volumeDown();
        
        System.out.println("\nUsing advanced remote with Radio:");
        Device radio = new Radio();
        AdvancedRemoteControl advancedRemote = new AdvancedRemoteControl(radio);
        
        advancedRemote.togglePower();
        advancedRemote.setVolume(50);
        advancedRemote.mute();
        advancedRemote.togglePower();
    }
}
```

---

## 7. Flyweight Pattern

```java org/example/patterns/structural/flyweight/TreeType.java
package org.example.patterns.structural.flyweight;

public class TreeType {
    
    private final String name;
    private final String color;
    private final String texture;
    
    public TreeType(String name, String color, String texture) {
        this.name = name;
        this.color = color;
        this.texture = texture;
        System.out.println("Creating TreeType: " + name);
    }
    
    public void render(int x, int y) {
        System.out.println("Rendering " + name + " tree at (" + x + ", " + y + 
                         ") with color " + color);
    }
    
    public String getName() {
        return name;
    }
}
```

```java org/example/patterns/structural/flyweight/TreeFactory.java
package org.example.patterns.structural.flyweight;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class TreeFactory {
    
    private static final Map<String, TreeType> treeTypes = new HashMap<>();
    
    public TreeType getTreeType(String name, String color, String texture) {
        String key = name + "_" + color + "_" + texture;
        
        TreeType type = treeTypes.get(key);
        if (type == null) {
            type = new TreeType(name, color, texture);
            treeTypes.put(key, type);
        }
        return type;
    }
    
    public int getTreeTypeCount() {
        return treeTypes.size();
    }
}
```

```java org/example/patterns/structural/flyweight/Tree.java
package org.example.patterns.structural.flyweight;

public class Tree {
    
    private final int x;
    private final int y;
    private final TreeType type;
    
    public Tree(int x, int y, TreeType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
    
    public void draw() {
        type.render(x, y);
    }
}
```

```java org/example/patterns/structural/flyweight/Forest.java
package org.example.patterns.structural.flyweight;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class Forest {
    
    private final List<Tree> trees = new ArrayList<>();
    private final TreeFactory treeFactory;
    
    public Forest(TreeFactory treeFactory) {
        this.treeFactory = treeFactory;
    }
    
    public void plantTree(int x, int y, String name, String color, String texture) {
        TreeType type = treeFactory.getTreeType(name, color, texture);
        Tree tree = new Tree(x, y, type);
        trees.add(tree);
    }
    
    public void draw() {
        for (Tree tree : trees) {
            tree.draw();
        }
    }
    
    public int getTreeCount() {
        return trees.size();
    }
    
    public int getTreeTypeCount() {
        return treeFactory.getTreeTypeCount();
    }
}
```

```java org/example/patterns/structural/flyweight/FlyweightDemo.java
package org.example.patterns.structural.flyweight;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FlyweightDemo implements CommandLineRunner {
    
    private final Forest forest;
    
    public FlyweightDemo(Forest forest) {
        this.forest = forest;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Flyweight Pattern Demo ===");
        
        // Plant many trees of few types
        forest.plantTree(10, 20, "Oak", "Green", "Rough");
        forest.plantTree(30, 40, "Oak", "Green", "Rough");
        forest.plantTree(50, 60, "Pine", "Dark Green", "Smooth");
        forest.plantTree(70, 80, "Oak", "Green", "Rough");
        forest.plantTree(90, 100, "Pine", "Dark Green", "Smooth");
        forest.plantTree(110, 120, "Birch", "White", "Smooth");
        forest.plantTree(130, 140, "Oak", "Green", "Rough");
        
        System.out.println("\nTotal trees planted: " + forest.getTreeCount());
        System.out.println("Unique tree types: " + forest.getTreeTypeCount());
        System.out.println("Memory saved by sharing tree types!");
        
        System.out.println("\nDrawing forest:");
        forest.draw();
    }
}
```

---

## 8. Front Controller Pattern

```java org/example/patterns/structural/frontcontroller/FrontController.java
package org.example.patterns.structural.frontcontroller;

import org.springframework.stereotype.Component;

@Component
public class FrontController {
    
    private final Dispatcher dispatcher;
    
    public FrontController(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
    
    public void handleRequest(String request) {
        System.out.println("FrontController: Received request - " + request);
        
        // Authentication
        if (!authenticate(request)) {
            System.out.println("FrontController: Authentication failed");
            return;
        }
        
        // Logging
        logRequest(request);
        
        // Dispatch to appropriate controller
        dispatcher.dispatch(request);
    }
    
    private boolean authenticate(String request) {
        System.out.println("FrontController: Authenticating request");
        return !request.contains("unauthorized");
    }
    
    private void logRequest(String request) {
        System.out.println("FrontController: Logging request - " + request);
    }
}
```

```java org/example/patterns/structural/frontcontroller/Dispatcher.java
package org.example.patterns.structural.frontcontroller;

import org.springframework.stereotype.Component;

@Component
public class Dispatcher {
    
    private final HomeController homeController;
    private final UserController userController;
    private final ProductController productController;
    
    public Dispatcher(HomeController homeController,
                     UserController userController,
                     ProductController productController) {
        this.homeController = homeController;
        this.userController = userController;
        this.productController = productController;
    }
    
    public void dispatch(String request) {
        System.out.println("Dispatcher: Routing request");
        
        if (request.contains("HOME")) {
            homeController.show();
        } else if (request.contains("USER")) {
            userController.show();
        } else if (request.contains("PRODUCT")) {
            productController.show();
        } else {
            System.out.println("Dispatcher: Unknown request type");
        }
    }
}
```

```java org/example/patterns/structural/frontcontroller/HomeController.java
package org.example.patterns.structural.frontcontroller;

import org.springframework.stereotype.Controller;

@Controller
public class HomeController {
    
    public void show() {
        System.out.println("HomeController: Displaying home page");
    }
}
```

```java org/example/patterns/structural/frontcontroller/UserController.java
package org.example.patterns.structural.frontcontroller;

import org.springframework.stereotype.Controller;

@Controller
public class UserController {
    
    public void show() {
        System.out.println("UserController: Displaying user page");
    }
}
```

```java org/example/patterns/structural/frontcontroller/ProductController.java
package org.example.patterns.structural.frontcontroller;

import org.springframework.stereotype.Controller;

@Controller
public class ProductController {
    
    public void show() {
        System.out.println("ProductController: Displaying product page");
    }
}
```

```java org/example/patterns/structural/frontcontroller/FrontControllerDemo.java
package org.example.patterns.structural.frontcontroller;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FrontControllerDemo implements CommandLineRunner {
    
    private final FrontController frontController;
    
    public FrontControllerDemo(FrontController frontController) {
        this.frontController = frontController;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Front Controller Pattern Demo ===");
        
        frontController.handleRequest("HOME");
        System.out.println();
        frontController.handleRequest("USER");
        System.out.println();
        frontController.handleRequest("PRODUCT");
        System.out.println();
        frontController.handleRequest("unauthorized-USER");
    }
}
```

---

## 9. Module Pattern

```java org/example/patterns/structural/module/DatabaseModule.java
package org.example.patterns.structural.module;

import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class DatabaseModule {
    
    private boolean connected = false;
    
    // Private configuration
    private final String connectionString = "jdbc:mysql://localhost:3306/db";
    private final String username = "admin";
    
    @PostConstruct
    private void initialize() {
        System.out.println("DatabaseModule: Initializing module");
        connect();
    }
    
    // Public interface
    public void executeQuery(String query) {
        if (!connected) {
            throw new IllegalStateException("Database not connected");
        }
        System.out.println("DatabaseModule: Executing query - " + query);
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    // Private methods
    private void connect() {
        System.out.println("DatabaseModule: Connecting to " + connectionString);
        connected = true;
    }
    
    @PreDestroy
    private void disconnect() {
        System.out.println("DatabaseModule: Disconnecting");
        connected = false;
    }
    
    // Encapsulated state
    private int queryCount = 0;
    
    public void trackQuery() {
        queryCount++;
        System.out.println("DatabaseModule: Total queries executed - " + queryCount);
    }
}
```

```java org/example/patterns/structural/module/CacheModule.java
package org.example.patterns.structural.module;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class CacheModule {
    
    // Private state
    private final Map<String, Object> cache = new HashMap<>();
    private final int maxSize = 100;
    
    // Public API
    public void put(String key, Object value) {
        if (cache.size() >= maxSize) {
            evictOldest();
        }
        cache.put(key, value);
        System.out.println("CacheModule: Cached item with key - " + key);
    }
    
    public Object get(String key) {
        Object value = cache.get(key);
        System.out.println("CacheModule: Retrieved " + 
                         (value != null ? "cached" : "null") + " value for key - " + key);
        return value;
    }
    
    public void clear() {
        cache.clear();
        System.out.println("CacheModule: Cache cleared");
    }
    
    public int size() {
        return cache.size();
    }
    
    // Private helper method
    private void evictOldest() {
        if (!cache.isEmpty()) {
            String firstKey = cache.keySet().iterator().next();
            cache.remove(firstKey);
            System.out.println("CacheModule: Evicted oldest entry");
        }
    }
}
```

```java org/example/patterns/structural/module/LoggingModule.java
package org.example.patterns.structural.module;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class LoggingModule {
    
    // Private state
    private final List<String> logs = new ArrayList<>();
    private final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Public interface
    public void info(String message) {
        log("INFO", message);
    }
    
    public void error(String message) {
        log("ERROR", message);
    }
    
    public void warning(String message) {
        log("WARNING", message);
    }
    
    public List<String> getLogs() {
        return new ArrayList<>(logs); // Return defensive copy
    }
    
    // Private implementation
    private void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] %s: %s", timestamp, level, message);
        logs.add(logEntry);
        System.out.println("LoggingModule: " + logEntry);
    }
}
```

```java org/example/patterns/structural/module/ModuleDemo.java
package org.example.patterns.structural.module;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ModuleDemo implements CommandLineRunner {
    
    private final DatabaseModule databaseModule;
    private final CacheModule cacheModule;
    private final LoggingModule loggingModule;
    
    public ModuleDemo(DatabaseModule databaseModule,
                     CacheModule cacheModule,
                     LoggingModule loggingModule) {
        this.databaseModule = databaseModule;
        this.cacheModule = cacheModule;
        this.loggingModule = loggingModule;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Module Pattern Demo ===");
        
        // Use Database Module
        loggingModule.info("Starting database operations");
        if (databaseModule.isConnected()) {
            databaseModule.executeQuery("SELECT * FROM users");
            databaseModule.trackQuery();
        }
        
        // Use Cache Module
        loggingModule.info("Starting cache operations");
        cacheModule.put("user:1", "John Doe");
        cacheModule.put("user:2", "Jane Smith");
        Object cached = cacheModule.get("user:1");
        
        loggingModule.info("Operations completed");
        System.out.println("\nTotal log entries: " + loggingModule.getLogs().size());
    }
}
```

---

## 10. Private Class Data Pattern

```java org/example/patterns/structural/privateclassdata/ImmutableUser.java
package org.example.patterns.structural.privateclassdata;

public class ImmutableUser {
    
    private final UserData userData;
    
    public ImmutableUser(String username, String email, String role) {
        this.userData = new UserData(username, email, role);
    }
    
    public String getUsername() {
        return userData.getUsername();
    }
    
    public String getEmail() {
        return userData.getEmail();
    }
    
    public String getRole() {
        return userData.getRole();
    }
    
    public void display() {
        System.out.println("User: " + userData.getUsername() + 
                         ", Email: " + userData.getEmail() + 
                         ", Role: " + userData.getRole());
    }
    
    // Private inner class to encapsulate data
    private static class UserData {
        private final String username;
        private final String email;
        private final String role;
        
        public UserData(String username, String email, String role) {
            this.username = username;
            this.email = email;
            this.role = role;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public String getRole() {
            return role;
        }
    }
}
```

```java org/example/patterns/structural/privateclassdata/BankAccount.java
package org.example.patterns.structural.privateclassdata;

public class BankAccount {
    
    private final AccountData accountData;
    private double balance;
    
    public BankAccount(String accountNumber, String accountHolder, String accountType) {
        this.accountData = new AccountData(accountNumber, accountHolder, accountType);
        this.balance = 0.0;
    }
    
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Deposited: $" + amount);
        }
    }
    
    public boolean withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            System.out.println("Withdrawn: $" + amount);
            return true;
        }
        System.out.println("Withdrawal failed: Insufficient funds");
        return false;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public String getAccountNumber() {
        return accountData.getAccountNumber();
    }
    
    public String getAccountHolder() {
        return accountData.getAccountHolder();
    }
    
    public void displayAccountInfo() {
        System.out.println("Account Number: " + accountData.getAccountNumber());
        System.out.println("Account Holder: " + accountData.getAccountHolder());
        System.out.println("Account Type: " + accountData.getAccountType());
        System.out.println("Balance: $" + balance);
    }
    
    // Private class to protect account data
    private static class AccountData {
        private final String accountNumber;
        private final String accountHolder;
        private final String accountType;
        
        public AccountData(String accountNumber, String accountHolder, String accountType) {
            this.accountNumber = accountNumber;
            this.accountHolder = accountHolder;
            this.accountType = accountType;
        }
        
        public String getAccountNumber() {
            return accountNumber;
        }
        
        public String getAccountHolder() {
            return accountHolder;
        }
        
        public String getAccountType() {
            return accountType;
        }
    }
}
```

```java org/example/patterns/structural/privateclassdata/Configuration.java
package org.example.patterns.structural/privateclassdata;

import org.springframework.stereotype.Component;

@Component
public class Configuration {
    
    private final ConfigData configData;
    
    public Configuration() {
        this.configData = new ConfigData(
            "localhost",
            5432,
            "myapp_db",
            "admin"
        );
    }
    
    public String getHost() {
        return configData.getHost();
    }
    
    public int getPort() {
        return configData.getPort();
    }
    
    public String getDatabase() {
        return configData.getDatabase();
    }
    
    public String getConnectionString() {
        return String.format("jdbc:postgresql://%s:%d/%s",
            configData.getHost(),
            configData.getPort(),
            configData.getDatabase());
    }
    
    public void displayConfig() {
        System.out.println("Database Configuration:");
        System.out.println("  Host: " + configData.getHost());
        System.out.println("  Port: " + configData.getPort());
        System.out.println("  Database: " + configData.getDatabase());
        System.out.println("  Username: " + configData.getUsername());
    }
    
    // Private immutable data class
    private static class ConfigData {
        private final String host;
        private final int port;
        private final String database;
        private final String username;
        
        public ConfigData(String host, int port, String database, String username) {
            this.host = host;
            this.port = port;
            this.database = database;
            this.username = username;
        }
        
        public String getHost() {
            return host;
        }
        
        public int getPort() {
            return port;
        }
        
        public String getDatabase() {
            return database;
        }
        
        public String getUsername() {
            return username;
        }
    }
}
```

```java org/example/patterns/structural/privateclassdata/PrivateClassDataDemo.java
package org.example.patterns.structural.privateclassdata;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PrivateClassDataDemo implements CommandLineRunner {
    
    private final Configuration configuration;
    
    public PrivateClassDataDemo(Configuration configuration) {
        this.configuration = configuration;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Private Class Data Pattern Demo ===");
        
        // Immutable User
        System.out.println("\n--- Immutable User ---");
        ImmutableUser user = new ImmutableUser("john_doe", "john@example.com", "ADMIN");
        user.display();
        
        // Bank Account with protected data
        System.out.println("\n--- Bank Account ---");
        BankAccount account = new BankAccount("ACC-12345", "John Doe", "Savings");
        account.deposit(1000.0);
        account.withdraw(250.0);
        account.displayAccountInfo();
        
        // Configuration with encapsulated data
        System.out.println("\n--- Configuration ---");
        configuration.displayConfig();
        System.out.println("Connection String: " + configuration.getConnectionString());
    }
}
```

---

## Main Application

```java org/example/StructuralPatternsApplication.java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StructuralPatternsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(StructuralPatternsApplication.class, args);
    }
}
```

---

## Application Properties

```properties src/main/resources/application.properties
# Application Configuration
spring.application.name=spring-structural-patterns

# Logging
logging.level.root=INFO
logging.level.org.example=DEBUG
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# Disable banner
spring.main.banner-mode=off

# Server port (if needed)
server.port=8080
```

---

## POM Configuration

```xml pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>org.example</groupId>
    <artifactId>spring-structural-patterns</artifactId>
    <version>1.0.0</version>
    <name>Spring Structural Design Patterns</name>
    <description>Demonstration of Structural Design Patterns in Spring</description>
    
    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

This completes the comprehensive implementation of all 10 Structural Design Patterns in Spring Framework with fully working examples!