package io.github.og4dev.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to disable automatic string trimming during JSON deserialization.
 * <p>
 * By default, the OG4Dev Spring API Response library automatically trims leading and trailing
 * whitespace from all string fields during JSON deserialization for data quality and security.
 * This annotation allows you to opt-out of automatic trimming for specific fields where
 * preserving the original whitespace is critical.
 * </p>
 * <p>
 * <b>Important:</b> Even when {@code @NoTrim} is applied, XSS validation (HTML tag detection)
 * is still performed on the field value to maintain security.
 * </p>
 *
 * <h2>Use Cases</h2>
 * <ul>
 *   <li><b>Password fields:</b> Users may intentionally include leading/trailing spaces in passwords</li>
 *   <li><b>Code snippets:</b> Preserving exact spacing in source code or formatted text</li>
 *   <li><b>Base64-encoded data:</b> Encoded strings that must not be modified</li>
 *   <li><b>Whitespace-sensitive data:</b> Any field where original formatting matters</li>
 *   <li><b>API tokens/keys:</b> Security credentials that should be processed exactly as provided</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * public class UserDTO {
 *     private String username;      // Trimmed automatically: "  john  " → "john"
 *     private String email;         // Trimmed automatically: " test@example.com " → "test@example.com"
 *
 *     @NoTrim
 *     private String password;      // NOT trimmed: "  pass123  " → "  pass123  "
 *
 *     @NoTrim
 *     private String bio;           // NOT trimmed: preserves formatting
 * }
 * }</pre>
 *
 * <h2>Security Considerations</h2>
 * <p>
 * Even with {@code @NoTrim}, all string values are still validated for XSS attacks.
 * The following will still be rejected:
 * </p>
 * <pre>
 * {"password": "&lt;script&gt;alert('XSS')&lt;/script&gt;"}  // Rejected: Contains HTML tags
 * {"bio": "Hello &lt;b&gt;World&lt;/b&gt;"}                     // Rejected: Contains HTML tags
 * </pre>
 *
 * <h2>How It Works</h2>
 * <p>
 * This annotation is processed by the {@code AdvancedStringDeserializer} in
 * {@link io.github.og4dev.config.ApiResponseAutoConfiguration#strictJsonCustomizer()}.
 * The deserializer uses {@link tools.jackson.databind.ValueDeserializer#createContextual}
 * to detect the annotation and create a specialized instance that skips trimming.
 * </p>
 *
 * @author Pasindu OG
 * @version 1.2.0
 * @see io.github.og4dev.config.ApiResponseAutoConfiguration#strictJsonCustomizer()
 * @since 1.2.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface NoTrim {
}
