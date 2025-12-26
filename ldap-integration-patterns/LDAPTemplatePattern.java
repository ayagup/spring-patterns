package com.example.ldap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.web.bind.annotation.*;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * LDAP Template Pattern
 * 
 * Demonstrates Spring LDAP Template for simplified LDAP operations and directory access.
 * 
 * LdapTemplate Features:
 * - Simplified LDAP search operations
 * - Automatic resource management (context handling)
 * - Exception translation to Spring's DataAccessException hierarchy
 * - Support for various search scopes (object, onelevel, subtree)
 * - Attribute mapping and extraction
 * 
 * Key Operations:
 * - Search: Find entries based on filters
 * - Lookup: Retrieve specific entry by DN
 * - Bind: Create new entry
 * - Rebind: Update existing entry
 * - Unbind: Delete entry
 * - ModifyAttributes: Partial update
 * 
 * Use Cases:
 * - User directory lookups
 * - Authentication against LDAP
 * - Group membership queries
 * - Organizational unit traversal
 * - Directory synchronization
 * 
 * Security Considerations:
 * - Use secure LDAP (ldaps://) for sensitive operations
 * - Sanitize user inputs to prevent LDAP injection
 * - Limit search scope and size to prevent DoS
 * - Use connection pooling for performance
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class LDAPTemplatePattern {

    /**
     * LDAP context source configuration
     */
    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://localhost:389");
        contextSource.setBase("dc=example,dc=com");
        contextSource.setUserDn("cn=admin,dc=example,dc=com");
        contextSource.setPassword("admin-password");
        
        // Connection pooling
        contextSource.setPooled(true);
        
        // Additional settings
        Map<String, Object> baseEnvironment = new HashMap<>();
        baseEnvironment.put("java.naming.ldap.attributes.binary", "objectGUID");
        contextSource.setBaseEnvironmentProperties(baseEnvironment);
        
        return contextSource;
    }

    /**
     * LDAP template bean
     */
    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }
}

/**
 * LDAP template service
 */
@RestController
@RequestMapping("/api/ldap-template")
class LDAPTemplateService {

    private final LdapTemplate ldapTemplate;

    public LDAPTemplateService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    /**
     * Search for users by common name
     */
    public List<Person> searchByName(String name) {
        LdapQuery query = query()
            .base("ou=people")
            .where("cn").like(name + "*");
        
        return ldapTemplate.search(query, new PersonAttributesMapper());
    }

    /**
     * Search for users by email
     */
    public List<Person> searchByEmail(String email) {
        LdapQuery query = query()
            .base("ou=people")
            .where("mail").is(email);
        
        return ldapTemplate.search(query, new PersonAttributesMapper());
    }

    /**
     * Search with multiple filters
     */
    public List<Person> searchByMultipleCriteria(String surname, String organizationalUnit) {
        LdapQuery query = query()
            .base("ou=people")
            .where("sn").is(surname)
            .and("ou").is(organizationalUnit);
        
        return ldapTemplate.search(query, new PersonAttributesMapper());
    }

    /**
     * Find all users in organizational unit
     */
    public List<Person> findAllInOU(String ouName) {
        LdapQuery query = query()
            .base("ou=" + ouName)
            .where("objectclass").is("person");
        
        return ldapTemplate.search(query, new PersonAttributesMapper());
    }

    /**
     * Lookup user by distinguished name
     */
    public Person findByDn(String dn) {
        try {
            return ldapTemplate.lookup(dn, new PersonAttributesMapper());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Create new person entry
     */
    public boolean createPerson(Person person) {
        try {
            DirContext context = ldapTemplate.getContextSource().getReadWriteContext();
            javax.naming.directory.Attributes attrs = new javax.naming.directory.BasicAttributes();
            
            javax.naming.directory.Attribute objClass = new javax.naming.directory.BasicAttribute("objectClass");
            objClass.add("top");
            objClass.add("person");
            objClass.add("organizationalPerson");
            objClass.add("inetOrgPerson");
            attrs.put(objClass);
            
            attrs.put("cn", person.commonName());
            attrs.put("sn", person.surname());
            if (person.mail() != null) {
                attrs.put("mail", person.mail());
            }
            if (person.telephoneNumber() != null) {
                attrs.put("telephoneNumber", person.telephoneNumber());
            }
            
            context.createSubcontext("cn=" + person.commonName() + ",ou=people", attrs);
            context.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Update person attributes
     */
    public boolean updatePerson(String dn, Map<String, String> updates) {
        try {
            DirContext context = ldapTemplate.getContextSource().getReadWriteContext();
            javax.naming.directory.ModificationItem[] mods = 
                new javax.naming.directory.ModificationItem[updates.size()];
            
            int i = 0;
            for (Map.Entry<String, String> entry : updates.entrySet()) {
                javax.naming.directory.Attribute attr = 
                    new javax.naming.directory.BasicAttribute(entry.getKey(), entry.getValue());
                mods[i++] = new javax.naming.directory.ModificationItem(
                    DirContext.REPLACE_ATTRIBUTE, attr);
            }
            
            context.modifyAttributes(dn, mods);
            context.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Delete person entry
     */
    public boolean deletePerson(String dn) {
        try {
            ldapTemplate.unbind(dn);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if entry exists
     */
    public boolean exists(String dn) {
        try {
            ldapTemplate.lookup(dn);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Count entries in organizational unit
     */
    public int countEntriesInOU(String ouName) {
        LdapQuery query = query()
            .base("ou=" + ouName)
            .where("objectclass").is("person");
        
        return ldapTemplate.search(query, new PersonAttributesMapper()).size();
    }

    /**
     * Search with custom filter
     */
    public List<Person> searchWithCustomFilter(String filter) {
        return ldapTemplate.search(
            "ou=people",
            filter,
            new PersonAttributesMapper()
        );
    }

    /**
     * Get all attribute names for entry
     */
    public List<String> getAttributeNames(String dn) {
        try {
            Attributes attrs = ldapTemplate.lookup(dn, (AttributesMapper<Attributes>) attributes -> attributes);
            if (attrs != null) {
                List<String> attrNames = new ArrayList<>();
                var enumeration = attrs.getIDs();
                while (enumeration.hasMoreElements()) {
                    attrNames.add(enumeration.nextElement());
                }
                return attrNames;
            }
        } catch (Exception e) {
            // Entry not found
        }
        return Collections.emptyList();
    }

    /**
     * Get LDAP statistics
     */
    public LDAPStatistics getStatistics() {
        int totalUsers = countEntriesInOU("people");
        int totalGroups = countEntriesInOU("groups");
        
        return new LDAPStatistics(
            totalUsers,
            totalGroups,
            List.of("ou=people", "ou=groups", "ou=system"),
            true,
            "ldap://localhost:389"
        );
    }

    record LDAPStatistics(int totalUsers, int totalGroups, List<String> organizationalUnits, 
                         boolean connected, String serverUrl) {}
}

/**
 * Person attributes mapper
 */
class PersonAttributesMapper implements AttributesMapper<Person> {
    @Override
    public Person mapFromAttributes(Attributes attrs) throws NamingException {
        return new Person(
            getAttributeValue(attrs, "cn"),
            getAttributeValue(attrs, "sn"),
            getAttributeValue(attrs, "givenName"),
            getAttributeValue(attrs, "mail"),
            getAttributeValue(attrs, "telephoneNumber"),
            getAttributeValue(attrs, "ou"),
            attrs.toString()
        );
    }
    
    private String getAttributeValue(Attributes attrs, String attrName) throws NamingException {
        var attr = attrs.get(attrName);
        return attr != null ? (String) attr.get() : null;
    }
}

/**
 * Person domain object
 */
record Person(String commonName, String surname, String givenName, 
             String mail, String telephoneNumber, String organizationalUnit, 
             String dn) {}

/**
 * REST controller for LDAP template endpoints
 */
@RestController
@RequestMapping("/api/ldap-template")
class LDAPTemplateController {

    private final LDAPTemplateService ldapService;

    public LDAPTemplateController(LDAPTemplateService ldapService) {
        this.ldapService = ldapService;
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Person>> searchByName(@RequestParam String name) {
        List<Person> results = ldapService.searchByName(name);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/search/email")
    public ResponseEntity<List<Person>> searchByEmail(@RequestParam String email) {
        List<Person> results = ldapService.searchByEmail(email);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/search/criteria")
    public ResponseEntity<List<Person>> searchByMultipleCriteria(
            @RequestParam String surname,
            @RequestParam String ou) {
        List<Person> results = ldapService.searchByMultipleCriteria(surname, ou);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/ou/{ouName}")
    public ResponseEntity<List<Person>> findAllInOU(@PathVariable String ouName) {
        List<Person> results = ldapService.findAllInOU(ouName);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/lookup")
    public ResponseEntity<Person> findByDn(@RequestParam String dn) {
        Person person = ldapService.findByDn(dn);
        return person != null ? 
            ResponseEntity.ok(person) : 
            ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public ResponseEntity<CreateResponse> createPerson(@RequestBody Person person) {
        boolean created = ldapService.createPerson(person);
        return ResponseEntity.ok(new CreateResponse(created, 
            created ? "Person created successfully" : "Failed to create person"));
    }

    @PutMapping("/update")
    public ResponseEntity<UpdateResponse> updatePerson(
            @RequestParam String dn,
            @RequestBody Map<String, String> updates) {
        boolean updated = ldapService.updatePerson(dn, updates);
        return ResponseEntity.ok(new UpdateResponse(updated, 
            updated ? "Person updated successfully" : "Failed to update person"));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<DeleteResponse> deletePerson(@RequestParam String dn) {
        boolean deleted = ldapService.deletePerson(dn);
        return ResponseEntity.ok(new DeleteResponse(deleted, 
            deleted ? "Person deleted successfully" : "Failed to delete person"));
    }

    @GetMapping("/exists")
    public ResponseEntity<ExistsResponse> exists(@RequestParam String dn) {
        boolean exists = ldapService.exists(dn);
        return ResponseEntity.ok(new ExistsResponse(dn, exists));
    }

    @GetMapping("/count/{ouName}")
    public ResponseEntity<CountResponse> countEntriesInOU(@PathVariable String ouName) {
        int count = ldapService.countEntriesInOU(ouName);
        return ResponseEntity.ok(new CountResponse(ouName, count));
    }

    @GetMapping("/search/custom")
    public ResponseEntity<List<Person>> searchWithCustomFilter(@RequestParam String filter) {
        List<Person> results = ldapService.searchWithCustomFilter(filter);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/attributes")
    public ResponseEntity<List<String>> getAttributeNames(@RequestParam String dn) {
        List<String> attributes = ldapService.getAttributeNames(dn);
        return ResponseEntity.ok(attributes);
    }

    @GetMapping("/statistics")
    public ResponseEntity<LDAPTemplateService.LDAPStatistics> getStatistics() {
        LDAPTemplateService.LDAPStatistics stats = ldapService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getPatternInfo() {
        PatternInfo info = new PatternInfo(
            "LDAP Template Pattern",
            "Simplified LDAP operations using Spring LdapTemplate",
            "1.0",
            List.of(
                "Simplified LDAP search operations",
                "Automatic resource management",
                "Exception translation",
                "Multiple search scopes support",
                "Attribute mapping and extraction",
                "CRUD operations on directory entries"
            ),
            List.of(
                "User directory lookups",
                "Authentication against LDAP",
                "Group membership queries",
                "Organizational unit traversal",
                "Directory synchronization"
            )
        );
        return ResponseEntity.ok(info);
    }

    record CreateResponse(boolean success, String message) {}
    record UpdateResponse(boolean success, String message) {}
    record DeleteResponse(boolean success, String message) {}
    record ExistsResponse(String dn, boolean exists) {}
    record CountResponse(String organizationalUnit, int count) {}
    record PatternInfo(String name, String description, String version, 
                      List<String> features, List<String> useCases) {}
}
