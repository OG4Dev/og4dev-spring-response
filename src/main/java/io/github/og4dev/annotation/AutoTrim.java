package io.github.og4dev.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to explicitly enable automatic string trimming during JSON deserialization.
 * <p>
 * By default, the OG4Dev Spring API Response library does NOT automatically trim strings.
 * This annotation allows you to opt-in to automatic trimming for specific fields where
 * removing leading and trailing whitespace is desired for data quality and consistency.
 * </p>
 * <p>
 * <b>Important:</b> When {@code @AutoTrim} is applied, XSS validation (HTML tag detection)
 * is still performed on the trimmed value to maintain security.
 * </p>
 *
 * <h2>Use Cases</h2>
 * <ul>
 *   <li><b>User input fields:</b> Names, emails, addresses where whitespace is typically unwanted</li>
 *   <li><b>Search queries:</b> Remove accidental spaces from user search inputs</li>
 *   <li><b>Usernames:</b> Ensure consistent username formatting without leading/trailing spaces</li>
 *   <li><b>Reference numbers:</b> IDs, codes, or identifiers that should not have extra whitespace</li>
 *   <li><b>Categories/Tags:</b> Taxonomy values that need consistent formatting</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * public class UserRegistrationDTO {
 *     @AutoTrim
 *     private String username;       // Trimmed: "  john_doe  " → "john_doe"
 *
 *     @AutoTrim
 *     private String email;          // Trimmed: " user@example.com " → "user@example.com"
 *
 *     @AutoTrim
 *     private String firstName;      // Trimmed: "  John  " → "John"
 *
 *     private String password;       // NOT trimmed (no annotation)
 *     private String bio;            // NOT trimmed (no annotation)
 * }
 * }</pre>
 *
 * <h2>Input/Output Examples</h2>
 * <pre>{@code
 * // Request JSON
 * {
 *   "username": "  john_doe  ",
 *   "email": " john@example.com ",
 *   "firstName": "\t\nJohn\t\n",
 *   "password": "  myPass123  ",
 *   "bio": "  Software Developer  "
 * }
 *
 * // After Deserialization
 * username  = "john_doe"              // ✓ Trimmed (has @AutoTrim)
 * email     = "john@example.com"      // ✓ Trimmed (has @AutoTrim)
 * firstName = "John"                  // ✓ Trimmed (has @AutoTrim)
 * password  = "  myPass123  "         // ✗ NOT trimmed (no annotation)
 * bio       = "  Software Developer  " // ✗ NOT trimmed (no annotation)
 * }</pre>
 *
 * <h2>XSS Validation Still Active</h2>
 * <p>
 * Even with {@code @AutoTrim}, all string values are still validated for XSS attacks.
 * The following will still be rejected:
 * </p>
 * <pre>{@code
 * {"username": "  <script>alert('XSS')</script>  "}  // Rejected: Contains HTML tags
 * {"email": "user@example.com<b>test</b>"}          // Rejected: Contains HTML tags
 * }</pre>
 *
 * <h2>Combining with @XssCheck</h2>
 * <p>
 * You can combine {@code @AutoTrim} with {@link XssCheck @XssCheck} for both behaviors:
 * </p>
 * <pre>{@code
 * public class SecureDTO {
 *     @AutoTrim
 *     @XssCheck
 *     private String cleanInput;  // Both trimmed and XSS-validated
 * }
 * }</pre>
 *
 * <h2>How It Works</h2>
 * <p>
 * This annotation is processed by the {@code AdvancedStringDeserializer} in
 * {@link io.github.og4dev.config.ApiResponseAutoConfiguration#strictJsonCustomizer()}.
 * The deserializer uses {@link tools.jackson.databind.ValueDeserializer#createContextual}
 * to detect the annotation and create a specialized instance that enables trimming.
 * </p>
 *
 * <h2>Null Value Handling</h2>
 * <p>
 * Null values are preserved and never converted to empty strings:
 * </p>
 * <pre>{@code
 * {"name": null}      → name = null (not "")
 * {"name": ""}        → name = ""
 * {"name": "  "}      → name = ""   (trimmed to empty)
 * }</pre>
 *
 * <h2>Performance Considerations</h2>
 * <p>
 * The trimming operation is highly optimized and adds negligible overhead (typically {@code <0.1ms}
 * per field). The deserializer is created once per field during mapper initialization,
 * not on every request, ensuring optimal runtime performance.
 * </p>
 *
 * @author Pasindu OG
 * @version 1.3.0
 * @see io.github.og4dev.config.ApiResponseAutoConfiguration#strictJsonCustomizer()
 * @see io.github.og4dev.annotation.XssCheck
 * @see tools.jackson.databind.ValueDeserializer#createContextual
 * @since 1.3.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface AutoTrim {
}
