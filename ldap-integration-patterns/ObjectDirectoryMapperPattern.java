package com.example.ldap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.odm.annotations.*;
import org.springframework.ldap.odm.core.OdmManager;
import org.springframework.ldap.odm.core.impl.OdmManagerImpl;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.web.bind.annotation.*;

import javax.naming.Name;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Object Directory Mapper (ODM) Pattern
 * 
 * Demonstrates object-to-LDAP mapping using annotations.
 * 
 * ODM Features:
 * - Annotation-based mapping (@Entry, @Id, @Attribute)
 * - Automatic conversion between objects and LDAP entries
 * - CRUD operations on mapped objects
 * - Relationship mapping
 * - Custom converters
 * - Multi-valued attribute support
 * 
 * Annotations:
 * - @Entry: Marks class as LDAP entry, specifies object classes
 * - @Id: Maps to DN (Distinguished Name)
 * - @Attribute: Maps field to LDAP attribute
 * - @Transient: Excludes field from mapping
 * - @DnAttribute: Maps field to DN component
 * 
 * Use Cases:
 * - Type-safe LDAP operations
 * - Object-oriented LDAP programming
 * - Simplified CRUD operations
 * - Domain model persistence to LDAP
 * - Bidirectional object-LDAP mapping
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class ObjectDirectoryMapperPattern {

    @Bean
    public OdmManager odmManager(LdapTemplate ldapTemplate) {
        OdmManagerImpl odmManager = new OdmManagerImpl();
        odmManager.setLdapOperations(ldapTemplate);
        return odmManager;
    }

    @Bean
    public ODMService odmService(OdmManager odmManager, LdapTemplate ldapTemplate) {
        return new ODMService(odmManager, ldapTemplate);
    }
}

@Entry(base = "ou=people", objectClasses = {"inetOrgPerson", "organizationalPerson", "person", "top"})
class LdapPerson {
    
    @Id
    private Name dn;
    
    @Attribute(name = "uid")
    @DnAttribute(value = "uid", index = 3)
    private String userId;
    
    @Attribute(name = "cn")
    private String commonName;
    
    @Attribute(name = "sn")
    private String surname;
    
    @Attribute(name = "givenName")
    private String givenName;
    
    @Attribute(name = "mail")
    private String email;
    
    @Attribute(name = "telephoneNumber")
    private String phoneNumber;
    
    @Attribute(name = "title")
    private String title;
    
    @Attribute(name = "departmentNumber")
    private String department;
    
    @Attribute(name = "employeeNumber")
    private String employeeNumber;
    
    @Transient
    private boolean loaded = false;

    // Constructors
    public LdapPerson() {}

    public LdapPerson(String userId, String commonName, String surname, String email) {
        this.userId = userId;
        this.commonName = commonName;
        this.surname = surname;
        this.email = email;
        this.dn = buildDn();
    }

    private Name buildDn() {
        return LdapNameBuilder.newInstance()
            .add("dc", "com")
            .add("dc", "example")
            .add("ou", "people")
            .add("uid", userId)
            .build();
    }

    // Getters and setters
    public Name getDn() { return dn; }
    public void setDn(Name dn) { this.dn = dn; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { 
        this.userId = userId;
        this.dn = buildDn();
    }
    
    public String getCommonName() { return commonName; }
    public void setCommonName(String commonName) { this.commonName = commonName; }
    
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    
    public String getGivenName() { return givenName; }
    public void setGivenName(String givenName) { this.givenName = givenName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
    
    public boolean isLoaded() { return loaded; }
    public void setLoaded(boolean loaded) { this.loaded = loaded; }
}

@Entry(base = "ou=groups", objectClasses = {"groupOfNames", "top"})
class LdapGroup {
    
    @Id
    private Name dn;
    
    @Attribute(name = "cn")
    @DnAttribute(value = "cn", index = 3)
    private String groupName;
    
    @Attribute(name = "description")
    private String description;
    
    @Attribute(name = "member")
    private Set<Name> members = new HashSet<>();

    public LdapGroup() {}

    public LdapGroup(String groupName, String description) {
        this.groupName = groupName;
        this.description = description;
        this.dn = buildDn();
    }

    private Name buildDn() {
        return LdapNameBuilder.newInstance()
            .add("dc", "com")
            .add("dc", "example")
            .add("ou", "groups")
            .add("cn", groupName)
            .build();
    }

    // Getters and setters
    public Name getDn() { return dn; }
    public void setDn(Name dn) { this.dn = dn; }
    
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { 
        this.groupName = groupName;
        this.dn = buildDn();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Set<Name> getMembers() { return members; }
    public void setMembers(Set<Name> members) { this.members = members; }
    
    public void addMember(Name member) { this.members.add(member); }
    public void removeMember(Name member) { this.members.remove(member); }
}

@RestController
@RequestMapping("/api/odm")
class ODMService {

    private final OdmManager odmManager;
    private final LdapTemplate ldapTemplate;

    public ODMService(OdmManager odmManager, LdapTemplate ldapTemplate) {
        this.odmManager = odmManager;
        this.ldapTemplate = ldapTemplate;
    }

    public void createPerson(LdapPerson person) {
        odmManager.create(person);
    }

    public LdapPerson findPerson(String userId) {
        Name dn = LdapNameBuilder.newInstance()
            .add("dc", "com")
            .add("dc", "example")
            .add("ou", "people")
            .add("uid", userId)
            .build();
        
        return odmManager.read(LdapPerson.class, dn);
    }

    public void updatePerson(LdapPerson person) {
        odmManager.update(person);
    }

    public void deletePerson(String userId) {
        Name dn = LdapNameBuilder.newInstance()
            .add("dc", "com")
            .add("dc", "example")
            .add("ou", "people")
            .add("uid", userId)
            .build();
        
        odmManager.delete(odmManager.read(LdapPerson.class, dn));
    }

    public List<LdapPerson> findAllPeople() {
        return odmManager.findAll(LdapPerson.class, 
            LdapNameBuilder.newInstance().add("dc", "com").add("dc", "example").add("ou", "people").build(), null);
    }

    public void createGroup(LdapGroup group) {
        odmManager.create(group);
    }

    public LdapGroup findGroup(String groupName) {
        Name dn = LdapNameBuilder.newInstance()
            .add("dc", "com")
            .add("dc", "example")
            .add("ou", "groups")
            .add("cn", groupName)
            .build();
        
        return odmManager.read(LdapGroup.class, dn);
    }

    public void addMemberToGroup(String groupName, String userId) {
        LdapGroup group = findGroup(groupName);
        Name memberDn = LdapNameBuilder.newInstance()
            .add("dc", "com")
            .add("dc", "example")
            .add("ou", "people")
            .add("uid", userId)
            .build();
        
        group.addMember(memberDn);
        odmManager.update(group);
    }

    public ODMStatistics getStatistics() {
        int totalPeople = findAllPeople().size();
        
        return new ODMStatistics(
            totalPeople,
            0,
            List.of(LdapPerson.class.getSimpleName(), LdapGroup.class.getSimpleName())
        );
    }

    record ODMStatistics(int totalPeople, int totalGroups, List<String> mappedClasses) {}
}

@RestController
@RequestMapping("/api/odm")
class ODMController {

    private final ODMService odmService;

    public ODMController(ODMService odmService) {
        this.odmService = odmService;
    }

    @PostMapping("/person")
    public ResponseEntity<String> createPerson(@RequestBody PersonRequest request) {
        LdapPerson person = new LdapPerson(request.userId(), request.commonName(), 
                                          request.surname(), request.email());
        person.setGivenName(request.givenName());
        person.setPhoneNumber(request.phoneNumber());
        person.setTitle(request.title());
        person.setDepartment(request.department());
        
        odmService.createPerson(person);
        return ResponseEntity.ok("Person created: " + request.userId());
    }

    @GetMapping("/person/{userId}")
    public ResponseEntity<LdapPerson> findPerson(@PathVariable String userId) {
        return ResponseEntity.ok(odmService.findPerson(userId));
    }

    @PutMapping("/person/{userId}")
    public ResponseEntity<String> updatePerson(@PathVariable String userId, 
                                               @RequestBody PersonRequest request) {
        LdapPerson person = odmService.findPerson(userId);
        person.setCommonName(request.commonName());
        person.setSurname(request.surname());
        person.setEmail(request.email());
        person.setGivenName(request.givenName());
        person.setPhoneNumber(request.phoneNumber());
        person.setTitle(request.title());
        person.setDepartment(request.department());
        
        odmService.updatePerson(person);
        return ResponseEntity.ok("Person updated: " + userId);
    }

    @DeleteMapping("/person/{userId}")
    public ResponseEntity<String> deletePerson(@PathVariable String userId) {
        odmService.deletePerson(userId);
        return ResponseEntity.ok("Person deleted: " + userId);
    }

    @GetMapping("/people")
    public ResponseEntity<List<LdapPerson>> findAllPeople() {
        return ResponseEntity.ok(odmService.findAllPeople());
    }

    @PostMapping("/group")
    public ResponseEntity<String> createGroup(@RequestBody GroupRequest request) {
        LdapGroup group = new LdapGroup(request.groupName(), request.description());
        odmService.createGroup(group);
        return ResponseEntity.ok("Group created: " + request.groupName());
    }

    @PostMapping("/group/{groupName}/member/{userId}")
    public ResponseEntity<String> addMemberToGroup(@PathVariable String groupName, 
                                                   @PathVariable String userId) {
        odmService.addMemberToGroup(groupName, userId);
        return ResponseEntity.ok("Added " + userId + " to " + groupName);
    }

    @GetMapping("/statistics")
    public ResponseEntity<ODMService.ODMStatistics> getStatistics() {
        return ResponseEntity.ok(odmService.getStatistics());
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getPatternInfo() {
        return ResponseEntity.ok(new PatternInfo(
            "Object Directory Mapper Pattern",
            "Object-to-LDAP mapping using annotations",
            "1.0",
            List.of("Annotation-based mapping", "Type-safe operations", "CRUD support", "Relationship mapping"),
            List.of("Domain model persistence", "Type-safe LDAP", "Simplified CRUD", "Object-oriented LDAP")
        ));
    }

    record PersonRequest(String userId, String commonName, String surname, String email,
                        String givenName, String phoneNumber, String title, String department) {}
    record GroupRequest(String groupName, String description) {}
    record PatternInfo(String name, String description, String version, 
                      List<String> features, List<String> useCases) {}
}
