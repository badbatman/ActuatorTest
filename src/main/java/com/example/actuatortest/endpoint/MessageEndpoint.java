package com.example.actuatortest.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Test endpoint to verify security headers are applied to regular endpoints.
 */
@RestController
public class MessageEndpoint {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Returns current timestamp in JSON format.
     * 
     * @return map containing current timestamp
     */
    @GetMapping("/message")
    public Map<String, Object> getMessage() {
        Map<String, Object> response = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        response.put("timestamp", now.format(DATE_TIME_FORMATTER));
        response.put("epoch", System.currentTimeMillis());
        response.put("message", "Security headers test endpoint");
        response.put("status", "success");
        
        return response;
    }

    /**
     * Alternative endpoint that returns a simple string.
     * 
     * @return greeting message
     */
    @GetMapping("/api/hello")
    public Map<String, String> getHello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from Spring Boot!");
        response.put("timestamp", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        return response;
    }
}
