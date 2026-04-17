package io.github.og4dev.exception;

import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A thread-safe registry for dynamically mapping third-party or framework-specific exceptions
 * to standard HTTP statuses and user-friendly messages.
 * <p>
 * This registry allows developers to handle external exceptions (for example, SQL, Mongo, or
 * authentication exceptions) centrally without writing dedicated {@code @ExceptionHandler}
 * methods for each type.
 * </p>
 *
 * @author Pasindu OG
 * @version 1.5.0
 * @since 1.5.0
 */
public class ApiExceptionRegistry {

    private final Map<Class<? extends Exception>, ExceptionRule> registry = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Registers a custom or third-party exception type with a specific HTTP status and message.
     * <p>
     * Registration order matters when both parent and child exception types are registered.
     * The first assignable entry wins during lookup.
     * </p>
     *
     * @param exceptionClass the exception class to map
     * @param status the HTTP status to return
     * @param defaultMessage the fallback client-facing message
     * @param <T> the exception type
     * @return this registry for fluent chaining
     * @throws IllegalArgumentException if any argument is null or message is blank
     */
    @SuppressWarnings("unused")
    public <T extends Exception> ApiExceptionRegistry register(Class<T> exceptionClass, HttpStatus status, String defaultMessage) {
        Assert.notNull(exceptionClass, "Exception class must not be null");
        Assert.notNull(status, "HttpStatus must not be null");
        Assert.hasText(defaultMessage, "Default message must not be empty or null");

        registry.put(exceptionClass, new ExceptionRule(status, defaultMessage));
        return this;
    }

    /**
     * Returns the first matching rule for the given thrown exception type.
     *
     * @param exceptionClass the thrown exception class
     * @return matching rule or {@code null} when no mapping exists
     */
    public ExceptionRule getRule(Class<? extends Exception> exceptionClass) {
        if (exceptionClass == null) {
            return null;
        }

        synchronized (registry) {
            for (Map.Entry<Class<? extends Exception>, ExceptionRule> entry : registry.entrySet()) {
                if (entry.getKey().isAssignableFrom(exceptionClass)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Value object containing status and message for a registered exception mapping.
     */
    @SuppressWarnings("ClassCanBeRecord")
    public static class ExceptionRule {
        private final HttpStatus status;
        private final String message;

        /**
         * Creates a new exception mapping rule.
         *
         * @param status mapped HTTP status
         * @param message mapped client-facing message
         */
        public ExceptionRule(HttpStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        /**
         * @return mapped HTTP status
         */
        public HttpStatus getStatus() {
            return status;
        }

        /**
         * @return mapped client-facing message
         */
        public String getMessage() {
            return message;
        }
    }
}