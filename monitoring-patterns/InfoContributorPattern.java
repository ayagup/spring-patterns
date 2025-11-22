package com.example.monitoring.info;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Info Contributor Pattern - Demonstrates Custom Application Info Contributors
 * 
 * This pattern shows how to:
 * 1. Create custom InfoContributor implementations
 * 2. Add build information to /actuator/info
 * 3. Add Git information to /actuator/info
 * 4. Add environment information
 * 5. Add team and contact information
 * 6. Add feature flags and capabilities
 * 7. Add runtime statistics
 * 8. Add external service dependencies
 * 9. Add custom business information
 * 10. Order and organize info contributors
 * 
 * Key Concepts:
 * - InfoContributor: Interface for adding custom info
 * - Info.Builder: Builder for constructing info response
 * - /actuator/info: Endpoint for application metadata
 * 
 * Configuration:
 * management.info.env.enabled=true
 * management.info.build.enabled=true
 * management.info.git.enabled=true
 * management.info.git.mode=full
 * 
 * Access:
 * GET http://localhost:8080/actuator/info
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class InfoContributorPattern {

    public static void main(String[] args) {
        SpringApplication.run(InfoContributorPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("INFO CONTRIBUTOR PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateInfoContributors();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ACTUATOR INFO ENDPOINT");
        System.out.println("=".repeat(80));
        System.out.println("\nAccess application info at:");
        System.out.println("GET http://localhost:8080/actuator/info");
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("INFO CONTRIBUTORS REGISTERED");
        System.out.println("=".repeat(80));
        System.out.println("\n1. Build Info Contributor");
        System.out.println("   - Application name, version, group");
        System.out.println("   - Build time and artifacts");
        
        System.out.println("\n2. Git Info Contributor");
        System.out.println("   - Git branch, commit, tags");
        System.out.println("   - Commit author and message");
        
        System.out.println("\n3. Environment Info Contributor");
        System.out.println("   - Active profiles");
        System.out.println("   - JDK version, OS information");
        
        System.out.println("\n4. Team Info Contributor");
        System.out.println("   - Development team details");
        System.out.println("   - Contact information");
        
        System.out.println("\n5. Features Info Contributor");
        System.out.println("   - Enabled features and capabilities");
        System.out.println("   - API versions");
        
        System.out.println("\n6. Dependencies Info Contributor");
        System.out.println("   - External service dependencies");
        System.out.println("   - Database, cache, messaging");
        
        System.out.println("\n7. Runtime Info Contributor");
        System.out.println("   - Application startup time");
        System.out.println("   - Runtime statistics");
        
        System.out.println("\n8. Business Info Contributor");
        System.out.println("   - Business domain information");
        System.out.println("   - Custom business metrics");
        
        System.out.println("\nApplication is running. Access /actuator/info for complete information.");
        System.out.println("Press Ctrl+C to stop.\n");
    }
    
    private static void demonstrateInfoContributors() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("INFO CONTRIBUTOR OVERVIEW");
        System.out.println("=".repeat(80));
        
        System.out.println("\nPurpose:");
        System.out.println("- Provide application metadata to clients");
        System.out.println("- Document API versions and capabilities");
        System.out.println("- Share contact and support information");
        System.out.println("- Display build and deployment details");
        
        System.out.println("\nBenefits:");
        System.out.println("- Self-documenting applications");
        System.out.println("- Easier troubleshooting and debugging");
        System.out.println("- Better service discovery");
        System.out.println("- Standardized metadata format");
        
        System.out.println("\nCommon Use Cases:");
        System.out.println("- API documentation and versioning");
        System.out.println("- Build tracking and CI/CD integration");
        System.out.println("- Feature flag discovery");
        System.out.println("- Dependency and compatibility information");
    }
}

/**
 * Build information contributor
 */
@Component
class BuildInfoContributor implements InfoContributor {
    
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> buildInfo = new HashMap<>();
        buildInfo.put("name", "Spring Monitoring Application");
        buildInfo.put("description", "Comprehensive monitoring and management demonstration");
        buildInfo.put("version", "2.0.0");
        buildInfo.put("group", "com.example.monitoring");
        buildInfo.put("artifact", "monitoring-patterns");
        buildInfo.put("buildTime", "2024-03-15T10:30:00Z");
        buildInfo.put("buildNumber", "1234");
        buildInfo.put("buildBy", "Jenkins");
        
        builder.withDetail("build", buildInfo);
    }
}

/**
 * Git information contributor
 */
@Component
class GitInfoContributor implements InfoContributor {
    
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> gitInfo = new HashMap<>();
        
        // Branch information
        gitInfo.put("branch", "main");
        gitInfo.put("tags", "v2.0.0");
        
        // Commit information
        Map<String, Object> commit = new HashMap<>();
        commit.put("id", "a1b2c3d4e5f6g7h8i9j0");
        commit.put("shortId", "a1b2c3d");
        commit.put("message", "Implement monitoring patterns");
        
        Map<String, Object> author = new HashMap<>();
        author.put("name", "John Doe");
        author.put("email", "john.doe@example.com");
        author.put("time", "2024-03-15T10:00:00Z");
        commit.put("author", author);
        
        Map<String, Object> committer = new HashMap<>();
        committer.put("name", "Jane Smith");
        committer.put("email", "jane.smith@example.com");
        committer.put("time", "2024-03-15T10:15:00Z");
        commit.put("committer", committer);
        
        gitInfo.put("commit", commit);
        
        // Remote information
        gitInfo.put("remote", "https://github.com/example/monitoring-patterns.git");
        
        builder.withDetail("git", gitInfo);
    }
}

/**
 * Environment information contributor
 */
@Component
class EnvironmentInfoContributor implements InfoContributor {
    
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> envInfo = new HashMap<>();
        
        // Active profiles
        envInfo.put("activeProfiles", Arrays.asList("dev", "monitoring"));
        envInfo.put("defaultProfile", "default");
        
        // Java information
        Map<String, Object> javaInfo = new HashMap<>();
        javaInfo.put("version", System.getProperty("java.version"));
        javaInfo.put("vendor", System.getProperty("java.vendor"));
        javaInfo.put("runtime", System.getProperty("java.runtime.name"));
        javaInfo.put("vm", System.getProperty("java.vm.name"));
        envInfo.put("java", javaInfo);
        
        // OS information
        Map<String, Object> osInfo = new HashMap<>();
        osInfo.put("name", System.getProperty("os.name"));
        osInfo.put("version", System.getProperty("os.version"));
        osInfo.put("arch", System.getProperty("os.arch"));
        envInfo.put("os", osInfo);
        
        // Server information
        envInfo.put("hostname", getHostname());
        envInfo.put("serverPort", 8080);
        envInfo.put("contextPath", "/");
        
        builder.withDetail("environment", envInfo);
    }
    
    private String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}

/**
 * Team and contact information contributor
 */
@Component
class TeamInfoContributor implements InfoContributor {
    
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> teamInfo = new HashMap<>();
        
        teamInfo.put("name", "Platform Engineering Team");
        teamInfo.put("department", "Technology");
        
        // Team members
        List<Map<String, String>> members = new ArrayList<>();
        members.add(createMember("Alice Johnson", "Tech Lead", "alice@example.com"));
        members.add(createMember("Bob Smith", "Senior Developer", "bob@example.com"));
        members.add(createMember("Carol White", "DevOps Engineer", "carol@example.com"));
        teamInfo.put("members", members);
        
        // Contact information
        Map<String, Object> contact = new HashMap<>();
        contact.put("email", "platform-team@example.com");
        contact.put("slack", "#platform-engineering");
        contact.put("jira", "https://jira.example.com/browse/PLATFORM");
        contact.put("wiki", "https://wiki.example.com/platform");
        teamInfo.put("contact", contact);
        
        // Support information
        Map<String, Object> support = new HashMap<>();
        support.put("email", "support@example.com");
        support.put("phone", "+1-555-0123");
        support.put("hours", "24/7");
        support.put("sla", "99.9% uptime");
        teamInfo.put("support", support);
        
        builder.withDetail("team", teamInfo);
    }
    
    private Map<String, String> createMember(String name, String role, String email) {
        Map<String, String> member = new HashMap<>();
        member.put("name", name);
        member.put("role", role);
        member.put("email", email);
        return member;
    }
}

/**
 * Features and capabilities information contributor
 */
@Component
class FeaturesInfoContributor implements InfoContributor {
    
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> featuresInfo = new HashMap<>();
        
        // API versions
        Map<String, Object> api = new HashMap<>();
        api.put("current", "v2");
        api.put("supported", Arrays.asList("v1", "v2"));
        api.put("deprecated", Arrays.asList("v0"));
        featuresInfo.put("api", api);
        
        // Enabled features
        Map<String, Boolean> enabledFeatures = new HashMap<>();
        enabledFeatures.put("authentication", true);
        enabledFeatures.put("authorization", true);
        enabledFeatures.put("caching", true);
        enabledFeatures.put("rateLimit", true);
        enabledFeatures.put("metrics", true);
        enabledFeatures.put("tracing", true);
        enabledFeatures.put("logging", true);
        enabledFeatures.put("darkMode", false);
        enabledFeatures.put("betaFeatures", false);
        featuresInfo.put("features", enabledFeatures);
        
        // Capabilities
        List<String> capabilities = new ArrayList<>();
        capabilities.add("RESTful API");
        capabilities.add("WebSocket Support");
        capabilities.add("GraphQL API");
        capabilities.add("OAuth2 Authentication");
        capabilities.add("Rate Limiting");
        capabilities.add("Caching");
        capabilities.add("Distributed Tracing");
        featuresInfo.put("capabilities", capabilities);
        
        // Limits
        Map<String, Object> limits = new HashMap<>();
        limits.put("maxRequestSize", "10MB");
        limits.put("maxResponseSize", "50MB");
        limits.put("rateLimit", "1000 requests/hour");
        limits.put("maxConnections", 100);
        featuresInfo.put("limits", limits);
        
        builder.withDetail("features", featuresInfo);
    }
}

/**
 * Dependencies information contributor
 */
@Component
class DependenciesInfoContributor implements InfoContributor {
    
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> dependenciesInfo = new HashMap<>();
        
        // Database dependencies
        List<Map<String, Object>> databases = new ArrayList<>();
        databases.add(createDependency("PostgreSQL", "14.5", "primary", "localhost:5432"));
        databases.add(createDependency("Redis", "7.0", "cache", "localhost:6379"));
        databases.add(createDependency("MongoDB", "6.0", "documents", "localhost:27017"));
        dependenciesInfo.put("databases", databases);
        
        // External services
        List<Map<String, Object>> services = new ArrayList<>();
        services.add(createService("Payment Gateway", "Stripe API", "v2023-10-16"));
        services.add(createService("Email Service", "SendGrid", "v3"));
        services.add(createService("Storage", "AWS S3", "latest"));
        dependenciesInfo.put("services", services);
        
        // Message brokers
        List<Map<String, Object>> messaging = new ArrayList<>();
        messaging.add(createDependency("RabbitMQ", "3.12", "events", "localhost:5672"));
        messaging.add(createDependency("Kafka", "3.5", "streaming", "localhost:9092"));
        dependenciesInfo.put("messaging", messaging);
        
        // Key frameworks
        Map<String, String> frameworks = new HashMap<>();
        frameworks.put("Spring Boot", "3.2.0");
        frameworks.put("Spring Cloud", "2023.0.0");
        frameworks.put("Hibernate", "6.3.0");
        frameworks.put("Micrometer", "1.12.0");
        dependenciesInfo.put("frameworks", frameworks);
        
        builder.withDetail("dependencies", dependenciesInfo);
    }
    
    private Map<String, Object> createDependency(String name, String version, 
                                                  String type, String url) {
        Map<String, Object> dep = new HashMap<>();
        dep.put("name", name);
        dep.put("version", version);
        dep.put("type", type);
        dep.put("url", url);
        return dep;
    }
    
    private Map<String, Object> createService(String name, String provider, String version) {
        Map<String, Object> service = new HashMap<>();
        service.put("name", name);
        service.put("provider", provider);
        service.put("version", version);
        return service;
    }
}

/**
 * Runtime statistics information contributor
 */
@Component
class RuntimeInfoContributor implements InfoContributor {
    
    private final LocalDateTime startupTime = LocalDateTime.now();
    
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> runtimeInfo = new HashMap<>();
        
        // Startup information
        runtimeInfo.put("startupTime", startupTime);
        runtimeInfo.put("uptime", getUptime());
        
        // Memory information
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", formatBytes(runtime.totalMemory()));
        memory.put("free", formatBytes(runtime.freeMemory()));
        memory.put("max", formatBytes(runtime.maxMemory()));
        memory.put("used", formatBytes(runtime.totalMemory() - runtime.freeMemory()));
        runtimeInfo.put("memory", memory);
        
        // Thread information
        Map<String, Object> threads = new HashMap<>();
        threads.put("current", Thread.activeCount());
        threads.put("peak", ManagementData.getPeakThreadCount());
        threads.put("daemon", ManagementData.getDaemonThreadCount());
        runtimeInfo.put("threads", threads);
        
        // System load
        runtimeInfo.put("processors", runtime.availableProcessors());
        runtimeInfo.put("systemLoad", ManagementData.getSystemLoadAverage());
        
        builder.withDetail("runtime", runtimeInfo);
    }
    
    private String getUptime() {
        long uptimeMillis = System.currentTimeMillis() - 
            java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime();
        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)) + " MB";
        return (bytes / (1024 * 1024 * 1024)) + " GB";
    }
}

/**
 * Business domain information contributor
 */
@Component
class BusinessInfoContributor implements InfoContributor {
    
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> businessInfo = new HashMap<>();
        
        // Business domain
        businessInfo.put("domain", "E-Commerce Platform");
        businessInfo.put("industry", "Retail");
        businessInfo.put("region", "North America");
        
        // Business metrics (mock data)
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalOrders", 125_000);
        metrics.put("activeCustomers", 45_000);
        metrics.put("productsListed", 12_500);
        metrics.put("averageOrderValue", "$125.50");
        metrics.put("conversionRate", "3.2%");
        businessInfo.put("metrics", metrics);
        
        // Business hours
        Map<String, String> hours = new HashMap<>();
        hours.put("timezone", "America/New_York");
        hours.put("support", "Mon-Fri 9AM-5PM EST");
        hours.put("maintenance", "Sun 2AM-4AM EST");
        businessInfo.put("hours", hours);
        
        // SLA commitments
        Map<String, Object> sla = new HashMap<>();
        sla.put("uptime", "99.9%");
        sla.put("responseTime", "< 200ms (p95)");
        sla.put("supportResponse", "< 1 hour");
        sla.put("criticalIssueResolution", "< 4 hours");
        businessInfo.put("sla", sla);
        
        // Compliance
        List<String> compliance = new ArrayList<>();
        compliance.add("PCI DSS Level 1");
        compliance.add("GDPR Compliant");
        compliance.add("SOC 2 Type II");
        compliance.add("ISO 27001");
        businessInfo.put("compliance", compliance);
        
        builder.withDetail("business", businessInfo);
    }
}

/**
 * Helper class for management data
 */
class ManagementData {
    
    public static int getPeakThreadCount() {
        try {
            return (int) java.lang.management.ManagementFactory
                .getThreadMXBean().getPeakThreadCount();
        } catch (Exception e) {
            return -1;
        }
    }
    
    public static int getDaemonThreadCount() {
        try {
            return java.lang.management.ManagementFactory
                .getThreadMXBean().getDaemonThreadCount();
        } catch (Exception e) {
            return -1;
        }
    }
    
    public static double getSystemLoadAverage() {
        try {
            return java.lang.management.ManagementFactory
                .getOperatingSystemMXBean().getSystemLoadAverage();
        } catch (Exception e) {
            return -1.0;
        }
    }
}
