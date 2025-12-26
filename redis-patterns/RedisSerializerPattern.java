package com.example.redis.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Redis Serializer Pattern
 * 
 * Demonstrates different Redis serialization strategies.
 * Redis Serializers provide:
 * - Multiple serialization formats (JSON, XML, Java, Protobuf)
 * - Custom serialization logic
 * - Type-safe serialization
 * - Compression support
 * - Interoperability with other systems
 * 
 * Use cases:
 * - JSON serialization for human-readable data
 * - Java serialization for Java-specific objects
 * - Custom serialization for performance optimization
 * - Cross-language data exchange
 * - Binary serialization for space efficiency
 */

@Configuration
class RedisSerializerConfig {
    
    // JSON Serializer with Jackson
    @Bean
    public RedisTemplate<String, Object> jsonRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Configure ObjectMapper for Java 8 date/time support
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        GenericJackson2JsonRedisSerializer jsonSerializer = 
            new GenericJackson2JsonRedisSerializer(objectMapper);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonSerializer);
        
        return template;
    }
    
    // String Serializer
    @Bean
    public RedisTemplate<String, String> stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        return template;
    }
    
    // JDK Serialization (for Java objects)
    @Bean
    public RedisTemplate<String, Object> jdkRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new JdkSerializationRedisSerializer());
        
        return template;
    }
    
    // Byte Array Serializer (for binary data)
    @Bean
    public RedisTemplate<String, byte[]> byteArrayRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new ByteArrayRedisSerializer());
        
        return template;
    }
}

record Customer(
    String id, 
    String name, 
    String email, 
    int age, 
    LocalDateTime registeredAt
) implements java.io.Serializable {}

@Service
class RedisSerializerService {
    
    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final RedisTemplate<String, Object> jdkRedisTemplate;
    private final RedisTemplate<String, byte[]> byteArrayRedisTemplate;
    
    public RedisSerializerService(
            RedisTemplate<String, Object> jsonRedisTemplate,
            RedisTemplate<String, String> stringRedisTemplate,
            RedisTemplate<String, Object> jdkRedisTemplate,
            RedisTemplate<String, byte[]> byteArrayRedisTemplate) {
        this.jsonRedisTemplate = jsonRedisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.jdkRedisTemplate = jdkRedisTemplate;
        this.byteArrayRedisTemplate = byteArrayRedisTemplate;
    }
    
    // JSON Serialization
    public void saveWithJson(String key, Customer customer) {
        jsonRedisTemplate.opsForValue().set("json:" + key, customer);
    }
    
    public Customer getWithJson(String key) {
        Object value = jsonRedisTemplate.opsForValue().get("json:" + key);
        return (Customer) value;
    }
    
    // String Serialization
    public void saveWithString(String key, String value) {
        stringRedisTemplate.opsForValue().set("string:" + key, value);
    }
    
    public String getWithString(String key) {
        return stringRedisTemplate.opsForValue().get("string:" + key);
    }
    
    // JDK Serialization
    public void saveWithJdk(String key, Customer customer) {
        jdkRedisTemplate.opsForValue().set("jdk:" + key, customer);
    }
    
    public Customer getWithJdk(String key) {
        Object value = jdkRedisTemplate.opsForValue().get("jdk:" + key);
        return (Customer) value;
    }
    
    // Byte Array Serialization
    public void saveWithByteArray(String key, byte[] data) {
        byteArrayRedisTemplate.opsForValue().set("bytes:" + key, data);
    }
    
    public byte[] getWithByteArray(String key) {
        return byteArrayRedisTemplate.opsForValue().get("bytes:" + key);
    }
    
    // Comparison methods
    public SerializationComparison compareSerializations(Customer customer) {
        String key = "test";
        
        // JSON
        saveWithJson(key, customer);
        long jsonSize = getValueSize("json:" + key);
        
        // JDK
        saveWithJdk(key, customer);
        long jdkSize = getValueSize("jdk:" + key);
        
        return new SerializationComparison(
            "JSON: " + jsonSize + " bytes",
            "JDK: " + jdkSize + " bytes",
            jsonSize < jdkSize ? "JSON is smaller" : "JDK is smaller"
        );
    }
    
    private long getValueSize(String key) {
        // This is a simplified estimation
        // In practice, you'd use Redis STRLEN or MEMORY USAGE commands
        Object value = jsonRedisTemplate.opsForValue().get(key);
        return value != null ? value.toString().length() : 0;
    }
}

record SerializationComparison(String jsonSize, String jdkSize, String comparison) {}

@RestController
@RequestMapping("/api/redis/serializer")
class RedisSerializerController {
    
    private final RedisSerializerService serializerService;
    
    public RedisSerializerController(RedisSerializerService serializerService) {
        this.serializerService = serializerService;
    }
    
    @PostMapping("/json")
    public String saveWithJson(@RequestParam String key, @RequestBody Customer customer) {
        serializerService.saveWithJson(key, customer);
        return "Saved with JSON serialization";
    }
    
    @GetMapping("/json/{key}")
    public Customer getWithJson(@PathVariable String key) {
        return serializerService.getWithJson(key);
    }
    
    @PostMapping("/string")
    public String saveWithString(@RequestParam String key, @RequestParam String value) {
        serializerService.saveWithString(key, value);
        return "Saved with String serialization";
    }
    
    @GetMapping("/string/{key}")
    public String getWithString(@PathVariable String key) {
        return serializerService.getWithString(key);
    }
    
    @PostMapping("/jdk")
    public String saveWithJdk(@RequestParam String key, @RequestBody Customer customer) {
        serializerService.saveWithJdk(key, customer);
        return "Saved with JDK serialization";
    }
    
    @GetMapping("/jdk/{key}")
    public Customer getWithJdk(@PathVariable String key) {
        return serializerService.getWithJdk(key);
    }
    
    @PostMapping("/bytes")
    public String saveWithByteArray(@RequestParam String key, @RequestBody byte[] data) {
        serializerService.saveWithByteArray(key, data);
        return "Saved with Byte Array serialization";
    }
    
    @GetMapping("/bytes/{key}")
    public byte[] getWithByteArray(@PathVariable String key) {
        return serializerService.getWithByteArray(key);
    }
    
    @PostMapping("/compare")
    public SerializationComparison compareSerializations(@RequestBody Customer customer) {
        return serializerService.compareSerializations(customer);
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return """
                Redis Serializer Pattern
                =======================
                Available Serializers:
                
                1. GenericJackson2JsonRedisSerializer
                   - JSON format
                   - Human-readable
                   - Type information included
                   - Good for cross-language compatibility
                   - Larger size but readable
                
                2. StringRedisSerializer
                   - Simple string serialization
                   - Most efficient for strings
                   - No type information
                   - Interoperable with all Redis clients
                
                3. JdkSerializationRedisSerializer
                   - Java native serialization
                   - Preserves full object graph
                   - Binary format
                   - Java-specific (not readable by other languages)
                   - Compact but not human-readable
                
                4. ByteArrayRedisSerializer
                   - Raw byte array storage
                   - Most compact
                   - No conversion overhead
                   - For binary data (images, files, etc.)
                
                5. OxmSerializer (not shown)
                   - XML serialization
                   - For XML-based data exchange
                
                Recommendations:
                - Use JSON for APIs and cross-language compatibility
                - Use String for simple text data
                - Use JDK for complex Java objects (internal use)
                - Use ByteArray for binary data
                
                Configuration:
                - Key serializer: Usually StringRedisSerializer
                - Value serializer: Based on use case
                - Hash key/value: Can be different from main serializers
                """;
    }
}
