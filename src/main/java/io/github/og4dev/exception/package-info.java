/**
 * Exception handling classes for comprehensive API error management.
 * <p>
 * This package provides production-ready centralized exception handling using Spring's
 * {@link org.springframework.web.bind.annotation.RestControllerAdvice} mechanism, automatically
 * converting exceptions into RFC 9457 ProblemDetail responses with trace IDs for debugging.
 * </p>
 * <p>
 * Key components include:
 * </p>
 * <ul>
 *   <li>{@link io.github.og4dev.exception.GlobalExceptionHandler} - Handles 10 common exception types
 *       including validation errors, malformed JSON, 404 errors, method not allowed, and more</li>
 *   <li>{@link io.github.og4dev.exception.ApiException} - Abstract base class for creating
 *       custom business logic exceptions with automatic handling</li>
 * </ul>
 * <p>
 * All error responses include:
 * </p>
 * <ul>
 *   <li>Standard RFC 9457 ProblemDetail format</li>
 *   <li>Unique trace ID for request correlation</li>
 *   <li>RFC 3339 UTC timestamp</li>
 *   <li>Detailed, actionable error messages</li>
 *   <li>Automatic SLF4J logging with trace IDs</li>
 * </ul>
 *
 * @author Pasindu OG
 * @version 1.0.0
 * @since 1.0.0
 */
package io.github.og4dev.exception;

