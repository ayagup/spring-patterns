package com.example.beanpostprocessor;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Proxy Bean Post Processor Pattern
 * 
 * Demonstrates creating proxies around beans using BeanPostProcessor
 * for implementing cross-cutting concerns like logging, monitoring,
 * security, and performance tracking.
 * 
 * Key Concepts:
 * - JDK Dynamic Proxies
 * - CGLIB Proxies
 * - ProxyFactory
 * - InvocationHandler
 * - MethodInterceptor
 * 
 * Use Cases:
 * - Method execution logging
 * - Performance monitoring
 * - Security checks
 * - Transaction management
 * - Caching
 */
@SpringBootApplication
public class ProxyBeanPostProcessorPattern {

    public static void main(String[] args) {
        SpringApplication.run(ProxyBeanPostProcessorPattern.class, args);
    }
}

/**
 * Interface for services (required for JDK proxy)
 */
interface BusinessService {
    String execute(String input);
    int calculate(int a, int b);
}

/**
 * Interface for repository
 */
interface DataRepository {
    String findById(String id);
    List<String> findAll();
}

/**
 * BeanPostProcessor that creates JDK Dynamic Proxies
 */
@Component
class JdkProxyBeanPostProcessor implements BeanPostProcessor {

    private static final List<String> proxiedBeans = new ArrayList<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Only proxy beans that implement BusinessService
        if (bean instanceof BusinessService) {
            proxiedBeans.add(beanName + " (JDK Dynamic Proxy)");
            System.out.println("Creating JDK proxy for: " + beanName);
            
            return Proxy.newProxyInstance(
                    bean.getClass().getClassLoader(),
                    bean.getClass().getInterfaces(),
                    new LoggingInvocationHandler(bean)
            );
        }
        return bean;
    }

    public static List<String> getProxiedBeans() {
        return new ArrayList<>(proxiedBeans);
    }
}

/**
 * InvocationHandler for logging method calls
 */
class LoggingInvocationHandler implements InvocationHandler {
    
    private final Object target;
    private static final List<String> methodCalls = new ArrayList<>();

    public LoggingInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        long start = System.currentTimeMillis();
        
        String methodCall = "Calling: " + method.getName() + 
                          " on " + target.getClass().getSimpleName();
        methodCalls.add(methodCall);
        System.out.println(methodCall);
        
        try {
            Object result = method.invoke(target, args);
            
            long duration = System.currentTimeMillis() - start;
            String completion = "Completed: " + method.getName() + 
                              " in " + duration + "ms";
            methodCalls.add(completion);
            System.out.println(completion);
            
            return result;
        } catch (Exception e) {
            String error = "Error in: " + method.getName() + 
                          " - " + e.getMessage();
            methodCalls.add(error);
            System.err.println(error);
            throw e;
        }
    }

    public static List<String> getMethodCalls() {
        return new ArrayList<>(methodCalls);
    }
}

/**
 * BeanPostProcessor using Spring ProxyFactory
 */
@Component
class SpringProxyBeanPostProcessor implements BeanPostProcessor {

    private static final List<String> proxiedBeans = new ArrayList<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Only proxy DataRepository beans
        if (bean instanceof DataRepository) {
            proxiedBeans.add(beanName + " (Spring ProxyFactory)");
            System.out.println("Creating Spring proxy for: " + beanName);
            
            ProxyFactory proxyFactory = new ProxyFactory(bean);
            proxyFactory.addAdvice(new PerformanceMethodInterceptor());
            
            return proxyFactory.getProxy();
        }
        return bean;
    }

    public static List<String> getProxiedBeans() {
        return new ArrayList<>(proxiedBeans);
    }
}

/**
 * MethodInterceptor for performance monitoring
 */
class PerformanceMethodInterceptor implements org.aopalliance.intercept.MethodInterceptor {
    
    private static final List<String> performanceLog = new ArrayList<>();

    @Override
    public Object invoke(org.aopalliance.intercept.MethodInvocation invocation) throws Throwable {
        long start = System.nanoTime();
        
        try {
            Object result = invocation.proceed();
            
            long duration = System.nanoTime() - start;
            String log = String.format("%s executed in %.2f ms",
                    invocation.getMethod().getName(),
                    duration / 1_000_000.0);
            performanceLog.add(log);
            System.out.println(log);
            
            return result;
        } catch (Throwable t) {
            long duration = System.nanoTime() - start;
            String log = String.format("%s failed after %.2f ms: %s",
                    invocation.getMethod().getName(),
                    duration / 1_000_000.0,
                    t.getMessage());
            performanceLog.add(log);
            System.err.println(log);
            throw t;
        }
    }

    public static List<String> getPerformanceLog() {
        return new ArrayList<>(performanceLog);
    }
}

/**
 * Implementation of BusinessService
 */
@Component
class UserBusinessService implements BusinessService {
    
    @Override
    public String execute(String input) {
        // Simulate work
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Processed: " + input;
    }

    @Override
    public int calculate(int a, int b) {
        return a + b;
    }
}

/**
 * Implementation of DataRepository
 */
@Component
class UserDataRepository implements DataRepository {
    
    @Override
    public String findById(String id) {
        // Simulate database access
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "User-" + id;
    }

    @Override
    public List<String> findAll() {
        // Simulate database access
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return List.of("User-1", "User-2", "User-3");
    }
}

/**
 * Controller to test proxied beans
 */
@RestController
class ProxyController {

    private final BusinessService businessService;
    private final DataRepository dataRepository;

    public ProxyController(BusinessService businessService,
                          DataRepository dataRepository) {
        this.businessService = businessService;
        this.dataRepository = dataRepository;
    }

    @GetMapping("/proxy/proxied-beans")
    public Map<String, Object> getProxiedBeans() {
        return Map.of(
                "jdkProxies", JdkProxyBeanPostProcessor.getProxiedBeans(),
                "springProxies", SpringProxyBeanPostProcessor.getProxiedBeans()
        );
    }

    @GetMapping("/proxy/method-calls")
    public List<String> getMethodCalls() {
        return LoggingInvocationHandler.getMethodCalls();
    }

    @GetMapping("/proxy/performance-log")
    public List<String> getPerformanceLog() {
        return PerformanceMethodInterceptor.getPerformanceLog();
    }

    @GetMapping("/proxy/test-business-service")
    public Map<String, Object> testBusinessService() {
        return Map.of(
                "execute", businessService.execute("test data"),
                "calculate", businessService.calculate(5, 10)
        );
    }

    @GetMapping("/proxy/test-repository")
    public Map<String, Object> testRepository() {
        return Map.of(
                "findById", dataRepository.findById("123"),
                "findAll", dataRepository.findAll()
        );
    }
}

/**
 * Documentation:
 * 
 * Proxy Types in Spring:
 * 
 * 1. JDK Dynamic Proxy:
 *    - Requires interface
 *    - Uses java.lang.reflect.Proxy
 *    - Implements InvocationHandler
 *    - Faster creation
 *    - Smaller memory footprint
 * 
 * 2. CGLIB Proxy:
 *    - Works with classes (no interface required)
 *    - Uses bytecode generation
 *    - Cannot proxy final methods/classes
 *    - Slower creation
 *    - Larger memory footprint
 * 
 * 3. Spring ProxyFactory:
 *    - Automatically chooses JDK or CGLIB
 *    - Unified API
 *    - Supports multiple advisors
 *    - Recommended approach
 * 
 * Creating JDK Dynamic Proxy:
 * 
 * Object proxy = Proxy.newProxyInstance(
 *     classLoader,
 *     new Class<?>[] { MyInterface.class },
 *     new InvocationHandler() {
 *         @Override
 *         public Object invoke(Object proxy, Method method, Object[] args) 
 *                 throws Throwable {
 *             // Before method
 *             Object result = method.invoke(target, args);
 *             // After method
 *             return result;
 *         }
 *     }
 * );
 * 
 * Using Spring ProxyFactory:
 * 
 * ProxyFactory factory = new ProxyFactory(target);
 * factory.addAdvice(new MethodInterceptor() {
 *     @Override
 *     public Object invoke(MethodInvocation invocation) throws Throwable {
 *         // Before
 *         Object result = invocation.proceed();
 *         // After
 *         return result;
 *     }
 * });
 * Object proxy = factory.getProxy();
 * 
 * Proxy Configuration:
 * 
 * ProxyFactory factory = new ProxyFactory(target);
 * factory.setProxyTargetClass(true); // Force CGLIB
 * factory.setExposeProxy(true); // Allow AopContext.currentProxy()
 * factory.setOptimize(true); // Enable optimizations
 * factory.setFrozen(true); // Prevent advice changes
 * 
 * Use Cases:
 * 
 * 1. Logging:
 *    - Log method entry/exit
 *    - Log parameters and results
 *    - Log execution time
 * 
 * 2. Performance Monitoring:
 *    - Measure execution time
 *    - Track method calls
 *    - Identify bottlenecks
 * 
 * 3. Security:
 *    - Check permissions
 *    - Validate authentication
 *    - Audit access
 * 
 * 4. Caching:
 *    - Cache method results
 *    - Invalidate cache
 *    - Cache statistics
 * 
 * 5. Transaction Management:
 *    - Begin transaction
 *    - Commit/rollback
 *    - Handle exceptions
 * 
 * 6. Retry Logic:
 *    - Retry failed operations
 *    - Exponential backoff
 *    - Circuit breaker
 * 
 * Best Practices:
 * - Prefer interfaces for better testability
 * - Use ProxyFactory over manual proxy creation
 * - Keep proxy logic lightweight
 * - Avoid modifying method arguments
 * - Handle exceptions properly
 * - Document proxy behavior
 * - Test both proxied and unproxied beans
 * 
 * Performance Considerations:
 * - Proxy creation overhead
 * - Method invocation overhead
 * - Memory overhead
 * - Use proxies judiciously
 * - Consider AspectJ for high-performance needs
 * 
 * Limitations:
 * - JDK proxy requires interface
 * - CGLIB cannot proxy final methods
 * - Self-invocation doesn't go through proxy
 * - Complexity in debugging
 * - Stack trace pollution
 * 
 * Self-Invocation Problem:
 * 
 * @Component
 * class MyService {
 *     public void publicMethod() {
 *         privateMethod(); // Direct call, bypasses proxy!
 *     }
 *     
 *     private void privateMethod() {
 *         // Proxy logic won't execute
 *     }
 * }
 * 
 * Solution: Use AopContext
 * ((MyService) AopContext.currentProxy()).privateMethod();
 * 
 * Testing Proxies:
 * - Test target object directly
 * - Test through proxy
 * - Verify proxy behavior
 * - Mock InvocationHandler/MethodInterceptor
 * 
 * Debugging:
 * - Enable AOP debug logging
 * - Check proxy type (JDK vs CGLIB)
 * - Verify interfaces are present
 * - Check for final methods
 * - Use breakpoints in handlers
 * 
 * Common Issues:
 * - ClassCastException with CGLIB proxies
 * - Self-invocation not proxied
 * - Final methods not proxied
 * - Proxy creation failures
 * - Performance degradation
 * 
 * Alternatives:
 * - AspectJ (compile-time weaving)
 * - Spring AOP (@Aspect)
 * - Decorator pattern
 * - Manual delegation
 */
