# Spring Boot Actuator Handler Mapping Issue - Troubleshooting Guide

## Problem Description

When creating a custom health endpoint with `@Endpoint(id = "health")`, you may encounter:
- 404 error when accessing `/actuator/health`
- `SimpleUrlHandlerMapping` is used instead of `WebMvcEndpointHandlerMapping`
- The custom endpoint doesn't override the built-in health endpoint properly

## Root Cause Analysis

### Why This Happens

1. **Spring Boot Actuator Architecture**:
   - Actuator endpoints are exposed through `WebEndpointDiscoverer`
   - Web exposure is handled by `WebMvcEndpointHandlerMapping` (for MVC) or `WebFluxEndpointHandlerMapping` (for WebFlux)
   - Built-in endpoints like `HealthEndpoint` are registered as `ExposableEndpoint`

2. **The Conflict**:
   - When you create `@Endpoint(id = "health")`, you're creating a **new** endpoint, not overriding the existing one
   - Spring Boot sees two endpoints with id "health"
   - The built-in `HealthEndpoint` takes precedence in the web layer
   - Your custom endpoint may be ignored or cause conflicts

3. **Handler Mapping Issue**:
   - `WebMvcEndpointHandlerMapping` maps actuator endpoints to `/actuator/**`
   - When there's a conflict or misconfiguration, Spring falls back to `SimpleUrlHandlerMapping`
   - `SimpleUrlHandlerMapping` doesn't know about your custom endpoint → 404

## How to Troubleshoot

### Step 1: Enable Debug Logging

Add to `application.properties`:
```properties
logging.level.org.springframework.boot.actuate=DEBUG
logging.level.org.springframework.web.servlet.handler=DEBUG
logging.level.org.springframework.boot.autoconfigure.web.servlet.WelcomePageHandlerMapping=DEBUG
```

### Step 2: Check Endpoint Registration

Add a `CommandLineRunner` to list all registered endpoints:

```java
@Bean
public CommandLineRunner checkEndpoints(ApplicationContext ctx) {
    return args -> {
        String[] endpointBeans = ctx.getBeanNamesForType(Endpoint.class);
        System.out.println("Found " + endpointBeans.length + " endpoint beans:");
        for (String beanName : endpointBeans) {
            System.out.println("  - " + beanName);
        }
    };
}
```

### Step 3: Check Handler Mapping

Access the mappings endpoint (if enabled):
```bash
curl http://localhost:8080/actuator/mappings
```

Or check logs for handler mapping registration.

## Solutions

### Solution 1: Use HealthIndicator (Recommended for Health Checks)

Instead of creating a custom `@Endpoint`, implement `HealthIndicator`:

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

This **adds** to the existing health endpoint rather than replacing it.

### Solution 2: Disable Built-in Health Endpoint and Use Custom Web Endpoint

```java
@Configuration
public class ActuatorConfig {
    
    @Bean
    @ConditionalOnAvailableEndpoint(endpoint = CustomHealthEndpoint.class)
    public CustomHealthEndpoint customHealthEndpoint() {
        return new CustomHealthEndpoint();
    }
}

// Then disable the built-in health endpoint
management.endpoint.health.enabled=false
```

### Solution 3: Use EndpointExtension (Proper Override)

Create a `HealthEndpointContributor` to extend the built-in endpoint:

```java
@Component
@ConditionalOnAvailableEndpoint(endpoint = HealthEndpoint.class)
public class CustomHealthEndpointContributor {
    
    private final HealthEndpoint healthEndpoint;
    
    public CustomHealthEndpointContributor(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }
    
    // Add custom details while keeping original functionality
}
```

### Solution 4: Full Control with Custom Controller

If you need complete control, create a regular `@RestController`:

```java
@RestController
@RequestMapping("/actuator")
public class CustomActuatorController {
    
    @GetMapping("/health")
    public ResponseEntity<Health> health() {
        return ResponseEntity.ok(Health.up().build());
    }
}
```

But you'll need to configure security and exposure manually.

## Verification Steps

After applying a solution:

1. Start the application
2. Check logs for `WebMvcEndpointHandlerMapping` registration
3. Look for lines like: "Mapped "{[/actuator/health]}" onto `...`
4. Test the endpoint:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

Expected response should include your custom details.

## Key Takeaways

1. **Don't use `@Endpoint(id = "health")`** to override built-in health endpoint
2. Use `HealthIndicator` to contribute to health status
3. `WebMvcEndpointHandlerMapping` handles actuator endpoints, not `SimpleUrlHandlerMapping`
4. Conflicts between custom and built-in endpoints cause handler mapping issues
5. Always check debug logs to understand which handler mapping is being used
