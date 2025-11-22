package com.example.serialization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java Serialization Pattern
 * 
 * Demonstrates native Java serialization using Serializable interface.
 * 
 * Key Concepts:
 * 1. Serializable interface
 * 2. ObjectOutputStream - Writing objects to streams
 * 3. ObjectInputStream - Reading objects from streams
 * 4. serialVersionUID - Version control
 * 5. transient keyword - Exclude fields from serialization
 * 6. Externalizable interface - Custom serialization logic
 * 7. readObject/writeObject methods - Custom serialization
 * 8. readResolve/writeReplace - Object replacement during serialization
 * 9. Serialization proxy pattern
 * 10. Deep copy using serialization
 * 
 * Use Cases:
 * - Session persistence
 * - Object caching
 * - Deep cloning objects
 * - RMI (Remote Method Invocation)
 * - Distributed computing
 * - Object persistence to disk
 * 
 * WARNING: Java serialization has security concerns and performance issues.
 * Consider using JSON/XML/Protobuf for most use cases.
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class JavaSerializationPattern {

    public static void main(String[] args) {
        SpringApplication.run(JavaSerializationPattern.class, args);
        
        // Demo Java serialization
        demonstrateJavaSerialization();
    }
    
    private static void demonstrateJavaSerialization() {
        System.out.println("=== Java Serialization Pattern Demo ===\n");
        
        try {
            // 1. Simple Object Serialization
            Person person = new Person(1L, "John Doe", "john@example.com", 30);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(person);
            oos.close();
            
            byte[] personBytes = baos.toByteArray();
            System.out.println("1. Serialized Person object: " + personBytes.length + " bytes");
            
            // Deserialization
            ByteArrayInputStream bais = new ByteArrayInputStream(personBytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Person deserializedPerson = (Person) ois.readObject();
            ois.close();
            
            System.out.println("Deserialized Person: " + deserializedPerson);
            System.out.println();
            
            // 2. Object with Transient Fields
            Account account = new Account(101L, "ACC-001", 5000.0, "secret-password");
            System.out.println("2. Before Serialization - Account: " + account);
            
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            ObjectOutputStream oos2 = new ObjectOutputStream(baos2);
            oos2.writeObject(account);
            oos2.close();
            
            ByteArrayInputStream bais2 = new ByteArrayInputStream(baos2.toByteArray());
            ObjectInputStream ois2 = new ObjectInputStream(bais2);
            Account deserializedAccount = (Account) ois2.readObject();
            ois2.close();
            
            System.out.println("After Deserialization - Account: " + deserializedAccount);
            System.out.println("Note: Password field is null due to 'transient' keyword");
            System.out.println();
            
            // 3. Complex Object Serialization
            Order order = new Order();
            order.setId(1001L);
            order.setOrderNumber("ORD-2024-001");
            order.setCustomerName("Alice Johnson");
            order.setOrderDate(LocalDateTime.now());
            order.setTotalAmount(299.99);
            
            List<OrderItem> items = new ArrayList<>();
            items.add(new OrderItem(1L, "Laptop", 1, 999.99));
            items.add(new OrderItem(2L, "Mouse", 2, 29.99));
            order.setItems(items);
            
            ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
            ObjectOutputStream oos3 = new ObjectOutputStream(baos3);
            oos3.writeObject(order);
            oos3.close();
            
            System.out.println("3. Serialized complex Order object: " + baos3.toByteArray().length + " bytes");
            
            ByteArrayInputStream bais3 = new ByteArrayInputStream(baos3.toByteArray());
            ObjectInputStream ois3 = new ObjectInputStream(bais3);
            Order deserializedOrder = (Order) ois3.readObject();
            ois3.close();
            
            System.out.println("Deserialized Order: " + deserializedOrder);
            System.out.println();
            
            // 4. Deep Copy using Serialization
            Person originalPerson = new Person(2L, "Jane Smith", "jane@example.com", 28);
            Person clonedPerson = (Person) deepCopy(originalPerson);
            
            System.out.println("4. Deep Copy using Serialization:");
            System.out.println("Original: " + originalPerson);
            System.out.println("Clone: " + clonedPerson);
            System.out.println("Are they same object? " + (originalPerson == clonedPerson));
            System.out.println();
            
            // 5. Custom Serialization with writeObject/readObject
            ConfigurableObject config = new ConfigurableObject("MyConfig", "v1.0");
            config.addProperty("host", "localhost");
            config.addProperty("port", "8080");
            config.addProperty("timeout", "30");
            
            ByteArrayOutputStream baos5 = new ByteArrayOutputStream();
            ObjectOutputStream oos5 = new ObjectOutputStream(baos5);
            oos5.writeObject(config);
            oos5.close();
            
            System.out.println("5. Custom Serialization:");
            System.out.println("Before: " + config);
            
            ByteArrayInputStream bais5 = new ByteArrayInputStream(baos5.toByteArray());
            ObjectInputStream ois5 = new ObjectInputStream(bais5);
            ConfigurableObject deserializedConfig = (ConfigurableObject) ois5.readObject();
            ois5.close();
            
            System.out.println("After: " + deserializedConfig);
            System.out.println();
            
            // 6. Externalizable Example
            ExternalizableProduct product = new ExternalizableProduct(
                100L, "Gaming Console", 499.99, "Electronics", true
            );
            
            ByteArrayOutputStream baos6 = new ByteArrayOutputStream();
            ObjectOutputStream oos6 = new ObjectOutputStream(baos6);
            oos6.writeObject(product);
            oos6.close();
            
            System.out.println("6. Externalizable Interface:");
            System.out.println("Before: " + product);
            
            ByteArrayInputStream bais6 = new ByteArrayInputStream(baos6.toByteArray());
            ObjectInputStream ois6 = new ObjectInputStream(bais6);
            ExternalizableProduct deserializedProduct = (ExternalizableProduct) ois6.readObject();
            ois6.close();
            
            System.out.println("After: " + deserializedProduct);
            System.out.println();
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Serialization Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Deep copy an object using serialization
     */
    private static Object deepCopy(Object original) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object copy = ois.readObject();
        ois.close();
        
        return copy;
    }
}

/**
 * Simple Serializable Person class
 */
class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private String email;
    private int age;
    
    public Person() {}
    
    public Person(Long id, String name, String email, int age) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    @Override
    public String toString() {
        return "Person{id=" + id + ", name='" + name + "', email='" + email + 
               "', age=" + age + "}";
    }
}

/**
 * Account class with transient field
 */
class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String accountNumber;
    private Double balance;
    
    // Transient field - will not be serialized
    private transient String password;
    
    public Account() {}
    
    public Account(Long id, String accountNumber, Double balance, String password) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.password = password;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    @Override
    public String toString() {
        return "Account{id=" + id + ", accountNumber='" + accountNumber + 
               "', balance=" + balance + ", password='" + password + "'}";
    }
}

/**
 * Order class for complex object serialization
 */
class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String orderNumber;
    private String customerName;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private List<OrderItem> items;
    
    public Order() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    @Override
    public String toString() {
        return "Order{id=" + id + ", orderNumber='" + orderNumber + 
               "', customerName='" + customerName + "', items=" + 
               (items != null ? items.size() : 0) + " items}";
    }
}

/**
 * OrderItem class
 */
class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String productName;
    private int quantity;
    private Double price;
    
    public OrderItem() {}
    
    public OrderItem(Long id, String productName, int quantity, Double price) {
        this.id = id;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}

/**
 * Custom serialization using writeObject/readObject
 */
class ConfigurableObject implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String version;
    private Map<String, String> properties;
    
    public ConfigurableObject(String name, String version) {
        this.name = name;
        this.version = version;
        this.properties = new HashMap<>();
    }
    
    public void addProperty(String key, String value) {
        properties.put(key, value);
    }
    
    // Custom serialization
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(properties.size());
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeUTF(entry.getValue());
        }
    }
    
    // Custom deserialization
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int size = in.readInt();
        properties = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = in.readUTF();
            String value = in.readUTF();
            properties.put(key, value);
        }
    }
    
    @Override
    public String toString() {
        return "ConfigurableObject{name='" + name + "', version='" + version + 
               "', properties=" + properties + "}";
    }
}

/**
 * Externalizable example for full control over serialization
 */
class ExternalizableProduct implements Externalizable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private Double price;
    private String category;
    private boolean inStock;
    
    public ExternalizableProduct() {}
    
    public ExternalizableProduct(Long id, String name, Double price, String category, boolean inStock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.inStock = inStock;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(id);
        out.writeUTF(name);
        out.writeDouble(price);
        out.writeUTF(category);
        out.writeBoolean(inStock);
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = in.readLong();
        name = in.readUTF();
        price = in.readDouble();
        category = in.readUTF();
        inStock = in.readBoolean();
    }
    
    @Override
    public String toString() {
        return "ExternalizableProduct{id=" + id + ", name='" + name + "', price=" + price + 
               ", category='" + category + "', inStock=" + inStock + "}";
    }
}

/**
 * REST Controller demonstrating Java serialization
 */
@RestController
@RequestMapping("/api/java-serialization")
class JavaSerializationController {
    
    @GetMapping("/serialize/person/{id}")
    public String serializePerson(@PathVariable Long id) throws IOException {
        Person person = new Person(id, "Person " + id, "person" + id + "@example.com", 30);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(person);
        oos.close();
        
        return "Serialized to " + baos.toByteArray().length + " bytes";
    }
    
    @PostMapping("/deep-copy")
    public Person deepCopyPerson(@RequestBody Person person) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(person);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Person copy = (Person) ois.readObject();
        ois.close();
        
        return copy;
    }
}
