package com.example.nativeimage.patterns;

import org.springframework.aot.hint.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 SPRING BOOT NATIVE IMAGE - JNI CONFIG PATTERN 💡
 * ===================================================
 * 
 * Demonstrates JNI (Java Native Interface) configuration for GraalVM Native Image.
 * JNI allows Java code to interact with native libraries (C/C++).
 * Native image requires explicit JNI registration for all native methods
 * and classes accessed from native code.
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ JNI (JAVA NATIVE INTERFACE):
 *    - Java ↔ Native code bridge
 *    - C/C++ library integration
 *    - System-level operations
 *    - Hardware access
 * 
 * 2️⃣ JNI IN NATIVE IMAGE:
 *    - All JNI calls must be registered
 *    - Native methods need hints
 *    - Field/method access from native code
 *    - Native library loading
 * 
 * 3️⃣ REGISTRATION REQUIREMENTS:
 *    - Classes with native methods
 *    - Classes accessed from native code
 *    - Fields accessed from native code
 *    - Methods called from native code
 * 
 * 4️⃣ COMMON USE CASES:
 *    - System libraries (libc, Windows API)
 *    - Hardware interfaces
 *    - Performance-critical operations
 *    - Legacy native code integration
 *    - Platform-specific functionality
 * 
 * 📦 DEPENDENCIES:
 * ===============
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-web</artifactId>
 * </dependency>
 * 
 * 🔧 JNI CONFIG API:
 * ==================
 * 
 * Register Class with Native Methods:
 * -----------------------------------
 * hints.jni().registerType(
 *     MyNativeClass.class,
 *     MemberCategory.INVOKE_DECLARED_METHODS
 * );
 * 
 * Register Specific Native Method:
 * ---------------------------------
 * Method method = MyNativeClass.class
 *     .getDeclaredMethod("nativeMethod", String.class);
 * hints.jni().registerMethod(method, ExecutableMode.INVOKE);
 * 
 * Register Field Accessed from Native:
 * -------------------------------------
 * Field field = MyClass.class.getDeclaredField("nativeField");
 * hints.jni().registerField(field);
 * 
 * Register Constructor:
 * ---------------------
 * Constructor<?> constructor = MyClass.class
 *     .getDeclaredConstructor(String.class);
 * hints.jni().registerConstructor(constructor, ExecutableMode.INVOKE);
 * 
 * 🎯 JNI PATTERNS:
 * ===============
 * 
 * Native Method Declaration:
 * --------------------------
 * public class NativeLib {
 *     static {
 *         System.loadLibrary("mylib");
 *     }
 *     
 *     public native String nativeMethod(String input);
 *     public native int computeNative(int a, int b);
 * }
 * 
 * C/C++ Implementation:
 * ---------------------
 * JNIEXPORT jstring JNICALL Java_NativeLib_nativeMethod
 *   (JNIEnv *env, jobject obj, jstring input) {
 *     // Native implementation
 * }
 * 
 * Registration:
 * -------------
 * hints.jni().registerType(
 *     NativeLib.class,
 *     MemberCategory.INVOKE_DECLARED_METHODS
 * );
 * 
 * 💡 WHEN TO USE JNI CONFIG:
 * =========================
 * ✅ Native library integration
 * ✅ System-level operations
 * ✅ Hardware access
 * ✅ Performance-critical code
 * ✅ Legacy native libraries
 * ✅ Platform-specific features
 * ✅ Graphics/multimedia processing
 * 
 * ❌ ALTERNATIVES:
 * ===============
 * ❌ Use pure Java implementations
 * ❌ Use Java Foreign Function & Memory API (JEP 424)
 * ❌ Use Project Panama
 * ❌ Containerize native dependencies
 * ❌ Use platform-agnostic solutions
 * 
 * ⚠️ LIMITATIONS:
 * ==============
 * ⚠️ Platform-specific binaries required
 * ⚠️ Increased complexity
 * ⚠️ Security concerns
 * ⚠️ Debugging difficulty
 * ⚠️ Distribution challenges
 * ⚠️ Native library version management
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
@ImportRuntimeHints(JniConfigPattern.JniConfigHints.class)
public class JniConfigPattern {

    public static void main(String[] args) {
        SpringApplication.run(JniConfigPattern.class, args);
    }

    /**
     * JNI Configuration Hints
     */
    static class JniConfigHints implements RuntimeHintsRegistrar {
        
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // 1. Register classes with native methods
            registerNativeMethodClasses(hints);
            
            // 2. Register classes accessed from native code
            registerNativeAccessedClasses(hints);
            
            // 3. Register fields accessed from native code
            registerNativeAccessedFields(hints);
            
            // 4. Register common JNI types
            registerCommonJniTypes(hints);
            
            System.out.println("✅ JNI configuration registered successfully");
        }

        private void registerNativeMethodClasses(RuntimeHints hints) {
            // Register class with native methods
            hints.jni().registerType(
                NativeLibraryWrapper.class,
                builder -> builder
                    .withMembers(MemberCategory.INVOKE_DECLARED_METHODS)
            );
        }

        private void registerNativeAccessedClasses(RuntimeHints hints) {
            // Register classes that may be accessed from native code
            hints.jni().registerType(
                NativeCallback.class,
                builder -> builder
                    .withMembers(
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.DECLARED_FIELDS
                    )
            );

            hints.jni().registerType(
                NativeDataStructure.class,
                builder -> builder
                    .withMembers(
                        MemberCategory.DECLARED_FIELDS
                    )
            );
        }

        private void registerNativeAccessedFields(RuntimeHints hints) {
            try {
                // Register specific fields accessed from native code
                hints.jni().registerField(
                    NativeDataStructure.class.getDeclaredField("nativePointer")
                );
                hints.jni().registerField(
                    NativeDataStructure.class.getDeclaredField("data")
                );
            } catch (NoSuchFieldException e) {
                System.err.println("⚠️ Field not found for JNI registration: " + e.getMessage());
            }
        }

        private void registerCommonJniTypes(RuntimeHints hints) {
            // Register common Java types used in JNI
            hints.jni().registerType(
                String.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS
            );
            hints.jni().registerType(
                java.nio.ByteBuffer.class,
                MemberCategory.INVOKE_DECLARED_METHODS
            );
        }
    }
}

// ============================================================================
// NATIVE METHOD CLASSES
// ============================================================================

/**
 * Native Library Wrapper
 * Contains native method declarations
 */
class NativeLibraryWrapper {
    
    // Load native library (would be actual .so/.dll file)
    static {
        try {
            // In real scenario: System.loadLibrary("mynativelib");
            System.out.println("Native library loading simulated (not actually loaded)");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("⚠️ Native library not found: " + e.getMessage());
        }
    }

    /**
     * Native method declaration (would be implemented in C/C++)
     */
    public native String processNative(String input);

    /**
     * Native computation method
     */
    public native int computeNative(int a, int b);

    /**
     * Native system call
     */
    public native long getSystemInfo();

    /**
     * Mock implementation for demonstration
     * (In real scenario, these would be implemented in native code)
     */
    public String processNativeMock(String input) {
        return "Processed (mock): " + input;
    }

    public int computeNativeMock(int a, int b) {
        return a + b; // Mock computation
    }

    public long getSystemInfoMock() {
        return System.currentTimeMillis();
    }
}

/**
 * Native Callback
 * Called from native code back to Java
 */
class NativeCallback {
    private String callbackData;

    public NativeCallback() {}

    public NativeCallback(String data) {
        this.callbackData = data;
    }

    /**
     * Method called from native code
     */
    public void onNativeEvent(String eventType, String eventData) {
        System.out.println("Native event received: " + eventType + " - " + eventData);
        this.callbackData = eventData;
    }

    /**
     * Method to retrieve data set by native code
     */
    public String getCallbackData() {
        return callbackData;
    }

    public void setCallbackData(String callbackData) {
        this.callbackData = callbackData;
    }
}

/**
 * Native Data Structure
 * Fields accessed directly from native code
 */
class NativeDataStructure {
    
    /**
     * Pointer to native memory (accessed from native code)
     */
    private long nativePointer;

    /**
     * Data field accessed from native code
     */
    public byte[] data;

    /**
     * Status flag accessed from native code
     */
    public int status;

    public NativeDataStructure() {
        this.data = new byte[1024];
        this.status = 0;
    }

    public long getNativePointer() {
        return nativePointer;
    }

    public void setNativePointer(long nativePointer) {
        this.nativePointer = nativePointer;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

// ============================================================================
// SERVICES
// ============================================================================

/**
 * JNI Configuration Service
 */
@Service
class JniConfigService {

    /**
     * Get JNI configuration summary
     */
    public Map<String, Object> getJniConfiguration() {
        Map<String, Object> config = new LinkedHashMap<>();
        
        List<String> nativeClasses = new ArrayList<>();
        nativeClasses.add("NativeLibraryWrapper - Contains native methods");
        nativeClasses.add("NativeCallback - Called from native code");
        nativeClasses.add("NativeDataStructure - Fields accessed from native");
        config.put("registeredClasses", nativeClasses);
        
        List<String> nativeMethods = new ArrayList<>();
        nativeMethods.add("processNative(String) - Process data in native code");
        nativeMethods.add("computeNative(int, int) - Native computation");
        nativeMethods.add("getSystemInfo() - Get system information");
        config.put("nativeMethods", nativeMethods);
        
        List<String> callbacks = new ArrayList<>();
        callbacks.add("NativeCallback.onNativeEvent() - Receive native events");
        config.put("callbacks", callbacks);
        
        return config;
    }

    /**
     * Get JNI use cases
     */
    public List<String> getJniUseCases() {
        List<String> useCases = new ArrayList<>();
        
        useCases.add("✅ System library integration (libc, Windows API)");
        useCases.add("✅ Hardware access (sensors, GPIO)");
        useCases.add("✅ Performance-critical operations");
        useCases.add("✅ Graphics/multimedia processing");
        useCases.add("✅ Encryption/cryptography libraries");
        useCases.add("✅ Database drivers (native)");
        useCases.add("✅ Legacy native code integration");
        useCases.add("✅ Platform-specific features");
        
        return useCases;
    }

    /**
     * Get JNI best practices
     */
    public List<String> getJniBestPractices() {
        List<String> practices = new ArrayList<>();
        
        practices.add("✅ Register all native methods and classes");
        practices.add("✅ Test JNI thoroughly in native image");
        practices.add("✅ Handle UnsatisfiedLinkError gracefully");
        practices.add("✅ Document native dependencies");
        practices.add("✅ Use try-catch for native method calls");
        practices.add("✅ Validate native library availability");
        practices.add("⚠️ Minimize JNI usage (performance overhead)");
        practices.add("⚠️ Avoid complex data structures in JNI");
        practices.add("⚠️ Be careful with memory management");
        practices.add("⚠️ Platform-specific binaries required");
        practices.add("💡 Consider Java Foreign Function & Memory API");
        
        return practices;
    }

    /**
     * Get JNI limitations
     */
    public List<String> getJniLimitations() {
        List<String> limitations = new ArrayList<>();
        
        limitations.add("⚠️ Platform-specific: Separate binaries per OS");
        limitations.add("⚠️ Complexity: C/C++ knowledge required");
        limitations.add("⚠️ Security: Native code bypasses Java security");
        limitations.add("⚠️ Debugging: Harder to debug native crashes");
        limitations.add("⚠️ Distribution: Must bundle native libraries");
        limitations.add("⚠️ Versioning: Native library compatibility");
        limitations.add("⚠️ Performance: JNI calls have overhead");
        limitations.add("⚠️ Portability: Reduced Java portability");
        
        return limitations;
    }
}

/**
 * JNI Test Service
 */
@Service
class JniTestService {

    private final NativeLibraryWrapper nativeLib = new NativeLibraryWrapper();

    /**
     * Test native method call (mock)
     */
    public Map<String, Object> testNativeMethodCall() {
        try {
            // Use mock implementation for demonstration
            String result = nativeLib.processNativeMock("test-data");
            
            return Map.of(
                "success", true,
                "input", "test-data",
                "output", result,
                "note", "Using mock implementation (native library not loaded)"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }

    /**
     * Test native computation (mock)
     */
    public Map<String, Object> testNativeComputation() {
        try {
            int result = nativeLib.computeNativeMock(42, 58);
            
            return Map.of(
                "success", true,
                "operation", "42 + 58",
                "result", result,
                "note", "Using mock implementation"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }

    /**
     * Test native callback
     */
    public Map<String, Object> testNativeCallback() {
        try {
            NativeCallback callback = new NativeCallback("initial-data");
            
            // Simulate native code calling back to Java
            callback.onNativeEvent("TEST_EVENT", "callback-data");
            
            return Map.of(
                "success", true,
                "callbackData", callback.getCallbackData(),
                "note", "Callback simulation successful"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }
}

/**
 * JNI Configuration REST Controller
 */
@RestController
@RequestMapping("/api/jni-config")
class JniConfigController {

    private final JniConfigService jniConfigService;
    private final JniTestService jniTestService;

    public JniConfigController(JniConfigService jniConfigService,
                                JniTestService jniTestService) {
        this.jniConfigService = jniConfigService;
        this.jniTestService = jniTestService;
    }

    /**
     * GET /api/jni-config/configuration
     * Get JNI configuration summary
     */
    @GetMapping("/configuration")
    public Map<String, Object> getConfiguration() {
        return jniConfigService.getJniConfiguration();
    }

    /**
     * GET /api/jni-config/use-cases
     * Get JNI use cases
     */
    @GetMapping("/use-cases")
    public List<String> getUseCases() {
        return jniConfigService.getJniUseCases();
    }

    /**
     * GET /api/jni-config/best-practices
     * Get JNI best practices
     */
    @GetMapping("/best-practices")
    public List<String> getBestPractices() {
        return jniConfigService.getJniBestPractices();
    }

    /**
     * GET /api/jni-config/limitations
     * Get JNI limitations
     */
    @GetMapping("/limitations")
    public List<String> getLimitations() {
        return jniConfigService.getJniLimitations();
    }

    /**
     * GET /api/jni-config/test/native-method
     * Test native method call
     */
    @GetMapping("/test/native-method")
    public Map<String, Object> testNativeMethod() {
        return jniTestService.testNativeMethodCall();
    }

    /**
     * GET /api/jni-config/test/computation
     * Test native computation
     */
    @GetMapping("/test/computation")
    public Map<String, Object> testComputation() {
        return jniTestService.testNativeComputation();
    }

    /**
     * GET /api/jni-config/test/callback
     * Test native callback
     */
    @GetMapping("/test/callback")
    public Map<String, Object> testCallback() {
        return jniTestService.testNativeCallback();
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ GET JNI CONFIGURATION:
 * --------------------------
 * curl http://localhost:8080/api/jni-config/configuration
 * 
 * Response:
 * {
 *   "registeredClasses": [
 *     "NativeLibraryWrapper - Contains native methods",
 *     "NativeCallback - Called from native code",
 *     "NativeDataStructure - Fields accessed from native"
 *   ],
 *   "nativeMethods": [...],
 *   "callbacks": [...]
 * }
 * 
 * 2️⃣ TEST NATIVE METHOD CALL:
 * ----------------------------
 * curl http://localhost:8080/api/jni-config/test/native-method
 * 
 * Response:
 * {
 *   "success": true,
 *   "input": "test-data",
 *   "output": "Processed (mock): test-data",
 *   "note": "Using mock implementation (native library not loaded)"
 * }
 * 
 * 3️⃣ REGISTER JNI CLASS:
 * -----------------------
 * public class MyJniHints implements RuntimeHintsRegistrar {
 *     @Override
 *     public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
 *         // Register class with native methods
 *         hints.jni().registerType(
 *             MyNativeClass.class,
 *             MemberCategory.INVOKE_DECLARED_METHODS
 *         );
 *         
 *         // Register specific native method
 *         try {
 *             Method method = MyNativeClass.class
 *                 .getDeclaredMethod("nativeMethod", String.class);
 *             hints.jni().registerMethod(method, ExecutableMode.INVOKE);
 *         } catch (NoSuchMethodException e) {
 *             // Handle error
 *         }
 *     }
 * }
 * 
 * 4️⃣ NATIVE METHOD DECLARATION:
 * ------------------------------
 * public class MyNativeLib {
 *     static {
 *         System.loadLibrary("mynativelib");
 *     }
 *     
 *     public native String processData(String input);
 *     public native int compute(int a, int b);
 * }
 * 
 * 5️⃣ C IMPLEMENTATION (mynativelib.c):
 * -------------------------------------
 * #include <jni.h>
 * #include "MyNativeLib.h"
 * 
 * JNIEXPORT jstring JNICALL Java_MyNativeLib_processData
 *   (JNIEnv *env, jobject obj, jstring input) {
 *     const char *str = (*env)->GetStringUTFChars(env, input, 0);
 *     // Process data
 *     (*env)->ReleaseStringUTFChars(env, input, str);
 *     return (*env)->NewStringUTF(env, "processed");
 * }
 */
