package io.github.og4dev.annotation;

import java.lang.annotation.*;

/**
 * Opt-in annotation to enable automatic API response wrapping for Spring REST controllers.
 * <p>
 * When applied to a {@link org.springframework.web.bind.annotation.RestController} class or
 * to a specific request-mapping method, {@link io.github.og4dev.advice.GlobalResponseWrapper}
 * intercepts the returned object and encapsulates it within the standardized
 * {@link io.github.og4dev.dto.ApiResponse} format before it is written to the HTTP response body.
 * </p>
 * <p>
 * This eliminates the need to manually wrap every return value in
 * {@code ResponseEntity<ApiResponse<T>>}, reducing boilerplate while keeping the response
 * contract consistent across the entire API surface.
 * </p>
 *
 * <h2>Target Scopes</h2>
 * <ul>
 *   <li><b>Class Level ({@link ElementType#TYPE}):</b> Applies automatic wrapping to
 *       <i>all</i> request-mapping methods within the annotated controller.</li>
 *   <li><b>Method Level ({@link ElementType#METHOD}):</b> Applies automatic wrapping
 *       <i>only</i> to the specific annotated method, leaving others unaffected.</li>
 * </ul>
 *
 * <h2>Custom Response Message</h2>
 * <p>
 * The {@link #message()} element controls the value of the {@code message} field in the
 * produced {@link io.github.og4dev.dto.ApiResponse}. It defaults to {@code "Success"} and
 * can be overridden at class or method level:
 * </p>
 * <pre>{@code
 * @GetMapping("/{id}")
 * @AutoResponse(message = "User retrieved successfully")
 * public UserDto getUser(@PathVariable Long id) {
 *     return userService.findById(id);
 * }
 * }</pre>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/users")
 * @AutoResponse // All methods in this controller are automatically wrapped
 * public class UserController {
 *
 *     @GetMapping("/{id}")
 *     public UserDto getUser(@PathVariable Long id) {
 *         // Response: { "status": 200, "message": "Success", "content": { ... }, "timestamp": "..." }
 *         return userService.findById(id);
 *     }
 *
 *     @PostMapping
 *     @ResponseStatus(HttpStatus.CREATED)
 *     public UserDto createUser(@RequestBody UserDto dto) {
 *         // Response: { "status": 201, "message": "Success", "content": { ... }, "timestamp": "..." }
 *         return userService.create(dto);
 *     }
 * }
 * }</pre>
 *
 * <h2>Excluded Return Types</h2>
 * <p>
 * The interceptor automatically skips wrapping for the following return types to prevent
 * conflicts and double-wrapping:
 * </p>
 * <ul>
 *   <li>{@code ApiResponse} — already wrapped by the developer; wrapping again would produce
 *       {@code ApiResponse<ApiResponse<T>>}.</li>
 *   <li>{@code ResponseEntity} — the developer has explicitly configured the response;
 *       the wrapper respects that decision.</li>
 *   <li>{@code ProblemDetail} — RFC 9457 error responses produced by the global exception
 *       handler; must not be re-wrapped.</li>
 *   <li>{@code String} — handled with explicit JSON serialization via the injected
 *       {@code ObjectMapper} to avoid {@code ClassCastException} with Spring's
 *       {@code StringHttpMessageConverter}.</li>
 * </ul>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.4.0
 * @see io.github.og4dev.advice.GlobalResponseWrapper
 * @see io.github.og4dev.dto.ApiResponse
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoResponse {

    /**
     * The message to include in the {@code message} field of the produced
     * {@link io.github.og4dev.dto.ApiResponse}.
     * <p>
     * When set at class level, all methods in the controller use this message unless
     * overridden at the method level. When set at method level, it takes precedence
     * over any class-level value.
     * </p>
     *
     * @return the response message; defaults to {@code "Success"}
     */
    String message() default "Success";
}