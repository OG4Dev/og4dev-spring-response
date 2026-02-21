package io.github.og4dev.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to explicitly enable XSS (Cross-Site Scripting) validation for string fields during JSON deserialization.
 * <p>
 * By default, the OG4Dev Spring API Response library does NOT perform XSS validation on strings.
 * This annotation allows you to opt-in to automatic HTML/XML tag detection and rejection for specific fields
 * where preventing malicious content injection is critical for security.
 * </p>
 * <p>
 * <b>Security Approach:</b> This annotation implements a <b>fail-fast rejection strategy</b> - requests
 * containing HTML tags are rejected entirely with a 400 Bad Request error. This is more secure than
 * HTML escaping, as it prevents stored XSS, DOM-based XSS, and second-order injection vulnerabilities.
 * </p>
 *
 * <h2>Use Cases</h2>
 * <ul>
 *   <li><b>User-generated content:</b> Comments, reviews, forum posts, chat messages</li>
 *   <li><b>Profile information:</b> Usernames, display names, bio fields</li>
 *   <li><b>Search queries:</b> User input that will be displayed or processed</li>
 *   <li><b>Form inputs:</b> Any field that accepts free-form text from users</li>
 *   <li><b>API parameters:</b> String parameters that should not contain markup</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * public class CommentDTO {
 *     @XssCheck
 *     private String content;        // XSS validated - rejects HTML tags
 *
 *     @XssCheck
 *     private String authorName;     // XSS validated - rejects HTML tags
 *
 *     private String commentId;      // NOT validated (no annotation)
 *     private Instant timestamp;     // NOT validated (not a string)
 * }
 * }</pre>
 *
 * <h2>Valid and Invalid Inputs</h2>
 * <pre>{@code
 * // ✅ Valid inputs (accepted)
 * {"content": "Hello World"}                     // Plain text
 * {"content": "Price: $100 < $200"}              // Comparison operators (no tag)
 * {"content": "2 + 2 = 4"}                       // Math expressions
 * {"content": "Use angle brackets: 3 < 5"}       // Text with < but no HTML tag
 *
 * // ❌ Invalid inputs (rejected with 400 Bad Request)
 * {"content": "<script>alert('XSS')</script>"}   // Script injection
 * {"content": "<img src=x onerror=alert(1)>"}    // Image XSS attack
 * {"content": "Hello<br>World"}                  // HTML break tag
 * {"content": "<!--comment-->"}                  // HTML comment
 * {"content": "<!DOCTYPE html>"}                 // DOCTYPE declaration
 * {"content": "</div>"}                          // Closing tag
 * {"content": "<b>Bold text</b>"}                // HTML formatting
 * }</pre>
 *
 * <h2>Error Response Format</h2>
 * <p>
 * When HTML tags are detected, the request is rejected with a 400 Bad Request error:
 * </p>
 * <pre>{@code
 * {
 *   "type": "about:blank",
 *   "title": "Bad Request",
 *   "status": 400,
 *   "detail": "Security Error: HTML tags or XSS payloads are not allowed in the request.",
 *   "traceId": "550e8400-e29b-41d4-a716-446655440000",
 *   "timestamp": "2026-02-21T10:30:45.123Z"
 * }
 * }</pre>
 *
 * <h2>XSS Detection Mechanism</h2>
 * <p>
 * The validation uses a robust regex pattern: {@code (?s).*<\s*[a-zA-Z/!].*}
 * </p>
 * <p>
 * This pattern detects:
 * </p>
 * <ul>
 *   <li><b>Opening tags:</b> {@code <script>}, {@code <img>}, {@code <div>}, {@code <iframe>}</li>
 *   <li><b>Closing tags:</b> {@code </div>}, {@code </script>}, {@code </body>}</li>
 *   <li><b>Self-closing tags:</b> {@code <br/>}, {@code <input/>}</li>
 *   <li><b>Special tags:</b> {@code <!DOCTYPE>}, {@code <!--comment-->}, {@code <![CDATA[]]>}</li>
 *   <li><b>Tags with attributes:</b> {@code <div class="test">}, {@code <img src="x">}</li>
 *   <li><b>Multiline tags:</b> Tags spanning multiple lines (DOTALL mode enabled)</li>
 * </ul>
 * <p>
 * <b>What is NOT detected (safe to use):</b>
 * </p>
 * <ul>
 *   <li>Mathematical comparisons: {@code 5 < 10}, {@code x > y}</li>
 *   <li>Arrows and symbols: {@code -> <-}, {@code <=>}</li>
 *   <li>Quoted examples: {@code "less than symbol: <"} (if properly escaped in JSON)</li>
 * </ul>
 *
 * <h2>Why Rejection Instead of Escaping?</h2>
 * <p>
 * This library uses a <b>fail-fast rejection approach</b> rather than HTML escaping (converting {@code <} to {@code &lt;}).
 * This is more secure because:
 * </p>
 * <ul>
 *   <li><b>Prevents stored XSS:</b> Malicious content never enters your database</li>
 *   <li><b>Prevents DOM-based XSS:</b> No chance of client-side re-interpretation</li>
 *   <li><b>Prevents second-order attacks:</b> Escaped content cannot be un-escaped later</li>
 *   <li><b>Prevents encoding bypasses:</b> No risk of double-encoding vulnerabilities</li>
 *   <li><b>Clear security policy:</b> Users know HTML is not allowed</li>
 * </ul>
 *
 * <h2>Combining with @AutoTrim</h2>
 * <p>
 * You can combine {@code @XssCheck} with {@link AutoTrim @AutoTrim} for both behaviors:
 * </p>
 * <pre>{@code
 * public class SecureInputDTO {
 *     @AutoTrim
 *     @XssCheck
 *     private String username;  // First trimmed, then XSS-validated
 *
 *     @XssCheck
 *     private String comment;   // Only XSS-validated (not trimmed)
 * }
 *
 * // Processing order:
 * // 1. String is trimmed (if @AutoTrim is present)
 * // 2. Trimmed string is checked for HTML tags (if @XssCheck is present)
 * // 3. If HTML tags found, IllegalArgumentException is thrown
 * }</pre>
 *
 * <h2>How It Works</h2>
 * <p>
 * This annotation is processed by the {@code AdvancedStringDeserializer} in
 * {@link io.github.og4dev.config.ApiResponseAutoConfiguration#strictJsonCustomizer()}.
 * The deserializer uses {@link tools.jackson.databind.ValueDeserializer#createContextual}
 * to detect the annotation and create a specialized instance that enables XSS validation.
 * </p>
 *
 * <h2>Null Value Handling</h2>
 * <p>
 * Null values are not validated (they are safe) and pass through unchanged:
 * </p>
 * <pre>{@code
 * {"content": null}      → content = null (no validation)
 * {"content": ""}        → content = "" (validated, but empty string is safe)
 * {"content": "  "}      → content = "  " (validated, but whitespace is safe)
 * }</pre>
 *
 * <h2>Performance Considerations</h2>
 * <p>
 * The regex validation is highly optimized and adds minimal overhead (typically {@code <1ms}
 * per field). The deserializer is created once per field during mapper initialization,
 * not on every request, ensuring optimal runtime performance.
 * </p>
 *
 * <h2>When to Use This Annotation</h2>
 * <table border="1" cellpadding="5">
 *   <tr>
 *     <th>Field Type</th>
 *     <th>Use @XssCheck?</th>
 *     <th>Reason</th>
 *   </tr>
 *   <tr>
 *     <td>User comments</td>
 *     <td>✅ Yes</td>
 *     <td>User-generated content that will be displayed</td>
 *   </tr>
 *   <tr>
 *     <td>Profile bio</td>
 *     <td>✅ Yes</td>
 *     <td>Free-form text that could contain malicious content</td>
 *   </tr>
 *   <tr>
 *     <td>Search queries</td>
 *     <td>✅ Yes</td>
 *     <td>User input that might be echoed back</td>
 *   </tr>
 *   <tr>
 *     <td>Email address</td>
 *     <td>⚠️ Optional</td>
 *     <td>Use if email will be displayed; skip if only stored</td>
 *   </tr>
 *   <tr>
 *     <td>IDs/UUIDs</td>
 *     <td>❌ No</td>
 *     <td>Structured format, not free-form text</td>
 *   </tr>
 *   <tr>
 *     <td>Timestamps</td>
 *     <td>❌ No</td>
 *     <td>Not a string field</td>
 *   </tr>
 *   <tr>
 *     <td>Rich text (HTML editor)</td>
 *     <td>❌ No</td>
 *     <td>Intentionally contains HTML; use server-side sanitization instead</td>
 *   </tr>
 * </table>
 *
 * @author Pasindu OG
 * @version 1.3.0
 * @see io.github.og4dev.config.ApiResponseAutoConfiguration#strictJsonCustomizer()
 * @see io.github.og4dev.annotation.AutoTrim
 * @see tools.jackson.databind.ValueDeserializer#createContextual
 * @since 1.3.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface XssCheck {
}
