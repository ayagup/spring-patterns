package com.example.elasticsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregation Query Pattern
 * 
 * Demonstrates Elasticsearch aggregations for analytics,
 * statistics, and data summarization.
 * 
 * Key concepts:
 * - Metrics aggregations (avg, sum, min, max, count)
 * - Bucket aggregations (terms, date histogram, range)
 * - Nested aggregations
 * - Pipeline aggregations
 * - Statistical aggregations
 * 
 * Use cases:
 * - Analytics dashboards
 * - Reporting and statistics
 * - Faceted search
 * - Time-series analysis
 * - Data summarization
 */
@SpringBootApplication
public class AggregationQueryPattern {

    public static void main(String[] args) {
        SpringApplication.run(AggregationQueryPattern.class, args);
    }
}

/**
 * Sale document for aggregations
 */
record Sale(
    String id,
    String product,
    String category,
    String region,
    Double amount,
    Integer quantity,
    String customer,
    LocalDateTime saleDate
) {}

/**
 * Aggregation result wrapper
 */
record AggregationResult(
    String name,
    Object value,
    Map<String, Object> buckets
) {}

/**
 * Service demonstrating aggregation queries
 */
@Service
class SaleAggregationService {
    
    private final ElasticsearchOperations elasticsearchOperations;
    private static final String INDEX_NAME = "sales";
    
    public SaleAggregationService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }
    
    /**
     * Index a sale
     */
    public Sale indexSale(Sale sale) {
        IndexQuery indexQuery = new IndexQueryBuilder()
            .withId(sale.id())
            .withObject(sale)
            .build();
        
        elasticsearchOperations.index(indexQuery, IndexCoordinates.of(INDEX_NAME));
        return sale;
    }
    
    /**
     * Index multiple sales in bulk
     */
    public List<Sale> indexSalesBulk(List<Sale> sales) {
        List<IndexQuery> queries = sales.stream()
            .map(sale -> new IndexQueryBuilder()
                .withId(sale.id())
                .withObject(sale)
                .build())
            .collect(Collectors.toList());
        
        elasticsearchOperations.bulkIndex(queries, IndexCoordinates.of(INDEX_NAME));
        return sales;
    }
    
    /**
     * Metrics aggregation - Average sale amount
     */
    public Double getAverageSaleAmount() {
        Query query = Query.findAll();
        // Note: Full aggregation support requires native Elasticsearch client
        // This is a simplified example showing the pattern
        SearchHits<Sale> searchHits = elasticsearchOperations.search(query, Sale.class, IndexCoordinates.of(INDEX_NAME));
        
        return searchHits.stream()
            .mapToDouble(hit -> hit.getContent().amount())
            .average()
            .orElse(0.0);
    }
    
    /**
     * Metrics aggregation - Total sales amount
     */
    public Double getTotalSalesAmount() {
        Query query = Query.findAll();
        SearchHits<Sale> searchHits = elasticsearchOperations.search(query, Sale.class, IndexCoordinates.of(INDEX_NAME));
        
        return searchHits.stream()
            .mapToDouble(hit -> hit.getContent().amount())
            .sum();
    }
    
    /**
     * Metrics aggregation - Min/Max sale amount
     */
    public Map<String, Double> getMinMaxSaleAmount() {
        Query query = Query.findAll();
        SearchHits<Sale> searchHits = elasticsearchOperations.search(query, Sale.class, IndexCoordinates.of(INDEX_NAME));
        
        DoubleSummaryStatistics stats = searchHits.stream()
            .mapToDouble(hit -> hit.getContent().amount())
            .summaryStatistics();
        
        return Map.of(
            "min", stats.getMin(),
            "max", stats.getMax()
        );
    }
    
    /**
     * Bucket aggregation - Sales by category (terms aggregation)
     */
    public Map<String, Long> getSalesByCategory() {
        Criteria criteria = new Criteria();
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Sale> searchHits = elasticsearchOperations.search(query, Sale.class, IndexCoordinates.of(INDEX_NAME));
        
        return searchHits.stream()
            .map(hit -> hit.getContent().category())
            .collect(Collectors.groupingBy(
                category -> category,
                Collectors.counting()
            ));
    }
    
    /**
     * Bucket aggregation - Sales by region
     */
    public Map<String, Long> getSalesByRegion() {
        Query query = Query.findAll();
        SearchHits<Sale> searchHits = elasticsearchOperations.search(query, Sale.class, IndexCoordinates.of(INDEX_NAME));
        
        return searchHits.stream()
            .map(hit -> hit.getContent().region())
            .collect(Collectors.groupingBy(
                region -> region,
                Collectors.counting()
            ));
    }
    
    /**
     * Bucket aggregation - Total amount by category
     */
    public Map<String, Double> getTotalAmountByCategory() {
        Query query = Query.findAll();
        SearchHits<Sale> searchHits = elasticsearchOperations.search(query, Sale.class, IndexCoordinates.of(INDEX_NAME));
        
        return searchHits.stream()
            .collect(Collectors.groupingBy(
                hit -> hit.getContent().category(),
                Collectors.summingDouble(hit -> hit.getContent().amount())
            ));
    }
    
    /**
     * Bucket aggregation - Average amount by region
     */
    public Map<String, Double> getAverageAmountByRegion() {
        Query query = Query.findAll();
        SearchHits<Sale> searchHits = elasticsearchOperations.search(query, Sale.class, IndexCoordinates.of(INDEX_NAME));
        
        return searchHits.stream()
            .collect(Collectors.groupingBy(
                hit -> hit.getContent().region(),
                Collectors.averagingDouble(hit -> hit.getContent().amount())
            ));
    }
    
    /**
     * Range aggregation - Sales by amount ranges
     */
    public Map<String, Long> getSalesByAmountRange() {
        Query query = Query.findAll();
        SearchHits<Sale> searchHits = elasticsearchOperations.search(query, Sale.class, IndexCoordinates.of(INDEX_NAME));
        
        return searchHits.stream()
            .collect(Collectors.groupingBy(
                hit -> {
                    double amount = hit.getContent().amount();
                    if (amount < 100) return "0-100";
                    else if (amount < 500) return "100-500";
                    else if (amount < 1000) return "500-1000";
                    else return "1000+";
                },
                Collectors.counting()
            ));
    }
    
    /**
     * Nested aggregation - Category stats by region
     */
    public Map<String, Map<String, Long>> getCategoryStatsByRegion() {
        Query query = Query.findAll();
        SearchHits<Sale> searchHits = elasticsearchOperations.search(query, Sale.class, IndexCoordinates.of(INDEX_NAME));
        
        return searchHits.stream()
            .collect(Collectors.groupingBy(
                hit -> hit.getContent().region(),
                Collectors.groupingBy(
                    hit -> hit.getContent().category(),
                    Collectors.counting()
                )
            ));
    }
    
    /**
     * Statistical aggregation - Quantity statistics
     */
    public Map<String, Double> getQuantityStatistics() {
        Query query = Query.findAll();
        SearchHits<Sale> searchHits = elasticsearchOperations.search(query, Sale.class, IndexCoordinates.of(INDEX_NAME));
        
        IntSummaryStatistics stats = searchHits.stream()
            .mapToInt(hit -> hit.getContent().quantity())
            .summaryStatistics();
        
        return Map.of(
            "count", (double) stats.getCount(),
            "sum", (double) stats.getSum(),
            "avg", stats.getAverage(),
            "min", (double) stats.getMin(),
            "max", (double) stats.getMax()
        );
    }
    
    /**
     * Top N aggregation - Top customers by sales count
     */
    public Map<String, Long> getTopCustomers(int limit) {
        Query query = Query.findAll();
        SearchHits<Sale> searchHits = elasticsearchOperations.search(query, Sale.class, IndexCoordinates.of(INDEX_NAME));
        
        return searchHits.stream()
            .collect(Collectors.groupingBy(
                hit -> hit.getContent().customer(),
                Collectors.counting()
            ))
            .entrySet()
            .stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    /**
     * Top products by revenue
     */
    public Map<String, Double> getTopProductsByRevenue(int limit) {
        Query query = Query.findAll();
        SearchHits<Sale> searchHits = elasticsearchOperations.search(query, Sale.class, IndexCoordinates.of(INDEX_NAME));
        
        return searchHits.stream()
            .collect(Collectors.groupingBy(
                hit -> hit.getContent().product(),
                Collectors.summingDouble(hit -> hit.getContent().amount())
            ))
            .entrySet()
            .stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    /**
     * Count total sales
     */
    public long countSales() {
        Query query = Query.findAll();
        return elasticsearchOperations.count(query, IndexCoordinates.of(INDEX_NAME));
    }
}

/**
 * REST controller for aggregation operations
 */
@RestController
@RequestMapping("/api/sales")
class SaleAggregationController {
    
    private final SaleAggregationService saleAggregationService;
    
    public SaleAggregationController(SaleAggregationService saleAggregationService) {
        this.saleAggregationService = saleAggregationService;
    }
    
    @PostMapping
    public ResponseEntity<Sale> indexSale(@RequestBody Sale sale) {
        return ResponseEntity.ok(saleAggregationService.indexSale(sale));
    }
    
    @PostMapping("/bulk")
    public ResponseEntity<List<Sale>> indexSalesBulk(@RequestBody List<Sale> sales) {
        return ResponseEntity.ok(saleAggregationService.indexSalesBulk(sales));
    }
    
    @GetMapping("/aggregations/average-amount")
    public ResponseEntity<Double> getAverageSaleAmount() {
        return ResponseEntity.ok(saleAggregationService.getAverageSaleAmount());
    }
    
    @GetMapping("/aggregations/total-amount")
    public ResponseEntity<Double> getTotalSalesAmount() {
        return ResponseEntity.ok(saleAggregationService.getTotalSalesAmount());
    }
    
    @GetMapping("/aggregations/min-max-amount")
    public ResponseEntity<Map<String, Double>> getMinMaxSaleAmount() {
        return ResponseEntity.ok(saleAggregationService.getMinMaxSaleAmount());
    }
    
    @GetMapping("/aggregations/by-category")
    public ResponseEntity<Map<String, Long>> getSalesByCategory() {
        return ResponseEntity.ok(saleAggregationService.getSalesByCategory());
    }
    
    @GetMapping("/aggregations/by-region")
    public ResponseEntity<Map<String, Long>> getSalesByRegion() {
        return ResponseEntity.ok(saleAggregationService.getSalesByRegion());
    }
    
    @GetMapping("/aggregations/total-by-category")
    public ResponseEntity<Map<String, Double>> getTotalAmountByCategory() {
        return ResponseEntity.ok(saleAggregationService.getTotalAmountByCategory());
    }
    
    @GetMapping("/aggregations/average-by-region")
    public ResponseEntity<Map<String, Double>> getAverageAmountByRegion() {
        return ResponseEntity.ok(saleAggregationService.getAverageAmountByRegion());
    }
    
    @GetMapping("/aggregations/by-amount-range")
    public ResponseEntity<Map<String, Long>> getSalesByAmountRange() {
        return ResponseEntity.ok(saleAggregationService.getSalesByAmountRange());
    }
    
    @GetMapping("/aggregations/category-by-region")
    public ResponseEntity<Map<String, Map<String, Long>>> getCategoryStatsByRegion() {
        return ResponseEntity.ok(saleAggregationService.getCategoryStatsByRegion());
    }
    
    @GetMapping("/aggregations/quantity-stats")
    public ResponseEntity<Map<String, Double>> getQuantityStatistics() {
        return ResponseEntity.ok(saleAggregationService.getQuantityStatistics());
    }
    
    @GetMapping("/aggregations/top-customers")
    public ResponseEntity<Map<String, Long>> getTopCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(saleAggregationService.getTopCustomers(limit));
    }
    
    @GetMapping("/aggregations/top-products")
    public ResponseEntity<Map<String, Double>> getTopProductsByRevenue(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(saleAggregationService.getTopProductsByRevenue(limit));
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> countSales() {
        return ResponseEntity.ok(saleAggregationService.countSales());
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("""
            Aggregation Query Pattern
            
            This pattern demonstrates Elasticsearch aggregations for analytics
            and data summarization.
            
            Aggregation Types:
            - Metrics aggregations: avg, sum, min, max, count, stats
            - Bucket aggregations: terms (grouping), range, date histogram
            - Nested aggregations: Multi-level grouping
            - Pipeline aggregations: Computed from other aggregations
            
            Features:
            - Average and total calculations
            - Min/Max statistics
            - Grouping by category, region, etc.
            - Range-based bucketing
            - Nested aggregations (region → category)
            - Top N queries (top customers, products)
            - Statistical summaries
            
            Use Cases:
            - Sales analytics dashboards
            - Revenue reporting
            - Customer analytics
            - Product performance analysis
            - Regional statistics
            - Trend analysis
            
            Endpoints:
            - POST /api/sales - Index sale
            - POST /api/sales/bulk - Bulk index sales
            - GET /api/sales/aggregations/average-amount - Average sale
            - GET /api/sales/aggregations/total-amount - Total revenue
            - GET /api/sales/aggregations/min-max-amount - Min/Max
            - GET /api/sales/aggregations/by-category - Sales by category
            - GET /api/sales/aggregations/by-region - Sales by region
            - GET /api/sales/aggregations/total-by-category - Revenue by category
            - GET /api/sales/aggregations/average-by-region - Avg by region
            - GET /api/sales/aggregations/by-amount-range - Amount ranges
            - GET /api/sales/aggregations/category-by-region - Nested agg
            - GET /api/sales/aggregations/quantity-stats - Quantity statistics
            - GET /api/sales/aggregations/top-customers?limit= - Top customers
            - GET /api/sales/aggregations/top-products?limit= - Top products
            - GET /api/sales/count - Count sales
            """);
    }
}
