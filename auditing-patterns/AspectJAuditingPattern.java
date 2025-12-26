package com.example.auditing.aspectj;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * AspectJ Auditing Pattern
 * 
 * Uses AOP to audit method calls and changes.
 * Intercepts service layer methods.
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class AspectJAuditingPattern {

    public static void main(String[] args) {
        SpringApplication.run(AspectJAuditingPattern.class, args);
    }

    @Aspect
    @Component
    public static class AuditAspect {

        @Before("execution(* com.example.auditing.aspectj.*.save*(..))")
        public void beforeSave(JoinPoint joinPoint) {
            System.out.println("[AUDIT] Before save: " + joinPoint.getSignature());
            System.out.println("[AUDIT] Args: " + java.util.Arrays.toString(joinPoint.getArgs()));
            System.out.println("[AUDIT] Time: " + LocalDateTime.now());
        }

        @AfterReturning(pointcut = "execution(* com.example.auditing.aspectj.*.update*(..))",
                       returning = "result")
        public void afterUpdate(JoinPoint joinPoint, Object result) {
            System.out.println("[AUDIT] After update: " + joinPoint.getSignature());
            System.out.println("[AUDIT] Result: " + result);
        }

        @After("execution(* com.example.auditing.aspectj.*.delete*(..))")
        public void afterDelete(JoinPoint joinPoint) {
            System.out.println("[AUDIT] After delete: " + joinPoint.getSignature());
        }

        @Around("@annotation(Audited)")
        public Object aroundAudited(org.aspectj.lang.ProceedingJoinPoint joinPoint) throws Throwable {
            long start = System.currentTimeMillis();
            System.out.println("[AUDIT] Starting: " + joinPoint.getSignature());
            
            try {
                Object result = joinPoint.proceed();
                long duration = System.currentTimeMillis() - start;
                System.out.println("[AUDIT] Completed in " + duration + "ms");
                return result;
            } catch (Exception e) {
                System.out.println("[AUDIT] Failed: " + e.getMessage());
                throw e;
            }
        }
    }

    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
    public @interface Audited {}
}
