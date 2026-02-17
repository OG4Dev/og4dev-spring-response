package io.github.og4dev.config;

import io.github.og4dev.exception.GlobalExceptionHandler;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Autoconfiguration class for the OG4Dev Spring API Response Library.
 * <p>
 * This configuration is automatically loaded by Spring Boot's autoconfiguration mechanism
 * when the library is present on the classpath. It registers essential beans required for
 * the library to function properly, including the comprehensive global exception handler.
 * </p>
 * <p>
 * <b>Zero Configuration Required:</b> Simply adding the library dependency enables all features
 * automatically. No manual {@code @ComponentScan} or {@code @Import} annotations are needed.
 * </p>
 * <h2>What Gets Auto-Configured:</h2>
 * <ul>
 *   <li>{@link GlobalExceptionHandler} - Comprehensive exception handling with RFC 9457 ProblemDetail format
 *       <ul>
 *         <li>10 built-in exception handlers covering all common error scenarios</li>
 *         <li>Automatic trace ID generation and logging</li>
 *         <li>Validation error aggregation and formatting</li>
 *         <li>Production-ready error messages</li>
 *       </ul>
 *   </li>
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
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 * }</pre>
 * <p>
 * Or in {@code application.properties}:
 * </p>
 * <pre>
 * spring.autoconfigure.exclude=io.github.og4dev.config.ApiResponseAutoConfiguration
 * </pre>
 * <p>
 * Alternatively, disable the exception handler while keeping other library features:
 * </p>
 * <pre>
 * api-response.enabled=false
 * </pre>
 *
 * @author Pasindu OG
 * @version 1.1.0
 * @see GlobalExceptionHandler
 * @see org.springframework.boot.autoconfigure.AutoConfiguration
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class ApiResponseAutoConfiguration {

    /**
     * Default constructor for ApiResponseAutoConfiguration.
     * <p>
     * This constructor is automatically invoked by Spring's dependency injection
     * container during application startup when autoconfiguration is enabled.
     * </p>
     */
    public ApiResponseAutoConfiguration() {
        // Default constructor for Spring autoconfiguration
    }

    /**
     * Registers the {@link GlobalExceptionHandler} as a Spring bean for automatic exception handling.
     * <p>
     * The handler provides comprehensive centralized exception management using Spring's
     * {@link org.springframework.web.bind.annotation.RestControllerAdvice} mechanism,
     * automatically converting 10 different types of exceptions to RFC 9457 ProblemDetail responses.
     * </p>
     * <p>
     * <b>Exception Coverage:</b>
     * </p>
     * <ul>
     *   <li>General exceptions (500 Internal Server Error)</li>
     *   <li>Validation errors with field-level details (400 Bad Request)</li>
     *   <li>Type mismatch errors (400 Bad Request)</li>
     *   <li>Malformed JSON requests (400 Bad Request)</li>
     *   <li>Missing required parameters (400 Bad Request)</li>
     *   <li>404 Not Found errors</li>
     *   <li>405 Method Not Allowed</li>
     *   <li>415 Unsupported Media Type</li>
     *   <li>Null pointer exceptions (500 Internal Server Error)</li>
     *   <li>Custom ApiException instances (custom status codes)</li>
     * </ul>
     * <p>
     * <b>Features:</b>
     * </p>
     * <ul>
     *   <li>Automatic trace ID generation for all errors</li>
     *   <li>Consistent trace IDs between logs and responses</li>
     *   <li>RFC 9457 compliant error format</li>
     *   <li>Comprehensive SLF4J logging with appropriate severity levels</li>
     *   <li>Automatic timestamp inclusion in all error responses</li>
     * </ul>
     *
     * @return A new instance of {@link GlobalExceptionHandler} registered as a Spring bean.
     */
    @Bean
    public GlobalExceptionHandler apiResponseAdvisor() {
        return new GlobalExceptionHandler();
    }

    /**
     * Configures strict JSON deserialization with automatic string validation and trimming.
     * <p>
     * This bean customizer enhances Jackson's JSON processing with three critical security
     * and data quality features that are automatically applied to all API endpoints:
     * </p>
     * <h3>1. Strict Property Validation</h3>
     * <p>
     * Enables {@link DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES} to reject JSON
     * payloads containing unexpected fields, preventing:
     * </p>
     * <ul>
     *   <li>Mass assignment vulnerabilities</li>
     *   <li>Data injection attacks</li>
     *   <li>Client-side typos that could cause silent data loss</li>
     * </ul>
     * <p>
     * <b>Example:</b> If your DTO expects {@code {name, email}}, sending
     * {@code {name, email, isAdmin}} will result in a 400 Bad Request error.
     * </p>
     *
     * <h3>2. Case-Insensitive Enum Handling</h3>
     * <p>
     * Enables {@link MapperFeature#ACCEPT_CASE_INSENSITIVE_ENUMS} for flexible enum
     * deserialization, allowing clients to send enum values in any case format
     * (e.g., "ACTIVE", "active", "Active" all map to the same enum constant).
     * This improves API usability without compromising type safety.
     * </p>
     *
     * <h3>3. Automatic HTML Tag Detection and Rejection</h3>
     * <p>
     * Registers a custom {@link StdDeserializer} that automatically:
     * </p>
     * <ul>
     *   <li><b>Trims whitespace:</b> Removes leading and trailing spaces from all string values</li>
     *   <li><b>Detects HTML tags:</b> Uses regex pattern {@code .*<\s*[a-zA-Z/!].*} to identify HTML/XML tags</li>
     *   <li><b>Rejects malicious input:</b> Throws {@link IllegalArgumentException} if HTML tags are detected</li>
     *   <li><b>Preserves nulls:</b> Null values remain null (not converted to empty strings)</li>
     * </ul>
     * <p>
     * <b>Security Benefit:</b> This provides automatic XSS prevention at the deserialization layer
     * using a fail-fast approach. Unlike HTML escaping, this prevents malicious content from ever
     * entering your system, providing stronger security guarantees.
     * </p>
     * <p>
     * <b>Example behavior:</b>
     * </p>
     * <pre>
     * Valid:   {"name": "  John Doe  "} → {"name": "John Doe"}
     * Invalid: {"name": "&lt;script&gt;alert('XSS')&lt;/script&gt;"}
     *          → Throws IllegalArgumentException: "Security Error: HTML tags or XSS payloads are not allowed in the request."
     * </pre>
     *
     * <h3>Global Application</h3>
     * <p>
     * As a {@link JsonMapperBuilderCustomizer} bean, these settings are automatically applied
     * to Spring Boot's default {@link tools.jackson.databind.ObjectMapper}, affecting:
     * </p>
     * <ul>
     *   <li>All {@code @RequestBody} deserialization in REST controllers</li>
     *   <li>All {@code @ResponseBody} serialization</li>
     *   <li>WebSocket message handling</li>
     *   <li>Any component using the autoconfigured ObjectMapper</li>
     * </ul>
     *
     * <h3>Disabling This Feature</h3>
     * <p>
     * If strict JSON validation or HTML tag rejection interferes with your use case
     * (e.g., you need to accept HTML content), you can exclude this autoconfiguration
     * or provide your own {@link JsonMapperBuilderCustomizer} bean with higher
     * precedence using {@code @Primary}.
     * </p>
     *
     * @return A {@link JsonMapperBuilderCustomizer} that configures strict JSON processing
     *         with automatic string validation, HTML tag rejection, unknown property rejection,
     *         and case-insensitive enum handling.
     * @see DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES
     * @see MapperFeature#ACCEPT_CASE_INSENSITIVE_ENUMS
     * @see JsonMapperBuilderCustomizer
     * @since 1.1.0
     */
    @Bean
    public JsonMapperBuilderCustomizer strictJsonCustomizer() {
        return builder -> {
            builder.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            builder.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
            SimpleModule stringTrimModule = new SimpleModule();
            stringTrimModule.addDeserializer(String.class, new StdDeserializer<>(String.class) {
                @Override
                public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
                    String value = p.getValueAsString();
                    if (value == null) return null;
                    if(!value.trim().matches(".*<\\s*[a-zA-Z/!].*")) return value.trim();
                    throw new IllegalArgumentException("Security Error: HTML tags or XSS payloads are not allowed in the request.");
                }
            });
            builder.addModules(stringTrimModule);
        };
    }
}
