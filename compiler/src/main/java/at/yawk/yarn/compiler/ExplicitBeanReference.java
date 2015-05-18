package at.yawk.yarn.compiler;

import at.yawk.yarn.compiler.instruction.resolver.BeanResolver;
import at.yawk.yarn.compiler.instruction.resolver.ExactBeanResolver;
import javax.lang.model.type.TypeMirror;
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

    @Override
    public TypeMirror getType() {
        return bean.getType();
    }

    @Override
    public BeanResolver getResolver() {
        return new ExactBeanResolver(bean);
    }
}
