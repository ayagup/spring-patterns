package com.example.auditing.interceptor;

import org.hibernate.*;
import org.hibernate.type.Type;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Hibernate Interceptor Auditing Pattern
 * 
 * Uses Hibernate Interceptor to track entity changes.
 * Low-level interception of database operations.
 */
@SpringBootApplication
public class HibernateInterceptorAuditingPattern {

    public static void main(String[] args) {
        SpringApplication.run(HibernateInterceptorAuditingPattern.class, args);
    }

    @Component
    public static class AuditInterceptor extends EmptyInterceptor {

        @Override
        public boolean onSave(Object entity, Serializable id, Object[] state,
                             String[] propertyNames, Type[] types) {
            System.out.println("Entity saved: " + entity.getClass().getSimpleName());
            logChange("INSERT", entity, id, state, propertyNames);
            return super.onSave(entity, id, state, propertyNames, types);
        }

        @Override
        public boolean onFlushDirty(Object entity, Serializable id,
                                   Object[] currentState, Object[] previousState,
                                   String[] propertyNames, Type[] types) {
            System.out.println("Entity updated: " + entity.getClass().getSimpleName());
            logChanges("UPDATE", entity, id, currentState, previousState, propertyNames);
            return super.onFlushDirty(entity, id, currentState, previousState, 
                                     propertyNames, types);
        }

        @Override
        public void onDelete(Object entity, Serializable id, Object[] state,
                           String[] propertyNames, Type[] types) {
            System.out.println("Entity deleted: " + entity.getClass().getSimpleName());
            logChange("DELETE", entity, id, state, propertyNames);
            super.onDelete(entity, id, state, propertyNames, types);
        }

        private void logChange(String operation, Object entity, Serializable id,
                             Object[] state, String[] propertyNames) {
            System.out.println(operation + " - " + entity.getClass().getName() + 
                             " [ID: " + id + "]");
            for (int i = 0; i < propertyNames.length; i++) {
                System.out.println("  " + propertyNames[i] + " = " + state[i]);
            }
        }

        private void logChanges(String operation, Object entity, Serializable id,
                              Object[] currentState, Object[] previousState,
                              String[] propertyNames) {
            System.out.println(operation + " - " + entity.getClass().getName() + 
                             " [ID: " + id + "]");
            for (int i = 0; i < propertyNames.length; i++) {
                if (currentState[i] != null && previousState[i] != null &&
                    !currentState[i].equals(previousState[i])) {
                    System.out.println("  " + propertyNames[i] + ": " +
                                     previousState[i] + " -> " + currentState[i]);
                }
            }
        }
    }
}
