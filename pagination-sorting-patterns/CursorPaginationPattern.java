package com.example.pagination.cursor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Cursor-based Pagination Pattern
 * 
 * Uses cursor (typically ID or timestamp) instead of page numbers.
 * More stable for real-time data where items are added/removed frequently.
 * Ideal for feeds and timelines.
 */
@SpringBootApplication
public class CursorPaginationPattern {

    public static void main(String[] args) {
        SpringApplication.run(CursorPaginationPattern.class, args);
    }

    @Entity
    @Table(name = "messages")
    public static class Message {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String content;
        private LocalDateTime createdAt;
        private Long userId;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }

    public interface MessageRepository extends JpaRepository<Message, Long> {
        
        @Query("SELECT m FROM Message m WHERE m.id < :cursor ORDER BY m.id DESC")
        List<Message> findMessagesBefore(Long cursor, org.springframework.data.domain.Pageable pageable);
        
        @Query("SELECT m FROM Message m WHERE m.id > :cursor ORDER BY m.id ASC")
        List<Message> findMessagesAfter(Long cursor, org.springframework.data.domain.Pageable pageable);
        
        List<Message> findTop20ByOrderByIdDesc();
    }

    @RestController
    @RequestMapping("/api/messages")
    public static class MessageController {

        private final MessageRepository repository;

        public MessageController(MessageRepository repository) {
            this.repository = repository;
        }

        /**
         * Initial load - returns latest messages
         * GET /api/messages?limit=20
         */
        @GetMapping
        public CursorResponse<Message> getMessages(
            @RequestParam(defaultValue = "20") int limit
        ) {
            List<Message> messages = repository.findTop20ByOrderByIdDesc();
            
            String nextCursor = messages.isEmpty() ? null : 
                messages.get(messages.size() - 1).getId().toString();
            
            return new CursorResponse<>(messages, nextCursor, null);
        }

        /**
         * Load older messages using cursor
         * GET /api/messages?cursor=100&limit=20
         */
        @GetMapping("/before")
        public CursorResponse<Message> getMessagesBefore(
            @RequestParam Long cursor,
            @RequestParam(defaultValue = "20") int limit
        ) {
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(0, limit);
            
            List<Message> messages = repository.findMessagesBefore(cursor, pageable);
            
            String nextCursor = messages.isEmpty() ? null : 
                messages.get(messages.size() - 1).getId().toString();
            
            return new CursorResponse<>(messages, nextCursor, cursor.toString());
        }

        /**
         * Load newer messages (for refresh)
         */
        @GetMapping("/after")
        public CursorResponse<Message> getMessagesAfter(
            @RequestParam Long cursor,
            @RequestParam(defaultValue = "20") int limit
        ) {
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(0, limit);
            
            List<Message> messages = repository.findMessagesAfter(cursor, pageable);
            
            return new CursorResponse<>(messages, null, cursor.toString());
        }
    }

    /**
     * Cursor response wrapper
     */
    public record CursorResponse<T>(
        List<T> data,
        String nextCursor,
        String prevCursor
    ) {}
}
