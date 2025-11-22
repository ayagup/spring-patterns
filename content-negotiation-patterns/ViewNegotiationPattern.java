package com.example.contentnegotiation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ContentNegotiationManagerFactoryBean;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.xml.MarshallingView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * VIEW NEGOTIATION PATTERN
 * =========================
 * 
 * Demonstrates view resolution based on content negotiation.
 * Uses ContentNegotiatingViewResolver to select appropriate view based on Accept header.
 * 
 * Key Concepts:
 * - ContentNegotiatingViewResolver
 * - Multiple view resolvers coordination
 * - View selection based on media type
 * - JSON/XML/HTML view rendering
 * - Custom view implementations
 * - View priority and fallback
 * 
 * Use Cases:
 * - Single endpoint serving web pages and API responses
 * - Progressive enhancement (HTML for browsers, JSON for apps)
 * - Multi-format report generation
 * - Flexible data visualization
 * - Responsive API design
 */

@SpringBootApplication
public class ViewNegotiationPattern {

    public static void main(String[] args) {
        SpringApplication.run(ViewNegotiationPattern.class, args);
        demonstrateViewNegotiation();
    }

    private static void demonstrateViewNegotiation() {
        System.out.println("=== View Negotiation Pattern Demonstrations ===\n");

        // Demo 1: View resolver hierarchy
        System.out.println("1. View Resolver Hierarchy:");
        ViewResolverChain chain = new ViewResolverChain();
        chain.addResolver("JSON", 1);
        chain.addResolver("XML", 2);
        chain.addResolver("HTML", 3);
        chain.addResolver("PDF", 4);
        System.out.println("   Resolvers in priority order:");
        chain.getResolvers().forEach(r -> 
            System.out.println("   - " + r.name + " (priority: " + r.priority + ")")
        );

        // Demo 2: View selection simulation
        System.out.println("\n2. View Selection Simulation:");
        ViewSelector selector = new ViewSelector();
        List<String> accepts = Arrays.asList(
            "text/html",
            "application/json",
            "application/xml",
            "application/pdf"
        );
        accepts.forEach(accept -> {
            String view = selector.selectView(accept);
            System.out.println("   Accept: " + accept + " -> View: " + view);
        });

        // Demo 3: Content negotiation strategy
        System.out.println("\n3. Content Negotiation Strategy:");
        NegotiationStrategy strategy = new NegotiationStrategy();
        Map<String, Object> model = Map.of(
            "title", "Report",
            "data", Arrays.asList("Item 1", "Item 2", "Item 3")
        );
        String[] acceptHeaders = {
            "text/html, application/xhtml+xml",
            "application/json",
            "application/xml, text/xml"
        };
        for (String accept : acceptHeaders) {
            ViewRenderingInfo info = strategy.determineView(accept, model);
            System.out.println("   Accept: " + accept);
            System.out.println("      Selected: " + info.viewName);
            System.out.println("      Format: " + info.format);
        }
    }
}

// ============================================================================
// CONFIGURATION
// ============================================================================

@Configuration
class ViewNegotiationConfiguration implements WebMvcConfigurer {

    /**
     * Configure content negotiation manager
     */
    @Bean
    public ContentNegotiationManager contentNegotiationManager() {
        ContentNegotiationManagerFactoryBean factory = new ContentNegotiationManagerFactoryBean();
        factory.setFavorParameter(true);
        factory.setParameterName("format");
        factory.setIgnoreAcceptHeader(false);
        factory.setDefaultContentType(MediaType.TEXT_HTML);
        
        Map<String, MediaType> mediaTypes = new HashMap<>();
        mediaTypes.put("html", MediaType.TEXT_HTML);
        mediaTypes.put("json", MediaType.APPLICATION_JSON);
        mediaTypes.put("xml", MediaType.APPLICATION_XML);
        mediaTypes.put("pdf", MediaType.APPLICATION_PDF);
        factory.setMediaTypes(mediaTypes);
        
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /**
     * Configure ContentNegotiatingViewResolver
     */
    @Bean
    public ViewResolver contentNegotiatingViewResolver(ContentNegotiationManager manager) {
        ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
        resolver.setContentNegotiationManager(manager);
        
        // Set view resolvers
        List<ViewResolver> resolvers = new ArrayList<>();
        resolvers.add(jsonViewResolver());
        resolvers.add(xmlViewResolver());
        resolvers.add(htmlViewResolver());
        
        resolver.setViewResolvers(resolvers);
        
        // Set default views
        List<View> defaultViews = new ArrayList<>();
        defaultViews.add(new MappingJackson2JsonView());
        resolver.setDefaultViews(defaultViews);
        
        return resolver;
    }

    /**
     * JSON view resolver
     */
    @Bean
    public ViewResolver jsonViewResolver() {
        return (viewName, locale) -> {
            MappingJackson2JsonView view = new MappingJackson2JsonView();
            view.setPrettyPrint(true);
            view.setExtractValueFromSingleKeyModel(true);
            return view;
        };
    }

    /**
     * XML view resolver
     */
    @Bean
    public ViewResolver xmlViewResolver() {
        return (viewName, locale) -> {
            // In real scenario, configure with JAXB marshaller
            return new CustomXmlView();
        };
    }

    /**
     * HTML view resolver (JSP/Thymeleaf)
     */
    @Bean
    public ViewResolver htmlViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        resolver.setOrder(Integer.MAX_VALUE);
        return resolver;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.enableContentNegotiation(
            new MappingJackson2JsonView(),
            new CustomXmlView()
        );
    }
}

// ============================================================================
// CONTROLLERS
// ============================================================================

@Controller
@RequestMapping("/reports")
class ReportController {

    /**
     * Single endpoint serving multiple formats
     * - Browser: HTML page
     * - API: JSON data
     * - System: XML export
     */
    @GetMapping("/{id}")
    public String getReport(@PathVariable Long id, Model model) {
        // Prepare model data
        Report report = new Report(
            id,
            "Monthly Sales Report",
            "Sales data for the month",
            LocalDateTime.now(),
            Arrays.asList(
                new ReportItem("Product A", 1000.0, 50),
                new ReportItem("Product B", 1500.0, 75),
                new ReportItem("Product C", 800.0, 40)
            )
        );
        
        model.addAttribute("report", report);
        model.addAttribute("timestamp", LocalDateTime.now());
        
        // View name - resolver will select appropriate view
        return "reportView";
    }

    /**
     * Dashboard with multiple representation support
     */
    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        Dashboard dashboard = new Dashboard(
            "Sales Dashboard",
            Map.of(
                "totalSales", 25000.0,
                "totalOrders", 150,
                "averageOrderValue", 166.67
            ),
            Arrays.asList("Chart1", "Chart2", "Chart3")
        );
        
        model.addAttribute("dashboard", dashboard);
        return "dashboardView";
    }

    /**
     * List with pagination and sorting
     */
    @GetMapping
    public String getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        List<Report> reports = generateSampleReports();
        
        model.addAttribute("reports", reports);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("total", reports.size());
        
        return "reportsList";
    }

    private List<Report> generateSampleReports() {
        return Arrays.asList(
            new Report(1L, "Report 1", "Description 1", LocalDateTime.now(), new ArrayList<>()),
            new Report(2L, "Report 2", "Description 2", LocalDateTime.now(), new ArrayList<>()),
            new Report(3L, "Report 3", "Description 3", LocalDateTime.now(), new ArrayList<>())
        );
    }
}

@Controller
@RequestMapping("/products")
class ProductViewController {

    /**
     * Product detail view negotiation
     */
    @GetMapping("/{id}")
    public String getProduct(@PathVariable Long id, Model model) {
        Product product = new Product(
            id,
            "Sample Product",
            "Product description",
            99.99,
            "Electronics"
        );
        
        model.addAttribute("product", product);
        return "productView";
    }

    /**
     * Product catalog
     */
    @GetMapping
    public String getProducts(
            @RequestParam(required = false) String category,
            Model model) {
        
        List<Product> products = Arrays.asList(
            new Product(1L, "Product 1", "Desc 1", 99.99, "Electronics"),
            new Product(2L, "Product 2", "Desc 2", 149.99, "Electronics"),
            new Product(3L, "Product 3", "Desc 3", 79.99, "Books")
        );
        
        if (category != null) {
            products = products.stream()
                .filter(p -> p.getCategory().equals(category))
                .toList();
        }
        
        model.addAttribute("products", products);
        model.addAttribute("category", category);
        
        return "productsList";
    }
}

// ============================================================================
// CUSTOM VIEWS
// ============================================================================

class CustomXmlView implements View {

    @Override
    public String getContentType() {
        return "application/xml";
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request,
                      javax.servlet.http.HttpServletResponse response) throws Exception {
        
        response.setContentType(getContentType());
        response.setCharacterEncoding("UTF-8");
        
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<response>\n");
        
        for (Map.Entry<String, ?> entry : model.entrySet()) {
            if (!entry.getKey().startsWith("org.springframework")) {
                xml.append("  <").append(entry.getKey()).append(">");
                xml.append(entry.getValue());
                xml.append("</").append(entry.getKey()).append(">\n");
            }
        }
        
        xml.append("</response>");
        
        response.getWriter().write(xml.toString());
    }
}

class CustomPdfView implements View {

    @Override
    public String getContentType() {
        return "application/pdf";
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request,
                      javax.servlet.http.HttpServletResponse response) throws Exception {
        
        response.setContentType(getContentType());
        response.setHeader("Content-Disposition", "attachment; filename=report.pdf");
        
        // In real scenario, use library like iText or Apache PDFBox
        byte[] pdfContent = "PDF Content Placeholder".getBytes();
        response.getOutputStream().write(pdfContent);
    }
}

class CustomCsvView implements View {

    @Override
    public String getContentType() {
        return "text/csv";
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request,
                      javax.servlet.http.HttpServletResponse response) throws Exception {
        
        response.setContentType(getContentType());
        response.setHeader("Content-Disposition", "attachment; filename=data.csv");
        
        StringBuilder csv = new StringBuilder();
        
        // Generate CSV from model
        for (Map.Entry<String, ?> entry : model.entrySet()) {
            if (entry.getValue() instanceof List) {
                csv.append(toCsv((List<?>) entry.getValue()));
            }
        }
        
        response.getWriter().write(csv.toString());
    }

    private String toCsv(List<?> items) {
        StringBuilder sb = new StringBuilder();
        for (Object item : items) {
            sb.append(item.toString()).append("\n");
        }
        return sb.toString();
    }
}

// ============================================================================
// VIEW RESOLUTION UTILITIES
// ============================================================================

class ViewResolverChain {
    
    private final List<ResolverInfo> resolvers = new ArrayList<>();

    public void addResolver(String name, int priority) {
        resolvers.add(new ResolverInfo(name, priority));
        resolvers.sort(Comparator.comparingInt(r -> r.priority));
    }

    public List<ResolverInfo> getResolvers() {
        return Collections.unmodifiableList(resolvers);
    }

    static class ResolverInfo {
        final String name;
        final int priority;

        ResolverInfo(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }
    }
}

class ViewSelector {

    private final Map<String, String> mediaTypeToView = new HashMap<>();

    public ViewSelector() {
        mediaTypeToView.put("text/html", "HTML View");
        mediaTypeToView.put("application/json", "JSON View");
        mediaTypeToView.put("application/xml", "XML View");
        mediaTypeToView.put("application/pdf", "PDF View");
        mediaTypeToView.put("text/csv", "CSV View");
    }

    public String selectView(String acceptHeader) {
        for (Map.Entry<String, String> entry : mediaTypeToView.entrySet()) {
            if (acceptHeader.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "Default HTML View";
    }
}

class NegotiationStrategy {

    public ViewRenderingInfo determineView(String acceptHeader, Map<String, Object> model) {
        ViewRenderingInfo info = new ViewRenderingInfo();
        
        if (acceptHeader.contains("application/json")) {
            info.viewName = "jsonView";
            info.format = "JSON";
            info.contentType = "application/json";
        } else if (acceptHeader.contains("application/xml")) {
            info.viewName = "xmlView";
            info.format = "XML";
            info.contentType = "application/xml";
        } else if (acceptHeader.contains("application/pdf")) {
            info.viewName = "pdfView";
            info.format = "PDF";
            info.contentType = "application/pdf";
        } else {
            info.viewName = "htmlView";
            info.format = "HTML";
            info.contentType = "text/html";
        }
        
        info.modelSize = model.size();
        info.timestamp = LocalDateTime.now();
        
        return info;
    }
}

class ViewRenderingInfo {
    String viewName;
    String format;
    String contentType;
    int modelSize;
    LocalDateTime timestamp;

    @Override
    public String toString() {
        return String.format("ViewRenderingInfo[view=%s, format=%s, contentType=%s]",
            viewName, format, contentType);
    }
}

// ============================================================================
// DOMAIN MODELS
// ============================================================================

class Report {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private List<ReportItem> items;

    public Report() {}

    public Report(Long id, String title, String description, LocalDateTime createdAt, 
                 List<ReportItem> items) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.items = items;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<ReportItem> getItems() { return items; }
    public void setItems(List<ReportItem> items) { this.items = items; }
}

class ReportItem {
    private String name;
    private Double value;
    private Integer quantity;

    public ReportItem() {}

    public ReportItem(String name, Double value, Integer quantity) {
        this.name = name;
        this.value = value;
        this.quantity = quantity;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}

class Dashboard {
    private String title;
    private Map<String, Object> metrics;
    private List<String> charts;

    public Dashboard() {}

    public Dashboard(String title, Map<String, Object> metrics, List<String> charts) {
        this.title = title;
        this.metrics = metrics;
        this.charts = charts;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Map<String, Object> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
    public List<String> getCharts() { return charts; }
    public void setCharts(List<String> charts) { this.charts = charts; }
}

class Product {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;

    public Product() {}

    public Product(Long id, String name, String description, Double price, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}

/*
 * BEST PRACTICES:
 * ===============
 * 1. Configure ContentNegotiatingViewResolver properly
 * 2. Set appropriate priority for view resolvers
 * 3. Provide default views for common formats
 * 4. Use separate resolvers for different view technologies
 * 5. Handle edge cases (no Accept header, unknown formats)
 * 6. Cache view resolution results when possible
 * 7. Document supported formats clearly
 * 8. Test all view negotiation paths
 * 
 * COMMON PITFALLS:
 * ================
 * 1. Incorrect view resolver ordering
 * 2. Missing default view configuration
 * 3. Not handling Accept header variations
 * 4. Forgetting to set content type in custom views
 * 5. Poor performance due to excessive view creation
 * 
 * CONFIGURATION EXAMPLES:
 * =======================
 * URL Parameter: /reports/1?format=json
 * Accept Header: curl -H "Accept: application/json" /reports/1
 * Path Extension: /reports/1.json (if enabled)
 * Default: /reports/1 (returns HTML for browser)
 * 
 * VIEW RESOLVER PRIORITY:
 * =======================
 * 1. ContentNegotiatingViewResolver (order: HIGHEST_PRECEDENCE)
 * 2. BeanNameViewResolver
 * 3. InternalResourceViewResolver (order: LOWEST_PRECEDENCE)
 */
