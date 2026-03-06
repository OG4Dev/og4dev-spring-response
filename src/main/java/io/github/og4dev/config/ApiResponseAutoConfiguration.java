package io.github.og4dev.config;

import io.github.og4dev.advice.GlobalResponseWrapper;
import io.github.og4dev.annotation.AutoResponse;
import io.github.og4dev.annotation.AutoTrim;
import io.github.og4dev.annotation.XssCheck;
import io.github.og4dev.exception.ApiExceptionTranslator;
import io.github.og4dev.exception.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.module.SimpleModule;

import java.util.List;

/**
 * Spring Boot autoconfiguration class for the OG4Dev Spring API Response Library.
 * <p>
 * This class is loaded automatically by Spring Boot's autoconfiguration mechanism
 * when the library is on the classpath — no manual {@code @ComponentScan} or
 * {@code @Import} annotation is required. It registers all core library beans and
 * applies a Jackson customizer that enforces strict, secure JSON deserialization.
 * </p>
 *
 * <h2>Beans Registered</h2>
 * <ul>
 *   <li>{@link GlobalExceptionHandler} — Central RFC 9457 exception handler with
 *       10 built-in handlers and support for {@link ApiExceptionTranslator} beans.</li>
 *   <li>{@link GlobalResponseWrapper} — Opt-in response envelope (activated by
 *       {@link AutoResponse @AutoResponse}); conditionally skipped when the developer
 *       provides their own {@code GlobalResponseWrapper} bean.</li>
 *   <li>{@link JsonMapperBuilderCustomizer} — Applies strict property validation,
 *       case-insensitive enum handling, and the {@link AdvancedStringDeserializer}
 *       for opt-in {@link AutoTrim @AutoTrim} / {@link XssCheck @XssCheck} support.</li>
 * </ul>
 *
 * <h2>How Autoconfiguration Works</h2>
 * <p>
 * Spring Boot 3.x reads
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * and loads this class during application context startup.
 * </p>
 *
 * <h2>Disabling Autoconfiguration</h2>
 * <p>
 * Exclude this class when you need full manual control:
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
 * Or via {@code application.properties}:
 * </p>
 * <pre>
 * spring.autoconfigure.exclude=io.github.og4dev.config.ApiResponseAutoConfiguration
 * </pre>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.0.0
 * @see GlobalExceptionHandler
 * @see GlobalResponseWrapper
 * @see AdvancedStringDeserializer
 * @see org.springframework.boot.autoconfigure.AutoConfiguration
 */
@Configuration
@SuppressWarnings("unused")
public class ApiResponseAutoConfiguration {

    /**
     * Default no-arg constructor required by Spring's configuration processing.
     */
    public ApiResponseAutoConfiguration() {
        // Required by Spring's @Configuration processing
    }

    /**
     * Registers {@link GlobalExceptionHandler} as a Spring bean.
     * <p>
     * All {@link ApiExceptionTranslator} beans found in the application context are
     * injected and stored in the handler. They are consulted before the generic HTTP 500
     * fallback, allowing third-party exceptions to be mapped to meaningful RFC 9457
     * ProblemDetail responses without any additional {@code @ExceptionHandler} methods.
     * </p>
     *
     * @param translators optional list of {@link ApiExceptionTranslator} beans discovered
     *                    by Spring; {@code null} when none are registered, in which case
     *                    the handler uses an empty list
     * @return a fully configured {@link GlobalExceptionHandler} instance
     * @see ApiExceptionTranslator
     */
    @Bean
    public GlobalExceptionHandler apiResponseAdvisor(@Autowired(required = false) List<ApiExceptionTranslator<?>> translators) {
        return new GlobalExceptionHandler(translators);
    }

    /**
     * Registers {@link GlobalResponseWrapper} as a Spring bean for opt-in response
     * envelope wrapping.
     * <p>
     * When a REST controller class or method is annotated with
     * {@link AutoResponse @AutoResponse}, this wrapper intercepts the return value and
     * encapsulates it inside an {@link io.github.og4dev.dto.ApiResponse} before it is
     * serialized to JSON.
     * </p>
     * <p>
     * This bean is guarded by {@code @ConditionalOnMissingBean}: if the application
     * defines its own {@code GlobalResponseWrapper} bean, this default registration is
     * skipped entirely, giving developers full control over wrapping behaviour.
     * </p>
     *
     * <h2>Key Behaviours</h2>
     * <ul>
     *   <li><b>Zero Boilerplate</b> — No manual {@code ResponseEntity<ApiResponse<T>>}
     *       wrapping required in controllers.</li>
     *   <li><b>Status Code Preservation</b> — Reads the current HTTP status set via
     *       {@code @ResponseStatus} and reflects it in the {@code ApiResponse.status}
     *       field.</li>
     *   <li><b>Double-Wrap Prevention</b> — Skips wrapping when the return type is
     *       already {@code ApiResponse}, {@code ResponseEntity}, or {@code ProblemDetail}.
     *       </li>
     *   <li><b>String Safety</b> — Raw {@code String} returns are serialized explicitly
     *       via the injected {@code ObjectMapper} to prevent
     *       {@code ClassCastException}.</li>
     * </ul>
     *
     * @param objectMapper the Jackson {@code ObjectMapper} injected by Spring, used for
     *                     explicit {@code String} payload serialization
     * @return a fully configured {@link GlobalResponseWrapper} instance
     * @since 1.4.0
     * @see AutoResponse
     * @see io.github.og4dev.dto.ApiResponse
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalResponseWrapper globalResponseWrapper(ObjectMapper objectMapper) {
        return new GlobalResponseWrapper(objectMapper);
    }

    /**
     * Applies a {@link JsonMapperBuilderCustomizer} that configures strict and secure
     * Jackson JSON deserialization globally.
     *
     * <h2>Always-On Features</h2>
     * <ul>
     *   <li><b>Unknown property rejection</b> ({@code FAIL_ON_UNKNOWN_PROPERTIES}) —
     *       prevents mass-assignment attacks by rejecting payloads with unexpected
     *       fields.</li>
     *   <li><b>Case-insensitive enums</b> ({@code ACCEPT_CASE_INSENSITIVE_ENUMS}) —
     *       accepts enum values in any letter case for improved API usability.</li>
     * </ul>
     *
     * <h2>Opt-in Features (Annotation-Driven)</h2>
     * <ul>
     *   <li>{@link XssCheck @XssCheck} — Rejects {@code String} values containing HTML
     *       or XML tags with a 400 Bad Request error.</li>
     *   <li>{@link AutoTrim @AutoTrim} — Strips leading and trailing whitespace from
     *       {@code String} values at deserialization time.</li>
     * </ul>
     * <p>
     * When both annotations are active on the same field, trimming is applied first and
     * XSS validation is performed on the trimmed value.
     * </p>
     *
     * <h2>Null Value Handling</h2>
     * <p>
     * {@code null} values pass through unchanged regardless of which annotations are
     * present on the field.
     * </p>
     *
     * @return a {@link JsonMapperBuilderCustomizer} that registers the strict deserialization
     *         settings and the {@link AdvancedStringDeserializer}
     * @since 1.1.0
     * @see AutoTrim
     * @see XssCheck
     * @see AdvancedStringDeserializer
     */
    @Bean
    public JsonMapperBuilderCustomizer strictJsonCustomizer() {
        return builder -> {
            builder.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            builder.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

            builder.addModules(new SimpleModule().addDeserializer(String.class, new AdvancedStringDeserializer()));
        };
    }
}