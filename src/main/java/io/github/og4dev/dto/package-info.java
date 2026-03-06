/**
 * Standardized API response DTO for consistent HTTP response formatting.
 * <p>
 * This package contains {@link io.github.og4dev.dto.ApiResponse}, the generic, immutable
 * response wrapper used throughout the OG4Dev Spring API Response Library. Every successful
 * API response produced by the library follows the same four-field structure:
 * </p>
 * <ul>
 *   <li><b>status</b> — The HTTP status code (e.g., 200, 201, 404).</li>
 *   <li><b>message</b> — A human-readable description of the result.</li>
 *   <li><b>content</b> — The response payload of generic type {@code T}; omitted from
 *       JSON when {@code null} (via {@code @JsonInclude(NON_NULL)}).</li>
 *   <li><b>timestamp</b> — An RFC 3339 UTC timestamp auto-generated at response creation.</li>
 * </ul>
 *
 * <h2>Factory Methods</h2>
 * <p>
 * {@link io.github.og4dev.dto.ApiResponse} exposes static factory methods for the most
 * common scenarios, eliminating the need to use the internal builder directly:
 * </p>
 * <ul>
 *   <li>{@code ApiResponse.success(message)} — HTTP 200, no content.</li>
 *   <li>{@code ApiResponse.success(message, content)} — HTTP 200 with a payload.</li>
 *   <li>{@code ApiResponse.created(message)} — HTTP 201, no content.</li>
 *   <li>{@code ApiResponse.created(message, content)} — HTTP 201 with a payload.</li>
 *   <li>{@code ApiResponse.status(message, status)} — Custom status, no content.</li>
 *   <li>{@code ApiResponse.status(message, content, status)} — Custom status with a payload.</li>
 *   <li>{@code ApiResponse.error(message, status)} — Error response, no content.</li>
 *   <li>{@code ApiResponse.error(message, content, status)} — Error response with details.</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * All fields are {@code final} and set at construction time, making
 * {@link io.github.og4dev.dto.ApiResponse} instances immutable and safe to share
 * across threads without synchronization.
 * </p>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.0.0
 * @see io.github.og4dev.dto.ApiResponse
 * @see io.github.og4dev.advice.GlobalResponseWrapper
 */
package io.github.og4dev.dto;

