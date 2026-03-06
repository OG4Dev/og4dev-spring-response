package io.github.og4dev.config;

import io.github.og4dev.annotation.AutoTrim;
import io.github.og4dev.annotation.XssCheck;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdScalarDeserializer;

import java.lang.annotation.Annotation;

/**
 * Custom Jackson {@code String} deserializer that provides opt-in XSS validation and
 * automatic whitespace trimming at the deserialization layer.
 * <p>
 * This deserializer is registered by
 * {@link ApiResponseAutoConfiguration#strictJsonCustomizer()} and is applied to every
 * {@code String} field during JSON deserialization. Its active behavior is determined
 * contextually per field via {@link #createContextual(DeserializationContext, BeanProperty)}:
 * by default, neither trimming nor XSS validation is performed. Features are activated
 * only when the corresponding annotation is present on the field or its declaring class.
 * </p>
 *
 * <h2>Activation Rules</h2>
 * <ul>
 *   <li><b>Trimming</b> — enabled when {@link AutoTrim @AutoTrim} is present on the field
 *       or on the enclosing class.</li>
 *   <li><b>XSS Validation</b> — enabled when {@link XssCheck @XssCheck} is present on the
 *       field or on the enclosing class.</li>
 * </ul>
 *
 * <h2>Processing Order</h2>
 * <p>
 * When both features are active, trimming is applied first and XSS validation is performed
 * on the trimmed value, ensuring that whitespace-padded payloads do not bypass detection.
 * </p>
 *
 * <h2>XSS Detection Pattern</h2>
 * <p>
 * HTML and XML tags are detected using the regular expression {@code (?s).*<\s*[a-zA-Z/!].*}.
 * When a match is found the deserializer throws an {@link IllegalArgumentException} which
 * propagates as an HTTP 400 Bad Request response via the global exception handler.
 * </p>
 *
 * <h2>Null Value Handling</h2>
 * <p>
 * {@code null} values are returned unchanged — they are never converted to empty strings
 * and are not subject to trimming or XSS validation.
 * </p>
 *
 * @author Pasindu OG
 * @version 1.4.0
 * @since 1.3.0
 * @see AutoTrim
 * @see XssCheck
 * @see ApiResponseAutoConfiguration#strictJsonCustomizer()
 */
public class AdvancedStringDeserializer extends StdScalarDeserializer<String> {

    /**
     * Whether leading and trailing whitespace should be stripped from deserialized strings.
     */
    private final boolean shouldTrim;

    /**
     * Whether strings should be validated against an HTML/XSS tag detection pattern.
     */
    private final boolean shouldXssCheck;

    /**
     * Constructs a default {@code AdvancedStringDeserializer} with both trimming and
     * XSS validation disabled.
     * <p>
     * This no-arg constructor is used when registering the deserializer with the Jackson
     * module. Contextual instances with the appropriate flags are created per field by
     * {@link #createContextual(DeserializationContext, BeanProperty)}.
     * </p>
     */
    public AdvancedStringDeserializer() {
        super(String.class);
        this.shouldTrim = false;
        this.shouldXssCheck = false;
    }

    /**
     * Constructs a fully configured {@code AdvancedStringDeserializer} with explicit
     * control over trimming and XSS validation behavior.
     * <p>
     * Instances with specific flags are created by
     * {@link #createContextual(DeserializationContext, BeanProperty)} after inspecting
     * the annotations on the target field and its declaring class.
     * </p>
     *
     * @param shouldTrim     {@code true} to strip leading and trailing whitespace
     * @param shouldXssCheck {@code true} to reject strings containing HTML or XML tags
     */
    public AdvancedStringDeserializer(boolean shouldTrim, boolean shouldXssCheck) {
        super(String.class);
        this.shouldTrim = shouldTrim;
        this.shouldXssCheck = shouldXssCheck;
    }

    /**
     * Deserializes a JSON string value, applying trimming and/or XSS validation
     * according to the flags set on this instance.
     *
     * @param p    the JSON parser positioned at the current string token
     * @param ctxt the deserialization context
     * @return the processed string value, or {@code null} if the token value is {@code null}
     * @throws JacksonException     if a JSON parsing error occurs
     * @throws IllegalArgumentException if XSS validation is enabled and the value contains
     *                              HTML or XML tags
     */
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String value = p.getValueAsString();
        if (value == null) return null;
        String processedValue = shouldTrim ? value.trim() : value;
        validateXss(processedValue);
        return processedValue;
    }

    /**
     * Creates a contextual deserializer instance configured for the specific field being
     * deserialized.
     * <p>
     * This method is called once per field during Jackson's mapper initialization. It inspects
     * the annotations on the {@link BeanProperty} and its declaring class to determine whether
     * trimming ({@link AutoTrim @AutoTrim}) and/or XSS validation ({@link XssCheck @XssCheck})
     * should be enabled, and returns an appropriately configured instance.
     * </p>
     *
     * @param ct       the deserialization context
     * @param property the bean property being deserialized; {@code null} when no property
     *                 context is available, in which case this instance is returned unchanged
     * @return a contextual {@code AdvancedStringDeserializer} configured for the target field,
     *         or {@code this} if {@code property} is {@code null}
     * @throws JacksonException if a Jackson processing error occurs during context creation
     */
    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ct, BeanProperty property) throws JacksonException {
        if (property == null) {
            return this;
        }

        boolean trim = hasAnnotation(property, AutoTrim.class);
        boolean xss = hasAnnotation(property, XssCheck.class);
        return new AdvancedStringDeserializer(trim, xss);
    }

    /**
     * Validates the given string value against the XSS detection pattern when XSS
     * validation is enabled on this instance.
     * <p>
     * Uses the regular expression {@code (?s).*<\s*[a-zA-Z/!].*} in DOTALL mode to detect
     * HTML and XML tags, including opening tags, closing tags, self-closing tags, comments,
     * and DOCTYPE declarations spanning single or multiple lines.
     * </p>
     *
     * @param value the string to validate; must not be {@code null}
     * @throws IllegalArgumentException if {@code shouldXssCheck} is {@code true} and the
     *                                  value contains HTML or XML tags
     */
    private void validateXss(String value) {
        if (shouldXssCheck && value.matches("(?s).*<\\s*[a-zA-Z/!].*"))
            throw new IllegalArgumentException("Security Error: HTML tags or XSS payloads are not allowed in the request.");
    }

    /**
     * Checks whether a given annotation is present on the bean property itself or on its
     * declaring class, supporting both field-level and class-level annotation scopes.
     *
     * @param property        the bean property to inspect; must not be {@code null}
     * @param annotationClass the annotation type to look for; must not be {@code null}
     * @return {@code true} if the annotation is found on the field or its declaring class;
     *         {@code false} otherwise
     */
    private boolean hasAnnotation(BeanProperty property, Class<? extends Annotation> annotationClass) {
        if (property.getAnnotation(annotationClass) != null) return true;
        if (property.getMember() != null) {
            Class<?> declaringClass = property.getMember().getDeclaringClass();
            return declaringClass != null && declaringClass.getAnnotation(annotationClass) != null;
        }
        return false;
    }
}
