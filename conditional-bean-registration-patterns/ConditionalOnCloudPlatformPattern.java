package com.example.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditional On Cloud Platform Pattern
 * ======================================
 * 
 * Demonstrates @ConditionalOnCloudPlatform annotation that creates beans
 * only when the application is running on a specific cloud platform. This
 * enables cloud-specific configurations for AWS, Azure, GCP, Cloud Foundry,
 * Kubernetes, Heroku, and SAP.
 * 
 * Key Concepts:
 * ------------
 * 1. @ConditionalOnCloudPlatform - Platform-specific bean registration
 * 2. CloudPlatform enum - Supported cloud platforms
 * 3. Platform Detection - Automatic environment detection
 * 4. Cloud-Specific Configuration - Platform optimizations
 * 5. Multi-Cloud Support - Different configs per platform
 * 
 * How It Works:
 * ------------
 * - Detects cloud platform from environment variables
 * - Checks platform-specific indicators
 * - Creates beans only on matching platform
 * - Evaluated at configuration processing time
 * 
 * Supported Cloud Platforms:
 * -------------------------
 * - CLOUD_FOUNDRY (CloudPlatform.CLOUD_FOUNDRY)
 *   * Detected by: VCAP_APPLICATION, VCAP_SERVICES
 * - HEROKU (CloudPlatform.HEROKU)
 *   * Detected by: DYNO environment variable
 * - KUBERNETES (CloudPlatform.KUBERNETES)
 *   * Detected by: KUBERNETES_SERVICE_HOST, KUBERNETES_SERVICE_PORT
 * - AZURE (CloudPlatform.AZURE)
 *   * Detected by: WEBSITE_INSTANCE_ID
 * - SAP (CloudPlatform.SAP)
 *   * Detected by: HC_LANDSCAPE
 * - AWS (custom detection)
 *   * Detected by: AWS_REGION, AWS_EXECUTION_ENV, EC2 metadata
 * - GCP (custom detection)
 *   * Detected by: GOOGLE_CLOUD_PROJECT, GAE_INSTANCE
 * 
 * Common Use Cases:
 * ----------------
 * - Cloud-specific data sources
 * - Platform-specific service bindings
 * - Cloud storage configuration (S3, Azure Blob, GCS)
 * - Cloud monitoring integration
 * - Platform-specific security
 * - Cloud messaging services
 * - Managed database connections
 * - Cloud secret management
 * 
 * Syntax:
 * ------
 * @ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
 * @ConditionalOnCloudPlatform(CloudPlatform.HEROKU)
 * @ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
 * @ConditionalOnCloudPlatform(CloudPlatform.AZURE)
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Cloud Foundry Configuration
 */
@Configuration
@ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
class CloudFoundryConfiguration {
    
    /**
     * Configure Cloud Foundry services
     */
    @Bean
    public String cloudFoundryConnector() {
        System.out.println("Creating Cloud Foundry Service Connector");
        System.out.println("  Platform: Cloud Foundry (Pivotal/VMware Tanzu)");
        System.out.println("  Environment: VCAP_APPLICATION, VCAP_SERVICES");
        return "Cloud Foundry Connector";
    }
    
    @Bean
    public String vcapParser() {
        System.out.println("Creating VCAP Services Parser");
        System.out.println("  Parsing: VCAP_APPLICATION, VCAP_SERVICES");
        return "VCAP Parser";
    }
    
    @Bean
    public String cfDataSource() {
        System.out.println("Creating Cloud Foundry DataSource");
        System.out.println("  Bindings: MySQL, PostgreSQL, Redis from VCAP_SERVICES");
        return "Cloud Foundry DataSource";
    }
}

/**
 * Example 2: Heroku Configuration
 */
@Configuration
@ConditionalOnCloudPlatform(CloudPlatform.HEROKU)
class HerokuConfiguration {
    
    /**
     * Configure Heroku-specific features
     */
    @Bean
    public String herokuConnector() {
        System.out.println("Creating Heroku Service Connector");
        System.out.println("  Platform: Heroku");
        System.out.println("  Environment: DYNO variable");
        return "Heroku Connector";
    }
    
    @Bean
    public String herokuPostgres() {
        System.out.println("Creating Heroku Postgres Connection");
        System.out.println("  DATABASE_URL: postgresql://...");
        return "Heroku Postgres";
    }
    
    @Bean
    public String herokuRedis() {
        System.out.println("Creating Heroku Redis Connection");
        System.out.println("  REDIS_URL: redis://...");
        return "Heroku Redis";
    }
    
    @Bean
    public String dynoMetadata() {
        System.out.println("Creating Dyno Metadata Service");
        System.out.println("  Dyno info: DYNO, DYNO_RAM");
        return "Dyno Metadata";
    }
}

/**
 * Example 3: Kubernetes Configuration
 */
@Configuration
@ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
class KubernetesConfiguration {
    
    /**
     * Configure Kubernetes-specific features
     */
    @Bean
    public String kubernetesConnector() {
        System.out.println("Creating Kubernetes Service Connector");
        System.out.println("  Platform: Kubernetes (K8s)");
        System.out.println("  Environment: KUBERNETES_SERVICE_HOST/PORT");
        return "Kubernetes Connector";
    }
    
    @Bean
    public String kubernetesServiceDiscovery() {
        System.out.println("Creating Kubernetes Service Discovery");
        System.out.println("  DNS-based: service.namespace.svc.cluster.local");
        return "Kubernetes Service Discovery";
    }
    
    @Bean
    public String configMapLoader() {
        System.out.println("Creating ConfigMap Loader");
        System.out.println("  Load config from Kubernetes ConfigMaps");
        return "ConfigMap Loader";
    }
    
    @Bean
    public String secretLoader() {
        System.out.println("Creating Secret Loader");
        System.out.println("  Load secrets from Kubernetes Secrets");
        return "Secret Loader";
    }
    
    @Bean
    public String podHealthIndicator() {
        System.out.println("Creating Pod Health Indicator");
        System.out.println("  Readiness and Liveness probes");
        return "Pod Health Indicator";
    }
}

/**
 * Example 4: Azure Configuration
 */
@Configuration
@ConditionalOnCloudPlatform(CloudPlatform.AZURE)
class AzureConfiguration {
    
    /**
     * Configure Azure-specific features
     */
    @Bean
    public String azureConnector() {
        System.out.println("Creating Azure Service Connector");
        System.out.println("  Platform: Microsoft Azure");
        System.out.println("  Environment: WEBSITE_INSTANCE_ID");
        return "Azure Connector";
    }
    
    @Bean
    public String azureBlobStorage() {
        System.out.println("Creating Azure Blob Storage Client");
        System.out.println("  Storage Account connection");
        return "Azure Blob Storage";
    }
    
    @Bean
    public String azureSqlDatabase() {
        System.out.println("Creating Azure SQL Database Connection");
        System.out.println("  Managed SQL Database");
        return "Azure SQL Database";
    }
    
    @Bean
    public String azureKeyVault() {
        System.out.println("Creating Azure Key Vault Client");
        System.out.println("  Secret management: Azure Key Vault");
        return "Azure Key Vault";
    }
    
    @Bean
    public String azureServiceBus() {
        System.out.println("Creating Azure Service Bus Client");
        System.out.println("  Messaging: Queues and Topics");
        return "Azure Service Bus";
    }
}

/**
 * Example 5: SAP Configuration
 */
@Configuration
@ConditionalOnCloudPlatform(CloudPlatform.SAP)
class SAPConfiguration {
    
    /**
     * Configure SAP Cloud Platform features
     */
    @Bean
    public String sapConnector() {
        System.out.println("Creating SAP Cloud Platform Connector");
        System.out.println("  Platform: SAP Cloud Platform (SCP)");
        System.out.println("  Environment: HC_LANDSCAPE");
        return "SAP Connector";
    }
    
    @Bean
    public String sapHanaDatabase() {
        System.out.println("Creating SAP HANA Database Connection");
        System.out.println("  In-memory database: SAP HANA");
        return "SAP HANA Database";
    }
    
    @Bean
    public String sapDestinationService() {
        System.out.println("Creating SAP Destination Service");
        System.out.println("  Connect to on-premise systems");
        return "SAP Destination Service";
    }
}

/**
 * Example 6: AWS-Specific Configuration (Custom Detection)
 */
@Configuration
// Note: AWS detection requires custom @Conditional implementation
class AWSConfiguration {
    
    /**
     * Configure AWS-specific features
     * 
     * Detection:
     * - AWS_REGION environment variable
     * - AWS_EXECUTION_ENV environment variable
     * - EC2 instance metadata endpoint
     */
    @Bean
    // @ConditionalOnAWS (custom annotation)
    public String awsConnector() {
        System.out.println("Creating AWS Service Connector");
        System.out.println("  Platform: Amazon Web Services (AWS)");
        System.out.println("  Detection: AWS_REGION, AWS_EXECUTION_ENV");
        return "AWS Connector";
    }
    
    @Bean
    public String s3Client() {
        System.out.println("Creating Amazon S3 Client");
        System.out.println("  Object storage: S3 buckets");
        return "S3 Client";
    }
    
    @Bean
    public String rdsConnection() {
        System.out.println("Creating RDS Database Connection");
        System.out.println("  Managed database: RDS MySQL/PostgreSQL");
        return "RDS Connection";
    }
    
    @Bean
    public String sqsClient() {
        System.out.println("Creating Amazon SQS Client");
        System.out.println("  Message queue: SQS");
        return "SQS Client";
    }
    
    @Bean
    public String secretsManager() {
        System.out.println("Creating AWS Secrets Manager Client");
        System.out.println("  Secret management: AWS Secrets Manager");
        return "AWS Secrets Manager";
    }
    
    @Bean
    public String cloudWatchMetrics() {
        System.out.println("Creating CloudWatch Metrics Publisher");
        System.out.println("  Monitoring: CloudWatch");
        return "CloudWatch Metrics";
    }
}

/**
 * Example 7: GCP Configuration (Custom Detection)
 */
@Configuration
// Note: GCP detection requires custom @Conditional implementation
class GCPConfiguration {
    
    /**
     * Configure GCP-specific features
     * 
     * Detection:
     * - GOOGLE_CLOUD_PROJECT environment variable
     * - GAE_INSTANCE for App Engine
     * - GCP metadata server
     */
    @Bean
    // @ConditionalOnGCP (custom annotation)
    public String gcpConnector() {
        System.out.println("Creating GCP Service Connector");
        System.out.println("  Platform: Google Cloud Platform (GCP)");
        System.out.println("  Detection: GOOGLE_CLOUD_PROJECT");
        return "GCP Connector";
    }
    
    @Bean
    public String cloudStorageClient() {
        System.out.println("Creating Google Cloud Storage Client");
        System.out.println("  Object storage: GCS buckets");
        return "Cloud Storage Client";
    }
    
    @Bean
    public String cloudSqlConnection() {
        System.out.println("Creating Cloud SQL Connection");
        System.out.println("  Managed database: Cloud SQL MySQL/PostgreSQL");
        return "Cloud SQL Connection";
    }
    
    @Bean
    public String pubSubClient() {
        System.out.println("Creating Cloud Pub/Sub Client");
        System.out.println("  Messaging: Pub/Sub topics");
        return "Pub/Sub Client";
    }
    
    @Bean
    public String secretManagerClient() {
        System.out.println("Creating Secret Manager Client");
        System.out.println("  Secret management: GCP Secret Manager");
        return "Secret Manager Client";
    }
}

/**
 * Example 8: Multi-Cloud Storage Configuration
 */
@Configuration
class MultiCloudStorageConfiguration {
    
    @Bean
    @ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
    public String cloudFoundryStorage() {
        System.out.println("Creating Cloud Foundry Storage Service");
        return "Cloud Foundry Storage";
    }
    
    @Bean
    @ConditionalOnCloudPlatform(CloudPlatform.HEROKU)
    public String herokuStorage() {
        System.out.println("Creating Heroku Storage (S3 addon)");
        return "Heroku Storage";
    }
    
    @Bean
    @ConditionalOnCloudPlatform(CloudPlatform.AZURE)
    public String azureStorage() {
        System.out.println("Creating Azure Blob Storage");
        return "Azure Storage";
    }
}

/**
 * Example 9: Cloud Monitoring Integration
 */
@Configuration
class CloudMonitoringConfiguration {
    
    @Bean
    @ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
    public String cfLoggregator() {
        System.out.println("Creating Cloud Foundry Loggregator Integration");
        return "CF Loggregator";
    }
    
    @Bean
    @ConditionalOnCloudPlatform(CloudPlatform.HEROKU)
    public String herokuLogplex() {
        System.out.println("Creating Heroku Logplex Integration");
        return "Heroku Logplex";
    }
    
    @Bean
    @ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
    public String k8sPrometheus() {
        System.out.println("Creating Kubernetes Prometheus Metrics");
        return "K8s Prometheus";
    }
}

/**
 * Example 10: Cloud Security Configuration
 */
@Configuration
class CloudSecurityConfiguration {
    
    @Bean
    @ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
    public String cfSecurity() {
        System.out.println("Creating Cloud Foundry Security Config");
        System.out.println("  UAA OAuth2 integration");
        return "CF Security";
    }
    
    @Bean
    @ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
    public String k8sSecurity() {
        System.out.println("Creating Kubernetes Security Config");
        System.out.println("  Service Account, RBAC");
        return "K8s Security";
    }
    
    @Bean
    @ConditionalOnCloudPlatform(CloudPlatform.AZURE)
    public String azureSecurity() {
        System.out.println("Creating Azure Security Config");
        System.out.println("  Azure AD, Managed Identity");
        return "Azure Security";
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class ConditionalOnCloudPlatformPattern {
    
    /**
     * Example: Platform detection service
     */
    @Bean
    public String platformDetectionService() {
        System.out.println("Creating Platform Detection Service");
        System.out.println("  Detects: Cloud Foundry, Heroku, Kubernetes, Azure, SAP");
        return "Platform Detection Service";
    }
}

/**
 * Usage Examples and Best Practices
 */
class ConditionalOnCloudPlatformUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Conditional On Cloud Platform Pattern");
        System.out.println("======================================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Create beans specific to cloud platforms");
        System.out.println("- Configure platform-specific services");
        System.out.println("- Enable multi-cloud deployments\n");
        
        System.out.println("Syntax:");
        System.out.println("@ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)");
        System.out.println("@ConditionalOnCloudPlatform(CloudPlatform.HEROKU)");
        System.out.println("@ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)");
        System.out.println("@ConditionalOnCloudPlatform(CloudPlatform.AZURE)");
        System.out.println("@ConditionalOnCloudPlatform(CloudPlatform.SAP)\n");
        
        System.out.println("Supported Platforms:");
        System.out.println("1. Cloud Foundry (CLOUD_FOUNDRY)");
        System.out.println("   - Detection: VCAP_APPLICATION, VCAP_SERVICES");
        System.out.println("   - Services: MySQL, PostgreSQL, Redis bindings");
        System.out.println("2. Heroku (HEROKU)");
        System.out.println("   - Detection: DYNO environment variable");
        System.out.println("   - Services: Postgres, Redis, add-ons");
        System.out.println("3. Kubernetes (KUBERNETES)");
        System.out.println("   - Detection: KUBERNETES_SERVICE_HOST/PORT");
        System.out.println("   - Features: ConfigMaps, Secrets, Service Discovery");
        System.out.println("4. Azure (AZURE)");
        System.out.println("   - Detection: WEBSITE_INSTANCE_ID");
        System.out.println("   - Services: Blob Storage, SQL, Key Vault, Service Bus");
        System.out.println("5. SAP (SAP)");
        System.out.println("   - Detection: HC_LANDSCAPE");
        System.out.println("   - Services: SAP HANA, Destination Service\n");
        
        System.out.println("Custom Platform Detection (AWS, GCP):");
        System.out.println("- AWS: AWS_REGION, AWS_EXECUTION_ENV, EC2 metadata");
        System.out.println("- GCP: GOOGLE_CLOUD_PROJECT, GAE_INSTANCE\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. Cloud-specific data sources");
        System.out.println("2. Platform service bindings");
        System.out.println("3. Cloud storage (S3, Azure Blob, GCS)");
        System.out.println("4. Cloud monitoring integration");
        System.out.println("5. Platform security (OAuth2, IAM)");
        System.out.println("6. Cloud messaging (SQS, Service Bus, Pub/Sub)");
        System.out.println("7. Managed databases (RDS, Cloud SQL, Azure SQL)");
        System.out.println("8. Secret management (Key Vault, Secrets Manager)\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Use for platform-specific optimizations");
        System.out.println("- Keep business logic platform-agnostic");
        System.out.println("- Use abstraction layers for cloud services");
        System.out.println("- Test on actual cloud platforms");
        System.out.println("- Document platform requirements");
        System.out.println("- Provide local development alternatives");
        System.out.println("- Use Spring Cloud Connectors for bindings");
        System.out.println("- Implement graceful degradation\n");
        
        System.out.println("Example Pattern:");
        System.out.println("@Configuration");
        System.out.println("@ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)");
        System.out.println("public class K8sConfiguration {");
        System.out.println("  ");
        System.out.println("  @Bean");
        System.out.println("  public ServiceDiscovery k8sServiceDiscovery() {");
        System.out.println("    return new KubernetesServiceDiscovery();");
        System.out.println("  }");
        System.out.println("  ");
        System.out.println("  @Bean");
        System.out.println("  public ConfigMapLoader configMapLoader() {");
        System.out.println("    return new ConfigMapPropertySource();");
        System.out.println("  }");
        System.out.println("}");
    }
}
