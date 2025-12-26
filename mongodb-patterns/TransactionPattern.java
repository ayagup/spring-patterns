package com.example.mongodb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * Transaction Pattern
 * 
 * Demonstrates MongoDB multi-document transactions.
 * 
 * Transaction Features:
 * - ACID properties
 * - Multi-document operations
 * - Rollback on error
 * - Isolation levels
 * - Declarative and programmatic transactions
 * 
 * Requirements:
 * - MongoDB 4.0+ (replica set)
 * - MongoDB 4.2+ (sharded cluster)
 * 
 * Use Cases:
 * - Financial transactions
 * - Multi-step operations
 * - Data consistency
 * - Complex business logic
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class TransactionPattern {

    @Bean
    public MongoTransactionManager transactionManager(MongoTemplate mongoTemplate) {
        return new MongoTransactionManager(mongoTemplate.getMongoDatabaseFactory());
    }

    @Bean
    public AccountService accountService(MongoTemplate mongoTemplate) {
        return new AccountService(mongoTemplate);
    }
}

record Account(String id, String accountNumber, double balance) {}

@RestController
@RequestMapping("/api/mongo/accounts")
class AccountService {

    private final MongoTemplate mongoTemplate;

    public AccountService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional
    public void transferMoney(String fromAccount, String toAccount, double amount) {
        // Debit from source
        org.springframework.data.mongodb.core.query.Query fromQuery = 
            org.springframework.data.mongodb.core.query.Query.query(
                org.springframework.data.mongodb.core.query.Criteria.where("accountNumber").is(fromAccount)
            );
        org.springframework.data.mongodb.core.query.Update fromUpdate = 
            new org.springframework.data.mongodb.core.query.Update().inc("balance", -amount);
        mongoTemplate.updateFirst(fromQuery, fromUpdate, Account.class);
        
        // Credit to destination
        org.springframework.data.mongodb.core.query.Query toQuery = 
            org.springframework.data.mongodb.core.query.Query.query(
                org.springframework.data.mongodb.core.query.Criteria.where("accountNumber").is(toAccount)
            );
        org.springframework.data.mongodb.core.query.Update toUpdate = 
            new org.springframework.data.mongodb.core.query.Update().inc("balance", amount);
        mongoTemplate.updateFirst(toQuery, toUpdate, Account.class);
    }

    @Transactional
    public Account createAccount(Account account) {
        return mongoTemplate.save(account);
    }

    public Account getAccount(String accountNumber) {
        org.springframework.data.mongodb.core.query.Query query = 
            org.springframework.data.mongodb.core.query.Query.query(
                org.springframework.data.mongodb.core.query.Criteria.where("accountNumber").is(accountNumber)
            );
        return mongoTemplate.findOne(query, Account.class);
    }
}

@RestController
@RequestMapping("/api/mongo/accounts")
class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        Account created = accountService.createAccount(account);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        Account account = accountService.getAccount(accountNumber);
        return account != null ? ResponseEntity.ok(account) : ResponseEntity.notFound().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam double amount) {
        try {
            accountService.transferMoney(from, to, amount);
            return ResponseEntity.ok("Transfer successful");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Transfer failed: " + e.getMessage());
        }
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getInfo() {
        return ResponseEntity.ok(new PatternInfo(
            "Transaction Pattern",
            "MongoDB multi-document transactions",
            "1.0",
            List.of("ACID properties", "Multi-document", "Rollback", "Declarative/Programmatic"),
            List.of("Financial transactions", "Multi-step operations", "Data consistency")
        ));
    }

    record PatternInfo(String name, String description, String version,
                      List<String> features, List<String> useCases) {}
}
