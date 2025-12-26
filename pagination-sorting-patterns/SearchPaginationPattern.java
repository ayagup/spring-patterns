package com.example.pagination.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;

/**
 * Search with Pagination Pattern
 * 
 * Combines complex search criteria with pagination.
 * Uses Spring Data JPA Specifications for dynamic queries.
 */
@SpringBootApplication
public class SearchPaginationPattern {

    public static void main(String[] args) {
        SpringApplication.run(SearchPaginationPattern.class, args);
    }

    @Entity
    @Table(name = "users")
    public static class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String username;
        private String email;
        private Integer age;
        private String city;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
    }

    public interface UserRepository extends JpaRepository<User, Long>, 
                                            JpaSpecificationExecutor<User> {}

    /**
     * Search criteria
     */
    public static class UserSearchCriteria {
        private String username;
        private String email;
        private Integer minAge;
        private Integer maxAge;
        private String city;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Integer getMinAge() { return minAge; }
        public void setMinAge(Integer minAge) { this.minAge = minAge; }
        public Integer getMaxAge() { return maxAge; }
        public void setMaxAge(Integer maxAge) { this.maxAge = maxAge; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
    }

    /**
     * Specification builder
     */
    public static class UserSpecification {

        public static Specification<User> hasUsername(String username) {
            return (root, query, cb) -> 
                username == null ? null : 
                cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%");
        }

        public static Specification<User> hasEmail(String email) {
            return (root, query, cb) -> 
                email == null ? null : 
                cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        }

        public static Specification<User> ageGreaterThan(Integer minAge) {
            return (root, query, cb) -> 
                minAge == null ? null : 
                cb.greaterThanOrEqualTo(root.get("age"), minAge);
        }

        public static Specification<User> ageLessThan(Integer maxAge) {
            return (root, query, cb) -> 
                maxAge == null ? null : 
                cb.lessThanOrEqualTo(root.get("age"), maxAge);
        }

        public static Specification<User> hasCity(String city) {
            return (root, query, cb) -> 
                city == null ? null : 
                cb.equal(root.get("city"), city);
        }

        public static Specification<User> buildSpecification(UserSearchCriteria criteria) {
            return Specification
                .where(hasUsername(criteria.getUsername()))
                .and(hasEmail(criteria.getEmail()))
                .and(ageGreaterThan(criteria.getMinAge()))
                .and(ageLessThan(criteria.getMaxAge()))
                .and(hasCity(criteria.getCity()));
        }
    }

    @RestController
    @RequestMapping("/api/users")
    public static class UserController {

        private final UserRepository repository;

        public UserController(UserRepository repository) {
            this.repository = repository;
        }

        /**
         * Search with pagination
         */
        @GetMapping("/search")
        public Page<User> searchUsers(
            UserSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sortBy
        ) {
            Specification<User> spec = UserSpecification.buildSpecification(criteria);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
            
            return repository.findAll(spec, pageable);
        }
    }
}
