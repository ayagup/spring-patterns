package com.example.cassandra;

import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.data.cassandra.core.cql.RowMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * CQL Template Pattern
 * 
 * Demonstrates the use of CqlTemplate for executing raw CQL statements
 * with Apache Cassandra, providing low-level control over queries.
 * 
 * Key concepts:
 * - CqlTemplate for raw CQL execution
 * - SimpleStatement for ad-hoc queries
 * - PreparedStatement for parameterized queries
 * - RowMapper for result mapping
 * - Batch statements
 * - Schema operations
 * 
 * Use cases:
 * - Custom CQL queries
 * - Schema management
 * - Complex queries not supported by higher-level APIs
 * - Performance-critical operations
 * - DDL operations (CREATE, ALTER, DROP)
 */
@SpringBootApplication
public class CQLTemplatePattern {

    public static void main(String[] args) {
        SpringApplication.run(CQLTemplatePattern.class, args);
    }
}

/**
 * Sensor entity for IoT data
 */
record Sensor(
    UUID id,
    String name,
    String location,
    String type,
    Double value,
    String unit,
    LocalDateTime timestamp
) {
    public Sensor {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

/**
 * RowMapper for Sensor entity
 */
class SensorRowMapper implements RowMapper<Sensor> {
    
    @Override
    public Sensor mapRow(Row row, int rowNum) {
        return new Sensor(
            row.getUuid("id"),
            row.getString("name"),
            row.getString("location"),
            row.getString("type"),
            row.getDouble("value"),
            row.getString("unit"),
            row.get("timestamp", LocalDateTime.class)
        );
    }
}

/**
 * Service demonstrating CqlTemplate operations
 */
@Service
class SensorService {
    
    private final CqlTemplate cqlTemplate;
    private final SensorRowMapper sensorRowMapper;
    
    public SensorService(CqlTemplate cqlTemplate) {
        this.cqlTemplate = cqlTemplate;
        this.sensorRowMapper = new SensorRowMapper();
    }
    
    /**
     * Create sensors table
     */
    public void createTable() {
        String cql = """
            CREATE TABLE IF NOT EXISTS sensors (
                id uuid PRIMARY KEY,
                name text,
                location text,
                type text,
                value double,
                unit text,
                timestamp timestamp
            )
            """;
        cqlTemplate.execute(cql);
    }
    
    /**
     * Create index on location
     */
    public void createLocationIndex() {
        String cql = "CREATE INDEX IF NOT EXISTS sensors_location_idx ON sensors (location)";
        cqlTemplate.execute(cql);
    }
    
    /**
     * Create index on type
     */
    public void createTypeIndex() {
        String cql = "CREATE INDEX IF NOT EXISTS sensors_type_idx ON sensors (type)";
        cqlTemplate.execute(cql);
    }
    
    /**
     * Insert sensor using SimpleStatement
     */
    public void insertSensor(Sensor sensor) {
        String cql = """
            INSERT INTO sensors (id, name, location, type, value, unit, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        cqlTemplate.execute(cql, 
            sensor.id(), 
            sensor.name(), 
            sensor.location(), 
            sensor.type(), 
            sensor.value(), 
            sensor.unit(), 
            sensor.timestamp()
        );
    }
    
    /**
     * Insert multiple sensors in batch
     */
    public void insertSensorsBatch(List<Sensor> sensors) {
        String cql = """
            BEGIN BATCH
            INSERT INTO sensors (id, name, location, type, value, unit, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            APPLY BATCH
            """;
        
        for (Sensor sensor : sensors) {
            cqlTemplate.execute(cql, 
                sensor.id(), 
                sensor.name(), 
                sensor.location(), 
                sensor.type(), 
                sensor.value(), 
                sensor.unit(), 
                sensor.timestamp()
            );
        }
    }
    
    /**
     * Update sensor value
     */
    public void updateSensorValue(UUID id, Double value) {
        String cql = "UPDATE sensors SET value = ?, timestamp = ? WHERE id = ?";
        cqlTemplate.execute(cql, value, LocalDateTime.now(), id);
    }
    
    /**
     * Find sensor by ID
     */
    public Sensor findById(UUID id) {
        String cql = "SELECT * FROM sensors WHERE id = ?";
        return cqlTemplate.queryForObject(cql, sensorRowMapper, id);
    }
    
    /**
     * Find all sensors
     */
    public List<Sensor> findAll() {
        String cql = "SELECT * FROM sensors";
        return cqlTemplate.query(cql, sensorRowMapper);
    }
    
    /**
     * Find sensors by location
     */
    public List<Sensor> findByLocation(String location) {
        String cql = "SELECT * FROM sensors WHERE location = ? ALLOW FILTERING";
        return cqlTemplate.query(cql, sensorRowMapper, location);
    }
    
    /**
     * Find sensors by type
     */
    public List<Sensor> findByType(String type) {
        String cql = "SELECT * FROM sensors WHERE type = ? ALLOW FILTERING";
        return cqlTemplate.query(cql, sensorRowMapper, type);
    }
    
    /**
     * Find sensors with value greater than threshold
     */
    public List<Sensor> findByValueGreaterThan(Double threshold) {
        String cql = "SELECT * FROM sensors WHERE value > ? ALLOW FILTERING";
        return cqlTemplate.query(cql, sensorRowMapper, threshold);
    }
    
    /**
     * Find sensors with limit
     */
    public List<Sensor> findWithLimit(int limit) {
        String cql = "SELECT * FROM sensors LIMIT ?";
        return cqlTemplate.query(cql, sensorRowMapper, limit);
    }
    
    /**
     * Count all sensors
     */
    public Long countAll() {
        String cql = "SELECT COUNT(*) FROM sensors";
        return cqlTemplate.queryForObject(cql, Long.class);
    }
    
    /**
     * Count sensors by location
     */
    public Long countByLocation(String location) {
        String cql = "SELECT COUNT(*) FROM sensors WHERE location = ? ALLOW FILTERING";
        return cqlTemplate.queryForObject(cql, Long.class, location);
    }
    
    /**
     * Get min value
     */
    public Double getMinValue() {
        String cql = "SELECT MIN(value) FROM sensors";
        return cqlTemplate.queryForObject(cql, Double.class);
    }
    
    /**
     * Get max value
     */
    public Double getMaxValue() {
        String cql = "SELECT MAX(value) FROM sensors";
        return cqlTemplate.queryForObject(cql, Double.class);
    }
    
    /**
     * Get average value
     */
    public Double getAverageValue() {
        String cql = "SELECT AVG(value) FROM sensors";
        return cqlTemplate.queryForObject(cql, Double.class);
    }
    
    /**
     * Delete sensor by ID
     */
    public void deleteById(UUID id) {
        String cql = "DELETE FROM sensors WHERE id = ?";
        cqlTemplate.execute(cql, id);
    }
    
    /**
     * Delete sensors by location
     */
    public void deleteByLocation(String location) {
        // First find all sensors in location, then delete
        List<Sensor> sensors = findByLocation(location);
        for (Sensor sensor : sensors) {
            deleteById(sensor.id());
        }
    }
    
    /**
     * Truncate table
     */
    public void truncate() {
        String cql = "TRUNCATE sensors";
        cqlTemplate.execute(cql);
    }
    
    /**
     * Drop table
     */
    public void dropTable() {
        String cql = "DROP TABLE IF EXISTS sensors";
        cqlTemplate.execute(cql);
    }
    
    /**
     * Execute custom CQL
     */
    public List<Sensor> executeCustomCql(String cql) {
        return cqlTemplate.query(cql, sensorRowMapper);
    }
    
    /**
     * Execute custom CQL with parameters
     */
    public List<Sensor> executeCustomCqlWithParams(String cql, Object... params) {
        return cqlTemplate.query(cql, sensorRowMapper, params);
    }
}

/**
 * REST controller for sensor operations
 */
@RestController
@RequestMapping("/api/sensors")
class SensorController {
    
    private final SensorService sensorService;
    
    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }
    
    @PostMapping("/schema/create")
    public ResponseEntity<String> createSchema() {
        sensorService.createTable();
        sensorService.createLocationIndex();
        sensorService.createTypeIndex();
        return ResponseEntity.ok("Schema created successfully");
    }
    
    @PostMapping
    public ResponseEntity<String> createSensor(@RequestBody Sensor sensor) {
        sensorService.insertSensor(sensor);
        return ResponseEntity.ok("Sensor created");
    }
    
    @PostMapping("/batch")
    public ResponseEntity<String> createSensorsBatch(@RequestBody List<Sensor> sensors) {
        sensorService.insertSensorsBatch(sensors);
        return ResponseEntity.ok("Sensors created in batch");
    }
    
    @PatchMapping("/{id}/value")
    public ResponseEntity<Void> updateValue(@PathVariable UUID id, @RequestParam Double value) {
        sensorService.updateSensorValue(id, value);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Sensor> getSensor(@PathVariable UUID id) {
        Sensor sensor = sensorService.findById(id);
        return sensor != null ? ResponseEntity.ok(sensor) : ResponseEntity.notFound().build();
    }
    
    @GetMapping
    public ResponseEntity<List<Sensor>> getAllSensors(@RequestParam(required = false) Integer limit) {
        List<Sensor> sensors = limit != null ? 
            sensorService.findWithLimit(limit) : 
            sensorService.findAll();
        return ResponseEntity.ok(sensors);
    }
    
    @GetMapping("/location/{location}")
    public ResponseEntity<List<Sensor>> getSensorsByLocation(@PathVariable String location) {
        return ResponseEntity.ok(sensorService.findByLocation(location));
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Sensor>> getSensorsByType(@PathVariable String type) {
        return ResponseEntity.ok(sensorService.findByType(type));
    }
    
    @GetMapping("/value/greater-than/{threshold}")
    public ResponseEntity<List<Sensor>> getSensorsByValueGreaterThan(@PathVariable Double threshold) {
        return ResponseEntity.ok(sensorService.findByValueGreaterThan(threshold));
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> countSensors() {
        return ResponseEntity.ok(sensorService.countAll());
    }
    
    @GetMapping("/count/location/{location}")
    public ResponseEntity<Long> countByLocation(@PathVariable String location) {
        return ResponseEntity.ok(sensorService.countByLocation(location));
    }
    
    @GetMapping("/statistics/min")
    public ResponseEntity<Double> getMinValue() {
        return ResponseEntity.ok(sensorService.getMinValue());
    }
    
    @GetMapping("/statistics/max")
    public ResponseEntity<Double> getMaxValue() {
        return ResponseEntity.ok(sensorService.getMaxValue());
    }
    
    @GetMapping("/statistics/average")
    public ResponseEntity<Double> getAverageValue() {
        return ResponseEntity.ok(sensorService.getAverageValue());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSensor(@PathVariable UUID id) {
        sensorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/location/{location}")
    public ResponseEntity<Void> deleteByLocation(@PathVariable String location) {
        sensorService.deleteByLocation(location);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/truncate")
    public ResponseEntity<Void> truncate() {
        sensorService.truncate();
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/schema/drop")
    public ResponseEntity<String> dropSchema() {
        sensorService.dropTable();
        return ResponseEntity.ok("Schema dropped");
    }
    
    @PostMapping("/cql")
    public ResponseEntity<List<Sensor>> executeCql(@RequestBody String cql) {
        return ResponseEntity.ok(sensorService.executeCustomCql(cql));
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("""
            CQL Template Pattern
            
            This pattern demonstrates the use of CqlTemplate for executing raw CQL statements
            with Apache Cassandra, providing low-level control over queries.
            
            Features:
            - Raw CQL execution
            - Schema operations (CREATE, DROP, INDEX)
            - SimpleStatement for ad-hoc queries
            - PreparedStatement for parameterized queries
            - RowMapper for result mapping
            - Batch operations
            - Aggregation functions (COUNT, MIN, MAX, AVG)
            - Custom CQL queries
            
            Endpoints:
            - POST /api/sensors/schema/create - Create schema
            - POST /api/sensors - Create sensor
            - POST /api/sensors/batch - Create sensors in batch
            - PATCH /api/sensors/{id}/value - Update value
            - GET /api/sensors/{id} - Get sensor
            - GET /api/sensors - Get all sensors (optional limit)
            - GET /api/sensors/location/{location} - Filter by location
            - GET /api/sensors/type/{type} - Filter by type
            - GET /api/sensors/value/greater-than/{threshold} - Filter by value
            - GET /api/sensors/count - Count all
            - GET /api/sensors/count/location/{location} - Count by location
            - GET /api/sensors/statistics/min - Get min value
            - GET /api/sensors/statistics/max - Get max value
            - GET /api/sensors/statistics/average - Get average value
            - DELETE /api/sensors/{id} - Delete sensor
            - DELETE /api/sensors/location/{location} - Delete by location
            - DELETE /api/sensors/truncate - Truncate table
            - DELETE /api/sensors/schema/drop - Drop schema
            - POST /api/sensors/cql - Execute custom CQL
            """);
    }
}
