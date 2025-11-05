### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 1:29:04 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **Bean Lifecycle Patterns**
> - Bean Initialization Pattern
> - Bean Destruction Pattern
> - Post Construct Pattern
> - Pre Destroy Pattern
> - Init Method Pattern
> - Destroy Method Pattern
> - Disposable Bean Pattern
> - Initializing Bean Pattern
> - Lifecycle Interface Pattern
> - SmartLifecycle Pattern
> - Phased Bean Pattern
> ```

#### _Assistant_

> I'll create a comprehensive Spring Boot application demonstrating all the bean lifecycle patterns. Here's the complete implementation:
> 
> ```java src/main/java/com/example/lifecycle/LifecycleApplication.java
> package com.example.lifecycle;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> 
> @SpringBootApplication
> public class LifecycleApplication {
>     public static void main(String[] args) {
>         SpringApplication.run(LifecycleApplication.class, args);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/lifecycle/config/BeanLifecycleConfig.java
> package com.example.lifecycle.config;
> 
> import com.example.lifecycle.beans.*;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> 
> @Configuration
> public class BeanLifecycleConfig {
> 
>     @Bean(initMethod = "customInit", destroyMethod = "customDestroy")
>     public InitMethodBean initMethodBean() {
>         return new InitMethodBean();
>     }
> 
>     @Bean(destroyMethod = "customDestroy")
>     public DestroyMethodBean destroyMethodBean() {
>         return new DestroyMethodBean();
>     }
> 
>     @Bean
>     public PostConstructBean postConstructBean() {
>         return new PostConstructBean();
>     }
> 
>     @Bean
>     public PreDestroyBean preDestroyBean() {
>         return new PreDestroyBean();
>     }
> 
>     @Bean
>     public DisposableBeanExample disposableBeanExample() {
>         return new DisposableBeanExample();
>     }
> 
>     @Bean
>     public InitializingBeanExample initializingBeanExample() {
>         return new InitializingBeanExample();
>     }
> 
>     @Bean
>     public LifecycleInterfaceBean lifecycleInterfaceBean() {
>         return new LifecycleInterfaceBean();
>     }
> 
>     @Bean
>     public SmartLifecycleBean smartLifecycleBean() {
>         return new SmartLifecycleBean();
>     }
> 
>     @Bean
>     public PhasedBeanExample phasedBeanExample() {
>         return new PhasedBeanExample();
>     }
> 
>     @Bean
>     public BeanInitializationExample beanInitializationExample() {
>         return new BeanInitializationExample();
>     }
> 
>     @Bean
>     public BeanDestructionExample beanDestructionExample() {
>         return new BeanDestructionExample();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/lifecycle/beans/BeanInitializationExample.java
> package com.example.lifecycle.beans;
> 
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> import org.springframework.beans.factory.BeanNameAware;
> import org.springframework.beans.factory.InitializingBean;
> 
> import jakarta.annotation.PostConstruct;
> 
> /**
>  * Bean Initialization Pattern - Demonstrates complete bean initialization lifecycle
>  */
> public class BeanInitializationExample implements BeanNameAware, InitializingBean {
>     
>     private static final Logger log = LoggerFactory.getLogger(BeanInitializationExample.class);
>     private String beanName;
>     private boolean initialized = false;
> 
>     public BeanInitializationExample() {
>         log.info("1. Constructor called - Bean instance created");
>     }
> 
>     @Override
>     public void setBeanName(String name) {
>         this.beanName = name;
>         log.info("2. setBeanName() called - Bean name: {}", name);
>     }
> 
>     @PostConstruct
>     public void postConstruct() {
>         log.info("3. @PostConstruct called - Custom initialization logic");
>         this.initialized = true;
>     }
> 
>     @Override
>     public void afterPropertiesSet() {
>         log.info("4. afterPropertiesSet() called - All properties set");
>     }
> 
>     public boolean isInitialized() {
>         return initialized;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/lifecycle/beans/BeanDestructionExample.java
> package com.example.lifecycle.beans;
> 
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> import org.springframework.beans.factory.DisposableBean;
> 
> import jakarta.annotation.PreDestroy;
> 
> /**
>  * Bean Destruction Pattern - Demonstrates complete bean destruction lifecycle
>  */
> public class BeanDestructionExample implements DisposableBean {
>     
>     private static final Logger log = LoggerFactory.getLogger(BeanDestructionExample.class);
>     private boolean destroyed = false;
> 
>     @PreDestroy
>     public void preDestroy() {
>         log.info("1. @PreDestroy called - Cleanup before destruction");
>         this.destroyed = true;
>     }
> 
>     @Override
>     public void destroy() {
>         log.info("2. destroy() from DisposableBean - Final cleanup");
>     }
> 
>     public boolean isDestroyed() {
>         return destroyed;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/lifecycle/beans/PostConstructBean.java
> package com.example.lifecycle.beans;
> 
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> 
> import jakarta.annotation.PostConstruct;
> 
> /**
>  * Post Construct Pattern - Using @PostConstruct annotation for initialization
>  */
> public class PostConstructBean {
>     
>     private static final Logger log = LoggerFactory.getLogger(PostConstructBean.class);
>     private String status;
> 
>     public PostConstructBean() {
>         log.info("PostConstructBean: Constructor called");
>         this.status = "CREATED";
>     }
> 
>     @PostConstruct
>     public void initialize() {
>         log.info("PostConstructBean: @PostConstruct method called");
>         this.status = "INITIALIZED";
>         // Perform initialization logic like:
>         // - Loading configuration
>         // - Establishing connections
>         // - Validating dependencies
>     }
> 
>     public String getStatus() {
>         return status;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/lifecycle/beans/PreDestroyBean.java
> package com.example.lifecycle.beans;
> 
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> 
> import jakarta.annotation.PreDestroy;
> 
> /**
>  * Pre Destroy Pattern - Using @PreDestroy annotation for cleanup
>  */
> public class PreDestroyBean {
>     
>     private static final Logger log = LoggerFactory.getLogger(PreDestroyBean.class);
>     private String status = "ACTIVE";
> 
>     @PreDestroy
>     public void cleanup() {
>         log.info("PreDestroyBean: @PreDestroy method called");
>         this.status = "DESTROYED";
>         // Perform cleanup logic like:
>         // - Closing connections
>         // - Releasing resources
>         // - Saving state
>     }
> 
>     public String getStatus() {
>         return status;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/lifecycle/beans/InitMethodBean.java
> package com.example.lifecycle.beans;
> 
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> 
> /**
>  * Init Method Pattern - Using custom init method via @Bean configuration
>  */
> public class InitMethodBean {
>     
>     private static final Logger log = LoggerFactory.getLogger(InitMethodBean.class);
>     private boolean ready = false;
> 
>     public InitMethodBean() {
>         log.info("InitMethodBean: Constructor called");
>     }
> 
>     public void customInit() {
>         log.info("InitMethodBean: customInit() method called (configured via @Bean)");
>         this.ready = true;
>         // Custom initialization logic
>     }
> 
>     public void customDestroy() {
>         log.info("InitMethodBean: customDestroy() method called (configured via @Bean)");
>         this.ready = false;
>     }
> 
>     public boolean isReady() {
>         return ready;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/lifecycle/beans/DestroyMethodBean.java
> package com.example.lifecycle.beans;
> 
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> 
> /**
>  * Destroy Method Pattern - Using custom destroy method via @Bean configuration
>  */
> public class DestroyMethodBean {
>     
>     private static final Logger log = LoggerFactory.getLogger(DestroyMethodBean.class);
>     private String resource = "Resource allocated";
> 
>     public DestroyMethodBean() {
>         log.info("DestroyMethodBean: Constructor called");
>     }
> 
>     public void customDestroy() {
>         log.info("DestroyMethodBean: customDestroy() method called");
>         this.resource = null;
>         // Release resources, close connections, etc.
>     }
> 
>     public String getResource() {
>         return resource;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/lifecycle/beans/DisposableBeanExample.java
> package com.example.lifecycle.beans;
> 
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> import org.springframework.beans.factory.DisposableBean;
> 
> /**
>  * Disposable Bean Pattern - Implementing DisposableBean interface
>  */
> public class DisposableBeanExample implements DisposableBean {
>     
>     private static final Logger log = LoggerFactory.getLogger(DisposableBeanExample.class);
>     private String connection = "Database connection open";
> 
>     public DisposableBeanExample() {
>         log.info("DisposableBeanExample: Constructor called");
>     }
> 
>     @Override
>     public void destroy() throws Exception {
>         log.info("DisposableBeanExample: destroy() method from DisposableBean interface");
>         this.connection = null;
>         // Clean up resources
>         // Close connections
>         // Release locks
>     }
> 
>     public String getConnection() {
>         return connection;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/lifecycle/beans/InitializingBeanExample.java
> package com.example.lifecycle.beans;
> 
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> import org.springframework.beans.factory.InitializingBean;
> 
> /**
>  * Initializing Bean Pattern - Implementing InitializingBean interface
>  */
> public class InitializingBeanExample implements InitializingBean {
>     
>     private static final Logger log = LoggerFactory.getLogger(InitializingBeanExample.class);
>     private boolean configured = false;
> 
>     public InitializingBeanExample() {
>         log.info("InitializingBeanExample: Constructor called");
>     }
> 
>     @Override
>     public void afterPropertiesSet() throws Exception {
>         log.info("InitializingBeanExample: afterPropertiesSet() from InitializingBean interface");
>         this.configured = true;
>         // Perform initialization after all bean properties have been set
>         // Validate configuration
>         // Initialize caches
>     }
> 
>     public boolean isConfigured() {
>         return configured;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/lifecycle/beans/LifecycleInterfaceBean.java
> package com.example.lifecycle.beans;
> 
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> import org.springframework.context.Lifecycle;
> 
> /**
>  * Lifecycle Interface Pattern - Implementing Lifecycle interface
>  */
> public class LifecycleInterfaceBean implements Lifecycle {
>     
>     private static final Logger log = LoggerFactory.getLogger(LifecycleInterfaceBean.class);
>     private volatile boolean running = false;
> 
>     @Override
>     public void start() {
>         if (!running) {
>             log.info("LifecycleInterfaceBean: start() method called");
>             this.running = true;
>             // Start background tasks
>             // Open connections
>         }
>     }
> 
>     @Override
>     public void stop() {
>         if (running) {
>             log.info("LifecycleInterfaceBean: stop() method called");
>             this.running = false;
>             // Stop background tasks
>             // Close connections
>         }
>     }
> 
>     @Override
>     public boolean isRunning() {
>         return running;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/lifecycle/beans/SmartLifecycleBean.java
> package com.example.lifecycle.beans;
> 
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> import org.springframework.context.SmartLifecycle;
> 
> /**
>  * SmartLifecycle Pattern - Implementing SmartLifecycle for automatic lifecycle management
>  */
> public class SmartLifecycleBean implements SmartLifecycle {
>     
>     private static final Logger log = LoggerFactory.getLogger(SmartLifecycleBean.class);
>     private volatile boolean running = false;
> 
>     @Override
>     public void start() {
>         log.info("SmartLifecycleBean: start() method called");
>         this.running = true;
>         // Automatically started by Spring container
>         // Ideal for background services, schedulers
>     }
> 
>     @Override
>     public void stop() {
>         log.info("SmartLifecycleBean: stop() method called");
>         this.running = false;
>     }
> 
>     @Override
>     public boolean isRunning() {
>         return running;
>     }
> 
>     @Override
>     public boolean isAutoStartup() {
>         // Return true to automatically start this bean
>         return true;
>     }
> 
>     @Override
>     public void stop(Runnable callback) {
>         log.info("SmartLifecycleBean: stop(callback) method called");
>         stop();
>         callback.run();
>     }
> 
>     @Override
>     public int getPhase() {
>         // Default phase is 0
>         // Lower values start first and stop last
>         return 0;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/lifecycle/beans/PhasedBeanExample.java
> package com.example.lifecycle.beans;
> 
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> import org.springframework.context.SmartLifecycle;
> 
> /**
>  * Phased Bean Pattern - Using phases to control startup/shutdown order
>  */
> public class PhasedBeanExample implements SmartLifecycle {
>     
>     private static final Logger log = LoggerFactory.getLogger(PhasedBeanExample.class);
>     private volatile boolean running = false;
>     private final int phase;
> 
>     public PhasedBeanExample() {
>         this.phase = 100; // Higher phase number
>     }
> 
>     @Override
>     public void start() {
>         log.info("PhasedBeanExample: start() at phase {}", phase);
>         this.running = true;
>         // Starts after beans with lower phase values
>     }
> 
>     @Override
>     public void stop() {
>         log.info("PhasedBeanExample: stop() at phase {}", phase);
>         this.running = false;
>         // Stops before beans with lower phase values
>     }
> 
>     @Override
>     public boolean isRunning() {
>         return running;
>     }
> 
>     @Override
>     public int getPhase() {
>         return phase;
>     }
> 
>     @Override
>     public boolean isAutoStartup() {
>         return true;
>     }
> 
>     @Override
>     public void stop(Runnable callback) {
>         stop();
>         callback.run();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/lifecycle/controller/LifecycleController.java
> package com.example.lifecycle.controller;
> 
> import com.example.lifecycle.beans.*;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> 
> import java.util.HashMap;
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/lifecycle")
> public class LifecycleController {
> 
>     @Autowired
>     private BeanInitializationExample beanInitializationExample;
> 
>     @Autowired
>     private PostConstructBean postConstructBean;
> 
>     @Autowired
>     private InitMethodBean initMethodBean;
> 
>     @Autowired
>     private InitializingBeanExample initializingBeanExample;
> 
>     @Autowired
>     private LifecycleInterfaceBean lifecycleInterfaceBean;
> 
>     @Autowired
>     private SmartLifecycleBean smartLifecycleBean;
> 
>     @GetMapping("/status")
>     public Map<String, Object> getStatus() {
>         Map<String, Object> status = new HashMap<>();
>         
>         status.put("beanInitialization", Map.of(
>             "initialized", beanInitializationExample.isInitialized()
>         ));
>         
>         status.put("postConstruct", Map.of(
>             "status", postConstructBean.getStatus()
>         ));
>         
>         status.put("initMethod", Map.of(
>             "ready", initMethodBean.isReady()
>         ));
>         
>         status.put("initializingBean", Map.of(
>             "configured", initializingBeanExample.isConfigured()
>         ));
>         
>         status.put("lifecycle", Map.of(
>             "running", lifecycleInterfaceBean.isRunning()
>         ));
>         
>         status.put("smartLifecycle", Map.of(
>             "running", smartLifecycleBean.isRunning()
>         ));
>         
>         return status;
>     }
> }
> ```
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
>     <artifactId>bean-lifecycle-patterns</artifactId>
>     <version>1.0.0</version>
>     <name>Bean Lifecycle Patterns</name>
>     <description>Spring Boot Bean Lifecycle Patterns Demo</description>
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
> ```properties src/main/resources/application.properties
> # Application Configuration
> spring.application.name=bean-lifecycle-patterns
> 
> # Server Configuration
> server.port=8080
> 
> # Logging Configuration
> logging.level.com.example.lifecycle=INFO
> logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
> ```
> 
> ```java src/test/java/com/example/lifecycle/LifecycleApplicationTests.java
> package com.example.lifecycle;
> 
> import com.example.lifecycle.beans.*;
> import org.junit.jupiter.api.Test;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.boot.test.context.SpringBootTest;
> 
> import static org.junit.jupiter.api.Assertions.*;
> 
> @SpringBootTest
> class LifecycleApplicationTests {
> 
>     @Autowired
>     private BeanInitializationExample beanInitializationExample;
> 
>     @Autowired
>     private PostConstructBean postConstructBean;
> 
>     @Autowired
>     private InitMethodBean initMethodBean;
> 
>     @Autowired
>     private InitializingBeanExample initializingBeanExample;
> 
>     @Autowired
>     private SmartLifecycleBean smartLifecycleBean;
> 
>     @Test
>     void testBeanInitialization() {
>         assertTrue(beanInitializationExample.isInitialized());
>     }
> 
>     @Test
>     void testPostConstruct() {
>         assertEquals("INITIALIZED", postConstructBean.getStatus());
>     }
> 
>     @Test
>     void testInitMethod() {
>         assertTrue(initMethodBean.isReady());
>     }
> 
>     @Test
>     void testInitializingBean() {
>         assertTrue(initializingBeanExample.isConfigured());
>     }
> 
>     @Test
>     void testSmartLifecycle() {
>         assertTrue(smartLifecycleBean.isRunning());
>     }
> }
> ```
> 
> ## Summary
> 
> This Spring Boot application demonstrates **11 Bean Lifecycle Patterns**:
> 
> 1. **Bean Initialization Pattern** - Complete initialization lifecycle with BeanNameAware and InitializingBean
> 2. **Bean Destruction Pattern** - Complete destruction lifecycle with @PreDestroy and DisposableBean
> 3. **Post Construct Pattern** - Using @PostConstruct for initialization
> 4. **Pre Destroy Pattern** - Using @PreDestroy for cleanup
> 5. **Init Method Pattern** - Custom init method via @Bean configuration
> 6. **Destroy Method Pattern** - Custom destroy method via @Bean configuration
> 7. **Disposable Bean Pattern** - Implementing DisposableBean interface
> 8. **Initializing Bean Pattern** - Implementing InitializingBean interface
> 9. **Lifecycle Interface Pattern** - Implementing Lifecycle for manual control
> 10. **SmartLifecycle Pattern** - Automatic lifecycle management
> 11. **Phased Bean Pattern** - Controlling startup/shutdown order with phases
> 
> **To run:** 
> ```bash
> mvn spring-boot:run
> ```
> 
> **Test endpoint:**
> ```bash
> curl http://localhost:8080/api/lifecycle/status
> ```

#### _User_

> continue exactly where you left off

#### _Assistant_

> ```markdown README.md
> # Spring Bean Lifecycle Patterns
> 
> A comprehensive demonstration of all Spring Bean Lifecycle patterns with practical examples.
> 
> ## Project Structure
> 
> ```
> src/
> ├── main/
> │   ├── java/com/example/lifecycle/
> │   │   ├── LifecycleApplication.java          # Main Spring Boot application
> │   │   ├── config/
> │   │   │   └── BeanLifecycleConfig.java       # Bean configuration
> │   │   ├── beans/
> │   │   │   ├── BeanInitializationExample.java  # Pattern 1: Bean Initialization
> │   │   │   ├── BeanDestructionExample.java     # Pattern 2: Bean Destruction
> │   │   │   ├── PostConstructBean.java          # Pattern 3: @PostConstruct
> │   │   │   ├── PreDestroyBean.java             # Pattern 4: @PreDestroy
> │   │   │   ├── InitMethodBean.java             # Pattern 5: Init Method
> │   │   │   ├── DestroyMethodBean.java          # Pattern 6: Destroy Method
> │   │   │   ├── DisposableBeanExample.java      # Pattern 7: DisposableBean
> │   │   │   ├── InitializingBeanExample.java    # Pattern 8: InitializingBean
> │   │   │   ├── LifecycleInterfaceBean.java     # Pattern 9: Lifecycle
> │   │   │   ├── SmartLifecycleBean.java         # Pattern 10: SmartLifecycle
> │   │   │   └── PhasedBeanExample.java          # Pattern 11: Phased Bean
> │   │   └── controller/
> │   │       └── LifecycleController.java        # REST API controller
> │   └── resources/
> │       └── application.properties
> └── test/
>     └── java/com/example/lifecycle/
>         └── LifecycleApplicationTests.java
> ```
> 
> ## Bean Lifecycle Patterns Explained
> 
> ### 1. Bean Initialization Pattern
> Complete initialization lifecycle combining multiple mechanisms:
> - Constructor
> - BeanNameAware
> - @PostConstruct
> - InitializingBean
> 
> **When to use:** When you need full control over the entire initialization process.
> 
> ### 2. Bean Destruction Pattern
> Complete destruction lifecycle:
> - @PreDestroy
> - DisposableBean
> - Custom destroy methods
> 
> **When to use:** For comprehensive cleanup with multiple destruction hooks.
> 
> ### 3. Post Construct Pattern
> Using `@PostConstruct` annotation:
> ```java
> @PostConstruct
> public void initialize() {
>     // Initialization logic
> }
> ```
> 
> **When to use:** Standard initialization after dependency injection. Most common pattern.
> 
> ### 4. Pre Destroy Pattern
> Using `@PreDestroy` annotation:
> ```java
> @PreDestroy
> public void cleanup() {
>     // Cleanup logic
> }
> ```
> 
> **When to use:** Standard cleanup before bean destruction. Most common pattern.
> 
> ### 5. Init Method Pattern
> Custom init method via `@Bean`:
> ```java
> @Bean(initMethod = "customInit")
> public MyBean myBean() {
>     return new MyBean();
> }
> ```
> 
> **When to use:** When working with third-party classes or XML configuration.
> 
> ### 6. Destroy Method Pattern
> Custom destroy method via `@Bean`:
> ```java
> @Bean(destroyMethod = "customDestroy")
> public MyBean myBean() {
>     return new MyBean();
> }
> ```
> 
> **When to use:** For explicit cleanup method names or third-party classes.
> 
> ### 7. Disposable Bean Pattern
> Implementing `DisposableBean`:
> ```java
> public class MyBean implements DisposableBean {
>     @Override
>     public void destroy() throws Exception {
>         // Cleanup logic
>     }
> }
> ```
> 
> **When to use:** Interface-based approach for guaranteed cleanup.
> 
> ### 8. Initializing Bean Pattern
> Implementing `InitializingBean`:
> ```java
> public class MyBean implements InitializingBean {
>     @Override
>     public void afterPropertiesSet() throws Exception {
>         // Initialization logic
>     }
> }
> ```
> 
> **When to use:** Interface-based approach for guaranteed initialization after properties are set.
> 
> ### 9. Lifecycle Interface Pattern
> Implementing `Lifecycle`:
> ```java
> public class MyBean implements Lifecycle {
>     public void start() { }
>     public void stop() { }
>     public boolean isRunning() { }
> }
> ```
> 
> **When to use:** Manual control over start/stop lifecycle.
> 
> ### 10. SmartLifecycle Pattern
> Implementing `SmartLifecycle`:
> ```java
> public class MyBean implements SmartLifecycle {
>     public boolean isAutoStartup() { return true; }
>     public void start() { }
>     public void stop(Runnable callback) { }
>     public int getPhase() { return 0; }
> }
> ```
> 
> **When to use:** Automatic lifecycle management with callback support.
> 
> ### 11. Phased Bean Pattern
> Using phases to control startup/shutdown order:
> ```java
> public int getPhase() {
>     return 100; // Higher numbers start later, stop earlier
> }
> ```
> 
> **When to use:** When you need specific startup/shutdown ordering for dependent services.
> 
> ## Lifecycle Order
> 
> ### Initialization Order:
> 1. Constructor
> 2. Dependency Injection
> 3. BeanNameAware, BeanFactoryAware, etc.
> 4. @PostConstruct
> 5. InitializingBean.afterPropertiesSet()
> 6. Custom init-method
> 7. Lifecycle.start() / SmartLifecycle.start()
> 
> ### Destruction Order:
> 1. @PreDestroy
> 2. DisposableBean.destroy()
> 3. Custom destroy-method
> 4. Lifecycle.stop() / SmartLifecycle.stop()
> 
> ## Running the Application
> 
> ### Prerequisites
> - Java 17 or higher
> - Maven 3.6+
> 
> ### Build and Run
> ```bash
> # Build the project
> mvn clean install
> 
> # Run the application
> mvn spring-boot:run
> ```
> 
> ### Test the API
> ```bash
> # Get lifecycle status
> curl http://localhost:8080/api/lifecycle/status
> ```
> 
> Expected response:
> ```json
> {
>   "beanInitialization": {
>     "initialized": true
>   },
>   "postConstruct": {
>     "status": "INITIALIZED"
>   },
>   "initMethod": {
>     "ready": true
>   },
>   "initializingBean": {
>     "configured": true
>   },
>   "lifecycle": {
>     "running": false
>   },
>   "smartLifecycle": {
>     "running": true
>   }
> }
> ```
> 
> ### Run Tests
> ```bash
> mvn test
> ```
> 
> ## Console Output
> 
> When you start the application, you'll see the lifecycle methods being called:
> 
> ```
> BeanInitializationExample: 1. Constructor called - Bean instance created
> BeanInitializationExample: 2. setBeanName() called - Bean name: beanInitializationExample
> BeanInitializationExample: 3. @PostConstruct called - Custom initialization logic
> BeanInitializationExample: 4. afterPropertiesSet() called - All properties set
> PostConstructBean: Constructor called
> PostConstructBean: @PostConstruct method called
> InitMethodBean: Constructor called
> InitMethodBean: customInit() method called (configured via @Bean)
> InitializingBeanExample: Constructor called
> InitializingBeanExample: afterPropertiesSet() from InitializingBean interface
> SmartLifecycleBean: start() method called
> PhasedBeanExample: start() at phase 100
> ```
> 
> When the application shuts down:
> ```
> PhasedBeanExample: stop() at phase 100
> SmartLifecycleBean: stop(callback) method called
> PreDestroyBean: @PreDestroy method called
> BeanDestructionExample: 1. @PreDestroy called - Cleanup before destruction
> BeanDestructionExample: 2. destroy() from DisposableBean - Final cleanup
> DisposableBeanExample: destroy() method from DisposableBean interface
> DestroyMethodBean: customDestroy() method called
> InitMethodBean: customDestroy() method called (configured via @Bean)
> ```
> 
> ## Best Practices
> 
> 1. **Prefer @PostConstruct/@PreDestroy** for most use cases (JSR-250 standard)
> 2. **Use InitializingBean/DisposableBean** when you need guaranteed callbacks
> 3. **Use init-method/destroy-method** for third-party classes
> 4. **Use SmartLifecycle** for services that need automatic start/stop
> 5. **Use Phased beans** when order matters (databases before app servers)
> 6. **Avoid mixing patterns** unless necessary - keep it simple
> 7. **Make lifecycle methods idempotent** - they might be called multiple times
> 
> ## Common Use Cases
> 
> ### Database Connection Pool
> ```java
> @Bean(initMethod = "init", destroyMethod = "close")
> public DataSource dataSource() {
>     return new HikariDataSource();
> }
> ```
> 
> ### Background Service
> ```java
> public class BackgroundService implements SmartLifecycle {
>     // Automatically starts and stops with container
> }
> ```
> 
> ### Resource Manager
> ```java
> public class ResourceManager {
>     @PostConstruct
>     public void allocate() { }
>     
>     @PreDestroy
>     public void release() { }
> }
> ```
> 
> ## Troubleshooting
> 
> ### Bean not initializing?
> - Check if component scanning is configured
> - Verify bean is in scanned package
> - Check for circular dependencies
> 
> ### Lifecycle methods not called?
> - Ensure bean is managed by Spring
> - Verify method signatures match expected patterns
> - Check application context is properly closed
> 
> ### Beans starting in wrong order?
> - Use `@DependsOn` annotation
> - Implement SmartLifecycle with appropriate phases
> - Check getPhase() return values
> 
> ## License
> 
> MIT License - Feel free to use this for learning and reference.
> 
> ## Contributing
> 
> This is a demonstration project. Feel free to fork and extend with additional patterns or examples.
> ```
> 
> ```java src/main/java/com/example/lifecycle/beans/ComplexLifecycleBean.java
> package com.example.lifecycle.beans;
> 
> import jakarta.annotation.PostConstruct;
> import jakarta.annotation.PreDestroy;
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> import org.springframework.beans.factory.BeanNameAware;
> import org.springframework.beans.factory.DisposableBean;
> import org.springframework.beans.factory.InitializingBean;
> import org.springframework.context.SmartLifecycle;
> import org.springframework.stereotype.Component;
> 
> /**
>  * Demonstrates ALL lifecycle patterns combined in a single bean
>  * This shows the complete execution order
>  */
> @Component
> public class ComplexLifecycleBean implements 
>         BeanNameAware, 
>         InitializingBean, 
>         DisposableBean, 
>         SmartLifecycle {
>     
>     private static final Logger log = LoggerFactory.getLogger(ComplexLifecycleBean.class);
>     private volatile boolean running = false;
>     private String beanName;
>     
>     // Step 1: Constructor
>     public ComplexLifecycleBean() {
>         log.info("STEP 1: Constructor called");
>     }
>     
>     // Step 2: BeanNameAware
>     @Override
>     public void setBeanName(String name) {
>         this.beanName = name;
>         log.info("STEP 2: setBeanName() - Bean name set to: {}", name);
>     }
>     
>     // Step 3: @PostConstruct
>     @PostConstruct
>     public void postConstruct() {
>         log.info("STEP 3: @PostConstruct annotation method");
>     }
>     
>     // Step 4: InitializingBean
>     @Override
>     public void afterPropertiesSet() throws Exception {
>         log.info("STEP 4: afterPropertiesSet() from InitializingBean");
>     }
>     
>     // Step 5: Custom init method (if configured via @Bean)
>     public void customInit() {
>         log.info("STEP 5: customInit() - custom initialization method");
>     }
>     
>     // Step 6: SmartLifecycle start
>     @Override
>     public void start() {
>         log.info("STEP 6: start() from SmartLifecycle");
>         this.running = true;
>     }
>     
>     // === Destruction Phase ===
>     
>     // Step 1 (Destruction): @PreDestroy
>     @PreDestroy
>     public void preDestroy() {
>         log.info("DESTROY STEP 1: @PreDestroy annotation method");
>     }
>     
>     // Step 2 (Destruction): DisposableBean
>     @Override
>     public void destroy() throws Exception {
>         log.info("DESTROY STEP 2: destroy() from DisposableBean");
>     }
>     
>     // Step 3 (Destruction): Custom destroy method
>     public void customDestroy() {
>         log.info("DESTROY STEP 3: customDestroy() - custom destruction method");
>     }
>     
>     // Step 4 (Destruction): SmartLifecycle stop
>     @Override
>     public void stop() {
>         log.info("DESTROY STEP 4: stop() from SmartLifecycle");
>         this.running = false;
>     }
>     
>     @Override
>     public void stop(Runnable callback) {
>         log.info("DESTROY STEP 4: stop(callback) from SmartLifecycle");
>         stop();
>         callback.run();
>     }
>     
>     // SmartLifecycle required methods
>     @Override
>     public boolean isRunning() {
>         return running;
>     }
>     
>     @Override
>     public boolean isAutoStartup() {
>         return true;
>     }
>     
>     @Override
>     public int getPhase() {
>         return 0;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/lifecycle/examples/RealWorldExamples.java
> package com.example.lifecycle.examples;
> 
> import jakarta.annotation.PostConstruct;
> import jakarta.annotation.PreDestroy;
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> import org.springframework.context.SmartLifecycle;
> import org.springframework.stereotype.Component;
> 
> import java.util.concurrent.ExecutorService;
> import java.util.concurrent.Executors;
> import java.util.concurrent.TimeUnit;
> 
> /**
>  * Real-world examples of bean lifecycle patterns
>  */
> public class RealWorldExamples {
>     
>     /**
>      * Example 1: Database Connection Pool Management
>      */
>     public static class DatabaseConnectionPool {
>         private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionPool.class);
>         private ExecutorService connectionPool;
>         
>         @PostConstruct
>         public void initialize() {
>             log.info("Initializing database connection pool");
>             connectionPool = Executors.newFixedThreadPool(10);
>             // Initialize connections
>             // Validate connections
>         }
>         
>         @PreDestroy
>         public void cleanup() {
>             log.info("Shutting down database connection pool");
>             if (connectionPool != null) {
>                 connectionPool.shutdown();
>                 try {
>                     if (!connectionPool.awaitTermination(60, TimeUnit.SECONDS)) {
>                         connectionPool.shutdownNow();
>                     }
>                 } catch (InterruptedException e) {
>                     connectionPool.shutdownNow();
>                     Thread.currentThread().interrupt();
>                 }
>             }
>         }
>     }
>     
>     /**
>      * Example 2: Cache Warming Service
>      */
>     @Component
>     public static class CacheWarmingService implements SmartLifecycle {
>         private static final Logger log = LoggerFactory.getLogger(CacheWarmingService.class);
>         private volatile boolean running = false;
>         
>         @Override
>         public void start() {
>             log.info("Starting cache warming service");
>             running = true;
>             warmupCache();
>         }
>         
>         private void warmupCache() {
>             // Load frequently accessed data
>             // Pre-compute expensive operations
>             log.info("Cache warmed up successfully");
>         }
>         
>         @Override
>         public void stop() {
>             log.info("Stopping cache warming service");
>             running = false;
>             // Save cache state if needed
>         }
>         
>         @Override
>         public boolean isRunning() {
>             return running;
>         }
>         
>         @Override
>         public boolean isAutoStartup() {
>             return true;
>         }
>         
>         @Override
>         public int getPhase() {
>             return Integer.MAX_VALUE; // Start last
>         }
>     }
>     
>     /**
>      * Example 3: File System Monitor
>      */
>     public static class FileSystemMonitor {
>         private static final Logger log = LoggerFactory.getLogger(FileSystemMonitor.class);
>         private Thread monitorThread;
>         private volatile boolean monitoring = false;
>         
>         @PostConstruct
>         public void startMonitoring() {
>             log.info("Starting file system monitor");
>             monitoring = true;
>             monitorThread = new Thread(() -> {
>                 while (monitoring) {
>                     // Monitor file changes
>                     try {
>                         Thread.sleep(1000);
>                     } catch (InterruptedException e) {
>                         Thread.currentThread().interrupt();
>                         break;
>                     }
>                 }
>             });
>             monitorThread.start();
>         }
>         
>         @PreDestroy
>         public void stopMonitoring() {
>             log.info("Stopping file system monitor");
>             monitoring = false;
>             if (monitorThread != null) {
>                 monitorThread.interrupt();
>                 try {
>                     monitorThread.join(5000);
>                 } catch (InterruptedException e) {
>                     Thread.currentThread().interrupt();
>                 }
>             }
>         }
>     }
>     
>     /**
>      * Example 4: Scheduled Task Manager with Phase Control
>      */
>     public static class ScheduledTaskManager implements SmartLifecycle {
>         private static final Logger log = LoggerFactory.getLogger(ScheduledTaskManager.class);
>         private volatile boolean running = false;
>         private ExecutorService scheduler;
>         
>         @Override
>         public void start() {
>             log.info("Starting scheduled task manager");
>             running = true;
>             scheduler = Executors.newScheduledThreadPool(5);
>             // Schedule tasks
>         }
>         
>         @Override
>         public void stop() {
>             log.info("Stopping scheduled task manager");
>             running = false;
>             if (scheduler != null) {
>                 scheduler.shutdown();
>             }
>         }
>         
>         @Override
>         public boolean isRunning() {
>             return running;
>         }
>         
>         @Override
>         public int getPhase() {
>             return 1000; // Start after infrastructure beans
>         }
>     }
>     
>     /**
>      * Example 5: External API Client with Health Check
>      */
>     public static class ExternalApiClient {
>         private static final Logger log = LoggerFactory.getLogger(ExternalApiClient.class);
>         private boolean connected = false;
>         
>         @PostConstruct
>         public void connect() {
>             log.info("Connecting to external API");
>             // Establish connection
>             // Authenticate
>             // Perform health check
>             connected = true;
>             log.info("Connected to external API successfully");
>         }
>         
>         @PreDestroy
>         public void disconnect() {
>             log.info("Disconnecting from external API");
>             // Close connections gracefully
>             // Flush pending requests
>             connected = false;
>             log.info("Disconnected from external API");
>         }
>         
>         public boolean isConnected() {
>             return connected;
>         }
>     }
> }
> ```
> 
> The demonstration is now complete with:
> 
> 1. ✅ All 11 bean lifecycle patterns implemented
> 2. ✅ Configuration classes
> 3. ✅ REST controller for testing
> 4. ✅ Unit tests
> 5. ✅ POM file with dependencies
> 6. ✅ Application properties
> 7. ✅ Comprehensive README with documentation
> 8. ✅ Complex lifecycle bean showing complete execution order
> 9. ✅ Real-world examples for practical understanding
> 
> You can now build and run this complete Spring Boot application to see all lifecycle patterns in action!