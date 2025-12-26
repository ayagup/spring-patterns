package com.example.testing;

import org.junit.jupiter.api.Test;

/**
 * REST Docs Pattern
 * ==================
 * 
 * Demonstrates the Spring REST Docs pattern for generating accurate
 * API documentation from tests.
 * 
 * Use Cases:
 * ----------
 * 1. Generate API documentation
 * 2. Document REST endpoints
 * 3. Keep docs synchronized with code
 * 4. Test-driven documentation
 * 5. Document request/response formats
 * 6. Generate snippets
 * 7. Ensure API contract accuracy
 * 8. Integration with AsciiDoc/Markdown
 * 
 * Key Features:
 * -------------
 * - Test-driven documentation
 * - Auto-generated snippets
 * - Request/response documentation
 * - Path parameters documentation
 * - Query parameters documentation
 * - Headers documentation
 * - Constraint documentation
 * - Customizable templates
 * 
 * Documentation Types:
 * --------------------
 * - Request fields
 * - Response fields
 * - Path parameters
 * - Request parameters
 * - Request headers
 * - Response headers
 * - Links (HATEOAS)
 * - Request parts (multipart)
 * 
 * Output Formats:
 * ---------------
 * - AsciiDoc (default)
 * - Markdown
 * - HTML (via AsciiDoctor)
 * - PDF (via AsciiDoctor)
 * 
 * @author Spring Patterns
 * @version 1.0
 */
public class RestDocsPattern {
    
    @Test
    void demonstrateRestDocs() {
        System.out.println("\n=== REST Docs Pattern ===");
        
        System.out.println("\nSetup:");
        System.out.println("  @ExtendWith({RestDocumentationExtension.class, SpringExtension.class})");
        System.out.println("  @WebMvcTest(UserController.class)");
        System.out.println("  class UserControllerDocsTest {");
        System.out.println("      @Autowired");
        System.out.println("      private MockMvc mockMvc;");
        System.out.println("      ");
        System.out.println("      @BeforeEach");
        System.out.println("      void setUp(RestDocumentationContextProvider restDocumentation) {");
        System.out.println("          this.mockMvc = MockMvcBuilders");
        System.out.println("              .webAppContextSetup(context)");
        System.out.println("              .apply(documentationConfiguration(restDocumentation))");
        System.out.println("              .build();");
        System.out.println("      }");
        System.out.println("  }");
        
        System.out.println("\nDocument GET Request:");
        System.out.println("  @Test");
        System.out.println("  void getUserDocumentation() throws Exception {");
        System.out.println("      mockMvc.perform(get(\"/users/{id}\", 1))");
        System.out.println("          .andExpect(status().isOk())");
        System.out.println("          .andDo(document(\"get-user\",");
        System.out.println("              pathParameters(");
        System.out.println("                  parameterWithName(\"id\")");
        System.out.println("                      .description(\"The user's ID\")");
        System.out.println("              ),");
        System.out.println("              responseFields(");
        System.out.println("                  fieldWithPath(\"id\").description(\"User ID\"),");
        System.out.println("                  fieldWithPath(\"name\").description(\"User name\"),");
        System.out.println("                  fieldWithPath(\"email\").description(\"Email address\")");
        System.out.println("              )");
        System.out.println("          ));");
        System.out.println("  }");
        
        System.out.println("\nDocument POST Request:");
        System.out.println("  @Test");
        System.out.println("  void createUserDocumentation() throws Exception {");
        System.out.println("      mockMvc.perform(post(\"/users\")");
        System.out.println("          .contentType(MediaType.APPLICATION_JSON)");
        System.out.println("          .content(\"{\\\"name\\\":\\\"John\\\",\\\"email\\\":\\\"john@example.com\\\"}\"))");
        System.out.println("          .andExpect(status().isCreated())");
        System.out.println("          .andDo(document(\"create-user\",");
        System.out.println("              requestFields(");
        System.out.println("                  fieldWithPath(\"name\")");
        System.out.println("                      .description(\"User's full name\")");
        System.out.println("                      .attributes(key(\"constraints\")");
        System.out.println("                          .value(\"Must not be null\")),");
        System.out.println("                  fieldWithPath(\"email\")");
        System.out.println("                      .description(\"Email address\")");
        System.out.println("                      .type(JsonFieldType.STRING)");
        System.out.println("              )");
        System.out.println("          ));");
        System.out.println("  }");
        
        System.out.println("\nDocument Query Parameters:");
        System.out.println("  mockMvc.perform(get(\"/users?page=0&size=10\"))");
        System.out.println("      .andDo(document(\"list-users\",");
        System.out.println("          requestParameters(");
        System.out.println("              parameterWithName(\"page\")");
        System.out.println("                  .description(\"Page number\"),");
        System.out.println("              parameterWithName(\"size\")");
        System.out.println("                  .description(\"Page size\")");
        System.out.println("          )");
        System.out.println("      ));");
        
        System.out.println("\nDocument Headers:");
        System.out.println("  mockMvc.perform(get(\"/users\")");
        System.out.println("      .header(\"Authorization\", \"Bearer token\"))");
        System.out.println("      .andDo(document(\"get-users\",");
        System.out.println("          requestHeaders(");
        System.out.println("              headerWithName(\"Authorization\")");
        System.out.println("                  .description(\"Bearer token\")");
        System.out.println("          ),");
        System.out.println("          responseHeaders(");
        System.out.println("              headerWithName(\"X-Total-Count\")");
        System.out.println("                  .description(\"Total number of users\")");
        System.out.println("          )");
        System.out.println("      ));");
        
        System.out.println("\nDocument Links (HATEOAS):");
        System.out.println("  mockMvc.perform(get(\"/users/1\"))");
        System.out.println("      .andDo(document(\"user-links\",");
        System.out.println("          links(");
        System.out.println("              linkWithRel(\"self\")");
        System.out.println("                  .description(\"Link to this user\"),");
        System.out.println("              linkWithRel(\"orders\")");
        System.out.println("                  .description(\"User's orders\")");
        System.out.println("          )");
        System.out.println("      ));");
        
        System.out.println("\nPretty Print:");
        System.out.println("  .apply(documentationConfiguration(restDocumentation)");
        System.out.println("      .operationPreprocessors()");
        System.out.println("          .withRequestDefaults(prettyPrint())");
        System.out.println("          .withResponseDefaults(prettyPrint()))");
        
        System.out.println("\n✓ REST Docs pattern demonstrated");
    }
    
    public static void main(String[] args) {
        System.out.println("=== REST Docs Pattern ===");
        System.out.println("Generate API documentation from tests");
        System.out.println("Run tests to see pattern in action");
    }
}

/**
 * REST Docs Summary:
 * 
 * Setup (JUnit 5):
 * ----------------
 * @ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
 * @WebMvcTest(UserController.class)
 * class ApiDocsTest {
 *     @BeforeEach
 *     void setUp(RestDocumentationContextProvider restDoc) {
 *         mockMvc = MockMvcBuilders.webAppContextSetup(context)
 *             .apply(documentationConfiguration(restDoc))
 *             .build();
 *     }
 * }
 * 
 * Document Endpoint:
 * ------------------
 * mockMvc.perform(get("/users"))
 *     .andDo(document("list-users"));
 * 
 * Request Fields:
 * ---------------
 * .andDo(document("create-user",
 *     requestFields(
 *         fieldWithPath("name").description("Name"),
 *         fieldWithPath("email").description("Email")
 *     )
 * ));
 * 
 * Response Fields:
 * ----------------
 * .andDo(document("get-user",
 *     responseFields(
 *         fieldWithPath("id").description("ID"),
 *         fieldWithPath("name").description("Name")
 *     )
 * ));
 * 
 * Path Parameters:
 * ----------------
 * .andDo(document("get-user",
 *     pathParameters(
 *         parameterWithName("id").description("User ID")
 *     )
 * ));
 * 
 * Gradle Plugin:
 * --------------
 * plugins {
 *     id 'org.asciidoctor.jvm.convert' version '3.3.2'
 * }
 * 
 * asciidoctor {
 *     inputs.dir snippetsDir
 *     dependsOn test
 * }
 */
