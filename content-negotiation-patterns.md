### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 1:23:11 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **Content Negotiation Patterns**
> - Accept Header Pattern
> - Content Type Pattern
> - Media Type Pattern
> - View Negotiation Pattern
> - JSON/XML Conversion Pattern
> - Custom Message Converter Pattern
> - HTTP Message Converter Pattern
> ```

#### _Assistant_

> # Spring Boot Content Negotiation Patterns
> 
> I'll create a comprehensive Spring Boot application demonstrating all the content negotiation patterns you requested.
> 
> ## 1. Main Application Class
> 
> ```java src/main/java/com/example/contentnegotiation/ContentNegotiationApplication.java
> package com.example.contentnegotiation;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> 
> @SpringBootApplication
> public class ContentNegotiationApplication {
>     public static void main(String[] args) {
>         SpringApplication.run(ContentNegotiationApplication.class, args);
>     }
> }
> ```
> 
> ## 2. Model Classes
> 
> ```java src/main/java/com/example/contentnegotiation/model/Product.java
> package com.example.contentnegotiation.model;
> 
> import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
> import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
> 
> @JacksonXmlRootElement(localName = "product")
> public class Product {
>     
>     @JacksonXmlProperty
>     private Long id;
>     
>     @JacksonXmlProperty
>     private String name;
>     
>     @JacksonXmlProperty
>     private Double price;
>     
>     @JacksonXmlProperty
>     private String category;
> 
>     public Product() {}
> 
>     public Product(Long id, String name, Double price, String category) {
>         this.id = id;
>         this.name = name;
>         this.price = price;
>         this.category = category;
>     }
> 
>     // Getters and Setters
>     public Long getId() { return id; }
>     public void setId(Long id) { this.id = id; }
>     
>     public String getName() { return name; }
>     public void setName(String name) { this.name = name; }
>     
>     public Double getPrice() { return price; }
>     public void setPrice(Double price) { this.price = price; }
>     
>     public String getCategory() { return category; }
>     public void setCategory(String category) { this.category = category; }
> }
> ```
> 
> ```java src/main/java/com/example/contentnegotiation/model/CustomFormat.java
> package com.example.contentnegotiation.model;
> 
> public class CustomFormat {
>     private String data;
>     private String format;
>     private long timestamp;
> 
>     public CustomFormat() {
>         this.timestamp = System.currentTimeMillis();
>     }
> 
>     public CustomFormat(String data, String format) {
>         this.data = data;
>         this.format = format;
>         this.timestamp = System.currentTimeMillis();
>     }
> 
>     // Getters and Setters
>     public String getData() { return data; }
>     public void setData(String data) { this.data = data; }
>     
>     public String getFormat() { return format; }
>     public void setFormat(String format) { this.format = format; }
>     
>     public long getTimestamp() { return timestamp; }
>     public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
> }
> ```
> 
> ## 3. Accept Header Pattern Controller
> 
> ```java src/main/java/com/example/contentnegotiation/controller/AcceptHeaderController.java
> package com.example.contentnegotiation.controller;
> 
> import com.example.contentnegotiation.model.Product;
> import org.springframework.http.MediaType;
> import org.springframework.web.bind.annotation.*;
> 
> import java.util.Arrays;
> import java.util.List;
> 
> /**
>  * Accept Header Pattern: Determines response format based on Accept header
>  * Example: Accept: application/json or Accept: application/xml
>  */
> @RestController
> @RequestMapping("/api/accept-header")
> public class AcceptHeaderController {
> 
>     @GetMapping(value = "/product/{id}", 
>                 produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
>     public Product getProduct(@PathVariable Long id) {
>         return new Product(id, "Laptop", 999.99, "Electronics");
>     }
> 
>     @GetMapping(value = "/products",
>                 produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
>     public List<Product> getProducts() {
>         return Arrays.asList(
>             new Product(1L, "Laptop", 999.99, "Electronics"),
>             new Product(2L, "Phone", 599.99, "Electronics"),
>             new Product(3L, "Desk", 299.99, "Furniture")
>         );
>     }
> 
>     @PostMapping(value = "/product",
>                  consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
>                  produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
>     public Product createProduct(@RequestBody Product product) {
>         product.setId(100L);
>         return product;
>     }
> }
> ```
> 
> ## 4. Content Type Pattern Controller
> 
> ```java src/main/java/com/example/contentnegotiation/controller/ContentTypeController.java
> package com.example.contentnegotiation.controller;
> 
> import com.example.contentnegotiation.model.Product;
> import org.springframework.http.MediaType;
> import org.springframework.web.bind.annotation.*;
> 
> /**
>  * Content Type Pattern: Validates and processes different input content types
>  * Example: Content-Type: application/json or Content-Type: application/xml
>  */
> @RestController
> @RequestMapping("/api/content-type")
> public class ContentTypeController {
> 
>     @PostMapping(value = "/json-only",
>                  consumes = MediaType.APPLICATION_JSON_VALUE,
>                  produces = MediaType.APPLICATION_JSON_VALUE)
>     public Product acceptJsonOnly(@RequestBody Product product) {
>         product.setCategory(product.getCategory() + " (JSON)");
>         return product;
>     }
> 
>     @PostMapping(value = "/xml-only",
>                  consumes = MediaType.APPLICATION_XML_VALUE,
>                  produces = MediaType.APPLICATION_XML_VALUE)
>     public Product acceptXmlOnly(@RequestBody Product product) {
>         product.setCategory(product.getCategory() + " (XML)");
>         return product;
>     }
> 
>     @PostMapping(value = "/multi-format",
>                  consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
>                  produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
>     public Product acceptMultipleFormats(
>             @RequestBody Product product,
>             @RequestHeader("Content-Type") String contentType) {
>         product.setCategory(product.getCategory() + " (Received as: " + contentType + ")");
>         return product;
>     }
> 
>     @PutMapping(value = "/form-data",
>                 consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
>                 produces = MediaType.APPLICATION_JSON_VALUE)
>     public Product acceptFormData(
>             @RequestParam String name,
>             @RequestParam Double price,
>             @RequestParam String category) {
>         return new Product(null, name, price, category);
>     }
> }
> ```
> 
> ## 5. Media Type Pattern Controller
> 
> ```java src/main/java/com/example/contentnegotiation/controller/MediaTypeController.java
> package com.example.contentnegotiation.controller;
> 
> import com.example.contentnegotiation.model.Product;
> import org.springframework.http.MediaType;
> import org.springframework.web.bind.annotation.*;
> 
> /**
>  * Media Type Pattern: Uses custom media types and versioning
>  * Example: Accept: application/vnd.company.product.v1+json
>  */
> @RestController
> @RequestMapping("/api/media-type")
> public class MediaTypeController {
> 
>     // Custom media type with versioning
>     private static final String CUSTOM_V1_JSON = "application/vnd.company.product.v1+json";
>     private static final String CUSTOM_V2_JSON = "application/vnd.company.product.v2+json";
>     private static final String CUSTOM_V1_XML = "application/vnd.company.product.v1+xml";
> 
>     @GetMapping(value = "/product/{id}", produces = CUSTOM_V1_JSON)
>     public Product getProductV1(@PathVariable Long id) {
>         return new Product(id, "Product V1", 100.0, "Version 1");
>     }
> 
>     @GetMapping(value = "/product/{id}", produces = CUSTOM_V2_JSON)
>     public ProductV2 getProductV2(@PathVariable Long id) {
>         ProductV2 product = new ProductV2();
>         product.setId(id);
>         product.setName("Product V2");
>         product.setPrice(100.0);
>         product.setCategory("Version 2");
>         product.setDescription("Enhanced product with additional fields");
>         product.setInStock(true);
>         return product;
>     }
> 
>     @GetMapping(value = "/product/{id}", produces = CUSTOM_V1_XML)
>     public Product getProductV1Xml(@PathVariable Long id) {
>         return new Product(id, "Product V1 XML", 100.0, "Version 1 XML");
>     }
> 
>     // Extended Product class for V2
>     public static class ProductV2 extends Product {
>         private String description;
>         private Boolean inStock;
> 
>         public String getDescription() { return description; }
>         public void setDescription(String description) { this.description = description; }
>         
>         public Boolean getInStock() { return inStock; }
>         public void setInStock(Boolean inStock) { this.inStock = inStock; }
>     }
> }
> ```
> 
> ## 6. View Negotiation Pattern Controller
> 
> ```java src/main/java/com/example/contentnegotiation/controller/ViewNegotiationController.java
> package com.example.contentnegotiation.controller;
> 
> import com.example.contentnegotiation.model.Product;
> import org.springframework.stereotype.Controller;
> import org.springframework.ui.Model;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PathVariable;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.ResponseBody;
> import org.springframework.web.servlet.ModelAndView;
> 
> import java.util.Arrays;
> import java.util.List;
> 
> /**
>  * View Negotiation Pattern: Returns either HTML views or JSON/XML data
>  * based on request (REST endpoint vs web page)
>  */
> @Controller
> @RequestMapping("/api/view-negotiation")
> public class ViewNegotiationController {
> 
>     @GetMapping("/products/html")
>     public String getProductsHtml(Model model) {
>         List<Product> products = getProductList();
>         model.addAttribute("products", products);
>         return "products"; // returns products.html view
>     }
> 
>     @GetMapping("/products/json")
>     @ResponseBody
>     public List<Product> getProductsJson() {
>         return getProductList();
>     }
> 
>     @GetMapping("/product/{id}/view")
>     public ModelAndView getProductView(@PathVariable Long id) {
>         ModelAndView mav = new ModelAndView("product-details");
>         mav.addObject("product", new Product(id, "Sample Product", 99.99, "Sample"));
>         return mav;
>     }
> 
>     @GetMapping("/product/{id}/data")
>     @ResponseBody
>     public Product getProductData(@PathVariable Long id) {
>         return new Product(id, "Sample Product", 99.99, "Sample");
>     }
> 
>     private List<Product> getProductList() {
>         return Arrays.asList(
>             new Product(1L, "Laptop", 999.99, "Electronics"),
>             new Product(2L, "Mouse", 29.99, "Accessories"),
>             new Product(3L, "Keyboard", 79.99, "Accessories")
>         );
>     }
> }
> ```
> 
> ## 7. JSON/XML Conversion Pattern Controller
> 
> ```java src/main/java/com/example/contentnegotiation/controller/JsonXmlConversionController.java
> package com.example.contentnegotiation.controller;
> 
> import com.example.contentnegotiation.model.Product;
> import com.fasterxml.jackson.databind.ObjectMapper;
> import com.fasterxml.jackson.dataformat.xml.XmlMapper;
> import org.springframework.http.MediaType;
> import org.springframework.web.bind.annotation.*;
> 
> import java.util.Arrays;
> import java.util.List;
> 
> /**
>  * JSON/XML Conversion Pattern: Explicit conversion between JSON and XML
>  * Uses Jackson ObjectMapper and XmlMapper
>  */
> @RestController
> @RequestMapping("/api/conversion")
> public class JsonXmlConversionController {
> 
>     private final ObjectMapper jsonMapper = new ObjectMapper();
>     private final XmlMapper xmlMapper = new XmlMapper();
> 
>     @PostMapping(value = "/json-to-xml",
>                  consumes = MediaType.APPLICATION_JSON_VALUE,
>                  produces = MediaType.APPLICATION_XML_VALUE)
>     public Product convertJsonToXml(@RequestBody Product product) throws Exception {
>         // Explicit conversion demonstration
>         String json = jsonMapper.writeValueAsString(product);
>         System.out.println("Received JSON: " + json);
>         
>         // Convert to XML and back (demonstrating conversion)
>         String xml = xmlMapper.writeValueAsString(product);
>         System.out.println("Converted to XML: " + xml);
>         
>         return product; // Spring automatically converts to XML due to produces attribute
>     }
> 
>     @PostMapping(value = "/xml-to-json",
>                  consumes = MediaType.APPLICATION_XML_VALUE,
>                  produces = MediaType.APPLICATION_JSON_VALUE)
>     public Product convertXmlToJson(@RequestBody Product product) throws Exception {
>         // Explicit conversion demonstration
>         String xml = xmlMapper.writeValueAsString(product);
>         System.out.println("Received XML: " + xml);
>         
>         // Convert to JSON and back
>         String json = jsonMapper.writeValueAsString(product);
>         System.out.println("Converted to JSON: " + json);
>         
>         return product; // Spring automatically converts to JSON due to produces attribute
>     }
> 
>     @GetMapping(value = "/auto-convert/{id}",
>                 produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
>     public Product autoConvert(@PathVariable Long id) {
>         // Spring automatically handles conversion based on Accept header
>         return new Product(id, "Auto-Convert Product", 149.99, "Multi-Format");
>     }
> 
>     @PostMapping(value = "/batch-convert",
>                  consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
>                  produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
>     public List<Product> batchConvert(@RequestBody List<Product> products) {
>         // Handles batch conversion
>         return products;
>     }
> }
> ```
> 
> ## 8. Custom Message Converter
> 
> ```java src/main/java/com/example/contentnegotiation/converter/CustomMessageConverter.java
> package com.example.contentnegotiation.converter;
> 
> import com.example.contentnegotiation.model.CustomFormat;
> import org.springframework.http.HttpInputMessage;
> import org.springframework.http.HttpOutputMessage;
> import org.springframework.http.MediaType;
> import org.springframework.http.converter.AbstractHttpMessageConverter;
> import org.springframework.http.converter.HttpMessageNotReadableException;
> import org.springframework.http.converter.HttpMessageNotWritableException;
> 
> import java.io.IOException;
> import java.nio.charset.StandardCharsets;
> 
> /**
>  * Custom Message Converter Pattern: Creates a custom converter for proprietary format
>  * Format: CUSTOM|data|format|timestamp
>  */
> public class CustomMessageConverter extends AbstractHttpMessageConverter<CustomFormat> {
> 
>     public static final MediaType CUSTOM_MEDIA_TYPE = new MediaType("application", "custom");
> 
>     public CustomMessageConverter() {
>         super(CUSTOM_MEDIA_TYPE);
>     }
> 
>     @Override
>     protected boolean supports(Class<?> clazz) {
>         return CustomFormat.class.equals(clazz);
>     }
> 
>     @Override
>     protected CustomFormat readInternal(Class<? extends CustomFormat> clazz, HttpInputMessage inputMessage)
>             throws IOException, HttpMessageNotReadableException {
>         
>         String body = new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8);
>         String[] parts = body.split("\\|");
>         
>         if (parts.length < 4 || !parts[0].equals("CUSTOM")) {
>             throw new HttpMessageNotReadableException("Invalid custom format", inputMessage);
>         }
> 
>         CustomFormat customFormat = new CustomFormat();
>         customFormat.setData(parts[1]);
>         customFormat.setFormat(parts[2]);
>         customFormat.setTimestamp(Long.parseLong(parts[3]));
>         
>         return customFormat;
>     }
> 
>     @Override
>     protected void writeInternal(CustomFormat customFormat, HttpOutputMessage outputMessage)
>             throws IOException, HttpMessageNotWritableException {
>         
>         String output = String.format("CUSTOM|%s|%s|%d",
>                 customFormat.getData(),
>                 customFormat.getFormat(),
>                 customFormat.getTimestamp());
>         
>         outputMessage.getBody().write(output.getBytes(StandardCharsets.UTF_8));
>     }
> }
> ```
> 
> ## 9. Custom Message Converter Controller
> 
> ```java src/main/java/com/example/contentnegotiation/controller/CustomMessageConverterController.java
> package com.example.contentnegotiation.controller;
> 
> import com.example.contentnegotiation.model.CustomFormat;
> import org.springframework.http.MediaType;
> import org.springframework.web.bind.annotation.*;
> 
> /**
>  * Custom Message Converter Pattern Controller
>  * Uses custom media type: application/custom
>  */
> @RestController
> @RequestMapping("/api/custom-converter")
> public class CustomMessageConverterController {
> 
>     private static final String CUSTOM_MEDIA_TYPE = "application/custom";
> 
>     @GetMapping(value = "/data", produces = CUSTOM_MEDIA_TYPE)
>     public CustomFormat getCustomData() {
>         return new CustomFormat("Sample Data", "Custom Format");
>     }
> 
>     @PostMapping(value = "/data",
>                  consumes = CUSTOM_MEDIA_TYPE,
>                  produces = CUSTOM_MEDIA_TYPE)
>     public CustomFormat processCustomData(@RequestBody CustomFormat customFormat) {
>         customFormat.setData(customFormat.getData() + " - Processed");
>         return customFormat;
>     }
> 
>     @PostMapping(value = "/convert",
>                  consumes = CUSTOM_MEDIA_TYPE,
>                  produces = MediaType.APPLICATION_JSON_VALUE)
>     public CustomFormat convertToJson(@RequestBody CustomFormat customFormat) {
>         return customFormat;
>     }
> 
>     @PostMapping(value = "/convert-from-json",
>                  consumes = MediaType.APPLICATION_JSON_VALUE,
>                  produces = CUSTOM_MEDIA_TYPE)
>     public CustomFormat convertFromJson(@RequestBody CustomFormat customFormat) {
>         return customFormat;
>     }
> }
> ```
> 
> ## 10. HTTP Message Converter Configuration
> 
> ```java src/main/java/com/example/contentnegotiation/config/HttpMessageConverterConfig.java
> package com.example.contentnegotiation.config;
> 
> import com.example.contentnegotiation.converter.CustomMessageConverter;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.http.converter.HttpMessageConverter;
> import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
> import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
> import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
> import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
> 
> import java.util.List;
> 
> /**
>  * HTTP Message Converter Pattern: Configures and customizes message converters
>  */
> @Configuration
> public class HttpMessageConverterConfig implements WebMvcConfigurer {
> 
>     @Override
>     public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
>         configurer
>             .favorParameter(false)  // Disable query parameter-based negotiation
>             .favorPathExtension(false)  // Disable path extension-based negotiation
>             .ignoreAcceptHeader(false)  // Enable Accept header
>             .defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON);
>     }
> 
>     @Override
>     public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

#### _User_

> Continue your response exactly where you left off:

#### _Assistant_

> ```java src/main/java/com/example/contentnegotiation/config/HttpMessageConverterConfig.java
> // ... existing code ...
> 
>     @Override
>     public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
>         // Add custom message converter
>         converters.add(new CustomMessageConverter());
>         
>         // Customize JSON converter
>         converters.stream()
>             .filter(converter -> converter instanceof MappingJackson2HttpMessageConverter)
>             .findFirst()
>             .ifPresent(converter -> {
>                 MappingJackson2HttpMessageConverter jsonConverter = 
>                     (MappingJackson2HttpMessageConverter) converter;
>                 // Additional JSON configuration can be added here
>                 System.out.println("JSON converter configured");
>             });
>         
>         // Customize XML converter
>         converters.stream()
>             .filter(converter -> converter instanceof MappingJackson2XmlHttpMessageConverter)
>             .findFirst()
>             .ifPresent(converter -> {
>                 MappingJackson2XmlHttpMessageConverter xmlConverter = 
>                     (MappingJackson2XmlHttpMessageConverter) converter;
>                 // Additional XML configuration can be added here
>                 System.out.println("XML converter configured");
>             });
>     }
> }
> ```
> 
> ## 11. HTTP Message Converter Controller
> 
> ```java src/main/java/com/example/contentnegotiation/controller/HttpMessageConverterController.java
> package com.example.contentnegotiation.controller;
> 
> import com.example.contentnegotiation.model.Product;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.MediaType;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> 
> import java.util.HashMap;
> import java.util.Map;
> 
> /**
>  * HTTP Message Converter Pattern: Demonstrates various converter behaviors
>  * Shows how Spring uses converters for serialization/deserialization
>  */
> @RestController
> @RequestMapping("/api/http-converter")
> public class HttpMessageConverterController {
> 
>     @GetMapping(value = "/product/{id}",
>                 produces = {MediaType.APPLICATION_JSON_VALUE, 
>                            MediaType.APPLICATION_XML_VALUE,
>                            MediaType.TEXT_PLAIN_VALUE})
>     public ResponseEntity<?> getProduct(
>             @PathVariable Long id,
>             @RequestHeader(value = "Accept", defaultValue = MediaType.APPLICATION_JSON_VALUE) String accept) {
>         
>         Product product = new Product(id, "Multi-Format Product", 299.99, "Electronics");
>         
>         // Different response based on Accept header
>         if (accept.contains(MediaType.TEXT_PLAIN_VALUE)) {
>             String plainText = String.format("Product: %s, Price: $%.2f, Category: %s",
>                     product.getName(), product.getPrice(), product.getCategory());
>             return ResponseEntity.ok()
>                     .contentType(MediaType.TEXT_PLAIN)
>                     .body(plainText);
>         }
>         
>         return ResponseEntity.ok(product);
>     }
> 
>     @PostMapping(value = "/process",
>                  consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
>                  produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
>     public ResponseEntity<Map<String, Object>> processProduct(
>             @RequestBody Product product,
>             @RequestHeader("Content-Type") String contentType) {
>         
>         Map<String, Object> response = new HashMap<>();
>         response.put("receivedProduct", product);
>         response.put("receivedAs", contentType);
>         response.put("processed", true);
>         response.put("timestamp", System.currentTimeMillis());
>         
>         return ResponseEntity.ok(response);
>     }
> 
>     @PutMapping(value = "/update/{id}",
>                 consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
>                 produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
>     public ResponseEntity<Product> updateProduct(
>             @PathVariable Long id,
>             @RequestBody Product product) {
>         
>         product.setId(id);
>         return ResponseEntity.ok(product);
>     }
> 
>     @DeleteMapping(value = "/delete/{id}")
>     public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
>         Map<String, Object> response = new HashMap<>();
>         response.put("deleted", true);
>         response.put("productId", id);
>         response.put("timestamp", System.currentTimeMillis());
>         
>         return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
>     }
> 
>     @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
>     public ResponseEntity<Map<String, String>> getConverterInfo() {
>         Map<String, String> info = new HashMap<>();
>         info.put("jsonConverter", "MappingJackson2HttpMessageConverter");
>         info.put("xmlConverter", "MappingJackson2XmlHttpMessageConverter");
>         info.put("customConverter", "CustomMessageConverter");
>         info.put("stringConverter", "StringHttpMessageConverter");
>         
>         return ResponseEntity.ok(info);
>     }
> }
> ```
> 
> ## 12. Application Properties
> 
> ```properties src/main/resources/application.properties
> # Server Configuration
> server.port=8080
> spring.application.name=content-negotiation-demo
> 
> # JSON Configuration
> spring.jackson.serialization.indent-output=true
> spring.jackson.serialization.write-dates-as-timestamps=false
> spring.jackson.default-property-inclusion=non_null
> 
> # XML Configuration
> spring.jackson.dataformat.xml.default-use-wrapper=false
> 
> # Content Negotiation
> spring.mvc.contentnegotiation.favor-parameter=false
> spring.mvc.contentnegotiation.favor-path-extension=false
> spring.mvc.contentnegotiation.media-types.json=application/json
> spring.mvc.contentnegotiation.media-types.xml=application/xml
> 
> # Logging
> logging.level.org.springframework.web=DEBUG
> logging.level.com.example.contentnegotiation=DEBUG
> ```
> 
> ## 13. Sample Thymeleaf Templates (for View Negotiation)
> 
> ```html src/main/resources/templates/products.html
> <!DOCTYPE html>
> <html xmlns:th="http://www.thymeleaf.org">
> <head>
>     <title>Products</title>
>     <style>
>         table { border-collapse: collapse; width: 100%; }
>         th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
>         th { background-color: #4CAF50; color: white; }
>     </style>
> </head>
> <body>
>     <h1>Product List</h1>
>     <table>
>         <thead>
>             <tr>
>                 <th>ID</th>
>                 <th>Name</th>
>                 <th>Price</th>
>                 <th>Category</th>
>             </tr>
>         </thead>
>         <tbody>
>             <tr th:each="product : ${products}">
>                 <td th:text="${product.id}"></td>
>                 <td th:text="${product.name}"></td>
>                 <td th:text="${'$' + product.price}"></td>
>                 <td th:text="${product.category}"></td>
>             </tr>
>         </tbody>
>     </table>
> </body>
> </html>
> ```
> 
> ```html src/main/resources/templates/product-details.html
> <!DOCTYPE html>
> <html xmlns:th="http://www.thymeleaf.org">
> <head>
>     <title>Product Details</title>
>     <style>
>         .product-card { 
>             border: 1px solid #ddd; 
>             padding: 20px; 
>             max-width: 400px; 
>             margin: 20px auto;
>             border-radius: 8px;
>         }
>         .label { font-weight: bold; }
>     </style>
> </head>
> <body>
>     <div class="product-card">
>         <h1>Product Details</h1>
>         <p><span class="label">ID:</span> <span th:text="${product.id}"></span></p>
>         <p><span class="label">Name:</span> <span th:text="${product.name}"></span></p>
>         <p><span class="label">Price:</span> $<span th:text="${product.price}"></span></p>
>         <p><span class="label">Category:</span> <span th:text="${product.category}"></span></p>
>     </div>
> </body>
> </html>
> ```
> 
> ## 14. Maven Dependencies (pom.xml)
> 
> ```xml pom.xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0"
>          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
>          https://maven.apache.org/xsd/maven-4.0.0.xsd">
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
>     <artifactId>content-negotiation</artifactId>
>     <version>1.0.0</version>
>     <name>Content Negotiation Demo</name>
>     <description>Spring Boot Content Negotiation Patterns</description>
>     
>     <properties>
>         <java.version>17</java.version>
>     </properties>
>     
>     <dependencies>
>         <!-- Spring Boot Web Starter -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         
>         <!-- Thymeleaf for View Negotiation -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-thymeleaf</artifactId>
>         </dependency>
>         
>         <!-- Jackson XML for XML Support -->
>         <dependency>
>             <groupId>com.fasterxml.jackson.dataformat</groupId>
>             <artifactId>jackson-dataformat-xml</artifactId>
>         </dependency>
>         
>         <!-- Spring Boot DevTools (Optional) -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-devtools</artifactId>
>             <scope>runtime</scope>
>             <optional>true</optional>
>         </dependency>
>         
>         <!-- Spring Boot Test -->
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
> ## 15. Testing Guide (README)
> 
> ```markdown README.md
> # Content Negotiation Patterns - Testing Guide
> 
> ## 1. Accept Header Pattern
> ```bash
> # JSON Response
> curl -H "Accept: application/json" http://localhost:8080/api/accept-header/product/1
> 
> # XML Response
> curl -H "Accept: application/xml" http://localhost:8080/api/accept-header/product/1
> 
> # Create Product (JSON)
> curl -X POST -H "Content-Type: application/json" -H "Accept: application/json" \
>   -d '{"name":"Tablet","price":399.99,"category":"Electronics"}' \
>   http://localhost:8080/api/accept-header/product
> ```
> 
> ## 2. Content Type Pattern
> ```bash
> # JSON Only Endpoint
> curl -X POST -H "Content-Type: application/json" \
>   -d '{"name":"Monitor","price":299.99,"category":"Electronics"}' \
>   http://localhost:8080/api/content-type/json-only
> 
> # XML Only Endpoint
> curl -X POST -H "Content-Type: application/xml" \
>   -d '<product><name>Mouse</name><price>29.99</price><category>Accessories</category></product>' \
>   http://localhost:8080/api/content-type/xml-only
> 
> # Form Data
> curl -X PUT -H "Content-Type: application/x-www-form-urlencoded" \
>   -d "name=Chair&price=199.99&category=Furniture" \
>   http://localhost:8080/api/content-type/form-data
> ```
> 
> ## 3. Media Type Pattern
> ```bash
> # Version 1 (Custom Media Type)
> curl -H "Accept: application/vnd.company.product.v1+json" \
>   http://localhost:8080/api/media-type/product/1
> 
> # Version 2 (Custom Media Type with additional fields)
> curl -H "Accept: application/vnd.company.product.v2+json" \
>   http://localhost:8080/api/media-type/product/1
> 
> # Version 1 XML
> curl -H "Accept: application/vnd.company.product.v1+xml" \
>   http://localhost:8080/api/media-type/product/1
> ```
> 
> ## 4. View Negotiation Pattern
> ```bash
> # HTML View (Open in browser)
> http://localhost:8080/api/view-negotiation/products/html
> 
> # JSON Data
> curl http://localhost:8080/api/view-negotiation/products/json
> 
> # Product Details View (Open in browser)
> http://localhost:8080/api/view-negotiation/product/1/view
> 
> # Product Data
> curl http://localhost:8080/api/view-negotiation/product/1/data
> ```
> 
> ## 5. JSON/XML Conversion Pattern
> ```bash
> # Convert JSON to XML
> curl -X POST -H "Content-Type: application/json" -H "Accept: application/xml" \
>   -d '{"name":"Speaker","price":79.99,"category":"Audio"}' \
>   http://localhost:8080/api/conversion/json-to-xml
> 
> # Convert XML to JSON
> curl -X POST -H "Content-Type: application/xml" -H "Accept: application/json" \
>   -d '<product><name>Headphones</name><price>149.99</price><category>Audio</category></product>' \
>   http://localhost:8080/api/conversion/xml-to-json
> 
> # Auto Convert
> curl -H "Accept: application/json" http://localhost:8080/api/conversion/auto-convert/1
> curl -H "Accept: application/xml" http://localhost:8080/api/conversion/auto-convert/1
> ```
> 
> ## 6. Custom Message Converter Pattern
> ```bash
> # Get Custom Format
> curl -H "Accept: application/custom" http://localhost:8080/api/custom-converter/data
> 
> # Post Custom Format
> curl -X POST -H "Content-Type: application/custom" -H "Accept: application/custom" \
>   -d "CUSTOM|Test Data|Custom|1234567890" \
>   http://localhost:8080/api/custom-converter/data
> 
> # Convert Custom to JSON
> curl -X POST -H "Content-Type: application/custom" -H "Accept: application/json" \
>   -d "CUSTOM|Convert Me|Custom Format|1234567890" \
>   http://localhost:8080/api/custom-converter/convert
> ```
> 
> ## 7. HTTP Message Converter Pattern
> ```bash
> # Get Product (JSON/XML)
> curl -H "Accept: application/json" http://localhost:8080/api/http-converter/product/1
> curl -H "Accept: application/xml" http://localhost:8080/api/http-converter/product/1
> 
> # Get Product (Plain Text)
> curl -H "Accept: text/plain" http://localhost:8080/api/http-converter/product/1
> 
> # Process Product
> curl -X POST -H "Content-Type: application/json" -H "Accept: application/json" \
>   -d '{"name":"Webcam","price":89.99,"category":"Accessories"}' \
>   http://localhost:8080/api/http-converter/process
> 
> # Get Converter Info
> curl http://localhost:8080/api/http-converter/info
> ```
> 
> ## Running the Application
> ```bash
> mvn clean install
> mvn spring-boot:run
> ```
> 
> ## Key Features Demonstrated
> 
> 1. **Accept Header Pattern**: Content negotiation via Accept header
> 2. **Content Type Pattern**: Input validation based on Content-Type
> 3. **Media Type Pattern**: Custom vendor-specific media types and versioning
> 4. **View Negotiation Pattern**: HTML views vs REST responses
> 5. **JSON/XML Conversion**: Automatic format conversion
> 6. **Custom Message Converter**: Proprietary format support
> 7. **HTTP Message Converter**: Spring's converter infrastructure
> 
> Each pattern showcases different aspects of content negotiation in Spring Boot!
> ```
> 
> ---
> 
> All patterns are now complete! The application demonstrates comprehensive content negotiation capabilities in Spring Boot. You can run the application and test each endpoint using the provided curl commands.