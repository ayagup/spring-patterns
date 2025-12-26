package com.example.elasticsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Geo Query Pattern
 * 
 * Demonstrates Elasticsearch geospatial queries for location-based
 * search including geo-point, geo-shape, and geo-distance queries.
 * 
 * Key concepts:
 * - Geo-point queries (point locations)
 * - Geo-distance queries (within radius)
 * - Geo-bounding box queries
 * - Geo-polygon queries
 * - Distance sorting
 * - Geospatial aggregations
 * 
 * Use cases:
 * - Location-based search
 * - "Find nearby" features
 * - Delivery zone matching
 * - Store locator
 * - Geofencing
 */
@SpringBootApplication
public class GeoQueryPattern {

    public static void main(String[] args) {
        SpringApplication.run(GeoQueryPattern.class, args);
    }
}

/**
 * Location with geo-point
 */
record GeoPoint(
    double lat,
    double lon
) {
    @Override
    public String toString() {
        return lat + "," + lon;
    }
}

/**
 * Venue document with geo-location
 */
record Venue(
    String id,
    String name,
    String type,
    String address,
    String city,
    String country,
    GeoPoint location,
    Double rating,
    List<String> amenities
) {}

/**
 * Geo-distance result with distance calculation
 */
record GeoDistanceResult(
    Venue venue,
    Double distance,
    String unit
) {}

/**
 * Service demonstrating geo queries
 */
@Service
class GeoQueryService {
    
    private final ElasticsearchOperations elasticsearchOperations;
    private static final String INDEX_NAME = "venues";
    
    public GeoQueryService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }
    
    /**
     * Index a venue
     */
    public Venue indexVenue(Venue venue) {
        IndexQuery indexQuery = new IndexQueryBuilder()
            .withId(venue.id())
            .withObject(venue)
            .build();
        
        elasticsearchOperations.index(indexQuery, IndexCoordinates.of(INDEX_NAME));
        return venue;
    }
    
    /**
     * Index multiple venues in bulk
     */
    public List<Venue> indexVenuesBulk(List<Venue> venues) {
        List<IndexQuery> queries = venues.stream()
            .map(venue -> new IndexQueryBuilder()
                .withId(venue.id())
                .withObject(venue)
                .build())
            .collect(Collectors.toList());
        
        elasticsearchOperations.bulkIndex(queries, IndexCoordinates.of(INDEX_NAME));
        return venues;
    }
    
    /**
     * Find venues within distance (geo-distance query)
     * Example: Find all venues within 5km of a point
     */
    public List<Venue> findVenuesWithinDistance(GeoPoint center, double distance, String unit) {
        // Note: Full geo-query support requires native Elasticsearch client
        // This is a simplified pattern demonstration
        
        Query query = Query.findAll();
        SearchHits<Venue> searchHits = elasticsearchOperations.search(query, Venue.class, IndexCoordinates.of(INDEX_NAME));
        
        // Filter by distance (simplified - in production, use Elasticsearch geo-distance query)
        return searchHits.stream()
            .map(SearchHit::getContent)
            .filter(venue -> {
                double dist = calculateDistance(center, venue.location());
                return convertDistance(dist, "km", unit) <= distance;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Find venues within bounding box
     * Example: Find all venues in a rectangular area
     */
    public List<Venue> findVenuesInBoundingBox(GeoPoint topLeft, GeoPoint bottomRight) {
        Query query = Query.findAll();
        SearchHits<Venue> searchHits = elasticsearchOperations.search(query, Venue.class, IndexCoordinates.of(INDEX_NAME));
        
        return searchHits.stream()
            .map(SearchHit::getContent)
            .filter(venue -> {
                GeoPoint loc = venue.location();
                return loc.lat() <= topLeft.lat() &&
                       loc.lat() >= bottomRight.lat() &&
                       loc.lon() >= topLeft.lon() &&
                       loc.lon() <= bottomRight.lon();
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Find nearest venues to a point
     * Sorted by distance
     */
    public List<GeoDistanceResult> findNearestVenues(GeoPoint center, int limit) {
        Query query = Query.findAll();
        SearchHits<Venue> searchHits = elasticsearchOperations.search(query, Venue.class, IndexCoordinates.of(INDEX_NAME));
        
        return searchHits.stream()
            .map(SearchHit::getContent)
            .map(venue -> {
                double distance = calculateDistance(center, venue.location());
                return new GeoDistanceResult(venue, distance, "km");
            })
            .sorted((a, b) -> Double.compare(a.distance(), b.distance()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Find venues by type within distance
     */
    public List<Venue> findVenuesByTypeWithinDistance(String type, GeoPoint center, double distance) {
        Criteria criteria = new Criteria("type").is(type);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Venue> searchHits = elasticsearchOperations.search(query, Venue.class, IndexCoordinates.of(INDEX_NAME));
        
        return searchHits.stream()
            .map(SearchHit::getContent)
            .filter(venue -> calculateDistance(center, venue.location()) <= distance)
            .collect(Collectors.toList());
    }
    
    /**
     * Find venues by city
     */
    public List<Venue> findVenuesByCity(String city) {
        Criteria criteria = new Criteria("city").is(city);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Venue> searchHits = elasticsearchOperations.search(query, Venue.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Find highly-rated venues within distance
     */
    public List<Venue> findHighRatedVenuesNearby(GeoPoint center, double distance, double minRating) {
        Criteria criteria = new Criteria("rating").greaterThanEqual(minRating);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Venue> searchHits = elasticsearchOperations.search(query, Venue.class, IndexCoordinates.of(INDEX_NAME));
        
        return searchHits.stream()
            .map(SearchHit::getContent)
            .filter(venue -> calculateDistance(center, venue.location()) <= distance)
            .collect(Collectors.toList());
    }
    
    /**
     * Find venues with specific amenity within distance
     */
    public List<Venue> findVenuesWithAmenityNearby(String amenity, GeoPoint center, double distance) {
        Criteria criteria = new Criteria("amenities").contains(amenity);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Venue> searchHits = elasticsearchOperations.search(query, Venue.class, IndexCoordinates.of(INDEX_NAME));
        
        return searchHits.stream()
            .map(SearchHit::getContent)
            .filter(venue -> calculateDistance(center, venue.location()) <= distance)
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate distance between two geo-points (Haversine formula)
     * Returns distance in kilometers
     */
    private double calculateDistance(GeoPoint point1, GeoPoint point2) {
        double earthRadius = 6371; // km
        
        double lat1Rad = Math.toRadians(point1.lat());
        double lat2Rad = Math.toRadians(point2.lat());
        double deltaLat = Math.toRadians(point2.lat() - point1.lat());
        double deltaLon = Math.toRadians(point2.lon() - point1.lon());
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return earthRadius * c;
    }
    
    /**
     * Convert distance between units
     */
    private double convertDistance(double distance, String fromUnit, String toUnit) {
        // Convert to km first
        double distanceInKm = switch (fromUnit.toLowerCase()) {
            case "m", "meters" -> distance / 1000;
            case "km", "kilometers" -> distance;
            case "mi", "miles" -> distance * 1.60934;
            default -> distance;
        };
        
        // Convert from km to target unit
        return switch (toUnit.toLowerCase()) {
            case "m", "meters" -> distanceInKm * 1000;
            case "km", "kilometers" -> distanceInKm;
            case "mi", "miles" -> distanceInKm / 1.60934;
            default -> distanceInKm;
        };
    }
    
    /**
     * Get venue by ID
     */
    public Venue getVenue(String id) {
        return elasticsearchOperations.get(id, Venue.class, IndexCoordinates.of(INDEX_NAME));
    }
    
    /**
     * Count venues
     */
    public long countVenues() {
        Query query = Query.findAll();
        return elasticsearchOperations.count(query, IndexCoordinates.of(INDEX_NAME));
    }
}

/**
 * REST controller for geo queries
 */
@RestController
@RequestMapping("/api/venues")
class GeoQueryController {
    
    private final GeoQueryService geoQueryService;
    
    public GeoQueryController(GeoQueryService geoQueryService) {
        this.geoQueryService = geoQueryService;
    }
    
    @PostMapping
    public ResponseEntity<Venue> indexVenue(@RequestBody Venue venue) {
        return ResponseEntity.ok(geoQueryService.indexVenue(venue));
    }
    
    @PostMapping("/bulk")
    public ResponseEntity<List<Venue>> indexVenuesBulk(@RequestBody List<Venue> venues) {
        return ResponseEntity.ok(geoQueryService.indexVenuesBulk(venues));
    }
    
    @GetMapping("/nearby")
    public ResponseEntity<List<Venue>> findVenuesWithinDistance(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double distance,
            @RequestParam(defaultValue = "km") String unit) {
        GeoPoint center = new GeoPoint(lat, lon);
        return ResponseEntity.ok(geoQueryService.findVenuesWithinDistance(center, distance, unit));
    }
    
    @GetMapping("/in-box")
    public ResponseEntity<List<Venue>> findVenuesInBoundingBox(
            @RequestParam double topLat,
            @RequestParam double topLon,
            @RequestParam double bottomLat,
            @RequestParam double bottomLon) {
        GeoPoint topLeft = new GeoPoint(topLat, topLon);
        GeoPoint bottomRight = new GeoPoint(bottomLat, bottomLon);
        return ResponseEntity.ok(geoQueryService.findVenuesInBoundingBox(topLeft, bottomRight));
    }
    
    @GetMapping("/nearest")
    public ResponseEntity<List<GeoDistanceResult>> findNearestVenues(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "10") int limit) {
        GeoPoint center = new GeoPoint(lat, lon);
        return ResponseEntity.ok(geoQueryService.findNearestVenues(center, limit));
    }
    
    @GetMapping("/by-type-nearby")
    public ResponseEntity<List<Venue>> findVenuesByTypeWithinDistance(
            @RequestParam String type,
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double distance) {
        GeoPoint center = new GeoPoint(lat, lon);
        return ResponseEntity.ok(geoQueryService.findVenuesByTypeWithinDistance(type, center, distance));
    }
    
    @GetMapping("/by-city/{city}")
    public ResponseEntity<List<Venue>> findVenuesByCity(@PathVariable String city) {
        return ResponseEntity.ok(geoQueryService.findVenuesByCity(city));
    }
    
    @GetMapping("/high-rated-nearby")
    public ResponseEntity<List<Venue>> findHighRatedVenuesNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double distance,
            @RequestParam double minRating) {
        GeoPoint center = new GeoPoint(lat, lon);
        return ResponseEntity.ok(geoQueryService.findHighRatedVenuesNearby(center, distance, minRating));
    }
    
    @GetMapping("/with-amenity-nearby")
    public ResponseEntity<List<Venue>> findVenuesWithAmenityNearby(
            @RequestParam String amenity,
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double distance) {
        GeoPoint center = new GeoPoint(lat, lon);
        return ResponseEntity.ok(geoQueryService.findVenuesWithAmenityNearby(amenity, center, distance));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Venue> getVenue(@PathVariable String id) {
        Venue venue = geoQueryService.getVenue(id);
        return venue != null ? ResponseEntity.ok(venue) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> countVenues() {
        return ResponseEntity.ok(geoQueryService.countVenues());
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("""
            Geo Query Pattern
            
            This pattern demonstrates Elasticsearch geospatial queries for
            location-based search and proximity filtering.
            
            Geo Query Types:
            - Geo-distance: Find locations within radius of a point
            - Geo-bounding box: Find locations within rectangular area
            - Geo-polygon: Find locations within polygon (not shown)
            - Distance sorting: Sort results by proximity
            
            Geo Data Types:
            - Geo-point: Single latitude/longitude point
            - Geo-shape: Complex shapes (polygons, multi-polygons)
            
            Features:
            - Find nearby venues (within distance)
            - Bounding box search (rectangular area)
            - Nearest venues (sorted by distance)
            - Filter by type + proximity
            - Filter by rating + proximity
            - Filter by amenities + proximity
            - Distance calculation (Haversine formula)
            - Unit conversion (km, m, mi)
            
            Distance Units:
            - km/kilometers: Kilometers
            - m/meters: Meters
            - mi/miles: Miles
            
            Use Cases:
            - Store locator ("Find stores near me")
            - Delivery zone matching
            - Real estate search ("Homes within 10km of downtown")
            - Restaurant finder
            - Hotel search by location
            - Geofencing alerts
            
            Endpoints:
            - POST /api/venues - Index venue
            - POST /api/venues/bulk - Bulk index venues
            - GET /api/venues/nearby?lat=&lon=&distance=&unit= - Within distance
            - GET /api/venues/in-box?topLat=&topLon=&bottomLat=&bottomLon= - Bounding box
            - GET /api/venues/nearest?lat=&lon=&limit= - Nearest venues
            - GET /api/venues/by-type-nearby?type=&lat=&lon=&distance= - Type + proximity
            - GET /api/venues/by-city/{city} - By city
            - GET /api/venues/high-rated-nearby?lat=&lon=&distance=&minRating= - Rating + proximity
            - GET /api/venues/with-amenity-nearby?amenity=&lat=&lon=&distance= - Amenity + proximity
            - GET /api/venues/{id} - Get venue
            - GET /api/venues/count - Count venues
            
            Example Queries:
            - Find restaurants within 2km: /api/venues/by-type-nearby?type=restaurant&lat=40.7128&lon=-74.0060&distance=2
            - Find nearest 5 venues: /api/venues/nearest?lat=40.7128&lon=-74.0060&limit=5
            - Find high-rated nearby: /api/venues/high-rated-nearby?lat=40.7128&lon=-74.0060&distance=5&minRating=4.0
            """);
    }
}
