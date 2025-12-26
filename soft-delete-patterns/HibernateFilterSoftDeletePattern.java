package com.example.softdelete.filter;

import org.hibernate.annotations.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Hibernate Filter Soft Delete Pattern
 * 
 * Uses Hibernate @Filter for dynamic soft delete queries.
 * Can enable/disable filter at runtime.
 */
@SpringBootApplication
public class HibernateFilterSoftDeletePattern {

    public static void main(String[] args) {
        SpringApplication.run(HibernateFilterSoftDeletePattern.class, args);
    }

    @Entity
    @Table(name = "items")
    @FilterDef(name = "deletedFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
    @Filter(name = "deletedFilter", condition = "deleted = :isDeleted")
    public static class Item {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;
        private Boolean deleted = false;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Boolean getDeleted() { return deleted; }
        public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    }

    public interface ItemRepository extends JpaRepository<Item, Long> {}

    public static class ItemService {

        @PersistenceContext
        private EntityManager entityManager;

        private final ItemRepository repository;

        public ItemService(ItemRepository repository) {
            this.repository = repository;
        }

        public java.util.List<Item> findActive() {
            org.hibernate.Session session = entityManager.unwrap(org.hibernate.Session.class);
            session.enableFilter("deletedFilter").setParameter("isDeleted", false);
            return repository.findAll();
        }

        public java.util.List<Item> findDeleted() {
            org.hibernate.Session session = entityManager.unwrap(org.hibernate.Session.class);
            session.enableFilter("deletedFilter").setParameter("isDeleted", true);
            return repository.findAll();
        }
    }
}
