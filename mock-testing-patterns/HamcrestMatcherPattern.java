package com.example.testing;

import org.junit.jupiter.api.Test;

/**
 * Hamcrest Matcher Pattern
 * =========================
 * 
 * Demonstrates the Hamcrest Matcher pattern for creating expressive,
 * readable test assertions using matcher objects.
 * 
 * Use Cases:
 * ----------
 * 1. Readable test assertions
 * 2. Complex object matching
 * 3. Collection assertions
 * 4. String pattern matching
 * 5. Number comparisons
 * 6. Custom matchers
 * 7. Composite matching
 * 8. Error message clarity
 * 
 * Key Features:
 * -------------
 * - Fluent assertion syntax
 * - Expressive matchers
 * - Descriptive failures
 * - Type-safe matching
 * - Composable matchers
 * - Custom matcher support
 * - Integration with JUnit
 * - Integration with MockMvc
 * 
 * Core Matchers:
 * --------------
 * - is() - Equals check
 * - equalTo() - Deep equals
 * - not() - Negation
 * - nullValue() - Null check
 * - notNullValue() - Not null check
 * - instanceOf() - Type check
 * - sameInstance() - Identity check
 * 
 * Collection Matchers:
 * --------------------
 * - hasSize() - Collection size
 * - hasItem() - Contains item
 * - hasItems() - Contains items
 * - contains() - Exact order
 * - containsInAnyOrder() - Any order
 * - empty() - Empty collection
 * - everyItem() - All items match
 * 
 * String Matchers:
 * ----------------
 * - containsString() - Substring
 * - startsWith() - Prefix
 * - endsWith() - Suffix
 * - equalToIgnoringCase() - Case insensitive
 * - matchesPattern() - Regex
 * - emptyString() - Empty check
 * 
 * Number Matchers:
 * ----------------
 * - greaterThan() - Greater than
 * - lessThan() - Less than
 * - greaterThanOrEqualTo() - >=
 * - lessThanOrEqualTo() - <=
 * - closeTo() - Approximate
 * 
 * @author Spring Patterns
 * @version 1.0
 */
public class HamcrestMatcherPattern {
    
    @Test
    void demonstrateCoreMatchers() {
        System.out.println("\n=== Core Matchers ===");
        
        System.out.println("\nEquality:");
        System.out.println("  assertThat(actual, is(expected));");
        System.out.println("  assertThat(actual, equalTo(expected));");
        System.out.println("  assertThat(actual, not(unexpected));");
        
        System.out.println("\nNull Checks:");
        System.out.println("  assertThat(value, nullValue());");
        System.out.println("  assertThat(value, notNullValue());");
        System.out.println("  assertThat(value, is(nullValue()));");
        
        System.out.println("\nType Checks:");
        System.out.println("  assertThat(obj, instanceOf(String.class));");
        System.out.println("  assertThat(obj, isA(Integer.class));");
        
        System.out.println("\nIdentity:");
        System.out.println("  assertThat(obj1, sameInstance(obj2));");
        System.out.println("  assertThat(obj1, not(sameInstance(obj2)));");
    }
    
    @Test
    void demonstrateCollectionMatchers() {
        System.out.println("\n=== Collection Matchers ===");
        
        System.out.println("\nSize:");
        System.out.println("  assertThat(list, hasSize(3));");
        System.out.println("  assertThat(list, empty());");
        System.out.println("  assertThat(list, not(empty()));");
        
        System.out.println("\nContains:");
        System.out.println("  assertThat(list, hasItem(\"value\"));");
        System.out.println("  assertThat(list, hasItems(\"a\", \"b\", \"c\"));");
        System.out.println("  assertThat(list, not(hasItem(\"x\")));");
        
        System.out.println("\nOrder:");
        System.out.println("  assertThat(list, contains(\"a\", \"b\", \"c\")); // Exact order");
        System.out.println("  assertThat(list, containsInAnyOrder(\"c\", \"a\", \"b\"));");
        
        System.out.println("\nAll Items:");
        System.out.println("  assertThat(list, everyItem(startsWith(\"test\")));");
        System.out.println("  assertThat(list, everyItem(greaterThan(0)));");
    }
    
    @Test
    void demonstrateStringMatchers() {
        System.out.println("\n=== String Matchers ===");
        
        System.out.println("\nSubstring:");
        System.out.println("  assertThat(str, containsString(\"hello\"));");
        System.out.println("  assertThat(str, not(containsString(\"goodbye\")));");
        
        System.out.println("\nPrefix/Suffix:");
        System.out.println("  assertThat(str, startsWith(\"Hello\"));");
        System.out.println("  assertThat(str, endsWith(\"World\"));");
        
        System.out.println("\nCase Insensitive:");
        System.out.println("  assertThat(str, equalToIgnoringCase(\"HELLO\"));");
        
        System.out.println("\nRegex:");
        System.out.println("  assertThat(email, matchesPattern(\".*@.*\\.com\"));");
        
        System.out.println("\nEmpty:");
        System.out.println("  assertThat(str, emptyString());");
        System.out.println("  assertThat(str, not(emptyString()));");
    }
    
    @Test
    void demonstrateNumberMatchers() {
        System.out.println("\n=== Number Matchers ===");
        
        System.out.println("\nComparisons:");
        System.out.println("  assertThat(age, greaterThan(18));");
        System.out.println("  assertThat(price, lessThan(100.0));");
        System.out.println("  assertThat(score, greaterThanOrEqualTo(0));");
        System.out.println("  assertThat(count, lessThanOrEqualTo(10));");
        
        System.out.println("\nApproximate:");
        System.out.println("  assertThat(pi, closeTo(3.14, 0.01));");
        System.out.println("  assertThat(value, closeTo(100.0, 0.5));");
    }
    
    @Test
    void demonstrateCompositeMatchers() {
        System.out.println("\n=== Composite Matchers ===");
        
        System.out.println("\nAnd:");
        System.out.println("  assertThat(str, allOf(");
        System.out.println("      startsWith(\"Hello\"),");
        System.out.println("      containsString(\"World\"),");
        System.out.println("      endsWith(\"!\")");
        System.out.println("  ));");
        
        System.out.println("\nOr:");
        System.out.println("  assertThat(value, anyOf(");
        System.out.println("      equalTo(\"A\"),");
        System.out.println("      equalTo(\"B\"),");
        System.out.println("      equalTo(\"C\")");
        System.out.println("  ));");
        
        System.out.println("\nNot:");
        System.out.println("  assertThat(list, not(anyOf(");
        System.out.println("      empty(),");
        System.out.println("      hasSize(1)");
        System.out.println("  )));");
    }
    
    @Test
    void demonstrateWithMockMvc() {
        System.out.println("\n=== With MockMvc ===");
        
        System.out.println("\nJSON Response:");
        System.out.println("  mockMvc.perform(get(\"/api/users\"))");
        System.out.println("      .andExpect(status().isOk())");
        System.out.println("      .andExpect(jsonPath(\"$.name\", is(\"John\")))");
        System.out.println("      .andExpect(jsonPath(\"$.age\", greaterThan(18)))");
        System.out.println("      .andExpect(jsonPath(\"$.email\", ");
        System.out.println("          containsString(\"@example.com\")))");
        System.out.println("      .andExpect(jsonPath(\"$.roles\", hasSize(2)))");
        System.out.println("      .andExpect(jsonPath(\"$.roles\", ");
        System.out.println("          hasItems(\"USER\", \"ADMIN\")));");
        
        System.out.println("\nResponse Headers:");
        System.out.println("  mockMvc.perform(post(\"/api/users\"))");
        System.out.println("      .andExpect(status().isCreated())");
        System.out.println("      .andExpect(header().string(\"Location\", ");
        System.out.println("          startsWith(\"/api/users/\")))");
        System.out.println("      .andExpect(header().string(\"Content-Type\", ");
        System.out.println("          containsString(\"application/json\")));");
    }
    
    @Test
    void demonstrateCustomMatcher() {
        System.out.println("\n=== Custom Matcher ===");
        
        System.out.println("\nDefine Custom Matcher:");
        System.out.println("  public static Matcher<User> isAdult() {");
        System.out.println("      return new TypeSafeMatcher<User>() {");
        System.out.println("          @Override");
        System.out.println("          protected boolean matchesSafely(User user) {");
        System.out.println("              return user.getAge() >= 18;");
        System.out.println("          }");
        System.out.println("          ");
        System.out.println("          @Override");
        System.out.println("          public void describeTo(Description description) {");
        System.out.println("              description.appendText(\"an adult user\");");
        System.out.println("          }");
        System.out.println("      };");
        System.out.println("  }");
        
        System.out.println("\nUse Custom Matcher:");
        System.out.println("  assertThat(user, isAdult());");
        System.out.println("  assertThat(users, everyItem(isAdult()));");
    }
    
    @Test
    void demonstrateMapMatchers() {
        System.out.println("\n=== Map Matchers ===");
        
        System.out.println("\nMap Assertions:");
        System.out.println("  assertThat(map, hasKey(\"name\"));");
        System.out.println("  assertThat(map, hasValue(\"John\"));");
        System.out.println("  assertThat(map, hasEntry(\"name\", \"John\"));");
        System.out.println("  assertThat(map.size(), is(3));");
        
        System.out.println("\nComposite:");
        System.out.println("  assertThat(map, allOf(");
        System.out.println("      hasKey(\"name\"),");
        System.out.println("      hasKey(\"email\"),");
        System.out.println("      not(hasKey(\"password\"))");
        System.out.println("  ));");
    }
    
    public static void main(String[] args) {
        System.out.println("=== Hamcrest Matcher Pattern ===");
        System.out.println("\nExpressive test assertions with matchers:");
        System.out.println("1. Core matchers (equality, null, type)");
        System.out.println("2. Collection matchers (size, contains, order)");
        System.out.println("3. String matchers (substring, pattern)");
        System.out.println("4. Number matchers (comparison, approximate)");
        System.out.println("5. Composite matchers (and, or, not)");
        System.out.println("6. MockMvc integration");
        System.out.println("7. Custom matchers");
        System.out.println("8. Map matchers");
        System.out.println("\nRun tests to see pattern in action");
    }
}

/**
 * Hamcrest Matcher Summary:
 * 
 * Import:
 * -------
 * import static org.hamcrest.MatcherAssert.assertThat;
 * import static org.hamcrest.Matchers.*;
 * 
 * Basic Usage:
 * ------------
 * assertThat(actual, matcher);
 * assertThat("reason", actual, matcher);
 * 
 * Common Matchers:
 * ----------------
 * // Equality
 * assertThat(value, is(10));
 * assertThat(value, equalTo(10));
 * assertThat(value, not(5));
 * 
 * // Strings
 * assertThat(str, containsString("hello"));
 * assertThat(str, startsWith("Hello"));
 * assertThat(str, endsWith("World"));
 * 
 * // Collections
 * assertThat(list, hasSize(3));
 * assertThat(list, hasItem("value"));
 * assertThat(list, contains("a", "b", "c"));
 * 
 * // Numbers
 * assertThat(age, greaterThan(18));
 * assertThat(price, lessThan(100.0));
 * assertThat(pi, closeTo(3.14, 0.01));
 * 
 * // Composite
 * assertThat(str, allOf(
 *     startsWith("Hello"),
 *     endsWith("World")
 * ));
 */
