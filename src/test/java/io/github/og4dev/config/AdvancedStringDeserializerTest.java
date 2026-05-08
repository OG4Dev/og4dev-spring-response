package io.github.og4dev.config;

import io.github.og4dev.annotation.AutoTrim;
import io.github.og4dev.annotation.XssCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the {@code AdvancedStringDeserializer} (extracted as a private static class in v1.5.0)
 * inside {@link ApiResponseAutoConfiguration}.
 * <p>
 * The deserializer is tested indirectly through the Jackson ObjectMapper configured by
 * {@link ApiResponseAutoConfiguration#strictJsonCustomizer()}.
 * </p>
 */
class AdvancedStringDeserializerTest {

    // -----------------------------------------------------------------------
    // DTOs used to exercise field-level and class-level annotations
    // -----------------------------------------------------------------------

    static class PlainDto {
        public String value;
    }

    static class AutoTrimFieldDto {
        @AutoTrim
        public String value;
    }

    static class XssCheckFieldDto {
        @XssCheck
        public String value;
    }

    static class BothAnnotationsFieldDto {
        @AutoTrim
        @XssCheck
        public String value;
    }

    @AutoTrim
    static class AutoTrimClassDto {
        public String value;
        public String other;
    }

    @XssCheck
    static class XssCheckClassDto {
        public String value;
    }

    @AutoTrim
    @XssCheck
    static class BothAnnotationsClassDto {
        public String value;
    }

    /** Class-level @AutoTrim; field-level @XssCheck — combined coverage. */
    @AutoTrim
    static class ClassTrimFieldXssDto {
        @XssCheck
        public String secured;
        public String plain;
    }

    /** Class-level @XssCheck; field-level @AutoTrim — inverse combination. */
    @XssCheck
    static class ClassXssFieldTrimDto {
        @AutoTrim
        public String trimmed;
        public String plain;
    }

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ApiResponseAutoConfiguration config = new ApiResponseAutoConfiguration();
        JsonMapperBuilderCustomizer customizer = config.strictJsonCustomizer();
        JsonMapper.Builder builder = JsonMapper.builder();
        customizer.customize(builder);
        objectMapper = builder.build();
    }

    // -----------------------------------------------------------------------
    // Default mode — no annotations on field or class
    // -----------------------------------------------------------------------

    @Test
    void plainField_preservesOriginalStringValue() throws Exception {
        String json = "{\"value\": \"  hello  \"}";
        PlainDto result = objectMapper.readValue(json, PlainDto.class);
        // No trimming should occur without @AutoTrim
        assertThat(result.value).isEqualTo("  hello  ");
    }

    @Test
    void plainField_allowsHtmlContent() throws Exception {
        String json = "{\"value\": \"<b>bold</b>\"}";
        PlainDto result = objectMapper.readValue(json, PlainDto.class);
        // No XSS check without @XssCheck
        assertThat(result.value).isEqualTo("<b>bold</b>");
    }

    @Test
    void plainField_handlesNullValue() throws Exception {
        String json = "{\"value\": null}";
        PlainDto result = objectMapper.readValue(json, PlainDto.class);
        assertThat(result.value).isNull();
    }

    // -----------------------------------------------------------------------
    // @AutoTrim on field
    // -----------------------------------------------------------------------

    @Test
    void autoTrimField_trimsLeadingAndTrailingWhitespace() throws Exception {
        String json = "{\"value\": \"  john_doe  \"}";
        AutoTrimFieldDto result = objectMapper.readValue(json, AutoTrimFieldDto.class);
        assertThat(result.value).isEqualTo("john_doe");
    }

    @Test
    void autoTrimField_trimsOnlyWhitespace_leavingContentIntact() throws Exception {
        String json = "{\"value\": \"  hello world  \"}";
        AutoTrimFieldDto result = objectMapper.readValue(json, AutoTrimFieldDto.class);
        assertThat(result.value).isEqualTo("hello world");
    }

    @Test
    void autoTrimField_handlesNullValue() throws Exception {
        String json = "{\"value\": null}";
        AutoTrimFieldDto result = objectMapper.readValue(json, AutoTrimFieldDto.class);
        assertThat(result.value).isNull();
    }

    @Test
    void autoTrimField_preservesValueWithNoWhitespace() throws Exception {
        String json = "{\"value\": \"exact\"}";
        AutoTrimFieldDto result = objectMapper.readValue(json, AutoTrimFieldDto.class);
        assertThat(result.value).isEqualTo("exact");
    }

    // -----------------------------------------------------------------------
    // @XssCheck on field
    // -----------------------------------------------------------------------

    @Test
    void xssCheckField_allowsCleanInput() throws Exception {
        String json = "{\"value\": \"clean input\"}";
        XssCheckFieldDto result = objectMapper.readValue(json, XssCheckFieldDto.class);
        assertThat(result.value).isEqualTo("clean input");
    }

    @Test
    void xssCheckField_rejectsScriptTag() {
        String json = "{\"value\": \"<script>alert(1)</script>\"}";
        assertThatThrownBy(() -> objectMapper.readValue(json, XssCheckFieldDto.class))
                .isInstanceOf(Exception.class);
    }

    @Test
    void xssCheckField_rejectsHtmlTag() {
        String json = "{\"value\": \"<b>bold</b>\"}";
        assertThatThrownBy(() -> objectMapper.readValue(json, XssCheckFieldDto.class))
                .isInstanceOf(Exception.class);
    }

    @Test
    void xssCheckField_rejectsHtmlComment() {
        String json = "{\"value\": \"<!-- comment -->\"}";
        assertThatThrownBy(() -> objectMapper.readValue(json, XssCheckFieldDto.class))
                .isInstanceOf(Exception.class);
    }

    @Test
    void xssCheckField_allowsAngleBracketInMath() throws Exception {
        // A lone '<' not followed by a letter, '/', or '!' does not match the pattern
        String json = "{\"value\": \"1 < 2\"}";
        XssCheckFieldDto result = objectMapper.readValue(json, XssCheckFieldDto.class);
        assertThat(result.value).isEqualTo("1 < 2");
    }

    @Test
    void xssCheckField_handlesNullValue() throws Exception {
        String json = "{\"value\": null}";
        XssCheckFieldDto result = objectMapper.readValue(json, XssCheckFieldDto.class);
        assertThat(result.value).isNull();
    }

    // -----------------------------------------------------------------------
    // @AutoTrim + @XssCheck on same field
    // -----------------------------------------------------------------------

    @Test
    void bothAnnotationsField_trimsAndAllowsCleanInput() throws Exception {
        String json = "{\"value\": \"  clean  \"}";
        BothAnnotationsFieldDto result = objectMapper.readValue(json, BothAnnotationsFieldDto.class);
        assertThat(result.value).isEqualTo("clean");
    }

    @Test
    void bothAnnotationsField_rejectsXssEvenAfterTrim() {
        String json = "{\"value\": \"  <script>alert()</script>  \"}";
        assertThatThrownBy(() -> objectMapper.readValue(json, BothAnnotationsFieldDto.class))
                .isInstanceOf(Exception.class);
    }

    // -----------------------------------------------------------------------
    // @AutoTrim at class level
    // -----------------------------------------------------------------------

    @Test
    void autoTrimClass_trimsAllStringFields() throws Exception {
        String json = "{\"value\": \"  hello  \", \"other\": \"  world  \"}";
        AutoTrimClassDto result = objectMapper.readValue(json, AutoTrimClassDto.class);
        assertThat(result.value).isEqualTo("hello");
        assertThat(result.other).isEqualTo("world");
    }

    @Test
    void autoTrimClass_handlesNullFields() throws Exception {
        String json = "{\"value\": null, \"other\": null}";
        AutoTrimClassDto result = objectMapper.readValue(json, AutoTrimClassDto.class);
        assertThat(result.value).isNull();
        assertThat(result.other).isNull();
    }

    // -----------------------------------------------------------------------
    // @XssCheck at class level
    // -----------------------------------------------------------------------

    @Test
    void xssCheckClass_allowsCleanInput() throws Exception {
        String json = "{\"value\": \"safe input\"}";
        XssCheckClassDto result = objectMapper.readValue(json, XssCheckClassDto.class);
        assertThat(result.value).isEqualTo("safe input");
    }

    @Test
    void xssCheckClass_rejectsHtmlOnAnyField() {
        String json = "{\"value\": \"<img src=x onerror=alert(1)>\"}";
        assertThatThrownBy(() -> objectMapper.readValue(json, XssCheckClassDto.class))
                .isInstanceOf(Exception.class);
    }

    // -----------------------------------------------------------------------
    // @AutoTrim + @XssCheck at class level
    // -----------------------------------------------------------------------

    @Test
    void bothAnnotationsClass_trimsCleanInput() throws Exception {
        String json = "{\"value\": \"  safe  \"}";
        BothAnnotationsClassDto result = objectMapper.readValue(json, BothAnnotationsClassDto.class);
        assertThat(result.value).isEqualTo("safe");
    }

    @Test
    void bothAnnotationsClass_rejectsHtmlContent() {
        String json = "{\"value\": \"<p>text</p>\"}";
        assertThatThrownBy(() -> objectMapper.readValue(json, BothAnnotationsClassDto.class))
                .isInstanceOf(Exception.class);
    }

    // -----------------------------------------------------------------------
    // Combined: class-level @AutoTrim + field-level @XssCheck
    // -----------------------------------------------------------------------

    @Test
    void classTrimFieldXss_trimsAllFieldsAndChecksXssOnSecuredField() throws Exception {
        String json = "{\"secured\": \"  clean  \", \"plain\": \"  padded  \"}";
        ClassTrimFieldXssDto result = objectMapper.readValue(json, ClassTrimFieldXssDto.class);
        // Both trimmed (class-level @AutoTrim), and secured field has XSS check
        assertThat(result.secured).isEqualTo("clean");
        assertThat(result.plain).isEqualTo("padded");
    }

    @Test
    void classTrimFieldXss_rejectsXssOnSecuredField() {
        String json = "{\"secured\": \"<script>\", \"plain\": \"safe\"}";
        assertThatThrownBy(() -> objectMapper.readValue(json, ClassTrimFieldXssDto.class))
                .isInstanceOf(Exception.class);
    }

    @Test
    void classTrimFieldXss_allowsHtmlOnPlainField_sinceNoXssCheck() throws Exception {
        // The 'plain' field only has class-level @AutoTrim, no @XssCheck
        // So HTML should pass through (class doesn't have @XssCheck)
        String json = "{\"secured\": \"clean\", \"plain\": \"safe text\"}";
        ClassTrimFieldXssDto result = objectMapper.readValue(json, ClassTrimFieldXssDto.class);
        assertThat(result.plain).isEqualTo("safe text");
    }

    // -----------------------------------------------------------------------
    // Regex boundary cases for XSS pattern
    // -----------------------------------------------------------------------

    @Test
    void xssCheck_rejectsClosingTag() {
        String json = "{\"value\": \"</script>\"}";
        assertThatThrownBy(() -> objectMapper.readValue(json, XssCheckFieldDto.class))
                .isInstanceOf(Exception.class);
    }

    @Test
    void xssCheck_rejectsDoctypeDeclaration() {
        String json = "{\"value\": \"<!DOCTYPE html>\"}";
        assertThatThrownBy(() -> objectMapper.readValue(json, XssCheckFieldDto.class))
                .isInstanceOf(Exception.class);
    }

    @Test
    void xssCheck_rejectsTagWithLeadingWhitespace() {
        // Pattern: < followed by optional whitespace then letter — should be caught
        String json = "{\"value\": \"< script>alert()</ script>\"}";
        assertThatThrownBy(() -> objectMapper.readValue(json, XssCheckFieldDto.class))
                .isInstanceOf(Exception.class);
    }

    @Test
    void xssCheck_allowsEmptyString() throws Exception {
        String json = "{\"value\": \"\"}";
        XssCheckFieldDto result = objectMapper.readValue(json, XssCheckFieldDto.class);
        assertThat(result.value).isEmpty();
    }
}