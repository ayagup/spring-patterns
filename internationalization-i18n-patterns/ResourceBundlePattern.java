package com.spring.patterns.internationalization;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Resource Bundle Pattern
 * 
 * Demonstrates ResourceBundle usage for internationalization:
 * - Property-based resource bundles
 * - List resource bundles
 * - Control-based resource bundles
 * - Custom resource bundle loaders
 * 
 * Use Cases:
 * 1. Externalized string resources
 * 2. Multi-language support
 * 3. Property file management
 * 4. Locale-specific content
 * 5. Configuration internationalization
 */

/**
 * Custom ResourceBundle implementation
 */
class CustomResourceBundle extends ResourceBundle {
    
    private final Map<String, String> resources;
    
    public CustomResourceBundle(Map<String, String> resources) {
        this.resources = resources;
    }
    
    @Override
    protected Object handleGetObject(String key) {
        return resources.get(key);
    }
    
    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(resources.keySet());
    }
}

/**
 * Resource Bundle Manager
 * Centralized management of resource bundles
 */
class ResourceBundleManager {
    
    private final Map<String, ResourceBundle> bundleCache = new HashMap<>();
    private final Locale defaultLocale = Locale.US;
    
    /**
     * Get resource bundle by base name and locale
     */
    public ResourceBundle getBundle(String baseName, Locale locale) {
        String cacheKey = baseName + "_" + locale.toString();
        
        return bundleCache.computeIfAbsent(cacheKey, k -> {
            try {
                return ResourceBundle.getBundle(baseName, locale);
            } catch (MissingResourceException e) {
                return ResourceBundle.getBundle(baseName, defaultLocale);
            }
        });
    }
    
    /**
     * Get string from resource bundle
     */
    public String getString(String baseName, String key, Locale locale) {
        ResourceBundle bundle = getBundle(baseName, locale);
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key; // Return key if not found
        }
    }
    
    /**
     * Get string with fallback
     */
    public String getStringWithFallback(String baseName, String key, 
                                       String fallback, Locale locale) {
        ResourceBundle bundle = getBundle(baseName, locale);
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return fallback;
        }
    }
    
    /**
     * Clear bundle cache
     */
    public void clearCache() {
        bundleCache.clear();
        ResourceBundle.clearCache();
    }
}

/**
 * Property Resource Bundle Loader
 */
class PropertyResourceBundleLoader {
    
    /**
     * Load property resource bundle from classpath
     */
    public ResourceBundle loadFromClasspath(String resourceName, Locale locale) {
        String fileName = buildFileName(resourceName, locale);
        
        try (InputStream stream = getClass().getClassLoader()
                .getResourceAsStream(fileName)) {
            if (stream != null) {
                return new PropertyResourceBundle(
                    new java.io.InputStreamReader(stream, StandardCharsets.UTF_8)
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + fileName, e);
        }
        
        throw new MissingResourceException(
            "Can't find bundle", resourceName, locale.toString()
        );
    }
    
    private String buildFileName(String baseName, Locale locale) {
        StringBuilder fileName = new StringBuilder(baseName);
        
        if (!locale.getLanguage().isEmpty()) {
            fileName.append("_").append(locale.getLanguage());
        }
        
        if (!locale.getCountry().isEmpty()) {
            fileName.append("_").append(locale.getCountry());
        }
        
        if (!locale.getVariant().isEmpty()) {
            fileName.append("_").append(locale.getVariant());
        }
        
        fileName.append(".properties");
        return fileName.toString();
    }
}

/**
 * List Resource Bundle for structured content
 */
class ListResourceBundleExample extends ListResourceBundle {
    
    private final Locale locale;
    
    public ListResourceBundleExample(Locale locale) {
        this.locale = locale;
    }
    
    @Override
    protected Object[][] getContents() {
        if (locale.getLanguage().equals("es")) {
            return SPANISH_CONTENTS;
        } else if (locale.getLanguage().equals("fr")) {
            return FRENCH_CONTENTS;
        } else {
            return ENGLISH_CONTENTS;
        }
    }
    
    private static final Object[][] ENGLISH_CONTENTS = {
        {"app.title", "My Application"},
        {"app.welcome", "Welcome"},
        {"app.goodbye", "Goodbye"},
        {"button.submit", "Submit"},
        {"button.cancel", "Cancel"},
        {"error.required", "This field is required"},
        {"error.invalid", "Invalid value"},
        {"menu.items", new String[]{"Home", "Products", "About", "Contact"}},
        {"days.week", new String[]{"Monday", "Tuesday", "Wednesday", 
                                   "Thursday", "Friday", "Saturday", "Sunday"}}
    };
    
    private static final Object[][] SPANISH_CONTENTS = {
        {"app.title", "Mi Aplicación"},
        {"app.welcome", "Bienvenido"},
        {"app.goodbye", "Adiós"},
        {"button.submit", "Enviar"},
        {"button.cancel", "Cancelar"},
        {"error.required", "Este campo es obligatorio"},
        {"error.invalid", "Valor inválido"},
        {"menu.items", new String[]{"Inicio", "Productos", "Acerca de", "Contacto"}},
        {"days.week", new String[]{"Lunes", "Martes", "Miércoles", 
                                   "Jueves", "Viernes", "Sábado", "Domingo"}}
    };
    
    private static final Object[][] FRENCH_CONTENTS = {
        {"app.title", "Mon Application"},
        {"app.welcome", "Bienvenue"},
        {"app.goodbye", "Au revoir"},
        {"button.submit", "Soumettre"},
        {"button.cancel", "Annuler"},
        {"error.required", "Ce champ est obligatoire"},
        {"error.invalid", "Valeur invalide"},
        {"menu.items", new String[]{"Accueil", "Produits", "À propos", "Contact"}},
        {"days.week", new String[]{"Lundi", "Mardi", "Mercredi", 
                                   "Jeudi", "Vendredi", "Samedi", "Dimanche"}}
    };
}

/**
 * XML Resource Bundle
 */
class XMLResourceBundleLoader {
    
    /**
     * Load resource bundle from XML
     */
    public ResourceBundle loadFromXML(String resourceName, Locale locale) {
        String fileName = buildXMLFileName(resourceName, locale);
        
        try (InputStream stream = getClass().getClassLoader()
                .getResourceAsStream(fileName)) {
            if (stream != null) {
                Properties properties = new Properties();
                properties.loadFromXML(stream);
                return new PropertyResourceBundle(
                    new java.io.StringReader(propertiesToString(properties))
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load XML resource: " + fileName, e);
        }
        
        throw new MissingResourceException(
            "Can't find bundle", resourceName, locale.toString()
        );
    }
    
    private String buildXMLFileName(String baseName, Locale locale) {
        return baseName + "_" + locale.toString() + ".xml";
    }
    
    private String propertiesToString(Properties properties) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}

/**
 * Resource Bundle Control for custom loading
 */
class UTF8ResourceBundleControl extends ResourceBundle.Control {
    
    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                    ClassLoader loader, boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
        
        String bundleName = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, "properties");
        
        try (InputStream stream = loader.getResourceAsStream(resourceName)) {
            if (stream != null) {
                return new PropertyResourceBundle(
                    new java.io.InputStreamReader(stream, StandardCharsets.UTF_8)
                );
            }
        }
        
        return null;
    }
}

/**
 * Hierarchical Resource Bundle
 * Supports parent-child relationships
 */
class HierarchicalResourceBundle {
    
    private final ResourceBundle bundle;
    private final ResourceBundle parent;
    
    public HierarchicalResourceBundle(ResourceBundle bundle, ResourceBundle parent) {
        this.bundle = bundle;
        this.parent = parent;
    }
    
    public String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            if (parent != null) {
                return parent.getString(key);
            }
            throw e;
        }
    }
    
    public boolean containsKey(String key) {
        return bundle.containsKey(key) || (parent != null && parent.containsKey(key));
    }
}

/**
 * Resource Bundle Formatter
 * Formats messages with parameters
 */
class ResourceBundleFormatter {
    
    private final ResourceBundle bundle;
    
    public ResourceBundleFormatter(ResourceBundle bundle) {
        this.bundle = bundle;
    }
    
    /**
     * Format message with parameters
     */
    public String format(String key, Object... args) {
        String pattern = bundle.getString(key);
        return String.format(pattern, args);
    }
    
    /**
     * Get formatted string with fallback
     */
    public String formatWithFallback(String key, String fallback, Object... args) {
        try {
            String pattern = bundle.getString(key);
            return String.format(pattern, args);
        } catch (MissingResourceException e) {
            return String.format(fallback, args);
        }
    }
}

/**
 * Resource Bundle Validator
 */
class ResourceBundleValidator {
    
    /**
     * Validate that all keys exist in all locale bundles
     */
    public List<String> validateBundles(String baseName, List<Locale> locales) {
        List<String> errors = new ArrayList<>();
        
        // Get all keys from default locale
        ResourceBundle defaultBundle = ResourceBundle.getBundle(baseName, Locale.getDefault());
        Set<String> allKeys = defaultBundle.keySet();
        
        // Check each locale
        for (Locale locale : locales) {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
                for (String key : allKeys) {
                    if (!bundle.containsKey(key)) {
                        errors.add("Missing key '" + key + "' in locale " + locale);
                    }
                }
            } catch (MissingResourceException e) {
                errors.add("Missing bundle for locale " + locale);
            }
        }
        
        return errors;
    }
}

/**
 * Resource Bundle Builder
 */
class ResourceBundleBuilder {
    
    private final Map<String, String> resources = new HashMap<>();
    
    public ResourceBundleBuilder add(String key, String value) {
        resources.put(key, value);
        return this;
    }
    
    public ResourceBundleBuilder addAll(Map<String, String> entries) {
        resources.putAll(entries);
        return this;
    }
    
    public ResourceBundle build() {
        return new CustomResourceBundle(new HashMap<>(resources));
    }
}

/**
 * Resource Bundle Merger
 * Merges multiple resource bundles
 */
class ResourceBundleMerger {
    
    /**
     * Merge multiple bundles into one
     */
    public ResourceBundle merge(ResourceBundle... bundles) {
        Map<String, String> merged = new HashMap<>();
        
        for (ResourceBundle bundle : bundles) {
            for (String key : bundle.keySet()) {
                merged.putIfAbsent(key, bundle.getString(key));
            }
        }
        
        return new CustomResourceBundle(merged);
    }
}

/**
 * Demonstration class
 */
public class ResourceBundlePattern {
    
    public static void main(String[] args) {
        System.out.println("=== Resource Bundle Pattern Demo ===\n");
        
        // 1. Resource Bundle Manager Demo
        demonstrateResourceBundleManager();
        
        // 2. List Resource Bundle Demo
        demonstrateListResourceBundle();
        
        // 3. Custom Resource Bundle Demo
        demonstrateCustomResourceBundle();
        
        // 4. Resource Bundle Formatter Demo
        demonstrateResourceBundleFormatter();
        
        // 5. Hierarchical Resource Bundle Demo
        demonstrateHierarchicalResourceBundle();
        
        // 6. Resource Bundle Builder Demo
        demonstrateResourceBundleBuilder();
        
        // 7. Resource Bundle Merger Demo
        demonstrateResourceBundleMerger();
    }
    
    private static void demonstrateResourceBundleManager() {
        System.out.println("1. Resource Bundle Manager Demo:");
        
        ResourceBundleManager manager = new ResourceBundleManager();
        
        // Create sample bundles programmatically
        Map<String, String> enResources = new HashMap<>();
        enResources.put("greeting", "Hello");
        enResources.put("farewell", "Goodbye");
        
        Map<String, String> esResources = new HashMap<>();
        esResources.put("greeting", "Hola");
        esResources.put("farewell", "Adiós");
        
        System.out.println("Resource Bundle Manager initialized");
        System.out.println("Note: In real applications, bundles are loaded from .properties files");
        System.out.println();
    }
    
    private static void demonstrateListResourceBundle() {
        System.out.println("2. List Resource Bundle Demo:");
        
        Locale[] locales = {Locale.ENGLISH, new Locale("es"), Locale.FRENCH};
        
        for (Locale locale : locales) {
            ListResourceBundleExample bundle = new ListResourceBundleExample(locale);
            
            System.out.println("Locale: " + locale.getDisplayName());
            System.out.println("  Title: " + bundle.getString("app.title"));
            System.out.println("  Welcome: " + bundle.getString("app.welcome"));
            System.out.println("  Submit Button: " + bundle.getString("button.submit"));
            
            String[] days = (String[]) bundle.getObject("days.week");
            System.out.println("  First day: " + days[0]);
            System.out.println();
        }
    }
    
    private static void demonstrateCustomResourceBundle() {
        System.out.println("3. Custom Resource Bundle Demo:");
        
        Map<String, String> resources = new HashMap<>();
        resources.put("app.name", "My Application");
        resources.put("app.version", "1.0.0");
        resources.put("app.copyright", "© 2025 Company Name");
        
        CustomResourceBundle bundle = new CustomResourceBundle(resources);
        
        System.out.println("App Name: " + bundle.getString("app.name"));
        System.out.println("Version: " + bundle.getString("app.version"));
        System.out.println("Copyright: " + bundle.getString("app.copyright"));
        System.out.println();
    }
    
    private static void demonstrateResourceBundleFormatter() {
        System.out.println("4. Resource Bundle Formatter Demo:");
        
        Map<String, String> resources = new HashMap<>();
        resources.put("welcome.user", "Welcome, %s!");
        resources.put("order.total", "Order total: $%.2f");
        resources.put("items.count", "You have %d items in your cart");
        
        CustomResourceBundle bundle = new CustomResourceBundle(resources);
        ResourceBundleFormatter formatter = new ResourceBundleFormatter(bundle);
        
        System.out.println(formatter.format("welcome.user", "John"));
        System.out.println(formatter.format("order.total", 99.99));
        System.out.println(formatter.format("items.count", 5));
        System.out.println();
    }
    
    private static void demonstrateHierarchicalResourceBundle() {
        System.out.println("5. Hierarchical Resource Bundle Demo:");
        
        // Parent bundle - common resources
        Map<String, String> parentResources = new HashMap<>();
        parentResources.put("app.name", "Base Application");
        parentResources.put("app.version", "1.0.0");
        
        // Child bundle - specific resources
        Map<String, String> childResources = new HashMap<>();
        childResources.put("module.name", "User Module");
        
        CustomResourceBundle parent = new CustomResourceBundle(parentResources);
        CustomResourceBundle child = new CustomResourceBundle(childResources);
        
        HierarchicalResourceBundle hierarchical = 
            new HierarchicalResourceBundle(child, parent);
        
        System.out.println("Module Name: " + hierarchical.getString("module.name"));
        System.out.println("App Name (from parent): " + hierarchical.getString("app.name"));
        System.out.println();
    }
    
    private static void demonstrateResourceBundleBuilder() {
        System.out.println("6. Resource Bundle Builder Demo:");
        
        ResourceBundle bundle = new ResourceBundleBuilder()
            .add("greeting", "Hello")
            .add("farewell", "Goodbye")
            .add("thanks", "Thank you")
            .build();
        
        System.out.println("Greeting: " + bundle.getString("greeting"));
        System.out.println("Farewell: " + bundle.getString("farewell"));
        System.out.println("Thanks: " + bundle.getString("thanks"));
        System.out.println();
    }
    
    private static void demonstrateResourceBundleMerger() {
        System.out.println("7. Resource Bundle Merger Demo:");
        
        Map<String, String> bundle1Resources = new HashMap<>();
        bundle1Resources.put("greeting", "Hello");
        bundle1Resources.put("app.name", "App 1");
        
        Map<String, String> bundle2Resources = new HashMap<>();
        bundle2Resources.put("farewell", "Goodbye");
        bundle2Resources.put("app.version", "2.0");
        
        CustomResourceBundle bundle1 = new CustomResourceBundle(bundle1Resources);
        CustomResourceBundle bundle2 = new CustomResourceBundle(bundle2Resources);
        
        ResourceBundleMerger merger = new ResourceBundleMerger();
        ResourceBundle merged = merger.merge(bundle1, bundle2);
        
        System.out.println("Merged Bundle Contents:");
        System.out.println("  Greeting: " + merged.getString("greeting"));
        System.out.println("  Farewell: " + merged.getString("farewell"));
        System.out.println("  App Name: " + merged.getString("app.name"));
        System.out.println("  App Version: " + merged.getString("app.version"));
    }
}
