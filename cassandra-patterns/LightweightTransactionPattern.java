package com.example.cassandra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Lightweight Transaction Pattern
 * 
 * Demonstrates the use of Lightweight Transactions (LWT) in Apache Cassandra
 * for linearizable consistency using Paxos consensus algorithm.
 * 
 * Key concepts:
 * - IF NOT EXISTS for inserts
 * - IF EXISTS for updates/deletes
 * - IF condition for conditional operations
 * - Compare-and-set operations
 * - Linearizable consistency
 * - Performance implications
 * 
 * Use cases:
 * - Preventing duplicate inserts
 * - Optimistic locking
 * - Compare-and-set operations
 * - Account balance updates
 * - Inventory management
 * - Unique constraints enforcement
 * 
 * Warning: LWTs are expensive (4x latency) - use sparingly
 */
@SpringBootApplication
public class LightweightTransactionPattern {

    public static void main(String[] args) {
        SpringApplication.run(LightweightTransactionPattern.class, args);
    }
}

/**
 * Account entity for banking operations
 */
record Account(
    UUID id,
    String accountNumber,
    String owner,
    Double balance,
    String currency,
    Integer version,
    LocalDateTime lastModified
) {
    public Account {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (version == null) {
            version = 0;
        }
        if (lastModified == null) {
            lastModified = LocalDateTime.now();
        }
    }
}

/**
 * Inventory item for stock management
 */
record InventoryItem(
    UUID id,
    String sku,
    String productName,
    Integer quantity,
    Integer reservedQuantity,
    Integer version,
    LocalDateTime lastUpdated
) {
    public InventoryItem {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (reservedQuantity == null) {
            reservedQuantity = 0;
        }
        if (version == null) {
            version = 0;
        }
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
    }
}

/**
 * Service demonstrating Lightweight Transactions
 */
@Service
class LWTService {
    
    private final CassandraTemplate cassandraTemplate;
    private final CqlTemplate cqlTemplate;
    
    public LWTService(CassandraTemplate cassandraTemplate, CqlTemplate cqlTemplate) {
        this.cassandraTemplate = cassandraTemplate;
        this.cqlTemplate = cqlTemplate;
    }
    
    /**
     * Insert account only if it doesn't exist (prevent duplicates)
     */
    public boolean createAccountIfNotExists(Account account) {
        String cql = """
            INSERT INTO accounts (id, account_number, owner, balance, currency, version, last_modified)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            IF NOT EXISTS
            """;
        
        Boolean applied = cqlTemplate.queryForObject(cql, Boolean.class,
            account.id(), account.accountNumber(), account.owner(),
            account.balance(), account.currency(), account.version(),
            account.lastModified()
        );
        
        return applied != null && applied;
    }
    
    /**
     * Update balance with optimistic locking (compare-and-set)
     */
    public boolean updateBalanceWithVersion(UUID id, Double newBalance, Integer expectedVersion) {
        String cql = """
            UPDATE accounts
            SET balance = ?, version = ?, last_modified = ?
            WHERE id = ?
            IF version = ?
            """;
        
        Integer newVersion = expectedVersion + 1;
        Boolean applied = cqlTemplate.queryForObject(cql, Boolean.class,
            newBalance, newVersion, LocalDateTime.now(), id, expectedVersion
        );
        
        return applied != null && applied;
    }
    
    /**
     * Transfer between accounts with LWT (prevents double spending)
     */
    public boolean transfer(UUID fromAccountId, UUID toAccountId, Double amount) {
        // Get current accounts
        Account fromAccount = cassandraTemplate.selectOneById(fromAccountId, Account.class);
        Account toAccount = cassandraTemplate.selectOneById(toAccountId, Account.class);
        
        if (fromAccount == null || toAccount == null || fromAccount.balance() < amount) {
            return false;
        }
        
        // Debit from account (with LWT)
        boolean debitSuccess = updateBalanceWithVersion(
            fromAccountId,
            fromAccount.balance() - amount,
            fromAccount.version()
        );
        
        if (!debitSuccess) {
            return false; // Concurrent modification detected
        }
        
        // Credit to account (with LWT)
        boolean creditSuccess = updateBalanceWithVersion(
            toAccountId,
            toAccount.balance() + amount,
            toAccount.version()
        );
        
        if (!creditSuccess) {
            // Rollback debit (compensating transaction)
            updateBalanceWithVersion(
                fromAccountId,
                fromAccount.balance(),
                fromAccount.version() + 1
            );
            return false;
        }
        
        return true;
    }
    
    /**
     * Delete account only if it exists
     */
    public boolean deleteAccountIfExists(UUID id) {
        String cql = "DELETE FROM accounts WHERE id = ? IF EXISTS";
        Boolean applied = cqlTemplate.queryForObject(cql, Boolean.class, id);
        return applied != null && applied;
    }
    
    /**
     * Delete account only if balance is zero
     */
    public boolean deleteAccountIfBalanceZero(UUID id) {
        String cql = "DELETE FROM accounts WHERE id = ? IF balance = 0.0";
        Boolean applied = cqlTemplate.queryForObject(cql, Boolean.class, id);
        return applied != null && applied;
    }
    
    /**
     * Reserve inventory with LWT (prevent overselling)
     */
    public boolean reserveInventory(UUID itemId, Integer quantity) {
        InventoryItem item = cassandraTemplate.selectOneById(itemId, InventoryItem.class);
        
        if (item == null || item.quantity() < quantity) {
            return false;
        }
        
        String cql = """
            UPDATE inventory_items
            SET quantity = ?, reserved_quantity = ?, version = ?, last_updated = ?
            WHERE id = ?
            IF version = ?
            """;
        
        Integer newQuantity = item.quantity() - quantity;
        Integer newReserved = item.reservedQuantity() + quantity;
        Integer newVersion = item.version() + 1;
        
        Boolean applied = cqlTemplate.queryForObject(cql, Boolean.class,
            newQuantity, newReserved, newVersion, LocalDateTime.now(),
            itemId, item.version()
        );
        
        return applied != null && applied;
    }
    
    /**
     * Release reserved inventory
     */
    public boolean releaseInventory(UUID itemId, Integer quantity) {
        InventoryItem item = cassandraTemplate.selectOneById(itemId, InventoryItem.class);
        
        if (item == null || item.reservedQuantity() < quantity) {
            return false;
        }
        
        String cql = """
            UPDATE inventory_items
            SET quantity = ?, reserved_quantity = ?, version = ?, last_updated = ?
            WHERE id = ?
            IF version = ?
            """;
        
        Integer newQuantity = item.quantity() + quantity;
        Integer newReserved = item.reservedQuantity() - quantity;
        Integer newVersion = item.version() + 1;
        
        Boolean applied = cqlTemplate.queryForObject(cql, Boolean.class,
            newQuantity, newReserved, newVersion, LocalDateTime.now(),
            itemId, item.version()
        );
        
        return applied != null && applied;
    }
    
    /**
     * Confirm inventory purchase (move from reserved to sold)
     */
    public boolean confirmPurchase(UUID itemId, Integer quantity) {
        InventoryItem item = cassandraTemplate.selectOneById(itemId, InventoryItem.class);
        
        if (item == null || item.reservedQuantity() < quantity) {
            return false;
        }
        
        String cql = """
            UPDATE inventory_items
            SET reserved_quantity = ?, version = ?, last_updated = ?
            WHERE id = ?
            IF version = ?
            """;
        
        Integer newReserved = item.reservedQuantity() - quantity;
        Integer newVersion = item.version() + 1;
        
        Boolean applied = cqlTemplate.queryForObject(cql, Boolean.class,
            newReserved, newVersion, LocalDateTime.now(),
            itemId, item.version()
        );
        
        return applied != null && applied;
    }
    
    /**
     * Find account by ID
     */
    public Account findAccountById(UUID id) {
        return cassandraTemplate.selectOneById(id, Account.class);
    }
    
    /**
     * Find inventory item by ID
     */
    public InventoryItem findInventoryById(UUID id) {
        return cassandraTemplate.selectOneById(id, InventoryItem.class);
    }
    
    /**
     * Find all accounts
     */
    public List<Account> findAllAccounts() {
        return cassandraTemplate.selectAll(Account.class);
    }
    
    /**
     * Find all inventory items
     */
    public List<InventoryItem> findAllInventory() {
        return cassandraTemplate.selectAll(InventoryItem.class);
    }
}

/**
 * REST controller for LWT operations
 */
@RestController
@RequestMapping("/api/lwt")
class LWTController {
    
    private final LWTService lwtService;
    
    public LWTController(LWTService lwtService) {
        this.lwtService = lwtService;
    }
    
    @PostMapping("/accounts")
    public ResponseEntity<Boolean> createAccount(@RequestBody Account account) {
        boolean created = lwtService.createAccountIfNotExists(account);
        return ResponseEntity.ok(created);
    }
    
    @PatchMapping("/accounts/{id}/balance")
    public ResponseEntity<Boolean> updateBalance(
            @PathVariable UUID id,
            @RequestParam Double newBalance,
            @RequestParam Integer expectedVersion) {
        boolean updated = lwtService.updateBalanceWithVersion(id, newBalance, expectedVersion);
        return ResponseEntity.ok(updated);
    }
    
    @PostMapping("/accounts/transfer")
    public ResponseEntity<Boolean> transfer(
            @RequestParam UUID fromAccountId,
            @RequestParam UUID toAccountId,
            @RequestParam Double amount) {
        boolean success = lwtService.transfer(fromAccountId, toAccountId, amount);
        return ResponseEntity.ok(success);
    }
    
    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Boolean> deleteAccount(@PathVariable UUID id) {
        boolean deleted = lwtService.deleteAccountIfExists(id);
        return ResponseEntity.ok(deleted);
    }
    
    @DeleteMapping("/accounts/{id}/if-zero")
    public ResponseEntity<Boolean> deleteIfBalanceZero(@PathVariable UUID id) {
        boolean deleted = lwtService.deleteAccountIfBalanceZero(id);
        return ResponseEntity.ok(deleted);
    }
    
    @GetMapping("/accounts/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable UUID id) {
        Account account = lwtService.findAccountById(id);
        return account != null ? ResponseEntity.ok(account) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(lwtService.findAllAccounts());
    }
    
    @PostMapping("/inventory/{id}/reserve")
    public ResponseEntity<Boolean> reserveInventory(
            @PathVariable UUID id,
            @RequestParam Integer quantity) {
        boolean reserved = lwtService.reserveInventory(id, quantity);
        return ResponseEntity.ok(reserved);
    }
    
    @PostMapping("/inventory/{id}/release")
    public ResponseEntity<Boolean> releaseInventory(
            @PathVariable UUID id,
            @RequestParam Integer quantity) {
        boolean released = lwtService.releaseInventory(id, quantity);
        return ResponseEntity.ok(released);
    }
    
    @PostMapping("/inventory/{id}/confirm")
    public ResponseEntity<Boolean> confirmPurchase(
            @PathVariable UUID id,
            @RequestParam Integer quantity) {
        boolean confirmed = lwtService.confirmPurchase(id, quantity);
        return ResponseEntity.ok(confirmed);
    }
    
    @GetMapping("/inventory/{id}")
    public ResponseEntity<InventoryItem> getInventoryItem(@PathVariable UUID id) {
        InventoryItem item = lwtService.findInventoryById(id);
        return item != null ? ResponseEntity.ok(item) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryItem>> getAllInventory() {
        return ResponseEntity.ok(lwtService.findAllInventory());
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("""
            Lightweight Transaction Pattern
            
            This pattern demonstrates the use of Lightweight Transactions (LWT) in Apache Cassandra
            for linearizable consistency using Paxos consensus algorithm.
            
            Features:
            - IF NOT EXISTS for preventing duplicates
            - IF EXISTS for conditional deletes
            - IF condition for compare-and-set operations
            - Optimistic locking with version numbers
            - Account balance transfers with consistency
            - Inventory reservation system
            
            Use Cases:
            - Preventing duplicate account creation
            - Transfer money between accounts
            - Optimistic locking for concurrent updates
            - Inventory reservation (prevent overselling)
            - Unique constraint enforcement
            
            Performance Warning:
            - LWTs are ~4x slower than normal writes
            - Use sparingly and only when necessary
            - Not suitable for high-throughput scenarios
            
            Endpoints:
            - POST /api/lwt/accounts - Create account (IF NOT EXISTS)
            - PATCH /api/lwt/accounts/{id}/balance - Update balance with version
            - POST /api/lwt/accounts/transfer - Transfer between accounts
            - DELETE /api/lwt/accounts/{id} - Delete account (IF EXISTS)
            - DELETE /api/lwt/accounts/{id}/if-zero - Delete if balance is zero
            - GET /api/lwt/accounts/{id} - Get account
            - GET /api/lwt/accounts - Get all accounts
            - POST /api/lwt/inventory/{id}/reserve - Reserve inventory
            - POST /api/lwt/inventory/{id}/release - Release reservation
            - POST /api/lwt/inventory/{id}/confirm - Confirm purchase
            - GET /api/lwt/inventory/{id} - Get inventory item
            - GET /api/lwt/inventory - Get all inventory
            """);
    }
}
