package at.yawk.yarn.compiler;

import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class ExplicitBeanReference implements BeanReference {
    private final BeanDefinition bean;
    private final boolean soft;

    @Override
    public String toString() {
        return "*{" + bean + "}s=" + soft;
    }
}
