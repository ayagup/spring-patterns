### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 1:35:44 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **Bean Factory Patterns**
> - Factory Bean Pattern
> - Factory Method Pattern
> - Static Factory Method Pattern
> - Instance Factory Method Pattern
> - Bean Factory Aware Pattern
> - Application Context Aware Pattern
> - Bean Name Aware Pattern
> - Bean Class Loader Aware Pattern
> ```

#### _Assistant_

> I'll create a comprehensive Spring Boot project demonstrating all these Bean Factory patterns.
> 
> ## Project Structure
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
>     </parent>
> 
>     <groupId>com.example</groupId>
>     <artifactId>bean-factory-patterns</artifactId>
>     <version>1.0.0</version>
> 
>     <properties>
>         <java.version>17</java.version>
>     </properties>
> 
>     <dependencies>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
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
> ## 1. Factory Bean Pattern
> 
> ```java src/main/java/com/example/patterns/factorybean/Connection.java
> package com.example.patterns.factorybean;
> 
> import lombok.AllArgsConstructor;
> import lombok.Data;
> 
> @Data
> @AllArgsConstructor
> public class Connection {
>     private String url;
>     private String username;
>     private boolean pooled;
>     
>     public void connect() {
>         System.out.println("Connecting to " + url + " as " + username + 
>                          " (pooled: " + pooled + ")");
>     }
> }
> ```
> 
> ```java src/main/java/com/example/patterns/factorybean/ConnectionFactoryBean.java
> package com.example.patterns.factorybean;
> 
> import org.springframework.beans.factory.FactoryBean;
> import org.springframework.stereotype.Component;
> 
> @Component("connectionFactory")
> public class ConnectionFactoryBean implements FactoryBean<Connection> {
>     
>     @Override
>     public Connection getObject() throws Exception {
>         // Complex object creation logic
>         System.out.println("FactoryBean: Creating Connection object");
>         return new Connection("jdbc:mysql://localhost:3306/db", "admin", true);
>     }
> 
>     @Override
>     public Class<?> getObjectType() {
>         return Connection.class;
>     }
> 
>     @Override
>     public boolean isSingleton() {
>         return true;
>     }
> }
> ```
> 
> ## 2. Factory Method Pattern
> 
> ```java src/main/java/com/example/patterns/factorymethod/Vehicle.java
> package com.example.patterns.factorymethod;
> 
> public interface Vehicle {
>     void drive();
>     String getType();
> }
> ```
> 
> ```java src/main/java/com/example/patterns/factorymethod/Car.java
> package com.example.patterns.factorymethod;
> 
> import lombok.Data;
> 
> @Data
> public class Car implements Vehicle {
>     private String model;
>     
>     public Car(String model) {
>         this.model = model;
>     }
>     
>     @Override
>     public void drive() {
>         System.out.println("Driving car: " + model);
>     }
>     
>     @Override
>     public String getType() {
>         return "Car";
>     }
> }
> ```
> 
> ```java src/main/java/com/example/patterns/factorymethod/Bike.java
> package com.example.patterns.factorymethod;
> 
> import lombok.Data;
> 
> @Data
> public class Bike implements Vehicle {
>     private String brand;
>     
>     public Bike(String brand) {
>         this.brand = brand;
>     }
>     
>     @Override
>     public void drive() {
>         System.out.println("Riding bike: " + brand);
>     }
>     
>     @Override
>     public String getType() {
>         return "Bike";
>     }
> }
> ```
> 
> ```java src/main/java/com/example/patterns/factorymethod/VehicleFactory.java
> package com.example.patterns.factorymethod;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> 
> @Configuration
> public class VehicleFactory {
>     
>     @Bean(name = "sportsCar")
>     public Vehicle createSportsCar() {
>         System.out.println("Factory Method: Creating Sports Car");
>         return new Car("Ferrari F8");
>     }
>     
>     @Bean(name = "mountainBike")
>     public Vehicle createMountainBike() {
>         System.out.println("Factory Method: Creating Mountain Bike");
>         return new Bike("Trek X-Caliber");
>     }
> }
> ```
> 
> ## 3. Static Factory Method Pattern
> 
> ```java src/main/java/com/example/patterns/staticfactory/DataSource.java
> package com.example.patterns.staticfactory;
> 
> import lombok.Data;
> 
> @Data
> public class DataSource {
>     private String host;
>     private int port;
>     private String database;
>     
>     private DataSource(String host, int port, String database) {
>         this.host = host;
>         this.port = port;
>         this.database = database;
>     }
>     
>     // Static factory method
>     public static DataSource createMySQLDataSource() {
>         System.out.println("Static Factory: Creating MySQL DataSource");
>         return new DataSource("localhost", 3306, "mysql_db");
>     }
>     
>     public static DataSource createPostgreSQLDataSource() {
>         System.out.println("Static Factory: Creating PostgreSQL DataSource");
>         return new DataSource("localhost", 5432, "postgres_db");
>     }
>     
>     public void printInfo() {
>         System.out.println("DataSource: " + database + " at " + host + ":" + port);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/patterns/staticfactory/StaticFactoryConfig.java
> package com.example.patterns.staticfactory;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> 
> @Configuration
> public class StaticFactoryConfig {
>     
>     @Bean(name = "mysqlDataSource")
>     public static DataSource mysqlDataSource() {
>         return DataSource.createMySQLDataSource();
>     }
>     
>     @Bean(name = "postgresDataSource")
>     public static DataSource postgresDataSource() {
>         return DataSource.createPostgreSQLDataSource();
>     }
> }
> ```
> 
> ## 4. Instance Factory Method Pattern
> 
> ```java src/main/java/com/example/patterns/instancefactory/MessageService.java
> package com.example.patterns.instancefactory;
> 
> import lombok.Data;
> 
> @Data
> public class MessageService {
>     private String protocol;
>     private int timeout;
>     
>     public MessageService(String protocol, int timeout) {
>         this.protocol = protocol;
>         this.timeout = timeout;
>     }
>     
>     public void sendMessage(String message) {
>         System.out.println("Sending via " + protocol + ": " + message + 
>                          " (timeout: " + timeout + "ms)");
>     }
> }
> ```
> 
> ```java src/main/java/com/example/patterns/instancefactory/MessageServiceFactory.java
> package com.example.patterns.instancefactory;
> 
> import org.springframework.stereotype.Component;
> 
> @Component
> public class MessageServiceFactory {
>     
>     private int requestCount = 0;
>     
>     public MessageService createEmailService() {
>         requestCount++;
>         System.out.println("Instance Factory: Creating Email Service (#" + requestCount + ")");
>         return new MessageService("SMTP", 5000);
>     }
>     
>     public MessageService createSmsService() {
>         requestCount++;
>         System.out.println("Instance Factory: Creating SMS Service (#" + requestCount + ")");
>         return new MessageService("SMS", 3000);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/patterns/instancefactory/InstanceFactoryConfig.java
> package com.example.patterns.instancefactory;
> 
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> 
> @Configuration
> public class InstanceFactoryConfig {
>     
>     @Autowired
>     private MessageServiceFactory factory;
>     
>     @Bean(name = "emailService")
>     public MessageService emailService() {
>         return factory.createEmailService();
>     }
>     
>     @Bean(name = "smsService")
>     public MessageService smsService() {
>         return factory.createSmsService();
>     }
> }
> ```
> 
> ## 5. Bean Factory Aware Pattern
> 
> ```java src/main/java/com/example/patterns/aware/BeanFactoryAwareExample.java
> package com.example.patterns.aware;
> 
> import org.springframework.beans.BeansException;
> import org.springframework.beans.factory.BeanFactory;
> import org.springframework.beans.factory.BeanFactoryAware;
> import org.springframework.stereotype.Component;
> 
> @Component
> public class BeanFactoryAwareExample implements BeanFactoryAware {
>     
>     private BeanFactory beanFactory;
>     
>     @Override
>     public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
>         System.out.println("BeanFactoryAware: BeanFactory injected");
>         this.beanFactory = beanFactory;
>     }
>     
>     public void demonstrateBeanFactoryUsage() {
>         System.out.println("BeanFactoryAware: Demonstrating BeanFactory usage");
>         
>         // Access beans programmatically
>         if (beanFactory.containsBean("connectionFactory")) {
>             System.out.println("BeanFactoryAware: Found 'connectionFactory' bean");
>             Object bean = beanFactory.getBean("connectionFactory");
>             System.out.println("BeanFactoryAware: Bean type: " + bean.getClass().getName());
>         }
>         
>         // Check if bean is singleton
>         boolean isSingleton = beanFactory.isSingleton("connectionFactory");
>         System.out.println("BeanFactoryAware: Is 'connectionFactory' singleton? " + isSingleton);
>     }
> }
> ```
> 
> ## 6. Application Context Aware Pattern
> 
> ```java src/main/java/com/example/patterns/aware/ApplicationContextAwareExample.java
> package com.example.patterns.aware;
> 
> import org.springframework.beans.BeansException;
> import org.springframework.context.ApplicationContext;
> import org.springframework.context.ApplicationContextAware;
> import org.springframework.stereotype.Component;
> 
> @Component
> public class ApplicationContextAwareExample implements ApplicationContextAware {
>     
>     private ApplicationContext applicationContext;
>     
>     @Override
>     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
>         System.out.println("ApplicationContextAware: ApplicationContext injected");
>         this.applicationContext = applicationContext;
>     }
>     
>     public void demonstrateApplicationContextUsage() {
>         System.out.println("ApplicationContextAware: Demonstrating ApplicationContext usage");
>         
>         // Get application name
>         String appName = applicationContext.getApplicationName();
>         System.out.println("ApplicationContextAware: Application name: " + appName);
>         
>         // Count all beans
>         String[] beanNames = applicationContext.getBeanDefinitionNames();
>         System.out.println("ApplicationContextAware: Total beans: " + beanNames.length);
>         
>         // Get environment properties
>         String javaVersion = applicationContext.getEnvironment().getProperty("java.version");
>         System.out.println("ApplicationContextAware: Java version: " + javaVersion);
>         
>         // Check for specific bean
>         boolean hasBean = applicationContext.containsBean("sportsCar");
>         System.out.println("ApplicationContextAware: Has 'sportsCar' bean? " + hasBean);
>     }
> }
> ```
> 
> ## 7. Bean Name Aware Pattern
> 
> ```java src/main/java/com/example/patterns/aware/BeanNameAwareExample.java
> package com.example.patterns.aware;
> 
> import org.springframework.beans.factory.BeanNameAware;
> import org.springframework.stereotype.Component;
> 
> @Component("customBeanName")
> public class BeanNameAwareExample implements BeanNameAware {
>     
>     private String beanName;
>     
>     @Override
>     public void setBeanName(String name) {
>         System.out.println("BeanNameAware: Bean name injected: " + name);
>         this.beanName = name;
>     }
>     
>     public void printBeanName() {
>         System.out.println("BeanNameAware: My bean name is: " + beanName);
>     }
>     
>     public String getBeanName() {
>         return beanName;
>     }
> }
> ```
> 
> ## 8. Bean Class Loader Aware Pattern
> 
> ```java src/main/java/com/example/patterns/aware/BeanClassLoaderAwareExample.java
> package com.example.patterns.aware;
> 
> import org.springframework.beans.factory.BeanClassLoaderAware;
> import org.springframework.stereotype.Component;
> 
> @Component
> public class BeanClassLoaderAwareExample implements BeanClassLoaderAware {
>     
>     private ClassLoader classLoader;
>     
>     @Override
>     public void setBeanClassLoader(ClassLoader classLoader) {
>         System.out.println("BeanClassLoaderAware: ClassLoader injected");
>         this.classLoader = classLoader;
>     }
>     
>     public void demonstrateClassLoaderUsage() {
>         System.out.println("BeanClassLoaderAware: Demonstrating ClassLoader usage");
>         System.out.println("BeanClassLoaderAware: ClassLoader: " + classLoader.getClass().getName());
>         
>         try {
>             // Load a class using the classloader
>             Class<?> loadedClass = classLoader.loadClass("java.util.ArrayList");
>             System.out.println("BeanClassLoaderAware: Successfully loaded class: " + 
>                              loadedClass.getName());
>             
>             // Get class information
>             System.out.println("BeanClassLoaderAware: Package: " + loadedClass.getPackage());
>             System.out.println("BeanClassLoaderAware: Simple name: " + loadedClass.getSimpleName());
>             
>         } catch (ClassNotFoundException e) {
>             System.err.println("BeanClassLoaderAware: Failed to load class: " + e.getMessage());
>         }
>     }
> }
> ```
> 
> ## Main Application
> 
> ```java src/main/java/com/example/BeanFactoryPatternsApplication.java
> package com.example;
> 
> import com.example.patterns.aware.*;
> import com.example.patterns.factorybean.Connection;
> import com.example.patterns.factorymethod.Vehicle;
> import com.example.patterns.instancefactory.MessageService;
> import com.example.patterns.staticfactory.DataSource;
> import org.springframework.boot.CommandLineRunner;
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> import org.springframework.context.ApplicationContext;
> import org.springframework.context.annotation.Bean;
> 
> @SpringBootApplication
> public class BeanFactoryPatternsApplication {
> 
>     public static void main(String[] args) {
>         SpringApplication.run(BeanFactoryPatternsApplication.class, args);
>     }
> 
>     @Bean
>     public CommandLineRunner demo(ApplicationContext context) {
>         return args -> {
>             System.out.println("\n========================================");
>             System.out.println("BEAN FACTORY PATTERNS DEMONSTRATION");
>             System.out.println("========================================\n");
> 
>             // 1. Factory Bean Pattern
>             System.out.println("\n--- 1. FACTORY BEAN PATTERN ---");
>             Connection connection = context.getBean("connectionFactory", Connection.class);
>             connection.connect();
> 
>             // 2. Factory Method Pattern
>             System.out.println("\n--- 2. FACTORY METHOD PATTERN ---");
>             Vehicle car = context.getBean("sportsCar", Vehicle.class);
>             car.drive();
>             Vehicle bike = context.getBean("mountainBike", Vehicle.class);
>             bike.drive();
> 
>             // 3. Static Factory Method Pattern
>             System.out.println("\n--- 3. STATIC FACTORY METHOD PATTERN ---");
>             DataSource mysql = context.getBean("mysqlDataSource", DataSource.class);
>             mysql.printInfo();
>             DataSource postgres = context.getBean("postgresDataSource", DataSource.class);
>             postgres.printInfo();
> 
>             // 4. Instance Factory Method Pattern
>             System.out.println("\n--- 4. INSTANCE FACTORY METHOD PATTERN ---");
>             MessageService emailService = context.getBean("emailService", MessageService.class);
>             emailService.sendMessage("Hello via Email");
>             MessageService smsService = context.getBean("smsService", MessageService.class);
>             smsService.sendMessage("Hello via SMS");
> 
>             // 5. Bean Factory Aware Pattern
>             System.out.println("\n--- 5. BEAN FACTORY AWARE PATTERN ---");
>             BeanFactoryAwareExample bfAware = context.getBean(BeanFactoryAwareExample.class);
>             bfAware.demonstrateBeanFactoryUsage();
> 
>             // 6. Application Context Aware Pattern
>             System.out.println("\n--- 6. APPLICATION CONTEXT AWARE PATTERN ---");
>             ApplicationContextAwareExample acAware = 
>                 context.getBean(ApplicationContextAwareExample.class);
>             acAware.demonstrateApplicationContextUsage();
> 
>             // 7. Bean Name Aware Pattern
>             System.out.println("\n--- 7. BEAN NAME AWARE PATTERN ---");
>             BeanNameAwareExample bnAware = context.getBean(BeanNameAwareExample.class);
>             bnAware.printBeanName();
> 
>             // 8. Bean Class Loader Aware Pattern
>             System.out.println("\n--- 8. BEAN CLASS LOADER AWARE PATTERN ---");
>             BeanClassLoaderAwareExample bclAware = 
>                 context.getBean(BeanClassLoaderAwareExample.class);
>             bclAware.demonstrateClassLoaderUsage();
> 
>             System.out.println("\n========================================");
>             System.out.println("DEMONSTRATION COMPLETE");
>             System.out.println("========================================\n");
>         };
>     }
> }
> ```
> 
> ## Application Properties
> 
> ```properties src/main/resources/application.properties
> spring.application.name=Bean Factory Patterns Demo
> logging.level.root=INFO
> ```
> 
> ## Running the Application
> 
> To run this application:
> 
> ```bash
> mvn clean install
> mvn spring-boot:run
> ```
> 
> ## Summary
> 
> This project demonstrates all 8 Bean Factory patterns:
> 
> 1. **Factory Bean Pattern**: Creates complex objects with `FactoryBean<T>` interface
> 2. **Factory Method Pattern**: Uses `@Bean` methods to create objects
> 3. **Static Factory Method Pattern**: Leverages static factory methods in bean creation
> 4. **Instance Factory Method Pattern**: Uses instance factory beans to create other beans
> 5. **BeanFactoryAware**: Provides access to the BeanFactory
> 6. **ApplicationContextAware**: Provides access to the ApplicationContext
> 7. **BeanNameAware**: Injects the bean's name
> 8. **BeanClassLoaderAware**: Injects the ClassLoader used to load the bean
> 
> Each pattern is isolated in its own package with practical examples showing real-world usage scenarios.