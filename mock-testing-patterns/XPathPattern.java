package com.example.testing;

import org.junit.jupiter.api.Test;

/**
 * XPath Pattern
 * ==============
 * 
 * Demonstrates the XPath pattern for validating XML responses
 * in REST API tests using XPath expressions.
 * 
 * Use Cases:
 * ----------
 * 1. Validate XML response structure
 * 2. Extract nested XML elements
 * 3. Assert XML attributes
 * 4. Test SOAP web services
 * 5. Validate XML namespaces
 * 6. Test legacy XML APIs
 * 7. Verify XML schema compliance
 * 8. Compare XML values
 * 
 * Key Features:
 * -------------
 * - Standard XPath syntax
 * - Navigate XML structure
 * - Attribute access
 * - Namespace support
 * - Type-safe value extraction
 * - Predicate filtering
 * - Axis navigation
 * - Integration with MockMvc
 * 
 * XPath Syntax:
 * -------------
 * / - Root element
 * // - Descendant elements
 * @ - Attribute
 * * - Wildcard
 * [] - Predicate filter
 * text() - Text content
 * 
 * Common Expressions:
 * -------------------
 * /user/name - Direct path
 * //email - All email elements
 * /user/@id - Attribute value
 * //user[1] - First user element
 * //user[@active='true'] - Filtered elements
 * //user/name/text() - Text content
 * 
 * @author Spring Patterns
 * @version 1.0
 */
public class XPathPattern {
    
    @Test
    void demonstrateXPath() {
        System.out.println("\n=== XPath Pattern ===");
        
        System.out.println("\nBasic XPath Expressions:");
        System.out.println("  /user/name - Direct child path");
        System.out.println("  /user/address/city - Nested elements");
        System.out.println("  //email - All email elements");
        System.out.println("  //user/email - All user emails");
        
        System.out.println("\nAttribute Access:");
        System.out.println("  /user/@id - User ID attribute");
        System.out.println("  /user/@type - User type attribute");
        System.out.println("  //product[@category='electronics'] - Filtered by attribute");
        
        System.out.println("\nArray Indexing:");
        System.out.println("  //user[1] - First user");
        System.out.println("  //user[last()] - Last user");
        System.out.println("  //user[position()<3] - First two users");
        
        System.out.println("\nText Content:");
        System.out.println("  /user/name/text() - Name text");
        System.out.println("  //email/text() - Email text");
        System.out.println("  /user/description/text() - Description");
        
        System.out.println("\nWith MockMvc:");
        System.out.println("  mockMvc.perform(get(\"/api/user.xml\"))");
        System.out.println("      .andExpect(status().isOk())");
        System.out.println("      .andExpect(content().contentType(MediaType.APPLICATION_XML))");
        System.out.println("      .andExpect(xpath(\"/user/name\").string(\"John\"))");
        System.out.println("      .andExpect(xpath(\"/user/@id\").number(1.0))");
        System.out.println("      .andExpect(xpath(\"/user/email\").exists());");
        
        System.out.println("\nNamespace Handling:");
        System.out.println("  Map<String, String> namespaces = new HashMap<>();");
        System.out.println("  namespaces.put(\"ns\", \"http://example.com/schema\");");
        System.out.println("  ");
        System.out.println("  mockMvc.perform(get(\"/api/data.xml\"))");
        System.out.println("      .andExpect(xpath(\"/ns:root/ns:element\", namespaces)");
        System.out.println("          .string(\"value\"));");
        
        System.out.println("\nAssertion Examples:");
        System.out.println("  .andExpect(xpath(\"/user/name\").string(\"John Doe\"))");
        System.out.println("  .andExpect(xpath(\"/user/age\").number(30.0))");
        System.out.println("  .andExpect(xpath(\"/user/active\").booleanValue(true))");
        System.out.println("  .andExpect(xpath(\"//email\").nodeCount(3))");
        System.out.println("  .andExpect(xpath(\"/user/@id\").exists())");
        
        System.out.println("\nComplex Predicates:");
        System.out.println("  //user[age>18] - Adults");
        System.out.println("  //product[price<100] - Cheap products");
        System.out.println("  //order[@status='pending'] - Pending orders");
        System.out.println("  //user[contains(name, 'John')] - Name contains John");
        
        System.out.println("\n✓ XPath pattern demonstrated");
    }
    
    public static void main(String[] args) {
        System.out.println("=== XPath Pattern ===");
        System.out.println("Validate XML responses with XPath expressions");
        System.out.println("Run tests to see pattern in action");
    }
}
