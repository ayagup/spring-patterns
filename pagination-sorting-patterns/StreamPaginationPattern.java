package com.example.pagination.streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.persistence.*;
import java.io.IOException;
import java.util.stream.Stream;

/**
 * Stream-based Pagination Pattern
 * 
 * Uses Java Streams for memory-efficient pagination.
 * Ideal for large datasets and exports.
 * 
 * @Transactional needed to keep stream open.
 */
@SpringBootApplication
public class StreamPaginationPattern {

    public static void main(String[] args) {
        SpringApplication.run(StreamPaginationPattern.class, args);
    }

    @Entity
    @Table(name = "records")
    public static class Record {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String data;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    public interface RecordRepository extends JpaRepository<Record, Long> {
        
        @Query("SELECT r FROM Record r")
        Stream<Record> streamAll();
    }

    @RestController
    @RequestMapping("/api/records")
    public static class RecordController {

        private final RecordRepository repository;

        public RecordController(RecordRepository repository) {
            this.repository = repository;
        }

        /**
         * Stream records as CSV
         */
        @GetMapping(value = "/stream", produces = "text/csv")
        @Transactional(readOnly = true)
        public StreamingResponseBody streamRecords() {
            return outputStream -> {
                try (Stream<Record> recordStream = repository.streamAll()) {
                    outputStream.write("id,data\n".getBytes());
                    
                    recordStream.forEach(record -> {
                        try {
                            String line = record.getId() + "," + record.getData() + "\n";
                            outputStream.write(line.getBytes());
                            outputStream.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            };
        }

        /**
         * Stream with filtering
         */
        @GetMapping(value = "/stream-filtered", produces = "application/json")
        @Transactional(readOnly = true)
        public StreamingResponseBody streamFiltered(
            @RequestParam String filter
        ) {
            return outputStream -> {
                try (Stream<Record> recordStream = repository.streamAll()) {
                    outputStream.write("[".getBytes());
                    
                    final boolean[] first = {true};
                    recordStream
                        .filter(r -> r.getData().contains(filter))
                        .forEach(record -> {
                            try {
                                if (!first[0]) {
                                    outputStream.write(",".getBytes());
                                }
                                String json = String.format(
                                    "{\"id\":%d,\"data\":\"%s\"}",
                                    record.getId(),
                                    record.getData()
                                );
                                outputStream.write(json.getBytes());
                                outputStream.flush();
                                first[0] = false;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    
                    outputStream.write("]".getBytes());
                }
            };
        }
    }
}
