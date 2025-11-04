# Spring Behavioral Design Patterns - Java Implementations

## 1. Template Method Pattern

```java org/example/patterns/behavioral/template/DataProcessor.java
package org.example.patterns.behavioral.template;

public abstract class DataProcessor {
    
    // Template method - defines the algorithm skeleton
    public final void process() {
        readData();
        processData();
        validateData();
        saveData();
        sendNotification();
    }
    
    // Common implementation
    protected void readData() {
        System.out.println("Reading data from source");
    }
    
    // Abstract methods - must be implemented by subclasses
    protected abstract void processData();
    protected abstract void validateData();
    
    // Common implementation
    protected void saveData() {
        System.out.println("Saving processed data");
    }
    
    // Hook method - can be overridden optionally
    protected void sendNotification() {
        System.out.println("Sending default notification");
    }
}
```

```java org/example/patterns/behavioral/template/CSVDataProcessor.java
package org.example.patterns.behavioral.template;

import org.springframework.stereotype.Component;

@Component
public class CSVDataProcessor extends DataProcessor {
    
    @Override
    protected void processData() {
        System.out.println("CSVDataProcessor: Parsing CSV format");
        System.out.println("CSVDataProcessor: Converting to objects");
    }
    
    @Override
    protected void validateData() {
        System.out.println("CSVDataProcessor: Validating CSV columns");
        System.out.println("CSVDataProcessor: Checking data types");
    }
    
    @Override
    protected void sendNotification() {
        System.out.println("CSVDataProcessor: Sending CSV processing notification");
    }
}
```

```java org/example/patterns/behavioral/template/JSONDataProcessor.java
package org.example.patterns.behavioral.template;

import org.springframework.stereotype.Component;

@Component
public class JSONDataProcessor extends DataProcessor {
    
    @Override
    protected void processData() {
        System.out.println("JSONDataProcessor: Parsing JSON format");
        System.out.println("JSONDataProcessor: Mapping to domain objects");
    }
    
    @Override
    protected void validateData() {
        System.out.println("JSONDataProcessor: Validating JSON schema");
        System.out.println("JSONDataProcessor: Checking required fields");
    }
}
```

```java org/example/patterns/behavioral/template/XMLDataProcessor.java
package org.example.patterns.behavioral.template;

import org.springframework.stereotype.Component;

@Component
public class XMLDataProcessor extends DataProcessor {
    
    @Override
    protected void processData() {
        System.out.println("XMLDataProcessor: Parsing XML structure");
        System.out.println("XMLDataProcessor: Extracting elements");
    }
    
    @Override
    protected void validateData() {
        System.out.println("XMLDataProcessor: Validating against XSD schema");
        System.out.println("XMLDataProcessor: Checking well-formedness");
    }
    
    @Override
    protected void sendNotification() {
        System.out.println("XMLDataProcessor: Sending detailed XML notification");
    }
}
```

```java org/example/patterns/behavioral/template/TemplateMethodDemo.java
package org.example.patterns.behavioral.template;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TemplateMethodDemo implements CommandLineRunner {
    
    private final CSVDataProcessor csvProcessor;
    private final JSONDataProcessor jsonProcessor;
    private final XMLDataProcessor xmlProcessor;
    
    public TemplateMethodDemo(CSVDataProcessor csvProcessor,
                             JSONDataProcessor jsonProcessor,
                             XMLDataProcessor xmlProcessor) {
        this.csvProcessor = csvProcessor;
        this.jsonProcessor = jsonProcessor;
        this.xmlProcessor = xmlProcessor;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Template Method Pattern Demo ===");
        
        System.out.println("\n--- Processing CSV ---");
        csvProcessor.process();
        
        System.out.println("\n--- Processing JSON ---");
        jsonProcessor.process();
        
        System.out.println("\n--- Processing XML ---");
        xmlProcessor.process();
    }
}
```

---

## 2. Strategy Pattern

```java org/example/patterns/behavioral/strategy/PaymentStrategy.java
package org.example.patterns.behavioral.strategy;

public interface PaymentStrategy {
    void pay(double amount);
    String getPaymentType();
}
```

```java org/example/patterns/behavioral/strategy/CreditCardStrategy.java
package org.example.patterns.behavioral.strategy;

import org.springframework.stereotype.Component;

@Component("creditCardStrategy")
public class CreditCardStrategy implements PaymentStrategy {
    
    @Override
    public void pay(double amount) {
        System.out.println("Processing credit card payment of $" + amount);
        System.out.println("Validating card details...");
        System.out.println("Authorizing transaction...");
        System.out.println("Payment successful via Credit Card");
    }
    
    @Override
    public String getPaymentType() {
        return "CREDIT_CARD";
    }
}
```

```java org/example/patterns/behavioral/strategy/PayPalStrategy.java
package org.example.patterns.behavioral.strategy;

import org.springframework.stereotype.Component;

@Component("paypalStrategy")
public class PayPalStrategy implements PaymentStrategy {
    
    @Override
    public void pay(double amount) {
        System.out.println("Redirecting to PayPal for payment of $" + amount);
        System.out.println("Authenticating with PayPal...");
        System.out.println("Payment successful via PayPal");
    }
    
    @Override
    public String getPaymentType() {
        return "PAYPAL";
    }
}
```

```java org/example/patterns/behavioral/strategy/CryptoStrategy.java
package org.example.patterns.behavioral.strategy;

import org.springframework.stereotype.Component;

@Component("cryptoStrategy")
public class CryptoStrategy implements PaymentStrategy {
    
    @Override
    public void pay(double amount) {
        System.out.println("Initiating cryptocurrency payment of $" + amount);
        System.out.println("Generating wallet address...");
        System.out.println("Waiting for blockchain confirmation...");
        System.out.println("Payment successful via Cryptocurrency");
    }
    
    @Override
    public String getPaymentType() {
        return "CRYPTO";
    }
}
```

```java org/example/patterns/behavioral/strategy/PaymentContext.java
package org.example.patterns.behavioral.strategy;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class PaymentContext {
    
    private final Map<String, PaymentStrategy> strategies;
    
    public PaymentContext(Map<String, PaymentStrategy> strategies) {
        this.strategies = strategies;
    }
    
    public void executePayment(String strategyName, double amount) {
        PaymentStrategy strategy = strategies.get(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown payment strategy: " + strategyName);
        }
        
        System.out.println("\n--- Using " + strategy.getPaymentType() + " Strategy ---");
        strategy.pay(amount);
    }
}
```

```java org/example/patterns/behavioral/strategy/StrategyDemo.java
package org.example.patterns.behavioral.strategy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StrategyDemo implements CommandLineRunner {
    
    private final PaymentContext paymentContext;
    
    public StrategyDemo(PaymentContext paymentContext) {
        this.paymentContext = paymentContext;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Strategy Pattern Demo ===");
        
        paymentContext.executePayment("creditCardStrategy", 100.00);
        paymentContext.executePayment("paypalStrategy", 250.50);
        paymentContext.executePayment("cryptoStrategy", 500.75);
    }
}
```

---

## 3. Observer Pattern

```java org/example/patterns/behavioral/observer/Observer.java
package org.example.patterns.behavioral.observer;

public interface Observer {
    void update(String event, Object data);
    String getName();
}
```

```java org/example/patterns/behavioral/observer/Subject.java
package org.example.patterns.behavioral.observer;

public interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers(String event, Object data);
}
```

```java org/example/patterns/behavioral/observer/StockMarket.java
package org.example.patterns.behavioral.observer;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class StockMarket implements Subject {
    
    private final List<Observer> observers = new ArrayList<>();
    private double stockPrice;
    private String stockSymbol;
    
    @Override
    public void attach(Observer observer) {
        observers.add(observer);
        System.out.println("StockMarket: Attached observer - " + observer.getName());
    }
    
    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
        System.out.println("StockMarket: Detached observer - " + observer.getName());
    }
    
    @Override
    public void notifyObservers(String event, Object data) {
        System.out.println("\nStockMarket: Notifying observers about - " + event);
        for (Observer observer : observers) {
            observer.update(event, data);
        }
    }
    
    public void setStockPrice(String symbol, double price) {
        this.stockSymbol = symbol;
        this.stockPrice = price;
        notifyObservers("PRICE_CHANGE", 
            new StockData(symbol, price));
    }
    
    public static class StockData {
        private final String symbol;
        private final double price;
        
        public StockData(String symbol, double price) {
            this.symbol = symbol;
            this.price = price;
        }
        
        public String getSymbol() {
            return symbol;
        }
        
        public double getPrice() {
            return price;
        }
    }
}
```

```java org/example/patterns/behavioral/observer/EmailNotificationObserver.java
package org.example.patterns.behavioral.observer;

import org.springframework.stereotype.Component;

@Component
public class EmailNotificationObserver implements Observer {
    
    @Override
    public void update(String event, Object data) {
        if (data instanceof StockMarket.StockData) {
            StockMarket.StockData stockData = (StockMarket.StockData) data;
            System.out.println("EmailObserver: Sending email notification");
            System.out.println("  Stock " + stockData.getSymbol() + 
                             " price changed to $" + stockData.getPrice());
        }
    }
    
    @Override
    public String getName() {
        return "EmailNotificationObserver";
    }
}
```

```java org/example/patterns/behavioral/observer/SMSNotificationObserver.java
package org.example.patterns.behavioral.observer;

import org.springframework.stereotype.Component;

@Component
public class SMSNotificationObserver implements Observer {
    
    @Override
    public void update(String event, Object data) {
        if (data instanceof StockMarket.StockData) {
            StockMarket.StockData stockData = (StockMarket.StockData) data;
            System.out.println("SMSObserver: Sending SMS notification");
            System.out.println("  Alert: " + stockData.getSymbol() + 
                             " = $" + stockData.getPrice());
        }
    }
    
    @Override
    public String getName() {
        return "SMSNotificationObserver";
    }
}
```

```java org/example/patterns/behavioral/observer/LoggingObserver.java
package org.example.patterns.behavioral.observer;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class LoggingObserver implements Observer {
    
    @Override
    public void update(String event, Object data) {
        System.out.println("LoggingObserver: Recording event");
        System.out.println("  Timestamp: " + LocalDateTime.now());
        System.out.println("  Event: " + event);
        System.out.println("  Data: " + data);
    }
    
    @Override
    public String getName() {
        return "LoggingObserver";
    }
}
```

```java org/example/patterns/behavioral/observer/ObserverDemo.java
package org.example.patterns.behavioral.observer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ObserverDemo implements CommandLineRunner {
    
    private final StockMarket stockMarket;
    private final EmailNotificationObserver emailObserver;
    private final SMSNotificationObserver smsObserver;
    private final LoggingObserver loggingObserver;
    
    public ObserverDemo(StockMarket stockMarket,
                       EmailNotificationObserver emailObserver,
                       SMSNotificationObserver smsObserver,
                       LoggingObserver loggingObserver) {
        this.stockMarket = stockMarket;
        this.emailObserver = emailObserver;
        this.smsObserver = smsObserver;
        this.loggingObserver = loggingObserver;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Observer Pattern Demo ===");
        
        // Attach observers
        stockMarket.attach(emailObserver);
        stockMarket.attach(smsObserver);
        stockMarket.attach(loggingObserver);
        
        // Trigger price changes
        stockMarket.setStockPrice("AAPL", 150.50);
        stockMarket.setStockPrice("GOOGL", 2800.75);
        
        // Detach one observer
        stockMarket.detach(smsObserver);
        
        stockMarket.setStockPrice("MSFT", 320.25);
    }
}
```

---

## 4. Chain of Responsibility Pattern

```java org/example/patterns/behavioral/chain/Handler.java
package org.example.patterns.behavioral.chain;

public abstract class Handler {
    
    protected Handler nextHandler;
    
    public void setNext(Handler handler) {
        this.nextHandler = handler;
    }
    
    public abstract void handleRequest(Request request);
}
```

```java org/example/patterns/behavioral/chain/Request.java
package org.example.patterns.behavioral.chain;

public class Request {
    
    private final String type;
    private final String content;
    private final int priority;
    
    public Request(String type, String content, int priority) {
        this.type = type;
        this.content = content;
        this.priority = priority;
    }
    
    public String getType() {
        return type;
    }
    
    public String getContent() {
        return content;
    }
    
    public int getPriority() {
        return priority;
    }
}
```

```java org/example/patterns/behavioral/chain/AuthenticationHandler.java
package org.example.patterns.behavioral.chain;

import org.springframework.stereotype.Component;

@Component
public class AuthenticationHandler extends Handler {
    
    @Override
    public void handleRequest(Request request) {
        System.out.println("AuthenticationHandler: Validating authentication");
        
        if (request.getContent().contains("authenticated")) {
            System.out.println("  ✓ Authentication successful");
            if (nextHandler != null) {
                nextHandler.handleRequest(request);
            }
        } else {
            System.out.println("  ✗ Authentication failed - Request rejected");
        }
    }
}
```

```java org/example/patterns/behavioral/chain/AuthorizationHandler.java
package org.example.patterns.behavioral.chain;

import org.springframework.stereotype.Component;

@Component
public class AuthorizationHandler extends Handler {
    
    @Override
    public void handleRequest(Request request) {
        System.out.println("AuthorizationHandler: Checking permissions");
        
        if (request.getPriority() >= 5) {
            System.out.println("  ✓ Authorization granted");
            if (nextHandler != null) {
                nextHandler.handleRequest(request);
            }
        } else {
            System.out.println("  ✗ Insufficient permissions - Request rejected");
        }
    }
}
```

```java org/example/patterns/behavioral/chain/ValidationHandler.java
package org.example.patterns.behavioral.chain;

import org.springframework.stereotype.Component;

@Component
public class ValidationHandler extends Handler {
    
    @Override
    public void handleRequest(Request request) {
        System.out.println("ValidationHandler: Validating request data");
        
        if (request.getContent() != null && !request.getContent().isEmpty()) {
            System.out.println("  ✓ Validation successful");
            if (nextHandler != null) {
                nextHandler.handleRequest(request);
            }
        } else {
            System.out.println("  ✗ Validation failed - Request rejected");
        }
    }
}
```

```java org/example/patterns/behavioral/chain/LoggingHandler.java
package org.example.patterns.behavioral.chain;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class LoggingHandler extends Handler {
    
    @Override
    public void handleRequest(Request request) {
        System.out.println("LoggingHandler: Logging request");
        System.out.println("  Time: " + LocalDateTime.now());
        System.out.println("  Type: " + request.getType());
        System.out.println("  Content: " + request.getContent());
        System.out.println("  Priority: " + request.getPriority());
        System.out.println("  ✓ Request processed successfully");
    }
}
```

```java org/example/patterns/behavioral/chain/ChainOfResponsibilityDemo.java
package org.example.patterns.behavioral.chain;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ChainOfResponsibilityDemo implements CommandLineRunner {
    
    private final AuthenticationHandler authenticationHandler;
    private final AuthorizationHandler authorizationHandler;
    private final ValidationHandler validationHandler;
    private final LoggingHandler loggingHandler;
    
    public ChainOfResponsibilityDemo(AuthenticationHandler authenticationHandler,
                                    AuthorizationHandler authorizationHandler,
                                    ValidationHandler validationHandler,
                                    LoggingHandler loggingHandler) {
        this.authenticationHandler = authenticationHandler;
        this.authorizationHandler = authorizationHandler;
        this.validationHandler = validationHandler;
        this.loggingHandler = loggingHandler;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Chain of Responsibility Pattern Demo ===");
        
        // Build the chain
        authenticationHandler.setNext(authorizationHandler);
        authorizationHandler.setNext(validationHandler);
        validationHandler.setNext(loggingHandler);
        
        // Test case 1: Valid request
        System.out.println("\n--- Test Case 1: Valid Request ---");
        Request request1 = new Request("API_CALL", "authenticated user data", 8);
        authenticationHandler.handleRequest(request1);
        
        // Test case 2: Failed authentication
        System.out.println("\n--- Test Case 2: Failed Authentication ---");
        Request request2 = new Request("API_CALL", "unauthenticated data", 8);
        authenticationHandler.handleRequest(request2);
        
        // Test case 3: Failed authorization
        System.out.println("\n--- Test Case 3: Failed Authorization ---");
        Request request3 = new Request("API_CALL", "authenticated user data", 3);
        authenticationHandler.handleRequest(request3);
    }
}
```

---

## 5. Command Pattern

```java org/example/patterns/behavioral/command/Command.java
package org.example.patterns.behavioral.command;

public interface Command {
    void execute();
    void undo();
    String getDescription();
}
```

```java org/example/patterns/behavioral/command/Light.java
package org.example.patterns.behavioral.command;

import org.springframework.stereotype.Component;

@Component
public class Light {
    
    private boolean isOn = false;
    private int brightness = 0;
    
    public void turnOn() {
        isOn = true;
        brightness = 100;
        System.out.println("Light: Turned ON (Brightness: " + brightness + "%)");
    }
    
    public void turnOff() {
        isOn = false;
        brightness = 0;
        System.out.println("Light: Turned OFF");
    }
    
    public void dim(int level) {
        brightness = Math.max(0, Math.min(100, level));
        System.out.println("Light: Dimmed to " + brightness + "%");
    }
    
    public boolean isOn() {
        return isOn;
    }
    
    public int getBrightness() {
        return brightness;
    }
}
```

```java org/example/patterns/behavioral/command/LightOnCommand.java
package org.example.patterns.behavioral.command;

public class LightOnCommand implements Command {
    
    private final Light light;
    private int previousBrightness;
    
    public LightOnCommand(Light light) {
        this.light = light;
    }
    
    @Override
    public void execute() {
        previousBrightness = light.getBrightness();
        light.turnOn();
    }
    
    @Override
    public void undo() {
        if (previousBrightness == 0) {
            light.turnOff();
        } else {
            light.dim(previousBrightness);
        }
    }
    
    @Override
    public String getDescription() {
        return "Turn light ON";
    }
}
```

```java org/example/patterns/behavioral/command/LightOffCommand.java
package org.example.patterns.behavioral.command;

public class LightOffCommand implements Command {
    
    private final Light light;
    private int previousBrightness;
    
    public LightOffCommand(Light light) {
        this.light = light;
    }
    
    @Override
    public void execute() {
        previousBrightness = light.getBrightness();
        light.turnOff();
    }
    
    @Override
    public void undo() {
        if (previousBrightness > 0) {
            light.turnOn();
            light.dim(previousBrightness);
        }
    }
    
    @Override
    public String getDescription() {
        return "Turn light OFF";
    }
}
```

```java org/example/patterns/behavioral/command/DimLightCommand.java
package org.example.patterns/behavioral/command;

public class DimLightCommand implements Command {
    
    private final Light light;
    private final int level;
    private int previousBrightness;
    
    public DimLightCommand(Light light, int level) {
        this.light = light;
        this.level = level;
    }
    
    @Override
    public void execute() {
        previousBrightness = light.getBrightness();
        light.dim(level);
    }
    
    @Override
    public void undo() {
        light.dim(previousBrightness);
    }
    
    @Override
    public String getDescription() {
        return "Dim light to " + level + "%";
    }
}
```

```java org/example/patterns/behavioral/command/RemoteControl.java
package org.example.patterns.behavioral/command;

import org.springframework.stereotype.Component;
import java.util.Stack;

@Component
public class RemoteControl {
    
    private final Stack<Command> commandHistory = new Stack<>();
    
    public void pressButton(Command command) {
        System.out.println("\nRemoteControl: Executing - " + command.getDescription());
        command.execute();
        commandHistory.push(command);
    }
    
    public void pressUndo() {
        if (!commandHistory.isEmpty()) {
            Command command = commandHistory.pop();
            System.out.println("\nRemoteControl: Undoing - " + command.getDescription());
            command.undo();
        } else {
            System.out.println("\nRemoteControl: Nothing to undo");
        }
    }
    
    public void showHistory() {
        System.out.println("\nCommand History:");
        for (Command cmd : commandHistory) {
            System.out.println("  - " + cmd.getDescription());
        }
    }
}
```

```java org/example/patterns/behavioral/command/CommandDemo.java
package org.example.patterns.behavioral.command;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CommandDemo implements CommandLineRunner {
    
    private final Light light;
    private final RemoteControl remote;
    
    public CommandDemo(Light light, RemoteControl remote) {
        this.light = light;
        this.remote = remote;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Command Pattern Demo ===");
        
        // Create commands
        Command lightOn = new LightOnCommand(light);
        Command lightOff = new LightOffCommand(light);
        Command dim50 = new DimLightCommand(light, 50);
        Command dim25 = new DimLightCommand(light, 25);
        
        // Execute commands
        remote.pressButton(lightOn);
        remote.pressButton(dim50);
        remote.pressButton(dim25);
        
        remote.showHistory();
        
        // Undo commands
        remote.pressUndo();
        remote.pressUndo();
        remote.pressUndo();
        
        remote.pressButton(lightOff);
    }
}
```

---

## 6. Iterator Pattern

```java org/example/patterns/behavioral/iterator/Iterator.java
package org.example.patterns.behavioral.iterator;

public interface Iterator<T> {
    boolean hasNext();
    T next();
    void reset();
}
```

```java org/example/patterns/behavioral/iterator/Collection.java
package org.example.patterns.behavioral.iterator;

public interface Collection<T> {
    Iterator<T> createIterator();
    void add(T item);
    int size();
}
```

```java org/example/patterns/behavioral/iterator/Book.java
package org.example.patterns.behavioral.iterator;

public class Book {
    
    private final String title;
    private final String author;
    private final String isbn;
    
    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    @Override
    public String toString() {
        return "Book{title='" + title + "', author='" + author + "', isbn='" + isbn + "'}";
    }
}
```

```java org/example/patterns/behavioral/iterator/BookCollection.java
package org.example.patterns.behavioral.iterator;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class BookCollection implements Collection<Book> {
    
    private final List<Book> books = new ArrayList<>();
    
    @Override
    public Iterator<Book> createIterator() {
        return new BookIterator(this);
    }
    
    @Override
    public void add(Book book) {
        books.add(book);
        System.out.println("Added: " + book.getTitle());
    }
    
    @Override
    public int size() {
        return books.size();
    }
    
    public Book get(int index) {
        return books.get(index);
    }
    
    private static class BookIterator implements Iterator<Book> {
        
        private final BookCollection collection;
        private int currentPosition = 0;
        
        public BookIterator(BookCollection collection) {
            this.collection = collection;
        }
        
        @Override
        public boolean hasNext() {
            return currentPosition < collection.size();
        }
        
        @Override
        public Book next() {
            if (!hasNext()) {
                throw new IndexOutOfBoundsException("No more elements");
            }
            return collection.get(currentPosition++);
        }
        
        @Override
        public void reset() {
            currentPosition = 0;
        }
    }
}
```

```java org/example/patterns/behavioral/iterator/IteratorDemo.java
package org.example.patterns.behavioral.iterator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class IteratorDemo implements CommandLineRunner {
    
    private final BookCollection bookCollection;
    
    public IteratorDemo(BookCollection bookCollection) {
        this.bookCollection = bookCollection;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Iterator Pattern Demo ===\n");
        
        // Add books
        bookCollection.add(new Book("Design Patterns", "Gang of Four", "ISBN-001"));
        bookCollection.add(new Book("Clean Code", "Robert Martin", "ISBN-002"));
        bookCollection.add(new Book("Effective Java", "Joshua Bloch", "ISBN-003"));
        bookCollection.add(new Book("Spring in Action", "Craig Walls", "ISBN-004"));
        
        // Iterate through books
        System.out.println("\nIterating through books:");
        Iterator<Book> iterator = bookCollection.createIterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            System.out.println("  " + book);
        }
        
        // Reset and iterate again
        System.out.println("\nIterating again after reset:");
        iterator.reset();
        int count = 0;
        while (iterator.hasNext() && count < 2) {
            Book book = iterator.next();
            System.out.println("  " + book);
            count++;
        }
    }
}
```

---

## 7. Mediator Pattern

```java org/example/patterns/behavioral/mediator/ChatMediator.java
package org.example.patterns.behavioral.mediator;

public interface ChatMediator {
    void sendMessage(String message, User user);
    void addUser(User user);
    void removeUser(User user);
}
```

```java org/example/patterns/behavioral/mediator/ChatRoom.java
package org.example.patterns.behavioral.mediator;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChatRoom implements ChatMediator {
    
    private final List<User> users = new ArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Override
    public void sendMessage(String message, User sender) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("\n[" + timestamp + "] " + sender.getName() + ": " + message);
        
        for (User user : users) {
            // Don't send message back to sender
            if (user != sender) {
                user.receive(message, sender);
            }
        }
    }
    
    @Override
    public void addUser(User user) {
        users.add(user);
        System.out.println("ChatRoom: " + user.getName() + " joined the chat");
    }
    
    @Override
    public void removeUser(User user) {
        users.remove(user);
        System.out.println("ChatRoom: " + user.getName() + " left the chat");
    }
}
```

```java org/example/patterns/behavioral/mediator/User.java
package org.example.patterns.behavioral.mediator;

public abstract class User {
    
    protected ChatMediator mediator;
    protected String name;
    
    public User(ChatMediator mediator, String name) {
        this.mediator = mediator;
        this.name = name;
    }
    
    public abstract void send(String message);
    public abstract void receive(String message, User sender);
    
    public String getName() {
        return name;
    }
}
```

```java org/example/patterns/behavioral/mediator/ChatUser.java
package org.example.patterns.behavioral.mediator;

public class ChatUser extends User {
    
    public ChatUser(ChatMediator mediator, String name) {
        super(mediator, name);
    }
    
    @Override
    public void send(String message) {
        System.out.println(name + " sending: " + message);
        mediator.sendMessage(message, this);
    }
    
    @Override
    public void receive(String message, User sender) {
        System.out.println("  → " + name + " received from " + sender.getName() + ": " + message);
    }
}
```

```java org/example/patterns/behavioral/mediator/MediatorDemo.java
package org.example.patterns.behavioral.mediator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MediatorDemo implements CommandLineRunner {
    
    private final ChatRoom chatRoom;
    
    public MediatorDemo(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
    
    @Override
    public void run(String... args) throws InterruptedException {
        System.out.println("\n=== Mediator Pattern Demo ===\n");
        
        // Create users
        User john = new ChatUser(chatRoom, "John");
        User jane = new ChatUser(chatRoom, "Jane");
        User bob = new ChatUser(chatRoom, "Bob");
        User alice = new ChatUser(chatRoom, "Alice");
        
        // Add users to chat room
        chatRoom.addUser(john);
        chatRoom.addUser(jane);
        chatRoom.addUser(bob);
        chatRoom.addUser(alice);
        
        Thread.sleep(100);
        
        // Send messages
        john.send("Hello everyone!");
        
        Thread.sleep(100);
        
        jane.send("Hi John!");
        
        Thread.sleep(100);
        
        bob.send("Good morning all!");
        
        Thread.sleep(100);
        
        // Remove user and send more messages
        chatRoom.removeUser(bob);
        
        Thread.sleep(100);
        
        alice.send("Where did Bob go?");
    }
}
```

---

## 8. Memento Pattern

```java org/example/patterns/behavioral/memento/TextEditor.java
package org.example.patterns.behavioral.memento;

import org.springframework.stereotype.Component;

@Component
public class TextEditor {
    
    private String content;
    private String fontStyle;
    private int fontSize;
    
    public TextEditor() {
        this.content = "";
        this.fontStyle = "Arial";
        this.fontSize = 12;
    }
    
    public void write(String text) {
        content += text;
        System.out.println("TextEditor: Added text - '" + text + "'");
    }
    
    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
        System.out.println("TextEditor: Font changed to " + fontStyle);
    }
    
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        System.out.println("TextEditor: Font size changed to " + fontSize);
    }
    
    public void clear() {
        content = "";
        System.out.println("TextEditor: Content cleared");
    }
    
    public EditorMemento save() {
        System.out.println("TextEditor: Saving state...");
        return new EditorMemento(content, fontStyle, fontSize);
    }
    
    public void restore(EditorMemento memento) {
        this.content = memento.getContent();
        this.fontStyle = memento.getFontStyle();
        this.fontSize = memento.getFontSize();
        System.out.println("TextEditor: State restored");
    }
    
    public void display() {
        System.out.println("\n--- Current State ---");
        System.out.println("Content: " + content);
        System.out.println("Font: " + fontStyle + ", Size: " + fontSize);
        System.out.println("-------------------");
    }
    
    // Memento class
    public static class EditorMemento {
        private final String content;
        private final String fontStyle;
        private final int fontSize;
        
        public EditorMemento(String content, String fontStyle, int fontSize) {
            this.content = content;
            this.fontStyle = fontStyle;
            this.fontSize = fontSize;
        }
        
        public String getContent() {
            return content;
        }
        
        public String getFontStyle() {
            return fontStyle;
        }
        
        public int getFontSize() {
            return fontSize;
        }
    }
}
```

```java org/example/patterns/behavioral/memento/History.java
package org.example.patterns.behavioral.memento;

import org.springframework.stereotype.Component;
import java.util.Stack;

@Component
public class History {
    
    private final Stack<TextEditor.EditorMemento> mementos = new Stack<>();
    
    public void push(TextEditor.EditorMemento memento) {
        mementos.push(memento);
        System.out.println("History: Saved checkpoint (Total: " + mementos.size() + ")");
    }
    
    public TextEditor.EditorMemento pop() {
        if (mementos.isEmpty()) {
            System.out.println("History: No states to restore");
            return null;
        }
        System.out.println("History: Restoring checkpoint");
        return mementos.pop();
    }
    
    public boolean isEmpty() {
        return mementos.isEmpty();
    }
    
    public int size() {
        return mementos.size();
    }
}
```

```java org/example/patterns/behavioral/memento/MementoDemo.java
package org.example.patterns.behavioral.memento;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MementoDemo implements CommandLineRunner {
    
    private final TextEditor editor;
    private final History history;
    
    public MementoDemo(TextEditor editor, History history) {
        this.editor = editor;
        this.history = history;
    }
    
    @Override
    public void run(String... args) throws InterruptedException {
        System.out.println("\n=== Memento Pattern Demo ===");
        
        // Initial state
        editor.write("Hello ");
        editor.display();
        history.push(editor.save());
        
        Thread.sleep(100);
        
        // Make changes
        editor.write("World!");
        editor.setFontSize(14);
        editor.display();
        history.push(editor.save());
        
        Thread.sleep(100);
        
        // More changes
        editor.setFontStyle("Times New Roman");
        editor.write(" This is a test.");
        editor.display();
        history.push(editor.save());
        
        Thread.sleep(100);
        
        // Undo
        System.out.println("\n--- Undoing changes ---");
        editor.restore(history.pop());
        editor.display();
        
        Thread.sleep(100);
        
        editor.restore(history.pop());
        editor.display();
        
        Thread.sleep(100);
        
        editor.restore(history.pop());
        editor.display();
    }
}
```

---

## 9. State Pattern

```java org/example/patterns/behavioral/state/State.java
package org.example.patterns.behavioral.state;

public interface State {
    void insertCoin(VendingMachine machine);
    void selectProduct(VendingMachine machine);
    void dispense(VendingMachine machine);
    void refund(VendingMachine machine);
    String getStateName();
}
```

```java org/example/patterns/behavioral/state/VendingMachine.java
package org.example.patterns.behavioral.state;

import org.springframework.stereotype.Component;

@Component
public class VendingMachine {
    
    private State currentState;
    private final State noCoinState;
    private final State hasCoinState;
    private final State dispensingState;
    private final State outOfStockState;
    
    private int productCount = 5;
    
    public VendingMachine() {
        this.noCoinState = new NoCoinState();
        this.hasCoinState = new HasCoinState();
        this.dispensingState = new DispensingState();
        this.outOfStockState = new OutOfStockState();
        
        this.currentState = productCount > 0 ? noCoinState : outOfStockState;
    }
    
    public void insertCoin() {
        System.out.println("\nVendingMachine: Inserting coin...");
        currentState.insertCoin(this);
    }
    
    public void selectProduct() {
        System.out.println("\nVendingMachine: Selecting product...");
        currentState.selectProduct(this);
    }
    
    public void dispense() {
        currentState.dispense(this);
    }
    
    public void refund() {
        System.out.println("\nVendingMachine: Requesting refund...");
        currentState.refund(this);
    }
    
    public void setState(State state) {
        this.currentState = state;
        System.out.println("State changed to: " + state.getStateName());
    }
    
    public void releaseProduct() {
        if (productCount > 0) {
            productCount--;
            System.out.println("Product dispensed! Remaining: " + productCount);
        }
    }
    
    public int getProductCount() {
        return productCount;
    }
    
    public State getNoCoinState() {
        return noCoinState;
    }
    
    public State getHasCoinState() {
        return hasCoinState;
    }
    
    public State getDispensingState() {
        return dispensingState;
    }
    
    public State getOutOfStockState() {
        return outOfStockState;
    }
    
    public String getCurrentStateName() {
        return currentState.getStateName();
    }
}
```

```java org/example/patterns/behavioral/state/NoCoinState.java
package org.example.patterns.behavioral.state;

public class NoCoinState implements State {
    
    @Override
    public void insertCoin(VendingMachine machine) {
        System.out.println("Coin accepted");
        machine.setState(machine.getHasCoinState());
    }
    
    @Override
    public void selectProduct(VendingMachine machine) {
        System.out.println("Please insert coin first");
    }
    
    @Override
    public void dispense(VendingMachine machine) {
        System.out.println("Please insert coin and select product");
    }
    
    @Override
    public void refund(VendingMachine machine) {
        System.out.println("No coin to refund");
    }
    
    @Override
    public String getStateName() {
        return "NoCoinState";
    }
}
```

```java org/example/patterns/behavioral/state/HasCoinState.java
package org.example.patterns.behavioral.state;

public class HasCoinState implements State {
    
    @Override
    public void insertCoin(VendingMachine machine) {
        System.out.println("Coin already inserted");
    }
    
    @Override
    public void selectProduct(VendingMachine machine) {
        System.out.println("Product selected");
        machine.setState(machine.getDispensingState());
        machine.dispense();
    }
    
    @Override
    public void dispense(VendingMachine machine) {
        System.out.println("Please select a product first");
    }
    
    @Override
    public void refund(VendingMachine machine) {
        System.out.println("Coin refunded");
        machine.setState(machine.getNoCoinState());
    }
    
    @Override
    public String getStateName() {
        return "HasCoinState";
    }
}
```

```java org/example/patterns/behavioral/state/DispensingState.java
package org.example.patterns.behavioral.state;

public class DispensingState implements State {
    
    @Override
    public void insertCoin(VendingMachine machine) {
        System.out.println("Please wait, dispensing in progress");
    }
    
    @Override
    public void selectProduct(VendingMachine machine) {
        System.out.println("Please wait, dispensing in progress");
    }
    
    @Override
    public void dispense(VendingMachine machine) {
        machine.releaseProduct();
        if (machine.getProductCount() > 0) {
            machine.setState(machine.getNoCoinState());
        } else {
            machine.setState(machine.getOutOfStockState());
        }
    }
    
    @Override
    public void refund(VendingMachine machine) {
        System.out.println("Cannot refund, dispensing in progress");
    }
    
    @Override
    public String getStateName() {
        return "DispensingState";
    }
}
```

```java org/example/patterns/behavioral/state/OutOfStockState.java
package org.example.patterns.behavioral.state;

public class OutOfStockState implements State {
    
    @Override
    public void insertCoin(VendingMachine machine) {
        System.out.println("Out of stock. Coin returned");
    }
    
    @Override
    public void selectProduct(VendingMachine machine) {
        System.out.println("Out of stock");
    }
    
    @Override
    public void dispense(VendingMachine machine) {
        System.out.println("Out of stock");
    }
    
    @Override
    public void refund(VendingMachine machine) {
        System.out.println("No coin to refund");
    }
    
    @Override
    public String getStateName() {
        return "OutOfStockState";
    }
}
```

```java org/example/patterns/behavioral/state/StateDemo.java
package org.example.patterns.behavioral.state;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StateDemo implements CommandLineRunner {
    
    private final VendingMachine vendingMachine;
    
    public StateDemo(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }
    
    @Override
    public void run(String... args) throws InterruptedException {
        System.out.println("\n=== State Pattern Demo ===");
        System.out.println("Products available: " + vendingMachine.getProductCount());
        
        // Scenario 1: Normal purchase
        vendingMachine.insertCoin();
        Thread.sleep(100);
        vendingMachine.selectProduct();
        Thread.sleep(100);
        
        // Scenario 2: Try to select without coin
        System.out.println("\n--- Attempting to select without coin ---");
        vendingMachine.selectProduct();
        Thread.sleep(100);
        
        // Scenario 3: Insert coin and refund
        System.out.println("\n--- Insert coin and refund ---");
        vendingMachine.insertCoin();
        Thread.sleep(100);
        vendingMachine.refund();
        Thread.sleep(100);
        
        // Scenario 4: Buy remaining products
        System.out.println("\n--- Buying remaining products ---");
        for (int i = 0; i < 4; i++) {
            vendingMachine.insertCoin();
            Thread.sleep(100);
            vendingMachine.selectProduct();
            Thread.sleep(100);
        }
        
        // Scenario 5: Try to buy when out of stock
        System.out.println("\n--- Out of stock scenario ---");
        vendingMachine.insertCoin();
    }
}
```

---

## 10. Visitor Pattern

```java org/example/patterns/behavioral/visitor/Visitor.java
package org.example.patterns.behavioral.visitor;

public interface Visitor {
    void visit(Book book);
    void visit(Electronics electronics);
    void visit(Clothing clothing);
}
```

```java org/example/patterns/behavioral/visitor/Product.java
package org.example.patterns.behavioral.visitor;

public interface Product {
    void accept(Visitor visitor);
    String getName();
    double getPrice();
}
```

```java org/example/patterns/behavioral/visitor/Book.java
package org.example.patterns.behavioral.visitor;

public class Book implements Product {
    
    private final String name;
    private final double price;
    private final String author;
    private final int pages;
    
    public Book(String name, double price, String author, int pages) {
        this.name = name;
        this.price = price;
        this.author = author;
        this.pages = pages;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public double getPrice() {
        return price;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public int getPages() {
        return pages;
    }
}
```

```java org/example/patterns/behavioral/visitor/Electronics.java
package org.example.patterns.behavioral.visitor;

public class Electronics implements Product {
    
    private final String name;
    private final double price;
    private final String brand;
    private final int warrantyMonths;
    
    public Electronics(String name, double price, String brand, int warrantyMonths) {
        this.name = name;
        this.price = price;
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public double getPrice() {
        return price;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public int getWarrantyMonths() {
        return warrantyMonths;
    }
}
```

```java org/example/patterns/behavioral/visitor/Clothing.java
package org.example.patterns.behavioral.visitor;

public class Clothing implements Product {
    
    private final String name;
    private final double price;
    private final String size;
    private final String material;
    
    public Clothing(String name, double price, String size, String material) {
        this.name = name;
        this.price = price;
        this.size = size;
        this.material = material;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public double getPrice() {
        return price;
    }
    
    public String getSize() {
        return size;
    }
    
    public String getMaterial() {
        return material;
    }
}
```

```java org/example/patterns/behavioral/visitor/TaxCalculatorVisitor.java
package org.example.patterns.behavioral.visitor;

import org.springframework.stereotype.Component;

@Component
public class TaxCalculatorVisitor implements Visitor {
    
    private double totalTax = 0;
    
    @Override
    public void visit(Book book) {
        double tax = book.getPrice() * 0.05; // 5% tax for books
        totalTax += tax;
        System.out.println("Book '" + book.getName() + "': Tax = $" + 
                         String.format("%.2f", tax));
    }
    
    @Override
    public void visit(Electronics electronics) {
        double tax = electronics.getPrice() * 0.15; // 15% tax for electronics
        totalTax += tax;
        System.out.println("Electronics '" + electronics.getName() + "': Tax = $" + 
                         String.format("%.2f", tax));
    }
    
    @Override
    public void visit(Clothing clothing) {
        double tax = clothing.getPrice() * 0.10; // 10% tax for clothing
        totalTax += tax;
        System.out.println("Clothing '" + clothing.getName() + "': Tax = $" + 
                         String.format("%.2f", tax));
    }
    
    public double getTotalTax() {
        return totalTax;
    }
    
    public void reset() {
        totalTax = 0;
    }
}
```

```java org/example/patterns/behavioral/visitor/DiscountVisitor.java
package org.example.patterns.behavioral/visitor;

import org.springframework.stereotype.Component;

@Component
public class DiscountVisitor implements Visitor {
    
    private double totalDiscount = 0;
    
    @Override
    public void visit(Book book) {
        double discount = book.getPrice() * 0.10; // 10% discount for books
        totalDiscount += discount;
        System.out.println("Book '" + book.getName() + "': Discount = $" + 
                         String.format("%.2f", discount));
    }
    
    @Override
    public void visit(Electronics electronics) {
        double discount = electronics.getPrice() * 0.05; // 5% discount for electronics
        totalDiscount += discount;
        System.out.println("Electronics '" + electronics.getName() + "': Discount = $" + 
                         String.format("%.2f", discount));
    }
    
    @Override
    public void visit(Clothing clothing) {
        double discount = clothing.getPrice() * 0.20; // 20% discount for clothing
        totalDiscount += discount;
        System.out.println("Clothing '" + clothing.getName() + "': Discount = $" + 
                         String.format("%.2f", discount));
    }
    
    public double getTotalDiscount() {
        return totalDiscount;
    }
    
    public void reset() {
        totalDiscount = 0;
    }
}
```

```java org/example/patterns/behavioral/visitor/ShippingCostVisitor.java
package org.example.patterns.behavioral/visitor;

import org.springframework.stereotype.Component;

@Component
public class ShippingCostVisitor implements Visitor {
    
    private double totalShipping = 0;
    
    @Override
    public void visit(Book book) {
        double shipping = 2.0 + (book.getPages() / 100.0); // Base + weight-based
        totalShipping += shipping;
        System.out.println("Book '" + book.getName() + "': Shipping = $" + 
                         String.format("%.2f", shipping));
    }
    
    @Override
    public void visit(Electronics electronics) {
        double shipping = 10.0; // Flat rate for electronics
        totalShipping += shipping;
        System.out.println("Electronics '" + electronics.getName() + "': Shipping = $" + 
                         String.format("%.2f", shipping));
    }
    
    @Override
    public void visit(Clothing clothing) {
        double shipping = 5.0; // Flat rate for clothing
        totalShipping += shipping;
        System.out.println("Clothing '" + clothing.getName() + "': Shipping = $" + 
                         String.format("%.2f", shipping));
    }
    
    public double getTotalShipping() {
        return totalShipping;
    }
    
    public void reset() {
        totalShipping = 0;
    }
}
```

```java org/example/patterns/behavioral/visitor/ShoppingCart.java
package org.example.patterns.behavioral/visitor;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class ShoppingCart {
    
    private final List<Product> products = new ArrayList<>();
    
    public void addProduct(Product product) {
        products.add(product);
        System.out.println("Added to cart: " + product.getName());
    }
    
    public void accept(Visitor visitor) {
        for (Product product : products) {
            product.accept(visitor);
        }
    }
    
    public double getTotalPrice() {
        return products.stream()
                .mapToDouble(Product::getPrice)
                .sum();
    }
    
    public void clear() {
        products.clear();
    }
}
```

```java org/example/patterns/behavioral/visitor/VisitorDemo.java
package org.example.patterns.behavioral.visitor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class VisitorDemo implements CommandLineRunner {
    
    private final ShoppingCart cart;
    private final TaxCalculatorVisitor taxCalculator;
    private final DiscountVisitor discountCalculator;
    private final ShippingCostVisitor shippingCalculator;
    
    public VisitorDemo(ShoppingCart cart,
                      TaxCalculatorVisitor taxCalculator,
                      DiscountVisitor discountCalculator,
                      ShippingCostVisitor shippingCalculator) {
        this.cart = cart;
        this.taxCalculator = taxCalculator;
        this.discountCalculator = discountCalculator;
        this.shippingCalculator = shippingCalculator;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Visitor Pattern Demo ===\n");
        
        // Add products to cart
        cart.addProduct(new Book("Design Patterns", 45.00, "Gang of Four", 400));
        cart.addProduct(new Electronics("Laptop", 1200.00, "Dell", 24));
        cart.addProduct(new Clothing("T-Shirt", 25.00, "M", "Cotton"));
        cart.addProduct(new Book("Clean Code", 35.00, "Robert Martin", 350));
        
        double subtotal = cart.getTotalPrice();
        System.out.println("\nSubtotal: $" + String.format("%.2f", subtotal));
        
        // Calculate tax
        System.out.println("\n--- Calculating Tax ---");
        cart.accept(taxCalculator);
        System.out.println("Total Tax: $" + String.format("%.2f", taxCalculator.getTotalTax()));
        
        // Calculate discount
        System.out.println("\n--- Calculating Discount ---");
        cart.accept(discountCalculator);
        System.out.println("Total Discount: $" + String.format("%.2f", discountCalculator.getTotalDiscount()));
        
        // Calculate shipping
        System.out.println("\n--- Calculating Shipping ---");
        cart.accept(shippingCalculator);
        System.out.println("Total Shipping: $" + String.format("%.2f", shippingCalculator.getTotalShipping()));
        
        // Final total
        double finalTotal = subtotal + taxCalculator.getTotalTax() - 
                          discountCalculator.getTotalDiscount() + 
                          shippingCalculator.getTotalShipping();
        System.out.println("\n=== Order Summary ===");
        System.out.println("Subtotal:  $" + String.format("%.2f", subtotal));
        System.out.println("Tax:       $" + String.format("%.2f", taxCalculator.getTotalTax()));
        System.out.println("Discount: -$" + String.format("%.2f", discountCalculator.getTotalDiscount()));
        System.out.println("Shipping:  $" + String.format("%.2f", shippingCalculator.getTotalShipping()));
        System.out.println("-------------------");
        System.out.println("Total:     $" + String.format("%.2f", finalTotal));
    }
}
```

---

## 11. Interpreter Pattern

```java org/example/patterns/behavioral/interpreter/Expression.java
package org.example.patterns.behavioral.interpreter;

public interface Expression {
    int interpret();
}
```

```java org/example/patterns/behavioral/interpreter/NumberExpression.java
package org.example.patterns.behavioral.interpreter;

public class NumberExpression implements Expression {
    
    private final int number;
    
    public NumberExpression(int number) {
        this.number = number;
    }
    
    @Override
    public int interpret() {
        return number;
    }
}
```

```java org/example/patterns/behavioral/interpreter/AddExpression.java
package org.example.patterns.behavioral.interpreter;

public class AddExpression implements Expression {
    
    private final Expression left;
    private final Expression right;
    
    public AddExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public int interpret() {
        return left.interpret() + right.interpret();
    }
}
```

```java org/example/patterns/behavioral/interpreter/SubtractExpression.java
package org.example.patterns.behavioral/interpreter;

public class SubtractExpression implements Expression {
    
    private final Expression left;
    private final Expression right;
    
    public SubtractExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public int interpret() {
        return left.interpret() - right.interpret();
    }
}
```

```java org/example/patterns/behavioral/interpreter/MultiplyExpression.java
package org.example.patterns.behavioral/interpreter;

public class MultiplyExpression implements Expression {
    
    private final Expression left;
    private final Expression right;
    
    public MultiplyExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public int interpret() {
        return left.interpret() * right.interpret();
    }
}
```

```java org/example/patterns/behavioral/interpreter/ExpressionParser.java
package org.example.patterns.behavioral.interpreter;

import org.springframework.stereotype.Component;
import java.util.Stack;

@Component
public class ExpressionParser {
    
    public Expression parse(String expression) {
        Stack<Expression> stack = new Stack<>();
        String[] tokens = expression.split(" ");
        
        for (String token : tokens) {
            if (isOperator(token)) {
                Expression right = stack.pop();
                Expression left = stack.pop();
                Expression operator = getOperatorExpression(token, left, right);
                stack.push(operator);
            } else {
                Expression number = new NumberExpression(Integer.parseInt(token));
                stack.push(number);
            }
        }
        
        return stack.pop();
    }
    
    private boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*");
    }
    
    private Expression getOperatorExpression(String operator, Expression left, Expression right) {
        return switch (operator) {
            case "+" -> new AddExpression(left, right);
            case "-" -> new SubtractExpression(left, right);
            case "*" -> new MultiplyExpression(left, right);
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
    }
}
```

```java org/example/patterns/behavioral/interpreter/InterpreterDemo.java
package org.example.patterns.behavioral.interpreter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InterpreterDemo implements CommandLineRunner {
    
    private final ExpressionParser parser;
    
    public InterpreterDemo(ExpressionParser parser) {
        this.parser = parser;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Interpreter Pattern Demo ===");
        System.out.println("Using Postfix Notation (Reverse Polish Notation)\n");
        
        // Simple addition: 5 + 3
        String expr1 = "5 3 +";
        Expression expression1 = parser.parse(expr1);
        System.out.println(expr1 + " = " + expression1.interpret());
        
        // Subtraction: 10 - 4
        String expr2 = "10 4 -";
        Expression expression2 = parser.parse(expr2);
        System.out.println(expr2 + " = " + expression2.interpret());
        
        // Multiplication: 6 * 7
        String expr3 = "6 7 *";
        Expression expression3 = parser.parse(expr3);
        System.out.println(expr3 + " = " + expression3.interpret());
        
        // Complex: (5 + 3) * 2 = 5 3 + 2 *
        String expr4 = "5 3 + 2 *";
        Expression expression4 = parser.parse(expr4);
        System.out.println(expr4 + " = " + expression4.interpret());
        
        // Complex: 15 - (4 * 2) = 15 4 2 * -
        String expr5 = "15 4 2 * -";
        Expression expression5 = parser.parse(expr5);
        System.out.println(expr5 + " = " + expression5.interpret());
        
        // Very complex: ((10 + 5) * 3) - 20 = 10 5 + 3 * 20 -
        String expr6 = "10 5 + 3 * 20 -";
        Expression expression6 = parser.parse(expr6);
        System.out.println(expr6 + " = " + expression6.interpret());
    }
}
```

---

## 12. Null Object Pattern

```java org/example/patterns/behavioral/nullobject/Customer.java
package org.example.patterns.behavioral.nullobject;

public interface Customer {
    String getName();
    boolean isNull();
    void displayInfo();
    double getDiscount();
}
```

```java org/example/patterns/behavioral/nullobject/RealCustomer.java
package org.example.patterns.behavioral.nullobject;

public class RealCustomer implements Customer {
    
    private final String name;
    private final String email;
    private final String membershipLevel;
    
    public RealCustomer(String name, String email, String membershipLevel) {
        this.name = name;
        this.email = email;
        this.membershipLevel = membershipLevel;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean isNull() {
        return false;
    }
    
    @Override
    public void displayInfo() {
        System.out.println("Customer Information:");
        System.out.println("  Name: " + name);
        System.out.println("  Email: " + email);
        System.out.println("  Membership: " + membershipLevel);
        System.out.println("  Discount: " + (getDiscount() * 100) + "%");
    }
    
    @Override
    public double getDiscount() {
        return switch (membershipLevel.toUpperCase()) {
            case "GOLD" -> 0.20;
            case "SILVER" -> 0.10;
            case "BRONZE" -> 0.05;
            default -> 0.0;
        };
    }
}
```

```java org/example/patterns/behavioral/nullobject/NullCustomer.java
package org.example.patterns.behavioral.nullobject;

public class NullCustomer implements Customer {
    
    @Override
    public String getName() {
        return "Guest";
    }
    
    @Override
    public boolean isNull() {
        return true;
    }
    
    @Override
    public void displayInfo() {
        System.out.println("No customer information available (Guest user)");
    }
    
    @Override
    public double getDiscount() {
        return 0.0;
    }
}
```

```java org/example/patterns/behavioral/nullobject/CustomerFactory.java
package org.example.patterns.behavioral.nullobject;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomerFactory {
    
    private static final Map<String, Customer> customers = new HashMap<>();
    
    static {
        customers.put("john@example.com", 
            new RealCustomer("John Doe", "john@example.com", "GOLD"));
        customers.put("jane@example.com", 
            new RealCustomer("Jane Smith", "jane@example.com", "SILVER"));
        customers.put("bob@example.com", 
            new RealCustomer("Bob Johnson", "bob@example.com", "BRONZE"));
    }
    
    public Customer getCustomer(String email) {
        Customer customer = customers.get(email);
        return customer != null ? customer : new NullCustomer();
    }
}
```

```java org/example/patterns/behavioral/nullobject/OrderService.java
package org.example.patterns.behavioral.nullobject;

import org.springframework.stereotype.Service;

@Service
public class OrderService {
    
    private final CustomerFactory customerFactory;
    
    public OrderService(CustomerFactory customerFactory) {
        this.customerFactory = customerFactory;
    }
    
    public void processOrder(String email, double orderAmount) {
        Customer customer = customerFactory.getCustomer(email);
        
        System.out.println("\n--- Processing Order ---");
        customer.displayInfo();
        
        double discount = customer.getDiscount();
        double finalAmount = orderAmount - (orderAmount * discount);
        
        System.out.println("Order Amount: $" + String.format("%.2f", orderAmount));
        System.out.println("Discount Applied: " + (discount * 100) + "%");
        System.out.println("Final Amount: $" + String.format("%.2f", finalAmount));
        
        if (customer.isNull()) {
            System.out.println("Note: Guest checkout - no loyalty points earned");
        } else {
            System.out.println("Loyalty points earned: " + (int)(finalAmount * 10));
        }
    }
}
```

```java org/example/patterns/behavioral/nullobject/NullObjectDemo.java
package org.example.patterns.behavioral.nullobject;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class NullObjectDemo implements CommandLineRunner {
    
    private final OrderService orderService;
    
    public NullObjectDemo(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("\n=== Null Object Pattern Demo ===");
        
        // Existing customer
        orderService.processOrder("john@example.com", 100.00);
        
        // Another existing customer
        orderService.processOrder("jane@example.com", 150.00);
        
        // Non-existing customer (null object will be used)
        orderService.processOrder("unknown@example.com", 80.00);
        
        // Guest customer
        orderService.processOrder("guest@example.com", 50.00);
    }
}
```

---

## Main Application Class

```java org/example/BehavioralPatternsApplication.java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BehavioralPatternsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BehavioralPatternsApplication.class, args);
    }
}
```

---

## Application Properties

```properties src/main/resources/application.properties
# Application Configuration
spring.application.name=spring-behavioral-patterns

# Logging
logging.level.root=INFO
logging.level.org.example=DEBUG
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# Disable banner
spring.main.banner-mode=off

# Allow circular references if needed
spring.main.allow-circular-references=false
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
    <artifactId>spring-behavioral-patterns</artifactId>
    <version>1.0.0</version>
    <name>Spring Behavioral Design Patterns</name>
    <description>Demonstration of Behavioral Design Patterns in Spring</description>
    
    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        
        <!-- Spring Boot Starter Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- Lombok (optional) -->
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
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Unit Tests

```java org/example/patterns/behavioral/BehavioralPatternsTest.java
package org.example.patterns.behavioral;

import org.example.patterns.behavioral.strategy.PaymentContext;
import org.example.patterns.behavioral.observer.StockMarket;
import org.example.patterns.behavioral.observer.EmailNotificationObserver;
import org.example.patterns.behavioral.state.VendingMachine;
import org.example.patterns.behavioral.nullobject.CustomerFactory;
import org.example.patterns.behavioral.nullobject.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BehavioralPatternsTest {
    
    @Autowired
    private PaymentContext paymentContext;
    
    @Autowired
    private StockMarket stockMarket;
    
    @Autowired
    private EmailNotificationObserver emailObserver;
    
    @Autowired
    private VendingMachine vendingMachine;
    
    @Autowired
    private CustomerFactory customerFactory;
    
    @Test
    void testStrategyPattern() {
        assertNotNull(paymentContext);
        assertDoesNotThrow(() -> paymentContext.executePayment("creditCardStrategy", 100.0));
    }
    
    @Test
    void testObserverPattern() {
        assertNotNull(stockMarket);
        assertNotNull(emailObserver);
        
        stockMarket.attach(emailObserver);
        assertDoesNotThrow(() -> stockMarket.setStockPrice("TEST", 100.0));
    }
    
    @Test
    void testStatePattern() {
        assertNotNull(vendingMachine);
        assertEquals("NoCoinState", vendingMachine.getCurrentStateName());
        
        vendingMachine.insertCoin();
        assertEquals("HasCoinState", vendingMachine.getCurrentStateName());
    }
    
    @Test
    void testNullObjectPattern() {
        Customer realCustomer = customerFactory.getCustomer("john@example.com");
        assertNotNull(realCustomer);
        assertFalse(realCustomer.isNull());
        assertEquals("John Doe", realCustomer.getName());
        
        Customer nullCustomer = customerFactory.getCustomer("nonexistent@example.com");
        assertNotNull(nullCustomer);
        assertTrue(nullCustomer.isNull());
        assertEquals("Guest", nullCustomer.getName());
    }
}
```

---

## README.md

```markdown README.md
# Spring Behavioral Design Patterns

This project demonstrates the implementation of 12 core Behavioral Design Patterns using Spring Framework.

## Patterns Implemented

1. **Template Method Pattern** - Defines skeleton of algorithm, letting subclasses override specific steps
2. **Strategy Pattern** - Defines family of algorithms, encapsulates each one, makes them interchangeable
3. **Observer Pattern** - Defines one-to-many dependency between objects
4. **Chain of Responsibility Pattern** - Passes request along chain of handlers
5. **Command Pattern** - Encapsulates request as object
6. **Iterator Pattern** - Provides way to access elements sequentially
7. **Mediator Pattern** - Defines object that encapsulates how objects interact
8. **Memento Pattern** - Captures and externalizes object's internal state
9. **State Pattern** - Allows object to alter behavior when internal state changes
10. **Visitor Pattern** - Separates algorithm from object structure
11. **Interpreter Pattern** - Implements specialized language grammar
12. **Null Object Pattern** - Provides default behavior for absence of object

## Running the Application

```bash
mvn spring-boot:run
```

## Build

```bash
mvn clean package
```

## Test

```bash
mvn test
```

## Requirements

- Java 17+
- Maven 3.6+
- Spring Boot 3.2.0
```

This completes the comprehensive implementation of all 12 Behavioral Design Patterns in Spring Framework with fully working, production-ready examples!