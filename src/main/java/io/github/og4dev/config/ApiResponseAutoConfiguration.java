package io.github.og4dev.config;

import io.github.og4dev.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
 * @version 1.0.0
 * @since 1.0.0
 * @see GlobalExceptionHandler
 * @see org.springframework.boot.autoconfigure.AutoConfiguration
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
}
