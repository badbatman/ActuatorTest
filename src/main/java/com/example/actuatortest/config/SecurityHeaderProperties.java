package com.example.actuatortest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for security headers filter.
 * 
 * These properties allow customization of the security headers
 * added by the SecurityHeadersFilter.
 * 
 * Example configuration in application.properties:
 * <pre>
 * # Enable/disable specific headers
 * security.headers.x-frame-options.enabled=true
 * security.headers.x-frame-options.value=DENY
 * security.headers.hsts.enabled=true
 * security.headers.hsts.max-age=31536000
 * security.headers.hsts.include-sub-domains=true
 * 
 * # Add custom headers
 * security.headers.custom.X-Custom-Header=custom-value
 * 
 * # Disable default headers
 * security.headers.x-powered-by.enabled=false
 * </pre>
 */
@ConfigurationProperties(prefix = "security.headers")
public class SecurityHeaderProperties {
    
    // Note: This class is registered via @EnableConfigurationProperties in SecurityHeadersAutoConfiguration
    // Do not add @Component annotation as it will cause duplicate bean registration

    /**
     * Whether the security headers filter is enabled.
     */
    private boolean enabled = true;

    /**
     * Filter order (lower values have higher priority).
     */
    private int order = Ordered.LOWEST_PRECEDENCE - 100;

    /**
     * X-Frame-Options header configuration.
     */
    private HeaderConfig xFrameOptions = new HeaderConfig("DENY", true);

    /**
     * X-Content-Type-Options header configuration.
     */
    private HeaderConfig xContentTypeOptions = new HeaderConfig("nosniff", true);

    /**
     * X-XSS-Protection header configuration.
     */
    private HeaderConfig xXssProtection = new HeaderConfig("1; mode=block", true);

    /**
     * Referrer-Policy header configuration.
     */
    private HeaderConfig referrerPolicy = new HeaderConfig("strict-origin-when-cross-origin", true);

    /**
     * Content-Security-Policy header configuration.
     */
    private HeaderConfig contentSecurityPolicy = new HeaderConfig("default-src 'self'; frame-ancestors 'none'", true);

    /**
     * Permissions-Policy header configuration.
     */
    private HeaderConfig permissionsPolicy = new HeaderConfig("geolocation=(), microphone=(), camera=()", true);

    /**
     * Strict-Transport-Security (HSTS) configuration.
     */
    private HstsConfig hsts = new HstsConfig();

    /**
     * Custom headers to add.
     */
    private Map<String, String> custom = new HashMap<>();

    /**
     * Server header configuration (for removal).
     */
    private HeaderConfig server = new HeaderConfig("REDACTED", false);

    /**
     * X-Powered-By header configuration (for removal).
     */
    private HeaderConfig xPoweredBy = new HeaderConfig("REDACTED", false);

    /**
     * Gets all configured headers as a map.
     * 
     * @return map of header names to values
     */
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();

        // Add enabled standard headers
        if (xFrameOptions.isEnabled()) {
            headers.put("X-Frame-Options", xFrameOptions.getValue());
        }
        if (xContentTypeOptions.isEnabled()) {
            headers.put("X-Content-Type-Options", xContentTypeOptions.getValue());
        }
        if (xXssProtection.isEnabled()) {
            headers.put("X-XSS-Protection", xXssProtection.getValue());
        }
        if (referrerPolicy.isEnabled()) {
            headers.put("Referrer-Policy", referrerPolicy.getValue());
        }
        if (contentSecurityPolicy.isEnabled()) {
            headers.put("Content-Security-Policy", contentSecurityPolicy.getValue());
        }
        if (permissionsPolicy.isEnabled()) {
            headers.put("Permissions-Policy", permissionsPolicy.getValue());
        }
        if (server.isEnabled()) {
            headers.put("Server", server.getValue());
        }
        if (xPoweredBy.isEnabled()) {
            headers.put("X-Powered-By", xPoweredBy.getValue());
        }

        // Add custom headers
        headers.putAll(custom);

        return headers;
    }

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public HeaderConfig getXFrameOptions() {
        return xFrameOptions;
    }

    public void setXFrameOptions(HeaderConfig xFrameOptions) {
        this.xFrameOptions = xFrameOptions;
    }

    public HeaderConfig getXContentTypeOptions() {
        return xContentTypeOptions;
    }

    public void setXContentTypeOptions(HeaderConfig xContentTypeOptions) {
        this.xContentTypeOptions = xContentTypeOptions;
    }

    public HeaderConfig getxXssProtection() {
        return xXssProtection;
    }

    public void setxXssProtection(HeaderConfig xXssProtection) {
        this.xXssProtection = xXssProtection;
    }

    public HeaderConfig getReferrerPolicy() {
        return referrerPolicy;
    }

    public void setReferrerPolicy(HeaderConfig referrerPolicy) {
        this.referrerPolicy = referrerPolicy;
    }

    public HeaderConfig getContentSecurityPolicy() {
        return contentSecurityPolicy;
    }

    public void setContentSecurityPolicy(HeaderConfig contentSecurityPolicy) {
        this.contentSecurityPolicy = contentSecurityPolicy;
    }

    public HeaderConfig getPermissionsPolicy() {
        return permissionsPolicy;
    }

    public void setPermissionsPolicy(HeaderConfig permissionsPolicy) {
        this.permissionsPolicy = permissionsPolicy;
    }

    public HstsConfig getHsts() {
        return hsts;
    }

    public void setHsts(HstsConfig hsts) {
        this.hsts = hsts;
    }

    public Map<String, String> getCustom() {
        return custom;
    }

    public void setCustom(Map<String, String> custom) {
        this.custom = custom;
    }

    public HeaderConfig getServer() {
        return server;
    }

    public void setServer(HeaderConfig server) {
        this.server = server;
    }

    public HeaderConfig getxPoweredBy() {
        return xPoweredBy;
    }

    public void setxPoweredBy(HeaderConfig xPoweredBy) {
        this.xPoweredBy = xPoweredBy;
    }

    // Helper classes

    /**
     * Configuration for a single header.
     */
    public static class HeaderConfig {
        private String value;
        private boolean enabled;

        public HeaderConfig() {
        }

        public HeaderConfig(String value, boolean enabled) {
            this.value = value;
            this.enabled = enabled;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * HSTS-specific configuration.
     */
    public static class HstsConfig {
        private boolean enabled = true;
        private long maxAge = 31536000L; // 1 year in seconds
        private boolean includeSubDomains = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }

        public boolean isIncludeSubDomains() {
            return includeSubDomains;
        }

        public void setIncludeSubDomains(boolean includeSubDomains) {
            this.includeSubDomains = includeSubDomains;
        }
    }
}
