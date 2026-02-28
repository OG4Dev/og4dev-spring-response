package io.github.og4dev.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to explicitly enable automatic string trimming during JSON deserialization.
 * <p>
 * By default, the OG4Dev Spring API Response library does NOT automatically trim strings.
 * This annotation allows you to opt-in to automatic trimming for specific fields or entire
 * classes where removing leading and trailing whitespace is desired for data quality and consistency.
 * </p>
 * <p>
 * <b>Important:</b> When {@code @AutoTrim} is applied, XSS validation (HTML tag detection)
 * is still performed on the trimmed value to maintain security.
 * </p>
 *
 * <h2>Target Scopes</h2>
 * <ul>
 * <li><b>Field Level ({@link ElementType#FIELD}):</b> Applies trimming <i>only</i> to the specific annotated String field.</li>
 * <li><b>Class Level ({@link ElementType#TYPE}):</b> Applies trimming to <i>all</i> String fields within the annotated class globally.</li>
 * </ul>
 *
 * <h2>Example Usage: Field Level</h2>
 * <pre>{@code
 * public class UserRegistrationDTO {
 * @AutoTrim
 * private String username;       // Trimmed: "  john_doe  " → "john_doe"
 *
 * @AutoTrim
 * private String email;          // Trimmed: " user@example.com " → "user@example.com"
 *
 * private String password;       // NOT trimmed (no annotation)
 * private String bio;            // NOT trimmed (no annotation)
 * }
 * }</pre>
 *
 * <h2>Example Usage: Class Level</h2>
 * <pre>{@code
 * @AutoTrim // Automatically applies to ALL String fields in this class!
 * public class GlobalTrimDTO {
 * private String firstName;      // Trimmed: "  John  " → "John"
 * private String lastName;       // Trimmed: " Doe  " → "Doe"
 * private String address;        // Trimmed: " 123 Main St " → "123 Main St"
 * }
 * }</pre>
 *
 * <h2>Input/Output Examples (Class Level)</h2>
 * <pre>{@code
 * // Request JSON for GlobalTrimDTO
 * {
 * "firstName": "\t\nJohn\t\n",
 * "lastName": "  Doe  ",
 * "address": " 123 Main St "
 * }
 *
 * // After Deserialization
 * firstName = "John"                  // ✓ Trimmed (due to class-level @AutoTrim)
 * lastName  = "Doe"                   // ✓ Trimmed (due to class-level @AutoTrim)
 * address   = "123 Main St"           // ✓ Trimmed (due to class-level @AutoTrim)
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
 * @AutoTrim // Trims all fields
 * public class SecureDTO {
 * @XssCheck
 * private String cleanInput;  // Both trimmed (from class scope) and XSS-validated
 * }
 * }</pre>
 *
 * <h2>How It Works</h2>
 * <p>
 * This annotation is processed by the {@code AdvancedStringDeserializer} in
 * {@link io.github.og4dev.config.ApiResponseAutoConfiguration#strictJsonCustomizer()}.
 * The deserializer uses {@link tools.jackson.databind.ValueDeserializer#createContextual}
 * to detect the annotation on either the field itself or its declaring class, creating a
 * specialized instance that enables trimming.
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
 * @version 1.4.0
 * @see io.github.og4dev.config.ApiResponseAutoConfiguration#strictJsonCustomizer()
 * @see io.github.og4dev.annotation.XssCheck
 * @see tools.jackson.databind.ValueDeserializer#createContextual
 * @since 1.3.0
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface AutoTrim {
}