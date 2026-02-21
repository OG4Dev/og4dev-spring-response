/**
 * Field-level annotations for opt-in JSON deserialization security and data processing features.
 * <p>
 * This package provides annotations that enable fine-grained control over JSON string field processing
 * in the OG4Dev Spring API Response library. Unlike automatic processing approaches, these annotations
 * follow an <b>opt-in philosophy</b>: fields are preserved as-is by default, and you explicitly choose
 * which fields should be trimmed or validated.
 * </p>
 *
 * <h2>Philosophy: Opt-in by Default</h2>
 * <p>
 * Version 1.3.0 introduces an opt-in security model that gives developers complete control:
 * </p>
 * <ul>
 *   <li><b>Default Behavior:</b> String fields are preserved exactly as received (no modifications)</li>
 *   <li><b>Explicit Control:</b> Use annotations to enable trimming or XSS validation per field</li>
 *   <li><b>No Surprises:</b> Your data is never modified unless you explicitly request it</li>
 *   <li><b>Production-Ready:</b> Enable security features only where needed</li>
 * </ul>
 *
 * <h2>Available Annotations:</h2>
 * <ul>
 *   <li>{@link io.github.og4dev.annotation.AutoTrim} - Opt-in automatic whitespace trimming
 *       for specific string fields (e.g., usernames, emails, search queries)</li>
 *   <li>{@link io.github.og4dev.annotation.XssCheck} - Opt-in XSS validation with fail-fast
 *       HTML tag rejection (e.g., user comments, profile fields, user-generated content)</li>
 * </ul>
 *
 * <h2>Default Behavior (No Annotations):</h2>
 * <p>
 * By default, string fields in DTOs are <b>NOT modified</b> during JSON deserialization:
 * </p>
 * <ul>
 *   <li><b>No Trimming:</b> Leading and trailing whitespace is preserved</li>
 *   <li><b>No XSS Validation:</b> HTML tags are allowed (not checked)</li>
 *   <li><b>Null Preservation:</b> Null values remain null (not converted to empty strings)</li>
 * </ul>
 * <pre>{@code
 * public class DefaultDTO {
 *     private String username;  // Preserved as-is: "  john  " → "  john  "
 *     private String comment;   // Preserved as-is: "<b>Hi</b>" → "<b>Hi</b>"
 * }
 * }</pre>
 *
 * <h2>Opt-in Features:</h2>
 *
 * <h3>1. Automatic Trimming with {@code @AutoTrim}</h3>
 * <p>
 * Apply {@code @AutoTrim} to fields where you want to remove leading/trailing whitespace:
 * </p>
 * <pre>{@code
 * import io.github.og4dev.annotation.AutoTrim;
 *
 * public class UserRegistrationDTO {
 *     @AutoTrim
 *     private String username;      // Trimmed: "  john  " → "john"
 *
 *     @AutoTrim
 *     private String email;         // Trimmed: " user@example.com " → "user@example.com"
 *
 *     private String password;      // NOT trimmed: "  pass  " → "  pass  "
 * }
 * }</pre>
 *
 * <h3>2. XSS Validation with {@code @XssCheck}</h3>
 * <p>
 * Apply {@code @XssCheck} to fields where you want to reject HTML tags:
 * </p>
 * <pre>{@code
 * import io.github.og4dev.annotation.XssCheck;
 *
 * public class CommentDTO {
 *     @XssCheck
 *     private String content;       // Rejects: "<script>alert()</script>"
 *
 *     @XssCheck
 *     private String authorName;    // Rejects: "<b>John</b>"
 *
 *     private String commentId;     // Allows: "<id-123>" (no validation)
 * }
 * }</pre>
 *
 * <h3>3. Combining Both Annotations</h3>
 * <p>
 * Use both annotations together for fields that need trimming AND XSS validation:
 * </p>
 * <pre>{@code
 * import io.github.og4dev.annotation.AutoTrim;
 * import io.github.og4dev.annotation.XssCheck;
 *
 * public class SecureInputDTO {
 *     @AutoTrim
 *     @XssCheck
 *     private String username;  // First trimmed, then XSS-validated
 *
 *     @XssCheck
 *     private String comment;   // Only XSS-validated (not trimmed)
 *
 *     @AutoTrim
 *     private String email;     // Only trimmed (not XSS-validated)
 *
 *     private String bio;       // Neither (preserved as-is)
 * }
 * }</pre>
 *
 * <h2>Processing Order:</h2>
 * <p>
 * When both annotations are present on a field, processing happens in this order:
 * </p>
 * <ol>
 *   <li>String is trimmed (if {@code @AutoTrim} is present)</li>
 *   <li>Trimmed string is checked for HTML tags (if {@code @XssCheck} is present)</li>
 *   <li>If HTML tags found, an exception is thrown</li>
 * </ol>
 *
 * <h2>Migration from v1.2.0:</h2>
 * <p>
 * Version 1.2.0 automatically trimmed and validated all string fields. Version 1.3.0 requires
 * explicit annotations. To maintain the same behavior:
 * </p>
 * <pre>{@code
 * // v1.2.0 (automatic)
 * public class UserDTO {
 *     private String username;  // Was automatically trimmed
 * }
 *
 * // v1.3.0 (opt-in)
 * import io.github.og4dev.annotation.AutoTrim;
 * import io.github.og4dev.annotation.XssCheck;
 *
 * public class UserDTO {
 *     @AutoTrim
 *     @XssCheck
 *     private String username;  // Now explicitly enabled
 * }
 * }</pre>
 *
 * <h2>Integration with Jackson:</h2>
 * <p>
 * These annotations are processed by the {@code AdvancedStringDeserializer} registered in
 * {@link io.github.og4dev.config.ApiResponseAutoConfiguration#strictJsonCustomizer()}.
 * The deserializer uses Jackson's contextual deserialization mechanism
 * ({@link tools.jackson.databind.ValueDeserializer#createContextual}) to detect
 * annotations and create specialized deserializer instances with the appropriate behavior.
 * </p>
 *
 * <h2>Performance:</h2>
 * <p>
 * The annotation detection and deserializer creation happens once per field during Jackson
 * ObjectMapper initialization, not on every request. This ensures optimal runtime performance
 * with negligible overhead (typically &lt;1ms per request).
 * </p>
 *
 * @author Pasindu OG
 * @version 1.3.0
 * @see io.github.og4dev.annotation.AutoTrim
 * @see io.github.og4dev.annotation.XssCheck
 * @see io.github.og4dev.config.ApiResponseAutoConfiguration
 * @since 1.3.0
 */
package io.github.og4dev.annotation;


