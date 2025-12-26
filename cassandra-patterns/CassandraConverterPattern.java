package com.example.cassandra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cassandra Converter Pattern
 * 
 * Demonstrates the use of CassandraConverter for custom type conversion
 * and User-Defined Types (UDTs) in Apache Cassandra.
 * 
 * Key concepts:
 * - User-Defined Types (UDTs)
 * - Custom type conversion
 * - Nested objects in Cassandra
 * - List and Set of UDTs
 * - Frozen types
 * 
 * Use cases:
 * - Complex data structures
 * - Embedded objects
 * - Address, contact information
 * - Hierarchical data
 * - Value objects
 */
@SpringBootApplication
public class CassandraConverterPattern {

    public static void main(String[] args) {
        SpringApplication.run(CassandraConverterPattern.class, args);
    }
}

/**
 * User-Defined Type for Address
 */
@UserDefinedType("address")
record Address(
    String street,
    String city,
    String state,
    String zipCode,
    String country
) {}

/**
 * User-Defined Type for Phone
 */
@UserDefinedType("phone")
record Phone(
    String type,      // mobile, home, work
    String number,
    String countryCode
) {}

/**
 * User-Defined Type for Email
 */
@UserDefinedType("email")
record Email(
    String type,      // personal, work
    String address
) {}

/**
 * Customer entity with UDTs
 */
record Customer(
    UUID id,
    String name,
    @CassandraType(type = CassandraType.Name.UDT, userTypeName = "address")
    Address billingAddress,
    @CassandraType(type = CassandraType.Name.UDT, userTypeName = "address")
    Address shippingAddress,
    List<@CassandraType(type = CassandraType.Name.UDT, userTypeName = "phone") Phone> phones,
    List<@CassandraType(type = CassandraType.Name.UDT, userTypeName = "email") Email> emails,
    LocalDateTime createdAt
) {
    public Customer {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (phones == null) {
            phones = new ArrayList<>();
        }
        if (emails == null) {
            emails = new ArrayList<>();
        }
    }
}

/**
 * Service for customer operations with UDTs
 */
@Service
class CustomerService {
    
    private final CassandraTemplate cassandraTemplate;
    private final CassandraConverter cassandraConverter;
    
    public CustomerService(CassandraTemplate cassandraTemplate, 
                          CassandraConverter cassandraConverter) {
        this.cassandraTemplate = cassandraTemplate;
        this.cassandraConverter = cassandraConverter;
    }
    
    /**
     * Create customer with UDTs
     */
    public Customer createCustomer(Customer customer) {
        return cassandraTemplate.insert(customer);
    }
    
    /**
     * Update customer
     */
    public Customer updateCustomer(Customer customer) {
        return cassandraTemplate.update(customer);
    }
    
    /**
     * Find customer by ID
     */
    public Customer findById(UUID id) {
        return cassandraTemplate.selectOneById(id, Customer.class);
    }
    
    /**
     * Find all customers
     */
    public List<Customer> findAll() {
        return cassandraTemplate.selectAll(Customer.class);
    }
    
    /**
     * Add phone to customer
     */
    public Customer addPhone(UUID customerId, Phone phone) {
        Customer customer = findById(customerId);
        if (customer != null) {
            List<Phone> phones = new ArrayList<>(customer.phones());
            phones.add(phone);
            Customer updated = new Customer(
                customer.id(),
                customer.name(),
                customer.billingAddress(),
                customer.shippingAddress(),
                phones,
                customer.emails(),
                customer.createdAt()
            );
            return cassandraTemplate.update(updated);
        }
        return null;
    }
    
    /**
     * Add email to customer
     */
    public Customer addEmail(UUID customerId, Email email) {
        Customer customer = findById(customerId);
        if (customer != null) {
            List<Email> emails = new ArrayList<>(customer.emails());
            emails.add(email);
            Customer updated = new Customer(
                customer.id(),
                customer.name(),
                customer.billingAddress(),
                customer.shippingAddress(),
                customer.phones(),
                emails,
                customer.createdAt()
            );
            return cassandraTemplate.update(updated);
        }
        return null;
    }
    
    /**
     * Update billing address
     */
    public Customer updateBillingAddress(UUID customerId, Address address) {
        Customer customer = findById(customerId);
        if (customer != null) {
            Customer updated = new Customer(
                customer.id(),
                customer.name(),
                address,
                customer.shippingAddress(),
                customer.phones(),
                customer.emails(),
                customer.createdAt()
            );
            return cassandraTemplate.update(updated);
        }
        return null;
    }
    
    /**
     * Update shipping address
     */
    public Customer updateShippingAddress(UUID customerId, Address address) {
        Customer customer = findById(customerId);
        if (customer != null) {
            Customer updated = new Customer(
                customer.id(),
                customer.name(),
                customer.billingAddress(),
                address,
                customer.phones(),
                customer.emails(),
                customer.createdAt()
            );
            return cassandraTemplate.update(updated);
        }
        return null;
    }
    
    /**
     * Delete customer
     */
    public boolean deleteById(UUID id) {
        return cassandraTemplate.deleteById(id, Customer.class);
    }
    
    /**
     * Count all customers
     */
    public long count() {
        return cassandraTemplate.count(Customer.class);
    }
}

/**
 * REST controller for customer operations
 */
@RestController
@RequestMapping("/api/customers")
class CustomerController {
    
    private final CustomerService customerService;
    
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }
    
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        return ResponseEntity.ok(customerService.createCustomer(customer));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable UUID id, @RequestBody Customer customer) {
        Customer updated = new Customer(id, customer.name(), customer.billingAddress(), 
                                       customer.shippingAddress(), customer.phones(), 
                                       customer.emails(), customer.createdAt());
        return ResponseEntity.ok(customerService.updateCustomer(updated));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable UUID id) {
        Customer customer = customerService.findById(id);
        return customer != null ? ResponseEntity.ok(customer) : ResponseEntity.notFound().build();
    }
    
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.findAll());
    }
    
    @PostMapping("/{id}/phones")
    public ResponseEntity<Customer> addPhone(@PathVariable UUID id, @RequestBody Phone phone) {
        Customer customer = customerService.addPhone(id, phone);
        return customer != null ? ResponseEntity.ok(customer) : ResponseEntity.notFound().build();
    }
    
    @PostMapping("/{id}/emails")
    public ResponseEntity<Customer> addEmail(@PathVariable UUID id, @RequestBody Email email) {
        Customer customer = customerService.addEmail(id, email);
        return customer != null ? ResponseEntity.ok(customer) : ResponseEntity.notFound().build();
    }
    
    @PutMapping("/{id}/billing-address")
    public ResponseEntity<Customer> updateBillingAddress(@PathVariable UUID id, @RequestBody Address address) {
        Customer customer = customerService.updateBillingAddress(id, address);
        return customer != null ? ResponseEntity.ok(customer) : ResponseEntity.notFound().build();
    }
    
    @PutMapping("/{id}/shipping-address")
    public ResponseEntity<Customer> updateShippingAddress(@PathVariable UUID id, @RequestBody Address address) {
        Customer customer = customerService.updateShippingAddress(id, address);
        return customer != null ? ResponseEntity.ok(customer) : ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> countCustomers() {
        return ResponseEntity.ok(customerService.count());
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("""
            Cassandra Converter Pattern
            
            This pattern demonstrates the use of CassandraConverter for custom type conversion
            and User-Defined Types (UDTs) in Apache Cassandra.
            
            Features:
            - User-Defined Types (UDTs) for complex structures
            - @UserDefinedType annotation
            - @CassandraType for UDT mapping
            - Nested objects (Address, Phone, Email)
            - Lists of UDTs
            - Custom type conversion
            
            UDTs:
            - Address: street, city, state, zipCode, country
            - Phone: type (mobile/home/work), number, countryCode
            - Email: type (personal/work), address
            
            Endpoints:
            - POST /api/customers - Create customer
            - PUT /api/customers/{id} - Update customer
            - GET /api/customers/{id} - Get customer
            - GET /api/customers - Get all customers
            - POST /api/customers/{id}/phones - Add phone
            - POST /api/customers/{id}/emails - Add email
            - PUT /api/customers/{id}/billing-address - Update billing address
            - PUT /api/customers/{id}/shipping-address - Update shipping address
            - DELETE /api/customers/{id} - Delete customer
            - GET /api/customers/count - Count customers
            """);
    }
}
