package at.yawk.yarn.compiler;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author yawkat
 */
public interface BeanProvider {
    /**
     * The base bean definition we need to depend on if we need this provider.
     */
    BeanDefinition getBaseDependency();

    List<Annotation> getAnnotations();
}
