package io.github.og4dev.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that generates and manages trace IDs for distributed tracing.
 * <p>
 * This filter generates a unique UUID for each incoming HTTP request and stores it in both
 * the request attributes and SLF4J's MDC (Mapped Diagnostic Context). This enables automatic
 * trace ID inclusion in all log statements throughout the request lifecycle, facilitating
 * request correlation and debugging across distributed systems.
 * </p>
 * <p>
 * <b>Key Features:</b>
 * </p>
 * <ul>
 *   <li>Automatic UUID generation for each request</li>
 *   <li>MDC integration for automatic log inclusion</li>
 *   <li>Request attribute storage for programmatic access</li>
 *   <li>Thread-safe with automatic MDC cleanup</li>
 *   <li>Compatible with microservices architectures</li>
 * </ul>
 * <p>
 * <b>Usage:</b> Register this filter as a Spring bean with highest precedence:
 * </p>
 * <pre>{@code
 * @Configuration
 * public class FilterConfig {
 *     @Bean
 *     public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
 *         FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
 *         registration.setFilter(new TraceIdFilter());
 *         registration.addUrlPatterns("/*");
 *         registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
 *         return registration;
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Logback Configuration:</b> Configure your logger to include the trace ID:
 * </p>
 * <pre>{@code
 * <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId}] %-5level %logger{36} - %msg%n</pattern>
 * }</pre>
 * <p>
 * <b>MDC Cleanup:</b> The filter automatically clears MDC in the finally block to prevent
 * memory leaks and cross-request contamination.
 * </p>
 *
 * @author Pasindu OG
 * @version 1.1.0
 * @since 1.0.0
 * @see org.springframework.web.filter.OncePerRequestFilter
 * @see org.slf4j.MDC
 */
@SuppressWarnings("unused")
public class TraceIdFilter extends OncePerRequestFilter {

    /**
     * Default constructor for Spring filter registration.
     */
    public TraceIdFilter() {
        // Default constructor for Spring filter registration
    }

    /**
     * Generates a trace ID and stores it in request attributes and MDC.
     * <p>
     * The trace ID is cleared from MDC after the request completes.
     * </p>
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    @NullMarked
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        UUID traceId = UUID.randomUUID();
        try {
            request.setAttribute("traceId", traceId);
            MDC.put("traceId", traceId.toString());
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
