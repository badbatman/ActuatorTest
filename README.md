# Spring Boot 3 Library Project - Actuator & Security Headers

This project provides **auto-configurable Spring Boot 3 libraries** for:
1. ✅ **Custom Health Endpoint** - Extend actuator health with custom indicators
2. ✅ **Security Headers Filter** - Automatically add OWASP-recommended security headers

Both libraries work out-of-the-box when added as dependencies to Spring Boot applications!

---

## Quick Start

### Add Dependency

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>actuator-test</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### That's It!

Your application now has:
- ✅ Custom health indicator contributing to `/actuator/health`
- ✅ OWASP security headers on all HTTP responses
- ✅ Zero configuration required!

---

## Features

### 1. Custom Health Indicator

Automatically contributes to Spring Boot Actuator's health endpoint.

**Response includes:**
```json
{
  "status": "UP",
  "components": {
    "customHealthIndicator": {
      "status": "UP",
      "details": {
        "custom": "true",
        "service": "my-service",
        "timestamp": 1234567890
      }
    },
    "diskSpace": { ... },
    "ping": { ... }
  }
}
```

### 2. Security Headers Filter

Automatically adds these headers to all HTTP responses:

| Header | Value | Protection |
|--------|-------|------------|
| **X-Frame-Options** | DENY | Clickjacking |
| **X-Content-Type-Options** | nosniff | MIME sniffing |
| **X-XSS-Protection** | 1; mode=block | XSS attacks |
| **Referrer-Policy** | strict-origin-when-cross-origin | Information leakage |
| **Content-Security-Policy** | default-src 'self' | XSS, data injection |
| **Permissions-Policy** | geolocation=(), microphone=(), camera=() | Feature abuse |
| **Strict-Transport-Security** | max-age=31536000; includeSubDomains | HTTPS enforcement |

---

## Configuration (Optional)

### Disable Specific Features

```properties
# Disable security headers (health still works)
security.headers.enabled=false

# Disable health indicator (security headers still work)
management.endpoint.health.enabled=false
```

### Customize Security Headers

```properties
# Relax CSP for development
security.headers.content-security-policy.value=default-src 'self' 'unsafe-inline'

# Allow framing from same origin
security.headers.x-frame-options.value=SAMEORIGIN

# Customize HSTS
security.headers.hsts.max-age=63072000
security.headers.hsts.include-sub-domains=true

# Add custom headers
security.headers.custom.X-API-Version=1.0.0
security.headers.custom.X-Custom-Header=my-value

# Disable specific headers
security.headers.x-powered-by.enabled=false
security.headers.server.enabled=false
```

### YAML Configuration

```yaml
security:
  headers:
    enabled: true
    order: -100
    hsts:
      max-age: 31536000
      include-sub-domains: true
    custom:
      X-API-Version: "1.0.0"
```

---

## Project Structure

```
ActuatorTest/
├── pom.xml                                    # Maven configuration with Spring Boot 3.2.0
├── src/main/java/com/example/actuatortest/
│   ├── ActuatorTestApplication.java          # Main application class
│   └── config/
│       ├── CustomHealthEndpointAutoConfiguration.java  # Health indicator auto-config
│       ├── ActuatorEndpointAutoConfiguration.java      # Endpoint properties
│       ├── SecurityHeadersFilter.java                  # Security headers filter
│       ├── SecurityHeaderProperties.java               # Filter configuration
│       └── SecurityHeadersAutoConfiguration.java       # Filter auto-config
├── src/main/resources/
│   ├── application.properties                # Example configuration
│   └── META-INF/spring/
│       └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
└── src/test/java/
    └── com/example/actuatortest/config/
        └── SecurityHeadersFilterTest.java    # Unit tests
```

---

## Testing

### Run Unit Tests

```bash
cd /Users/bob/test_project/ActuatorTest
mvn clean test
```

Expected output:
```
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Test Security Headers

```bash
# Start application
mvn spring-boot:run

# In another terminal, check response headers
curl -I http://localhost:8080/api/endpoint
```

Expected response:
```http
HTTP/1.1 200 OK
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Referrer-Policy: strict-origin-when-cross-origin
Content-Security-Policy: default-src 'self'; frame-ancestors 'none'
Permissions-Policy: geolocation=(), microphone=(), camera=()
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

### Test Health Endpoint

```bash
curl http://localhost:8080/actuator/health | jq .
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "customHealthIndicator": {
      "status": "UP",
      "details": {
        "custom": "true",
        "service": "my-service"
      }
    },
    "diskSpace": { ... },
    "ping": { ... }
  }
}
```

---

## How It Works

### Auto-Configuration Magic

When you add this library as a dependency:

1. **Spring Boot scans** `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
2. **Discovers configuration classes**:
   - `SecurityHeadersAutoConfiguration`
   - `CustomHealthEndpointAutoConfiguration`
3. **Checks conditions**:
   - Is it a web application? ✅
   - Servlet API present? ✅
   - Not disabled via properties? ✅
4. **Creates beans automatically**:
   - Security headers filter
   - Custom health indicator
5. **Your application now has enhanced security and health monitoring!** 🎉

### Conditional Loading

The library only activates when appropriate:

```java
@ConditionalOnWebApplication      // Only for web apps
@ConditionalOnClass({Filter.class}) // Servlet API required
@ConditionalOnProperty(            // Can be disabled
    name = "security.headers.enabled",
    havingValue = "true",
    matchIfMissing = true
)
```

---

## Advanced Usage

### Override Default Configuration

Create your own bean to replace defaults:

```java
@Configuration
public class CustomConfig {
    
    @Bean
    public SecurityHeadersFilter myCustomFilter() {
        return new MyCustomSecurityHeadersFilter();
    }
}
```

Since we use `@ConditionalOnMissingBean`, your bean replaces the default!

### Extend the Filter

```java
public class MyCustomFilter extends SecurityHeadersFilter {
    
    @Override
    protected void addSecurityHeaders(HttpServletResponse response, HttpServletRequest request) {
        // Call parent for standard headers
        super.addSecurityHeaders(response, request);
        
        // Add custom logic
        if (request.getRequestURI().startsWith("/api/")) {
            response.setHeader("X-API-Security", "enhanced");
        }
    }
}
```

### Integration with Spring Security

If you're using Spring Security, you can still use this filter alongside Spring Security's headers:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                // Spring Security headers
            );
        // Our auto-configured filter runs separately
        return http.build();
    }
}
```

---

## Troubleshooting

### Security Headers Not Appearing

**Check:**
1. Is it a web application? (`@ConditionalOnWebApplication`)
2. Is Servlet API on classpath?
3. Is filter disabled? (`security.headers.enabled=false`)
4. Check logs for: `Loaded EnvironmentPostProcessor`

**Debug:**
```properties
logging.level.com.example.actuatortest=DEBUG
```

### Health Endpoint Returns 404

**Check:**
1. Is actuator enabled? (`management.endpoint.health.enabled=true`)
2. Is exposure configured? (`management.endpoints.web.exposure.include=health`)
3. Check startup logs for endpoint registration

### HSTS Header Not Showing

**Remember:** HSTS only applies to HTTPS requests!
- Make sure request is HTTPS (`request.isSecure()`)
- Check HSTS is enabled: `security.headers.hsts.enabled=true`

### Conflicting Headers

If another library or proxy sets different headers:
1. Adjust filter order: `security.headers.order=-200` (higher priority)
2. Disable conflicting headers in configuration
3. Configure proxy to not override headers

---

1. **404 Error** when accessing `/actuator/health`
2. **SimpleUrlHandlerMapping** is used instead of **WebMvcEndpointHandlerMapping**
3. Custom endpoint doesn't work as expected

### Why This Happens

- Spring Boot Actuator uses `WebEndpointDiscoverer` to discover and expose endpoints
- Web exposure is handled by `WebMvcEndpointHandlerMapping` for MVC applications
- Creating `@Endpoint(id = "health")` creates a **conflict** with the built-in `HealthEndpoint`
- The conflict causes Spring to fall back to `SimpleUrlHandlerMapping`, which doesn't know about your custom endpoint
- Result: 404 Not Found

## The Solution: Use HealthIndicator

Instead of trying to override the entire health endpoint, implement `HealthIndicator`:

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        return Health.up()
                .withDetail("custom", "true")
                .withDetail("service", "my-service")
                .build();
    }
}
```

This approach:
- ✅ Works with the existing health endpoint infrastructure
- ✅ Uses `WebMvcEndpointHandlerMapping` correctly
- ✅ Allows multiple health contributors
- ✅ No handler mapping conflicts

## How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build and Run

```bash
cd /Users/bob/test_project/ActuatorTest
mvn clean package
mvn spring-boot:run
```

### Verify Endpoint Registration

On startup, you'll see output like:
```
=== Actuator Endpoint Debug Info ===
Found X endpoint beans:
  - healthEndpoint
  - ...
Found Y HealthIndicator beans:
  - diskSpaceHealthIndicator
  - pingHealthIndicator
  - customHealthIndicator
===================================
Access health endpoint at: http://localhost:8080/actuator/health
```

### Test the Health Endpoint

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "customHealthIndicator": {
      "status": "UP",
      "details": {
        "custom": "true",
        "service": "my-service",
        "timestamp": 1234567890
      }
    },
    "diskSpace": { ... },
    "ping": { ... }
  }
}
```

## Troubleshooting

### Check Handler Mapping in Logs

Look for these lines in the startup logs:
```
Mapped "{[/actuator/health]}" onto ...
```

This confirms `WebMvcEndpointHandlerMapping` is handling the endpoint.

### Enable Debug Logging

The project includes debug logging configuration. Check logs for:
- `org.springframework.boot.actuate` - Endpoint registration
- `org.springframework.web.servlet.handler` - Handler mapping details

### Common Issues

1. **404 on /actuator/health**
   - Check if `management.endpoints.web.exposure.include=health` is set
   - Verify `WebMvcEndpointHandlerMapping` is being used (check logs)
   - Remove any `@Endpoint(id = "health")` that conflicts

2. **Custom details not showing**
   - Ensure your `HealthIndicator` bean is created (`@Component`)
   - Check bean name appears in startup logs

3. **SimpleUrlHandlerMapping being used**
   - This indicates a configuration issue
   - Check for conflicting endpoint definitions
   - Review debug logs for endpoint discovery issues

## Configuration Reference

### application.properties

```properties
# Enable only health endpoint
management.endpoints.enabled-by-default=false
management.endpoint.health.enabled=true
management.endpoints.web.exposure.include=health

# Debug logging
logging.level.org.springframework.boot.actuate=DEBUG
logging.level.org.springframework.web.servlet.handler=DEBUG
```

## See Also

- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Detailed troubleshooting guide
- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
