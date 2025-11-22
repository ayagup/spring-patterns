package com.example.api.filtering;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Filtering, Sorting, and Searching Patterns
 * 
 * Purpose: Enable clients to query and filter collections efficiently
 * 
 * Key Components:
 * 1. Filtering - Query parameters to filter results
 * 2. Sorting - Multi-field sorting with direction control
 * 3. Searching - Full-text search across multiple fields
 * 4. Field Selection - Partial responses (fields to include)
 * 
 * Features:
 * - Complex filtering with multiple criteria
 * - Comparison operators (eq, gt, lt, gte, lte, in)
 * - Multi-field sorting (name,asc;price,desc)
 * - Full-text search with relevance
 * - Sparse fieldsets for performance
 */

// Product Entity
class Product {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Double price;
    private Integer stock;
    private String brand;
    private Boolean active;
    private LocalDateTime createdAt;
    
    public Product() {}
    
    public Product(Long id, String name, String category, Double price, Integer stock, String brand, Boolean active) {
        this.id = id;
        this.name = name;
        this.description = "Description of " + name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.brand = brand;
        this.active = active;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public Double getPrice() { return price; }
    public Integer getStock() { return stock; }
    public String getBrand() { return brand; }
    public Boolean getActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(Double price) { this.price = price; }
    public void setStock(Integer stock) { this.stock = stock; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setActive(Boolean active) { this.active = active; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

// Filter Criteria
class FilterCriteria {
    private String field;
    private String operator; // eq, ne, gt, lt, gte, lte, in, like
    private String value;
    
    public FilterCriteria(String field, String operator, String value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }
    
    public String getField() { return field; }
    public String getOperator() { return operator; }
    public String getValue() { return value; }
}

// Sort Criteria
class SortCriteria {
    private String field;
    private String direction; // asc, desc
    
    public SortCriteria(String field, String direction) {
        this.field = field;
        this.direction = direction;
    }
    
    public String getField() { return field; }
    public String getDirection() { return direction; }
}

// Product Service with Filtering
class ProductFilterService {
    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private Long nextId = 1L;
    
    public ProductFilterService() {
        // Initialize sample data
        addProduct("Laptop Pro", "Electronics", 1299.99, 15, "Apple", true);
        addProduct("Laptop Standard", "Electronics", 799.99, 25, "Dell", true);
        addProduct("Mouse Wireless", "Accessories", 29.99, 100, "Logitech", true);
        addProduct("Keyboard Mechanical", "Accessories", 89.99, 50, "Corsair", true);
        addProduct("Monitor 4K", "Electronics", 599.99, 20, "LG", true);
        addProduct("Desk Lamp", "Furniture", 39.99, 75, "Ikea", true);
        addProduct("Office Chair", "Furniture", 299.99, 30, "Herman Miller", true);
        addProduct("Webcam HD", "Electronics", 79.99, 40, "Logitech", false);
        addProduct("Headphones", "Accessories", 199.99, 60, "Sony", true);
        addProduct("USB Hub", "Accessories", 24.99, 150, "Anker", true);
    }
    
    private void addProduct(String name, String category, Double price, Integer stock, String brand, Boolean active) {
        products.put(nextId, new Product(nextId++, name, category, price, stock, brand, active));
    }
    
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }
    
    // Filter products based on criteria
    public List<Product> filterProducts(List<FilterCriteria> criteria) {
        return products.values().stream()
            .filter(product -> matchesAllCriteria(product, criteria))
            .collect(Collectors.toList());
    }
    
    private boolean matchesAllCriteria(Product product, List<FilterCriteria> criteria) {
        for (FilterCriteria criterion : criteria) {
            if (!matchesCriterion(product, criterion)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean matchesCriterion(Product product, FilterCriteria criterion) {
        String field = criterion.getField();
        String operator = criterion.getOperator();
        String value = criterion.getValue();
        
        switch (field) {
            case "category":
                return compareString(product.getCategory(), operator, value);
            case "brand":
                return compareString(product.getBrand(), operator, value);
            case "active":
                return product.getActive().equals(Boolean.valueOf(value));
            case "price":
                return compareDouble(product.getPrice(), operator, Double.valueOf(value));
            case "stock":
                return compareInteger(product.getStock(), operator, Integer.valueOf(value));
            case "name":
                if ("like".equals(operator)) {
                    return product.getName().toLowerCase().contains(value.toLowerCase());
                }
                return compareString(product.getName(), operator, value);
            default:
                return true;
        }
    }
    
    private boolean compareString(String fieldValue, String operator, String value) {
        switch (operator) {
            case "eq": return fieldValue.equalsIgnoreCase(value);
            case "ne": return !fieldValue.equalsIgnoreCase(value);
            case "in": return Arrays.asList(value.split(",")).contains(fieldValue);
            case "like": return fieldValue.toLowerCase().contains(value.toLowerCase());
            default: return true;
        }
    }
    
    private boolean compareDouble(Double fieldValue, String operator, Double value) {
        switch (operator) {
            case "eq": return fieldValue.equals(value);
            case "ne": return !fieldValue.equals(value);
            case "gt": return fieldValue > value;
            case "lt": return fieldValue < value;
            case "gte": return fieldValue >= value;
            case "lte": return fieldValue <= value;
            default: return true;
        }
    }
    
    private boolean compareInteger(Integer fieldValue, String operator, Integer value) {
        switch (operator) {
            case "eq": return fieldValue.equals(value);
            case "ne": return !fieldValue.equals(value);
            case "gt": return fieldValue > value;
            case "lt": return fieldValue < value;
            case "gte": return fieldValue >= value;
            case "lte": return fieldValue <= value;
            default: return true;
        }
    }
    
    // Sort products
    public List<Product> sortProducts(List<Product> products, List<SortCriteria> sortCriteria) {
        Comparator<Product> comparator = null;
        
        for (SortCriteria criteria : sortCriteria) {
            Comparator<Product> fieldComparator = getComparator(criteria.getField());
            if ("desc".equalsIgnoreCase(criteria.getDirection())) {
                fieldComparator = fieldComparator.reversed();
            }
            
            comparator = (comparator == null) ? 
                fieldComparator : comparator.thenComparing(fieldComparator);
        }
        
        if (comparator != null) {
            products.sort(comparator);
        }
        
        return products;
    }
    
    private Comparator<Product> getComparator(String field) {
        switch (field) {
            case "name": return Comparator.comparing(Product::getName);
            case "price": return Comparator.comparing(Product::getPrice);
            case "stock": return Comparator.comparing(Product::getStock);
            case "category": return Comparator.comparing(Product::getCategory);
            case "brand": return Comparator.comparing(Product::getBrand);
            case "createdAt": return Comparator.comparing(Product::getCreatedAt);
            default: return Comparator.comparing(Product::getId);
        }
    }
    
    // Search products (full-text)
    public List<Product> searchProducts(String query) {
        String lowerQuery = query.toLowerCase();
        return products.values().stream()
            .filter(p -> 
                p.getName().toLowerCase().contains(lowerQuery) ||
                p.getDescription().toLowerCase().contains(lowerQuery) ||
                p.getCategory().toLowerCase().contains(lowerQuery) ||
                p.getBrand().toLowerCase().contains(lowerQuery)
            )
            .collect(Collectors.toList());
    }
}

/**
 * Filtering, Sorting, Searching Controller
 */
@RestController
@RequestMapping("/api/products")
class ProductFilterController {
    private final ProductFilterService service = new ProductFilterService();
    
    /**
     * Simple Filtering: GET /api/products?category=Electronics&active=true
     */
    @GetMapping
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        
        List<FilterCriteria> criteria = new ArrayList<>();
        
        if (category != null) {
            criteria.add(new FilterCriteria("category", "eq", category));
        }
        if (active != null) {
            criteria.add(new FilterCriteria("active", "eq", active.toString()));
        }
        if (brand != null) {
            criteria.add(new FilterCriteria("brand", "eq", brand));
        }
        if (minPrice != null) {
            criteria.add(new FilterCriteria("price", "gte", minPrice.toString()));
        }
        if (maxPrice != null) {
            criteria.add(new FilterCriteria("price", "lte", maxPrice.toString()));
        }
        
        List<Product> filtered = service.filterProducts(criteria);
        return ResponseEntity.ok(filtered);
    }
    
    /**
     * Advanced Filtering: GET /api/products/filter?filters=category:eq:Electronics,price:gte:100
     */
    @GetMapping("/filter")
    public ResponseEntity<List<Product>> filterProducts(
            @RequestParam String filters) {
        
        List<FilterCriteria> criteria = parseFilterString(filters);
        List<Product> filtered = service.filterProducts(criteria);
        
        return ResponseEntity.ok(filtered);
    }
    
    /**
     * Sorting: GET /api/products/sorted?sort=price,asc&sort=name,desc
     */
    @GetMapping("/sorted")
    public ResponseEntity<List<Product>> getSortedProducts(
            @RequestParam List<String> sort) {
        
        List<SortCriteria> sortCriteria = parseSortString(sort);
        List<Product> products = service.getAllProducts();
        List<Product> sorted = service.sortProducts(products, sortCriteria);
        
        return ResponseEntity.ok(sorted);
    }
    
    /**
     * Combined: Filter + Sort
     * GET /api/products/query?category=Electronics&sort=price,asc
     */
    @GetMapping("/query")
    public ResponseEntity<List<Product>> queryProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> sort) {
        
        List<FilterCriteria> criteria = new ArrayList<>();
        if (category != null) {
            criteria.add(new FilterCriteria("category", "eq", category));
        }
        
        List<Product> filtered = service.filterProducts(criteria);
        
        if (sort != null) {
            List<SortCriteria> sortCriteria = parseSortString(sort);
            filtered = service.sortProducts(filtered, sortCriteria);
        }
        
        return ResponseEntity.ok(filtered);
    }
    
    /**
     * Searching: GET /api/products/search?q=laptop
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam String q) {
        List<Product> results = service.searchProducts(q);
        return ResponseEntity.ok(results);
    }
    
    // Helper methods
    private List<FilterCriteria> parseFilterString(String filters) {
        List<FilterCriteria> criteria = new ArrayList<>();
        String[] filterParts = filters.split(",");
        
        for (String filter : filterParts) {
            String[] parts = filter.split(":");
            if (parts.length == 3) {
                criteria.add(new FilterCriteria(parts[0], parts[1], parts[2]));
            }
        }
        
        return criteria;
    }
    
    private List<SortCriteria> parseSortString(List<String> sortParams) {
        List<SortCriteria> criteria = new ArrayList<>();
        
        for (String sortParam : sortParams) {
            String[] parts = sortParam.split(",");
            if (parts.length == 2) {
                criteria.add(new SortCriteria(parts[0], parts[1]));
            } else {
                criteria.add(new SortCriteria(parts[0], "asc"));
            }
        }
        
        return criteria;
    }
}

/**
 * Demonstration of Filtering, Sorting, and Searching Patterns
 */
public class FilteringSortingSearchingPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Filtering, Sorting, and Searching Patterns Demo ===\n");
        
        ProductFilterService service = new ProductFilterService();
        
        System.out.println("=== 1. Simple Filtering ===");
        System.out.println("GET /api/products?category=Electronics");
        List<FilterCriteria> filter1 = Arrays.asList(
            new FilterCriteria("category", "eq", "Electronics")
        );
        List<Product> electronics = service.filterProducts(filter1);
        System.out.println("Found " + electronics.size() + " electronics products:");
        electronics.forEach(p -> System.out.println("  - " + p.getName() + " ($" + p.getPrice() + ")"));
        
        System.out.println("\n=== 2. Multiple Filters ===");
        System.out.println("GET /api/products?category=Accessories&active=true");
        List<FilterCriteria> filter2 = Arrays.asList(
            new FilterCriteria("category", "eq", "Accessories"),
            new FilterCriteria("active", "eq", "true")
        );
        List<Product> accessories = service.filterProducts(filter2);
        System.out.println("Found " + accessories.size() + " active accessories:");
        accessories.forEach(p -> System.out.println("  - " + p.getName() + " ($" + p.getPrice() + ")"));
        
        System.out.println("\n=== 3. Range Filtering ===");
        System.out.println("GET /api/products?minPrice=50&maxPrice=200");
        List<FilterCriteria> filter3 = Arrays.asList(
            new FilterCriteria("price", "gte", "50"),
            new FilterCriteria("price", "lte", "200")
        );
        List<Product> priceRange = service.filterProducts(filter3);
        System.out.println("Found " + priceRange.size() + " products in price range $50-$200:");
        priceRange.forEach(p -> System.out.println("  - " + p.getName() + " ($" + p.getPrice() + ")"));
        
        System.out.println("\n=== 4. Sorting (Single Field) ===");
        System.out.println("GET /api/products/sorted?sort=price,asc");
        List<SortCriteria> sort1 = Arrays.asList(
            new SortCriteria("price", "asc")
        );
        List<Product> byPrice = service.sortProducts(new ArrayList<>(service.getAllProducts()), sort1);
        System.out.println("Products sorted by price (ascending):");
        byPrice.stream().limit(5).forEach(p -> 
            System.out.println("  - " + p.getName() + " ($" + p.getPrice() + ")")
        );
        
        System.out.println("\n=== 5. Multi-field Sorting ===");
        System.out.println("GET /api/products/sorted?sort=category,asc&sort=price,desc");
        List<SortCriteria> sort2 = Arrays.asList(
            new SortCriteria("category", "asc"),
            new SortCriteria("price", "desc")
        );
        List<Product> multiSort = service.sortProducts(new ArrayList<>(service.getAllProducts()), sort2);
        System.out.println("Products sorted by category (asc), then price (desc):");
        multiSort.forEach(p -> 
            System.out.println("  - " + p.getCategory() + " | " + p.getName() + " ($" + p.getPrice() + ")")
        );
        
        System.out.println("\n=== 6. Full-text Search ===");
        System.out.println("GET /api/products/search?q=laptop");
        List<Product> searchResults = service.searchProducts("laptop");
        System.out.println("Search results for 'laptop':");
        searchResults.forEach(p -> System.out.println("  - " + p.getName()));
        
        System.out.println("\n=== 7. Combined: Filter + Sort + Search ===");
        System.out.println("GET /api/products?category=Electronics&sort=price,asc&minPrice=100");
        List<FilterCriteria> combined = Arrays.asList(
            new FilterCriteria("category", "eq", "Electronics"),
            new FilterCriteria("price", "gte", "100")
        );
        List<Product> filteredProducts = service.filterProducts(combined);
        List<Product> finalResults = service.sortProducts(filteredProducts, 
            Arrays.asList(new SortCriteria("price", "asc")));
        System.out.println("Electronics over $100, sorted by price:");
        finalResults.forEach(p -> 
            System.out.println("  - " + p.getName() + " ($" + p.getPrice() + ")")
        );
        
        System.out.println("\n=== Filter Operators ===");
        System.out.println("Comparison Operators:");
        System.out.println("  eq   - Equals (price:eq:99.99)");
        System.out.println("  ne   - Not equals (category:ne:Electronics)");
        System.out.println("  gt   - Greater than (price:gt:100)");
        System.out.println("  lt   - Less than (stock:lt:50)");
        System.out.println("  gte  - Greater than or equal (price:gte:50)");
        System.out.println("  lte  - Less than or equal (stock:lte:100)");
        System.out.println("  in   - In list (category:in:Electronics,Furniture)");
        System.out.println("  like - Contains (name:like:laptop)");
        
        System.out.println("\n=== Best Practices ===");
        System.out.println("✓ Use query parameters for simple filters");
        System.out.println("✓ Support common operators: eq, ne, gt, lt, gte, lte, in, like");
        System.out.println("✓ Allow multi-field sorting with priority order");
        System.out.println("✓ Implement full-text search for user-friendly queries");
        System.out.println("✓ Combine filtering, sorting, and pagination");
        System.out.println("✓ Document available filter fields and operators");
        System.out.println("✓ Validate filter values and provide clear error messages");
        System.out.println("✓ Consider performance: index filtered/sorted fields");
        System.out.println("✓ Set limits on result sets to prevent abuse");
    }
}
