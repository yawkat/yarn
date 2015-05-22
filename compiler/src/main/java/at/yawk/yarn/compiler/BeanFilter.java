package at.yawk.yarn.compiler;

/**
 * @author yawkat
 */
public interface BeanFilter {
    boolean accept(BeanProvider definition);
}
