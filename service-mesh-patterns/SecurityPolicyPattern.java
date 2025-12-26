package com.example.servicemesh;

/**
 * Security Policy Pattern
 * =======================
 * 
 * Demonstrates security policies in service mesh.
 * 
 * Key Concepts:
 * ------------
 * 1. mTLS - Mutual TLS between services
 * 2. Authorization - Service-to-service access control
 * 3. Authentication - JWT validation, OIDC
 * 4. Certificate Management - Auto-rotation
 * 5. Network Policies - L3/L4 traffic control
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Strict mTLS Mode
 */
class StrictMTLSPolicy {
    
    /**
     * Istio PeerAuthentication:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: PeerAuthentication
     * metadata:
     *   name: default
     *   namespace: default
     * spec:
     *   mtls:
     *     mode: STRICT  # Require mTLS for all traffic
     * 
     * ---
     * Per-service mTLS:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: PeerAuthentication
     * metadata:
     *   name: order-service-mtls
     * spec:
     *   selector:
     *     matchLabels:
     *       app: order-service
     *   mtls:
     *     mode: STRICT
     *   portLevelMtls:
     *     8080:
     *       mode: STRICT
     *     9090:
     *       mode: PERMISSIVE  # Allow plain text for metrics
     */
}

/**
 * Example 2: Service-to-Service Authorization
 */
class ServiceAuthorizationPolicy {
    
    /**
     * Allow specific services:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: AuthorizationPolicy
     * metadata:
     *   name: order-service-authz
     *   namespace: default
     * spec:
     *   selector:
     *     matchLabels:
     *       app: order-service
     *   action: ALLOW
     *   rules:
     *   - from:
     *     - source:
     *         principals:
     *         - "cluster.local/ns/default/sa/frontend"
     *         - "cluster.local/ns/default/sa/admin-service"
     *     to:
     *     - operation:
     *         methods: ["GET", "POST", "PUT", "DELETE"]
     *         paths: ["/orders/*"]
     * 
     * ---
     * Deny policy:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: AuthorizationPolicy
     * metadata:
     *   name: deny-public-access
     * spec:
     *   selector:
     *     matchLabels:
     *       app: order-service
     *   action: DENY
     *   rules:
     *   - from:
     *     - source:
     *         principals: ["*"]
     *     to:
     *     - operation:
     *         paths: ["/internal/*", "/admin/*"]
     */
}

/**
 * Example 3: JWT Authentication
 */
class JWTAuthenticationPolicy {
    
    /**
     * Validate JWT tokens:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: RequestAuthentication
     * metadata:
     *   name: jwt-auth
     * spec:
     *   selector:
     *     matchLabels:
     *       app: order-service
     *   jwtRules:
     *   - issuer: "https://auth.example.com"
     *     jwksUri: "https://auth.example.com/.well-known/jwks.json"
     *     audiences:
     *     - "order-service-api"
     *     forwardOriginalToken: true
     * 
     * ---
     * Require JWT:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: AuthorizationPolicy
     * metadata:
     *   name: require-jwt
     * spec:
     *   selector:
     *     matchLabels:
     *       app: order-service
     *   action: ALLOW
     *   rules:
     *   - from:
     *     - source:
     *         requestPrincipals: ["*"]
     *     when:
     *     - key: request.auth.claims[role]
     *       values: ["admin", "user"]
     */
}

/**
 * Example 4: RBAC with Claims
 */
class ClaimsBasedRBACPolicy {
    
    /**
     * Role-based access control:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: AuthorizationPolicy
     * metadata:
     *   name: rbac-order-service
     * spec:
     *   selector:
     *     matchLabels:
     *       app: order-service
     *   action: ALLOW
     *   rules:
     *   # Admins can do everything
     *   - when:
     *     - key: request.auth.claims[role]
     *       values: ["admin"]
     *       
     *   # Users can read and create
     *   - to:
     *     - operation:
     *         methods: ["GET", "POST"]
     *     when:
     *     - key: request.auth.claims[role]
     *       values: ["user"]
     *       
     *   # Service accounts for internal calls
     *   - from:
     *     - source:
     *         namespaces: ["default"]
     *         principals:
     *         - "cluster.local/ns/default/sa/payment-service"
     */
}

/**
 * Example 5: IP-Based Access Control
 */
class IPBasedAccessControl {
    
    /**
     * Allow specific IP ranges:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: AuthorizationPolicy
     * metadata:
     *   name: ip-whitelist
     * spec:
     *   selector:
     *     matchLabels:
     *       app: order-service
     *   action: ALLOW
     *   rules:
     *   - from:
     *     - source:
     *         ipBlocks:
     *         - "10.0.0.0/8"      # Internal network
     *         - "192.168.1.0/24"   # Office network
     *         
     * ---
     * Deny specific IPs:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: AuthorizationPolicy
     * metadata:
     *   name: ip-blacklist
     * spec:
     *   selector:
     *     matchLabels:
     *       app: order-service
     *   action: DENY
     *   rules:
     *   - from:
     *     - source:
     *         ipBlocks:
     *         - "203.0.113.0/24"  # Blocked range
     */
}

/**
 * Example 6: Namespace Isolation
 */
class NamespaceIsolationPolicy {
    
    /**
     * Deny cross-namespace traffic by default:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: AuthorizationPolicy
     * metadata:
     *   name: deny-all
     *   namespace: production
     * spec:
     *   action: DENY
     *   rules:
     *   - from:
     *     - source:
     *         notNamespaces: ["production"]
     * 
     * ---
     * Allow specific cross-namespace calls:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: AuthorizationPolicy
     * metadata:
     *   name: allow-monitoring
     *   namespace: production
     * spec:
     *   action: ALLOW
     *   rules:
     *   - from:
     *     - source:
     *         namespaces: ["monitoring"]
     *         principals:
     *         - "cluster.local/ns/monitoring/sa/prometheus"
     */
}

/**
 * Example 7: HTTP Method-Based Authorization
 */
class MethodBasedAuthorizationPolicy {
    
    /**
     * Method-specific permissions:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: AuthorizationPolicy
     * metadata:
     *   name: method-authz
     * spec:
     *   selector:
     *     matchLabels:
     *       app: order-service
     *   action: ALLOW
     *   rules:
     *   # Read operations for all authenticated users
     *   - to:
     *     - operation:
     *         methods: ["GET", "HEAD", "OPTIONS"]
     *     from:
     *     - source:
     *         requestPrincipals: ["*"]
     *         
     *   # Write operations only for specific roles
     *   - to:
     *     - operation:
     *         methods: ["POST", "PUT", "PATCH"]
     *     when:
     *     - key: request.auth.claims[role]
     *       values: ["admin", "editor"]
     *       
     *   # Delete only for admins
     *   - to:
     *     - operation:
     *         methods: ["DELETE"]
     *     when:
     *     - key: request.auth.claims[role]
     *       values: ["admin"]
     */
}

/**
 * Example 8: Custom Headers Authorization
 */
class CustomHeadersAuthorizationPolicy {
    
    /**
     * Authorize based on custom headers:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: AuthorizationPolicy
     * metadata:
     *   name: header-based-authz
     * spec:
     *   selector:
     *     matchLabels:
     *       app: order-service
     *   action: ALLOW
     *   rules:
     *   - when:
     *     - key: request.headers[x-api-key]
     *       values: ["valid-api-key-1", "valid-api-key-2"]
     *   - when:
     *     - key: request.headers[x-tenant-id]
     *       values: ["tenant-a", "tenant-b"]
     *     - key: request.headers[x-environment]
     *       values: ["production"]
     */
}

/**
 * Example 9: Time-Based Access Control
 */
class TimeBasedAccessControl {
    
    /**
     * While Istio doesn't natively support time-based policies,
     * you can use external authorization service:
     * 
     * apiVersion: security.istio.io/v1beta1
     * kind: AuthorizationPolicy
     * metadata:
     *   name: external-authz
     * spec:
     *   selector:
     *     matchLabels:
     *       app: order-service
     *   action: CUSTOM
     *   provider:
     *     name: "my-ext-authz"
     *   rules:
     *   - to:
     *     - operation:
     *         paths: ["/admin/*"]
     * 
     * ---
     * External authorizer can check:
     * - Business hours (9am-5pm)
     * - Weekday vs weekend
     * - Maintenance windows
     * - Rate limits per time period
     */
}

/**
 * Example 10: Defense in Depth
 */
class DefenseInDepthPolicy {
    
    /**
     * Layered security approach:
     * 
     * # Layer 1: Network policy (L3/L4)
     * apiVersion: networking.k8s.io/v1
     * kind: NetworkPolicy
     * metadata:
     *   name: order-service-netpol
     * spec:
     *   podSelector:
     *     matchLabels:
     *       app: order-service
     *   policyTypes:
     *   - Ingress
     *   - Egress
     *   ingress:
     *   - from:
     *     - podSelector:
     *         matchLabels:
     *           app: frontend
     *     ports:
     *     - protocol: TCP
     *       port: 8080
     * 
     * ---
     * # Layer 2: mTLS (L4)
     * apiVersion: security.istio.io/v1beta1
     * kind: PeerAuthentication
     * metadata:
     *   name: default-mtls
     * spec:
     *   mtls:
     *     mode: STRICT
     * 
     * ---
     * # Layer 3: Service authorization (L7)
     * apiVersion: security.istio.io/v1beta1
     * kind: AuthorizationPolicy
     * metadata:
     *   name: order-service-authz
     * spec:
     *   selector:
     *     matchLabels:
     *       app: order-service
     *   action: ALLOW
     *   rules:
     *   - from:
     *     - source:
     *         principals: ["cluster.local/ns/default/sa/frontend"]
     *     to:
     *     - operation:
     *         methods: ["GET", "POST"]
     * 
     * ---
     * # Layer 4: JWT authentication (L7)
     * apiVersion: security.istio.io/v1beta1
     * kind: RequestAuthentication
     * metadata:
     *   name: jwt-validation
     * spec:
     *   selector:
     *     matchLabels:
     *       app: order-service
     *   jwtRules:
     *   - issuer: "https://auth.example.com"
     *     jwksUri: "https://auth.example.com/.well-known/jwks.json"
     */
}

/**
 * Main Pattern Class
 */
public class SecurityPolicyPattern {
    
    public static void main(String[] args) {
        System.out.println("Security Policy Pattern");
        System.out.println("======================\n");
        
        System.out.println("Security Layers:");
        System.out.println("1. mTLS - Encrypted service-to-service communication");
        System.out.println("2. Authorization - Service access control");
        System.out.println("3. Authentication - JWT/OIDC validation");
        System.out.println("4. Network Policies - L3/L4 traffic control\n");
        
        System.out.println("Authorization Methods:");
        System.out.println("- Service identity (SPIFFE)");
        System.out.println("- JWT claims (role, scope)");
        System.out.println("- IP-based (whitelist/blacklist)");
        System.out.println("- Namespace isolation");
        System.out.println("- HTTP method-based");
        System.out.println("- Custom headers\n");
        
        System.out.println("Best Practices:");
        System.out.println("✓ Enable STRICT mTLS by default");
        System.out.println("✓ Use deny-by-default policies");
        System.out.println("✓ Implement defense in depth");
        System.out.println("✓ Validate JWT tokens");
        System.out.println("✓ Isolate namespaces");
        System.out.println("✓ Monitor security events");
    }
}
