package com.example.events.sourcing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event Sourcing Pattern - Demonstrates Event-Sourced Aggregate Implementation
 * 
 * This pattern shows how to:
 * 1. Store state as sequence of events
 * 2. Reconstruct aggregate from events
 * 3. Implement event store
 * 4. Create snapshots for performance
 * 5. Replay events to rebuild state
 * 6. Handle event versioning
 * 7. Implement command handling
 * 8. Apply events to aggregates
 * 9. Track aggregate versions
 * 10. Implement event-sourced repositories
 * 
 * Key Concepts:
 * - Event Sourcing: Store changes as events, not current state
 * - Aggregate: Domain object reconstructed from events
 * - Event Store: Append-only storage for events
 * - Snapshot: Point-in-time aggregate state for optimization
 * - Replay: Rebuilding state by applying all events
 * 
 * Event Sourcing Benefits:
 * 1. Complete Audit Trail - All changes recorded
 * 2. Temporal Queries - State at any point in time
 * 3. Event Replay - Rebuild state from events
 * 4. Debugging - Understand how state evolved
 * 5. Event-Driven Architecture - Natural fit
 * 
 * Dependencies:
 * - spring-boot-starter
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class EventSourcingPattern {

    public static void main(String[] args) {
        SpringApplication.run(EventSourcingPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("EVENT SOURCING PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateEventSourcing();
        demonstrateSnapshots();
        
        System.out.println("\nApplication running with Event Sourcing");
        System.out.println("Test endpoints:");
        System.out.println("POST /api/sourcing/accounts - Create account");
        System.out.println("POST /api/sourcing/accounts/{id}/deposit - Deposit money");
        System.out.println("GET /api/sourcing/accounts/{id} - Get account state");
        System.out.println("GET /api/sourcing/accounts/{id}/events - Get account events");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateEventSourcing() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EVENT SOURCING PRINCIPLES");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. Events as Source of Truth:");
        System.out.println("   State derived from events, not stored directly");
        
        System.out.println("\n2. Append-Only Storage:");
        System.out.println("   Events never deleted or modified");
        
        System.out.println("\n3. Event Replay:");
        System.out.println("   Rebuild state by replaying all events");
    }
    
    private static void demonstrateSnapshots() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SNAPSHOTS FOR PERFORMANCE");
        System.out.println("=".repeat(80));
        
        System.out.println("\n- Save aggregate state periodically");
        System.out.println("- Load snapshot + subsequent events");
        System.out.println("- Faster reconstruction for long histories");
    }
}

/**
 * Base Domain Event
 */
abstract class DomainEvent {
    private final String eventId;
    private final String aggregateId;
    private final long version;
    private final LocalDateTime timestamp;
    
    protected DomainEvent(String aggregateId, long version) {
        this.eventId = UUID.randomUUID().toString();
        this.aggregateId = aggregateId;
        this.version = version;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getEventId() { return eventId; }
    public String getAggregateId() { return aggregateId; }
    public long getVersion() { return version; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public abstract String getEventType();
}

/**
 * Account Domain Events
 */
class AccountCreatedEvent extends DomainEvent {
    private final String accountHolder;
    private final double initialBalance;
    
    public AccountCreatedEvent(String aggregateId, long version, String accountHolder, double initialBalance) {
        super(aggregateId, version);
        this.accountHolder = accountHolder;
        this.initialBalance = initialBalance;
    }
    
    public String getAccountHolder() { return accountHolder; }
    public double getInitialBalance() { return initialBalance; }
    
    @Override
    public String getEventType() { return "AccountCreated"; }
}

class MoneyDepositedEvent extends DomainEvent {
    private final double amount;
    private final String description;
    
    public MoneyDepositedEvent(String aggregateId, long version, double amount, String description) {
        super(aggregateId, version);
        this.amount = amount;
        this.description = description;
    }
    
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    
    @Override
    public String getEventType() { return "MoneyDeposited"; }
}

class MoneyWithdrawnEvent extends DomainEvent {
    private final double amount;
    private final String description;
    
    public MoneyWithdrawnEvent(String aggregateId, long version, double amount, String description) {
        super(aggregateId, version);
        this.amount = amount;
        this.description = description;
    }
    
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    
    @Override
    public String getEventType() { return "MoneyWithdrawn"; }
}

class AccountClosedEvent extends DomainEvent {
    private final String reason;
    
    public AccountClosedEvent(String aggregateId, long version, String reason) {
        super(aggregateId, version);
        this.reason = reason;
    }
    
    public String getReason() { return reason; }
    
    @Override
    public String getEventType() { return "AccountClosed"; }
}

/**
 * Event-Sourced Account Aggregate
 */
class Account {
    private String accountId;
    private String accountHolder;
    private double balance;
    private boolean closed;
    private long version;
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();
    
    // For reconstruction from events
    public Account() {
        this.version = 0;
    }
    
    // Create new account
    public static Account create(String accountId, String accountHolder, double initialBalance) {
        Account account = new Account();
        account.applyEvent(new AccountCreatedEvent(accountId, 1, accountHolder, initialBalance));
        return account;
    }
    
    // Commands
    public void deposit(double amount, String description) {
        if (closed) {
            throw new IllegalStateException("Account is closed");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        applyEvent(new MoneyDepositedEvent(accountId, version + 1, amount, description));
    }
    
    public void withdraw(double amount, String description) {
        if (closed) {
            throw new IllegalStateException("Account is closed");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > balance) {
            throw new IllegalStateException("Insufficient funds");
        }
        
        applyEvent(new MoneyWithdrawnEvent(accountId, version + 1, amount, description));
    }
    
    public void close(String reason) {
        if (closed) {
            throw new IllegalStateException("Account already closed");
        }
        
        applyEvent(new AccountClosedEvent(accountId, version + 1, reason));
    }
    
    // Apply event (for new events)
    private void applyEvent(DomainEvent event) {
        applyChange(event);
        uncommittedEvents.add(event);
    }
    
    // Apply change to aggregate state
    private void applyChange(DomainEvent event) {
        if (event instanceof AccountCreatedEvent) {
            AccountCreatedEvent e = (AccountCreatedEvent) event;
            this.accountId = e.getAggregateId();
            this.accountHolder = e.getAccountHolder();
            this.balance = e.getInitialBalance();
            this.closed = false;
        } else if (event instanceof MoneyDepositedEvent) {
            MoneyDepositedEvent e = (MoneyDepositedEvent) event;
            this.balance += e.getAmount();
        } else if (event instanceof MoneyWithdrawnEvent) {
            MoneyWithdrawnEvent e = (MoneyWithdrawnEvent) event;
            this.balance -= e.getAmount();
        } else if (event instanceof AccountClosedEvent) {
            this.closed = true;
        }
        
        this.version = event.getVersion();
    }
    
    // Load from history (for reconstruction)
    public void loadFromHistory(List<DomainEvent> history) {
        for (DomainEvent event : history) {
            applyChange(event);
        }
    }
    
    public List<DomainEvent> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }
    
    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }
    
    // Getters
    public String getAccountId() { return accountId; }
    public String getAccountHolder() { return accountHolder; }
    public double getBalance() { return balance; }
    public boolean isClosed() { return closed; }
    public long getVersion() { return version; }
}

/**
 * Aggregate Snapshot
 */
class AggregateSnapshot {
    private final String aggregateId;
    private final long version;
    private final Object state;
    private final LocalDateTime timestamp;
    
    public AggregateSnapshot(String aggregateId, long version, Object state) {
        this.aggregateId = aggregateId;
        this.version = version;
        this.state = state;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getAggregateId() { return aggregateId; }
    public long getVersion() { return version; }
    public Object getState() { return state; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

/**
 * In-Memory Event Store
 */
@Component
class InMemoryEventStore {
    
    private final Map<String, List<DomainEvent>> eventStreams = new ConcurrentHashMap<>();
    private final Map<String, AggregateSnapshot> snapshots = new ConcurrentHashMap<>();
    
    public void saveEvents(String aggregateId, List<DomainEvent> events, long expectedVersion) {
        List<DomainEvent> stream = eventStreams.computeIfAbsent(aggregateId, 
            k -> new CopyOnWriteArrayList<>());
        
        // Optimistic concurrency check
        long currentVersion = stream.isEmpty() ? 0 : stream.get(stream.size() - 1).getVersion();
        if (currentVersion != expectedVersion) {
            throw new ConcurrencyException("Aggregate has been modified");
        }
        
        stream.addAll(events);
        
        System.out.printf("[EventStore] Saved %d events for aggregate %s%n", 
            events.size(), aggregateId);
    }
    
    public List<DomainEvent> getEvents(String aggregateId) {
        return new ArrayList<>(eventStreams.getOrDefault(aggregateId, Collections.emptyList()));
    }
    
    public List<DomainEvent> getEventsAfterVersion(String aggregateId, long version) {
        return eventStreams.getOrDefault(aggregateId, Collections.emptyList())
            .stream()
            .filter(e -> e.getVersion() > version)
            .toList();
    }
    
    public void saveSnapshot(String aggregateId, long version, Object state) {
        snapshots.put(aggregateId, new AggregateSnapshot(aggregateId, version, state));
        System.out.printf("[EventStore] Saved snapshot for aggregate %s at version %d%n", 
            aggregateId, version);
    }
    
    public Optional<AggregateSnapshot> getSnapshot(String aggregateId) {
        return Optional.ofNullable(snapshots.get(aggregateId));
    }
}

/**
 * Event-Sourced Repository
 */
@Component
class AccountRepository {
    
    private final InMemoryEventStore eventStore;
    private static final int SNAPSHOT_THRESHOLD = 10;
    
    public AccountRepository(InMemoryEventStore eventStore) {
        this.eventStore = eventStore;
    }
    
    public void save(Account account) {
        List<DomainEvent> uncommittedEvents = account.getUncommittedEvents();
        
        if (!uncommittedEvents.isEmpty()) {
            long expectedVersion = account.getVersion() - uncommittedEvents.size();
            eventStore.saveEvents(account.getAccountId(), uncommittedEvents, expectedVersion);
            account.markEventsAsCommitted();
            
            // Create snapshot if threshold reached
            if (account.getVersion() % SNAPSHOT_THRESHOLD == 0) {
                createSnapshot(account);
            }
        }
    }
    
    public Account findById(String accountId) {
        Account account = new Account();
        
        // Try to load from snapshot first
        Optional<AggregateSnapshot> snapshot = eventStore.getSnapshot(accountId);
        
        List<DomainEvent> events;
        if (snapshot.isPresent()) {
            // Load from snapshot + subsequent events
            System.out.printf("[Repository] Loading from snapshot at version %d%n", 
                snapshot.get().getVersion());
            events = eventStore.getEventsAfterVersion(accountId, snapshot.get().getVersion());
            // TODO: Restore state from snapshot
        } else {
            // Load all events
            System.out.printf("[Repository] Loading all events for %s%n", accountId);
            events = eventStore.getEvents(accountId);
        }
        
        if (events.isEmpty() && !snapshot.isPresent()) {
            return null;
        }
        
        account.loadFromHistory(events);
        return account;
    }
    
    private void createSnapshot(Account account) {
        Map<String, Object> state = Map.of(
            "accountId", account.getAccountId(),
            "accountHolder", account.getAccountHolder(),
            "balance", account.getBalance(),
            "closed", account.isClosed()
        );
        
        eventStore.saveSnapshot(account.getAccountId(), account.getVersion(), state);
    }
}

/**
 * Account Service
 */
@Service
class AccountService {
    
    private final AccountRepository repository;
    
    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }
    
    public String createAccount(String accountId, String accountHolder, double initialBalance) {
        Account account = Account.create(accountId, accountHolder, initialBalance);
        repository.save(account);
        
        System.out.printf("[AccountService] Created account %s for %s%n", 
            accountId, accountHolder);
        
        return accountId;
    }
    
    public void deposit(String accountId, double amount, String description) {
        Account account = repository.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        
        account.deposit(amount, description);
        repository.save(account);
        
        System.out.printf("[AccountService] Deposited %.2f to account %s%n", 
            amount, accountId);
    }
    
    public void withdraw(String accountId, double amount, String description) {
        Account account = repository.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        
        account.withdraw(amount, description);
        repository.save(account);
        
        System.out.printf("[AccountService] Withdrew %.2f from account %s%n", 
            amount, accountId);
    }
    
    public Account getAccount(String accountId) {
        return repository.findById(accountId);
    }
}

/**
 * Concurrency Exception
 */
class ConcurrencyException extends RuntimeException {
    public ConcurrencyException(String message) {
        super(message);
    }
}

/**
 * REST Controller for Event Sourcing
 */
@RestController
@RequestMapping("/api/sourcing")
class EventSourcingController {
    
    private final AccountService accountService;
    private final InMemoryEventStore eventStore;
    
    public EventSourcingController(AccountService accountService, InMemoryEventStore eventStore) {
        this.accountService = accountService;
        this.eventStore = eventStore;
    }
    
    @PostMapping("/accounts")
    public Map<String, Object> createAccount(
            @RequestParam String accountId,
            @RequestParam String accountHolder,
            @RequestParam double initialBalance) {
        
        accountService.createAccount(accountId, accountHolder, initialBalance);
        
        return Map.of(
            "accountId", accountId,
            "accountHolder", accountHolder,
            "initialBalance", initialBalance
        );
    }
    
    @PostMapping("/accounts/{accountId}/deposit")
    public Map<String, Object> deposit(
            @PathVariable String accountId,
            @RequestParam double amount,
            @RequestParam(required = false, defaultValue = "Deposit") String description) {
        
        accountService.deposit(accountId, amount, description);
        Account account = accountService.getAccount(accountId);
        
        return Map.of(
            "accountId", accountId,
            "amount", amount,
            "newBalance", account.getBalance()
        );
    }
    
    @PostMapping("/accounts/{accountId}/withdraw")
    public Map<String, Object> withdraw(
            @PathVariable String accountId,
            @RequestParam double amount,
            @RequestParam(required = false, defaultValue = "Withdrawal") String description) {
        
        accountService.withdraw(accountId, amount, description);
        Account account = accountService.getAccount(accountId);
        
        return Map.of(
            "accountId", accountId,
            "amount", amount,
            "newBalance", account.getBalance()
        );
    }
    
    @GetMapping("/accounts/{accountId}")
    public Map<String, Object> getAccount(@PathVariable String accountId) {
        Account account = accountService.getAccount(accountId);
        
        if (account == null) {
            return Map.of("error", "Account not found");
        }
        
        return Map.of(
            "accountId", account.getAccountId(),
            "accountHolder", account.getAccountHolder(),
            "balance", account.getBalance(),
            "closed", account.isClosed(),
            "version", account.getVersion()
        );
    }
    
    @GetMapping("/accounts/{accountId}/events")
    public List<Map<String, Object>> getAccountEvents(@PathVariable String accountId) {
        List<DomainEvent> events = eventStore.getEvents(accountId);
        
        return events.stream()
            .map(event -> Map.of(
                "eventId", event.getEventId(),
                "eventType", event.getEventType(),
                "version", event.getVersion(),
                "timestamp", event.getTimestamp().toString()
            ))
            .toList();
    }
}
