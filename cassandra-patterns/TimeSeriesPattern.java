package com.example.cassandra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Time Series Pattern
 * 
 * Demonstrates best practices for storing and querying time-series data
 * in Apache Cassandra with efficient data modeling.
 * 
 * Key concepts:
 * - Time bucketing (partition by time window)
 * - Composite clustering keys
 * - Time-based queries
 * - Data retention and TTL
 * - Compaction strategies for time-series
 * - Time window compaction strategy (TWCS)
 * 
 * Use cases:
 * - IoT sensor data
 * - Application metrics
 * - Log aggregation
 * - Financial tick data
 * - User activity tracking
 * - System monitoring
 */
@SpringBootApplication
public class TimeSeriesPattern {

    public static void main(String[] args) {
        SpringApplication.run(TimeSeriesPattern.class, args);
    }
}

/**
 * Metric data point with time bucketing
 */
record MetricDataPoint(
    String metricName,
    String bucket,      // Time bucket: "2024-12-26" for daily, "2024-12" for monthly
    LocalDateTime timestamp,
    Double value,
    String unit,
    String host,
    String tags
) {
    public MetricDataPoint {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (bucket == null) {
            // Default to daily bucket
            bucket = timestamp.toLocalDate().toString();
        }
    }
}

/**
 * Sensor reading with composite clustering key
 */
record SensorReading(
    String sensorId,
    String bucket,      // Partition key bucket (e.g., "2024-12-26")
    LocalDateTime timestamp,
    Double temperature,
    Double humidity,
    Double pressure,
    String location
) {
    public SensorReading {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (bucket == null) {
            bucket = timestamp.toLocalDate().toString();
        }
    }
}

/**
 * Service demonstrating time-series patterns
 */
@Service
class TimeSeriesService {
    
    private final CassandraTemplate cassandraTemplate;
    private final CqlTemplate cqlTemplate;
    
    public TimeSeriesService(CassandraTemplate cassandraTemplate, CqlTemplate cqlTemplate) {
        this.cassandraTemplate = cassandraTemplate;
        this.cqlTemplate = cqlTemplate;
    }
    
    /**
     * Insert metric data point with TTL
     */
    public void insertMetric(MetricDataPoint dataPoint, int ttlSeconds) {
        String cql = """
            INSERT INTO metrics (metric_name, bucket, timestamp, value, unit, host, tags)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            USING TTL ?
            """;
        
        cqlTemplate.execute(cql,
            dataPoint.metricName(),
            dataPoint.bucket(),
            dataPoint.timestamp(),
            dataPoint.value(),
            dataPoint.unit(),
            dataPoint.host(),
            dataPoint.tags(),
            ttlSeconds
        );
    }
    
    /**
     * Insert sensor reading
     */
    public void insertSensorReading(SensorReading reading) {
        String cql = """
            INSERT INTO sensor_readings (sensor_id, bucket, timestamp, temperature, humidity, pressure, location)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        cqlTemplate.execute(cql,
            reading.sensorId(),
            reading.bucket(),
            reading.timestamp(),
            reading.temperature(),
            reading.humidity(),
            reading.pressure(),
            reading.location()
        );
    }
    
    /**
     * Insert multiple metrics in batch
     */
    public void insertMetricsBatch(List<MetricDataPoint> dataPoints, int ttlSeconds) {
        StringBuilder cql = new StringBuilder("BEGIN UNLOGGED BATCH\n");
        
        for (MetricDataPoint dp : dataPoints) {
            cql.append(String.format(
                "INSERT INTO metrics (metric_name, bucket, timestamp, value, unit, host, tags) " +
                "VALUES ('%s', '%s', '%s', %f, '%s', '%s', '%s') USING TTL %d;\n",
                dp.metricName(), dp.bucket(), dp.timestamp(), dp.value(),
                dp.unit(), dp.host(), dp.tags(), ttlSeconds
            ));
        }
        
        cql.append("APPLY BATCH;");
        cqlTemplate.execute(cql.toString());
    }
    
    /**
     * Query metrics for a specific time range within a bucket
     */
    public List<MetricDataPoint> findMetricsByTimeRange(
            String metricName, 
            String bucket,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        
        String cql = """
            SELECT * FROM metrics
            WHERE metric_name = ? AND bucket = ?
            AND timestamp >= ? AND timestamp <= ?
            ORDER BY timestamp ASC
            """;
        
        return cqlTemplate.query(cql, (row, rowNum) -> 
            new MetricDataPoint(
                row.getString("metric_name"),
                row.getString("bucket"),
                row.get("timestamp", LocalDateTime.class),
                row.getDouble("value"),
                row.getString("unit"),
                row.getString("host"),
                row.getString("tags")
            ), metricName, bucket, startTime, endTime);
    }
    
    /**
     * Query sensor readings for a sensor in a time range
     */
    public List<SensorReading> findSensorReadingsByTimeRange(
            String sensorId,
            String bucket,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        
        String cql = """
            SELECT * FROM sensor_readings
            WHERE sensor_id = ? AND bucket = ?
            AND timestamp >= ? AND timestamp <= ?
            ORDER BY timestamp DESC
            """;
        
        return cqlTemplate.query(cql, (row, rowNum) -> 
            new SensorReading(
                row.getString("sensor_id"),
                row.getString("bucket"),
                row.get("timestamp", LocalDateTime.class),
                row.getDouble("temperature"),
                row.getDouble("humidity"),
                row.getDouble("pressure"),
                row.getString("location")
            ), sensorId, bucket, startTime, endTime);
    }
    
    /**
     * Get latest N readings for a sensor
     */
    public List<SensorReading> getLatestSensorReadings(String sensorId, String bucket, int limit) {
        String cql = """
            SELECT * FROM sensor_readings
            WHERE sensor_id = ? AND bucket = ?
            ORDER BY timestamp DESC
            LIMIT ?
            """;
        
        return cqlTemplate.query(cql, (row, rowNum) -> 
            new SensorReading(
                row.getString("sensor_id"),
                row.getString("bucket"),
                row.get("timestamp", LocalDateTime.class),
                row.getDouble("temperature"),
                row.getDouble("humidity"),
                row.getDouble("pressure"),
                row.getString("location")
            ), sensorId, bucket, limit);
    }
    
    /**
     * Calculate average metric value for a time range
     */
    public Double getAverageMetricValue(String metricName, String bucket) {
        String cql = """
            SELECT AVG(value) AS avg_value
            FROM metrics
            WHERE metric_name = ? AND bucket = ?
            """;
        
        return cqlTemplate.queryForObject(cql, Double.class, metricName, bucket);
    }
    
    /**
     * Calculate min/max for a time range
     */
    public record MinMax(Double min, Double max) {}
    
    public MinMax getMinMaxMetricValue(String metricName, String bucket) {
        String cql = """
            SELECT MIN(value) AS min_value, MAX(value) AS max_value
            FROM metrics
            WHERE metric_name = ? AND bucket = ?
            """;
        
        return cqlTemplate.queryForObject(cql, (row, rowNum) -> 
            new MinMax(row.getDouble("min_value"), row.getDouble("max_value")),
            metricName, bucket);
    }
    
    /**
     * Count data points in a bucket
     */
    public Long countMetricsInBucket(String metricName, String bucket) {
        String cql = """
            SELECT COUNT(*) FROM metrics
            WHERE metric_name = ? AND bucket = ?
            """;
        
        return cqlTemplate.queryForObject(cql, Long.class, metricName, bucket);
    }
    
    /**
     * Delete old partitions (data retention)
     */
    public void deleteOldMetrics(String metricName, String oldBucket) {
        String cql = "DELETE FROM metrics WHERE metric_name = ? AND bucket = ?";
        cqlTemplate.execute(cql, metricName, oldBucket);
    }
    
    /**
     * Generate time bucket for given timestamp and granularity
     */
    public String generateBucket(LocalDateTime timestamp, String granularity) {
        return switch (granularity.toLowerCase()) {
            case "hour" -> timestamp.truncatedTo(ChronoUnit.HOURS).toString();
            case "day" -> timestamp.toLocalDate().toString();
            case "month" -> timestamp.getYear() + "-" + String.format("%02d", timestamp.getMonthValue());
            case "year" -> String.valueOf(timestamp.getYear());
            default -> timestamp.toLocalDate().toString(); // Default to daily
        };
    }
}

/**
 * REST controller for time-series operations
 */
@RestController
@RequestMapping("/api/timeseries")
class TimeSeriesController {
    
    private final TimeSeriesService timeSeriesService;
    
    public TimeSeriesController(TimeSeriesService timeSeriesService) {
        this.timeSeriesService = timeSeriesService;
    }
    
    @PostMapping("/metrics")
    public ResponseEntity<String> insertMetric(
            @RequestBody MetricDataPoint dataPoint,
            @RequestParam(defaultValue = "86400") int ttlSeconds) {
        timeSeriesService.insertMetric(dataPoint, ttlSeconds);
        return ResponseEntity.ok("Metric inserted with TTL: " + ttlSeconds + " seconds");
    }
    
    @PostMapping("/metrics/batch")
    public ResponseEntity<String> insertMetricsBatch(
            @RequestBody List<MetricDataPoint> dataPoints,
            @RequestParam(defaultValue = "86400") int ttlSeconds) {
        timeSeriesService.insertMetricsBatch(dataPoints, ttlSeconds);
        return ResponseEntity.ok("Metrics batch inserted");
    }
    
    @PostMapping("/sensors")
    public ResponseEntity<String> insertSensorReading(@RequestBody SensorReading reading) {
        timeSeriesService.insertSensorReading(reading);
        return ResponseEntity.ok("Sensor reading inserted");
    }
    
    @GetMapping("/metrics/{metricName}")
    public ResponseEntity<List<MetricDataPoint>> getMetrics(
            @PathVariable String metricName,
            @RequestParam String bucket,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        List<MetricDataPoint> metrics = timeSeriesService.findMetricsByTimeRange(
            metricName, bucket,
            LocalDateTime.parse(startTime),
            LocalDateTime.parse(endTime)
        );
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/sensors/{sensorId}")
    public ResponseEntity<List<SensorReading>> getSensorReadings(
            @PathVariable String sensorId,
            @RequestParam String bucket,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        List<SensorReading> readings = timeSeriesService.findSensorReadingsByTimeRange(
            sensorId, bucket,
            LocalDateTime.parse(startTime),
            LocalDateTime.parse(endTime)
        );
        return ResponseEntity.ok(readings);
    }
    
    @GetMapping("/sensors/{sensorId}/latest")
    public ResponseEntity<List<SensorReading>> getLatestReadings(
            @PathVariable String sensorId,
            @RequestParam String bucket,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<SensorReading> readings = timeSeriesService.getLatestSensorReadings(sensorId, bucket, limit);
        return ResponseEntity.ok(readings);
    }
    
    @GetMapping("/metrics/{metricName}/average")
    public ResponseEntity<Double> getAverageValue(
            @PathVariable String metricName,
            @RequestParam String bucket) {
        
        Double average = timeSeriesService.getAverageMetricValue(metricName, bucket);
        return ResponseEntity.ok(average);
    }
    
    @GetMapping("/metrics/{metricName}/minmax")
    public ResponseEntity<TimeSeriesService.MinMax> getMinMax(
            @PathVariable String metricName,
            @RequestParam String bucket) {
        
        TimeSeriesService.MinMax minMax = timeSeriesService.getMinMaxMetricValue(metricName, bucket);
        return ResponseEntity.ok(minMax);
    }
    
    @GetMapping("/metrics/{metricName}/count")
    public ResponseEntity<Long> countMetrics(
            @PathVariable String metricName,
            @RequestParam String bucket) {
        
        Long count = timeSeriesService.countMetricsInBucket(metricName, bucket);
        return ResponseEntity.ok(count);
    }
    
    @DeleteMapping("/metrics/{metricName}/bucket/{bucket}")
    public ResponseEntity<String> deleteOldMetrics(
            @PathVariable String metricName,
            @PathVariable String bucket) {
        
        timeSeriesService.deleteOldMetrics(metricName, bucket);
        return ResponseEntity.ok("Metrics deleted for bucket: " + bucket);
    }
    
    @GetMapping("/bucket")
    public ResponseEntity<String> generateBucket(
            @RequestParam String timestamp,
            @RequestParam(defaultValue = "day") String granularity) {
        
        String bucket = timeSeriesService.generateBucket(LocalDateTime.parse(timestamp), granularity);
        return ResponseEntity.ok(bucket);
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("""
            Time Series Pattern
            
            This pattern demonstrates best practices for storing and querying time-series data
            in Apache Cassandra with efficient data modeling.
            
            Features:
            - Time bucketing (partition by time window: hour/day/month/year)
            - Composite clustering keys (bucket + timestamp)
            - TTL for automatic data expiration
            - Efficient time-range queries
            - Aggregations (AVG, MIN, MAX, COUNT)
            - Batch inserts for high throughput
            - Data retention management
            
            Best Practices:
            - Use time bucketing to prevent hot partitions
            - Choose bucket size based on query patterns and data volume
            - Use TTL for automatic data cleanup
            - Use Time Window Compaction Strategy (TWCS)
            - Order by timestamp DESC for latest data queries
            - Use unlogged batches for different partitions
            
            Time Granularities:
            - hour: "2024-12-26T14:00:00"
            - day: "2024-12-26"
            - month: "2024-12"
            - year: "2024"
            
            Endpoints:
            - POST /api/timeseries/metrics - Insert metric with TTL
            - POST /api/timeseries/metrics/batch - Batch insert metrics
            - POST /api/timeseries/sensors - Insert sensor reading
            - GET /api/timeseries/metrics/{metricName} - Query metrics by time range
            - GET /api/timeseries/sensors/{sensorId} - Query sensor readings
            - GET /api/timeseries/sensors/{sensorId}/latest - Get latest readings
            - GET /api/timeseries/metrics/{metricName}/average - Calculate average
            - GET /api/timeseries/metrics/{metricName}/minmax - Get min/max
            - GET /api/timeseries/metrics/{metricName}/count - Count data points
            - DELETE /api/timeseries/metrics/{metricName}/bucket/{bucket} - Delete old data
            - GET /api/timeseries/bucket - Generate time bucket
            """);
    }
}
