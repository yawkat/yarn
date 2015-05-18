package at.yawk.yarn;

import java.lang.annotation.*;

/**
 * @author yawkat
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ComponentScan.ComponentScanRepeatable.class)
public @interface ComponentScan {
    String value() default "";

    Class<?> packageClass() default NoClass.class;

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface ComponentScanRepeatable {
        ComponentScan[] value();
    }

    class NoClass {}
}
