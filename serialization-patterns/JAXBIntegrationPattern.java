package com.example.serialization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * JAXB Integration Pattern
 * 
 * Comprehensive demonstration of JAXB (Java Architecture for XML Binding) integration.
 * 
 * Key Concepts:
 * 1. @XmlRootElement - Define root XML element
 * 2. @XmlElement - Map fields to XML elements
 * 3. @XmlAttribute - Map fields to XML attributes
 * 4. @XmlElementWrapper - Wrap collections
 * 5. @XmlAccessorType - Control field access
 * 6. @XmlType - Control property order
 * 7. @XmlJavaTypeAdapter - Custom type adapters
 * 8. @XmlTransient - Exclude from marshalling
 * 9. Jaxb2Marshaller configuration
 * 10. Schema validation
 * 
 * Use Cases:
 * - SOAP web services
 * - XML configuration files
 * - Legacy system integration
 * - Document generation
 * - Standards compliance
 * - Enterprise messaging
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class JAXBIntegrationPattern {

    public static void main(String[] args) {
        SpringApplication.run(JAXBIntegrationPattern.class, args);
        demonstrateJAXBIntegration();
    }
    
    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(
            OrderXML.class,
            CustomerXML.class,
            InvoiceXML.class,
            CompanyXML.class
        );
        marshaller.setMarshallerProperties(Map.of(
            Marshaller.JAXB_FORMATTED_OUTPUT, true,
            Marshaller.JAXB_ENCODING, "UTF-8"
        ));
        return marshaller;
    }
    
    private static void demonstrateJAXBIntegration() {
        System.out.println("=== JAXB Integration Pattern Demo ===\n");
        
        try {
            // 1. Basic Marshalling with Annotations
            CustomerXML customer = new CustomerXML();
            customer.setId(1L);
            customer.setName("John Doe");
            customer.setEmail("john@example.com");
            customer.setMemberSince(LocalDate.of(2020, 1, 15));
            customer.setStatus("ACTIVE");
            
            JAXBContext context = JAXBContext.newInstance(CustomerXML.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            StringWriter writer = new StringWriter();
            marshaller.marshal(customer, writer);
            
            System.out.println("1. Customer XML with Adapters:");
            System.out.println(writer.toString());
            System.out.println();
            
            // 2. Complex Object with Collections
            OrderXML order = new OrderXML();
            order.setOrderId(101L);
            order.setOrderNumber("ORD-2024-001");
            order.setOrderDate(LocalDateTime.now());
            order.setTotalAmount(599.99);
            
            List<OrderItemXML> items = new ArrayList<>();
            items.add(new OrderItemXML(1L, "Laptop", 1, 999.99));
            items.add(new OrderItemXML(2L, "Mouse", 2, 29.99));
            order.setItems(items);
            
            context = JAXBContext.newInstance(OrderXML.class);
            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            writer = new StringWriter();
            marshaller.marshal(order, writer);
            
            System.out.println("2. Order XML with Collections:");
            System.out.println(writer.toString());
            System.out.println();
            
            // 3. Unmarshalling
            String customerXml = writer.toString();
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader reader = new StringReader(customerXml);
            OrderXML unmarshalledOrder = (OrderXML) unmarshaller.unmarshal(new StreamSource(reader));
            
            System.out.println("3. Unmarshalled Order:");
            System.out.println("Order Number: " + unmarshalledOrder.getOrderNumber());
            System.out.println("Items: " + unmarshalledOrder.getItems().size());
            System.out.println();
            
            // 4. Invoice with Property Ordering
            InvoiceXML invoice = new InvoiceXML();
            invoice.setInvoiceNumber("INV-001");
            invoice.setIssueDate(LocalDate.now());
            invoice.setDueDate(LocalDate.now().plusDays(30));
            invoice.setAmount(1500.00);
            invoice.setCustomerName("Alice Johnson");
            
            context = JAXBContext.newInstance(InvoiceXML.class);
            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            writer = new StringWriter();
            marshaller.marshal(invoice, writer);
            
            System.out.println("4. Invoice XML with Ordered Properties:");
            System.out.println(writer.toString());
            System.out.println();
            
            // 5. Company with Address (Nested Objects)
            AddressXML address = new AddressXML("123 Main St", "New York", "NY", "10001", "USA");
            CompanyXML company = new CompanyXML();
            company.setCompanyId(1001L);
            company.setCompanyName("Tech Corp");
            company.setTaxId("TAX-123456");
            company.setAddress(address);
            company.setFoundedDate(LocalDate.of(2010, 6, 15));
            
            context = JAXBContext.newInstance(CompanyXML.class);
            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            writer = new StringWriter();
            marshaller.marshal(company, writer);
            
            System.out.println("5. Company XML with Nested Address:");
            System.out.println(writer.toString());
            System.out.println();
            
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}

/**
 * Customer with JAXB annotations
 */
@XmlRootElement(name = "customer")
@XmlAccessorType(XmlAccessType.FIELD)
class CustomerXML {
    @XmlAttribute
    private Long id;
    
    @XmlElement(required = true)
    private String name;
    
    @XmlElement
    private String email;
    
    @XmlElement
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate memberSince;
    
    @XmlElement
    private String status;
    
    public CustomerXML() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public LocalDate getMemberSince() { return memberSince; }
    public void setMemberSince(LocalDate memberSince) { this.memberSince = memberSince; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

/**
 * Order with XML collection wrapping
 */
@XmlRootElement(name = "order")
@XmlAccessorType(XmlAccessType.FIELD)
class OrderXML {
    @XmlAttribute(name = "id")
    private Long orderId;
    
    @XmlElement(required = true)
    private String orderNumber;
    
    @XmlElement
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime orderDate;
    
    @XmlElement
    private Double totalAmount;
    
    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    private List<OrderItemXML> items;
    
    public OrderXML() {}
    
    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public List<OrderItemXML> getItems() { return items; }
    public void setItems(List<OrderItemXML> items) { this.items = items; }
}

/**
 * Order Item
 */
@XmlAccessorType(XmlAccessType.FIELD)
class OrderItemXML {
    @XmlAttribute
    private Long id;
    
    @XmlElement
    private String productName;
    
    @XmlElement
    private int quantity;
    
    @XmlElement
    private Double price;
    
    public OrderItemXML() {}
    
    public OrderItemXML(Long id, String productName, int quantity, Double price) {
        this.id = id;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}

/**
 * Invoice with property ordering
 */
@XmlRootElement(name = "invoice")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"invoiceNumber", "issueDate", "dueDate", "customerName", "amount"})
class InvoiceXML {
    @XmlElement(required = true)
    private String invoiceNumber;
    
    @XmlElement
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate issueDate;
    
    @XmlElement
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate dueDate;
    
    @XmlElement(required = true)
    private String customerName;
    
    @XmlElement
    private Double amount;
    
    public InvoiceXML() {}
    
    // Getters and Setters
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}

/**
 * Address
 */
@XmlAccessorType(XmlAccessType.FIELD)
class AddressXML {
    @XmlElement
    private String street;
    
    @XmlElement
    private String city;
    
    @XmlElement
    private String state;
    
    @XmlElement
    private String zipCode;
    
    @XmlElement
    private String country;
    
    public AddressXML() {}
    
    public AddressXML(String street, String city, String state, String zipCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }
    
    // Getters and Setters
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}

/**
 * Company with nested address
 */
@XmlRootElement(name = "company")
@XmlAccessorType(XmlAccessType.FIELD)
class CompanyXML {
    @XmlAttribute(name = "id")
    private Long companyId;
    
    @XmlElement(required = true)
    private String companyName;
    
    @XmlElement
    private String taxId;
    
    @XmlElement
    private AddressXML address;
    
    @XmlElement
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate foundedDate;
    
    public CompanyXML() {}
    
    // Getters and Setters
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    
    public AddressXML getAddress() { return address; }
    public void setAddress(AddressXML address) { this.address = address; }
    
    public LocalDate getFoundedDate() { return foundedDate; }
    public void setFoundedDate(LocalDate foundedDate) { this.foundedDate = foundedDate; }
}

/**
 * LocalDate XML Adapter
 */
class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @Override
    public LocalDate unmarshal(String v) {
        return v != null ? LocalDate.parse(v, FORMATTER) : null;
    }
    
    @Override
    public String marshal(LocalDate v) {
        return v != null ? v.format(FORMATTER) : null;
    }
}

/**
 * LocalDateTime XML Adapter
 */
class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    @Override
    public LocalDateTime unmarshal(String v) {
        return v != null ? LocalDateTime.parse(v, FORMATTER) : null;
    }
    
    @Override
    public String marshal(LocalDateTime v) {
        return v != null ? v.format(FORMATTER) : null;
    }
}

@RestController
@RequestMapping("/api/jaxb")
class JAXBIntegrationController {
    
    @GetMapping(value = "/customer/{id}", produces = "application/xml")
    public CustomerXML getCustomer(@PathVariable Long id) {
        CustomerXML customer = new CustomerXML();
        customer.setId(id);
        customer.setName("Customer " + id);
        customer.setEmail("customer" + id + "@example.com");
        customer.setMemberSince(LocalDate.now());
        customer.setStatus("ACTIVE");
        return customer;
    }
    
    @GetMapping(value = "/order/{id}", produces = "application/xml")
    public OrderXML getOrder(@PathVariable Long id) {
        OrderXML order = new OrderXML();
        order.setOrderId(id);
        order.setOrderNumber("ORD-" + id);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(99.99);
        
        List<OrderItemXML> items = new ArrayList<>();
        items.add(new OrderItemXML(1L, "Product 1", 2, 49.99));
        order.setItems(items);
        
        return order;
    }
}
