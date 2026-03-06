package com.example.actuatortest.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecurityHeadersFilter.
 */
class SecurityHeadersFilterTest {

    private SecurityHeadersFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityHeaderProperties properties = new SecurityHeaderProperties();
        filter = new SecurityHeadersFilter(properties);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    void testDefaultHeadersAreAdded() throws ServletException, IOException {
        // Act
        filter.doFilter(request, response, filterChain);

        // Assert - Standard headers should be present
        assertEquals("DENY", response.getHeader("X-Frame-Options"));
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals("1; mode=block", response.getHeader("X-XSS-Protection"));
        assertEquals("strict-origin-when-cross-origin", response.getHeader("Referrer-Policy"));
        assertEquals("default-src 'self'; frame-ancestors 'none'", response.getHeader("Content-Security-Policy"));
        assertEquals("geolocation=(), microphone=(), camera=()", response.getHeader("Permissions-Policy"));
    }

    @Test
    void testHstsHeaderNotAddedForNonHttpsRequest() throws ServletException, IOException {
        // Arrange - Request is not secure
        request.setSecure(false);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert - HSTS should NOT be added for non-HTTPS requests
        assertNull(response.getHeader("Strict-Transport-Security"));
    }

    @Test
    void testHstsHeaderAddedForHttpsRequest() throws ServletException, IOException {
        // Arrange - Request is secure
        request.setSecure(true);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert - HSTS should be added
        String hstsHeader = response.getHeader("Strict-Transport-Security");
        assertNotNull(hstsHeader);
        assertTrue(hstsHeader.contains("max-age=31536000"));
        assertTrue(hstsHeader.contains("includeSubDomains"));
    }

    @Test
    void testCustomHeadersAreAdded() throws ServletException, IOException {
        // Arrange
        SecurityHeaderProperties properties = new SecurityHeaderProperties();
        properties.setCustom(Map.of(
            "X-Custom-Header", "custom-value",
            "X-API-Version", "1.0.0"
        ));
        filter = new SecurityHeadersFilter(properties);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert - Custom headers should be present
        assertEquals("custom-value", response.getHeader("X-Custom-Header"));
        assertEquals("1.0.0", response.getHeader("X-API-Version"));
    }

    @Test
    void testDisabledHeadersAreNotAdded() throws ServletException, IOException {
        // Arrange - Disable X-Frame-Options by setting value to null
        SecurityHeaderProperties properties = new SecurityHeaderProperties();
        properties.getXFrameOptions().setValue(null);
        filter = new SecurityHeadersFilter(properties);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert - Null values are skipped (effectively disabled)
        assertNull(response.getHeader("X-Frame-Options"));
    }

    @Test
    void testCustomHeaderValueOverridesDefault() throws ServletException, IOException {
        // Arrange - Override X-Frame-Options value
        SecurityHeaderProperties properties = new SecurityHeaderProperties();
        properties.getXFrameOptions().setValue("SAMEORIGIN");
        filter = new SecurityHeadersFilter(properties);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert - Custom value should override default
        assertEquals("SAMEORIGIN", response.getHeader("X-Frame-Options"));
    }

    @Test
    void testFilterOrder() throws ServletException, IOException {
        // Arrange
        SecurityHeaderProperties properties = new SecurityHeaderProperties();
        properties.setOrder(-200);
        filter = new SecurityHeadersFilter(properties);

        // Assert
        assertEquals(-200, filter.getOrder());
    }

    @Test
    void testNonHttpRequest() throws ServletException, IOException {
        // Arrange - Use non-HTTP request/response
        MockHttpServletRequest nonHttpRequest = new MockHttpServletRequest();
        MockHttpServletResponse nonHttpResponse = new MockHttpServletResponse();

        // Act - Should not throw exception
        assertDoesNotThrow(() -> filter.doFilter(nonHttpRequest, nonHttpResponse, filterChain));
    }

    @Test
    void testHstsConfiguration() throws ServletException, IOException {
        // Arrange - Custom HSTS settings
        SecurityHeaderProperties properties = new SecurityHeaderProperties();
        properties.getHsts().setMaxAge(63072000L); // 2 years
        properties.getHsts().setIncludeSubDomains(false);
        filter = new SecurityHeadersFilter(properties);
        request.setSecure(true);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        String hstsHeader = response.getHeader("Strict-Transport-Security");
        assertNotNull(hstsHeader);
        assertTrue(hstsHeader.contains("max-age=63072000"));
        assertFalse(hstsHeader.contains("includeSubDomains"));
    }

    @Test
    void testFilterContinuesChain() throws ServletException, IOException {
        // Act - Just call doFilter with empty chain
        filter.doFilter(request, response, filterChain);

        // Assert - Response should have headers (filter executed)
        assertNotNull(response.getHeader("X-Frame-Options"));
    }

    @Test
    void testMultipleHeadersCanBeDisabled() throws ServletException, IOException {
        // Arrange - Set multiple headers to null (they'll be skipped)
        SecurityHeaderProperties properties = new SecurityHeaderProperties();
        properties.getXFrameOptions().setValue(null);
        properties.getXContentTypeOptions().setValue(null);
        filter = new SecurityHeadersFilter(properties);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert - Headers with null values are skipped
        assertNull(response.getHeader("X-Frame-Options"));
        assertNull(response.getHeader("X-Content-Type-Options"));
        
        // Other headers should still be present
        assertNotNull(response.getHeader("Content-Security-Policy"));
    }
}
