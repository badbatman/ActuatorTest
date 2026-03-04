# Spring Boot 3.x Auto-Configuration - No spring.factories Needed!

## Why We Removed spring.factories

Great question! In **Spring Boot 3.x**, the auto-configuration mechanism has been modernized.

---

## Old Way (Spring Boot 2.x) ❌

```properties
# src/main/resources/META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.example.actuatortest.config.ActuatorEndpointAutoConfiguration,\
com.example.actuatortest.config.CustomHealthEndpointAutoConfiguration
```

**Problems:**
- String-based configuration (no type safety)
- Easy to make typos
- Hard to refactor
- No IDE autocomplete
- Separate file from actual code

---

## New Way (Spring Boot 3.x) ✅

```java
// ActuatorEndpointAutoConfiguration.java
@AutoConfiguration
public class ActuatorEndpointAutoConfiguration implements EnvironmentPostProcessor {
    // Implementation
}

// CustomHealthEndpointAutoConfiguration.java  
@AutoConfiguration
public class CustomHealthEndpointAutoConfiguration {
    @Bean
    public HealthIndicator customHealthIndicator() {
        // Implementation
    }
}
```

**Benefits:**
- ✅ Type-safe annotation
- ✅ Discovered automatically via component scanning
- ✅ No separate configuration file needed
- ✅ Better IDE support
- ✅ Easier refactoring
- ✅ Clear intent in code

---

## How @AutoConfiguration Works

### 1. Annotation Definition

`@AutoConfiguration` is a specialized stereotype annotation that combines:
- `@Configuration` - Marks as configuration class
- `@AutoConfigurationMetadata` - Registers for auto-configuration
- Component scanning detection

### 2. Discovery Mechanism

Spring Boot 3 uses **component scanning** instead of service loader:

```java
// Spring Boot internally does something like:
Set<Class<?>> autoConfigs = ComponentScan.scanForAnnotatedClasses(
    classpath, 
    AutoConfiguration.class
);
```

### 3. Loading Order

Auto-configurations are loaded in this order:
1. `@AutoConfiguration` classes from dependencies
2. `@AutoConfiguration` classes from your project
3. User's `@Configuration` classes

---

## What Changed in Our Project

### Before (spring.factories)

```
Files:
├── META-INF/spring.factories                    ← Required
└── config/
    ├── ActuatorEndpointAutoConfiguration.java   ← Plain class
    └── CustomHealthEndpointAutoConfiguration.java ← @Configuration
```

### After (@AutoConfiguration)

```
Files:
├── config/
│   ├── ActuatorEndpointAutoConfiguration.java   ← @AutoConfiguration
│   └── CustomHealthEndpointAutoConfiguration.java ← @AutoConfiguration
└── (No spring.factories needed!)
```

---

## Code Changes

### ActuatorEndpointAutoConfiguration.java

```java
// OLD (Spring Boot 2.x style)
public class ActuatorEndpointAutoConfiguration implements EnvironmentPostProcessor {
    // ...
}

// NEW (Spring Boot 3.x style)
@AutoConfiguration
public class ActuatorEndpointAutoConfiguration implements EnvironmentPostProcessor {
    // ...
}
```

### CustomHealthEndpointAutoConfiguration.java

```java
// OLD
@Configuration
public class CustomHealthEndpointAutoConfiguration {
    @Bean
    public HealthIndicator customHealthIndicator() { ... }
}

// NEW
@AutoConfiguration
public class CustomHealthEndpointAutoConfiguration {
    @Bean
    public HealthIndicator customHealthIndicator() { ... }
}
```

---

## Testing the Change

### Build and Run

```bash
cd /Users/bob/test_project/ActuatorTest
mvn clean compile
mvn spring-boot:run
```

### Verify Auto-Configuration Loaded

Look for in logs:
```
=== Actuator Endpoint Debug Info ===
Found 3 HealthIndicator beans:
  - customHealthIndicator        ← Your auto-configured bean
  - diskSpaceHealthIndicator     ← Spring Boot default
  - pingHealthContributor        ← Spring Boot default
```

---

## Why This Matters

### For Library Authors

If you're creating a Spring Boot starter library:

**Spring Boot 2.x:**
```xml
<!-- Your library JAR -->
├── META-INF/spring.factories          ← Must maintain this
└── com/example/YourAutoConfig.java
```

**Spring Boot 3.x:**
```xml
<!-- Your library JAR -->
└── com/example/YourAutoConfig.java    ← Just use @AutoConfiguration
```

### For Application Developers

- Less boilerplate
- Better IDE integration
- Easier to understand
- More maintainable

---

## Compatibility

| Spring Boot Version | Mechanism | File Needed |
|---------------------|-----------|-------------|
| 2.x and earlier | `spring.factories` | ✅ Required |
| 3.0+ | `@AutoConfiguration` | ❌ Not needed |
| 3.x (transitional) | Both supported | Use `@AutoConfiguration` |

**Note:** Spring Boot 3 still supports `spring.factories` for backward compatibility, but it's deprecated for new development.

---

## Additional @AutoConfiguration Features

### Conditional Loading

```java
@AutoConfiguration
@ConditionalOnProperty(name = "custom.health.enabled", havingValue = "true")
public class CustomHealthEndpointAutoConfiguration {
    // Only loads if property is set
}
```

### Ordering

```java
@AutoConfiguration(before = HealthEndpointAutoConfiguration.class)
public class ActuatorEndpointAutoConfiguration {
    // Loads before built-in health endpoint
}
```

Or with order value:

```java
@AutoConfiguration
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ActuatorEndpointAutoConfiguration {
    // High priority loading
}
```

### Exclusion

Users can exclude your auto-configuration if needed:

```java
@SpringBootApplication(exclude = {CustomHealthEndpointAutoConfiguration.class})
public class MyApplication {
    // Your auto-config won't be loaded
}
```

---

## Migration Checklist

If migrating from spring.factories:

- [ ] Replace `@Configuration` with `@AutoConfiguration`
- [ ] Delete `META-INF/spring.factories`
- [ ] Update documentation
- [ ] Test auto-configuration loading
- [ ] Verify conditional annotations work
- [ ] Check loading order if important

---

## Summary

**Why we removed spring.factories:**

1. ✅ **Spring Boot 3.x** provides better mechanism via `@AutoConfiguration`
2. ✅ **Type-safe** - No string-based configuration
3. ✅ **Cleaner** - No separate configuration file
4. ✅ **Modern** - Uses component scanning
5. ✅ **Maintainable** - Easier refactoring and IDE support

**Result:** Our project now follows Spring Boot 3 best practices! 🎉

---

## References

- [Spring Boot 3 AutoConfiguration Javadoc](https://docs.spring.io/spring-boot/docs/3.0.x/api/org/springframework/boot/autoconfigure/AutoConfiguration.html)
- [Spring Boot 3 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Spring Boot 3.0 Release Notes](https://spring.io/blog/2022/11/24/spring-boot-3-0-goes-ga)
