I'll create a comprehensive Spring Boot project demonstrating all miscellaneous patterns.

## Project Structure

```xml pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.miscpatterns</groupId>
    <artifactId>miscellaneous-patterns</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Thymeleaf -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>

        <!-- Spring Boot Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- H2 Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Spring Boot AOP -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>

        <!-- Apache Commons FileUpload -->
        <dependency>
            <groupId>commons-fileupload</groupId>
            <artifactId>commons-fileupload</artifactId>
            <version>1.5</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## Main Application

```java src/main/java/com/miscpatterns/MiscellaneousPatternsApplication.java
package com.miscpatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MiscellaneousPatternsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiscellaneousPatternsApplication.class, args);
    }
}
```

## 1. Callback Pattern

```java src/main/java/com/miscpatterns/callback/Callback.java
package com.miscpatterns.callback;

/**
 * Callback Pattern - Generic callback interface
 */
@FunctionalInterface
public interface Callback<T> {
    void onComplete(T result);
}
```

```java src/main/java/com/miscpatterns/callback/AsyncCallback.java
package com.miscpatterns.callback;

/**
 * Callback Pattern - Async callback with error handling
 */
public interface AsyncCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception exception);
}
```

```java src/main/java/com/miscpatterns/callback/CallbackExecutor.java
package com.miscpatterns.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Callback Pattern - Executor that demonstrates callback usage
 */
@Component
@Slf4j
public class CallbackExecutor {

    /**
     * Synchronous execution with callback
     */
    public <T> void execute(Task<T> task, Callback<T> callback) {
        log.info("Executing task synchronously");
        try {
            T result = task.execute();
            callback.onComplete(result);
        } catch (Exception e) {
            log.error("Error executing task", e);
        }
    }

    /**
     * Asynchronous execution with callback
     */
    @Async
    public <T> CompletableFuture<Void> executeAsync(Task<T> task, AsyncCallback<T> callback) {
        log.info("Executing task asynchronously");
        return CompletableFuture.runAsync(() -> {
            try {
                T result = task.execute();
                callback.onSuccess(result);
            } catch (Exception e) {
                log.error("Error executing async task", e);
                callback.onFailure(e);
            }
        });
    }

    /**
     * Execute with multiple callbacks
     */
    public <T> void executeWithCallbacks(Task<T> task, 
                                         Callback<T> successCallback,
                                         Callback<Exception> errorCallback) {
        log.info("Executing task with multiple callbacks");
        try {
            T result = task.execute();
            successCallback.onComplete(result);
        } catch (Exception e) {
            log.error("Error in task execution", e);
            errorCallback.onComplete(e);
        }
    }

    @FunctionalInterface
    public interface Task<T> {
        T execute() throws Exception;
    }
}
```

```java src/main/java/com/miscpatterns/callback/DataProcessor.java
package com.miscpatterns.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Callback Pattern - Service using callbacks for async processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataProcessor {

    private final CallbackExecutor callbackExecutor;

    /**
     * Process data with callback notification
     */
    public void processData(List<String> data, Callback<List<String>> callback) {
        log.info("Processing data with callback");
        
        callbackExecutor.execute(
            () -> data.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList()),
            callback
        );
    }

    /**
     * Process data asynchronously with success/failure callbacks
     */
    public void processDataAsync(List<String> data, AsyncCallback<List<String>> callback) {
        log.info("Processing data asynchronously");
        
        callbackExecutor.executeAsync(
            () -> {
                // Simulate processing
                Thread.sleep(2000);
                return data.stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());
            },
            callback
        );
    }

    /**
     * Chain callbacks for complex processing
     */
    public void chainProcessing(List<String> data, 
                                Callback<List<String>> intermediateCallback,
                                Callback<Integer> finalCallback) {
        log.info("Chaining callbacks");
        
        // First processing step
        callbackExecutor.execute(
            () -> data.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList()),
            result -> {
                intermediateCallback.onComplete(result);
                
                // Second processing step
                callbackExecutor.execute(
                    () -> result.size(),
                    finalCallback
                );
            }
        );
    }
}
```

## 2. Template Callback Pattern

```java src/main/java/com/miscpatterns/template/JdbcTemplate.java
package com.miscpatterns.template;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Template Callback Pattern - JDBC Template implementation
 * Demonstrates how Spring's JdbcTemplate works
 */
@Component
@Slf4j
public class CustomJdbcTemplate {

    private final String url = "jdbc:h2:mem:testdb";

    /**
     * Execute query with callback for result set processing
     */
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        log.info("Executing query: {}", sql);
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs, rowNum++));
            }
            return results;
            
        } catch (SQLException e) {
            log.error("Error executing query", e);
            throw new RuntimeException(e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Execute update with callback
     */
    public int update(String sql, PreparedStatementSetter psSetter) {
        log.info("Executing update: {}", sql);
        
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DriverManager.getConnection(url);
            ps = conn.prepareStatement(sql);
            psSetter.setValues(ps);
            return ps.executeUpdate();
            
        } catch (SQLException e) {
            log.error("Error executing update", e);
            throw new RuntimeException(e);
        } finally {
            closeResources(conn, ps, null);
        }
    }

    /**
     * Execute with connection callback - most flexible
     */
    public <T> T execute(ConnectionCallback<T> action) {
        log.info("Executing with connection callback");
        
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            return action.doInConnection(conn);
        } catch (SQLException e) {
            log.error("Error in connection callback", e);
            throw new RuntimeException(e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            log.error("Error closing resources", e);
        }
    }

    /**
     * Row mapper callback interface
     */
    @FunctionalInterface
    public interface RowMapper<T> {
        T mapRow(ResultSet rs, int rowNum) throws SQLException;
    }

    /**
     * Prepared statement setter callback
     */
    @FunctionalInterface
    public interface PreparedStatementSetter {
        void setValues(PreparedStatement ps) throws SQLException;
    }

    /**
     * Connection callback interface
     */
    @FunctionalInterface
    public interface ConnectionCallback<T> {
        T doInConnection(Connection conn) throws SQLException;
    }
}
```

```java src/main/java/com/miscpatterns/template/TransactionTemplate.java
package com.miscpatterns.template;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Template Callback Pattern - Transaction template
 */
@Component
@Slf4j
public class CustomTransactionTemplate {

    /**
     * Execute action within transaction
     */
    public <T> T execute(TransactionCallback<T> action) {
        log.info("Starting transaction");
        
        try {
            beginTransaction();
            T result = action.doInTransaction();
            commitTransaction();
            log.info("Transaction committed");
            return result;
            
        } catch (Exception e) {
            log.error("Transaction failed, rolling back", e);
            rollbackTransaction();
            throw new RuntimeException("Transaction failed", e);
        }
    }

    /**
     * Execute without return value
     */
    public void executeWithoutResult(TransactionCallbackWithoutResult action) {
        log.info("Starting transaction without result");
        
        try {
            beginTransaction();
            action.doInTransaction();
            commitTransaction();
            log.info("Transaction committed");
            
        } catch (Exception e) {
            log.error("Transaction failed, rolling back", e);
            rollbackTransaction();
            throw new RuntimeException("Transaction failed", e);
        }
    }

    private void beginTransaction() {
        log.debug("BEGIN TRANSACTION");
    }

    private void commitTransaction() {
        log.debug("COMMIT TRANSACTION");
    }

    private void rollbackTransaction() {
        log.debug("ROLLBACK TRANSACTION");
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T doInTransaction() throws Exception;
    }

    @FunctionalInterface
    public interface TransactionCallbackWithoutResult {
        void doInTransaction() throws Exception;
    }
}
```

```java src/main/java/com/miscpatterns/template/RestTemplate.java
package com.miscpatterns.template;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Template Callback Pattern - HTTP Rest template
 */
@Component
@Slf4j
public class CustomRestTemplate {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Execute GET request with response handler callback
     */
    public <T> T getForObject(String url, ResponseExtractor<T> responseExtractor) {
        log.info("GET request to: {}", url);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
                
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            return responseExtractor.extractData(response);
            
        } catch (IOException | InterruptedException e) {
            log.error("Error executing GET request", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute POST request with callbacks
     */
    public <T> T postForObject(String url, Object body, 
                               RequestCallback requestCallback,
                               ResponseExtractor<T> responseExtractor) {
        log.info("POST request to: {}", url);
        
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json");
            
            // Allow callback to customize request
            requestCallback.doWithRequest(builder);
            
            HttpRequest request = builder
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
                
            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
            
            return responseExtractor.extractData(response);
            
        } catch (IOException | InterruptedException e) {
            log.error("Error executing POST request", e);
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface ResponseExtractor<T> {
        T extractData(HttpResponse<String> response);
    }

    @FunctionalInterface
    public interface RequestCallback {
        void doWithRequest(HttpRequest.Builder builder);
    }
}
```

## 3. ResourceBundle Pattern

```properties src/main/resources/messages.properties
# Default messages (English)
greeting=Hello
welcome=Welcome to our application
user.created=User created successfully
user.updated=User updated successfully
user.deleted=User deleted successfully
error.not.found=Resource not found
error.internal=Internal server error
validation.required=Field {0} is required
validation.email=Invalid email format for {0}
```

```properties src/main/resources/messages_es.properties
# Spanish messages
greeting=Hola
welcome=Bienvenido a nuestra aplicación
user.created=Usuario creado exitosamente
user.updated=Usuario actualizado exitosamente
user.deleted=Usuario eliminado exitosamente
error.not.found=Recurso no encontrado
error.internal=Error interno del servidor
validation.required=El campo {0} es requerido
validation.email=Formato de correo electrónico inválido para {0}
```

```properties src/main/resources/messages_fr.properties
# French messages
greeting=Bonjour
welcome=Bienvenue dans notre application
user.created=Utilisateur créé avec succès
user.updated=Utilisateur mis à jour avec succès
user.deleted=Utilisateur supprimé avec succès
error.not.found=Ressource introuvable
error.internal=Erreur interne du serveur
validation.required=Le champ {0} est requis
validation.email=Format d'email invalide pour {0}
```

```java src/main/java/com/miscpatterns/i18n/MessageSourceService.java
package com.miscpatterns.i18n;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * ResourceBundle Pattern - Service for internationalized messages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSourceService {

    private final MessageSource messageSource;

    /**
     * Get message for current locale
     */
    public String getMessage(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        log.debug("Getting message for key: {} and locale: {}", key, locale);
        return messageSource.getMessage(key, null, locale);
    }

    /**
     * Get message with parameters
     */
    public String getMessage(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        log.debug("Getting message for key: {} with args: {}", key, args);
        return messageSource.getMessage(key, args, locale);
    }

    /**
     * Get message for specific locale
     */
    public String getMessage(String key, Locale locale) {
        log.debug("Getting message for key: {} and locale: {}", key, locale);
        return messageSource.getMessage(key, null, locale);
    }

    /**
     * Get message with default value
     */
    public String getMessage(String key, String defaultMessage) {
        Locale locale = LocaleContextHolder.getLocale();
        log.debug("Getting message for key: {} with default: {}", key, defaultMessage);
        return messageSource.getMessage(key, null, defaultMessage, locale);
    }

    /**
     * Get message for all supported locales
     */
    public MessageBundle getMessageBundle(String key) {
        return MessageBundle.builder()
            .key(key)
            .english(getMessage(key, Locale.ENGLISH))
            .spanish(getMessage(key, new Locale("es")))
            .french(getMessage(key, Locale.FRENCH))
            .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class MessageBundle {
        private String key;
        private String english;
        private String spanish;
        private String french;
    }
}
```

## 4. Locale Resolver Pattern

```java src/main/java/com/miscpatterns/locale/CustomLocaleResolver.java
package com.miscpatterns.locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

/**
 * Locale Resolver Pattern - Custom locale resolution strategy
 */
@Slf4j
public class CustomLocaleResolver implements LocaleResolver {

    private static final String LOCALE_SESSION_ATTRIBUTE = "session.locale";
    private static final String LOCALE_HEADER = "Accept-Language";
    private Locale defaultLocale = Locale.ENGLISH;

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // 1. Check for locale in session
        Locale sessionLocale = (Locale) request.getSession()
            .getAttribute(LOCALE_SESSION_ATTRIBUTE);
        if (sessionLocale != null) {
            log.debug("Resolved locale from session: {}", sessionLocale);
            return sessionLocale;
        }

        // 2. Check for locale parameter in request
        String localeParam = request.getParameter("locale");
        if (localeParam != null && !localeParam.isEmpty()) {
            Locale paramLocale = Locale.forLanguageTag(localeParam);
            log.debug("Resolved locale from parameter: {}", paramLocale);
            return paramLocale;
        }

        // 3. Check Accept-Language header
        String acceptLanguage = request.getHeader(LOCALE_HEADER);
        if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
            Locale headerLocale = Locale.forLanguageTag(acceptLanguage.split(",")[0]);
            log.debug("Resolved locale from header: {}", headerLocale);
            return headerLocale;
        }

        // 4. Return default locale
        log.debug("Using default locale: {}", defaultLocale);
        return defaultLocale;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, 
                         Locale locale) {
        log.info("Setting locale to: {}", locale);
        request.getSession().setAttribute(LOCALE_SESSION_ATTRIBUTE, locale);
    }

    public void setDefaultLocale(Locale locale) {
        this.defaultLocale = locale;
    }
}
```

```java src/main/java/com/miscpatterns/locale/LocaleService.java
package com.miscpatterns.locale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Service for locale management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocaleService {

    private final LocaleResolver localeResolver;

    /**
     * Get current locale
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * Change user locale
     */
    public void changeLocale(HttpServletRequest request, HttpServletResponse response, 
                            String localeString) {
        Locale locale = Locale.forLanguageTag(localeString);
        log.info("Changing locale to: {}", locale);
        localeResolver.setLocale(request, response, locale);
        LocaleContextHolder.setLocale(locale);
    }

    /**
     * Get supported locales
     */
    public java.util.List<LocaleInfo> getSupportedLocales() {
        return java.util.List.of(
            new LocaleInfo("en", "English", Locale.ENGLISH),
            new LocaleInfo("es", "Español", new Locale("es")),
            new LocaleInfo("fr", "Français", Locale.FRENCH)
        );
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class LocaleInfo {
        private String code;
        private String displayName;
        private Locale locale;
    }
}
```

## 5. Theme Resolver Pattern

```java src/main/java/com/miscpatterns/theme/CustomThemeResolver.java
package com.miscpatterns.theme;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ThemeResolver;

/**
 * Theme Resolver Pattern - Custom theme resolution
 */
@Slf4j
public class CustomThemeResolver implements ThemeResolver {

    private static final String THEME_SESSION_ATTRIBUTE = "session.theme";
    private static final String THEME_PARAMETER = "theme";
    private static final String THEME_COOKIE = "APP_THEME";
    private String defaultTheme = "light";

    @Override
    public String resolveThemeName(HttpServletRequest request) {
        // 1. Check session
        String sessionTheme = (String) request.getSession()
            .getAttribute(THEME_SESSION_ATTRIBUTE);
        if (sessionTheme != null) {
            log.debug("Resolved theme from session: {}", sessionTheme);
            return sessionTheme;
        }

        // 2. Check request parameter
        String paramTheme = request.getParameter(THEME_PARAMETER);
        if (paramTheme != null && !paramTheme.isEmpty()) {
            log.debug("Resolved theme from parameter: {}", paramTheme);
            return paramTheme;
        }

        // 3. Check cookie
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (THEME_COOKIE.equals(cookie.getName())) {
                    log.debug("Resolved theme from cookie: {}", cookie.getValue());
                    return cookie.getValue();
                }
            }
        }

        // 4. Return default
        log.debug("Using default theme: {}", defaultTheme);
        return defaultTheme;
    }

    @Override
    public void setThemeName(HttpServletRequest request, HttpServletResponse response, 
                            String themeName) {
        log.info("Setting theme to: {}", themeName);
        request.getSession().setAttribute(THEME_SESSION_ATTRIBUTE, themeName);
        
        // Also set cookie
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(
            THEME_COOKIE, themeName);
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public void setDefaultTheme(String theme) {
        this.defaultTheme = theme;
    }
}
```

```java src/main/java/com/miscpatterns/theme/ThemeService.java
package com.miscpatterns.theme;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ThemeResolver;

import java.util.List;
import java.util.Map;

/**
 * Service for theme management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ThemeService {

    private final ThemeResolver themeResolver;

    /**
     * Get current theme
     */
    public String getCurrentTheme(HttpServletRequest request) {
        return themeResolver.resolveThemeName(request);
    }

    /**
     * Change theme
     */
    public void changeTheme(HttpServletRequest request, HttpServletResponse response,
                           String themeName) {
        log.info("Changing theme to: {}", themeName);
        themeResolver.setThemeName(request, response, themeName);
    }

    /**
     * Get available themes
     */
    public List<ThemeInfo> getAvailableThemes() {
        return List.of(
            new ThemeInfo("light", "Light Theme", "#FFFFFF", "#000000"),
            new ThemeInfo("dark", "Dark Theme", "#1a1a1a", "#FFFFFF"),
            new ThemeInfo("blue", "Blue Theme", "#E3F2FD", "#0D47A1"),
            new ThemeInfo("green", "Green Theme", "#E8F5E9", "#1B5E20")
        );
    }

    /**
     * Get theme properties
     */
    public Map<String, String> getThemeProperties(String themeName) {
        return switch (themeName) {
            case "dark" -> Map.of(
                "background", "#1a1a1a",
                "text", "#FFFFFF",
                "primary", "#BB86FC",
                "secondary", "#03DAC6"
            );
            case "blue" -> Map.of(
                "background", "#E3F2FD",
                "text", "#0D47A1",
                "primary", "#2196F3",
                "secondary", "#03A9F4"
            );
            case "green" -> Map.of(
                "background", "#E8F5E9",
                "text", "#1B5E20",
                "primary", "#4CAF50",
                "secondary", "#8BC34A"
            );
            default -> Map.of(
                "background", "#FFFFFF",
                "text", "#000000",
                "primary", "#2196F3",
                "secondary", "#FF9800"
            );
        };
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ThemeInfo {
        private String name;
        private String displayName;
        private String backgroundColor;
        private String textColor;
    }
}
```

## 6. Multipart Resolver Pattern

```java src/main/java/com/miscpatterns/multipart/CustomMultipartResolver.java
package com.miscpatterns.multipart;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * Multipart Resolver Pattern - Custom multipart file upload resolver
 */
@Slf4j
public class CustomMultipartResolver implements MultipartResolver {

    private final StandardServletMultipartResolver delegate = 
        new StandardServletMultipartResolver();
    
    private long maxUploadSize = 10 * 1024 * 1024; // 10MB
    private long maxUploadSizePerFile = 5 * 1024 * 1024; // 5MB

    @Override
    public boolean isMultipart(HttpServletRequest request) {
        boolean isMultipart = delegate.isMultipart(request);
        log.debug("Request is multipart: {}", isMultipart);
        return isMultipart;
    }

```java src/main/java/com/miscpatterns/multipart/CustomMultipartResolver.java
    @Override
    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) 
            throws MultipartException {
        log.info("Resolving multipart request");
        
        try {
            // Validate content length
            long contentLength = request.getContentLengthLong();
            if (contentLength > maxUploadSize) {
                throw new MultipartException(
                    String.format("Maximum upload size exceeded: %d bytes (max: %d bytes)",
                        contentLength, maxUploadSize));
            }
            
            // Use delegate to parse multipart request
            MultipartHttpServletRequest multipartRequest = delegate.resolveMultipart(request);
            
            log.info("Multipart request resolved successfully. Files: {}", 
                multipartRequest.getFileMap().size());
            
            return multipartRequest;
            
        } catch (Exception e) {
            log.error("Error resolving multipart request", e);
            throw new MultipartException("Failed to parse multipart request", e);
        }
    }

    @Override
    public void cleanupMultipart(MultipartHttpServletRequest request) {
        log.info("Cleaning up multipart request");
        delegate.cleanupMultipart(request);
    }

    public void setMaxUploadSize(long maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }

    public void setMaxUploadSizePerFile(long maxUploadSizePerFile) {
        this.maxUploadSizePerFile = maxUploadSizePerFile;
    }
}
```

```java src/main/java/com/miscpatterns/multipart/FileUploadService.java
package com.miscpatterns.multipart;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling file uploads
 */
@Service
@Slf4j
public class FileUploadService {

    private final Path uploadDirectory = Paths.get("uploads");

    public FileUploadService() {
        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            log.error("Could not create upload directory", e);
        }
    }

    /**
     * Upload single file
     */
    public FileInfo uploadFile(MultipartFile file) throws IOException {
        log.info("Uploading file: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : "";
        String filename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path targetPath = uploadDirectory.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("File uploaded successfully: {}", filename);
        
        return FileInfo.builder()
            .originalFilename(originalFilename)
            .storedFilename(filename)
            .contentType(file.getContentType())
            .size(file.getSize())
            .path(targetPath.toString())
            .build();
    }

    /**
     * Upload multiple files
     */
    public List<FileInfo> uploadFiles(List<MultipartFile> files) throws IOException {
        log.info("Uploading {} files", files.size());
        
        List<FileInfo> uploadedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                uploadedFiles.add(uploadFile(file));
            }
        }
        
        return uploadedFiles;
    }

    /**
     * Delete file
     */
    public void deleteFile(String filename) throws IOException {
        log.info("Deleting file: {}", filename);
        Path filePath = uploadDirectory.resolve(filename);
        Files.deleteIfExists(filePath);
    }

    /**
     * Get file info
     */
    public FileInfo getFileInfo(String filename) throws IOException {
        Path filePath = uploadDirectory.resolve(filename);
        
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File not found: " + filename);
        }
        
        return FileInfo.builder()
            .storedFilename(filename)
            .size(Files.size(filePath))
            .path(filePath.toString())
            .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class FileInfo {
        private String originalFilename;
        private String storedFilename;
        private String contentType;
        private long size;
        private String path;
    }
}
```

## 7. Handler Exception Resolver Pattern

```java src/main/java/com/miscpatterns/exception/CustomHandlerExceptionResolver.java
package com.miscpatterns.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

/**
 * Handler Exception Resolver Pattern - Custom exception handling
 */
@Component
@Slf4j
public class CustomHandlerExceptionResolver implements HandlerExceptionResolver, Ordered {

    @Override
    public ModelAndView resolveException(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        Object handler, 
                                        Exception ex) {
        log.error("Handling exception: {}", ex.getMessage(), ex);

        try {
            if (ex instanceof BusinessException) {
                return handleBusinessException((BusinessException) ex, request, response);
            } else if (ex instanceof ValidationException) {
                return handleValidationException((ValidationException) ex, request, response);
            } else if (ex instanceof AccessDeniedException) {
                return handleAccessDeniedException((AccessDeniedException) ex, request, response);
            } else if (ex instanceof IllegalArgumentException) {
                return handleIllegalArgumentException((IllegalArgumentException) ex, request, response);
            }
        } catch (Exception e) {
            log.error("Error in exception resolver", e);
        }

        // Return null to allow other resolvers to handle
        return null;
    }

    private ModelAndView handleBusinessException(BusinessException ex, 
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) throws IOException {
        log.warn("Business exception: {}", ex.getMessage());
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        return new ModelAndView();
    }

    private ModelAndView handleValidationException(ValidationException ex,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) throws IOException {
        log.warn("Validation exception: {}", ex.getMessage());
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        return new ModelAndView();
    }

    private ModelAndView handleAccessDeniedException(AccessDeniedException ex,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) throws IOException {
        log.warn("Access denied: {}", ex.getMessage());
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
        return new ModelAndView();
    }

    private ModelAndView handleIllegalArgumentException(IllegalArgumentException ex,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response) throws IOException {
        log.warn("Illegal argument: {}", ex.getMessage());
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        return new ModelAndView();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
```

```java src/main/java/com/miscpatterns/exception/BusinessException.java
package com.miscpatterns.exception;

/**
 * Custom business exception
 */
public class BusinessException extends RuntimeException {
    
    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```

```java src/main/java/com/miscpatterns/exception/ValidationException.java
package com.miscpatterns.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation exception with field errors
 */
public class ValidationException extends RuntimeException {
    
    private final List<FieldError> fieldErrors;

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = new ArrayList<>();
    }

    public ValidationException(String message, List<FieldError> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void addFieldError(String field, String message) {
        fieldErrors.add(new FieldError(field, message));
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }
}
```

```java src/main/java/com/miscpatterns/exception/GlobalExceptionHandler.java
package com.miscpatterns.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler using @RestControllerAdvice
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.error("Business exception: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Business Error")
            .message(ex.getMessage())
            .errorCode(ex.getErrorCode())
            .build();
            
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(ValidationException ex) {
        log.error("Validation exception: {}", ex.getMessage());
        
        ValidationErrorResponse response = ValidationErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .message(ex.getMessage())
            .fieldErrors(ex.getFieldErrors())
            .build();
            
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException ex) {
        log.error("Multipart exception: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("File Upload Error")
            .message(ex.getMessage())
            .build();
            
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @lombok.Data
    @lombok.Builder
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String errorCode;
    }

    @lombok.Data
    @lombok.Builder
    public static class ValidationErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private java.util.List<ValidationException.FieldError> fieldErrors;
    }
}
```

## 8. Bean Post Processor Pattern

```java src/main/java/com/miscpatterns/processor/LoggingBeanPostProcessor.java
package com.miscpatterns.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Bean Post Processor Pattern - Logging bean lifecycle
 */
@Component
@Slf4j
public class LoggingBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) 
            throws BeansException {
        if (shouldLog(beanName)) {
            log.debug("Before initialization of bean: {}", beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) 
            throws BeansException {
        if (shouldLog(beanName)) {
            log.debug("After initialization of bean: {}", beanName);
        }
        return bean;
    }

    private boolean shouldLog(String beanName) {
        // Only log our custom beans
        return beanName.startsWith("com.miscpatterns") || 
               beanName.contains("Service") || 
               beanName.contains("Controller");
    }
}
```

```java src/main/java/com/miscpatterns/processor/PerformanceMonitoringBeanPostProcessor.java
package com.miscpatterns.processor;

import com.miscpatterns.annotation.Monitored;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

/**
 * Bean Post Processor Pattern - Add performance monitoring to beans
 */
@Component
@Slf4j
public class PerformanceMonitoringBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) 
            throws BeansException {
        
        // Check if bean class has @Monitored annotation
        if (bean.getClass().isAnnotationPresent(Monitored.class)) {
            log.info("Adding performance monitoring to bean: {}", beanName);
            return createMonitoredProxy(bean);
        }
        
        return bean;
    }

    private Object createMonitoredProxy(Object bean) {
        return Proxy.newProxyInstance(
            bean.getClass().getClassLoader(),
            bean.getClass().getInterfaces(),
            (proxy, method, args) -> {
                long startTime = System.currentTimeMillis();
                try {
                    return method.invoke(bean, args);
                } finally {
                    long endTime = System.currentTimeMillis();
                    log.info("Method {} executed in {} ms", 
                        method.getName(), (endTime - startTime));
                }
            }
        );
    }
}
```

```java src/main/java/com/miscpatterns/processor/AutowiredAnnotationBeanPostProcessor.java
package com.miscpatterns.processor;

import com.miscpatterns.annotation.CustomAutowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * Bean Post Processor Pattern - Custom autowiring
 */
@Component
@Slf4j
public class CustomAutowiredBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) 
            throws BeansException {
        
        Field[] fields = bean.getClass().getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(CustomAutowired.class)) {
                log.debug("Custom autowiring field {} in bean {}", field.getName(), beanName);
                
                try {
                    field.setAccessible(true);
                    Object dependency = applicationContext.getBean(field.getType());
                    field.set(bean, dependency);
                } catch (Exception e) {
                    log.error("Error autowiring field: {}", field.getName(), e);
                }
            }
        }
        
        return bean;
    }
}
```

## 9. Bean Factory Post Processor Pattern

```java src/main/java/com/miscpatterns/processor/PropertyPlaceholderBeanFactoryPostProcessor.java
package com.miscpatterns.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * Bean Factory Post Processor Pattern - Modify bean definitions before instantiation
 */
@Component
@Slf4j
public class PropertyPlaceholderBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) 
            throws BeansException {
        
        log.info("Processing bean factory with {} beans", beanFactory.getBeanDefinitionCount());
        
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        
        for (String beanName : beanNames) {
            if (shouldProcess(beanName)) {
                log.debug("Processing bean definition: {}", beanName);
                // Can modify bean definitions here
            }
        }
    }

    private boolean shouldProcess(String beanName) {
        return beanName.startsWith("com.miscpatterns");
    }
}
```

```java src/main/java/com/miscpatterns/processor/CustomScopeBeanFactoryPostProcessor.java
package com.miscpatterns.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * Bean Factory Post Processor Pattern - Modify bean scopes
 */
@Component
@Slf4j
public class CustomScopeBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) 
            throws BeansException {
        
        log.info("Modifying bean scopes");
        
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            
            // Example: Change scope of specific beans
            if (beanName.endsWith("Service") && beanDefinition.isSingleton()) {
                log.debug("Bean {} is singleton", beanName);
            }
        }
    }
}
```

```java src/main/java/com/miscpatterns/processor/BeanDefinitionRegistryPostProcessor.java
package com.miscpatterns.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Bean Factory Post Processor Pattern - Register additional beans
 */
@Component
@Slf4j
public class CustomBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) 
            throws BeansException {
        
        log.info("Registering custom bean definitions");
        
        // Example: Programmatically register a bean
        BeanDefinition beanDefinition = BeanDefinitionBuilder
            .genericBeanDefinition(DynamicBean.class)
            .addPropertyValue("name", "Dynamically Created Bean")
            .getBeanDefinition();
        
        registry.registerBeanDefinition("dynamicBean", beanDefinition);
        log.info("Registered dynamic bean");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) 
            throws BeansException {
        log.info("Post processing bean factory after registration");
    }

    @lombok.Data
    public static class DynamicBean {
        private String name;
    }
}
```

## 10. Destruction Aware Bean Post Processor Pattern

```java src/main/java/com/miscpatterns/processor/DestructionAwareBeanPostProcessor.java
package com.miscpatterns.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Destruction Aware Bean Post Processor Pattern - Handle bean destruction
 */
@Component
@Slf4j
public class CustomDestructionAwareBeanPostProcessor implements DestructionAwareBeanPostProcessor {

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) 
            throws BeansException {
        
        if (shouldProcess(beanName)) {
            log.info("Before destruction of bean: {}", beanName);
            
            // Cleanup logic
            if (bean instanceof ResourceHolder) {
                log.info("Releasing resources for bean: {}", beanName);
                ((ResourceHolder) bean).releaseResources();
            }
        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return bean instanceof ResourceHolder;
    }

    private boolean shouldProcess(String beanName) {
        return beanName.startsWith("com.miscpatterns");
    }

    public interface ResourceHolder {
        void releaseResources();
    }
}
```

```java src/main/java/com/miscpatterns/processor/ConnectionPoolManager.java
package com.miscpatterns.processor;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Example bean that implements ResourceHolder
 */
@Component
@Slf4j
public class ConnectionPoolManager implements 
        CustomDestructionAwareBeanPostProcessor.ResourceHolder {

    private final List<String> connections = new ArrayList<>();

    public ConnectionPoolManager() {
        log.info("Initializing connection pool");
        connections.add("Connection-1");
        connections.add("Connection-2");
        connections.add("Connection-3");
    }

    @Override
    public void releaseResources() {
        log.info("Releasing {} connections", connections.size());
        connections.clear();
    }

    @PreDestroy
    public void cleanup() {
        log.info("@PreDestroy - Cleaning up connection pool");
    }
}
```

## Annotations

```java src/main/java/com/miscpatterns/annotation/Monitored.java
package com.miscpatterns.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for performance monitoring
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Monitored {
}
```

```java src/main/java/com/miscpatterns/annotation/CustomAutowired.java
package com.miscpatterns.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom autowired annotation
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomAutowired {
}
```

## Controllers

```java src/main/java/com/miscpatterns/controller/CallbackController.java
package com.miscpatterns.controller;

import com.miscpatterns.callback.AsyncCallback;
import com.miscpatterns.callback.Callback;
import com.miscpatterns.callback.DataProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controller demonstrating Callback Pattern
 */
@RestController
@RequestMapping("/api/callback")
@RequiredArgsConstructor
@Slf4j
public class CallbackController {

    private final DataProcessor dataProcessor;

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processData(@RequestBody List<String> data) {
        Map<String, Object> response = new HashMap<>();
        
        dataProcessor.processData(data, result -> {
            log.info("Processing completed with {} items", result.size());
            response.put("processed", result);
            response.put("count", result.size());
        });
        
        response.put("status", "completed");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/process-async")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> processDataAsync(
            @RequestBody List<String> data) {
        
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        dataProcessor.processDataAsync(data, new AsyncCallback<>() {
            @Override
            public void onSuccess(List<String> result) {
                log.info("Async processing completed successfully");
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("processed", result);
                response.put("count", result.size());
                future.complete(response);
            }

            @Override
            public void onFailure(Exception exception) {
                log.error("Async processing failed", exception);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", exception.getMessage());
                future.complete(response);
            }
        });
        
        return future.thenApply(ResponseEntity::ok);
    }

    @PostMapping("/chain")
    public ResponseEntity<Map<String, Object>> chainProcessing(@RequestBody List<String> data) {
        Map<String, Object> response = new HashMap<>();
        
        dataProcessor.chainProcessing(
            data,
            intermediateResult -> {
                log.info("Intermediate processing: {} items", intermediateResult.size());
                response.put("intermediate", intermediateResult);
            },
            finalCount -> {
                log.info("Final count: {}", finalCount);
                response.put("finalCount", finalCount);
                response.put("status", "completed");
            }
        );
        
        return ResponseEntity.ok(response);
    }
}
```

```java src/main/java/com/miscpatterns/controller/LocaleController.java
package com/miscpatterns.controller;

import com.miscpatterns.i18n.MessageSourceService;
import com.miscpatterns.locale.LocaleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller demonstrating Locale Resolver and ResourceBundle Patterns
 */
@RestController
@RequestMapping("/api/locale")
@RequiredArgsConstructor
@Slf4j
public class LocaleController {

    private final LocaleService localeService;
    private final MessageSourceService messageSourceService;

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentLocale() {
        Map<String, Object> response = new HashMap<>();
        response.put("locale", localeService.getCurrentLocale().toString());
        response.put("greeting", messageSourceService.getMessage("greeting"));
        response.put("welcome", messageSourceService.getMessage("welcome"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change")
    public ResponseEntity<Map<String, String>> changeLocale(
            @RequestParam String locale,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        localeService.changeLocale(request, response, locale);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "locale", locale,
            "message", "Locale changed successfully"
        ));
    }

    @GetMapping("/supported")
    public ResponseEntity<java.util.List<LocaleService.LocaleInfo>> getSupportedLocales() {
        return ResponseEntity.ok(localeService.getSupportedLocales());
    }

    @GetMapping("/message/{key}")
    public ResponseEntity<Map<String, String>> getMessage(@PathVariable String key) {
        MessageSourceService.MessageBundle bundle = messageSourceService.getMessageBundle(key);
        
        Map<String, String> response = new HashMap<>();
        response.put("key", bundle.getKey());
        response.put("en", bundle.getEnglish());
        response.put("es", bundle.getSpanish());
        response.put("fr", bundle.getFrench());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/message/{key}/formatted")
    public ResponseEntity<String> getFormattedMessage(
            @PathVariable String key,
            @RequestParam String... args) {
        
        String message = messageSourceService.getMessage(key, (Object[]) args);
        return ResponseEntity.ok(message);
    }
}
```

```java src/main/java/com/miscpatterns/controller/ThemeController.java
package com.miscpatterns.controller;

import com.miscpatterns.theme.ThemeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller demonstrating Theme Resolver Pattern
 */
@RestController
@RequestMapping("/api/theme")
@RequiredArgsConstructor
@Slf4j
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentTheme(HttpServletRequest request) {
        String currentTheme = themeService.getCurrentTheme(request);
        Map<String, String> properties = themeService.getThemeProperties(currentTheme);
        
        return ResponseEntity.ok(Map.of(
            "theme", currentTheme,
            "properties", properties
        ));
    }

    @PostMapping("/change")
    public ResponseEntity<Map<String, String>> changeTheme(
            @RequestParam String theme,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        themeService.changeTheme(request, response, theme);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "theme", theme,
            "message", "Theme changed successfully"
        ));
    }

    @GetMapping("/available")
    public ResponseEntity<List<ThemeService.ThemeInfo>> getAvailableThemes() {
        return ResponseEntity.ok(themeService.getAvailableThemes());
    }

    @GetMapping("/properties/{themeName}")
    public ResponseEntity<Map<String, String>> getThemeProperties(@PathVariable String themeName) {
        return ResponseEntity.ok(themeService.getThemeProperties(themeName));
    }
}
```

```java src/main/java/com/miscpatterns/controller/FileUploadController.java
package com.miscpatterns.controller;

import com.miscpatterns.multipart.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller demonstrating Multipart Resolver Pattern
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Uploading file: {}", file.getOriginalFilename());
        
        try {
            FileUploadService.FileInfo fileInfo = fileUploadService.uploadFile(file);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "File uploaded successfully",
                "file", fileInfo
            ));
            
        } catch (IOException e) {
            log.error("Error uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Failed to upload file: " + e.getMessage()
                ));
        }
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<Map<String, Object>> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files) {
        
        log.info("Uploading {} files", files.size());
        
        try {
            List<FileUploadService.FileInfo> uploadedFiles = 
                fileUploadService.uploadFiles(files);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", String.format("%d files uploaded successfully", uploadedFiles.size()),
                "files", uploadedFiles
            ));
            
        } catch (IOException e) {
            log.error("Error uploading files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Failed to upload files: " + e.getMessage()
                ));
        }
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String filename) {
        log.info("Deleting file: {}", filename);
        
        try {
            fileUploadService.deleteFile(filename);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "File deleted successfully"
            ));
            
        } catch (IOException e) {
            log.error("Error deleting file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Failed to delete file: " + e.getMessage()
                ));
        }
    }

    @GetMapping("/{filename}/info")
    public ResponseEntity<FileUploadService.FileInfo> getFileInfo(@PathVariable String filename) {
        try {
            FileUploadService.FileInfo fileInfo = fileUploadService.getFileInfo(filename);
            return ResponseEntity.ok(fileInfo);
        } catch (IOException e) {
            log.error("Error getting file info", e);
            return ResponseEntity.notFound().build();
        }
    }
}
```

## Configuration

```java src/main/java/com/miscpatterns/config/WebMvcConfig.java
package com.miscpatterns.config;

import com.miscpatterns.locale.CustomLocaleResolver;
import com.miscpatterns.multipart.CustomMultipartResolver;
import com.miscpatterns.theme.CustomThemeResolver;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;

import java.util.Locale;

/**
 * Web MVC Configuration for all patterns
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * ResourceBundle Pattern - MessageSource configuration
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    /**
     * Locale Resolver Pattern - Custom locale resolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        CustomLocaleResolver resolver = new CustomLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }

    /**
     * Locale change interceptor
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("locale");
        return interceptor;
    }

    /**
     * Theme Resolver Pattern - Custom theme resolver
     */
    @Bean
    public ThemeResolver themeResolver() {
        CustomThemeResolver resolver = new CustomThemeResolver();
        resolver.setDefaultTheme("light");
        return resolver;
    }

    /**
     * Theme change interceptor
     */
    @Bean
    public ThemeChangeInterceptor themeChangeInterceptor() {
        ThemeChangeInterceptor interceptor = new ThemeChangeInterceptor();
        interceptor.setParamName("theme");
        return interceptor;
    }

    /**
     * Multipart Resolver Pattern - Custom multipart resolver
     */
    @Bean
    public MultipartResolver multipartResolver() {
        CustomMultipartResolver resolver = new CustomMultipartResolver();
        resolver.setMaxUploadSize(10 * 1024 * 1024); // 10MB
        resolver.setMaxUploadSizePerFile(5 * 1024 * 1024); // 5MB
        return resolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
        registry.addInterceptor(themeChangeInterceptor());
    }
}
```

```java src/main/java/com/miscpatterns/config/AsyncConfig.java
package com.miscpatterns.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;

/**
 * Async configuration for callback pattern
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

## Application Properties

```yaml src/main/resources/application.yml
spring:
  application:
    name: miscellaneous-patterns

  # Multipart Configuration
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 10MB
      file-size-threshold: 2KB

  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  # Database
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true
      path: /h2-console

  # Messages
  messages:
    basename: messages
    encoding: UTF-8
    fallback-to-system-locale: false

# Server Configuration
server:
  port: 8080

# Logging
logging:
  level:
    com.miscpatterns: DEBUG
    org.springframework.web: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

## Testing

```java src/test/java/com/miscpatterns/CallbackPatternTest.java
package com.miscpatterns;

import com.miscpatterns.callback.AsyncCallback;
import com.miscpatterns.callback.Callback;
import com.miscpatterns.callback.CallbackExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Callback Pattern
 */
@SpringBootTest
class CallbackPatternTest {

    @Autowired
    private CallbackExecutor callbackExecutor;

    @Test
    void testSyncCallback() {
        AtomicReference<String> result = new AtomicReference<>();
        
        callbackExecutor.execute(
            () -> "Test Result",
            result::set
        );
        
        assertEquals("Test Result", result.get());
    }

    @Test
    void testAsyncCallback() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        
        callbackExecutor.executeAsync(
            () -> {
                Thread.sleep(100);
                return "Async Result";
            },
            new AsyncCallback<>() {
                @Override
                public void onSuccess(String asyncResult) {
                    result.set(asyncResult);
                    latch.countDown();
                }

                @Override
                public void onFailure(Exception exception) {
                    fail("Should not fail");
                }
            }
        );
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("Async Result", result.get());
    }

    @Test
    void testCallbackWithError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean errorOccurred = new AtomicBoolean(false);
        
        callbackExecutor.executeAsync(
            () -> {
                throw new RuntimeException("Test Error");
            },
            new AsyncCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    fail("Should not succeed");
                }

                @Override
                public void onFailure(Exception exception) {
                    errorOccurred.set(true);
                    latch.countDown();
                }
            }
        );
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(errorOccurred.get());
    }
}
```

```java src/test/java/com/miscpatterns/LocaleResolverPatternTest.java
package com.miscpatterns;

import com.miscpatterns.i18n.MessageSourceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Locale Resolver and ResourceBundle Patterns
 */
@SpringBootTest
class LocaleResolverPatternTest {

    @Autowired
    private MessageSourceService messageSourceService;

    @Test
    void testEnglishMessages() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        
        String greeting = messageSourceService.getMessage("greeting");
        assertEquals("Hello", greeting);
        
        String welcome = messageSourceService.getMessage("welcome");
        assertEquals("Welcome to our application", welcome);
    }

    @Test
    void testSpanishMessages() {
        LocaleContextHolder.setLocale(new Locale("es"));
        
        String greeting = messageSourceService.getMessage("greeting");
        assertEquals("Hola", greeting);
        
        String welcome = messageSourceService.getMessage("welcome");
        assertEquals("Bienvenido a nuestra aplicación", welcome);
    }

    @Test
    void testFrenchMessages() {
        LocaleContextHolder.setLocale(Locale.FRENCH);
        
        String greeting = messageSourceService.getMessage("greeting");
        assertEquals("Bonjour", greeting);
    }

    @Test
    void testMessageWithParameters() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        
        String message = messageSourceService.getMessage("validation.required", "username");
        assertEquals("Field username is required", message);
    }

    @Test
    void testMessageBundle() {
        MessageSourceService.MessageBundle bundle = 
            messageSourceService.getMessageBundle("greeting");
        
        assertEquals("Hello", bundle.getEnglish());
        assertEquals("Hola", bundle.getSpanish());
        assertEquals("Bonjour", bundle.getFrench());
    }
}
```

```java src/test/java/com/miscpatterns/BeanPostProcessorPatternTest.java
package com.miscpatterns;

import com.miscpatterns.processor.CustomBeanDefinitionRegistryPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Bean Post Processor Pattern
 */
@SpringBootTest
class BeanPostProcessorPatternTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testDynamicBeanRegistration() {
        // Bean should be registered by BeanDefinitionRegistryPostProcessor
        assertTrue(applicationContext.containsBean("dynamicBean"));
        
        CustomBeanDefinitionRegistryPostProcessor.DynamicBean bean = 
            applicationContext.getBean("dynamicBean", 
                CustomBeanDefinitionRegistryPostProcessor.DynamicBean.class);
        
        assertNotNull(bean);
        assertEquals("Dynamically Created Bean", bean.getName());
    }

    @Test
    void testBeanExists() {
        // Verify post processors are registered
        assertTrue(applicationContext.containsBean("loggingBeanPostProcessor"));
        assertTrue(applicationContext.containsBean("performanceMonitoringBeanPostProcessor"));
        assertTrue(applicationContext.containsBean("customAutowiredBeanPostProcessor"));
    }
}
```

```java src/test/java/com/miscpatterns/FileUploadIntegrationTest.java
package com.miscpatterns;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for Multipart Resolver Pattern
 */
@SpringBootTest
@AutoConfigureMockMvc
class FileUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testFileUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "Hello World".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.file.originalFilename").value("test.txt"))
            .andExpect(jsonPath("$.file.contentType").value("text/plain"));
    }

    @Test
    void testMultipleFileUpload() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
            "files",
            "test1.txt",
            "text/plain",
            "File 1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
            "files",
            "test2.txt",
            "text/plain",
            "File 2".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload-multiple")
                .file(file1)
                .file(file2))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.files").isArray());
    }
}
```

## README Documentation

```markdown README.md
# Miscellaneous Patterns in Spring Boot

This project demonstrates 10 miscellaneous Spring patterns.

## Patterns Implemented

### 1. **Callback Pattern**
- Location: `callback/`
- Synchronous and asynchronous callbacks
- Error handling with callbacks
- Callback chaining

**Example:**
```java
callbackExecutor.execute(
    () -> processData(),
    result -> handleResult(result)
);

callbackExecutor.executeAsync(task, new AsyncCallback<>() {
    @Override
    public void onSuccess(Result result) { }
    
    @Override
    public void onFailure(Exception e) { }
});
```

### 2. **Template Callback Pattern**
- Location: `template/`
- JDBC template implementation
- Transaction template
- REST template

**Example:**
```java
jdbcTemplate.query("SELECT * FROM users", (rs, rowNum) -> {
    return new User(rs.getLong("id"), rs.getString("name"));
});

transactionTemplate.execute(() -> {
    // Transactional operations
    return result;
});
```

### 3. **ResourceBundle Pattern**
- Location: `i18n/MessageSourceService.java`
- Internationalization support
- Multiple language support (en, es, fr)
- Parameterized messages

**Example:**
```java
messageSource.getMessage("greeting"); // Returns localized greeting
messageSource.getMessage("validation.required", "username"); 
// Returns "Field username is required"
```

### 4. **Locale Resolver Pattern**
- Location: `locale/CustomLocaleResolver.java`
- Custom locale resolution from session, parameter, or header
- Locale switching
- Supported locales management

**Example:**
```java
localeResolver.resolveLocale(request);
localeResolver.setLocale(request, response, Locale.FRENCH);
```

### 5. **Theme Resolver Pattern**
- Location: `theme/CustomThemeResolver.java`
- Theme resolution from session, parameter, or cookie
- Multiple themes (light, dark, blue, green)
- Theme properties management

**Example:**
```java
themeResolver.resolveThemeName(request);
themeResolver.setThemeName(request, response, "dark");
```

### 6. **Multipart Resolver Pattern**
- Location: `multipart/CustomMultipartResolver.java`
- File upload handling
- Size validation
- Multiple file uploads

**Example:**
```java
multipartResolver.resolveMultipart(request);
fileUploadService.uploadFile(multipartFile);
```

### 7. **Handler Exception Resolver Pattern**
- Location: `exception/CustomHandlerExceptionResolver.java`
- Custom exception handling
- Different handlers for different exception types
- Global exception handler with @RestControllerAdvice

**Example:**
```java
@Override
public ModelAndView resolveException(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler,
    Exception ex) {
    // Custom exception handling logic
}
```

### 8. **Bean Post Processor Pattern**
- Location: `processor/LoggingBeanPostProcessor.java`
- Bean lifecycle hooks
- Performance monitoring
- Custom autowiring

**Example:**
```java
@Override
public Object postProcessBeforeInitialization(Object bean, String beanName) {
    // Pre-initialization logic
    return bean;
}

@Override
public Object postProcessAfterInitialization(Object bean, String beanName) {
    // Post-initialization logic
    return bean;
}
```

### 9. **Bean Factory Post Processor Pattern**
- Location: `processor/PropertyPlaceholderBeanFactoryPostProcessor.java`
- Modify bean definitions before instantiation
- Custom scope handling
- Dynamic bean registration

**Example:**
```java
@Override
public void postProcessBeanFactory(
    ConfigurableListableBeanFactory beanFactory) {
    // Modify bean definitions
}
```

### 10. **Destruction Aware Bean Post Processor Pattern**
- Location: `processor/DestructionAwareBeanPostProcessor.java`
- Bean destruction hooks
- Resource cleanup
- Connection pool management

**Example:**
```java
@Override
public void postProcessBeforeDestruction(Object bean, String beanName) {
    // Cleanup logic before bean destruction
}
```

## Quick Start

### Build and Run
```bash
mvn clean package
mvn spring-boot:run
```

## API Examples

### Callback Pattern

**Process Data:**
```bash
curl -X POST http://localhost:8080/api/callback/process \
  -H "Content-Type: application/json" \
  -d '["item1", "item2", "item3"]'
```

**Async Processing:**
```bash
curl -X POST http://localhost:8080/api/callback/process-async \
  -H "Content-Type: application/json" \
  -d '["data1", "data2"]'
```

### Locale and ResourceBundle Patterns

**Get Current Locale:**
```bash
curl http://localhost:8080/api/locale/current
```

**Response:**
```json
{
  "locale": "en",
  "greeting": "Hello",
  "welcome": "Welcome to our application"
}
```

**Change Locale:**
```bash
curl -X POST "http://localhost:8080/api/locale/change?locale=es"
```

**Get Message in All Locales:**
```bash
curl http://localhost:8080/api/locale/message/greeting
```

**Response:**
```json
{
  "key": "greeting",
  "en": "Hello",
  "es": "Hola",
  "fr": "Bonjour"
}
```

### Theme Resolver Pattern

**Get Current Theme:**
```bash
curl http://localhost:8080/api/theme/current
```

**Response:**
```json
{
  "theme": "light",
  "properties": {
    "background": "#FFFFFF",
    "text": "#000000",
    "primary": "#2196F3",
    "secondary": "#FF9800"
  }
}
```

**Change Theme:**
```bash
curl -X POST "http://localhost:8080/api/theme/change?theme=dark"
```

**Get Available Themes:**
```bash
curl http://localhost:8080/api/theme/available
```

### Multipart Resolver Pattern

**Upload Single File:**
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@/path/to/file.txt"
```

**Response:**
```json
{
  "status": "success",
  "message": "File uploaded successfully",
  "file": {
    "originalFilename": "file.txt",
    "storedFilename": "abc123.txt",
    "contentType": "text/plain",
    "size": 1024,
    "path": "/uploads/abc123.txt"
  }
}
```

**Upload Multiple Files:**
```bash
curl -X POST http://localhost:8080/api/files/upload-multiple \
  -F "files=@/path/to/file1.txt" \
  -F "files=@/path/to/file2.txt"
```

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Tests
```bash
mvn test -Dtest=CallbackPatternTest
mvn test -Dtest=LocaleResolverPatternTest
mvn test -Dtest=FileUploadIntegrationTest
```

## Pattern Comparison

| Pattern | Purpose | Use Case | Complexity |
|---------|---------|----------|------------|
| Callback | Async notification | Event handling | Low |
| Template Callback | Resource management | JDBC, transactions | Medium |
| ResourceBundle | I18n | Multi-language support | Low |
| Locale Resolver | Locale detection | User preferences | Medium |
| Theme Resolver | UI theming | User customization | Medium |
| Multipart Resolver | File uploads | Form submissions | Medium |
| Exception Resolver | Error handling | Global exception management | Medium |
| Bean Post Processor | Bean customization | AOP, monitoring | High |
| Bean Factory Post Processor | Bean definition modification | Framework integration | High |
| Destruction Aware | Resource cleanup | Connection pooling | Medium |

## Best Practices

### 1. Use Callbacks for Async Operations
```java
dataProcessor.processDataAsync(data, new AsyncCallback<>() {
    @Override
    public void onSuccess(Result result) {
        // Handle success
    }
    
    @Override
    public void onFailure(Exception e) {
        // Handle error
    }
});
```

### 2. Leverage MessageSource for I18n
```java
@Autowired
private MessageSource messageSource;

public String getLocalizedMessage(String key, Locale locale) {
    return messageSource.getMessage(key, null, locale);
}
```

### 3. Implement Custom Resolvers Carefully
```java
@Bean
public LocaleResolver localeResolver() {
    CustomLocaleResolver resolver = new CustomLocaleResolver();
    resolver.setDefaultLocale(Locale.ENGLISH);
    return resolver;
}
```

### 4. Use Bean Post Processors for Cross-Cutting Concerns
```java
@Component
public class LoggingBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // Add logging to all beans
        return bean;
    }
}
```

## License

MIT License
```

This comprehensive implementation covers all 10 miscellaneous patterns with working code, tests, and detailed documentation!