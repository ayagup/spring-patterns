package com.example.errorhandling.localization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

/**
 * Error Message Localization Pattern
 * 
 * Provides localized error messages based on client's locale.
 * Uses Spring's MessageSource for internationalization.
 */
@SpringBootApplication
public class ErrorMessageLocalizationPattern {

    public static void main(String[] args) {
        SpringApplication.run(ErrorMessageLocalizationPattern.class, args);
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages/errors");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @ControllerAdvice
    public static class LocalizedErrorAdvice {

        private final MessageSource messageSource;

        public LocalizedErrorAdvice(MessageSource messageSource) {
            this.messageSource = messageSource;
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
            Locale locale = LocaleContextHolder.getLocale();
            String localizedMessage = messageSource.getMessage(
                ex.getMessageKey(),
                ex.getArgs(),
                locale
            );
            
            ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                localizedMessage,
                locale.toString()
            );
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    public record ErrorResponse(String code, String message, String locale) {}

    public static class BusinessException extends RuntimeException {
        private final String errorCode;
        private final String messageKey;
        private final Object[] args;

        public BusinessException(String errorCode, String messageKey, Object... args) {
            super(messageKey);
            this.errorCode = errorCode;
            this.messageKey = messageKey;
            this.args = args;
        }

        public String getErrorCode() { return errorCode; }
        public String getMessageKey() { return messageKey; }
        public Object[] getArgs() { return args; }
    }
}
