/**
 * Servlet filter for distributed tracing and request correlation.
 * <p>
 * This package contains {@link io.github.og4dev.filter.TraceIdFilter}, a
 * {@link org.springframework.web.filter.OncePerRequestFilter} that generates a unique
 * UUID trace ID for every incoming HTTP request and makes it available in two places:
 * </p>
 * <ul>
 *   <li><b>SLF4J MDC</b> ({@code traceId} key) — automatically included in every log
 *       statement written during the request lifecycle when the logging pattern contains
 *       {@code %X{traceId}}.</li>
 *   <li><b>Request attributes</b> ({@code traceId} key) — accessible programmatically
 *       via {@code request.getAttribute("traceId")}.</li>
 * </ul>
 * <p>
 * The MDC entry is cleared in a {@code finally} block after the filter chain completes,
 * preventing trace ID leakage between requests in thread-pool environments.
 * </p>
 *
 * <h2>Registration</h2>
 * <p>
 * {@link io.github.og4dev.filter.TraceIdFilter} is <b>not</b> registered automatically.
 * Register it as a Spring bean with highest precedence to ensure the trace ID is available
 * before any other filter or servlet processes the request:
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
 * <h2>Logback Configuration</h2>
 * <pre>{@code
 * <pattern>%d{yyyy-MM-dd HH:mm:ss} [%X{traceId}] %-5level %logger - %msg%n</pattern>
 * }</pre>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.0.0
 * @see io.github.og4dev.filter.TraceIdFilter
 * @see org.slf4j.MDC
 */
package io.github.og4dev.filter;

