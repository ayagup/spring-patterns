package com.example.graphql.schema;

import graphql.schema.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * GraphQL Schema Pattern
 * 
 * Demonstrates how to define and configure GraphQL schemas in Spring Boot.
 * This pattern shows schema definition using SDL (Schema Definition Language),
 * type definitions, field definitions, and schema registration.
 * 
 * Key Components:
 * - Schema Definition (SDL)
 * - Type Definitions
 * - Field Resolvers
 * - Custom Scalars
 * - Directives
 * 
 * Dependencies:
 * - spring-boot-starter-graphql
 * - graphql-java
 */
@SpringBootApplication
public class GraphQLSchemaPattern {

    public static void main(String[] args) {
        SpringApplication.run(GraphQLSchemaPattern.class, args);
    }

    /**
     * GraphQL Schema Configuration
     * Configures runtime wiring for custom scalars, directives, and type resolvers
     */
    @Component
    public static class GraphQLSchemaConfig implements RuntimeWiringConfigurer {
        
        @Override
        public void configure(RuntimeWiring.Builder builder) {
            builder
                .scalar(CustomScalars.dateTime())
                .directive("uppercase", new UppercaseDirective())
                .type("Query", typeWiring -> typeWiring
                    .dataFetcher("book", env -> {
                        String id = env.getArgument("id");
                        return bookService.findById(id);
                    })
                    .dataFetcher("books", env -> bookService.findAll())
                )
                .type("Mutation", typeWiring -> typeWiring
                    .dataFetcher("createBook", env -> {
                        Map<String, Object> input = env.getArgument("input");
                        return bookService.createBook(input);
                    })
                )
                .type("Book", typeWiring -> typeWiring
                    .dataFetcher("author", env -> {
                        Book book = env.getSource();
                        return authorService.findById(book.getAuthorId());
                    })
                );
        }

        private static BookService bookService = new BookService();
        private static AuthorService authorService = new AuthorService();
    }

    /**
     * Custom Scalars Definition
     * Defines custom GraphQL scalar types
     */
    public static class CustomScalars {
        
        public static GraphQLScalarType dateTime() {
            return GraphQLScalarType.newScalar()
                .name("DateTime")
                .description("Custom DateTime scalar")
                .coercing(new Coercing<Date, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) {
                        if (dataFetcherResult instanceof Date) {
                            return ((Date) dataFetcherResult).toString();
                        }
                        return null;
                    }

                    @Override
                    public Date parseValue(Object input) {
                        if (input instanceof String) {
                            return new Date(Long.parseLong((String) input));
                        }
                        return null;
                    }

                    @Override
                    public Date parseLiteral(Object input) {
                        return parseValue(input);
                    }
                })
                .build();
        }
    }

    /**
     * Custom Directive Implementation
     * Demonstrates custom directive for field transformation
     */
    public static class UppercaseDirective implements SchemaDirectiveWiring {
        
        @Override
        public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
            GraphQLFieldDefinition field = environment.getElement();
            DataFetcher<?> originalFetcher = environment.getCodeRegistry().getDataFetcher(
                environment.getFieldsContainer(), field
            );
            
            DataFetcher<?> uppercaseFetcher = dataFetchingEnvironment -> {
                Object result = originalFetcher.get(dataFetchingEnvironment);
                if (result instanceof String) {
                    return ((String) result).toUpperCase();
                }
                return result;
            };
            
            environment.getCodeRegistry().dataFetcher(
                environment.getFieldsContainer(), field, uppercaseFetcher
            );
            
            return field;
        }
    }

    /**
     * Domain Models
     */
    public static class Book {
        private String id;
        private String title;
        private String isbn;
        private String authorId;
        private Date publishedDate;
        private List<String> tags;

        public Book(String id, String title, String isbn, String authorId) {
            this.id = id;
            this.title = title;
            this.isbn = isbn;
            this.authorId = authorId;
            this.publishedDate = new Date();
            this.tags = new ArrayList<>();
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }
        public String getAuthorId() { return authorId; }
        public void setAuthorId(String authorId) { this.authorId = authorId; }
        public Date getPublishedDate() { return publishedDate; }
        public void setPublishedDate(Date publishedDate) { this.publishedDate = publishedDate; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
    }

    public static class Author {
        private String id;
        private String name;
        private String email;

        public Author(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    /**
     * Book Service
     * Handles book-related operations
     */
    @Service
    public static class BookService {
        private final Map<String, Book> books = new HashMap<>();

        public BookService() {
            books.put("1", new Book("1", "GraphQL in Action", "978-1617295683", "1"));
            books.put("2", new Book("2", "Spring Boot in Action", "978-1617292545", "2"));
        }

        public Book findById(String id) {
            return books.get(id);
        }

        public List<Book> findAll() {
            return new ArrayList<>(books.values());
        }

        public Book createBook(Map<String, Object> input) {
            String id = UUID.randomUUID().toString();
            String title = (String) input.get("title");
            String isbn = (String) input.get("isbn");
            String authorId = (String) input.get("authorId");
            
            Book book = new Book(id, title, isbn, authorId);
            books.put(id, book);
            return book;
        }

        public CompletableFuture<Book> findByIdAsync(String id) {
            return CompletableFuture.supplyAsync(() -> findById(id));
        }
    }

    /**
     * Author Service
     * Handles author-related operations
     */
    @Service
    public static class AuthorService {
        private final Map<String, Author> authors = new HashMap<>();

        public AuthorService() {
            authors.put("1", new Author("1", "Samer Buna", "samer@example.com"));
            authors.put("2", new Author("2", "Craig Walls", "craig@example.com"));
        }

        public Author findById(String id) {
            return authors.get(id);
        }

        public List<Author> findAll() {
            return new ArrayList<>(authors.values());
        }

        public CompletableFuture<Author> findByIdAsync(String id) {
            return CompletableFuture.supplyAsync(() -> findById(id));
        }
    }
}
