package io.github.og4dev.advice;

import io.github.og4dev.annotation.AutoResponse;
import io.github.og4dev.dto.ApiResponse;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Spring {@link ResponseBodyAdvice} implementation that automatically encapsulates REST
 * controller return values inside a standardized {@link ApiResponse} envelope.
 * <p>
 * This wrapper is activated <b>only</b> for controllers or individual methods annotated
 * with {@link AutoResponse @AutoResponse}. It intercepts the outgoing response body
 * before Jackson serializes it, wraps the payload, and sends the resulting
 * {@link ApiResponse} structure to the client — eliminating manual
 * {@code ResponseEntity<ApiResponse<T>>} boilerplate.
 * </p>
 *
 * <h2>Core Behaviours</h2>
 * <ul>
 *   <li><b>Automatic Encapsulation</b> — Raw DTOs, collections, and primitive values are
 *       placed in the {@code content} field of an {@link ApiResponse}.</li>
 *   <li><b>Status Code Preservation</b> — The HTTP status already set on the response
 *       (e.g., via {@code @ResponseStatus(HttpStatus.CREATED)}) is read and reflected
 *       in the final {@code ApiResponse.status} field.</li>
 *   <li><b>Custom Message</b> — The {@link AutoResponse#message()} value at method level
 *       takes precedence; falls back to the class-level value, then {@code "Success"}.</li>
 *   <li><b>String Payload Compatibility</b> — Raw {@code String} returns are explicitly
 *       serialized via the injected {@code ObjectMapper} and the response
 *       {@code Content-Type} is forced to {@code application/json}, preventing
 *       {@code ClassCastException} with Spring's {@code StringHttpMessageConverter}.</li>
 *   <li><b>Double-Wrap Prevention</b> — Already-formatted return types ({@link ApiResponse},
 *       {@link ResponseEntity}, {@link ProblemDetail}) are skipped entirely.</li>
 * </ul>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.4.0
 * @see AutoResponse
 * @see ApiResponse
 * @see org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
 */
@RestControllerAdvice
@SuppressWarnings("unused")
@NullMarked
public class GlobalResponseWrapper implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    /**
     * Constructs a {@code GlobalResponseWrapper} with the Jackson {@code ObjectMapper}
     * used for explicit {@code String} payload serialization.
     *
     * @param objectMapper the configured Jackson mapper injected by Spring; must not be {@code null}
     */
    public GlobalResponseWrapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Determines whether this advice should process the current response body.
     * <p>
     * Returns {@code true} only when <b>both</b> of the following conditions are met:
     * </p>
     * <ol>
     *   <li>The controller class or the specific handler method is annotated with
     *       {@link AutoResponse}.</li>
     *   <li>The method's return type is <b>not</b> one of the explicitly excluded types:
     *       {@link ApiResponse}, {@link ResponseEntity}, or {@link ProblemDetail}.</li>
     * </ol>
     *
     * @param returnType    the return type descriptor of the handler method
     * @param converterType the message converter selected by Spring MVC
     * @return {@code true} if the response body should be wrapped; {@code false} otherwise
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> type = returnType.getParameterType();
        boolean isExcludedType = ApiResponse.class.isAssignableFrom(type) ||
                ResponseEntity.class.isAssignableFrom(type) ||
                ProblemDetail.class.isAssignableFrom(type);
        if (isExcludedType) return false;
        boolean hasClassAnnotation = returnType.getDeclaringClass().isAnnotationPresent(AutoResponse.class);
        boolean hasMethodAnnotation = returnType.hasMethodAnnotation(AutoResponse.class);
        return hasClassAnnotation || hasMethodAnnotation;
    }

    /**
     * Intercepts the outgoing response body and wraps it inside an {@link ApiResponse}.
     * <p>
     * The HTTP status code already set on the servlet response is read and encoded in the
     * {@code ApiResponse.status} field. For 2xx status codes the
     * {@link AutoResponse#message()} value (method-level first, then class-level) is used
     * as the message; for all other codes the message is {@code "Processed"}.
     * </p>
     * <p>
     * <b>Raw {@code String} handling:</b> if the original body is a {@code String}, the
     * resulting {@link ApiResponse} is serialized to a JSON string immediately using the
     * injected {@code ObjectMapper} and the response {@code Content-Type} header is set to
     * {@code application/json}. This prevents Spring from routing the value through the
     * {@code StringHttpMessageConverter}, which would cause a {@code ClassCastException}.
     * If serialization fails, the original string is returned unchanged as a fallback.
     * </p>
     *
     * @param body                  the value returned by the handler method; may be {@code null}
     * @param returnType            the return type descriptor of the handler method
     * @param selectedContentType   the content type selected by content negotiation
     * @param selectedConverterType the message converter selected by Spring MVC
     * @param request               the current server-side HTTP request
     * @param response              the current server-side HTTP response
     * @return the wrapped {@link ApiResponse} object, or a pre-serialized JSON
     *         {@code String} when the original body was a raw string
     */
    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        int statusCode = HttpStatus.OK.value();

        if (response instanceof ServletServerHttpResponse serverHttpResponse) {
            statusCode = serverHttpResponse.getServletResponse().getStatus();
        }
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);

        String responseMessage = "Success";
        AutoResponse methodAnnotation = returnType.getMethodAnnotation(AutoResponse.class);
        if (methodAnnotation != null) {
            responseMessage = methodAnnotation.message();
        } else {
            AutoResponse classAnnotation = returnType.getDeclaringClass().getAnnotation(AutoResponse.class);
            if (classAnnotation != null) responseMessage = classAnnotation.message();
        }
        ApiResponse<Object> apiResponse = ApiResponse.status(httpStatus.is2xxSuccessful() ? responseMessage : "Processed", body, httpStatus).getBody();

        if (body instanceof String) {
            try {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                assert apiResponse != null;
                return objectMapper.writeValueAsString(apiResponse);
            } catch (JacksonException e) {
                return body;
            }
        }
        return apiResponse;
    }
}