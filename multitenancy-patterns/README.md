# Multi-Tenancy Patterns

This collection demonstrates various multi-tenancy implementation patterns for building SaaS applications with Spring Boot.

## Patterns Overview

### 1. Tenant Identification Pattern (`TenantIdentificationPattern.java`)
**Purpose:** Identify tenant from incoming requests  
**Use Case:** Request routing, tenant context initialization  
**Key Features:**
- Multiple identification strategies:
  - Header-based (`X-Tenant-ID`)
  - Subdomain-based (`tenant1.app.com`)
  - Path-based (`/tenant/tenant1/api`)
  - Token-based (JWT claims)
- Fallback to default tenant
- Strategy priority configuration

**Example Usage:**
```java
// Header-based
GET /api/tenant-identification/current
Headers: X-Tenant-ID: tenant1

// Subdomain-based
GET https://tenant1.myapp.com/api/tenant-identification/current

// Path-based
GET /tenant/tenant1/api/tenant-identification/current
```

### 2. Tenant Context Pattern (`TenantContextPattern.java`)
**Purpose:** Thread-safe tenant context storage  
**Use Case:** Access current tenant anywhere in the application  
**Key Features:**
- ThreadLocal-based storage
- Thread-safe tenant access
- Automatic context cleanup
- Request-scoped tenant information
- Integration with filters/interceptors

**Example Usage:**
```java
// Set tenant context (typically in filter)
POST /api/tenant-context/set/tenant1

// Get current tenant
GET /api/tenant-context/get
Response: {"tenantId": "tenant1", "threadName": "http-nio-8080-exec-1"}

// Clear context
DELETE /api/tenant-context/clear
```

### 3. Tenant Resolver Pattern (`TenantResolverPattern.java`)
**Purpose:** Resolve tenant configuration and metadata  
**Use Case:** Load tenant-specific settings, database connections  
**Key Features:**
- Tenant configuration registry
- Dynamic tenant registration
- Configuration caching
- Tenant metadata management
- Database URL resolution

**Example Usage:**
```java
// Register tenant
POST /api/tenant-resolver/register
{
  "tenantId": "tenant1",
  "tenantName": "Tenant One Inc.",
  "databaseUrl": "jdbc:postgresql://localhost/tenant1_db"
}

// Resolve tenant configuration
GET /api/tenant-resolver/resolve/tenant1
Response: {
  "tenantId": "tenant1",
  "tenantName": "Tenant One Inc.",
  "databaseUrl": "jdbc:postgresql://localhost/tenant1_db"
}
```

### 4. Shared Schema Pattern (`SharedSchemaPattern.java`)
**Purpose:** All tenants share same database schema with tenant_id discriminator  
**Use Case:** Small to medium SaaS applications  
**Key Features:**
- Single database, single schema
- `tenant_id` column in all tables
- Automatic tenant filtering
- Row-level security with @Where annotations
- Cost-effective for many tenants

**Pros:**
- Simple deployment
- Easy maintenance
- Cost-effective
- Schema changes affect all tenants

**Cons:**
- Potential data leakage risk
- Scaling limitations
- Complex query optimization

**Example Usage:**
```java
// Save data for tenant
POST /api/shared-schema/data
{
  "tenantId": "tenant1",
  "data": "Tenant-specific data"
}

// Get data for tenant (automatically filtered)
GET /api/shared-schema/data/tenant1
```

### 5. Separate Schema Pattern (`SeparateSchemaPattern.java`)
**Purpose:** Each tenant has its own schema within shared database  
**Use Case:** Medium to large SaaS applications  
**Key Features:**
- Single database, multiple schemas
- Schema-level isolation
- Independent schema evolution per tenant
- Better isolation than shared schema
- Moderate resource usage

**Pros:**
- Better data isolation
- Per-tenant schema customization
- Easier to backup individual tenants
- Better performance than shared schema

**Cons:**
- Schema management complexity
- Limited by database connection limits
- Migration complexity

**Example Usage:**
```java
// Initialize schema for new tenant
POST /api/separate-schema/initialize/tenant1

// Save to tenant schema
POST /api/separate-schema/data/tenant1
{
  "content": "Data in tenant1 schema"
}

// Query from tenant schema
GET /api/separate-schema/data/tenant1
```

### 6. Separate Database Pattern (`SeparateDatabasePattern.java`)
**Purpose:** Each tenant has dedicated database  
**Use Case:** Enterprise SaaS, regulated industries  
**Key Features:**
- Complete database isolation
- Independent scaling per tenant
- Tenant-specific database optimization
- Maximum security and compliance
- Flexible database technology per tenant

**Pros:**
- Maximum isolation and security
- Independent scaling
- Easy tenant migration
- Per-tenant backups and disaster recovery
- Compliance friendly

**Cons:**
- Higher infrastructure costs
- Complex connection management
- Difficult cross-tenant analytics
- Higher operational overhead

**Example Usage:**
```java
// Register tenant database
POST /api/separate-database/register/tenant1
{
  "dbUrl": "jdbc:postgresql://tenant1-db.example.com/tenant1_db"
}

// Insert into tenant database
POST /api/separate-database/insert/tenant1
{
  "data": "Tenant-specific data in dedicated DB"
}

// Query tenant database
GET /api/separate-database/query/tenant1
```

### 7. Tenant Isolation Pattern (`TenantIsolationPattern.java`)
**Purpose:** Enforce data isolation and prevent cross-tenant access  
**Use Case:** Security layer for multi-tenant applications  
**Key Features:**
- Request validation
- Cross-tenant access prevention
- Resource ownership verification
- Audit logging for violations
- Configurable isolation rules

**Example Usage:**
```java
// Access resource with tenant validation
GET /api/tenant-isolation/resource/res1
Headers: X-Tenant-ID: tenant1

// Success if tenant1 owns res1
// TenantIsolationViolationException if tenant mismatch
```

### 8. Tenant Routing Pattern (`TenantRoutingPattern.java`)
**Purpose:** Route requests to tenant-specific resources/services  
**Use Case:** Distributed multi-tenant architecture  
**Key Features:**
- Tenant-to-service mapping
- Dynamic routing rules
- Load balancing per tenant
- Geo-based routing
- Service discovery integration

**Example Usage:**
```java
// Register tenant route
POST /api/tenant-routing/register
{
  "tenantId": "tenant1",
  "url": "https://tenant1.services.example.com"
}

// Get routed URL for tenant
GET /api/tenant-routing/route/tenant1?path=/api/data
Response: {
  "tenantId": "tenant1",
  "requestPath": "/api/data",
  "routedUrl": "https://tenant1.services.example.com/api/data"
}
```

## Pattern Comparison Matrix

| Pattern | Isolation Level | Cost | Scalability | Complexity | Best For |
|---------|----------------|------|-------------|------------|----------|
| Shared Schema | Low | Very Low | Medium | Low | Startups, many small tenants |
| Separate Schema | Medium | Low | Medium | Medium | Growing SaaS, 100s of tenants |
| Separate Database | High | High | High | High | Enterprise, regulated industries |
| Tenant Identification | N/A | N/A | N/A | Low | Foundation for all patterns |
| Tenant Context | N/A | N/A | N/A | Low | Required for all patterns |
| Tenant Resolver | N/A | N/A | N/A | Medium | Configuration management |
| Tenant Isolation | Security | Low | N/A | Medium | Security layer |
| Tenant Routing | N/A | Medium | High | High | Distributed architecture |

## Choosing the Right Pattern

### Decision Tree

```
How many tenants? 
├─ < 100 tenants
│  ├─ Simple requirements → Shared Schema
│  └─ Need isolation → Separate Schema
├─ 100-1000 tenants
│  ├─ Budget conscious → Separate Schema
│  └─ Need performance → Mix of Separate Schema + Database
└─ > 1000 tenants
   ├─ Enterprise customers → Separate Database
   └─ SMB customers → Shared Schema with sharding
```

### By Industry

**Financial Services / Healthcare (High Compliance)**
- Use: Separate Database
- Reason: Maximum isolation, audit requirements

**SaaS Startups (Cost-sensitive)**
- Use: Shared Schema
- Reason: Low cost, simple management

**Enterprise SaaS (Mixed Customer Sizes)**
- Use: Hybrid approach
  - Enterprise customers: Separate Database
  - SMB customers: Separate Schema

**E-commerce Platforms**
- Use: Separate Schema
- Reason: Balance between isolation and cost

## Implementation Strategies

### Strategy 1: Pure Shared Schema
```
Database: app_db
Tables: 
  - users (id, tenant_id, name, email)
  - products (id, tenant_id, name, price)
  - orders (id, tenant_id, customer_id, total)

@Where(clause = "tenant_id = :tenantId")
@Entity
public class Product { ... }
```

### Strategy 2: Pure Separate Schema
```
Database: app_db
Schemas:
  - tenant1_schema
  - tenant2_schema
  - tenant3_schema

Each schema has same structure:
  - users (id, name, email)
  - products (id, name, price)
  - orders (id, customer_id, total)
```

### Strategy 3: Pure Separate Database
```
Databases:
  - tenant1_db (host: db1.example.com)
  - tenant2_db (host: db2.example.com)
  - tenant3_db (host: db3.example.com)

Each database independent:
  - Own backup schedule
  - Own scaling
  - Own geographic location
```

### Strategy 4: Hybrid Approach
```
Shared for configuration:
  - Database: config_db
  - Tables: tenants, features, subscriptions

Separate for tenant data:
  - Enterprise: Separate Database
  - SMB: Shared Schema with sharding
```

## Configuration

### application.properties

```properties
# Tenant Identification
tenancy.identification.strategy=header
tenancy.header.name=X-Tenant-ID
tenancy.default.tenant=default

# Shared Schema
spring.datasource.url=jdbc:postgresql://localhost/shared_db
spring.jpa.properties.hibernate.multiTenancy=DISCRIMINATOR

# Separate Schema
tenancy.schema.prefix=tenant_
spring.datasource.url=jdbc:postgresql://localhost/app_db

# Separate Database
tenancy.database.config-source=database
tenancy.database.default-driver=org.postgresql.Driver

# Tenant Isolation
tenancy.isolation.enabled=true
tenancy.isolation.strict-mode=true
tenancy.isolation.audit-violations=true

# Tenant Routing
tenancy.routing.enabled=true
tenancy.routing.load-balancing=round-robin
```

## Database Schema Examples

### Shared Schema Approach
```sql
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  tenant_id VARCHAR(50) NOT NULL,
  username VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(tenant_id, email)
);

CREATE INDEX idx_users_tenant ON users(tenant_id);

-- Enforce tenant isolation at DB level
ALTER TABLE users ADD CONSTRAINT check_tenant 
  CHECK (tenant_id = current_setting('app.current_tenant'));
```

### Separate Schema Approach
```sql
-- Create schema for each tenant
CREATE SCHEMA tenant1;
CREATE SCHEMA tenant2;

-- Each schema has same structure
CREATE TABLE tenant1.users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(100) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE tenant2.users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(100) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL
);
```

## Security Best Practices

1. **Always validate tenant context** before data access
2. **Use row-level security** in database
3. **Implement audit logging** for all tenant operations
4. **Encrypt sensitive tenant data** at rest and in transit
5. **Isolate tenant backups**
6. **Implement rate limiting** per tenant
7. **Use connection pooling** per tenant (separate DB)
8. **Monitor cross-tenant queries** and alert on violations
9. **Implement tenant-level RBAC**
10. **Regular security audits** for data leakage

## Performance Optimization

### Shared Schema
```java
// Use database views with tenant filter
CREATE VIEW tenant_users AS
SELECT * FROM users 
WHERE tenant_id = current_setting('app.current_tenant');

// Index on tenant_id
CREATE INDEX idx_tenant_id ON users(tenant_id) 
  INCLUDE (id, email, created_at);
```

### Separate Schema
```java
// Schema caching
@Cacheable("tenant-schemas")
public String getTenantSchema(String tenantId) {
    return "tenant_" + tenantId;
}

// Connection pooling per schema
HikariConfig config = new HikariConfig();
config.setSchema("tenant_" + tenantId);
```

### Separate Database
```java
// Connection pool per tenant
Map<String, DataSource> tenantDataSources;

// Lazy connection creation
public DataSource getDataSource(String tenantId) {
    return tenantDataSources.computeIfAbsent(tenantId, 
        this::createDataSource);
}
```

## Migration Strategies

### Onboarding New Tenant

**Shared Schema:**
```java
// Just insert tenant record
INSERT INTO tenants (id, name) VALUES ('tenant1', 'Tenant One');
// Data automatically isolated by tenant_id
```

**Separate Schema:**
```java
// Create schema
CREATE SCHEMA tenant_tenant1;
// Copy schema structure
pg_dump --schema-only template_schema | 
  psql -d app_db --set SCHEMA=tenant_tenant1
```

**Separate Database:**
```java
// Create database
CREATE DATABASE tenant1_db;
// Restore from template
pg_restore -d tenant1_db template.dump
```

## Testing

```bash
# Test tenant identification
curl http://localhost:8080/api/tenant-identification/current \
  -H "X-Tenant-ID: tenant1"

# Test tenant isolation
curl http://localhost:8080/api/tenant-isolation/resource/res1 \
  -H "X-Tenant-ID: tenant1"

# Test tenant routing
curl http://localhost:8080/api/tenant-routing/route/tenant1?path=/api/data
```

## Monitoring & Observability

### Key Metrics
- Tenant request rate
- Tenant-specific errors
- Database connections per tenant
- Query performance per tenant
- Storage usage per tenant
- Isolation violations

### Logging
```java
@Aspect
public class TenantLoggingAspect {
    @Before("execution(* com.example..*(..))")
    public void logTenantAccess(JoinPoint joinPoint) {
        String tenantId = TenantContext.getTenantId();
        log.info("Tenant {} accessing {}", tenantId, joinPoint.getSignature());
    }
}
```

## Troubleshooting

### Common Issues

**Issue: Cross-tenant data leakage**
```java
// Solution: Always add tenant filter
@Where(clause = "tenant_id = :tenantId")
// Or use database RLS
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
```

**Issue: Performance degradation in shared schema**
```java
// Solution: Partition tables by tenant_id
CREATE TABLE users (
  ...
) PARTITION BY LIST (tenant_id);
```

**Issue: Too many database connections**
```java
// Solution: Connection pooling with limits
hikari.maximum-pool-size=10
tenancy.max-connections-per-tenant=5
```

## License

These patterns are provided as educational examples for Spring Boot multi-tenancy implementation.
