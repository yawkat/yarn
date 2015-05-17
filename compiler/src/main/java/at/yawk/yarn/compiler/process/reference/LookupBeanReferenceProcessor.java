package at.yawk.yarn.compiler.process.reference;

import at.yawk.yarn.compiler.*;
import at.yawk.yarn.compiler.Compiler;

/**
 * @author yawkat
 */
public interface LookupBeanReferenceProcessor {
    void process(Compiler compiler, LookupBeanReference reference);
}
