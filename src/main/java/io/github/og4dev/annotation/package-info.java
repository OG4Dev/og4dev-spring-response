/**
 * Opt-in annotations for JSON deserialization security and data quality.
 * <p>
 * This package provides three annotations that give developers fine-grained, declarative
 * control over how string fields are processed during Jackson deserialization. All features
 * follow an <b>opt-in philosophy</b> — no field is ever modified or validated unless an
 * annotation explicitly requests it.
 * </p>
 *
 * <h2>Available Annotations</h2>
 * <ul>
 *   <li>{@link io.github.og4dev.annotation.AutoResponse} — Enables automatic
 *       {@link io.github.og4dev.dto.ApiResponse} wrapping for a REST controller class or
 *       individual request-mapping method.</li>
 *   <li>{@link io.github.og4dev.annotation.AutoTrim} — Removes leading and trailing
 *       whitespace from annotated {@code String} fields or all {@code String} fields in an
 *       annotated class at deserialization time.</li>
 *   <li>{@link io.github.og4dev.annotation.XssCheck} — Rejects strings that contain HTML
 *       or XML tags with an HTTP 400 Bad Request error (fail-fast XSS prevention) for
 *       annotated fields or entire classes.</li>
 * </ul>
 *
 * <h2>Default Behavior</h2>
 * <p>
 * Without any annotation, string fields are passed through exactly as received:
 * </p>
 * <pre>{@code
 * public class DefaultDTO {
 *     private String username; // "  john  " stays "  john  "
 *     private String comment;  // "<b>Hi</b>" stays "<b>Hi</b>"
 * }
 * }</pre>
 *
 * <h2>Field-Level Usage</h2>
 * <pre>{@code
 * public class UserDTO {
 *
 *     @AutoTrim
 *     private String username;  // "  john  " → "john"
 *
 *     @XssCheck
 *     private String comment;   // "<script>" → 400 Bad Request
 *
 *     @AutoTrim
 *     @XssCheck
 *     private String bio;       // Trimmed first, then XSS-validated
 *
 *     private String role;      // Untouched
 * }
 * }</pre>
 *
 * <h2>Class-Level Usage</h2>
 * <pre>{@code
 * @AutoTrim
 * @XssCheck
 * public class SecureInputDTO {
 *     private String firstName; // Trimmed and XSS-validated automatically
 *     private String lastName;  // Trimmed and XSS-validated automatically
 * }
 * }</pre>
 *
 * <h2>Processing Order</h2>
 * <p>
 * When both {@code @AutoTrim} and {@code @XssCheck} are active on the same field,
 * processing always occurs in this order:
 * </p>
 * <ol>
 *   <li>The string value is trimmed.</li>
 *   <li>The trimmed value is checked for HTML or XML tags.</li>
 *   <li>If a tag is found, an {@link java.lang.IllegalArgumentException} is thrown,
 *       which produces an HTTP 400 Bad Request response.</li>
 * </ol>
 *
 * <h2>Integration</h2>
 * <p>
 * {@code @AutoTrim} and {@code @XssCheck} are processed by
 * {@link io.github.og4dev.config.AdvancedStringDeserializer}, which is registered with
 * Jackson via
 * {@link io.github.og4dev.config.ApiResponseAutoConfiguration#strictJsonCustomizer()}.
 * Annotation detection happens once per field during {@code ObjectMapper} initialization,
 * not on every request, keeping runtime overhead negligible.
 * </p>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.3.0
 * @see io.github.og4dev.annotation.AutoResponse
 * @see io.github.og4dev.annotation.AutoTrim
 * @see io.github.og4dev.annotation.XssCheck
 * @see io.github.og4dev.config.ApiResponseAutoConfiguration
 * @see io.github.og4dev.config.AdvancedStringDeserializer
 */
package io.github.og4dev.annotation;

