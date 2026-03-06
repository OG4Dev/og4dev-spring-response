package io.github.og4dev.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Opt-in annotation to enable XSS (Cross-Site Scripting) protection for string fields
 * during JSON deserialization.
 * <p>
 * By default the OG4Dev Spring API Response library does <b>not</b> validate string values.
 * Placing {@code @XssCheck} on a field or class opts in to automatic HTML and XML tag
 * detection at the deserialization layer. Any string containing a tag pattern is rejected
 * immediately with an HTTP 400 Bad Request response — the malicious payload never reaches
 * application logic or the database.
 * </p>
 * <p>
 * This implements a <b>fail-fast rejection strategy</b> which is more secure than HTML
 * escaping because it prevents stored XSS, DOM-based XSS, and second-order injection
 * vulnerabilities at the earliest possible point.
 * </p>
 *
 * <h2>Target Scopes</h2>
 * <ul>
 *   <li><b>Field Level ({@link ElementType#FIELD}):</b> Validates <i>only</i> the annotated
 *       {@code String} field; all other fields in the class are unaffected.</li>
 *   <li><b>Class Level ({@link ElementType#TYPE}):</b> Validates <i>all</i> {@code String}
 *       fields within the annotated class without requiring per-field annotations.</li>
 * </ul>
 *
 * <h2>Example — Field Level</h2>
 * <pre>{@code
 * public class CommentDTO {
 *
 *     @XssCheck
 *     private String content;    // Rejects HTML tags
 *
 *     @XssCheck
 *     private String authorName; // Rejects HTML tags
 *
 *     private String commentId;  // NOT validated (no annotation)
 * }
 * }</pre>
 *
 * <h2>Example — Class Level</h2>
 * <pre>{@code
 * @XssCheck
 * public class SecureUserProfileDTO {
 *     private String bio;         // Validated automatically
 *     private String displayName; // Validated automatically
 *     private String websiteUrl;  // Validated automatically
 * }
 * }</pre>
 *
 * <h2>Valid and Invalid Inputs</h2>
 * <pre>{@code
 * // Accepted
 * {"content": "Hello World"}               // Plain text
 * {"content": "Price: $100 < $200"}        // Comparison operator, not an HTML tag
 * {"content": "3 < 5 and 6 > 4"}          // Arithmetic, not HTML
 *
 * // Rejected with 400 Bad Request
 * {"content": "<script>alert(1)</script>"} // Script injection
 * {"content": "<img src=x onerror=...>"}   // Attribute-based XSS
 * {"content": "Hello<br>World"}            // HTML tag
 * {"content": "<!DOCTYPE html>"}           // DOCTYPE declaration
 * {"content": "</div>"}                    // Closing tag
 * }</pre>
 *
 * <h2>Error Response</h2>
 * <p>
 * When a tag is detected the request is rejected with an RFC 9457 ProblemDetail response:
 * </p>
 * <pre>{@code
 * {
 *     "type": "about:blank",
 *     "title": "Bad Request",
 *     "status": 400,
 *     "detail": "Security Error: HTML tags or XSS payloads are not allowed in the request.",
 *     "traceId": "550e8400-e29b-41d4-a716-446655440000",
 *     "timestamp": "2026-03-03T10:30:45.123Z"
 * }
 * }</pre>
 *
 * <h2>Detection Pattern</h2>
 * <p>
 * Tags are detected with the regular expression {@code (?s).*<\s*[a-zA-Z/!].*} (DOTALL mode).
 * It matches opening tags, closing tags, self-closing tags, HTML comments, DOCTYPE declarations,
 * and tags spanning multiple lines. Bare {@code <} characters in mathematical comparisons
 * (e.g., {@code 5 < 10}) are <b>not</b> matched because they are not followed by a letter,
 * slash, or exclamation mark.
 * </p>
 *
 * <h2>Combining with {@code @AutoTrim}</h2>
 * <p>
 * Both annotations may be applied together. Trimming is always applied first so that
 * whitespace-padded payloads (e.g., {@code "  <script>...  "}) are correctly detected after
 * the leading and trailing spaces are removed:
 * </p>
 * <pre>{@code
 * @AutoTrim
 * @XssCheck
 * private String username; // First trimmed, then validated for HTML tags
 * }</pre>
 *
 * <h2>Null Value Handling</h2>
 * <p>
 * {@code null} values bypass validation and pass through unchanged:
 * </p>
 * <pre>{@code
 * {"content": null}  → content = null  (no validation performed)
 * {"content": ""}    → content = ""    (validated; empty string is safe)
 * }</pre>
 *
 * <h2>How It Works</h2>
 * <p>
 * This annotation is detected by the {@code AdvancedStringDeserializer} registered via
 * {@link io.github.og4dev.config.ApiResponseAutoConfiguration#strictJsonCustomizer()}.
 * The deserializer inspects each field's annotations — and the annotations on its
 * declaring class — at mapper initialization time (once per field, not per request) and
 * returns a contextual instance with XSS validation enabled when {@code @XssCheck} is found.
 * </p>
 *
 * <h2>Performance</h2>
 * <p>
 * Validation adds minimal overhead (typically under {@code 1 ms} per field) because the
 * contextual deserializer is created once during {@code ObjectMapper} initialization,
 * not on every request.
 * </p>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.3.0
 * @see AutoTrim
 * @see io.github.og4dev.config.ApiResponseAutoConfiguration#strictJsonCustomizer()
 * @see io.github.og4dev.config.AdvancedStringDeserializer
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface XssCheck {
}