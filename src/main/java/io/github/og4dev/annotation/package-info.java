/**
 * Custom annotations for controlling JSON deserialization behavior.
 * <p>
 * This package provides annotations that modify the default JSON processing behavior
 * of the OG4Dev Spring API Response library, allowing fine-grained control over
 * string field handling at the field level.
 * </p>
 * <h2>Available Annotations:</h2>
 * <ul>
 *   <li>{@link io.github.og4dev.annotation.NoTrim} - Disables automatic whitespace trimming
 *       for specific string fields while maintaining XSS validation</li>
 * </ul>
 * <h2>Default Behavior Without Annotations:</h2>
 * <p>
 * By default, all string fields in DTOs undergo automatic processing during JSON deserialization:
 * </p>
 * <ul>
 *   <li><b>Whitespace Trimming:</b> Leading and trailing spaces are removed</li>
 *   <li><b>XSS Validation:</b> HTML tags are detected and rejected</li>
 *   <li><b>Null Preservation:</b> Null values remain null (not converted to empty strings)</li>
 * </ul>
 * <h2>Use Case Example:</h2>
 * <pre>{@code
 * public class AuthDTO {
 *     private String username;      // Automatically trimmed
 *     private String email;         // Automatically trimmed
 *
 *     @NoTrim
 *     private String password;      // Preserves original whitespace
 * }
 * }</pre>
 * <h2>Integration with Jackson:</h2>
 * <p>
 * These annotations are processed by custom Jackson deserializers registered in
 * {@link io.github.og4dev.config.ApiResponseAutoConfiguration#strictJsonCustomizer()}.
 * The deserializers use Jackson's contextual deserialization mechanism
 * ({@link tools.jackson.databind.ValueDeserializer#createContextual}) to detect
 * annotations and adjust behavior accordingly.
 * </p>
 *
 * @author Pasindu OG
 * @version 1.2.0
 * @see io.github.og4dev.annotation.NoTrim
 * @see io.github.og4dev.config.ApiResponseAutoConfiguration
 * @since 1.2.0
 */
package io.github.og4dev.annotation;


