package com.example.r2dbc.patterns;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Connection Factory Pattern
 * 
 * Demonstrates ConnectionFactory configuration for R2DBC.
 * ConnectionFactory creates reactive database connections.
 * 
 * Key Features:
 * - Database connection configuration
 * - Connection pooling
 * - URL-based configuration
 * - Programmatic configuration
 * - Multiple datasource support
 */
@SpringBootApplication
public class ConnectionFactoryPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ConnectionFactoryPattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Connection Factory Pattern ===\n");
        System.out.println("ConnectionFactory configuration:");
        System.out.println("- URL: r2dbc:postgresql://localhost:5432/mydb");
        System.out.println("- Driver: PostgreSQL R2DBC driver");
        System.out.println("- Connection pooling for performance");
        System.out.println("- Reactive, non-blocking connections");
    }

    @Configuration
    static class R2dbcConfig {
        @Bean
        public ConnectionFactory connectionFactory() {
            ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "postgresql")
                .option(ConnectionFactoryOptions.HOST, "localhost")
                .option(ConnectionFactoryOptions.PORT, 5432)
                .option(ConnectionFactoryOptions.USER, "user")
                .option(ConnectionFactoryOptions.PASSWORD, "password")
                .option(ConnectionFactoryOptions.DATABASE, "mydb")
                .build();
            
            System.out.println("ConnectionFactory configured with:");
            System.out.println("  Host: localhost:5432");
            System.out.println("  Database: mydb");
            
            return io.r2dbc.spi.ConnectionFactories.get(options);
        }
    }
}
