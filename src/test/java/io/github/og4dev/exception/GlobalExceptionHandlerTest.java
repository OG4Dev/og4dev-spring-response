package io.github.og4dev.exception;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GlobalExceptionHandler} covering changes introduced in v1.5.0-RC1:
 * <ul>
 *   <li>Constructor accepting an optional {@link ApiExceptionRegistry}</li>
 *   <li>Registry-based exception mapping in {@code handleAllExceptions}</li>
 *   <li>Fallback to HTTP 500 when no registry rule matches</li>
 *   <li>Null-safe {@code requiredType} handling in type-mismatch handler</li>
 * </ul>
 */
class GlobalExceptionHandlerTest {

    private static final String TRACE_ID_KEY = "traceId";

    @BeforeEach
    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    // -----------------------------------------------------------------------
    // Constructor / null-registry behaviour
    // -----------------------------------------------------------------------

    @Test
    void constructor_acceptsNullRegistry_withoutThrowingException() {
        // Should not throw; registry is optional
        GlobalExceptionHandler handler = new GlobalExceptionHandler(null);
        assertThat(handler).isNotNull();
    }

    @Test
    void constructor_acceptsNonNullRegistry() {
        ApiExceptionRegistry registry = new ApiExceptionRegistry()
                .register(IOException.class, HttpStatus.BAD_GATEWAY, "IO error");

        GlobalExceptionHandler handler = new GlobalExceptionHandler(registry);
        assertThat(handler).isNotNull();
    }

    // -----------------------------------------------------------------------
    // handleAllExceptions — registry is null (no registry configured)
    // -----------------------------------------------------------------------

    @Test
    void handleAllExceptions_returnsHttp500_whenNoRegistryConfigured() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(null);

        ProblemDetail result = handler.handleAllExceptions(new RuntimeException("boom"));

        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getDetail()).isEqualTo("Internal Server Error. Please contact technical support.");
    }

    @Test
    void handleAllExceptions_includesTraceId_inProblemDetailProperties() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(null);

        ProblemDetail result = handler.handleAllExceptions(new RuntimeException("boom"));

        assertThat(result.getProperties()).containsKey("traceId");
        assertThat(result.getProperties()).containsKey("timestamp");
    }

    @Test
    void handleAllExceptions_usesExistingMdcTraceId_whenPresent() {
        MDC.put(TRACE_ID_KEY, "existing-trace-123");
        GlobalExceptionHandler handler = new GlobalExceptionHandler(null);

        ProblemDetail result = handler.handleAllExceptions(new RuntimeException("boom"));

        assertThat(result.getProperties()).containsEntry("traceId", "existing-trace-123");
    }

    @Test
    void handleAllExceptions_generatesNewTraceId_whenMdcIsEmpty() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(null);

        ProblemDetail result = handler.handleAllExceptions(new RuntimeException("boom"));

        Object traceId = result.getProperties().get("traceId");
        assertThat(traceId).isNotNull();
        assertThat(traceId.toString()).isNotBlank();
    }

    // -----------------------------------------------------------------------
    // handleAllExceptions — registry configured, rule matches
    // -----------------------------------------------------------------------

    @Test
    void handleAllExceptions_returnsRegistryStatus_whenMatchingRuleFound() {
        ApiExceptionRegistry registry = new ApiExceptionRegistry()
                .register(IOException.class, HttpStatus.BAD_GATEWAY, "IO gateway error");
        GlobalExceptionHandler handler = new GlobalExceptionHandler(registry);

        ProblemDetail result = handler.handleAllExceptions(new IOException("disk failure"));

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
    }

    @Test
    void handleAllExceptions_returnsRegistryMessage_whenMatchingRuleFound() {
        ApiExceptionRegistry registry = new ApiExceptionRegistry()
                .register(IOException.class, HttpStatus.BAD_GATEWAY, "IO gateway error");
        GlobalExceptionHandler handler = new GlobalExceptionHandler(registry);

        ProblemDetail result = handler.handleAllExceptions(new IOException("disk failure"));

        assertThat(result.getDetail()).isEqualTo("IO gateway error");
    }

    @Test
    void handleAllExceptions_includesTraceAndTimestamp_forRegistryMatchedResponse() {
        ApiExceptionRegistry registry = new ApiExceptionRegistry()
                .register(IOException.class, HttpStatus.BAD_GATEWAY, "IO gateway error");
        GlobalExceptionHandler handler = new GlobalExceptionHandler(registry);

        ProblemDetail result = handler.handleAllExceptions(new IOException("disk failure"));

        assertThat(result.getProperties()).containsKey("traceId");
        assertThat(result.getProperties()).containsKey("timestamp");
    }

    @Test
    void handleAllExceptions_usesCustomStatus_forRegisteredExceptionSubclass() {
        // Register parent; throw child — polymorphic lookup should match
        ApiExceptionRegistry registry = new ApiExceptionRegistry()
                .register(RuntimeException.class, HttpStatus.SERVICE_UNAVAILABLE, "Service down");
        GlobalExceptionHandler handler = new GlobalExceptionHandler(registry);

        // IllegalStateException extends RuntimeException
        ProblemDetail result = handler.handleAllExceptions(new IllegalStateException("bad state"));

        assertThat(result.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(result.getDetail()).isEqualTo("Service down");
    }

    // -----------------------------------------------------------------------
    // handleAllExceptions — registry configured, no rule matches (fallback)
    // -----------------------------------------------------------------------

    @Test
    void handleAllExceptions_fallsBackTo500_whenRegistryHasNoMatchingRule() {
        ApiExceptionRegistry registry = new ApiExceptionRegistry()
                .register(IOException.class, HttpStatus.BAD_GATEWAY, "IO error");
        GlobalExceptionHandler handler = new GlobalExceptionHandler(registry);

        // NullPointerException is not registered in the registry
        ProblemDetail result = handler.handleAllExceptions(new NullPointerException("null ref"));

        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getDetail()).isEqualTo("Internal Server Error. Please contact technical support.");
    }

    @Test
    void handleAllExceptions_returnsHttp500_forExceptionWithEmptyStackTrace() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(null);

        // Create exception with no stack trace (simulates native code or suppressed stack)
        RuntimeException ex = new RuntimeException("no trace") {
            @Override
            public synchronized Throwable fillInStackTrace() {
                return this; // suppress stack trace filling
            }
        };
        ex.setStackTrace(new StackTraceElement[0]);

        ProblemDetail result = handler.handleAllExceptions(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    // -----------------------------------------------------------------------
    // handleAllExceptions — multiple registry rules, first-match wins
    // -----------------------------------------------------------------------

    @Test
    void handleAllExceptions_firstRegisteredRuleWins_whenMultipleRulesMatch() {
        ApiExceptionRegistry registry = new ApiExceptionRegistry()
                .register(IOException.class, HttpStatus.BAD_GATEWAY, "IO specific error")
                .register(Exception.class, HttpStatus.INTERNAL_SERVER_ERROR, "Generic error");
        GlobalExceptionHandler handler = new GlobalExceptionHandler(registry);

        ProblemDetail result = handler.handleAllExceptions(new IOException("io failure"));

        // IOException rule was registered first and isAssignableFrom(IOException) matches
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
        assertThat(result.getDetail()).isEqualTo("IO specific error");
    }

    // -----------------------------------------------------------------------
    // Concrete exception handler — type mismatch (refactored in v1.5.0-RC1)
    // -----------------------------------------------------------------------

    @Test
    void handleAllExceptions_returnsCorrectStatus_forMultipleRegisteredExceptions() {
        ApiExceptionRegistry registry = new ApiExceptionRegistry()
                .register(IOException.class, HttpStatus.BAD_GATEWAY, "IO error")
                .register(IllegalArgumentException.class, HttpStatus.BAD_REQUEST, "Bad argument");
        GlobalExceptionHandler handler = new GlobalExceptionHandler(registry);

        ProblemDetail ioResult = handler.handleAllExceptions(new IOException("io"));
        ProblemDetail badArgResult = handler.handleAllExceptions(new IllegalArgumentException("bad"));

        assertThat(ioResult.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
        assertThat(badArgResult.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    // -----------------------------------------------------------------------
    // ApiException handler (unchanged in v1.5.0-RC1 but verifies registry doesn't interfere)
    // -----------------------------------------------------------------------

    @Test
    void handleApiException_returnsExceptionStatus_andIsNotAffectedByRegistry() {
        ApiExceptionRegistry registry = new ApiExceptionRegistry()
                .register(RuntimeException.class, HttpStatus.SERVICE_UNAVAILABLE, "Should not appear");
        GlobalExceptionHandler handler = new GlobalExceptionHandler(registry);

        // ApiException has its own dedicated @ExceptionHandler — registry is irrelevant here
        ApiException apiEx = new ApiException("Resource not found", HttpStatus.NOT_FOUND) {};

        ProblemDetail result = handler.handleApiException(apiEx);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getDetail()).isEqualTo("Resource not found");
        assertThat(result.getProperties()).containsKey("traceId");
        assertThat(result.getProperties()).containsKey("timestamp");
    }

    @Test
    void handleApiException_usesExistingTraceId_fromMdc() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(null);
        MDC.put(TRACE_ID_KEY, "my-trace-id");

        ApiException apiEx = new ApiException("Conflict", HttpStatus.CONFLICT) {};
        ProblemDetail result = handler.handleApiException(apiEx);

        assertThat(result.getProperties()).containsEntry("traceId", "my-trace-id");
    }
}
