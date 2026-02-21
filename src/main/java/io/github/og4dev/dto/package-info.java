/**
 * Data Transfer Object (DTO) classes for standardized API responses.
 * <p>
 * This package contains classes for structuring API responses in a consistent, type-safe format.
 * The main class {@link io.github.og4dev.dto.ApiResponse} provides a generic wrapper that includes:
 * </p>
 * <ul>
 *   <li>HTTP status code</li>
 *   <li>Human-readable message</li>
 *   <li>Response content (generic type T)</li>
 *   <li>Automatic UTC timestamp generation</li>
 * </ul>
 * <p>
 * All response objects are immutable and thread-safe, using a builder pattern for flexible construction.
 * Factory methods like {@code success()}, {@code created()}, and {@code status()} provide convenient
 * ways to create responses without manual building.
 * </p>
 *
 * @author Pasindu OG
 * @version 1.2.0
 * @since 1.0.0
 */
package io.github.og4dev.dto;

