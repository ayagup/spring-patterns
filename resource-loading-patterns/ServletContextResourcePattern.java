package com.example.resource;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servlet Context Resource Pattern
 * =================================
 * 
 * Demonstrates ServletContextResource for loading resources from
 * the web application's ServletContext (WEB-INF, webapp root, etc.).
 * 
 * Key Concepts:
 * ------------
 * 1. ServletContextResource - Web application resource access
 * 2. ServletContext - Web app context integration
 * 3. WEB-INF Access - Protected resources
 * 4. Context Path - Relative to webapp root
 * 5. ServletContextAware - Automatic injection
 * 
 * Path Formats:
 * ------------
 * - /WEB-INF/config.properties - Protected area
 * - /WEB-INF/classes/data.xml - Class path within web app
 * - /static/images/logo.png - Public static resources
 * - /templates/email.html - Template files
 * 
 * When to Use:
 * -----------
 * - Web application resources
 * - Access WEB-INF contents
 * - Servlet-specific resources
 * - Webapp configuration files
 * - JSP/template files
 * - Static web resources
 * 
 * Advantages:
 * ----------
 * - Access to WEB-INF (protected)
 * - ServletContext integration
 * - Webapp-relative paths
 * - Works in servlet containers
 * - Security through WEB-INF
 * 
 * Best Practices:
 * --------------
 * - Store configs in WEB-INF for security
 * - Use absolute paths from webapp root
 * - Paths start with /
 * - Check resource existence
 * - Close streams properly
 * - Cache loaded resources
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@Component
public class ServletContextResourcePattern implements ServletContextAware {
    
    private ServletContext servletContext;
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /**
     * Load resource from WEB-INF
     */
    public String loadFromWebInf(String path) throws IOException {
        String fullPath = "/WEB-INF/" + path;
        return loadResource(fullPath);
    }
    
    /**
     * Load resource from webapp root
     */
    public String loadFromWebapp(String path) throws IOException {
        String fullPath = path.startsWith("/") ? path : "/" + path;
        return loadResource(fullPath);
    }
    
    /**
     * Load resource using ServletContext
     */
    public String loadResource(String path) throws IOException {
        InputStream is = servletContext.getResourceAsStream(path);
        
        if (is == null) {
            throw new IOException("Resource not found: " + path);
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    /**
     * Get resource real path
     */
    public String getRealPath(String path) {
        return servletContext.getRealPath(path);
    }
}

/**
 * Example 2: Web Configuration Loader
 */
@Component
class WebConfigurationLoader implements ServletContextAware {
    
    private ServletContext servletContext;
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /**
     * Load application configuration
     */
    public java.util.Properties loadConfig() throws IOException {
        InputStream is = servletContext.getResourceAsStream("/WEB-INF/config/application.properties");
        
        if (is == null) {
            throw new IOException("Configuration not found");
        }
        
        java.util.Properties props = new java.util.Properties();
        props.load(is);
        
        return props;
    }
    
    /**
     * Load database configuration
     */
    public java.util.Properties loadDatabaseConfig() throws IOException {
        InputStream is = servletContext.getResourceAsStream("/WEB-INF/config/database.properties");
        
        if (is == null) {
            throw new IOException("Database configuration not found");
        }
        
        java.util.Properties props = new java.util.Properties();
        props.load(is);
        
        return props;
    }
    
    /**
     * Load JSON configuration
     */
    public String loadJsonConfig(String filename) throws IOException {
        String path = "/WEB-INF/config/" + filename;
        InputStream is = servletContext.getResourceAsStream(path);
        
        if (is == null) {
            throw new IOException("JSON config not found: " + filename);
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}

/**
 * Example 3: Template Loader
 */
@Component
class WebTemplateLoader implements ServletContextAware {
    
    private ServletContext servletContext;
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /**
     * Load JSP template
     */
    public String loadJspTemplate(String name) throws IOException {
        String path = "/WEB-INF/jsp/" + name + ".jsp";
        return loadTemplate(path);
    }
    
    /**
     * Load HTML template
     */
    public String loadHtmlTemplate(String name) throws IOException {
        String path = "/WEB-INF/templates/" + name + ".html";
        return loadTemplate(path);
    }
    
    /**
     * Load email template
     */
    public String loadEmailTemplate(String name) throws IOException {
        String path = "/WEB-INF/templates/email/" + name + ".html";
        return loadTemplate(path);
    }
    
    private String loadTemplate(String path) throws IOException {
        InputStream is = servletContext.getResourceAsStream(path);
        
        if (is == null) {
            throw new IOException("Template not found: " + path);
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}

/**
 * Example 4: Static Resource Locator
 */
@Component
class StaticResourceLocator implements ServletContextAware {
    
    private ServletContext servletContext;
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /**
     * Get CSS file path
     */
    public String getCssPath(String filename) {
        return servletContext.getRealPath("/static/css/" + filename);
    }
    
    /**
     * Get JavaScript file path
     */
    public String getJsPath(String filename) {
        return servletContext.getRealPath("/static/js/" + filename);
    }
    
    /**
     * Get image path
     */
    public String getImagePath(String filename) {
        return servletContext.getRealPath("/static/images/" + filename);
    }
    
    /**
     * Check if static resource exists
     */
    public boolean staticResourceExists(String path) {
        String realPath = servletContext.getRealPath(path);
        return realPath != null && new java.io.File(realPath).exists();
    }
}

/**
 * Example 5: Web Resource Lister
 */
@Component
class WebResourceLister implements ServletContextAware {
    
    private ServletContext servletContext;
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /**
     * List resources in directory
     */
    public Set<String> listResources(String path) {
        String fullPath = path.startsWith("/") ? path : "/" + path;
        return servletContext.getResourcePaths(fullPath);
    }
    
    /**
     * List WEB-INF resources
     */
    public Set<String> listWebInfResources(String subPath) {
        String path = "/WEB-INF/" + subPath;
        return servletContext.getResourcePaths(path);
    }
    
    /**
     * List all configuration files
     */
    public Set<String> listConfigFiles() {
        return servletContext.getResourcePaths("/WEB-INF/config/");
    }
    
    /**
     * List all templates
     */
    public Set<String> listTemplates() {
        return servletContext.getResourcePaths("/WEB-INF/templates/");
    }
}

/**
 * Example 6: ServletContext Info Provider
 */
@Component
class ServletContextInfoProvider implements ServletContextAware {
    
    private ServletContext servletContext;
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /**
     * Get context path
     */
    public String getContextPath() {
        return servletContext.getContextPath();
    }
    
    /**
     * Get server info
     */
    public String getServerInfo() {
        return servletContext.getServerInfo();
    }
    
    /**
     * Get servlet version
     */
    public String getServletVersion() {
        return servletContext.getMajorVersion() + "." + 
               servletContext.getMinorVersion();
    }
    
    /**
     * Get context init parameter
     */
    public String getInitParameter(String name) {
        return servletContext.getInitParameter(name);
    }
    
    /**
     * Get all init parameter names
     */
    public java.util.Enumeration<String> getInitParameterNames() {
        return servletContext.getInitParameterNames();
    }
}

/**
 * Example 7: Resource Metadata Provider
 */
@Component
class WebResourceMetadataProvider implements ServletContextAware {
    
    private ServletContext servletContext;
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /**
     * Get resource metadata
     */
    public WebResourceMetadata getMetadata(String path) throws IOException {
        String realPath = servletContext.getRealPath(path);
        
        if (realPath == null) {
            throw new IOException("Resource not found: " + path);
        }
        
        java.io.File file = new java.io.File(realPath);
        
        return new WebResourceMetadata(
            path,
            realPath,
            file.exists(),
            file.isFile(),
            file.length(),
            file.lastModified()
        );
    }
    
    /**
     * Get MIME type
     */
    public String getMimeType(String path) {
        return servletContext.getMimeType(path);
    }
}

/**
 * Web resource metadata
 */
class WebResourceMetadata {
    private final String path;
    private final String realPath;
    private final boolean exists;
    private final boolean isFile;
    private final long size;
    private final long lastModified;
    
    public WebResourceMetadata(String path, String realPath, boolean exists,
                              boolean isFile, long size, long lastModified) {
        this.path = path;
        this.realPath = realPath;
        this.exists = exists;
        this.isFile = isFile;
        this.size = size;
        this.lastModified = lastModified;
    }
    
    @Override
    public String toString() {
        return String.format("WebResourceMetadata{path='%s', realPath='%s', exists=%s, " +
                           "isFile=%s, size=%d, lastModified=%d}",
                           path, realPath, exists, isFile, size, lastModified);
    }
}

/**
 * Usage Examples
 */
class ServletContextResourceUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("ServletContext Resource Pattern Demonstration");
        System.out.println("============================================\n");
        
        System.out.println("Resource Paths:");
        System.out.println("- /WEB-INF/config/application.properties (protected)");
        System.out.println("- /WEB-INF/classes/data.xml (classpath in webapp)");
        System.out.println("- /WEB-INF/templates/email/welcome.html (template)");
        System.out.println("- /static/css/styles.css (public resource)");
        System.out.println("- /static/images/logo.png (public image)");
        
        System.out.println("\nAdvantages:");
        System.out.println("1. Access to WEB-INF (security)");
        System.out.println("2. ServletContext integration");
        System.out.println("3. Webapp-relative paths");
        System.out.println("4. Works in all servlet containers");
        System.out.println("5. MIME type detection");
        
        System.out.println("\nCommon Use Cases:");
        System.out.println("- Load protected configuration from WEB-INF");
        System.out.println("- Access JSP/template files");
        System.out.println("- Locate static web resources");
        System.out.println("- List available resources");
        System.out.println("- Get servlet context information");
    }
}
