package io.github.og4dev.config;

import io.github.og4dev.advice.GlobalResponseWrapper;
import io.github.og4dev.annotation.AutoResponse;
import io.github.og4dev.annotation.AutoTrim;
import io.github.og4dev.annotation.XssCheck;
import io.github.og4dev.exception.ApiExceptionRegistry;
import io.github.og4dev.exception.GlobalExceptionHandler;
import io.github.og4dev.exception.XssValidationException;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @version 1.5.0-RC1
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
     * <p>
     * The handler provides comprehensive centralized exception management using Spring's
     * {@link org.springframework.web.bind.annotation.RestControllerAdvice} mechanism,
     * automatically converting various exceptions to RFC 9457 ProblemDetail responses.
     * </p>
     *
     * @param registry optional registry used to map external exception types
     * @return A new instance of {@link GlobalExceptionHandler} registered as a Spring bean.
     */
    @Bean
    public GlobalExceptionHandler apiResponseAdvisor(@Autowired(required = false) ApiExceptionRegistry registry) {
        return new GlobalExceptionHandler(registry);
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
     * <li><b>String Payload Support:</b> Safely intercepts and serializes raw {@code String} returns
     * using the injected {@link ObjectMapper} to prevent {@code ClassCastException} with Spring's
     * native message converters.</li>
     * <li><b>Error Compatibility:</b> Bypasses {@code ProblemDetail} and exception responses to maintain
     * RFC 9457 compliance managed by {@link GlobalExceptionHandler}.</li>
     * </ul>
     * <h2>Example Usage:</h2>
     * <pre>{@code
     * @RestController
     * @RequestMapping("/api/users")
     * @AutoResponse // Enables automatic wrapping for all methods in this controller
     * public class UserController {
     * @GetMapping("/{id}")
     * public UserDto getUser(@PathVariable Long id) {
     * // Simply return the DTO. It will be sent to the client as:
     * // { "status": "Success", "content": { "id": 1, "name": "..." }, "timestamp": "..." }
     * return userService.findById(id);
     * }
     * @PostMapping
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
     *
     * @param objectMapper The Jackson object mapper injected by Spring, used by the wrapper for explicit string serialization.
     * @return A new instance of {@link GlobalResponseWrapper} registered as a Spring bean.
     * @see AutoResponse
     * @see io.github.og4dev.dto.ApiResponse
     * @since 1.4.0
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalResponseWrapper globalResponseWrapper(ObjectMapper objectMapper) {
        return new GlobalResponseWrapper(objectMapper);
    }

    /**
     * Configures strict JSON deserialization with opt-in security features via field-level and class-level annotations.
     * <p>
     * This bean customizer enhances Jackson's JSON processing with production-ready security and data quality
     * features that can be selectively applied to specific fields or entire classes using the
     * {@link AutoTrim @AutoTrim} and {@link XssCheck @XssCheck} annotations. By default, fields are NOT
     * trimmed or XSS-validated unless explicitly annotated.
     * </p>
     *
     * @return A {@link JsonMapperBuilderCustomizer} that configures strict JSON processing.
     * @see DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES
     * @see MapperFeature#ACCEPT_CASE_INSENSITIVE_ENUMS
     * @see io.github.og4dev.annotation.AutoTrim
     * @see io.github.og4dev.annotation.XssCheck
     * @since 1.1.0
     */
    @Bean
    public JsonMapperBuilderCustomizer strictJsonCustomizer() {
        return builder -> {
            builder.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            builder.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

            SimpleModule stringTrimModule = new SimpleModule();
            stringTrimModule.addDeserializer(String.class, new AdvancedStringDeserializer());
            builder.addModules(stringTrimModule);
        };
    }

    /**
     * A specialized deserializer that applies opt-in string trimming and XSS validation.
     * Extracted as a private static class to reduce cognitive complexity.
     */
    private static class AdvancedStringDeserializer extends StdScalarDeserializer<String> {
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
        public String deserialize(JsonParser p, DeserializationContext text) throws JacksonException {
            String value = p.getValueAsString();
            if (value == null) {
                return null;
            }

            String processedValue = shouldTrim ? value.trim() : value;

            if (shouldXssCheck && processedValue.matches("(?s).*<\\s*[a-zA-Z/!].*")) {
                throw new XssValidationException("Security Error: HTML tags or XSS payloads are not allowed in the request.");
            }

            return processedValue;
        }

        @Override
        public ValueDeserializer<?> createContextual(DeserializationContext ct, BeanProperty property) throws JacksonException {
            if (property == null) {
                return this;
            }

            boolean trim = property.getAnnotation(AutoTrim.class) != null;
            boolean xss = property.getAnnotation(XssCheck.class) != null;
            if (trim && xss) {
                return new AdvancedStringDeserializer(true, true);
            }
            if (property.getMember() != null) {
                Class<?> declaringClass = property.getMember().getDeclaringClass();
                if (declaringClass != null) {
                    trim = trim || declaringClass.getAnnotation(AutoTrim.class) != null;
                    xss = xss || declaringClass.getAnnotation(XssCheck.class) != null;
                }
            }
            return new AdvancedStringDeserializer(trim, xss);
        }
    }
}