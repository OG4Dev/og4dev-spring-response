package io.github.og4dev.exception;

import org.springframework.http.HttpStatus;

/**
 * Strategy interface for translating arbitrary exceptions into structured API error responses.
 * <p>
 * Implement this interface to register a custom exception translator that maps any
 * {@link Throwable} subtype to an HTTP status code and a human-readable error message
 * derived from the exception instance itself.
 * Registered translators are picked up by {@link GlobalExceptionHandler} and invoked
 * automatically when the matching exception type is thrown during request processing,
 * producing an RFC 9457 ProblemDetail response without requiring additional
 * {@code @ExceptionHandler} methods.
 * </p>
 * <p>
 * This is the recommended extension point when you need to handle third-party or
 * framework-level exceptions that cannot extend {@link ApiException}.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * @Component
 * public class EntityNotFoundTranslator implements ApiExceptionTranslator<EntityNotFoundException> {
 *
 *     @Override
 *     public Class<EntityNotFoundException> getTargetException() {
 *         return EntityNotFoundException.class;
 *     }
 *
 *     @Override
 *     public HttpStatus getStatus() {
 *         return HttpStatus.NOT_FOUND;
 *     }
 *
 *     @Override
 *     public String getMessage(EntityNotFoundException ex) {
 *         // Access the exception instance to build a contextual message
 *         return "Entity not found: " + ex.getEntityName();
 *     }
 * }
 * }</pre>
 *
 * <h2>Error Response Format</h2>
 * <p>
 * When a matching exception is caught, the handler produces an RFC 9457 ProblemDetail
 * response using the values supplied by this interface:
 * </p>
 * <pre>{@code
 * {
 *     "type": "about:blank",
 *     "title": "Not Found",
 *     "status": 404,
 *     "detail": "Entity not found: User",
 *     "traceId": "550e8400-e29b-41d4-a716-446655440000",
 *     "timestamp": "2026-03-03T10:30:45.123Z"
 * }
 * }</pre>
 *
 * <h2>Type Parameter</h2>
 * <p>
 * The generic type parameter {@code T} constrains the translator to a specific exception
 * type, providing compile-time safety and giving {@link #getMessage(Throwable)} access to
 * the strongly-typed exception instance so contextual details (e.g., entity names, field
 * values) can be included in the error message.
 * </p>
 *
 * @param <T> the specific {@link Throwable} subtype this translator handles
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.4.0
 * @see ApiException
 * @see GlobalExceptionHandler
 * @see org.springframework.http.ProblemDetail
 * @see org.springframework.http.HttpStatus
 */
public interface ApiExceptionTranslator<T extends Throwable> {

    /**
     * Returns the exact exception class this translator is responsible for handling.
     * <p>
     * The {@link GlobalExceptionHandler} uses this value to match incoming exceptions
     * against registered translators via
     * {@link Class#isAssignableFrom(Class)}, so a translator registered for a supertype
     * will also be invoked for its subtypes. Register a more specific translator to
     * override the behaviour for a particular subtype.
     * </p>
     *
     * @return the {@link Class} object representing the target exception type {@code T};
     *         never {@code null}
     */
    Class<T> getTargetException();

    /**
     * Returns the HTTP status code to be used in the error response when the target
     * exception is caught.
     * <p>
     * Choose a status that accurately reflects the nature of the error. Common mappings:
     * </p>
     * <ul>
     *   <li>{@link HttpStatus#BAD_REQUEST} (400) — invalid client input</li>
     *   <li>{@link HttpStatus#NOT_FOUND} (404) — resource does not exist</li>
     *   <li>{@link HttpStatus#CONFLICT} (409) — state conflict (e.g., duplicate entry)</li>
     *   <li>{@link HttpStatus#UNPROCESSABLE_CONTENT} (422) — semantically invalid request</li>
     *   <li>{@link HttpStatus#INTERNAL_SERVER_ERROR} (500) — unexpected server-side failure</li>
     * </ul>
     *
     * @return the {@link HttpStatus} to set on the ProblemDetail response; never {@code null}
     */
    HttpStatus getStatus();

    /**
     * Produces the human-readable detail message to include in the error response body,
     * using the caught exception instance to provide contextual information.
     * <p>
     * The returned value is mapped to the {@code detail} field of the RFC 9457 ProblemDetail
     * response. Access the exception's fields or message to build a specific, actionable
     * description. The message must be safe to expose to API consumers — it must
     * <b>not</b> leak internal stack traces, class names, or sensitive data.
     * </p>
     * <p>
     * Example implementation that extracts context from the exception:
     * </p>
     * <pre>{@code
     * @Override
     * public String getMessage(EntityNotFoundException ex) {
     *     return "Entity not found: " + ex.getEntityName();
     * }
     * }</pre>
     *
     * @param ex the caught exception instance of type {@code T}; never {@code null}
     * @return a non-{@code null}, client-safe error detail message derived from {@code ex}
     */
    String getMessage(T ex);
}
