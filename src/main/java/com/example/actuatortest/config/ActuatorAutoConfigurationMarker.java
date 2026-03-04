package com.example.actuatortest.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * Auto-configuration marker for actuator endpoint setup.
 * 
 * This class serves as an exclusion point for the entire actuator
 * auto-configuration setup, including both:
 * - ActuatorEndpointAutoConfiguration (EnvironmentPostProcessor)
 * - CustomHealthEndpointAutoConfiguration (HealthIndicator bean)
 * 
 * When this class is excluded via @SpringBootApplication(exclude = ...),
 * users can prevent all custom actuator configuration from loading.
 * 
 * Note: The actual EnvironmentPostProcessor cannot be excluded this way
 * because it runs before the application context is created. To disable
 * the properties it sets, override them in application.properties.
 * 
 * @see ActuatorEndpointAutoConfiguration
 * @see CustomHealthEndpointAutoConfiguration
 */
@AutoConfiguration
public class ActuatorAutoConfigurationMarker {

    /**
     * Marker bean to indicate actuator auto-configuration is active.
     * 
     * @return dummy object, only exists for conditional checks
     */
    @Bean
    @ConditionalOnProperty(name = "custom.actuator.enabled", havingValue = "true", matchIfMissing = true)
    public Object actuatorConfigurationMarker() {
        return new Object();
    }
}
