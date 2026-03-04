package com.example.actuatortest.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.Ordered;

import java.util.HashMap;
import java.util.Map;

/**
 * Environment post-processor for Spring Boot Actuator endpoints.
 * 
 * This class programmatically configures actuator endpoint settings
 * by post-processing the environment BEFORE the application context is refreshed.
 * 
 * IMPORTANT: This is NOT an @AutoConfiguration class because:
 * - EnvironmentPostProcessor runs BEFORE context creation
 * - AutoConfiguration runs DURING context creation
 * - We need to set properties early, before beans are created
 * 
 * Default configuration:
 * - All endpoints disabled by default (security best practice)
 * - Health endpoint explicitly enabled
 * - Only health endpoint exposed over HTTP
 * 
 * This replaces the need for application.properties configuration.
 */
public class ActuatorEndpointAutoConfiguration implements EnvironmentPostProcessor, Ordered {

    /**
     * Post-processes the environment to set up actuator defaults.
     * 
     * @param environment the configurable environment
     * @param application the spring application
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> properties = new HashMap<>();
        
        // Disable all endpoints by default
        properties.put("management.endpoints.enabled-by-default", "false");
        
        // Enable health endpoint
        properties.put("management.endpoint.health.enabled", "true");
        
        // Expose only health endpoint over HTTP
        properties.put("management.endpoints.web.exposure.include", "health");
        
        // Add property source with lower priority than application.properties
        // This allows users to override these defaults in application.properties
        environment.getPropertySources().addLast(
            new MapPropertySource("actuatorDefaults", properties)
        );
    }

    /**
     * Set high priority to run early in the environment post-processing chain.
     * 
     * @return Ordered.HIGHEST_PRECEDENCE
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
