package io.github.og4dev.exception;

import org.springframework.http.HttpStatus;

/**
 * Abstract base class for domain-specific API exceptions with an associated HTTP status code.
 * <p>
 * Extend this class to create typed business logic exceptions that are automatically
 * intercepted by {@link GlobalExceptionHandler} and converted to RFC 9457 ProblemDetail
 * responses without any additional {@code @ExceptionHandler} method.
 * </p>
 * <p>
 * Prefer {@code ApiException} subclasses over implementing {@link ApiExceptionTranslator}
 * when the exception is defined within your own codebase. Use {@link ApiExceptionTranslator}
 * for third-party exceptions that you cannot modify.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class UserNotFoundException extends ApiException {
 *     public UserNotFoundException(Long id) {
 *         super("User not found with ID: " + id, HttpStatus.NOT_FOUND);
 *     }
 * }
 *
 * public class InsufficientFundsException extends ApiException {
 *     public InsufficientFundsException(String accountId) {
 *         super("Insufficient funds in account: " + accountId, HttpStatus.PAYMENT_REQUIRED);
 *     }
 * }
 * }</pre>
 *
 * <h2>Resulting Error Response</h2>
 * <p>
 * Throwing any {@code ApiException} subclass produces an RFC 9457 ProblemDetail response:
 * </p>
 * <pre>{@code
 * {
 *     "type": "about:blank",
 *     "title": "Not Found",
 *     "status": 404,
 *     "detail": "User not found with ID: 42",
 *     "traceId": "550e8400-e29b-41d4-a716-446655440000",
 *     "timestamp": "2026-03-03T10:30:45.123Z"
 * }
 * }</pre>
 *
 * <h2>Benefits</h2>
 * <ul>
 *   <li>No {@code @ExceptionHandler} boilerplate required.</li>
 *   <li>RFC 9457 ProblemDetail formatting out of the box.</li>
 *   <li>Consistent error responses across all business exceptions.</li>
 *   <li>Automatic {@code WARN}-level logging with trace ID correlation.</li>
 * </ul>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.0.0
 * @see GlobalExceptionHandler
 * @see ApiExceptionTranslator
 * @see org.springframework.http.HttpStatus
 */
public abstract class ApiException extends RuntimeException {

    /**
     * The HTTP status code to include in the ProblemDetail error response.
     */
    private final HttpStatus status;

    /**
     * Constructs a new {@code ApiException} with a detail message and an HTTP status code.
     *
     * @param message the detail message surfaced in the {@code detail} field of the
     *                ProblemDetail response; must not be {@code null}
     * @param status  the HTTP status code for the error response; must not be {@code null}
     */
    protected ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Returns the HTTP status code associated with this exception.
     *
     * @return the HTTP status; never {@code null}
     */
    public HttpStatus getStatus() {
        return status;
    }
}