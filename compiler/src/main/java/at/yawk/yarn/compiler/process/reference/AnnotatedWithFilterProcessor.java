package at.yawk.yarn.compiler.process.reference;

import at.yawk.yarn.AnnotatedWith;
import at.yawk.yarn.compiler.Compiler;
import at.yawk.yarn.compiler.LookupBeanReference;
import java.lang.annotation.Annotation;

/**
 * @author yawkat
 */
public class AnnotatedWithFilterProcessor extends AnnotationLookupBeanReferenceProcessor<AnnotatedWith> {
    @Override
    protected void process(Compiler compiler, LookupBeanReference reference, AnnotatedWith annotation) {
        reference.addFilter(definition -> {
            for (Annotation a : definition.getAnnotations()) {
                if (annotation.value().isInstance(a)) {
                    return true;
                }
            }
            return false;
        });
    }
}
