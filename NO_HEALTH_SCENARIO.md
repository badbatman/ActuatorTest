# What Happens When No Health Endpoint is Created?

## Scenario Analysis

### Case 1: Health Endpoint Disabled via Configuration

```properties
management.endpoints.enabled-by-default=false
management.endpoint.health.enabled=false
management.endpoints.web.exposure.include=health
```

**What happens:**
1. ✅ `HealthEndpoint` bean is **NOT created** (due to `@ConditionalOnAvailableEndpoint`)
2. ❌ `/actuator/health` returns **404 Not Found**
3. ✅ Application starts successfully
4. ❌ No health monitoring available

**Test it yourself:**
```bash
# Change application.properties to:
management.endpoint.health.enabled=false

# Then restart and test:
curl http://localhost:8080/actuator/health
# Response: 404 Not Found
```

---

### Case 2: No HealthIndicator Beans at All

Even if the health endpoint is enabled, if there are **NO** `HealthIndicator` beans:

```java
// No @Component implementing HealthIndicator
// No DataSource (no database connection)
// No Redis, RabbitMQ, etc.
```

**What happens:**
1. ✅ `HealthEndpoint` bean **IS created** (auto-configured by Spring Boot)
2. ✅ `/actuator/health` returns **200 OK**
3. ✅ Default health indicators are registered:
   - `diskSpaceHealthIndicator` (checks disk space)
   - `pingHealthContributor` (simple ping check)
4. ✅ Returns minimal health status:
   ```json
   {
     "status": "UP"
   }
   ```

**Why?** Spring Boot Actuator auto-configures these by default:
- `DiskSpaceHealthIndicator` - Always present unless disabled
- `PingHealthContributor` - Simple availability check

---

### Case 3: Completely Remove Actuator Dependency

```xml
<!-- Remove this from pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**What happens:**
1. ❌ No actuator endpoints available
2. ❌ `/actuator/**` returns **404 Not Found**
3. ✅ Application runs as normal web app
4. ❌ No monitoring capabilities

---

## Auto-Configuration Behavior

### HealthEndpointAutoConfiguration

Spring Boot's `HealthEndpointAutoConfiguration` class is responsible for creating the health endpoint:

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnAvailableEndpoint(endpoint = HealthEndpoint.class)
@Import(HealthEndpointConfiguration.class)
public class HealthEndpointAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public HealthContributorRegistry healthContributorRegistry(
        Map<String, HealthContributor> contributors) {
        return new InMemoryHealthContributorRegistry(contributors);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public HealthEndpoint healthEndpoint(
        HealthContributorRegistry registry, 
        HealthEndpointGroups groups) {
        return new HealthEndpoint(registry, groups);
    }
}
```

**Key Conditions:**
- `@ConditionalOnAvailableEndpoint` - Only creates bean if endpoint is enabled
- `@ConditionalOnMissingBean` - Only creates if no custom bean exists

---

## Practical Demonstration

### Test 1: Disable Health Endpoint

**application.properties:**
```properties
management.endpoint.health.enabled=false
```

**Result:**
```bash
$ curl http://localhost:8080/actuator/health
{
  "timestamp": "2026-03-04T15:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/actuator/health"
}
```

**Logs show:**
```
ConditionOnAvailableEndpoint: condition failed on bean 'healthEndpoint'
  Property management.endpoint.health.enabled = false
```

---

### Test 2: Enable Health But No Custom Indicators

**application.properties:**
```properties
management.endpoints.enabled-by-default=true
management.endpoint.health.enabled=true
# No custom HealthIndicator beans
```

**Result:**
```bash
$ curl http://localhost:8080/actuator/health
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 250685511680,
        "free": 100685511680,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**Console output:**
```
=== Actuator Endpoint Debug Info ===
Found 2 HealthIndicator beans:
  - diskSpaceHealthIndicator
  - pingHealthContributor
===================================
```

---

### Test 3: Your Current Configuration (Working)

**application.properties:**
```properties
management.endpoints.enabled-by-default=false
management.endpoint.health.enabled=true
management.endpoints.web.exposure.include=health
```

**Your custom indicator:**
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        return Health.up()
                .withDetail("custom", "true")
                .build();
    }
}
```

**Result:**
```bash
$ curl http://localhost:8080/actuator/health
{
  "status": "UP",
  "components": {
    "customHealthIndicator": {
      "status": "UP",
      "details": {
        "custom": "true"
      }
    },
    "diskSpace": { ... },
    "ping": { ... }
  }
}
```

---

## Summary Table

| Scenario | HealthEndpoint Bean | /actuator/health | Default Indicators | Custom Indicators |
|----------|---------------------|------------------|-------------------|-------------------|
| **Enabled + Custom** | ✅ Created | ✅ 200 OK | ✅ Yes | ✅ Yes |
| **Enabled only** | ✅ Created | ✅ 200 OK | ✅ Yes (disk, ping) | ❌ No |
| **Disabled** | ❌ Not created | ❌ 404 Not Found | ❌ No | ❌ No |
| **No Actuator** | ❌ N/A | ❌ 404 Not Found | ❌ N/A | ❌ N/A |

---

## Key Takeaways

1. **HealthEndpoint is auto-configured** when:
   - `spring-boot-starter-actuator` is in dependencies
   - `management.endpoint.health.enabled=true` (default: true)
   - Endpoint is exposed via `management.endpoints.web.exposure.include`

2. **Default health indicators** are always provided:
   - `DiskSpaceHealthIndicator` - Checks disk space
   - `PingHealthContributor` - Simple availability check

3. **Custom HealthIndicators** add to (not replace) default indicators

4. **404 occurs when:**
   - Health endpoint is disabled (`management.endpoint.health.enabled=false`)
   - Endpoint not exposed (`management.endpoints.web.exposure.include` doesn't include "health")
   - Actuator dependency missing

5. **Your current setup is optimal:**
   ```properties
   management.endpoints.enabled-by-default=false  # Disable all by default
   management.endpoint.health.enabled=true        # Enable health specifically
   management.endpoints.web.exposure.include=health # Expose over HTTP
   ```

---

## Quick Test Commands

```bash
# Check current health
curl http://localhost:8080/actuator/health | jq .

# Check which beans are loaded
curl http://localhost:8080/actuator/beans | jq '.beans | keys'

# Temporarily disable health endpoint
echo "management.endpoint.health.enabled=false" >> application.properties
# Restart app and test again
curl http://localhost:8080/actuator/health
# Expected: 404 Not Found
```
