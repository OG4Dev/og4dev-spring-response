package io.github.og4dev.advice;

import io.github.og4dev.annotation.AutoResponse;
import io.github.og4dev.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link GlobalResponseWrapper}, covering:
 * <ul>
 *   <li>{@code supports()} — annotation detection and type exclusion</li>
 *   <li>{@code beforeBodyWrite()} — message resolution from method/class annotation and default</li>
 *   <li>Non-2xx status code produces "Processed" message</li>
 *   <li>String body serialization path</li>
 * </ul>
 * Changes introduced in v1.5.0-RC1: the {@link AutoResponse#message()} attribute is now
 * resolved (method-level first, then class-level, then {@code "Success"} default).
 */
@SuppressWarnings("unused")
class GlobalResponseWrapperTest {

    // -----------------------------------------------------------------------
    // Inner test controller stubs used for MethodParameter construction
    // -----------------------------------------------------------------------

    /** No @AutoResponse on class; individual methods may have it. */
    static class NoAnnotationController {
        public String noAnnotationMethod() { return "hello"; }
        @AutoResponse
        public String methodAnnotatedDefault() { return "hello"; }
        @AutoResponse(message = "Method message")
        public String methodAnnotatedCustom() { return "hello"; }
        public ApiResponse<String> returnsApiResponse() { return null; }
        public ResponseEntity<String> returnsResponseEntity() { return null; }
        public ProblemDetail returnsProblemDetail() { return null; }
    }

    @AutoResponse
    static class ClassAnnotatedController {
        public String defaultMethod() { return "hello"; }
        @AutoResponse(message = "Method overrides class")
        public String methodOverridesClass() { return "hello"; }
    }

    @AutoResponse(message = "Class custom message")
    static class ClassAnnotatedWithCustomMessageController {
        public String defaultMethod() { return "hello"; }
        @AutoResponse(message = "Method custom message")
        public String methodOverridesClass() { return "hello"; }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private GlobalResponseWrapper wrapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        wrapper = new GlobalResponseWrapper(objectMapper);
    }

    private MethodParameter returnTypeOf(Class<?> clazz, String methodName) throws Exception {
        Method method = clazz.getDeclaredMethod(methodName);
        return new MethodParameter(method, -1); // -1 = return type
    }

    private ServerHttpResponse mockResponse(int status) {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        servletResponse.setStatus(status);
        return new ServletServerHttpResponse(servletResponse);
    }

    // -----------------------------------------------------------------------
    // supports() — annotation presence
    // -----------------------------------------------------------------------

    @Test
    void supports_returnsFalse_whenNoAnnotationOnClassOrMethod() throws Exception {
        MethodParameter returnType = returnTypeOf(NoAnnotationController.class, "noAnnotationMethod");
        assertThat(wrapper.supports(returnType, null)).isFalse();
    }

    @Test
    void supports_returnsTrue_whenMethodAnnotatedWithAutoResponse() throws Exception {
        MethodParameter returnType = returnTypeOf(NoAnnotationController.class, "methodAnnotatedDefault");
        assertThat(wrapper.supports(returnType, null)).isTrue();
    }

    @Test
    void supports_returnsTrue_whenClassAnnotatedWithAutoResponse() throws Exception {
        MethodParameter returnType = returnTypeOf(ClassAnnotatedController.class, "defaultMethod");
        assertThat(wrapper.supports(returnType, null)).isTrue();
    }

    @Test
    void supports_returnsTrue_whenBothClassAndMethodAnnotated() throws Exception {
        MethodParameter returnType = returnTypeOf(ClassAnnotatedController.class, "methodOverridesClass");
        assertThat(wrapper.supports(returnType, null)).isTrue();
    }

    // -----------------------------------------------------------------------
    // supports() — excluded return types
    // -----------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"returnsApiResponse", "returnsResponseEntity", "returnsProblemDetail"})
    void supports_returnsFalse_forExcludedReturnTypes(String methodName) throws Exception {
        MethodParameter returnType = returnTypeOf(NoAnnotationController.class, methodName);
        assertThat(wrapper.supports(returnType, null)).isFalse();
    }

    // -----------------------------------------------------------------------
    // beforeBodyWrite() — message resolution
    // -----------------------------------------------------------------------

    @Test
    void beforeBodyWrite_usesDefaultSuccessMessage_whenAnnotationHasNoCustomMessage() throws Exception {
        MethodParameter returnType = returnTypeOf(NoAnnotationController.class, "methodAnnotatedDefault");
        ServerHttpResponse response = mockResponse(200);

        Object result = wrapper.beforeBodyWrite(
                "data", returnType, MediaType.APPLICATION_JSON, null,
                mock(ServerHttpRequest.class), response);

        // String body triggers JSON serialization path; result is a JSON string
        assertThat(result).isInstanceOf(String.class);
        assertThat(result.toString()).contains("\"message\":\"Success\"");
    }

    @Test
    void beforeBodyWrite_usesMethodLevelMessage_whenMethodAnnotationHasCustomMessage() throws Exception {
        MethodParameter returnType = returnTypeOf(NoAnnotationController.class, "methodAnnotatedCustom");
        ServerHttpResponse response = mockResponse(200);

        Object result = wrapper.beforeBodyWrite(
                "data", returnType, MediaType.APPLICATION_JSON, null,
                mock(ServerHttpRequest.class), response);

        assertThat(result).isInstanceOf(String.class);
        assertThat(result.toString()).contains("\"message\":\"Method message\"");
    }

    @Test
    void beforeBodyWrite_usesClassLevelMessage_whenNoMethodAnnotation() throws Exception {
        MethodParameter returnType = returnTypeOf(
                ClassAnnotatedWithCustomMessageController.class, "defaultMethod");
        ServerHttpResponse response = mockResponse(200);

        // Body is a non-String object so result is ApiResponse directly
        Object body = new Object();
        Object result = wrapper.beforeBodyWrite(
                body, returnType, MediaType.APPLICATION_JSON, null,
                mock(ServerHttpRequest.class), response);

        assertThat(result).isInstanceOf(ApiResponse.class);
        @SuppressWarnings("unchecked")
        ApiResponse<Object> apiResponse = (ApiResponse<Object>) result;
        assertThat(apiResponse.getMessage()).isEqualTo("Class custom message");
    }

    @Test
    void beforeBodyWrite_methodAnnotationOverridesClassAnnotation() throws Exception {
        MethodParameter returnType = returnTypeOf(
                ClassAnnotatedWithCustomMessageController.class, "methodOverridesClass");
        ServerHttpResponse response = mockResponse(200);

        Object body = new Object();
        Object result = wrapper.beforeBodyWrite(
                body, returnType, MediaType.APPLICATION_JSON, null,
                mock(ServerHttpRequest.class), response);

        assertThat(result).isInstanceOf(ApiResponse.class);
        @SuppressWarnings("unchecked")
        ApiResponse<Object> apiResponse = (ApiResponse<Object>) result;
        assertThat(apiResponse.getMessage()).isEqualTo("Method custom message");
    }

    @Test
    void beforeBodyWrite_usesDefaultSuccessMessage_whenClassAnnotationHasNoCustomMessage() throws Exception {
        MethodParameter returnType = returnTypeOf(ClassAnnotatedController.class, "defaultMethod");
        ServerHttpResponse response = mockResponse(200);

        Object body = new Object();
        Object result = wrapper.beforeBodyWrite(
                body, returnType, MediaType.APPLICATION_JSON, null,
                mock(ServerHttpRequest.class), response);

        assertThat(result).isInstanceOf(ApiResponse.class);
        @SuppressWarnings("unchecked")
        ApiResponse<Object> apiResponse = (ApiResponse<Object>) result;
        assertThat(apiResponse.getMessage()).isEqualTo("Success");
    }

    // -----------------------------------------------------------------------
    // beforeBodyWrite() — HTTP status handling
    // -----------------------------------------------------------------------

    @Test
    void beforeBodyWrite_usesProcessedMessage_forNon2xxStatus() throws Exception {
        MethodParameter returnType = returnTypeOf(NoAnnotationController.class, "methodAnnotatedDefault");
        ServerHttpResponse response = mockResponse(302);

        Object body = new Object();
        Object result = wrapper.beforeBodyWrite(
                body, returnType, MediaType.APPLICATION_JSON, null,
                mock(ServerHttpRequest.class), response);

        assertThat(result).isInstanceOf(ApiResponse.class);
        @SuppressWarnings("unchecked")
        ApiResponse<Object> apiResponse = (ApiResponse<Object>) result;
        assertThat(apiResponse.getMessage()).isEqualTo("Processed");
    }

    @Test
    void beforeBodyWrite_preservesStatusCode_inApiResponse() throws Exception {
        MethodParameter returnType = returnTypeOf(ClassAnnotatedController.class, "defaultMethod");
        ServerHttpResponse response = mockResponse(201);

        Object body = new Object();
        Object result = wrapper.beforeBodyWrite(
                body, returnType, MediaType.APPLICATION_JSON, null,
                mock(ServerHttpRequest.class), response);

        assertThat(result).isInstanceOf(ApiResponse.class);
        @SuppressWarnings("unchecked")
        ApiResponse<Object> apiResponse = (ApiResponse<Object>) result;
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    void beforeBodyWrite_defaults200Status_whenResponseIsNotServletServerHttpResponse() throws Exception {
        MethodParameter returnType = returnTypeOf(ClassAnnotatedController.class, "defaultMethod");
        // Use a non-Servlet ServerHttpResponse mock (not instanceof ServletServerHttpResponse)
        ServerHttpResponse mockedResponse = mock(ServerHttpResponse.class);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        org.mockito.Mockito.when(mockedResponse.getHeaders()).thenReturn(headers);

        Object body = new Object();
        Object result = wrapper.beforeBodyWrite(
                body, returnType, MediaType.APPLICATION_JSON, null,
                mock(ServerHttpRequest.class), mockedResponse);

        assertThat(result).isInstanceOf(ApiResponse.class);
        @SuppressWarnings("unchecked")
        ApiResponse<Object> apiResponse = (ApiResponse<Object>) result;
        assertThat(apiResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    // -----------------------------------------------------------------------
    // beforeBodyWrite() — null body
    // -----------------------------------------------------------------------

    @Test
    void beforeBodyWrite_wrapsNullBody_inApiResponse() throws Exception {
        MethodParameter returnType = returnTypeOf(ClassAnnotatedController.class, "defaultMethod");
        ServerHttpResponse response = mockResponse(200);

        Object result = wrapper.beforeBodyWrite(
                null, returnType, MediaType.APPLICATION_JSON, null,
                mock(ServerHttpRequest.class), response);

        assertThat(result).isInstanceOf(ApiResponse.class);
        @SuppressWarnings("unchecked")
        ApiResponse<Object> apiResponse = (ApiResponse<Object>) result;
        assertThat(apiResponse.getContent()).isNull();
    }

    // -----------------------------------------------------------------------
    // beforeBodyWrite() — String payload serialization
    // -----------------------------------------------------------------------

    @Test
    void beforeBodyWrite_returnsJsonString_whenBodyIsString() throws Exception {
        MethodParameter returnType = returnTypeOf(NoAnnotationController.class, "methodAnnotatedDefault");
        ServerHttpResponse response = mockResponse(200);

        Object result = wrapper.beforeBodyWrite(
                "Hello World", returnType, MediaType.APPLICATION_JSON, null,
                mock(ServerHttpRequest.class), response);

        assertThat(result).isInstanceOf(String.class);
        String jsonResult = (String) result;
        // Should be a valid JSON string containing the ApiResponse structure
        assertThat(jsonResult).contains(
                "\"message\"",
                "\"status\"",
                "Hello World"
        );
    }

    @Test
    void beforeBodyWrite_setsContentTypeToJson_whenBodyIsString() throws Exception {
        MethodParameter returnType = returnTypeOf(NoAnnotationController.class, "methodAnnotatedDefault");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        servletResponse.setStatus(200);
        ServletServerHttpResponse response = new ServletServerHttpResponse(servletResponse);

        wrapper.beforeBodyWrite(
                "Hello World", returnType, MediaType.APPLICATION_JSON, null,
                mock(ServerHttpRequest.class), response);

        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    // -----------------------------------------------------------------------
    // beforeBodyWrite() — body content is propagated
    // -----------------------------------------------------------------------

    @Test
    void beforeBodyWrite_placesBodyAsContent_inApiResponse() throws Exception {
        MethodParameter returnType = returnTypeOf(ClassAnnotatedController.class, "defaultMethod");
        ServerHttpResponse response = mockResponse(200);
        String body = "test-content";

        // String body goes through JSON serialization, check via JSON parsing
        Object result = wrapper.beforeBodyWrite(
                body, returnType, MediaType.APPLICATION_JSON, null,
                mock(ServerHttpRequest.class), response);

        assertThat(result).isInstanceOf(String.class);
        assertThat(result.toString()).contains("test-content");
    }

    @Test
    void beforeBodyWrite_preservesTimestamp_inApiResponse() throws Exception {
        MethodParameter returnType = returnTypeOf(ClassAnnotatedController.class, "defaultMethod");
        ServerHttpResponse response = mockResponse(200);

        Object body = new Object();
        Object result = wrapper.beforeBodyWrite(
                body, returnType, MediaType.APPLICATION_JSON, null,
                mock(ServerHttpRequest.class), response);

        assertThat(result).isInstanceOf(ApiResponse.class);
        @SuppressWarnings("unchecked")
        ApiResponse<Object> apiResponse = (ApiResponse<Object>) result;
        assertThat(apiResponse.getTimestamp()).isNotNull();
    }
}