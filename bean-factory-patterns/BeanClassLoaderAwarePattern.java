package com.spring.patterns.factory;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.URL;

/**
 * BeanClassLoaderAware Pattern
 * 
 * BeanClassLoaderAware provides beans with the ClassLoader that loaded their class.
 * The ClassLoader is injected via setBeanClassLoader() callback.
 * 
 * Characteristics:
 * - Implements BeanClassLoaderAware interface
 * - setBeanClassLoader() called during initialization
 * - Receives ClassLoader reference
 * - Can load classes and resources dynamically
 * - Useful for plugin systems
 * - Enables runtime class loading
 * 
 * Use Cases:
 * - Dynamic class loading
 * - Plugin architecture
 * - Resource loading
 * - Reflection utilities
 * - Class scanning
 * - Framework integration
 */
@SpringBootApplication
public class BeanClassLoaderAwarePattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(BeanClassLoaderAwarePattern.class, args);
        
        System.out.println("\n=== BeanClassLoaderAware Pattern ===");
        
        // Example 1: Dynamic class loader
        DynamicClassLoader classLoader = context.getBean(DynamicClassLoader.class);
        classLoader.loadClassByName("java.util.HashMap");
        classLoader.loadClassByName("java.time.LocalDateTime");
        
        // Example 2: Resource loader
        ResourceLoaderService resourceLoader = context.getBean(ResourceLoaderService.class);
        resourceLoader.loadResource("/application.properties");
        
        // Example 3: Plugin loader
        PluginLoader pluginLoader = context.getBean(PluginLoader.class);
        pluginLoader.scanForPlugins("com.spring.patterns");
        
        // Example 4: Class inspector
        ClassInspector inspector = context.getBean(ClassInspector.class);
        inspector.inspectClass(String.class);
    }
}

/**
 * Example 1: Dynamic Class Loader
 */
@Component
class DynamicClassLoader implements BeanClassLoaderAware {
    
    private ClassLoader classLoader;
    
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        System.out.println("DynamicClassLoader.setBeanClassLoader() called");
        System.out.println("   ClassLoader: " + classLoader.getClass().getSimpleName());
        this.classLoader = classLoader;
    }
    
    public Class<?> loadClassByName(String className) {
        System.out.println("\n1. Loading class: " + className);
        
        try {
            Class<?> clazz = classLoader.loadClass(className);
            System.out.println("   Loaded: " + clazz.getName());
            System.out.println("   Package: " + clazz.getPackageName());
            System.out.println("   Modifiers: " + java.lang.reflect.Modifier.toString(clazz.getModifiers()));
            return clazz;
            
        } catch (ClassNotFoundException e) {
            System.out.println("   Class not found: " + className);
            return null;
        }
    }
    
    public boolean isClassAvailable(String className) {
        try {
            classLoader.loadClass(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}

/**
 * Example 2: Resource Loader Service
 */
@Component
class ResourceLoaderService implements BeanClassLoaderAware {
    
    private ClassLoader classLoader;
    
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        System.out.println("ResourceLoaderService.setBeanClassLoader() called");
        this.classLoader = classLoader;
    }
    
    public void loadResource(String resourcePath) {
        System.out.println("\n2. Loading resource: " + resourcePath);
        
        try {
            URL resourceUrl = classLoader.getResource(resourcePath.startsWith("/") 
                ? resourcePath.substring(1) : resourcePath);
            
            if (resourceUrl != null) {
                System.out.println("   Found: " + resourceUrl);
                System.out.println("   Protocol: " + resourceUrl.getProtocol());
            } else {
                System.out.println("   Resource not found: " + resourcePath);
            }
            
        } catch (Exception e) {
            System.out.println("   Error loading resource: " + e.getMessage());
        }
    }
    
    public InputStream getResourceAsStream(String resourcePath) {
        return classLoader.getResourceAsStream(resourcePath);
    }
    
    public URL getResource(String resourcePath) {
        return classLoader.getResource(resourcePath);
    }
}

/**
 * Example 3: Plugin Loader
 */
@Component
class PluginLoader implements BeanClassLoaderAware {
    
    private ClassLoader classLoader;
    
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        System.out.println("PluginLoader.setBeanClassLoader() called");
        this.classLoader = classLoader;
    }
    
    public void scanForPlugins(String packageName) {
        System.out.println("\n3. Scanning for plugins in: " + packageName);
        
        try {
            String path = packageName.replace('.', '/');
            java.util.Enumeration<URL> resources = classLoader.getResources(path);
            
            int count = 0;
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                System.out.println("   Found: " + resource);
                count++;
            }
            System.out.println("   Total resources found: " + count);
            
        } catch (Exception e) {
            System.out.println("   Error scanning: " + e.getMessage());
        }
    }
    
    public Object loadPlugin(String className) {
        try {
            Class<?> pluginClass = classLoader.loadClass(className);
            return pluginClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.out.println("   Error loading plugin: " + e.getMessage());
            return null;
        }
    }
}

/**
 * Example 4: Class Inspector
 */
@Component
class ClassInspector implements BeanClassLoaderAware {
    
    private ClassLoader classLoader;
    
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        System.out.println("ClassInspector.setBeanClassLoader() called");
        this.classLoader = classLoader;
    }
    
    public void inspectClass(Class<?> clazz) {
        System.out.println("\n4. Inspecting class: " + clazz.getName());
        
        System.out.println("   ClassLoader: " + clazz.getClassLoader());
        System.out.println("   Package: " + clazz.getPackageName());
        System.out.println("   Simple Name: " + clazz.getSimpleName());
        System.out.println("   Is Interface: " + clazz.isInterface());
        System.out.println("   Is Abstract: " + java.lang.reflect.Modifier.isAbstract(clazz.getModifiers()));
        System.out.println("   Superclass: " + (clazz.getSuperclass() != null ? clazz.getSuperclass().getName() : "none"));
        
        System.out.println("   Fields: " + clazz.getDeclaredFields().length);
        System.out.println("   Methods: " + clazz.getDeclaredMethods().length);
        System.out.println("   Constructors: " + clazz.getDeclaredConstructors().length);
    }
    
    public void compareClassLoaders(Class<?> clazz1, Class<?> clazz2) {
        ClassLoader cl1 = clazz1.getClassLoader();
        ClassLoader cl2 = clazz2.getClassLoader();
        
        System.out.println("Comparing ClassLoaders:");
        System.out.println("  " + clazz1.getName() + ": " + cl1);
        System.out.println("  " + clazz2.getName() + ": " + cl2);
        System.out.println("  Same ClassLoader: " + (cl1 == cl2));
    }
}

/**
 * Example 5: Reflection Helper
 */
@Component
class ReflectionHelper implements BeanClassLoaderAware {
    
    private ClassLoader classLoader;
    
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    public Object createInstance(String className) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.out.println("Error creating instance: " + e.getMessage());
            return null;
        }
    }
    
    public boolean hasMethod(String className, String methodName) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            for (java.lang.reflect.Method method : clazz.getMethods()) {
                if (method.getName().equals(methodName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    public java.lang.reflect.Method[] getMethods(String className) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            return clazz.getMethods();
        } catch (Exception e) {
            return new java.lang.reflect.Method[0];
        }
    }
}

/**
 * Example 6: Class Path Scanner
 */
@Component
class ClassPathScanner implements BeanClassLoaderAware {
    
    private ClassLoader classLoader;
    
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    public void scanClassPath() {
        System.out.println("Scanning classpath:");
        
        try {
            java.util.Enumeration<URL> roots = classLoader.getResources("");
            while (roots.hasMoreElements()) {
                URL root = roots.nextElement();
                System.out.println("  Root: " + root);
            }
        } catch (Exception e) {
            System.out.println("  Error scanning: " + e.getMessage());
        }
    }
    
    public java.util.List<URL> findResources(String name) {
        java.util.List<URL> urls = new java.util.ArrayList<>();
        try {
            java.util.Enumeration<URL> resources = classLoader.getResources(name);
            while (resources.hasMoreElements()) {
                urls.add(resources.nextElement());
            }
        } catch (Exception e) {
            // Handle exception
        }
        return urls;
    }
}

/**
 * Example 7: Class Loader Info
 */
@Component
class ClassLoaderInfo implements BeanClassLoaderAware {
    
    private ClassLoader classLoader;
    
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    public void printClassLoaderHierarchy() {
        System.out.println("ClassLoader Hierarchy:");
        
        ClassLoader current = classLoader;
        int level = 0;
        
        while (current != null) {
            String indent = "  ".repeat(level);
            System.out.println(indent + current.getClass().getName());
            current = current.getParent();
            level++;
        }
    }
    
    public String getClassLoaderName() {
        return classLoader.getName();
    }
    
    public ClassLoader getParentClassLoader() {
        return classLoader.getParent();
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/bean-classloader-aware")
class BeanClassLoaderAwareController {
    
    private final DynamicClassLoader classLoader;
    private final ResourceLoaderService resourceLoader;
    private final ClassInspector inspector;
    
    public BeanClassLoaderAwareController(DynamicClassLoader classLoader,
                                         ResourceLoaderService resourceLoader,
                                         ClassInspector inspector) {
        this.classLoader = classLoader;
        this.resourceLoader = resourceLoader;
        this.inspector = inspector;
    }
    
    @GetMapping("/load-class/{className}")
    public String loadClass(@PathVariable String className) {
        Class<?> clazz = classLoader.loadClassByName(className.replace("-", "."));
        return clazz != null ? "Loaded: " + clazz.getName() : "Class not found";
    }
    
    @GetMapping("/check-class/{className}")
    public String checkClass(@PathVariable String className) {
        boolean available = classLoader.isClassAvailable(className.replace("-", "."));
        return "Class available: " + available;
    }
    
    @GetMapping("/load-resource/{path}")
    public String loadResource(@PathVariable String path) {
        resourceLoader.loadResource(path);
        return "Resource loading attempted: " + path;
    }
}

/**
 * Key Points:
 * 
 * 1. BeanClassLoaderAware Interface:
 *    public interface BeanClassLoaderAware extends Aware {
 *        void setBeanClassLoader(ClassLoader classLoader);
 *    }
 * 
 * 2. Callback Lifecycle:
 *    1. Bean instantiated
 *    2. Dependencies injected
 *    3. setBeanName() called
 *    4. setBeanClassLoader() called ← BeanClassLoaderAware
 *    5. setBeanFactory() called
 *    6. setApplicationContext() called
 *    7. @PostConstruct methods
 *    8. Bean ready
 * 
 * 3. ClassLoader Capabilities:
 *    ✓ loadClass(name) - Load class dynamically
 *    ✓ getResource(name) - Get resource URL
 *    ✓ getResourceAsStream(name) - Get resource stream
 *    ✓ getResources(name) - Get all matching resources
 *    ✓ getParent() - Get parent ClassLoader
 * 
 * 4. Use Cases:
 *    ✓ Dynamic class loading
 *    ✓ Plugin systems
 *    ✓ Resource loading
 *    ✓ Reflection utilities
 *    ✓ Class path scanning
 *    ✓ Framework development
 * 
 * 5. ClassLoader Hierarchy:
 *    Bootstrap ClassLoader (native)
 *    ├── Platform ClassLoader (Java 9+)
 *    │   └── Application ClassLoader
 *    │       └── Custom ClassLoaders
 * 
 * 6. Best Practices:
 *    ✓ Cache loaded classes
 *    ✓ Handle ClassNotFoundException
 *    ✓ Consider thread context ClassLoader
 *    ✓ Be aware of class visibility
 *    ✓ Use for framework code only
 * 
 * 7. Common Patterns:
 *    - Plugin Loading: Load classes at runtime
 *    - Resource Loading: Access classpath resources
 *    - Class Scanning: Find annotated classes
 *    - Reflection: Create instances dynamically
 * 
 * 8. Security Considerations:
 *    ⚠ Validate class names before loading
 *    ⚠ Beware of class injection attacks
 *    ⚠ Use SecurityManager if needed
 *    ⚠ Restrict class loading sources
 * 
 * 9. Testing:
 *    @Test
 *    void testBeanClassLoaderAware() {
 *        ClassLoader classLoader = Thread.currentThread()
 *            .getContextClassLoader();
 *        
 *        DynamicClassLoader loader = new DynamicClassLoader();
 *        loader.setBeanClassLoader(classLoader);
 *        
 *        Class<?> clazz = loader.loadClassByName("java.util.HashMap");
 *        assertNotNull(clazz);
 *        assertEquals("HashMap", clazz.getSimpleName());
 *    }
 * 
 * 10. When to Use:
 *     ✓ Building plugin architectures
 *     ✓ Dynamic class loading needed
 *     ✓ Framework development
 *     ✓ Resource management
 *     ✗ Regular application code
 *     ✗ Simple dependency injection
 */
