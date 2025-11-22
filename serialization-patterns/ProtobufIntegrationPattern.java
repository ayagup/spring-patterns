package com.example.serialization;

import com.google.protobuf.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.time.Instant;

/**
 * Protobuf Integration Pattern
 * 
 * Demonstrates Protocol Buffers (Protobuf) serialization integration.
 * 
 * Key Concepts:
 * 1. Protocol Buffers - Google's language-neutral serialization
 * 2. Binary serialization - Compact and efficient
 * 3. Schema definition (.proto files)
 * 4. Code generation from .proto files
 * 5. Forward and backward compatibility
 * 6. Type safety with generated classes
 * 7. Parsing and building messages
 * 8. Nested messages
 * 9. Repeated fields (collections)
 * 10. Performance benefits over JSON/XML
 * 
 * Use Cases:
 * - High-performance microservices
 * - gRPC services
 * - Inter-service communication
 * - Mobile applications
 * - IoT data transmission
 * - Real-time data streaming
 * 
 * Setup:
 * 1. Define .proto schema files
 * 2. Generate Java classes using protoc compiler
 * 3. Use generated classes for serialization
 * 
 * Proto File Example:
 * ```
 * syntax = "proto3";
 * 
 * message User {
 *   int64 id = 1;
 *   string name = 2;
 *   string email = 3;
 *   int32 age = 4;
 * }
 * ```
 * 
 * Benefits:
 * - 3-10x smaller than JSON
 * - 20-100x faster than XML
 * - Strongly typed
 * - Language agnostic
 * - Built-in versioning
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class ProtobufIntegrationPattern {

    public static void main(String[] args) {
        SpringApplication.run(ProtobufIntegrationPattern.class, args);
        demonstrateProtobufSerialization();
    }
    
    private static void demonstrateProtobufSerialization() {
        System.out.println("=== Protobuf Integration Pattern Demo ===\n");
        
        // NOTE: This demonstrates the pattern. In real applications,
        // you would use protoc-generated classes.
        
        System.out.println("Protobuf Serialization Pattern Overview:\n");
        
        System.out.println("1. PROTO FILE DEFINITION:");
        System.out.println("----------------------------");
        System.out.println("syntax = \"proto3\";");
        System.out.println();
        System.out.println("package com.example.proto;");
        System.out.println();
        System.out.println("message User {");
        System.out.println("  int64 id = 1;");
        System.out.println("  string name = 2;");
        System.out.println("  string email = 3;");
        System.out.println("  int32 age = 4;");
        System.out.println("  repeated string roles = 5;");
        System.out.println("}");
        System.out.println();
        
        System.out.println("2. COMPILE PROTO FILE:");
        System.out.println("----------------------------");
        System.out.println("protoc --java_out=src/main/java user.proto");
        System.out.println();
        
        System.out.println("3. USAGE IN CODE:");
        System.out.println("----------------------------");
        System.out.println("// Create a User message");
        System.out.println("UserProto.User user = UserProto.User.newBuilder()");
        System.out.println("    .setId(1)");
        System.out.println("    .setName(\"John Doe\")");
        System.out.println("    .setEmail(\"john@example.com\")");
        System.out.println("    .setAge(30)");
        System.out.println("    .addRoles(\"ADMIN\")");
        System.out.println("    .addRoles(\"USER\")");
        System.out.println("    .build();");
        System.out.println();
        
        System.out.println("4. SERIALIZATION:");
        System.out.println("----------------------------");
        System.out.println("// Serialize to bytes");
        System.out.println("byte[] bytes = user.toByteArray();");
        System.out.println();
        System.out.println("// Serialize to OutputStream");
        System.out.println("user.writeTo(outputStream);");
        System.out.println();
        System.out.println("// Get JSON representation");
        System.out.println("String json = JsonFormat.printer().print(user);");
        System.out.println();
        
        System.out.println("5. DESERIALIZATION:");
        System.out.println("----------------------------");
        System.out.println("// Deserialize from bytes");
        System.out.println("UserProto.User parsedUser = UserProto.User.parseFrom(bytes);");
        System.out.println();
        System.out.println("// Deserialize from InputStream");
        System.out.println("UserProto.User user2 = UserProto.User.parseFrom(inputStream);");
        System.out.println();
        
        System.out.println("6. NESTED MESSAGES:");
        System.out.println("----------------------------");
        System.out.println("message Address {");
        System.out.println("  string street = 1;");
        System.out.println("  string city = 2;");
        System.out.println("  string country = 3;");
        System.out.println("}");
        System.out.println();
        System.out.println("message Customer {");
        System.out.println("  int64 id = 1;");
        System.out.println("  string name = 2;");
        System.out.println("  Address address = 3;  // Nested message");
        System.out.println("}");
        System.out.println();
        
        System.out.println("7. ONEOF (UNION TYPES):");
        System.out.println("----------------------------");
        System.out.println("message Payment {");
        System.out.println("  oneof payment_method {");
        System.out.println("    CreditCard credit_card = 1;");
        System.out.println("    BankAccount bank_account = 2;");
        System.out.println("    string paypal_email = 3;");
        System.out.println("  }");
        System.out.println("}");
        System.out.println();
        
        System.out.println("8. ENUMS:");
        System.out.println("----------------------------");
        System.out.println("enum Status {");
        System.out.println("  UNKNOWN = 0;  // First value must be 0");
        System.out.println("  ACTIVE = 1;");
        System.out.println("  INACTIVE = 2;");
        System.out.println("  SUSPENDED = 3;");
        System.out.println("}");
        System.out.println();
        
        System.out.println("9. MAPS:");
        System.out.println("----------------------------");
        System.out.println("message UserPreferences {");
        System.out.println("  map<string, string> settings = 1;");
        System.out.println("  map<string, int32> counters = 2;");
        System.out.println("}");
        System.out.println();
        
        System.out.println("10. COMPARISON - JSON vs PROTOBUF:");
        System.out.println("----------------------------");
        System.out.println("JSON (150 bytes):");
        System.out.println("{");
        System.out.println("  \"id\": 1,");
        System.out.println("  \"name\": \"John Doe\",");
        System.out.println("  \"email\": \"john@example.com\",");
        System.out.println("  \"age\": 30");
        System.out.println("}");
        System.out.println();
        System.out.println("Protobuf (40-50 bytes):");
        System.out.println("[Binary representation - 3x smaller]");
        System.out.println();
        
        System.out.println("11. ADVANTAGES:");
        System.out.println("----------------------------");
        System.out.println("✓ Compact binary format (3-10x smaller than JSON)");
        System.out.println("✓ Fast serialization/deserialization (20-100x faster than XML)");
        System.out.println("✓ Strongly typed with generated code");
        System.out.println("✓ Language agnostic (Java, C++, Python, Go, etc.)");
        System.out.println("✓ Built-in backward/forward compatibility");
        System.out.println("✓ Schema validation");
        System.out.println("✓ Excellent for microservices & gRPC");
        System.out.println();
        
        System.out.println("12. DISADVANTAGES:");
        System.out.println("----------------------------");
        System.out.println("✗ Not human-readable (binary format)");
        System.out.println("✗ Requires code generation step");
        System.out.println("✗ Schema evolution requires planning");
        System.out.println("✗ Less tooling support than JSON");
        System.out.println("✗ Debugging more complex");
        System.out.println();
        
        System.out.println("13. SPRING BOOT INTEGRATION:");
        System.out.println("----------------------------");
        System.out.println("dependencies {");
        System.out.println("    implementation 'com.google.protobuf:protobuf-java:3.24.0'");
        System.out.println("    implementation 'io.grpc:grpc-protobuf:1.58.0'");
        System.out.println("    implementation 'io.grpc:grpc-stub:1.58.0'");
        System.out.println("}");
        System.out.println();
        
        System.out.println("14. GRPC SERVICE EXAMPLE:");
        System.out.println("----------------------------");
        System.out.println("service UserService {");
        System.out.println("  rpc GetUser(UserRequest) returns (UserResponse);");
        System.out.println("  rpc CreateUser(CreateUserRequest) returns (UserResponse);");
        System.out.println("  rpc ListUsers(ListUsersRequest) returns (stream UserResponse);");
        System.out.println("}");
        System.out.println();
        
        System.out.println("15. BEST PRACTICES:");
        System.out.println("----------------------------");
        System.out.println("1. Never change field numbers");
        System.out.println("2. Always use optional for nullable fields");
        System.out.println("3. Reserve removed field numbers");
        System.out.println("4. Use meaningful field names");
        System.out.println("5. Version your proto files");
        System.out.println("6. Use enums for fixed sets of values");
        System.out.println("7. Avoid deeply nested messages");
        System.out.println("8. Use oneof for mutually exclusive fields");
        System.out.println();
    }
}

/**
 * Simulated Protobuf User Message
 * In real applications, this would be generated from .proto file
 */
class UserProtoExample {
    private long id;
    private String name;
    private String email;
    private int age;
    
    // Builder pattern as used by Protobuf
    public static class Builder {
        private long id;
        private String name;
        private String email;
        private int age;
        
        public Builder setId(long id) {
            this.id = id;
            return this;
        }
        
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        
        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }
        
        public Builder setAge(int age) {
            this.age = age;
            return this;
        }
        
        public UserProtoExample build() {
            UserProtoExample user = new UserProtoExample();
            user.id = this.id;
            user.name = this.name;
            user.email = this.email;
            user.age = this.age;
            return user;
        }
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    // Simulated serialization
    public byte[] toByteArray() {
        // In real Protobuf, this would return binary serialized data
        return new byte[0];
    }
    
    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
}

/**
 * Proto Schema Documentation
 */
class ProtoSchemas {
    
    public static final String USER_PROTO = """
        syntax = "proto3";
        
        package com.example.serialization;
        
        option java_package = "com.example.serialization.proto";
        option java_outer_classname = "UserProto";
        
        message User {
          int64 id = 1;
          string name = 2;
          string email = 3;
          int32 age = 4;
          repeated string roles = 5;
          google.protobuf.Timestamp created_at = 6;
        }
        
        message UserRequest {
          int64 user_id = 1;
        }
        
        message UserResponse {
          User user = 1;
          bool success = 2;
          string message = 3;
        }
        """;
    
    public static final String ORDER_PROTO = """
        syntax = "proto3";
        
        package com.example.serialization;
        
        message Order {
          int64 order_id = 1;
          string order_number = 2;
          int64 customer_id = 3;
          repeated OrderItem items = 4;
          double total_amount = 5;
          OrderStatus status = 6;
          google.protobuf.Timestamp order_date = 7;
        }
        
        message OrderItem {
          int64 item_id = 1;
          string product_name = 2;
          int32 quantity = 3;
          double price = 4;
        }
        
        enum OrderStatus {
          ORDER_STATUS_UNKNOWN = 0;
          ORDER_STATUS_PENDING = 1;
          ORDER_STATUS_CONFIRMED = 2;
          ORDER_STATUS_SHIPPED = 3;
          ORDER_STATUS_DELIVERED = 4;
          ORDER_STATUS_CANCELLED = 5;
        }
        """;
    
    public static final String PAYMENT_PROTO = """
        syntax = "proto3";
        
        package com.example.serialization;
        
        message Payment {
          int64 payment_id = 1;
          int64 order_id = 2;
          double amount = 3;
          
          oneof payment_method {
            CreditCardPayment credit_card = 4;
            BankTransfer bank_transfer = 5;
            string paypal_email = 6;
          }
          
          PaymentStatus status = 7;
          google.protobuf.Timestamp payment_date = 8;
        }
        
        message CreditCardPayment {
          string card_number = 1;
          string cardholder_name = 2;
          string expiry_date = 3;
        }
        
        message BankTransfer {
          string account_number = 1;
          string routing_number = 2;
          string bank_name = 3;
        }
        
        enum PaymentStatus {
          PAYMENT_STATUS_UNKNOWN = 0;
          PAYMENT_STATUS_PENDING = 1;
          PAYMENT_STATUS_COMPLETED = 2;
          PAYMENT_STATUS_FAILED = 3;
          PAYMENT_STATUS_REFUNDED = 4;
        }
        """;
}

/**
 * REST Controller demonstrating Protobuf pattern
 */
@RestController
@RequestMapping("/api/protobuf")
class ProtobufIntegrationController {
    
    @GetMapping("/schema/user")
    public String getUserProtoSchema() {
        return ProtoSchemas.USER_PROTO;
    }
    
    @GetMapping("/schema/order")
    public String getOrderProtoSchema() {
        return ProtoSchemas.ORDER_PROTO;
    }
    
    @GetMapping("/schema/payment")
    public String getPaymentProtoSchema() {
        return ProtoSchemas.PAYMENT_PROTO;
    }
    
    @GetMapping("/info")
    public ProtobufInfo getProtobufInfo() {
        return new ProtobufInfo();
    }
}

/**
 * Protobuf Information
 */
class ProtobufInfo {
    private String version = "3.24.0";
    private String description = "Protocol Buffers - Google's data interchange format";
    private List<String> advantages = List.of(
        "Compact binary format",
        "Fast serialization/deserialization",
        "Strongly typed",
        "Language agnostic",
        "Built-in versioning"
    );
    private List<String> useCases = List.of(
        "gRPC services",
        "Microservices communication",
        "Mobile applications",
        "IoT data transmission",
        "Real-time streaming"
    );
    
    // Getters
    public String getVersion() { return version; }
    public String getDescription() { return description; }
    public List<String> getAdvantages() { return advantages; }
    public List<String> getUseCases() { return useCases; }
}
