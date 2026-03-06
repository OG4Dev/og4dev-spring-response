/**
 * Global response-body advisory component for automatic API response wrapping.
 * <p>
 * This package contains the {@link io.github.og4dev.advice.GlobalResponseWrapper}, a Spring
 * {@link org.springframework.web.bind.annotation.RestControllerAdvice} implementation that
 * intercepts outgoing HTTP response bodies and encapsulates them within the standardized
 * {@link io.github.og4dev.dto.ApiResponse} structure before they are serialized and sent
 * to the client.
 * </p>
 * <p>
 * The wrapper is activation-based: it only processes controllers or methods annotated
 * with {@link io.github.og4dev.annotation.AutoResponse @AutoResponse}, leaving all other
 * endpoints completely unaffected.
 * </p>
 *
 * <h2>Registration</h2>
 * <p>
 * {@link io.github.og4dev.advice.GlobalResponseWrapper} is registered automatically as a
 * Spring bean by
 * {@link io.github.og4dev.config.ApiResponseAutoConfiguration#globalResponseWrapper(tools.jackson.databind.ObjectMapper)}.
 * No manual configuration is needed.
 * </p>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.4.0
 * @see io.github.og4dev.advice.GlobalResponseWrapper
 * @see io.github.og4dev.annotation.AutoResponse
 * @see io.github.og4dev.dto.ApiResponse
 */
package io.github.og4dev.advice;