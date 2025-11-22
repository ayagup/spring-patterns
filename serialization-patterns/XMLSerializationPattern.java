package com.example.serialization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * XML Serialization Pattern
 * 
 * Demonstrates XML serialization and deserialization using JAXB (Java Architecture for XML Binding).
 * 
 * Key Concepts:
 * 1. JAXB Marshaller - Converting objects to XML
 * 2. JAXB Unmarshaller - Converting XML to objects
 * 3. XML Annotations (@XmlRootElement, @XmlElement, etc.)
 * 4. XML Element mapping
 * 5. XML Attribute mapping
 * 6. Complex type handling
 * 7. Collection handling in XML
 * 8. Namespace management
 * 9. XML schema validation
 * 10. Custom XML formatting
 * 
 * Use Cases:
 * - SOAP web services
 * - Configuration files
 * - Data exchange with legacy systems
 * - Document generation
 * - Enterprise application integration
 * - Regulatory compliance (XML-based standards)
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class XMLSerializationPattern {

    public static void main(String[] args) {
        SpringApplication.run(XMLSerializationPattern.class, args);
        
        // Demo XML serialization
        demonstrateXMLSerialization();
    }
    
    /**
     * Configure JAXB Marshaller bean
     */
    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(
            BookXML.class, 
            AuthorXML.class, 
            LibraryXML.class,
            EmployeeXML.class,
            DepartmentXML.class
        );
        return marshaller;
    }
    
    private static void demonstrateXMLSerialization() {
        System.out.println("=== XML Serialization Pattern Demo ===\n");
        
        try {
            JAXBContext jaxbContext;
            Marshaller marshaller;
            Unmarshaller unmarshaller;
            
            // 1. Simple Object to XML
            BookXML book = new BookXML(
                1L, 
                "Effective Java", 
                "978-0134685991", 
                49.99,
                LocalDate.of(2017, 12, 27)
            );
            
            jaxbContext = JAXBContext.newInstance(BookXML.class);
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            StringWriter bookWriter = new StringWriter();
            marshaller.marshal(book, bookWriter);
            System.out.println("1. Simple Object to XML:");
            System.out.println(bookWriter.toString());
            System.out.println();
            
            // 2. Complex Object with Nested Elements
            AuthorXML author = new AuthorXML(
                1L,
                "Joshua Bloch",
                "joshua@example.com",
                "USA"
            );
            
            jaxbContext = JAXBContext.newInstance(AuthorXML.class);
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            StringWriter authorWriter = new StringWriter();
            marshaller.marshal(author, authorWriter);
            System.out.println("2. Complex Object to XML:");
            System.out.println(authorWriter.toString());
            System.out.println();
            
            // 3. Collection to XML
            LibraryXML library = new LibraryXML();
            library.setName("City Library");
            library.setLocation("New York");
            
            List<BookXML> books = new ArrayList<>();
            books.add(new BookXML(1L, "Java Concurrency", "978-0321349606", 59.99, LocalDate.of(2006, 5, 9)));
            books.add(new BookXML(2L, "Clean Code", "978-0132350884", 44.99, LocalDate.of(2008, 8, 1)));
            books.add(new BookXML(3L, "Design Patterns", "978-0201633612", 54.99, LocalDate.of(1994, 10, 21)));
            library.setBooks(books);
            
            jaxbContext = JAXBContext.newInstance(LibraryXML.class);
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            StringWriter libraryWriter = new StringWriter();
            marshaller.marshal(library, libraryWriter);
            System.out.println("3. Collection to XML:");
            System.out.println(libraryWriter.toString());
            System.out.println();
            
            // 4. XML to Object (Unmarshalling)
            String bookXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                           "<book id=\"5\">" +
                           "<title>Spring in Action</title>" +
                           "<isbn>978-1617294945</isbn>" +
                           "<price>44.99</price>" +
                           "<publishedDate>2018-10-05</publishedDate>" +
                           "</book>";
            
            jaxbContext = JAXBContext.newInstance(BookXML.class);
            unmarshaller = jaxbContext.createUnmarshaller();
            
            StringReader reader = new StringReader(bookXml);
            BookXML unmarshalledBook = (BookXML) unmarshaller.unmarshal(new StreamSource(reader));
            System.out.println("4. XML to Object (Unmarshalling):");
            System.out.println("Unmarshalled Book: " + unmarshalledBook);
            System.out.println();
            
            // 5. XML with Attributes
            EmployeeXML employee = new EmployeeXML(
                101L,
                "EMP101",
                "John Doe",
                "john.doe@company.com",
                "Senior Developer",
                LocalDateTime.now()
            );
            
            jaxbContext = JAXBContext.newInstance(EmployeeXML.class);
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            StringWriter empWriter = new StringWriter();
            marshaller.marshal(employee, empWriter);
            System.out.println("5. XML with Attributes:");
            System.out.println(empWriter.toString());
            System.out.println();
            
            // 6. Department with Employee List
            DepartmentXML department = new DepartmentXML();
            department.setId(1L);
            department.setName("Engineering");
            department.setCode("ENG");
            
            List<EmployeeXML> employees = new ArrayList<>();
            employees.add(new EmployeeXML(101L, "EMP101", "Alice Johnson", "alice@company.com", "Tech Lead", LocalDateTime.now()));
            employees.add(new EmployeeXML(102L, "EMP102", "Bob Smith", "bob@company.com", "Developer", LocalDateTime.now()));
            department.setEmployees(employees);
            
            jaxbContext = JAXBContext.newInstance(DepartmentXML.class);
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            StringWriter deptWriter = new StringWriter();
            marshaller.marshal(department, deptWriter);
            System.out.println("6. Department with Employee Collection:");
            System.out.println(deptWriter.toString());
            System.out.println();
            
        } catch (JAXBException e) {
            System.err.println("JAXB Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * Book entity with XML annotations
 */
@XmlRootElement(name = "book")
@XmlAccessorType(XmlAccessType.FIELD)
class BookXML {
    
    @XmlAttribute
    private Long id;
    
    @XmlElement(required = true)
    private String title;
    
    @XmlElement
    private String isbn;
    
    @XmlElement
    private Double price;
    
    @XmlElement
    private LocalDate publishedDate;
    
    public BookXML() {}
    
    public BookXML(Long id, String title, String isbn, Double price, LocalDate publishedDate) {
        this.id = id;
        this.title = title;
        this.isbn = isbn;
        this.price = price;
        this.publishedDate = publishedDate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public LocalDate getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDate publishedDate) { this.publishedDate = publishedDate; }
    
    @Override
    public String toString() {
        return "BookXML{id=" + id + ", title='" + title + "', isbn='" + isbn + 
               "', price=" + price + ", publishedDate=" + publishedDate + "}";
    }
}

/**
 * Author entity with XML annotations
 */
@XmlRootElement(name = "author")
@XmlAccessorType(XmlAccessType.FIELD)
class AuthorXML {
    
    @XmlElement
    private Long id;
    
    @XmlElement(required = true)
    private String name;
    
    @XmlElement
    private String email;
    
    @XmlElement
    private String country;
    
    public AuthorXML() {}
    
    public AuthorXML(Long id, String name, String email, String country) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.country = country;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}

/**
 * Library entity with book collection
 */
@XmlRootElement(name = "library")
@XmlAccessorType(XmlAccessType.FIELD)
class LibraryXML {
    
    @XmlElement
    private String name;
    
    @XmlElement
    private String location;
    
    @XmlElementWrapper(name = "books")
    @XmlElement(name = "book")
    private List<BookXML> books;
    
    public LibraryXML() {}
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public List<BookXML> getBooks() { return books; }
    public void setBooks(List<BookXML> books) { this.books = books; }
}

/**
 * Employee entity with XML attributes and elements
 */
@XmlRootElement(name = "employee")
@XmlAccessorType(XmlAccessType.FIELD)
class EmployeeXML {
    
    @XmlAttribute
    private Long id;
    
    @XmlAttribute
    private String employeeCode;
    
    @XmlElement(required = true)
    private String name;
    
    @XmlElement
    private String email;
    
    @XmlElement
    private String position;
    
    @XmlElement
    private LocalDateTime hiredDate;
    
    public EmployeeXML() {}
    
    public EmployeeXML(Long id, String employeeCode, String name, String email, 
                       String position, LocalDateTime hiredDate) {
        this.id = id;
        this.employeeCode = employeeCode;
        this.name = name;
        this.email = email;
        this.position = position;
        this.hiredDate = hiredDate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public LocalDateTime getHiredDate() { return hiredDate; }
    public void setHiredDate(LocalDateTime hiredDate) { this.hiredDate = hiredDate; }
}

/**
 * Department entity with employee collection
 */
@XmlRootElement(name = "department")
@XmlAccessorType(XmlAccessType.FIELD)
class DepartmentXML {
    
    @XmlAttribute
    private Long id;
    
    @XmlAttribute
    private String code;
    
    @XmlElement(required = true)
    private String name;
    
    @XmlElementWrapper(name = "employees")
    @XmlElement(name = "employee")
    private List<EmployeeXML> employees;
    
    public DepartmentXML() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public List<EmployeeXML> getEmployees() { return employees; }
    public void setEmployees(List<EmployeeXML> employees) { this.employees = employees; }
}

/**
 * REST Controller demonstrating XML serialization
 */
@RestController
@RequestMapping("/api/xml")
class XMLSerializationController {
    
    private final Jaxb2Marshaller jaxb2Marshaller;
    
    public XMLSerializationController(Jaxb2Marshaller jaxb2Marshaller) {
        this.jaxb2Marshaller = jaxb2Marshaller;
    }
    
    @GetMapping(value = "/book/{id}", produces = "application/xml")
    public BookXML getBook(@PathVariable Long id) {
        return new BookXML(id, "Sample Book " + id, "ISBN-" + id, 29.99, LocalDate.now());
    }
    
    @PostMapping(value = "/book", consumes = "application/xml", produces = "application/xml")
    public BookXML createBook(@RequestBody BookXML book) {
        System.out.println("Received book: " + book);
        return book;
    }
    
    @GetMapping(value = "/library", produces = "application/xml")
    public LibraryXML getLibrary() {
        LibraryXML library = new LibraryXML();
        library.setName("Central Library");
        library.setLocation("Boston");
        
        List<BookXML> books = new ArrayList<>();
        books.add(new BookXML(1L, "Book One", "ISBN-001", 19.99, LocalDate.now()));
        books.add(new BookXML(2L, "Book Two", "ISBN-002", 24.99, LocalDate.now()));
        library.setBooks(books);
        
        return library;
    }
    
    @GetMapping(value = "/department/{id}", produces = "application/xml")
    public DepartmentXML getDepartment(@PathVariable Long id) {
        DepartmentXML department = new DepartmentXML();
        department.setId(id);
        department.setName("Department " + id);
        department.setCode("DEPT" + id);
        
        List<EmployeeXML> employees = new ArrayList<>();
        employees.add(new EmployeeXML(1L, "EMP001", "Employee 1", "emp1@company.com", "Manager", LocalDateTime.now()));
        department.setEmployees(employees);
        
        return department;
    }
    
    @PostMapping("/marshal")
    public String marshalObject(@RequestBody Object object) {
        StringWriter writer = new StringWriter();
        jaxb2Marshaller.marshal(object, new StreamResult(writer));
        return writer.toString();
    }
}
