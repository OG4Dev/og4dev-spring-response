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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global exception handler for Spring Boot REST APIs with comprehensive error coverage.
 * <p>
 * This class provides production-ready centralized exception handling using Spring's
 * {@link RestControllerAdvice} mechanism. It automatically converts various exceptions
 * into RFC 9457 ProblemDetail responses (the latest standard, superseding RFC 7807) with
 * trace IDs for debugging and request correlation.
 * </p>
 * <p>
 * <b>Supported Exception Types (10 handlers):</b>
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
 * @version 1.1.0
 * @since 1.0.0
 * @see org.springframework.web.bind.annotation.RestControllerAdvice
 * @see org.springframework.http.ProblemDetail
 * @see io.github.og4dev.exception.ApiException
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

    /**
     * Default constructor for Spring bean instantiation.
     */
    public GlobalExceptionHandler() {
        // Default constructor for Spring bean instantiation
    }

    /**
     * Retrieves the trace ID from MDC or generates a new one if not present.
     *
     * @return the trace ID
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
     * Handles all unhandled exceptions.
     *
     * @param ex the exception
     * @return ProblemDetail response with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllExceptions(Exception ex) {
        String traceId = getOrGenerateTraceId();
        StackTraceElement rootCause = ex.getStackTrace().length > 0 ? ex.getStackTrace()[0] : null;
        String className = (rootCause != null) ? rootCause.getClassName() : "Unknown Class";
        int lineNumber = (rootCause != null) ? rootCause.getLineNumber() : -1;

        log.error("[TraceID: {}] Error in {}:{} - Message: {}",
                traceId, className, lineNumber, ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error. Please contact technical support");
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles validation exceptions from @Valid annotations.
     *
     * @param ex the validation exception
     * @return ProblemDetail response with 400 status and field errors
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
     * Handles method argument type mismatch exceptions.
     *
     * @param ex the type mismatch exception
     * @return ProblemDetail response with 400 status
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
     * Handles malformed JSON request exceptions.
     *
     * @param ex the HTTP message not readable exception
     * @return ProblemDetail response with 400 status
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
     * Handles missing required request parameter exceptions.
     *
     * @param ex the missing parameter exception
     * @return ProblemDetail response with 400 status
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
     * Handles 404 Not Found exceptions.
     *
     * @param ex the no resource found exception
     * @return ProblemDetail response with 404 status
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
     * Handles HTTP method not supported exceptions.
     *
     * @param ex the method not supported exception
     * @return ProblemDetail response with 405 status
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
     * Handles unsupported media type exceptions.
     *
     * @param ex the media type not supported exception
     * @return ProblemDetail response with 415 status
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
     * Handles null pointer exceptions.
     *
     * @param ex the null pointer exception
     * @return ProblemDetail response with 500 status
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
     * Handles custom API exceptions.
     *
     * @param ex the API exception
     * @return ProblemDetail response with the exception's status code
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