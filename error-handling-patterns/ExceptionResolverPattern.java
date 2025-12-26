package com.example.errorhandling.resolver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Exception Resolver Pattern
 * 
 * Custom HandlerExceptionResolver for fine-grained exception handling.
 * Lower-level alternative to @ControllerAdvice.
 * 
 * Key Concepts:
 * - HandlerExceptionResolver interface
 * - Custom exception resolution logic
 * - ModelAndView for error pages
 * - Direct response writing
 * 
 * Dependencies:
 * - spring-boot-starter-web
 */
@SpringBootApplication
public class ExceptionResolverPattern {

    public static void main(String[] args) {
        SpringApplication.run(ExceptionResolverPattern.class, args);
    }

    /**
     * Custom Exception Resolver
     */
    @Component
    public static class CustomExceptionResolver implements HandlerExceptionResolver {

        @Override
        public ModelAndView resolveException(
                HttpServletRequest request,
                HttpServletResponse response,
                Object handler,
                Exception ex) {
            
            try {
                if (ex instanceof BusinessLogicException) {
                    return handleBusinessLogicException(
                        (BusinessLogicException) ex, request, response);
                } else if (ex instanceof ValidationException) {
                    return handleValidationException(
                        (ValidationException) ex, request, response);
                } else if (ex instanceof SecurityException) {
                    return handleSecurityException(
                        (SecurityException) ex, request, response);
                }
            } catch (Exception handlerException) {
                System.err.println("Error handling exception: " + handlerException);
            }
            
            return null; // Let other resolvers handle it
        }

        private ModelAndView handleBusinessLogicException(
                BusinessLogicException ex,
                HttpServletRequest request,
                HttpServletResponse response) throws IOException {
            
            response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            response.setContentType("application/json");
            response.getWriter().write(
                String.format("{\"error\":\"%s\",\"code\":\"%s\"}", 
                    ex.getMessage(), ex.getErrorCode())
            );
            
            return new ModelAndView(); // Empty ModelAndView to indicate handled
        }

        private ModelAndView handleValidationException(
                ValidationException ex,
                HttpServletRequest request,
                HttpServletResponse response) throws IOException {
            
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json");
            response.getWriter().write(
                String.format("{\"error\":\"%s\",\"fields\":%s}", 
                    ex.getMessage(), ex.getFields())
            );
            
            return new ModelAndView();
        }

        private ModelAndView handleSecurityException(
                SecurityException ex,
                HttpServletRequest request,
                HttpServletResponse response) throws IOException {
            
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write(
                String.format("{\"error\":\"%s\"}", ex.getMessage())
            );
            
            return new ModelAndView();
        }
    }

    /**
     * Composite Exception Resolver
     * Chains multiple resolvers
     */
    @Component
    public static class CompositeExceptionResolver implements HandlerExceptionResolver {

        private final List<HandlerExceptionResolver> resolvers;

        public CompositeExceptionResolver(List<HandlerExceptionResolver> resolvers) {
            this.resolvers = resolvers;
        }

        @Override
        public ModelAndView resolveException(
                HttpServletRequest request,
                HttpServletResponse response,
                Object handler,
                Exception ex) {
            
            for (HandlerExceptionResolver resolver : resolvers) {
                ModelAndView mav = resolver.resolveException(request, response, handler, ex);
                if (mav != null) {
                    return mav;
                }
            }
            
            return null;
        }
    }

    /**
     * Custom Exceptions
     */
    public static class BusinessLogicException extends RuntimeException {
        private final String errorCode;

        public BusinessLogicException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    public static class ValidationException extends RuntimeException {
        private final List<String> fields;

        public ValidationException(String message, List<String> fields) {
            super(message);
            this.fields = fields;
        }

        public List<String> getFields() {
            return fields;
        }
    }
}
