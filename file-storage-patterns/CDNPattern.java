package com.example.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Content Delivery Network (CDN) Pattern
 * 
 * Purpose: Integration with CDN services for global content distribution.
 * Provides cache management, purging, and edge location optimization.
 * 
 * Key Features:
 * - CDN integration (CloudFront, Cloudflare, Akamai)
 * - Origin configuration
 * - Cache invalidation/purging
 * - TTL management
 * - Edge location distribution
 * - SSL/TLS support
 * - Geographic restrictions
 * - Access control
 * - Cache hit ratio tracking
 * - Custom error pages
 * 
 * Use Cases:
 * - Static asset delivery
 * - Media streaming
 * - Website acceleration
 * - API response caching
 * - Global content distribution
 * - DDoS protection
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class CDNPattern {

    public static void main(String[] args) {
        SpringApplication.run(CDNPattern.class, args);
    }

    /**
     * CDN Configuration
     */
    @Configuration
    public static class CDNConfig {
        
        @Bean
        public CDNProperties cdnProperties() {
            CDNProperties properties = new CDNProperties();
            properties.setProvider("cloudfront");
            properties.setDistributionId("EXAMPLE123");
            properties.setDomainName("d123abc.cloudfront.net");
            properties.setOriginDomain("my-bucket.s3.amazonaws.com");
            properties.setDefaultTtl(86400L); // 24 hours
            return properties;
        }

        @Bean
        public CDNManager cdnManager(CDNProperties properties) {
            return new CDNManager(properties);
        }
    }

    /**
     * CDN Controller
     */
    @RestController
    @RequestMapping("/api/cdn")
    public static class CDNController {

        private final CDNService cdnService;

        public CDNController(CDNService cdnService) {
            this.cdnService = cdnService;
        }

        /**
         * Upload content to CDN
         */
        @PostMapping("/upload")
        public ResponseEntity<CDNContentInfo> uploadContent(
                @RequestParam("file") MultipartFile file,
                @RequestParam(required = false) String path,
                @RequestParam(required = false) Long ttl,
                @RequestParam(required = false) Map<String, String> headers) {
            
            try {
                CDNContentInfo contentInfo = cdnService.uploadContent(
                    file, path, ttl, headers
                );
                return ResponseEntity.ok(contentInfo);
            } catch (IOException e) {
                throw new CDNException("Upload failed", e);
            }
        }

        /**
         * Get CDN URL for content
         */
        @GetMapping("/url")
        public ResponseEntity<Map<String, String>> getContentUrl(
                @RequestParam String path,
                @RequestParam(required = false) boolean signed,
                @RequestParam(required = false) Integer expirationMinutes) {
            
            String url = signed 
                ? cdnService.getSignedUrl(path, expirationMinutes != null ? expirationMinutes : 60)
                : cdnService.getPublicUrl(path);
            
            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("path", path);
            response.put("signed", String.valueOf(signed));
            
            return ResponseEntity.ok(response);
        }

        /**
         * Invalidate cache
         */
        @PostMapping("/invalidate")
        public ResponseEntity<InvalidationResult> invalidateCache(
                @RequestBody List<String> paths) {
            
            InvalidationResult result = cdnService.invalidateCache(paths);
            return ResponseEntity.ok(result);
        }

        /**
         * Purge entire cache
         */
        @PostMapping("/purge")
        public ResponseEntity<Map<String, String>> purgeCache() {
            cdnService.purgeCache();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache purged successfully");
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        }

        /**
         * Set cache TTL for path
         */
        @PutMapping("/ttl")
        public ResponseEntity<Map<String, Object>> setCacheTTL(
                @RequestParam String path,
                @RequestParam long ttl) {
            
            cdnService.setCacheTTL(path, ttl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "TTL updated successfully");
            response.put("path", path);
            response.put("ttl", ttl);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Get cache statistics
         */
        @GetMapping("/stats")
        public ResponseEntity<CDNStats> getStats() {
            return ResponseEntity.ok(cdnService.getStats());
        }

        /**
         * Get cache hit ratio
         */
        @GetMapping("/hit-ratio")
        public ResponseEntity<Map<String, Object>> getCacheHitRatio() {
            CDNStats stats = cdnService.getStats();
            
            double hitRatio = stats.getTotalRequests() > 0 
                ? (double) stats.getCacheHits() / stats.getTotalRequests() * 100 
                : 0.0;
            
            Map<String, Object> response = new HashMap<>();
            response.put("hitRatio", hitRatio);
            response.put("cacheHits", stats.getCacheHits());
            response.put("cacheMisses", stats.getCacheMisses());
            response.put("totalRequests", stats.getTotalRequests());
            
            return ResponseEntity.ok(response);
        }

        /**
         * List cached content
         */
        @GetMapping("/list")
        public ResponseEntity<List<CDNContentInfo>> listContent(
                @RequestParam(required = false) String prefix) {
            
            List<CDNContentInfo> content = cdnService.listContent(prefix);
            return ResponseEntity.ok(content);
        }

        /**
         * Delete content from CDN
         */
        @DeleteMapping("/content")
        public ResponseEntity<Map<String, String>> deleteContent(@RequestParam String path) {
            cdnService.deleteContent(path);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Content deleted successfully");
            response.put("path", path);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Configure geographic restrictions
         */
        @PostMapping("/geo-restrict")
        public ResponseEntity<Map<String, Object>> setGeoRestrictions(
                @RequestBody GeoRestrictionConfig config) {
            
            cdnService.setGeoRestrictions(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Geographic restrictions updated");
            response.put("type", config.getRestrictionType());
            response.put("countries", config.getCountries());
            
            return ResponseEntity.ok(response);
        }

        /**
         * Set custom error page
         */
        @PostMapping("/error-page")
        public ResponseEntity<Map<String, String>> setErrorPage(
                @RequestParam int errorCode,
                @RequestParam String pagePath) {
            
            cdnService.setCustomErrorPage(errorCode, pagePath);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Custom error page configured");
            response.put("errorCode", String.valueOf(errorCode));
            response.put("pagePath", pagePath);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Enable/disable compression
         */
        @PutMapping("/compression")
        public ResponseEntity<Map<String, Boolean>> setCompression(
                @RequestParam boolean enabled) {
            
            cdnService.setCompressionEnabled(enabled);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("compressionEnabled", enabled);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Get edge location statistics
         */
        @GetMapping("/edge-locations")
        public ResponseEntity<List<EdgeLocationStats>> getEdgeLocationStats() {
            return ResponseEntity.ok(cdnService.getEdgeLocationStats());
        }
    }

    /**
     * CDN Service
     */
    @Service
    public static class CDNService {

        private final CDNManager cdnManager;

        public CDNService(CDNManager cdnManager) {
            this.cdnManager = cdnManager;
        }

        public CDNContentInfo uploadContent(MultipartFile file, String path, 
                                           Long ttl, Map<String, String> headers) 
                throws IOException {
            
            if (path == null || path.isEmpty()) {
                path = generateContentPath(file.getOriginalFilename());
            }
            
            return cdnManager.uploadContent(path, file, ttl, headers);
        }

        public String getPublicUrl(String path) {
            return cdnManager.getPublicUrl(path);
        }

        public String getSignedUrl(String path, int expirationMinutes) {
            return cdnManager.getSignedUrl(path, expirationMinutes);
        }

        public InvalidationResult invalidateCache(List<String> paths) {
            return cdnManager.invalidateCache(paths);
        }

        public void purgeCache() {
            cdnManager.purgeCache();
        }

        public void setCacheTTL(String path, long ttl) {
            cdnManager.setCacheTTL(path, ttl);
        }

        public CDNStats getStats() {
            return cdnManager.getStats();
        }

        public List<CDNContentInfo> listContent(String prefix) {
            return cdnManager.listContent(prefix);
        }

        public void deleteContent(String path) {
            cdnManager.deleteContent(path);
        }

        public void setGeoRestrictions(GeoRestrictionConfig config) {
            cdnManager.setGeoRestrictions(config);
        }

        public void setCustomErrorPage(int errorCode, String pagePath) {
            cdnManager.setCustomErrorPage(errorCode, pagePath);
        }

        public void setCompressionEnabled(boolean enabled) {
            cdnManager.setCompressionEnabled(enabled);
        }

        public List<EdgeLocationStats> getEdgeLocationStats() {
            return cdnManager.getEdgeLocationStats();
        }

        private String generateContentPath(String filename) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            return "content/" + timestamp + "/" + filename;
        }
    }

    /**
     * CDN Manager
     */
    public static class CDNManager {
        
        private final CDNProperties properties;
        private final Map<String, CDNContent> contentCache = new ConcurrentHashMap<>();
        private final Map<String, Long> cacheTTLs = new ConcurrentHashMap<>();
        private long totalRequests = 0;
        private long cacheHits = 0;
        private long cacheMisses = 0;
        private long totalInvalidations = 0;
        private boolean compressionEnabled = true;
        private GeoRestrictionConfig geoRestrictions;
        private final Map<Integer, String> customErrorPages = new ConcurrentHashMap<>();

        public CDNManager(CDNProperties properties) {
            this.properties = properties;
        }

        @PostConstruct
        public void init() {
            System.out.println("CDN Manager initialized");
            System.out.println("Provider: " + properties.getProvider());
            System.out.println("Distribution: " + properties.getDistributionId());
            System.out.println("Domain: " + properties.getDomainName());
            System.out.println("Origin: " + properties.getOriginDomain());
        }

        /**
         * Upload content
         */
        public CDNContentInfo uploadContent(String path, MultipartFile file, 
                                           Long ttl, Map<String, String> headers) 
                throws IOException {
            
            byte[] data = file.readAllBytes();
            
            CDNContent content = new CDNContent(
                path,
                data,
                file.getContentType(),
                ttl != null ? ttl : properties.getDefaultTtl(),
                headers != null ? headers : new HashMap<>(),
                LocalDateTime.now()
            );
            
            contentCache.put(path, content);
            
            if (ttl != null) {
                cacheTTLs.put(path, ttl);
            }
            
            return new CDNContentInfo(
                path,
                getPublicUrl(path),
                data.length,
                file.getContentType(),
                content.getTtl(),
                content.getUploadedAt()
            );
        }

        /**
         * Get public URL
         */
        public String getPublicUrl(String path) {
            return "https://" + properties.getDomainName() + "/" + path;
        }

        /**
         * Get signed URL
         */
        public String getSignedUrl(String path, int expirationMinutes) {
            long expirationTime = System.currentTimeMillis() + 
                                TimeUnit.MINUTES.toMillis(expirationMinutes);
            
            return "https://" + properties.getDomainName() + "/" + path + 
                   "?Expires=" + expirationTime + 
                   "&Signature=mock-signature";
        }

        /**
         * Invalidate cache
         */
        public InvalidationResult invalidateCache(List<String> paths) {
            String invalidationId = UUID.randomUUID().toString();
            
            for (String path : paths) {
                contentCache.remove(path);
            }
            
            totalInvalidations += paths.size();
            
            return new InvalidationResult(
                invalidationId,
                paths,
                "completed",
                LocalDateTime.now()
            );
        }

        /**
         * Purge entire cache
         */
        public void purgeCache() {
            totalInvalidations += contentCache.size();
            contentCache.clear();
            cacheTTLs.clear();
        }

        /**
         * Set cache TTL
         */
        public void setCacheTTL(String path, long ttl) {
            cacheTTLs.put(path, ttl);
            
            CDNContent content = contentCache.get(path);
            if (content != null) {
                content.setTtl(ttl);
            }
        }

        /**
         * Get statistics
         */
        public CDNStats getStats() {
            long totalSize = contentCache.values().stream()
                .mapToLong(c -> c.getData().length)
                .sum();
            
            long bandwidthSaved = (long) (totalSize * (cacheHits / (double) Math.max(1, totalRequests)));
            
            return new CDNStats(
                contentCache.size(),
                totalSize,
                totalRequests,
                cacheHits,
                cacheMisses,
                totalInvalidations,
                bandwidthSaved,
                compressionEnabled,
                LocalDateTime.now()
            );
        }

        /**
         * List content
         */
        public List<CDNContentInfo> listContent(String prefix) {
            return contentCache.entrySet().stream()
                .filter(entry -> prefix == null || entry.getKey().startsWith(prefix))
                .map(entry -> {
                    CDNContent content = entry.getValue();
                    return new CDNContentInfo(
                        entry.getKey(),
                        getPublicUrl(entry.getKey()),
                        content.getData().length,
                        content.getContentType(),
                        content.getTtl(),
                        content.getUploadedAt()
                    );
                })
                .collect(Collectors.toList());
        }

        /**
         * Delete content
         */
        public void deleteContent(String path) {
            contentCache.remove(path);
            cacheTTLs.remove(path);
        }

        /**
         * Set geographic restrictions
         */
        public void setGeoRestrictions(GeoRestrictionConfig config) {
            this.geoRestrictions = config;
        }

        /**
         * Set custom error page
         */
        public void setCustomErrorPage(int errorCode, String pagePath) {
            customErrorPages.put(errorCode, pagePath);
        }

        /**
         * Set compression enabled
         */
        public void setCompressionEnabled(boolean enabled) {
            this.compressionEnabled = enabled;
        }

        /**
         * Get edge location statistics
         */
        public List<EdgeLocationStats> getEdgeLocationStats() {
            // Mock edge locations
            return Arrays.asList(
                new EdgeLocationStats("us-east-1", "Virginia", cacheHits / 3, cacheMisses / 3),
                new EdgeLocationStats("us-west-1", "California", cacheHits / 3, cacheMisses / 3),
                new EdgeLocationStats("eu-west-1", "Ireland", cacheHits / 3, cacheMisses / 3)
            );
        }

        /**
         * Simulate cache request
         */
        public void recordRequest(String path, boolean hit) {
            totalRequests++;
            if (hit) {
                cacheHits++;
            } else {
                cacheMisses++;
            }
        }
    }

    // Model Classes

    public static class CDNContentInfo {
        private String path;
        private String url;
        private long size;
        private String contentType;
        private long ttl;
        private LocalDateTime uploadedAt;

        public CDNContentInfo(String path, String url, long size, String contentType,
                             long ttl, LocalDateTime uploadedAt) {
            this.path = path;
            this.url = url;
            this.size = size;
            this.contentType = contentType;
            this.ttl = ttl;
            this.uploadedAt = uploadedAt;
        }

        // Getters
        public String getPath() { return path; }
        public String getUrl() { return url; }
        public long getSize() { return size; }
        public String getContentType() { return contentType; }
        public long getTtl() { return ttl; }
        public LocalDateTime getUploadedAt() { return uploadedAt; }
    }

    public static class CDNContent {
        private String path;
        private byte[] data;
        private String contentType;
        private long ttl;
        private Map<String, String> headers;
        private LocalDateTime uploadedAt;

        public CDNContent(String path, byte[] data, String contentType, long ttl,
                         Map<String, String> headers, LocalDateTime uploadedAt) {
            this.path = path;
            this.data = data;
            this.contentType = contentType;
            this.ttl = ttl;
            this.headers = headers;
            this.uploadedAt = uploadedAt;
        }

        // Getters and Setters
        public String getPath() { return path; }
        public byte[] getData() { return data; }
        public String getContentType() { return contentType; }
        public long getTtl() { return ttl; }
        public void setTtl(long ttl) { this.ttl = ttl; }
        public Map<String, String> getHeaders() { return headers; }
        public LocalDateTime getUploadedAt() { return uploadedAt; }
    }

    public static class CDNProperties {
        private String provider;
        private String distributionId;
        private String domainName;
        private String originDomain;
        private Long defaultTtl;

        // Getters and Setters
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getDistributionId() { return distributionId; }
        public void setDistributionId(String distributionId) { 
            this.distributionId = distributionId; 
        }
        public String getDomainName() { return domainName; }
        public void setDomainName(String domainName) { this.domainName = domainName; }
        public String getOriginDomain() { return originDomain; }
        public void setOriginDomain(String originDomain) { this.originDomain = originDomain; }
        public Long getDefaultTtl() { return defaultTtl; }
        public void setDefaultTtl(Long defaultTtl) { this.defaultTtl = defaultTtl; }
    }

    public static class InvalidationResult {
        private String invalidationId;
        private List<String> paths;
        private String status;
        private LocalDateTime timestamp;

        public InvalidationResult(String invalidationId, List<String> paths, 
                                 String status, LocalDateTime timestamp) {
            this.invalidationId = invalidationId;
            this.paths = paths;
            this.status = status;
            this.timestamp = timestamp;
        }

        // Getters
        public String getInvalidationId() { return invalidationId; }
        public List<String> getPaths() { return paths; }
        public String getStatus() { return status; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class CDNStats {
        private long totalContent;
        private long totalSize;
        private long totalRequests;
        private long cacheHits;
        private long cacheMisses;
        private long totalInvalidations;
        private long bandwidthSaved;
        private boolean compressionEnabled;
        private LocalDateTime timestamp;

        public CDNStats(long totalContent, long totalSize, long totalRequests,
                       long cacheHits, long cacheMisses, long totalInvalidations,
                       long bandwidthSaved, boolean compressionEnabled,
                       LocalDateTime timestamp) {
            this.totalContent = totalContent;
            this.totalSize = totalSize;
            this.totalRequests = totalRequests;
            this.cacheHits = cacheHits;
            this.cacheMisses = cacheMisses;
            this.totalInvalidations = totalInvalidations;
            this.bandwidthSaved = bandwidthSaved;
            this.compressionEnabled = compressionEnabled;
            this.timestamp = timestamp;
        }

        // Getters
        public long getTotalContent() { return totalContent; }
        public long getTotalSize() { return totalSize; }
        public long getTotalRequests() { return totalRequests; }
        public long getCacheHits() { return cacheHits; }
        public long getCacheMisses() { return cacheMisses; }
        public long getTotalInvalidations() { return totalInvalidations; }
        public long getBandwidthSaved() { return bandwidthSaved; }
        public boolean isCompressionEnabled() { return compressionEnabled; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class GeoRestrictionConfig {
        private String restrictionType; // "whitelist" or "blacklist"
        private List<String> countries;

        // Getters and Setters
        public String getRestrictionType() { return restrictionType; }
        public void setRestrictionType(String restrictionType) { 
            this.restrictionType = restrictionType; 
        }
        public List<String> getCountries() { return countries; }
        public void setCountries(List<String> countries) { this.countries = countries; }
    }

    public static class EdgeLocationStats {
        private String locationCode;
        private String locationName;
        private long requests;
        private long misses;

        public EdgeLocationStats(String locationCode, String locationName, 
                                long requests, long misses) {
            this.locationCode = locationCode;
            this.locationName = locationName;
            this.requests = requests;
            this.misses = misses;
        }

        // Getters
        public String getLocationCode() { return locationCode; }
        public String getLocationName() { return locationName; }
        public long getRequests() { return requests; }
        public long getMisses() { return misses; }
    }

    public static class CDNException extends RuntimeException {
        public CDNException(String message) {
            super(message);
        }

        public CDNException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

/*
 * Application Properties:
 * 
 * # CDN Configuration
 * cdn.provider=cloudfront
 * cdn.distribution-id=EXAMPLE123
 * cdn.domain-name=d123abc.cloudfront.net
 * cdn.origin-domain=my-bucket.s3.amazonaws.com
 * cdn.default-ttl=86400
 */
