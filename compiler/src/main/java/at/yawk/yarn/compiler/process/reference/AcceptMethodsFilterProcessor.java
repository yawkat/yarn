package at.yawk.yarn.compiler.process.reference;

import at.yawk.yarn.AcceptMethods;
import at.yawk.yarn.compiler.*;
import at.yawk.yarn.compiler.Compiler;
import java.lang.annotation.Annotation;

/**
 * @author yawkat
 */
public class AcceptMethodsFilterProcessor implements LookupBeanReferenceProcessor {
    @Override
    public void process(Compiler compiler, LookupBeanReference reference) {
        for (Annotation annotation : reference.getAnnotations()) {
            if (annotation instanceof AcceptMethods) {
                // we can accept methods on this ref, pass
                return;
            }
        }
        // don't accept BeanMethods because we're missing @AcceptMethods
        reference.addFilter(definition -> !(definition instanceof BeanMethod));
    }
}
