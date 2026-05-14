package io.github.og4dev.annotation;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link AutoResponse} annotation metadata and the {@code message()} attribute
 * added in v1.5.0-RC1.
 */
class AutoResponseAnnotationTest {

    // -----------------------------------------------------------------------
    // Annotation meta-attributes
    // -----------------------------------------------------------------------

    @Test
    void autoResponse_hasRuntimeRetention() {
        Retention retention = AutoResponse.class.getAnnotation(Retention.class);
        assertThat(retention).isNotNull();
        assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
    }

    @Test
    void autoResponse_targetsTypeAndMethod() {
        Target target = AutoResponse.class.getAnnotation(Target.class);
        assertThat(target).isNotNull();
        assertThat(target.value()).contains(ElementType.TYPE, ElementType.METHOD);
    }

    @Test
    void autoResponse_isDocumented() {
        assertThat(AutoResponse.class.isAnnotationPresent(Documented.class)).isTrue();
    }

    // -----------------------------------------------------------------------
    // message() default value
    // -----------------------------------------------------------------------

    @Test
    void autoResponse_message_defaultsToSuccess() throws Exception {
        Method messageMethod = AutoResponse.class.getDeclaredMethod("message");
        Object defaultValue = messageMethod.getDefaultValue();
        assertThat(defaultValue).isEqualTo("Success");
    }

    // -----------------------------------------------------------------------
    // Applying @AutoResponse at class level
    // -----------------------------------------------------------------------

    @AutoResponse
    static class ClassLevelDefault {}

    @AutoResponse(message = "Custom class message")
    static class ClassLevelCustomMessage {}

    @Test
    void autoResponse_classLevel_hasDefaultMessage() {
        AutoResponse annotation = ClassLevelDefault.class.getAnnotation(AutoResponse.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.message()).isEqualTo("Success");
    }

    @Test
    void autoResponse_classLevel_hasCustomMessage() {
        AutoResponse annotation = ClassLevelCustomMessage.class.getAnnotation(AutoResponse.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.message()).isEqualTo("Custom class message");
    }

    // -----------------------------------------------------------------------
    // Applying @AutoResponse at method level
    // -----------------------------------------------------------------------

    static class MethodAnnotatedMethods {
        @AutoResponse
        public void defaultMessageMethod() {
            // Empty method used for reflection testing
        }

        @AutoResponse(message = "Custom method message")
        public void customMessageMethod() {
            // Empty method used for reflection testing
        }
    }

    @Test
    void autoResponse_methodLevel_hasDefaultMessage() throws Exception {
        Method method = MethodAnnotatedMethods.class.getMethod("defaultMessageMethod");
        AutoResponse annotation = method.getAnnotation(AutoResponse.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.message()).isEqualTo("Success");
    }

    @Test
    void autoResponse_methodLevel_hasCustomMessage() throws Exception {
        Method method = MethodAnnotatedMethods.class.getMethod("customMessageMethod");
        AutoResponse annotation = method.getAnnotation(AutoResponse.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.message()).isEqualTo("Custom method message");
    }

    // -----------------------------------------------------------------------
    // Annotation is accessible at runtime
    // -----------------------------------------------------------------------

    @Test
    void autoResponse_isDetectable_viaReflectionAtRuntime() throws Exception {
        Method method = MethodAnnotatedMethods.class.getMethod("customMessageMethod");
        assertThat(method.isAnnotationPresent(AutoResponse.class)).isTrue();
    }

    static class NoAnnotationClass {
        public void unannotatedMethod() {
            // Empty method used for reflection testing
        }
    }

    @Test
    void autoResponse_notPresent_onUnannotatedMethod() throws Exception {
        Method method = NoAnnotationClass.class.getMethod("unannotatedMethod");
        assertThat(method.isAnnotationPresent(AutoResponse.class)).isFalse();
        assertThat(NoAnnotationClass.class.isAnnotationPresent(AutoResponse.class)).isFalse();
    }

    @Test
    void autoResponse_messageAttributeIsEmpty_doesNotReturnNull() {
        // Edge case: message() should never be null since it has a default
        AutoResponse annotation = ClassLevelDefault.class.getAnnotation(AutoResponse.class);
        assertThat(annotation.message()).isNotNull();
    }

    // -----------------------------------------------------------------------
    // Boundary — very long message string
    // -----------------------------------------------------------------------

    @AutoResponse(message = "A very long custom message that spans many characters to verify no truncation occurs in the annotation processing pipeline")
    static class LongMessageClass {}

    @Test
    void autoResponse_preservesLongMessage_withoutTruncation() {
        String expected = "A very long custom message that spans many characters to verify no truncation occurs in the annotation processing pipeline";
        AutoResponse annotation = LongMessageClass.class.getAnnotation(AutoResponse.class);
        assertThat(annotation.message()).isEqualTo(expected);
    }

    // -----------------------------------------------------------------------
    // Boundary — special characters in message
    // -----------------------------------------------------------------------

    @AutoResponse(message = "Données récupérées avec succès")
    static class UnicodeMessageClass {}

    @Test
    void autoResponse_preservesUnicodeMessage() {
        AutoResponse annotation = UnicodeMessageClass.class.getAnnotation(AutoResponse.class);
        assertThat(annotation.message()).isEqualTo("Données récupérées avec succès");
    }
}