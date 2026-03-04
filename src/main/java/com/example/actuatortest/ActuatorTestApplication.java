package com.example.actuatortest;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.actuate.health.HealthIndicator;

import com.example.actuatortest.config.ActuatorAutoConfigurationMarker;

/**
 * Spring Boot Actuator Test Application.
 * 
 * This application demonstrates auto-configured health endpoint using:
 * - ActuatorEndpointAutoConfiguration (EnvironmentPostProcessor) - Sets defaults early
 * - CustomHealthEndpointAutoConfiguration (@AutoConfiguration) - Provides health indicator bean
 * 
 * Configuration is loaded automatically via @AutoConfiguration annotation (Spring Boot 3.x).
 * No spring.factories or application.properties configuration needed for basic setup.
 * 
 * To disable custom actuator configuration, exclude ActuatorAutoConfigurationMarker:
 * @SpringBootApplication(exclude = {ActuatorAutoConfigurationMarker.class})
 */
@SpringBootApplication
public class ActuatorTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActuatorTestApplication.class, args);
    }
    
    /**
     * Debug helper to verify endpoint registration.
     * This helps troubleshoot handler mapping issues by showing registered beans.
     */
    @Bean
    public CommandLineRunner checkEndpointRegistration(ApplicationContext ctx) {
        return args -> {
            System.out.println("=== Actuator Endpoint Debug Info ===");
            
            // List all HealthIndicator beans
            String[] healthIndicators = ctx.getBeanNamesForType(
                HealthIndicator.class);
            System.out.println("\nFound " + healthIndicators.length + " HealthIndicator beans:");
            for (String beanName : healthIndicators) {
                System.out.println("  - " + beanName);
            }
            
            System.out.println("===================================");
            System.out.println("Access health endpoint at: http://localhost:8080/actuator/health");
        };
    }
}
