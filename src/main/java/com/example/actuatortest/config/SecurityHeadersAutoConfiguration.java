package com.example.actuatortest.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import jakarta.servlet.Filter;

/**
 * Auto-configuration for security headers filter.
 * 
 * This configuration is automatically applied when:
 * - The application is a web application (Spring MVC or WebFlux)
 * - Servlet API is on the classpath
 * - security.headers.enabled=true (default: true)
 * 
 * When used as a dependency, this automatically adds security headers to all responses.
 * 
 * Configuration options in application.properties:
 * <pre>
 * # Enable/disable the entire filter
 * security.headers.enabled=true
 * 
 * # Configure specific headers
 * security.headers.x-frame-options.enabled=true
 * security.headers.x-frame-options.value=DENY
 * security.headers.hsts.max-age=31536000
 * 
 * # Add custom headers
 * security.headers.custom.X-Custom-Header=value
 * </pre>
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnClass({Filter.class})
@ConditionalOnProperty(name = "security.headers.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityHeadersAutoConfiguration {

    /**
     * Creates and configures the security header properties bean.
     * 
     * @return security header properties
     */
    @Bean
    @ConditionalOnMissingBean
    public SecurityHeaderProperties securityHeaderProperties() {
        return new SecurityHeaderProperties();
    }

    /**
     * Creates the security headers filter bean.
     * 
     * @param properties security header configuration properties
     * @return configured security headers filter
     */
    @Bean
    @ConditionalOnMissingBean
    public SecurityHeadersFilter securityHeadersFilter(SecurityHeaderProperties properties) {
        return new SecurityHeadersFilter(properties);
    }

    /**
     * Registers the security headers filter in the filter chain.
     * 
     * @param filter the security headers filter
     * @return filter registration bean
     */
    @Bean
    @ConditionalOnMissingBean(name = "securityHeadersFilterRegistration")
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration(
            SecurityHeadersFilter filter) {
        
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(filter.getOrder());
        registration.setName("securityHeadersFilter");
        
        return registration;
    }
}
