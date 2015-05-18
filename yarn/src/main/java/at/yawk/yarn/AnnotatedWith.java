package at.yawk.yarn;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author yawkat
 */
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AnnotatedWith.AnnotatedWithRepeatable.class)
public @interface AnnotatedWith {
    Class<? extends Annotation> value();

    @Retention(RetentionPolicy.RUNTIME)
    @interface AnnotatedWithRepeatable {
        AnnotatedWith[] value();
    }
}
