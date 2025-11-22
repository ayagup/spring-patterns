package com.spring.patterns.batch;

import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;

/**
 * Item Processor Pattern - Spring Batch
 * 
 * ItemProcessor transforms input items to output items.
 * It's the middle component in Read-Process-Write pattern.
 * 
 * Key Method:
 * - process(I item): Transforms item from type I to O, or returns null to filter
 * 
 * Processor Capabilities:
 * - Transform data (change type or values)
 * - Validate data (throw exception or return null)
 * - Filter items (return null to skip)
 * - Enrich data (add additional information)
 * - Aggregate data (combine information)
 * 
 * Processing Patterns:
 * - Transformation: Change data format/structure
 * - Validation: Check business rules
 * - Filtering: Remove unwanted items
 * - Enrichment: Add data from external sources
 * - Composite: Chain multiple processors
 * 
 * Use Cases:
 * - Data normalization and standardization
 * - Business rule validation
 * - Data enrichment from multiple sources
 * - Format conversion
 * - Filtering based on criteria
 */
public class ItemProcessorPattern {

    /**
     * Input: Transaction record
     */
    public static class Transaction {
        private Long id;
        private String accountNumber;
        private BigDecimal amount;
        private String currency;
        private Date transactionDate;
        private String type; // DEBIT or CREDIT
        private String status; // PENDING, APPROVED, REJECTED

        public Transaction() {}

        public Transaction(Long id, String accountNumber, BigDecimal amount, 
                          String currency, Date transactionDate, String type, String status) {
            this.id = id;
            this.accountNumber = accountNumber;
            this.amount = amount;
            this.currency = currency;
            this.transactionDate = transactionDate;
            this.type = type;
            this.status = status;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public Date getTransactionDate() { return transactionDate; }
        public void setTransactionDate(Date transactionDate) { this.transactionDate = transactionDate; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        @Override
        public String toString() {
            return "Transaction{id=" + id + ", account='" + accountNumber + 
                   "', amount=" + amount + " " + currency + ", type=" + type + ", status=" + status + "}";
        }
    }

    /**
     * Output: Processed transaction report
     */
    public static class TransactionReport {
        private Long transactionId;
        private String accountNumber;
        private BigDecimal amountInUSD;
        private String transactionType;
        private String reportDate;
        private boolean flagged;

        public TransactionReport() {}

        // Getters and setters
        public Long getTransactionId() { return transactionId; }
        public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public BigDecimal getAmountInUSD() { return amountInUSD; }
        public void setAmountInUSD(BigDecimal amountInUSD) { this.amountInUSD = amountInUSD; }
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        public String getReportDate() { return reportDate; }
        public void setReportDate(String reportDate) { this.reportDate = reportDate; }
        public boolean isFlagged() { return flagged; }
        public void setFlagged(boolean flagged) { this.flagged = flagged; }

        @Override
        public String toString() {
            return "TransactionReport{id=" + transactionId + ", account='" + accountNumber + 
                   "', amountUSD=" + amountInUSD + ", type=" + transactionType + 
                   ", flagged=" + flagged + "}";
        }
    }

    // ========== Processor Implementations ==========

    /**
     * 1. Validation Processor
     * Validates business rules, returns null if invalid
     */
    public static class ValidationProcessor implements ItemProcessor<Transaction, Transaction> {
        
        @Override
        public Transaction process(Transaction transaction) {
            System.out.println("  [VALIDATE] " + transaction.getId());

            // Validation rules
            if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("    ✗ Invalid amount: " + transaction.getAmount());
                return null; // Filter out invalid
            }

            if (transaction.getAccountNumber() == null || transaction.getAccountNumber().isEmpty()) {
                System.out.println("    ✗ Invalid account number");
                return null;
            }

            if (transaction.getCurrency() == null || transaction.getCurrency().length() != 3) {
                System.out.println("    ✗ Invalid currency code");
                return null;
            }

            System.out.println("    ✓ Validation passed");
            return transaction;
        }
    }

    /**
     * 2. Transformation Processor
     * Transforms Transaction to TransactionReport
     */
    public static class TransformationProcessor implements ItemProcessor<Transaction, TransactionReport> {
        private static final Map<String, BigDecimal> EXCHANGE_RATES = new HashMap<String, BigDecimal>() {{
            put("USD", new BigDecimal("1.00"));
            put("EUR", new BigDecimal("1.18"));
            put("GBP", new BigDecimal("1.37"));
            put("JPY", new BigDecimal("0.0091"));
            put("CAD", new BigDecimal("0.79"));
        }};

        @Override
        public TransactionReport process(Transaction transaction) {
            System.out.println("  [TRANSFORM] " + transaction.getId());

            TransactionReport report = new TransactionReport();
            report.setTransactionId(transaction.getId());
            report.setAccountNumber(transaction.getAccountNumber());
            report.setTransactionType(transaction.getType());
            report.setReportDate(new Date().toString());

            // Convert to USD
            BigDecimal rate = EXCHANGE_RATES.getOrDefault(transaction.getCurrency(), BigDecimal.ONE);
            BigDecimal amountInUSD = transaction.getAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
            report.setAmountInUSD(amountInUSD);

            // Flag large transactions
            report.setFlagged(amountInUSD.compareTo(new BigDecimal("10000")) > 0);

            System.out.println("    -> Converted to: " + report);
            return report;
        }
    }

    /**
     * 3. Filtering Processor
     * Filters out unwanted items
     */
    public static class FilteringProcessor implements ItemProcessor<Transaction, Transaction> {
        private final String statusFilter;

        public FilteringProcessor(String statusFilter) {
            this.statusFilter = statusFilter;
        }

        @Override
        public Transaction process(Transaction transaction) {
            if (!statusFilter.equals(transaction.getStatus())) {
                System.out.println("  [FILTER] Skipping transaction " + transaction.getId() + 
                                 " (status: " + transaction.getStatus() + ")");
                return null; // Skip this item
            }
            System.out.println("  [FILTER] Accepting transaction " + transaction.getId());
            return transaction;
        }
    }

    /**
     * 4. Enrichment Processor
     * Adds additional data from external sources
     */
    public static class EnrichmentProcessor implements ItemProcessor<Transaction, Transaction> {
        private static final Map<String, String> ACCOUNT_NAMES = new HashMap<String, String>() {{
            put("ACC001", "John Doe");
            put("ACC002", "Jane Smith");
            put("ACC003", "Bob Johnson");
        }};

        @Override
        public Transaction process(Transaction transaction) {
            System.out.println("  [ENRICH] " + transaction.getId());

            // Simulate external lookup
            String accountName = ACCOUNT_NAMES.get(transaction.getAccountNumber());
            if (accountName != null) {
                System.out.println("    -> Account holder: " + accountName);
                // In real scenario, would add to transaction object
            }

            return transaction;
        }
    }

    /**
     * 5. Composite Processor
     * Chains multiple processors
     */
    public static class CompositeProcessor<I, O> implements ItemProcessor<I, O> {
        private final List<ItemProcessor<?, ?>> processors;

        @SafeVarargs
        public CompositeProcessor(ItemProcessor<?, ?>... processors) {
            this.processors = Arrays.asList(processors);
        }

        @Override
        @SuppressWarnings("unchecked")
        public O process(I item) throws Exception {
            Object current = item;
            
            for (ItemProcessor processor : processors) {
                if (current == null) {
                    return null; // Item was filtered out
                }
                current = processor.process(current);
            }
            
            return (O) current;
        }
    }

    /**
     * 6. Conditional Processor
     * Applies different processing based on condition
     */
    public static class ConditionalProcessor<T> implements ItemProcessor<T, T> {
        private final Function<T, Boolean> condition;
        private final ItemProcessor<T, T> trueProcessor;
        private final ItemProcessor<T, T> falseProcessor;

        public ConditionalProcessor(Function<T, Boolean> condition,
                                   ItemProcessor<T, T> trueProcessor,
                                   ItemProcessor<T, T> falseProcessor) {
            this.condition = condition;
            this.trueProcessor = trueProcessor;
            this.falseProcessor = falseProcessor;
        }

        @Override
        public T process(T item) throws Exception {
            if (condition.apply(item)) {
                return trueProcessor.process(item);
            } else {
                return falseProcessor.process(item);
            }
        }
    }

    /**
     * 7. Async Processor (Simulation)
     * Processes items asynchronously
     */
    public static class AsyncProcessor<I, O> implements ItemProcessor<I, O> {
        private final ItemProcessor<I, O> delegate;

        public AsyncProcessor(ItemProcessor<I, O> delegate) {
            this.delegate = delegate;
        }

        @Override
        public O process(I item) throws Exception {
            System.out.println("  [ASYNC] Processing in thread: " + Thread.currentThread().getName());
            return delegate.process(item);
        }
    }

    /**
     * Main demonstration
     */
    public static void main(String[] args) {
        System.out.println("=== Item Processor Pattern Demonstration ===\n");

        // Sample data
        List<Transaction> transactions = Arrays.asList(
            new Transaction(1L, "ACC001", new BigDecimal("1500.00"), "USD", new Date(), "DEBIT", "APPROVED"),
            new Transaction(2L, "ACC002", new BigDecimal("2500.00"), "EUR", new Date(), "CREDIT", "APPROVED"),
            new Transaction(3L, "ACC003", new BigDecimal("500.00"), "GBP", new Date(), "DEBIT", "PENDING"),
            new Transaction(4L, "ACC001", new BigDecimal("15000.00"), "USD", new Date(), "CREDIT", "APPROVED"),
            new Transaction(5L, "", new BigDecimal("100.00"), "USD", new Date(), "DEBIT", "REJECTED"), // Invalid
            new Transaction(6L, "ACC002", new BigDecimal("-100.00"), "USD", new Date(), "DEBIT", "APPROVED"), // Invalid
            new Transaction(7L, "ACC003", new BigDecimal("750.00"), "CAD", new Date(), "CREDIT", "APPROVED")
        );

        try {
            // Demo 1: Validation Processor
            System.out.println("\n========== Demo 1: Validation Processor ==========");
            ValidationProcessor validator = new ValidationProcessor();
            for (Transaction tx : transactions) {
                Transaction result = validator.process(tx);
                if (result != null) {
                    System.out.println("    Valid: " + result);
                }
            }

            // Demo 2: Transformation Processor
            System.out.println("\n========== Demo 2: Transformation Processor ==========");
            TransformationProcessor transformer = new TransformationProcessor();
            List<Transaction> validTransactions = transactions.subList(0, 4);
            for (Transaction tx : validTransactions) {
                TransactionReport report = transformer.process(tx);
                System.out.println("    Result: " + report);
            }

            // Demo 3: Filtering Processor
            System.out.println("\n========== Demo 3: Filtering Processor (APPROVED only) ==========");
            FilteringProcessor filter = new FilteringProcessor("APPROVED");
            for (Transaction tx : transactions) {
                Transaction result = filter.process(tx);
                if (result != null) {
                    System.out.println("    Passed: " + result);
                }
            }

            // Demo 4: Enrichment Processor
            System.out.println("\n========== Demo 4: Enrichment Processor ==========");
            EnrichmentProcessor enricher = new EnrichmentProcessor();
            for (Transaction tx : validTransactions) {
                enricher.process(tx);
            }

            // Demo 5: Composite Processor (Validate + Filter + Transform)
            System.out.println("\n========== Demo 5: Composite Processor Chain ==========");
            System.out.println("Chain: Validate -> Filter -> Transform");
            CompositeProcessor<Transaction, TransactionReport> composite = 
                new CompositeProcessor<>(
                    validator,
                    filter,
                    transformer
                );
            
            for (Transaction tx : transactions) {
                TransactionReport result = composite.process(tx);
                if (result != null) {
                    System.out.println("    Final: " + result);
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== Demonstration Complete ===");
    }
}

/**
 * KEY TAKEAWAYS:
 * 
 * 1. Processor Contract:
 *    - process(I item): Transform or validate one item
 *    - Return null to filter out item
 *    - Can change item type (I -> O)
 *    - Throw exception to fail processing
 * 
 * 2. Common Processing Patterns:
 *    - Validation: Check business rules
 *    - Transformation: Convert data format/type
 *    - Filtering: Remove unwanted items
 *    - Enrichment: Add external data
 *    - Normalization: Standardize data
 * 
 * 3. Null Return Behavior:
 *    - null = item filtered out
 *    - Not written to output
 *    - Counted as filtered in statistics
 *    - Different from skipping (exceptions)
 * 
 * 4. Processor Chaining:
 *    - CompositeItemProcessor: Chain processors
 *    - Output of one becomes input of next
 *    - null stops chain (item filtered)
 *    - Order matters!
 * 
 * 5. Error Handling:
 *    - Throw exception: Item skipped (with retry/skip policy)
 *    - Return null: Item filtered (no error)
 *    - Log warnings: Item processed with issues
 * 
 * 6. Stateless vs Stateful:
 *    - Stateless: No shared state (preferred)
 *    - Stateful: Maintain state (use with caution)
 *    - Thread safety important for partitioning
 * 
 * 7. Performance:
 *    - Keep processing logic efficient
 *    - Cache external lookups
 *    - Avoid heavy I/O in processor
 *    - Consider async processing for slow operations
 * 
 * 8. Best Practices:
 *    - Keep processors focused (single responsibility)
 *    - Make stateless when possible
 *    - Use null for filtering, exceptions for errors
 *    - Chain processors for complex logic
 *    - Log processing decisions
 *    - Validate early, transform late
 * 
 * 9. Testing:
 *    - Easy to unit test (simple interface)
 *    - Mock external dependencies
 *    - Test null returns
 *    - Test exception scenarios
 *    - Test with valid and invalid data
 * 
 * 10. Common Pitfalls:
 *     - Shared mutable state (thread safety)
 *     - Heavy I/O operations
 *     - Not handling null inputs
 *     - Swallowing exceptions
 *     - Complex logic (should be simple)
 */
