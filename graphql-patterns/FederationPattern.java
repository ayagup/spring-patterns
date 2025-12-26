package com.example.graphql.federation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.*;

/**
 * Federation Pattern
 * 
 * Demonstrates Apollo Federation for GraphQL microservices.
 * Allows building a distributed GraphQL architecture.
 * 
 * Key Concepts:
 * - @key directive for entity identification
 * - @extends for extending types from other services
 * - @external for fields owned by other services
 * - Entity resolution
 * 
 * Dependencies:
 * - spring-boot-starter-graphql
 * - graphql-java-federation
 */
@SpringBootApplication
public class FederationPattern {

    public static void main(String[] args) {
        SpringApplication.run(FederationPattern.class, args);
    }

    /**
     * Book Service (Federated Entity)
     * Schema with Federation directives:
     * 
     * type Book @key(fields: "id") {
     *   id: ID!
     *   title: String!
     *   authorId: ID!
     * }
     */
    @Controller
    public static class BookFederationController {
        
        private final Map<String, Book> books = new HashMap<>();
        
        public BookFederationController() {
            books.put("1", new Book("1", "Spring Boot Guide", "1"));
            books.put("2", new Book("2", "GraphQL in Action", "2"));
        }
        
        @QueryMapping
        public List<Book> books() {
            return new ArrayList<>(books.values());
        }
        
        @QueryMapping
        public Book book(@Argument String id) {
            return books.get(id);
        }
        
        /**
         * Entity resolver for Federation
         * Resolves Book entities by ID for other services
         */
        public Book _resolveReference(Map<String, Object> reference) {
            String id = (String) reference.get("id");
            return books.get(id);
        }
    }

    /**
     * Author Service (Extending Book type)
     * Schema:
     * 
     * extend type Book @key(fields: "id") {
     *   id: ID! @external
     *   author: Author
     * }
     */
    @Controller
    public static class AuthorFederationController {
        
        private final Map<String, Author> authors = new HashMap<>();
        
        public AuthorFederationController() {
            authors.put("1", new Author("1", "John Doe", "john@example.com"));
            authors.put("2", new Author("2", "Jane Smith", "jane@example.com"));
        }
        
        /**
         * Field resolver for federated Book.author field
         */
        @SchemaMapping(typeName = "Book", field = "author")
        public Author author(Book book) {
            return authors.get(book.getAuthorId());
        }
    }

    public record Book(String id, String title, String authorId) {}
    public record Author(String id, String name, String email) {}
}
