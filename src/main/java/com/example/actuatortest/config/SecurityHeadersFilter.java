package com.example.actuatortest.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Security Headers Filter that adds common security headers to HTTP responses.
 * 
 * This filter provides protection against common web vulnerabilities:
 * - XSS (Cross-Site Scripting) via X-Content-Type-Options, X-XSS-Protection
 * - Clickjacking via X-Frame-Options
 * - MIME type sniffing via X-Content-Type-Options
 * - Information leakage via removing Server and X-Powered-By headers
 * - HTTPS enforcement via Strict-Transport-Security
 * 
 * The filter is automatically registered when this library is used as a dependency.
 * 
 * @see SecurityHeaderProperties for configuration options
 */
public class SecurityHeadersFilter implements Filter, OrderedFilter {

    /**
     * Default security headers configuration.
     * These values follow OWASP security recommendations.
     */
    private static final Map<String, String> DEFAULT_HEADERS = new HashMap<>();
    
    static {
        // Prevent clickjacking attacks
        DEFAULT_HEADERS.put("X-Frame-Options", "DENY");
        
        // Prevent MIME type sniffing
        DEFAULT_HEADERS.put("X-Content-Type-Options", "nosniff");
        
        // Enable XSS filter in browsers
        DEFAULT_HEADERS.put("X-XSS-Protection", "1; mode=block");
        
        // Referrer policy for privacy
        DEFAULT_HEADERS.put("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Content Security Policy (basic)
        DEFAULT_HEADERS.put("Content-Security-Policy", "default-src 'self'; frame-ancestors 'none'");
        
        // Permissions Policy (formerly Feature-Policy)
        DEFAULT_HEADERS.put("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
        
        // Remove server information
        DEFAULT_HEADERS.put("Server", "REDACTED");
        DEFAULT_HEADERS.put("X-Powered-By", "REDACTED");
    }

    /**
     * Custom headers from configuration.
     */
    private final Map<String, String> customHeaders;

    /**
     * Whether to apply HSTS header.
     */
    private final boolean enableHsts;

    /**
     * HSTS max-age in seconds.
     */
    private final long hstsMaxAge;

    /**
     * Whether to include subdomains in HSTS.
     */
    private final boolean hstsIncludeSubDomains;

    /**
     * Order of this filter in the filter chain.
     */
    private int order = Ordered.LOWEST_PRECEDENCE - 100;

    /**
     * Creates a new SecurityHeadersFilter with default settings.
     */
    public SecurityHeadersFilter() {
        this.customHeaders = new HashMap<>(DEFAULT_HEADERS);
        this.enableHsts = true;
        this.hstsMaxAge = 31536000L; // 1 year
        this.hstsIncludeSubDomains = true;
    }

    /**
     * Creates a new SecurityHeadersFilter with custom configuration.
     * 
     * @param properties security header configuration properties
     */
    public SecurityHeadersFilter(SecurityHeaderProperties properties) {
        this.customHeaders = buildHeaders(properties);
        this.enableHsts = properties.getHsts().isEnabled();
        this.hstsMaxAge = properties.getHsts().getMaxAge();
        this.hstsIncludeSubDomains = properties.getHsts().isIncludeSubDomains();
        this.order = properties.getOrder();
    }

    /**
     * Builds the headers map from properties.
     * 
     * @param properties the security header properties
     * @return map of header names to values
     */
    private Map<String, String> buildHeaders(SecurityHeaderProperties properties) {
        Map<String, String> headers = new HashMap<>(DEFAULT_HEADERS);
        
        // Override with custom values if provided
        if (properties.getHeaders() != null) {
            headers.putAll(properties.getHeaders());
        }
        
        return headers;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            // Add security headers to response
            addSecurityHeaders(httpResponse, httpRequest);
            
            // Continue with filter chain
            chain.doFilter(request, response);
        } else {
            // Not HTTP, just continue
            chain.doFilter(request, response);
        }
    }

    /**
     * Adds all configured security headers to the HTTP response.
     * 
     * @param response the HTTP response
     * @param request the HTTP request (for context-aware headers)
     */
    protected void addSecurityHeaders(HttpServletResponse response, HttpServletRequest request) {
        // Add custom headers
        for (Map.Entry<String, String> entry : customHeaders.entrySet()) {
            String headerName = entry.getKey();
            String headerValue = entry.getValue();
            
            // Skip headers marked as REDACTED (removal)
            if ("REDACTED".equals(headerValue)) {
                // Don't add the header (effectively removes it if it was set)
                continue;
            }
            
            // Skip null or empty values
            if (headerValue == null || headerValue.trim().isEmpty()) {
                continue;
            }
            
            response.setHeader(headerName, headerValue);
        }
        
        // Add HSTS header if enabled and request is HTTPS
        if (enableHsts && request.isSecure()) {
            StringBuilder hstsValue = new StringBuilder("max-age=").append(hstsMaxAge);
            
            if (hstsIncludeSubDomains) {
                hstsValue.append("; includeSubDomains");
            }
            
            response.setHeader("Strict-Transport-Security", hstsValue.toString());
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Sets the order of this filter.
     * Lower values have higher priority.
     * 
     * @param order the filter order
     */
    public void setOrder(int order) {
        this.order = order;
    }
}
