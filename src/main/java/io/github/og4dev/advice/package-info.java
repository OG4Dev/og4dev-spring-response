/**
 * Provides global advisory components for the OG4Dev Spring API Response Library.
 * <p>
 * This package contains Spring {@link org.springframework.web.bind.annotation.RestControllerAdvice}
 * implementations that intercept and modify HTTP responses and requests globally across the application.
 * </p>
 * <p>
 * The primary component within this package is the {@link io.github.og4dev.advice.GlobalResponseWrapper}.
 * It facilitates the seamless, opt-in encapsulation of standard controller return values into the
 * standardized {@link io.github.og4dev.dto.ApiResponse} format, significantly reducing boilerplate code.
 * </p>
 * <h2>Integration &amp; Usage</h2>
 * <p>
 * Components in this package are automatically registered via Spring Boot's auto-configuration
 * mechanism ({@code ApiResponseAutoConfiguration}). Developers do not need to manually scan, import,
 * or configure this package.
 * </p>
 * <p>
 * To activate the response wrapping capabilities provided by this package, simply annotate
 * target REST controllers or specific methods with the {@link io.github.og4dev.annotation.AutoResponse @AutoResponse}
 * annotation.
 * </p>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @see io.github.og4dev.advice.GlobalResponseWrapper
 * @see io.github.og4dev.annotation.AutoResponse
 * @since 1.4.0
 */
package io.github.og4dev.advice;