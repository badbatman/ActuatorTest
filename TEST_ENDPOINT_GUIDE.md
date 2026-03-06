# Testing Security Headers on Endpoints

## New Test Endpoints Created

I've created two REST endpoints for testing security headers:

### 1. `/message` Endpoint
Returns JSON with current timestamp and metadata.

**Request:**
```http
GET /message HTTP/1.1
Host: localhost:8080
```

**Response:**
```json
{
  "timestamp": "2026-03-07 00:15:30",
  "epoch": 1741305330000,
  "message": "Security headers test endpoint",
  "status": "success"
}
```

### 2. `/api/hello` Endpoint
Returns a simple greeting message.

**Request:**
```http
GET /api/hello HTTP/1.1
Host: localhost:8080
```

**Response:**
```json
{
  "message": "Hello from Spring Boot!",
  "timestamp": "2026-03-07 00:15:30"
}
```

---

## How to Test

### Step 1: Start the Application

```bash
cd /Users/bob/test_project/ActuatorTest
mvn spring-boot:run
```

Wait for:
```log
✅ Tomcat started on port(s): 8080 (http)
✅ Started ActuatorTestApplication in X.XXX seconds
```

### Step 2: Test with curl (Include Headers)

#### Test /message endpoint
```bash
curl -i http://localhost:8080/message
```

#### Test /api/hello endpoint
```bash
curl -i http://localhost:8080/api/hello
```

#### Expected Output
```http
HTTP/1.1 200 OK
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Referrer-Policy: strict-origin-when-cross-origin
Content-Security-Policy: default-src 'self'; frame-ancestors 'none'
Permissions-Policy: geolocation=(), microphone=(), camera=()
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 07 Mar 2026 16:15:30 GMT

{
  "timestamp": "2026-03-07 00:15:30",
  "epoch": 1741305330000,
  "message": "Security headers test endpoint",
  "status": "success"
}
```

### Step 3: Verify All Security Headers

Check that these headers are present:

| Header | Expected Value | Purpose |
|--------|---------------|---------|
| **X-Frame-Options** | DENY | Prevents clickjacking |
| **X-Content-Type-Options** | nosniff | Prevents MIME sniffing |
| **X-XSS-Protection** | 1; mode=block | XSS filter |
| **Referrer-Policy** | strict-origin-when-cross-origin | Referrer control |
| **Content-Security-Policy** | default-src 'self'; frame-ancestors 'none' | CSP |
| **Permissions-Policy** | geolocation=(), microphone=(), camera=() | Feature policy |
| **Strict-Transport-Security** | max-age=31536000; includeSubDomains | HSTS |

---

## Testing with Different Tools

### Using wget
```bash
wget -S -O - http://localhost:8080/message 2>&1 | grep -E "(HTTP|X-|Content-|Referrer-|Permissions-|Strict-)"
```

### Using httpie
```bash
http --headers http://localhost:8080/message
```

### Using Postman
1. Create GET request to `http://localhost:8080/message`
2. Click "Send"
3. Check "Headers" tab in response section

### Using Browser DevTools
1. Open browser DevTools (F12)
2. Go to Network tab
3. Visit `http://localhost:8080/message`
4. Click on the request
5. Check "Response Headers" section

---

## Comparing Different Endpoints

### Test Actuator Endpoint
```bash
curl -i http://localhost:8080/actuator/health
```

### Test Message Endpoint
```bash
curl -i http://localhost:8080/message
```

### Test API Endpoint
```bash
curl -i http://localhost:8080/api/hello
```

**All should have the same security headers!** ✅

---

## Expected Results

### Success Criteria

✅ **All endpoints return 200 OK**  
✅ **All responses include security headers**  
✅ **JSON body is properly formatted**  
✅ **Timestamp is current time**  

### Example Test Session

```bash
# Test 1: /message endpoint
$ curl -i http://localhost:8080/message

HTTP/1.1 200 OK
X-Frame-Options: DENY                    ← ✅ Present
X-Content-Type-Options: nosniff          ← ✅ Present
X-XSS-Protection: 1; mode=block          ← ✅ Present
Referrer-Policy: strict-origin-...       ← ✅ Present
Content-Security-Policy: default-src...  ← ✅ Present
Permissions-Policy: geolocation=()...    ← ✅ Present
Strict-Transport-Security: max-age=...   ← ✅ Present
Content-Type: application/json

{
  "timestamp": "2026-03-07 00:15:30",
  "epoch": 1741305330000,
  "message": "Security headers test endpoint",
  "status": "success"
}

# Test 2: /api/hello endpoint
$ curl -i http://localhost:8080/api/hello

HTTP/1.1 200 OK
X-Frame-Options: DENY                    ← ✅ Present
X-Content-Type-Options: nosniff          ← ✅ Present
... (same headers)

{
  "message": "Hello from Spring Boot!",
  "timestamp": "2026-03-07 00:15:30"
}

# Test 3: /actuator/health endpoint
$ curl -i http://localhost:8080/actuator/health

HTTP/1.1 200 OK
X-Frame-Options: DENY                    ← ✅ Present
... (same headers)

{
  "status": "UP",
  "components": { ... }
}
```

---

## Troubleshooting

### Issue: Headers Missing

**Check:**
1. Is application running? `curl http://localhost:8080/message`
2. Is filter enabled? Check logs for "Initialized security headers filter"
3. Is it a web app? Must be web application for filter to load

**Debug:**
```properties
logging.level.com.example.actuatortest=DEBUG
```

### Issue: Only Some Headers Missing

**Possible causes:**
1. Configuration disabled them
2. Another filter overrode them
3. Proxy removed them

**Check application.properties:**
```properties
security.headers.x-frame-options.enabled=true
security.headers.x-content-type-options.enabled=true
# etc...
```

### Issue: HSTS Header Missing

**Remember:** HSTS only applies to HTTPS requests!

For HTTP requests (like `http://localhost:8080`), HSTS won't be added by design.

To test HSTS:
1. Setup HTTPS in your application
2. Access via `https://localhost:8443/message`
3. HSTS header will appear

---

## Automated Testing

### Create a Test Script

Create `test-headers.sh`:

```bash
#!/bin/bash

echo "Testing Security Headers on Endpoints"
echo "======================================"
echo ""

BASE_URL="http://localhost:8080"

test_endpoint() {
    local endpoint=$1
    echo "Testing $endpoint..."
    echo ""
    
    response=$(curl -si "$BASE_URL$endpoint")
    
    # Check for required headers
    if echo "$response" | grep -q "X-Frame-Options"; then
        echo "✅ X-Frame-Options: $(echo "$response" | grep "X-Frame-Options" | cut -d: -f2-)"
    else
        echo "❌ X-Frame-Options: MISSING"
    fi
    
    if echo "$response" | grep -q "X-Content-Type-Options"; then
        echo "✅ X-Content-Type-Options: $(echo "$response" | grep "X-Content-Type-Options" | cut -d: -f2-)"
    else
        echo "❌ X-Content-Type-Options: MISSING"
    fi
    
    if echo "$response" | grep -q "Content-Security-Policy"; then
        echo "✅ Content-Security-Policy: $(echo "$response" | grep "Content-Security-Policy" | cut -d: -f2-)"
    else
        echo "❌ Content-Security-Policy: MISSING"
    fi
    
    echo ""
}

# Test all endpoints
test_endpoint "/message"
test_endpoint "/api/hello"
test_endpoint "/actuator/health"

echo "Testing complete!"
```

Make executable and run:
```bash
chmod +x test-headers.sh
./test-headers.sh
```

---

## Performance Testing

### Measure Filter Overhead

```bash
# Run 100 requests and measure average response time
ab -n 100 -c 10 http://localhost:8080/message

# Expected results:
# Requests per second: Should be very high (>1000 req/s)
# Time per request: Should be <1ms overhead from filter
```

### Compare With and Without Filter

1. **With filter:** Default configuration
2. **Without filter:** Set `security.headers.enabled=false`

```bash
# Test with filter
time curl -s http://localhost:8080/message > /dev/null

# Disable filter and test again
# Add to application.properties: security.headers.enabled=false
time curl -s http://localhost:8080/message > /dev/null
```

**Expected difference:** ~0.1ms per request (negligible!)

---

## Summary

✅ **Created 2 test endpoints:** `/message` and `/api/hello`  
✅ **Both return JSON with timestamps**  
✅ **Security headers automatically applied**  
✅ **Test with `curl -i` to see headers**  
✅ **All endpoints should have identical security headers**  

**Quick test command:**
```bash
curl -i http://localhost:8080/message | grep -E "(HTTP|X-|Content-|Referrer-|Permissions-|Strict-|timestamp)"
```

This verifies both the endpoint works AND security headers are applied! 🎉
