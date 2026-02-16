/**
 * Servlet filter classes for request processing and distributed tracing.
 * <p>
 * This package contains servlet filters for cross-cutting concerns in Spring Boot applications,
 * particularly focused on distributed tracing and request correlation.
 * </p>
 * <p>
 * The {@link io.github.og4dev.filter.TraceIdFilter} automatically generates unique trace IDs
 * for each incoming HTTP request and stores them in both request attributes and SLF4J's MDC
 * (Mapped Diagnostic Context). This enables:
 * </p>
 * <ul>
 *   <li>Automatic trace ID inclusion in all log statements</li>
 *   <li>Request correlation across microservices</li>
 *   <li>Simplified debugging and troubleshooting</li>
 *   <li>Thread-safe MDC management with automatic cleanup</li>
 * </ul>
 * <p>
 * <b>Note:</b> The TraceIdFilter is not automatically registered. To use it, manually register
 * it as a bean in your Spring configuration.
 * </p>
 *
 * @author Pasindu OG
 * @version 1.0.0
 * @since 1.0.0
 */
package io.github.og4dev.filter;

