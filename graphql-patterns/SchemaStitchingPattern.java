package com.example.graphql.stitching;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.stereotype.Component;

/**
 * Schema Stitching Pattern
 * 
 * Demonstrates schema stitching to combine multiple GraphQL schemas
 * into a single unified schema.
 * 
 * Key Concepts:
 * - Schema Merging
 * - Type Extensions
 * - Remote Schema Integration
 * - Schema Delegation
 * 
 * Dependencies:
 * - spring-boot-starter-graphql
 * - graphql-java
 */
@SpringBootApplication
public class SchemaStitchingPattern {

    public static void main(String[] args) {
        SpringApplication.run(SchemaStitchingPattern.class, args);
    }

    @Component
    public static class SchemaStitcher {
        
        @Bean
        public GraphQLSchema stitchedSchema() {
            // Book Service Schema
            String bookSchema = """
                type Book {
                    id: ID!
                    title: String!
                    authorId: ID!
                }
                
                type Query {
                    book(id: ID!): Book
                    books: [Book]
                }
                """;
            
            // Author Service Schema
            String authorSchema = """
                type Author {
                    id: ID!
                    name: String!
                    email: String!
                }
                
                type Query {
                    author(id: ID!): Author
                    authors: [Author]
                }
                """;
            
            // Stitch schemas together with type extensions
            String stitchedSchemaDefinition = """
                type Book {
                    id: ID!
                    title: String!
                    authorId: ID!
                    author: Author
                }
                
                type Author {
                    id: ID!
                    name: String!
                    email: String!
                    books: [Book]
                }
                
                type Query {
                    book(id: ID!): Book
                    books: [Book]
                    author(id: ID!): Author
                    authors: [Author]
                }
                """;
            
            SchemaParser schemaParser = new SchemaParser();
            TypeDefinitionRegistry typeRegistry = schemaParser.parse(stitchedSchemaDefinition);
            
            RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", builder -> builder
                    .dataFetcher("book", env -> null)
                    .dataFetcher("author", env -> null))
                .type("Book", builder -> builder
                    .dataFetcher("author", env -> null))
                .type("Author", builder -> builder
                    .dataFetcher("books", env -> null))
                .build();
            
            SchemaGenerator schemaGenerator = new SchemaGenerator();
            return schemaGenerator.makeExecutableSchema(typeRegistry, wiring);
        }
    }
}
