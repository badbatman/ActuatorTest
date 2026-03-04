package com.example.actuatortest.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * Auto-configuration for the custom health endpoint.
 * 
 * This configuration class provides a default HealthIndicator bean
 * that will be automatically loaded when the application starts.
 * 
 * The HealthIndicator contributes to the overall health status
 * managed by Spring Boot's built-in HealthEndpoint.
 * 
 * @since Spring Boot 3.x - Uses @AutoConfiguration instead of spring.factories
 */
@AutoConfiguration
public class CustomHealthEndpointAutoConfiguration {

    /**
     * Creates a custom health indicator bean.
     * 
     * This bean contributes to the overall health status of the application.
     * Multiple HealthIndicator beans can coexist and their statuses are aggregated.
     * 
     * @return HealthIndicator with custom health check logic
     */
    @Bean
    public HealthIndicator customHealthIndicator() {
        return new CustomHealthIndicator();
    }

    /**
     * Inner class implementing the health check logic.
     * Keeps the configuration clean and encapsulated.
     */
    private static class CustomHealthIndicator implements HealthIndicator {
        
        @Override
        public Health health() {
            // Perform your custom health checks here
            boolean serviceHealthy = checkServiceHealth();
            
            if (serviceHealthy) {
                return Health.up()
                        .withDetail("custom", "true")
                        .withDetail("service", "my-service")
                        .withDetail("timestamp", System.currentTimeMillis())
                        .withDetail("checks", "all passed")
                        .build();
            } else {
                return Health.down()
                        .withDetail("custom", "true")
                        .withDetail("service", "my-service")
                        .withDetail("error", "Service health check failed")
                        .build();
            }
        }
        
        /**
         * Example health check logic - replace with your actual checks
         */
        private boolean checkServiceHealth() {
            // Add your custom health check logic here
            // Examples:
            // - Database connectivity check
            // - External service availability check
            // - Disk space check
            // - Memory usage check
            return true; // Simulating healthy state
        }
    }
}
