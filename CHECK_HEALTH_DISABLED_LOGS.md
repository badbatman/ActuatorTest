# How to Check if health.enabled=false in Logs

## Quick Answer

When `management.endpoint.health.enabled=false`, you'll see **NO** health endpoint registration in the logs, and specifically these indicators:

---

## 1. Look for MISSING Health Endpoint Logs

### ✅ When health is ENABLED (normal):
```
Mapped "{[/actuator/health]}" onto ...
Exposing 1 endpoint(s) beneath base path '/actuator'
Found X HealthIndicator beans:
  - customHealthIndicator
  - diskSpaceHealthIndicator
  - pingHealthContributor
```

### ❌ When health is DISABLED:
```
(No mention of /actuator/health mapping)
(No HealthIndicator beans listed)
Endpoint [health] not enabled so will not be exposed
```

---

## 2. Enable Debug Logging to See Details

Add to `application.properties`:

```properties
# Show endpoint auto-configuration decisions
logging.level.org.springframework.boot.actuate=DEBUG
logging.level.org.springframework.boot.autoconfigure.condition=DEBUG
```

---

## 3. What You'll See in DEBUG Logs

### When health.enabled=false:

```log
2026-03-04T23:00:00.000+08:00 DEBUG --- [           main] 
o.s.b.a.ConditionEvaluationReportLogger : 

============================
CONDITIONS EVALUATION REPORT
============================


Positive matches:
-----------------

    None matched for HealthEndpoint


Negative matches:
-----------------

   HealthEndpointAutoConfiguration:
      Did not match:
         - @ConditionalOnAvailableEndpoint marked as unavailable 
           (OnAvailableEndpointCondition)
          
      Matched:
         - @Configuration class found
         
   HealthEndpoint#healthEndpoint:
      Did not match:
         - @ConditionalOnAvailableEndpoint: property 'management.endpoint.health.enabled' = false
```

---

## 4. Key Log Patterns to Search For

### Search for these patterns:

```bash
# In your terminal or log file
grep -i "health" application.log | grep -i "endpoint"
```

### Look for:

#### ❌ Signs that health is DISABLED:

1. **Explicit disable message:**
   ```
   OnAvailableEndpointCondition health endpoint marked as unavailable
   ```

2. **Property evaluation:**
   ```
   management.endpoint.health.enabled = false
   ```

3. **Bean not created:**
   ```
   HealthEndpointAutoConfiguration matched but bean not created
   ```

4. **No handler mapping:**
   ```
   (No "Mapped /actuator/health" message)
   ```

#### ✅ Signs that health is ENABLED:

1. **Endpoint registered:**
   ```
   Mapped "{[/actuator/health]}" onto WebMvcEndpointHandlerMapping
   ```

2. **Endpoints exposed:**
   ```
   Exposing 1 endpoint(s) beneath base path '/actuator'
   ```

3. **Health indicators found:**
   ```
   Found 3 HealthIndicator beans:
     - customHealthIndicator
     - diskSpaceHealthIndicator  
     - pingHealthContributor
   ```

---

## 5. Practical Test

### Step 1: Create test configuration

Create `application-disabled.properties`:

```properties
# Disable health endpoint
management.endpoints.enabled-by-default=false
management.endpoint.health.enabled=false

# Enable debug logging
logging.level.org.springframework.boot.actuate=DEBUG
logging.level.org.springframework.boot.autoconfigure.condition=DEBUG
```

### Step 2: Run with disabled config

```bash
cd /Users/bob/test_project/ActuatorTest
mvn spring-boot:run -Dspring-boot.run.profiles=disabled
```

### Step 3: Check logs

Look for:

```log
✅ EXPECTED OUTPUT (health disabled):

2026-03-04T23:00:00.000+08:00 DEBUG --- [main] o.s.b.a.l.ConditionEvaluationReportLogger
================================================================================
                           CONDITION EVALUATION REPORT
================================================================================

Negative matches:
-----------------

   HealthEndpointAutoConfiguration#healthEndpoint:
      Did not match:
         - @ConditionalOnAvailableEndpoint: 
           management.endpoint.health.enabled is false
```

---

## 6. Quick Runtime Check

While app is running, check if health endpoint exists:

```bash
# Test the endpoint
curl -s http://localhost:8080/actuator/health

# If health.enabled=false, you'll get:
{
  "timestamp": "2026-03-04T15:00:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/actuator/health"
}

# If health.enabled=true, you'll get:
{
  "status": "UP",
  ...
}
```

---

## 7. Complete Log Comparison

### ENABLED (health.enabled=true):

```log
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

2026-03-04T23:28:24.067+08:00  INFO 97617 --- [           main] 
c.e.a.ActuatorTestApplication            : Starting ActuatorTestApplication...

2026-03-04T23:28:24.777+08:00 DEBUG --- [           main] 
o.s.w.s.handler.SimpleUrlHandlerMapping  : Patterns [/webjars/**, /**] in 'resourceHandlerMapping'

2026-03-04T23:28:24.872+08:00  INFO 97617 --- [           main] 
o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint(s) beneath base path '/actuator'
                                                                                              ^^^
                                                    HEALTH ENDPOINT IS EXPOSED!

2026-03-04T23:28:24.905+08:00  INFO 97617 --- [           main] 
o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''

=== Actuator Endpoint Debug Info ===
Found 3 HealthIndicator beans:
  - customHealthIndicator
  - diskSpaceHealthIndicator
  - pingHealthContributor
```

### DISABLED (health.enabled=false):

```log
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

2026-03-04T23:00:00.000+08:00  INFO 97000 --- [           main] 
c.e.a.ActuatorTestApplication            : Starting ActuatorTestApplication...

2026-03-04T23:00:00.500+08:00 DEBUG --- [           main] 
o.s.w.s.handler.SimpleUrlHandlerMapping  : Patterns [/webjars/**, /**] in 'resourceHandlerMapping'

2026-03-04T23:00:00.600+08:00  INFO 97000 --- [           main] 
o.s.b.a.e.web.EndpointLinksResolver      : Exposing 0 endpoint(s) beneath base path '/actuator'
                                                                                              ^^^
                                              NO ENDPOINTS EXPOSED (health disabled)!

2026-03-04T23:00:00.700+08:00  INFO 97000 --- [           main] 
o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''

=== Actuator Endpoint Debug Info ===
Found 0 HealthIndicator beans:
                                      ^^^
                    NO HEALTH INDICATORS REGISTERED!
```

---

## 8. One-Liner Checks

```bash
# Check if health endpoint is mentioned in startup logs
tail -f application.log | grep -i "health"

# Count exposed endpoints
tail -f application.log | grep "Exposing.*endpoint(s)"

# Check for 404 errors (indicates disabled/not configured)
tail -f application.log | grep "404"
```

---

## 9. Most Reliable Indicators

### Top 3 things to check:

1. **"Exposing X endpoint(s)" line:**
   - `Exposing 1 endpoint(s)` → ✅ Health enabled
   - `Exposing 0 endpoint(s)` → ❌ Health disabled

2. **HealthIndicator beans count:**
   - `Found X HealthIndicator beans` → ✅ Health enabled
   - No such line or `Found 0` → ❌ Health disabled

3. **HTTP test:**
   - `curl /actuator/health` returns JSON → ✅ Enabled
   - Returns 404 → ❌ Disabled

---

## Summary Table

| Log Indicator | health.enabled=true | health.enabled=false |
|--------------|---------------------|----------------------|
| **"Exposing X endpoint(s)"** | `Exposing 1+` | `Exposing 0` or none |
| **"HealthIndicator beans"** | `Found X beans` | No mention or 0 |
| **"Mapped /actuator/health"** | ✅ Present | ❌ Absent |
| **DEBUG condition report** | Positive match | Negative match |
| **curl test** | 200 OK + JSON | 404 Not Found |

---

## Pro Tip

The **most reliable** way is to look for this specific line:

```
Exposing X endpoint(s) beneath base path '/actuator'
```

- If X ≥ 1 → Health endpoint is enabled ✅
- If X = 0 → Health endpoint is disabled ❌

This line appears in **every** Spring Boot Actuator application and clearly shows how many endpoints are exposed.
