package com.example.testing;

import org.junit.jupiter.api.Test;

/**
 * JSON Path Pattern
 * ==================
 * 
 * Demonstrates the JSONPath pattern for validating JSON responses
 * in REST API tests using path expressions.
 * 
 * Use Cases:
 * ----------
 * 1. Validate JSON response structure
 * 2. Extract nested values
 * 3. Assert array elements
 * 4. Test dynamic JSON content
 * 5. Validate partial responses
 * 6. Test API contracts
 * 7. Verify field existence
 * 8. Compare JSON values
 * 
 * Key Features:
 * -------------
 * - XPath-like syntax for JSON
 * - Navigate nested structures
 * - Filter array elements
 * - Type-safe value extraction
 * - Wildcard support
 * - Recursive descent
 * - Array indexing
 * - Integration with MockMvc
 * 
 * JSONPath Syntax:
 * ----------------
 * $ - Root element
 * . - Child element
 * .. - Recursive descent
 * * - Wildcard
 * [] - Array indexing
 * [?(@.condition)] - Filter expression
 * 
 * Common Expressions:
 * -------------------
 * $.name - Root level property
 * $.user.email - Nested property
 * $.users[0] - First array element
 * $.users[*].name - All user names
 * $..email - All email fields (recursive)
 * $.users[?(@.age > 18)] - Filtered elements
 * 
 * @author Spring Patterns
 * @version 1.0
 */
public class JsonPathPattern {
    
    @Test
    void demonstrateJsonPath() {
        System.out.println("\n=== JSON Path Pattern ===");
        
        System.out.println("\nBasic Path Expressions:");
        System.out.println("  $.name - Root property");
        System.out.println("  $.user.email - Nested property");
        System.out.println("  $.address.city - Deep nesting");
        
        System.out.println("\nArray Access:");
        System.out.println("  $.users[0] - First element");
        System.out.println("  $.users[0].name - Property of first element");
        System.out.println("  $.users[-1] - Last element");
        System.out.println("  $.users[0:3] - Slice (elements 0-2)");
        
        System.out.println("\nWildcard and Recursive:");
        System.out.println("  $.users[*].name - All user names");
        System.out.println("  $..email - All email fields anywhere");
        System.out.println("  $..* - All values recursively");
        
        System.out.println("\nFilter Expressions:");
        System.out.println("  $.users[?(@.age > 18)] - Adults only");
        System.out.println("  $.users[?(@.active == true)] - Active users");
        System.out.println("  $.products[?(@.price < 100)] - Cheap products");
        
        System.out.println("\nWith MockMvc:");
        System.out.println("  mockMvc.perform(get(\"/api/user\"))");
        System.out.println("      .andExpect(status().isOk())");
        System.out.println("      .andExpect(jsonPath(\"$.name\").value(\"John\"))");
        System.out.println("      .andExpect(jsonPath(\"$.email\").exists())");
        System.out.println("      .andExpect(jsonPath(\"$.age\").isNumber())");
        System.out.println("      .andExpect(jsonPath(\"$.active\").isBoolean());");
        
        System.out.println("\nArray Assertions:");
        System.out.println("  .andExpect(jsonPath(\"$.users\").isArray())");
        System.out.println("  .andExpect(jsonPath(\"$.users\", hasSize(3)))");
        System.out.println("  .andExpect(jsonPath(\"$.users[0].name\", is(\"John\")))");
        System.out.println("  .andExpect(jsonPath(\"$.users[*].name\", ");
        System.out.println("      containsInAnyOrder(\"John\", \"Jane\", \"Bob\")));");
        
        System.out.println("\nType Checks:");
        System.out.println("  .andExpect(jsonPath(\"$.name\").isString())");
        System.out.println("  .andExpect(jsonPath(\"$.age\").isNumber())");
        System.out.println("  .andExpect(jsonPath(\"$.active\").isBoolean())");
        System.out.println("  .andExpect(jsonPath(\"$.tags\").isArray())");
        System.out.println("  .andExpect(jsonPath(\"$.metadata\").isMap())");
        
        System.out.println("\nExistence Checks:");
        System.out.println("  .andExpect(jsonPath(\"$.email\").exists())");
        System.out.println("  .andExpect(jsonPath(\"$.phone\").doesNotExist())");
        System.out.println("  .andExpect(jsonPath(\"$.data\").isNotEmpty())");
        System.out.println("  .andExpect(jsonPath(\"$.errors\").isEmpty())");
        
        System.out.println("\n✓ JSONPath pattern demonstrated");
    }
    
    public static void main(String[] args) {
        System.out.println("=== JSON Path Pattern ===");
        System.out.println("Validate JSON responses with path expressions");
        System.out.println("Run tests to see pattern in action");
    }
}
