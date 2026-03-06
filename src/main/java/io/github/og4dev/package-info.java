/**
 * Root package for the OG4Dev Spring API Response Library.
 * <p>
 * This library provides zero-configuration, production-ready REST API response handling
 * for Spring Boot applications. Simply adding the dependency enables all features
 * automatically through Spring Boot's autoconfiguration mechanism.
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li><b>Standardized Responses</b> — Uniform {@link io.github.og4dev.dto.ApiResponse}
 *       structure with HTTP status, message, content, and timestamp.</li>
 *   <li><b>Automatic Wrapping</b> — Opt-in {@link io.github.og4dev.annotation.AutoResponse}
 *       annotation eliminates manual {@code ResponseEntity<ApiResponse<T>>} boilerplate.</li>
 *   <li><b>Global Exception Handling</b> — 10 built-in RFC 9457 ProblemDetail handlers
 *       covering validation, 404, 405, 415, malformed JSON, and more.</li>
 *   <li><b>Extensible Translators</b> — {@link io.github.og4dev.exception.ApiExceptionTranslator}
 *       allows mapping of third-party exceptions without additional {@code @ExceptionHandler}
 *       methods.</li>
 *   <li><b>Distributed Tracing</b> — {@link io.github.og4dev.filter.TraceIdFilter} injects
 *       a UUID trace ID into every request for log correlation.</li>
 *   <li><b>XSS Protection</b> — Opt-in {@link io.github.og4dev.annotation.XssCheck} rejects
 *       HTML payloads at the deserialization layer with fail-fast 400 responses.</li>
 *   <li><b>String Trimming</b> — Opt-in {@link io.github.og4dev.annotation.AutoTrim} strips
 *       whitespace at the deserialization layer before values reach business logic.</li>
 * </ul>
 *
 * <h2>Quick Start</h2>
 * <p>
 * Add the Maven dependency — no other configuration is required:
 * </p>
 * <pre>{@code
 * <dependency>
 *     <groupId>io.github.og4dev</groupId>
 *     <artifactId>og4dev-spring-response</artifactId>
 *     <version>1.4.0</version>
 * </dependency>
 * }</pre>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.0.0
 * @see io.github.og4dev.dto.ApiResponse
 * @see io.github.og4dev.advice.GlobalResponseWrapper
 * @see io.github.og4dev.exception.GlobalExceptionHandler
 * @see io.github.og4dev.config.ApiResponseAutoConfiguration
 */
package io.github.og4dev;

