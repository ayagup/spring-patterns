package com.example.servicemesh;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Consul Connect Pattern
 * ======================
 * 
 * Demonstrates Consul Connect service mesh integration.
 * 
 * Key Concepts:
 * ------------
 * 1. Service Discovery - Register and discover services
 * 2. Service Mesh - Automatic sidecar proxy injection
 * 3. mTLS - Certificate-based authentication
 * 4. Intentions - Service-to-service authorization
 * 5. L7 Traffic Management - Routing, splitting, failover
 * 
 * Consul Connect Architecture:
 * ---------------------------
 * Data Plane: Envoy sidecar proxies
 * Control Plane: Consul servers (discovery + mesh)
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Service Registration
 */
@Service
class ConsulServiceRegistration {
    
    /**
     * application.yml:
     * 
     * spring:
     *   application:
     *     name: order-service
     *   cloud:
     *     consul:
     *       host: localhost
     *       port: 8500
     *       discovery:
     *         enabled: true
     *         instance-id: ${spring.application.name}:${random.value}
     *         health-check-path: /actuator/health
     *         health-check-interval: 10s
     *         prefer-ip-address: true
     *         tags:
     *           - version=v1
     *           - environment=production
     */
    
    public void registerService() {
        System.out.println("Service auto-registered with Consul");
    }
}

/**
 * Example 2: Connect Sidecar Proxy
 */
class ConnectSidecarProxy {
    
    /**
     * service-definition.json:
     * 
     * {
     *   "service": {
     *     "name": "order-service",
     *     "port": 8080,
     *     "connect": {
     *       "sidecar_service": {
     *         "port": 21000,
     *         "proxy": {
     *           "upstreams": [
     *             {
     *               "destination_name": "payment-service",
     *               "local_bind_port": 9090
     *             },
     *             {
     *               "destination_name": "inventory-service",
     *               "local_bind_port": 9091
     *             }
     *           ]
     *         }
     *       }
     *     }
     *   }
     * }
     * 
     * Register service:
     * consul services register service-definition.json
     * 
     * Start sidecar proxy:
     * consul connect envoy -sidecar-for order-service
     */
}

/**
 * Example 3: Service Intentions (Authorization)
 */
class ServiceIntentionsExample {
    
    /**
     * Allow frontend to call order-service:
     * consul intention create -allow frontend order-service
     * 
     * Deny public access:
     * consul intention create -deny public order-service
     * 
     * intention.hcl:
     * 
     * Kind = "service-intentions"
     * Name = "order-service"
     * Sources = [
     *   {
     *     Name   = "frontend"
     *     Action = "allow"
     *   },
     *   {
     *     Name   = "payment-service"
     *     Action = "allow"
     *   },
     *   {
     *     Name   = "*"
     *     Action = "deny"
     *   }
     * ]
     * 
     * Apply:
     * consul config write intention.hcl
     */
}

/**
 * Example 4: L7 Traffic Routing
 */
class TrafficRoutingExample {
    
    /**
     * service-router.hcl:
     * 
     * Kind = "service-router"
     * Name = "order-service"
     * Routes = [
     *   {
     *     Match {
     *       HTTP {
     *         Header = [
     *           {
     *             Name  = "x-user-type"
     *             Exact = "premium"
     *           }
     *         ]
     *       }
     *     }
     *     Destination {
     *       Service = "order-service"
     *       ServiceSubset = "v2"
     *     }
     *   },
     *   {
     *     Match {
     *       HTTP {
     *         PathPrefix = "/api/v1/"
     *       }
     *     }
     *     Destination {
     *       Service = "order-service"
     *       ServiceSubset = "v1"
     *     }
     *   }
     * ]
     * 
     * Apply:
     * consul config write service-router.hcl
     */
}

/**
 * Example 5: Traffic Splitting (Canary)
 */
class TrafficSplittingExample {
    
    /**
     * service-splitter.hcl:
     * 
     * Kind = "service-splitter"
     * Name = "order-service"
     * Splits = [
     *   {
     *     Weight        = 90
     *     ServiceSubset = "v1"
     *   },
     *   {
     *     Weight        = 10
     *     ServiceSubset = "v2"
     *   }
     * ]
     * 
     * service-resolver.hcl:
     * 
     * Kind = "service-resolver"
     * Name = "order-service"
     * Subsets = {
     *   "v1" = {
     *     Filter = "Service.Tags contains \"version=v1\""
     *   }
     *   "v2" = {
     *     Filter = "Service.Tags contains \"version=v2\""
     *   }
     * }
     * 
     * Apply:
     * consul config write service-resolver.hcl
     * consul config write service-splitter.hcl
     */
}

/**
 * Example 6: Service Defaults and Retries
 */
class ServiceDefaultsExample {
    
    /**
     * service-defaults.hcl:
     * 
     * Kind = "service-defaults"
     * Name = "order-service"
     * Protocol = "http"
     * 
     * # Retry configuration
     * UpstreamConfig {
     *   Defaults {
     *     Retry {
     *       RetryOn = [
     *         "5xx",
     *         "gateway-error",
     *         "reset",
     *         "connect-failure"
     *       ]
     *       NumRetries = 3
     *       RetryOnStatusCodes = [500, 502, 503, 504]
     *     }
     *   }
     * }
     * 
     * # Circuit breaker
     * PassiveHealthCheck {
     *   MaxFailures = 5
     *   Interval = "10s"
     * }
     * 
     * Apply:
     * consul config write service-defaults.hcl
     */
}

/**
 * Example 7: Spring Cloud Consul Integration
 */
@Configuration
class SpringCloudConsulConfig {
    
    /**
     * pom.xml:
     * <dependency>
     *     <groupId>org.springframework.cloud</groupId>
     *     <artifactId>spring-cloud-starter-consul-discovery</artifactId>
     * </dependency>
     * <dependency>
     *     <groupId>org.springframework.cloud</groupId>
     *     <artifactId>spring-cloud-starter-consul-config</artifactId>
     * </dependency>
     * 
     * application.yml:
     * spring:
     *   cloud:
     *     consul:
     *       host: localhost
     *       port: 8500
     *       discovery:
     *         enabled: true
     *         register: true
     *         deregister: true
     *         instance-id: ${spring.application.name}:${server.port}
     *         service-name: ${spring.application.name}
     *         health-check-path: /actuator/health
     *         health-check-interval: 10s
     *         prefer-ip-address: true
     *         tags:
     *           - version=v1
     *       config:
     *         enabled: true
     *         format: YAML
     *         prefix: config
     *         default-context: application
     */
    
    @Bean
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate();
    }
}

/**
 * Example 8: Service Discovery with DiscoveryClient
 */
@Service
class ServiceDiscoveryExample {
    
    private final DiscoveryClient discoveryClient;
    
    public ServiceDiscoveryExample(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }
    
    public void discoverServices() {
        // Get all registered services
        discoveryClient.getServices().forEach(serviceId -> {
            System.out.println("Service: " + serviceId);
            
            // Get instances of each service
            discoveryClient.getInstances(serviceId).forEach(instance -> {
                System.out.println("  - " + instance.getHost() + ":" + instance.getPort());
                System.out.println("    Metadata: " + instance.getMetadata());
            });
        });
    }
    
    public void callUpstreamService() {
        // Service calls through Connect proxy
        // Call payment-service on local bind port 9090
        // Connect proxy handles mTLS and routing
        String url = "http://localhost:9090/payments";
        System.out.println("Calling: " + url);
    }
}

/**
 * Example 9: Observability with Metrics
 */
class ObservabilityExample {
    
    /**
     * Enable Prometheus metrics:
     * 
     * consul agent -config-dir=/etc/consul.d \
     *   -ui \
     *   -bind=0.0.0.0 \
     *   -client=0.0.0.0 \
     *   -enable-script-checks \
     *   -telemetry { prometheus_retention_time = "30s" }
     * 
     * Metrics available at:
     * http://localhost:8500/v1/agent/metrics?format=prometheus
     * 
     * Key metrics:
     * - consul_catalog_services: Number of registered services
     * - consul_health_service_query: Health check status
     * - consul_mesh_active_connections: Active connections
     * - consul_mesh_request_duration: Request latency
     */
}

/**
 * Example 10: Multi-Datacenter Mesh
 */
class MultiDatacenterExample {
    
    /**
     * Mesh Gateway configuration:
     * 
     * mesh-gateway.hcl:
     * 
     * Kind = "mesh-gateway"
     * Name = "mesh-gateway"
     * Namespace = "default"
     * Partition = "default"
     * 
     * Meta {
     *   consul-version = "1.14.0"
     * }
     * 
     * service-defaults.hcl for cross-DC:
     * 
     * Kind = "service-defaults"
     * Name = "order-service"
     * Protocol = "http"
     * 
     * MeshGateway {
     *   Mode = "local"  # local, remote, none
     * }
     * 
     * Federate datacenters:
     * consul members -wan
     * 
     * Service query across DCs:
     * curl http://localhost:8500/v1/catalog/service/order-service?dc=dc2
     */
}

/**
 * Main Pattern Class
 */
public class ConsulConnectPattern {
    
    public static void main(String[] args) {
        System.out.println("Consul Connect Pattern");
        System.out.println("=====================\n");
        
        System.out.println("Consul Connect Features:");
        System.out.println("✓ Service Discovery - Register and find services");
        System.out.println("✓ Service Mesh - Automatic mTLS and proxy");
        System.out.println("✓ Intentions - Service-to-service authorization");
        System.out.println("✓ L7 Traffic Management - Routing, splitting");
        System.out.println("✓ Health Checking - Monitor service health");
        System.out.println("✓ Multi-Datacenter - WAN federation\n");
        
        System.out.println("Key Components:");
        System.out.println("1. Service Registration - Auto-register with Consul");
        System.out.println("2. Connect Proxy - Envoy sidecar for mTLS");
        System.out.println("3. Intentions - Authorization policies");
        System.out.println("4. Service Router - L7 traffic routing");
        System.out.println("5. Service Splitter - Traffic percentage splits");
        System.out.println("6. Service Resolver - Service subset selection\n");
        
        System.out.println("Benefits:");
        System.out.println("- Unified discovery + mesh in one platform");
        System.out.println("- Simple intention-based security");
        System.out.println("- Multi-datacenter support out-of-box");
        System.out.println("- Native Spring Cloud integration");
        System.out.println("- Built-in KV store for configuration\n");
        
        System.out.println("Quick Start:");
        System.out.println("1. consul agent -dev");
        System.out.println("2. consul services register service.json");
        System.out.println("3. consul connect envoy -sidecar-for order-service");
        System.out.println("4. consul intention create -allow frontend order-service");
        System.out.println("5. Visit: http://localhost:8500/ui");
    }
}
