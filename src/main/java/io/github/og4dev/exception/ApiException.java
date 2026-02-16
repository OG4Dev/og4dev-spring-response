package io.github.og4dev.exception;

import org.springframework.http.HttpStatus;

/**
 * Base abstract exception class for custom API-related business logic exceptions.
 * <p>
 * This class provides a foundation for creating domain-specific exceptions with associated
 * HTTP status codes. Extend this class to create business logic exceptions that are automatically
 * handled by {@link io.github.og4dev.exception.GlobalExceptionHandler} and converted to
 * RFC 9457 ProblemDetail responses.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * </p>
 * <pre>{@code
 * public class ResourceNotFoundException extends ApiException {
 *     public ResourceNotFoundException(String resource, Long id) {
 *         super(String.format("%s not found with ID: %d", resource, id), HttpStatus.NOT_FOUND);
 *     }
 * }
 *
 * public class InsufficientFundsException extends ApiException {
 *     public InsufficientFundsException(String accountId) {
 *         super("Insufficient funds in account: " + accountId, HttpStatus.PAYMENT_REQUIRED);
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Benefits:</b>
 * </p>
 * <ul>
 *   <li>Automatic exception handling - No need to create {@code @ExceptionHandler} methods</li>
 *   <li>RFC 9457 ProblemDetail formatting - Industry-standard error responses</li>
 *   <li>Type-safe with compile-time checking</li>
 *   <li>Clean, readable code - Express business rules clearly</li>
 *   <li>Consistent error responses - All custom exceptions follow the same format</li>
 * </ul>
 *
 * @author Pasindu OG
 * @version 1.0.0
 * @since 1.0.0
 * @see io.github.og4dev.exception.GlobalExceptionHandler
 * @see org.springframework.http.HttpStatus
 */
public abstract class ApiException extends RuntimeException {

    /**
     * The HTTP status code associated with this exception.
     */
    private final HttpStatus status;

    /**
     * Constructs a new ApiException with the specified message and status.
     *
     * @param message the detail message
     * @param status the HTTP status code
     */
    protected ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Gets the HTTP status code associated with this exception.
     *
     * @return the HTTP status
     */
    public HttpStatus getStatus() {
        return status;
    }
}