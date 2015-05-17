package at.yawk.yarn;

import java.lang.invoke.MethodHandle;

/**
 * @author yawkat
 */
public interface BeanMethod {
    Object getBean();

    MethodHandle getMethod();
}
