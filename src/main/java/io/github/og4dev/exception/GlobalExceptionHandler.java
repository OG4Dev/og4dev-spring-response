package io.github.og4dev.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.*;

/**
 * Global exception handler for Spring Boot REST APIs with comprehensive error coverage.
 * <p>
 * This class provides production-ready centralized exception handling using Spring's
 * {@link RestControllerAdvice} mechanism. It automatically converts various exceptions
 * into RFC 9457 ProblemDetail responses (the latest standard, superseding RFC 7807) with
 * trace IDs for debugging and request correlation.
 * </p>
 * <p>
 * <b>Built-in Supported Exception Types (10 handlers):</b>
 * </p>
 * <ol>
 *   <li><b>General Exceptions</b> - Catches all unhandled exceptions (HTTP 500)</li>
 *   <li><b>Validation Errors</b> - {@code @Valid} annotation failures (HTTP 400)</li>
 *   <li><b>Type Mismatches</b> - Method argument type conversion errors (HTTP 400)</li>
 *   <li><b>Malformed JSON</b> - Invalid request body format (HTTP 400)</li>
 *   <li><b>Missing Parameters</b> - Required {@code @RequestParam} missing (HTTP 400)</li>
 *   <li><b>404 Not Found</b> - Missing endpoints or resources (HTTP 404)</li>
 *   <li><b>Method Not Allowed</b> - Unsupported HTTP methods (HTTP 405)</li>
 *   <li><b>Unsupported Media Type</b> - Invalid Content-Type headers (HTTP 415)</li>
 *   <li><b>Null Pointer Exceptions</b> - NullPointerException handling (HTTP 500)</li>
 *   <li><b>Custom API Exceptions</b> - Domain-specific business logic errors (custom status)</li>
 * </ol>
 * <p>
 * <b>Extensible Exception Handling via {@link ApiExceptionTranslator}:</b>
 * Register one or more {@link ApiExceptionTranslator} beans in the application context to handle
 * third-party or framework exceptions that cannot extend {@link ApiException}. Each translator
 * is automatically discovered and invoked when its target exception type is thrown, producing
 * a consistent RFC 9457 ProblemDetail response without any additional {@code @ExceptionHandler}
 * methods.
 * </p>
 * <p>
 * <b>Error Response Format (RFC 9457 ProblemDetail):</b>
 * </p>
 * <ul>
 *   <li><b>type</b> - URI reference identifying the problem type (defaults to "about:blank")</li>
 *   <li><b>title</b> - Short, human-readable summary of the problem</li>
 *   <li><b>status</b> - HTTP status code</li>
 *   <li><b>detail</b> - Human-readable explanation specific to this occurrence</li>
 *   <li><b>traceId</b> - Unique UUID for request correlation and debugging</li>
 *   <li><b>timestamp</b> - RFC 3339 UTC timestamp</li>
 *   <li><b>errors</b> - Validation field errors (for validation failures only)</li>
 * </ul>
 * <p>
 * <b>Trace ID Management:</b> All exception handlers ensure consistent trace IDs between logs
 * and error responses. If no trace ID exists in MDC, one is automatically generated to ensure
 * every error has a correlatable identifier.
 * </p>
 * <p>
 * <b>Disabling the Handler:</b> This handler can be disabled by setting the application property
 * {@code api-response.enabled=false}.
 * </p>
 * <p>
 * <b>Logging:</b> All exceptions are automatically logged with appropriate severity levels:
 * </p>
 * <ul>
 *   <li><b>ERROR</b> - General exceptions, null pointer exceptions</li>
 *   <li><b>WARN</b> - Validation errors, type mismatches, business logic exceptions, 400/404/405/415 errors</li>
 * </ul>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.0.0
 * @see org.springframework.web.bind.annotation.RestControllerAdvice
 * @see org.springframework.http.ProblemDetail
 * @see ApiException
 * @see ApiExceptionTranslator
 */
@ConditionalOnProperty(
        prefix = "api-response",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RestControllerAdvice
@SuppressWarnings({"unused","java:S1192"})
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final List<ApiExceptionTranslator<?>> translators;
    /**
     * Constructs a new {@code GlobalExceptionHandler} with an optional list of custom
     * {@link ApiExceptionTranslator} beans.
     * <p>
     * The provided translators are stored and consulted by the general
     * {@link #handleAllExceptions(Exception)} handler before falling back to the default
     * HTTP 500 response, allowing third-party exceptions to be mapped to meaningful
     * RFC 9457 ProblemDetail responses without additional {@code @ExceptionHandler} methods.
     * </p>
     *
     * @param translators an optional list of {@link ApiExceptionTranslator} beans injected
     *                    by Spring; may be {@code null} if no translators are registered,
     *                    in which case an empty list is used
     */
    public GlobalExceptionHandler(List<ApiExceptionTranslator<?>> translators) {
        this.translators = translators != null ? translators : Collections.emptyList();
    }

    /**
     * Returns the current trace ID from SLF4J MDC, generating and storing a new UUID
     * if none is present.
     * <p>
     * This ensures every error response carries a trace ID regardless of whether a
     * {@link io.github.og4dev.filter.TraceIdFilter} is registered, so logs and responses
     * are always correlatable.
     * </p>
     *
     * @return the existing or newly generated trace ID string; never {@code null}
     */
    private String getOrGenerateTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            MDC.put("traceId", traceId);
        }
        return traceId;
    }

    /**
     * Handles all unhandled exceptions and dispatches to registered {@link ApiExceptionTranslator}
     * beans before falling back to a generic HTTP 500 response.
     * <p>
     * Processing order:
     * </p>
     * <ol>
     *   <li>Logs the exception at {@code ERROR} level with the trace ID, source class, and line number.</li>
     *   <li>Iterates registered {@link ApiExceptionTranslator} beans and checks whether any translator's
     *       {@link ApiExceptionTranslator#getTargetException()} is assignable from the thrown exception type.</li>
     *   <li>On a match, logs the translation at {@code WARN} level (exception type, translator class name,
     *       and translated message), then returns a ProblemDetail built from the translator's status and message.</li>
     *   <li>If no translator matches, returns a generic HTTP 500 ProblemDetail response.</li>
     * </ol>
     *
     * @param ex the unhandled exception
     * @return a {@link ProblemDetail} response — translated by a matching {@link ApiExceptionTranslator}
     *         if one is registered, otherwise HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllExceptions(Exception ex) {
        String traceId = getOrGenerateTraceId();
        StackTraceElement rootCause = ex.getStackTrace().length > 0 ? ex.getStackTrace()[0] : null;
        String className = (rootCause != null) ? rootCause.getClassName() : "Unknown Class";
        int lineNumber = (rootCause != null) ? rootCause.getLineNumber() : -1;

        log.error("[TraceID: {}] Error in {}:{} - Message: {}",
                traceId, className, lineNumber, ex.getMessage());

        if (translators != null) {
            for (ApiExceptionTranslator<?> translator : translators) {
                if (translator.getTargetException().isAssignableFrom(ex.getClass())) {

                    @SuppressWarnings("unchecked")
                    ApiExceptionTranslator<Exception> typedTranslator = (ApiExceptionTranslator<Exception>) translator;

                    log.warn("[TraceID: {}] Translated exception [{}] via {}: {}",
                            traceId,
                            ex.getClass().getSimpleName(),
                            typedTranslator.getClass().getSimpleName(),
                            typedTranslator.getMessage(ex));

                    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                            typedTranslator.getStatus(),
                            typedTranslator.getMessage(ex)
                    );
                    problemDetail.setProperty("traceId", traceId);
                    problemDetail.setProperty("timestamp", Instant.now());
                    return problemDetail;
                }
            }
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error. Please contact technical support");
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles validation failures raised by {@code @Valid} and {@code @Validated} annotations.
     * <p>
     * Collects all field-level constraint violations from the binding result. When multiple
     * violations exist for the same field, their messages are merged with a {@code "; "}
     * separator. The aggregated map is included in the {@code errors} extension field of the
     * ProblemDetail response and logged at {@code WARN} level.
     * </p>
     *
     * @param ex the validation exception containing one or more field errors
     * @return a {@link ProblemDetail} with HTTP 400 status and an {@code errors} map
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        String traceId = getOrGenerateTraceId();
        Map<String, String> errorMessage = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            assert fieldError.getDefaultMessage() != null;
            // If multiple errors exist for the same field, merge them
            errorMessage.merge(fieldError.getField(), fieldError.getDefaultMessage(),
                    (msg1, msg2) -> msg1 + "; " + msg2);
        }
        log.warn("[TraceID: {}] Validation error: {}", traceId, errorMessage);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation Failed");
        problemDetail.setProperty("errors", errorMessage);
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles type conversion failures for method arguments (e.g., passing a non-numeric
     * string where an {@code Integer} path variable is expected).
     * <p>
     * Logs the mismatch details at {@code WARN} level and returns a descriptive message
     * that includes the rejected value, the parameter name, and the expected type.
     * </p>
     *
     * @param ex the exception describing the type mismatch
     * @return a {@link ProblemDetail} with HTTP 400 status
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String traceId = getOrGenerateTraceId();
        String errorMessage = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s.",
                ex.getValue(), ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown");
        log.warn("[TraceID: {}] Type mismatch error: {}", traceId, errorMessage);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles malformed or unreadable JSON request bodies.
     * <p>
     * Triggered when Jackson cannot parse the incoming request body (e.g., missing quotes,
     * invalid structure, wrong data types). Logs at {@code WARN} level and returns a
     * generic message that guides the client to check the request body format.
     * </p>
     *
     * @param ex the exception raised when the HTTP message body cannot be read
     * @return a {@link ProblemDetail} with HTTP 400 status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String traceId = getOrGenerateTraceId();
        log.warn("[TraceID: {}] Malformed JSON request: {}", traceId, ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed JSON request. Please check your request body format.");
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles missing required {@code @RequestParam} query parameters.
     * <p>
     * Returns a descriptive message that includes the expected parameter name and its
     * declared type so the client can correct the request.
     * </p>
     *
     * @param ex the exception carrying the missing parameter name and type
     * @return a {@link ProblemDetail} with HTTP 400 status
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        String traceId = getOrGenerateTraceId();
        String message = String.format("Required request parameter '%s' (type: %s) is missing.",
                ex.getParameterName(), ex.getParameterType());
        log.warn("[TraceID: {}] Missing parameter: {}", traceId, message);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles requests for endpoints or static resources that do not exist.
     * <p>
     * Includes the requested resource path in the response message to help clients
     * identify the incorrect URL. Logs at {@code WARN} level.
     * </p>
     *
     * @param ex the exception carrying the unresolved resource path
     * @return a {@link ProblemDetail} with HTTP 404 status
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFoundException(NoResourceFoundException ex) {
        String traceId = getOrGenerateTraceId();
        String message = String.format("The requested resource '/%s' was not found.", ex.getResourcePath());
        log.warn("[TraceID: {}] 404 Not Found: {}", traceId, message);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles requests that use an HTTP method not supported by the target endpoint.
     * <p>
     * Includes the unsupported method and the list of allowed methods in the response
     * message so the client can retry with a valid method. Logs at {@code WARN} level.
     * </p>
     *
     * @param ex the exception carrying the unsupported method and the supported method set
     * @return a {@link ProblemDetail} with HTTP 405 status
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        String traceId = getOrGenerateTraceId();
        String message = String.format("Method '%s' is not supported for this endpoint. Supported methods are: %s",
                ex.getMethod(), ex.getSupportedHttpMethods());
        log.warn("[TraceID: {}] Method not allowed: {}", traceId, message);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, message);
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles requests whose {@code Content-Type} header specifies a media type not
     * accepted by the target endpoint.
     * <p>
     * Includes the received content type and the list of supported types in the response
     * message. Logs at {@code WARN} level.
     * </p>
     *
     * @param ex the exception carrying the unsupported content type and the supported set
     * @return a {@link ProblemDetail} with HTTP 415 status
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        String traceId = getOrGenerateTraceId();
        String message = String.format("Content type '%s' is not supported. Supported content types: %s",
                ex.getContentType(), ex.getSupportedMediaTypes());
        log.warn("[TraceID: {}] Unsupported media type: {}", traceId, message);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles {@link NullPointerException} thrown anywhere during request processing.
     * <p>
     * Logs the full stack trace at {@code ERROR} level for server-side investigation while
     * returning a generic, non-leaking message to the client. Stack trace details are
     * intentionally withheld from the response to prevent information disclosure.
     * </p>
     *
     * @param ex the null pointer exception
     * @return a {@link ProblemDetail} with HTTP 500 status
     */
    @ExceptionHandler(NullPointerException.class)
    public ProblemDetail handleNullPointerExceptions(NullPointerException ex) {
        String traceId = getOrGenerateTraceId();
        log.error("[TraceID: {}] Null pointer exception occurred: ", traceId, ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "A null pointer exception occurred.");
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles all {@link ApiException} subclasses representing domain-specific business
     * logic errors.
     * <p>
     * The HTTP status and detail message are taken directly from the exception instance,
     * giving each subclass full control over the error response. Logs at {@code WARN} level
     * with the trace ID, message, and status code.
     * </p>
     *
     * @param ex the domain exception carrying the status and detail message
     * @return a {@link ProblemDetail} whose status and {@code detail} field are sourced
     *         from {@link ApiException#getStatus()} and {@link ApiException#getMessage()}
     */
    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex) {
        String traceId = getOrGenerateTraceId();
        log.warn("[TraceID: {}] Business logic exception: {} | Status: {}", traceId, ex.getMessage(), ex.getStatus());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}