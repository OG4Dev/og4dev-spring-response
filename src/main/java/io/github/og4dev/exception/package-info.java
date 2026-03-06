/**
 * Exception handling infrastructure for comprehensive, RFC 9457-compliant API error management.
 * <p>
 * This package contains all classes responsible for converting Java exceptions into structured
 * HTTP error responses. Every error response produced by this package conforms to
 * RFC 9457 ProblemDetail format and includes a unique trace ID for request correlation.
 * </p>
 *
 * <h2>Components</h2>
 * <ul>
 *   <li>{@link io.github.og4dev.exception.GlobalExceptionHandler} — Central
 *       {@link org.springframework.web.bind.annotation.RestControllerAdvice} with 10 built-in
 *       {@link org.springframework.web.bind.annotation.ExceptionHandler} methods covering
 *       the most common Spring MVC error scenarios. Also discovers and invokes registered
 *       {@link io.github.og4dev.exception.ApiExceptionTranslator} beans.</li>
 *   <li>{@link io.github.og4dev.exception.ApiException} — Abstract base class for
 *       domain-specific business logic exceptions. Subclasses are automatically caught and
 *       converted to ProblemDetail responses by
 *       {@link io.github.og4dev.exception.GlobalExceptionHandler}.</li>
 *   <li>{@link io.github.og4dev.exception.ApiExceptionTranslator} — Strategy interface
 *       for translating third-party or framework exceptions (that cannot extend
 *       {@link io.github.og4dev.exception.ApiException}) into structured error responses
 *       without additional {@code @ExceptionHandler} methods.</li>
 * </ul>
 *
 * <h2>Error Response Format</h2>
 * <p>
 * All error responses conform to RFC 9457 ProblemDetail:
 * </p>
 * <pre>{@code
 * {
 *     "type": "about:blank",
 *     "title": "Not Found",
 *     "status": 404,
 *     "detail": "The requested resource '/api/users/99' was not found.",
 *     "traceId": "550e8400-e29b-41d4-a716-446655440000",
 *     "timestamp": "2026-03-03T10:30:45.123Z"
 * }
 * }</pre>
 *
 * <h2>Custom Business Logic Exceptions</h2>
 * <pre>{@code
 * public class UserNotFoundException extends ApiException {
 *     public UserNotFoundException(Long id) {
 *         super("User not found with ID: " + id, HttpStatus.NOT_FOUND);
 *     }
 * }
 * }</pre>
 *
 * <h2>Third-Party Exception Translation</h2>
 * <pre>{@code
 * @Component
 * public class EntityNotFoundTranslator
 *         implements ApiExceptionTranslator<EntityNotFoundException> {
 *
 *     @Override
 *     public Class<EntityNotFoundException> getTargetException() {
 *         return EntityNotFoundException.class;
 *     }
 *
 *     @Override
 *     public HttpStatus getStatus() { return HttpStatus.NOT_FOUND; }
 *
 *     @Override
 *     public String getMessage(EntityNotFoundException ex) {
 *         return "Entity not found: " + ex.getEntityName();
 *     }
 * }
 * }</pre>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.0.0
 * @see io.github.og4dev.exception.GlobalExceptionHandler
 * @see io.github.og4dev.exception.ApiException
 * @see io.github.og4dev.exception.ApiExceptionTranslator
 * @see org.springframework.http.ProblemDetail
 */
package io.github.og4dev.exception;

