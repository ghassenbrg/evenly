package io.evenly.core.mock.config;

import jakarta.enterprise.util.AnnotationLiteral;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Qualifier annotation to mark mock implementations.
 * Used in combination with profile-based activation.
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MockProfile {
    /**
     * Annotation literal for programmatic usage.
     */
    class Literal extends AnnotationLiteral<MockProfile> implements MockProfile {
        public static final Literal INSTANCE = new Literal();
    }
}
