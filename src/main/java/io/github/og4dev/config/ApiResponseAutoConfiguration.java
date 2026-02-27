package io.github.og4dev.config;

import io.github.og4dev.advice.GlobalResponseWrapper;
import io.github.og4dev.annotation.AutoResponse;
import io.github.og4dev.annotation.AutoTrim;
import io.github.og4dev.annotation.XssCheck;
import io.github.og4dev.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Autoconfiguration class for the OG4Dev Spring API Response Library.
 * <p>
 * This configuration is automatically loaded by Spring Boot's autoconfiguration mechanism
 * when the library is present on the classpath. It registers essential beans required for
 * the library to function properly, including the comprehensive global exception handler
 * and the automatic response wrapper.
 * </p>
 * <p>
 * <b>Zero Configuration Required:</b> Simply adding the library dependency enables all features
 * automatically. No manual {@code @ComponentScan} or {@code @Import} annotations are needed.
 * </p>
 * <h2>What Gets Auto-Configured:</h2>
 * <ul>
 * <li>{@link GlobalExceptionHandler} - Comprehensive exception handling with RFC 9457 ProblemDetail format
 * <ul>
 * <li>10 built-in exception handlers covering all common error scenarios</li>
 * <li>Automatic trace ID generation and logging</li>
 * <li>Validation error aggregation and formatting</li>
 * <li>Production-ready error messages</li>
 * </ul>
 * </li>
 * <li>{@link GlobalResponseWrapper} - Automatic wrapping of controller responses (Opt-in via {@code @AutoResponse})</li>
 * </ul>
 * <h2>How It Works:</h2>
 * <p>
 * Spring Boot 3.x+ automatically reads {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * and loads this configuration class during application startup.
 * </p>
 * <h2>Disabling Auto-Configuration:</h2>
 * <p>
 * If you need to customize or disable this autoconfiguration, you can exclude it in your main application class:
 * </p>
 * <pre>{@code
 * @SpringBootApplication(exclude = ApiResponseAutoConfiguration.class)
 * public class Application {
 * public static void main(String[] args) {
 * SpringApplication.run(Application.class, args);
 * }
 * }
 * }</pre>
 * <p>
 * Or in {@code application.properties}:
 * </p>
 * <pre>
 * spring.autoconfigure.exclude=io.github.og4dev.config.ApiResponseAutoConfiguration
 * </pre>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @see GlobalExceptionHandler
 * @see GlobalResponseWrapper
 * @see org.springframework.boot.autoconfigure.AutoConfiguration
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class ApiResponseAutoConfiguration {

    /**
     * Default constructor for ApiResponseAutoConfiguration.
     */
    public ApiResponseAutoConfiguration() {
        // Default constructor for Spring autoconfiguration
    }

    /**
     * Registers the {@link GlobalExceptionHandler} as a Spring bean for automatic exception handling.
     * * // ... (පරණ JavaDoc එකමයි, වෙනස් වෙන්නේ නෑ) ...
     * * @return A new instance of {@link GlobalExceptionHandler} registered as a Spring bean.
     */
    @Bean
    public GlobalExceptionHandler apiResponseAdvisor() {
        return new GlobalExceptionHandler();
    }

    /**
     * Registers the {@link GlobalResponseWrapper} as a Spring bean for automatic API response wrapping.
     * <p>
     * This bean enables the opt-in {@link AutoResponse @AutoResponse} feature. When a REST controller
     * or method is annotated with {@code @AutoResponse}, this wrapper automatically intercepts the
     * outgoing payload and encapsulates it within the standardized {@code ApiResponse<T>} structure
     * before it is written to the HTTP response body.
     * </p>
     * <h2>Key Capabilities:</h2>
     * <ul>
     * <li><b>Zero Boilerplate:</b> Eliminates the need to manually return {@code ResponseEntity<ApiResponse<T>>}
     * from every controller method.</li>
     * <li><b>Status Code Preservation:</b> Intelligently reads and preserves custom HTTP status codes
     * set via {@code @ResponseStatus} (e.g., 201 Created).</li>
     * <li><b>Double-Wrap Prevention:</b> Safely skips wrapping if the controller already returns
     * an {@code ApiResponse} or {@code ResponseEntity}.</li>
     * <li><b>Error Compatibility:</b> Bypasses {@code ProblemDetail} and exception responses to maintain
     * RFC 9457 compliance managed by {@link GlobalExceptionHandler}.</li>
     * </ul>
     * <h2>Example Usage:</h2>
     * <pre>{@code
     * @RestController
     * @RequestMapping("/api/users")
     * @AutoResponse // Enables automatic wrapping for all methods in this controller
     * public class UserController {
     * * @GetMapping("/{id}")
     * public UserDto getUser(@PathVariable Long id) {
     * // Simply return the DTO. It will be sent to the client as:
     * // { "status": "Success", "content": { "id": 1, "name": "..." }, "timestamp": "..." }
     * return userService.findById(id);
     * }
     * * @PostMapping
     * @ResponseStatus(HttpStatus.CREATED)
     * public UserDto createUser(@RequestBody UserDto dto) {
     * // The 201 Created status will be preserved in the final ApiResponse
     * return userService.create(dto);
     * }
     * }
     * }</pre>
     * <p>
     * <b>Note:</b> This bean is conditionally loaded using {@code @ConditionalOnMissingBean}, allowing developers
     * to easily override the default wrapping behavior by defining their own {@code GlobalResponseWrapper} bean.
     * </p>
     * * @return A new instance of {@link GlobalResponseWrapper} registered as a Spring bean.
     * @see AutoResponse
     * @see io.github.og4dev.dto.ApiResponse
     * @since 1.4.0
     */
    @Bean
    @ConditionalOnMissingBean // මේක දැම්මොත් අනිත් අයට මේක override කරන්න පුළුවන්
    public GlobalResponseWrapper globalResponseWrapper() {
        return new GlobalResponseWrapper();
    }

    /**
     * Configures strict JSON deserialization with opt-in security features via field-level annotations.
     * <p>
     * This bean customizer enhances Jackson's JSON processing with production-ready security and data quality
     * features that can be selectively applied to specific fields using the {@link AutoTrim @AutoTrim} and
     * {@link XssCheck @XssCheck} annotations. By default, fields are NOT trimmed or XSS-validated unless
     * explicitly annotated.
     * </p>
     *
     * <h2>Overview of Features</h2>
     * <p>
     * This configuration provides four critical layers of protection and data processing:
     * </p>
     * <ol>
     *   <li><b>Strict Property Validation</b> - Prevents mass assignment attacks (automatic)</li>
     *   <li><b>Case-Insensitive Enum Handling</b> - Improves API usability (automatic)</li>
     *   <li><b>Opt-in XSS Prevention</b> - Blocks HTML/XML injection attacks (requires {@code @XssCheck})</li>
     *   <li><b>Opt-in String Trimming</b> - Removes whitespace (requires {@code @AutoTrim})</li>
     * </ol>
     *
     * <h2>Feature 1: Strict Property Validation</h2>
     * <p>
     * Enables {@link DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES} to reject JSON
     * payloads containing unexpected fields. This prevents three critical security issues:
     * </p>
     * <ul>
     *   <li><b>Mass Assignment Vulnerabilities:</b> Attackers cannot inject fields like {@code isAdmin: true}</li>
     *   <li><b>Data Injection Attacks:</b> Prevents modification of unintended database fields</li>
     *   <li><b>Client Errors:</b> Detects typos early, preventing silent data loss</li>
     * </ul>
     * <p>
     * <b>Example:</b>
     * </p>
     * <pre>{@code
     * // DTO Definition
     * public class UserDTO {
     *     private String name;
     *     private String email;
     * }
     *
     * // Valid Request
     * {"name": "John", "email": "john@example.com"}  // ✓ Success
     *
     * // Invalid Request (will be rejected with 400 Bad Request)
     * {"name": "John", "email": "john@example.com", "isAdmin": true}  // ✗ Unknown field
     * }</pre>
     *
     * <h2>Feature 2: Case-Insensitive Enum Handling</h2>
     * <p>
     * Enables {@link MapperFeature#ACCEPT_CASE_INSENSITIVE_ENUMS} for flexible enum
     * deserialization. Clients can send enum values in any case format, improving API
     * usability without compromising type safety or security.
     * </p>
     * <p>
     * <b>Example:</b>
     * </p>
     * <pre>{@code
     * public enum Status { ACTIVE, INACTIVE, PENDING }
     *
     * // All these are accepted and map to Status.ACTIVE:
     * {"status": "ACTIVE"}   // ✓ Original
     * {"status": "active"}   // ✓ Lowercase
     * {"status": "Active"}   // ✓ Title case
     * {"status": "AcTiVe"}   // ✓ Mixed case
     * }</pre>
     *
     * <h2>Feature 3: Opt-in XSS Prevention with @XssCheck</h2>
     * <p>
     * Registers a custom {@link StdScalarDeserializer} ({@code AdvancedStringDeserializer})
     * that performs automatic HTML/XML tag detection and rejection at the deserialization layer
     * for fields annotated with {@link XssCheck @XssCheck}. This provides fail-fast security
     * that prevents malicious content from ever entering your system.
     * </p>
     * <p>
     * <b>Default Behavior (No Annotation):</b> Fields are NOT validated for HTML tags
     * </p>
     * <p>
     * <b>With @XssCheck Annotation:</b> HTML tags are detected and rejected
     * </p>
     * <p>
     * <b>Detection Mechanism:</b>
     * </p>
     * <ul>
     *   <li>Uses regex pattern: {@code (?s).*<\s*[a-zA-Z/!].*}</li>
     *   <li>Detects opening tags: {@code <script>}, {@code <img>}, {@code <div>}</li>
     *   <li>Detects closing tags: {@code </div>}, {@code </script>}</li>
     *   <li>Detects special tags: {@code <!DOCTYPE>}, {@code <!--comment-->}</li>
     *   <li>Works across multiple lines (DOTALL mode enabled)</li>
     * </ul>
     * <p>
     * <b>Security Advantage:</b> Unlike HTML escaping (which transforms {@code <} to {@code &lt;}),
     * this approach rejects the request entirely. This prevents:
     * </p>
     * <ul>
     *   <li>Stored XSS attacks</li>
     *   <li>DOM-based XSS attacks</li>
     *   <li>Second-order injection vulnerabilities</li>
     *   <li>Encoding bypass attempts</li>
     * </ul>
     * <p>
     * <b>Example:</b>
     * </p>
     * <pre>{@code
     * public class CommentDTO {
     *     @XssCheck
     *     private String content;        // XSS validated
     *
     *     private String commentId;      // NOT validated (no annotation)
     * }
     *
     * // Valid Requests (for content field with @XssCheck)
     * {"content": "Hello World"}                     // ✓ Plain text
     * {"content": "Price: $100 < $200"}              // ✓ Comparison operators (no tag)
     * {"content": "2 + 2 = 4"}                       // ✓ Math expressions
     *
     * // Invalid Requests (throws IllegalArgumentException)
     * {"content": "<script>alert('XSS')</script>"}   // ✗ Script injection
     * {"content": "<img src=x onerror=alert(1)>"}    // ✗ Image XSS
     * {"content": "Hello<br>World"}                  // ✗ HTML tags
     * {"content": "<!--comment-->"}                  // ✗ HTML comments
     * {"content": "<!DOCTYPE html>"}                 // ✗ DOCTYPE declaration
     * }</pre>
     *
     * <h2>Feature 4: Opt-in String Trimming with @AutoTrim</h2>
     * <p>
     * Automatically removes leading and trailing whitespace from string fields annotated with
     * {@link AutoTrim @AutoTrim}, improving data quality and preventing common user input errors.
     * Fields without the annotation preserve their original whitespace.
     * </p>
     *
     * <h3>Default Behavior (No Annotation)</h3>
     * <p>
     * By default, string fields preserve all whitespace:
     * </p>
     * <pre>{@code
     * public class UserDTO {
     *     private String username;  // NOT trimmed (no annotation)
     *     private String email;     // NOT trimmed (no annotation)
     * }
     *
     * // Input                            → Output
     * {"username": "  john  "}            → {"username": "  john  "}
     * {"email": " test@example.com   "}   → {"email": " test@example.com   "}
     * }</pre>
     *
     * <h3>Opt-in with @AutoTrim Annotation</h3>
     * <p>
     * The {@code AdvancedStringDeserializer} implements context-aware deserialization using
     * {@link ValueDeserializer#createContextual(DeserializationContext, BeanProperty)}.
     * When it detects the {@code @AutoTrim} annotation on a field, it creates a specialized
     * deserializer instance with trimming enabled.
     * </p>
     * <p>
     * <b>Use Cases for @AutoTrim:</b>
     * </p>
     * <ul>
     *   <li><b>User Input Fields:</b> Names, emails, addresses where whitespace is unwanted</li>
     *   <li><b>Search Queries:</b> Remove accidental spaces from user inputs</li>
     *   <li><b>Usernames:</b> Ensure consistent formatting without extra spaces</li>
     *   <li><b>Reference Numbers:</b> IDs, codes that should not have whitespace</li>
     * </ul>
     * <p>
     * <b>Example:</b>
     * </p>
     * <pre>{@code
     * public class LoginDTO {
     *     @AutoTrim
     *     private String username;       // Trimmed: "  admin  " → "admin"
     *
     *     private String password;       // NOT trimmed: "  pass123  " → "  pass123  "
     * }
     *
     * public class ProductDTO {
     *     @AutoTrim
     *     private String name;           // Trimmed
     *
     *     private String description;    // NOT trimmed: preserves formatting
     * }
     * }</pre>
     *
     * <h3>Combining Both Annotations</h3>
     * <p>
     * You can use both annotations together for fields that need both trimming and XSS validation:
     * </p>
     * <pre>{@code
     * public class SecureDTO {
     *     @AutoTrim
     *     @XssCheck
     *     private String cleanInput;  // First trimmed, then XSS-validated
     * }
     *
     * // These will be rejected (after trimming):
     * {"cleanInput": "  <script>alert(1)</script>  "}  // ✗ XSS attempt blocked
     * {"cleanInput": "  <img src=x>  "}               // ✗ HTML tag blocked
     * }</pre>
     *
     * <h2>Implementation Details</h2>
     * <p>
     * This method registers an inner class {@code AdvancedStringDeserializer} that extends
     * {@link StdScalarDeserializer}{@code <String>}. The deserializer operates in different modes
     * based on field annotations:
     * </p>
     * <ul>
     *   <li><b>Default Mode:</b> No processing (preserves original value)</li>
     *   <li><b>Trim Mode:</b> {@code shouldTrim = true} when {@code @AutoTrim} is present</li>
     *   <li><b>XSS Mode:</b> {@code shouldXssCheck = true} when {@code @XssCheck} is present</li>
     *   <li><b>Combined Mode:</b> Both trimming and validation when both annotations present</li>
     * </ul>
     * <p>
     * The {@code createContextual()} method inspects each field's annotations during
     * deserialization context creation and returns an appropriately configured deserializer instance.
     * </p>
     *
     * <h2>Global Application Scope</h2>
     * <p>
     * As a {@link JsonMapperBuilderCustomizer} bean, these settings are automatically applied
     * to Spring Boot's default {@link tools.jackson.databind.ObjectMapper}, affecting:
     * </p>
     * <ul>
     *   <li>All {@code @RequestBody} deserialization in REST controllers</li>
     *   <li>All {@code @ResponseBody} serialization</li>
     *   <li>WebSocket message handling</li>
     *   <li>Spring Data REST endpoints</li>
     *   <li>Spring Cloud Feign clients</li>
     *   <li>Any component using the autoconfigured ObjectMapper</li>
     * </ul>
     *
     * <h2>Null Value Handling</h2>
     * <p>
     * Null values are preserved and never converted to empty strings:
     * </p>
     * <pre>{@code
     * {"name": null}      → name = null (not "")
     * {"name": ""}        → name = ""
     * {"name": "  "}      → name = "  " (unchanged unless @AutoTrim is used)
     * }</pre>
     *
     * <h2>Disabling These Features</h2>
     * <p>
     * If you need to disable any of these features, you have three options:
     * </p>
     * <ol>
     *   <li><b>Exclude Auto-Configuration:</b>
     *     <pre>{@code
     * @SpringBootApplication(exclude = ApiResponseAutoConfiguration.class)
     * public class Application { }
     *     }</pre>
     *   </li>
     *   <li><b>Override with @Primary Bean:</b>
     *     <pre>{@code
     * @Configuration
     * public class CustomConfig {
     *     @Bean
     *     @Primary
     *     public JsonMapperBuilderCustomizer myCustomizer() {
     *         return builder -> {
     *             // Your custom configuration
     *         };
     *     }
     * }
     *     }</pre>
     *   </li>
     *   <li><b>Use Application Properties:</b>
     *     <pre>
     * spring.autoconfigure.exclude=io.github.og4dev.config.ApiResponseAutoConfiguration
     *     </pre>
     *   </li>
     * </ol>
     *
     * <h2>Performance Considerations</h2>
     * <p>
     * The regex validation and trimming operations are highly optimized and add negligible overhead
     * (typically {@code <1ms} per request). The contextual deserializer is created once per field
     * during mapper initialization, not on every request, ensuring optimal runtime performance.
     * </p>
     *
     * @return A {@link JsonMapperBuilderCustomizer} that configures strict JSON processing
     *         with opt-in string validation via {@code @XssCheck}, opt-in trimming via {@code @AutoTrim},
     *         unknown property rejection, and case-insensitive enum handling.
     * @see DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES
     * @see MapperFeature#ACCEPT_CASE_INSENSITIVE_ENUMS
     * @see io.github.og4dev.annotation.AutoTrim
     * @see io.github.og4dev.annotation.XssCheck
     * @see JsonMapperBuilderCustomizer
     * @see StdScalarDeserializer
     * @see ValueDeserializer#createContextual(DeserializationContext, BeanProperty)
     * @since 1.1.0
     */
    @Bean
    public JsonMapperBuilderCustomizer strictJsonCustomizer() {
        return builder -> {
            builder.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            builder.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
            SimpleModule stringTrimModule = new SimpleModule();

            class AdvancedStringDeserializer extends StdScalarDeserializer<String> {
                private final boolean shouldTrim;
                private final boolean shouldXssCheck;

                public AdvancedStringDeserializer() {
                    super(String.class);
                    this.shouldTrim = false;
                    this.shouldXssCheck = false;
                }

                public AdvancedStringDeserializer(boolean shouldTrim, boolean shouldXssCheck) {
                    super(String.class);
                    this.shouldTrim = shouldTrim;
                    this.shouldXssCheck = shouldXssCheck;
                }

                @Override
                public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
                    String value = p.getValueAsString();
                    if (value == null) return null;
                    String processedValue = shouldTrim ? value.trim() : value;
                    if (shouldXssCheck && processedValue.matches("(?s).*<\\s*[a-zA-Z/!].*")) {
                        throw new IllegalArgumentException("Security Error: HTML tags or XSS payloads are not allowed in the request.");
                    }
                    return processedValue;
                }

                @Override
                public ValueDeserializer<?> createContextual(DeserializationContext ct, BeanProperty property) throws JacksonException {
                    if (property != null) {
                        boolean trim = property.getAnnotation(AutoTrim.class) != null;
                        boolean xss = property.getAnnotation(XssCheck.class) != null;
                        return new AdvancedStringDeserializer(trim, xss);
                    }
                    return this;
                }
            }
            stringTrimModule.addDeserializer(String.class, new AdvancedStringDeserializer());
            builder.addModules(stringTrimModule);
        };
    }
}
