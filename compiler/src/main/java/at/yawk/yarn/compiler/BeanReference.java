package at.yawk.yarn.compiler;

import at.yawk.yarn.compiler.instruction.resolver.BeanResolver;
import javax.lang.model.type.TypeMirror;

/**
 * @author yawkat
 */
public interface BeanReference {
    boolean isSoft();

    TypeMirror getType();

    BeanResolver getResolver();
}
