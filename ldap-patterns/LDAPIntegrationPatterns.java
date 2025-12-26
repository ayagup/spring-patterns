package com.example.ldap;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapName;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LDAP Integration Patterns - Complete Collection
 * 
 * Covers all LDAP integration patterns:
 * 1. LDAP Template Pattern
 * 2. LDAP Context Source Pattern
 * 3. LDAP User Details Pattern
 * 4. LDAP Authentication Pattern
 * 5. LDAP Search Pattern
 * 6. Distinguished Name Pattern
 * 7. LDAP Query Pattern
 * 8. Object Directory Mapper Pattern
 * 
 * @author Spring Patterns
 */

@Data
class LdapUser {
    private String dn;
    private String uid;
    private String cn;
    private String sn;
    private String mail;
    private String telephoneNumber;
    private List<String> objectClasses;
}

@Data
class LdapSearchCriteria {
    private String base;
    private String filter;
    private String[] attributes;
    private int searchScope;
}

/**
 * 1. LDAP Template Pattern
 * Core template for LDAP operations
 */
@Service
@Slf4j
class LdapTemplateService {
    
    private final LdapTemplate ldapTemplate;
    
    public LdapTemplateService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }
    
    public List<String> getAllPersonNames() {
        return ldapTemplate.search(
            LdapQueryBuilder.query().where("objectClass").is("person"),
            (AttributesMapper<String>) attrs -> (String) attrs.get("cn").get()
        );
    }
    
    public void createUser(LdapUser user) {
        Name dn = buildDn(user.getUid());
        Attributes attrs = buildAttributes(user);
        ldapTemplate.bind(dn, null, attrs);
        log.info("Created user: {}", dn);
    }
    
    public void updateUser(LdapUser user) {
        Name dn = buildDn(user.getUid());
        ldapTemplate.rebind(dn, null, buildAttributes(user));
        log.info("Updated user: {}", dn);
    }
    
    public void deleteUser(String uid) {
        Name dn = buildDn(uid);
        ldapTemplate.unbind(dn);
        log.info("Deleted user: {}", dn);
    }
    
    public boolean authenticate(String uid, String password) {
        Name dn = buildDn(uid);
        return ldapTemplate.authenticate(dn, "", password);
    }
    
    private Name buildDn(String uid) {
        try {
            return new LdapName("uid=" + uid + ",ou=people,dc=example,dc=com");
        } catch (Exception e) {
            throw new RuntimeException("Invalid DN", e);
        }
    }
    
    private Attributes buildAttributes(LdapUser user) {
        Attributes attrs = new BasicAttributes();
        attrs.put(new BasicAttribute("objectClass", "inetOrgPerson"));
        attrs.put(new BasicAttribute("uid", user.getUid()));
        attrs.put(new BasicAttribute("cn", user.getCn()));
        attrs.put(new BasicAttribute("sn", user.getSn()));
        if (user.getMail() != null) {
            attrs.put(new BasicAttribute("mail", user.getMail()));
        }
        return attrs;
    }
    
    public String getTemplateInfo() {
        return """
                LDAP Template Pattern
                ====================
                
                Purpose:
                - Simplify LDAP operations
                - Handle connection management
                - Provide high-level API
                
                Operations:
                - search() - Search directory
                - lookup() - Retrieve entry
                - bind() - Create entry
                - rebind() - Update entry
                - unbind() - Delete entry
                - authenticate() - Verify credentials
                
                Configuration:
                @Bean
                public LdapTemplate ldapTemplate() {
                    return new LdapTemplate(contextSource());
                }
                
                Usage:
                ldapTemplate.search(
                    query().where("uid").is("john"),
                    new PersonAttributesMapper()
                );
                
                Benefits:
                - Exception translation
                - Resource cleanup
                - Simplified API
                - Consistent error handling
                """;
    }
}

/**
 * 2. LDAP Context Source Pattern
 * Configure LDAP connection
 */
@Service
@Slf4j
class LdapContextSourceService {
    
    public String getContextSourceInfo() {
        return """
                LDAP Context Source Pattern
                ==========================
                
                Purpose:
                - Configure LDAP connection
                - Manage connection pooling
                - Handle authentication
                
                Configuration:
                @Bean
                public LdapContextSource contextSource() {
                    LdapContextSource contextSource = new LdapContextSource();
                    contextSource.setUrl("ldap://localhost:389");
                    contextSource.setBase("dc=example,dc=com");
                    contextSource.setUserDn("cn=admin,dc=example,dc=com");
                    contextSource.setPassword("secret");
                    
                    // Pooling configuration
                    contextSource.setPooled(true);
                    contextSource.setPoolMinSize(1);
                    contextSource.setPoolMaxSize(10);
                    
                    // Authentication
                    contextSource.setAnonymousReadOnly(false);
                    contextSource.setAuthenticationStrategy(
                        new DigestMd5DirContextAuthenticationStrategy()
                    );
                    
                    return contextSource;
                }
                
                Properties:
                - url: LDAP server URL
                - base: Base DN for all operations
                - userDn: Admin DN for binding
                - password: Admin password
                - pooled: Enable connection pooling
                - referral: Handle referrals (ignore/follow)
                
                Security:
                - Use LDAPS (ldaps://) for encryption
                - Configure certificate trust store
                - Use strong authentication mechanisms
                """;
    }
}

/**
 * 3. LDAP User Details Pattern
 * Integration with Spring Security
 */
@Service
@Slf4j
class LdapUserDetailsService {
    
    private final LdapTemplate ldapTemplate;
    
    public LdapUserDetailsService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }
    
    public UserDetails loadUserByUsername(String username) {
        List<UserDetails> users = ldapTemplate.search(
            LdapQueryBuilder.query()
                .where("objectClass").is("person")
                .and("uid").is(username),
            (AttributesMapper<UserDetails>) attrs -> 
                LdapUserDetailsImpl.Essence()
                    .setUsername((String) attrs.get("uid").get())
                    .setDn("uid=" + username + ",ou=people,dc=example,dc=com")
                    .build()
        );
        
        if (users.isEmpty()) {
            throw new RuntimeException("User not found: " + username);
        }
        
        return users.get(0);
    }
    
    public String getUserDetailsInfo() {
        return """
                LDAP User Details Pattern
                ========================
                
                Purpose:
                - Load user from LDAP for authentication
                - Map LDAP attributes to UserDetails
                - Integrate with Spring Security
                
                Configuration:
                @Bean
                public LdapUserDetailsService userDetailsService() {
                    return new LdapUserDetailsService(ldapTemplate);
                }
                
                Security Configuration:
                @Bean
                public SecurityFilterChain filterChain(HttpSecurity http) {
                    http
                        .authorizeRequests()
                            .anyRequest().authenticated()
                        .and()
                        .formLogin()
                        .and()
                        .ldapAuthentication()
                            .userDnPatterns("uid={0},ou=people")
                            .contextSource()
                                .url("ldap://localhost:389/dc=example,dc=com");
                    return http.build();
                }
                
                Mapping:
                - DN → Principal
                - cn → Display name
                - mail → Email
                - Groups → Authorities
                """;
    }
}

/**
 * 4. LDAP Authentication Pattern
 * Authenticate users against LDAP
 */
@Service
@Slf4j
class LdapAuthenticationService {
    
    private final LdapTemplate ldapTemplate;
    
    public LdapAuthenticationService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }
    
    public boolean authenticate(String username, String password) {
        try {
            String dn = "uid=" + username + ",ou=people,dc=example,dc=com";
            return ldapTemplate.authenticate(dn, "", password);
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", username, e);
            return false;
        }
    }
    
    public String getAuthenticationInfo() {
        return """
                LDAP Authentication Pattern
                ==========================
                
                Purpose:
                - Verify user credentials against LDAP
                - Bind as user to validate password
                - Handle authentication errors
                
                Methods:
                1. Bind Authentication
                   - Attempt to bind with user credentials
                   - Most common and secure
                   
                2. Compare Authentication
                   - Compare password attribute
                   - Less secure (password readable)
                
                3. Password Policy
                   - Check password expiration
                   - Enforce password complexity
                   - Handle account lockout
                
                Configuration:
                @Bean
                public AuthenticationProvider ldapAuthenticationProvider() {
                    LdapAuthenticationProvider provider = 
                        new LdapAuthenticationProvider(authenticator());
                    provider.setUserDetailsContextMapper(userDetailsMapper());
                    return provider;
                }
                
                @Bean
                public LdapAuthenticator authenticator() {
                    BindAuthenticator authenticator = 
                        new BindAuthenticator(contextSource());
                    authenticator.setUserDnPatterns(
                        new String[] {"uid={0},ou=people"}
                    );
                    return authenticator;
                }
                
                Security:
                - Always use LDAPS for password transmission
                - Implement rate limiting
                - Log authentication attempts
                - Handle account lockout
                """;
    }
}

/**
 * 5. LDAP Search Pattern
 * Search directory entries
 */
@Service
@Slf4j
class LdapSearchService {
    
    private final LdapTemplate ldapTemplate;
    
    public LdapSearchService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }
    
    public List<LdapUser> searchUsers(String filter) {
        return ldapTemplate.search(
            LdapQueryBuilder.query()
                .where("objectClass").is("person")
                .and(filter),
            (AttributesMapper<LdapUser>) attrs -> {
                LdapUser user = new LdapUser();
                user.setUid(getAttributeValue(attrs, "uid"));
                user.setCn(getAttributeValue(attrs, "cn"));
                user.setSn(getAttributeValue(attrs, "sn"));
                user.setMail(getAttributeValue(attrs, "mail"));
                return user;
            }
        );
    }
    
    public List<LdapUser> searchByEmail(String email) {
        return ldapTemplate.search(
            LdapQueryBuilder.query()
                .base("ou=people")
                .where("mail").is(email),
            new UserAttributesMapper()
        );
    }
    
    public List<String> searchGroups(String userId) {
        return ldapTemplate.search(
            LdapQueryBuilder.query()
                .base("ou=groups")
                .where("member").is("uid=" + userId + ",ou=people,dc=example,dc=com"),
            (AttributesMapper<String>) attrs -> (String) attrs.get("cn").get()
        );
    }
    
    private String getAttributeValue(Attributes attrs, String name) {
        try {
            return attrs.get(name) != null ? (String) attrs.get(name).get() : null;
        } catch (NamingException e) {
            return null;
        }
    }
    
    public String getSearchInfo() {
        return """
                LDAP Search Pattern
                ==================
                
                Purpose:
                - Query directory entries
                - Filter results
                - Map attributes
                
                Search Scopes:
                - OBJECT_SCOPE: Base object only
                - ONELEVEL_SCOPE: Direct children
                - SUBTREE_SCOPE: Entire subtree
                
                Filters:
                - (uid=john) - Equality
                - (cn=John*) - Wildcard
                - (|(uid=john)(uid=jane)) - OR
                - (&(cn=John)(mail=*@example.com)) - AND
                - (!(cn=John)) - NOT
                
                Query Builder:
                LdapQueryBuilder.query()
                    .base("ou=people")
                    .searchScope(SearchScope.ONELEVEL)
                    .timeLimit(3000)
                    .countLimit(100)
                    .where("objectClass").is("person")
                    .and("cn").like("John*")
                
                Performance:
                - Use specific base DN
                - Limit search scope
                - Add count/time limits
                - Create LDAP indexes
                """;
    }
    
    private static class UserAttributesMapper implements AttributesMapper<LdapUser> {
        @Override
        public LdapUser mapFromAttributes(Attributes attrs) throws NamingException {
            LdapUser user = new LdapUser();
            user.setUid(getAttr(attrs, "uid"));
            user.setCn(getAttr(attrs, "cn"));
            user.setSn(getAttr(attrs, "sn"));
            user.setMail(getAttr(attrs, "mail"));
            return user;
        }
        
        private String getAttr(Attributes attrs, String name) throws NamingException {
            return attrs.get(name) != null ? (String) attrs.get(name).get() : null;
        }
    }
}

/**
 * 6. Distinguished Name Pattern
 * Handle LDAP distinguished names
 */
@Service
@Slf4j
class DistinguishedNameService {
    
    public String getDnInfo() {
        return """
                Distinguished Name Pattern
                =========================
                
                Purpose:
                - Uniquely identify LDAP entries
                - Build DN programmatically
                - Parse DN components
                
                DN Structure:
                uid=john,ou=people,dc=example,dc=com
                
                Components:
                - uid=john: Relative DN (RDN)
                - ou=people: Organizational Unit
                - dc=example,dc=com: Domain Components
                
                Building DN:
                LdapName dn = LdapNameBuilder
                    .newInstance()
                    .add("dc", "com")
                    .add("dc", "example")
                    .add("ou", "people")
                    .add("uid", "john")
                    .build();
                
                Parsing DN:
                LdapName name = new LdapName("uid=john,ou=people,dc=example,dc=com");
                String uid = (String) name.getRdn(name.size() - 1).getValue();
                
                Operations:
                - add() - Add component
                - addAll() - Add multiple
                - remove() - Remove component
                - getSuffix() - Get parent DN
                - startsWith() - Check hierarchy
                
                Best Practices:
                - Use LdapNameBuilder for construction
                - Escape special characters
                - Validate DN format
                - Handle case sensitivity
                """;
    }
}

/**
 * 7. LDAP Query Pattern
 * Build complex LDAP queries
 */
@Service
@Slf4j
class LdapQueryService {
    
    private final LdapTemplate ldapTemplate;
    
    public LdapQueryService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }
    
    public List<LdapUser> complexQuery() {
        return ldapTemplate.search(
            LdapQueryBuilder.query()
                .base("ou=people")
                .searchScope(2) // SUBTREE_SCOPE
                .timeLimit(5000)
                .countLimit(100)
                .where("objectClass").is("person")
                .and("cn").like("John*")
                .and("mail").isPresent(),
            new UserMapper()
        );
    }
    
    public String getQueryInfo() {
        return """
                LDAP Query Pattern
                ==================
                
                Purpose:
                - Build type-safe queries
                - Fluent query API
                - Complex filter construction
                
                Query Builder API:
                LdapQueryBuilder.query()
                    .base("ou=people")
                    .searchScope(SearchScope.SUBTREE)
                    .timeLimit(3000)
                    .countLimit(100)
                    .where("objectClass").is("person")
                    .and("cn").like("John*")
                    .and("mail").isPresent()
                    .and(query()
                        .where("department").is("IT")
                        .or("department").is("Engineering")
                    );
                
                Filter Operators:
                - is(value) - Equality
                - like(pattern) - Wildcard
                - gte(value) - Greater or equal
                - lte(value) - Less or equal
                - isPresent() - Attribute exists
                - not() - Negation
                - and() - Logical AND
                - or() - Logical OR
                
                Advanced:
                - Nested queries
                - Dynamic filters
                - Parameter binding
                - Result pagination
                """;
    }
    
    private static class UserMapper implements AttributesMapper<LdapUser> {
        @Override
        public LdapUser mapFromAttributes(Attributes attrs) throws NamingException {
            LdapUser user = new LdapUser();
            if (attrs.get("uid") != null) user.setUid((String) attrs.get("uid").get());
            if (attrs.get("cn") != null) user.setCn((String) attrs.get("cn").get());
            if (attrs.get("sn") != null) user.setSn((String) attrs.get("sn").get());
            if (attrs.get("mail") != null) user.setMail((String) attrs.get("mail").get());
            return user;
        }
    }
}

/**
 * 8. Object Directory Mapper (ODM) Pattern
 * Map Java objects to LDAP entries
 */
@Service
@Slf4j
class ObjectDirectoryMapperService {
    
    public String getOdmInfo() {
        return """
                Object Directory Mapper Pattern
                ==============================
                
                Purpose:
                - Map Java objects to LDAP entries
                - Automatic attribute mapping
                - Annotation-based configuration
                
                Entity Definition:
                @Entry(base = "ou=people", objectClasses = {"person", "inetOrgPerson"})
                public class Person {
                    @Id
                    private Name dn;
                    
                    @Attribute(name = "cn")
                    private String commonName;
                    
                    @Attribute(name = "sn")
                    private String surname;
                    
                    @Attribute(name = "mail")
                    private String email;
                    
                    @DnAttribute(value = "uid")
                    private String userId;
                }
                
                Repository:
                public interface PersonRepository extends LdapRepository<Person> {
                    List<Person> findByCommonName(String cn);
                    List<Person> findByEmail(String email);
                    List<Person> findBySurnameLike(String pattern);
                }
                
                Usage:
                // Create
                Person person = new Person();
                person.setUserId("john");
                person.setCommonName("John Doe");
                personRepository.save(person);
                
                // Query
                List<Person> people = personRepository.findByCommonName("John Doe");
                
                // Update
                person.setEmail("john@example.com");
                personRepository.save(person);
                
                // Delete
                personRepository.delete(person);
                
                Annotations:
                - @Entry: Mark LDAP entity
                - @Id: Distinguished name
                - @Attribute: Map attribute
                - @DnAttribute: RDN component
                - @Transient: Ignore field
                
                Benefits:
                - Type-safe operations
                - Reduced boilerplate
                - Spring Data integration
                - Query method generation
                """;
    }
}

/**
 * REST Controller for all LDAP patterns
 */
@RestController
@RequestMapping("/ldap/patterns")
@Slf4j
class LdapPatternsController {
    
    private final LdapTemplateService templateService;
    private final LdapContextSourceService contextSourceService;
    private final LdapUserDetailsService userDetailsService;
    private final LdapAuthenticationService authenticationService;
    private final LdapSearchService searchService;
    private final DistinguishedNameService dnService;
    private final LdapQueryService queryService;
    private final ObjectDirectoryMapperService odmService;
    
    public LdapPatternsController(
            LdapTemplateService templateService,
            LdapContextSourceService contextSourceService,
            LdapUserDetailsService userDetailsService,
            LdapAuthenticationService authenticationService,
            LdapSearchService searchService,
            DistinguishedNameService dnService,
            LdapQueryService queryService,
            ObjectDirectoryMapperService odmService) {
        this.templateService = templateService;
        this.contextSourceService = contextSourceService;
        this.userDetailsService = userDetailsService;
        this.authenticationService = authenticationService;
        this.searchService = searchService;
        this.dnService = dnService;
        this.queryService = queryService;
        this.odmService = odmService;
    }
    
    @GetMapping("/template")
    public String getTemplateInfo() {
        return templateService.getTemplateInfo();
    }
    
    @GetMapping("/context-source")
    public String getContextSourceInfo() {
        return contextSourceService.getContextSourceInfo();
    }
    
    @GetMapping("/user-details")
    public String getUserDetailsInfo() {
        return userDetailsService.getUserDetailsInfo();
    }
    
    @GetMapping("/authentication")
    public String getAuthenticationInfo() {
        return authenticationService.getAuthenticationInfo();
    }
    
    @GetMapping("/search")
    public String getSearchInfo() {
        return searchService.getSearchInfo();
    }
    
    @GetMapping("/distinguished-name")
    public String getDnInfo() {
        return dnService.getDnInfo();
    }
    
    @GetMapping("/query")
    public String getQueryInfo() {
        return queryService.getQueryInfo();
    }
    
    @GetMapping("/odm")
    public String getOdmInfo() {
        return odmService.getOdmInfo();
    }
    
    @PostMapping("/test/search")
    public List<LdapUser> testSearch(@RequestParam String filter) {
        return searchService.searchUsers(filter);
    }
    
    @PostMapping("/test/authenticate")
    public boolean testAuthenticate(@RequestParam String username, @RequestParam String password) {
        return authenticationService.authenticate(username, password);
    }
}

@SpringBootApplication
public class LDAPIntegrationPatterns {
    public static void main(String[] args) {
        SpringApplication.run(LDAPIntegrationPatterns.class, args);
    }
}
