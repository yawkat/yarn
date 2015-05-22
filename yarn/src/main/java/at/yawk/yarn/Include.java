package at.yawk.yarn;

import java.lang.annotation.*;

/**
 * @author yawkat
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Include.IncludeRepeatable.class)
public @interface Include {
    Class<?> value();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface IncludeRepeatable {
        Include[] value();
    }
}
