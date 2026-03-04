# Spring Boot 3 Actuator Test Project

This project demonstrates the correct way to customize the health endpoint in Spring Boot 3 Actuator.

## Project Structure

```
ActuatorTest/
├── pom.xml                                    # Maven configuration with Spring Boot 3.2.0
├── src/main/java/com/example/actuatortest/
│   ├── ActuatorTestApplication.java          # Main application class with debug helper
│   └── endpoint/
│       ├── CustomHealthEndpoint.java         # ❌ WRONG approach (causes handler mapping issue)
│       └── CustomHealthIndicator.java        # ✅ CORRECT approach (HealthIndicator)
└── src/main/resources/
    └── application.properties                # Actuator configuration
```

## The Problem: Handler Mapping Issue

When you create `@Endpoint(id = "health")` to override the built-in health endpoint, you may encounter:

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
