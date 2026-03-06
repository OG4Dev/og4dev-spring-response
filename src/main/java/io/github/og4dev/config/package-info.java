/**
 * Spring Boot autoconfiguration and Jackson customization for the OG4Dev Spring API
 * Response Library.
 * <p>
 * This package contains the classes that wire the library into a Spring Boot application
 * context automatically, with zero manual configuration required from the developer.
 * </p>
 *
 * <h2>Components</h2>
 * <ul>
 *   <li>{@link io.github.og4dev.config.ApiResponseAutoConfiguration} — The
 *       {@link org.springframework.context.annotation.Configuration} class loaded by Spring
 *       Boot's autoconfiguration mechanism via
 *       {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.
 *       It registers {@link io.github.og4dev.exception.GlobalExceptionHandler},
 *       {@link io.github.og4dev.advice.GlobalResponseWrapper}, and the Jackson customizer
 *       as Spring beans.</li>
 *   <li>{@link io.github.og4dev.config.AdvancedStringDeserializer} — A contextual Jackson
 *       {@code StdScalarDeserializer} that enforces opt-in string trimming
 *       ({@link io.github.og4dev.annotation.AutoTrim @AutoTrim}) and XSS validation
 *       ({@link io.github.og4dev.annotation.XssCheck @XssCheck}) at the deserialization
 *       layer.</li>
 * </ul>
 *
 * <h2>Disabling Autoconfiguration</h2>
 * <p>
 * Exclude the configuration class from your main application if you need full manual
 * control:
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
 * @see io.github.og4dev.config.ApiResponseAutoConfiguration
 * @see io.github.og4dev.config.AdvancedStringDeserializer
 */
package io.github.og4dev.config;

