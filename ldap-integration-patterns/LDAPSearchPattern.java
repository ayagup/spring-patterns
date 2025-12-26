package com.example.ldap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.*;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.SearchScope;
import org.springframework.web.bind.annotation.*;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.*;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * LDAP Search Pattern
 * 
 * Demonstrates advanced LDAP search operations with various filters and scopes.
 * 
 * Search Capabilities:
 * - Basic search by attribute
 * - Complex filters (AND, OR, NOT)
 * - Wildcard searches
 * - Search scope control (object, onelevel, subtree)
 * - Attribute selection
 * - Size and time limits
 * 
 * Filter Types:
 * - Equals filter: (attribute=value)
 * - Like filter: (attribute=value*)
 * - Present filter: (attribute=*)
 * - And/Or filters: (&(filter1)(filter2))
 * - Not filter: (!(filter))
 * 
 * Use Cases:
 * - User directory searches
 * - Group membership queries
 * - Organizational unit browsing
 * - Complex directory queries
 * - Directory data mining
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class LDAPSearchPattern {

    @Bean
    public LDAPSearchService ldapSearchService(LdapTemplate ldapTemplate) {
        return new LDAPSearchService(ldapTemplate);
    }
}

@RestController
@RequestMapping("/api/ldap-search")
class LDAPSearchService {

    private final LdapTemplate ldapTemplate;

    public LDAPSearchService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public List<SearchResult> searchByAttribute(String attribute, String value) {
        LdapQuery query = query()
            .base("ou=people")
            .where(attribute).is(value);
        
        return ldapTemplate.search(query, new SearchResultMapper());
    }

    public List<SearchResult> searchByWildcard(String attribute, String pattern) {
        LdapQuery query = query()
            .base("ou=people")
            .where(attribute).like(pattern);
        
        return ldapTemplate.search(query, new SearchResultMapper());
    }

    public List<SearchResult> searchWithComplexFilter(Map<String, String> criteria) {
        AndFilter filter = new AndFilter();
        criteria.forEach((key, value) -> filter.and(new EqualsFilter(key, value)));
        
        return ldapTemplate.search("ou=people", filter.encode(), new SearchResultMapper());
    }

    public List<SearchResult> searchWithOrFilter(List<AttributeFilter> filters) {
        OrFilter orFilter = new OrFilter();
        filters.forEach(f -> orFilter.or(new EqualsFilter(f.attribute(), f.value())));
        
        return ldapTemplate.search("ou=people", orFilter.encode(), new SearchResultMapper());
    }

    public List<SearchResult> searchSubtree(String baseDn, String filter) {
        LdapQuery query = query()
            .base(baseDn)
            .searchScope(SearchScope.SUBTREE)
            .filter(filter);
        
        return ldapTemplate.search(query, new SearchResultMapper());
    }

    public List<SearchResult> searchOneLevel(String baseDn, String filter) {
        LdapQuery query = query()
            .base(baseDn)
            .searchScope(SearchScope.ONELEVEL)
            .filter(filter);
        
        return ldapTemplate.search(query, new SearchResultMapper());
    }

    public int countResults(String baseDn, String filter) {
        LdapQuery query = query()
            .base(baseDn)
            .filter(filter);
        
        return ldapTemplate.search(query, new SearchResultMapper()).size();
    }

    public List<String> getDistinctValues(String attribute) {
        LdapQuery query = query()
            .base("ou=people")
            .where("objectClass").is("person");
        
        return ldapTemplate.search(query, (Attributes attrs) -> {
            try {
                var attr = attrs.get(attribute);
                return attr != null ? (String) attr.get() : null;
            } catch (NamingException e) {
                return null;
            }
        }).stream().filter(Objects::nonNull).distinct().toList();
    }

    record AttributeFilter(String attribute, String value) {}
    record SearchResult(String dn, Map<String, String> attributes) {}
}

class SearchResultMapper implements AttributesMapper<LDAPSearchService.SearchResult> {
    @Override
    public LDAPSearchService.SearchResult mapFromAttributes(Attributes attrs) throws NamingException {
        Map<String, String> attributes = new HashMap<>();
        var enumeration = attrs.getIDs();
        while (enumeration.hasMoreElements()) {
            String attrName = enumeration.nextElement();
            var attr = attrs.get(attrName);
            if (attr != null) {
                attributes.put(attrName, attr.get().toString());
            }
        }
        return new LDAPSearchService.SearchResult(attrs.toString(), attributes);
    }
}

@RestController
@RequestMapping("/api/ldap-search")
class LDAPSearchController {

    private final LDAPSearchService searchService;

    public LDAPSearchController(LDAPSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/by-attribute")
    public ResponseEntity<List<LDAPSearchService.SearchResult>> searchByAttribute(
            @RequestParam String attribute,
            @RequestParam String value) {
        return ResponseEntity.ok(searchService.searchByAttribute(attribute, value));
    }

    @GetMapping("/wildcard")
    public ResponseEntity<List<LDAPSearchService.SearchResult>> searchByWildcard(
            @RequestParam String attribute,
            @RequestParam String pattern) {
        return ResponseEntity.ok(searchService.searchByWildcard(attribute, pattern));
    }

    @PostMapping("/complex")
    public ResponseEntity<List<LDAPSearchService.SearchResult>> searchWithComplexFilter(
            @RequestBody Map<String, String> criteria) {
        return ResponseEntity.ok(searchService.searchWithComplexFilter(criteria));
    }

    @PostMapping("/or-filter")
    public ResponseEntity<List<LDAPSearchService.SearchResult>> searchWithOrFilter(
            @RequestBody List<LDAPSearchService.AttributeFilter> filters) {
        return ResponseEntity.ok(searchService.searchWithOrFilter(filters));
    }

    @GetMapping("/subtree")
    public ResponseEntity<List<LDAPSearchService.SearchResult>> searchSubtree(
            @RequestParam String baseDn,
            @RequestParam String filter) {
        return ResponseEntity.ok(searchService.searchSubtree(baseDn, filter));
    }

    @GetMapping("/onelevel")
    public ResponseEntity<List<LDAPSearchService.SearchResult>> searchOneLevel(
            @RequestParam String baseDn,
            @RequestParam String filter) {
        return ResponseEntity.ok(searchService.searchOneLevel(baseDn, filter));
    }

    @GetMapping("/count")
    public ResponseEntity<CountResponse> countResults(
            @RequestParam String baseDn,
            @RequestParam String filter) {
        int count = searchService.countResults(baseDn, filter);
        return ResponseEntity.ok(new CountResponse(baseDn, filter, count));
    }

    @GetMapping("/distinct/{attribute}")
    public ResponseEntity<List<String>> getDistinctValues(@PathVariable String attribute) {
        return ResponseEntity.ok(searchService.getDistinctValues(attribute));
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getPatternInfo() {
        return ResponseEntity.ok(new PatternInfo(
            "LDAP Search Pattern",
            "Advanced LDAP search with filters and scopes",
            "1.0",
            List.of("Complex filters", "Search scopes", "Wildcard searches", "Attribute selection"),
            List.of("Directory searches", "Group queries", "OU browsing", "Data mining")
        ));
    }

    record CountResponse(String baseDn, String filter, int count) {}
    record PatternInfo(String name, String description, String version, 
                      List<String> features, List<String> useCases) {}
}
