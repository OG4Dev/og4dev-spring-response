package io.github.og4dev.exception;

/**
 * Exception thrown when XSS payloads or raw HTML tags are detected in incoming requests
 * for fields annotated with @XssCheck.
 */
public class XssValidationException extends RuntimeException {
    public XssValidationException(String message) {
        super(message);
    }
}