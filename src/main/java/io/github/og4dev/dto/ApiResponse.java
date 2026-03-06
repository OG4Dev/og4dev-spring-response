package io.github.og4dev.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

/**
 * Immutable, type-safe wrapper for standardized HTTP API responses.
 * <p>
 * Every response produced by this class follows the same four-field contract, ensuring a
 * uniform API surface across the entire application:
 * </p>
 * <ul>
 *   <li><b>status</b> — The HTTP status code (e.g., 200, 201, 404).</li>
 *   <li><b>message</b> — A human-readable description of the outcome.</li>
 *   <li><b>content</b> — The response payload of generic type {@code T};
 *       excluded from JSON serialization when {@code null}.</li>
 *   <li><b>timestamp</b> — An RFC 3339 UTC {@link Instant} auto-generated at
 *       construction time.</li>
 * </ul>
 *
 * <h2>Factory Methods</h2>
 * <p>
 * Use the static factory methods instead of the internal builder for common scenarios:
 * </p>
 * <pre>{@code
 * // HTTP 200 — with payload
 * return ApiResponse.success("User retrieved", user);
 *
 * // HTTP 200 — without payload
 * return ApiResponse.success("User deleted");
 *
 * // HTTP 201 — with payload
 * return ApiResponse.created("User created", newUser);
 *
 * // HTTP 201 — without payload
 * return ApiResponse.created("Resource created");
 *
 * // Custom status — with payload
 * return ApiResponse.status("Request accepted", data, HttpStatus.ACCEPTED);
 *
 * // Error — without payload
 * return ApiResponse.error("Insufficient funds", HttpStatus.PAYMENT_REQUIRED);
 * }</pre>
 *
 * <h2>JSON Output</h2>
 * <p>
 * Fields annotated with {@code @JsonInclude(NON_NULL)} are omitted from serialization
 * when {@code null}, keeping error or no-content responses concise:
 * </p>
 * <pre>{@code
 * // ApiResponse.success("User deleted") →
 * {
 *     "status": 200,
 *     "message": "User deleted",
 *     "timestamp": "2026-03-03T10:30:45.123Z"
 * }
 *
 * // ApiResponse.success("User retrieved", user) →
 * {
 *     "status": 200,
 *     "message": "User retrieved",
 *     "content": { "id": 1, "name": "Alice" },
 *     "timestamp": "2026-03-03T10:30:45.123Z"
 * }
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * All fields are {@code final} and set once at construction. Instances are immutable
 * and safe to share across threads without synchronization.
 * </p>
 *
 * @param <T> the type of the response content; use {@link Void} for responses without a payload
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.0.0
 * @see org.springframework.http.ResponseEntity
 * @see org.springframework.http.HttpStatus
 * @see io.github.og4dev.annotation.AutoResponse
 */
@SuppressWarnings({"unused"})
public class ApiResponse<T> {

    /**
     * The HTTP status code of the response.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Integer status;

    /**
     * A human-readable description of the response outcome.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String message;

    /**
     * The response payload; {@code null} for responses that carry no body content.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T content;

    /**
     * The UTC timestamp at which this response object was created.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Instant timestamp;

    /**
     * Constructs an {@code ApiResponse} from the given builder, auto-generating the
     * current UTC timestamp.
     *
     * @param builder the populated builder; must not be {@code null}
     */
    private ApiResponse(ApiResponseBuilder<T> builder) {
        this.status = builder.status;
        this.message = builder.message;
        this.content = builder.content;
        this.timestamp = Instant.now();
    }

    /**
     * Returns the HTTP status code.
     *
     * @return the status code, or {@code null} if not set
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * Returns the human-readable response message.
     *
     * @return the message, or {@code null} if not set
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the response content payload.
     *
     * @return the content, or {@code null} for no-content responses
     */
    public T getContent() {
        return content;
    }

    /**
     * Returns the UTC timestamp at which this response was created.
     *
     * @return the timestamp; never {@code null}
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Creates an HTTP 201 Created response with a message and no content.
     *
     * @param <T>     the type of the response content
     * @param message the human-readable response message; must not be {@code null}
     * @return a {@link ResponseEntity} with HTTP 201 status wrapping an {@code ApiResponse}
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseBuilder<T>()
                        .status(HttpStatus.CREATED.value())
                        .message(message)
                        .build());
    }

    /**
     * Creates an HTTP 201 Created response with a message and a content payload.
     *
     * @param <T>     the type of the response content
     * @param message the human-readable response message; must not be {@code null}
     * @param content the response payload; may be {@code null}
     * @return a {@link ResponseEntity} with HTTP 201 status wrapping an {@code ApiResponse}
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T content) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseBuilder<T>()
                        .status(HttpStatus.CREATED.value())
                        .message(message)
                        .content(content)
                        .build());
    }

    /**
     * Creates an HTTP 200 OK response with a message and no content.
     *
     * @param message the human-readable response message; must not be {@code null}
     * @return a {@link ResponseEntity} with HTTP 200 status wrapping an {@code ApiResponse<Void>}
     */
    public static ResponseEntity<ApiResponse<Void>> success(String message) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseBuilder<Void>()
                        .status(HttpStatus.OK.value())
                        .message(message)
                        .build());
    }

    /**
     * Creates an HTTP 200 OK response with a message and a content payload.
     *
     * @param <T>     the type of the response content
     * @param message the human-readable response message; must not be {@code null}
     * @param content the response payload; may be {@code null}
     * @return a {@link ResponseEntity} with HTTP 200 status wrapping an {@code ApiResponse}
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T content) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseBuilder<T>()
                        .status(HttpStatus.OK.value())
                        .message(message)
                        .content(content)
                        .build());
    }

    /**
     * Creates a response with a custom HTTP status and a message, carrying no content.
     *
     * @param message the human-readable response message; must not be {@code null}
     * @param status  the HTTP status to use; must not be {@code null}
     * @return a {@link ResponseEntity} with the specified status wrapping an {@code ApiResponse<Void>}
     */
    public static ResponseEntity<ApiResponse<Void>> status(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(new ApiResponseBuilder<Void>()
                        .status(status.value())
                        .message(message)
                        .build());
    }

    /**
     * Creates a response with a custom HTTP status, a message, and a content payload.
     *
     * @param <T>     the type of the response content
     * @param message the human-readable response message; must not be {@code null}
     * @param content the response payload; may be {@code null}
     * @param status  the HTTP status to use; must not be {@code null}
     * @return a {@link ResponseEntity} with the specified status wrapping an {@code ApiResponse}
     */
    public static <T> ResponseEntity<ApiResponse<T>> status(String message, T content, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(new ApiResponseBuilder<T>()
                        .status(status.value())
                        .message(message)
                        .content(content)
                        .build());
    }

    /**
     * Creates an error response with a custom HTTP status and a message, carrying no content.
     *
     * @param message the human-readable error message; must not be {@code null}
     * @param status  the HTTP error status to use; must not be {@code null}
     * @return a {@link ResponseEntity} with the specified status wrapping an {@code ApiResponse<Void>}
     */
    public static ResponseEntity<ApiResponse<Void>> error(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(new ApiResponseBuilder<Void>()
                        .status(status.value())
                        .message(message)
                        .build());
    }

    /**
     * Creates an error response with a custom HTTP status, a message, and a content payload.
     * <p>
     * Use this overload when you need to include structured error details (e.g., field-level
     * validation errors) alongside the error message.
     * </p>
     *
     * @param <T>     the type of the error detail content
     * @param message the human-readable error message; must not be {@code null}
     * @param content the structured error detail payload; may be {@code null}
     * @param status  the HTTP error status to use; must not be {@code null}
     * @return a {@link ResponseEntity} with the specified status wrapping an {@code ApiResponse}
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(String message, T content, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(new ApiResponseBuilder<T>()
                        .status(status.value())
                        .message(message)
                        .content(content)
                        .build());
    }

    /**
     * Builder for constructing {@link ApiResponse} instances with a fluent API.
     * <p>
     * Prefer the static factory methods ({@link #success}, {@link #created}, {@link #status},
     * {@link #error}) over this builder for common use cases.
     * </p>
     *
     * @param <T> the type of the response content
     */
    public static class ApiResponseBuilder<T> {

        private Integer status;
        private String message;
        private T content;

        /**
         * Creates an empty builder. All fields are {@code null} until explicitly set.
         */
        public ApiResponseBuilder() {
            // Default constructor
        }

        /**
         * Sets the HTTP status code.
         *
         * @param status the HTTP status code value (e.g., 200, 201, 404)
         * @return this builder instance for chaining
         */
        public ApiResponseBuilder<T> status(Integer status) {
            this.status = status;
            return this;
        }

        /**
         * Sets the human-readable response message.
         *
         * @param message the response message
         * @return this builder instance for chaining
         */
        public ApiResponseBuilder<T> message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the response content payload.
         *
         * @param content the content payload; may be {@code null}
         * @return this builder instance for chaining
         */
        public ApiResponseBuilder<T> content(T content) {
            this.content = content;
            return this;
        }

        /**
         * Builds and returns the {@link ApiResponse} instance.
         * <p>
         * The {@code timestamp} field is auto-generated to the current UTC instant at
         * the moment this method is called.
         * </p>
         *
         * @return a new, fully populated {@link ApiResponse}; never {@code null}
         */
        public ApiResponse<T> build() {
            return new ApiResponse<>(this);
        }
    }
}