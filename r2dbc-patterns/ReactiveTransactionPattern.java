package com.example.r2dbc.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

/**
 * Reactive Transaction Pattern
 * 
 * Demonstrates reactive transaction management with R2DBC.
 * Provides declarative and programmatic transaction support.
 * 
 * Key Features:
 * - @Transactional for declarative transactions
 * - TransactionalOperator for programmatic control
 * - Reactive rollback handling
 * - Isolation levels
 * - Transaction propagation
 */
@SpringBootApplication
public class ReactiveTransactionPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveTransactionPattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Reactive Transaction Pattern ===\n");
        demonstrateTransactions();
    }

    private void demonstrateTransactions() {
        System.out.println("Reactive Transaction Management:");

        System.out.println("\n1. Declarative Transactions:");
        System.out.println("   @Transactional");
        System.out.println("   public Mono<User> createUser(User user) {");
        System.out.println("       return userRepository.save(user);");
        System.out.println("   }");

        System.out.println("\n2. Programmatic Transactions:");
        System.out.println("   TransactionalOperator operator = TransactionalOperator.create(txManager);");
        System.out.println("   userRepository.save(user)");
        System.out.println("       .as(operator::transactional)");
        System.out.println("       .subscribe();");

        System.out.println("\n3. Rollback Handling:");
        System.out.println("   @Transactional");
        System.out.println("   public Mono<Void> processWithRollback() {");
        System.out.println("       return operation1()");
        System.out.println("           .then(operation2())");
        System.out.println("           .onErrorResume(e -> Mono.error(e)); // Triggers rollback");
        System.out.println("   }");

        System.out.println("\n4. Isolation Levels:");
        System.out.println("   @Transactional(isolation = Isolation.READ_COMMITTED)");

        System.out.println("\n5. Propagation:");
        System.out.println("   @Transactional(propagation = Propagation.REQUIRES_NEW)");

        System.out.println("\n6. Benefits:");
        System.out.println("   - ACID properties");
        System.out.println("   - Automatic rollback on errors");
        System.out.println("   - Non-blocking transaction management");
        System.out.println("   - Integration with reactive streams");
    }

    // Example service with transactions
    static class UserService {
        @Transactional
        public Mono<User> createUserWithTransaction(User user) {
            // All operations in this method are transactional
            return Mono.just(user);
        }
    }

    static class User {
        private Long id;
        private String name;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
