package at.yawk.yarn.compiler.process.entrypoint;

import at.yawk.yarn.compiler.Compiler;
import at.yawk.yarn.compiler.EntryPoint;
import at.yawk.yarn.compiler.tree.BeanPool;

/**
 * @author yawkat
 */
public interface EntryPointProcessor {
    void process(Compiler compiler, EntryPoint entryPoint, BeanPool tree);
}
