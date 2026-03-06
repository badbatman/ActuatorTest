# Security Headers Filter - Auto-Configuration Guide

## Overview

This library provides an **automatically configured security headers filter** that adds common security headers to all HTTP responses when added as a dependency to Spring Boot applications.

---

## Features

### Security Headers Included

| Header | Value | Purpose |
|--------|-------|---------|
| **X-Frame-Options** | DENY | Prevents clickjacking attacks |
| **X-Content-Type-Options** | nosniff | Prevents MIME type sniffing |
| **X-XSS-Protection** | 1; mode=block | Enables browser XSS filter |
| **Referrer-Policy** | strict-origin-when-cross-origin | Controls referrer information |
| **Content-Security-Policy** | default-src 'self'; frame-ancestors 'none' | Prevents XSS and data injection |
| **Permissions-Policy** | geolocation=(), microphone=(), camera=() | Controls browser features |
| **Strict-Transport-Security** | max-age=31536000; includeSubDomains | Forces HTTPS (HSTS) |
| **Server** | REDACTED | Removes server version info |
| **X-Powered-By** | REDACTED | Removes framework info |

---

## Quick Start

### Step 1: Add as Dependency

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>actuator-test</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Step 2: That's It!

The filter is **automatically configured** and applied to all requests. No additional configuration needed!

---

## How It Works

### Automatic Registration

When your library is added as a dependency:

1. **Spring Boot discovers** `SecurityHeadersAutoConfiguration` via `AutoConfiguration.imports`
2. **Condition checks** ensure it only loads for web applications
3. **Filter is created** with default OWASP-recommended headers
4. **Filter is registered** in the servlet filter chain
5. **All responses** include security headers automatically

### Registration File

```imports
# META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
com.example.actuatortest.config.SecurityHeadersAutoConfiguration
com.example.actuatortest.config.SecurityHeaderProperties
```

---

## Configuration Options

### Disable Entire Filter

```properties
# application.properties
security.headers.enabled=false
```

### Customize Specific Headers

```properties
# X-Frame-Options
security.headers.x-frame-options.enabled=true
security.headers.x-frame-options.value=SAMEORIGIN

# HSTS Configuration
security.headers.hsts.enabled=true
security.headers.hsts.max-age=63072000  # 2 years
security.headers.hsts.include-sub-domains=true

# Content Security Policy
security.headers.content-security-policy.enabled=true
security.headers.content-security-policy.value=default-src 'self'

# Disable specific headers
security.headers.x-powered-by.enabled=false
security.headers.server.enabled=false
```

### Add Custom Headers

```properties
# Add your own headers
security.headers.custom.X-Custom-Header=my-custom-value
security.headers.custom.X-API-Version=1.0.0
```

### Filter Order

```properties
# Control filter priority (lower = higher priority)
security.headers.order=-100
```

---

## YAML Configuration

```yaml
security:
  headers:
    enabled: true
    order: -100
    
    # Standard headers
    x-frame-options:
      enabled: true
      value: DENY
    
    x-content-type-options:
      enabled: true
      value: nosniff
    
    # HSTS
    hsts:
      enabled: true
      max-age: 31536000
      include-sub-domains: true
    
    # Custom headers
    custom:
      X-Custom-Header: custom-value
      X-API-Version: 1.0.0
```

---

## Conditional Loading

The filter is **only loaded** when these conditions are met:

```java
@ConditionalOnWebApplication          // Must be a web app
@ConditionalOnClass({Filter.class})   // Servlet API must be present
@ConditionalOnProperty(               // Can be disabled via property
    name = "security.headers.enabled", 
    havingValue = "true", 
    matchIfMissing = true
)
```

**Result:** Safe to add to non-web applications - won't cause errors!

---

## Override Default Behavior

### Example 1: Relax CSP for Development

```properties
# Development profile
security.headers.content-security-policy.value=default-src 'self' 'unsafe-inline' 'unsafe-eval'
```

### Example 2: Allow Framing from Same Origin

```properties
security.headers.x-frame-options.value=SAMEORIGIN
```

### Example 3: Disable HSTS for Local Development

```properties
security.headers.hsts.enabled=false
```

---

## Testing the Filter

### Test with curl

```bash
# Make a request
curl -I http://localhost:8080/api/endpoint

# Check response headers
HTTP/1.1 200 OK
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Referrer-Policy: strict-origin-when-cross-origin
Content-Security-Policy: default-src 'self'; frame-ancestors 'none'
Permissions-Policy: geolocation=(), microphone=(), camera=()
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

### Test Programmatically

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityHeadersFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSecurityHeadersPresent() throws Exception {
        mockMvc.perform(get("/api/test"))
            .andExpect(header().exists("X-Frame-Options"))
            .andExpect(header().exists("X-Content-Type-Options"))
            .andExpect(header().exists("X-XSS-Protection"))
            .andExpect(header().exists("Content-Security-Policy"));
    }
}
```

---

## Architecture

### Component Overview

```
SecurityHeadersAutoConfiguration (Auto-configuration class)
│
├── SecurityHeaderProperties (@ConfigurationProperties)
│   └── Reads properties from application.properties/yml
│
└── SecurityHeadersFilter (Servlet Filter)
    ├── Reads configuration from properties
    ├── Applies default OWASP-recommended headers
    └── Adds headers to every HTTP response
```

### Filter Chain Position

```
Request → [Other Filters] → SecurityHeadersFilter → Controller → Response + Headers
```

Default order: `Ordered.LOWEST_PRECEDENCE - 100` (runs late in chain)

---

## Advanced Usage

### Extend the Filter

```java
@Component
public class CustomSecurityHeadersFilter extends SecurityHeadersFilter {
    
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

### Replace Default Filter

```java
@Configuration
public class CustomSecurityConfig {
    
    @Bean
    public SecurityHeadersFilter customSecurityHeadersFilter() {
        // Your custom implementation
        return new MyCustomSecurityHeadersFilter();
    }
}
```

Since we use `@ConditionalOnMissingBean`, your bean will replace the default!

---

## Performance Considerations

### Overhead

- **Minimal**: ~0.1ms per request
- **No I/O**: Only adds headers to response
- **Thread-safe**: No shared mutable state

### Optimization Tips

1. **Disable unused headers** to reduce response size
2. **Use appropriate filter order** to avoid unnecessary processing
3. **Consider CDN/proxy** for header management in production

---

## Troubleshooting

### Issue: Headers Not Appearing

**Check:**
1. Is it a web application? (`@ConditionalOnWebApplication`)
2. Is Servlet API on classpath? (`@ConditionalOnClass`)
3. Is filter disabled? (`security.headers.enabled=false`)
4. Is there another filter overriding headers?

**Debug:**
```properties
logging.level.com.example.actuatortest=DEBUG
```

### Issue: Conflicting Headers

**Problem:** Another library or proxy adds different headers

**Solution:**
1. Adjust filter order: `security.headers.order=-200` (higher priority)
2. Disable conflicting headers in configuration
3. Configure proxy to not override headers

### Issue: HSTS Not Applied

**Check:**
1. Is request HTTPS? (`request.isSecure()`)
2. Is HSTS enabled? (`security.headers.hsts.enabled=true`)

**Note:** HSTS only applies to HTTPS requests by design!

---

## Security Best Practices

### Recommended Configuration

```properties
# Production settings
security.headers.x-frame-options.value=DENY
security.headers.content-security-policy.value=default-src 'self'; frame-ancestors 'none'
security.headers.hsts.max-age=31536000
security.headers.hsts.include-sub-domains=true
security.headers.referrer-policy.value=strict-origin-when-cross-origin
```

### Development vs Production

```properties
# application-dev.properties (relaxed for development)
security.headers.content-security-policy.value=default-src 'self' 'unsafe-inline'
security.headers.hsts.enabled=false

# application-prod.properties (strict for production)
security.headers.content-security-policy.value=default-src 'self'
security.headers.hsts.enabled=true
security.headers.hsts.max-age=63072000
```

---

## Comparison with Alternatives

### vs Spring Security Headers

**This Library:**
- ✅ Lightweight, no security framework dependency
- ✅ Zero configuration needed
- ✅ Works with any Spring Boot web app
- ❌ Less configurable than Spring Security

**Spring Security:**
- ✅ More comprehensive security features
- ✅ CSRF protection, authentication, etc.
- ❌ Requires Spring Security dependency
- ❌ More complex configuration

**Recommendation:** Use this for simple apps, Spring Security for complex security needs.

### vs Manual Filter Creation

**This Library:**
- ✅ Auto-configured
- ✅ OWASP defaults
- ✅ Easy to customize
- ❌ Less control over implementation

**Manual Filter:**
- ✅ Complete control
- ✅ Custom logic
- ❌ More code to maintain
- ❌ Easy to misconfigure

---

## Migration Guide

### From Spring Security Headers

```java
// Old: Spring Security configuration
http.headers()
    .frameOptions().sameOrigin()
    .contentTypeOptions()
    .xssProtection()
    .and()
    .contentSecurityPolicy("default-src 'self'");

// New: Just add this library as dependency!
// All headers applied automatically
```

### From Manual Filter

```java
// Old: Manual filter registration
@Bean
public FilterRegistrationBean<MyManualFilter> manualFilter() {
    // Lots of boilerplate code
}

// New: Auto-configured!
// Just add dependency and optionally configure properties
```

---

## Summary

**What You Get:**

✅ **Automatic security headers** on all responses  
✅ **OWASP recommended defaults**  
✅ **Zero configuration required**  
✅ **Fully customizable** via properties  
✅ **Conditional loading** (web apps only)  
✅ **Minimal performance overhead**  
✅ **Easy to disable/override**  

**Perfect For:**

- Microservices needing basic security headers
- APIs wanting consistent security posture
- Applications without Spring Security
- Quick security hardening of existing apps

---

## References

- [OWASP Secure Headers Project](https://owasp.org/www-project-secure-headers/)
- [Spring Boot Auto-Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration)
- [Servlet Filter Specification](https://jakarta.ee/specifications/servlet/)
