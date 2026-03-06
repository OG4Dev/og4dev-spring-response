package io.github.og4dev.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Opt-in annotation to enable automatic whitespace trimming for string fields during
 * JSON deserialization.
 * <p>
 * By default the OG4Dev Spring API Response library does <b>not</b> modify string values.
 * Placing {@code @AutoTrim} on a field or class opts in to automatic removal of leading
 * and trailing whitespace at the deserialization layer, before the value reaches application
 * code, ensuring consistent data quality without manual {@code .trim()} calls.
 * </p>
 *
 * <h2>Target Scopes</h2>
 * <ul>
 *   <li><b>Field Level ({@link ElementType#FIELD}):</b> Trims <i>only</i> the annotated
 *       {@code String} field; all other fields in the class are unaffected.</li>
 *   <li><b>Class Level ({@link ElementType#TYPE}):</b> Trims <i>all</i> {@code String}
 *       fields within the annotated class without requiring per-field annotations.</li>
 * </ul>
 *
 * <h2>Example — Field Level</h2>
 * <pre>{@code
 * public class UserRegistrationDTO {
 *
 *     @AutoTrim
 *     private String username;   // "  john_doe  " → "john_doe"
 *
 *     @AutoTrim
 *     private String email;      // " user@example.com " → "user@example.com"
 *
 *     private String password;   // untouched — "  secret  " → "  secret  "
 * }
 * }</pre>
 *
 * <h2>Example — Class Level</h2>
 * <pre>{@code
 * @AutoTrim
 * public class AddressDTO {
 *     private String street;   // "  123 Main St  " → "123 Main St"
 *     private String city;     // "  London  " → "London"
 *     private String postCode; // "  SW1A 1AA  " → "SW1A 1AA"
 * }
 * }</pre>
 *
 * <h2>Combining with {@code @XssCheck}</h2>
 * <p>
 * Both annotations may be applied together. When combined, trimming is applied first
 * and XSS validation is then performed on the trimmed value, ensuring that whitespace
 * padding cannot be used to bypass HTML tag detection:
 * </p>
 * <pre>{@code
 * @AutoTrim
 * @XssCheck
 * private String comment; // First trimmed, then validated for HTML tags
 * }</pre>
 *
 * <h2>Null Value Handling</h2>
 * <p>
 * {@code null} values pass through unchanged and are never converted to empty strings:
 * </p>
 * <pre>{@code
 * {"name": null}   → name = null  (not "")
 * {"name": ""}     → name = ""
 * {"name": "  "}   → name = ""    (trimmed to empty)
 * }</pre>
 *
 * <h2>How It Works</h2>
 * <p>
 * This annotation is detected by the {@code AdvancedStringDeserializer} registered via
 * {@link io.github.og4dev.config.ApiResponseAutoConfiguration#strictJsonCustomizer()}.
 * The deserializer inspects each field's annotations — and the annotations on its
 * declaring class — at mapper initialization time (once per field, not per request) and
 * returns a contextual instance with trimming enabled when {@code @AutoTrim} is found.
 * </p>
 *
 * <h2>Performance</h2>
 * <p>
 * Trimming adds negligible overhead (typically under {@code 0.1 ms} per field) because
 * the contextual deserializer is created once during {@code ObjectMapper} initialization,
 * not on every request.
 * </p>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.3.0
 * @see XssCheck
 * @see io.github.og4dev.config.ApiResponseAutoConfiguration#strictJsonCustomizer()
 * @see io.github.og4dev.config.AdvancedStringDeserializer
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface AutoTrim {
}