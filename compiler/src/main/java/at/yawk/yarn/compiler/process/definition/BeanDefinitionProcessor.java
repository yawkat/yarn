package at.yawk.yarn.compiler.process.definition;

import at.yawk.yarn.compiler.*;
import at.yawk.yarn.compiler.Compiler;

/**
 * @author yawkat
 */
public interface BeanDefinitionProcessor {
    void process(Compiler compiler, BeanDefinition definition);
}
