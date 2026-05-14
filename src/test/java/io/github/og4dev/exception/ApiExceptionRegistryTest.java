package io.github.og4dev.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ApiExceptionRegistry} (new in v1.5.0-RC1).
 */
class ApiExceptionRegistryTest {

    private ApiExceptionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ApiExceptionRegistry();
    }

    // --- register() tests ---

    @Test
    void register_returnsSelf_forFluentChaining() {
        ApiExceptionRegistry result = registry.register(IOException.class, HttpStatus.BAD_GATEWAY, "IO error");
        assertThat(result).isSameAs(registry);
    }

    @Test
    void register_storesRule_retrievableByGetRule() {
        registry.register(IOException.class, HttpStatus.BAD_GATEWAY, "IO error");

        ApiExceptionRegistry.ExceptionRule rule = registry.getRule(IOException.class);

        assertThat(rule).isNotNull();
        assertThat(rule.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(rule.getMessage()).isEqualTo("IO error");
    }

    @Test
    void register_throwsIllegalArgument_whenExceptionClassIsNull() {
        assertThatThrownBy(() -> registry.register(null, HttpStatus.INTERNAL_SERVER_ERROR, "error"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_throwsIllegalArgument_whenStatusIsNull() {
        assertThatThrownBy(() -> registry.register(IOException.class, null, "error"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_throwsIllegalArgument_whenMessageIsNull() {
        assertThatThrownBy(() -> registry.register(IOException.class, HttpStatus.INTERNAL_SERVER_ERROR, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_throwsIllegalArgument_whenMessageIsBlank() {
        assertThatThrownBy(() -> registry.register(IOException.class, HttpStatus.INTERNAL_SERVER_ERROR, "   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_throwsIllegalArgument_whenMessageIsEmpty() {
        assertThatThrownBy(() -> registry.register(IOException.class, HttpStatus.INTERNAL_SERVER_ERROR, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_overwritesExistingRule_forSameExceptionClass() {
        registry.register(IOException.class, HttpStatus.BAD_GATEWAY, "First message");
        registry.register(IOException.class, HttpStatus.SERVICE_UNAVAILABLE, "Second message");

        ApiExceptionRegistry.ExceptionRule rule = registry.getRule(IOException.class);

        assertThat(rule).isNotNull();
        assertThat(rule.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(rule.getMessage()).isEqualTo("Second message");
    }

    @Test
    void register_supportsMultipleExceptions_viaFluentChaining() {
        registry.register(IOException.class, HttpStatus.BAD_GATEWAY, "IO error")
                .register(SQLException.class, HttpStatus.INTERNAL_SERVER_ERROR, "DB error")
                .register(IllegalArgumentException.class, HttpStatus.BAD_REQUEST, "Bad argument");

        assertThat(registry.getRule(IOException.class)).isNotNull();
        assertThat(registry.getRule(SQLException.class)).isNotNull();
        assertThat(registry.getRule(IllegalArgumentException.class)).isNotNull();
    }

    // --- getRule() tests ---

    @Test
    void getRule_returnsNull_whenRegistryIsEmpty() {
        ApiExceptionRegistry.ExceptionRule rule = registry.getRule(RuntimeException.class);
        assertThat(rule).isNull();
    }

    @Test
    void getRule_returnsNull_whenExceptionClassIsNull() {
        registry.register(IOException.class, HttpStatus.BAD_GATEWAY, "IO error");

        ApiExceptionRegistry.ExceptionRule rule = registry.getRule(null);

        assertThat(rule).isNull();
    }

    @Test
    void getRule_returnsNull_whenNoMatchingRule() {
        registry.register(IOException.class, HttpStatus.BAD_GATEWAY, "IO error");

        ApiExceptionRegistry.ExceptionRule rule = registry.getRule(NullPointerException.class);

        assertThat(rule).isNull();
    }

    @Test
    void getRule_matchesExactExceptionClass() {
        registry.register(IllegalStateException.class, HttpStatus.CONFLICT, "Conflict occurred");

        ApiExceptionRegistry.ExceptionRule rule = registry.getRule(IllegalStateException.class);

        assertThat(rule).isNotNull();
        assertThat(rule.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(rule.getMessage()).isEqualTo("Conflict occurred");
    }

    @Test
    void getRule_matchesSubclassViaPolymorphism() {
        // Register parent class; thrown exception is a subclass
        registry.register(RuntimeException.class, HttpStatus.INTERNAL_SERVER_ERROR, "Runtime error");

        // IllegalStateException extends RuntimeException
        ApiExceptionRegistry.ExceptionRule rule = registry.getRule(IllegalStateException.class);

        assertThat(rule).isNotNull();
        assertThat(rule.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getRule_matchesMostSpecificFirst_whenParentAndChildBothRegistered() {
        // Registration order matters: child registered first wins for child exceptions
        registry.register(IllegalStateException.class, HttpStatus.CONFLICT, "State conflict");
        registry.register(RuntimeException.class, HttpStatus.INTERNAL_SERVER_ERROR, "General runtime error");

        // Child-specific rule should win because it was registered first
        ApiExceptionRegistry.ExceptionRule rule = registry.getRule(IllegalStateException.class);

        assertThat(rule).isNotNull();
        assertThat(rule.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(rule.getMessage()).isEqualTo("State conflict");
    }

    @Test
    void getRule_parentCatchesChildWhenRegisteredFirst() {
        // Parent registered first; child thrown - parent should match
        registry.register(RuntimeException.class, HttpStatus.INTERNAL_SERVER_ERROR, "General runtime error");
        registry.register(IllegalStateException.class, HttpStatus.CONFLICT, "State conflict");

        // Since RuntimeException is first in the registry and isAssignableFrom(IllegalStateException) is true,
        // it catches before checking IllegalStateException's own entry
        ApiExceptionRegistry.ExceptionRule rule = registry.getRule(IllegalStateException.class);

        assertThat(rule).isNotNull();
        // The first assignable entry wins - RuntimeException is first
        assertThat(rule.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getRule_returnsCorrectRuleForMultipleRegistrations() {
        registry.register(IOException.class, HttpStatus.BAD_GATEWAY, "IO error")
                .register(IllegalArgumentException.class, HttpStatus.BAD_REQUEST, "Bad argument");

        assertThat(registry.getRule(IOException.class).getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(registry.getRule(IllegalArgumentException.class).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // --- ExceptionRule value object tests ---

    @Test
    void exceptionRule_getStatus_returnsRegisteredStatus() {
        ApiExceptionRegistry.ExceptionRule rule =
                new ApiExceptionRegistry.ExceptionRule(HttpStatus.FORBIDDEN, "Forbidden");

        assertThat(rule.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void exceptionRule_getMessage_returnsRegisteredMessage() {
        ApiExceptionRegistry.ExceptionRule rule =
                new ApiExceptionRegistry.ExceptionRule(HttpStatus.FORBIDDEN, "Access denied");

        assertThat(rule.getMessage()).isEqualTo("Access denied");
    }

    // --- Thread-safety tests ---

    @Test
    void register_isSafeUnderConcurrentWrites() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger counter = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final String message = "Error " + i;
            futures.add(executor.submit(() -> {
                registry.register(IOException.class, HttpStatus.BAD_GATEWAY, message);
                counter.incrementAndGet();
            }));
        }

        for (Future<?> future : futures) {
            future.get(); // propagates exceptions
        }
        executor.shutdown();

        assertThat(counter.get()).isEqualTo(threadCount);
        // After all writes, a rule must exist
        assertThat(registry.getRule(IOException.class)).isNotNull();
    }

    @Test
    void getRule_isSafeUnderConcurrentReads() throws Exception {
        registry.register(IOException.class, HttpStatus.BAD_GATEWAY, "IO error");

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<ApiExceptionRegistry.ExceptionRule>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> registry.getRule(IOException.class)));
        }

        for (Future<ApiExceptionRegistry.ExceptionRule> future : futures) {
            ApiExceptionRegistry.ExceptionRule rule = future.get();
            assertThat(rule).isNotNull();
            assertThat(rule.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
        }
        executor.shutdown();
    }
}