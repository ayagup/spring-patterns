package com.example.ldap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.SearchScope;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * LDAP Query Pattern
 * 
 * Demonstrates fluent query builder API for complex LDAP queries.
 * 
 * Query Builder Features:
 * - Fluent API for query construction
 * - Base DN specification
 * - Search scope configuration
 * - Filter conditions (where, and, or, not)
 * - Attribute selection
 * - Count limits
 * - Time limits
 * 
 * Query Components:
 * - Base: Starting point for search
 * - Filter: Criteria for matching entries
 * - Scope: Depth of search (object/onelevel/subtree)
 * - Attributes: Which attributes to return
 * - Limits: Size and time constraints
 * 
 * Use Cases:
 * - Complex directory queries
 * - Optimized searches with attribute selection
 * - Scoped searches for performance
 * - Dynamic query building
 * - Query reuse and composition
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class LDAPQueryPattern {

    @Bean
    public LDAPQueryService ldapQueryService(LdapTemplate ldapTemplate) {
        return new LDAPQueryService(ldapTemplate);
    }
}

@RestController
@RequestMapping("/api/ldap-query")
class LDAPQueryService {

    private final LdapTemplate ldapTemplate;

    public LDAPQueryService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public List<String> simpleQuery(String attribute, String value) {
        LdapQuery query = query()
            .base("ou=people")
            .where(attribute).is(value);
        
        return ldapTemplate.search(query, ctx -> ctx.getNameInNamespace());
    }

    public List<String> queryWithScope(String baseDn, String attribute, String value, SearchScope scope) {
        LdapQuery query = query()
            .base(baseDn)
            .searchScope(scope)
            .where(attribute).is(value);
        
        return ldapTemplate.search(query, ctx -> ctx.getNameInNamespace());
    }

    public List<String> queryWithAndConditions(String attr1, String val1, String attr2, String val2) {
        LdapQuery query = query()
            .base("ou=people")
            .where(attr1).is(val1)
            .and(attr2).is(val2);
        
        return ldapTemplate.search(query, ctx -> ctx.getNameInNamespace());
    }

    public List<String> queryWithOrConditions(String attr1, String val1, String attr2, String val2) {
        LdapQuery query = query()
            .base("ou=people")
            .where(attr1).is(val1)
            .or(attr2).is(val2);
        
        return ldapTemplate.search(query, ctx -> ctx.getNameInNamespace());
    }

    public List<String> queryWithLike(String attribute, String pattern) {
        LdapQuery query = query()
            .base("ou=people")
            .where(attribute).like(pattern);
        
        return ldapTemplate.search(query, ctx -> ctx.getNameInNamespace());
    }

    public List<String> queryWithPresence(String attribute) {
        LdapQuery query = query()
            .base("ou=people")
            .where(attribute).isPresent();
        
        return ldapTemplate.search(query, ctx -> ctx.getNameInNamespace());
    }

    public List<String> queryWithCountLimit(String attribute, String value, int limit) {
        LdapQuery query = query()
            .base("ou=people")
            .countLimit(limit)
            .where(attribute).is(value);
        
        return ldapTemplate.search(query, ctx -> ctx.getNameInNamespace());
    }

    public List<String> queryWithTimeLimit(String attribute, String value, int milliseconds) {
        LdapQuery query = query()
            .base("ou=people")
            .timeLimit(milliseconds)
            .where(attribute).is(value);
        
        return ldapTemplate.search(query, ctx -> ctx.getNameInNamespace());
    }

    public List<String> complexQuery(QueryRequest request) {
        var queryBuilder = query().base(request.baseDn());
        
        if (request.scope() != null) {
            queryBuilder = queryBuilder.searchScope(request.scope());
        }
        
        if (request.countLimit() != null) {
            queryBuilder = queryBuilder.countLimit(request.countLimit());
        }
        
        if (request.timeLimit() != null) {
            queryBuilder = queryBuilder.timeLimit(request.timeLimit());
        }
        
        if (request.attribute() != null && request.value() != null) {
            queryBuilder = queryBuilder.where(request.attribute()).is(request.value());
        }
        
        return ldapTemplate.search(queryBuilder, ctx -> ctx.getNameInNamespace());
    }

    public int countQuery(String baseDn, String attribute, String value) {
        LdapQuery query = query()
            .base(baseDn)
            .where(attribute).is(value);
        
        return ldapTemplate.search(query, ctx -> ctx.getNameInNamespace()).size();
    }

    public QueryBuilder createQueryBuilder() {
        return new QueryBuilder();
    }

    record QueryRequest(String baseDn, SearchScope scope, Integer countLimit, 
                       Integer timeLimit, String attribute, String value) {}
}

class QueryBuilder {
    private String baseDn = "";
    private SearchScope scope = SearchScope.SUBTREE;
    private Integer countLimit;
    private Integer timeLimit;
    private String attribute;
    private String value;

    public QueryBuilder base(String baseDn) {
        this.baseDn = baseDn;
        return this;
    }

    public QueryBuilder scope(SearchScope scope) {
        this.scope = scope;
        return this;
    }

    public QueryBuilder countLimit(int limit) {
        this.countLimit = limit;
        return this;
    }

    public QueryBuilder timeLimit(int milliseconds) {
        this.timeLimit = milliseconds;
        return this;
    }

    public QueryBuilder where(String attribute, String value) {
        this.attribute = attribute;
        this.value = value;
        return this;
    }

    public LdapQuery build() {
        var queryBuilder = query().base(baseDn).searchScope(scope);
        
        if (countLimit != null) {
            queryBuilder = queryBuilder.countLimit(countLimit);
        }
        
        if (timeLimit != null) {
            queryBuilder = queryBuilder.timeLimit(timeLimit);
        }
        
        if (attribute != null && value != null) {
            queryBuilder = queryBuilder.where(attribute).is(value);
        }
        
        return queryBuilder;
    }

    public LDAPQueryService.QueryRequest buildRequest() {
        return new LDAPQueryService.QueryRequest(baseDn, scope, countLimit, timeLimit, attribute, value);
    }
}

@RestController
@RequestMapping("/api/ldap-query")
class LDAPQueryController {

    private final LDAPQueryService queryService;

    public LDAPQueryController(LDAPQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/simple")
    public ResponseEntity<List<String>> simpleQuery(
            @RequestParam String attribute,
            @RequestParam String value) {
        return ResponseEntity.ok(queryService.simpleQuery(attribute, value));
    }

    @GetMapping("/with-scope")
    public ResponseEntity<List<String>> queryWithScope(
            @RequestParam String baseDn,
            @RequestParam String attribute,
            @RequestParam String value,
            @RequestParam SearchScope scope) {
        return ResponseEntity.ok(queryService.queryWithScope(baseDn, attribute, value, scope));
    }

    @GetMapping("/and")
    public ResponseEntity<List<String>> queryWithAndConditions(
            @RequestParam String attr1, @RequestParam String val1,
            @RequestParam String attr2, @RequestParam String val2) {
        return ResponseEntity.ok(queryService.queryWithAndConditions(attr1, val1, attr2, val2));
    }

    @GetMapping("/or")
    public ResponseEntity<List<String>> queryWithOrConditions(
            @RequestParam String attr1, @RequestParam String val1,
            @RequestParam String attr2, @RequestParam String val2) {
        return ResponseEntity.ok(queryService.queryWithOrConditions(attr1, val1, attr2, val2));
    }

    @GetMapping("/like")
    public ResponseEntity<List<String>> queryWithLike(
            @RequestParam String attribute,
            @RequestParam String pattern) {
        return ResponseEntity.ok(queryService.queryWithLike(attribute, pattern));
    }

    @GetMapping("/present")
    public ResponseEntity<List<String>> queryWithPresence(@RequestParam String attribute) {
        return ResponseEntity.ok(queryService.queryWithPresence(attribute));
    }

    @GetMapping("/count-limit")
    public ResponseEntity<List<String>> queryWithCountLimit(
            @RequestParam String attribute,
            @RequestParam String value,
            @RequestParam int limit) {
        return ResponseEntity.ok(queryService.queryWithCountLimit(attribute, value, limit));
    }

    @PostMapping("/complex")
    public ResponseEntity<List<String>> complexQuery(@RequestBody LDAPQueryService.QueryRequest request) {
        return ResponseEntity.ok(queryService.complexQuery(request));
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> countQuery(
            @RequestParam String baseDn,
            @RequestParam String attribute,
            @RequestParam String value) {
        return ResponseEntity.ok(queryService.countQuery(baseDn, attribute, value));
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getPatternInfo() {
        return ResponseEntity.ok(new PatternInfo(
            "LDAP Query Pattern",
            "Fluent query builder API for complex LDAP queries",
            "1.0",
            List.of("Fluent API", "Query composition", "Search scopes", "Attribute selection", "Query limits"),
            List.of("Complex queries", "Dynamic query building", "Optimized searches", "Query reuse")
        ));
    }

    record PatternInfo(String name, String description, String version, 
                      List<String> features, List<String> useCases) {}
}
