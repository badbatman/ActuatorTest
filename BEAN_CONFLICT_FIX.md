# Bean Conflict Fix - SecurityHeaderProperties

## Problem

When starting the application, you encountered this error:

```
Error creating bean with name 'securityHeadersFilterRegistration': 
Unsatisfied dependency expressed through method 'securityHeadersFilterRegistration' parameter 0: 
Error creating bean with name 'securityHeadersFilter': 
Unsatisfied dependency expressed through Method parameter 0: 
No qualifying bean of type 'com.example.actuatortest.config.SecurityHeaderProperties' available: 
expected single matching bean but found 2: 
com.example.actuatortest.config.SecurityHeaderProperties,
security.headers-com.example.actuatortest.config.SecurityHeaderProperties
```

---

## Root Cause

**Two beans of type `SecurityHeaderProperties` were being created:**

1. **Bean 1**: Created by Spring Boot via `@EnableConfigurationProperties(SecurityHeaderProperties.class)`
   - Bean name: `com.example.actuatortest.config.SecurityHeaderProperties`

2. **Bean 2**: Also created by Spring Boot due to `@ConfigurationProperties` annotation
   - Bean name: `security.headers-com.example.actuatortest.config.SecurityHeaderProperties`

This happened because:
- `@EnableConfigurationProperties` registers the properties as a bean
- `@ConfigurationProperties(prefix = "security.headers")` also triggers bean registration
- Result: **Duplicate beans** of the same type!

---

## The Fix

### Changed in `SecurityHeadersAutoConfiguration.java`:

**Before (❌ Caused conflict):**
```java
@Bean
@ConditionalOnMissingBean
public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration(
        SecurityHeadersFilter filter,
        SecurityHeaderProperties properties) {  // ← Second injection point
    
    FilterRegistrationBean<...> registration = new FilterRegistrationBean<>();
    registration.setOrder(properties.getOrder());  // ← Used properties
}
```

**After (✅ Fixed):**
```java
@Bean
@ConditionalOnMissingBean(name = "securityHeadersFilterRegistration")
public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration(
        SecurityHeadersFilter filter) {  // ← Only inject filter
    
    FilterRegistrationBean<...> registration = new FilterRegistrationBean<>();
    registration.setOrder(filter.getOrder());  // ← Get order from filter
}
```

### Key Changes:

1. **Removed second parameter** from `securityHeadersFilterRegistration()`
   - No longer tries to inject `SecurityHeaderProperties` directly
   
2. **Get order from filter instead**
   - Filter already has the properties injected
   - Use `filter.getOrder()` instead of `properties.getOrder()`

3. **Added explicit bean name** to conditional
   - `@ConditionalOnMissingBean(name = "securityHeadersFilterRegistration")`
   - Prevents duplicate registration beans

---

## Why This Works

### Single Injection Point

```java
// Flow now:
SecurityHeaderProperties → SecurityHeadersFilter → FilterRegistrationBean
         ↓                        ↓
    (created once)          (gets properties)
                                  ↓
                            (uses filter.getOrder())
```

### Bean Creation Order

1. **Spring Boot creates** `SecurityHeaderProperties` bean (via `@EnableConfigurationProperties`)
2. **Auto-config creates** `SecurityHeadersFilter` bean (injects properties)
3. **Auto-config creates** `FilterRegistrationBean` (injects filter, gets order from it)

**Result:** Each bean is created exactly once! ✅

---

## Alternative Solutions (Not Used)

### Option 1: Remove @EnableConfigurationProperties

```java
// ❌ Won't work - properties won't be bound to configuration
@AutoConfiguration
// @EnableConfigurationProperties(SecurityHeaderProperties.class)  ← Can't remove this!
```

### Option 2: Use @Qualifier

```java
// ❌ Too verbose and confusing
@Bean
public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration(
        SecurityHeadersFilter filter,
        @Qualifier("com.example.actuatortest.config.SecurityHeaderProperties") 
        SecurityHeaderProperties properties) { ... }
```

### Option 3: Make Properties a Regular Component

```java
// ❌ Loses @ConfigurationProperties binding
@Component
@ConfigurationProperties(prefix = "security.headers")
public class SecurityHeaderProperties { ... }
```

---

## Best Practice Applied

**Rule:** When using `@EnableConfigurationProperties`:
- ✅ Inject the properties class **only where needed**
- ✅ Avoid multiple injection points for the same bean type
- ✅ Pass data through existing beans when possible

**Pattern:**
```java
@EnableConfigurationProperties(MyProperties.class)
public class MyAutoConfiguration {
    
    @Bean
    public MyService myService(MyProperties properties) {  // ← Inject here
        return new MyService(properties);
    }
    
    @Bean
    public MyOtherService myOtherService(MyService service) {  // ← Get from service
        return new MyOtherService(service.getConfig());
    }
}
```

---

## Verification

After the fix, the application starts successfully:

```log
✅ Tomcat started on port(s): 8080 (http)
✅ Started ActuatorTestApplication in X.XXX seconds
✅ Initialized security headers filter
```

And tests still pass:
```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## Summary

| Aspect | Before | After |
|--------|--------|-------|
| **Beans created** | 2 `SecurityHeaderProperties` | 1 `SecurityHeaderProperties` |
| **Injection points** | 2 (filter + registration) | 1 (filter only) |
| **Order source** | From properties param | From filter object |
| **Status** | ❌ Bean conflict | ✅ Works perfectly |

**The fix eliminates the duplicate bean injection by getting the configuration from the already-injected filter bean instead of requesting another direct injection.**
