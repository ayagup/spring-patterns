package com.example.mongodb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * Aggregation Pattern
 * 
 * Demonstrates MongoDB aggregation pipeline operations.
 * 
 * Aggregation Stages:
 * - $match: Filter documents
 * - $project: Select fields
 * - $group: Group by field
 * - $sort: Sort results
 * - $limit: Limit results
 * - $skip: Skip documents
 * - $unwind: Deconstruct arrays
 * - $lookup: Join collections
 * 
 * Aggregation Operators:
 * - $sum, $avg, $min, $max
 * - $count, $push, $addToSet
 * - $first, $last
 * 
 * Use Cases:
 * - Complex analytics
 * - Data aggregation
 * - Reporting
 * - Data transformations
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class AggregationPattern {

    @Bean
    public SalesAggregationService salesAggregationService(MongoTemplate mongoTemplate) {
        return new SalesAggregationService(mongoTemplate);
    }
}

@RestController
@RequestMapping("/api/mongo/aggregation")
class SalesAggregationService {

    private final MongoTemplate mongoTemplate;

    public SalesAggregationService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<CategoryTotal> getTotalsByCategory() {
        Aggregation aggregation = newAggregation(
            group("category").sum("amount").as("total"),
            project("total").and("_id").as("category"),
            sort(org.springframework.data.domain.Sort.Direction.DESC, "total")
        );
        
        AggregationResults<CategoryTotal> results = 
            mongoTemplate.aggregate(aggregation, "sales", CategoryTotal.class);
        
        return results.getMappedResults();
    }

    public List<MonthlyStats> getMonthlyStats() {
        Aggregation aggregation = newAggregation(
            project()
                .and("amount").as("amount")
                .and(DateOperators.dateOf("date").month()).as("month")
                .and(DateOperators.dateOf("date").year()).as("year"),
            group("month", "year")
                .sum("amount").as("totalSales")
                .count().as("orderCount")
                .avg("amount").as("averageAmount"),
            sort(org.springframework.data.domain.Sort.Direction.ASC, "year", "month")
        );
        
        AggregationResults<MonthlyStats> results = 
            mongoTemplate.aggregate(aggregation, "sales", MonthlyStats.class);
        
        return results.getMappedResults();
    }

    public List<TopProduct> getTopProducts(int limit) {
        Aggregation aggregation = newAggregation(
            group("productId")
                .sum("quantity").as("totalQuantity")
                .sum("amount").as("totalRevenue")
                .count().as("orderCount"),
            project("totalQuantity", "totalRevenue", "orderCount")
                .and("_id").as("productId"),
            sort(org.springframework.data.domain.Sort.Direction.DESC, "totalRevenue"),
            limit(limit)
        );
        
        AggregationResults<TopProduct> results = 
            mongoTemplate.aggregate(aggregation, "sales", TopProduct.class);
        
        return results.getMappedResults();
    }

    record CategoryTotal(String category, double total) {}
    record MonthlyStats(int month, int year, double totalSales, 
                       long orderCount, double averageAmount) {}
    record TopProduct(String productId, long totalQuantity, 
                     double totalRevenue, long orderCount) {}
}

@RestController
@RequestMapping("/api/mongo/aggregation")
class AggregationController {

    private final SalesAggregationService service;

    public AggregationController(SalesAggregationService service) {
        this.service = service;
    }

    @GetMapping("/category-totals")
    public ResponseEntity<List<SalesAggregationService.CategoryTotal>> getCategoryTotals() {
        return ResponseEntity.ok(service.getTotalsByCategory());
    }

    @GetMapping("/monthly-stats")
    public ResponseEntity<List<SalesAggregationService.MonthlyStats>> getMonthlyStats() {
        return ResponseEntity.ok(service.getMonthlyStats());
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<SalesAggregationService.TopProduct>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.getTopProducts(limit));
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getInfo() {
        return ResponseEntity.ok(new PatternInfo(
            "Aggregation Pattern",
            "MongoDB aggregation pipeline operations",
            "1.0",
            List.of("$match", "$group", "$project", "$sort", "$limit"),
            List.of("Analytics", "Reporting", "Data transformation")
        ));
    }

    record PatternInfo(String name, String description, String version,
                      List<String> features, List<String> useCases) {}
}
