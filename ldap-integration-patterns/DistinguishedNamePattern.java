package com.example.ldap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.web.bind.annotation.*;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.ArrayList;
import java.util.List;

/**
 * Distinguished Name (DN) Pattern
 * 
 * Demonstrates DN parsing, construction, and manipulation.
 * 
 * DN Operations:
 * - Parse DN string to components
 * - Build DN programmatically
 * - Extract RDN (Relative DN)
 * - Get parent DN
 * - Compare DNs
 * - Validate DN syntax
 * - Escape special characters
 * 
 * DN Structure:
 * - cn=John Doe,ou=people,dc=example,dc=com
 * - Components: RDN + Parent DN
 * - RDN: cn=John Doe
 * - Parent: ou=people,dc=example,dc=com
 * 
 * Use Cases:
 * - DN construction for LDAP operations
 * - DN validation before operations
 * - Extracting information from DNs
 * - DN hierarchy navigation
 * - Entry relocation (changing parent DN)
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class DistinguishedNamePattern {

    @Bean
    public DNService dnService() {
        return new DNService();
    }
}

@RestController
@RequestMapping("/api/dn")
class DNService {

    public LdapName parseDN(String dnString) throws InvalidNameException {
        return new LdapName(dnString);
    }

    public String buildDN(String cn, String ou, String dc1, String dc2) {
        return LdapNameBuilder.newInstance()
            .add("dc", dc2)
            .add("dc", dc1)
            .add("ou", ou)
            .add("cn", cn)
            .build()
            .toString();
    }

    public String buildUserDN(String username) {
        return LdapNameBuilder.newInstance()
            .add("dc", "com")
            .add("dc", "example")
            .add("ou", "people")
            .add("uid", username)
            .build()
            .toString();
    }

    public String buildGroupDN(String groupName) {
        return LdapNameBuilder.newInstance()
            .add("dc", "com")
            .add("dc", "example")
            .add("ou", "groups")
            .add("cn", groupName)
            .build()
            .toString();
    }

    public List<RdnComponent> extractComponents(String dnString) throws InvalidNameException {
        LdapName ldapName = new LdapName(dnString);
        List<RdnComponent> components = new ArrayList<>();
        
        for (Rdn rdn : ldapName.getRdns()) {
            components.add(new RdnComponent(rdn.getType(), rdn.getValue().toString()));
        }
        
        return components;
    }

    public String extractRDN(String dnString) throws InvalidNameException {
        LdapName ldapName = new LdapName(dnString);
        if (ldapName.size() == 0) return "";
        return ldapName.getRdn(ldapName.size() - 1).toString();
    }

    public String extractAttribute(String dnString, String attribute) throws InvalidNameException {
        LdapName ldapName = new LdapName(dnString);
        for (Rdn rdn : ldapName.getRdns()) {
            if (rdn.getType().equalsIgnoreCase(attribute)) {
                return rdn.getValue().toString();
            }
        }
        return null;
    }

    public String getParentDN(String dnString) throws InvalidNameException {
        LdapName ldapName = new LdapName(dnString);
        if (ldapName.size() <= 1) return "";
        
        return ldapName.getPrefix(ldapName.size() - 1).toString();
    }

    public boolean isChildOf(String childDN, String parentDN) throws InvalidNameException {
        LdapName child = new LdapName(childDN);
        LdapName parent = new LdapName(parentDN);
        
        return child.startsWith(parent);
    }

    public boolean dnEquals(String dn1, String dn2) throws InvalidNameException {
        return new LdapName(dn1).equals(new LdapName(dn2));
    }

    public boolean isValidDN(String dnString) {
        try {
            new LdapName(dnString);
            return true;
        } catch (InvalidNameException e) {
            return false;
        }
    }

    public int getDepth(String dnString) throws InvalidNameException {
        return new LdapName(dnString).size();
    }

    public String escapeValue(String value) {
        return Rdn.escapeValue(value);
    }

    public DNInfo getDNInfo(String dnString) throws InvalidNameException {
        LdapName ldapName = new LdapName(dnString);
        
        return new DNInfo(
            dnString,
            ldapName.size(),
            ldapName.size() > 0 ? ldapName.getRdn(ldapName.size() - 1).toString() : "",
            ldapName.size() > 1 ? ldapName.getPrefix(ldapName.size() - 1).toString() : "",
            extractComponents(dnString),
            isValidDN(dnString)
        );
    }

    record RdnComponent(String type, String value) {}
    record DNInfo(String fullDN, int depth, String rdn, String parentDN, 
                 List<RdnComponent> components, boolean valid) {}
}

@RestController
@RequestMapping("/api/dn")
class DNController {

    private final DNService dnService;

    public DNController(DNService dnService) {
        this.dnService = dnService;
    }

    @PostMapping("/parse")
    public ResponseEntity<DNService.DNInfo> parseDN(@RequestBody DNRequest request) {
        try {
            return ResponseEntity.ok(dnService.getDNInfo(request.dn()));
        } catch (InvalidNameException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/build")
    public ResponseEntity<String> buildDN(@RequestBody BuildDNRequest request) {
        String dn = dnService.buildDN(request.cn(), request.ou(), request.dc1(), request.dc2());
        return ResponseEntity.ok(dn);
    }

    @GetMapping("/build-user/{username}")
    public ResponseEntity<String> buildUserDN(@PathVariable String username) {
        return ResponseEntity.ok(dnService.buildUserDN(username));
    }

    @GetMapping("/build-group/{groupName}")
    public ResponseEntity<String> buildGroupDN(@PathVariable String groupName) {
        return ResponseEntity.ok(dnService.buildGroupDN(groupName));
    }

    @GetMapping("/extract-rdn")
    public ResponseEntity<String> extractRDN(@RequestParam String dn) {
        try {
            return ResponseEntity.ok(dnService.extractRDN(dn));
        } catch (InvalidNameException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/parent")
    public ResponseEntity<String> getParentDN(@RequestParam String dn) {
        try {
            return ResponseEntity.ok(dnService.getParentDN(dn));
        } catch (InvalidNameException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/is-child")
    public ResponseEntity<Boolean> isChildOf(@RequestBody ChildCheckRequest request) {
        try {
            return ResponseEntity.ok(dnService.isChildOf(request.childDN(), request.parentDN()));
        } catch (InvalidNameException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/equals")
    public ResponseEntity<Boolean> dnEquals(@RequestBody DNCompareRequest request) {
        try {
            return ResponseEntity.ok(dnService.dnEquals(request.dn1(), request.dn2()));
        } catch (InvalidNameException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> isValidDN(@RequestParam String dn) {
        return ResponseEntity.ok(dnService.isValidDN(dn));
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getPatternInfo() {
        return ResponseEntity.ok(new PatternInfo(
            "Distinguished Name Pattern",
            "DN parsing, construction, and manipulation",
            "1.0",
            List.of("DN parsing", "DN building", "RDN extraction", "DN validation", "Hierarchy navigation"),
            List.of("DN construction", "Entry relocation", "Hierarchy navigation", "DN validation")
        ));
    }

    record DNRequest(String dn) {}
    record BuildDNRequest(String cn, String ou, String dc1, String dc2) {}
    record ChildCheckRequest(String childDN, String parentDN) {}
    record DNCompareRequest(String dn1, String dn2) {}
    record PatternInfo(String name, String description, String version, 
                      List<String> features, List<String> useCases) {}
}
