package io.github.og4dev.annotation;

import java.lang.annotation.*;

/**
 * Opt-in annotation to enable automatic API response wrapping for Spring REST controllers.
 * <p>
 * When this annotation is applied to a {@link org.springframework.web.bind.annotation.RestController}
 * class or a specific request mapping method, the {@link io.github.og4dev.advice.GlobalResponseWrapper}
 * intercepts the returned object and automatically encapsulates it within the standardized
 * {@link io.github.og4dev.dto.ApiResponse} format.
 * </p>
 * <h2>Usage:</h2>
 * <ul>
 * <li><b>Class Level ({@link ElementType#TYPE}):</b> Applies the wrapping behavior to <i>all</i> endpoint methods within the controller.</li>
 * <li><b>Method Level ({@link ElementType#METHOD}):</b> Applies the wrapping behavior <i>only</i> to the specific annotated method.</li>
 * </ul>
 * <h2>Example:</h2>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/users")
 * @AutoResponse // All methods in this controller will be automatically wrapped
 * public class UserController {
 * * @GetMapping("/{id}")
 * public UserDto getUser(@PathVariable Long id) {
 * // Returns: { "status": "Success", "content": { "id": 1, ... }, "timestamp": "..." }
 * return userService.findById(id);
 * }
 * * @PostMapping
 * @ResponseStatus(HttpStatus.CREATED)
 * // @AutoResponse can also be placed here for method-level granularity instead of class-level
 * public UserDto createUser(@RequestBody UserDto dto) {
 * return userService.create(dto);
 * }
 * }
 * }</pre>
 * <h2>Exclusions:</h2>
 * <p>
 * To prevent errors and double-wrapping, the interceptor will safely ignore methods that return:
 * </p>
 * <ul>
 * <li>{@code ApiResponse} or {@code ResponseEntity} (Assumes the developer has explicitly formatted the response)</li>
 * <li>{@code ProblemDetail} (RFC 9457 error responses managed by the global exception handler)</li>
 * <li>{@code String} (Bypassed to avoid {@code ClassCastException} with Spring's internal string message converters)</li>
 * </ul>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @see io.github.og4dev.advice.GlobalResponseWrapper
 * @see io.github.og4dev.dto.ApiResponse
 * @since 1.4.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoResponse {
}