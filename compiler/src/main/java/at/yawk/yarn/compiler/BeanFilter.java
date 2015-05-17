package at.yawk.yarn.compiler;

/**
 * @author yawkat
 */
public interface BeanFilter {
    boolean accept(BeanDefinition definition);
}
