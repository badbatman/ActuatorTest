# Security Headers Filter - Quick Start Guide

## Add Dependency & Go! 🚀

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>actuator-test</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**That's it!** Security headers are automatically added to all responses.

---

## Default Headers Applied

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

---

## Common Configurations

### Disable Specific Headers

```properties
security.headers.x-powered-by.enabled=false
security.headers.server.enabled=false
```

### Customize HSTS

```properties
security.headers.hsts.max-age=63072000        # 2 years
security.headers.hsts.include-sub-domains=true
```

### Relax CSP for Development

```properties
security.headers.content-security-policy.value=default-src 'self' 'unsafe-inline'
```

### Add Custom Headers

```properties
security.headers.custom.X-API-Version=1.0.0
security.headers.custom.X-Custom-Header=my-value
```

### Change Filter Order

```properties
security.headers.order=-200  # Lower = higher priority
```

### Disable Entire Filter

```properties
security.headers.enabled=false
```

---

## YAML Configuration

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

## Verify It Works

```bash
curl -I http://localhost:8080/api/endpoint
```

Look for security headers in response!

---

## Files Created

```
src/main/java/com/example/actuatortest/config/
├── SecurityHeadersFilter.java              # The filter
├── SecurityHeaderProperties.java           # Configuration properties
└── SecurityHeadersAutoConfiguration.java   # Auto-configuration

META-INF/spring/
└── org.springframework.boot.autoconfigure.AutoConfiguration.imports
    └── Registers the auto-configuration classes
```

---

## Key Features

✅ **Zero configuration required**  
✅ **OWASP recommended defaults**  
✅ **Auto-detected for web apps only**  
✅ **Fully customizable via properties**  
✅ **Can be disabled/overridden**  
✅ **Minimal performance impact**  

---

## Need Help?

See [`SECURITY_HEADERS_GUIDE.md`](SECURITY_HEADERS_GUIDE.md) for complete documentation!
