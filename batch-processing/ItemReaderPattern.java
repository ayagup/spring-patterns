package com.spring.patterns.batch;

import org.springframework.batch.item.*;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Item Reader Pattern - Spring Batch
 * 
 * ItemReader is responsible for reading data from a source one item at a time.
 * It's a key component of chunk-oriented processing.
 * 
 * Key Method:
 * - read(): Returns one item or null when no more items
 * 
 * Built-in Readers:
 * - FlatFileItemReader: Read from CSV, fixed-width files
 * - StaxEventItemReader: Read XML files
 * - JdbcCursorItemReader: Read from database using cursor
 * - JdbcPagingItemReader: Read database using pagination
 * - JpaPagingItemReader: Read using JPA
 * - MongoItemReader: Read from MongoDB
 * - JsonItemReader: Read JSON files
 * 
 * Reader Characteristics:
 * - Stateful during chunk processing
 * - Returns null to indicate end of data
 * - Should be thread-safe for partitioning
 * - Can implement ItemStream for restart support
 * 
 * Use Cases:
 * - Read files (CSV, JSON, XML)
 * - Read database records
 * - Read from message queues
 * - Read from REST APIs
 * - Read from NoSQL databases
 */
public class ItemReaderPattern {

    /**
     * Customer domain object
     */
    public static class Customer {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String city;
        private String country;

        public Customer() {}

        public Customer(Long id, String firstName, String lastName, String email, String city, String country) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.city = city;
            this.country = country;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        @Override
        public String toString() {
            return "Customer{id=" + id + ", name='" + firstName + " " + lastName + 
                   "', email='" + email + "', city='" + city + "', country='" + country + "'}";
        }
    }

    // ========== Custom ItemReader Implementations ==========

    /**
     * 1. Simple List-based ItemReader
     * Reads from an in-memory list
     */
    public static class ListItemReader<T> implements ItemReader<T> {
        private final List<T> items;
        private int currentIndex = 0;

        public ListItemReader(List<T> items) {
            this.items = new ArrayList<>(items);
        }

        @Override
        public T read() {
            if (currentIndex < items.size()) {
                T item = items.get(currentIndex++);
                System.out.println("  [READ] Item " + currentIndex + "/" + items.size() + ": " + item);
                return item;
            }
            System.out.println("  [READ] End of data reached");
            return null;
        }

        public void reset() {
            currentIndex = 0;
        }
    }

    /**
     * 2. Paginated ItemReader
     * Reads data in pages/chunks
     */
    public static class PaginatedItemReader<T> implements ItemReader<T> {
        private final List<T> allItems;
        private final int pageSize;
        private int currentPage = 0;
        private int currentIndexInPage = 0;
        private List<T> currentPageItems;

        public PaginatedItemReader(List<T> allItems, int pageSize) {
            this.allItems = allItems;
            this.pageSize = pageSize;
            loadPage();
        }

        private void loadPage() {
            int startIndex = currentPage * pageSize;
            int endIndex = Math.min(startIndex + pageSize, allItems.size());
            
            if (startIndex < allItems.size()) {
                currentPageItems = allItems.subList(startIndex, endIndex);
                System.out.println("  [PAGINATION] Loaded page " + (currentPage + 1) + 
                                 " with " + currentPageItems.size() + " items");
            } else {
                currentPageItems = Collections.emptyList();
            }
        }

        @Override
        public T read() {
            if (currentIndexInPage >= currentPageItems.size()) {
                currentPage++;
                currentIndexInPage = 0;
                loadPage();
            }

            if (currentIndexInPage < currentPageItems.size()) {
                return currentPageItems.get(currentIndexInPage++);
            }

            return null;
        }
    }

    /**
     * 3. Filtered ItemReader
     * Wraps another reader and filters results
     */
    public static class FilteredItemReader<T> implements ItemReader<T> {
        private final ItemReader<T> delegate;
        private final Predicate<T> filter;

        public interface Predicate<T> {
            boolean test(T item);
        }

        public FilteredItemReader(ItemReader<T> delegate, Predicate<T> filter) {
            this.delegate = delegate;
            this.filter = filter;
        }

        @Override
        public T read() throws Exception {
            T item;
            while ((item = delegate.read()) != null) {
                if (filter.test(item)) {
                    System.out.println("  [FILTER] Item passed filter: " + item);
                    return item;
                }
                System.out.println("  [FILTER] Item filtered out: " + item);
            }
            return null;
        }
    }

    /**
     * 4. Composite ItemReader
     * Reads from multiple readers sequentially
     */
    public static class CompositeItemReader<T> implements ItemReader<T> {
        private final List<ItemReader<T>> readers;
        private int currentReaderIndex = 0;

        public CompositeItemReader(List<ItemReader<T>> readers) {
            this.readers = readers;
        }

        @Override
        public T read() throws Exception {
            while (currentReaderIndex < readers.size()) {
                T item = readers.get(currentReaderIndex).read();
                if (item != null) {
                    return item;
                }
                // Current reader exhausted, move to next
                currentReaderIndex++;
                if (currentReaderIndex < readers.size()) {
                    System.out.println("  [COMPOSITE] Switching to reader " + (currentReaderIndex + 1));
                }
            }
            return null;
        }
    }

    /**
     * 5. Synchronized/Thread-Safe ItemReader
     * Wraps a reader for multi-threaded access
     */
    public static class SynchronizedItemReader<T> implements ItemReader<T> {
        private final ItemReader<T> delegate;

        public SynchronizedItemReader(ItemReader<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public synchronized T read() throws Exception {
            return delegate.read();
        }
    }

    /**
     * 6. Counting ItemReader
     * Tracks read statistics
     */
    public static class CountingItemReader<T> implements ItemReader<T> {
        private final ItemReader<T> delegate;
        private final AtomicInteger readCount = new AtomicInteger(0);
        private final AtomicInteger nullCount = new AtomicInteger(0);

        public CountingItemReader(ItemReader<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T read() throws Exception {
            T item = delegate.read();
            if (item != null) {
                readCount.incrementAndGet();
            } else {
                nullCount.incrementAndGet();
            }
            return item;
        }

        public int getReadCount() {
            return readCount.get();
        }

        public int getNullCount() {
            return nullCount.get();
        }

        public void printStatistics() {
            System.out.println("\n=== Reader Statistics ===");
            System.out.println("Items Read: " + getReadCount());
            System.out.println("Null Returns: " + getNullCount());
            System.out.println("========================\n");
        }
    }

    /**
     * 7. Retry-able ItemReader
     * Retries on transient failures
     */
    public static class RetryableItemReader<T> implements ItemReader<T> {
        private final ItemReader<T> delegate;
        private final int maxRetries;
        private final long retryDelay;

        public RetryableItemReader(ItemReader<T> delegate, int maxRetries, long retryDelay) {
            this.delegate = delegate;
            this.maxRetries = maxRetries;
            this.retryDelay = retryDelay;
        }

        @Override
        public T read() throws Exception {
            int attempts = 0;
            while (attempts < maxRetries) {
                try {
                    return delegate.read();
                } catch (Exception e) {
                    attempts++;
                    if (attempts >= maxRetries) {
                        throw e;
                    }
                    System.out.println("  [RETRY] Attempt " + attempts + " failed, retrying...");
                    Thread.sleep(retryDelay);
                }
            }
            return null;
        }
    }

    /**
     * 8. Peek-able ItemReader
     * Allows peeking at next item without consuming
     */
    public static class PeekableItemReader<T> implements ItemReader<T> {
        private final ItemReader<T> delegate;
        private T peekedItem = null;
        private boolean hasPeeked = false;

        public PeekableItemReader(ItemReader<T> delegate) {
            this.delegate = delegate;
        }

        public T peek() throws Exception {
            if (!hasPeeked) {
                peekedItem = delegate.read();
                hasPeeked = true;
            }
            return peekedItem;
        }

        @Override
        public T read() throws Exception {
            if (hasPeeked) {
                hasPeeked = false;
                T item = peekedItem;
                peekedItem = null;
                return item;
            }
            return delegate.read();
        }
    }

    /**
     * Main demonstration
     */
    public static void main(String[] args) {
        System.out.println("=== Item Reader Pattern Demonstration ===\n");

        // Sample data
        List<Customer> customers = Arrays.asList(
            new Customer(1L, "John", "Doe", "john@example.com", "New York", "USA"),
            new Customer(2L, "Jane", "Smith", "jane@example.com", "London", "UK"),
            new Customer(3L, "Bob", "Johnson", "bob@example.com", "Toronto", "Canada"),
            new Customer(4L, "Alice", "Williams", "alice@example.com", "Sydney", "Australia"),
            new Customer(5L, "Charlie", "Brown", "charlie@example.com", "Paris", "France"),
            new Customer(6L, "Diana", "Prince", "diana@example.com", "Berlin", "Germany"),
            new Customer(7L, "Eve", "Davis", "eve@example.com", "Tokyo", "Japan"),
            new Customer(8L, "Frank", "Miller", "frank@example.com", "Mumbai", "India"),
            new Customer(9L, "Grace", "Lee", "grace@example.com", "Seoul", "South Korea"),
            new Customer(10L, "Henry", "Wilson", "henry@example.com", "Mexico City", "Mexico")
        );

        try {
            // Demo 1: Simple List Reader
            System.out.println("\n========== Demo 1: Simple List Reader ==========");
            ListItemReader<Customer> listReader = new ListItemReader<>(customers.subList(0, 5));
            readAll(listReader);

            // Demo 2: Paginated Reader
            System.out.println("\n========== Demo 2: Paginated Reader (Page Size: 3) ==========");
            PaginatedItemReader<Customer> paginatedReader = new PaginatedItemReader<>(customers, 3);
            readAll(paginatedReader);

            // Demo 3: Filtered Reader (US customers only)
            System.out.println("\n========== Demo 3: Filtered Reader (USA only) ==========");
            FilteredItemReader<Customer> filteredReader = new FilteredItemReader<>(
                new ListItemReader<>(customers),
                customer -> "USA".equals(customer.getCountry())
            );
            readAll(filteredReader);

            // Demo 4: Composite Reader
            System.out.println("\n========== Demo 4: Composite Reader (Multiple Sources) ==========");
            CompositeItemReader<Customer> compositeReader = new CompositeItemReader<>(Arrays.asList(
                new ListItemReader<>(customers.subList(0, 3)),
                new ListItemReader<>(customers.subList(3, 6)),
                new ListItemReader<>(customers.subList(6, 10))
            ));
            readAll(compositeReader);

            // Demo 5: Counting Reader
            System.out.println("\n========== Demo 5: Counting Reader (Statistics) ==========");
            CountingItemReader<Customer> countingReader = new CountingItemReader<>(
                new ListItemReader<>(customers)
            );
            readAll(countingReader);
            countingReader.printStatistics();

            // Demo 6: Synchronized Reader (Thread-Safe)
            System.out.println("\n========== Demo 6: Synchronized Reader ==========");
            SynchronizedItemReader<Customer> syncReader = new SynchronizedItemReader<>(
                new ListItemReader<>(customers.subList(0, 5))
            );
            System.out.println("Thread-safe reader created (safe for concurrent access)");
            readAll(syncReader);

            // Demo 7: Peekable Reader
            System.out.println("\n========== Demo 7: Peekable Reader ==========");
            PeekableItemReader<Customer> peekableReader = new PeekableItemReader<>(
                new ListItemReader<>(customers.subList(0, 3))
            );
            System.out.println("Peeking at first item: " + peekableReader.peek());
            System.out.println("Peeking again (same item): " + peekableReader.peek());
            System.out.println("Now reading (consumes item): " + peekableReader.read());
            System.out.println("Reading next item: " + peekableReader.read());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== Demonstration Complete ===");
    }

    private static <T> void readAll(ItemReader<T> reader) throws Exception {
        T item;
        int count = 0;
        while ((item = reader.read()) != null) {
            count++;
        }
        System.out.println("Total items read: " + count);
    }
}

/**
 * KEY TAKEAWAYS:
 * 
 * 1. ItemReader Contract:
 *    - read() returns one item at a time
 *    - Returns null to indicate end of data
 *    - Should be stateful during processing
 * 
 * 2. Reader Types:
 *    - List-based: In-memory data
 *    - File-based: CSV, JSON, XML files
 *    - Database: JDBC, JPA, Hibernate
 *    - NoSQL: MongoDB, Cassandra, Redis
 *    - API-based: REST, SOAP services
 * 
 * 3. Reader Patterns:
 *    - Filtered: Skip unwanted items
 *    - Paginated: Process in pages
 *    - Composite: Multiple sources
 *    - Synchronized: Thread-safe access
 *    - Counting: Track statistics
 *    - Retryable: Handle transient failures
 * 
 * 4. Thread Safety:
 *    - Readers should be thread-safe for partitioning
 *    - Use synchronized wrapper for non-thread-safe readers
 *    - Avoid shared mutable state
 * 
 * 5. Restart Support:
 *    - Implement ItemStream for restart capability
 *    - Save position in ExecutionContext
 *    - Resume from last position on restart
 * 
 * 6. Performance Considerations:
 *    - Batch database reads (pagination)
 *    - Stream large files (don't load all in memory)
 *    - Use cursors for databases
 *    - Cache frequently accessed data
 * 
 * 7. Best Practices:
 *    - Return null only when truly done
 *    - Make idempotent for restarts
 *    - Log read operations
 *    - Handle exceptions gracefully
 *    - Validate data as early as possible
 *    - Close resources properly
 * 
 * 8. Common Pitfalls:
 *    - Not returning null at end
 *    - Resource leaks (not closing connections)
 *    - Not thread-safe for partitioning
 *    - Loading entire dataset in memory
 *    - Not handling restarts
 * 
 * 9. Integration:
 *    - Pair with ItemProcessor for transformation
 *    - Use with ItemWriter for complete pipeline
 *    - Combine with listeners for monitoring
 *    - Add fault tolerance for resilience
 */
