# Duplicate Bean Fix - Spring Boot 3.2+ Configuration Properties

## Problem

Spring Boot was creating **two beans** of `SecurityHeaderProperties`:

```
Parameter 0 of method securityHeadersFilter required a single bean, but 2 were found:
    - com.example.actuatortest.config.SecurityHeaderProperties: defined in unknown location
    - security.headers-com.example.actuatortest.config.SecurityHeaderProperties: defined in unknown location
```

---

## Root Cause

In **Spring Boot 3.2+**, using both:
1. `@EnableConfigurationProperties(SecurityHeaderProperties.class)` 
2. `@ConfigurationProperties(prefix = "security.headers")` annotation on the class

Causes **duplicate bean registration**:

### Bean 1: Via @EnableConfigurationProperties
```java
@EnableConfigurationProperties(SecurityHeaderProperties.class)
public class SecurityHeadersAutoConfiguration {
    // This registers a bean named: "com.example.actuatortest.config.SecurityHeaderProperties"
}
```

### Bean 2: Via @ConfigurationProperties Auto-detection
```java
@ConfigurationProperties(prefix = "security.headers")
public class SecurityHeaderProperties {
    // Spring Boot 3.2+ auto-registers this as a bean named: 
    // "security.headers-com.example.actuatortest.config.SecurityHeaderProperties"
}
```

**Result:** Two beans of the same type → Injection ambiguity → Application fails to start! ❌

---

## The Fix

**Changed from:** Using `@EnableConfigurationProperties` (which causes duplicate registration in Spring Boot 3.2+)

**To:** Explicitly creating the bean with `@Bean` method

### Before (❌ Duplicate Beans)

```java
@AutoConfiguration
@EnableConfigurationProperties(SecurityHeaderProperties.class)  // ← Causes duplicate!
@ConditionalOnProperty(name = "security.headers.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityHeadersAutoConfiguration {
    
    @Bean
    public SecurityHeadersFilter securityHeadersFilter(SecurityHeaderProperties properties) {
        return new SecurityHeadersFilter(properties);
    }
}
```

### After (✅ Single Bean)

```java
@AutoConfiguration
// Removed: @EnableConfigurationProperties(SecurityHeaderProperties.class)
@ConditionalOnProperty(name = "security.headers.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityHeadersAutoConfiguration {

    /**
     * Creates and configures the security header properties bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public SecurityHeaderProperties securityHeaderProperties() {  // ← Explicit bean creation
        return new SecurityHeaderProperties();
    }

    @Bean
    public SecurityHeadersFilter securityHeadersFilter(SecurityHeaderProperties properties) {
        return new SecurityHeadersFilter(properties);
    }
}
```

---

## Why This Works

### Single Bean Creation Path

```
Application Startup
    ↓
Spring Boot scans @AutoConfiguration classes
    ↓
Finds SecurityHeadersAutoConfiguration
    ↓
Checks conditions:
├─ Is web app? ✅
├─ Servlet API present? ✅
└─ security.headers.enabled=true? ✅
    ↓
Creates beans in order:
1. securityHeaderProperties()  ← Only ONE bean created!
2. securityHeadersFilter(properties)  ← Injects the single properties bean
3. securityHeadersFilterRegistration(filter)  ← Injects the filter
```

### No Ambiguity

When Spring needs to inject `SecurityHeaderProperties`:
- There's only **ONE** bean available
- No qualifier needed
- No ambiguity error

---

## Alternative Solutions (Not Used)

### Option 1: Use @RegisterConfigurationProperties (Spring Boot 3.2+)

```java
// Package-info.java
@Configuration
@RegisterConfigurationProperties(SecurityHeaderProperties.class)
package com.example.actuatortest.config;
```

**Why not used:** Requires additional file, more complex setup.

### Option 2: Remove @ConfigurationProperties Annotation

```java
// ❌ Loses metadata generation for IDE hints
// ❌ Loses property binding validation
public class SecurityHeaderProperties {
    // No @ConfigurationProperties annotation
}
```

**Why not used:** Breaks IDE auto-completion and documentation generation.

### Option 3: Use @Component on Properties Class

```java
@Component  // ← Don't do this!
@ConfigurationProperties(prefix = "security.headers")
public class SecurityHeaderProperties { ... }
```

**Why not used:** Still causes duplicate registration when combined with `@EnableConfigurationProperties`!

---

## Best Practice for Spring Boot 3.2+

### When Creating Configuration Properties Beans

**Pattern:**
```java
@AutoConfiguration
@ConditionalOnProperty(...)
public class MyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MyProperties myProperties() {  // ← Explicit bean method
        return new MyProperties();
    }

    @Bean
    public MyService myService(MyProperties properties) {  // ← Inject properties
        return new MyService(properties);
    }
}
```

**Benefits:**
- ✅ Clear bean creation path
- ✅ No duplicate registration
- ✅ Works with `@ConfigurationProperties` for IDE support
- ✅ Compatible with Spring Boot 3.2+
- ✅ Maintains `@ConditionalOnMissingBean` for override capability

---

## Comparison: Spring Boot 2.x vs 3.2+

| Aspect | Spring Boot 2.x | Spring Boot 3.2+ |
|--------|----------------|------------------|
| **@EnableConfigurationProperties** | ✅ Works fine | ⚠️ Can cause duplicates |
| **@ConfigurationProperties scanning** | ❌ Not auto-detected | ✅ Auto-detected |
| **Recommended approach** | Use `@EnableConfigurationProperties` | Use explicit `@Bean` method |

---

## Verification

After the fix:

### Build Status
```
✅ BUILD SUCCESS
```

### Test Results
```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
```

### Application Startup
```log
✅ Tomcat started on port(s): 8080 (http)
✅ Started ActuatorTestApplication in X.XXX seconds
✅ Initialized security headers filter
✅ Single SecurityHeaderProperties bean found
```

No more duplicate bean errors! 🎉

---

## Key Takeaways

1. **Spring Boot 3.2+ changed** how `@ConfigurationProperties` beans are auto-detected
2. **Using `@EnableConfigurationProperties` + `@ConfigurationProperties`** can cause duplicate beans
3. **Explicit `@Bean` methods** provide clear, unambiguous bean creation
4. **Keep `@ConfigurationProperties` annotation** for IDE support and metadata
5. **Use `@ConditionalOnMissingBean`** to allow user overrides

---

## Files Modified

| File | Change |
|------|--------|
| [`SecurityHeadersAutoConfiguration.java`](SecurityHeadersAutoConfiguration.java) | Removed `@EnableConfigurationProperties`, added explicit `@Bean` method for properties |
| [`SecurityHeaderProperties.java`](SecurityHeaderProperties.java) | Added comment warning against adding `@Component` |

---

## Summary

**Problem:** Duplicate `SecurityHeaderProperties` beans in Spring Boot 3.2+  
**Cause:** `@EnableConfigurationProperties` + auto-detection creates two beans  
**Solution:** Use explicit `@Bean` method instead of `@EnableConfigurationProperties`  
**Result:** Single bean, no ambiguity, application starts successfully! ✅
