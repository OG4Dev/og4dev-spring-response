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
 * {@link OncePerRequestFilter} that generates a unique UUID trace ID for every incoming
 * HTTP request and propagates it through SLF4J MDC and request attributes.
 * <p>
 * The trace ID is stored under the key {@code "traceId"} in two places:
 * </p>
 * <ul>
 *   <li><b>SLF4J MDC</b> — automatically appended to every log statement produced during
 *       the request when the Logback/Log4j pattern includes {@code %X{traceId}}.</li>
 *   <li><b>Servlet request attributes</b> — accessible programmatically within filters,
 *       interceptors, and controllers via {@code request.getAttribute("traceId")}.</li>
 * </ul>
 * <p>
 * The MDC entry is always removed in a {@code finally} block after the filter chain
 * completes, preventing trace ID leakage across requests in shared thread-pool environments.
 * </p>
 * <p>
 * The same trace ID is also embedded in every RFC 9457 ProblemDetail error response
 * produced by {@link io.github.og4dev.exception.GlobalExceptionHandler}, allowing
 * client-reported trace IDs to be matched directly against server logs.
 * </p>
 *
 * <h2>Registration</h2>
 * <p>
 * This filter is <b>not</b> registered automatically. Register it with highest precedence
 * so the trace ID is available to all downstream filters and handlers:
 * </p>
 * <pre>{@code
 * @Bean
 * public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
 *     FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
 *     registration.setFilter(new TraceIdFilter());
 *     registration.addUrlPatterns("/*");
 *     registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
 *     return registration;
 * }
 * }</pre>
 *
 * <h2>Logback Pattern</h2>
 * <pre>{@code
 * <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId}] %-5level %logger{36} - %msg%n</pattern>
 * }</pre>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.0.0
 * @see org.springframework.web.filter.OncePerRequestFilter
 * @see org.slf4j.MDC
 * @see io.github.og4dev.exception.GlobalExceptionHandler
 */
@SuppressWarnings("unused")
public class TraceIdFilter extends OncePerRequestFilter {

    /**
     * Default no-arg constructor required for Spring's
     * {@link org.springframework.web.filter.GenericFilterBean} registration mechanism.
     */
    public TraceIdFilter() {
        // Required by Spring's filter registration mechanism
    }

    /**
     * Generates a UUID trace ID, stores it in MDC and request attributes, then delegates
     * to the rest of the filter chain.
     * <p>
     * MDC is unconditionally cleared in the {@code finally} block to prevent the trace ID
     * from leaking into subsequent requests handled by the same thread.
     * </p>
     *
     * @param request     the current HTTP servlet request
     * @param response    the current HTTP servlet response
     * @param filterChain the remaining filter chain to delegate to
     * @throws ServletException if a servlet-layer error occurs during processing
     * @throws IOException      if an I/O error occurs during processing
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
